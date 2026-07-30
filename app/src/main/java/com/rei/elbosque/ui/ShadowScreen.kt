package com.rei.elbosque.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rei.elbosque.audio.Narrador
import com.rei.elbosque.audio.Sonidos
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SombrasScreen(
    vm: SombrasViewModel,
    narrador: Narrador,
    premiar: () -> Unit,
    onVolver: () -> Unit
) {
    val indice by vm.indice.collectAsStateWithLifecycle()
    val objetivo = vm.elementos[indice]
    val opciones = remember(indice) { vm.opciones() }
    val escala = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    val tinta = Color(0xFF3D5360)

    fun preguntar() {
        val pregunta = when (objetivo.tipo) {
            TipoSombra.ANIMAL -> "¿Qué animal es?"
            TipoSombra.PLANTA -> "¿Qué planta es?"
            TipoSombra.OBJETO -> "¿Qué objeto es?"
        }
        narrador.decir("¿De quién es la sombra? $pregunta Toca una imagen")
    }

    LaunchedEffect(indice) {
        delay(650)
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
                    "¿De quién es la sombra?",
                    Modifier.weight(1f),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    color = tinta
                )
                Spacer(Modifier.size(60.dp))
            }

            Button(
                onClick = { preguntar() },
                modifier = Modifier.size(280.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(.72f))
            ) {
                Image(
                    painter = painterResource(objetivo.icono),
                    contentDescription = "Sombra misteriosa",
                    colorFilter = ColorFilter.tint(Color(0xFF263238)),
                    modifier = Modifier.size(235.dp).scale(escala.value)
                )
            }

            Text(
                "Escucha y toca una imagen",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = tinta,
                textAlign = TextAlign.Center
            )

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                opciones.forEachIndexed { posicion, opcion ->
                    Button(
                        onClick = {
                            if (vm.comprobar(opcion)) {
                                Sonidos.estrella()
                                premiar()
                                narrador.decirSecuencia(
                                    opcion.nombre,
                                    "¡Yupi! ¡Muy bien! Es ${objetivo.nombre}"
                                )
                                scope.launch {
                                    escala.animateTo(1.18f, tween(160))
                                    escala.animateTo(1f, tween(220))
                                }
                            } else {
                                Sonidos.errorSuave()
                                narrador.decirSecuencia(
                                    opcion.nombre,
                                    "Oh, no. Mira la sombra otra vez"
                                )
                            }
                        },
                        modifier = Modifier.weight(1f).height(155.dp),
                        shape = RoundedCornerShape(38.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = listOf(
                                Color(0xFF9BE3C5),
                                Color(0xFFFFB7CE),
                                Color(0xFFCAB8F5)
                            )[posicion]
                        )
                    ) {
                        Image(
                            painterResource(opcion.icono),
                            contentDescription = opcion.nombre,
                            modifier = Modifier.size(120.dp)
                        )
                    }
                }
            }
        }
    }
}
