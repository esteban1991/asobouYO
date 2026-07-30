package com.rei.elbosque.ui

import androidx.annotation.DrawableRes
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

data class AnimalParque(val nombre: String, @DrawableRes val icono: Int, val color: String)

val animalesParque = listOf(
    AnimalParque("caballo", R.drawable.parque_caballo, "café"),
    AnimalParque("conejo", R.drawable.parque_conejo, "blanco"),
    AnimalParque("elefante", R.drawable.parque_elefante, "azul"),
    AnimalParque("pato", R.drawable.parque_pato, "amarillo"),
    AnimalParque("mono", R.drawable.parque_mono, "café"),
    AnimalParque("león", R.drawable.parque_leon, "naranja")
)

class ToboganViewModel(private val saved: SavedStateHandle) : ViewModel() {
    private val _ronda = MutableStateFlow(saved["tobogan_ronda"] ?: 0)
    val ronda = _ronda
    private val _paso = MutableStateFlow(saved["tobogan_paso"] ?: 0)
    val paso = _paso
    val animales get() = animalesParque.shuffled(java.util.Random(_ronda.value.toLong())).take(3)
    fun comprobar(animal: AnimalParque): Boolean {
        if (animal != animales[_paso.value]) return false
        _paso.value++
        if (_paso.value == 3) { _paso.value = 0; _ronda.value++ }
        saved["tobogan_paso"] = _paso.value; saved["tobogan_ronda"] = _ronda.value
        return true
    }
}

class CarruselViewModel(private val saved: SavedStateHandle) : ViewModel() {
    private val _ronda = MutableStateFlow(saved["carrusel_ronda"] ?: 0)
    val ronda = _ronda
    val opciones get() = animalesParque.shuffled(java.util.Random((_ronda.value + 81).toLong())).take(3)
    val objetivo get() = opciones[_ronda.value % 3]
    fun comprobar(a: AnimalParque): Boolean {
        if (a != objetivo) return false
        _ronda.value++; saved["carrusel_ronda"] = _ronda.value
        return true
    }
}

data class ObjetoEstacion(val nombre: String, @DrawableRes val icono: Int)
data class Estacion(val nombre: String, val objetos: List<ObjetoEstacion>)
val estaciones = listOf(
    Estacion("animales", animalesParque.map { ObjetoEstacion(it.nombre, it.icono) }),
    Estacion("colores", listOf(ObjetoEstacion("rojo",R.drawable.color_rojo),ObjetoEstacion("azul",R.drawable.color_azul),ObjetoEstacion("amarillo",R.drawable.color_amarillo),ObjetoEstacion("verde",R.drawable.color_verde))),
    Estacion("frutas", listOf(ObjetoEstacion("manzana",R.drawable.ic_manzana),ObjetoEstacion("plátano",R.drawable.ic_platano),ObjetoEstacion("fresa",R.drawable.ic_flor),ObjetoEstacion("naranja",R.drawable.color_naranja))),
    Estacion("juguetes", listOf(ObjetoEstacion("pelota",R.drawable.ic_pelota),ObjetoEstacion("osito",R.drawable.ic_osito),ObjetoEstacion("libro",R.drawable.ic_libro),ObjetoEstacion("paraguas",R.drawable.ic_paraguas)))
)
class TrenParqueViewModel(private val saved: SavedStateHandle) : ViewModel() {
    private val _estacion = MutableStateFlow(saved["tren_estacion"] ?: 0); val estacion = _estacion
    private val _paso = MutableStateFlow(saved["tren_paso"] ?: 0); val paso = _paso
    val actual get() = estaciones[_estacion.value]
    val objetivos get() = actual.objetos.shuffled(java.util.Random((_estacion.value * 17L) + 5)).take(3)
    val objetivo get() = objetivos[_paso.value]
    fun comprobar(o: ObjetoEstacion): Boolean {
        if (o != objetivo) return false
        _paso.value++
        if (_paso.value == 3) { _paso.value = 0; _estacion.value = (_estacion.value + 1) % estaciones.size }
        saved["tren_paso"]=_paso.value; saved["tren_estacion"]=_estacion.value
        return true
    }
}

@Composable private fun FondoParque(titulo:String,onVolver:()->Unit,contenido:@Composable ColumnScope.()->Unit) {
    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFFFF8DE),Color(0xFFDDF8FA)))).padding(14.dp),
        horizontalAlignment=Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
            IconButton(onClick=onVolver,modifier=Modifier.size(62.dp).background(Color.White,CircleShape)){ Text("‹",fontSize=52.sp,color=ReiColores.Tinta) }
            Text(titulo,Modifier.weight(1f),textAlign=TextAlign.Center,fontSize=32.sp,fontWeight=FontWeight.ExtraBold,color=ReiColores.Tinta)
            Spacer(Modifier.size(62.dp))
        }
        contenido()
    }
}

@Composable private fun RowScope.OpcionAnimal(a:AnimalParque,habilitado:Boolean=true,onClick:()->Unit) {
    Card(onClick=onClick,enabled=habilitado,modifier=Modifier.weight(1f).aspectRatio(.9f),
        shape=RoundedCornerShape(30.dp),colors=CardDefaults.cardColors(containerColor=Color.White),elevation=CardDefaults.cardElevation(7.dp)) {
        Image(painterResource(a.icono),a.nombre,Modifier.fillMaxSize().padding(6.dp))
    }
}

@Composable fun ToboganScreen(vm:ToboganViewModel,narrador:Narrador,premiar:()->Unit,onVolver:()->Unit) {
    val ronda by vm.ronda.collectAsStateWithLifecycle(); val paso by vm.paso.collectAsStateWithLifecycle()
    val animales=remember(ronda){vm.animales}; val scope=rememberCoroutineScope()
    val bajada=remember{Animatable(0f)}; var bloqueado by remember{mutableStateOf(false)}
    val ordinales=listOf("Primero","Segundo","Tercero")
    LaunchedEffect(ronda,paso){ delay(350); narrador.decir("${ordinales[paso]} baja el ${animales[paso].nombre}") }
    FondoParque("Tobogán de animales",onVolver) {
        Text("${paso+1} de 3",fontSize=28.sp,fontWeight=FontWeight.Bold,color=ReiColores.Tinta)
        Box(Modifier.fillMaxWidth().weight(1f)) {
            Canvas(Modifier.fillMaxSize()){ val p=Path().apply{moveTo(size.width*.68f,size.height*.08f);cubicTo(size.width*.78f,size.height*.38f,size.width*.58f,size.height*.72f,size.width*.16f,size.height*.86f)}
                drawPath(p,Color(0xFFFF8DA8),style=androidx.compose.ui.graphics.drawscope.Stroke(size.width*.16f,cap=androidx.compose.ui.graphics.StrokeCap.Round))
                drawPath(p,Color(0xFFFFD2DC),style=androidx.compose.ui.graphics.drawscope.Stroke(size.width*.10f,cap=androidx.compose.ui.graphics.StrokeCap.Round)) }
            Image(painterResource(animales[paso].icono),null,Modifier.size(120.dp).align(Alignment.TopCenter).offset(y=(bajada.value*260).dp).scale(1f-bajada.value*.25f))
        }
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){ animales.forEach { a-> OpcionAnimal(a,!bloqueado){
            narrador.decir(a.nombre)
            if(vm.comprobar(a)){ bloqueado=true; Sonidos.estrella(); premiar(); scope.launch{bajada.snapTo(0f);bajada.animateTo(1f,tween(850,easing=FastOutSlowInEasing));bajada.snapTo(0f);bloqueado=false} }
            else { Sonidos.errorSuave(); narrador.decirSecuencia(a.nombre,"Oh, no. ${ordinales[paso]} baja el ${animales[paso].nombre}") }
        }}}
    }
}

@Composable fun CarruselScreen(vm:CarruselViewModel,narrador:Narrador,premiar:()->Unit,onVolver:()->Unit) {
    val ronda by vm.ronda.collectAsStateWithLifecycle(); val opciones=remember(ronda){vm.opciones}; val objetivo=vm.objetivo
    val giro=remember{Animatable(0f)}; val scope=rememberCoroutineScope(); var bloqueado by remember{mutableStateOf(false)}
    LaunchedEffect(ronda){delay(350);narrador.decir(if(ronda%3==2) "Toca el animal ${objetivo.color}" else if(ronda%3==1) "Busca el ${objetivo.nombre}" else "Sube al ${objetivo.nombre}")}
    FondoParque("Carrusel de animales",onVolver){
        Box(Modifier.fillMaxWidth().weight(1f),contentAlignment=Alignment.Center){
            Canvas(Modifier.fillMaxSize()){drawRect(Color(0xFF7E66C8),topLeft=androidx.compose.ui.geometry.Offset(size.width*.48f,size.height*.15f),size=androidx.compose.ui.geometry.Size(size.width*.04f,size.height*.7f));drawCircle(Color(0xFFFF8FB1),size.width*.38f,androidx.compose.ui.geometry.Offset(size.width*.5f,size.height*.55f));drawCircle(Color(0xFFFFD86D),size.width*.34f,androidx.compose.ui.geometry.Offset(size.width*.5f,size.height*.55f))}
            Row(Modifier.fillMaxWidth(.88f).rotate(giro.value),horizontalArrangement=Arrangement.SpaceEvenly){opciones.forEach{Image(painterResource(it.icono),null,Modifier.size(105.dp))}}
        }
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){opciones.forEach{a->OpcionAnimal(a,!bloqueado){
            narrador.decir(a.nombre)
            if(vm.comprobar(a)){bloqueado=true;Sonidos.carrusel();premiar();scope.launch{giro.animateTo(giro.value+720f,tween(2500));bloqueado=false}}
            else {Sonidos.errorSuave();narrador.decirSecuencia(a.nombre,"Oh, no. Busca el ${objetivo.nombre}")}
        }}}
    }
}

@Composable fun TrenParqueScreen(vm:TrenParqueViewModel,narrador:Narrador,premiar:()->Unit,onVolver:()->Unit){
    val estacionIdx by vm.estacion.collectAsStateWithLifecycle();val paso by vm.paso.collectAsStateWithLifecycle()
    val estacion=vm.actual;val objetivo=vm.objetivo;val opciones=remember(estacionIdx,paso){(estacion.objetos.filterNot{it==objetivo}.shuffled().take(2)+objetivo).shuffled()}
    val x=remember{Animatable(0f)};val scope=rememberCoroutineScope();var bloqueado by remember{mutableStateOf(false)}
    LaunchedEffect(estacionIdx,paso){delay(400);narrador.decir("Estación de ${estacion.nombre}. Recoge ${objetivo.nombre}")}
    FondoParque("Tren del parque",onVolver){
        Text("Estación de ${estacion.nombre}",fontSize=30.sp,fontWeight=FontWeight.ExtraBold,color=ReiColores.Tinta)
        Box(Modifier.fillMaxWidth().weight(1f).offset(x=x.value.dp),contentAlignment=Alignment.Center){
            Canvas(Modifier.fillMaxSize()){drawRoundRect(Color(0xFF67C7B1),androidx.compose.ui.geometry.Offset(size.width*.12f,size.height*.35f),androidx.compose.ui.geometry.Size(size.width*.76f,size.height*.34f),androidx.compose.ui.geometry.CornerRadius(35f));drawRect(Color(0xFFFFCF67),androidx.compose.ui.geometry.Offset(size.width*.16f,size.height*.29f),androidx.compose.ui.geometry.Size(size.width*.22f,size.height*.18f));repeat(3){drawCircle(Color(0xFF51466C),size.width*.075f,androidx.compose.ui.geometry.Offset(size.width*(.25f+it*.25f),size.height*.73f))}}
            Row(Modifier.fillMaxWidth(.58f).offset(y=(-10).dp),horizontalArrangement=Arrangement.SpaceAround){vm.objetivos.take(paso).forEach{Image(painterResource(it.icono),null,Modifier.size(65.dp))}}
        }
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){opciones.forEach{o->Card(onClick={
            narrador.decir(o.nombre)
            if(vm.comprobar(o)){Sonidos.estrella();premiar();if(paso==2){bloqueado=true;scope.launch{x.animateTo(900f,tween(850));x.snapTo(-900f);x.animateTo(0f,tween(850));bloqueado=false}}}
            else{Sonidos.errorSuave();narrador.decirSecuencia(o.nombre,"Oh, no. Recoge ${objetivo.nombre}")}
        },enabled=!bloqueado,modifier=Modifier.weight(1f).aspectRatio(.9f),shape=RoundedCornerShape(28.dp),colors=CardDefaults.cardColors(containerColor=Color.White)){Image(painterResource(o.icono),o.nombre,Modifier.fillMaxSize().padding(8.dp))}}}
    }
}
