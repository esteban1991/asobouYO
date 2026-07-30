package com.rei.elbosque.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import com.rei.elbosque.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rei.elbosque.audio.Narrador
import com.rei.elbosque.audio.Sonidos
import com.rei.elbosque.ui.ReiColores.Fondo1
import com.rei.elbosque.ui.ReiColores.Fondo2
import com.rei.elbosque.ui.ReiColores.Lila
import com.rei.elbosque.ui.ReiColores.Menta
import com.rei.elbosque.ui.ReiColores.Rosa
import com.rei.elbosque.ui.ReiColores.Tinta as BusTinta
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun BusScreen(
    vm: BusViewModel,
    narrador: Narrador,
    premiar: () -> Unit,
    onVolver: () -> Unit
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val posicionBus = remember { Animatable(-1_300f) }
    var viajando by remember { mutableStateOf(false) }
    val objetoPedido = estado.necesarios.firstOrNull { it.nombre !in estado.subidos }
    val repetirConsigna: () -> Unit = {
        objetoPedido?.let {
            narrador.decir("El autobús necesita ${it.nombre}. Arrastra ${it.nombre} al autobús")
        }
        Unit
    }

    LaunchedEffect(Unit) {
        posicionBus.animateTo(0f, tween(900, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(estado.ronda, estado.subidos.size) {
        delay(700)
        repetirConsigna()
    }

    val colocarObjeto: (ObjetoBus) -> Unit = { objeto ->
        val resultado = vm.tocar(objeto)
        if (resultado.acierto) {
            Sonidos.estrella()
            narrador.felicitar("¡Yupi! ¡Muy bien!")
        } else {
            Sonidos.errorSuave()
            narrador.decirSecuencia(objeto.nombre, "Oh, no. Ese no entra aquí")
        }
        if (resultado.autobusCompleto) {
            viajando = true
            premiar()
            scope.launch {
                delay(700)
                narrador.felicitar("¡Autobús completo! ¡Adiós!")
                posicionBus.animateTo(1_300f, tween(900, easing = FastOutSlowInEasing))
                vm.siguienteAutobus()
                posicionBus.snapTo(-1_300f)
                posicionBus.animateTo(0f, tween(850, easing = FastOutSlowInEasing))
                viajando = false
            }
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Fondo1, Fondo2)))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onVolver,
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color.White.copy(.85f), CircleShape)
                ) {
                    Text("←", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = BusTinta)
                }
                Text(
                    "El autobús de Rei",
                    modifier = Modifier.weight(1f),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BusTinta,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.size(60.dp))
            }

            Text(
                "¿Qué necesita el autobús?",
                modifier = Modifier.padding(top = 10.dp),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = BusTinta
            )

            Autobus(
                estado = estado,
                alTocar = repetirConsigna,
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .offset { IntOffset(posicionBus.value.roundToInt(), 0) }
            )

            Text(
                "Toca para escuchar • Arrastra al autobús",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = BusTinta
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                estado.opciones.forEach { objeto ->
                    ObjetoArrastrable(
                        objeto = objeto,
                        habilitado = !viajando,
                        modifier = Modifier.weight(1f),
                        alPresionar = { narrador.decir(objeto.nombre) },
                        alSoltarEnBus = { colocarObjeto(objeto) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ObjetoArrastrable(
    objeto: ObjetoBus,
    habilitado: Boolean,
    modifier: Modifier = Modifier,
    alPresionar: () -> Unit,
    alSoltarEnBus: () -> Unit
) {
    var desplazamientoY by remember(objeto.nombre) { mutableFloatStateOf(0f) }
    Button(
        enabled = habilitado,
        onClick = alPresionar,
        modifier = modifier
            .height(145.dp)
            .offset { IntOffset(0, desplazamientoY.roundToInt()) }
            .zIndex(if (desplazamientoY != 0f) 20f else 0f)
            .pointerInput(objeto.nombre, habilitado) {
                if (!habilitado) return@pointerInput
                detectDragGestures(
                    onDragStart = { alPresionar() },
                    onDrag = { cambio, desplazamiento ->
                        cambio.consume()
                        // Solo interesa llevar el objeto hacia arriba, donde está el autobús.
                        desplazamientoY = (desplazamientoY + desplazamiento.y)
                            .coerceIn(-size.height * 2.2f, size.height * .25f)
                    },
                    onDragEnd = {
                        if (desplazamientoY < -80.dp.toPx()) alSoltarEnBus()
                        desplazamientoY = 0f
                    },
                    onDragCancel = { desplazamientoY = 0f }
                )
            },
        shape = RoundedCornerShape(36.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = listOf(Menta, Rosa, Lila)[objeto.nombre.length % 3]
        ),
        elevation = ButtonDefaults.buttonElevation(8.dp)
    ) {
        Image(
            painter = painterResource(objeto.icono),
            contentDescription = objeto.nombre,
            modifier = Modifier.size(112.dp)
        )
    }
}

@Composable
private fun Autobus(
    estado: EstadoBus,
    alTocar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = alTocar,
        modifier = modifier.fillMaxWidth().height(215.dp),
        shape = RoundedCornerShape(38.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD45F)),
        elevation = CardDefaults.cardElevation(9.dp)
    ) {
        Column(
            Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                estado.necesarios.forEach { objeto ->
                    val subio = objeto.nombre in estado.subidos
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier
                                .size(78.dp)
                                .background(Color(0xFFBDEBFA), RoundedCornerShape(18.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(objeto.icono),
                                contentDescription = objeto.nombre,
                                modifier = Modifier
                                    .size(68.dp)
                                    .alpha(if (subio) 1f else .22f)
                            )
                        }
                        Text(
                            if (subio) objeto.nombre else "Necesita",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = BusTinta
                        )
                    }
                }
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_bus),
                    contentDescription = "Autobús",
                    modifier = Modifier.size(68.dp)
                )
                Text(
                    "${estado.subidos.size} de ${estado.necesarios.size}",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BusTinta
                )
                Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    Box(Modifier.size(28.dp).background(Color(0xFF37474F), CircleShape))
                    Box(Modifier.size(28.dp).background(Color(0xFF37474F), CircleShape))
                }
            }
        }
    }
}
