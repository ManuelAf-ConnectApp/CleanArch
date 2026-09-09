package com.connectapp.presentation.orders

import com.connectapp.domain.analytics.AnalyticsReporter
import com.connectapp.domain.analytics.FailureCategory
import com.connectapp.domain.analytics.FlowOutcome
import com.connectapp.domain.analytics.KeyFlow
import com.connectapp.domain.model.Order
import com.connectapp.domain.model.OrderStatus
import com.connectapp.domain.usecase.ObserveOrdersFetchedAtUseCase
import com.connectapp.domain.usecase.ObserveOrdersUseCase
import com.connectapp.domain.usecase.RefreshOrdersUseCase
import com.connectapp.domain.usecase.TrackFlowEventUseCase
import com.connectapp.presentation.orders.fake.FakeOrdersRepository
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
class OrdersViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val sampleOrders = listOf(
        Order(id = "1", title = "Order 1", status = OrderStatus.PENDING, date = "2026-08-01"),
    )

    private class FakeAnalyticsReporter : AnalyticsReporter {
        override fun setConsent(granted: Boolean) = Unit
        override fun observeConsent(): Flow<Boolean> = MutableStateFlow(false)
        override fun setCurrentScreen(screen: String) = Unit
        override fun trackFlowEvent(flow: KeyFlow, outcome: FlowOutcome, failureCategory: FailureCategory?) = Unit
    }

    private fun viewModel(repository: FakeOrdersRepository) =
        OrdersViewModel(
            ObserveOrdersUseCase(repository),
            RefreshOrdersUseCase(repository),
            ObserveOrdersFetchedAtUseCase(repository),
            TrackFlowEventUseCase(FakeAnalyticsReporter()),
        )

    @Test
    fun `initial refresh success populates orders and clears loading`() = runTest {
        val viewModel = viewModel(FakeOrdersRepository(refreshResult = Result.success(sampleOrders)))

        advanceUntilIdle()

        assertEquals(sampleOrders, viewModel.state.value.orders)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `initial refresh failure with no cache leaves orders empty and sets hasError`() = runTest {
        val viewModel = viewModel(FakeOrdersRepository(refreshResult = Result.failure(Exception("network down"))))

        advanceUntilIdle()

        assertTrue(viewModel.state.value.orders.isEmpty())
        assertFalse(viewModel.state.value.isLoading)
        assertTrue(viewModel.state.value.hasError)
    }

    @Test
    fun `Retry after a failure reloads the orders`() = runTest {
        val repository = FakeOrdersRepository(refreshResult = Result.success(sampleOrders))
        val viewModel = viewModel(repository)

        advanceUntilIdle()
        viewModel.onIntent(OrdersIntent.Retry)
        advanceUntilIdle()

        assertEquals(sampleOrders, viewModel.state.value.orders)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `a failed refresh does not clear orders already shown from cache`() = runTest {
        val repository = FakeOrdersRepository(initialOrders = sampleOrders, refreshResult = Result.failure(Exception("network down")))
        val viewModel = viewModel(repository)

        advanceUntilIdle()

        assertEquals(sampleOrders, viewModel.state.value.orders)
        assertTrue(viewModel.state.value.hasError)
    }

    @Test
    fun `Refresh on an already-loaded list uses isRefreshing and never touches isLoading`() = runTest {
        val repository = FakeOrdersRepository(refreshResult = Result.success(sampleOrders))
        val viewModel = viewModel(repository)
        advanceUntilIdle()

        viewModel.onIntent(OrdersIntent.Refresh)

        assertTrue(viewModel.state.value.isRefreshing)
        assertFalse(viewModel.state.value.isLoading)

        advanceUntilIdle()

        assertEquals(sampleOrders, viewModel.state.value.orders)
        assertFalse(viewModel.state.value.isRefreshing)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `a failed Refresh preserves the cached orders and never sets isLoading`() = runTest {
        val repository = FakeOrdersRepository(
            initialOrders = sampleOrders,
            refreshResult = Result.failure(Exception("network down")),
        )
        val viewModel = viewModel(repository)
        advanceUntilIdle()

        viewModel.onIntent(OrdersIntent.Refresh)
        advanceUntilIdle()

        assertEquals(sampleOrders, viewModel.state.value.orders)
        assertTrue(viewModel.state.value.hasError)
        assertFalse(viewModel.state.value.isRefreshing)
        assertFalse(viewModel.state.value.isLoading)
    }
}
