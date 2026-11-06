package com.connectapp.presentation.bookings

import androidx.lifecycle.viewModelScope
import com.connectapp.core.mvi.StateViewModel
import com.connectapp.domain.analytics.FailureCategory
import com.connectapp.domain.analytics.FlowOutcome
import com.connectapp.domain.analytics.KeyFlow
import com.connectapp.domain.usecase.ObserveBookingsFetchedAtUseCase
import com.connectapp.domain.usecase.ObserveBookingsUseCase
import com.connectapp.domain.usecase.RefreshBookingsUseCase
import com.connectapp.domain.usecase.TrackFlowEventUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * See specs/010-offline-cache-layer/contracts/bookings-repository-contract.md. [state].bookings is
 * driven entirely by [observeBookingsUseCase] (the cache) — [refreshBookingsUseCase]'s result only
 * ever controls [BookingsState.isLoading]/[BookingsState.hasError], never `bookings` directly, so a
 * failed refresh can never wipe out an already-cached list (FR-012).
 */
class BookingsViewModel(
    private val observeBookingsUseCase: ObserveBookingsUseCase,
    private val refreshBookingsUseCase: RefreshBookingsUseCase,
    private val observeBookingsFetchedAtUseCase: ObserveBookingsFetchedAtUseCase,
    private val trackFlowEventUseCase: TrackFlowEventUseCase,
) : StateViewModel<BookingsState, BookingsIntent>(BookingsState()) {

    init {
        observeBookingsUseCase()
            .onEach { bookings -> updateState { it.copy(bookings = bookings) } }
            .launchIn(viewModelScope)
        observeBookingsFetchedAtUseCase()
            .onEach { fetchedAt -> updateState { it.copy(lastFetchedAt = fetchedAt) } }
            .launchIn(viewModelScope)
        loadBookings(isRefresh = false)
    }

    override fun reduce(intent: BookingsIntent) {
        when (intent) {
            BookingsIntent.Retry -> loadBookings(isRefresh = false)
            BookingsIntent.Refresh -> loadBookings(isRefresh = true)
        }
    }

    /**
     * [isRefresh] only picks which "in progress" flag to flip ([BookingsState.isRefreshing] vs
     * [BookingsState.isLoading], specs/009-order-detail-and-refresh/data-model.md) — success/
     * failure handling is otherwise identical, so a pull-to-refresh on an already-loaded list
     * never falls back to the full-screen initial-load treatment.
     */
    private fun loadBookings(isRefresh: Boolean) {
        if (isRefresh) {
            updateState { it.copy(isRefreshing = true, hasError = false) }
        } else {
            updateState { it.copy(isLoading = true, hasError = false) }
        }
        viewModelScope.launch {
            refreshBookingsUseCase()
                .onSuccess {
                    updateState { it.copy(isLoading = false, isRefreshing = false) }
                    trackFlowEventUseCase(KeyFlow.ORDERS_LIST, FlowOutcome.SUCCESS)
                }
                .onFailure {
                    updateState { it.copy(isLoading = false, isRefreshing = false, hasError = true) }
                    // RefreshBookingsUseCase doesn't distinguish failure causes more specifically
                    // than "the refresh failed" yet — see plan.md § Technical Context. The UI only
                    // ever shows the generic orders_error string, never the raw exception message.
                    trackFlowEventUseCase(KeyFlow.ORDERS_LIST, FlowOutcome.FAILURE, FailureCategory.NETWORK)
                }
        }
    }
}
