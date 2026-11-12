package com.example.modoguardian_grupomg.model

import com.example.modoguardian_grupomg.data.model.Post
import java.util.Locale

enum class Severidad(val etiqueta: String) {
    CRITICA("CRÍTICA"),
    ALERTA("ALERTA"),
    EVENTO("EVENTO")
}

enum class TipoEvento(val etiqueta: String, val categoria: String, val severidad: Severidad) {
    INTRUSION("Movimiento no autorizado", "Intrusión perimetral", Severidad.CRITICA),
    MOVIMIENTO("Movimiento detectado", "Detección de movimiento", Severidad.ALERTA),
    ACCESO("Acceso autorizado", "Control de acceso", Severidad.EVENTO),
    CAMARA_OFFLINE("Cámara desconectada", "Falla de equipo", Severidad.CRITICA),
    RONDA("Ronda completada", "Ronda de guardia", Severidad.EVENTO),
    INCIDENCIA("Incidencia reportada", "Reporte manual", Severidad.ALERTA)
}

enum class ResolucionAlerta(val etiqueta: String) {
    CONFIRMADA("Confirmada"),
    FALSA_ALARMA("Falsa alarma"),
    ESCALADA("Escalada")
}

// JSONPlaceholder devuelve posts con userId 1..10: cada userId se interpreta como una cámara.
object CatalogoZonas {
    private val ubicaciones = listOf(
        "Entrada Principal" to "Zona Sur",
        "Perímetro Norte" to "Zona Norte",
        "Bodega A" to "Zona Sur",
        "Estacionamiento" to "Zona Sur",
        "Portón Vehicular" to "Zona Este",
        "Almacén Central" to "Zona Centro",
        "Perímetro Sur" to "Zona Sur",
        "Perímetro Este" to "Zona Este",
        "Sala de Servidores" to "Zona Centro",
        "Azotea" to "Zona Norte"
    )

    val total: Int get() = ubicaciones.size

    // Devuelve siempre un id válido entre 1 y total
    fun normalizar(userId: Int): Int = Math.floorMod(userId - 1, total) + 1

    fun nombre(camaraId: Int): String = ubicaciones[normalizar(camaraId) - 1].first
    fun zona(camaraId: Int): String = ubicaciones[normalizar(camaraId) - 1].second
    fun codigo(camaraId: Int): String = String.format(Locale.ROOT, "CAM-%02d", normalizar(camaraId))
}

// Los posts de la API no traen fecha: se reparte una cada 11 minutos hacia atrás desde la última sincronización.
const val MINUTOS_ENTRE_EVENTOS = 11
// Los posts con id superior a este valor fueron creados en la app (la API no los guarda realmente).
const val ID_BASE_LOCAL = 100
// Una alerta sin resolver deja de considerarse «vigente» pasadas 3 horas.
const val VENTANA_VIGENTE_MS = 3L * 60 * 60 * 1000

data class Evento(
    val id: Int,
    val tipo: TipoEvento,
    val camaraId: Int,
    val detalle: String,
    val observaciones: String,
    val instante: Long,
    val confianzaIA: Int?,
    val duracionSegundos: Int,
    val esLocal: Boolean = false
) {
    val severidad: Severidad get() = tipo.severidad
    val codigoCamara: String get() = CatalogoZonas.codigo(camaraId)
    val nombreCamara: String get() = CatalogoZonas.nombre(camaraId)
    val zona: String get() = CatalogoZonas.zona(camaraId)

    // Las alertas (críticas o normales) requieren una decisión del guardia; los eventos solo se registran.
    val esAccionable: Boolean get() = severidad != Severidad.EVENTO

    fun esVigente(referencia: Long): Boolean = referencia - instante <= VENTANA_VIGENTE_MS
}

fun tipoPorId(id: Int): TipoEvento = when (Math.floorMod(id, 10)) {
    0 -> TipoEvento.INTRUSION
    1, 2 -> TipoEvento.MOVIMIENTO
    3, 4, 5 -> TipoEvento.ACCESO
    6 -> TipoEvento.CAMARA_OFFLINE
    else -> TipoEvento.RONDA
}

// Convierte un post de la API en un evento de seguridad.
// creadoEn != null indica una incidencia reportada desde la app.
fun Post.toEvento(referencia: Long, creadoEn: Long? = null): Evento {
    val local = creadoEn != null
    val tipo = if (local) TipoEvento.INCIDENCIA else tipoPorId(id)
    val instante = creadoEn ?: (referencia - id.toLong() * MINUTOS_ENTRE_EVENTOS * 60_000L)
    val confianza = if (tipo == TipoEvento.INTRUSION || tipo == TipoEvento.MOVIMIENTO) 70 + (id * 7) % 30 else null
    return Evento(
        id = id,
        tipo = tipo,
        camaraId = CatalogoZonas.normalizar(userId),
        detalle = title.trim().replaceFirstChar { it.uppercase() },
        observaciones = body.replace("\n", " ").trim(),
        instante = instante,
        confianzaIA = confianza,
        duracionSegundos = 5 + Math.floorMod(id, 20),
        esLocal = local
    )
}

// Alertas aún sin resolver y recientes (se usan para la insignia de la campana y la pestaña Alertas).
fun List<Evento>.pendientes(resoluciones: Map<Int, ResolucionAlerta>, referencia: Long): List<Evento> =
    filter { it.esAccionable && it.esVigente(referencia) && it.id !in resoluciones }
