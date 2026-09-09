package com.connectapp.data.interceptor

import com.connectapp.core.network.NetworkInterceptor
import com.connectapp.data.datasource.AuthLocalDataSource
import com.connectapp.data.mapper.toAuthError

class AuthInterceptor(
    private val authLocalDataSource: AuthLocalDataSource
) : NetworkInterceptor {

    override fun getHeaders(): Map<String, String> {
        val headers = mutableMapOf<String, String>()
        headers["Accept"] = "application/json"
        headers["Content-Type"] = "application/json"

        authLocalDataSource.getToken()?.let {
            headers["Authorization"] = "Bearer $it"
        }
        return headers
    }

    override suspend fun <T> execute(call: suspend () -> T): Result<T> {
        return try {
            Result.success(call())
        } catch (e: Exception) {
            // Ktor exceptions (and any other exception from the HTTP layer) are mapped to the
            // domain's typed AuthError here — see AuthErrorMapper.kt.
            Result.failure(e.toAuthError())
        }
    }
}
