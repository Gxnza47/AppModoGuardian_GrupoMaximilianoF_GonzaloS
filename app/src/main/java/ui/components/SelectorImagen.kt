package com.example.modoguardian_grupomg.ui.components

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

// Crea un archivo temporal en la caché y devuelve su URI seguro (vía FileProvider)
private fun crearUriParaFoto(context: Context): Uri {
    val carpeta = File(context.cacheDir, "images").apply { mkdirs() }
    val archivo = File.createTempFile("foto_", ".jpg", carpeta)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
}

// Acciones para elegir una imagen: abrir la galería o abrir la cámara
class SelectorImagen(
    val abrirGaleria: () -> Unit,
    val abrirCamara: () -> Unit
)

// Guía 13: GetContent() para la galería y TakePicture() para la cámara (con permiso y FileProvider).
// Si el usuario cancela, no se llama a onImagenElegida y la app no falla.
@Composable
fun rememberSelectorImagen(onImagenElegida: (Uri) -> Unit): SelectorImagen {
    val context = LocalContext.current
    val alElegir by rememberUpdatedState(onImagenElegida)

    // URI temporal donde la cámara guardará la foto
    var uriTemporal by rememberSaveable { mutableStateOf<Uri?>(null) }

    // Lanzador de la GALERÍA: devuelve el URI elegido (o null si cancela)
    val galeriaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) alElegir(uri)
    }

    // Lanzador de la CÁMARA: devuelve true si se tomó la foto, false si se canceló
    val camaraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { exito ->
        val uri = uriTemporal
        if (exito && uri != null) alElegir(uri)
    }

    fun abrirCamara() {
        try {
            val uri = crearUriParaFoto(context)
            uriTemporal = uri
            camaraLauncher.launch(uri)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No hay una app de cámara disponible", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "No se pudo abrir la cámara", Toast.LENGTH_SHORT).show()
        }
    }

    // Lanzador del PERMISO de cámara: si lo conceden, abre la cámara
    val permisoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) abrirCamara()
        else Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
    }

    return SelectorImagen(
        abrirGaleria = { galeriaLauncher.launch("image/*") },
        abrirCamara = {
            val tienePermiso = ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
            if (tienePermiso) abrirCamara() else permisoLauncher.launch(Manifest.permission.CAMERA)
        }
    )
}

// Diálogo para preguntar si la imagen viene de la cámara o de la galería
@Composable
fun DialogoOrigenImagen(
    selector: SelectorImagen,
    onCerrar: () -> Unit,
    titulo: String = "Agregar foto"
) {
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(titulo) },
        text = { Text("Elige de dónde obtener la imagen.") },
        confirmButton = {
            TextButton(onClick = { onCerrar(); selector.abrirCamara() }) { Text("Tomar foto") }
        },
        dismissButton = {
            TextButton(onClick = { onCerrar(); selector.abrirGaleria() }) { Text("Galería") }
        }
    )
}
