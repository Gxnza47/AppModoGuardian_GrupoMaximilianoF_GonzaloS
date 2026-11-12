package com.example.modoguardian_grupomg.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.modoguardian_grupomg.ui.theme.ColorExito
import com.example.modoguardian_grupomg.ui.theme.TextoSecundario
import com.example.modoguardian_grupomg.viewmodel.EstadoViewModel

// Guía 12: interruptor del «Modo Guardián» con estado persistente (DataStore) y feedback animado.
// Antes vivía en la pantalla PantallaPrincipal; ahora es una tarjeta del Inicio.
@Composable
fun ModoGuardianCard(
    viewModel: EstadoViewModel,
    modifier: Modifier = Modifier
) {
    // Observa los estados del ViewModel
    val estado by viewModel.activo.collectAsState()
    val mostrarMensaje by viewModel.mostrarMensaje.collectAsState()

    // rememberSaveable: este contador sobrevive a rotaciones de pantalla
    var cambios by rememberSaveable { mutableStateOf(0) }

    val valorEstado = estado

    GuardianCard(modifier = modifier.fillMaxWidth()) {
        EtiquetaSeccion("Modo Guardián")
        Spacer(Modifier.height(12.dp))

        if (valorEstado == null) {
            // Mientras carga el estado guardado, mostramos el loader
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val estaActivo = valorEstado

            // Animación 1: el color cambia suavemente según el estado
            val colorAnimado by animateColorAsState(
                targetValue = if (estaActivo) ColorExito else TextoSecundario,
                animationSpec = tween(durationMillis = 500),
                label = "colorBoton"
            )

            // Texto derivado del estado
            val textoBoton by remember(estaActivo) {
                derivedStateOf { if (estaActivo) "Desactivar" else "Activar" }
            }

            // Animación extra 2: el ícono cambia de tamaño
            val tamanoIcono by animateDpAsState(
                targetValue = if (estaActivo) 56.dp else 40.dp,
                animationSpec = tween(durationMillis = 500),
                label = "tamanoIcono"
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(64.dp), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = "Estado del modo",
                        tint = colorAnimado,
                        modifier = Modifier.size(tamanoIcono)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Animación extra 3: el texto de estado cambia con una transición suave
                    Crossfade(targetState = estaActivo, label = "textoEstado") { activo ->
                        Text(
                            text = if (activo) "Modo Guardián ACTIVO" else "Modo Guardián apagado",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Text(
                        text = "Veces que cambiaste el modo: $cambios",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.alternarEstado()
                    cambios++
                },
                colors = ButtonDefaults.buttonColors(containerColor = colorAnimado),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(textoBoton, style = MaterialTheme.typography.titleMedium)
            }

            // Mensaje de éxito con animación de aparecer y desaparecer
            AnimatedVisibility(visible = mostrarMensaje) {
                Text(
                    text = "¡Estado guardado exitosamente!",
                    color = ColorExito,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}
