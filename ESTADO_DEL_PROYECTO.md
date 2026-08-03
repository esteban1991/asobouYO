# Estado del proyecto — El Bosque de Rei

_Notas de contexto para retomar el trabajo en una próxima sesión. Última actualización: 2026-08-03._

## Qué es esto

App educativa infantil offline (Kotlin + Jetpack Compose) para niños de 2-3 años, con
narración por TTS en español en cada toque. Repo remoto:
`https://github.com/esteban1991/asobouYO.git`, rama `main`.

**Importante:** este proyecto se está editando en paralelo por Android Studio (el usuario
o su propio flujo de trabajo con otra IA) mientras Claude Code también edita. Es normal
encontrar archivos cambiados "por otra sesión" al abrir — no revertir esos cambios sin
preguntar, se han estado integrando en vez de pisar.

## Catálogo actual de juegos (27 + álbum)

Animales, Colores (30 colores, 12 con mascota ilustrada + florcita para el resto), Formas
(9 figuras), Números, Autobús, Busca y encuentra, Alimenta al animal, Trazo, Sombras,
Sonidos de animales, Burbujas, Grande y Pequeño, Cestas (aro de básquet), Mi planta,
Rompecabezas, ¿Quién dice esto?, Vestir a Rei (30 estilos de ropa por familias visuales),
Emociones, Contar hasta 3, Mi rutina, ¿Dónde vive?, El Camino (laberinto), Imita el Ritmo
(4 instrumentos), ¿Qué Falta?, Ordena Tamaños, Tobogán, Carrusel, Tren. "Respira con Rei"
existe en el código pero está oculto del menú (a pedido del usuario).

## Cómo compilar sin Android Studio

No hay `gradlew` en el repo. Se usa el Gradle 8.10.2 cacheado en:
`C:\Users\Teban\.gradle\wrapper\dists\gradle-8.10.2-bin\a04bxjujx95o3nb99gddekhwo\gradle-8.10.2\bin\gradle.bat`
con `-p <ruta del proyecto> :app:assembleDebug`. `local.properties` con `sdk.dir` ya está
creado (gitignored). El dispositivo de prueba es una tablet Android para niños conectada
por `adb` (paquete `com.rei.elbosque`); a veces está bloqueada/dormida porque el usuario
la tiene en la mano — no forzar despertarla si ya está en uso.

## Decisiones importantes (y el porqué)

- **Paleta compartida**: `ReiColores.kt` — antes cada archivo repetía los mismos hex.
- **`Sonidos` en `AudioAyudante.kt`**: todo el audio es sintetizado en el dispositivo (sin
  archivos), incluida música. `Narrador.felicitar()` protege la cola de TTS ~3.2s para que
  la consigna de la ronda siguiente no corte la felicitación.
- **Vestir a Rei**: se intentó una vez replicar exactamente las 30 formas de la bandeja
  sobre el cuerpo de Rei con coordenadas inventadas sin poder verlas — quedó horrible y se
  revirtió a familias de estilo simples (corona/tiara-lazo-orejitas/sombrero/genérico, etc.)
  bien ajustadas al cuerpo. Lección: **no inventar posiciones sobre una imagen sin verla**;
  para "Alimenta al animal" en cambio se leyeron las 3 fotos reales (gato/oso/pájaro) con
  la herramienta de lectura de imágenes antes de ubicar la boca/ojos, y funcionó bien.
- **Ícono de la app**: el usuario pidió algo "parecido a Pocoyo o el pato de Pocoyo"; se
  hizo un patito **original** (vector XML dibujado a mano, `ic_launcher_pato.xml`) para no
  reproducir un personaje con derechos de otra productora.
- **Assets con fondo verde/magenta sin procesar**: varias imágenes nuevas (tren, animales
  del parque, colores) llegan con fondo de croma sin quitar. Hay scripts en `tools/`
  (`remove_green_screen_tren.py`, `slice_park_animals.py`, `slice_color_atlas.py`) que usan
  Python + Pillow para quitarlo — si aparece un ícono con fondo verde/magenta visible,
  revisar si necesita el mismo tratamiento antes de usarlo.

## Verificación

Antes de dar algo por terminado: `assembleDebug` con Gradle (no alcanza con
`compileDebugKotlin`, ya hubo builds "exitosos" en Kotlin que fallaban después). Cuando fue
posible, se instaló en la tablet real y se sacó captura por `adb` para confirmar visualmente
— sobre todo para íconos y animaciones nuevas, ya que adivinar posiciones a ciegas ya causó
un problema real (ver "Vestir a Rei" arriba).

## Pendiente / sin verificar al momento de este guardado

- Hay cambios sin commitear en el working tree (de la otra sesión/editor en paralelo):
  `FondosJuego.kt` (nuevo, agrega `Modifier.fondoBosque()` para poner el fondo ilustrado
  del bosque como imagen en vez de gradiente) y su uso en `ColorsScreen.kt`, además de
  imágenes de fondo para Carrusel/Tobogán. No se tocaron ni commitearon: parecía trabajo en
  progreso de la otra sesión, no terminado de integrar en todas las pantallas.
