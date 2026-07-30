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

    private val reproductor = MediaPlayer.create(
        context,
        R.raw.musica_menu_rei,
        atributos,
        0
    ).apply {
        isLooping = true
        // Presente pero suave: la narración siempre queda claramente por delante.
        setVolume(.42f, .42f)
    }

    fun reproducir() {
        if (!reproductor.isPlaying) reproductor.start()
    }

    fun pausar() {
        if (reproductor.isPlaying) reproductor.pause()
    }

    fun cerrar() {
        reproductor.release()
    }
}
