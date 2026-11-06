package com.connectapp.presentation.home

import com.connectapp.core.mvi.MviViewModel

class HomeViewModel : MviViewModel<HomeState, HomeIntent, HomeEffect>(HomeState()) {
    override fun reduce(intent: HomeIntent) = when (intent) {
        is HomeIntent.SearchQueryChanged -> updateState { it.copy(searchQuery = intent.query) }
        is HomeIntent.CuisineSelected -> updateState { it.copy(selectedCuisine = intent.cuisine) }
        HomeIntent.ClearFilters -> updateState { it.copy(searchQuery = "", selectedCuisine = Cuisine.ALL) }
        is HomeIntent.RestaurantSelected -> emitEffect(HomeEffect.NavigateToRestaurant(intent.restaurant.id))
    }
}
