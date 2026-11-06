package com.connectapp.presentation.bookings.fake

import com.connectapp.domain.model.Booking
import com.connectapp.domain.repository.BookingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * [refreshResult] models what a real `refreshBookings()` would do: on success, it carries the
 * orders a network refresh would have returned, which this fake publishes to [observeBookings] —
 * mirroring `BookingsRepositoryImpl` (a successful refresh replaces the cache, FR-005); on failure,
 * [observeBookings] keeps emitting whatever was already there (FR-012).
 */
internal class FakeBookingsRepository(
    initialOrders: List<Booking> = emptyList(),
    private val refreshResult: Result<List<Booking>> = Result.success(initialOrders),
) : BookingsRepository {

    private val ordersFlow = MutableStateFlow(initialOrders)

    var clearCacheCallCount: Int = 0
        private set

    override fun observeBookings(): Flow<List<Booking>> = ordersFlow

    override suspend fun refreshBookings(): Result<Unit> =
        refreshResult.map { orders -> ordersFlow.value = orders }

    override suspend fun clearCache() {
        clearCacheCallCount++
        ordersFlow.value = emptyList()
    }

    override fun observeLastFetchedAt(): Flow<Long?> = MutableStateFlow(null)
}
