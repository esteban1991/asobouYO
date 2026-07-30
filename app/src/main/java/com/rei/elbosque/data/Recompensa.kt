package com.rei.elbosque.data

import androidx.annotation.DrawableRes

/** Sticker coleccionable del álbum de Rei. */
data class Recompensa(
    val nombre: String,
    @DrawableRes val icono: Int,
    val desbloqueado: Boolean
)
