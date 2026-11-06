package com.connectapp.presentation.bookings

import com.connectapp.domain.analytics.AnalyticsReporter
import com.connectapp.domain.analytics.FailureCategory
import com.connectapp.domain.analytics.FlowOutcome
import com.connectapp.domain.analytics.KeyFlow
import com.connectapp.domain.model.Booking
import com.connectapp.domain.model.BookingStatus
import com.connectapp.domain.usecase.ObserveBookingsFetchedAtUseCase
import com.connectapp.domain.usecase.ObserveBookingsUseCase
import com.connectapp.domain.usecase.RefreshBookingsUseCase
import com.connectapp.domain.usecase.TrackFlowEventUseCase
import com.connectapp.presentation.bookings.fake.FakeBookingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class BookingsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val sampleBookings = listOf(
        Booking(id = "1", title = "Booking 1", status = BookingStatus.CONFIRMED, date = "2026-08-01"),
    )

    private class FakeAnalyticsReporter : AnalyticsReporter {
        override fun setConsent(granted: Boolean) = Unit
        override fun observeConsent(): Flow<Boolean> = MutableStateFlow(false)
        override fun setCurrentScreen(screen: String) = Unit
        override fun trackFlowEvent(flow: KeyFlow, outcome: FlowOutcome, failureCategory: FailureCategory?) = Unit
    }

    private fun viewModel(repository: FakeBookingsRepository) =
        BookingsViewModel(
            ObserveBookingsUseCase(repository),
            RefreshBookingsUseCase(repository),
            ObserveBookingsFetchedAtUseCase(repository),
            TrackFlowEventUseCase(FakeAnalyticsReporter()),
        )

    @Test
    fun `initial refresh success populates orders and clears loading`() = runTest {
        val viewModel = viewModel(FakeBookingsRepository(refreshResult = Result.success(sampleBookings)))

        advanceUntilIdle()

        assertEquals(sampleBookings, viewModel.state.value.bookings)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `initial refresh failure with no cache leaves orders empty and sets hasError`() = runTest {
        val viewModel = viewModel(FakeBookingsRepository(refreshResult = Result.failure(Exception("network down"))))

        advanceUntilIdle()

        assertTrue(viewModel.state.value.bookings.isEmpty())
        assertFalse(viewModel.state.value.isLoading)
        assertTrue(viewModel.state.value.hasError)
    }

    @Test
    fun `Retry after a failure reloads the orders`() = runTest {
        val repository = FakeBookingsRepository(refreshResult = Result.success(sampleBookings))
        val viewModel = viewModel(repository)

        advanceUntilIdle()
        viewModel.onIntent(BookingsIntent.Retry)
        advanceUntilIdle()

        assertEquals(sampleBookings, viewModel.state.value.bookings)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `a failed refresh does not clear orders already shown from cache`() = runTest {
        val repository = FakeBookingsRepository(initialOrders = sampleBookings, refreshResult = Result.failure(Exception("network down")))
        val viewModel = viewModel(repository)

        advanceUntilIdle()

        assertEquals(sampleBookings, viewModel.state.value.bookings)
        assertTrue(viewModel.state.value.hasError)
    }

    @Test
    fun `Refresh on an already-loaded list uses isRefreshing and never touches isLoading`() = runTest {
        val repository = FakeBookingsRepository(refreshResult = Result.success(sampleBookings))
        val viewModel = viewModel(repository)
        advanceUntilIdle()

        viewModel.onIntent(BookingsIntent.Refresh)

        assertTrue(viewModel.state.value.isRefreshing)
        assertFalse(viewModel.state.value.isLoading)

        advanceUntilIdle()

        assertEquals(sampleBookings, viewModel.state.value.bookings)
        assertFalse(viewModel.state.value.isRefreshing)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `a failed Refresh preserves the cached orders and never sets isLoading`() = runTest {
        val repository = FakeBookingsRepository(
            initialOrders = sampleBookings,
            refreshResult = Result.failure(Exception("network down")),
        )
        val viewModel = viewModel(repository)
        advanceUntilIdle()

        viewModel.onIntent(BookingsIntent.Refresh)
        advanceUntilIdle()

        assertEquals(sampleBookings, viewModel.state.value.bookings)
        assertTrue(viewModel.state.value.hasError)
        assertFalse(viewModel.state.value.isRefreshing)
        assertFalse(viewModel.state.value.isLoading)
    }
}
