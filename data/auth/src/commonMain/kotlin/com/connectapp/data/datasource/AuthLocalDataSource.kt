package com.connectapp.data.datasource

import com.liftric.kvault.KVault

private const val KEY_AUTH_TOKEN = "auth_token"

interface AuthLocalDataSource {
    fun saveToken(token: String)
    fun getToken(): String?
    fun clearSession()
}

/**
 * Minimal shape of the KVault operations [AuthLocalDataSourceImpl] actually needs.
 *
 * `com.liftric.kvault.KVault` is an `expect open class` whose constructors are platform-specific
 * (Android needs a [android.content.Context], iOS's Keychain-backed variant does not — see
 * KVaultProvider.kt/.android.kt/.ios.kt), so it cannot be instantiated from commonTest. This
 * internal seam lets commonTest substitute an in-memory fake to verify persistence semantics,
 * while production code keeps constructing [AuthLocalDataSourceImpl] with a real [KVault] via the
 * public constructor below — [AuthLocalDataSource]'s public contract is unchanged.
 */
internal interface SecureKeyValueStore {
    fun set(key: String, value: String): Boolean
    fun string(key: String): String?
    fun deleteObject(key: String): Boolean
}

private class KVaultKeyValueStore(private val kVault: KVault) : SecureKeyValueStore {
    override fun set(key: String, value: String) = kVault.set(key, value)
    override fun string(key: String) = kVault.string(key)
    override fun deleteObject(key: String) = kVault.deleteObject(key)
}

/**
 * Persists the session token in OS-backed secure storage via KVault (Android Keystore-backed
 * EncryptedSharedPreferences / iOS Keychain — see contracts/secure-storage-contract.md), instead
 * of the in-memory field this class used to have. The real [KVault] instance is provided
 * per-platform through [createKVault] (see KVaultProvider.kt/.android.kt/.ios.kt), wired via Koin
 * in composeApp's `dataModule`.
 */
class AuthLocalDataSourceImpl internal constructor(
    private val store: SecureKeyValueStore
) : AuthLocalDataSource {

    constructor(kVault: KVault) : this(KVaultKeyValueStore(kVault))

    override fun saveToken(token: String) {
        store.set(KEY_AUTH_TOKEN, token)
    }

    override fun getToken(): String? {
        return store.string(KEY_AUTH_TOKEN)
    }

    override fun clearSession() {
        store.deleteObject(KEY_AUTH_TOKEN)
    }
}
