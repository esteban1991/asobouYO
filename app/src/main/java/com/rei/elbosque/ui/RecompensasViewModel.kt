package com.rei.elbosque.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rei.elbosque.R
import com.rei.elbosque.data.Recompensa
import com.rei.elbosque.data.RecompensasRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecompensasViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = RecompensasRepository(application)

    val progreso = repository.progreso.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        com.rei.elbosque.data.ProgresoPersistido()
    )

    val recompensas = progreso.map { estado ->
        val base = listOf(
            "Sol" to R.drawable.ic_sol,
            "Nube" to R.drawable.ic_nube,
            "Flor" to R.drawable.ic_flor,
            "Arcoíris" to R.drawable.ic_arcoiris,
            "Oso" to R.drawable.ic_oso
        )
        base.mapIndexed { indice, item ->
            Recompensa(item.first, item.second, indice < estado.stickersDesbloqueados)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Evento efímero: la UI muestra confeti una sola vez por desbloqueo. */
    val stickerDesbloqueado = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    fun premiarAcierto() {
        viewModelScope.launch {
            if (repository.agregarEstrella()) stickerDesbloqueado.emit(Unit)
        }
    }
}
