package com.connectapp.presentation.bookings

sealed interface BookingsIntent {
    data object Retry : BookingsIntent
    data object Refresh : BookingsIntent
}
