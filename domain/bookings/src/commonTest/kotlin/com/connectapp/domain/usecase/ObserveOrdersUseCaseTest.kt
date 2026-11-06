package com.connectapp.domain.usecase

import com.connectapp.domain.fake.FakeBookingsRepository
import com.connectapp.domain.model.Booking
import com.connectapp.domain.model.BookingStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveBookingsUseCaseTest {

    @Test
    fun `invoke emits the repository's current orders without triggering a refresh`() = runTest {
        val orders = listOf(Booking(id = "1", title = "Booking 1", status = BookingStatus.CONFIRMED, date = "2026-08-01"))
        val repository = FakeBookingsRepository(ordersFlow = MutableStateFlow(orders))
        val useCase = ObserveBookingsUseCase(repository)

        assertEquals(orders, useCase().first())
        assertEquals(0, repository.refreshCallCount)
    }

    @Test
    fun `invoke emits an empty list when nothing is cached yet`() = runTest {
        val repository = FakeBookingsRepository()
        val useCase = ObserveBookingsUseCase(repository)

        assertEquals(emptyList(), useCase().first())
    }
}
