package com.connectapp.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import com.connectapp.commonresources.main_title
import com.connectapp.commonresources.notifications_disabled_os_notice
import com.connectapp.commonresources.platform
import com.connectapp.presentation.component.CustomBottomBar
import com.connectapp.presentation.component.CustomTopBar
import com.connectapp.presentation.navigation.NavigationScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

// iOS-only: androidx.navigation3's default NavDisplay transitions get hard-cut to zero duration
// when the OS "Reduce Motion" accessibility setting is on (JetBrains YouTrack CMP-10284), which
// makes screen swaps look abrupt/overlapping. A brief full-screen loader masks that jump-cut
// regardless of the device's motion setting. Android's transitions already animate correctly, so
// this stays iOS-only to avoid degrading an already-smooth experience there.
private const val ROUTE_TRANSITION_LOADING_DURATION_MILLIS = 1_000L

// SnackbarHostState.showSnackbar() defaults to SnackbarDuration.Indefinite whenever an
// actionLabel is passed (every snackbar here has one, e.g. Home's "close" action) — so without
// an explicit timed dismissal it would stay on screen until the user taps the action.
private const val SNACKBAR_AUTO_DISMISS_MILLIS = 5_000L

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    entryProvider: (Any) -> NavEntry<out Any>,
) {
    val viewModel: MainViewModel = koinViewModel()
    val navState by viewModel.navState.collectAsStateWithLifecycle()
    val snackBarState by viewModel.snackBarState.collectAsStateWithLifecycle()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val notificationsDisabledNotice = stringResource(notifications_disabled_os_notice)
    val snackBarHostState = remember { SnackbarHostState() }

    // Re-syncs the bell icon with the real OS permission whenever the app comes back to the
    // foreground (e.g. returning from the OS app settings screen), same pattern as SettingsScreen.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onScreenResumed()
    }

    val currentRoute = navState.route
    val isIOSPlatform = remember { platform() == "iOS" }
    var isRouteTransitionLoading by remember { mutableStateOf(false) }

    LaunchedEffect(currentRoute) {
        if (isIOSPlatform) {
            isRouteTransitionLoading = true
            delay(ROUTE_TRANSITION_LOADING_DURATION_MILLIS)
            isRouteTransitionLoading = false
        }
    }

    LaunchedEffect(snackBarState) {
        snackBarState?.let { state ->
            val autoDismissJob = launch {
                delay(SNACKBAR_AUTO_DISMISS_MILLIS)
                snackBarHostState.currentSnackbarData?.dismiss()
            }
            val result = snackBarHostState.showSnackbar(
                message = state.message,
                actionLabel = state.actionLabel,
            )
            autoDismissJob.cancel()
            if (result == SnackbarResult.ActionPerformed) {
                state.onAction?.invoke()
            }
            viewModel.dismissSnackBar()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().imePadding().statusBarsPadding()
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
            topBar = {
                if (currentRoute.showTopBar) {
                    CustomTopBar(
                        showBackButton = currentRoute.showBackButton,
                        title = stringResource(currentRoute.titleRes),
                        notificationsEnabled = notificationsEnabled,
                        onNavigationClick = {
                            viewModel.onBack()
                        },
                        onNotificationClick = {
                            viewModel.onNotificationBellClicked(disabledNoticeMessage = notificationsDisabledNotice)
                        }
                    )
                }
            },
            bottomBar = {
                if (currentRoute.showBottomBar) {
                    CustomBottomBar(
                        currentRoute = currentRoute,
                        onNavigate = { viewModel.updateNavigationState(it) }
                    )
                }
            }
        ) { paddingValues ->
            NavigationScreen(
                modifier = modifier.padding(paddingValues = paddingValues),
                viewModel = viewModel,
                entryProvider = entryProvider
            )
        }

        if (isIOSPlatform) {
            AnimatedVisibility(
                visible = isRouteTransitionLoading,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
