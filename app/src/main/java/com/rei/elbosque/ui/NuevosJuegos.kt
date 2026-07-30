package com.rei.elbosque.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rei.elbosque.R
import com.rei.elbosque.audio.Narrador
import com.rei.elbosque.audio.Sonidos
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.random.Random

data class OpcionVisual(val nombre: String, @DrawableRes val icono: Int)

class BuscaObjetoViewModel(private val saved: SavedStateHandle) : ViewModel() {
    val objetos = listOf(
        OpcionVisual("manzana", R.drawable.ic_manzana), OpcionVisual("pelota", R.drawable.ic_pelota),
        OpcionVisual("zapato", R.drawable.ic_zapato), OpcionVisual("flor", R.drawable.ic_flor),
        OpcionVisual("libro", R.drawable.ic_libro), OpcionVisual("paraguas", R.drawable.ic_paraguas),
        OpcionVisual("osito", R.drawable.ic_osito), OpcionVisual("gorra", R.drawable.ic_gorra)
    )
    private val _indice = MutableStateFlow(saved["busca_indice"] ?: Random.nextInt(objetos.size))
    val indice = _indice
    val objetivo get() = objetos[_indice.value]
    fun opciones() = (objetos.filterNot { it == objetivo }.shuffled().take(3) + objetivo).shuffled()
    fun comprobar(opcion: OpcionVisual): Boolean {
        if (opcion != objetivo) return false
        var nuevo: Int
        do nuevo = Random.nextInt(objetos.size) while (nuevo == _indice.value)
        _indice.value = nuevo; saved["busca_indice"] = nuevo
        return true
    }
}

data class ComidaAnimal(
    val animal: OpcionVisual,
    val comida: OpcionVisual
)

class AlimentaAnimalViewModel(private val saved: SavedStateHandle) : ViewModel() {
    private val comidas = listOf(
        OpcionVisual("pez", R.drawable.ic_pez),
        OpcionVisual("manzana", R.drawable.ic_manzana),
        OpcionVisual("plátano", R.drawable.ic_platano)
    )
    val rondas = listOf(
        ComidaAnimal(OpcionVisual("gato", R.drawable.ic_gato), comidas[0]),
        ComidaAnimal(OpcionVisual("oso", R.drawable.ic_oso), comidas[1]),
        ComidaAnimal(OpcionVisual("pájaro", R.drawable.ic_pajaro), comidas[2])
    )
    private val _indice = MutableStateFlow(saved["alimenta_indice"] ?: Random.nextInt(rondas.size))
    val indice = _indice
    val ronda get() = rondas[_indice.value]
    fun opciones() = comidas.shuffled()
    fun comprobar(opcion: OpcionVisual): Boolean {
        if (opcion != ronda.comida) return false
        val nuevo = (_indice.value + 1) % rondas.size
        _indice.value = nuevo; saved["alimenta_indice"] = nuevo
        return true
    }
}

@Composable
private fun CabeceraNueva(titulo: String, volver: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            onClick = volver,
            modifier = Modifier.size(60.dp).background(Color.White.copy(.85f), CircleShape)
        ) { Text("←", fontSize = 40.sp, fontWeight = FontWeight.Bold) }
        Text(
            titulo, Modifier.weight(1f), fontSize = 31.sp, fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center, color = Color(0xFF3D5360)
        )
        Spacer(Modifier.size(60.dp))
    }
}

@Composable
private fun OpcionesGrandes(
    opciones: List<OpcionVisual>,
    modifier: Modifier = Modifier,
    alTocar: (OpcionVisual) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 165.dp),
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(opciones, key = { it.nombre }) { opcion ->
            Card(
                onClick = { alTocar(opcion) },
                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                shape = RoundedCornerShape(42.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(.88f)),
                elevation = CardDefaults.cardElevation(7.dp)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Image(painterResource(opcion.icono), opcion.nombre, Modifier.size(135.dp))
                }
            }
        }
    }
}

@Composable
fun BuscaObjetoScreen(vm: BuscaObjetoViewModel, narrador: Narrador, premiar: () -> Unit, volver: () -> Unit) {
    val indice by vm.indice.collectAsStateWithLifecycle()
    val objetivo = vm.objetivo
    val opciones = remember(indice) { vm.opciones() }
    LaunchedEffect(indice) { delay(550); narrador.decir("Busca ${objetivo.nombre}") }
    Column(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFFFF9E6), Color(0xFFE0F7FA))))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CabeceraNueva("Busca y encuentra", volver)
        Text("👀", fontSize = 64.sp)
        OpcionesGrandes(opciones, Modifier.weight(1f)) { opcion ->
            narrador.decir(opcion.nombre)
            if (vm.comprobar(opcion)) {
                Sonidos.estrella(); premiar()
                narrador.felicitar(opcion.nombre, "¡Lo encontraste, Rei!")
            } else {
                Sonidos.errorSuave(); narrador.decir("Oh, no. Busca ${objetivo.nombre}")
            }
        }
    }
}

@Composable
fun AlimentaAnimalScreen(vm: AlimentaAnimalViewModel, narrador: Narrador, premiar: () -> Unit, volver: () -> Unit) {
    val indice by vm.indice.collectAsStateWithLifecycle()
    val ronda = vm.ronda
    val opciones = remember(indice) { vm.opciones() }
    LaunchedEffect(indice) {
        delay(550); narrador.decir("Dale ${ronda.comida.nombre} al ${ronda.animal.nombre}")
    }
    Column(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFFFF9E6), Color(0xFFE0F7FA))))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CabeceraNueva("Alimenta al animal", volver)
        Image(painterResource(ronda.animal.icono), ronda.animal.nombre, Modifier.size(190.dp))
        OpcionesGrandes(opciones, Modifier.weight(1f)) { opcion ->
            narrador.decir(opcion.nombre)
            if (vm.comprobar(opcion)) {
                Sonidos.estrella(); premiar()
                narrador.felicitar("¡Ñam, ñam!", "¡Muy bien, Rei!")
            } else {
                Sonidos.errorSuave(); narrador.decir("Oh, no. Quiere ${ronda.comida.nombre}")
            }
        }
    }
}
