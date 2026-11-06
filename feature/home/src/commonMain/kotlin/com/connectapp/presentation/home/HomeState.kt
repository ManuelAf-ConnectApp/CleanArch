package com.connectapp.presentation.home

/** Local deterministic data used until the restaurant repository is introduced. */
data class RestaurantPreview(
    val id: String,
    val name: String,
    val cuisine: Cuisine,
    val city: String,
    val reservationPriceCents: Int,
    val description: String,
)

enum class Cuisine(val label: String) {
    ALL("Todos"), ITALIAN("Italiana"), SPANISH("Española"), ASIAN("Asiática"),
    MEXICAN("Mexicana"), VEGETARIAN("Vegetariana"),
}

val demoRestaurants = listOf(
    RestaurantPreview("la-mesa-del-sol", "La Mesa del Sol", Cuisine.SPANISH, "Madrid", 1200, "Cocina de mercado y sabores de temporada."),
    RestaurantPreview("forno-verde", "Forno Verde", Cuisine.ITALIAN, "Madrid", 1000, "Pasta artesanal y pizzas al horno de piedra."),
    RestaurantPreview("sakura-barrio", "Sakura Barrio", Cuisine.ASIAN, "Barcelona", 1500, "Cocina japonesa contemporánea para compartir."),
    RestaurantPreview("maiz-y-lima", "Maíz y Lima", Cuisine.MEXICAN, "Valencia", 900, "Recetas mexicanas de inspiración mediterránea."),
    RestaurantPreview("huerta-clara", "Huerta Clara", Cuisine.VEGETARIAN, "Sevilla", 800, "Platos vegetales frescos, locales y creativos."),
)

data class HomeState(
    val searchQuery: String = "",
    val selectedCuisine: Cuisine = Cuisine.ALL,
    val restaurants: List<RestaurantPreview> = demoRestaurants,
) {
    val filteredRestaurants: List<RestaurantPreview>
        get() = restaurants.filter { restaurant ->
            val matchesCuisine = selectedCuisine == Cuisine.ALL || restaurant.cuisine == selectedCuisine
            val query = searchQuery.trim()
            val matchesQuery = query.isBlank() || restaurant.name.contains(query, true) || restaurant.city.contains(query, true)
            matchesCuisine && matchesQuery
        }
}
