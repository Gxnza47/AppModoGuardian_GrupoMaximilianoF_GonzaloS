package com.example.modoguardian_grupomg.ui.screens

import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.modoguardian_grupomg.model.EstadoCamara
import com.example.modoguardian_grupomg.model.Evento
import com.example.modoguardian_grupomg.model.PostUiState
import com.example.modoguardian_grupomg.model.ResolucionAlerta
import com.example.modoguardian_grupomg.model.Severidad
import com.example.modoguardian_grupomg.model.Turno
import com.example.modoguardian_grupomg.model.UsuarioAutenticado
import com.example.modoguardian_grupomg.model.calcularEstadoCamaras
import com.example.modoguardian_grupomg.model.pendientes
import com.example.modoguardian_grupomg.ui.components.EtiquetaSeccion
import com.example.modoguardian_grupomg.ui.components.EventoItem
import com.example.modoguardian_grupomg.ui.components.GuardianCard
import com.example.modoguardian_grupomg.ui.components.ImagenInteligente
import com.example.modoguardian_grupomg.ui.components.ModoGuardianCard
import com.example.modoguardian_grupomg.ui.components.Pastilla
import com.example.modoguardian_grupomg.ui.components.colorSeveridad
import com.example.modoguardian_grupomg.ui.theme.ColorAviso
import com.example.modoguardian_grupomg.ui.theme.ColorCritico
import com.example.modoguardian_grupomg.ui.theme.ColorExito
import com.example.modoguardian_grupomg.ui.utils.Formato
import com.example.modoguardian_grupomg.viewmodel.EstadoViewModel

@Composable
fun InicioScreen(
    usuario: UsuarioAutenticado,
    postState: PostUiState,
    eventos: List<Evento>,
    resoluciones: Map<Int, ResolucionAlerta>,
    estadoViewModel: EstadoViewModel,
    avatar: Uri?,
    onReintentar: () -> Unit,
    onAbrirEvento: (Int) -> Unit,
    onAbrirCamara: (Int) -> Unit,
    onVerCamaras: () -> Unit,
    onVerEventos: (() -> Unit)?,
    onVerAlertas: () -> Unit,
    onReportar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val referencia = postState.referencia
    val camaras = remember(eventos, resoluciones, referencia) {
        calcularEstadoCamaras(eventos, resoluciones, referencia)
    }
    val pendientes = remember(eventos, resoluciones, referencia) {
        eventos.pendientes(resoluciones, referencia).size
    }
    val activo by estadoViewModel.activo.collectAsState()

    Box(modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                EncabezadoInicio(usuario, avatar, activo, pendientes, onVerAlertas)
            }
            item {
                EstadoGeneralCard(camaras, referencia, postState.isLoading)
            }
            item {
                ModoGuardianCard(estadoViewModel)
            }
            item {
                SeccionConAccion("Cámaras asignadas", "Ver todas", onVerCamaras)
            }
            item {
                // Cuadrícula de 2 columnas con las primeras 4 cámaras
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    camaras.take(4).chunked(2).forEach { fila ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            fila.forEach { camara ->
                                CamaraMiniCard(
                                    camara = camara,
                                    hora = Formato.horaCompleta(if (referencia > 0) referencia else System.currentTimeMillis()),
                                    onClick = { onAbrirCamara(camara.id) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (fila.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Eventos en vivo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Box(Modifier.size(8.dp).clip(CircleShape).background(ColorCritico))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "LIVE FEED",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            when {
                postState.isLoading && eventos.isEmpty() -> item {
                    Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                postState.error != null && eventos.isEmpty() -> item {
                    ErrorConReintento(postState.error, onReintentar)
                }
                eventos.isEmpty() -> item {
                    Text("Aún no hay eventos registrados.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> items(eventos.take(4), key = { it.id }) { evento ->
                    val resolucion = resoluciones[evento.id]
                    EventoItem(
                        evento = evento,
                        estadoTexto = resolucion?.etiqueta ?: evento.severidad.etiqueta,
                        estadoColor = if (resolucion != null) ColorExito else colorSeveridad(evento.severidad),
                        onClick = { onAbrirEvento(evento.id) }
                    )
                }
            }

            if (onVerEventos != null && eventos.isNotEmpty()) {
                item {
                    TextButton(onClick = onVerEventos, modifier = Modifier.fillMaxWidth()) {
                        Text("Ver historial completo")
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = onReportar,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Reportar Incidencia", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun EncabezadoInicio(
    usuario: UsuarioAutenticado,
    avatar: Uri?,
    activo: Boolean?,
    pendientes: Int,
    onVerAlertas: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        ImagenInteligente(uri = avatar, tamano = 48.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                "Hola, ${usuario.nombre.substringBefore(' ')}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "${usuario.rol.etiqueta} • Turno ${Turno.ETIQUETA}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onVerAlertas) {
            BadgedBox(badge = { if (pendientes > 0) Badge { Text(pendientes.toString()) } }) {
                Icon(Icons.Filled.Notifications, contentDescription = "Alertas pendientes")
            }
        }
        when (activo) {
            true -> Pastilla("EN LÍNEA", ColorExito, conPunto = true)
            false -> Pastilla("FUERA DE SERVICIO", ColorAviso, conPunto = true)
            null -> Unit
        }
    }
}

@Composable
private fun EstadoGeneralCard(camaras: List<EstadoCamara>, referencia: Long, cargando: Boolean) {
    val total = camaras.size
    val activas = camaras.count { it.enLinea }
    val atencion = camaras.count { !it.enLinea || it.pendientes > 0 }
    val capacidad by animateFloatAsState(
        targetValue = if (total == 0) 0f else activas.toFloat() / total,
        label = "capacidad"
    )
    GuardianCard(modifier = Modifier.fillMaxWidth(), fondo = MaterialTheme.colorScheme.surfaceVariant) {
        Text("Estado General", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            "INSERCODE · GUARDIÁN",
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MiniMetrica("TOTAL", total.toString(), "Cámaras", MaterialTheme.colorScheme.onSurface, Modifier.weight(1f))
            MiniMetrica("ACTIVAS", activas.toString(), "En línea", ColorExito, Modifier.weight(1f))
            MiniMetrica("ATENCIÓN", atencion.toString(), "Revisar", ColorAviso, Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        Row {
            EtiquetaSeccion("Capacidad operativa", Modifier.weight(1f))
            Text("${(capacidad * 100).toInt()}%", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { capacidad },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
            trackColor = MaterialTheme.colorScheme.outline
        )
        Spacer(Modifier.height(12.dp))
        val sincronizado = referencia > 0
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                when {
                    cargando -> "Sincronizando…"
                    sincronizado -> "Última sincronización: ${Formato.hora(referencia)}"
                    else -> "Sin sincronizar"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            if (sincronizado && !cargando) Pastilla("SINCRONIZADO", ColorExito, conPunto = true)
        }
    }
}

@Composable
private fun MiniMetrica(titulo: String, valor: String, detalle: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.08f))
            .padding(12.dp)
    ) {
        Text(titulo, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
        Text(valor, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(detalle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SeccionConAccion(titulo: String, accion: String, onAccion: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            titulo,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Text(
            accion,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.clickable(onClick = onAccion)
        )
    }
}

@Composable
fun CamaraMiniCard(camara: EstadoCamara, hora: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    GuardianCard(modifier = modifier, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (camara.enLinea) Pastilla("REC", ColorCritico, conPunto = true)
            else Pastilla("OFFLINE", MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.weight(1f))
            Text(camara.codigo, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace)
        }
        Spacer(Modifier.height(12.dp))
        Icon(
            if (camara.enLinea) Icons.Filled.Videocam else Icons.Filled.VideocamOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(28.dp)
        )
        Text(
            if (camara.enLinea) hora else "--:--:--",
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Text(camara.nombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                camara.zona,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Text(
                if (camara.enLinea) "En línea" else "Sin conexión",
                style = MaterialTheme.typography.labelSmall,
                color = if (camara.enLinea) ColorExito else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ErrorConReintento(mensaje: String, onReintentar: () -> Unit, modifier: Modifier = Modifier) {
    GuardianCard(modifier = modifier.fillMaxWidth(), borde = ColorCritico.copy(alpha = 0.5f)) {
        Text(mensaje, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onReintentar) { Text("Reintentar") }
    }
}
