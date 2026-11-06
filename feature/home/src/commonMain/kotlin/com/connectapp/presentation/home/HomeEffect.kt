package com.connectapp.presentation.home

sealed interface HomeEffect {
    data class NavigateToRestaurant(val restaurantId: String) : HomeEffect
}
