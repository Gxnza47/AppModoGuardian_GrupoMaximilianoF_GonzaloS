package com.example.modoguardian_grupomg.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.modoguardian_grupomg.model.CatalogoZonas
import com.example.modoguardian_grupomg.model.ResolucionAlerta
import com.example.modoguardian_grupomg.model.Turno
import com.example.modoguardian_grupomg.model.UsuarioAutenticado
import com.example.modoguardian_grupomg.ui.components.DialogoOrigenImagen
import com.example.modoguardian_grupomg.ui.components.EtiquetaSeccion
import com.example.modoguardian_grupomg.ui.components.GuardianCard
import com.example.modoguardian_grupomg.ui.components.IconoCircular
import com.example.modoguardian_grupomg.ui.components.ImagenInteligente
import com.example.modoguardian_grupomg.ui.components.Pastilla
import com.example.modoguardian_grupomg.ui.components.rememberSelectorImagen
import com.example.modoguardian_grupomg.ui.theme.ColorCritico
import com.example.modoguardian_grupomg.ui.theme.ColorExito
import com.example.modoguardian_grupomg.viewmodel.AjustesViewModel
import com.example.modoguardian_grupomg.viewmodel.EstadoViewModel
import com.example.modoguardian_grupomg.viewmodel.PerfilViewModel

// Guía 13: foto de perfil desde la galería o la cámara (ImagenInteligente + ActivityResultLauncher).
// Reemplaza a la antigua pantalla de prueba «Mi perfil» de la sección Guías.
@Composable
fun PerfilScreen(
    usuario: UsuarioAutenticado,
    perfilViewModel: PerfilViewModel,
    estadoViewModel: EstadoViewModel,
    ajustesViewModel: AjustesViewModel,
    resoluciones: Map<Int, ResolucionAlerta>,
    puedeVerEventos: Boolean,
    onVerCamaras: () -> Unit,
    onVerEventos: () -> Unit,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Observa la imagen guardada en el ViewModel
    val imagenUri: Uri? by perfilViewModel.imagenUri.collectAsState()
    val activo by estadoViewModel.activo.collectAsState()
    val oscuro by ajustesViewModel.modoOscuro.collectAsState()
    val push by ajustesViewModel.notificacionesPush.collectAsState()
    val criticas by ajustesViewModel.alertasCriticas.collectAsState()

    // Cada usuario ve su propia foto
    LaunchedEffect(usuario.email) { perfilViewModel.cargarParaUsuario(usuario.email) }

    var mostrarOrigen by remember { mutableStateOf(false) }
    val selector = rememberSelectorImagen { uri -> perfilViewModel.actualizarImagenDesdeGaleria(uri) }
    if (mostrarOrigen) {
        DialogoOrigenImagen(selector = selector, onCerrar = { mostrarOrigen = false }, titulo = "Foto de perfil")
    }

    Box(modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column {
                Text("Mi Perfil", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    "Cuenta y preferencias del turno",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            GuardianCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        ImagenInteligente(uri = imagenUri, tamano = 80.dp)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(usuario.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            usuario.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Pastilla(usuario.rol.etiqueta.uppercase(), MaterialTheme.colorScheme.primary)
                            when (activo) {
                                true -> Pastilla("TURNO ACTIVO", ColorExito, conPunto = true)
                                false -> Pastilla("FUERA DE SERVICIO", MaterialTheme.colorScheme.onSurfaceVariant)
                                null -> Unit
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { mostrarOrigen = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Cambiar foto de perfil")
                }
            }

            // Datos del turno
            GuardianCard(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    DatoTurno(Turno.ETIQUETA, "Turno")
                    DatoTurno("${CatalogoZonas.total} asignadas", "Cámaras")
                    DatoTurno(
                        resoluciones.values.count { it != ResolucionAlerta.ESCALADA }.toString() + " resueltas",
                        "Alertas hoy"
                    )
                }
            }

            EtiquetaSeccion("Mi cuenta")
            GuardianCard(Modifier.fillMaxWidth()) {
                FilaNavegable(Icons.Filled.Videocam, "Mis cámaras asignadas", "${CatalogoZonas.total}", onVerCamaras)
                if (puedeVerEventos) {
                    Spacer(Modifier.height(8.dp))
                    FilaNavegable(Icons.Filled.History, "Historial de turnos", "Eventos y registros", onVerEventos)
                }
            }

            EtiquetaSeccion("Preferencias")
            GuardianCard(Modifier.fillMaxWidth()) {
                FilaInterruptor(
                    Icons.Filled.DarkMode, "Modo oscuro", "Tema oscuro aplicado", oscuro,
                    ajustesViewModel::cambiarModoOscuro
                )
                Spacer(Modifier.height(8.dp))
                FilaInterruptor(
                    Icons.Filled.Notifications, "Notificaciones push", "Preferencia guardada", push,
                    ajustesViewModel::cambiarNotificacionesPush
                )
                Spacer(Modifier.height(8.dp))
                FilaInterruptor(
                    Icons.Filled.NotificationsActive, "Alertas críticas", "Siempre visibles y sonoras", criticas,
                    ajustesViewModel::cambiarAlertasCriticas
                )
            }

            Button(
                onClick = onCerrarSesion,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorCritico.copy(alpha = 0.15f),
                    contentColor = ColorCritico
                )
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Cerrar Sesión", fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DatoTurno(valor: String, etiqueta: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(valor, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(etiqueta, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun FilaNavegable(icono: ImageVector, titulo: String, detalle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconoCircular(icono, MaterialTheme.colorScheme.primary, tamano = 40)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(titulo, style = MaterialTheme.typography.bodyLarge)
            Text(detalle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
    }
}

@Composable
private fun FilaInterruptor(
    icono: ImageVector,
    titulo: String,
    detalle: String,
    valor: Boolean,
    onCambio: (Boolean) -> Unit
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconoCircular(icono, MaterialTheme.colorScheme.primary, tamano = 40)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(titulo, style = MaterialTheme.typography.bodyLarge)
            Text(detalle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = valor, onCheckedChange = onCambio)
    }
}
