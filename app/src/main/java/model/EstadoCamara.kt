package com.example.modoguardian_grupomg.model

data class EstadoCamara(
    val id: Int,
    val codigo: String,
    val nombre: String,
    val zona: String,
    val enLinea: Boolean,
    val pendientes: Int,
    val totalEventos: Int
)

// Una cámara figura sin conexión si tiene un evento «Cámara desconectada» vigente y sin resolver.
fun calcularEstadoCamaras(
    eventos: List<Evento>,
    resoluciones: Map<Int, ResolucionAlerta>,
    referencia: Long
): List<EstadoCamara> = (1..CatalogoZonas.total).map { id ->
    val propios = eventos.filter { it.camaraId == id }
    EstadoCamara(
        id = id,
        codigo = CatalogoZonas.codigo(id),
        nombre = CatalogoZonas.nombre(id),
        zona = CatalogoZonas.zona(id),
        enLinea = propios.none {
            it.tipo == TipoEvento.CAMARA_OFFLINE && it.esVigente(referencia) && it.id !in resoluciones
        },
        pendientes = propios.pendientes(resoluciones, referencia).size,
        totalEventos = propios.size
    )
}
