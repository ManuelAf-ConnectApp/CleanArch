package com.connectapp.data.mapper

import com.connectapp.domain.model.AuthError
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException

/**
 * Translates exceptions coming out of the HTTP layer (Ktor) into the single typed failure
 * [AuthError] that [com.connectapp.domain.repository.AuthRepository] may ever return, per
 * contracts/http-auth-contract.md. This is the only place in `data` allowed to do this mapping —
 * no other class should build an [AuthError] from a raw exception.
 */
fun Throwable.toAuthError(): AuthError = when (this) {
    is AuthError -> this
    is ClientRequestException -> response.status.toAuthError(cause = this)
    is ServerResponseException -> AuthError.Server(response.status.value)
    is ResponseException -> response.status.toAuthError(cause = this)
    is SerializationException -> AuthError.Unknown(this)
    is IOException -> AuthError.Network(this)
    else -> AuthError.Unknown(this)
}

/**
 * Maps a specific HTTP status code (4xx from [ClientRequestException], or any other non-2xx
 * surfaced via the generic [ResponseException]) to the [AuthError] variant documented in
 * contracts/http-auth-contract.md. Any status not explicitly listed there falls back to
 * [AuthError.Server] (5xx) or [AuthError.Unknown] (other/unexpected codes).
 */
private fun HttpStatusCode.toAuthError(cause: Throwable): AuthError = when (this) {
    HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden -> AuthError.InvalidCredentials
    HttpStatusCode.Conflict -> AuthError.EmailAlreadyRegistered
    HttpStatusCode.NotFound -> AuthError.AccountNotFound
    else -> if (value in 500..599) AuthError.Server(value) else AuthError.Unknown(cause)
}
