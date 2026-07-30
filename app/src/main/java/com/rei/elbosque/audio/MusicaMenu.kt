package com.rei.elbosque.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import com.rei.elbosque.R

/** Controla únicamente la música del menú; nunca necesita Internet. */
class MusicaMenu(context: Context) {
    private val atributos = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()

    // MediaPlayer.create() devuelve null si el recurso no se puede decodificar;
    // sin esta comprobación, un .wav dañado tumbaría la app entera al iniciar.
    private val reproductor = MediaPlayer.create(
        context,
        R.raw.musica_menu_rei,
        atributos,
        0
    )?.apply {
        isLooping = true
        // Presente pero suave: la narración siempre queda claramente por delante.
        setVolume(.42f, .42f)
    }

    fun reproducir() {
        reproductor?.let { if (!it.isPlaying) it.start() }
    }

    fun pausar() {
        reproductor?.let { if (it.isPlaying) it.pause() }
    }

    fun cerrar() {
        reproductor?.release()
    }
}
