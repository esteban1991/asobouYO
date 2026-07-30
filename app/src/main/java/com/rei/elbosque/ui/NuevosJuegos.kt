package com.rei.elbosque.ui

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
import kotlinx.coroutines.launch
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
        return opcion == ronda.comida
    }
    fun avanzar() {
        val nuevo = (_indice.value + 1) % rondas.size
        _indice.value = nuevo; saved["alimenta_indice"] = nuevo
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
    habilitado:Boolean = true,
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
                enabled = habilitado,
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
private fun LagrimaAnimal(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width; val h = size.height
        val gota = Path().apply {
            moveTo(w * .5f, 0f)
            cubicTo(w * .95f, h * .55f, w * .82f, h, w * .5f, h)
            cubicTo(w * .18f, h, w * .05f, h * .55f, w * .5f, 0f)
            close()
        }
        drawPath(gota, Color(0xFF64B5F6))
        drawCircle(
            Color.White.copy(.5f), radius = w * .14f,
            center = androidx.compose.ui.geometry.Offset(w * .36f, h * .60f)
        )
    }
}

@Composable
private fun BocaAnimal(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        drawOval(Color(0xFF7A2E22))
        drawOval(
            Color(0xFFC1584B),
            topLeft = androidx.compose.ui.geometry.Offset(size.width * .18f, size.height * .12f),
            size = androidx.compose.ui.geometry.Size(size.width * .64f, size.height * .58f)
        )
    }
}

/** Dónde queda la boca y los ojos en cada foto, mirado a mano una por una. */
private data class RasgosCara(
    val bocaX: Float, val bocaY: Float, val bocaAncho: Float, val bocaAlto: Float,
    val ojo1X: Float, val ojo1Y: Float, val ojo2X: Float, val ojo2Y: Float
)

private val rasgosPorAnimal = mapOf(
    "gato" to RasgosCara(.47f, .350f, .16f, .10f, .38f, .29f, .58f, .28f),
    "oso" to RasgosCara(.49f, .430f, .15f, .10f, .40f, .31f, .59f, .30f),
    "pájaro" to RasgosCara(.47f, .450f, .22f, .12f, .40f, .37f, .48f, .39f)
)

@Composable
fun AlimentaAnimalScreen(vm: AlimentaAnimalViewModel, narrador: Narrador, premiar: () -> Unit, volver: () -> Unit) {
    val indice by vm.indice.collectAsStateWithLifecycle()
    val ronda = vm.ronda
    val opciones = remember(indice) { vm.opciones() }
    val scope = rememberCoroutineScope()
    val rasgos = rasgosPorAnimal[ronda.animal.nombre]
        ?: RasgosCara(.47f, .40f, .16f, .10f, .40f, .32f, .58f, .32f)
    val lado = 190.dp

    val bocaAbierta = remember { Animatable(0f) }
    val comidaEscala = remember { Animatable(0f) }
    var comidaVolando by remember { mutableStateOf<OpcionVisual?>(null) }
    val inclinacion = remember { Animatable(0f) }
    val lagrimaCaida = remember { Animatable(0f) }
    var triste by remember { mutableStateOf(false) }
    var bloqueado by remember { mutableStateOf(false) }

    LaunchedEffect(indice) {
        delay(550); narrador.decir("Dale ${ronda.comida.nombre} al ${ronda.animal.nombre}")
    }
    Column(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFFFF9E6), Color(0xFFE0F7FA))))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CabeceraNueva("Alimenta al animal", volver)
        Box(Modifier.size(lado)) {
            AnimalAnimado(
                ronda.animal.icono, ronda.animal.nombre,
                Modifier.fillMaxSize().rotate(inclinacion.value)
            )
            if (bocaAbierta.value > .03f) {
                val anchoBoca = lado * rasgos.bocaAncho
                val altoBoca = lado * rasgos.bocaAlto * bocaAbierta.value
                BocaAnimal(
                    Modifier
                        .size(width = anchoBoca, height = altoBoca)
                        .offset(x = lado * rasgos.bocaX - anchoBoca / 2, y = lado * rasgos.bocaY - altoBoca / 2)
                )
            }
            comidaVolando?.let { comida ->
                val tamanoComida = 60.dp * comidaEscala.value
                Image(
                    painterResource(comida.icono), null,
                    modifier = Modifier
                        .size(tamanoComida)
                        .offset(
                            x = lado * rasgos.bocaX - tamanoComida / 2,
                            y = lado * rasgos.bocaY - tamanoComida / 2
                        )
                )
            }
            if (triste) {
                val tamanoLagrima = 24.dp * lagrimaCaida.value.coerceIn(0f, 1f)
                Box(
                    Modifier
                        .size(tamanoLagrima)
                        .offset(
                            x = lado * rasgos.ojo1X - tamanoLagrima / 2,
                            y = lado * rasgos.ojo1Y + 34.dp * lagrimaCaida.value
                        )
                ) { LagrimaAnimal(Modifier.fillMaxSize()) }
                Box(
                    Modifier
                        .size(tamanoLagrima * .85f)
                        .offset(
                            x = lado * rasgos.ojo2X - tamanoLagrima * .425f,
                            y = lado * rasgos.ojo2Y + 40.dp * lagrimaCaida.value
                        )
                ) { LagrimaAnimal(Modifier.fillMaxSize()) }
            }
        }
        OpcionesGrandes(opciones,Modifier.weight(1f),habilitado=!bloqueado) { opcion ->
            narrador.decir(opcion.nombre)
            if (vm.comprobar(opcion)) {
                bloqueado=true
                comidaVolando = opcion
                scope.launch {
                    comidaEscala.snapTo(1f)
                    bocaAbierta.animateTo(1f, tween(180))
                    comidaEscala.animateTo(0f, tween(320, easing = FastOutSlowInEasing))
                    comidaVolando = null
                    // Mastica un ratito: la boca se abre y cierra varias veces.
                    repeat(3) {
                        bocaAbierta.animateTo(.25f, tween(160))
                        bocaAbierta.animateTo(1f, tween(180))
                    }
                    bocaAbierta.animateTo(0f, tween(220))
                    // Solo ahora, cuando terminó de comer, cambia la ronda.
                    Sonidos.estrella()
                    premiar()
                    narrador.felicitar("¡Ñam, ñam!", "¡Muy bien, Rei!")
                    delay(650)
                    vm.avanzar()
                    bloqueado=false
                }
            } else {
                bloqueado=true
                Sonidos.errorSuave(); narrador.decir("Oh, no. Quiere ${ronda.comida.nombre}")
                scope.launch {
                    triste = true
                    lagrimaCaida.snapTo(0f)
                    inclinacion.animateTo(-7f, tween(220))
                    lagrimaCaida.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
                    inclinacion.animateTo(5f, tween(280))
                    inclinacion.animateTo(-4f, tween(280))
                    inclinacion.animateTo(0f, tween(240))
                    delay(500)
                    triste = false
                    bloqueado=false
                }
            }
        }
    }
}
