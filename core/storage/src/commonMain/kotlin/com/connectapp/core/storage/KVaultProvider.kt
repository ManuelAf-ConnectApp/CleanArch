package com.connectapp.core.storage

import com.liftric.kvault.KVault

/**
 * Creates the platform-backed [KVault] instance for secure key-value storage.
 *
 * [name] namespaces the underlying store — the Android Keystore-backed file name, or the iOS
 * Keychain service name — so callers should pass a value that's stable across app launches, and
 * distinct per logical store if more than one is ever needed.
 */
expect fun createKVault(name: String): KVault
