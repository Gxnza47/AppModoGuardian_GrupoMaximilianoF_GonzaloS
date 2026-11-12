package com.example.modoguardian_grupomg.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.modoguardian_grupomg.R
import com.example.modoguardian_grupomg.model.Evento
import com.example.modoguardian_grupomg.model.ResolucionAlerta
import com.example.modoguardian_grupomg.model.Severidad
import com.example.modoguardian_grupomg.ui.components.DialogoOrigenImagen
import com.example.modoguardian_grupomg.ui.components.EtiquetaSeccion
import com.example.modoguardian_grupomg.ui.components.FilaDetalle
import com.example.modoguardian_grupomg.ui.components.GuardianCard
import com.example.modoguardian_grupomg.ui.components.IconoCircular
import com.example.modoguardian_grupomg.ui.components.SeveridadChip
import com.example.modoguardian_grupomg.ui.components.colorSeveridad
import com.example.modoguardian_grupomg.ui.components.iconoTipo
import com.example.modoguardian_grupomg.ui.components.rememberSelectorImagen
import com.example.modoguardian_grupomg.ui.theme.ColorAviso
import com.example.modoguardian_grupomg.ui.theme.ColorCritico
import com.example.modoguardian_grupomg.ui.theme.ColorExito
import com.example.modoguardian_grupomg.ui.utils.Formato
import com.example.modoguardian_grupomg.viewmodel.AlertaViewModel
import kotlinx.coroutines.delay

// Detalle de un evento. Si es una alerta, permite confirmarla, marcarla como falsa alarma,
// adjuntar una foto como evidencia (cámara/galería) y llamar a la Central.
// Las alertas críticas tienen 2 minutos para resolverse; si no, se escalan automáticamente.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventoDetalleScreen(
    evento: Evento?,
    referencia: Long,
    resolucion: ResolucionAlerta?,
    evidencia: String?,
    alertaViewModel: AlertaViewModel,
    onAdjuntarEvidencia: (Int, String) -> Unit,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val telefonoCentral = stringResource(R.string.telefono_central)
    var mostrarOrigen by remember { mutableStateOf(false) }

    val selector = rememberSelectorImagen { uri ->
        if (evento != null) onAdjuntarEvidencia(evento.id, uri.toString())
    }

    // Cuenta regresiva (solo alertas críticas sin resolver)
    val conPlazo = evento != null && evento.severidad == Severidad.CRITICA && resolucion == null
    var restante by remember { mutableIntStateOf(AlertaViewModel.PLAZO_SEGUNDOS) }
    LaunchedEffect(evento?.id, conPlazo) {
        if (evento != null && conPlazo) {
            alertaViewModel.iniciarPlazo(evento.id)
            while (true) {
                val segundos = alertaViewModel.segundosRestantes(evento.id)
                restante = segundos
                if (segundos <= 0) {
                    alertaViewModel.escalar(evento.id)
                    break
                }
                delay(1000)
            }
        }
    }

    if (mostrarOrigen) {
        DialogoOrigenImagen(
            selector = selector,
            onCerrar = { mostrarOrigen = false },
            titulo = "Evidencia fotográfica"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (evento?.severidad) {
                            Severidad.CRITICA -> "Alerta Crítica"
                            Severidad.ALERTA -> "Alerta"
                            else -> "Detalle del evento"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (evento != null) {
                        SeveridadChip(evento.severidad)
                        Text(
                            Formato.hace(evento.instante, if (referencia > 0) referencia else System.currentTimeMillis()),
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 8.dp, end = 16.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (evento == null) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("El evento ya no existe.")
            }
            return@Scaffold
        }

        val color = colorSeveridad(evento.severidad)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tarjeta principal
            GuardianCard(Modifier.fillMaxWidth(), borde = color.copy(alpha = 0.6f)) {
                Row(verticalAlignment = Alignment.Top) {
                    IconoCircular(iconoTipo(evento.tipo), color, tamano = 56)
                    Spacer(Modifier.width(14.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(evento.tipo.etiqueta, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Place, null, tint = color, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("${evento.nombreCamara} - ${evento.codigoCamara}")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AccessTime, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(Formato.fechaHoraCompleta(evento.instante), fontFamily = FontFamily.Monospace)
                        }
                        if (evento.confianzaIA != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Visibility, null, tint = ColorExito, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Confianza IA: ${evento.confianzaIA}%", color = ColorExito, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
                if (evento.confianzaIA != null) {
                    Spacer(Modifier.height(12.dp))
                    val confianza by animateFloatAsState(evento.confianzaIA / 100f, label = "confianza")
                    LinearProgressIndicator(
                        progress = { confianza },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                        color = ColorExito,
                        trackColor = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Evidencia fotográfica
            GuardianCard(Modifier.fillMaxWidth()) {
                EtiquetaSeccion("Evidencia")
                Spacer(Modifier.height(8.dp))
                if (evidencia != null) {
                    AsyncImage(
                        model = Uri.parse(evidencia),
                        contentDescription = "Evidencia adjunta",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                } else {
                    Text("Sin evidencia fotográfica adjunta.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Detalles
            GuardianCard(Modifier.fillMaxWidth()) {
                EtiquetaSeccion("Detalles del evento")
                FilaDetalle("Tipo", evento.tipo.categoria)
                FilaDetalle("Zona", "${evento.nombreCamara} - ${evento.zona}")
                FilaDetalle("Cámara", evento.codigoCamara)
                FilaDetalle("Duración", "${evento.duracionSegundos} segundos")
                FilaDetalle("Detalle", evento.detalle)
                if (evento.observaciones.isNotBlank()) FilaDetalle("Observaciones", evento.observaciones)
            }

            if (evento.esAccionable) {
                if (resolucion == null) {
                    AccionesRequeridas(
                        puedeAdjuntar = true,
                        onConfirmar = { alertaViewModel.confirmar(evento.id) },
                        onFalsaAlarma = { alertaViewModel.marcarFalsaAlarma(evento.id) },
                        onFoto = { mostrarOrigen = true },
                        onLlamar = {
                            // Solo abre el marcador con el número de la Central; el usuario decide si llama
                            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$telefonoCentral")))
                        }
                    )
                } else {
                    ResultadoResolucion(resolucion)
                    OutlinedButton(onClick = { mostrarOrigen = true }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Adjuntar o cambiar evidencia")
                    }
                }
            }

            // Plazo de respuesta
            if (evento.severidad == Severidad.CRITICA) {
                GuardianCard(Modifier.fillMaxWidth(), borde = ColorCritico.copy(alpha = 0.4f)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Filled.Warning, null, tint = ColorCritico)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Protocolo Insercode: confirmar en menos de 2 minutos o se escala automáticamente a Central y Jefe de Seguridad.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                if (conPlazo) {
                    val avance by animateFloatAsState(
                        1f - restante / AlertaViewModel.PLAZO_SEGUNDOS.toFloat(),
                        label = "avancePlazo"
                    )
                    Column {
                        Row {
                            EtiquetaSeccion("Tiempo de respuesta", Modifier.weight(1f))
                            Text(
                                "${Formato.cronometro(restante)} restante",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { avance },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                            color = if (restante <= 30) ColorCritico else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AccionesRequeridas(
    puedeAdjuntar: Boolean,
    onConfirmar: () -> Unit,
    onFalsaAlarma: () -> Unit,
    onFoto: () -> Unit,
    onLlamar: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        EtiquetaSeccion("Acciones requeridas")
        Button(
            onClick = onConfirmar,
            colors = ButtonDefaults.buttonColors(containerColor = ColorCritico, contentColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Confirmar Alerta - Enviar a Central", fontWeight = FontWeight.SemiBold)
        }
        OutlinedButton(
            onClick = onFalsaAlarma,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = ColorAviso)
            Spacer(Modifier.width(8.dp))
            Text("Falsa Alarma", color = ColorAviso, fontWeight = FontWeight.SemiBold)
        }
        if (puedeAdjuntar) {
            OutlinedButton(
                onClick = onFoto,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Icon(Icons.Filled.CameraAlt, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Reportar Incidencia con foto")
            }
        }
        OutlinedButton(
            onClick = onLlamar,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Icon(Icons.Filled.Phone, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Llamar a Central")
        }
    }
}

@Composable
private fun ResultadoResolucion(resolucion: ResolucionAlerta) {
    val (texto, color) = when (resolucion) {
        ResolucionAlerta.CONFIRMADA -> "Alerta confirmada y enviada a Central" to ColorExito
        ResolucionAlerta.FALSA_ALARMA -> "Marcada como falsa alarma" to ColorAviso
        ResolucionAlerta.ESCALADA -> "Plazo vencido: escalada automáticamente a Central y Jefe de Seguridad" to ColorCritico
    }
    AnimatedVisibility(visible = true) {
        GuardianCard(Modifier.fillMaxWidth(), borde = color.copy(alpha = 0.6f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = color)
                Spacer(Modifier.width(12.dp))
                Text(texto, color = color, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
