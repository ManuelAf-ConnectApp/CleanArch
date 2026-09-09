package com.connectapp.data.orders.datasource

import com.connectapp.data.orders.model.OrderDto
import com.connectapp.data.orders.service.OrdersService

interface OrdersRemoteDataSource {
    suspend fun getOrders(): Result<List<OrderDto>>
}

class OrdersRemoteDataSourceImpl(
    private val ordersService: OrdersService
) : OrdersRemoteDataSource {
    override suspend fun getOrders(): Result<List<OrderDto>> {
        return try {
            Result.success(ordersService.getOrders())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
