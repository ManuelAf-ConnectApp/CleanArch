package com.connectapp.presentation.home

sealed interface HomeIntent {
    data class SearchQueryChanged(val query: String) : HomeIntent
    data class CuisineSelected(val cuisine: Cuisine) : HomeIntent
    data class RestaurantSelected(val restaurant: RestaurantPreview) : HomeIntent
    data object ClearFilters : HomeIntent
}
