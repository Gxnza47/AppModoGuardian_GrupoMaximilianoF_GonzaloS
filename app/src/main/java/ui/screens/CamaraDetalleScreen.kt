package com.example.modoguardian_grupomg.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.modoguardian_grupomg.model.EstadoCamara
import com.example.modoguardian_grupomg.ui.components.EtiquetaSeccion
import com.example.modoguardian_grupomg.ui.components.FilaDetalle
import com.example.modoguardian_grupomg.ui.components.GuardianCard
import com.example.modoguardian_grupomg.ui.components.Pastilla
import com.example.modoguardian_grupomg.ui.theme.ColorCritico
import com.example.modoguardian_grupomg.ui.theme.ColorExito
import com.example.modoguardian_grupomg.ui.utils.Formato
import kotlinx.coroutines.delay

private val VerdeVisor = Color(0xFF39FF8A)

// Detalle de una cámara: visor en vivo (simulado), controles y estado del dispositivo.
// «Foto» abre el reporte de incidencia con esta cámara ya seleccionada.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CamaraDetalleScreen(
    camara: EstadoCamara?,
    onVolver: () -> Unit,
    onReportar: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(camara?.nombre ?: "Cámara", fontWeight = FontWeight.Bold)
                        if (camara != null) {
                            Text(
                                if (camara.enLinea) "EN VIVO · ${camara.codigo}" else "SIN CONEXIÓN · ${camara.codigo}",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = if (camara.enLinea) ColorCritico else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (camara == null) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("La cámara no existe o aún no se cargaron los datos.")
            }
            return@Scaffold
        }

        var grabando by rememberSaveable { mutableStateOf(camara.enLinea) }
        var segundos by rememberSaveable { mutableStateOf(0) }
        var microfono by rememberSaveable { mutableStateOf(false) }
        var luz by rememberSaveable { mutableStateOf(false) }
        var pantallaCompleta by rememberSaveable { mutableStateOf(false) }
        var ahora by remember { mutableLongStateOf(System.currentTimeMillis()) }

        // Reloj del visor
        LaunchedEffect(Unit) {
            while (true) {
                delay(1000)
                ahora = System.currentTimeMillis()
            }
        }
        // Cronómetro de grabación
        LaunchedEffect(grabando) {
            while (grabando) {
                delay(1000)
                segundos++
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Visor
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (pantallaCompleta) Modifier.height(420.dp) else Modifier.aspectRatio(4f / 3f))
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                if (luz) Color(0xFF1F5E3A) else Color(0xFF0C2A1B),
                                if (luz) Color(0xFF123824) else Color(0xFF05140D)
                            )
                        )
                    )
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val largo = 28.dp.toPx()
                    val margen = 16.dp.toPx()
                    val grosor = 2.dp.toPx()
                    val c = VerdeVisor.copy(alpha = 0.6f)
                    val w = size.width
                    val h = size.height
                    // Esquinas del visor
                    drawLine(c, Offset(margen, margen), Offset(margen + largo, margen), grosor)
                    drawLine(c, Offset(margen, margen), Offset(margen, margen + largo), grosor)
                    drawLine(c, Offset(w - margen, margen), Offset(w - margen - largo, margen), grosor)
                    drawLine(c, Offset(w - margen, margen), Offset(w - margen, margen + largo), grosor)
                    drawLine(c, Offset(margen, h - margen), Offset(margen + largo, h - margen), grosor)
                    drawLine(c, Offset(margen, h - margen), Offset(margen, h - margen - largo), grosor)
                    drawLine(c, Offset(w - margen, h - margen), Offset(w - margen - largo, h - margen), grosor)
                    drawLine(c, Offset(w - margen, h - margen), Offset(w - margen, h - margen - largo), grosor)
                }
                Column(Modifier.align(Alignment.TopStart).padding(start = 28.dp, top = 24.dp)) {
                    Text(
                        Formato.fechaHoraCompleta(ahora),
                        color = VerdeVisor,
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        "INSERCODE ${camara.codigo}",
                        color = VerdeVisor,
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = FontFamily.Monospace
                    )
                }
                if (!camara.enLinea) {
                    Text(
                        "SIN SEÑAL",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 28.dp, bottom = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (grabando) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(ColorCritico))
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        if (grabando) "REC ${Formato.cronometro(segundos)}" else "EN PAUSA",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Controles
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Control("FOTO", Icons.Filled.CameraAlt, activo = false, habilitado = true) { onReportar(camara.id) }
                Control(
                    if (grabando) "PARAR" else "GRABAR",
                    if (grabando) Icons.Filled.Stop else Icons.Filled.FiberManualRecord,
                    activo = grabando,
                    activoColor = ColorCritico,
                    habilitado = camara.enLinea
                ) { grabando = !grabando }
                Control(
                    "HABLAR",
                    if (microfono) Icons.Filled.Mic else Icons.Filled.MicOff,
                    activo = microfono,
                    habilitado = camara.enLinea
                ) { microfono = !microfono }
                Control("LUZ", Icons.Filled.LightMode, activo = luz, habilitado = camara.enLinea) { luz = !luz }
                Control(
                    "FULL",
                    if (pantallaCompleta) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                    activo = pantallaCompleta,
                    habilitado = true
                ) { pantallaCompleta = !pantallaCompleta }
            }

            // Estado del dispositivo (con datos reales calculados desde los eventos)
            GuardianCard(Modifier.fillMaxWidth()) {
                EtiquetaSeccion("Estado del dispositivo")
                Spacer(Modifier.height(8.dp))
                FilaDetalle("Estado", if (camara.enLinea) "En línea" else "Sin conexión")
                FilaDetalle("Zona", camara.zona)
                FilaDetalle("Eventos registrados", camara.totalEventos.toString())
                FilaDetalle("Alertas pendientes", camara.pendientes.toString())
                if (camara.enLinea) Pastilla("TRANSMITIENDO", ColorExito, conPunto = true)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun Control(
    etiqueta: String,
    icono: ImageVector,
    activo: Boolean,
    habilitado: Boolean,
    activoColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    val fondo by animateColorAsState(
        targetValue = if (activo) activoColor else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(300),
        label = "fondoControl"
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(fondo.copy(alpha = if (habilitado) 1f else 0.4f))
                .then(if (habilitado) Modifier.clickable(onClick = onClick) else Modifier),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icono,
                contentDescription = etiqueta,
                tint = if (activo) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            etiqueta,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
