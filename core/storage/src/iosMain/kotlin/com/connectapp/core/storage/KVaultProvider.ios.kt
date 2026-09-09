package com.connectapp.core.storage

import com.liftric.kvault.KVault

actual fun createKVault(name: String): KVault = KVault(serviceName = name)
