package com.example.modoguardian_grupomg.model

import com.example.modoguardian_grupomg.data.model.Post

// Estado de la pantalla que consume la API (Guía 14)
data class PostUiState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    // Error de la carga inicial (se muestra con botón «Reintentar»)
    val error: String? = null,
    // Mensaje puntual de una operación (crear, editar, eliminar); se muestra una vez
    val mensaje: String? = null,
    // Momento de la última sincronización correcta
    val referencia: Long = 0L,
    // id de incidencia creada en la app -> instante de creación
    val creadasEn: Map<Int, Long> = emptyMap(),
    // id de evento -> URI (texto) de la foto adjunta como evidencia
    val evidencias: Map<Int, String> = emptyMap()
) {
    fun aEventos(): List<Evento> =
        posts.map { it.toEvento(referencia, creadasEn[it.id]) }.sortedByDescending { it.instante }
}
