package com.connectapp.domain.model

/**
 * Only type of failure exception that [com.connectapp.domain.repository.AuthRepository]'s
 * `Result<T>` may carry — no other exception type should ever reach `Result.failure`.
 */
sealed class AuthError : Exception() {
    object InvalidCredentials : AuthError()
    object EmailAlreadyRegistered : AuthError()
    object AccountNotFound : AuthError()
    object SessionExpired : AuthError()
    data class Network(override val cause: Throwable) : AuthError()
    data class Server(val code: Int) : AuthError()
    data class Unknown(override val cause: Throwable) : AuthError()
}
