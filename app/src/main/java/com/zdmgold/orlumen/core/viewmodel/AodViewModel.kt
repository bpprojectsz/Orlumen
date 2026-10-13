package com.zdmgold.orlumen.core.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zdmgold.orlumen.core.model.AodConfig
import com.zdmgold.orlumen.core.model.ColorPalette
import com.zdmgold.orlumen.core.repository.ConfigRepository
import com.zdmgold.orlumen.core.repository.PaletteRepository
import com.zdmgold.orlumen.core.repository.Preset
import com.zdmgold.orlumen.designs.aod.AodRegistry
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val STOP_TIMEOUT_MS = 5_000L

data class AodUiEditorState(
    val draft: AodConfig,
    val active: AodConfig,
    val palettes: List<ColorPalette>,
    val presets: List<Preset<AodConfig>>
) {
    val isDirty: Boolean
        get() = draft != active
}

/** Switching design keeps the common settings but drops the previous design's own settings. */
internal fun AodConfig.withDesign(designId: String): AodConfig =
    if (designId == this.designId) this else copy(designId = designId, extras = emptyMap(), choices = emptyMap())

@HiltViewModel
class AodViewModel @Inject constructor(
    private val configRepository: ConfigRepository,
    paletteRepository: PaletteRepository
) : ViewModel() {
    private val draft = MutableStateFlow<AodConfig?>(null)

    /** Null until the saved configuration has been loaded. */
    val uiState: StateFlow<AodUiEditorState?> = combine(
        draft,
        configRepository.activeAodConfig,
        paletteRepository.palettes,
        configRepository.observeAodPresets()
    ) { current, active, palettes, presets ->
        current?.let { AodUiEditorState(it, active, palettes, presets) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    init {
        viewModelScope.launch { draft.value = configRepository.activeAodConfig.first() }
    }

    fun selectDesign(designId: String) {
        if (AodRegistry.byId(designId) != null) edit { it.withDesign(designId) }
    }

    fun setClockWeight(value: Int) {
        edit { it.copy(clockWeight = value.coerceIn(AodConfig.MIN_CLOCK_WEIGHT, AodConfig.MAX_CLOCK_WEIGHT)) }
    }

    fun setSymbol(symbolId: String?) {
        edit { it.copy(symbolId = symbolId) }
    }

    fun setSymbolScale(value: Float) {
        edit { it.copy(symbolScale = value.coerceIn(AodConfig.MIN_SYMBOL_SCALE, AodConfig.MAX_SYMBOL_SCALE)) }
    }

    fun setShowMusicCard(show: Boolean) {
        edit { it.copy(showMusicCard = show) }
    }

    fun setShowStatusRow(show: Boolean) {
        edit { it.copy(showStatusRow = show) }
    }

    fun setBackground(uri: String?) {
        edit { it.copy(backgroundUri = uri) }
    }

    fun setExtra(key: String, value: Float) {
        edit { it.withExtra(key, value) }
    }

    fun setChoice(key: String, optionId: String) {
        edit { it.withChoice(key, optionId) }
    }

    fun selectPalette(paletteId: String) {
        edit { it.copy(paletteId = paletteId) }
    }

    fun apply() {
        viewModelScope.launch { draft.value?.let { configRepository.setActiveAodConfig(it) } }
    }

    fun resetDraft() {
        viewModelScope.launch { draft.value = configRepository.activeAodConfig.first() }
    }

    fun savePreset(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch { draft.value?.let { configRepository.saveAodPreset(trimmed, it) } }
    }

    fun loadPreset(preset: Preset<AodConfig>) {
        draft.value = preset.config
    }

    fun deletePreset(id: Long) {
        viewModelScope.launch { configRepository.deletePreset(id) }
    }

    private fun edit(transform: (AodConfig) -> AodConfig) {
        draft.update { current -> current?.let(transform) }
    }
}
