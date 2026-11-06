package com.connectapp.domain.fake

import com.connectapp.domain.model.Booking
import com.connectapp.domain.repository.BookingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeBookingsRepository(
    private val ordersFlow: MutableStateFlow<List<Booking>> = MutableStateFlow(emptyList()),
    private val refreshResult: Result<Unit> = Result.success(Unit),
) : BookingsRepository {

    var refreshCallCount: Int = 0
        private set

    var clearCacheCallCount: Int = 0
        private set

    override fun observeBookings(): Flow<List<Booking>> = ordersFlow

    override suspend fun refreshBookings(): Result<Unit> {
        refreshCallCount++
        return refreshResult
    }

    override suspend fun clearCache() {
        clearCacheCallCount++
        ordersFlow.value = emptyList()
    }

    override fun observeLastFetchedAt(): Flow<Long?> = MutableStateFlow(null)
}
