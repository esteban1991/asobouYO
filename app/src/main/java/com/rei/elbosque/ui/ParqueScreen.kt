package com.rei.elbosque.ui

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
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
        saved["tobogan_paso"] = _paso.value; saved["tobogan_ronda"] = _ronda.value
        return true
    }

    fun siguienteRonda() {
        _paso.value = 0
        _ronda.value++
        saved["tobogan_paso"] = 0
        saved["tobogan_ronda"] = _ronda.value
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
    val objetivo get() = objetivos[_paso.value.coerceAtMost(2)]
    fun comprobar(o: ObjetoEstacion): Boolean {
        if (o != objetivo) return false
        _paso.value++
        saved["tren_paso"]=_paso.value; saved["tren_estacion"]=_estacion.value
        return true
    }
    fun siguienteEstacion() {
        _paso.value = 0
        _estacion.value = (_estacion.value + 1) % estaciones.size
        saved["tren_paso"] = 0
        saved["tren_estacion"] = _estacion.value
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
        AnimalAnimado(a.icono,a.nombre,Modifier.fillMaxSize().padding(6.dp),habilitado)
    }
}

@Composable fun ToboganScreen(vm:ToboganViewModel,narrador:Narrador,premiar:()->Unit,onVolver:()->Unit) {
    val ronda by vm.ronda.collectAsStateWithLifecycle(); val paso by vm.paso.collectAsStateWithLifecycle()
    val animales=remember(ronda){vm.animales}; val scope=rememberCoroutineScope()
    val bajada=remember{Animatable(0f)}
    val festejo=remember{Animatable(1f)}
    var bloqueado by remember{mutableStateOf(false)}
    var animalBajando by remember { mutableStateOf<AnimalParque?>(null) }
    var llegados by remember(ronda) { mutableStateOf(emptyList<AnimalParque>()) }
    var fiesta by remember { mutableStateOf(false) }
    val ordinales=listOf("Primero","Segundo","Tercero")
    LaunchedEffect(ronda,paso){
        if (paso < 3 && !bloqueado) {
            delay(350)
            narrador.decir("${ordinales[paso]} baja el ${animales[paso].nombre}")
        }
    }
    FondoParque("Tobogán de animales",onVolver) {
        Text("${paso.coerceAtMost(3)} de 3",fontSize=28.sp,fontWeight=FontWeight.Bold,color=ReiColores.Tinta)
        Box(Modifier.fillMaxWidth().weight(1f)) {
            Canvas(Modifier.fillMaxSize()){ val p=Path().apply{moveTo(size.width*.68f,size.height*.08f);cubicTo(size.width*.78f,size.height*.38f,size.width*.58f,size.height*.72f,size.width*.16f,size.height*.86f)}
                drawPath(p,Color(0xFFFF8DA8),style=androidx.compose.ui.graphics.drawscope.Stroke(size.width*.16f,cap=androidx.compose.ui.graphics.StrokeCap.Round))
                drawPath(p,Color(0xFFFFD2DC),style=androidx.compose.ui.graphics.drawscope.Stroke(size.width*.10f,cap=androidx.compose.ui.graphics.StrokeCap.Round)) }
            if (paso < 3 && animalBajando == null) {
                Image(
                    painterResource(animales[paso].icono),
                    null,
                    Modifier.size(120.dp).align(Alignment.TopCenter).offset(x=65.dp)
                )
            }
            animalBajando?.let { bajando ->
                Box(
                    Modifier.size(130.dp).align(Alignment.TopCenter)
                        .offset(x=(65f-bajada.value*205f).dp,y=(bajada.value*285f).dp)
                        .rotate(-bajada.value*22f)
                        .scale((1f-bajada.value*.17f)*festejo.value),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Image(painterResource(bajando.icono),bajando.nombre,Modifier.fillMaxSize())
                    if (bajada.value > .96f) {
                        Text("🙌",fontSize=38.sp,modifier=Modifier.offset(y=(-24).dp))
                    }
                }
            }
            Row(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(105.dp),
                horizontalArrangement=Arrangement.Center,
                verticalAlignment=Alignment.Bottom
            ) {
                llegados.forEach { llegado ->
                    AnimalAnimado(llegado.icono,llegado.nombre,Modifier.size(98.dp).padding(2.dp))
                }
            }
            if (fiesta) FiestaTobogan(Modifier.fillMaxSize())
        }
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){ animales.forEach { a-> OpcionAnimal(a,!bloqueado){
            narrador.decir(a.nombre)
            val esUltimo = paso == 2
            if(vm.comprobar(a)){
                bloqueado=true
                animalBajando=a
                scope.launch{
                    bajada.snapTo(0f)
                    bajada.animateTo(1f,tween(1050,easing=FastOutSlowInEasing))
                    Sonidos.estrella()
                    narrador.felicitar("¡Yupi!")
                    festejo.animateTo(1.28f,tween(160))
                    festejo.animateTo(.92f,tween(130))
                    festejo.animateTo(1.15f,tween(130))
                    festejo.animateTo(1f,tween(180))
                    llegados=llegados+a
                    premiar()
                    animalBajando=null
                    bajada.snapTo(0f)
                    if(esUltimo){
                        fiesta=true
                        Sonidos.celebracion()
                        narrador.felicitar("¡Yupi! ¡Los tres animalitos llegaron!")
                        delay(3200)
                        fiesta=false
                        vm.siguienteRonda()
                    }
                    bloqueado=false
                }
            }
            else { Sonidos.errorSuave(); narrador.decirSecuencia(a.nombre,"Oh, no. ${ordinales[paso]} baja el ${animales[paso].nombre}") }
        }}}
    }
}

@Composable
private fun FiestaTobogan(modifier: Modifier = Modifier) {
    val animacion = remember { Animatable(0f) }
    LaunchedEffect(Unit) { animacion.animateTo(1f, tween(2800)) }
    Canvas(modifier) {
        val colores = listOf(
            Color(0xFFFF6F91), Color(0xFFFFD54F), Color(0xFF66D3C1),
            Color(0xFF8F7AE5), Color(0xFF64B5F6), Color(0xFFFF9F68)
        )
        // Globos que ascienden desde ambos lados.
        repeat(10) { i ->
            val lado = if (i % 2 == 0) .10f else .90f
            val x = size.width * (lado + ((i % 3) - 1) * .06f)
            val y = size.height * (1.12f - animacion.value * (1.05f + (i % 4) * .08f))
            drawOval(
                colores[i % colores.size],
                topLeft = androidx.compose.ui.geometry.Offset(x - 24f, y - 32f),
                size = androidx.compose.ui.geometry.Size(48f, 64f)
            )
            drawLine(Color(0xFF8A7790),androidx.compose.ui.geometry.Offset(x,y+32f),androidx.compose.ui.geometry.Offset(x-7f,y+75f),2f)
        }
        // Serpentinas onduladas que caen por toda la pantalla.
        repeat(30) { i ->
            val x = ((i * 97) % size.width.toInt()).toFloat()
            val y = ((i * 61f + animacion.value * size.height * 1.35f) % (size.height + 80f)) - 40f
            val serpentina = Path().apply {
                moveTo(x,y)
                cubicTo(x+22f,y+12f,x-22f,y+28f,x,y+42f)
            }
            drawPath(serpentina,colores[(i+2)%colores.size],style=androidx.compose.ui.graphics.drawscope.Stroke(7f,cap=androidx.compose.ui.graphics.StrokeCap.Round))
        }
    }
}

@Composable
private fun CaballoCarrusel(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width; val h = size.height
        val crema = Color(0xFFFFF6EE)
        val rosa = Color(0xFFFF9FBD)
        // Poste dorado, de punta a punta.
        drawRoundRect(
            Color(0xFFE3B23C), androidx.compose.ui.geometry.Offset(w * .47f, 0f),
            androidx.compose.ui.geometry.Size(w * .07f, h),
            androidx.compose.ui.geometry.CornerRadius(w * .03f)
        )
        drawRoundRect(
            Color(0xFFFFE07A), androidx.compose.ui.geometry.Offset(w * .49f, 0f),
            androidx.compose.ui.geometry.Size(w * .03f, h)
        )
        // Cola.
        drawOval(rosa, androidx.compose.ui.geometry.Offset(w * .04f, h * .40f), androidx.compose.ui.geometry.Size(w * .16f, h * .30f))
        // Patas en pose de galope.
        drawRoundRect(
            crema, androidx.compose.ui.geometry.Offset(w * .20f, h * .58f),
            androidx.compose.ui.geometry.Size(w * .08f, h * .32f), androidx.compose.ui.geometry.CornerRadius(w * .03f)
        )
        drawRoundRect(
            crema, androidx.compose.ui.geometry.Offset(w * .66f, h * .52f),
            androidx.compose.ui.geometry.Size(w * .08f, h * .24f), androidx.compose.ui.geometry.CornerRadius(w * .03f)
        )
        // Cuerpo.
        val cuerpo = Path().apply {
            moveTo(w * .16f, h * .56f)
            cubicTo(w * .08f, h * .34f, w * .30f, h * .18f, w * .56f, h * .20f)
            cubicTo(w * .76f, h * .22f, w * .87f, h * .34f, w * .87f, h * .48f)
            cubicTo(w * .88f, h * .60f, w * .76f, h * .66f, w * .60f, h * .66f)
            lineTo(w * .28f, h * .66f)
            cubicTo(w * .20f, h * .66f, w * .14f, h * .63f, w * .16f, h * .56f)
            close()
        }
        drawPath(cuerpo, crema)
        drawPath(
            cuerpo, color = Color.Black.copy(.10f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(w * .012f)
        )
        // Cabeza.
        val cabeza = Path().apply {
            moveTo(w * .70f, h * .28f)
            cubicTo(w * .80f, h * .16f, w * .97f, h * .16f, w * .98f, h * .28f)
            cubicTo(w * .99f, h * .38f, w * .90f, h * .42f, w * .81f, h * .40f)
            lineTo(w * .76f, h * .48f)
            close()
        }
        drawPath(cabeza, crema)
        // Crin y silla.
        drawOval(rosa, androidx.compose.ui.geometry.Offset(w * .60f, h * .14f), androidx.compose.ui.geometry.Size(w * .18f, h * .30f))
        drawRoundRect(
            Color(0xFFEF5DA8), androidx.compose.ui.geometry.Offset(w * .34f, h * .36f),
            androidx.compose.ui.geometry.Size(w * .30f, h * .14f), androidx.compose.ui.geometry.CornerRadius(w * .04f)
        )
        drawCircle(Color(0xFFE3B23C), w * .018f, androidx.compose.ui.geometry.Offset(w * .40f, h * .43f))
        drawCircle(Color(0xFFE3B23C), w * .018f, androidx.compose.ui.geometry.Offset(w * .58f, h * .43f))
        // Ojo y sonrisa.
        drawCircle(ReiColores.Tinta, w * .018f, androidx.compose.ui.geometry.Offset(w * .90f, h * .27f))
        drawArc(
            ReiColores.Tinta, 20f, 90f, false,
            androidx.compose.ui.geometry.Offset(w * .82f, h * .30f), androidx.compose.ui.geometry.Size(w * .12f, h * .10f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(w * .012f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
    }
}

@Composable
private fun ToldoCarrusel(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width; val h = size.height
        val colores = listOf(
            Color(0xFFEF5DA8), Color(0xFFFFD54F), Color(0xFF64B5F6),
            Color(0xFF8F7AE5), Color(0xFFFF9F68)
        )
        val centro = androidx.compose.ui.geometry.Offset(w * .5f, h * .96f)
        val radio = w * .5f
        val franjas = colores.size
        for (i in 0 until franjas) {
            val a0 = 180f + i * 180f / franjas
            val a1 = 180f + (i + 1) * 180f / franjas
            drawArc(
                colores[i], a0, a1 - a0, true,
                topLeft = androidx.compose.ui.geometry.Offset(centro.x - radio, centro.y - radio),
                size = androidx.compose.ui.geometry.Size(radio * 2, radio * 2)
            )
        }
        for (i in 0..franjas) {
            val angulo = Math.toRadians((180f + i * 180f / franjas).toDouble())
            drawLine(
                Color.White.copy(.55f), centro,
                androidx.compose.ui.geometry.Offset(
                    centro.x + (radio * kotlin.math.cos(angulo)).toFloat(),
                    centro.y + (radio * kotlin.math.sin(angulo)).toFloat()
                ),
                strokeWidth = w * .01f
            )
        }
        drawLine(
            Color(0xFFE3B23C), androidx.compose.ui.geometry.Offset(w * .5f, 0f),
            androidx.compose.ui.geometry.Offset(w * .5f, centro.y - radio), strokeWidth = w * .02f
        )
        val bandera = Path().apply {
            moveTo(w * .5f, 0f); lineTo(w * .5f + w * .11f, h * .12f); lineTo(w * .5f, h * .22f); close()
        }
        drawPath(bandera, Color(0xFFEF5DA8))
    }
}

@Composable
private fun EstrellitaCarrusel(fase: Float, modifier: Modifier = Modifier) {
    val brillo = kotlin.math.abs(kotlin.math.sin((fase) * Math.PI)).toFloat()
    Canvas(modifier) {
        drawCircle(Color.White.copy(alpha = .4f + brillo * .5f), radius = size.minDimension / 2f)
    }
}

@Composable fun CarruselScreen(vm:CarruselViewModel,narrador:Narrador,premiar:()->Unit,onVolver:()->Unit) {
    val ronda by vm.ronda.collectAsStateWithLifecycle(); val opciones=remember(ronda){vm.opciones}; val objetivo=vm.objetivo
    val giro=remember{Animatable(0f)}; val scope=rememberCoroutineScope(); var bloqueado by remember{mutableStateOf(false)}
    val bamboleo = remember { Animatable(0f) }
    val montaEscala = remember { Animatable(0f) }
    var animalMontado by remember { mutableStateOf<AnimalParque?>(null) }
    val idle = rememberInfiniteTransition(label = "idle_carrusel")
    val vaiven by idle.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1300, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "vaiven"
    )
    val destello by idle.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1600), RepeatMode.Restart),
        label = "destello"
    )
    LaunchedEffect(ronda){delay(350);narrador.decir(if(ronda%3==2) "Toca el animal ${objetivo.color}" else if(ronda%3==1) "Busca el ${objetivo.nombre}" else "Sube al ${objetivo.nombre}")}
    FondoParque("Carrusel de animales",onVolver){
        Box(Modifier.fillMaxWidth().weight(1f),contentAlignment=Alignment.Center){
            ToldoCarrusel(Modifier.fillMaxWidth(.9f).height(90.dp).align(Alignment.TopCenter))
            listOf(.22f to .0f, .78f to .35f, .5f to .18f).forEachIndexed { i, (fx, desfase) ->
                EstrellitaCarrusel(
                    fase = (destello + desfase) % 1f,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(x = ((fx - .5f) * 220f).dp, y = 18.dp)
                        .size(10.dp)
                )
            }
            Canvas(Modifier.fillMaxSize().rotate(giro.value)){drawRect(Color(0xFF7E66C8),topLeft=androidx.compose.ui.geometry.Offset(size.width*.48f,size.height*.15f),size=androidx.compose.ui.geometry.Size(size.width*.04f,size.height*.7f));drawCircle(Color(0xFFFF8FB1),size.width*.38f,androidx.compose.ui.geometry.Offset(size.width*.5f,size.height*.55f));drawCircle(Color(0xFFFFD86D),size.width*.34f,androidx.compose.ui.geometry.Offset(size.width*.5f,size.height*.55f))}
            CaballoCarrusel(
                Modifier
                    .size(160.dp)
                    .offset(y = (vaiven * -6f + bamboleo.value * -16f).dp)
            )
            if (animalMontado == null) {
                Row(Modifier.fillMaxWidth(.88f).rotate(giro.value),horizontalArrangement=Arrangement.SpaceEvenly){opciones.forEach{AnimalAnimado(it.icono,it.nombre,Modifier.size(105.dp))}}
            } else {
                AnimalAnimado(
                    animalMontado!!.icono,
                    animalMontado!!.nombre,
                    Modifier
                        .size(84.dp)
                        .offset(x = 20.dp, y = (12 + vaiven * -6f + bamboleo.value * -16f).dp)
                        .scale(montaEscala.value),
                    activo=!bloqueado
                )
            }
        }
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){opciones.forEach{a->OpcionAnimal(a,!bloqueado){
            narrador.decir(a.nombre)
            if(vm.comprobar(a)){
                bloqueado=true
                animalMontado=a
                Sonidos.carrusel()
                scope.launch{
                    montaEscala.snapTo(.35f)
                    montaEscala.animateTo(1f, tween(260, easing = FastOutSlowInEasing))
                    launch {
                        repeat(4) {
                            bamboleo.animateTo(1f, tween(260, easing = FastOutSlowInEasing))
                            bamboleo.animateTo(0f, tween(260, easing = FastOutSlowInEasing))
                        }
                    }
                    giro.animateTo(giro.value + 360f, tween(2100, easing = FastOutSlowInEasing))
                    narrador.felicitar("¡Muy bien subido, Rei!")
                    premiar()
                    delay(250)
                    montaEscala.animateTo(0f, tween(200))
                    animalMontado=null
                    bloqueado=false
                }
            }
            else {Sonidos.errorSuave();narrador.decirSecuencia(a.nombre,"Oh, no. Busca el ${objetivo.nombre}")}
        }}}
    }
}

@Composable fun TrenParqueScreen(vm:TrenParqueViewModel,narrador:Narrador,premiar:()->Unit,onVolver:()->Unit){
    val estacionIdx by vm.estacion.collectAsStateWithLifecycle();val paso by vm.paso.collectAsStateWithLifecycle()
    val estacion=vm.actual;val objetivo=vm.objetivo;val opciones=remember(estacionIdx,paso){(estacion.objetos.filterNot{it==objetivo}.shuffled().take(2)+objetivo).shuffled()}
    val x=remember{Animatable(-1000f)}
    val yTren=remember{Animatable(20f)}
    val escalaTren=remember{Animatable(.72f)}
    val subida=remember{Animatable(0f)}
    val scope=rememberCoroutineScope()
    var bloqueado by remember{mutableStateOf(true)}
    var pasajeroSubiendo by remember{mutableStateOf<ObjetoEstacion?>(null)}
    var abordo by remember(estacionIdx){mutableStateOf(vm.objetivos.take(paso))}
    LaunchedEffect(estacionIdx){
        bloqueado=true
        x.snapTo(-1050f)
        yTren.snapTo(20f)
        escalaTren.snapTo(.76f)
        Sonidos.trenLlega()
        x.animateTo(0f,tween(1500,easing=FastOutSlowInEasing))
        escalaTren.animateTo(1f,tween(700))
        bloqueado=false
        delay(250)
        narrador.decir("Estación de ${estacion.nombre}. Recoge ${objetivo.nombre}")
    }
    LaunchedEffect(paso){
        if(!bloqueado && paso<3){delay(250);narrador.decir("Ahora recoge ${objetivo.nombre}")}
    }
    FondoParque("Tren del parque",onVolver){
        Text("Estación de ${estacion.nombre}",fontSize=30.sp,fontWeight=FontWeight.ExtraBold,color=ReiColores.Tinta)
        Box(Modifier.fillMaxWidth().weight(1f),contentAlignment=Alignment.Center){
            Image(
                painterResource(R.drawable.paisaje_tren),
                "Paisaje con vías hacia el horizonte",
                Modifier.fillMaxSize(),
                contentScale=ContentScale.Crop
            )
            // Un velo ligero mantiene al tren y a los pasajeros claramente visibles.
            Box(Modifier.fillMaxSize().background(Color.White.copy(alpha=.08f)))
            Box(
                Modifier.fillMaxWidth().offset(x=x.value.dp,y=yTren.value.dp).scale(escalaTren.value),
                contentAlignment=Alignment.Center
            ){
                HumoTren(
                    activo=bloqueado,
                    modifier=Modifier.size(150.dp).align(Alignment.TopEnd).offset(x=(-80).dp,y=(-5).dp)
                )
                // Silueta crema: separa el tren del paisaje sin volver opaca la escena completa.
                Image(
                    painterResource(R.drawable.tren_contraste),
                    null,
                    Modifier.fillMaxWidth().scale(1.035f).offset(y=3.dp),
                    colorFilter=ColorFilter.tint(Color(0xFFFFF3C4))
                )
                Image(
                    painterResource(R.drawable.tren_contraste),
                    "Tren violeta de alto contraste",
                    Modifier.fillMaxWidth()
                )
                // Los pasajeros permanecen visibles tras las cortinas del vagón.
                Row(
                    Modifier.fillMaxWidth(.42f).align(Alignment.CenterStart).offset(x=18.dp,y=(-22).dp),
                    horizontalArrangement=Arrangement.SpaceEvenly
                ){
                    abordo.forEach{AnimalAnimado(it.icono,it.nombre,Modifier.size(55.dp))}
                }
            }
            pasajeroSubiendo?.let { pasajero ->
                Image(
                    painterResource(pasajero.icono),
                    pasajero.nombre,
                    Modifier.size((95f-subida.value*35f).dp)
                        .offset(
                            x=(-subida.value*125f).dp,
                            y=(190f-subida.value*185f).dp
                        )
                        .scale(1f+(.5f-kotlin.math.abs(subida.value-.5f))*.25f)
                )
            }
        }
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){opciones.forEach{o->Card(onClick={
            narrador.decir(o.nombre)
            val esUltimo=paso==2
            if(vm.comprobar(o)){
                bloqueado=true
                pasajeroSubiendo=o
                scope.launch{
                    subida.snapTo(0f)
                    subida.animateTo(1f,tween(900,easing=FastOutSlowInEasing))
                    Sonidos.estrella()
                    narrador.felicitar("¡Yupi! ${o.nombre} subió al tren")
                    abordo=abordo+o
                    pasajeroSubiendo=null
                    subida.snapTo(0f)
                    premiar()
                    if(esUltimo){
                        delay(700)
                        narrador.felicitar("¡Todos a bordo! ¡Nos vamos!")
                        Sonidos.trenParte()
                        // Sigue las vías hacia el punto de fuga central del paisaje.
                        scope.launch{escalaTren.animateTo(.20f,tween(2200,easing=FastOutSlowInEasing))}
                        scope.launch{yTren.animateTo(-245f,tween(2300,easing=FastOutSlowInEasing))}
                        x.animateTo(45f,tween(2300,easing=FastOutSlowInEasing))
                        vm.siguienteEstacion()
                    }else bloqueado=false
                }
            }
            else{Sonidos.errorSuave();narrador.decirSecuencia(o.nombre,"Oh, no. Recoge ${objetivo.nombre}")}
        },enabled=!bloqueado,modifier=Modifier.weight(1f).aspectRatio(.9f),shape=RoundedCornerShape(28.dp),colors=CardDefaults.cardColors(containerColor=Color.White)){Image(painterResource(o.icono),o.nombre,Modifier.fillMaxSize().padding(8.dp))}}}
    }
}

@Composable
private fun HumoTren(activo:Boolean,modifier:Modifier=Modifier){
    if(!activo)return
    val infinito=rememberInfiniteTransition(label="humo tren")
    val avance by infinito.animateFloat(
        initialValue=0f,
        targetValue=1f,
        animationSpec=infiniteRepeatable(tween(1500),repeatMode=RepeatMode.Restart),
        label="subida del humo"
    )
    Canvas(modifier){
        repeat(5){i->
            val fase=(avance+i*.19f)%1f
            val radio=10f+fase*18f
            val x=size.width*.54f+(i%2*2-1)*fase*13f
            val y=size.height*.88f-fase*size.height*.78f
            drawCircle(Color.White.copy(alpha=(.72f-fase*.55f).coerceAtLeast(.08f)),radio,androidx.compose.ui.geometry.Offset(x,y))
            drawCircle(Color(0xFFDCCFE2).copy(alpha=(.25f-fase*.2f).coerceAtLeast(.03f)),radio*.62f,androidx.compose.ui.geometry.Offset(x+4f,y+2f))
        }
    }
}
