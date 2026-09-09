package com.connectapp.presentation.home

sealed interface HomeIntent {
    data class ItemClicked(val item: DashboardItem) : HomeIntent
}