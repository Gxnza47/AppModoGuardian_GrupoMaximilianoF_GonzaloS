package com.example.modoguardian_grupomg.ui.screens

import android.net.Uri
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.modoguardian_grupomg.model.CatalogoZonas
import com.example.modoguardian_grupomg.ui.components.EtiquetaSeccion
import com.example.modoguardian_grupomg.ui.components.ImagenInteligente
import com.example.modoguardian_grupomg.ui.components.rememberSelectorImagen
import com.example.modoguardian_grupomg.viewmodel.PostViewModel

// Reporte manual de una incidencia: se envía con POST a la API y puede llevar una foto de evidencia.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportarIncidenciaScreen(
    postViewModel: PostViewModel,
    camaraInicial: Int,
    onVolver: () -> Unit
) {
    var camaraId by rememberSaveable { mutableStateOf(if (camaraInicial in 1..CatalogoZonas.total) camaraInicial else 1) }
    var titulo by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var foto by rememberSaveable { mutableStateOf<Uri?>(null) }
    // Sin rememberSaveable: si la pantalla se recrea mientras se envía, no queda bloqueada en «Enviando…»
    var enviando by remember { mutableStateOf(false) }
    var intentoEnvio by rememberSaveable { mutableStateOf(false) }

    val selector = rememberSelectorImagen { uri -> foto = uri }

    val tituloError = intentoEnvio && titulo.trim().length < 3
    val descripcionError = intentoEnvio && descripcion.isBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportar incidencia", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EtiquetaSeccion("Cámara / zona")
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                (1..CatalogoZonas.total).forEach { id ->
                    FilterChip(
                        selected = camaraId == id,
                        onClick = { camaraId = id },
                        label = { Text(CatalogoZonas.nombre(id)) },
                        enabled = !enviando
                    )
                }
            }

            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Título") },
                singleLine = true,
                isError = tituloError,
                enabled = !enviando,
                supportingText = { if (tituloError) Text("Ingresa al menos 3 caracteres") },
                shape = RoundedCornerShape(16.dp)
            )
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Descripción de lo ocurrido") },
                minLines = 4,
                isError = descripcionError,
                enabled = !enviando,
                supportingText = { if (descripcionError) Text("Describe brevemente la incidencia") },
                shape = RoundedCornerShape(16.dp)
            )

            EtiquetaSeccion("Evidencia fotográfica (opcional)")
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Componente reutilizable de la Guía 13, con forma redondeada en lugar de círculo
                ImagenInteligente(uri = foto, tamano = 120.dp, forma = RoundedCornerShape(16.dp))
                Spacer(Modifier.width(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = selector.abrirCamara, enabled = !enviando) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Tomar foto")
                    }
                    OutlinedButton(onClick = selector.abrirGaleria, enabled = !enviando) {
                        Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Galería")
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    intentoEnvio = true
                    if (titulo.trim().length >= 3 && descripcion.isNotBlank() && !enviando) {
                        enviando = true
                        postViewModel.crearIncidencia(
                            titulo = titulo,
                            descripcion = descripcion,
                            camaraId = camaraId,
                            evidencia = foto?.toString(),
                            onResultado = { exito ->
                                enviando = false
                                if (exito) onVolver()
                            }
                        )
                    }
                },
                enabled = !enviando,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (enviando) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(10.dp))
                    Text("Enviando…")
                } else {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Enviar reporte", fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
