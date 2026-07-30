package com.rei.elbosque.ui

import androidx.annotation.DrawableRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.compose.ui.graphics.Color
import com.rei.elbosque.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random

enum class Forma(val titulo: String, val articulo: String = "el") {
    CIRCULO("Círculo"),
    CUADRADO("Cuadrado"),
    TRIANGULO("Triángulo"),
    ESTRELLA("Estrella", "la"),
    CORAZON("Corazón"),
    OVALO("Óvalo"),
    RECTANGULO("Rectángulo"),
    ROMBO("Rombo"),
    HEXAGONO("Hexágono"),
    LUNA("Luna", "la")
}

class FormasViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    private val _forma = MutableStateFlow(
        Forma.valueOf(savedState["forma"] ?: Forma.entries.random().name)
    )
    val forma: StateFlow<Forma> = _forma

    /** Tres opciones: la correcta y dos distractoras, para que quepan cómodas sin importar cuántas formas haya. */
    fun opciones(): List<Forma> =
        (Forma.entries.filterNot { it == _forma.value }.shuffled().take(2) + _forma.value).shuffled()

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

data class ColorAprendizaje(
    val nombre: String,
    val color: Color,
    val femenino: String = nombre,
    @DrawableRes val icono: Int? = null
)

class ColoresViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    val colores = listOf(
        ColorAprendizaje("rojo", Color(0xFFE95D68), icono = R.drawable.color_rojo),
        ColorAprendizaje("azul", Color(0xFF55A9E8), icono = R.drawable.color_azul),
        ColorAprendizaje("amarillo", Color(0xFFFFD45F), icono = R.drawable.color_amarillo),
        ColorAprendizaje("verde", Color(0xFF6FCC78), icono = R.drawable.color_verde),
        ColorAprendizaje("rosa", Color(0xFFF39ABC), icono = R.drawable.color_rosa),
        ColorAprendizaje("naranja", Color(0xFFF5A15D), icono = R.drawable.color_naranja),
        ColorAprendizaje("morado", Color(0xFF9B7ED9), icono = R.drawable.color_morado),
        ColorAprendizaje("turquesa", Color(0xFF42C7C7), icono = R.drawable.color_turquesa),
        ColorAprendizaje("café", Color(0xFFA87343), icono = R.drawable.color_cafe),
        ColorAprendizaje("negro", Color(0xFF34383E), icono = R.drawable.color_negro),
        ColorAprendizaje("blanco", Color(0xFFF8F8F2), icono = R.drawable.color_blanco),
        ColorAprendizaje("gris", Color(0xFF9DA7AE), icono = R.drawable.color_gris),
        // Sin mascota ilustrada todavía: usan la carita dibujada como respaldo.
        ColorAprendizaje("celeste", Color(0xFF8ED8F8)),
        ColorAprendizaje("violeta", Color(0xFFB37FEA)),
        ColorAprendizaje("fucsia", Color(0xFFEF5DA8)),
        ColorAprendizaje("lima", Color(0xFFC6E86B)),
        ColorAprendizaje("dorado", Color(0xFFF0C94D)),
        ColorAprendizaje("beige", Color(0xFFE8D3B0)),
        ColorAprendizaje("índigo", Color(0xFF6A6FD8)),
        ColorAprendizaje("coral", Color(0xFFFF8C74)),
        ColorAprendizaje("salmón", Color(0xFFFFA791)),
        ColorAprendizaje("menta", Color(0xFFA8ECD1)),
        ColorAprendizaje("lavanda", Color(0xFFD8C6F5)),
        ColorAprendizaje("mostaza", Color(0xFFE3B23C)),
        ColorAprendizaje("vino", Color(0xFFA14B5C)),
        ColorAprendizaje("oliva", Color(0xFFA8AE5A)),
        ColorAprendizaje("cian", Color(0xFF63D9E0)),
        ColorAprendizaje("magenta", Color(0xFFE455C4)),
        ColorAprendizaje("plateado", Color(0xFFC8CDD2)),
        ColorAprendizaje("esmeralda", Color(0xFF2FBF8F))
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
    // Un "mazo" que reparte los 6 colores sin repetir hasta agotarlos: con solo
    // .random() independiente, 5 burbujas de 6 colores posibles casi siempre
    // repetían color por puro azar (problema del cumpleaños).
    private var bolsaColores = coloresBurbuja.shuffled().toMutableList()

    private fun siguienteColor(): Color {
        if (bolsaColores.isEmpty()) bolsaColores = coloresBurbuja.shuffled().toMutableList()
        return bolsaColores.removeAt(0)
    }

    private var siguienteId = savedState["burbuja_id"] ?: 0
    private var reventadas = savedState["burbujas_reventadas"] ?: 0
    val totalReventadas: Int get() = reventadas

    /** Reintenta unas pocas veces para que las burbujas no queden pegadas entre sí. */
    private fun nuevaBurbuja(existentes: List<Burbuja>): Burbuja {
        val color = siguienteColor()
        val radio = listOf(.11f, .14f, .17f).random()
        var candidata: Burbuja
        var intentos = 0
        do {
            candidata = Burbuja(
                id = siguienteId,
                x = Random.nextFloat() * .74f + .06f,
                y = Random.nextFloat() * .58f + .06f,
                color = color,
                radio = radio
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

data class ObjetoTamano(val nombre: String, @DrawableRes val icono: Int, val femenino: Boolean = false)

class GrandePequenoViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    private val objetos = listOf(
        ObjetoTamano("Pelota", R.drawable.ic_pelota, femenino = true),
        ObjetoTamano("Manzana", R.drawable.ic_manzana, femenino = true),
        ObjetoTamano("Osito", R.drawable.ic_osito),
        ObjetoTamano("Zapato", R.drawable.ic_zapato),
        ObjetoTamano("Flor", R.drawable.ic_flor, femenino = true),
        ObjetoTamano("Estrella", R.drawable.ic_estrella, femenino = true)
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
        ColorAprendizaje("rojo", Color(0xFFE95D68), femenino = "roja"),
        ColorAprendizaje("azul", Color(0xFF55A9E8), femenino = "azul"),
        ColorAprendizaje("amarillo", Color(0xFFFFD45F), femenino = "amarilla"),
        ColorAprendizaje("verde", Color(0xFF6FCC9B), femenino = "verde")
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
enum class EstiloPrenda(
    val tipo: TipoPrenda,
    val nombre: String,
    val articulo: String
) {
    GORRO_LANA(TipoPrenda.GORRO, "gorro de lana", "Un"),
    SOMBRERO(TipoPrenda.GORRO, "sombrero", "Un"),
    CORONA(TipoPrenda.GORRO, "corona", "Una"),
    LAZO(TipoPrenda.GORRO, "lazo", "Un"),
    BOINA(TipoPrenda.GORRO, "boina", "Una"),
    GORRA(TipoPrenda.GORRO, "gorra", "Una"),
    TIARA(TipoPrenda.GORRO, "tiara", "Una"),
    CAPUCHA(TipoPrenda.GORRO, "capucha", "Una"),
    SOMBRERO_SOL(TipoPrenda.GORRO, "sombrero de sol", "Un"),
    OREJITAS(TipoPrenda.GORRO, "orejitas", "Unas"),

    BUFANDA(TipoPrenda.BUFANDA, "bufanda", "Una"),
    COLLAR(TipoPrenda.BUFANDA, "collar", "Un"),
    CAPA(TipoPrenda.BUFANDA, "capa", "Una"),
    CHALECO(TipoPrenda.BUFANDA, "chaleco", "Un"),
    SUETER(TipoPrenda.BUFANDA, "suéter", "Un"),
    CHAQUETA(TipoPrenda.BUFANDA, "chaqueta", "Una"),
    VESTIDO(TipoPrenda.BUFANDA, "vestido", "Un"),
    CAMISETA(TipoPrenda.BUFANDA, "camiseta", "Una"),
    IMPERMEABLE(TipoPrenda.BUFANDA, "impermeable", "Un"),
    MOCHILA(TipoPrenda.BUFANDA, "mochila", "Una"),

    BOTAS(TipoPrenda.BOTAS, "botas", "Unas"),
    ZAPATOS(TipoPrenda.BOTAS, "zapatos", "Unos"),
    ZAPATILLAS(TipoPrenda.BOTAS, "zapatillas", "Unas"),
    SANDALIAS(TipoPrenda.BOTAS, "sandalias", "Unas"),
    PANTUFLAS(TipoPrenda.BOTAS, "pantuflas", "Unas"),
    BOTAS_LLUVIA(TipoPrenda.BOTAS, "botas de lluvia", "Unas"),
    ZAPATOS_FIESTA(TipoPrenda.BOTAS, "zapatos de fiesta", "Unos"),
    TENIS(TipoPrenda.BOTAS, "tenis", "Unos"),
    CALCETINES(TipoPrenda.BOTAS, "calcetines", "Unos"),
    BOTINES(TipoPrenda.BOTAS, "botines", "Unos")
}

data class Prenda(val estilo: EstiloPrenda, val color: Color) {
    val tipo get() = estilo.tipo
    val nombre get() = estilo.nombre
    val articulo get() = estilo.articulo
}

class VestirReiViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    private val coloresPrenda = listOf(
        Color(0xFFE95D68), Color(0xFF55A9E8), Color(0xFFFFD45F),
        Color(0xFF6FCC9B), Color(0xFF9B7ED9)
    )

    private fun prendasAleatorias(anteriores: List<Prenda> = emptyList()) =
        TipoPrenda.entries.map { tipo ->
            val anterior = anteriores.firstOrNull { it.tipo == tipo }?.estilo
            val estilo = EstiloPrenda.entries.filter { it.tipo == tipo && it != anterior }.random()
            Prenda(estilo, coloresPrenda.random())
        }

    private val estilosGuardados = savedState.get<ArrayList<String>>("vestir_estilos")
    private val _prendas = MutableStateFlow(
        estilosGuardados?.mapNotNull { nombre ->
            runCatching { EstiloPrenda.valueOf(nombre) }.getOrNull()
        }?.takeIf { it.size == 3 }?.map { Prenda(it, coloresPrenda.random()) }
            ?: prendasAleatorias()
    )
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
        _prendas.value = prendasAleatorias(_prendas.value)
        _puestas.value = emptySet()
        savedState["vestir_estilos"] = ArrayList(_prendas.value.map { it.estilo.name })
        savedState["vestir_puestas"] = arrayListOf<String>()
    }
}

enum class Emocion(val nombre: String) { FELIZ("feliz"), TRISTE("triste"), SORPRENDIDO("sorprendida") }

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

enum class Habitat(val etiqueta: String) {
    GRANJA("la granja"),
    MAR("el mar"),
    SELVA("la selva")
}

data class AnimalHabitat(val nombre: String, @DrawableRes val icono: Int, val habitat: Habitat)

private val animalesHabitat = listOf(
    AnimalHabitat("Perro", R.drawable.ic_perro, Habitat.GRANJA),
    AnimalHabitat("Gato", R.drawable.ic_gato, Habitat.GRANJA),
    AnimalHabitat("Pez", R.drawable.ic_pez, Habitat.MAR),
    AnimalHabitat("Pájaro", R.drawable.ic_pajaro, Habitat.SELVA),
    AnimalHabitat("Oso", R.drawable.ic_oso, Habitat.SELVA)
)

class DondeViveViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    private val animales = animalesHabitat
    private val _indice = MutableStateFlow(savedState["donde_vive"] ?: Random.nextInt(animales.size))
    val indice: StateFlow<Int> = _indice
    val objetivo: AnimalHabitat get() = animales[_indice.value]

    /** Dos opciones: el hábitat correcto y uno distinto al azar. */
    fun opciones(): List<Habitat> {
        val distractor = Habitat.entries.filterNot { it == objetivo.habitat }.random()
        return listOf(objetivo.habitat, distractor).shuffled()
    }

    fun comprobar(habitat: Habitat): Boolean {
        val acierto = habitat == objetivo.habitat
        if (acierto) {
            var siguiente: Int
            do siguiente = Random.nextInt(animales.size) while (siguiente == _indice.value)
            _indice.value = siguiente
            savedState["donde_vive"] = siguiente
        }
        return acierto
    }
}

/** Trayecto simple de A (arriba-izquierda) a B (abajo-derecha), en fracciones 0f..1f del área de juego. */
data class TrayectoCamino(val ax: Float, val ay: Float, val bx: Float, val by: Float)

class LaberintoViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    private fun trayectoAleatorio() = TrayectoCamino(
        ax = .12f,
        ay = Random.nextFloat() * .28f + .10f,
        bx = .82f,
        by = Random.nextFloat() * .28f + .54f
    )

    private val _trayecto = MutableStateFlow(
        TrayectoCamino(
            ax = savedState["lab_ax"] ?: .12f,
            ay = savedState["lab_ay"] ?: .16f,
            bx = savedState["lab_bx"] ?: .82f,
            by = savedState["lab_by"] ?: .62f
        )
    )
    val trayecto: StateFlow<TrayectoCamino> = _trayecto

    /** Se llama al soltar a Rei sobre la meta: siempre premia y prepara un camino nuevo. */
    fun llegar(): Boolean {
        val nuevo = trayectoAleatorio()
        _trayecto.value = nuevo
        savedState["lab_ax"] = nuevo.ax
        savedState["lab_ay"] = nuevo.ay
        savedState["lab_bx"] = nuevo.bx
        savedState["lab_by"] = nuevo.by
        return true
    }
}

enum class Instrumento(val nombre: String) {
    TAMBOR("tambor"),
    CAMPANA("campana"),
    XILOFONO("xilófono"),
    MARACAS("maracas")
}

data class ResultadoRitmo(val acierto: Boolean, val rondaCompleta: Boolean)

class RitmoViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    private val instrumentos = Instrumento.entries

    private fun nuevaSecuencia(longitud: Int): List<Instrumento> = List(longitud) { instrumentos.random() }

    private fun guardarSecuencia(secuencia: List<Instrumento>) {
        savedState["ritmo_secuencia"] = ArrayList(secuencia.map { it.name })
    }

    // Empieza con dos sonidos y sube a tres; para 2-3 años no conviene ir más allá.
    private val _longitud = MutableStateFlow(savedState["ritmo_longitud"] ?: 2)
    val longitud: StateFlow<Int> = _longitud

    private val _secuencia = MutableStateFlow(
        (savedState.get<ArrayList<String>>("ritmo_secuencia"))
            ?.map { Instrumento.valueOf(it) }
            ?.takeIf { it.isNotEmpty() }
            ?: nuevaSecuencia(_longitud.value).also { guardarSecuencia(it) }
    )
    val secuencia: StateFlow<List<Instrumento>> = _secuencia

    private val _progreso = MutableStateFlow(savedState["ritmo_progreso"] ?: 0)
    val progreso: StateFlow<Int> = _progreso

    /** Devuelve si el toque siguió la secuencia y si con él se completó toda la ronda. */
    fun tocar(instrumento: Instrumento): ResultadoRitmo {
        if (_secuencia.value.getOrNull(_progreso.value) != instrumento) {
            _progreso.value = 0
            savedState["ritmo_progreso"] = 0
            return ResultadoRitmo(acierto = false, rondaCompleta = false)
        }
        val siguientePaso = _progreso.value + 1
        val completa = siguientePaso >= _secuencia.value.size
        if (completa) {
            val siguienteLongitud = (_longitud.value + 1).coerceAtMost(3)
            _longitud.value = siguienteLongitud
            savedState["ritmo_longitud"] = siguienteLongitud
            val nueva = nuevaSecuencia(siguienteLongitud)
            _secuencia.value = nueva
            guardarSecuencia(nueva)
            _progreso.value = 0
            savedState["ritmo_progreso"] = 0
        } else {
            _progreso.value = siguientePaso
            savedState["ritmo_progreso"] = siguientePaso
        }
        return ResultadoRitmo(acierto = true, rondaCompleta = completa)
    }
}

data class ObjetoMemoria(val nombre: String, @DrawableRes val icono: Int)

private val objetosMemoria = listOf(
    ObjetoMemoria("Pelota", R.drawable.ic_pelota),
    ObjetoMemoria("Manzana", R.drawable.ic_manzana),
    ObjetoMemoria("Osito", R.drawable.ic_osito),
    ObjetoMemoria("Flor", R.drawable.ic_flor),
    ObjetoMemoria("Estrella", R.drawable.ic_estrella),
    ObjetoMemoria("Zapato", R.drawable.ic_zapato),
    ObjetoMemoria("Plátano", R.drawable.ic_platano),
    ObjetoMemoria("Sol", R.drawable.ic_sol)
)

class QueFaltaViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    private fun nuevaRonda() = objetosMemoria.shuffled().take(3)

    private fun guardarMostrados(lista: List<ObjetoMemoria>) {
        savedState["quefalta_mostrados"] = ArrayList(lista.map { it.nombre })
    }

    private val _mostrados = MutableStateFlow(
        savedState.get<ArrayList<String>>("quefalta_mostrados")
            ?.mapNotNull { nombre -> objetosMemoria.find { it.nombre == nombre } }
            ?.takeIf { it.size == 3 }
            ?: nuevaRonda().also { guardarMostrados(it) }
    )
    val mostrados: StateFlow<List<ObjetoMemoria>> = _mostrados

    private val _indiceFaltante = MutableStateFlow(savedState["quefalta_indice"] ?: Random.nextInt(3))
    val indiceFaltante: StateFlow<Int> = _indiceFaltante

    val faltante: ObjetoMemoria get() = _mostrados.value[_indiceFaltante.value]

    /** Dos objetos que no estaban en la ronda y el correcto, mezclados. */
    fun opciones(): List<ObjetoMemoria> {
        val distractores = objetosMemoria.filterNot { it in _mostrados.value }.shuffled().take(2)
        return (distractores + faltante).shuffled()
    }

    fun comprobar(objeto: ObjetoMemoria): Boolean {
        val acierto = objeto == faltante
        if (acierto) {
            val nueva = nuevaRonda()
            _mostrados.value = nueva
            guardarMostrados(nueva)
            val nuevoIndice = Random.nextInt(3)
            _indiceFaltante.value = nuevoIndice
            savedState["quefalta_indice"] = nuevoIndice
        }
        return acierto
    }
}

enum class TamanoObjeto(val etiqueta: String, val etiquetaFemenina: String) {
    CHICO("chico", "chica"),
    MEDIANO("mediano", "mediana"),
    GRANDE("grande", "grande")
}

enum class OrdenTamano { CHICO_A_GRANDE, GRANDE_A_CHICO }

data class ObjetoOrden(val nombre: String, @DrawableRes val icono: Int, val femenino: Boolean = false)

data class ResultadoOrden(val acierto: Boolean, val rondaCompleta: Boolean)

private val objetosOrden = listOf(
    ObjetoOrden("Pelota", R.drawable.ic_pelota, femenino = true),
    ObjetoOrden("Manzana", R.drawable.ic_manzana, femenino = true),
    ObjetoOrden("Estrella", R.drawable.ic_estrella, femenino = true),
    ObjetoOrden("Osito", R.drawable.ic_osito),
    ObjetoOrden("Flor", R.drawable.ic_flor, femenino = true)
)

class OrdenaTamanoViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    private val _indiceObjeto = MutableStateFlow(savedState["orden_objeto"] ?: Random.nextInt(objetosOrden.size))
    val objeto: ObjetoOrden get() = objetosOrden[_indiceObjeto.value]

    private val _modo = MutableStateFlow(
        OrdenTamano.valueOf(savedState["orden_modo"] ?: OrdenTamano.entries.random().name)
    )
    val modo: StateFlow<OrdenTamano> = _modo

    // posiciones[casilla] = qué tamaño (0=chico,1=mediano,2=grande) se ve en esa casilla.
    private val _posiciones = MutableStateFlow(
        savedState.get<ArrayList<Int>>("orden_posiciones")?.toList() ?: listOf(0, 1, 2).shuffled()
    )
    val posiciones: StateFlow<List<Int>> = _posiciones

    private val _progreso = MutableStateFlow(savedState["orden_progreso"] ?: 0)
    val progreso: StateFlow<Int> = _progreso

    private val secuenciaEsperada: List<Int>
        get() = if (_modo.value == OrdenTamano.CHICO_A_GRANDE) listOf(0, 1, 2) else listOf(2, 1, 0)

    /** Toca una casilla; si no era la que seguía en el orden, se reinicia la secuencia. */
    fun tocar(casilla: Int): ResultadoOrden {
        val tamanoTocado = _posiciones.value[casilla]
        val tamanoEsperado = secuenciaEsperada[_progreso.value]
        if (tamanoTocado != tamanoEsperado) {
            _progreso.value = 0
            savedState["orden_progreso"] = 0
            return ResultadoOrden(acierto = false, rondaCompleta = false)
        }
        val siguiente = _progreso.value + 1
        val completa = siguiente >= 3
        if (completa) {
            var nuevoIndice: Int
            do nuevoIndice = Random.nextInt(objetosOrden.size) while (
                nuevoIndice == _indiceObjeto.value && objetosOrden.size > 1
            )
            _indiceObjeto.value = nuevoIndice
            _modo.value = OrdenTamano.entries.random()
            _posiciones.value = listOf(0, 1, 2).shuffled()
            _progreso.value = 0
            savedState["orden_objeto"] = nuevoIndice
            savedState["orden_modo"] = _modo.value.name
            savedState["orden_posiciones"] = ArrayList(_posiciones.value)
            savedState["orden_progreso"] = 0
        } else {
            _progreso.value = siguiente
            savedState["orden_progreso"] = siguiente
        }
        return ResultadoOrden(acierto = true, rondaCompleta = completa)
    }
}

class RespiraViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    private var ciclos = savedState["respira_ciclos"] ?: 0

    /** Se llama al completar un ciclo de inspirar y exhalar; premia cada tres ciclos. */
    fun completarCiclo(): Boolean {
        ciclos++
        savedState["respira_ciclos"] = ciclos
        return ciclos % 3 == 0
    }
}
