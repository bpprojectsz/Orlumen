package com.zdmgold.orlumen.core.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zdmgold.orlumen.core.datastore.PreferencesRepository
import com.zdmgold.orlumen.core.model.ColorPalette
import com.zdmgold.orlumen.core.model.StockPalettes
import com.zdmgold.orlumen.core.repository.ConfigRepository
import com.zdmgold.orlumen.core.repository.NowPlayingRepository
import com.zdmgold.orlumen.core.repository.PaletteRepository
import com.zdmgold.orlumen.platform.media.ALBUM_PALETTE_ID
import com.zdmgold.orlumen.platform.media.WALLPAPER_PALETTE_ID
import com.zdmgold.orlumen.platform.media.WallpaperColorsProvider
import com.zdmgold.orlumen.platform.media.wallpaperPalette
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val STOP_TIMEOUT_MS = 5_000L

data class PaletteUiState(
    val stock: List<ColorPalette>,
    val custom: List<ColorPalette>,
    val album: ColorPalette?,
    val wallpaper: ColorPalette?,
    val autoApplyAlbumColors: Boolean,
    val visualizerPaletteId: String,
    val aodPaletteId: String
)

/** Album and wallpaper palettes are computed on the fly, so they must be saved before they can be selected. */
internal fun needsSaving(palette: ColorPalette): Boolean =
    palette.id == ALBUM_PALETTE_ID || palette.id == WALLPAPER_PALETTE_ID

@HiltViewModel
class PaletteViewModel @Inject constructor(
    private val paletteRepository: PaletteRepository,
    private val configRepository: ConfigRepository,
    private val preferencesRepository: PreferencesRepository,
    nowPlayingRepository: NowPlayingRepository,
    private val wallpaperColorsProvider: WallpaperColorsProvider
) : ViewModel() {
    private val wallpaper = MutableStateFlow<ColorPalette?>(null)

    private val library = combine(
        paletteRepository.customPalettes,
        nowPlayingRepository.albumPalette,
        wallpaper
    ) { custom, album, wallpaperColors -> Triple(custom, album, wallpaperColors) }

    val uiState: StateFlow<PaletteUiState?> = combine(
        library,
        preferencesRepository.preferences,
        configRepository.activeVisualizerConfig,
        configRepository.activeAodConfig
    ) { (custom, album, wallpaperColors), prefs, visualizer, aod ->
        PaletteUiState(
            stock = StockPalettes.all,
            custom = custom,
            album = album,
            wallpaper = wallpaperColors,
            autoApplyAlbumColors = prefs.autoApplyAlbumColors,
            visualizerPaletteId = visualizer.paletteId,
            aodPaletteId = aod.paletteId
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    init {
        refreshWallpaper()
    }

    fun refreshWallpaper() {
        viewModelScope.launch {
            wallpaper.value = withContext(Dispatchers.IO) { wallpaperPalette(wallpaperColorsProvider.colors()) }
        }
    }

    fun applyToVisualizer(palette: ColorPalette) {
        viewModelScope.launch {
            val saved = ensureSaved(palette)
            val current = configRepository.activeVisualizerConfig.first()
            configRepository.setActiveVisualizerConfig(current.copy(paletteId = saved.id))
        }
    }

    fun applyToAod(palette: ColorPalette) {
        viewModelScope.launch {
            val saved = ensureSaved(palette)
            val current = configRepository.activeAodConfig.first()
            configRepository.setActiveAodConfig(current.copy(paletteId = saved.id))
        }
    }

    fun setAutoApplyAlbumColors(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.update { it.copy(autoApplyAlbumColors = enabled) } }
    }

    fun saveCustomPalette(name: String, colorsArgb: List<Int>) {
        if (name.isBlank() || colorsArgb.size !in ColorPalette.MIN_COLORS..ColorPalette.MAX_COLORS) return
        viewModelScope.launch { paletteRepository.saveCustomPalette(name.trim(), colorsArgb) }
    }

    fun deleteCustomPalette(id: String) {
        viewModelScope.launch { paletteRepository.deleteCustomPalette(id) }
    }

    private suspend fun ensureSaved(palette: ColorPalette): ColorPalette = if (needsSaving(palette)) {
        paletteRepository.saveCustomPalette(palette.name, palette.colorsArgb)
    } else {
        palette
    }
}
