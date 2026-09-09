package com.connectapp.core.network

/**
 * A per-datasource seam for attaching request headers (e.g. auth tokens) and mapping failures
 * uniformly, regardless of which feature's remote datasource is calling through it.
 */
interface NetworkInterceptor {
    fun getHeaders(): Map<String, String>
    suspend fun <T> execute(call: suspend () -> T): Result<T>
}
