package com.connectapp.presentation.settings

sealed interface SettingsEffect {
    data object NavigateToLogin : SettingsEffect
    data object NavigateToEditProfile : SettingsEffect
    data object NavigateToPrivacyPolicy : SettingsEffect

    /**
     * See specs/015-notification-permissions FR-006: disabling the toggle can never revoke the
     * real OS permission, so this warns the user that the OS Settings screen is the only way to
     * do that.
     */
    data object ShowNotificationsDisabledNotice : SettingsEffect
}
