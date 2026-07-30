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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
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
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

import com.rei.elbosque.ui.ReiColores.Fondo1
import com.rei.elbosque.ui.ReiColores.Fondo2
import com.rei.elbosque.ui.ReiColores.Menta
import com.rei.elbosque.ui.ReiColores.Rosa
import com.rei.elbosque.ui.ReiColores.Lila
import com.rei.elbosque.ui.ReiColores.Melon
import com.rei.elbosque.ui.ReiColores.Tinta
import com.rei.elbosque.ui.ReiColores.Oro

@Composable
private fun Fondo(content: @Composable () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Fondo1, Fondo2))
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            Modifier.fillMaxHeight().fillMaxWidth().widthIn(max = 720.dp)
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) { content() }
    }
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
    var mostrarMasJuegos by rememberSaveable { mutableStateOf(false) }
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
            Modifier.align(Alignment.TopCenter).fillMaxHeight().fillMaxWidth()
                .widthIn(max = 720.dp).padding(horizontal = 18.dp, vertical = 16.dp),
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
                if (!mostrarMasJuegos) {
                    item { BotonMenu(R.drawable.ic_oso, "Animales", Lila) {
                        narrador.decir("Animales"); onAbrir("animales")
                    } }
                    item { BotonMenu(R.drawable.ic_arcoiris, "Colores", Menta) {
                        narrador.decir("Colores"); onAbrir("colores")
                    } }
                    item { BotonMenu(R.drawable.ic_formas, "Formas", Rosa) {
                        narrador.decir("Formas"); onAbrir("formas")
                    } }
                    item { BotonMenu(R.drawable.ic_estrellas, "Números", Melon) {
                        narrador.decir("Números"); onAbrir("numeros")
                    } }
                    item { BotonMenu(R.drawable.ic_bus, "Autobús", Color(0xFFFFD66B)) {
                        narrador.decir("Autobús"); onAbrir("bus")
                    } }
                    item { BotonMenu(R.drawable.ic_regalo, "Más juegos", Color(0xFFB8D8F5)) {
                        narrador.decir("Más juegos")
                        mostrarMasJuegos = true
                    } }
                } else {
                    item { BotonMenu(R.drawable.ic_regalo, "Volver", Color(0xFFB8D8F5)) {
                        narrador.decir("Volver a favoritos")
                        mostrarMasJuegos = false
                    } }
                    item { BotonMenu(R.drawable.ic_paraguas, "Busca", Menta) {
                        narrador.decir("Busca y encuentra"); onAbrir("busca_objeto")
                    } }
                    item { BotonMenu(R.drawable.ic_pez, "Alimenta", Rosa) {
                        narrador.decir("Alimenta al animal"); onAbrir("alimenta_animal")
                    } }
                    item { BotonMenu(R.drawable.ic_lapiz, "Trazo", Melon) {
                        narrador.decir("Trazo"); onAbrir("trazo")
                    } }
                    item { BotonMenu(R.drawable.ic_gato, "Sombras", Color(0xFFB8D8F5)) {
                        narrador.decir("Sombras"); onAbrir("sombras")
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
                    item { BotonMenu(R.drawable.ic_regadera, "Mi planta", Menta) {
                        narrador.decir("Riega tu planta"); onAbrir("mi_planta")
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
                    item { BotonMenu(R.drawable.ic_nube, "¿Dónde vive?", Lila) {
                        narrador.decir("¿Dónde vive?"); onAbrir("donde_vive")
                    } }
                    item { BotonMenu(R.drawable.ic_zapato, "El Camino", Color(0xFFB8D8F5)) {
                        narrador.decir("El camino de Rei"); onAbrir("laberinto")
                    } }
                    item { BotonMenu(R.drawable.ic_cuchara, "Imita el Ritmo", Menta) {
                        narrador.decir("Imita el ritmo"); onAbrir("ritmo")
                    } }
                    item { BotonMenu(R.drawable.ic_regalo, "¿Qué Falta?", Rosa) {
                        narrador.decir("¿Qué falta?"); onAbrir("que_falta")
                    } }
                    item { BotonMenu(R.drawable.ic_libro, "Ordena Tamaños", Color(0xFFFFD66B)) {
                        narrador.decir("Ordena por tamaño"); onAbrir("ordena_tamano")
                    } }
                    item { BotonMenu(R.drawable.ic_paraguas, "Respira", Color(0xFFB8D8F5)) {
                        narrador.decir("Respira con Rei"); onAbrir("respira")
                    } }
                }
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
    val opciones = remember(forma) { vm.opciones() }
    val scope = rememberCoroutineScope()
    val escala = remember { Animatable(1f) }
    val giro = remember { Animatable(0f) }

    LaunchedEffect(forma) {
        delay(500)
        narrador.decir("Toca ${forma.articulo} ${forma.titulo}")
    }

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
                opciones.forEachIndexed { i, opcion ->
                    Button(
                        onClick = {
                            val acierto = vm.comprobar(opcion)
                            scope.launch {
                                if (acierto) {
                                    Sonidos.estrella(); premiar()
                                    narrador.felicitar(
                                        opcion.titulo,
                                        "¡Yupi! ¡Muy bien, Rei!"
                                    )
                                    escala.animateTo(1.28f, tween(180))
                                    escala.animateTo(1f, tween(260, easing = FastOutSlowInEasing))
                                } else {
                                    Sonidos.errorSuave()
                                    narrador.decirSecuencia(
                                        opcion.titulo,
                                        "Oh, no. Busca ${forma.articulo} ${forma.titulo}"
                                    )
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

private fun trazarEstrella(w: Float, h: Float): Path {
    val centro = Offset(w / 2f, h / 2f)
    val radioExterior = minOf(w, h) / 2f * .95f
    val radioInterior = radioExterior * .42f
    val path = Path()
    for (i in 0 until 10) {
        val angulo = (-PI / 2 + i * PI / 5).toFloat()
        val radio = if (i % 2 == 0) radioExterior else radioInterior
        val punto = Offset(centro.x + cos(angulo) * radio, centro.y + sin(angulo) * radio)
        if (i == 0) path.moveTo(punto.x, punto.y) else path.lineTo(punto.x, punto.y)
    }
    path.close()
    return path
}

private fun trazarPoligono(w: Float, h: Float, lados: Int): Path {
    val centro = Offset(w / 2f, h / 2f)
    val radio = minOf(w, h) / 2f * .92f
    val path = Path()
    for (i in 0 until lados) {
        val angulo = (-PI / 2 + i * 2 * PI / lados).toFloat()
        val punto = Offset(centro.x + cos(angulo) * radio, centro.y + sin(angulo) * radio)
        if (i == 0) path.moveTo(punto.x, punto.y) else path.lineTo(punto.x, punto.y)
    }
    path.close()
    return path
}

/** Luna creciente real (resta de dos círculos), transparente donde no hay luna. */
private fun trazarLuna(w: Float, h: Float): Path {
    val radio = minOf(w, h) / 2f * .9f
    val centro1 = Offset(w * .42f, h * .5f)
    val centro2 = Offset(w * .64f, h * .40f)
    val circulo1 = Path().apply {
        addOval(
            androidx.compose.ui.geometry.Rect(
                centro1.x - radio, centro1.y - radio, centro1.x + radio, centro1.y + radio
            )
        )
    }
    val circulo2 = Path().apply {
        addOval(
            androidx.compose.ui.geometry.Rect(
                centro2.x - radio * .85f, centro2.y - radio * .85f,
                centro2.x + radio * .85f, centro2.y + radio * .85f
            )
        )
    }
    val resultado = Path()
    resultado.op(circulo1, circulo2, androidx.compose.ui.graphics.PathOperation.Difference)
    return resultado
}

/** Corazón trazado con la curva paramétrica clásica, normalizada al tamaño del ícono. */
private fun trazarCorazon(w: Float, h: Float): Path {
    val path = Path()
    val pasos = 48
    for (i in 0..pasos) {
        val t = (i / pasos.toFloat()) * (2 * PI).toFloat()
        val s = sin(t)
        val x = 16f * s * s * s
        val y = 13f * cos(t) - 5f * cos(2f * t) - 2f * cos(3f * t) - cos(4f * t)
        val px = (x + 16f) / 32f * w
        val py = (1f - (y + 17f) / 30f) * h
        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
    }
    path.close()
    return path
}

@Composable
private fun Figura(forma: Forma, modifier: Modifier, color: Color) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val margen = w * .04f
        val camino = when (forma) {
            Forma.CIRCULO -> Path().apply {
                addOval(androidx.compose.ui.geometry.Rect(margen, margen, w - margen, h - margen))
            }
            Forma.CUADRADO -> Path().apply {
                addRoundRect(
                    androidx.compose.ui.geometry.RoundRect(
                        androidx.compose.ui.geometry.Rect(margen, margen, w - margen, h - margen),
                        androidx.compose.ui.geometry.CornerRadius(w * .16f)
                    )
                )
            }
            Forma.TRIANGULO -> Path().apply {
                moveTo(w / 2f, h * .06f)
                lineTo(w * .95f, h * .92f)
                lineTo(w * .05f, h * .92f)
                close()
            }
            Forma.ESTRELLA -> trazarEstrella(w, h)
            Forma.CORAZON -> trazarCorazon(w, h)
            Forma.OVALO -> Path().apply {
                addOval(androidx.compose.ui.geometry.Rect(margen, h * .18f, w - margen, h * .82f))
            }
            Forma.RECTANGULO -> Path().apply {
                addRoundRect(
                    androidx.compose.ui.geometry.RoundRect(
                        androidx.compose.ui.geometry.Rect(margen, h * .23f, w - margen, h * .77f),
                        androidx.compose.ui.geometry.CornerRadius(w * .10f)
                    )
                )
            }
            Forma.ROMBO -> Path().apply {
                moveTo(w / 2f, h * .05f)
                lineTo(w * .95f, h / 2f)
                lineTo(w / 2f, h * .95f)
                lineTo(w * .05f, h / 2f)
                close()
            }
            Forma.HEXAGONO -> trazarPoligono(w, h, 6)
            Forma.LUNA -> trazarLuna(w, h)
        }
        val brillo = androidx.compose.ui.graphics.Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = .55f), Color.Transparent),
            center = Offset(w * .32f, h * .26f),
            radius = w * .65f
        )
        drawPath(camino, color)
        drawPath(camino, brush = brillo)
        drawPath(camino, color = Color.Black.copy(alpha = .16f), style = Stroke(width = w * .045f))
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
                                narrador.felicitar("¡Yupi! ¡Muy bien, Rei! $numero")
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
                                narrador.felicitar(animal.nombre, "¡Yupi! ${actual.silabas}")
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
                                    narrador.felicitar(
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
    var stickerSaltando by rememberSaveable { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val ambiente = rememberInfiniteTransition(label = "stickers vivos")
    val movimiento by ambiente.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1_800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "movimiento del jardín"
    )
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
            Spacer(Modifier.height(12.dp))
            // Los premios forman un pequeño jardín jugable, no solo una lista.
            Box(
                Modifier.fillMaxWidth().height(245.dp)
                    .background(Color(0xFFDDF3B5), RoundedCornerShape(34.dp))
                    .border(4.dp, Color.White.copy(.75f), RoundedCornerShape(34.dp))
            ) {
                Text(
                    "El jardín de Rei",
                    Modifier.align(Alignment.TopCenter).padding(8.dp),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Tinta
                )
                val lugares = listOf(
                    Alignment.BottomStart, Alignment.TopEnd, Alignment.Center,
                    Alignment.BottomEnd, Alignment.CenterStart
                )
                recompensas.filter { it.desbloqueado }.forEachIndexed { indice, recompensa ->
                    val salto = if (stickerSaltando == recompensa.nombre) 1.24f else 1f
                    val escalaAmbiente = if (recompensa.nombre == "Sol") 1f + (movimiento+1f)*.025f else 1f
                    Image(
                        painterResource(recompensa.icono),
                        recompensa.nombre,
                        Modifier.align(lugares[indice % lugares.size])
                            .padding(18.dp).size(82.dp)
                            .graphicsLayer {
                                scaleX = salto * escalaAmbiente
                                scaleY = salto * escalaAmbiente
                                translationX = if (recompensa.nombre == "Nube") movimiento * 12.dp.toPx() else 0f
                                translationY = if (recompensa.nombre == "Oso") -((movimiento+1f)*4.dp.toPx()) else 0f
                                rotationZ = when (recompensa.nombre) {
                                    "Flor" -> movimiento * 5f
                                    "Oso" -> movimiento * 2.5f
                                    else -> 0f
                                }
                                alpha = if (recompensa.nombre == "Arcoíris") .88f + (movimiento+1f)*.06f else 1f
                            }
                            .clickable {
                                narrador.felicitar("¡${recompensa.nombre}!", "Tu jardín está precioso")
                                stickerSaltando = recompensa.nombre
                                scope.launch {
                                    delay(650)
                                    stickerSaltando = null
                                }
                            }
                    )
                }
                if (recompensas.none { it.desbloqueado }) {
                    Text(
                        "Gana cinco estrellas para plantar tu primer sticker",
                        Modifier.align(Alignment.Center).padding(26.dp),
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = Tinta
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
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
                                narrador.felicitar(animal.nombre, "¡Sí! ¡Muy bien!")
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

private fun nombreColorBurbuja(color: Color): String = when (color) {
    Color(0xFF6FCC9B) -> "¡Burbuja verde!"
    Color(0xFF55A9E8) -> "¡Burbuja azul!"
    Color(0xFFF39ABC) -> "¡Burbuja rosa!"
    Color(0xFFFFD45F) -> "¡Burbuja amarilla!"
    Color(0xFF9B7ED9) -> "¡Burbuja morada!"
    Color(0xFFF5A15D) -> "¡Burbuja naranja!"
    else -> "¡Burbuja!"
}

/** Burbuja que flota suavemente en su lugar y revienta con una animación antes de desaparecer. */
@Composable
private fun BurbujaFlotante(
    burbuja: Burbuja,
    anchoArea: androidx.compose.ui.unit.Dp,
    altoArea: androidx.compose.ui.unit.Dp,
    onReventar: () -> Unit
) {
    val tamano = anchoArea * (burbuja.radio * 2f)
    val transicion = rememberInfiniteTransition(label = "flote_burbuja")
    val duracionFlote = 2600 + (burbuja.id % 5) * 350
    val fase by transicion.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(duracionFlote, easing = LinearEasing)),
        label = "fase_burbuja"
    )
    val desfase = (burbuja.id * 37 % 100) / 100f * (2 * PI).toFloat()
    val flotarX = (cos(fase + desfase) * 7f).dp
    val flotarY = (sin(fase * 1.3f) * 9f).dp

    val escala = remember(burbuja.id) { Animatable(1f) }
    val alfa = remember(burbuja.id) { Animatable(1f) }
    var reventando by remember(burbuja.id) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        Modifier
            .offset(x = anchoArea * burbuja.x + flotarX, y = altoArea * burbuja.y + flotarY)
            .size(tamano)
            .scale(escala.value)
            .alpha(alfa.value)
            .clickable(enabled = !reventando) {
                reventando = true
                Sonidos.burbuja()
                scope.launch {
                    escala.animateTo(1.4f, tween(150))
                    alfa.animateTo(0f, tween(150))
                    onReventar()
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
            BoxWithConstraints(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White.copy(.35f), RoundedCornerShape(36.dp))
            ) {
                val anchoArea = maxWidth
                val altoArea = maxHeight
                burbujas.forEach { burbuja ->
                    BurbujaFlotante(
                        burbuja = burbuja,
                        anchoArea = anchoArea,
                        altoArea = altoArea,
                        onReventar = {
                            narrador.decir(nombreColorBurbuja(burbuja.color))
                            if (vm.reventar(burbuja.id)) {
                                Sonidos.estrella()
                                premiar()
                            }
                        }
                    )
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

    val articulo = if (objeto.femenino) "la" else "el"
    fun tamano(esGrande: Boolean) = if (esGrande) "grande" else if (objeto.femenino) "pequeña" else "pequeño"

    fun preguntar() {
        narrador.decir("Toca $articulo ${objeto.nombre} ${tamano(pideGrande)}")
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
                "¿Cuál es $articulo ${tamano(pideGrande)}?",
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
                                narrador.felicitar("¡Muy bien!", objeto.nombre)
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
        narrador.decir("Lleva la pelota a la cesta ${objetivo.femenino}")
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
                                narrador.felicitar("¡Sí!", "Cesta ${cesta.femenino}")
                            } else {
                                Sonidos.errorSuave()
                                narrador.decir("Oh, no. Busca la cesta ${objetivo.femenino}")
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
                                narrador.felicitar(pieza.nombre, "¡Completaste el dibujo!")
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

/**
 * Dibuja la prenda (con su estilo real: corona, sombrero, mochila, tenis...) dentro
 * de un rectángulo arbitrario. La usan tanto el ícono de la bandeja como Rei puesta,
 * así los dos SIEMPRE muestran la misma forma — si no, la bandeja podía mostrar una
 * corona y Rei terminaba con un gorro genérico que no tenía nada que ver.
 */
private fun DrawScope.dibujarPrenda(
    estilo: EstiloPrenda,
    color: Color,
    origenX: Float,
    origenY: Float,
    ancho: Float,
    alto: Float
) {
    fun x(f: Float) = origenX + ancho * f
    fun y(f: Float) = origenY + alto * f
    val borde = Color(0xFF5B6170).copy(alpha = .62f)
    when (estilo.tipo) {
        TipoPrenda.GORRO -> when (estilo) {
            EstiloPrenda.CORONA -> {
                val p = Path().apply {
                    moveTo(x(.12f), y(.78f)); lineTo(x(.12f), y(.28f))
                    lineTo(x(.34f), y(.53f)); lineTo(x(.5f), y(.18f))
                    lineTo(x(.66f), y(.53f)); lineTo(x(.88f), y(.28f))
                    lineTo(x(.88f), y(.78f)); close()
                }
                drawPath(p, color)
                drawCircle(borde, ancho * .035f, Offset(x(.5f), y(.65f)))
            }
            EstiloPrenda.LAZO -> {
                drawOval(color, Offset(x(.08f), y(.28f)), Size(ancho * .40f, alto * .42f))
                drawOval(color, Offset(x(.52f), y(.28f)), Size(ancho * .40f, alto * .42f))
                drawCircle(borde, ancho * .11f, Offset(x(.5f), y(.5f)))
            }
            EstiloPrenda.OREJITAS -> {
                drawOval(color, Offset(x(.18f), y(.02f)), Size(ancho * .24f, alto * .68f))
                drawOval(color, Offset(x(.58f), y(.02f)), Size(ancho * .24f, alto * .68f))
                drawRoundRect(
                    color, Offset(x(.10f), y(.62f)), Size(ancho * .80f, alto * .20f),
                    androidx.compose.ui.geometry.CornerRadius(ancho * .1f)
                )
            }
            EstiloPrenda.SOMBRERO, EstiloPrenda.SOMBRERO_SOL -> {
                drawOval(color, Offset(x(.04f), y(.57f)), Size(ancho * .92f, alto * .25f))
                drawRoundRect(
                    color, Offset(x(.28f), y(.18f)), Size(ancho * .44f, alto * .50f),
                    androidx.compose.ui.geometry.CornerRadius(ancho * .12f)
                )
                drawLine(borde, Offset(x(.30f), y(.55f)), Offset(x(.70f), y(.55f)), ancho * .035f)
            }
            EstiloPrenda.TIARA -> {
                drawArc(
                    color, 190f, 160f, false, Offset(x(.15f), y(.20f)), Size(ancho * .70f, alto * .60f),
                    style = Stroke(ancho * .11f, cap = StrokeCap.Round)
                )
                drawCircle(color, ancho * .10f, Offset(x(.5f), y(.25f)))
            }
            else -> {
                drawCircle(color, ancho * .105f, Offset(x(.5f), y(.13f)))
                drawArc(color, 180f, 180f, true, Offset(x(.16f), y(.18f)), Size(ancho * .68f, alto * .82f))
                drawRoundRect(
                    color, Offset(x(.10f), y(.63f)), Size(ancho * .80f, alto * .22f),
                    androidx.compose.ui.geometry.CornerRadius(ancho * .10f)
                )
            }
        }
        TipoPrenda.BUFANDA -> when (estilo) {
            EstiloPrenda.COLLAR -> {
                drawArc(
                    color, 15f, 150f, false, Offset(x(.16f), y(.12f)), Size(ancho * .68f, alto * .62f),
                    style = Stroke(ancho * .08f, cap = StrokeCap.Round)
                )
                drawCircle(color, ancho * .12f, Offset(x(.5f), y(.68f)))
            }
            EstiloPrenda.CAPA -> {
                val p = Path().apply {
                    moveTo(x(.5f), y(.12f)); lineTo(x(.90f), y(.88f))
                    lineTo(x(.10f), y(.88f)); close()
                }
                drawPath(p, color)
                drawCircle(borde, ancho * .07f, Offset(x(.5f), y(.2f)))
            }
            EstiloPrenda.MOCHILA -> {
                drawRoundRect(
                    color, Offset(x(.20f), y(.18f)), Size(ancho * .60f, alto * .70f),
                    androidx.compose.ui.geometry.CornerRadius(ancho * .16f)
                )
                drawRoundRect(
                    borde, Offset(x(.33f), y(.08f)), Size(ancho * .34f, alto * .18f),
                    androidx.compose.ui.geometry.CornerRadius(ancho * .08f)
                )
            }
            EstiloPrenda.CHALECO, EstiloPrenda.SUETER, EstiloPrenda.CHAQUETA,
            EstiloPrenda.VESTIDO, EstiloPrenda.CAMISETA, EstiloPrenda.IMPERMEABLE -> {
                val p = Path().apply {
                    moveTo(x(.30f), y(.12f)); lineTo(x(.08f), y(.32f))
                    lineTo(x(.23f), y(.52f)); lineTo(x(.30f), y(.42f))
                    lineTo(x(.24f), y(.90f)); lineTo(x(.76f), y(.90f))
                    lineTo(x(.70f), y(.42f)); lineTo(x(.77f), y(.52f))
                    lineTo(x(.92f), y(.32f)); lineTo(x(.70f), y(.12f)); close()
                }
                drawPath(p, color)
                drawLine(borde, Offset(x(.5f), y(.22f)), Offset(x(.5f), y(.82f)), ancho * .025f)
            }
            else -> {
                drawRoundRect(
                    color, Offset(x(.08f), y(.22f)), Size(ancho * .73f, alto * .28f),
                    androidx.compose.ui.geometry.CornerRadius(ancho * .12f)
                )
                drawRoundRect(
                    color, Offset(x(.56f), y(.38f)), Size(ancho * .27f, alto * .44f),
                    androidx.compose.ui.geometry.CornerRadius(ancho * .08f)
                )
                repeat(3) { i ->
                    val fx = .60f + i * .085f
                    drawLine(color, Offset(x(fx), y(.76f)), Offset(x(fx), y(.92f)), ancho * .045f, cap = StrokeCap.Round)
                }
            }
        }
        TipoPrenda.BOTAS -> {
            listOf(.08f, .55f).forEach { fx ->
                val altoBota = if (estilo in listOf(
                        EstiloPrenda.SANDALIAS, EstiloPrenda.PANTUFLAS,
                        EstiloPrenda.ZAPATILLAS, EstiloPrenda.TENIS
                    )
                ) .48f else .22f
                val bota = Path().apply {
                    moveTo(x(fx), y(altoBota)); lineTo(x(fx + .28f), y(altoBota))
                    lineTo(x(fx + .28f), y(.62f)); lineTo(x(fx + .39f), y(.68f))
                    quadraticTo(x(fx + .43f), y(.72f), x(fx + .38f), y(.84f))
                    lineTo(x(fx), y(.84f)); close()
                }
                drawPath(bota, color)
                if (estilo !in listOf(EstiloPrenda.SANDALIAS, EstiloPrenda.PANTUFLAS)) {
                    drawRoundRect(
                        color, Offset(x(fx - .025f), y(.16f)), Size(ancho * .33f, alto * .17f),
                        androidx.compose.ui.geometry.CornerRadius(ancho * .06f)
                    )
                }
                drawLine(
                    borde, Offset(x(fx), y(.84f)), Offset(x(fx + .38f), y(.84f)),
                    strokeWidth = ancho * .045f, cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
private fun ReiConRopa(prendas: List<Prenda>, puestas: Set<TipoPrenda>, modifier: Modifier) {
    val prendaDe = { tipo: TipoPrenda -> prendas.firstOrNull { it.tipo == tipo } }
    Canvas(modifier) {
        val w = size.width; val h = size.height
        val piel = Color(0xFFFFD7BC)
        val cabello = Color(0xFF65452F)
        // Piernas, cuerpo, brazos y cabeza de una pequeña Rei.
        drawRoundRect(piel, Offset(w*.39f, h*.73f), Size(w*.09f, h*.20f),
            androidx.compose.ui.geometry.CornerRadius(w*.04f))
        drawRoundRect(piel, Offset(w*.52f, h*.73f), Size(w*.09f, h*.20f),
            androidx.compose.ui.geometry.CornerRadius(w*.04f))
        drawRoundRect(Color(0xFFFFAFC5), Offset(w*.30f, h*.47f), Size(w*.40f, h*.34f),
            androidx.compose.ui.geometry.CornerRadius(w*.10f))
        drawRoundRect(piel, Offset(w*.20f, h*.51f), Size(w*.13f, h*.27f),
            androidx.compose.ui.geometry.CornerRadius(w*.06f))
        drawRoundRect(piel, Offset(w*.67f, h*.51f), Size(w*.13f, h*.27f),
            androidx.compose.ui.geometry.CornerRadius(w*.06f))
        drawCircle(cabello, radius = w*.235f, center = Offset(w*.5f, h*.29f))
        drawCircle(piel, radius = w*.205f, center = Offset(w*.5f, h*.32f))
        // Flequillo, ojos, mejillas y sonrisa.
        drawCircle(cabello, radius = w*.10f, center = Offset(w*.39f, h*.18f))
        drawCircle(cabello, radius = w*.09f, center = Offset(w*.53f, h*.17f))
        drawCircle(Tinta, radius = w*.018f, center = Offset(w*.43f, h*.31f))
        drawCircle(Tinta, radius = w*.018f, center = Offset(w*.57f, h*.31f))
        drawCircle(Color(0xFFFF9FA8).copy(.65f), radius = w*.035f, center = Offset(w*.37f, h*.37f))
        drawCircle(Color(0xFFFF9FA8).copy(.65f), radius = w*.035f, center = Offset(w*.63f, h*.37f))
        drawArc(Tinta, 18f, 144f, false, Offset(w*.44f,h*.34f), Size(w*.12f,h*.08f),
            style = Stroke(w*.012f, cap = StrokeCap.Round))
        if (TipoPrenda.GORRO in puestas) prendaDe(TipoPrenda.GORRO)?.let { prenda ->
            dibujarPrenda(prenda.estilo, prenda.color, w * .20f, -h * .04f, w * .56f, h * .56f)
        }
        if (TipoPrenda.BUFANDA in puestas) prendaDe(TipoPrenda.BUFANDA)?.let { prenda ->
            dibujarPrenda(prenda.estilo, prenda.color, w * .18f, h * .40f, w * .60f, h * .56f)
        }
        if (TipoPrenda.BOTAS in puestas) prendaDe(TipoPrenda.BOTAS)?.let { prenda ->
            dibujarPrenda(prenda.estilo, prenda.color, w * .18f, h * .70f, w * .64f, h * .26f)
        }
    }
}

@Composable
private fun IconoPrenda(prenda: Prenda, modifier: Modifier) {
    Canvas(modifier) {
        // Mismo dibujo exacto que ve Rei puesta, en blanco para resaltar sobre el
        // botón de color: así la bandeja nunca promete algo distinto de lo que pasa.
        dibujarPrenda(prenda.estilo, Color.White, 0f, 0f, size.width, size.height)
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
    val entradaPrenda = remember { Animatable(1f) }

    LaunchedEffect(prendas) {
        delay(500)
        narrador.decir("Vamos a vestir a Rei. Toca la ropa")
    }
    LaunchedEffect(puestas.size) {
        if (puestas.isNotEmpty()) {
            entradaPrenda.snapTo(.88f)
            entradaPrenda.animateTo(1.08f, tween(170))
            entradaPrenda.animateTo(1f, tween(180))
        }
    }

    Fondo {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Cabecera("Vestir a Rei", onVolver)
            ReiConRopa(prendas, puestas, Modifier.size(300.dp).scale(entradaPrenda.value))
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
                            narrador.decir("${prenda.articulo} ${prenda.nombre}")
                            if (vm.tocar(prenda)) {
                                Sonidos.pop(); premiar()
                                if (vm.completo) {
                                    scope.launch {
                                        delay(600)
                                        Sonidos.estrella()
                                        narrador.felicitar("¡Rei ya está lista!", "¡Qué bonita!")
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
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconoPrenda(prenda, Modifier.size(76.dp))
                            Text(
                                prenda.nombre.replaceFirstChar { it.uppercase() },
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                maxLines = 1
                            )
                        }
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
                                narrador.felicitar("¡Sí!", opcion.nombre)
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
                                narrador.felicitar(
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
                    if (completo) narrador.felicitar("¡Terminaste la rutina, Rei!")
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
private fun IconoHabitat(habitat: Habitat, modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width; val h = size.height
        when (habitat) {
            Habitat.GRANJA -> {
                drawRoundRect(
                    Color(0xFFE9C08D),
                    topLeft = Offset(w * .2f, h * .45f),
                    size = Size(w * .6f, h * .4f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f)
                )
                val techo = Path().apply {
                    moveTo(w * .12f, h * .45f)
                    lineTo(w * .5f, h * .12f)
                    lineTo(w * .88f, h * .45f)
                    close()
                }
                drawPath(techo, Color(0xFFE95D68))
            }
            Habitat.MAR -> {
                drawCircle(Color(0xFF55A9E8), radius = w * .42f, center = Offset(w * .5f, h * .56f))
                repeat(3) { i ->
                    drawArc(
                        color = Color.White,
                        startAngle = 200f,
                        sweepAngle = 140f,
                        useCenter = false,
                        topLeft = Offset(w * (.18f + i * .02f), h * (.4f + i * .13f)),
                        size = Size(w * .64f, h * .18f),
                        style = Stroke(width = w * .035f, cap = StrokeCap.Round)
                    )
                }
            }
            Habitat.SELVA -> {
                drawRoundRect(
                    Color(0xFF9B7146),
                    topLeft = Offset(w * .44f, h * .55f),
                    size = Size(w * .12f, h * .35f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f)
                )
                drawCircle(Color(0xFF6FCC9B), radius = w * .34f, center = Offset(w * .5f, h * .38f))
            }
        }
    }
}

@Composable
fun DondeViveScreen(
    vm: DondeViveViewModel,
    narrador: Narrador,
    premiar: () -> Unit,
    onVolver: () -> Unit
) {
    val indice by vm.indice.collectAsStateWithLifecycle()
    val objetivo = vm.objetivo
    val opciones = remember(indice) { vm.opciones() }

    fun preguntar() {
        narrador.decir("¿Dónde vive el ${objetivo.nombre}?")
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
            Cabecera("¿Dónde Vive?", onVolver)
            Button(
                onClick = { preguntar() },
                modifier = Modifier.size(220.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(.78f)),
                elevation = ButtonDefaults.buttonElevation(8.dp)
            ) {
                Image(
                    painterResource(objetivo.icono),
                    contentDescription = objetivo.nombre,
                    modifier = Modifier.size(150.dp)
                )
            }
            Text(
                "Escucha y toca su hogar",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Tinta,
                textAlign = TextAlign.Center
            )
            Row(
                Modifier.fillMaxWidth().height(200.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                opciones.forEachIndexed { i, habitat ->
                    Button(
                        onClick = {
                            if (vm.comprobar(habitat)) {
                                Sonidos.estrella(); premiar()
                                narrador.felicitar("¡Sí!", "Vive en ${habitat.etiqueta}")
                            } else {
                                Sonidos.errorSuave()
                                narrador.decir("Oh, no. Vive en ${objetivo.habitat.etiqueta}")
                            }
                        },
                        modifier = Modifier.weight(1f).fillMaxSize(),
                        shape = RoundedCornerShape(36.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = listOf(Menta, Rosa)[i % 2]
                        ),
                        elevation = ButtonDefaults.buttonElevation(7.dp)
                    ) {
                        IconoHabitat(habitat, Modifier.size(110.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CaritaRei(modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width; val h = size.height
        drawCircle(Color(0xFFE9C08D), radius = w * .48f, center = Offset(w * .5f, h * .5f))
        drawCircle(Tinta, radius = w * .07f, center = Offset(w * .36f, h * .45f))
        drawCircle(Tinta, radius = w * .07f, center = Offset(w * .64f, h * .45f))
        drawCircle(Color(0xFFF6DDB6), radius = w * .18f, center = Offset(w * .5f, h * .63f))
    }
}

@Composable
fun LaberintoScreen(
    vm: LaberintoViewModel,
    narrador: Narrador,
    premiar: () -> Unit,
    onVolver: () -> Unit
) {
    val trayecto by vm.trayecto.collectAsStateWithLifecycle()
    val densidad = LocalDensity.current

    LaunchedEffect(trayecto) {
        delay(500)
        narrador.decir("Arrastra a Rei por el camino hasta la estrella")
    }

    Fondo {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Cabecera("El Camino de Rei", onVolver)
            Text(
                "Lleva a Rei hasta la estrella",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Tinta,
                textAlign = TextAlign.Center
            )
            BoxWithConstraints(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White.copy(.35f), RoundedCornerShape(32.dp))
            ) {
                val anchoPx = with(densidad) { maxWidth.toPx() }
                val altoPx = with(densidad) { maxHeight.toPx() }
                val personajePx = with(densidad) { 46.dp.toPx() }
                val inicioPx = remember(trayecto, anchoPx, altoPx) {
                    Offset(anchoPx * trayecto.ax, altoPx * trayecto.ay)
                }
                val metaPx = remember(trayecto, anchoPx, altoPx) {
                    Offset(anchoPx * trayecto.bx, altoPx * trayecto.by)
                }
                var posicion by remember(trayecto, anchoPx, altoPx) { mutableStateOf(inicioPx) }
                var llego by remember(trayecto) { mutableStateOf(false) }

                Canvas(Modifier.matchParentSize()) {
                    val medio = Offset(
                        (inicioPx.x + metaPx.x) / 2f,
                        minOf(inicioPx.y, metaPx.y) - size.height * .08f
                    )
                    val camino = Path().apply {
                        moveTo(inicioPx.x, inicioPx.y)
                        quadraticTo(medio.x, medio.y, metaPx.x, metaPx.y)
                    }
                    drawPath(
                        camino,
                        color = Color(0xFFFFD45F),
                        style = Stroke(width = size.width * .12f, cap = StrokeCap.Round)
                    )
                    drawPath(
                        camino,
                        color = Color.White,
                        style = Stroke(
                            width = size.width * .018f,
                            cap = StrokeCap.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 18f))
                        )
                    )
                    drawCircle(Color(0xFF6FCC9B), radius = size.width * .08f, center = metaPx)
                    drawCircle(Color.White, radius = size.width * .05f, center = metaPx)
                }
                Box(
                    Modifier
                        .offset {
                            IntOffset(
                                (posicion.x - personajePx / 2f).roundToInt(),
                                (posicion.y - personajePx / 2f).roundToInt()
                            )
                        }
                        .size(with(densidad) { personajePx.toDp() })
                        .pointerInput(trayecto, anchoPx, altoPx) {
                            detectDragGestures(
                                onDrag = { cambio, arrastre ->
                                    cambio.consume()
                                    posicion = Offset(
                                        (posicion.x + arrastre.x).coerceIn(0f, anchoPx),
                                        (posicion.y + arrastre.y).coerceIn(0f, altoPx)
                                    )
                                },
                                onDragEnd = {
                                    val distancia = hypot(posicion.x - metaPx.x, posicion.y - metaPx.y)
                                    if (!llego && distancia < anchoPx * .16f) {
                                        llego = true
                                        Sonidos.estrella()
                                        premiar()
                                        narrador.felicitar("¡Llegaste, Rei!", "¡Muy bien!")
                                        vm.llegar()
                                    } else {
                                        Sonidos.pop()
                                        posicion = inicioPx
                                    }
                                }
                            )
                        }
                ) {
                    CaritaRei(Modifier.fillMaxSize())
                }
            }
        }
    }
}

private fun sonarInstrumento(instrumento: Instrumento) = when (instrumento) {
    Instrumento.TAMBOR -> Sonidos.tambor()
    Instrumento.CAMPANA -> Sonidos.campana()
    Instrumento.XILOFONO -> Sonidos.xilofono()
    Instrumento.MARACAS -> Sonidos.maracas()
}

@Composable
private fun IconoInstrumento(instrumento: Instrumento, modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        when (instrumento) {
            Instrumento.TAMBOR -> {
                drawRoundRect(
                    Color(0xFFE95D68),
                    topLeft = Offset(w * .18f, h * .30f),
                    size = Size(w * .64f, h * .48f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * .06f)
                )
                drawOval(Color(0xFFFFF3DD), topLeft = Offset(w * .18f, h * .18f), size = Size(w * .64f, h * .22f))
                drawOval(
                    Color.Black.copy(alpha = .15f),
                    topLeft = Offset(w * .18f, h * .18f),
                    size = Size(w * .64f, h * .22f),
                    style = Stroke(width = w * .02f)
                )
                drawOval(Color(0xFFC1414F), topLeft = Offset(w * .18f, h * .68f), size = Size(w * .64f, h * .16f))
                drawLine(
                    Color(0xFF7A4B2E), Offset(w * .30f, h * .15f), Offset(w * .42f, h * .32f),
                    strokeWidth = w * .035f, cap = StrokeCap.Round
                )
                drawLine(
                    Color(0xFF7A4B2E), Offset(w * .70f, h * .15f), Offset(w * .58f, h * .32f),
                    strokeWidth = w * .035f, cap = StrokeCap.Round
                )
            }
            Instrumento.CAMPANA -> {
                val cuerpo = Path().apply {
                    moveTo(w * .35f, h * .75f)
                    lineTo(w * .30f, h * .35f)
                    quadraticTo(w * .5f, h * .12f, w * .70f, h * .35f)
                    lineTo(w * .65f, h * .75f)
                    close()
                }
                drawPath(cuerpo, Color(0xFFF0C94D))
                drawRoundRect(
                    Color(0xFFE3B23C),
                    topLeft = Offset(w * .28f, h * .73f),
                    size = Size(w * .44f, h * .08f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * .03f)
                )
                drawCircle(Color(0xFFE3B23C), radius = w * .06f, center = Offset(w * .5f, h * .88f))
                drawCircle(Color(0xFFE3B23C), radius = w * .05f, center = Offset(w * .5f, h * .08f))
            }
            Instrumento.XILOFONO -> {
                val colores = listOf(Color(0xFFE95D68), Color(0xFFF5A15D), Color(0xFF6FCC9B), Color(0xFF55A9E8))
                colores.forEachIndexed { i, color ->
                    val anchoBarra = w * (.78f - i * .12f)
                    drawRoundRect(
                        color,
                        topLeft = Offset(w / 2f - anchoBarra / 2f, h * (.12f + i * .21f)),
                        size = Size(anchoBarra, h * .15f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(h * .05f)
                    )
                }
            }
            Instrumento.MARACAS -> {
                listOf(-1f, 1f).forEach { lado ->
                    val cx = w * .5f + lado * w * .18f
                    drawOval(
                        Color(0xFFB37FEA),
                        topLeft = Offset(cx - w * .16f, h * .15f),
                        size = Size(w * .32f, h * .42f)
                    )
                    drawLine(
                        Color(0xFF7A4B2E), Offset(cx, h * .55f), Offset(cx, h * .90f),
                        strokeWidth = w * .05f, cap = StrokeCap.Round
                    )
                    drawCircle(Color.White.copy(alpha = .5f), radius = w * .03f, center = Offset(cx - w * .05f, h * .28f))
                    drawCircle(Color.White.copy(alpha = .5f), radius = w * .03f, center = Offset(cx + w * .04f, h * .38f))
                }
            }
        }
    }
}

@Composable
fun RitmoScreen(
    vm: RitmoViewModel,
    narrador: Narrador,
    premiar: () -> Unit,
    onVolver: () -> Unit
) {
    val secuencia by vm.secuencia.collectAsStateWithLifecycle()
    val progreso by vm.progreso.collectAsStateWithLifecycle()
    var reproduciendo by remember { mutableStateOf(true) }
    var resaltado by remember { mutableStateOf<Instrumento?>(null) }
    var replayId by remember { mutableStateOf(0) }

    LaunchedEffect(secuencia, replayId) {
        reproduciendo = true
        delay(500)
        narrador.decir("Escucha")
        delay(700)
        secuencia.forEach { instrumento ->
            resaltado = instrumento
            sonarInstrumento(instrumento)
            delay(600)
            resaltado = null
            delay(220)
        }
        narrador.decir("Ahora repite tú")
        reproduciendo = false
    }

    Fondo {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Cabecera("Imita el Ritmo", onVolver)
            Text(
                "Escucha y repite tocando en orden",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Tinta,
                textAlign = TextAlign.Center
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                secuencia.forEachIndexed { i, _ ->
                    Box(
                        Modifier
                            .padding(6.dp)
                            .size(18.dp)
                            .background(if (i < progreso) Menta else Color.White.copy(.5f), CircleShape)
                    )
                }
            }
            Row(
                Modifier.fillMaxWidth().height(150.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(Instrumento.TAMBOR, Instrumento.CAMPANA).forEachIndexed { i, instrumento ->
                    Button(
                        enabled = !reproduciendo,
                        onClick = {
                            sonarInstrumento(instrumento)
                            val resultado = vm.tocar(instrumento)
                            if (resultado.acierto) {
                                if (resultado.rondaCompleta) {
                                    Sonidos.estrella(); premiar()
                                    narrador.felicitar("¡Muy bien, Rei!")
                                } else {
                                    narrador.decir(instrumento.nombre)
                                }
                            } else {
                                Sonidos.errorSuave()
                                narrador.decir("Oh, no. Escucha de nuevo")
                                replayId++
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .scale(if (resaltado == instrumento) 1.15f else 1f),
                        shape = RoundedCornerShape(36.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = listOf(Menta, Rosa)[i]
                        ),
                        elevation = ButtonDefaults.buttonElevation(7.dp)
                    ) { IconoInstrumento(instrumento, Modifier.size(96.dp)) }
                }
            }
            Row(
                Modifier.fillMaxWidth().height(150.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(Instrumento.XILOFONO, Instrumento.MARACAS).forEachIndexed { i, instrumento ->
                    Button(
                        enabled = !reproduciendo,
                        onClick = {
                            sonarInstrumento(instrumento)
                            val resultado = vm.tocar(instrumento)
                            if (resultado.acierto) {
                                if (resultado.rondaCompleta) {
                                    Sonidos.estrella(); premiar()
                                    narrador.felicitar("¡Muy bien, Rei!")
                                } else {
                                    narrador.decir(instrumento.nombre)
                                }
                            } else {
                                Sonidos.errorSuave()
                                narrador.decir("Oh, no. Escucha de nuevo")
                                replayId++
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .scale(if (resaltado == instrumento) 1.15f else 1f),
                        shape = RoundedCornerShape(36.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = listOf(Lila, Melon)[i]
                        ),
                        elevation = ButtonDefaults.buttonElevation(7.dp)
                    ) { IconoInstrumento(instrumento, Modifier.size(96.dp)) }
                }
            }
        }
    }
}

@Composable
fun QueFaltaScreen(
    vm: QueFaltaViewModel,
    narrador: Narrador,
    premiar: () -> Unit,
    onVolver: () -> Unit
) {
    val mostrados by vm.mostrados.collectAsStateWithLifecycle()
    val indiceFaltante by vm.indiceFaltante.collectAsStateWithLifecycle()
    val opciones = remember(mostrados, indiceFaltante) { vm.opciones() }
    var mostrarTodo by remember(mostrados, indiceFaltante) { mutableStateOf(true) }
    var peekId by remember(mostrados, indiceFaltante) { mutableStateOf(0) }

    fun mirarBien() {
        narrador.decir("Mira bien estos juguetes")
    }

    LaunchedEffect(mostrados, indiceFaltante, peekId) {
        mostrarTodo = true
        delay(500)
        mirarBien()
        delay(2_200)
        mostrarTodo = false
        Sonidos.pop()
        narrador.decir("¿Qué falta?")
    }

    Fondo {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Cabecera("¿Qué Falta?", onVolver)
            Text(
                if (mostrarTodo) "Mira bien" else "¿Qué falta? Toca abajo",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Tinta,
                textAlign = TextAlign.Center
            )
            Row(
                Modifier.fillMaxWidth().height(150.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                mostrados.forEachIndexed { i, objeto ->
                    val esElFaltante = i == indiceFaltante
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .background(Color.White.copy(.55f), RoundedCornerShape(32.dp))
                            .clickable(enabled = esElFaltante && !mostrarTodo) { peekId++ },
                        contentAlignment = Alignment.Center
                    ) {
                        if (mostrarTodo || !esElFaltante) {
                            Image(
                                painterResource(objeto.icono),
                                contentDescription = objeto.nombre,
                                modifier = Modifier.size(100.dp)
                            )
                        } else {
                            Text("?", fontSize = 50.sp, fontWeight = FontWeight.ExtraBold, color = Tinta.copy(.4f))
                        }
                    }
                }
            }
            Row(
                Modifier.fillMaxWidth().height(150.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                opciones.forEachIndexed { i, objeto ->
                    Button(
                        enabled = !mostrarTodo,
                        onClick = {
                            if (vm.comprobar(objeto)) {
                                Sonidos.estrella(); premiar()
                                narrador.felicitar(objeto.nombre, "¡Sí, eso faltaba!")
                            } else {
                                Sonidos.errorSuave()
                                narrador.decirSecuencia(objeto.nombre, "Oh, no. Intenta de nuevo")
                            }
                        },
                        modifier = Modifier.weight(1f).fillMaxSize(),
                        shape = RoundedCornerShape(36.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = listOf(Menta, Rosa, Lila)[i]
                        ),
                        elevation = ButtonDefaults.buttonElevation(7.dp)
                    ) {
                        Image(
                            painterResource(objeto.icono),
                            contentDescription = objeto.nombre,
                            modifier = Modifier.size(100.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun etiquetaTamano(tamano: TamanoObjeto, femenino: Boolean): String =
    if (femenino) tamano.etiquetaFemenina else tamano.etiqueta

@Composable
fun OrdenaTamanoScreen(
    vm: OrdenaTamanoViewModel,
    narrador: Narrador,
    premiar: () -> Unit,
    onVolver: () -> Unit
) {
    val objeto = vm.objeto
    val modo by vm.modo.collectAsStateWithLifecycle()
    val posiciones by vm.posiciones.collectAsStateWithLifecycle()
    val progreso by vm.progreso.collectAsStateWithLifecycle()

    fun preguntar() {
        val direccion = if (modo == OrdenTamano.CHICO_A_GRANDE) {
            "del más chico al más grande"
        } else {
            "del más grande al más chico"
        }
        narrador.decir("Toca ${objeto.nombre} en orden, $direccion")
    }

    LaunchedEffect(objeto, modo, posiciones) {
        delay(550)
        preguntar()
    }

    Fondo {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Cabecera("Ordena por Tamaño", onVolver)
            Button(
                onClick = { preguntar() },
                modifier = Modifier.size(90.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Oro)
            ) { IconoAltavoz(Modifier.size(44.dp)) }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                repeat(3) { i ->
                    Box(
                        Modifier
                            .padding(6.dp)
                            .size(16.dp)
                            .background(if (i < progreso) Menta else Color.White.copy(.5f), CircleShape)
                    )
                }
            }
            Row(
                Modifier.fillMaxWidth().height(220.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                posiciones.forEachIndexed { casilla, tamanoIndice ->
                    val tamano = TamanoObjeto.entries[tamanoIndice]
                    val tamanoDp = when (tamano) {
                        TamanoObjeto.CHICO -> 66.dp
                        TamanoObjeto.MEDIANO -> 104.dp
                        TamanoObjeto.GRANDE -> 150.dp
                    }
                    Button(
                        onClick = {
                            val resultado = vm.tocar(casilla)
                            if (resultado.acierto) {
                                narrador.decir(etiquetaTamano(tamano, objeto.femenino))
                                if (resultado.rondaCompleta) {
                                    Sonidos.estrella(); premiar()
                                    narrador.felicitar("¡Muy bien ordenado, Rei!")
                                }
                            } else {
                                Sonidos.errorSuave()
                                narrador.decir("Oh, no. Empecemos de nuevo")
                            }
                        },
                        modifier = Modifier.size(tamanoDp),
                        shape = CircleShape,
                        contentPadding = PaddingValues(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = listOf(Menta, Rosa, Lila)[casilla]
                        ),
                        elevation = ButtonDefaults.buttonElevation(7.dp)
                    ) {
                        Image(
                            painterResource(objeto.icono),
                            contentDescription = objeto.nombre,
                            modifier = Modifier.size(tamanoDp * .68f)
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun CirculoRespiracion(modifier: Modifier) {
    Canvas(modifier) {
        drawCircle(
            brush = androidx.compose.ui.graphics.Brush.radialGradient(
                colors = listOf(Color(0xFFAEE9D1), Color(0xFF6FCC9B))
            )
        )
        drawCircle(
            Color.White.copy(alpha = .32f),
            radius = size.width * .26f,
            center = Offset(size.width * .36f, size.height * .32f)
        )
    }
}

@Composable
fun RespiraScreen(
    vm: RespiraViewModel,
    narrador: Narrador,
    premiar: () -> Unit,
    onVolver: () -> Unit
) {
    val escala = remember { Animatable(.6f) }
    var fase by remember { mutableStateOf("Respira con Rei") }

    LaunchedEffect(Unit) {
        delay(500)
        narrador.decir("Vamos a respirar juntos, despacito")
        delay(1_400)
        while (true) {
            fase = "Inspira"
            narrador.decir("Inspira")
            escala.animateTo(1.15f, tween(3_600, easing = LinearEasing))
            fase = "Exhala"
            narrador.decir("Exhala")
            escala.animateTo(.6f, tween(3_600, easing = LinearEasing))
            if (vm.completarCiclo()) {
                Sonidos.estrella()
                premiar()
                narrador.felicitar("¡Qué bien respiras, Rei!")
                delay(900)
            }
        }
    }

    Fondo {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Cabecera("Respira con Rei", onVolver)
            Box(
                Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CirculoRespiracion(Modifier.size(220.dp).scale(escala.value))
            }
            Text(
                fase,
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Tinta,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
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
