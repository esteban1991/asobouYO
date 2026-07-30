package com.rei.elbosque.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rei.elbosque.audio.Narrador
import com.rei.elbosque.audio.Sonidos
import com.rei.elbosque.ui.ReiColores.Fondo1
import com.rei.elbosque.ui.ReiColores.Fondo2
import com.rei.elbosque.ui.ReiColores.Tinta as tinta
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ColoresScreen(
    vm: ColoresViewModel,
    narrador: Narrador,
    premiar: () -> Unit,
    onVolver: () -> Unit
) {
    val indice by vm.indice.collectAsStateWithLifecycle()
    val objetivo = vm.colores[indice]
    val opciones = remember(indice) { vm.opciones() }

    fun preguntar() {
        narrador.decir("Toca el color ${objetivo.nombre}")
    }

    LaunchedEffect(indice) {
        delay(600)
        preguntar()
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Fondo1, Fondo2)))
            .padding(16.dp)
    ) {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onVolver,
                    modifier = Modifier.size(60.dp).background(Color.White.copy(.85f), CircleShape)
                ) {
                    Text("←", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = tinta)
                }
                Text(
                    "Los colores de Rei",
                    Modifier.weight(1f),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    color = tinta
                )
                Spacer(Modifier.size(60.dp))
            }

            Button(
                onClick = { preguntar() },
                modifier = Modifier.fillMaxWidth(.85f).height(90.dp),
                shape = RoundedCornerShape(45.dp),
                colors = ButtonDefaults.buttonColors(containerColor = objetivo.color)
            ) {
                IconoSonido(Modifier.size(52.dp))
            }

            Text(
                "Escucha y toca el color",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = tinta,
                textAlign = TextAlign.Center
            )

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                opciones.forEach { opcion ->
                    Button(
                        onClick = {
                            if (vm.comprobar(opcion)) {
                                Sonidos.estrella()
                                premiar()
                                narrador.felicitar(
                                    opcion.nombre,
                                    "¡Yupi! ¡Muy bien!"
                                )
                            } else {
                                Sonidos.errorSuave()
                                narrador.decirSecuencia(
                                    opcion.nombre,
                                    "Oh, no. Busca ${objetivo.nombre}"
                                )
                            }
                        },
                        modifier = Modifier.weight(1f).height(230.dp),
                        shape = RoundedCornerShape(45.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(.82f))
                    ) {
                        if (opcion.icono != null) {
                            Image(
                                painter = painterResource(opcion.icono),
                                contentDescription = opcion.nombre,
                                modifier = Modifier.size(165.dp)
                            )
                        } else {
                            FlorColor(opcion.color, Modifier.size(150.dp))
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

/**
 * Una florcita del color pedido: para los colores que todavía no tienen mascota
 * ilustrada, es más lindo y más "objeto reconocible" que una bola lisa con carita.
 */
@Composable
private fun FlorColor(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val centro = Offset(w * .5f, h * .42f)
        val radioPetalo = w * .23f
        val distancia = w * .21f

        // Tallo y hojita, detrás de los pétalos.
        drawLine(
            color = Color(0xFF6FAE6B),
            start = Offset(centro.x, centro.y + radioPetalo * .5f),
            end = Offset(w * .5f, h * .95f),
            strokeWidth = w * .035f,
            cap = StrokeCap.Round
        )
        val hoja = Path().apply {
            moveTo(w * .5f, h * .78f)
            quadraticTo(w * .70f, h * .74f, w * .60f, h * .88f)
            quadraticTo(w * .52f, h * .86f, w * .5f, h * .78f)
            close()
        }
        drawPath(hoja, Color(0xFF7BC47F))

        // Cinco pétalos alrededor del centro.
        for (i in 0 until 5) {
            val angulo = (-PI / 2 + i * 2 * PI / 5).toFloat()
            val posicion = Offset(
                centro.x + cos(angulo) * distancia,
                centro.y + sin(angulo) * distancia
            )
            drawCircle(color, radius = radioPetalo, center = posicion)
        }
        // Brillo suave sobre toda la flor, como si le diera el sol.
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = .38f), Color.Transparent),
                center = Offset(w * .36f, h * .28f),
                radius = w * .6f
            ),
            radius = w * .48f,
            center = centro
        )
        // Contorno suave: sin esto, los pétalos claros (blanco, beige) se pierden
        // contra el fondo blanco del botón.
        for (i in 0 until 5) {
            val angulo = (-PI / 2 + i * 2 * PI / 5).toFloat()
            val posicion = Offset(
                centro.x + cos(angulo) * distancia,
                centro.y + sin(angulo) * distancia
            )
            drawCircle(
                Color.Black.copy(alpha = .12f),
                radius = radioPetalo,
                center = posicion,
                style = Stroke(width = w * .015f)
            )
        }
        // Centro de la flor.
        drawCircle(Color(0xFFFFE07A), radius = w * .15f, center = centro)
        drawCircle(
            Color.Black.copy(alpha = .14f),
            radius = w * .15f,
            center = centro,
            style = Stroke(width = w * .02f)
        )
    }
}

@Composable
private fun IconoSonido(modifier: Modifier = Modifier) {
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
