package com.connectapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectapp.domain.analytics.AnalyticsReporter
import com.connectapp.domain.provider.NotificationPermissionProvider
import com.connectapp.domain.provider.NotificationPermissionStatus
import com.connectapp.domain.repository.SettingsRepository
import com.connectapp.presentation.navigation.NavigationRoute
import com.connectapp.presentation.navigation.NavigationState
import com.connectapp.presentation.navigation.SnackBarState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val analyticsReporter: AnalyticsReporter,
    private val settingsRepository: SettingsRepository,
    private val notificationPermissionProvider: NotificationPermissionProvider,
) : ViewModel() {
    private val _navState = MutableStateFlow(NavigationState(route = NavigationRoute.SplashRoute))
    val navState: StateFlow<NavigationState> get() = _navState

    private val _snackBarState = MutableStateFlow<SnackBarState?>(null)
    val snackBarState: StateFlow<SnackBarState?> get() = _snackBarState

    // specs/007-persist-apply-appearance-notifications: null = no explicit preference saved yet,
    // App() falls back to isSystemInDarkTheme() in that case (FR-003).
    private val _darkModeEnabled = MutableStateFlow<Boolean?>(null)
    val darkModeEnabled: StateFlow<Boolean?> get() = _darkModeEnabled

    // Drives the bell icon in CustomTopBar (Notifications vs NotificationsOff). Same source of
    // truth as SettingsViewModel.notificationsEnabled: the real OS permission, not the
    // settingsRepository cache.
    private val _notificationsEnabled = MutableStateFlow(false)
    val notificationsEnabled: StateFlow<Boolean> get() = _notificationsEnabled

    init {
        settingsRepository.observeDarkModeEnabled()
            .onEach { enabled -> _darkModeEnabled.update { enabled } }
            .launchIn(viewModelScope)
        refreshNotificationsState()
    }

    fun updateNavigationState(navRoute: NavigationRoute) {
        _navState.update {
            it.copy(route = navRoute)
        }
        // FR-002: breadcrumb so a crash report includes which screen the user was in.
        analyticsReporter.setCurrentScreen(navRoute::class.simpleName ?: "Unknown")
    }

    fun showSnackBar(message: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
        _snackBarState.value = SnackBarState(message, actionLabel, onAction)
    }

    fun dismissSnackBar() {
        _snackBarState.value = null
    }

    /**
     * The Home bell (CustomTopBar's only action, see specs/015-notification-permissions) acts as
     * a toggle, same semantics as SettingsViewModel.onNotificationsToggled: the OS offers no way
     * to revoke an already-granted permission programmatically (FR-006), so tapping it while
     * enabled does NOT turn it off — the icon stays showing enabled and [disabledNoticeMessage] is
     * shown as a snackbar explaining that only the OS Settings screen can actually revoke it.
     * Tapping it while disabled defers to the real OS permission: already granted needs nothing
     * further, deniable triggers the native dialog, permanently denied opens the OS app settings
     * screen — never this app's own Settings screen.
     */
    fun onNotificationBellClicked(disabledNoticeMessage: String) {
        if (_notificationsEnabled.value) {
            showSnackBar(message = disabledNoticeMessage)
            return
        }
        viewModelScope.launch {
            val status = when (val current = notificationPermissionProvider.checkStatus()) {
                NotificationPermissionStatus.GRANTED -> NotificationPermissionStatus.GRANTED
                NotificationPermissionStatus.DENIABLE -> notificationPermissionProvider.requestPermission()
                NotificationPermissionStatus.PERMANENTLY_DENIED -> {
                    notificationPermissionProvider.openAppSettings()
                    current
                }
            }
            applyNotificationStatus(status)
        }
    }

    /**
     * Re-syncs the bell icon with the real OS permission whenever MainScreen comes back to the
     * foreground (e.g. returning from the OS app settings screen opened above), same pattern as
     * SettingsViewModel.refreshNotificationsState (specs/015-notification-permissions US3).
     */
    fun onScreenResumed() {
        refreshNotificationsState()
    }

    private fun refreshNotificationsState() {
        viewModelScope.launch { applyNotificationStatus(notificationPermissionProvider.checkStatus()) }
    }

    private suspend fun applyNotificationStatus(status: NotificationPermissionStatus) {
        val granted = status == NotificationPermissionStatus.GRANTED
        _notificationsEnabled.update { granted }
        settingsRepository.setNotificationsEnabled(granted)
    }

    fun onBack() {
        when (navState.value.route) {
            NavigationRoute.ForgotPasswordRoute, NavigationRoute.RegisterRoute -> {
                updateNavigationState(NavigationRoute.LoginRoute)
            }
            // specs/009-order-detail-and-refresh FR-003: back from the detail returns to the
            // orders list specifically, not the generic Home fallback below.
            is NavigationRoute.OrderDetailRoute -> {
                updateNavigationState(NavigationRoute.OrdersRoute)
            }
            // specs/008-complete-edit-profile-privacy: both are only reachable from Settings,
            // so back returns there specifically, not the generic Home fallback below.
            NavigationRoute.EditProfileRoute, NavigationRoute.PrivacyPolicyRoute -> {
                updateNavigationState(NavigationRoute.SettingsRoute)
            }
           else ->{
               updateNavigationState(NavigationRoute.HomeRoute)
           }
        }
    }
}
