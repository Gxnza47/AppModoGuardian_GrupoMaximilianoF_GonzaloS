package com.example.modoguardian_grupomg.ui.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Utilidades de fecha y hora. Se usa Calendar/SimpleDateFormat porque minSdk = 24 (java.time exige API 26).
object Formato {

    private fun formatear(patron: String, ms: Long): String =
        SimpleDateFormat(patron, Locale.ROOT).format(Date(ms))

    fun hora(ms: Long): String = formatear("HH:mm", ms)

    fun horaCompleta(ms: Long): String = formatear("HH:mm:ss", ms)

    fun fechaHoraCompleta(ms: Long): String = formatear("dd-MM-yyyy HH:mm:ss", ms)

    fun cronometro(segundos: Int): String =
        String.format(Locale.ROOT, "%02d:%02d", segundos / 60, segundos % 60)

    private fun calendario(ms: Long): Calendar = Calendar.getInstance().apply { timeInMillis = ms }

    fun mismoDia(a: Long, b: Long): Boolean {
        val x = calendario(a)
        val y = calendario(b)
        return x.get(Calendar.YEAR) == y.get(Calendar.YEAR) && x.get(Calendar.DAY_OF_YEAR) == y.get(Calendar.DAY_OF_YEAR)
    }

    fun esAyer(ms: Long, referencia: Long): Boolean {
        val ayer = calendario(referencia).apply { add(Calendar.DAY_OF_YEAR, -1) }.timeInMillis
        return mismoDia(ms, ayer)
    }

    // Inicio de la hora a la que pertenece el instante (sirve para agrupar eventos)
    fun inicioDeHora(ms: Long): Long = calendario(ms).apply {
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    // Ej.: "14:00 - 15:00", "Ayer · 23:00 - 00:00"
    fun etiquetaBloque(inicioHora: Long, referencia: Long): String {
        val h = calendario(inicioHora).get(Calendar.HOUR_OF_DAY)
        val rango = String.format(Locale.ROOT, "%02d:00 - %02d:00", h, (h + 1) % 24)
        return when {
            mismoDia(inicioHora, referencia) -> rango
            esAyer(inicioHora, referencia) -> "Ayer · $rango"
            else -> formatear("dd-MM", inicioHora) + " · " + rango
        }
    }

    fun hace(ms: Long, ahora: Long): String {
        val minutos = (ahora - ms) / 60_000
        return when {
            minutos < 1 -> "Ahora"
            minutos < 60 -> "Hace $minutos min"
            minutos < 24 * 60 -> "Hace ${minutos / 60} h"
            else -> "Hace ${minutos / (24 * 60)} d"
        }
    }
}
