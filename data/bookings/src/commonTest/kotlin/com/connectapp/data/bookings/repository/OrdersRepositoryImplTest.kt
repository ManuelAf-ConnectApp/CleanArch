package com.connectapp.data.bookings.repository

import com.connectapp.data.bookings.database.CachedOrderEntity
import com.connectapp.data.bookings.datasource.BookingsLocalDataSource
import com.connectapp.data.bookings.datasource.BookingsRemoteDataSource
import com.connectapp.data.bookings.model.OrderDto
import com.connectapp.domain.model.Booking
import com.connectapp.domain.model.BookingStatus
import com.connectapp.domain.model.DemoScenario
import com.connectapp.domain.model.DemoScenarioConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** In-memory stand-in for the SQLDelight-backed cache — no real driver/database involved. */
private class FakeBookingsLocalDataSource : BookingsLocalDataSource {
    private val flow = MutableStateFlow<List<CachedOrderEntity>>(emptyList())

    var clearCallCount: Int = 0
        private set

    override fun observeAll(): Flow<List<CachedOrderEntity>> = flow

    override fun replaceAll(orders: List<CachedOrderEntity>) {
        flow.value = orders
    }

    override fun upsert(order: CachedOrderEntity) {
        flow.value = flow.value.filterNot { it.id == order.id } + order
    }

    override fun delete(id: String) {
        flow.value = flow.value.filterNot { it.id == id }
    }

    override fun clear() {
        clearCallCount++
        flow.value = emptyList()
    }
}

private class FakeBookingsRemoteDataSource(
    private val result: Result<List<OrderDto>>,
) : BookingsRemoteDataSource {
    var callCount: Int = 0
        private set

    override suspend fun getBookings(): Result<List<OrderDto>> {
        callCount++
        return result
    }
}

private val REMOTE_ORDER = OrderDto(id = "1", title = "Booking 1", status = "PENDING", date = "2026-08-01")

private val CACHED_ORDER = CachedOrderEntity(
    id = "0",
    title = "Cached Booking",
    status = "DELIVERED",
    date = "2026-07-01",
    guests = 2,
    totalCents = 2_400,
    paymentMethod = "CARD",
    cancellationWindowHours = 24,
    fetchedAt = 1_000L,
)

class BookingsRepositoryImplTest {

    @Test
    fun `createDemoBooking persists a local restaurant reservation`() = runTest {
        val local = FakeBookingsLocalDataSource()
        val repository = BookingsRepositoryImpl(
            FakeBookingsRemoteDataSource(Result.success(emptyList())),
            local,
        )
        val booking = Booking(
            id = "demo-forno-verde-2030",
            title = "Reserva en Forno Verde · 2 comensales",
            status = BookingStatus.CONFIRMED,
            date = "Mañana, 20:30",
        )

        val result = repository.createDemoBooking(booking)

        assertTrue(result.isSuccess)
        assertEquals(listOf(booking), repository.observeBookings().first())
    }

    @Test
    fun `createDemoBooking does not persist a booking for a failing demo scenario`() = runTest {
        val local = FakeBookingsLocalDataSource()
        val repository = BookingsRepositoryImpl(
            FakeBookingsRemoteDataSource(Result.success(emptyList())),
            local,
        )

        val result = repository.createDemoBooking(
            booking = Booking(id = "conflict", title = "Reserva", status = BookingStatus.CONFIRMED, date = "Mañana"),
            scenarioConfig = DemoScenarioConfig(DemoScenario.CONFLICT),
        )

        assertTrue(result.isFailure)
        assertTrue(repository.observeBookings().first().isEmpty())
    }

    @Test
    fun `every failing demo scenario leaves the cache unchanged`() = runTest {
        DemoScenario.entries
            .filterNot { it == DemoScenario.SUCCESS }
            .forEach { scenario ->
                val local = FakeBookingsLocalDataSource()
                val repository = BookingsRepositoryImpl(
                    FakeBookingsRemoteDataSource(Result.success(emptyList())),
                    local,
                )

                val result = repository.createDemoBooking(
                    Booking(id = scenario.name, title = "Reserva", status = BookingStatus.CONFIRMED, date = "Mañana"),
                    DemoScenarioConfig(scenario),
                )

                assertTrue(result.isFailure, scenario.name)
                assertTrue(repository.observeBookings().first().isEmpty(), scenario.name)
            }
    }

    @Test
    fun `cancelDemoBooking keeps the reservation and marks it as cancelled`() = runTest {
        val local = FakeBookingsLocalDataSource().apply { replaceAll(listOf(CACHED_ORDER)) }
        val repository = BookingsRepositoryImpl(FakeBookingsRemoteDataSource(Result.success(emptyList())), local)

        val result = repository.cancelDemoBooking(CACHED_ORDER.id)

        assertTrue(result.isSuccess)
        assertEquals(
            BookingStatus.CANCELLED,
            repository.observeBookings().first().single().status,
        )
    }

    @Test
    fun `cancelDemoBooking rejects an already cancelled reservation`() = runTest {
        val cancelled = CACHED_ORDER.copy(status = BookingStatus.CANCELLED.name)
        val local = FakeBookingsLocalDataSource().apply { replaceAll(listOf(cancelled)) }
        val repository = BookingsRepositoryImpl(FakeBookingsRemoteDataSource(Result.success(emptyList())), local)

        val result = repository.cancelDemoBooking(cancelled.id)

        assertTrue(result.isFailure)
        assertEquals(BookingStatus.CANCELLED, repository.observeBookings().first().single().status)
    }

    @Test
    fun `observeBookings emits the existing cache without calling the network`() = runTest {
        val local = FakeBookingsLocalDataSource().apply { replaceAll(listOf(CACHED_ORDER)) }
        val remote = FakeBookingsRemoteDataSource(Result.success(emptyList()))
        val repository = BookingsRepositoryImpl(remote, local)

        val orders = repository.observeBookings().first()

        assertEquals(listOf(CACHED_ORDER.id), orders.map { it.id })
        assertEquals(0, remote.callCount)
    }

    @Test
    fun `refreshBookings success replaces the cache and is reflected by observeBookings`() = runTest {
        val local = FakeBookingsLocalDataSource().apply { replaceAll(listOf(CACHED_ORDER)) }
        val remote = FakeBookingsRemoteDataSource(Result.success(listOf(REMOTE_ORDER)))
        val repository = BookingsRepositoryImpl(remote, local)

        val result = repository.refreshBookings()

        assertTrue(result.isSuccess)
        assertEquals(listOf(REMOTE_ORDER.id), repository.observeBookings().first().map { it.id })
    }

    @Test
    fun `refreshBookings failure leaves the cache untouched`() = runTest {
        val local = FakeBookingsLocalDataSource().apply { replaceAll(listOf(CACHED_ORDER)) }
        val remote = FakeBookingsRemoteDataSource(Result.failure(Exception("network down")))
        val repository = BookingsRepositoryImpl(remote, local)

        val result = repository.refreshBookings()

        assertTrue(result.isFailure)
        assertEquals(listOf(CACHED_ORDER.id), repository.observeBookings().first().map { it.id })
    }

    @Test
    fun `clearCache empties the cache`() = runTest {
        val local = FakeBookingsLocalDataSource().apply { replaceAll(listOf(CACHED_ORDER)) }
        val repository = BookingsRepositoryImpl(FakeBookingsRemoteDataSource(Result.success(emptyList())), local)

        repository.clearCache()

        assertEquals(1, local.clearCallCount)
        assertTrue(repository.observeBookings().first().isEmpty())
    }

    @Test
    fun `observeLastFetchedAt reflects the cached rows' fetchedAt`() = runTest {
        val local = FakeBookingsLocalDataSource().apply { replaceAll(listOf(CACHED_ORDER)) }
        val repository = BookingsRepositoryImpl(FakeBookingsRemoteDataSource(Result.success(emptyList())), local)

        assertEquals(CACHED_ORDER.fetchedAt, repository.observeLastFetchedAt().first())
    }

    @Test
    fun `observeLastFetchedAt is null when nothing is cached`() = runTest {
        val repository = BookingsRepositoryImpl(FakeBookingsRemoteDataSource(Result.success(emptyList())), FakeBookingsLocalDataSource())

        assertEquals(null, repository.observeLastFetchedAt().first())
    }
}
