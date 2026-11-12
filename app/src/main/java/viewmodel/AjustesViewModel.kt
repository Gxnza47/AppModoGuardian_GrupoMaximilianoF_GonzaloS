package com.example.modoguardian_grupomg.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.modoguardian_grupomg.data.EstadoDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Preferencias de la app guardadas en DataStore (se conservan al cerrar la app)
class AjustesViewModel(application: Application) : AndroidViewModel(application) {

    private val store = EstadoDataStore(application)

    val modoOscuro: StateFlow<Boolean> = store.obtenerBoolean(CLAVE_OSCURO, true)
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val notificacionesPush: StateFlow<Boolean> = store.obtenerBoolean(CLAVE_PUSH, true)
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val alertasCriticas: StateFlow<Boolean> = store.obtenerBoolean(CLAVE_CRITICAS, true)
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    fun cambiarModoOscuro(valor: Boolean) = guardar(CLAVE_OSCURO, valor)
    fun cambiarNotificacionesPush(valor: Boolean) = guardar(CLAVE_PUSH, valor)
    fun cambiarAlertasCriticas(valor: Boolean) = guardar(CLAVE_CRITICAS, valor)

    private fun guardar(clave: String, valor: Boolean) {
        viewModelScope.launch { store.guardarBoolean(clave, valor) }
    }

    private companion object {
        const val CLAVE_OSCURO = "modo_oscuro"
        const val CLAVE_PUSH = "notificaciones_push"
        const val CLAVE_CRITICAS = "alertas_criticas"
    }
}
