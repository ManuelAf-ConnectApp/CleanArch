package com.connectapp.presentation.orders

import com.connectapp.domain.model.Order

data class OrdersState(
    val orders: List<Order> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val hasError: Boolean = false,
    val lastFetchedAt: Long? = null,
) {
    /**
     * FR-006 (specs/010-offline-cache-layer): non-blocking signal that [orders] may be stale —
     * there is cached data, but the most recent refresh attempt failed to confirm it's current.
     */
    val isShowingCachedData: Boolean
        get() = lastFetchedAt != null && hasError
}
