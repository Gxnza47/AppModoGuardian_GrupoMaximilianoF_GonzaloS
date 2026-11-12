package com.example.modoguardian_grupomg

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.modoguardian_grupomg.navigation.AppIntegrada
import com.example.modoguardian_grupomg.navigation.NavigationEvent
import com.example.modoguardian_grupomg.navigation.Screen
import com.example.modoguardian_grupomg.repository.DemoLoginRepository
import com.example.modoguardian_grupomg.repository.LocalLoginPreferences
import com.example.modoguardian_grupomg.ui.theme.ModoGuardian_GrupoMGTheme
import com.example.modoguardian_grupomg.viewmodel.AjustesViewModel
import com.example.modoguardian_grupomg.viewmodel.LoginViewModel
import com.example.modoguardian_grupomg.viewmodel.MainViewModel
import kotlinx.coroutines.flow.collect

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // El tema (claro/oscuro) se guarda en DataStore y se cambia desde el Perfil
            val ajustesViewModel: AjustesViewModel = viewModel()
            val oscuro by ajustesViewModel.modoOscuro.collectAsState()

            // Íconos de la barra de estado y de navegación acordes al tema
            DisposableEffect(oscuro) {
                enableEdgeToEdge(
                    statusBarStyle = if (oscuro) SystemBarStyle.dark(Color.TRANSPARENT)
                    else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                    navigationBarStyle = if (oscuro) SystemBarStyle.dark(Color.TRANSPARENT)
                    else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                )
                onDispose { }
            }

            ModoGuardian_GrupoMGTheme(darkTheme = oscuro) {
                val navController = rememberNavController()
                // Reutilizamos MainViewModel y NavigationEvent originales.
                val mainViewModel: MainViewModel = viewModel()
                val factory = remember {
                    LoginViewModel.Factory(DemoLoginRepository(), LocalLoginPreferences(applicationContext))
                }
                val loginViewModel: LoginViewModel = viewModel(factory = factory)
                val loginState by loginViewModel.uiState.collectAsState()
                val entry by navController.currentBackStackEntryAsState()
                val route = entry?.destination?.route

                LaunchedEffect(navController, mainViewModel) {
                    mainViewModel.navigationEvents.collect { event ->
                        when (event) {
                            is NavigationEvent.NavigateTo -> navController.navigate(event.route.route) {
                                event.popUpToRoute?.let { popUpTo(it.route) { inclusive = event.inclusive } }
                                launchSingleTop = event.singleTop
                            }
                            NavigationEvent.PopBackStack -> navController.popBackStack()
                            NavigationEvent.NavigateUp -> navController.navigateUp()
                        }
                    }
                }
                // La sesión se guarda en memoria; una ruta restaurada no concede acceso.
                LaunchedEffect(loginState.usuario, route) {
                    val usuario = loginState.usuario
                    if (route != null) {
                        when {
                            usuario == null && route !in Screen.rutasPublicas -> navController.navigate(Screen.Login.route) {
                                popUpTo(navController.graph.id) { inclusive = true }
                                launchSingleTop = true
                            }
                            usuario != null && route == Screen.Login.route -> mainViewModel.navigateTo(
                                Screen.Inicio, popUpToRoute = Screen.Login, inclusive = true, singleTop = true)
                            // Ej.: un Operador que intenta abrir «eventos» vuelve al inicio
                            usuario != null && !usuario.rol.puedeAcceder(route) ->
                                mainViewModel.navigateTo(Screen.Inicio, popUpToRoute = Screen.Inicio, singleTop = true)
                        }
                    }
                }
                AppIntegrada(navController, mainViewModel, loginViewModel, ajustesViewModel)
            }
        }
    }
}
