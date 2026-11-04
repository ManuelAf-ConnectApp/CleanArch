package com.connectapp.cleanarch

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import cleanarch.composeapp.generated.resources.Res
import cleanarch.composeapp.generated.resources.compose_multiplatform
import com.connectapp.cleanarch.navigation.appEntryProvider
import com.connectapp.commonresources.theme.ConnectAppTheme
import com.connectapp.presentation.MainScreen
import com.connectapp.presentation.MainViewModel
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.context.startKoin

private var koinInitialized = false

/**
 * Initializes the dependency graph once per process, before composition starts. Keeping this out
 * of [App] prevents recompositions from rebuilding global DI state.
 */
fun initKoin() {
    if (!koinInitialized) {
        startKoin { modules(appModules) }
        koinInitialized = true
    }
}

@Composable
fun App() {
    val viewModel: MainViewModel = koinViewModel()
    val darkModeEnabled by viewModel.darkModeEnabled.collectAsState()

    ConnectAppTheme(darkTheme = darkModeEnabled ?: isSystemInDarkTheme()) {
        MainScreen(
            viewModel = viewModel,
            entryProvider = appEntryProvider(modifier = Modifier, viewModel = viewModel)
        )
    }
}

@Preview
@Composable
fun AppPreview() {
    initKoin()
    App()
}
