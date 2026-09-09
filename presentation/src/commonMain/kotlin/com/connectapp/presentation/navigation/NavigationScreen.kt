package com.connectapp.presentation.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.connectapp.presentation.MainViewModel


@Composable
fun NavigationScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    entryProvider: (Any) -> NavEntry<out Any>,
) {
    val backStack = remember { mutableStateListOf<Any>(NavigationRoute.SplashRoute) }

    val navState by viewModel.navState.collectAsState()

    LaunchedEffect(navState.route) {
        val newRoute = navState.route
        if (backStack.lastOrNull() != newRoute) {
            if (newRoute == NavigationRoute.HomeRoute || newRoute == NavigationRoute.SplashRoute) {
                backStack.clear()
                backStack.add(newRoute)
            } else {
                backStack.add(newRoute)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    val typedEntryProvider = entryProvider as (Any) -> NavEntry<Any>

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = {
            if (backStack.size > 1) {
                backStack.removeAt(backStack.lastIndex)
                // Al volver atrás, informamos al ViewModel para que la UI (BottomBar) se actualice
                val previousRoute = backStack.last() as NavigationRoute
                viewModel.updateNavigationState(previousRoute)
            }
        },
        entryProvider = typedEntryProvider
    )

}