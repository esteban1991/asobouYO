package com.rei.elbosque.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.rotate
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

class PlantaViewModel(private val saved: SavedStateHandle) : ViewModel() {
    private val _etapa = MutableStateFlow(saved["planta_etapa"] ?: 0)
    val etapa = _etapa
    private val _variedad = MutableStateFlow(saved["planta_variedad"] ?: 0)
    val variedad = _variedad

    fun regar(): Int {
        if (_etapa.value < 4) {
            _etapa.value++
            saved["planta_etapa"] = _etapa.value
        }
        return _etapa.value
    }

    fun nuevaSemilla() {
        _etapa.value = 0
        _variedad.value = (_variedad.value + 1) % 5
        saved["planta_etapa"] = 0
        saved["planta_variedad"] = _variedad.value
    }
}

@Composable
private fun Planta(etapa: Int, variedad: Int, crecimiento: Float, modifier: Modifier) {
    val flores = listOf(
        Color(0xFFFF7FA6), Color(0xFFFFC84A), Color(0xFF9C82E8),
        Color(0xFF66CFE3), Color(0xFFFF916F)
    )
    Canvas(modifier) {
        val w=size.width; val h=size.height
        val tierraY=h*.79f
        drawOval(Color(0xFF8D623E),Offset(w*.12f,tierraY),Size(w*.76f,h*.17f))
        drawOval(Color(0xFFB98352),Offset(w*.18f,tierraY),Size(w*.64f,h*.10f))
        if (etapa == 0) {
            drawOval(Color(0xFF70452F),Offset(w*.46f,tierraY+h*.015f),Size(w*.08f,h*.045f))
            return@Canvas
        }
        val altoFinal = h * when(etapa) { 1->.18f; 2->.36f; 3->.52f; else->.62f }
        val alto = altoFinal * crecimiento
        val base=Offset(w*.5f,tierraY+h*.02f)
        val punta=Offset(w*.5f,base.y-alto)
        drawLine(Color(0xFF56A85E),base,punta,w*.035f,cap=StrokeCap.Round)
        if (etapa>=2 && crecimiento>.45f) {
            rotate(-25f,pivot=Offset(w*.47f,base.y-alto*.48f)) {
                drawOval(Color(0xFF69C66F),Offset(w*.30f,base.y-alto*.58f),Size(w*.20f,h*.09f))
            }
            rotate(25f,pivot=Offset(w*.53f,base.y-alto*.70f)) {
                drawOval(Color(0xFF55B866),Offset(w*.50f,base.y-alto*.78f),Size(w*.20f,h*.09f))
            }
        }
        if (etapa==3) {
            drawOval(Color(0xFF72BC65),Offset(punta.x-w*.07f,punta.y-h*.055f),Size(w*.14f,h*.11f))
        }
        if (etapa>=4) {
            val color=flores[variedad%flores.size]
            val petalos=5+(variedad%3)
            val radio=w*.115f*crecimiento
            repeat(petalos) { i ->
                val a=(i*360f/petalos)
                rotate(a,punta) {
                    drawOval(color,Offset(punta.x-radio*.52f,punta.y-radio*1.55f),Size(radio,radio*1.55f))
                }
            }
            drawCircle(Color(0xFFFFD85C),radio*.58f,punta)
            drawCircle(Color.White.copy(.45f),radio*.16f,Offset(punta.x-radio*.18f,punta.y-radio*.18f))
        }
    }
}

@Composable
fun PlantaScreen(vm: PlantaViewModel, narrador: Narrador, premiar: () -> Unit, volver: () -> Unit) {
    val etapa by vm.etapa.collectAsStateWithLifecycle()
    val variedad by vm.variedad.collectAsStateWithLifecycle()
    val crecimiento = remember { Animatable(1f) }
    val scope=rememberCoroutineScope()
    var regando by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(500)
        narrador.decir("Riega tu semilla para verla crecer")
    }

    Column(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFFE4F7FF),Color(0xFFFFFAE4)))
        ).padding(16.dp),
        horizontalAlignment=Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
            IconButton(onClick=volver,Modifier.size(60.dp).background(Color.White.copy(.85f),CircleShape)) {
                Text("←",fontSize=40.sp,fontWeight=FontWeight.Bold)
            }
            Text("Mi planta",Modifier.weight(1f),fontSize=34.sp,fontWeight=FontWeight.ExtraBold,
                textAlign=TextAlign.Center,color=Color(0xFF3D5360))
            Spacer(Modifier.size(60.dp))
        }
        Planta(etapa,variedad,crecimiento.value,Modifier.fillMaxWidth().weight(1f))
        if (etapa < 4) {
            Button(
                enabled=!regando,
                onClick={
                    if (regando) return@Button
                    regando=true
                    val nueva=vm.regar()
                    narrador.decir(listOf("La semilla bebe agua","¡Salió un brote!","La planta crece","¡Está saliendo una flor!")[nueva-1])
                    scope.launch {
                        crecimiento.snapTo(.55f)
                        crecimiento.animateTo(1f,tween(1_350,easing=FastOutSlowInEasing))
                        regando=false
                        if (nueva==4) {
                            Sonidos.celebracion(); premiar()
                            narrador.felicitar("¡Qué linda flor creaste, Rei!","Toca la semilla para plantar otra")
                        }
                    }
                },
                modifier=Modifier.fillMaxWidth(.72f).height(115.dp),
                shape=RoundedCornerShape(50),
                colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF82D6DD))
            ) {
                Image(painterResource(R.drawable.ic_regadera),null,Modifier.size(92.dp))
            }
        } else {
            Card(
                onClick={
                    vm.nuevaSemilla()
                    narrador.decir("Una semilla nueva. Vamos a regarla")
                },
                modifier=Modifier.fillMaxWidth(.72f).height(115.dp),
                shape=RoundedCornerShape(50),
                colors=CardDefaults.cardColors(containerColor=Color(0xFFFFD77A))
            ) {
                Row(Modifier.fillMaxSize(),verticalAlignment=Alignment.CenterVertically,
                    horizontalArrangement=Arrangement.Center) {
                    Text("🌱",fontSize=62.sp)
                    Text("Otra flor",fontSize=28.sp,fontWeight=FontWeight.ExtraBold,color=Color(0xFF3D5360))
                }
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}
