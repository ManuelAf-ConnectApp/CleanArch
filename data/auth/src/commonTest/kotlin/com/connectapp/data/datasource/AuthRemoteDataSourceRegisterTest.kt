package com.connectapp.data.datasource

import com.connectapp.core.network.NetworkInterceptor
import com.connectapp.data.mapper.toAuthError
import com.connectapp.data.model.RegisterRequestDto
import com.connectapp.core.network.defaultJson
import com.connectapp.core.network.configureDefaultClient
import com.connectapp.data.service.AuthServiceImpl
import com.connectapp.domain.model.AuthError
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.client.request.HttpRequestData
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.decodeFromString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

private const val TEST_BASE_URL = "https://auth.connectapp.test"

/**
 * Pass-through [NetworkInterceptor] fake that only does the try/catch + [toAuthError] mapping
 * (no headers, no local token lookup). [AuthInterceptor][com.connectapp.data.interceptor.AuthInterceptor]
 * itself is not under test here — this keeps the test focused on the actual contract being
 * verified per contracts/http-auth-contract.md: [AuthRemoteDataSourceImpl] + [AuthServiceImpl] +
 * `AuthErrorMapper` turning a real HTTP response (simulated via [MockEngine]) into the right
 * `Result`.
 */
private class RegisterFakeNetworkInterceptor : NetworkInterceptor {
    override fun getHeaders(): Map<String, String> = emptyMap()

    override suspend fun <T> execute(call: suspend () -> T): Result<T> {
        return try {
            Result.success(call())
        } catch (e: Exception) {
            Result.failure(e.toAuthError())
        }
    }
}

private fun buildRegisterSut(engine: MockEngine): AuthRemoteDataSourceImpl {
    val httpClient = HttpClient(engine) {
        // Reuse the production content-negotiation/expectSuccess config instead of hand-rolling
        // a separate one that could silently diverge from what AuthServiceImpl actually runs
        // against in production (see HttpClientFactory.kt).
        configureDefaultClient(baseUrl = TEST_BASE_URL)
    }
    return AuthRemoteDataSourceImpl(
        networkInterceptor = RegisterFakeNetworkInterceptor(),
        authService = AuthServiceImpl(httpClient)
    )
}

private fun HttpRequestData.registerBodyAsText(): String =
    (body as OutgoingContent.ByteArrayContent).bytes().decodeToString()

class AuthRemoteDataSourceRegisterTest {

    // contracts/http-auth-contract.md POST /auth/register, Response 200/201 -> success.
    // AuthRemoteDataSourceImpl.register() discards the UserDto response body and returns true,
    // but the body still has to be valid JSON matching UserDto's shape or deserialization would
    // throw, so the mock response below is a realistic created-user payload.
    @Test
    fun `register returns success true on a 200 response with a valid UserDto body`() = runTest {
        val responseJson = """
            {
              "id": "usr-9931",
              "firstName": "Ada",
              "lastName": "Lovelace",
              "email": "ada.lovelace@example.com",
              "phone": "+1-555-0142",
              "createdAt": "2024-02-10T09:00:00Z",
              "updatedAt": "2024-02-10T09:00:00Z"
            }
        """.trimIndent()

        val engine = MockEngine { request ->
            respond(
                content = responseJson,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        val sut = buildRegisterSut(engine)

        val result = sut.register(
            name = "Ada Lovelace",
            email = "ada.lovelace@example.com",
            password = "S3cr3t!2024"
        )

        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrNull())

        // Contract sanity check: the request actually sent matches contracts/http-auth-contract.md
        // (POST /auth/register with { name, email, password }), so a passing test above isn't accidental.
        val sentRequest = engine.requestHistory.single()
        assertEquals(HttpMethod.Post, sentRequest.method)
        assertEquals("/auth/register", sentRequest.url.encodedPath)
        assertEquals(
            RegisterRequestDto(
                name = "Ada Lovelace",
                email = "ada.lovelace@example.com",
                password = "S3cr3t!2024"
            ),
            defaultJson.decodeFromString<RegisterRequestDto>(sentRequest.registerBodyAsText())
        )
    }

    // contracts/http-auth-contract.md: Response 201 also counts as success.
    @Test
    fun `register returns success true on a 201 response with a valid UserDto body`() = runTest {
        val responseJson = """
            {
              "id": "usr-9932",
              "firstName": "Ada",
              "lastName": "Lovelace",
              "email": "ada.lovelace@example.com",
              "phone": "+1-555-0142",
              "createdAt": "2024-02-10T09:00:00Z",
              "updatedAt": "2024-02-10T09:00:00Z"
            }
        """.trimIndent()

        val engine = MockEngine {
            respond(
                content = responseJson,
                status = HttpStatusCode.Created,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        val sut = buildRegisterSut(engine)

        val result = sut.register(
            name = "Ada Lovelace",
            email = "ada.lovelace@example.com",
            password = "S3cr3t!2024"
        )

        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrNull())
    }

    // contracts/http-auth-contract.md: Response 409 -> AuthError.EmailAlreadyRegistered.
    @Test
    fun `register maps a 409 response to AuthError EmailAlreadyRegistered`() = runTest {
        val engine = MockEngine { respondError(HttpStatusCode.Conflict) }
        val sut = buildRegisterSut(engine)

        val result = sut.register(
            name = "Ada Lovelace",
            email = "ada.lovelace@example.com",
            password = "S3cr3t!2024"
        )

        assertTrue(result.isFailure)
        assertEquals(AuthError.EmailAlreadyRegistered, result.exceptionOrNull())
    }

    // contracts/http-auth-contract.md: Response 5xx -> AuthError.Server(code).
    @Test
    fun `register maps a 500 response to AuthError Server with the response code`() = runTest {
        val engine = MockEngine { respondError(HttpStatusCode.InternalServerError) }
        val sut = buildRegisterSut(engine)

        val result = sut.register(
            name = "Ada Lovelace",
            email = "ada.lovelace@example.com",
            password = "S3cr3t!2024"
        )

        assertTrue(result.isFailure)
        assertEquals(AuthError.Server(500), result.exceptionOrNull())
    }

    // contracts/http-auth-contract.md: fallo de conexión/timeout -> AuthError.Network(cause).
    @Test
    fun `register maps a connection failure to AuthError Network`() = runTest {
        val engine = MockEngine {
            throw kotlinx.io.IOException("Simulated connection failure")
        }
        val sut = buildRegisterSut(engine)

        val result = sut.register(
            name = "Ada Lovelace",
            email = "ada.lovelace@example.com",
            password = "S3cr3t!2024"
        )

        assertTrue(result.isFailure)
        assertIs<AuthError.Network>(result.exceptionOrNull())
    }
}
