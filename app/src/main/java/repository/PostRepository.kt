package com.example.modoguardian_grupomg.repository

import com.example.modoguardian_grupomg.data.model.Post
import com.example.modoguardian_grupomg.data.remote.ApiService
import com.example.modoguardian_grupomg.data.remote.RetrofitInstance
import java.io.IOException

// Única puerta de acceso a la API: el ViewModel no conoce Retrofit.
class PostRepository(private val api: ApiService = RetrofitInstance.api) {

    suspend fun obtenerPosts(): List<Post> = api.obtenerPosts()

    suspend fun crearPost(post: Post): Post = api.crearPost(post)

    suspend fun actualizarPost(post: Post): Post = api.actualizarPost(post.id, post)

    suspend fun eliminarPost(id: Int) {
        val respuesta = api.eliminarPost(id)
        if (!respuesta.isSuccessful) throw IOException("Error HTTP ${respuesta.code()}")
    }
}
