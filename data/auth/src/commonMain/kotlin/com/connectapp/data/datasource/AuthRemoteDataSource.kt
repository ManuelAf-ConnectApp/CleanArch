package com.connectapp.data.datasource

import com.connectapp.core.network.NetworkInterceptor
import com.connectapp.data.model.UserDto
import com.connectapp.data.service.AuthService

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
