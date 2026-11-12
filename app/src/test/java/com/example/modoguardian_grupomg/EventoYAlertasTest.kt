package com.example.modoguardian_grupomg

import com.example.modoguardian_grupomg.data.model.Post
import com.example.modoguardian_grupomg.model.CatalogoZonas
import com.example.modoguardian_grupomg.model.PostUiState
import com.example.modoguardian_grupomg.model.ResolucionAlerta
import com.example.modoguardian_grupomg.model.Severidad
import com.example.modoguardian_grupomg.model.TipoEvento
import com.example.modoguardian_grupomg.model.calcularEstadoCamaras
import com.example.modoguardian_grupomg.model.pendientes
import com.example.modoguardian_grupomg.model.toEvento
import com.example.modoguardian_grupomg.viewmodel.AlertaViewModel
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

// Mapeo de posts de la API a eventos de seguridad, cálculo de cámaras y plazo de las alertas
class EventoYAlertasTest : FunSpec({

    val referencia = 100_000_000L
    fun post(id: Int, userId: Int = 1) = Post(userId, id, "titulo $id", "cuerpo $id")

    test("el tipo de evento se deriva del id del post") {
        post(10).toEvento(referencia).tipo shouldBe TipoEvento.INTRUSION
        post(11).toEvento(referencia).tipo shouldBe TipoEvento.MOVIMIENTO
        post(13).toEvento(referencia).tipo shouldBe TipoEvento.ACCESO
        post(16).toEvento(referencia).tipo shouldBe TipoEvento.CAMARA_OFFLINE
        post(17).toEvento(referencia).tipo shouldBe TipoEvento.RONDA
    }

    test("el userId se convierte en cámara y zona, y siempre queda en rango") {
        val e = post(1, userId = 2).toEvento(referencia)
        e.codigoCamara shouldBe "CAM-02"
        e.nombreCamara shouldBe CatalogoZonas.nombre(2)
        post(1, userId = 11).toEvento(referencia).camaraId shouldBe 1
        post(1, userId = 0).toEvento(referencia).camaraId shouldBe 10
    }

    test("las incidencias creadas en la app son alertas con la hora de creación") {
        val e = post(101).toEvento(referencia, creadoEn = referencia - 5_000L)
        e.tipo shouldBe TipoEvento.INCIDENCIA
        e.severidad shouldBe Severidad.ALERTA
        e.esLocal shouldBe true
        e.instante shouldBe referencia - 5_000L
    }

    test("la IA solo reporta confianza en intrusiones y movimientos") {
        (post(10).toEvento(referencia).confianzaIA!! in 70..99) shouldBe true
        post(13).toEvento(referencia).confianzaIA shouldBe null
    }

    test("los eventos quedan ordenados del más reciente al más antiguo") {
        val estado = PostUiState(posts = listOf(post(5), post(1), post(3)), referencia = referencia)
        estado.aEventos().map { it.id } shouldBe listOf(1, 3, 5)
    }

    test("pendientes excluye eventos normales, antiguos y ya resueltos") {
        val eventos = listOf(post(10), post(11), post(13), post(90)).map { it.toEvento(referencia) }
        // id 10: crítica reciente (110 min); id 11: alerta reciente; id 13: evento normal; id 90: demasiado antigua
        eventos.pendientes(emptyMap(), referencia).map { it.id } shouldBe listOf(10, 11)
        eventos.pendientes(mapOf(10 to ResolucionAlerta.CONFIRMADA), referencia).map { it.id } shouldBe listOf(11)
    }

    test("una cámara figura sin conexión mientras tenga un evento de desconexión sin resolver") {
        val eventos = listOf(post(6, userId = 1)).map { it.toEvento(referencia) }

        calcularEstadoCamaras(eventos, emptyMap(), referencia).first { it.id == 1 }.enLinea shouldBe false
        calcularEstadoCamaras(eventos, mapOf(6 to ResolucionAlerta.CONFIRMADA), referencia)
            .first { it.id == 1 }.enLinea shouldBe true
        calcularEstadoCamaras(eventos, emptyMap(), referencia).size shouldBe CatalogoZonas.total
    }

    test("AlertaViewModel: el plazo de 2 minutos corre desde que se abre y no se reinicia") {
        var ahora = 1_000_000L
        val vm = AlertaViewModel(reloj = { ahora })

        vm.segundosRestantes(10) shouldBe AlertaViewModel.PLAZO_SEGUNDOS
        vm.iniciarPlazo(10)
        ahora += 30_000
        vm.segundosRestantes(10) shouldBe 90
        vm.iniciarPlazo(10)
        vm.segundosRestantes(10) shouldBe 90
        ahora += 200_000
        vm.segundosRestantes(10) shouldBe 0
    }

    test("AlertaViewModel: la primera decisión es la definitiva") {
        val vm = AlertaViewModel()

        vm.confirmar(10)
        vm.marcarFalsaAlarma(10)
        vm.escalar(10)

        vm.estado.value.resoluciones[10] shouldBe ResolucionAlerta.CONFIRMADA
    }
})
