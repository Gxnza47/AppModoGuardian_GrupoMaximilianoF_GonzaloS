package com.example.modoguardian_grupomg.model

// Decisiones tomadas sobre las alertas durante la sesión (se guardan en memoria)
data class AlertaUiState(
    val resoluciones: Map<Int, ResolucionAlerta> = emptyMap(),
    // id de alerta -> instante límite para confirmarla antes del auto-escalado
    val plazos: Map<Int, Long> = emptyMap()
)
