package com.example.modoguardian_grupomg.data.model

// Modelo que devuelve https://jsonplaceholder.typicode.com/posts
// La app lo interpreta como un evento de seguridad (ver model/Evento.kt).
data class Post(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String
)
