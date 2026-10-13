package com.zdmgold.orlumen.core.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zdmgold.orlumen.core.datastore.PreferencesRepository
import com.zdmgold.orlumen.core.model.AodHostMode
import com.zdmgold.orlumen.core.model.AudioSourceMode
import com.zdmgold.orlumen.core.model.ThemeMode
import com.zdmgold.orlumen.core.model.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val STOP_TIMEOUT_MS = 5_000L

@HiltViewModel
class SettingsViewModel @Inject constructor(private val preferencesRepository: PreferencesRepository) : ViewModel() {
    val preferences: StateFlow<UserPreferences> = preferencesRepository.preferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), UserPreferences())

    fun setThemeMode(mode: ThemeMode) = change { it.copy(themeMode = mode) }

    fun setAudioSourceMode(mode: AudioSourceMode) = change { it.copy(audioSourceMode = mode) }

    fun setAodHostMode(mode: AodHostMode) = change { it.copy(aodHostMode = mode) }

    fun setDimBackground(enabled: Boolean) = change { it.copy(dimBackground = enabled) }

    fun setKeepScreenOn(enabled: Boolean) = change { it.copy(keepScreenOn = enabled) }

    fun setHideOverFullscreen(enabled: Boolean) = change { it.copy(hideOverFullscreen = enabled) }

    fun setAutoApplyAlbumColors(enabled: Boolean) = change { it.copy(autoApplyAlbumColors = enabled) }

    private fun change(transform: (UserPreferences) -> UserPreferences) {
        viewModelScope.launch { preferencesRepository.update(transform) }
    }
}
