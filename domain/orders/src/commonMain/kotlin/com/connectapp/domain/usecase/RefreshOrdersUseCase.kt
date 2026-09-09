package com.connectapp.domain.usecase

import com.connectapp.domain.repository.OrdersRepository

class RefreshOrdersUseCase(
    private val repository: OrdersRepository,
) {
    suspend operator fun invoke(): Result<Unit> = repository.refreshOrders()
}
