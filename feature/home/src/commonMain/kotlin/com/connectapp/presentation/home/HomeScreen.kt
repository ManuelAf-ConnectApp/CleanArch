package com.connectapp.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.presentation.home.component.HomeCard
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(modifier: Modifier = Modifier, onRestaurantSelected: (String) -> Unit) {
    val viewModel: HomeViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.effect.collect { onRestaurantSelected((it as HomeEffect.NavigateToRestaurant).restaurantId) } }
    Box(modifier.fillMaxSize().padding(horizontal = 24.dp)) { HomeScreenContent(state, viewModel::onIntent) }
}

@Composable
private fun HomeScreenContent(state: HomeState, onIntent: (HomeIntent) -> Unit) {
    LazyColumn(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Descubre dónde reservar", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Restaurantes ficticios para probar RestaurantApp.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(state.searchQuery, { onIntent(HomeIntent.SearchQueryChanged(it)) }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Busca por restaurante o ciudad") })
            Spacer(Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(Cuisine.entries) { cuisine ->
                    AssistChip(
                        onClick = { onIntent(HomeIntent.CuisineSelected(cuisine)) }, label = { Text(cuisine.label) },
                        colors = if (state.selectedCuisine == cuisine) AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer, labelColor = MaterialTheme.colorScheme.onPrimaryContainer) else AssistChipDefaults.assistChipColors(),
                    )
                }
            }
        }
        if (state.filteredRestaurants.isEmpty()) item {
            Spacer(Modifier.height(32.dp))
            Text("No hemos encontrado restaurantes con esos filtros.", style = MaterialTheme.typography.titleMedium)
            Button({ onIntent(HomeIntent.ClearFilters) }, Modifier.padding(top = 12.dp)) { Text("Borrar filtros") }
        } else items(state.filteredRestaurants, key = { it.id }) { restaurant ->
            HomeCard(restaurant) { onIntent(HomeIntent.RestaurantSelected(restaurant)) }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}
