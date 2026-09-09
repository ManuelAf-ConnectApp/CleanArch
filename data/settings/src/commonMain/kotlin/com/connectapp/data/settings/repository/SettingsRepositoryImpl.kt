package com.connectapp.data.settings.repository

import com.connectapp.domain.repository.SettingsRepository
import com.liftric.kvault.KVault
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val KEY_DARK_MODE_ENABLED = "dark_mode_enabled"
private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"

/**
 * Minimal shape of the KVault Boolean operations [SettingsRepositoryImpl] actually needs.
 *
 * `com.liftric.kvault.KVault` is an `expect open class` whose constructors are platform-specific,
 * so it cannot be instantiated from commonTest — see AuthLocalDataSource.kt for the same seam
 * used by `:data:auth`. This internal interface lets commonTest substitute an in-memory fake,
 * while production code keeps constructing [SettingsRepositoryImpl] with a real [KVault] via the
 * public constructor below.
 */
internal interface SettingsKeyValueStore {
    fun setBool(key: String, value: Boolean): Boolean
    fun bool(key: String): Boolean?
}

private class KVaultSettingsStore(private val kVault: KVault) : SettingsKeyValueStore {
    override fun setBool(key: String, value: Boolean) = kVault.set(key, value)
    override fun bool(key: String) = kVault.bool(key)
}

/**
 * See specs/007-persist-apply-appearance-notifications/data-model.md. Backed by its own KVault
 * store (`connectapp_settings`), distinct from `:data:auth`'s `connectapp_auth_session` — no
 * data from unrelated domains shares the same store.
 */
class SettingsRepositoryImpl internal constructor(
    private val store: SettingsKeyValueStore,
) : SettingsRepository {

    constructor(kVault: KVault) : this(KVaultSettingsStore(kVault))

    // Seeded once at construction and kept in sync by setDarkModeEnabled — MainViewModel and
    // SettingsViewModel share this single Koin instance, so both observe the same value.
    private val darkModeEnabled = MutableStateFlow(store.bool(KEY_DARK_MODE_ENABLED))

    override fun observeDarkModeEnabled(): Flow<Boolean?> = darkModeEnabled.asStateFlow()

    override suspend fun setDarkModeEnabled(enabled: Boolean) {
        store.setBool(KEY_DARK_MODE_ENABLED, enabled)
        darkModeEnabled.value = enabled
    }

    override suspend fun isNotificationsEnabled(): Boolean? = store.bool(KEY_NOTIFICATIONS_ENABLED)

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        store.setBool(KEY_NOTIFICATIONS_ENABLED, enabled)
    }
}
