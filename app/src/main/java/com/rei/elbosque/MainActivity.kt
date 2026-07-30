package com.rei.elbosque

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rei.elbosque.audio.MusicaMenu
import com.rei.elbosque.audio.Narrador
import com.rei.elbosque.audio.Sonidos
import com.rei.elbosque.ui.AlbumScreen
import com.rei.elbosque.ui.AlimentaAnimalScreen
import com.rei.elbosque.ui.AlimentaAnimalViewModel
import com.rei.elbosque.ui.AnimalesScreen
import com.rei.elbosque.ui.AnimalesViewModel
import com.rei.elbosque.ui.BurbujasScreen
import com.rei.elbosque.ui.BurbujasViewModel
import com.rei.elbosque.ui.BusScreen
import com.rei.elbosque.ui.BusViewModel
import com.rei.elbosque.ui.BuscaObjetoScreen
import com.rei.elbosque.ui.BuscaObjetoViewModel
import com.rei.elbosque.ui.ClasificarColorScreen
import com.rei.elbosque.ui.ClasificarColorViewModel
import com.rei.elbosque.ui.ColoresScreen
import com.rei.elbosque.ui.ColoresViewModel
import com.rei.elbosque.ui.Confetti
import com.rei.elbosque.ui.CelebracionAcierto
import com.rei.elbosque.ui.ContarHasta3Screen
import com.rei.elbosque.ui.ContarHasta3ViewModel
import com.rei.elbosque.ui.DondeViveScreen
import com.rei.elbosque.ui.DondeViveViewModel
import com.rei.elbosque.ui.EmocionesScreen
import com.rei.elbosque.ui.EmocionesViewModel
import com.rei.elbosque.ui.FormasScreen
import com.rei.elbosque.ui.FormasViewModel
import com.rei.elbosque.ui.GrandePequenoScreen
import com.rei.elbosque.ui.GrandePequenoViewModel
import com.rei.elbosque.ui.InicioScreen
import com.rei.elbosque.ui.LaberintoScreen
import com.rei.elbosque.ui.LaberintoViewModel
import com.rei.elbosque.ui.NumerosScreen
import com.rei.elbosque.ui.NumerosViewModel
import com.rei.elbosque.ui.PuzzleScreen
import com.rei.elbosque.ui.PuzzleViewModel
import com.rei.elbosque.ui.PlantaScreen
import com.rei.elbosque.ui.PlantaViewModel
import com.rei.elbosque.ui.QueFaltaScreen
import com.rei.elbosque.ui.QueFaltaViewModel
import com.rei.elbosque.ui.OrdenaTamanoScreen
import com.rei.elbosque.ui.OrdenaTamanoViewModel
import com.rei.elbosque.ui.RespiraScreen
import com.rei.elbosque.ui.RespiraViewModel
import com.rei.elbosque.ui.QuienDiceEstoScreen
import com.rei.elbosque.ui.QuienDiceEstoViewModel
import com.rei.elbosque.ui.RecompensasViewModel
import com.rei.elbosque.ui.RitmoScreen
import com.rei.elbosque.ui.RitmoViewModel
import com.rei.elbosque.ui.RutinaDiariaScreen
import com.rei.elbosque.ui.RutinaDiariaViewModel
import com.rei.elbosque.ui.SombrasScreen
import com.rei.elbosque.ui.SombrasViewModel
import com.rei.elbosque.ui.SonidosAnimalesScreen
import com.rei.elbosque.ui.SonidosAnimalesViewModel
import com.rei.elbosque.ui.TrazoScreen
import com.rei.elbosque.ui.TrazoViewModel
import com.rei.elbosque.ui.VestirReiScreen
import com.rei.elbosque.ui.VestirReiViewModel
import com.rei.elbosque.ui.theme.BosqueDeReiTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ocultarBarras()

        setContent {
            BosqueDeReiTheme {
                val navController = rememberNavController()
                val recompensas: RecompensasViewModel = viewModel()
                val progreso by recompensas.progreso.collectAsStateWithLifecycle()
                val stickers by recompensas.recompensas.collectAsStateWithLifecycle()
                val narrador = remember { Narrador(applicationContext) }
                val musicaMenu = remember { MusicaMenu(applicationContext) }
                val entradaActual by navController.currentBackStackEntryAsState()
                val lifecycleOwner = LocalLifecycleOwner.current
                var celebracionId by remember { mutableIntStateOf(0) }
                var aciertoId by remember { mutableIntStateOf(0) }

                DisposableEffect(Unit) {
                    onDispose {
                        musicaMenu.cerrar()
                        narrador.cerrar()
                    }
                }

                // La música acompaña el menú y se calla al entrar en cualquier actividad.
                LaunchedEffect(entradaActual?.destination?.route) {
                    // Ninguna consigna del juego anterior debe continuar en la nueva pantalla.
                    narrador.detener()
                    if (entradaActual?.destination?.route == "inicio") {
                        musicaMenu.reproducir()
                    } else {
                        musicaMenu.pausar()
                    }
                }

                // Nunca deja música sonando cuando la app se minimiza o se bloquea.
                DisposableEffect(lifecycleOwner, entradaActual?.destination?.route) {
                    val observador = LifecycleEventObserver { _, evento ->
                        when (evento) {
                            Lifecycle.Event.ON_RESUME ->
                                if (entradaActual?.destination?.route == "inicio") {
                                    musicaMenu.reproducir()
                                }
                            Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP ->
                                musicaMenu.pausar()
                            else -> Unit
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observador)
                    onDispose { lifecycleOwner.lifecycle.removeObserver(observador) }
                }

                LaunchedEffect(Unit) {
                    recompensas.stickerDesbloqueado.collect {
                        celebracionId++
                        Sonidos.celebracion()
                        narrador.felicitar("¡Yupi! ¡Lo hiciste muy bien, Rei! Tienes un sticker nuevo")
                    }
                }
                LaunchedEffect(Unit) {
                    recompensas.aciertoCelebrado.collect { aciertoId++ }
                }

                Box(Modifier.fillMaxSize()) {
                    NavHost(navController, startDestination = "inicio") {
                        composable("inicio") {
                            InicioScreen(
                                estrellas = progreso.estrellas,
                                narrador = narrador,
                                onAbrir = { ruta ->
                                    navController.navigate(ruta) { launchSingleTop = true }
                                }
                            )
                        }
                        composable("formas") {
                            val vm: FormasViewModel = viewModel()
                            FormasScreen(vm, narrador, recompensas::premiarAcierto) {
                                navController.popBackStack()
                            }
                        }
                        composable("numeros") {
                            val vm: NumerosViewModel = viewModel()
                            NumerosScreen(vm, narrador, recompensas::premiarAcierto) {
                                navController.popBackStack()
                            }
                        }
                        composable("animales") {
                            val vm: AnimalesViewModel = viewModel()
                            AnimalesScreen(vm, narrador, recompensas::premiarAcierto) {
                                navController.popBackStack()
                            }
                        }
                        composable("trazo") {
                            val vm: TrazoViewModel = viewModel()
                            TrazoScreen(vm, narrador, recompensas::premiarAcierto) {
                                navController.popBackStack()
                            }
                        }
                        composable("bus") {
                            val vm: BusViewModel = viewModel()
                            BusScreen(vm, narrador, recompensas::premiarAcierto) {
                                navController.popBackStack()
                            }
                        }
                        composable("sombras") {
                            val vm: SombrasViewModel = viewModel()
                            SombrasScreen(vm, narrador, recompensas::premiarAcierto) {
                                navController.popBackStack()
                            }
                        }
                        composable("colores") {
                            val vm: ColoresViewModel = viewModel()
                            ColoresScreen(vm, narrador, recompensas::premiarAcierto) {
                                navController.popBackStack()
                            }
                        }
                        composable("sonidos_animales") {
                            val vm: SonidosAnimalesViewModel = viewModel()
                            SonidosAnimalesScreen(vm, narrador, recompensas::premiarAcierto) {
                                navController.popBackStack()
                            }
                        }
                        composable("quien_dice_esto") {
                            val vm: QuienDiceEstoViewModel = viewModel()
                            QuienDiceEstoScreen(vm, narrador, recompensas::premiarAcierto) {
                                navController.popBackStack()
                            }
                        }
                        composable("burbujas") {
                            val vm: BurbujasViewModel = viewModel()
                            BurbujasScreen(vm, narrador, recompensas::premiarAcierto) {
                                navController.popBackStack()
                            }
                        }
                        composable("grande_pequeno") {
                            val vm: GrandePequenoViewModel = viewModel()
                            GrandePequenoScreen(vm, narrador, recompensas::premiarAcierto) {
                                navController.popBackStack()
                            }
                        }
                        composable("clasificar_color") {
                            val vm: ClasificarColorViewModel = viewModel()
                            ClasificarColorScreen(vm, narrador, recompensas::premiarAcierto) {
                                navController.popBackStack()
                            }
                        }
                        composable("puzzle") {
                            val vm: PuzzleViewModel = viewModel()
                            PuzzleScreen(vm, narrador, recompensas::premiarAcierto) {
                                navController.popBackStack()
                            }
                        }
                        composable("vestir_rei") {
                            val vm: VestirReiViewModel = viewModel()
                            VestirReiScreen(vm, narrador, recompensas::premiarAcierto) {
                                navController.popBackStack()
                            }
                        }
                        composable("emociones") {
                            val vm: EmocionesViewModel = viewModel()
                            EmocionesScreen(vm, narrador, recompensas::premiarAcierto) {
                                navController.popBackStack()
                            }
                        }
                        composable("contar_hasta_3") {
                            val vm: ContarHasta3ViewModel = viewModel()
                            ContarHasta3Screen(vm, narrador, recompensas::premiarAcierto) {
                                navController.popBackStack()
                            }
                        }
                        composable("rutina_diaria") {
                            val vm: RutinaDiariaViewModel = viewModel()
                            RutinaDiariaScreen(vm, narrador, recompensas::premiarAcierto) {
                                navController.popBackStack()
                            }
                        }
                        composable("album") {
                            AlbumScreen(stickers, progreso.estrellas, narrador) {
                                navController.popBackStack()
                            }
                        }
                        composable("busca_objeto") {
                            val vm: BuscaObjetoViewModel = viewModel()
                            BuscaObjetoScreen(vm, narrador, recompensas::premiarAcierto) {
                                navController.popBackStack()
                            }
                        }
                        composable("alimenta_animal") {
                            val vm: AlimentaAnimalViewModel = viewModel()
                            AlimentaAnimalScreen(vm, narrador, recompensas::premiarAcierto) {
                                navController.popBackStack()
                            }
                        }
                        composable("mi_planta") {
                            val vm: PlantaViewModel = viewModel()
                            PlantaScreen(vm, narrador, recompensas::premiarAcierto) {
                                navController.popBackStack()
                            }
                        }
                        composable("donde_vive") {
                            val vm: DondeViveViewModel = viewModel()
                            DondeViveScreen(vm, narrador, recompensas::premiarAcierto) {
                                navController.popBackStack()
                            }
                        }
                        composable("laberinto") {
                            val vm: LaberintoViewModel = viewModel()
                            LaberintoScreen(vm, narrador, recompensas::premiarAcierto) {
                                navController.popBackStack()
                            }
                        }
                        composable("ritmo") {
                            val vm: RitmoViewModel = viewModel()
                            RitmoScreen(vm, narrador, recompensas::premiarAcierto) {
                                navController.popBackStack()
                            }
                        }
                        composable("que_falta") {
                            val vm: QueFaltaViewModel = viewModel()
                            QueFaltaScreen(vm, narrador, recompensas::premiarAcierto) {
                                navController.popBackStack()
                            }
                        }
                        composable("ordena_tamano") {
                            val vm: OrdenaTamanoViewModel = viewModel()
                            OrdenaTamanoScreen(vm, narrador, recompensas::premiarAcierto) {
                                navController.popBackStack()
                            }
                        }
                        composable("respira") {
                            val vm: RespiraViewModel = viewModel()
                            RespiraScreen(vm, narrador, recompensas::premiarAcierto) {
                                navController.popBackStack()
                            }
                        }
                    }
                    if (celebracionId > 0) {
                        Confetti(key = celebracionId) {
                            // El propio efecto desaparece después de tres segundos.
                            celebracionId = 0
                        }
                    }
                    if (aciertoId > 0) {
                        CelebracionAcierto(key = aciertoId) { aciertoId = 0 }
                    }
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) ocultarBarras()
    }

    private fun ocultarBarras() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
