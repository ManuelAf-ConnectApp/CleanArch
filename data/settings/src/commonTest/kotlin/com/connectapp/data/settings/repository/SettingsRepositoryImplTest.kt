package com.connectapp.data.settings.repository

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * In-memory fake for [SettingsKeyValueStore], the seam [SettingsRepositoryImpl] uses instead of
 * a real `KVault` (which cannot be constructed in commonTest — see SettingsRepositoryImpl.kt).
 * Values live in [backingStore], not on the fake instance itself, so two fakes constructed over
 * the same [backingStore] behave like two [SettingsRepositoryImpl] instances backed by the same
 * underlying storage (e.g. across an app restart).
 */
private class FakeSettingsKeyValueStore(
    private val backingStore: MutableMap<String, Boolean> = mutableMapOf(),
) : SettingsKeyValueStore {
    override fun setBool(key: String, value: Boolean): Boolean {
        backingStore[key] = value
        return true
    }

    override fun bool(key: String): Boolean? = backingStore[key]
}

class SettingsRepositoryImplTest {

    @Test
    fun `observeDarkModeEnabled starts null on fresh storage where nothing was ever saved`() = runTest {
        val repository = SettingsRepositoryImpl(FakeSettingsKeyValueStore())

        assertNull(repository.observeDarkModeEnabled().first())
    }

    @Test
    fun `setDarkModeEnabled persists so a new instance backed by the same storage still returns it`() = runTest {
        val sharedBackingStore = mutableMapOf<String, Boolean>()
        val firstInstance = SettingsRepositoryImpl(FakeSettingsKeyValueStore(sharedBackingStore))

        firstInstance.setDarkModeEnabled(true)

        val secondInstance = SettingsRepositoryImpl(FakeSettingsKeyValueStore(sharedBackingStore))
        assertEquals(true, secondInstance.observeDarkModeEnabled().first())
    }

    @Test
    fun `setDarkModeEnabled updates the observable flow immediately`() = runTest {
        val repository = SettingsRepositoryImpl(FakeSettingsKeyValueStore())

        repository.setDarkModeEnabled(true)

        assertEquals(true, repository.observeDarkModeEnabled().first())
    }

    @Test
    fun `isNotificationsEnabled returns null on fresh storage where nothing was ever saved`() = runTest {
        val repository = SettingsRepositoryImpl(FakeSettingsKeyValueStore())

        assertNull(repository.isNotificationsEnabled())
    }

    @Test
    fun `setNotificationsEnabled persists so a new instance backed by the same storage still returns it`() = runTest {
        val sharedBackingStore = mutableMapOf<String, Boolean>()
        val firstInstance = SettingsRepositoryImpl(FakeSettingsKeyValueStore(sharedBackingStore))

        firstInstance.setNotificationsEnabled(false)

        val secondInstance = SettingsRepositoryImpl(FakeSettingsKeyValueStore(sharedBackingStore))
        assertEquals(false, secondInstance.isNotificationsEnabled())
    }

    @Test
    fun `dark mode and notifications preferences are independent`() = runTest {
        val repository = SettingsRepositoryImpl(FakeSettingsKeyValueStore())

        repository.setDarkModeEnabled(true)

        assertNull(repository.isNotificationsEnabled())
    }
}
