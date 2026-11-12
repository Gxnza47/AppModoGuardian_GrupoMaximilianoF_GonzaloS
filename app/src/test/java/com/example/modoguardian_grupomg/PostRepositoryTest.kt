package com.example.modoguardian_grupomg

import com.example.modoguardian_grupomg.data.model.Post
import com.example.modoguardian_grupomg.data.remote.ApiService
import com.example.modoguardian_grupomg.repository.PostRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response

// Guía 15: pruebas de PostRepository con Kotest + MockK (ApiService simulado, sin red real).
class PostRepositoryTest : FunSpec({

    val post = Post(userId = 1, id = 7, title = "titulo", body = "cuerpo")

    test("obtenerPosts devuelve lo que entrega la API") {
        val api = mockk<ApiService>()
        coEvery { api.obtenerPosts() } returns listOf(post)

        PostRepository(api).obtenerPosts() shouldBe listOf(post)
    }

    test("obtenerPosts propaga el error de red") {
        val api = mockk<ApiService>()
        coEvery { api.obtenerPosts() } throws IOException("sin red")

        shouldThrow<IOException> { PostRepository(api).obtenerPosts() }
    }

    test("crearPost envía el post con POST y devuelve la respuesta") {
        val api = mockk<ApiService>()
        coEvery { api.crearPost(post) } returns post.copy(id = 101)

        PostRepository(api).crearPost(post).id shouldBe 101
        coVerify(exactly = 1) { api.crearPost(post) }
    }

    test("actualizarPost usa el id del post en la URL (PUT)") {
        val api = mockk<ApiService>()
        coEvery { api.actualizarPost(7, post) } returns post

        PostRepository(api).actualizarPost(post) shouldBe post
        coVerify(exactly = 1) { api.actualizarPost(7, post) }
    }

    test("eliminarPost termina sin error cuando la API responde 200") {
        val api = mockk<ApiService>()
        coEvery { api.eliminarPost(7) } returns Response.success(Unit)

        PostRepository(api).eliminarPost(7)

        coVerify(exactly = 1) { api.eliminarPost(7) }
    }

    test("eliminarPost lanza IOException cuando la API responde con error") {
        val api = mockk<ApiService>()
        val cuerpo = "".toResponseBody("application/json".toMediaType())
        coEvery { api.eliminarPost(7) } returns Response.error(404, cuerpo)

        shouldThrow<IOException> { PostRepository(api).eliminarPost(7) }
    }
})
