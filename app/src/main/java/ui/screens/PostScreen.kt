package com.example.modoguardian_grupomg.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.modoguardian_grupomg.model.Evento
import com.example.modoguardian_grupomg.model.PostUiState
import com.example.modoguardian_grupomg.model.ResolucionAlerta
import com.example.modoguardian_grupomg.model.Rol
import com.example.modoguardian_grupomg.model.Severidad
import com.example.modoguardian_grupomg.model.TipoEvento
import com.example.modoguardian_grupomg.model.Turno
import com.example.modoguardian_grupomg.model.pendientes
import com.example.modoguardian_grupomg.ui.components.EtiquetaSeccion
import com.example.modoguardian_grupomg.ui.components.EventoItem
import com.example.modoguardian_grupomg.ui.components.GuardianCard
import com.example.modoguardian_grupomg.ui.components.colorSeveridad
import com.example.modoguardian_grupomg.ui.theme.ColorAviso
import com.example.modoguardian_grupomg.ui.theme.ColorCritico
import com.example.modoguardian_grupomg.ui.theme.ColorExito
import com.example.modoguardian_grupomg.ui.utils.Formato
import com.example.modoguardian_grupomg.viewmodel.PostViewModel

// Pestañas de categoría del historial
enum class FiltroCategoria(val etiqueta: String) {
    TODOS("Todos"), ALERTAS("Alertas"), EVENTOS("Eventos"), ACCESOS("Accesos");

    fun acepta(evento: Evento): Boolean = when (this) {
        TODOS -> true
        ALERTAS -> evento.esAccionable || evento.tipo == TipoEvento.CAMARA_OFFLINE
        EVENTOS -> evento.tipo == TipoEvento.RONDA
        ACCESOS -> evento.tipo == TipoEvento.ACCESO
    }
}

// Aplica categoría, búsqueda y chips de día/severidad
fun filtrarEventos(
    eventos: List<Evento>,
    categoria: FiltroCategoria,
    consulta: String,
    soloHoy: Boolean,
    soloAyer: Boolean,
    soloCriticas: Boolean,
    referencia: Long
): List<Evento> {
    val q = consulta.trim().lowercase()
    return eventos.filter { e ->
        categoria.acepta(e) &&
            (!soloHoy || Formato.mismoDia(e.instante, referencia)) &&
            (!soloAyer || Formato.esAyer(e.instante, referencia)) &&
            (!soloCriticas || e.severidad == Severidad.CRITICA) &&
            (q.isEmpty() ||
                e.tipo.etiqueta.lowercase().contains(q) ||
                e.nombreCamara.lowercase().contains(q) ||
                e.zona.lowercase().contains(q) ||
                e.codigoCamara.lowercase().contains(q) ||
                e.detalle.lowercase().contains(q))
    }
}

// Pantalla conectada al ViewModel (Guía 14: PostViewModel + PostScreen)
@Composable
fun PostScreen(
    postViewModel: PostViewModel,
    resoluciones: Map<Int, ResolucionAlerta>,
    rol: Rol,
    onAbrirEvento: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by postViewModel.uiState.collectAsState()
    val context = LocalContext.current
    PostContent(
        state = state,
        resoluciones = resoluciones,
        puedeEditar = rol.puedeEditarEventos,
        onReintentar = postViewModel::cargarPosts,
        onAbrirEvento = onAbrirEvento,
        onEditar = postViewModel::actualizarPost,
        onEliminar = postViewModel::eliminarPost,
        onExportar = { texto ->
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Reporte de turno")
                putExtra(Intent.EXTRA_TEXT, texto)
            }
            context.startActivity(Intent.createChooser(intent, "Exportar reporte de turno"))
        },
        modifier = modifier
    )
}

// Contenido sin ViewModel (más fácil de probar con Compose UI Test)
@Composable
fun PostContent(
    state: PostUiState,
    resoluciones: Map<Int, ResolucionAlerta>,
    puedeEditar: Boolean,
    onReintentar: () -> Unit,
    onAbrirEvento: (Int) -> Unit,
    onEditar: (Int, String, String) -> Unit,
    onEliminar: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onExportar: (String) -> Unit = {}
) {
    val referencia = if (state.referencia > 0) state.referencia else System.currentTimeMillis()
    val eventos = remember(state.posts, state.referencia, state.creadasEn) { state.aEventos() }

    var categoria by rememberSaveable { mutableStateOf(FiltroCategoria.TODOS) }
    var consulta by rememberSaveable { mutableStateOf("") }
    var soloHoy by rememberSaveable { mutableStateOf(false) }
    var soloAyer by rememberSaveable { mutableStateOf(false) }
    var soloCriticas by rememberSaveable { mutableStateOf(false) }

    var editando by remember { mutableStateOf<Evento?>(null) }
    var eliminando by remember { mutableStateOf<Evento?>(null) }

    val filtrados = remember(eventos, categoria, consulta, soloHoy, soloAyer, soloCriticas, referencia) {
        filtrarEventos(eventos, categoria, consulta, soloHoy, soloAyer, soloCriticas, referencia)
    }
    val grupos = remember(filtrados) { filtrados.groupBy { Formato.inicioDeHora(it.instante) }.toList() }

    val hoy = remember(eventos, referencia) { eventos.filter { Formato.mismoDia(it.instante, referencia) } }
    val confirmadasHoy = hoy.count { resoluciones[it.id] == ResolucionAlerta.CONFIRMADA }
    val pendientes = remember(eventos, resoluciones, referencia) { eventos.pendientes(resoluciones, referencia).size }

    Box(modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .testTag("lista_eventos"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Historial", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(
                            "Turno ${Turno.ETIQUETA}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { onExportar(construirReporte(filtrados, resoluciones)) },
                        enabled = filtrados.isNotEmpty()
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "Exportar reporte de turno")
                    }
                }
            }

            item {
                GuardianCard(Modifier.fillMaxWidth(), fondo = MaterialTheme.colorScheme.surfaceVariant) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Resumen("HOY", hoy.size.toString(), "eventos registrados", MaterialTheme.colorScheme.onSurface)
                        Resumen("CONFIRMADAS", confirmadasHoy.toString(), "alertas hoy", ColorCritico)
                        Resumen("PENDIENTES", pendientes.toString(), "requieren atención", ColorAviso)
                    }
                    Spacer(Modifier.height(16.dp))
                    val progreso = Turno.progreso(referencia)
                    Row {
                        EtiquetaSeccion("Progreso de turno", Modifier.weight(1f))
                        Text(
                            "${(progreso * 100).toInt()}% • ${Turno.restante(referencia)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progreso },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                        trackColor = MaterialTheme.colorScheme.outline
                    )
                }
            }

            item {
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FiltroCategoria.entries.forEach { f ->
                        FilterChip(selected = categoria == f, onClick = { categoria = f }, label = { Text(f.etiqueta) })
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = consulta,
                    onValueChange = { consulta = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Buscar por cámara, zona o tipo…") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    shape = RoundedCornerShape(16.dp)
                )
            }

            item {
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = soloHoy,
                        onClick = { soloHoy = !soloHoy; if (soloHoy) soloAyer = false },
                        label = { Text("Hoy") }
                    )
                    FilterChip(
                        selected = soloAyer,
                        onClick = { soloAyer = !soloAyer; if (soloAyer) soloHoy = false },
                        label = { Text("Ayer") }
                    )
                    FilterChip(
                        selected = soloCriticas,
                        onClick = { soloCriticas = !soloCriticas },
                        label = { Text("Críticas") }
                    )
                }
            }

            when {
                state.isLoading && state.posts.isEmpty() -> item {
                    Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                state.error != null && state.posts.isEmpty() -> item {
                    ErrorConReintento(state.error, onReintentar)
                }

                filtrados.isEmpty() -> item {
                    Text(
                        "No hay eventos que coincidan con los filtros.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                else -> grupos.forEach { (inicioHora, lista) ->
                    item(key = "hora_$inicioHora") {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                Formato.etiquetaBloque(inicioHora, referencia),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "${lista.size} eventos",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    items(lista, key = { it.id }) { evento ->
                        val resolucion = resoluciones[evento.id]
                        if (puedeEditar) {
                            // Admin: puede editar (PUT) y eliminar (DELETE)
                            EventoItem(
                                evento = evento,
                                estadoTexto = textoEstado(evento, resolucion),
                                estadoColor = colorEstado(evento, resolucion),
                                onClick = { onAbrirEvento(evento.id) },
                                menu = {
                                    MenuEvento(
                                        onEditar = { editando = evento },
                                        onEliminar = { eliminando = evento }
                                    )
                                }
                            )
                        } else {
                            EventoItem(
                                evento = evento,
                                estadoTexto = textoEstado(evento, resolucion),
                                estadoColor = colorEstado(evento, resolucion),
                                onClick = { onAbrirEvento(evento.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    editando?.let { evento ->
        DialogoEditar(
            evento = evento,
            onCancelar = { editando = null },
            onGuardar = { titulo, descripcion ->
                onEditar(evento.id, titulo, descripcion)
                editando = null
            }
        )
    }

    eliminando?.let { evento ->
        AlertDialog(
            onDismissRequest = { eliminando = null },
            title = { Text("Eliminar evento") },
            text = { Text("¿Seguro que quieres eliminar «${evento.tipo.etiqueta}» de ${evento.nombreCamara}?") },
            confirmButton = {
                TextButton(onClick = { onEliminar(evento.id); eliminando = null }) {
                    Text("Eliminar", color = ColorCritico)
                }
            },
            dismissButton = { TextButton(onClick = { eliminando = null }) { Text("Cancelar") } }
        )
    }
}

private fun textoEstado(evento: Evento, resolucion: ResolucionAlerta?): String? = when {
    resolucion != null -> resolucion.etiqueta
    evento.tipo == TipoEvento.ACCESO -> "Autorizado"
    evento.tipo == TipoEvento.RONDA -> "Confirmada"
    else -> evento.severidad.etiqueta
}

@Composable
private fun colorEstado(evento: Evento, resolucion: ResolucionAlerta?) = when {
    resolucion == ResolucionAlerta.FALSA_ALARMA -> ColorAviso
    resolucion != null -> ColorExito
    else -> colorSeveridad(evento.severidad)
}

@Composable
private fun Resumen(titulo: String, valor: String, detalle: String, color: androidx.compose.ui.graphics.Color) {
    Column {
        Text(titulo, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
        Text(valor, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(detalle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MenuEvento(onEditar: () -> Unit, onEliminar: () -> Unit) {
    var abierto by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { abierto = true }, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Filled.MoreVert, contentDescription = "Opciones del evento")
        }
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            DropdownMenuItem(
                text = { Text("Editar") },
                leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                onClick = { abierto = false; onEditar() }
            )
            DropdownMenuItem(
                text = { Text("Eliminar") },
                leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                onClick = { abierto = false; onEliminar() }
            )
        }
    }
}

@Composable
private fun DialogoEditar(
    evento: Evento,
    onCancelar: () -> Unit,
    onGuardar: (String, String) -> Unit
) {
    var titulo by remember { mutableStateOf(evento.detalle) }
    var descripcion by remember { mutableStateOf(evento.observaciones) }
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Editar evento") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(titulo, { titulo = it }, label = { Text("Detalle") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    descripcion, { descripcion = it },
                    label = { Text("Observaciones") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onGuardar(titulo, descripcion) }, enabled = titulo.isNotBlank()) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}

// Texto plano del reporte de turno (se comparte con el selector del sistema)
fun construirReporte(eventos: List<Evento>, resoluciones: Map<Int, ResolucionAlerta>): String = buildString {
    appendLine("REPORTE DE TURNO · Insercode Guardián")
    appendLine("Turno ${Turno.ETIQUETA} · ${eventos.size} eventos")
    appendLine()
    eventos.forEach { e ->
        val estado = resoluciones[e.id]?.etiqueta ?: e.severidad.etiqueta
        appendLine("${Formato.fechaHoraCompleta(e.instante)} | ${e.tipo.etiqueta} | ${e.nombreCamara} (${e.codigoCamara}) | $estado")
    }
}
