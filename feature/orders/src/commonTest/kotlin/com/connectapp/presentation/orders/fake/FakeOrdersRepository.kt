package com.connectapp.presentation.orders.fake

import com.connectapp.domain.model.Order
import com.connectapp.domain.repository.OrdersRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * [refreshResult] models what a real `refreshOrders()` would do: on success, it carries the
 * orders a network refresh would have returned, which this fake publishes to [observeOrders] —
 * mirroring `OrdersRepositoryImpl` (a successful refresh replaces the cache, FR-005); on failure,
 * [observeOrders] keeps emitting whatever was already there (FR-012).
 */
internal class FakeOrdersRepository(
    initialOrders: List<Order> = emptyList(),
    private val refreshResult: Result<List<Order>> = Result.success(initialOrders),
) : OrdersRepository {

    private val ordersFlow = MutableStateFlow(initialOrders)

    var clearCacheCallCount: Int = 0
        private set

    override fun observeOrders(): Flow<List<Order>> = ordersFlow

    override suspend fun refreshOrders(): Result<Unit> =
        refreshResult.map { orders -> ordersFlow.value = orders }

    override suspend fun clearCache() {
        clearCacheCallCount++
        ordersFlow.value = emptyList()
    }

    override fun observeLastFetchedAt(): Flow<Long?> = MutableStateFlow(null)
}
