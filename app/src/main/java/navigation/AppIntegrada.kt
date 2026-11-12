package com.example.modoguardian_grupomg.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.modoguardian_grupomg.model.calcularEstadoCamaras
import com.example.modoguardian_grupomg.model.pendientes
import com.example.modoguardian_grupomg.ui.components.BarraNavegacion
import com.example.modoguardian_grupomg.ui.screens.AlertasScreen
import com.example.modoguardian_grupomg.ui.screens.CamaraDetalleScreen
import com.example.modoguardian_grupomg.ui.screens.CamarasScreen
import com.example.modoguardian_grupomg.ui.screens.EventoDetalleScreen
import com.example.modoguardian_grupomg.ui.screens.InicioScreen
import com.example.modoguardian_grupomg.ui.screens.LoginScreen
import com.example.modoguardian_grupomg.ui.screens.PerfilScreen
import com.example.modoguardian_grupomg.ui.screens.PostScreen
import com.example.modoguardian_grupomg.ui.screens.RegistroScreen
import com.example.modoguardian_grupomg.ui.screens.ReportarIncidenciaScreen
import com.example.modoguardian_grupomg.ui.screens.ResumenScreen
import com.example.modoguardian_grupomg.viewmodel.AjustesViewModel
import com.example.modoguardian_grupomg.viewmodel.AlertaViewModel
import com.example.modoguardian_grupomg.viewmodel.EstadoViewModel
import com.example.modoguardian_grupomg.viewmodel.LoginViewModel
import com.example.modoguardian_grupomg.viewmodel.MainViewModel
import com.example.modoguardian_grupomg.viewmodel.PerfilViewModel
import com.example.modoguardian_grupomg.viewmodel.PostViewModel
import com.example.modoguardian_grupomg.viewmodel.UsuarioViewModel

// App integrada: Login -> app de monitoreo con barra inferior (Inicio, Cámaras, Eventos, Alertas, Perfil).
// Los ViewModels se crean aquí, a nivel de Activity, para compartir los datos entre pantallas.
@Composable
fun AppIntegrada(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    loginViewModel: LoginViewModel,
    ajustesViewModel: AjustesViewModel
) {
    val loginState by loginViewModel.uiState.collectAsState()
    val usuario = loginState.usuario
    val rol = usuario?.rol

    val postViewModel: PostViewModel = viewModel()
    val alertaViewModel: AlertaViewModel = viewModel()
    val estadoViewModel: EstadoViewModel = viewModel()
    val perfilViewModel: PerfilViewModel = viewModel()
    val usuarioViewModel: UsuarioViewModel = viewModel()

    val postState by postViewModel.uiState.collectAsState()
    val alertaState by alertaViewModel.estado.collectAsState()
    val resoluciones = alertaState.resoluciones
    val referencia = postState.referencia

    val eventos = remember(postState.posts, postState.referencia, postState.creadasEn) { postState.aEventos() }
    val pendientes = remember(eventos, resoluciones, referencia) { eventos.pendientes(resoluciones, referencia).size }

    val entry by navController.currentBackStackEntryAsState()
    val rutaActual = entry?.destination?.route
    val mostrarBarra = rol != null && Screen.pestanas.any { it.route == rutaActual }

    // Mensajes puntuales (incidencia enviada, evento editado o eliminado...)
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(postState.mensaje) {
        postState.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            postViewModel.consumirMensaje()
        }
    }

    fun irAPestana(ruta: String) {
        navController.navigate(ruta) {
            popUpTo(Screen.Inicio.route) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Surface(color = MaterialTheme.colorScheme.background) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            // Cada pantalla gestiona sus propios márgenes del sistema (barra de estado y de navegación)
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (mostrarBarra && rol != null) {
                    BarraNavegacion(
                        rutaActual = rutaActual,
                        rol = rol,
                        alertasPendientes = pendientes,
                        onSeleccionar = ::irAPestana
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Login.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Login.route) {
                    LoginScreen(loginViewModel, onCrearCuenta = { navController.navigate(Screen.Registro.route) })
                }

                // «Crear cuenta»: formulario con validaciones y resumen (antes Guía 11)
                composable(Screen.Registro.route) { RegistroScreen(navController, usuarioViewModel) }
                composable(Screen.Resumen.route) {
                    ResumenScreen(
                        usuarioViewModel,
                        onVolver = { navController.popBackStack(Screen.Login.route, inclusive = false) }
                    )
                }

                composable(Screen.Inicio.route) {
                    if (usuario != null) InicioScreen(
                        usuario = usuario,
                        postState = postState,
                        eventos = eventos,
                        resoluciones = resoluciones,
                        estadoViewModel = estadoViewModel,
                        avatar = perfilViewModel.imagenUri.collectAsState().value,
                        onReintentar = postViewModel::cargarPosts,
                        onAbrirEvento = { navController.navigate(Screen.EventoDetalle.crearRuta(it)) },
                        onAbrirCamara = { navController.navigate(Screen.CamaraDetalle.crearRuta(it)) },
                        onVerCamaras = { irAPestana(Screen.Camaras.route) },
                        onVerEventos = if (usuario.rol.puedeAcceder(Screen.Eventos.route)) {
                            { irAPestana(Screen.Eventos.route) }
                        } else null,
                        onVerAlertas = { irAPestana(Screen.Alertas.route) },
                        onReportar = { navController.navigate(Screen.Incidencia.crearRuta()) }
                    )
                }

                composable(Screen.Camaras.route) {
                    if (usuario != null) CamarasScreen(
                        eventos = eventos,
                        resoluciones = resoluciones,
                        referencia = referencia,
                        onAbrirCamara = { navController.navigate(Screen.CamaraDetalle.crearRuta(it)) }
                    )
                }

                composable(Screen.Eventos.route) {
                    if (usuario != null && usuario.rol.puedeAcceder(Screen.Eventos.route)) PostScreen(
                        postViewModel = postViewModel,
                        resoluciones = resoluciones,
                        rol = usuario.rol,
                        onAbrirEvento = { navController.navigate(Screen.EventoDetalle.crearRuta(it)) }
                    )
                }

                composable(Screen.Alertas.route) {
                    if (usuario != null) AlertasScreen(
                        eventos = eventos,
                        resoluciones = resoluciones,
                        referencia = referencia,
                        onAbrirEvento = { navController.navigate(Screen.EventoDetalle.crearRuta(it)) }
                    )
                }

                composable(Screen.Perfil.route) {
                    if (usuario != null) PerfilScreen(
                        usuario = usuario,
                        perfilViewModel = perfilViewModel,
                        estadoViewModel = estadoViewModel,
                        ajustesViewModel = ajustesViewModel,
                        resoluciones = resoluciones,
                        puedeVerEventos = usuario.rol.puedeAcceder(Screen.Eventos.route),
                        onVerCamaras = { irAPestana(Screen.Camaras.route) },
                        onVerEventos = { irAPestana(Screen.Eventos.route) },
                        onCerrarSesion = loginViewModel::logout
                    )
                }

                composable(
                    route = Screen.CamaraDetalle.route,
                    arguments = listOf(navArgument("id") { type = NavType.IntType })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getInt("id") ?: 0
                    if (usuario != null) {
                        val camara = remember(eventos, resoluciones, referencia, id) {
                            calcularEstadoCamaras(eventos, resoluciones, referencia).firstOrNull { it.id == id }
                        }
                        CamaraDetalleScreen(
                            camara = camara,
                            onVolver = { navController.popBackStack() },
                            onReportar = { navController.navigate(Screen.Incidencia.crearRuta(it)) }
                        )
                    }
                }

                composable(
                    route = Screen.EventoDetalle.route,
                    arguments = listOf(navArgument("id") { type = NavType.IntType })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getInt("id") ?: 0
                    if (usuario != null) {
                        EventoDetalleScreen(
                            evento = eventos.firstOrNull { it.id == id },
                            referencia = referencia,
                            resolucion = resoluciones[id],
                            evidencia = postState.evidencias[id],
                            alertaViewModel = alertaViewModel,
                            onAdjuntarEvidencia = postViewModel::adjuntarEvidencia,
                            onVolver = { navController.popBackStack() }
                        )
                    }
                }

                composable(
                    route = Screen.Incidencia.route,
                    arguments = listOf(navArgument("camara") {
                        type = NavType.IntType
                        defaultValue = 0
                    })
                ) { backStackEntry ->
                    if (usuario != null) {
                        ReportarIncidenciaScreen(
                            postViewModel = postViewModel,
                            camaraInicial = backStackEntry.arguments?.getInt("camara") ?: 0,
                            onVolver = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
