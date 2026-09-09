package com.connectapp.presentation.orders

sealed interface OrdersIntent {
    data object Retry : OrdersIntent
    data object Refresh : OrdersIntent
}
