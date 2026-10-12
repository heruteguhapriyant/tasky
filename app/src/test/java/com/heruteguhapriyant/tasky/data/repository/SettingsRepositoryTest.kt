package com.heruteguhapriyant.tasky.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import com.heruteguhapriyant.tasky.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SettingsRepositoryTest {

    private lateinit var fakeDataStore: FakeDataStore
    private lateinit var repository: SettingsRepositoryImpl

    @Before
    fun setUp() {
        fakeDataStore = FakeDataStore()
        repository = SettingsRepositoryImpl(fakeDataStore)
    }

    // ── ThemeMode tests ──────────────────────────────────────────────────────

    @Test
    fun getThemeMode_withNoPreference_returnsSystemDefault() = runBlocking {
        val result = repository.getThemeMode().first()
        assertEquals(ThemeMode.SYSTEM, result)
    }

    @Test
    fun setThemeMode_toLight_persistsAndReturnsLight() = runBlocking {
        repository.setThemeMode(ThemeMode.LIGHT)
        val result = repository.getThemeMode().first()
        assertEquals(ThemeMode.LIGHT, result)
    }

    @Test
    fun setThemeMode_toDark_persistsAndReturnsDark() = runBlocking {
        repository.setThemeMode(ThemeMode.DARK)
        val result = repository.getThemeMode().first()
        assertEquals(ThemeMode.DARK, result)
    }

    @Test
    fun setThemeMode_multipleTimes_returnsLastValue() = runBlocking {
        repository.setThemeMode(ThemeMode.LIGHT)
        repository.setThemeMode(ThemeMode.DARK)
        repository.setThemeMode(ThemeMode.SYSTEM)
        val result = repository.getThemeMode().first()
        assertEquals(ThemeMode.SYSTEM, result)
    }

    @Test
    fun getThemeMode_withInvalidStoredValue_returnsSystemFallback() = runBlocking {
        // Simpan nilai invalid secara langsung ke fake datastore
        fakeDataStore.setRaw(SettingsRepositoryImpl.KEY_THEME_MODE, "INVALID_VALUE")
        val result = repository.getThemeMode().first()
        assertEquals(ThemeMode.SYSTEM, result)
    }

    // ── Onboarding tests ─────────────────────────────────────────────────────

    @Test
    fun isOnboardingCompleted_withNoPreference_returnsFalse() = runBlocking {
        val result = repository.isOnboardingCompleted().first()
        assertFalse(result)
    }

    @Test
    fun setOnboardingCompleted_toTrue_persistsAndReturnsTrue() = runBlocking {
        repository.setOnboardingCompleted(true)
        val result = repository.isOnboardingCompleted().first()
        assertTrue(result)
    }

    @Test
    fun setOnboardingCompleted_toFalseAfterTrue_returnsFalse() = runBlocking {
        repository.setOnboardingCompleted(true)
        repository.setOnboardingCompleted(false)
        val result = repository.isOnboardingCompleted().first()
        assertFalse(result)
    }

    // ── Independence test ─────────────────────────────────────────────────────

    @Test
    fun settingsAreIndependent_changingThemeDoesNotAffectOnboarding() = runBlocking {
        repository.setOnboardingCompleted(true)
        repository.setThemeMode(ThemeMode.DARK)

        // Onboarding harus tetap true setelah theme diubah
        val onboarding = repository.isOnboardingCompleted().first()
        val theme = repository.getThemeMode().first()

        assertTrue(onboarding)
        assertEquals(ThemeMode.DARK, theme)
    }
}

// ─── Fake DataStore ────────────────────────────────────────────────────────────

/**
 * Implementasi in-memory [DataStore] untuk keperluan unit test.
 * Mendukung operasi [edit] secara sinkron dan memancarkan perubahan via [Flow].
 */
class FakeDataStore : DataStore<Preferences> {

    private val prefsMap = mutableMapOf<Preferences.Key<*>, Any?>()
    private val stateFlow = MutableStateFlow<Preferences>(buildPreferences())

    private fun buildPreferences(): Preferences {
        val mutable = mutablePreferencesOf()
        @Suppress("UNCHECKED_CAST")
        prefsMap.forEach { (key, value) ->
            if (value != null) {
                mutable[key as Preferences.Key<Any>] = value
            }
        }
        return mutable
    }

    /** Helper untuk menulis nilai raw ke dalam fake datastore (bypass [edit]). */
    fun <T> setRaw(key: Preferences.Key<T>, value: T) {
        prefsMap[key] = value
        stateFlow.value = buildPreferences()
    }

    override val data: Flow<Preferences>
        get() = stateFlow

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        val current = stateFlow.value
        val updated = transform(current)
        // Salin data baru ke prefsMap
        prefsMap.clear()
        (updated as? MutablePreferences)?.asMap()?.forEach { (key, value) ->
            @Suppress("UNCHECKED_CAST")
            prefsMap[key as Preferences.Key<Any>] = value
        }
        stateFlow.value = buildPreferences()
        return stateFlow.value
    }
}
