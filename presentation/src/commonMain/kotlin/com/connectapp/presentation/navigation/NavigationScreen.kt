package com.connectapp.presentation.navigation

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
    val viewModelBackStack by viewModel.backStack.collectAsState()

    LaunchedEffect(viewModelBackStack) {
        backStack.clear()
        backStack.addAll(viewModelBackStack)
    }

    @Suppress("UNCHECKED_CAST")
    val typedEntryProvider = entryProvider as (Any) -> NavEntry<Any>

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = viewModel::onBack,
        entryProvider = typedEntryProvider
    )

}
