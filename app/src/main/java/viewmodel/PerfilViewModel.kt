package com.example.modoguardian_grupomg.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ViewModel del perfil: guarda la imagen elegida (desde galería o cámara).
// La foto se copia al almacenamiento interno de la app, así sigue disponible al reabrirla.
class PerfilViewModel(application: Application) : AndroidViewModel(application) {

    // Estado interno mutable: URI de la imagen. null = todavía no hay imagen
    private val _imagenUri = MutableStateFlow<Uri?>(null)

    // Estado expuesto a la UI (solo lectura)
    val imagenUri: StateFlow<Uri?> = _imagenUri

    // Cada usuario tiene su propia foto (se identifica por el correo de la sesión)
    private var clave: String? = null

    private fun carpeta(): File = File(getApplication<Application>().filesDir, "avatares").apply { mkdirs() }

    private fun archivosDe(clave: String): List<File> =
        carpeta().listFiles { f -> f.name.startsWith("avatar_${clave}_") }?.toList().orEmpty()

    fun cargarParaUsuario(email: String) {
        val nueva = email.filter { it.isLetterOrDigit() }
        if (nueva == clave) return
        clave = nueva
        _imagenUri.value = archivosDe(nueva).maxByOrNull { it.lastModified() }?.let { Uri.fromFile(it) }
    }

    // Actualiza la imagen elegida desde la galería.
    // Si el usuario cancela, el selector devuelve null y se mantiene la imagen anterior.
    fun actualizarImagenDesdeGaleria(uri: Uri?) {
        actualizarImagen(uri)
    }

    // Actualiza la imagen capturada con la cámara
    fun actualizarImagenDesdeCamara(uri: Uri?) {
        actualizarImagen(uri)
    }

    private fun actualizarImagen(uri: Uri?) {
        if (uri == null) return
        val usuario = clave ?: run { _imagenUri.value = uri; return }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val destino = File(carpeta(), "avatar_${usuario}_${System.currentTimeMillis()}.jpg")
                getApplication<Application>().contentResolver.openInputStream(uri)?.use { entrada ->
                    destino.outputStream().use { salida -> entrada.copyTo(salida) }
                } ?: error("No se pudo abrir la imagen")
                archivosDe(usuario).filter { it != destino }.forEach { it.delete() }
                _imagenUri.value = Uri.fromFile(destino)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Si no se puede copiar, se usa la imagen original mientras dure la sesión
                _imagenUri.value = uri
            }
        }
    }
}
