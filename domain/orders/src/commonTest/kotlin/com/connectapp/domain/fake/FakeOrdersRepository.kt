package com.connectapp.domain.fake

import com.connectapp.domain.model.Order
import com.connectapp.domain.repository.OrdersRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeOrdersRepository(
    private val ordersFlow: MutableStateFlow<List<Order>> = MutableStateFlow(emptyList()),
    private val refreshResult: Result<Unit> = Result.success(Unit),
) : OrdersRepository {

    var refreshCallCount: Int = 0
        private set

    var clearCacheCallCount: Int = 0
        private set

    override fun observeOrders(): Flow<List<Order>> = ordersFlow

    override suspend fun refreshOrders(): Result<Unit> {
        refreshCallCount++
        return refreshResult
    }

    override suspend fun clearCache() {
        clearCacheCallCount++
        ordersFlow.value = emptyList()
    }

    override fun observeLastFetchedAt(): Flow<Long?> = MutableStateFlow(null)
}
