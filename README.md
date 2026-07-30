# El Bosque de Rei

Aplicación educativa infantil, totalmente offline, escrita en Kotlin y Jetpack
Compose. Incluye cuatro mini-juegos, narración TTS en español, estrellas
persistentes, álbum de stickers, confeti y sonidos generados en el dispositivo.

## Abrir y ejecutar en Android Studio

1. Instala una versión reciente de Android Studio con JDK 17.
2. Abre esta carpeta con **File > Open**.
3. En **Tools > SDK Manager**, instala **Android SDK Platform 35** y las
   herramientas de compilación correspondientes.
4. Espera a que termine **Gradle Sync**. Android Studio descargará las
   dependencias de desarrollo; la app instalada no necesita Internet.
5. Crea un emulador con Android 8.0 (API 26) o superior, o conecta un teléfono
   con depuración USB.
6. Comprueba en los ajustes del dispositivo que haya un motor de
   texto-a-voz con datos de voz en español descargados. Google Speech Services
   o el motor TTS del fabricante funcionan sin conexión una vez instalada la voz.
7. Selecciona el dispositivo y pulsa **Run ▶**.

La pantalla se fuerza a retrato. Las barras del sistema quedan ocultas y se
pueden revelar temporalmente deslizando desde un borde.

## Generar un APK

### APK de depuración

En Android Studio selecciona:

**Build > Build Bundle(s) / APK(s) > Build APK(s)**

El resultado queda en:

`app/build/outputs/apk/debug/app-debug.apk`

### APK firmado para instalar o distribuir

1. Selecciona **Build > Generate Signed Bundle / APK**.
2. Elige **APK**.
3. Crea o selecciona un almacén de claves (`.jks`) y guarda su contraseña de
   forma segura.
4. Selecciona la variante **release**, activa las firmas V1 y V2 y termina el
   asistente.
5. El APK se genera en `app/build/outputs/apk/release/`.

## Privacidad y persistencia

El manifiesto no declara `android.permission.INTERNET`. El progreso se guarda
localmente con Preferences DataStore bajo el nombre `progreso_rei`. Cada
acierto concede una estrella; a las 5, 10, 15, 20 y 25 estrellas se desbloquean
el sol, la nube, la flor, el arcoíris y el oso, respectivamente.

## Estructura principal

- `MainActivity.kt`: modo inmersivo y NavHost.
- `Screens.kt`: inicio, álbum, cuatro juegos, animaciones y confeti.
- `GameViewModels.kt`: los cuatro ViewModels con `SavedStateHandle`.
- `RecompensasViewModel.kt`: estado global de estrellas y desbloqueos.
- `RecompensasRepository.kt`: almacenamiento DataStore.
- `AudioAyudante.kt`: TextToSpeech, campanita y melodía de celebración.
