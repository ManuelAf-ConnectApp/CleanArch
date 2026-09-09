package com.connectapp.domain.usecase

import com.connectapp.domain.model.Order
import com.connectapp.domain.repository.OrdersRepository
import kotlinx.coroutines.flow.Flow

class ObserveOrdersUseCase(
    private val repository: OrdersRepository,
) {
    operator fun invoke(): Flow<List<Order>> = repository.observeOrders()
}
