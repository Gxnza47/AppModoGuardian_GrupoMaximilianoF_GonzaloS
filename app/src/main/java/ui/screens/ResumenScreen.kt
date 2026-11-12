package com.example.modoguardian_grupomg.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.modoguardian_grupomg.viewmodel.UsuarioViewModel

// Pantalla de resumen: muestra los datos ingresados en el formulario de «Crear cuenta»
@Composable
fun ResumenScreen(viewModel: UsuarioViewModel, onVolver: () -> Unit = {}) {
    // Observa el MISMO ViewModel que usa el formulario
    val estado by viewModel.estado.collectAsState()

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(16.dp)) {
        Text("Resumen del Registro", style = MaterialTheme.typography.headlineMedium)
        Text("Nombre: ${estado.nombre}")
        Text("Correo: ${estado.correo}")
        Text("Dirección: ${estado.direccion}")
        // La contraseña se oculta con asteriscos
        Text("Contraseña: ${"*".repeat(estado.clave.length)}")
        Text("Términos: ${if (estado.aceptaTerminos) "Aceptados" else "No aceptados"}")
        Text(
            "Demostración: la cuenta no se crea en un servidor. Para ingresar usa uno de los usuarios de prueba.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp)
        )
        Button(onClick = onVolver, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            Text("Volver al inicio de sesión")
        }
    }
}
