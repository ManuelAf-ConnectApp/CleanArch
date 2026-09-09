package com.connectapp.data.datasource

import com.connectapp.core.network.NetworkInterceptor
import com.connectapp.data.mapper.toAuthError
import com.connectapp.data.model.UserDto
import com.connectapp.data.model.UserRequestDto
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
private class FakeNetworkInterceptor : NetworkInterceptor {
    override fun getHeaders(): Map<String, String> = emptyMap()

    override suspend fun <T> execute(call: suspend () -> T): Result<T> {
        return try {
            Result.success(call())
        } catch (e: Exception) {
            Result.failure(e.toAuthError())
        }
    }
}

private fun buildSut(engine: MockEngine): AuthRemoteDataSourceImpl {
    val httpClient = HttpClient(engine) {
        // Reuse the production content-negotiation/expectSuccess config instead of hand-rolling
        // a separate one that could silently diverge from what AuthServiceImpl actually runs
        // against in production (see HttpClientFactory.kt).
        configureDefaultClient(baseUrl = TEST_BASE_URL)
    }
    return AuthRemoteDataSourceImpl(
        networkInterceptor = FakeNetworkInterceptor(),
        authService = AuthServiceImpl(httpClient)
    )
}

private fun HttpRequestData.bodyAsText(): String =
    (body as OutgoingContent.ByteArrayContent).bytes().decodeToString()

class AuthRemoteDataSourceLoginTest {

    // contracts/http-auth-contract.md POST /auth/login, Response 200. This is the test that
    // directly guards against the original bug this initiative started from: login must return
    // the exact User the backend responded with, never a hardcoded/mock fallback (quickstart.md §1).
    @Test
    fun `login returns the exact UserDto from a 200 response and not a hardcoded fallback`() = runTest {
        val responseJson = """
            {
              "id": "usr-8842",
              "firstName": "Grace",
              "lastName": "Hopper",
              "email": "grace.hopper@example.com",
              "phone": "+1-555-0199",
              "createdAt": "2024-01-15T10:30:00Z",
              "updatedAt": "2024-06-20T08:00:00Z"
            }
        """.trimIndent()

        val engine = MockEngine { request ->
            respond(
                content = responseJson,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        val sut = buildSut(engine)

        val result = sut.login(email = "grace.hopper@example.com", password = "S3cr3t!2024")

        assertTrue(result.isSuccess)
        assertEquals(
            UserDto(
                id = "usr-8842",
                firstName = "Grace",
                lastName = "Hopper",
                email = "grace.hopper@example.com",
                phone = "+1-555-0199",
                createdAt = "2024-01-15T10:30:00Z",
                updatedAt = "2024-06-20T08:00:00Z"
            ),
            result.getOrNull()
        )

        // Contract sanity check: the request actually sent matches contracts/http-auth-contract.md
        // (POST /auth/login with { email, password }), so a passing test above isn't accidental.
        val sentRequest = engine.requestHistory.single()
        assertEquals(HttpMethod.Post, sentRequest.method)
        assertEquals("/auth/login", sentRequest.url.encodedPath)
        assertEquals(
            UserRequestDto(email = "grace.hopper@example.com", password = "S3cr3t!2024"),
            defaultJson.decodeFromString<UserRequestDto>(sentRequest.bodyAsText())
        )
    }

    // contracts/http-auth-contract.md: Response 401 -> AuthError.InvalidCredentials.
    @Test
    fun `login maps a 401 response to AuthError InvalidCredentials`() = runTest {
        val engine = MockEngine { respondError(HttpStatusCode.Unauthorized) }
        val sut = buildSut(engine)

        val result = sut.login(email = "grace.hopper@example.com", password = "wrong-password")

        assertTrue(result.isFailure)
        assertEquals(AuthError.InvalidCredentials, result.exceptionOrNull())
    }

    // contracts/http-auth-contract.md: Response 5xx -> AuthError.Server(code).
    @Test
    fun `login maps a 500 response to AuthError Server with the response code`() = runTest {
        val engine = MockEngine { respondError(HttpStatusCode.InternalServerError) }
        val sut = buildSut(engine)

        val result = sut.login(email = "grace.hopper@example.com", password = "S3cr3t!2024")

        assertTrue(result.isFailure)
        assertEquals(AuthError.Server(500), result.exceptionOrNull())
    }

    // contracts/http-auth-contract.md: fallo de conexión/timeout -> AuthError.Network(cause).
    @Test
    fun `login maps a connection failure to AuthError Network`() = runTest {
        val engine = MockEngine {
            throw kotlinx.io.IOException("Simulated connection failure")
        }
        val sut = buildSut(engine)

        val result = sut.login(email = "grace.hopper@example.com", password = "S3cr3t!2024")

        assertTrue(result.isFailure)
        assertIs<AuthError.Network>(result.exceptionOrNull())
    }
}
