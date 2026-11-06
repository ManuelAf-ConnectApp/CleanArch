package com.connectapp.data.bookings.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.connectapp.data.bookings.database.CachedOrderEntity
import com.connectapp.data.bookings.database.OrdersDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

/**
 * Local (SQLDelight-backed) cache of pedidos — see specs/010-offline-cache-layer/data-model.md.
 * [observeAll] is the single source of truth [BookingsRepositoryImpl][com.connectapp.data.bookings.repository.BookingsRepositoryImpl]
 * exposes as `observeBookings()`; writes only ever happen via [replaceAll] (a full, atomic
 * replacement after a successful network refresh) or [clear] (logout).
 */
interface BookingsLocalDataSource {
    fun observeAll(): Flow<List<CachedOrderEntity>>
    fun replaceAll(orders: List<CachedOrderEntity>)
    fun upsert(order: CachedOrderEntity)
    fun delete(id: String)
    fun clear()
}

class BookingsLocalDataSourceImpl(
    database: OrdersDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : BookingsLocalDataSource {

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
                    guests = order.guests,
                    totalCents = order.totalCents,
                    paymentMethod = order.paymentMethod,
                    cancellationWindowHours = order.cancellationWindowHours,
                    fetchedAt = order.fetchedAt,
                )
            }
        }
    }

    override fun upsert(order: CachedOrderEntity) {
        queries.insertOrder(
            id = order.id,
            title = order.title,
            status = order.status,
            date = order.date,
            guests = order.guests,
            totalCents = order.totalCents,
            paymentMethod = order.paymentMethod,
            cancellationWindowHours = order.cancellationWindowHours,
            fetchedAt = order.fetchedAt,
        )
    }

    override fun delete(id: String) {
        queries.deleteById(id)
    }

    override fun clear() {
        queries.clear()
    }
}
