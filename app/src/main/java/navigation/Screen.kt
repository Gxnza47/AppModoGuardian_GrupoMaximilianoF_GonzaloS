package com.example.modoguardian_grupomg.navigation

// Sealed class para definir rutas tipo-safe en la navegación
sealed class Screen(val route: String) {

    // Acceso
    data object Login : Screen("login")
    data object Registro : Screen("registro")
    data object Resumen : Screen("resumen")

    // Pestañas de la barra inferior
    data object Inicio : Screen("inicio")
    data object Camaras : Screen("camaras")
    data object Eventos : Screen("eventos")
    data object Alertas : Screen("alertas")
    data object Perfil : Screen("perfil")

    // Pantallas con argumentos
    data object CamaraDetalle : Screen("camara/{id}") {
        fun crearRuta(id: Int) = "camara/$id"
    }

    data object EventoDetalle : Screen("evento/{id}") {
        fun crearRuta(id: Int) = "evento/$id"
    }

    // camara = 0 significa «sin cámara preseleccionada»
    data object Incidencia : Screen("incidencia?camara={camara}") {
        fun crearRuta(camara: Int = 0) = "incidencia?camara=$camara"
    }

    companion object {
        // Con get() se evalúan al usarse: si se inicializaran aquí, los objetos aún serían null
        // (Screen se está inicializando mientras sus subobjetos la heredan) y la app se cerraría.
        val pestanas: List<Screen>
            get() = listOf(Inicio, Camaras, Eventos, Alertas, Perfil)
        val rutasPublicas: Set<String>
            get() = setOf(Login.route, Registro.route, Resumen.route)
    }
}
