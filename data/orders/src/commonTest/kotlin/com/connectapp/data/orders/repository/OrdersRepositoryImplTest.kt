package com.connectapp.data.orders.repository

import com.connectapp.data.orders.database.CachedOrderEntity
import com.connectapp.data.orders.datasource.OrdersLocalDataSource
import com.connectapp.data.orders.datasource.OrdersRemoteDataSource
import com.connectapp.data.orders.model.OrderDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** In-memory stand-in for the SQLDelight-backed cache — no real driver/database involved. */
private class FakeOrdersLocalDataSource : OrdersLocalDataSource {
    private val flow = MutableStateFlow<List<CachedOrderEntity>>(emptyList())

    var clearCallCount: Int = 0
        private set

    override fun observeAll(): Flow<List<CachedOrderEntity>> = flow

    override fun replaceAll(orders: List<CachedOrderEntity>) {
        flow.value = orders
    }

    override fun clear() {
        clearCallCount++
        flow.value = emptyList()
    }
}

private class FakeOrdersRemoteDataSource(
    private val result: Result<List<OrderDto>>,
) : OrdersRemoteDataSource {
    var callCount: Int = 0
        private set

    override suspend fun getOrders(): Result<List<OrderDto>> {
        callCount++
        return result
    }
}

private val REMOTE_ORDER = OrderDto(id = "1", title = "Order 1", status = "PENDING", date = "2026-08-01")

private val CACHED_ORDER = CachedOrderEntity(
    id = "0",
    title = "Cached Order",
    status = "DELIVERED",
    date = "2026-07-01",
    fetchedAt = 1_000L,
)

class OrdersRepositoryImplTest {

    @Test
    fun `observeOrders emits the existing cache without calling the network`() = runTest {
        val local = FakeOrdersLocalDataSource().apply { replaceAll(listOf(CACHED_ORDER)) }
        val remote = FakeOrdersRemoteDataSource(Result.success(emptyList()))
        val repository = OrdersRepositoryImpl(remote, local)

        val orders = repository.observeOrders().first()

        assertEquals(listOf(CACHED_ORDER.id), orders.map { it.id })
        assertEquals(0, remote.callCount)
    }

    @Test
    fun `refreshOrders success replaces the cache and is reflected by observeOrders`() = runTest {
        val local = FakeOrdersLocalDataSource().apply { replaceAll(listOf(CACHED_ORDER)) }
        val remote = FakeOrdersRemoteDataSource(Result.success(listOf(REMOTE_ORDER)))
        val repository = OrdersRepositoryImpl(remote, local)

        val result = repository.refreshOrders()

        assertTrue(result.isSuccess)
        assertEquals(listOf(REMOTE_ORDER.id), repository.observeOrders().first().map { it.id })
    }

    @Test
    fun `refreshOrders failure leaves the cache untouched`() = runTest {
        val local = FakeOrdersLocalDataSource().apply { replaceAll(listOf(CACHED_ORDER)) }
        val remote = FakeOrdersRemoteDataSource(Result.failure(Exception("network down")))
        val repository = OrdersRepositoryImpl(remote, local)

        val result = repository.refreshOrders()

        assertTrue(result.isFailure)
        assertEquals(listOf(CACHED_ORDER.id), repository.observeOrders().first().map { it.id })
    }

    @Test
    fun `clearCache empties the cache`() = runTest {
        val local = FakeOrdersLocalDataSource().apply { replaceAll(listOf(CACHED_ORDER)) }
        val repository = OrdersRepositoryImpl(FakeOrdersRemoteDataSource(Result.success(emptyList())), local)

        repository.clearCache()

        assertEquals(1, local.clearCallCount)
        assertTrue(repository.observeOrders().first().isEmpty())
    }

    @Test
    fun `observeLastFetchedAt reflects the cached rows' fetchedAt`() = runTest {
        val local = FakeOrdersLocalDataSource().apply { replaceAll(listOf(CACHED_ORDER)) }
        val repository = OrdersRepositoryImpl(FakeOrdersRemoteDataSource(Result.success(emptyList())), local)

        assertEquals(CACHED_ORDER.fetchedAt, repository.observeLastFetchedAt().first())
    }

    @Test
    fun `observeLastFetchedAt is null when nothing is cached`() = runTest {
        val repository = OrdersRepositoryImpl(FakeOrdersRemoteDataSource(Result.success(emptyList())), FakeOrdersLocalDataSource())

        assertEquals(null, repository.observeLastFetchedAt().first())
    }
}
