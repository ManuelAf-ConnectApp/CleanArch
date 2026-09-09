package com.connectapp.presentation.orders

import androidx.lifecycle.viewModelScope
import com.connectapp.core.mvi.StateViewModel
import com.connectapp.domain.analytics.FailureCategory
import com.connectapp.domain.analytics.FlowOutcome
import com.connectapp.domain.analytics.KeyFlow
import com.connectapp.domain.usecase.ObserveOrdersFetchedAtUseCase
import com.connectapp.domain.usecase.ObserveOrdersUseCase
import com.connectapp.domain.usecase.RefreshOrdersUseCase
import com.connectapp.domain.usecase.TrackFlowEventUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * See specs/010-offline-cache-layer/contracts/orders-repository-contract.md. [state].orders is
 * driven entirely by [observeOrdersUseCase] (the cache) — [refreshOrdersUseCase]'s result only
 * ever controls [OrdersState.isLoading]/[OrdersState.hasError], never `orders` directly, so a
 * failed refresh can never wipe out an already-cached list (FR-012).
 */
class OrdersViewModel(
    private val observeOrdersUseCase: ObserveOrdersUseCase,
    private val refreshOrdersUseCase: RefreshOrdersUseCase,
    private val observeOrdersFetchedAtUseCase: ObserveOrdersFetchedAtUseCase,
    private val trackFlowEventUseCase: TrackFlowEventUseCase,
) : StateViewModel<OrdersState, OrdersIntent>(OrdersState()) {

    init {
        observeOrdersUseCase()
            .onEach { orders -> updateState { it.copy(orders = orders) } }
            .launchIn(viewModelScope)
        observeOrdersFetchedAtUseCase()
            .onEach { fetchedAt -> updateState { it.copy(lastFetchedAt = fetchedAt) } }
            .launchIn(viewModelScope)
        loadOrders(isRefresh = false)
    }

    override fun reduce(intent: OrdersIntent) {
        when (intent) {
            OrdersIntent.Retry -> loadOrders(isRefresh = false)
            OrdersIntent.Refresh -> loadOrders(isRefresh = true)
        }
    }

    /**
     * [isRefresh] only picks which "in progress" flag to flip ([OrdersState.isRefreshing] vs
     * [OrdersState.isLoading], specs/009-order-detail-and-refresh/data-model.md) — success/
     * failure handling is otherwise identical, so a pull-to-refresh on an already-loaded list
     * never falls back to the full-screen initial-load treatment.
     */
    private fun loadOrders(isRefresh: Boolean) {
        if (isRefresh) {
            updateState { it.copy(isRefreshing = true, hasError = false) }
        } else {
            updateState { it.copy(isLoading = true, hasError = false) }
        }
        viewModelScope.launch {
            refreshOrdersUseCase()
                .onSuccess {
                    updateState { it.copy(isLoading = false, isRefreshing = false) }
                    trackFlowEventUseCase(KeyFlow.ORDERS_LIST, FlowOutcome.SUCCESS)
                }
                .onFailure {
                    updateState { it.copy(isLoading = false, isRefreshing = false, hasError = true) }
                    // RefreshOrdersUseCase doesn't distinguish failure causes more specifically
                    // than "the refresh failed" yet — see plan.md § Technical Context. The UI only
                    // ever shows the generic orders_error string, never the raw exception message.
                    trackFlowEventUseCase(KeyFlow.ORDERS_LIST, FlowOutcome.FAILURE, FailureCategory.NETWORK)
                }
        }
    }
}
