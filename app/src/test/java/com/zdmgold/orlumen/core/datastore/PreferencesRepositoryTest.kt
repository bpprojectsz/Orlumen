package com.zdmgold.orlumen.core.datastore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.zdmgold.orlumen.core.model.AodHostMode
import com.zdmgold.orlumen.core.model.AudioSourceMode
import com.zdmgold.orlumen.core.model.ThemeMode
import com.zdmgold.orlumen.core.model.UserPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class PreferencesRepositoryTest {
    @get:Rule
    val folder = TemporaryFolder()

    private fun repository(scope: TestScope): PreferencesRepository {
        val store = PreferenceDataStoreFactory.create(scope = scope.backgroundScope) {
            folder.newFile("prefs.preferences_pb")
        }
        return PreferencesRepository(store)
    }

    @Test
    fun emptyStoreYieldsDefaults() = runTest(UnconfinedTestDispatcher()) {
        assertEquals(UserPreferences(), repository(this).preferences.first())
    }

    @Test
    fun settersPersistIndividually() = runTest(UnconfinedTestDispatcher()) {
        val repo = repository(this)
        repo.setThemeMode(ThemeMode.LIGHT)
        repo.setAudioSourceMode(AudioSourceMode.PLAYBACK_CAPTURE)
        repo.setAodHostMode(AodHostMode.SCREENSAVER)
        repo.setProUser(true)
        repo.setOnboardingCompleted(true)
        val saved = repo.preferences.first()
        assertEquals(ThemeMode.LIGHT, saved.themeMode)
        assertEquals(AudioSourceMode.PLAYBACK_CAPTURE, saved.audioSourceMode)
        assertEquals(AodHostMode.SCREENSAVER, saved.aodHostMode)
        assertTrue(saved.isProUser)
        assertTrue(saved.onboardingCompleted)
    }

    @Test
    fun updateRoundTripsSets() = runTest(UnconfinedTestDispatcher()) {
        val repo = repository(this)
        repo.update { it.copy(visualizerAppPackages = setOf("a.b", "c.d"), dimBackground = true) }
        val saved = repo.preferences.first()
        assertEquals(setOf("a.b", "c.d"), saved.visualizerAppPackages)
        assertTrue(saved.dimBackground)
        assertEquals(emptySet<String>(), saved.musicSourcePackages)
    }
}
