package com.example.modoguardian_grupomg.model

// Permisos centralizados: también se comprueban antes de mostrar un destino.
//  - Operador:   Inicio, Cámaras, Alertas y Perfil.
//  - Supervisor: lo anterior + Eventos (historial completo del turno).
//  - Admin:      lo anterior + editar y eliminar eventos (PUT y DELETE).
enum class Rol(val etiqueta: String) {
    Admin("Admin"), Supervisor("Supervisor"), Operador("Operador");

    // Las rutas coinciden con las de navigation/Screen.kt
    fun puedeAcceder(ruta: String): Boolean = when (ruta) {
        "eventos" -> this != Operador
        else -> true
    }

    val puedeEditarEventos: Boolean get() = this == Admin
}
