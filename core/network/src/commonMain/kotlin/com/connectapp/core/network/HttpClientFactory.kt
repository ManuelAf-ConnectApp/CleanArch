package com.connectapp.core.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Creates the platform [HttpClient] a feature's data layer uses to talk to its backend.
 *
 * `baseUrl` is injected by the caller (typically the app's composition root) — this factory never
 * hardcodes a real backend address.
 */
expect fun createHttpClient(baseUrl: String): HttpClient

/** Shared JSON config for (de)serializing DTOs over the wire. */
val defaultJson: Json = Json {
    ignoreUnknownKeys = true
}

/**
 * Common configuration applied by every platform's [createHttpClient] actual — content
 * negotiation via kotlinx.serialization, the injected base URL, and `expectSuccess = true` so
 * non-2xx responses surface as Ktor exceptions for callers to map into their own domain errors.
 * Public so consumers can build an equivalent client against a different engine (e.g. a
 * [io.ktor.client.engine.mock.MockEngine] in tests) without the config silently diverging.
 */
fun <T : HttpClientEngineConfig> HttpClientConfig<T>.configureDefaultClient(baseUrl: String) {
    expectSuccess = true

    install(ContentNegotiation) {
        json(defaultJson)
    }

    install(HttpTimeout) {
        requestTimeoutMillis = 30_000
        connectTimeoutMillis = 15_000
        socketTimeoutMillis = 30_000
    }

    defaultRequest {
        url(baseUrl)
    }
}
