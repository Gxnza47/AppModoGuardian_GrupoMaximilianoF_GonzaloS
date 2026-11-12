package com.example.modoguardian_grupomg.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.modoguardian_grupomg.model.Evento
import com.example.modoguardian_grupomg.model.ResolucionAlerta
import com.example.modoguardian_grupomg.model.pendientes
import com.example.modoguardian_grupomg.ui.components.EventoItem
import com.example.modoguardian_grupomg.ui.components.colorSeveridad
import com.example.modoguardian_grupomg.ui.theme.ColorExito

// Pestaña «Alertas»: pendientes (vigentes y sin resolver) o historial de todas las alertas
@Composable
fun AlertasScreen(
    eventos: List<Evento>,
    resoluciones: Map<Int, ResolucionAlerta>,
    referencia: Long,
    onAbrirEvento: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var soloPendientes by rememberSaveable { mutableStateOf(true) }
    val pendientes = remember(eventos, resoluciones, referencia) { eventos.pendientes(resoluciones, referencia) }
    val historial = remember(eventos) { eventos.filter { it.esAccionable } }
    val lista = if (soloPendientes) pendientes else historial

    Box(modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Alertas", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = soloPendientes,
                            onClick = { soloPendientes = true },
                            label = { Text("Pendientes (${pendientes.size})") }
                        )
                        FilterChip(
                            selected = !soloPendientes,
                            onClick = { soloPendientes = false },
                            label = { Text("Historial (${historial.size})") }
                        )
                    }
                }
            }
            if (lista.isEmpty()) {
                item {
                    Text(
                        if (soloPendientes) "Sin alertas pendientes. ¡Buen trabajo!" else "Aún no hay alertas registradas.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            items(lista, key = { it.id }) { evento ->
                val resolucion = resoluciones[evento.id]
                EventoItem(
                    evento = evento,
                    estadoTexto = resolucion?.etiqueta ?: evento.severidad.etiqueta,
                    estadoColor = if (resolucion != null) ColorExito else colorSeveridad(evento.severidad),
                    onClick = { onAbrirEvento(evento.id) }
                )
            }
        }
    }
}
