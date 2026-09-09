package com.connectapp.presentation.home

sealed interface HomeEffect {

    data class ShowToast(val item: DashboardItem) : HomeEffect
}