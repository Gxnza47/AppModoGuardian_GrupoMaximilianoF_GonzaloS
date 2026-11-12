package com.example.modoguardian_grupomg

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import com.example.modoguardian_grupomg.data.model.Post
import com.example.modoguardian_grupomg.model.PostUiState
import com.example.modoguardian_grupomg.ui.screens.PostContent
import com.example.modoguardian_grupomg.ui.theme.ModoGuardian_GrupoMGTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

// Guía 15 (Parte 5): prueba de UI de PostScreen con Compose UI Test.
// Se prueba PostContent (sin ViewModel) para controlar el estado desde la prueba.
class PostScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val referencia = System.currentTimeMillis()

    // id 1 = «Movimiento detectado», id 3 = «Acceso autorizado»
    private val estadoConDatos = PostUiState(
        posts = listOf(
            Post(userId = 1, id = 1, title = "sensor activado", body = "detalle uno"),
            Post(userId = 2, id = 3, title = "credencial valida", body = "detalle tres")
        ),
        referencia = referencia
    )

    private fun mostrar(
        estado: PostUiState,
        puedeEditar: Boolean = false,
        onReintentar: () -> Unit = {},
        onAbrir: (Int) -> Unit = {}
    ) {
        composeTestRule.setContent {
            ModoGuardian_GrupoMGTheme {
                PostContent(
                    state = estado,
                    resoluciones = emptyMap(),
                    puedeEditar = puedeEditar,
                    onReintentar = onReintentar,
                    onAbrirEvento = onAbrir,
                    onEditar = { _, _, _ -> },
                    onEliminar = {}
                )
            }
        }
    }

    // La lista es perezosa: se desplaza hasta el evento para que esté compuesto en pantalla
    private fun irAlEvento(texto: String) {
        composeTestRule.onNodeWithTag("lista_eventos").performScrollToNode(hasText(texto))
    }

    @Test
    fun muestraLosEventosDerivadosDeLosPosts() {
        mostrar(estadoConDatos)

        composeTestRule.onNodeWithText("Historial").assertIsDisplayed()
        irAlEvento("Movimiento detectado")
        composeTestRule.onNodeWithText("Movimiento detectado").assertIsDisplayed()
        irAlEvento("Acceso autorizado")
        composeTestRule.onNodeWithText("Acceso autorizado").assertIsDisplayed()
    }

    @Test
    fun conErrorMuestraMensajeYBotonReintentar() {
        var reintentos = 0
        mostrar(
            PostUiState(error = "Sin conexión. Revisa tu internet e intenta de nuevo."),
            onReintentar = { reintentos++ }
        )

        irAlEvento("Reintentar")
        composeTestRule.onNodeWithText("Sin conexión. Revisa tu internet e intenta de nuevo.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reintentar").performClick()

        assertEquals(1, reintentos)
    }

    @Test
    fun tocarUnEventoLoAbre() {
        var abierto = -1
        mostrar(estadoConDatos, onAbrir = { abierto = it })

        irAlEvento("Acceso autorizado")
        composeTestRule.onNodeWithText("Acceso autorizado").performClick()

        assertEquals(3, abierto)
    }

    @Test
    fun laBusquedaFiltraLosEventos() {
        mostrar(estadoConDatos)

        composeTestRule.onNodeWithText("Buscar por cámara, zona o tipo…").performTextInput("acceso")

        irAlEvento("Acceso autorizado")
        composeTestRule.onNodeWithText("Acceso autorizado").assertIsDisplayed()
        composeTestRule.onNodeWithText("Movimiento detectado").assertDoesNotExist()
    }

    @Test
    fun soloElAdminVeLasOpcionesDeEdicion() {
        mostrar(estadoConDatos, puedeEditar = false)

        irAlEvento("Acceso autorizado")
        composeTestRule.onNodeWithContentDescription("Opciones del evento").assertDoesNotExist()
    }

    @Test
    fun elAdminPuedeAbrirElMenuDeEdicion() {
        mostrar(estadoConDatos, puedeEditar = true)

        irAlEvento("Movimiento detectado")
        composeTestRule.onAllNodesWithContentDescription("Opciones del evento")[0].performClick()

        composeTestRule.onNodeWithText("Editar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Eliminar").assertIsDisplayed()
    }
}
