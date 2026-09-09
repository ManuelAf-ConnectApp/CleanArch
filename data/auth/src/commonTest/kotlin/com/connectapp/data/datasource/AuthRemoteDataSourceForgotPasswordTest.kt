package com.connectapp.data.datasource

import com.connectapp.core.network.NetworkInterceptor
import com.connectapp.data.mapper.toAuthError
import com.connectapp.data.model.ForgotPasswordRequestDto
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
 *
 * Named with a `ForgotPassword` prefix (rather than reusing `FakeNetworkInterceptor`) because a
 * `private` top-level class still gets a JVM binary name clash across files in the same package —
 * see AuthRemoteDataSourceRegisterTest.kt, which hit this first and adopted the same convention.
 */
private class ForgotPasswordFakeNetworkInterceptor : NetworkInterceptor {
    override fun getHeaders(): Map<String, String> = emptyMap()

    override suspend fun <T> execute(call: suspend () -> T): Result<T> {
        return try {
            Result.success(call())
        } catch (e: Exception) {
            Result.failure(e.toAuthError())
        }
    }
}

private fun buildForgotPasswordSut(engine: MockEngine): AuthRemoteDataSourceImpl {
    val httpClient = HttpClient(engine) {
        // Reuse the production content-negotiation/expectSuccess config instead of hand-rolling
        // a separate one that could silently diverge from what AuthServiceImpl actually runs
        // against in production (see HttpClientFactory.kt).
        configureDefaultClient(baseUrl = TEST_BASE_URL)
    }
    return AuthRemoteDataSourceImpl(
        networkInterceptor = ForgotPasswordFakeNetworkInterceptor(),
        authService = AuthServiceImpl(httpClient)
    )
}

private fun HttpRequestData.forgotPasswordBodyAsText(): String =
    (body as OutgoingContent.ByteArrayContent).bytes().decodeToString()

class AuthRemoteDataSourceForgotPasswordTest {

    // contracts/http-auth-contract.md POST /auth/forgot-password, Response 200 -> success, "solo
    // si el backend confirma que procesó la solicitud". AuthServiceImpl.forgotPassword() never
    // calls .body() on the response, so an empty 200 body (as a real "request accepted"
    // acknowledgement would be) must not blow up deserialization.
    @Test
    fun `forgotPassword returns success Unit on a 200 response with an empty body`() = runTest {
        val engine = MockEngine {
            respond(
                content = "",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        val sut = buildForgotPasswordSut(engine)

        val result = sut.forgotPassword(email = "grace.hopper@example.com")

        assertTrue(result.isSuccess)
        assertEquals(Result.success(Unit), result)

        // Contract sanity check: the request actually sent matches contracts/http-auth-contract.md
        // (POST /auth/forgot-password with { email }), so a passing test above isn't accidental.
        val sentRequest = engine.requestHistory.single()
        assertEquals(HttpMethod.Post, sentRequest.method)
        assertEquals("/auth/forgot-password", sentRequest.url.encodedPath)
        assertEquals(
            ForgotPasswordRequestDto(email = "grace.hopper@example.com"),
            defaultJson.decodeFromString<ForgotPasswordRequestDto>(sentRequest.forgotPasswordBodyAsText())
        )
    }

    // contracts/http-auth-contract.md: Response 404 -> AuthError.AccountNotFound. This is the
    // client-side mapping AuthErrorMapper.kt already applies unconditionally for 404 (the
    // privacy-policy nuance of whether the real backend ever sends a 404 here is a
    // presentation-layer/backend-contract concern handled elsewhere, not this test's job).
    @Test
    fun `forgotPassword maps a 404 response to AuthError AccountNotFound`() = runTest {
        val engine = MockEngine { respondError(HttpStatusCode.NotFound) }
        val sut = buildForgotPasswordSut(engine)

        val result = sut.forgotPassword(email = "grace.hopper@example.com")

        assertTrue(result.isFailure)
        assertEquals(AuthError.AccountNotFound, result.exceptionOrNull())
    }

    // contracts/http-auth-contract.md: Response 5xx -> AuthError.Server(code).
    @Test
    fun `forgotPassword maps a 500 response to AuthError Server with the response code`() = runTest {
        val engine = MockEngine { respondError(HttpStatusCode.InternalServerError) }
        val sut = buildForgotPasswordSut(engine)

        val result = sut.forgotPassword(email = "grace.hopper@example.com")

        assertTrue(result.isFailure)
        assertEquals(AuthError.Server(500), result.exceptionOrNull())
    }

    // contracts/http-auth-contract.md: fallo de conexión/timeout -> AuthError.Network(cause).
    @Test
    fun `forgotPassword maps a connection failure to AuthError Network`() = runTest {
        val engine = MockEngine {
            throw kotlinx.io.IOException("Simulated connection failure")
        }
        val sut = buildForgotPasswordSut(engine)

        val result = sut.forgotPassword(email = "grace.hopper@example.com")

        assertTrue(result.isFailure)
        assertIs<AuthError.Network>(result.exceptionOrNull())
    }
}
