package com.rei.elbosque.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.os.SystemClock
import java.util.Locale

/** Narrador offline. Usa únicamente el motor TTS instalado en Android. */
class Narrador(context: Context) : TextToSpeech.OnInitListener {
    private var listo = false
    @Volatile private var protegerColaHasta = 0L
    @Volatile private var ultimaPeticion = ""
    @Volatile private var momentoUltimaPeticion = 0L
    private val pendientes = mutableListOf<List<String>>()
    private val motorGoogleDisponible = runCatching {
        context.packageManager.getPackageInfo("com.google.android.tts", 0)
    }.isSuccess
    // Google ofrece la mejor voz en el teléfono de Rei. En otros dispositivos se
    // utiliza el motor configurado por Android en lugar de dejar la app muda.
    private val tts = if (motorGoogleDisponible) {
        TextToSpeech(context.applicationContext, this, "com.google.android.tts")
    } else {
        TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(resultado: Int) {
        Log.i("ReiTTS", "onInit=$resultado motor=${tts.defaultEngine}")
        if (resultado == TextToSpeech.SUCCESS) {
            val vocesEspanolas = tts.voices?.filter { it.locale.language == "es" }.orEmpty()
            val vocesInstaladas = vocesEspanolas.filterNot {
                TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED in it.features
            }
            Log.i(
                "ReiTTS",
                "vocesTotales=${tts.voices?.size ?: 0} " +
                    "vocesEspanolas=${vocesEspanolas.size} instaladas=${vocesInstaladas.size}"
            )
            // Prefiere una voz local; si el motor solo ofrece otra española, la usa
            // antes que dejar la aplicación muda.
            val mejorVoz = vocesInstaladas
                ?.maxWithOrNull(
                    compareBy<android.speech.tts.Voice> {
                        if (it.isNetworkConnectionRequired) 0 else 1
                    }
                        .thenBy { it.quality }
                        .thenByDescending { it.latency }
                )
            listo = if (mejorVoz != null) {
                tts.voice = mejorVoz
                true
            } else {
                // Algunos motores no publican la lista hasta seleccionar un idioma.
                listOf(Locale("es", "MX"), Locale("es", "ES"), Locale("es", "US"))
                    .any { tts.setLanguage(it) >= TextToSpeech.LANG_AVAILABLE }
            }
            // Ritmo calmado y tono joven, sin exagerarlo para evitar efecto robótico.
            tts.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            tts.setSpeechRate(0.78f)
            tts.setPitch(1.08f)
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    Log.i("ReiTTS", "Comenzó a hablar: $utteranceId")
                }

                override fun onDone(utteranceId: String?) {
                    Log.i("ReiTTS", "Terminó de hablar: $utteranceId")
                }

                @Deprecated("API antigua requerida por Android")
                override fun onError(utteranceId: String?) {
                    Log.e("ReiTTS", "Error al hablar: $utteranceId")
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    Log.e("ReiTTS", "Error $errorCode al hablar: $utteranceId")
                }
            })
            if (listo) {
                Log.i("ReiTTS", "Narrador listo con voz=${tts.voice?.name}")
                val guardadas = synchronized(pendientes) {
                    pendientes.toList().also { pendientes.clear() }
                }
                guardadas.forEach(::reproducir)
            } else {
                Log.e("ReiTTS", "No hay datos de voz española instalados")
            }
        } else {
            Log.e("ReiTTS", "No se pudo iniciar Google TTS")
        }
    }

    fun decir(texto: String) {
        decirSecuencia(texto)
    }

    /**
     * Protege una felicitación para que la consigna de la ronda siguiente se
     * encole y no la corte. Es especialmente importante con voces lentas.
     */
    fun felicitar(vararg frases: String) {
        protegerColaHasta = SystemClock.elapsedRealtime() + 3_200L
        decirSecuencia(*frases)
    }

    /** Pronuncia varias frases en orden, sin que una corte a la anterior. */
    fun decirSecuencia(vararg frases: String) {
        val limpias = frases.filter { it.isNotBlank() }
        if (limpias.isEmpty()) return
        val ahora = SystemClock.elapsedRealtime()
        val firma = limpias.joinToString("\u0000")
        // Un toddler puede tocar tres veces en una fracción de segundo. Un mismo
        // mensaje se acepta una sola vez para evitar eco y colas interminables.
        synchronized(this) {
            if (firma == ultimaPeticion && ahora - momentoUltimaPeticion < 850L) return
            ultimaPeticion = firma
            momentoUltimaPeticion = ahora
        }
        if (!listo) {
            synchronized(pendientes) {
                pendientes += limpias
                // Conserva solo las consignas más recientes si se toca muy rápido.
                while (pendientes.size > 6) pendientes.removeAt(0)
            }
            return
        }
        reproducir(limpias)
    }

    /** Vacía cualquier frase pendiente al abandonar una pantalla o juego. */
    fun detener() {
        protegerColaHasta = 0L
        synchronized(pendientes) { pendientes.clear() }
        if (listo) tts.stop()
    }

    private fun reproducir(frases: List<String>) {
        val conservarFelicitacion = SystemClock.elapsedRealtime() < protegerColaHasta
        frases.forEachIndexed { indice, frase ->
            tts.speak(
                frase,
                if (indice == 0 && !conservarFelicitacion) {
                    TextToSpeech.QUEUE_FLUSH
                } else {
                    TextToSpeech.QUEUE_ADD
                },
                null,
                "rei_${System.nanoTime()}_$indice"
            )
        }
    }

    fun cerrar() {
        tts.stop()
        tts.shutdown()
    }
}

object Sonidos {
    /** Acorde ascendente brillante, parecido a tres estrellitas mágicas. */
    fun estrella() = reproducirMelodia(
        notas = listOf(1046.5 to 0.00, 1318.5 to 0.12, 1568.0 to 0.24),
        duracion = .72,
        volumen = .28
    )

    /** Dos notas descendentes y suaves: corrige sin asustar ni castigar. */
    fun errorSuave() = reproducirMelodia(
        notas = listOf(440.0 to 0.00, 349.2 to 0.18),
        duracion = .55,
        volumen = .16
    )

    private val frecuenciaMuestreo = 44_100

    private fun reproducirMelodia(
        notas: List<Pair<Double, Double>>,
        duracion: Double,
        volumen: Double
    ) {
        val total = (frecuenciaMuestreo * duracion).toInt()
        val muestras = ShortArray(total)
        for (i in 0 until total) {
            val tiempo = i.toDouble() / frecuenciaMuestreo
            var mezcla = 0.0
            notas.forEach { (frecuencia, inicio) ->
                if (tiempo >= inicio) {
                    val edad = tiempo - inicio
                    val envolvente = kotlin.math.exp(-edad * 5.2)
                    mezcla += kotlin.math.sin(2.0 * Math.PI * frecuencia * edad) * envolvente
                }
            }
            muestras[i] = aMuestra(mezcla * volumen)
        }
        reproducirMuestras(muestras, duracion)
    }

    /** Chasquido breve y agudo: toque suave confirmado, sin ser una burbuja. */
    fun pop() = reproducirMelodia(
        notas = listOf(1600.0 to 0.00, 2200.0 to 0.03),
        duracion = .14,
        volumen = .20
    )

    /**
     * El "plop" real de una burbuja de jabón: un golpe de aire breve, un barrido de
     * tono descendente rápido y una chispita aguda al final para que suene dulce
     * y no solo un golpe seco.
     */
    fun burbuja() {
        val duracion = .22
        val total = (frecuenciaMuestreo * duracion).toInt()
        val muestras = ShortArray(total)
        val ruido = kotlin.random.Random(System.nanoTime())
        for (i in 0 until total) {
            val t = i.toDouble() / frecuenciaMuestreo
            var mezcla = 0.0

            // Golpe de aire: un chasquido de ruido brevísimo, como el "clic" del estallido.
            if (t < 0.012) {
                mezcla += ruido.nextDouble(-1.0, 1.0) * kotlin.math.exp(-t * 220.0) * .45
            }

            // El "plop": la frecuencia cae rápido de agudo a grave, como el aire escapando.
            val progresoPlop = (t / 0.09).coerceAtMost(1.0)
            val frecuenciaPlop = 1500.0 - progresoPlop * 950.0
            mezcla += kotlin.math.sin(2.0 * Math.PI * frecuenciaPlop * t) *
                kotlin.math.exp(-t * 24.0) * .8

            // Una chispita cristalina justo después, para que suene bonito y no solo seco.
            if (t >= 0.05) {
                val t2 = t - 0.05
                val envolvente = kotlin.math.exp(-t2 * 13.0)
                mezcla += kotlin.math.sin(2.0 * Math.PI * 2100.0 * t2) * envolvente * .30
                mezcla += kotlin.math.sin(2.0 * Math.PI * 2800.0 * t2) * envolvente * .18
            }

            muestras[i] = aMuestra(mezcla * .34)
        }
        reproducirMuestras(muestras, duracion)
    }

    private fun aMuestra(valor: Double): Short = (valor * Short.MAX_VALUE)
        .coerceIn(Short.MIN_VALUE.toDouble(), Short.MAX_VALUE.toDouble())
        .toInt()
        .toShort()

    private fun reproducirMuestras(muestras: ShortArray, duracion: Double) {
        Thread {
            val pista = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(frecuenciaMuestreo)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(muestras.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
            pista.write(muestras, 0, muestras.size)
            pista.play()
            Thread.sleep((duracion * 1_000).toLong() + 80)
            pista.release()
        }.start()
    }

    /** Pequeña melodía sin archivos ni permisos: tres tonos locales. */
    fun celebracion() = reproducirMelodia(
        notas = listOf(
            784.0 to 0.00,
            987.8 to 0.14,
            1174.7 to 0.28,
            1568.0 to 0.44
        ),
        duracion = .95,
        volumen = .24
    )
}
