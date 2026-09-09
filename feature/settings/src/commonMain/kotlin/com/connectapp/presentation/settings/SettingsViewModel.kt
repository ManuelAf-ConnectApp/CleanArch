package com.connectapp.presentation.settings

import androidx.lifecycle.viewModelScope
import com.connectapp.core.mvi.MviViewModel
import com.connectapp.domain.provider.AppVersionProvider
import com.connectapp.domain.provider.NotificationPermissionProvider
import com.connectapp.domain.provider.NotificationPermissionStatus
import com.connectapp.domain.repository.SettingsRepository
import com.connectapp.domain.usecase.LogoutUseCase
import com.connectapp.domain.usecase.ObserveAnalyticsConsentUseCase
import com.connectapp.domain.usecase.ObserveProfileUseCase
import com.connectapp.domain.usecase.SetAnalyticsConsentUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * See specs/007-persist-apply-appearance-notifications and specs/015-notification-permissions.
 * [settingsRepository] backs [SettingsState.darkModeEnabled] (observed reactively, same shared
 * Koin `single` `MainViewModel` observes, so a toggle here is reflected app-wide without
 * restarting). [SettingsState.notificationsEnabled] is now driven by [notificationPermissionProvider]
 * — the real OS-level notification permission is the source of truth, refreshed on init and on
 * every `ScreenResumed` (`ON_RESUME`, see SettingsScreen.kt); `settingsRepository` only keeps a
 * cache of the last known granted state (FR-010), it's never written from user intent alone.
 */
class SettingsViewModel(
    private val logoutUseCase: LogoutUseCase,
    private val observeAnalyticsConsentUseCase: ObserveAnalyticsConsentUseCase,
    private val setAnalyticsConsentUseCase: SetAnalyticsConsentUseCase,
    private val observeProfileUseCase: ObserveProfileUseCase,
    private val settingsRepository: SettingsRepository,
    private val appVersionProvider: AppVersionProvider,
    private val notificationPermissionProvider: NotificationPermissionProvider,
) : MviViewModel<SettingsState, SettingsIntent, SettingsEffect>(SettingsState()) {

    init {
        updateState { it.copy(appVersion = appVersionProvider.versionName) }
        observeAnalyticsConsentUseCase()
            .onEach { granted -> updateState { it.copy(analyticsConsentEnabled = granted) } }
            .launchIn(viewModelScope)
        observeProfileUseCase()
            .onEach { user -> updateState { it.copy(user = user) } }
            .launchIn(viewModelScope)
        settingsRepository.observeDarkModeEnabled()
            .onEach { enabled -> updateState { it.copy(darkModeEnabled = enabled ?: it.darkModeEnabled) } }
            .launchIn(viewModelScope)
        refreshNotificationsState()
    }

    override fun reduce(intent: SettingsIntent) {
        when (intent) {
            SettingsIntent.LogoutClicked -> logout()
            is SettingsIntent.NotificationsToggled -> onNotificationsToggled(intent.enabled)
            is SettingsIntent.DarkModeToggled -> {
                updateState { it.copy(darkModeEnabled = intent.enabled) }
                viewModelScope.launch { settingsRepository.setDarkModeEnabled(intent.enabled) }
            }
            is SettingsIntent.AnalyticsConsentToggled -> {
                setAnalyticsConsentUseCase(intent.enabled)
            }
            SettingsIntent.EditProfileClicked -> {
                emitEffect(SettingsEffect.NavigateToEditProfile)
            }
            SettingsIntent.PrivacyPolicyClicked -> {
                emitEffect(SettingsEffect.NavigateToPrivacyPolicy)
            }
            SettingsIntent.ScreenResumed -> refreshNotificationsState()
        }
    }

    private fun logout() {
        updateState { it.copy(isLoading = true) }
        viewModelScope.launch {
            logoutUseCase()
            updateState { it.copy(isLoading = false) }
            emitEffect(SettingsEffect.NavigateToLogin)
        }
    }

    /**
     * See specs/015-notification-permissions/contracts/notification-permission-provider-contract.md.
     * The OS offers no way to revoke an already-granted permission programmatically (FR-006), so
     * unchecking the switch does NOT turn it off: the toggle snaps back to its current (enabled)
     * state and [SettingsEffect.ShowNotificationsDisabledNotice] explains why — showing it as off
     * would lie about notifications actually being disabled. Enabling defers to the real OS
     * permission: `GRANTED` needs nothing further, `DENIABLE` triggers the native dialog, and
     * `PERMANENTLY_DENIED` leaves the toggle off (openAppSettings() navigation added in US2).
     */
    private fun onNotificationsToggled(enabled: Boolean) {
        if (!enabled) {
            emitEffect(SettingsEffect.ShowNotificationsDisabledNotice)
            return
        }
        viewModelScope.launch {
            val status = when (val current = notificationPermissionProvider.checkStatus()) {
                NotificationPermissionStatus.GRANTED -> NotificationPermissionStatus.GRANTED
                NotificationPermissionStatus.DENIABLE -> notificationPermissionProvider.requestPermission()
                NotificationPermissionStatus.PERMANENTLY_DENIED -> {
                    notificationPermissionProvider.openAppSettings()
                    current // toggle stays off until the next resync (US3, ScreenResumed)
                }
            }
            applyStatus(status)
        }
    }

    private suspend fun applyStatus(status: NotificationPermissionStatus) {
        val granted = status == NotificationPermissionStatus.GRANTED
        updateState { it.copy(notificationsEnabled = granted) }
        settingsRepository.setNotificationsEnabled(granted)
    }

    /**
     * Re-syncs the toggle with the real OS permission — called on init and on every
     * `ScreenResumed`, so changes made outside the app (Ajustes del sistema operativo) are
     * reflected without any manual action (FR-007, US3).
     */
    private fun refreshNotificationsState() {
        viewModelScope.launch { applyStatus(notificationPermissionProvider.checkStatus()) }
    }
}
