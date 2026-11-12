package com.example.modoguardian_grupomg.model

import java.util.Calendar
import java.util.Locale

// Turno de trabajo del guardia (14:00 - 22:00)
object Turno {
    const val HORA_INICIO = 14
    const val HORA_FIN = 22
    const val ETIQUETA = "14:00 - 22:00"

    private const val INICIO_MIN = HORA_INICIO * 60
    private const val FIN_MIN = HORA_FIN * 60

    private fun minutosDelDia(ahora: Long): Int {
        val c = Calendar.getInstance().apply { timeInMillis = ahora }
        return c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE)
    }

    fun enTurno(ahora: Long): Boolean = minutosDelDia(ahora) in INICIO_MIN until FIN_MIN

    // 0f antes del turno, 1f al terminar
    fun progreso(ahora: Long): Float =
        ((minutosDelDia(ahora) - INICIO_MIN).toFloat() / (FIN_MIN - INICIO_MIN)).coerceIn(0f, 1f)

    fun restante(ahora: Long): String {
        val m = minutosDelDia(ahora)
        if (m !in INICIO_MIN until FIN_MIN) return "Fuera de turno"
        val faltan = FIN_MIN - m
        return String.format(Locale.ROOT, "%dh %02dm restantes", faltan / 60, faltan % 60)
    }
}
