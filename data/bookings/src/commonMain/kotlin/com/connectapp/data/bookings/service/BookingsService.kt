package com.connectapp.data.bookings.service

import com.connectapp.data.bookings.model.OrderDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

/**
 * Real HTTP endpoint for the orders list. No backend is deployed/connected as part of this
 * template — `baseUrl` for the injected [HttpClient] is configured by the app's composition root.
 */
interface BookingsService {
    suspend fun getBookings(): List<OrderDto>
}

class BookingsServiceImpl(
    private val httpClient: HttpClient
) : BookingsService {
    override suspend fun getBookings(): List<OrderDto> {
        return httpClient.get("/orders").body()
    }
}
