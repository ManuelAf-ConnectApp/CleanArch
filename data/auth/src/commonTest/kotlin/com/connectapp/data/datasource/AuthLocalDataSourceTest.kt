package com.connectapp.data.datasource

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * In-memory fake for [SecureKeyValueStore], the minimal seam [AuthLocalDataSourceImpl] uses
 * instead of a real `KVault` (which cannot be constructed in commonTest — see
 * AuthLocalDataSource.kt). Values live in [backingStore], not on the fake instance itself, so two
 * fakes constructed over the same [backingStore] behave like two `AuthLocalDataSourceImpl`
 * instances backed by the same underlying secure storage (e.g. across an app restart).
 */
private class FakeSecureKeyValueStore(
    private val backingStore: MutableMap<String, String> = mutableMapOf()
) : SecureKeyValueStore {

    override fun set(key: String, value: String): Boolean {
        backingStore[key] = value
        return true
    }

    override fun string(key: String): String? = backingStore[key]

    override fun deleteObject(key: String): Boolean {
        val existed = backingStore.remove(key) != null
        return existed
    }
}

class AuthLocalDataSourceTest {

    // contracts/secure-storage-contract.md #1 (FR-002, SC-002): saveToken must persist in the
    // underlying storage, not in an in-memory field on the AuthLocalDataSourceImpl instance —
    // this is the exact bug the old `private var cachedToken` implementation had, since that
    // token would NOT survive constructing a new instance.
    @Test
    fun `saveToken persists so a new instance backed by the same storage still returns it`() {
        val sharedBackingStore = mutableMapOf<String, String>()
        val firstInstance = AuthLocalDataSourceImpl(FakeSecureKeyValueStore(sharedBackingStore))

        firstInstance.saveToken("abc")

        val secondInstance = AuthLocalDataSourceImpl(FakeSecureKeyValueStore(sharedBackingStore))
        assertEquals("abc", secondInstance.getToken())
    }

    // contracts/secure-storage-contract.md #2 (FR-005, SC-004): getToken() after clearSession()
    // must return null consistently, with no residue.
    @Test
    fun `clearSession leaves getToken null on the same instance`() {
        val sut = AuthLocalDataSourceImpl(FakeSecureKeyValueStore())
        sut.saveToken("abc")

        sut.clearSession()

        assertNull(sut.getToken())
    }

    @Test
    fun `clearSession leaves getToken null on a new instance sharing the same storage`() {
        val sharedBackingStore = mutableMapOf<String, String>()
        val firstInstance = AuthLocalDataSourceImpl(FakeSecureKeyValueStore(sharedBackingStore))
        firstInstance.saveToken("abc")

        firstInstance.clearSession()

        val secondInstance = AuthLocalDataSourceImpl(FakeSecureKeyValueStore(sharedBackingStore))
        assertNull(secondInstance.getToken())
    }

    @Test
    fun `getToken returns null on fresh storage where nothing was ever saved`() {
        val sut = AuthLocalDataSourceImpl(FakeSecureKeyValueStore())

        assertNull(sut.getToken())
    }
}
