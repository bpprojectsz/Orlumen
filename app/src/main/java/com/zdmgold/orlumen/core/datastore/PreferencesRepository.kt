package com.zdmgold.orlumen.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.zdmgold.orlumen.core.model.AodHostMode
import com.zdmgold.orlumen.core.model.AudioSourceMode
import com.zdmgold.orlumen.core.model.ThemeMode
import com.zdmgold.orlumen.core.model.UserPreferences
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

internal inline fun <reified T : Enum<T>> enumOrDefault(name: String?, default: T): T = enumValues<T>().firstOrNull {
    it.name == name
} ?: default

private object Keys {
    val themeMode = stringPreferencesKey("theme_mode")
    val audioSourceMode = stringPreferencesKey("audio_source_mode")
    val aodHostMode = stringPreferencesKey("aod_host_mode")
    val aodEnabled = booleanPreferencesKey("aod_enabled")
    val isProUser = booleanPreferencesKey("is_pro_user")
    val dimBackground = booleanPreferencesKey("dim_background")
    val keepScreenOn = booleanPreferencesKey("keep_screen_on")
    val hideOverFullscreen = booleanPreferencesKey("hide_over_fullscreen")
    val autoApplyAlbumColors = booleanPreferencesKey("auto_apply_album_colors")
    val onboardingCompleted = booleanPreferencesKey("onboarding_completed")
    val visualizerAppPackages = stringSetPreferencesKey("visualizer_app_packages")
    val musicSourcePackages = stringSetPreferencesKey("music_source_packages")
}

@Singleton
class PreferencesRepository @Inject constructor(private val dataStore: DataStore<Preferences>) {
    val preferences: Flow<UserPreferences> = dataStore.data
        .catch { failure ->
            if (failure is IOException) emit(emptyPreferences()) else throw failure
        }
        .map { it.toUserPreferences() }

    suspend fun update(transform: (UserPreferences) -> UserPreferences) {
        dataStore.edit { stored -> stored.write(transform(stored.toUserPreferences())) }
    }

    suspend fun setThemeMode(mode: ThemeMode) = update { it.copy(themeMode = mode) }

    suspend fun setAudioSourceMode(mode: AudioSourceMode) = update { it.copy(audioSourceMode = mode) }

    suspend fun setAodHostMode(mode: AodHostMode) = update { it.copy(aodHostMode = mode) }

    suspend fun setAodEnabled(enabled: Boolean) = update { it.copy(aodEnabled = enabled) }

    suspend fun setProUser(isPro: Boolean) = update { it.copy(isProUser = isPro) }

    suspend fun setOnboardingCompleted(completed: Boolean) = update { it.copy(onboardingCompleted = completed) }
}

private fun Preferences.toUserPreferences(): UserPreferences {
    val defaults = UserPreferences()
    return UserPreferences(
        themeMode = enumOrDefault(this[Keys.themeMode], defaults.themeMode),
        audioSourceMode = enumOrDefault(this[Keys.audioSourceMode], defaults.audioSourceMode),
        aodHostMode = enumOrDefault(this[Keys.aodHostMode], defaults.aodHostMode),
        aodEnabled = this[Keys.aodEnabled] ?: defaults.aodEnabled,
        isProUser = this[Keys.isProUser] ?: defaults.isProUser,
        dimBackground = this[Keys.dimBackground] ?: defaults.dimBackground,
        keepScreenOn = this[Keys.keepScreenOn] ?: defaults.keepScreenOn,
        hideOverFullscreen = this[Keys.hideOverFullscreen] ?: defaults.hideOverFullscreen,
        autoApplyAlbumColors = this[Keys.autoApplyAlbumColors] ?: defaults.autoApplyAlbumColors,
        onboardingCompleted = this[Keys.onboardingCompleted] ?: defaults.onboardingCompleted,
        visualizerAppPackages = this[Keys.visualizerAppPackages] ?: defaults.visualizerAppPackages,
        musicSourcePackages = this[Keys.musicSourcePackages] ?: defaults.musicSourcePackages
    )
}

private fun MutablePreferences.write(value: UserPreferences) {
    this[Keys.themeMode] = value.themeMode.name
    this[Keys.audioSourceMode] = value.audioSourceMode.name
    this[Keys.aodHostMode] = value.aodHostMode.name
    this[Keys.aodEnabled] = value.aodEnabled
    this[Keys.isProUser] = value.isProUser
    this[Keys.dimBackground] = value.dimBackground
    this[Keys.keepScreenOn] = value.keepScreenOn
    this[Keys.hideOverFullscreen] = value.hideOverFullscreen
    this[Keys.autoApplyAlbumColors] = value.autoApplyAlbumColors
    this[Keys.onboardingCompleted] = value.onboardingCompleted
    this[Keys.visualizerAppPackages] = value.visualizerAppPackages
    this[Keys.musicSourcePackages] = value.musicSourcePackages
}
