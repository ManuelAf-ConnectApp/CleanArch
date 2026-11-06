package com.connectapp.presentation.bookings

import com.connectapp.domain.model.Booking

data class BookingsState(
    val bookings: List<Booking> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val hasError: Boolean = false,
    val lastFetchedAt: Long? = null,
) {
    /**
     * FR-006 (specs/010-offline-cache-layer): non-blocking signal that [bookings] may be stale —
     * there is cached data, but the most recent refresh attempt failed to confirm it's current.
     */
    val isShowingCachedData: Boolean
        get() = lastFetchedAt != null && hasError
}
