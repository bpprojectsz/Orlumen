package com.zdmgold.orlumen.core.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zdmgold.orlumen.core.model.ColorPalette
import com.zdmgold.orlumen.core.model.VisualizerConfig
import com.zdmgold.orlumen.core.model.VisualizerDirection
import com.zdmgold.orlumen.core.repository.ConfigRepository
import com.zdmgold.orlumen.core.repository.PaletteRepository
import com.zdmgold.orlumen.core.repository.Preset
import com.zdmgold.orlumen.designs.visualizer.VisualizerRegistry
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
private const val UNIT_MIN = 0f
private const val UNIT_MAX = 1f

data class VisualizerUiState(
    val draft: VisualizerConfig,
    val active: VisualizerConfig,
    val palettes: List<ColorPalette>,
    val presets: List<Preset<VisualizerConfig>>
) {
    val isDirty: Boolean
        get() = draft != active
}

/** Switching design keeps the common settings but drops the previous design's own settings. */
internal fun VisualizerConfig.withDesign(designId: String): VisualizerConfig =
    if (designId == this.designId) this else copy(designId = designId, extras = emptyMap())

@HiltViewModel
class VisualizerViewModel @Inject constructor(
    private val configRepository: ConfigRepository,
    paletteRepository: PaletteRepository
) : ViewModel() {
    private val draft = MutableStateFlow<VisualizerConfig?>(null)

    /** Null until the saved configuration has been loaded. */
    val uiState: StateFlow<VisualizerUiState?> = combine(
        draft,
        configRepository.activeVisualizerConfig,
        paletteRepository.palettes,
        configRepository.observeVisualizerPresets()
    ) { current, active, palettes, presets ->
        current?.let { VisualizerUiState(it, active, palettes, presets) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    init {
        viewModelScope.launch { draft.value = configRepository.activeVisualizerConfig.first() }
    }

    fun selectDesign(designId: String) {
        if (VisualizerRegistry.byId(designId) != null) edit { it.withDesign(designId) }
    }

    fun setThickness(value: Float) {
        edit { it.copy(thickness = value.coerceIn(VisualizerConfig.MIN_THICKNESS, VisualizerConfig.MAX_THICKNESS)) }
    }

    fun setHeight(value: Float) {
        edit { it.copy(height = value.coerceIn(VisualizerConfig.MIN_HEIGHT, VisualizerConfig.MAX_HEIGHT)) }
    }

    fun setSensitivity(value: Float) {
        edit { it.copy(sensitivity = value.coerceIn(UNIT_MIN, UNIT_MAX)) }
    }

    fun setSmoothing(value: Float) {
        edit { it.copy(smoothing = value.coerceIn(UNIT_MIN, UNIT_MAX)) }
    }

    fun setDirection(direction: VisualizerDirection) {
        edit { it.copy(direction = direction) }
    }

    fun setExtra(key: String, value: Float) {
        edit { it.withExtra(key, value) }
    }

    fun selectPalette(paletteId: String) {
        edit { it.copy(paletteId = paletteId) }
    }

    fun apply() {
        viewModelScope.launch { draft.value?.let { configRepository.setActiveVisualizerConfig(it) } }
    }

    fun resetDraft() {
        viewModelScope.launch { draft.value = configRepository.activeVisualizerConfig.first() }
    }

    fun savePreset(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch { draft.value?.let { configRepository.saveVisualizerPreset(trimmed, it) } }
    }

    fun loadPreset(preset: Preset<VisualizerConfig>) {
        draft.value = preset.config
    }

    fun deletePreset(id: Long) {
        viewModelScope.launch { configRepository.deletePreset(id) }
    }

    private fun edit(transform: (VisualizerConfig) -> VisualizerConfig) {
        draft.update { current -> current?.let(transform) }
    }
}
