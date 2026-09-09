package com.connectapp.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * See specs/007-persist-apply-appearance-notifications/contracts/settings-repository-contract.md.
 * `null` means "no explicit preference saved yet" — callers fall back to the OS-level default
 * (e.g. `isSystemInDarkTheme()`) rather than a hardcoded light/enabled default (FR-003).
 */
interface SettingsRepository {
    fun observeDarkModeEnabled(): Flow<Boolean?>
    suspend fun setDarkModeEnabled(enabled: Boolean)
    suspend fun isNotificationsEnabled(): Boolean?
    suspend fun setNotificationsEnabled(enabled: Boolean)
}
