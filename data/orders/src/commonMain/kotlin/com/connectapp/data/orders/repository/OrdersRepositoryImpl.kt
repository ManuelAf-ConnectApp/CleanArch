package com.connectapp.data.orders.repository

import com.connectapp.core.database.currentEpochMillis
import com.connectapp.data.orders.datasource.OrdersLocalDataSource
import com.connectapp.data.orders.datasource.OrdersRemoteDataSource
import com.connectapp.data.orders.mapper.toCacheEntity
import com.connectapp.data.orders.mapper.toDomain
import com.connectapp.domain.model.Order
import com.connectapp.domain.repository.OrdersRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * See specs/010-offline-cache-layer/contracts/orders-repository-contract.md. [ordersLocalDataSource]
 * is the single source of truth for [observeOrders] — [refreshOrders] only ever writes to it after
 * a successful network call (FR-005); a failed refresh leaves it untouched (FR-012).
 */
class OrdersRepositoryImpl(
    private val ordersRemoteDataSource: OrdersRemoteDataSource,
    private val ordersLocalDataSource: OrdersLocalDataSource,
) : OrdersRepository {

    override fun observeOrders(): Flow<List<Order>> =
        ordersLocalDataSource.observeAll().map { cached -> cached.map { it.toDomain() } }

    override suspend fun refreshOrders(): Result<Unit> {
        return ordersRemoteDataSource.getOrders().map { dtos ->
            val fetchedAt = currentEpochMillis()
            val cacheEntities = dtos.map { it.toDomain().toCacheEntity(fetchedAt) }
            ordersLocalDataSource.replaceAll(cacheEntities)
        }
    }

    override suspend fun clearCache() {
        ordersLocalDataSource.clear()
    }

    override fun observeLastFetchedAt(): Flow<Long?> =
        ordersLocalDataSource.observeAll().map { cached -> cached.maxOfOrNull { it.fetchedAt } }
}
