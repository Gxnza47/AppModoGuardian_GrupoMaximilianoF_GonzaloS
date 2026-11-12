package com.example.modoguardian_grupomg.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.modoguardian_grupomg.model.Evento
import com.example.modoguardian_grupomg.model.Severidad
import com.example.modoguardian_grupomg.model.TipoEvento
import com.example.modoguardian_grupomg.ui.theme.ColorAviso
import com.example.modoguardian_grupomg.ui.theme.ColorCritico
import com.example.modoguardian_grupomg.ui.theme.ColorExito
import com.example.modoguardian_grupomg.ui.utils.Formato

fun colorSeveridad(severidad: Severidad): Color = when (severidad) {
    Severidad.CRITICA -> ColorCritico
    Severidad.ALERTA -> ColorAviso
    Severidad.EVENTO -> ColorExito
}

fun iconoTipo(tipo: TipoEvento): ImageVector = when (tipo) {
    TipoEvento.INTRUSION -> Icons.Filled.Shield
    TipoEvento.MOVIMIENTO -> Icons.Filled.DirectionsWalk
    TipoEvento.ACCESO -> Icons.Filled.MeetingRoom
    TipoEvento.CAMARA_OFFLINE -> Icons.Filled.VideocamOff
    TipoEvento.RONDA -> Icons.Filled.Place
    TipoEvento.INCIDENCIA -> Icons.Filled.ReportProblem
}

// Tarjeta base con el estilo Insercode (fondo de superficie, borde fino, esquinas redondeadas)
@Composable
fun GuardianCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    borde: Color = MaterialTheme.colorScheme.outline,
    fondo: Color = MaterialTheme.colorScheme.surface,
    contenido: @Composable ColumnScope.() -> Unit
) {
    val forma = RoundedCornerShape(20.dp)
    Column(
        modifier = modifier
            .clip(forma)
            .background(fondo)
            .border(1.dp, borde, forma)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp),
        content = contenido
    )
}

// Texto pequeño en mayúsculas para títulos de sección (ej. «DETALLES DEL EVENTO»)
@Composable
fun EtiquetaSeccion(texto: String, modifier: Modifier = Modifier) {
    Text(
        text = texto.uppercase(),
        modifier = modifier,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.5.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
fun IconoCircular(icono: ImageVector, color: Color, modifier: Modifier = Modifier, tamano: Int = 44) {
    Box(
        modifier = modifier
            .size(tamano.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size((tamano * 0.5f).dp))
    }
}

@Composable
fun Pastilla(texto: String, color: Color, modifier: Modifier = Modifier, conPunto: Boolean = false) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.16f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (conPunto) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(6.dp))
        }
        Text(
            texto,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        )
    }
}

@Composable
fun SeveridadChip(severidad: Severidad, modifier: Modifier = Modifier) {
    Pastilla(severidad.etiqueta, colorSeveridad(severidad), modifier, conPunto = true)
}

// Fila de evento: franja de color según severidad, ícono, textos y estado a la derecha
@Composable
fun EventoItem(
    evento: Evento,
    estadoTexto: String?,
    estadoColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    menu: (@Composable () -> Unit)? = null
) {
    val color = colorSeveridad(evento.severidad)
    GuardianCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(Modifier.width(12.dp))
            IconoCircular(iconoTipo(evento.tipo), color)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    evento.tipo.etiqueta,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    evento.nombreCamara,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${Formato.hora(evento.instante)} · ${evento.zona} · ${evento.codigoCamara}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                if (estadoTexto != null) Pastilla(estadoTexto, estadoColor)
                menu?.invoke()
            }
        }
    }
}

@Composable
fun FilaDetalle(etiqueta: String, valor: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(etiqueta, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(16.dp))
        Text(
            valor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}
