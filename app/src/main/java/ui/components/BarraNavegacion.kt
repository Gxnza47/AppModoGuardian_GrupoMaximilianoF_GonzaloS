package com.example.modoguardian_grupomg.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.modoguardian_grupomg.model.Rol
import com.example.modoguardian_grupomg.navigation.Screen

private fun etiquetaPestana(screen: Screen): String = when (screen) {
    Screen.Inicio -> "Inicio"
    Screen.Camaras -> "Cámaras"
    Screen.Eventos -> "Eventos"
    Screen.Alertas -> "Alertas"
    else -> "Perfil"
}

private fun iconoPestana(screen: Screen): ImageVector = when (screen) {
    Screen.Inicio -> Icons.Filled.Home
    Screen.Camaras -> Icons.Filled.Videocam
    Screen.Eventos -> Icons.AutoMirrored.Filled.List
    Screen.Alertas -> Icons.Filled.Notifications
    else -> Icons.Filled.Person
}

// Barra inferior: solo muestra las pestañas que el rol puede abrir
@Composable
fun BarraNavegacion(
    rutaActual: String?,
    rol: Rol,
    alertasPendientes: Int,
    onSeleccionar: (String) -> Unit
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        Screen.pestanas.filter { rol.puedeAcceder(it.route) }.forEach { screen ->
            NavigationBarItem(
                selected = rutaActual == screen.route,
                onClick = { onSeleccionar(screen.route) },
                icon = {
                    BadgedBox(badge = {
                        if (screen == Screen.Alertas && alertasPendientes > 0) {
                            Badge { Text(alertasPendientes.toString()) }
                        }
                    }) {
                        Icon(iconoPestana(screen), contentDescription = etiquetaPestana(screen))
                    }
                },
                label = { Text(etiquetaPestana(screen)) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}
