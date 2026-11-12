package com.example.modoguardian_grupomg

import com.example.modoguardian_grupomg.data.model.Post
import com.example.modoguardian_grupomg.repository.PostRepository
import com.example.modoguardian_grupomg.viewmodel.PostViewModel
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

// Guía 15: pruebas de PostViewModel con Kotest + MockK (el repositorio se simula).
@OptIn(ExperimentalCoroutinesApi::class)
class PostViewModelTest : FunSpec({

    val posts = listOf(
        Post(userId = 1, id = 1, title = "uno", body = "cuerpo uno"),
        Post(userId = 2, id = 2, title = "dos", body = "cuerpo dos")
    )

    beforeTest { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterTest { Dispatchers.resetMain() }

    test("al iniciar carga los posts y deja de cargar") {
        val repo = mockk<PostRepository>()
        coEvery { repo.obtenerPosts() } returns posts

        val vm = PostViewModel(repo, reloj = { 5_000L })

        vm.uiState.value.posts shouldBe posts
        vm.uiState.value.isLoading shouldBe false
        vm.uiState.value.error shouldBe null
        vm.uiState.value.referencia shouldBe 5_000L
    }

    test("sin internet muestra un error de conexión y la lista queda vacía") {
        val repo = mockk<PostRepository>()
        coEvery { repo.obtenerPosts() } throws IOException("sin red")

        val vm = PostViewModel(repo)

        vm.uiState.value.posts shouldBe emptyList()
        vm.uiState.value.isLoading shouldBe false
        vm.uiState.value.error!! shouldContain "Sin conexión"
    }

    test("si el servidor responde con error HTTP lo informa") {
        val repo = mockk<PostRepository>()
        val cuerpo = "".toResponseBody("application/json".toMediaType())
        coEvery { repo.obtenerPosts() } throws HttpException(Response.error<List<Post>>(500, cuerpo))

        val vm = PostViewModel(repo)

        vm.uiState.value.error!! shouldContain "500"
    }

    test("reintentar después de un fallo carga los datos") {
        val repo = mockk<PostRepository>()
        coEvery { repo.obtenerPosts() } throws IOException("sin red") andThen posts

        val vm = PostViewModel(repo)
        (vm.uiState.value.error != null) shouldBe true

        vm.cargarPosts()

        vm.uiState.value.error shouldBe null
        vm.uiState.value.posts shouldBe posts
    }

    test("crearIncidencia agrega la incidencia al inicio con un id propio mayor a 100") {
        val repo = mockk<PostRepository>()
        coEvery { repo.obtenerPosts() } returns posts
        // La API real responde siempre con id 101
        coEvery { repo.crearPost(any()) } answers { firstArg<Post>().copy(id = 101) }
        val vm = PostViewModel(repo, reloj = { 9_000L })
        var resultado: Boolean? = null

        vm.crearIncidencia("Puerta abierta", "Encontrada sin seguro", camaraId = 3, evidencia = "content://foto") {
            resultado = it
        }

        resultado shouldBe true
        val nueva = vm.uiState.value.posts.first()
        nueva.id shouldBe 101
        nueva.userId shouldBe 3
        nueva.title shouldBe "Puerta abierta"
        vm.uiState.value.creadasEn[101] shouldBe 9_000L
        vm.uiState.value.evidencias[101] shouldBe "content://foto"
        vm.uiState.value.mensaje!! shouldContain "Incidencia reportada"
    }

    test("si falla el envío informa el error y no agrega nada") {
        val repo = mockk<PostRepository>()
        coEvery { repo.obtenerPosts() } returns posts
        coEvery { repo.crearPost(any()) } throws IOException("sin red")
        val vm = PostViewModel(repo)
        var resultado: Boolean? = null

        vm.crearIncidencia("Titulo", "Descripción", camaraId = 1) { resultado = it }

        resultado shouldBe false
        vm.uiState.value.posts shouldBe posts
        vm.uiState.value.mensaje!! shouldContain "No se pudo enviar"
    }

    test("actualizarPost de un evento remoto llama a la API y reemplaza el post") {
        val repo = mockk<PostRepository>()
        coEvery { repo.obtenerPosts() } returns posts
        coEvery { repo.actualizarPost(any()) } answers { firstArg() }
        val vm = PostViewModel(repo)

        vm.actualizarPost(2, "  nuevo título ", "nuevo cuerpo")

        coVerify(exactly = 1) { repo.actualizarPost(match { it.id == 2 && it.title == "nuevo título" }) }
        vm.uiState.value.posts.first { it.id == 2 }.body shouldBe "nuevo cuerpo"
        vm.uiState.value.mensaje shouldBe "Evento actualizado"
    }

    test("eliminarPost de un evento remoto llama a DELETE y lo quita de la lista") {
        val repo = mockk<PostRepository>()
        coEvery { repo.obtenerPosts() } returns posts
        coEvery { repo.eliminarPost(1) } just Runs
        val vm = PostViewModel(repo)

        vm.eliminarPost(1)

        coVerify(exactly = 1) { repo.eliminarPost(1) }
        vm.uiState.value.posts.map { it.id } shouldBe listOf(2)
    }

    test("eliminar o editar una incidencia local no llama a la API") {
        val repo = mockk<PostRepository>()
        coEvery { repo.obtenerPosts() } returns posts
        coEvery { repo.crearPost(any()) } answers { firstArg() }
        val vm = PostViewModel(repo)
        vm.crearIncidencia("Local", "Solo en la app", camaraId = 1)

        vm.actualizarPost(101, "Local editada", "Sigue local")
        vm.eliminarPost(101)

        coVerify(exactly = 0) { repo.actualizarPost(any()) }
        coVerify(exactly = 0) { repo.eliminarPost(any()) }
        vm.uiState.value.posts.none { it.id == 101 } shouldBe true
    }

    test("adjuntarEvidencia guarda la foto asociada al evento") {
        val repo = mockk<PostRepository>()
        coEvery { repo.obtenerPosts() } returns posts
        val vm = PostViewModel(repo)

        vm.adjuntarEvidencia(2, "content://foto/2")

        vm.uiState.value.evidencias[2] shouldBe "content://foto/2"
        vm.consumirMensaje()
        vm.uiState.value.mensaje shouldBe null
    }
})
