package com.rei.elbosque.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "progreso_rei")

data class ProgresoPersistido(val estrellas: Int = 0, val stickersDesbloqueados: Int = 0)

/**
 * DataStore conserva estrellas y stickers aunque se cierre la aplicación.
 * No se escribe ningún dato fuera del dispositivo.
 */
class RecompensasRepository(private val context: Context) {
    private val estrellasKey = intPreferencesKey("estrellas")
    private val stickersKey = intPreferencesKey("stickers_desbloqueados")

    val progreso: Flow<ProgresoPersistido> = context.dataStore.data.map { preferencias ->
        ProgresoPersistido(
            estrellas = preferencias[estrellasKey] ?: 0,
            stickersDesbloqueados = preferencias[stickersKey] ?: 0
        )
    }

    suspend fun agregarEstrella(): Boolean {
        var nuevoSticker = false
        context.dataStore.edit { preferencias ->
            val nuevasEstrellas = (preferencias[estrellasKey] ?: 0) + 1
            val anteriores = preferencias[stickersKey] ?: 0
            val desbloqueados = minOf(5, nuevasEstrellas / 5)
            preferencias[estrellasKey] = nuevasEstrellas
            preferencias[stickersKey] = desbloqueados
            nuevoSticker = desbloqueados > anteriores
        }
        return nuevoSticker
    }
}
