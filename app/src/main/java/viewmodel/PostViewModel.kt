package com.example.modoguardian_grupomg.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.modoguardian_grupomg.data.model.Post
import com.example.modoguardian_grupomg.model.ID_BASE_LOCAL
import com.example.modoguardian_grupomg.model.PostUiState
import com.example.modoguardian_grupomg.repository.PostRepository
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

// Guía 14: consume la API REST con Retrofit y expone el estado con StateFlow.
// Los posts se interpretan como eventos de seguridad (ver model/Evento.kt).
class PostViewModel(
    private val repository: PostRepository = PostRepository(),
    private val reloj: () -> Long = System::currentTimeMillis
) : ViewModel() {

    private val _uiState = MutableStateFlow(PostUiState())
    val uiState: StateFlow<PostUiState> = _uiState.asStateFlow()

    init {
        cargarPosts()
    }

    // GET /posts
    fun cargarPosts() {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val remotos = repository.obtenerPosts()
                _uiState.update { actual ->
                    // Las incidencias creadas en la app se conservan al recargar
                    val locales = actual.posts.filter { it.id in actual.creadasEn }
                    actual.copy(
                        posts = locales + remotos,
                        isLoading = false,
                        error = null,
                        referencia = reloj()
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = textoDeError(e)) }
            }
        }
    }

    // POST /posts: reporta una incidencia (la API responde siempre id 101, por eso se asigna uno propio)
    fun crearIncidencia(
        titulo: String,
        descripcion: String,
        camaraId: Int,
        evidencia: String? = null,
        onResultado: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val respuesta = repository.crearPost(
                    Post(userId = camaraId, id = 0, title = titulo.trim(), body = descripcion.trim())
                )
                _uiState.update { actual ->
                    val nuevoId = maxOf(ID_BASE_LOCAL, actual.posts.maxOfOrNull { it.id } ?: 0) + 1
                    actual.copy(
                        posts = listOf(respuesta.copy(id = nuevoId)) + actual.posts,
                        creadasEn = actual.creadasEn + (nuevoId to reloj()),
                        evidencias = if (evidencia != null) actual.evidencias + (nuevoId to evidencia) else actual.evidencias,
                        mensaje = "Incidencia reportada correctamente"
                    )
                }
                onResultado(true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(mensaje = "No se pudo enviar el reporte. ${textoDeError(e)}") }
                onResultado(false)
            }
        }
    }

    // PUT /posts/{id}: solo Admin. Las incidencias locales no existen en el servidor y se editan solo en la app.
    fun actualizarPost(id: Int, titulo: String, descripcion: String) {
        val actual = _uiState.value.posts.firstOrNull { it.id == id } ?: return
        val editado = actual.copy(title = titulo.trim(), body = descripcion.trim())
        viewModelScope.launch {
            try {
                val guardado = if (id in _uiState.value.creadasEn) editado else repository.actualizarPost(editado)
                _uiState.update { estado ->
                    estado.copy(
                        posts = estado.posts.map { if (it.id == id) guardado.copy(id = id) else it },
                        mensaje = "Evento actualizado"
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(mensaje = "No se pudo actualizar. ${textoDeError(e)}") }
            }
        }
    }

    // DELETE /posts/{id}: solo Admin
    fun eliminarPost(id: Int) {
        viewModelScope.launch {
            try {
                if (id !in _uiState.value.creadasEn) repository.eliminarPost(id)
                _uiState.update { estado ->
                    estado.copy(
                        posts = estado.posts.filterNot { it.id == id },
                        creadasEn = estado.creadasEn - id,
                        evidencias = estado.evidencias - id,
                        mensaje = "Evento eliminado"
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(mensaje = "No se pudo eliminar. ${textoDeError(e)}") }
            }
        }
    }

    // Guarda la foto (cámara o galería) como evidencia de un evento
    fun adjuntarEvidencia(id: Int, uri: String) {
        _uiState.update { it.copy(evidencias = it.evidencias + (id to uri), mensaje = "Evidencia adjuntada") }
    }

    fun consumirMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }

    private fun textoDeError(e: Exception): String = when (e) {
        is HttpException -> "El servidor respondió con error (${e.code()})."
        is IOException -> "Sin conexión. Revisa tu internet e intenta de nuevo."
        else -> "Ocurrió un error inesperado."
    }
}
