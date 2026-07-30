@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.rei.elbosque.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rei.elbosque.R
import com.rei.elbosque.audio.Narrador
import com.rei.elbosque.audio.Sonidos
import com.rei.elbosque.data.Recompensa
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val Fondo1 = Color(0xFFFFF9E6)
private val Fondo2 = Color(0xFFE0F7FA)
private val Menta = Color(0xFF9BE3C5)
private val Rosa = Color(0xFFFFB7CE)
private val Lila = Color(0xFFCAB8F5)
private val Melon = Color(0xFFFFC28F)
private val Tinta = Color(0xFF3D5360)
private val Oro = Color(0xFFFFC83D)

@Composable
private fun Fondo(content: @Composable () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Fondo1, Fondo2))
            )
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) { content() }
}

@Composable
private fun Cabecera(titulo: String, onVolver: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            onClick = onVolver,
            modifier = Modifier.size(60.dp).background(Color.White.copy(.8f), CircleShape)
        ) { Text("←", fontSize = 40.sp, color = Tinta, fontWeight = FontWeight.Bold) }
        Text(
            titulo,
            Modifier.weight(1f),
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Tinta,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.width(60.dp))
    }
}

/**
 * Movimiento ambiental muy suave para el menú. No recibe gestos, por lo que nunca
 * bloquea los botones: el sol respira y algunos pétalos de sakura cruzan la escena.
 */
@Composable
private fun FondoInicioAnimado() {
    val movimiento = rememberInfiniteTransition(label = "ambiente del menú")
    val fase by movimiento.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(14_000, easing = LinearEasing)
        ),
        label = "caída de pétalos"
    )
    val respiracion by movimiento.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2_400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "brillo del sol"
    )

    Canvas(Modifier.fillMaxSize()) {
        // El sol está dibujado en la esquina superior derecha del fondo.
        val centroSol = Offset(size.width * .885f, size.height * .073f)
        val radio = size.minDimension * (.092f + respiracion * .006f)
        drawCircle(
            color = Color(0xFFFFD85A).copy(alpha = .10f + respiracion * .07f),
            radius = radio,
            center = centroSol
        )
        repeat(8) { rayo ->
            val angulo = (rayo * PI / 4.0 + fase * .24).toFloat()
            val inicio = radio * 1.08f
            val fin = radio * (1.23f + respiracion * .05f)
            drawLine(
                color = Color(0xFFFFC83D).copy(alpha = .18f),
                start = centroSol + Offset(cos(angulo) * inicio, sin(angulo) * inicio),
                end = centroSol + Offset(cos(angulo) * fin, sin(angulo) * fin),
                strokeWidth = 5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Pocos pétalos, espaciados y con recorridos diferentes para no distraer.
        val comienzos = floatArrayOf(.02f, .17f, .31f, .48f, .66f, .82f)
        comienzos.forEachIndexed { indice, comienzo ->
            val avance = (fase + 1f - comienzo) % 1f
            val xBase = size.width * (.08f + indice * .17f)
            val balanceo = sin((avance * 4f + indice) * PI.toFloat()) * size.width * .055f
            val x = xBase + balanceo
            val y = -40.dp.toPx() + avance * (size.height + 90.dp.toPx())
            val ancho = (13 + indice % 3 * 3).dp.toPx()
            val alto = ancho * .62f
            val petalo = Path().apply {
                moveTo(x, y)
                quadraticTo(x + ancho, y - alto, x + ancho * .72f, y + alto)
                quadraticTo(x, y + alto * 1.35f, x, y)
            }
            drawPath(
                petalo,
                color = if (indice % 2 == 0) Color(0xFFFFAFC5) else Color(0xFFFFD4DF),
                alpha = .62f
            )
        }
    }
}

@Composable
fun InicioScreen(
    estrellas: Int,
    narrador: Narrador,
    onAbrir: (String) -> Unit
) {
    LaunchedEffect(Unit) {
        delay(700)
        narrador.decir("¡Hola Rei! Elige un juego")
    }
    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.bg_bosque_rei),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        FondoInicioAnimado()
        // Velo mínimo: conserva la ilustración y mejora el contraste de los controles.
        Box(Modifier.fillMaxSize().background(Color.White.copy(alpha = .10f)))
        Column(
            Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("¡Hola Rei!", fontSize = 50.sp, fontWeight = FontWeight.ExtraBold, color = Tinta)
                val brillo = rememberInfiniteTransition(label = "brillo")
                val escalaEstrella by brillo.animateFloat(
                    initialValue = .94f,
                    targetValue = 1.10f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(650),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "escala estrella"
                )
                Row(
                    Modifier.scale(escalaEstrella),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painterResource(R.drawable.ic_estrella),
                        contentDescription = "Estrellas",
                        modifier = Modifier.size(48.dp)
                    )
                    Text("$estrellas", fontSize = 34.sp, color = Tinta)
                }
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item { BotonMenu(R.drawable.ic_formas, "Formas", Menta) {
                    narrador.decir("Formas"); onAbrir("formas")
                } }
                item { BotonMenu(R.drawable.ic_estrellas, "Números", Rosa) {
                    narrador.decir("Números"); onAbrir("numeros")
                } }
                item { BotonMenu(R.drawable.ic_oso, "Animales", Lila) {
                    narrador.decir("Animales"); onAbrir("animales")
                } }
                item { BotonMenu(R.drawable.ic_lapiz, "Trazo", Melon) {
                    narrador.decir("Trazo"); onAbrir("trazo")
                } }
                item { BotonMenu(R.drawable.ic_bus, "Bus", Color(0xFFFFD66B)) {
                    narrador.decir("Autobús"); onAbrir("bus")
                } }
                item { BotonMenu(R.drawable.ic_gato, "Sombras", Color(0xFFB8D8F5)) {
                    narrador.decir("Sombras"); onAbrir("sombras")
                } }
                item { BotonMenu(R.drawable.ic_arcoiris, "Colores", Menta) {
                    narrador.decir("Colores"); onAbrir("colores")
                } }
                item { BotonMenu(R.drawable.ic_perro, "Sonidos", Rosa) {
                    narrador.decir("Sonidos de animales"); onAbrir("sonidos_animales")
                } }
                item { BotonMenu(R.drawable.ic_pelota, "Burbujas", Lila) {
                    narrador.decir("Burbujas"); onAbrir("burbujas")
                } }
                item { BotonMenu(R.drawable.ic_osito, "Tamaños", Melon) {
                    narrador.decir("Grande y pequeño"); onAbrir("grande_pequeno")
                } }
                item { BotonMenu(R.drawable.ic_manzana, "Cestas", Color(0xFFFFD66B)) {
                    narrador.decir("A cada cesta su color"); onAbrir("clasificar_color")
                } }
                item { BotonMenu(R.drawable.ic_platano, "Rompecabezas", Color(0xFFB8D8F5)) {
                    narrador.decir("Rompecabezas"); onAbrir("puzzle")
                } }
                item { BotonMenu(R.drawable.ic_pajaro, "¿Quién dice?", Menta) {
                    narrador.decir("¿Quién dice esto?"); onAbrir("quien_dice_esto")
                } }
                item { BotonMenu(R.drawable.ic_gorra, "Vestir a Rei", Rosa) {
                    narrador.decir("Vestir a Rei"); onAbrir("vestir_rei")
                } }
                item { BotonMenu(R.drawable.ic_flor, "Emociones", Lila) {
                    narrador.decir("Emociones"); onAbrir("emociones")
                } }
                item { BotonMenu(R.drawable.ic_estrella, "Contar", Melon) {
                    narrador.decir("Contar hasta tres"); onAbrir("contar_hasta_3")
                } }
                item { BotonMenu(R.drawable.ic_sol, "Mi rutina", Color(0xFFFFD66B)) {
                    narrador.decir("Mi rutina"); onAbrir("rutina_diaria")
                } }
            }
            Button(
                onClick = { narrador.decir("Álbum"); onAbrir("album") },
                modifier = Modifier.fillMaxWidth(.82f).height(78.dp).padding(bottom = 6.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Oro),
                elevation = ButtonDefaults.buttonElevation(7.dp)
            ) {
                Image(
                    painterResource(R.drawable.ic_album),
                    contentDescription = null,
                    modifier = Modifier.size(58.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Álbum", fontSize = 32.sp, color = Tinta, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun BotonMenu(
    @androidx.annotation.DrawableRes icono: Int,
    texto: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().aspectRatio(1f),
        shape = CircleShape,
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painterResource(icono),
                contentDescription = texto,
                modifier = Modifier.size(58.dp)
            )
            Text(
                texto,
                fontSize = if (texto.length > 10) 12.sp else 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Tinta,
                maxLines = 1,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(.88f)
            )
        }
    }
}

@Composable
fun FormasScreen(
    vm: FormasViewModel,
    narrador: Narrador,
    premiar: () -> Unit,
    onVolver: () -> Unit
) {
    val forma by vm.forma.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val escala = remember { Animatable(1f) }
    val giro = remember { Animatable(0f) }

    Fondo {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Cabecera("Formas en el Bosque", onVolver)
            Text(forma.titulo, fontSize = 42.sp, fontWeight = FontWeight.ExtraBold, color = Tinta)
            Figura(forma, Modifier.size(190.dp).scale(escala.value).rotate(giro.value), Lila)
            Row(
                Modifier.fillMaxWidth().height(150.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Forma.entries.forEachIndexed { i, opcion ->
                    Button(
                        onClick = {
                            val acierto = vm.comprobar(opcion)
                            scope.launch {
                                if (acierto) {
                                    Sonidos.estrella(); premiar()
                                    narrador.decir("¡Yupi! ¡Muy bien, Rei!")
                                    escala.animateTo(1.28f, tween(180))
                                    escala.animateTo(1f, tween(260, easing = FastOutSlowInEasing))
                                } else {
                                    Sonidos.errorSuave()
                                    narrador.decir("Oh, no. Intenta otra vez")
                                    listOf(-7f, 7f, -5f, 5f, 0f).forEach {
                                        giro.animateTo(it, tween(60))
                                    }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = listOf(Menta, Rosa, Melon)[i]
                        ),
                        elevation = ButtonDefaults.buttonElevation(7.dp)
                    ) { Figura(opcion, Modifier.size(82.dp), Color.White) }
                }
            }
        }
    }
}

@Composable
private fun Figura(forma: Forma, modifier: Modifier, color: Color) {
    Canvas(modifier) {
        when (forma) {
            Forma.CIRCULO -> drawCircle(color)
            Forma.CUADRADO -> drawRoundRect(color, cornerRadius = androidx.compose.ui.geometry.CornerRadius(18f))
            Forma.TRIANGULO -> {
                val p = Path().apply {
                    moveTo(size.width / 2, 0f)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(p, color)
            }
        }
    }
}

@Composable
fun NumerosScreen(
    vm: NumerosViewModel,
    narrador: Narrador,
    premiar: () -> Unit,
    onVolver: () -> Unit
) {
    val cantidad by vm.cantidad.collectAsStateWithLifecycle()
    var preguntaId by rememberSaveable { mutableStateOf(0) }
    LaunchedEffect(cantidad, preguntaId) {
        delay(500)
        narrador.decir("¿Cuántas estrellas hay?")
    }
    Fondo {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Cabecera("Contando Estrellas", onVolver)
            Text("¿Cuántas estrellas hay?", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Tinta)
            Row(
                Modifier.fillMaxWidth().height(280.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(cantidad) {
                    Image(
                        painterResource(R.drawable.ic_estrella),
                        contentDescription = null,
                        modifier = Modifier.size(if (cantidad <= 3) 76.dp else 58.dp)
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                (1..5).forEachIndexed { i, numero ->
                    Button(
                        onClick = {
                            if (vm.comprobar(numero)) {
                                Sonidos.estrella(); premiar()
                                narrador.decir("¡Yupi! ¡Muy bien, Rei! $numero")
                            } else {
                                Sonidos.errorSuave()
                                narrador.decir("Oh, no. Intenta otra vez")
                                preguntaId++
                            }
                        },
                        modifier = Modifier.weight(1f).height(92.dp),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = listOf(Menta, Rosa, Lila, Melon, Color(0xFFAED8FF))[i]
                        )
                    ) { Text("$numero", fontSize = 42.sp, fontWeight = FontWeight.ExtraBold, color = Tinta) }
                }
            }
        }
    }
}

@Composable
fun AnimalesScreen(
    vm: AnimalesViewModel,
    narrador: Narrador,
    premiar: () -> Unit,
    onVolver: () -> Unit
) {
    val indice by vm.indice.collectAsStateWithLifecycle()
    val actual = vm.animales[indice]
    val opciones = remember(indice) { vm.opciones() }
    LaunchedEffect(indice) {
        delay(450)
        narrador.decir("¿Dónde está el ${actual.nombre}?")
    }
    Fondo {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Cabecera("Animales del Bosque", onVolver)
            Text(
                "Escucha y toca el animal",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Tinta,
                textAlign = TextAlign.Center
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                opciones.forEachIndexed { i, animal ->
                    Button(
                        onClick = {
                            if (vm.comprobar(animal)) {
                                Sonidos.estrella(); premiar()
                                narrador.decirSecuencia(animal.nombre, "¡Yupi! ${actual.silabas}")
                            } else {
                                Sonidos.errorSuave()
                                narrador.decirSecuencia(
                                    animal.nombre,
                                    "Oh, no. Busca ${actual.nombre}"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = listOf(Menta, Rosa, Lila)[i]
                        ),
                        elevation = ButtonDefaults.buttonElevation(7.dp)
                    ) {
                        Image(
                            painterResource(animal.icono),
                            contentDescription = animal.nombre,
                            modifier = Modifier.size(125.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TrazoScreen(
    vm: TrazoViewModel,
    narrador: Narrador,
    premiar: () -> Unit,
    onVolver: () -> Unit
) {
    val progreso by vm.progreso.collectAsStateWithLifecycle()
    val figura by vm.figura.collectAsStateWithLifecycle()
    val sectores = remember(figura) { mutableSetOf<Int>() }
    val scope = rememberCoroutineScope()
    LaunchedEffect(figura) {
        delay(550)
        narrador.decir("Traza el ${figura.nombre}. Sigue los puntitos con tu dedo")
    }
    Fondo {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Cabecera("Traza la Figura", onVolver)
            Text(
                "Sigue los puntitos",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Tinta
            )
            Box(Modifier.fillMaxWidth().aspectRatio(1f), contentAlignment = Alignment.Center) {
                Canvas(
                    Modifier.fillMaxSize().pointerInput(figura) {
                        fun registrar(posicion: Offset) {
                            val puntos = puntosDeFigura(figura, size.width.toFloat(), size.height.toFloat())
                            val cercano = puntos.indices.minByOrNull { indice ->
                                val dx = posicion.x - puntos[indice].x
                                val dy = posicion.y - puntos[indice].y
                                dx * dx + dy * dy
                            } ?: return
                            val punto = puntos[cercano]
                            val distancia = kotlin.math.hypot(
                                posicion.x - punto.x,
                                posicion.y - punto.y
                            )
                            if (distancia < size.width * .12f) {
                                for (vecino in -2..2) {
                                    sectores += (cercano + vecino).coerceIn(puntos.indices)
                                }
                                if (vm.actualizar(sectores.size / puntos.size.toFloat())) {
                                    Sonidos.estrella()
                                    premiar()
                                    narrador.decirSecuencia(
                                        "¡Yupi! ¡Muy bien, Rei!",
                                        "Has trazado un ${figura.nombre}"
                                    )
                                    scope.launch {
                                        delay(1_700)
                                        vm.siguienteFigura()
                                    }
                                }
                            }
                        }
                        detectDragGestures(
                            onDragStart = ::registrar,
                            onDrag = { cambio, _ ->
                                cambio.consume()
                                registrar(cambio.position)
                            }
                        )
                    }
                ) {
                    val puntos = puntosDeFigura(figura, size.width, size.height)
                    val camino = Path().apply {
                        moveTo(puntos.first().x, puntos.first().y)
                        puntos.drop(1).forEach { lineTo(it.x, it.y) }
                        close()
                    }
                    drawPath(
                        path = camino,
                        color = Color(0xFF9AAEB7),
                        style = Stroke(
                            width = 18f,
                            cap = StrokeCap.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 28f))
                        )
                    )
                    sectores.forEach { indice ->
                        drawCircle(Menta, radius = 12f, center = puntos[indice])
                    }
                }
                Text("${(progreso * 100).toInt()}%", fontSize = 38.sp, fontWeight = FontWeight.ExtraBold, color = Tinta)
            }
            Button(
                onClick = { sectores.clear(); vm.reiniciar() },
                modifier = Modifier.fillMaxWidth(.72f).height(72.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Melon)
            ) { Text("Otra vez", fontSize = 30.sp, color = Tinta, fontWeight = FontWeight.Bold) }
        }
    }
}

private fun puntosDeFigura(
    figura: FiguraTrazo,
    ancho: Float,
    alto: Float
): List<Offset> {
    val centro = Offset(ancho / 2f, alto / 2f)
    fun elipse(radioX: Float, radioY: Float) = List(120) { i ->
        val angulo = 2.0 * PI * i / 120.0
        Offset(
            centro.x + cos(angulo).toFloat() * radioX,
            centro.y + sin(angulo).toFloat() * radioY
        )
    }
    fun poligono(vertices: List<Offset>): List<Offset> {
        val puntos = mutableListOf<Offset>()
        vertices.indices.forEach { i ->
            val inicio = vertices[i]
            val fin = vertices[(i + 1) % vertices.size]
            repeat(30) { paso ->
                val t = paso / 30f
                puntos += Offset(
                    inicio.x + (fin.x - inicio.x) * t,
                    inicio.y + (fin.y - inicio.y) * t
                )
            }
        }
        return puntos
    }
    val izquierda = ancho * .17f
    val derecha = ancho * .83f
    val arriba = alto * .17f
    val abajo = alto * .83f
    return when (figura) {
        FiguraTrazo.CIRCULO -> elipse(ancho * .33f, alto * .33f)
        FiguraTrazo.OVALO -> elipse(ancho * .36f, alto * .25f)
        FiguraTrazo.CUADRADO -> poligono(
            listOf(
                Offset(izquierda, arriba), Offset(derecha, arriba),
                Offset(derecha, abajo), Offset(izquierda, abajo)
            )
        )
        FiguraTrazo.RECTANGULO -> poligono(
            listOf(
                Offset(ancho * .10f, alto * .25f), Offset(ancho * .90f, alto * .25f),
                Offset(ancho * .90f, alto * .75f), Offset(ancho * .10f, alto * .75f)
            )
        )
        FiguraTrazo.TRIANGULO -> poligono(
            listOf(
                Offset(centro.x, alto * .12f),
                Offset(ancho * .88f, alto * .83f),
                Offset(ancho * .12f, alto * .83f)
            )
        )
    }
}

@Composable
fun AlbumScreen(
    recompensas: List<Recompensa>,
    estrellas: Int,
    narrador: Narrador,
    onVolver: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(600)
        narrador.decir(
            "Este es tu álbum, Rei. Cada cinco estrellas ganas un sticker nuevo. Toca uno para escucharlo"
        )
    }
    Fondo {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Cabecera("Álbum de stickers", onVolver)
            Spacer(Modifier.height(16.dp))
            Text("⭐ $estrellas  •  Uno nuevo cada 5", fontSize = 28.sp, color = Tinta)
            Spacer(Modifier.height(20.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(recompensas, key = { _, recompensa -> recompensa.nombre }) {
                        indice, recompensa ->
                    Card(
                        onClick = {
                            if (recompensa.desbloqueado) {
                                narrador.decir(
                                    "¡Yupi! Tienes el sticker ${recompensa.nombre}"
                                )
                            } else {
                                val necesarias = (indice + 1) * 5
                                val faltan = (necesarias - estrellas).coerceAtLeast(1)
                                narrador.decir(
                                    "El sticker ${recompensa.nombre} está bloqueado. " +
                                        "Te faltan $faltan estrellas"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth().aspectRatio(.9f),
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (recompensa.desbloqueado) Color.White.copy(.88f)
                            else Color(0xFFD5DADD)
                        ),
                        elevation = CardDefaults.cardElevation(6.dp)
                    ) {
                        Column(
                            Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painterResource(recompensa.icono),
                                contentDescription = recompensa.nombre,
                                modifier = Modifier.size(105.dp).alpha(
                                    if (recompensa.desbloqueado) 1f else .20f
                                )
                            )
                            Text(
                                if (recompensa.desbloqueado) recompensa.nombre else "Bloqueado",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (recompensa.desbloqueado) Tinta else Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IconoAltavoz(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val blanco = Color.White
        drawCircle(blanco, radius = size.width * .12f, center = Offset(size.width * .28f, size.height * .5f))
        listOf(.20f, .32f, .44f).forEachIndexed { indice, radio ->
            drawArc(
                color = blanco,
                startAngle = -55f,
                sweepAngle = 110f,
                useCenter = false,
                topLeft = Offset(
                    size.width * (.28f - radio / 2 + indice * .07f),
                    size.height * (.5f - radio)
                ),
                size = Size(size.width * radio * 2, size.height * radio * 2),
                style = Stroke(width = size.width * .07f, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun SonidosAnimalesScreen(
    vm: SonidosAnimalesViewModel,
    narrador: Narrador,
    premiar: () -> Unit,
    onVolver: () -> Unit
) {
    val indice by vm.indice.collectAsStateWithLifecycle()
    val actual = vm.animales[indice]
    val scope = rememberCoroutineScope()
    val escala = remember { Animatable(1f) }

    LaunchedEffect(indice) {
        delay(450)
        narrador.decirSecuencia(actual.sonido, actual.nombre)
    }

    Fondo {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Cabecera("Sonidos del Bosque", onVolver)
            Text(
                "Toca al animal y escúchalo",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Tinta,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = {
                    val sonido = actual.sonido
                    val nombre = actual.nombre
                    scope.launch {
                        narrador.decirSecuencia(sonido, nombre)
                        if (vm.tocar()) { Sonidos.estrella(); premiar() }
                        escala.animateTo(1.22f, tween(160))
                        escala.animateTo(1f, tween(240, easing = FastOutSlowInEasing))
                    }
                },
                modifier = Modifier.size(260.dp).scale(escala.value),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Menta),
                elevation = ButtonDefaults.buttonElevation(9.dp)
            ) {
                Image(
                    painterResource(actual.icono),
                    contentDescription = actual.nombre,
                    modifier = Modifier.size(170.dp)
                )
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
fun QuienDiceEstoScreen(
    vm: QuienDiceEstoViewModel,
    narrador: Narrador,
    premiar: () -> Unit,
    onVolver: () -> Unit
) {
    val indice by vm.indice.collectAsStateWithLifecycle()
    val objetivo = vm.objetivo
    val opciones = remember(indice) { vm.opciones() }

    fun preguntar() {
        narrador.decirSecuencia("¿Quién dice esto?", objetivo.sonido)
    }

    LaunchedEffect(indice) {
        delay(500)
        preguntar()
    }

    Fondo {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Cabecera("¿Quién Dice Esto?", onVolver)
            Button(
                onClick = { preguntar() },
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Oro)
            ) { IconoAltavoz(Modifier.size(60.dp)) }
            Text(
                "Toca quién hace ese sonido",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Tinta,
                textAlign = TextAlign.Center
            )
            Row(
                Modifier.fillMaxWidth().height(190.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                opciones.forEachIndexed { i, animal ->
                    Button(
                        onClick = {
                            if (vm.comprobar(animal)) {
                                Sonidos.estrella(); premiar()
                                narrador.decirSecuencia(animal.nombre, "¡Sí! ¡Muy bien!")
                            } else {
                                Sonidos.errorSuave()
                                narrador.decirSecuencia(
                                    animal.nombre,
                                    "Oh, no. Escucha de nuevo",
                                    objetivo.sonido
                                )
                            }
                        },
                        modifier = Modifier.weight(1f).fillMaxSize(),
                        shape = RoundedCornerShape(40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = listOf(Menta, Rosa, Lila)[i]
                        ),
                        elevation = ButtonDefaults.buttonElevation(7.dp)
                    ) {
                        Image(
                            painterResource(animal.icono),
                            contentDescription = animal.nombre,
                            modifier = Modifier.size(100.dp)
                        )
                    }
                }
            }
        }
    }
}

private val AreaBurbujasAncho = 300.dp
private val AreaBurbujasAlto = 420.dp

private fun nombreColorBurbuja(color: Color): String = when (color) {
    Color(0xFF6FCC9B) -> "¡Burbuja verde!"
    Color(0xFF55A9E8) -> "¡Burbuja azul!"
    Color(0xFFF39ABC) -> "¡Burbuja rosa!"
    Color(0xFFFFD45F) -> "¡Burbuja amarilla!"
    Color(0xFF9B7ED9) -> "¡Burbuja morada!"
    Color(0xFFF5A15D) -> "¡Burbuja naranja!"
    else -> "¡Burbuja!"
}

@Composable
fun BurbujasScreen(
    vm: BurbujasViewModel,
    narrador: Narrador,
    premiar: () -> Unit,
    onVolver: () -> Unit
) {
    val burbujas by vm.burbujas.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        delay(400)
        narrador.decir("Revienta las burbujas de colores")
    }

    Fondo {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Cabecera("Burbujas Mágicas", onVolver)
            Box(
                Modifier
                    .width(AreaBurbujasAncho)
                    .height(AreaBurbujasAlto)
                    .background(Color.White.copy(.35f), RoundedCornerShape(36.dp))
            ) {
                burbujas.forEach { burbuja ->
                    val tamano = AreaBurbujasAncho * (burbuja.radio * 2f)
                    Box(
                        Modifier
                            .offset(
                                x = AreaBurbujasAncho * burbuja.x,
                                y = AreaBurbujasAlto * burbuja.y
                            )
                            .size(tamano)
                            .clickable {
                                Sonidos.pop()
                                narrador.decir(nombreColorBurbuja(burbuja.color))
                                if (vm.reventar(burbuja.id)) {
                                    Sonidos.estrella()
                                    premiar()
                                }
                            }
                            .background(burbuja.color.copy(alpha = .82f), CircleShape)
                    ) {
                        Box(
                            Modifier
                                .padding(top = 6.dp, start = 8.dp)
                                .size(tamano * .3f)
                                .background(Color.White.copy(.55f), CircleShape)
                        )
                    }
                }
            }
            Text(
                "¡Muy bien, Rei!",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Tinta
            )
        }
    }
}

@Composable
fun GrandePequenoScreen(
    vm: GrandePequenoViewModel,
    narrador: Narrador,
    premiar: () -> Unit,
    onVolver: () -> Unit
) {
    val indice by vm.indice.collectAsStateWithLifecycle()
    val pideGrande by vm.pideGrande.collectAsStateWithLifecycle()
    val grandeAIzquierda by vm.grandeAIzquierda.collectAsStateWithLifecycle()
    val objeto = vm.objeto

    fun preguntar() {
        narrador.decir("Toca el ${objeto.nombre} ${if (pideGrande) "grande" else "pequeño"}")
    }

    LaunchedEffect(indice, pideGrande) {
        delay(500)
        preguntar()
    }

    Fondo {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Cabecera("Grande y Pequeño", onVolver)
            Text(
                "¿Cuál es el ${if (pideGrande) "grande" else "pequeño"}?",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Tinta,
                textAlign = TextAlign.Center
            )
            Row(
                Modifier.fillMaxWidth().height(300.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val ordenGrande = if (grandeAIzquierda) listOf(true, false) else listOf(false, true)
                ordenGrande.forEach { esGrande ->
                    Button(
                        onClick = {
                            if (vm.comprobar(esGrande)) {
                                Sonidos.estrella(); premiar()
                                narrador.decirSecuencia("¡Muy bien!", objeto.nombre)
                            } else {
                                Sonidos.errorSuave()
                                narrador.decir("Oh, no. Intenta otra vez")
                            }
                        },
                        modifier = Modifier.size(if (esGrande) 220.dp else 130.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = if (esGrande) Menta else Rosa),
                        elevation = ButtonDefaults.buttonElevation(7.dp)
                    ) {
                        Image(
                            painterResource(objeto.icono),
                            contentDescription = objeto.nombre,
                            modifier = Modifier.size(if (esGrande) 140.dp else 76.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ClasificarColorScreen(
    vm: ClasificarColorViewModel,
    narrador: Narrador,
    premiar: () -> Unit,
    onVolver: () -> Unit
) {
    val indice by vm.indice.collectAsStateWithLifecycle()
    val objetivo = vm.objetivo
    val cestas = remember(indice) { vm.cestas() }

    fun preguntar() {
        narrador.decir("Lleva la pelota a la cesta ${objetivo.nombre}")
    }

    LaunchedEffect(indice) {
        delay(500)
        preguntar()
    }

    Fondo {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Cabecera("A Cada Cesta su Color", onVolver)
            Button(
                onClick = { preguntar() },
                modifier = Modifier.size(140.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = objetivo.color),
                elevation = ButtonDefaults.buttonElevation(8.dp)
            ) { IconoAltavoz(Modifier.size(64.dp)) }
            Text(
                "Toca la cesta del mismo color",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Tinta,
                textAlign = TextAlign.Center
            )
            Row(
                Modifier.fillMaxWidth().height(200.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                cestas.forEach { cesta ->
                    Button(
                        onClick = {
                            if (vm.comprobar(cesta)) {
                                Sonidos.estrella(); premiar()
                                narrador.decirSecuencia("¡Sí!", "Cesta ${cesta.nombre}")
                            } else {
                                Sonidos.errorSuave()
                                narrador.decir("Oh, no. Busca ${objetivo.nombre}")
                            }
                        },
                        modifier = Modifier.weight(1f).fillMaxSize(),
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomEnd = 40.dp,
                            bottomStart = 40.dp
                        ),
                        colors = ButtonDefaults.buttonColors(containerColor = cesta.color),
                        elevation = ButtonDefaults.buttonElevation(7.dp)
                    ) {}
                }
            }
        }
    }
}

@Composable
fun PuzzleScreen(
    vm: PuzzleViewModel,
    narrador: Narrador,
    premiar: () -> Unit,
    onVolver: () -> Unit
) {
    val indice by vm.indice.collectAsStateWithLifecycle()
    val objetivo = vm.objetivo
    val opciones = remember(indice) { vm.opciones() }
    val scope = rememberCoroutineScope()
    val alphaObjetivo = remember(indice) { Animatable(.22f) }

    LaunchedEffect(indice) {
        delay(500)
        narrador.decir("¿Qué pieza completa el dibujo?")
    }

    Fondo {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Cabecera("Arma el Dibujo", onVolver)
            Image(
                painterResource(objetivo.icono),
                contentDescription = objetivo.nombre,
                modifier = Modifier.size(190.dp).alpha(alphaObjetivo.value)
            )
            Text(
                "Toca la pieza correcta",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Tinta
            )
            Row(
                Modifier.fillMaxWidth().height(200.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                opciones.forEachIndexed { i, pieza ->
                    Button(
                        onClick = {
                            if (vm.comprobar(pieza)) {
                                Sonidos.estrella(); premiar()
                                narrador.decirSecuencia(pieza.nombre, "¡Completaste el dibujo!")
                                scope.launch { alphaObjetivo.animateTo(1f, tween(500)) }
                            } else {
                                Sonidos.errorSuave()
                                narrador.decirSecuencia(pieza.nombre, "Oh, no. Intenta otra vez")
                            }
                        },
                        modifier = Modifier.weight(1f).fillMaxSize(),
                        shape = RoundedCornerShape(36.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = listOf(Menta, Rosa)[i % 2]
                        ),
                        elevation = ButtonDefaults.buttonElevation(7.dp)
                    ) {
                        Image(
                            painterResource(pieza.icono),
                            contentDescription = pieza.nombre,
                            modifier = Modifier.size(110.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReiConRopa(prendas: List<Prenda>, puestas: Set<TipoPrenda>, modifier: Modifier) {
    val colorDe = { tipo: TipoPrenda -> prendas.firstOrNull { it.tipo == tipo }?.color }
    Canvas(modifier) {
        val w = size.width; val h = size.height
        drawCircle(Color(0xFFE9C08D), radius = w * .38f, center = Offset(w * .5f, h * .58f))
        drawCircle(Color(0xFFE9C08D), radius = w * .12f, center = Offset(w * .28f, h * .28f))
        drawCircle(Color(0xFFE9C08D), radius = w * .12f, center = Offset(w * .72f, h * .28f))
        drawCircle(Tinta, radius = w * .035f, center = Offset(w * .40f, h * .52f))
        drawCircle(Tinta, radius = w * .035f, center = Offset(w * .60f, h * .52f))
        drawCircle(Color(0xFFF6DDB6), radius = w * .10f, center = Offset(w * .5f, h * .63f))
        if (TipoPrenda.GORRO in puestas) colorDe(TipoPrenda.GORRO)?.let { color ->
            val p = Path().apply {
                moveTo(w * .30f, h * .30f)
                lineTo(w * .70f, h * .30f)
                lineTo(w * .5f, h * .06f)
                close()
            }
            drawPath(p, color)
        }
        if (TipoPrenda.BUFANDA in puestas) colorDe(TipoPrenda.BUFANDA)?.let { color ->
            drawRoundRect(
                color,
                topLeft = Offset(w * .30f, h * .78f),
                size = Size(w * .40f, h * .12f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f)
            )
        }
        if (TipoPrenda.BOTAS in puestas) colorDe(TipoPrenda.BOTAS)?.let { color ->
            drawRoundRect(
                color,
                topLeft = Offset(w * .32f, h * .90f),
                size = Size(w * .14f, h * .10f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f)
            )
            drawRoundRect(
                color,
                topLeft = Offset(w * .54f, h * .90f),
                size = Size(w * .14f, h * .10f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f)
            )
        }
    }
}

@Composable
private fun IconoPrenda(tipo: TipoPrenda, modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width; val h = size.height
        when (tipo) {
            TipoPrenda.GORRO -> {
                val p = Path().apply {
                    moveTo(w * .15f, h * .75f)
                    lineTo(w * .85f, h * .75f)
                    lineTo(w * .5f, h * .1f)
                    close()
                }
                drawPath(p, Color.White)
            }
            TipoPrenda.BUFANDA -> drawRoundRect(
                Color.White,
                topLeft = Offset(w * .1f, h * .35f),
                size = Size(w * .8f, h * .3f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f)
            )
            TipoPrenda.BOTAS -> {
                drawRoundRect(
                    Color.White,
                    topLeft = Offset(w * .12f, h * .3f),
                    size = Size(w * .3f, h * .5f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f)
                )
                drawRoundRect(
                    Color.White,
                    topLeft = Offset(w * .58f, h * .3f),
                    size = Size(w * .3f, h * .5f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f)
                )
            }
        }
    }
}

@Composable
fun VestirReiScreen(
    vm: VestirReiViewModel,
    narrador: Narrador,
    premiar: () -> Unit,
    onVolver: () -> Unit
) {
    val prendas by vm.prendas.collectAsStateWithLifecycle()
    val puestas by vm.puestas.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    LaunchedEffect(prendas) {
        delay(500)
        narrador.decir("Vamos a vestir a Rei. Toca la ropa")
    }

    Fondo {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Cabecera("Vestir a Rei", onVolver)
            ReiConRopa(prendas, puestas, Modifier.size(220.dp))
            Text(
                "Toca la ropa para vestirlo",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Tinta
            )
            Row(
                Modifier.fillMaxWidth().height(150.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                prendas.forEach { prenda ->
                    val puesta = prenda.tipo in puestas
                    Button(
                        onClick = {
                            narrador.decir("Un ${prenda.nombre}")
                            if (vm.tocar(prenda)) {
                                Sonidos.pop(); premiar()
                                if (vm.completo) {
                                    scope.launch {
                                        delay(600)
                                        Sonidos.estrella()
                                        narrador.decir("¡Rei ya está listo!")
                                        delay(1_800)
                                        vm.siguienteConjunto()
                                    }
                                }
                            }
                        },
                        enabled = !puesta,
                        modifier = Modifier.weight(1f).fillMaxSize(),
                        shape = RoundedCornerShape(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = prenda.color,
                            disabledContainerColor = Color.White.copy(.4f)
                        ),
                        elevation = ButtonDefaults.buttonElevation(7.dp)
                    ) {
                        IconoPrenda(prenda.tipo, Modifier.size(72.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CaritaEmocion(emocion: Emocion, modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width; val h = size.height
        drawCircle(Color.White, radius = w * .48f, center = Offset(w * .5f, h * .5f))
        drawCircle(Tinta, radius = w * .045f, center = Offset(w * .36f, h * .42f))
        drawCircle(Tinta, radius = w * .045f, center = Offset(w * .64f, h * .42f))
        when (emocion) {
            Emocion.FELIZ -> drawArc(
                color = Tinta,
                startAngle = 20f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(w * .32f, h * .48f),
                size = Size(w * .36f, h * .26f),
                style = Stroke(width = w * .04f, cap = StrokeCap.Round)
            )
            Emocion.TRISTE -> drawArc(
                color = Tinta,
                startAngle = 200f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(w * .32f, h * .62f),
                size = Size(w * .36f, h * .26f),
                style = Stroke(width = w * .04f, cap = StrokeCap.Round)
            )
            Emocion.SORPRENDIDO -> drawCircle(
                Tinta,
                radius = w * .07f,
                center = Offset(w * .5f, h * .62f),
                style = Stroke(width = w * .035f)
            )
        }
    }
}

@Composable
fun EmocionesScreen(
    vm: EmocionesViewModel,
    narrador: Narrador,
    premiar: () -> Unit,
    onVolver: () -> Unit
) {
    val emocion by vm.emocion.collectAsStateWithLifecycle()

    fun preguntar() {
        narrador.decir("Toca la carita ${emocion.nombre}")
    }

    LaunchedEffect(emocion) {
        delay(500)
        preguntar()
    }

    Fondo {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Cabecera("Las Emociones de Rei", onVolver)
            Button(
                onClick = { preguntar() },
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Oro)
            ) { IconoAltavoz(Modifier.size(58.dp)) }
            Text(
                "¿Cuál es la carita ${emocion.nombre}?",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Tinta,
                textAlign = TextAlign.Center
            )
            Row(
                Modifier.fillMaxWidth().height(190.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Emocion.entries.forEachIndexed { i, opcion ->
                    Button(
                        onClick = {
                            if (vm.comprobar(opcion)) {
                                Sonidos.estrella(); premiar()
                                narrador.decirSecuencia("¡Sí!", opcion.nombre)
                            } else {
                                Sonidos.errorSuave()
                                narrador.decir("Oh, no. Intenta otra vez")
                            }
                        },
                        modifier = Modifier.weight(1f).fillMaxSize(),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = listOf(Menta, Rosa, Lila)[i]
                        ),
                        elevation = ButtonDefaults.buttonElevation(7.dp)
                    ) { CaritaEmocion(opcion, Modifier.size(90.dp)) }
                }
            }
        }
    }
}

@Composable
fun ContarHasta3Screen(
    vm: ContarHasta3ViewModel,
    narrador: Narrador,
    premiar: () -> Unit,
    onVolver: () -> Unit
) {
    val total by vm.total.collectAsStateWithLifecycle()
    val tocados by vm.tocados.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val escala = remember { Animatable(1f) }
    val numeroEnPalabras = listOf("uno", "dos", "tres")

    LaunchedEffect(total, tocados) {
        if (tocados == 0) {
            delay(500)
            narrador.decir("Cuenta conmigo hasta $total")
        }
    }

    Fondo {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Cabecera("Contando hasta Tres", onVolver)
            Text(
                "Toca las estrellitas en orden",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Tinta,
                textAlign = TextAlign.Center
            )
            Row(
                Modifier.fillMaxWidth().height(220.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(total) { i ->
                    val esSiguiente = i == tocados
                    val yaContada = i < tocados
                    var tamanoImagen = Modifier
                        .size(if (esSiguiente) 110.dp else 84.dp)
                        .alpha(if (yaContada || esSiguiente) 1f else .35f)
                        .scale(if (esSiguiente) escala.value else 1f)
                    if (esSiguiente) {
                        tamanoImagen = tamanoImagen.clickable {
                            val completo = vm.tocarSiguiente()
                            if (completo) {
                                // Se encolan ambas frases para que el último número no sea
                                // reemplazado por la felicitación ni por la ronda siguiente.
                                narrador.decirSecuencia(
                                    numeroEnPalabras[i],
                                    "¡Muy bien contando, Rei!"
                                )
                            } else {
                                narrador.decir(numeroEnPalabras[i])
                            }
                            scope.launch {
                                escala.animateTo(1.3f, tween(140))
                                escala.animateTo(1f, tween(200))
                            }
                            if (completo) {
                                Sonidos.estrella(); premiar()
                                // Conserva las estrellas completas en pantalla mientras habla.
                                scope.launch {
                                    delay(2_700)
                                    vm.siguienteRonda()
                                }
                            }
                        }
                    }
                    Image(
                        painterResource(R.drawable.ic_estrella),
                        contentDescription = null,
                        modifier = tamanoImagen
                    )
                }
            }
        }
    }
}

@Composable
private fun IconoPaso(paso: PasoRutina, modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width; val h = size.height
        when (paso) {
            PasoRutina.DESAYUNAR -> {
                drawArc(
                    color = Color.White,
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(w * .2f, h * .35f),
                    size = Size(w * .6f, h * .4f),
                    style = Stroke(width = w * .07f, cap = StrokeCap.Round)
                )
                drawRoundRect(
                    Color.White,
                    topLeft = Offset(w * .35f, h * .15f),
                    size = Size(w * .08f, h * .2f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f)
                )
            }
            PasoRutina.DIENTES -> {
                drawRoundRect(
                    Color.White,
                    topLeft = Offset(w * .40f, h * .12f),
                    size = Size(w * .2f, h * .55f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f)
                )
                drawRoundRect(
                    Color.White,
                    topLeft = Offset(w * .30f, h * .60f),
                    size = Size(w * .4f, h * .12f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f)
                )
            }
            PasoRutina.PIJAMA -> {
                val p = Path().apply {
                    moveTo(w * .5f, h * .2f)
                    lineTo(w * .75f, h * .35f)
                    lineTo(w * .68f, h * .8f)
                    lineTo(w * .32f, h * .8f)
                    lineTo(w * .25f, h * .35f)
                    close()
                }
                drawPath(p, Color.White)
            }
            PasoRutina.DORMIR -> {
                drawCircle(Color.White, radius = w * .28f, center = Offset(w * .46f, h * .5f))
                drawCircle(Melon, radius = w * .24f, center = Offset(w * .62f, h * .42f))
            }
        }
    }
}

@Composable
fun RutinaDiariaScreen(
    vm: RutinaDiariaViewModel,
    narrador: Narrador,
    premiar: () -> Unit,
    onVolver: () -> Unit
) {
    val indice by vm.indice.collectAsStateWithLifecycle()
    val paso = vm.pasos[indice]

    LaunchedEffect(indice) {
        delay(500)
        narrador.decir(paso.texto)
    }

    Fondo {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Cabecera("La Rutina de Rei", onVolver)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                vm.pasos.forEachIndexed { i, _ ->
                    Box(
                        Modifier
                            .padding(4.dp)
                            .size(16.dp)
                            .background(if (i <= indice) Menta else Color.White.copy(.5f), CircleShape)
                    )
                }
            }
            Button(
                onClick = {
                    narrador.decir(paso.texto)
                    val completo = vm.avanzar()
                    Sonidos.estrella()
                    premiar()
                    if (completo) narrador.decirSecuencia("¡Terminaste la rutina, Rei!")
                },
                modifier = Modifier.size(230.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Melon),
                elevation = ButtonDefaults.buttonElevation(9.dp)
            ) { IconoPaso(paso, Modifier.size(140.dp)) }
            Text(
                paso.texto,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Tinta,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun Confetti(key: Int, onTerminar: () -> Unit) {
    val terminarActual by rememberUpdatedState(onTerminar)
    val avance = remember(key) { Animatable(0f) }
    val particulas = remember(key) {
        List(100) {
            Triple(Random.nextFloat(), Random.nextFloat() * .35f, Random.nextFloat() * 360f)
        }
    }
    LaunchedEffect(key) {
        avance.animateTo(1f, tween(3000, easing = LinearEasing))
        terminarActual()
    }
    Canvas(Modifier.fillMaxSize()) {
        val colores = listOf(Menta, Rosa, Lila, Melon, Oro, Color(0xFF76C7FF))
        particulas.forEachIndexed { i, p ->
            val x = p.first * size.width +
                sin((avance.value * 8 + i) * .7).toFloat() * 35f
            val y = ((p.second + avance.value * 1.15f) % 1.1f) * size.height
            drawRect(colores[i % colores.size], Offset(x, y), Size(16f, 28f))
        }
    }
}
