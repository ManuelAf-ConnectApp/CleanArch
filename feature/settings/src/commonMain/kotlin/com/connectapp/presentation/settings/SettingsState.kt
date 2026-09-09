package com.connectapp.presentation.settings

import com.connectapp.domain.model.User

data class SettingsState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val notificationsEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val analyticsConsentEnabled: Boolean = false,
    val appVersion: String = ""
) {
    val userName: String get() = user?.let { "${it.firstName} ${it.lastName}" }.orEmpty()
    val userEmail: String get() = user?.email.orEmpty()
}
