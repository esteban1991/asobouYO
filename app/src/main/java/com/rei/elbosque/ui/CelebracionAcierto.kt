package com.rei.elbosque.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.rei.elbosque.R
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Destello corto y no bloqueante que aparece con cada acierto. */
@Composable
fun CelebracionAcierto(key: Int, alTerminar: () -> Unit) {
    val progreso = remember(key) { Animatable(0f) }
    LaunchedEffect(key) {
        progreso.animateTo(1f, tween(850, easing = FastOutSlowInEasing))
        delay(120)
        alTerminar()
    }
    val p = progreso.value
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val centro = Offset(size.width/2f,size.height*.48f)
            repeat(10) { i ->
                val a=(i*2*PI/10).toFloat()
                val distancia=size.minDimension*(.10f+.25f*p)
                val punto=Offset(centro.x+cos(a)*distancia,centro.y+sin(a)*distancia)
                drawCircle(
                    listOf(Color(0xFFFFD84D),Color(0xFFFF8FB1),Color(0xFF79DCC8))[i%3]
                        .copy(alpha=(1f-p).coerceIn(0f,1f)),
                    radius=(10.dp.toPx()*(1f-p*.5f)),
                    center=punto
                )
            }
        }
        val entrada=(p/.38f).coerceAtMost(1f)
        val salida=((1f-p)/.28f).coerceIn(0f,1f)
        Image(
            painterResource(R.drawable.ic_estrella),
            null,
            Modifier.size(150.dp).scale((.45f+entrada*.75f)*salida).alpha(salida)
        )
    }
}
