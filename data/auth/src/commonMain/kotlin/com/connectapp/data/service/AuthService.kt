package com.connectapp.data.service

import com.connectapp.data.model.ForgotPasswordRequestDto
import com.connectapp.data.model.RegisterRequestDto
import com.connectapp.data.model.UserDto
import com.connectapp.data.model.UserRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Real HTTP endpoints for the auth flow, per contracts/http-auth-contract.md. No backend is
 * deployed/connected in this initiative (FR-010) — `baseUrl` for the injected [HttpClient] is a
 * placeholder configured in composeApp's `dataModule` (see di.kt).
 */
interface AuthService {

    suspend fun login(email: String, password: String): UserDto

    suspend fun register(name: String, email: String, password: String): UserDto

    suspend fun forgotPassword(email: String)
}

class AuthServiceImpl(
    private val httpClient: HttpClient
) : AuthService {

    override suspend fun login(email: String, password: String): UserDto {
        return httpClient.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(UserRequestDto(email = email, password = password))
        }.body()
    }

    override suspend fun register(name: String, email: String, password: String): UserDto {
        return httpClient.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequestDto(name = name, email = email, password = password))
        }.body()
    }

    override suspend fun forgotPassword(email: String) {
        httpClient.post("/auth/forgot-password") {
            contentType(ContentType.Application.Json)
            setBody(ForgotPasswordRequestDto(email = email))
        }
    }
}
