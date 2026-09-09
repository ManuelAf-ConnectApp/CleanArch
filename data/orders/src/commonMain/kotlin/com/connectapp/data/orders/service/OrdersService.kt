package com.connectapp.data.orders.service

import com.connectapp.data.orders.model.OrderDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

/**
 * Real HTTP endpoint for the orders list. No backend is deployed/connected as part of this
 * template — `baseUrl` for the injected [HttpClient] is configured by the app's composition root.
 */
interface OrdersService {
    suspend fun getOrders(): List<OrderDto>
}

class OrdersServiceImpl(
    private val httpClient: HttpClient
) : OrdersService {
    override suspend fun getOrders(): List<OrderDto> {
        return httpClient.get("/orders").body()
    }
}
