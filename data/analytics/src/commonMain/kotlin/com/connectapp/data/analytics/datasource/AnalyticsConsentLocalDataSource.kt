package com.connectapp.data.analytics.datasource

import com.liftric.kvault.KVault

private const val KEY_ANALYTICS_CONSENT = "analytics_consent_granted"

interface AnalyticsConsentLocalDataSource {
    fun isConsentGranted(): Boolean
    fun setConsentGranted(granted: Boolean)
}

/**
 * Minimal shape of the KVault operations [AnalyticsConsentLocalDataSourceImpl] actually needs —
 * same testability seam as data:auth's AuthLocalDataSource (`SecureKeyValueStore`): `KVault`'s
 * constructors are platform-specific, so this internal interface lets commonTest substitute an
 * in-memory fake without touching the public [AnalyticsConsentLocalDataSource] contract.
 */
internal interface BooleanKeyValueStore {
    fun bool(key: String): Boolean?
    fun set(key: String, value: Boolean): Boolean
}

private class KVaultBooleanStore(private val kVault: KVault) : BooleanKeyValueStore {
    override fun bool(key: String) = kVault.bool(key)
    override fun set(key: String, value: Boolean) = kVault.set(key, value)
}

/** Defaults to `false` (opt-in only — FR-012) when no value has ever been persisted. */
class AnalyticsConsentLocalDataSourceImpl internal constructor(
    private val store: BooleanKeyValueStore
) : AnalyticsConsentLocalDataSource {

    constructor(kVault: KVault) : this(KVaultBooleanStore(kVault))

    override fun isConsentGranted(): Boolean = store.bool(KEY_ANALYTICS_CONSENT) ?: false

    override fun setConsentGranted(granted: Boolean) {
        store.set(KEY_ANALYTICS_CONSENT, granted)
    }
}
