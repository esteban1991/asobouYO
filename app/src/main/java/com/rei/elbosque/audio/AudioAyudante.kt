package com.rei.elbosque.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

/** Narrador offline. Usa únicamente el motor TTS instalado en Android. */
class Narrador(context: Context) : TextToSpeech.OnInitListener {
    private var listo = false
    private val pendientes = mutableListOf<List<String>>()
    // El teléfono de prueba tiene Google TTS instalado, pero ningún motor por defecto
    // configurado. Elegirlo explícitamente evita que los toques queden en silencio.
    private val tts = TextToSpeech(
        context.applicationContext,
        this,
        "com.google.android.tts"
    )

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

    /** Pronuncia varias frases en orden, sin que una corte a la anterior. */
    fun decirSecuencia(vararg frases: String) {
        val limpias = frases.filter { it.isNotBlank() }
        if (limpias.isEmpty()) return
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

    private fun reproducir(frases: List<String>) {
        frases.forEachIndexed { indice, frase ->
            tts.speak(
                frase,
                if (indice == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD,
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

    private fun reproducirMelodia(
        notas: List<Pair<Double, Double>>,
        duracion: Double,
        volumen: Double
    ) {
        Thread {
            val frecuenciaMuestreo = 44_100
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
                muestras[i] = (mezcla * volumen * Short.MAX_VALUE)
                    .coerceIn(Short.MIN_VALUE.toDouble(), Short.MAX_VALUE.toDouble())
                    .toInt()
                    .toShort()
            }
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

    /** Chasquido breve y agudo: burbuja reventando o toque suave confirmado. */
    fun pop() = reproducirMelodia(
        notas = listOf(1600.0 to 0.00, 2200.0 to 0.03),
        duracion = .14,
        volumen = .20
    )

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
