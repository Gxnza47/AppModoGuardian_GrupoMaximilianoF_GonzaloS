package com.example.modoguardian_grupomg.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.modoguardian_grupomg.model.Evento
import com.example.modoguardian_grupomg.model.ResolucionAlerta
import com.example.modoguardian_grupomg.model.calcularEstadoCamaras
import com.example.modoguardian_grupomg.ui.components.GuardianCard
import com.example.modoguardian_grupomg.ui.components.IconoCircular
import com.example.modoguardian_grupomg.ui.components.Pastilla
import com.example.modoguardian_grupomg.ui.theme.ColorAviso
import com.example.modoguardian_grupomg.ui.theme.ColorExito

// Pestaña «Cámaras»: lista de todas las cámaras con su estado
@Composable
fun CamarasScreen(
    eventos: List<Evento>,
    resoluciones: Map<Int, ResolucionAlerta>,
    referencia: Long,
    onAbrirCamara: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val camaras = remember(eventos, resoluciones, referencia) {
        calcularEstadoCamaras(eventos, resoluciones, referencia)
    }
    Box(modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column {
                    Text("Cámaras", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(
                        "${camaras.count { it.enLinea }} de ${camaras.size} en línea",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(camaras, key = { it.id }) { camara ->
                val color = if (camara.enLinea) ColorExito else ColorAviso
                GuardianCard(modifier = Modifier.fillMaxWidth(), onClick = { onAbrirCamara(camara.id) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconoCircular(if (camara.enLinea) Icons.Filled.Videocam else Icons.Filled.VideocamOff, color)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(camara.nombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(
                                "${camara.codigo} · ${camara.zona}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "${camara.totalEventos} eventos · ${camara.pendientes} pendientes",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Pastilla(if (camara.enLinea) "EN LÍNEA" else "SIN CONEXIÓN", color, conPunto = true)
                    }
                }
            }
        }
    }
}
