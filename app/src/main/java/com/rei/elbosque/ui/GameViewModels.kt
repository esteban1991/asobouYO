package com.rei.elbosque.ui

import androidx.annotation.DrawableRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.compose.ui.graphics.Color
import com.rei.elbosque.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random

enum class Forma(val titulo: String) { CIRCULO("Círculo"), CUADRADO("Cuadrado"), TRIANGULO("Triángulo") }

class FormasViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    private val _forma = MutableStateFlow(
        Forma.valueOf(savedState["forma"] ?: Forma.entries.random().name)
    )
    val forma: StateFlow<Forma> = _forma

    fun comprobar(elegida: Forma): Boolean {
        val acierto = elegida == _forma.value
        if (acierto) {
            val opciones = Forma.entries.filterNot { it == _forma.value }
            _forma.value = opciones.random()
            savedState["forma"] = _forma.value.name
        }
        return acierto
    }
}

class NumerosViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    private val _cantidad = MutableStateFlow(savedState["cantidad"] ?: Random.nextInt(1, 6))
    val cantidad: StateFlow<Int> = _cantidad

    fun comprobar(numero: Int): Boolean {
        val acierto = numero == _cantidad.value
        if (acierto) {
            var siguiente: Int
            do siguiente = Random.nextInt(1, 6) while (siguiente == _cantidad.value)
            _cantidad.value = siguiente
            savedState["cantidad"] = siguiente
        }
        return acierto
    }
}

data class Animal(
    val nombre: String,
    @DrawableRes val icono: Int,
    val silabas: String
)

class AnimalesViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    val animales = listOf(
        Animal("Perro", R.drawable.ic_perro, "Pe-rro"),
        Animal("Gato", R.drawable.ic_gato, "Ga-to"),
        Animal("Pájaro", R.drawable.ic_pajaro, "Pá-ja-ro"),
        Animal("Pez", R.drawable.ic_pez, "Pez")
    )
    private val _indice = MutableStateFlow(savedState["animal"] ?: Random.nextInt(animales.size))
    val indice: StateFlow<Int> = _indice

    fun opciones(): List<Animal> =
        (animales.filterNot { it == animales[_indice.value] }.shuffled().take(2) +
            animales[_indice.value]).shuffled()

    fun comprobar(animal: Animal): Boolean {
        val acierto = animal == animales[_indice.value]
        if (acierto) {
            var siguiente: Int
            do siguiente = Random.nextInt(animales.size) while (siguiente == _indice.value)
            _indice.value = siguiente
            savedState["animal"] = siguiente
        }
        return acierto
    }
}

enum class FiguraTrazo(val nombre: String) {
    CIRCULO("círculo"),
    CUADRADO("cuadrado"),
    TRIANGULO("triángulo"),
    RECTANGULO("rectángulo"),
    OVALO("óvalo")
}

class TrazoViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    private val _figura = MutableStateFlow(
        FiguraTrazo.valueOf(savedState["figura_trazo"] ?: FiguraTrazo.CIRCULO.name)
    )
    val figura: StateFlow<FiguraTrazo> = _figura
    private val _progreso = MutableStateFlow(savedState["trazo"] ?: 0f)
    val progreso: StateFlow<Float> = _progreso
    private var completado = savedState["completado"] ?: false

    fun actualizar(valor: Float): Boolean {
        _progreso.value = maxOf(_progreso.value, valor.coerceIn(0f, 1f))
        savedState["trazo"] = _progreso.value
        if (_progreso.value >= .8f && !completado) {
            completado = true
            savedState["completado"] = true
            return true
        }
        return false
    }

    fun reiniciar() {
        _progreso.value = 0f
        completado = false
        savedState["trazo"] = 0f
        savedState["completado"] = false
    }

    fun siguienteFigura() {
        val opciones = FiguraTrazo.entries.filterNot { it == _figura.value }
        _figura.value = opciones.random()
        savedState["figura_trazo"] = _figura.value.name
        reiniciar()
    }
}

/** Objeto cotidiano que Rei puede asociar con su nombre hablado. */
data class ObjetoBus(val nombre: String, @DrawableRes val icono: Int)

data class EstadoBus(
    val ronda: Int,
    val necesarios: List<ObjetoBus>,
    val opciones: List<ObjetoBus>,
    val subidos: Set<String>
)

data class ResultadoBus(val acierto: Boolean, val autobusCompleto: Boolean)

class BusViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    private val objetos = listOf(
        ObjetoBus("Pelota", R.drawable.ic_pelota),
        ObjetoBus("Manzana", R.drawable.ic_manzana),
        ObjetoBus("Osito", R.drawable.ic_osito),
        ObjetoBus("Zapato", R.drawable.ic_zapato),
        ObjetoBus("Libro", R.drawable.ic_libro),
        ObjetoBus("Flor", R.drawable.ic_flor),
        ObjetoBus("Plátano", R.drawable.ic_platano),
        ObjetoBus("Gorra", R.drawable.ic_gorra),
        ObjetoBus("Cuchara", R.drawable.ic_cuchara),
        ObjetoBus("Regalo", R.drawable.ic_regalo),
        ObjetoBus("Paraguas", R.drawable.ic_paraguas),
        ObjetoBus("Gato", R.drawable.ic_gato)
    )

    private val rondas = listOf(
        listOf("Pelota", "Manzana", "Osito"),
        listOf("Zapato", "Libro", "Flor"),
        listOf("Plátano", "Gorra", "Cuchara"),
        listOf("Regalo", "Paraguas", "Gato")
    )

    private val rondaInicial: Int = savedState["ronda_bus"] ?: 0
    private val subidosIniciales =
        (savedState.get<ArrayList<String>>("subidos_bus") ?: arrayListOf()).toSet()

    private val _estado = MutableStateFlow(crearEstado(rondaInicial, subidosIniciales))
    val estado: StateFlow<EstadoBus> = _estado

    private fun crearEstado(ronda: Int, subidos: Set<String>): EstadoBus {
        val indice = ronda.mod(rondas.size)
        val nombresNecesarios = rondas[indice]
        val necesarios = nombresNecesarios.map { nombre -> objetos.first { it.nombre == nombre } }
        val siguienteCorrecto = necesarios.firstOrNull { it.nombre !in subidos }
        // En cada paso hay exactamente tres respuestas: una correcta y dos distractores.
        val opciones = if (siguienteCorrecto == null) {
            emptyList()
        } else {
            val distractores = objetos
                .filterNot { it.nombre in nombresNecesarios || it.nombre in subidos }
                .shuffled()
                .take(2)
            (listOf(siguienteCorrecto) + distractores).shuffled()
        }
        return EstadoBus(
            ronda = ronda,
            necesarios = necesarios,
            opciones = opciones,
            subidos = subidos
        )
    }

    fun tocar(objeto: ObjetoBus): ResultadoBus {
        val actual = _estado.value
        if (objeto !in actual.necesarios || objeto.nombre in actual.subidos) {
            return ResultadoBus(acierto = false, autobusCompleto = false)
        }
        val nuevosSubidos = actual.subidos + objeto.nombre
        _estado.value = crearEstado(actual.ronda, nuevosSubidos)
        savedState["subidos_bus"] = ArrayList(nuevosSubidos)
        return ResultadoBus(
            acierto = true,
            autobusCompleto = nuevosSubidos.size == actual.necesarios.size
        )
    }

    fun siguienteAutobus() {
        val siguiente = _estado.value.ronda + 1
        _estado.value = crearEstado(siguiente, emptySet())
        savedState["ronda_bus"] = siguiente
        savedState["subidos_bus"] = arrayListOf<String>()
    }
}

enum class TipoSombra { ANIMAL, PLANTA, OBJETO }

data class ElementoSombra(
    val nombre: String,
    @DrawableRes val icono: Int,
    val tipo: TipoSombra
)

class SombrasViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    val elementos = listOf(
        ElementoSombra("Perro", R.drawable.ic_perro, TipoSombra.ANIMAL),
        ElementoSombra("Gato", R.drawable.ic_gato, TipoSombra.ANIMAL),
        ElementoSombra("Pájaro", R.drawable.ic_pajaro, TipoSombra.ANIMAL),
        ElementoSombra("Pez", R.drawable.ic_pez, TipoSombra.ANIMAL),
        ElementoSombra("Flor", R.drawable.ic_flor, TipoSombra.PLANTA),
        ElementoSombra("Manzana", R.drawable.ic_manzana, TipoSombra.OBJETO),
        ElementoSombra("Pelota", R.drawable.ic_pelota, TipoSombra.OBJETO),
        ElementoSombra("Zapato", R.drawable.ic_zapato, TipoSombra.OBJETO),
        ElementoSombra("Plátano", R.drawable.ic_platano, TipoSombra.OBJETO),
        ElementoSombra("Osito", R.drawable.ic_osito, TipoSombra.OBJETO)
    )

    private val _indice = MutableStateFlow(
        savedState["sombra"] ?: Random.nextInt(elementos.size)
    )
    val indice: StateFlow<Int> = _indice

    fun opciones(): List<ElementoSombra> {
        val correcto = elementos[_indice.value]
        return (elementos.filterNot { it == correcto }.shuffled().take(2) + correcto).shuffled()
    }

    fun comprobar(elemento: ElementoSombra): Boolean {
        val acierto = elemento == elementos[_indice.value]
        if (acierto) {
            var siguiente: Int
            do siguiente = Random.nextInt(elementos.size) while (siguiente == _indice.value)
            _indice.value = siguiente
            savedState["sombra"] = siguiente
        }
        return acierto
    }
}

data class ColorAprendizaje(val nombre: String, val color: Color)

class ColoresViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    val colores = listOf(
        ColorAprendizaje("rojo", Color(0xFFE95D68)),
        ColorAprendizaje("azul", Color(0xFF55A9E8)),
        ColorAprendizaje("amarillo", Color(0xFFFFD45F)),
        ColorAprendizaje("verde", Color(0xFF6FCC9B)),
        ColorAprendizaje("rosa", Color(0xFFF39ABC)),
        ColorAprendizaje("naranja", Color(0xFFF5A15D)),
        ColorAprendizaje("morado", Color(0xFF9B7ED9))
    )

    private val _indice = MutableStateFlow(
        savedState["color"] ?: Random.nextInt(colores.size)
    )
    val indice: StateFlow<Int> = _indice

    fun opciones(): List<ColorAprendizaje> {
        val correcto = colores[_indice.value]
        return (colores.filterNot { it == correcto }.shuffled().take(2) + correcto).shuffled()
    }

    fun comprobar(color: ColorAprendizaje): Boolean {
        val acierto = color == colores[_indice.value]
        if (acierto) {
            var siguiente: Int
            do siguiente = Random.nextInt(colores.size) while (siguiente == _indice.value)
            _indice.value = siguiente
            savedState["color"] = siguiente
        }
        return acierto
    }
}

/** Animal con su onomatopeya, compartido por Sonidos de Animales y Quién dice esto. */
data class AnimalSonoro(val nombre: String, @DrawableRes val icono: Int, val sonido: String)

private val animalesSonoros = listOf(
    AnimalSonoro("Perro", R.drawable.ic_perro, "Guau guau"),
    AnimalSonoro("Gato", R.drawable.ic_gato, "Miau"),
    AnimalSonoro("Pájaro", R.drawable.ic_pajaro, "Pío pío"),
    AnimalSonoro("Pez", R.drawable.ic_pez, "Blup blup")
)

class SonidosAnimalesViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    val animales = animalesSonoros
    private val _indice = MutableStateFlow(savedState["indice_sonido_animal"] ?: Random.nextInt(animales.size))
    val indice: StateFlow<Int> = _indice

    /** Cada toque es un acierto: solo cambia de animal y confirma el premio. */
    fun tocar(): Boolean {
        var siguiente: Int
        do siguiente = Random.nextInt(animales.size) while (siguiente == _indice.value)
        _indice.value = siguiente
        savedState["indice_sonido_animal"] = siguiente
        return true
    }
}

class QuienDiceEstoViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    private val animales = animalesSonoros
    private val _indice = MutableStateFlow(savedState["quien_dice"] ?: Random.nextInt(animales.size))
    val indice: StateFlow<Int> = _indice
    val objetivo: AnimalSonoro get() = animales[_indice.value]

    fun opciones(): List<AnimalSonoro> =
        (animales.filterNot { it == objetivo }.shuffled().take(2) + objetivo).shuffled()

    fun comprobar(animal: AnimalSonoro): Boolean {
        val acierto = animal == objetivo
        if (acierto) {
            var siguiente: Int
            do siguiente = Random.nextInt(animales.size) while (siguiente == _indice.value)
            _indice.value = siguiente
            savedState["quien_dice"] = siguiente
        }
        return acierto
    }
}

data class Burbuja(
    val id: Int,
    val x: Float,
    val y: Float,
    val color: Color,
    val radio: Float
)

class BurbujasViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    private val coloresBurbuja = listOf(
        Color(0xFF6FCC9B), Color(0xFF55A9E8), Color(0xFFF39ABC),
        Color(0xFFFFD45F), Color(0xFF9B7ED9), Color(0xFFF5A15D)
    )
    private var siguienteId = savedState["burbuja_id"] ?: 0
    private var reventadas = savedState["burbujas_reventadas"] ?: 0
    val totalReventadas: Int get() = reventadas

    /** Reintenta unas pocas veces para que las burbujas no queden pegadas entre sí. */
    private fun nuevaBurbuja(existentes: List<Burbuja>): Burbuja {
        var candidata: Burbuja
        var intentos = 0
        do {
            candidata = Burbuja(
                id = siguienteId,
                x = Random.nextFloat() * .74f + .06f,
                y = Random.nextFloat() * .58f + .06f,
                color = coloresBurbuja.random(),
                radio = listOf(.11f, .14f, .17f).random()
            )
            intentos++
        } while (
            intentos < 12 &&
            existentes.any { kotlin.math.hypot(it.x - candidata.x, it.y - candidata.y) < .24f }
        )
        siguienteId++
        savedState["burbuja_id"] = siguienteId
        return candidata
    }

    private val _burbujas = MutableStateFlow<List<Burbuja>>(
        mutableListOf<Burbuja>().apply { repeat(5) { add(nuevaBurbuja(this)) } }
    )
    val burbujas: StateFlow<List<Burbuja>> = _burbujas

    /** Revienta la burbuja tocada; devuelve true si con esta se completa un premio. */
    fun reventar(id: Int): Boolean {
        val actuales = _burbujas.value
        if (actuales.none { it.id == id }) return false
        val restantes = actuales.filterNot { it.id == id }
        _burbujas.value = restantes + nuevaBurbuja(restantes)
        reventadas++
        savedState["burbujas_reventadas"] = reventadas
        return reventadas % 5 == 0
    }
}

data class ObjetoTamano(val nombre: String, @DrawableRes val icono: Int)

class GrandePequenoViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    private val objetos = listOf(
        ObjetoTamano("Pelota", R.drawable.ic_pelota),
        ObjetoTamano("Manzana", R.drawable.ic_manzana),
        ObjetoTamano("Osito", R.drawable.ic_osito),
        ObjetoTamano("Zapato", R.drawable.ic_zapato),
        ObjetoTamano("Flor", R.drawable.ic_flor),
        ObjetoTamano("Estrella", R.drawable.ic_estrella)
    )
    private val _indice = MutableStateFlow(savedState["gp_objeto"] ?: Random.nextInt(objetos.size))
    val indice: StateFlow<Int> = _indice
    val objeto: ObjetoTamano get() = objetos[_indice.value]

    private val _pideGrande = MutableStateFlow(savedState["gp_pide_grande"] ?: Random.nextBoolean())
    val pideGrande: StateFlow<Boolean> = _pideGrande

    private val _grandeAIzquierda = MutableStateFlow(savedState["gp_izquierda"] ?: Random.nextBoolean())
    val grandeAIzquierda: StateFlow<Boolean> = _grandeAIzquierda

    fun comprobar(tocoGrande: Boolean): Boolean {
        val acierto = tocoGrande == _pideGrande.value
        if (acierto) {
            var siguiente: Int
            do siguiente = Random.nextInt(objetos.size) while (siguiente == _indice.value)
            _indice.value = siguiente
            _pideGrande.value = Random.nextBoolean()
            _grandeAIzquierda.value = Random.nextBoolean()
            savedState["gp_objeto"] = siguiente
            savedState["gp_pide_grande"] = _pideGrande.value
            savedState["gp_izquierda"] = _grandeAIzquierda.value
        }
        return acierto
    }
}

class ClasificarColorViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    private val colores = listOf(
        ColorAprendizaje("rojo", Color(0xFFE95D68)),
        ColorAprendizaje("azul", Color(0xFF55A9E8)),
        ColorAprendizaje("amarillo", Color(0xFFFFD45F)),
        ColorAprendizaje("verde", Color(0xFF6FCC9B))
    )
    private val _indice = MutableStateFlow(savedState["clas_color"] ?: Random.nextInt(colores.size))
    val indice: StateFlow<Int> = _indice
    val objetivo: ColorAprendizaje get() = colores[_indice.value]

    fun cestas(): List<ColorAprendizaje> =
        (colores.filterNot { it == objetivo }.shuffled().take(2) + objetivo).shuffled()

    fun comprobar(cesta: ColorAprendizaje): Boolean {
        val acierto = cesta == objetivo
        if (acierto) {
            var siguiente: Int
            do siguiente = Random.nextInt(colores.size) while (siguiente == _indice.value)
            _indice.value = siguiente
            savedState["clas_color"] = siguiente
        }
        return acierto
    }
}

data class PiezaPuzzle(val nombre: String, @DrawableRes val icono: Int)

class PuzzleViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    private val piezas = listOf(
        PiezaPuzzle("Manzana", R.drawable.ic_manzana),
        PiezaPuzzle("Plátano", R.drawable.ic_platano),
        PiezaPuzzle("Pelota", R.drawable.ic_pelota),
        PiezaPuzzle("Osito", R.drawable.ic_osito),
        PiezaPuzzle("Flor", R.drawable.ic_flor)
    )
    private val _indice = MutableStateFlow(savedState["puzzle_pieza"] ?: Random.nextInt(piezas.size))
    val indice: StateFlow<Int> = _indice
    val objetivo: PiezaPuzzle get() = piezas[_indice.value]

    /** Solo dos opciones (la correcta y una distractora): más fácil para 2-3 años. */
    fun opciones(): List<PiezaPuzzle> =
        (piezas.filterNot { it == objetivo }.shuffled().take(1) + objetivo).shuffled()

    fun comprobar(pieza: PiezaPuzzle): Boolean {
        val acierto = pieza == objetivo
        if (acierto) {
            var siguiente: Int
            do siguiente = Random.nextInt(piezas.size) while (siguiente == _indice.value)
            _indice.value = siguiente
            savedState["puzzle_pieza"] = siguiente
        }
        return acierto
    }
}

enum class TipoPrenda { GORRO, BUFANDA, BOTAS }
data class Prenda(val tipo: TipoPrenda, val nombre: String, val color: Color)

class VestirReiViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    private val coloresPrenda = listOf(
        Color(0xFFE95D68), Color(0xFF55A9E8), Color(0xFFFFD45F),
        Color(0xFF6FCC9B), Color(0xFF9B7ED9)
    )

    private fun prendasAleatorias() = listOf(
        Prenda(TipoPrenda.GORRO, "gorro", coloresPrenda.random()),
        Prenda(TipoPrenda.BUFANDA, "bufanda", coloresPrenda.random()),
        Prenda(TipoPrenda.BOTAS, "botas", coloresPrenda.random())
    )

    private val _prendas = MutableStateFlow(prendasAleatorias())
    val prendas: StateFlow<List<Prenda>> = _prendas

    private val _puestas = MutableStateFlow(
        (savedState.get<ArrayList<String>>("vestir_puestas") ?: arrayListOf())
            .map { TipoPrenda.valueOf(it) }.toSet()
    )
    val puestas: StateFlow<Set<TipoPrenda>> = _puestas

    /** Devuelve true si la prenda tocada todavía no estaba puesta. */
    fun tocar(prenda: Prenda): Boolean {
        if (prenda.tipo in _puestas.value) return false
        _puestas.value = _puestas.value + prenda.tipo
        savedState["vestir_puestas"] = ArrayList(_puestas.value.map { it.name })
        return true
    }

    val completo: Boolean get() = _puestas.value.size == _prendas.value.size

    fun siguienteConjunto() {
        _prendas.value = prendasAleatorias()
        _puestas.value = emptySet()
        savedState["vestir_puestas"] = arrayListOf<String>()
    }
}

enum class Emocion(val nombre: String) { FELIZ("feliz"), TRISTE("triste"), SORPRENDIDO("sorprendido") }

class EmocionesViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    private val _emocion = MutableStateFlow(
        Emocion.valueOf(savedState["emocion"] ?: Emocion.entries.random().name)
    )
    val emocion: StateFlow<Emocion> = _emocion

    fun comprobar(elegida: Emocion): Boolean {
        val acierto = elegida == _emocion.value
        if (acierto) {
            val opciones = Emocion.entries.filterNot { it == _emocion.value }
            _emocion.value = opciones.random()
            savedState["emocion"] = _emocion.value.name
        }
        return acierto
    }
}

class ContarHasta3ViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    private val _total = MutableStateFlow(savedState["contar_total"] ?: Random.nextInt(1, 4))
    val total: StateFlow<Int> = _total
    private val _tocados = MutableStateFlow(savedState["contar_tocados"] ?: 0)
    val tocados: StateFlow<Int> = _tocados

    /** Devuelve true si con este toque se completó la cuenta 1..total en orden. */
    fun tocarSiguiente(): Boolean {
        val nuevo = _tocados.value + 1
        _tocados.value = nuevo
        savedState["contar_tocados"] = nuevo
        return nuevo >= _total.value
    }

    /** Prepara otra cuenta. Se llama después de que termine la felicitación hablada. */
    fun siguienteRonda() {
        var siguienteTotal: Int
        do siguienteTotal = Random.nextInt(1, 4) while (siguienteTotal == _total.value)
        _total.value = siguienteTotal
        _tocados.value = 0
        savedState["contar_total"] = siguienteTotal
        savedState["contar_tocados"] = 0
    }
}

enum class PasoRutina(val texto: String) {
    DESAYUNAR("Desayunar"),
    DIENTES("Cepillarse los dientes"),
    PIJAMA("Ponerse el pijama"),
    DORMIR("Ir a dormir")
}

class RutinaDiariaViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    val pasos = PasoRutina.entries
    private val _indice = MutableStateFlow(savedState["rutina_paso"] ?: 0)
    val indice: StateFlow<Int> = _indice

    /** Devuelve true si con este paso se completó toda la rutina (y reinicia). */
    fun avanzar(): Boolean {
        val siguiente = _indice.value + 1
        val completo = siguiente >= pasos.size
        _indice.value = if (completo) 0 else siguiente
        savedState["rutina_paso"] = _indice.value
        return completo
    }
}
