package com.example.modoguardian_grupomg.viewmodel

import androidx.lifecycle.ViewModel
import com.example.modoguardian_grupomg.model.AlertaUiState
import com.example.modoguardian_grupomg.model.ResolucionAlerta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Decisiones del guardia sobre las alertas y plazo de 2 minutos para confirmar las críticas.
class AlertaViewModel(
    private val reloj: () -> Long = System::currentTimeMillis
) : ViewModel() {

    companion object {
        const val PLAZO_SEGUNDOS = 120
    }

    private val _estado = MutableStateFlow(AlertaUiState())
    val estado: StateFlow<AlertaUiState> = _estado.asStateFlow()

    // El plazo empieza la primera vez que se abre la alerta y no se reinicia al volver a entrar.
    fun iniciarPlazo(id: Int) {
        _estado.update {
            if (id in it.plazos || id in it.resoluciones) it
            else it.copy(plazos = it.plazos + (id to reloj() + PLAZO_SEGUNDOS * 1000L))
        }
    }

    fun segundosRestantes(id: Int): Int {
        val limite = _estado.value.plazos[id] ?: return PLAZO_SEGUNDOS
        return (((limite - reloj()) + 999) / 1000).toInt().coerceIn(0, PLAZO_SEGUNDOS)
    }

    fun confirmar(id: Int) = resolver(id, ResolucionAlerta.CONFIRMADA)

    fun marcarFalsaAlarma(id: Int) = resolver(id, ResolucionAlerta.FALSA_ALARMA)

    fun escalar(id: Int) = resolver(id, ResolucionAlerta.ESCALADA)

    // La primera decisión es la definitiva
    private fun resolver(id: Int, resolucion: ResolucionAlerta) {
        _estado.update {
            if (id in it.resoluciones) it else it.copy(resoluciones = it.resoluciones + (id to resolucion))
        }
    }
}
