package com.connectapp.data.settings.di

import com.connectapp.core.storage.createKVault
import com.connectapp.data.settings.repository.SettingsRepositoryImpl
import com.connectapp.domain.repository.SettingsRepository
import org.koin.dsl.module

/** Store name passed to `:core:storage`'s createKVault() for appearance/notifications prefs. */
private const val SETTINGS_STORE_NAME = "connectapp_settings"

/**
 * specs/003-decentralize-domain-data-di pattern (see :data:auth/:data:orders): :data:settings
 * owns its own Koin module instead of composeApp constructing SettingsRepositoryImpl directly.
 * Registered as a `single` (not `factory`) — MainViewModel and SettingsViewModel must observe
 * the same [SettingsRepository] instance for a toggle in Settings to be reflected app-wide
 * without restarting (specs/007-persist-apply-appearance-notifications).
 */
val settingsDataModule = module {
    single<SettingsRepository> { SettingsRepositoryImpl(createKVault(name = SETTINGS_STORE_NAME)) }
}
