package com.connectapp.data.orders.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.connectapp.data.orders.database.CachedOrderEntity
import com.connectapp.data.orders.database.OrdersDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

/**
 * Local (SQLDelight-backed) cache of pedidos — see specs/010-offline-cache-layer/data-model.md.
 * [observeAll] is the single source of truth [OrdersRepositoryImpl][com.connectapp.data.orders.repository.OrdersRepositoryImpl]
 * exposes as `observeOrders()`; writes only ever happen via [replaceAll] (a full, atomic
 * replacement after a successful network refresh) or [clear] (logout).
 */
interface OrdersLocalDataSource {
    fun observeAll(): Flow<List<CachedOrderEntity>>
    fun replaceAll(orders: List<CachedOrderEntity>)
    fun clear()
}

class OrdersLocalDataSourceImpl(
    database: OrdersDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : OrdersLocalDataSource {

    private val queries = database.cachedOrderQueries

    override fun observeAll(): Flow<List<CachedOrderEntity>> =
        queries.selectAll().asFlow().mapToList(dispatcher)

    override fun replaceAll(orders: List<CachedOrderEntity>) {
        queries.transaction {
            queries.clear()
            orders.forEach { order ->
                queries.insertOrder(
                    id = order.id,
                    title = order.title,
                    status = order.status,
                    date = order.date,
                    fetchedAt = order.fetchedAt,
                )
            }
        }
    }

    override fun clear() {
        queries.clear()
    }
}
