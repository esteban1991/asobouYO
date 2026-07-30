package com.rei.elbosque.ui

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource

private val nombresAnimales = setOf(
    "perro","gato","pájaro","pajaro","pez","oso","osito",
    "caballo","conejo","elefante","pato","mono","león","leon"
)

/**
 * Da vida a cualquier animal sin alterar su área táctil.
 * Cada especie tiene un gesto reconocible: salto, aleteo, nado o saludo.
 */
@Composable
fun AnimalAnimado(
    @DrawableRes icono:Int,
    nombre:String,
    modifier:Modifier=Modifier,
    activo:Boolean=true
){
    val esAnimal=nombre.lowercase() in nombresAnimales
    if(!esAnimal || !activo){
        Image(painterResource(icono),nombre,modifier)
        return
    }
    val movimiento=rememberInfiniteTransition(label="movimiento $nombre")
    val fase by movimiento.animateFloat(
        initialValue=-1f,targetValue=1f,
        animationSpec=infiniteRepeatable(
            tween(
                durationMillis=when(nombre.lowercase()){
                    "pájaro","pajaro"->420
                    "pez"->900
                    "conejo"->650
                    else->1100
                },
                easing=FastOutSlowInEasing
            ),
            RepeatMode.Reverse
        ),
        label="gesto $nombre"
    )
    val tipo=nombre.lowercase()
    val rotacion=when(tipo){
        "pájaro","pajaro"->fase*7f       // Aletea y se balancea.
        "mono"->fase*9f                  // Saluda con entusiasmo.
        "elefante"->fase*3f              // Mueve suavemente la trompa.
        "perro","gato"->fase*4f          // Mueve cabeza y cola.
        else->fase*2.5f
    }
    val salto=when(tipo){
        "conejo"->if(fase<0f) 0f else -fase*14f
        "pájaro","pajaro"->fase*8f
        "caballo"->fase*5f
        "pato"->kotlin.math.abs(fase)*-5f
        else->kotlin.math.abs(fase)*-3f
    }
    val desplazamientoX=if(tipo=="pez") fase*12f else 0f
    Image(
        painterResource(icono),
        nombre,
        modifier.graphicsLayer(
            translationX=desplazamientoX,
            translationY=salto,
            rotationZ=rotacion,
            scaleX=1f+kotlin.math.abs(fase)*.025f,
            scaleY=1f-kotlin.math.abs(fase)*.012f,
            transformOrigin=androidx.compose.ui.graphics.TransformOrigin(.5f,.88f)
        )
    )
}
