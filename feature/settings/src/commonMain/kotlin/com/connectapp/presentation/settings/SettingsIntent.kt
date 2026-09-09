package com.connectapp.presentation.settings

sealed interface SettingsIntent {
    data object LogoutClicked : SettingsIntent
    data class NotificationsToggled(val enabled: Boolean) : SettingsIntent
    data class DarkModeToggled(val enabled: Boolean) : SettingsIntent
    data class AnalyticsConsentToggled(val enabled: Boolean) : SettingsIntent
    data object EditProfileClicked : SettingsIntent
    data object PrivacyPolicyClicked : SettingsIntent

    /** See specs/015-notification-permissions US3 — dispatched on `ON_RESUME` from SettingsScreen. */
    data object ScreenResumed : SettingsIntent
}
