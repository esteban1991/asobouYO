package com.rei.elbosque.ui

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rei.elbosque.audio.Narrador
import com.rei.elbosque.audio.Sonidos
import kotlinx.coroutines.delay

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
    val tinta = Color(0xFF3D5360)

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
            .background(Brush.verticalGradient(listOf(Color(0xFFFFF9E6), Color(0xFFE0F7FA))))
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
                                narrador.decirSecuencia(
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
                        CaritaColor(opcion.color, Modifier.size(145.dp))
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun CaritaColor(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        drawCircle(color)
        drawCircle(Color(0xFF3D5360), radius = size.width * .045f, center = Offset(size.width * .37f, size.height * .42f))
        drawCircle(Color(0xFF3D5360), radius = size.width * .045f, center = Offset(size.width * .63f, size.height * .42f))
        drawArc(
            color = Color(0xFF3D5360),
            startAngle = 20f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(size.width * .34f, size.height * .45f),
            size = Size(size.width * .32f, size.height * .25f),
            style = Stroke(width = size.width * .035f, cap = StrokeCap.Round)
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
