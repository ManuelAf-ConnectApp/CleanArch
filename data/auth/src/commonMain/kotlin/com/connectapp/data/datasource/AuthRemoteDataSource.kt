package com.connectapp.data.datasource

import com.connectapp.core.network.NetworkInterceptor
import com.connectapp.data.model.UserDto
import com.connectapp.data.service.AuthService
import com.connectapp.domain.model.AuthError

interface AuthRemoteDataSource {
    suspend fun login(email: String, password: String): Result<UserDto>
    suspend fun register(name: String, email: String, password: String): Result<Boolean>
    suspend fun forgotPassword(email: String): Result<Unit>
}

class AuthRemoteDataSourceImpl(
    private val networkInterceptor: NetworkInterceptor,
    private val authService: AuthService
) : AuthRemoteDataSource {
    override suspend fun login(email: String, password: String): Result<UserDto> {
        return networkInterceptor.execute {
            authService.login(email, password)
        }
    }

    override suspend fun register(name: String, email: String, password: String): Result<Boolean> {
        return networkInterceptor.execute {
            authService.register(name, email, password)
            true
        }
    }

    override suspend fun forgotPassword(email: String): Result<Unit> {
        return networkInterceptor.execute {
            authService.forgotPassword(email)
        }
    }
}

/**
 * Local authentication used by the MVP builds while there is no backend to connect to.
 *
 * Keeping it behind [AuthRemoteDataSource] means the repository still seeds the profile cache
 * and persists the remembered session exactly as it does with the real service.
 */
class MvpAuthRemoteDataSource : AuthRemoteDataSource {
    override suspend fun login(email: String, password: String): Result<UserDto> =
        if (email == DEMO_EMAIL && password == DEMO_PASSWORD) {
            Result.success(DEMO_USER)
        } else {
            Result.failure(AuthError.InvalidCredentials)
        }

    override suspend fun register(name: String, email: String, password: String): Result<Boolean> =
        Result.success(true)

    override suspend fun forgotPassword(email: String): Result<Unit> = Result.success(Unit)

    private companion object {
        const val DEMO_EMAIL = "demo@restaurantapp.local"
        const val DEMO_PASSWORD = "demo1234"

        val DEMO_USER = UserDto(
            id = "mvp-demo-user",
            firstName = "Sofía",
            lastName = "Martín",
            email = DEMO_EMAIL,
            phone = "+34 600 123 456",
            createdAt = "2026-01-01T00:00:00Z",
            updatedAt = "2026-01-01T00:00:00Z",
        )
    }
}
