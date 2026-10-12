package com.zdmgold.orlumen.core.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.zdmgold.orlumen.core.database.ConfigDao
import com.zdmgold.orlumen.core.database.KeyValueCodec
import com.zdmgold.orlumen.core.database.PresetEntity
import com.zdmgold.orlumen.core.datastore.enumOrDefault
import com.zdmgold.orlumen.core.model.AodConfig
import com.zdmgold.orlumen.core.model.DesignKind
import com.zdmgold.orlumen.core.model.VisualizerConfig
import com.zdmgold.orlumen.core.model.VisualizerDirection
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

data class Preset<T>(val id: Long, val name: String, val config: T)

private object Keys {
    val vizDesign = stringPreferencesKey("viz_design")
    val vizPalette = stringPreferencesKey("viz_palette")
    val vizNumbers = stringPreferencesKey("viz_numbers")
    val vizTexts = stringPreferencesKey("viz_texts")
    val aodDesign = stringPreferencesKey("aod_design")
    val aodPalette = stringPreferencesKey("aod_palette")
    val aodNumbers = stringPreferencesKey("aod_numbers")
    val aodTexts = stringPreferencesKey("aod_texts")
}

private const val EXTRA_PREFIX = "x."
private const val CHOICE_PREFIX = "c."
private const val KEY_THICKNESS = "thickness"
private const val KEY_HEIGHT = "height"
private const val KEY_SENSITIVITY = "sensitivity"
private const val KEY_SMOOTHING = "smoothing"
private const val KEY_DIRECTION = "direction"
private const val KEY_CLOCK_WEIGHT = "clockWeight"
private const val KEY_SYMBOL_SCALE = "symbolScale"
private const val KEY_SYMBOL_ID = "symbolId"
private const val KEY_BACKGROUND_URI = "backgroundUri"
private const val KEY_SHOW_MUSIC = "showMusicCard"
private const val KEY_SHOW_STATUS = "showStatusRow"

@Singleton
class ConfigRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val configDao: ConfigDao
) {
    private val stored: Flow<Preferences> = dataStore.data.catch { failure ->
        if (failure is IOException) emit(emptyPreferences()) else throw failure
    }

    val activeVisualizerConfig: Flow<VisualizerConfig> = stored.map { prefs ->
        visualizerFrom(
            designId = prefs[Keys.vizDesign],
            paletteId = prefs[Keys.vizPalette],
            numbers = KeyValueCodec.decodeFloats(prefs[Keys.vizNumbers].orEmpty()),
            texts = KeyValueCodec.decodeStrings(prefs[Keys.vizTexts].orEmpty())
        ) ?: VisualizerConfig.defaults()
    }

    val activeAodConfig: Flow<AodConfig> = stored.map { prefs ->
        aodFrom(
            designId = prefs[Keys.aodDesign],
            paletteId = prefs[Keys.aodPalette],
            numbers = KeyValueCodec.decodeFloats(prefs[Keys.aodNumbers].orEmpty()),
            texts = KeyValueCodec.decodeStrings(prefs[Keys.aodTexts].orEmpty())
        ) ?: AodConfig.defaults()
    }

    suspend fun setActiveVisualizerConfig(config: VisualizerConfig) {
        dataStore.edit { prefs ->
            prefs[Keys.vizDesign] = config.designId
            prefs[Keys.vizPalette] = config.paletteId
            prefs[Keys.vizNumbers] = KeyValueCodec.encodeFloats(config.toNumbers())
            prefs[Keys.vizTexts] = KeyValueCodec.encodeStrings(config.toTexts())
        }
    }

    suspend fun setActiveAodConfig(config: AodConfig) {
        dataStore.edit { prefs ->
            prefs[Keys.aodDesign] = config.designId
            prefs[Keys.aodPalette] = config.paletteId
            prefs[Keys.aodNumbers] = KeyValueCodec.encodeFloats(config.toNumbers())
            prefs[Keys.aodTexts] = KeyValueCodec.encodeStrings(config.toTexts())
        }
    }

    fun observeVisualizerPresets(): Flow<List<Preset<VisualizerConfig>>> =
        configDao.observePresets(DesignKind.VISUALIZER).map { rows ->
            rows.mapNotNull { row ->
                visualizerFrom(row.designId, row.paletteId, row.numbers, row.texts)
                    ?.let { Preset(row.id, row.name, it) }
            }
        }

    fun observeAodPresets(): Flow<List<Preset<AodConfig>>> = configDao.observePresets(DesignKind.AOD).map { rows ->
        rows.mapNotNull { row ->
            aodFrom(row.designId, row.paletteId, row.numbers, row.texts)
                ?.let { Preset(row.id, row.name, it) }
        }
    }

    suspend fun saveVisualizerPreset(name: String, config: VisualizerConfig): Long = configDao.insert(
        PresetEntity(
            name = name,
            kind = DesignKind.VISUALIZER,
            designId = config.designId,
            paletteId = config.paletteId,
            numbers = config.toNumbers(),
            texts = config.toTexts(),
            createdAtMillis = System.currentTimeMillis()
        )
    )

    suspend fun saveAodPreset(name: String, config: AodConfig): Long = configDao.insert(
        PresetEntity(
            name = name,
            kind = DesignKind.AOD,
            designId = config.designId,
            paletteId = config.paletteId,
            numbers = config.toNumbers(),
            texts = config.toTexts(),
            createdAtMillis = System.currentTimeMillis()
        )
    )

    suspend fun deletePreset(id: Long) = configDao.delete(id)
}

private fun VisualizerConfig.toNumbers(): Map<String, Float> {
    val numbers = mutableMapOf(
        KEY_THICKNESS to thickness,
        KEY_HEIGHT to height,
        KEY_SENSITIVITY to sensitivity,
        KEY_SMOOTHING to smoothing
    )
    extras.forEach { (key, value) -> numbers[EXTRA_PREFIX + key] = value }
    return numbers
}

private fun VisualizerConfig.toTexts(): Map<String, String> = mapOf(KEY_DIRECTION to direction.name)

private fun visualizerFrom(
    designId: String?,
    paletteId: String?,
    numbers: Map<String, Float>,
    texts: Map<String, String>
): VisualizerConfig? {
    if (designId == null || paletteId == null) return null
    return runCatching {
        VisualizerConfig(
            designId = designId,
            paletteId = paletteId,
            thickness = numbers[KEY_THICKNESS] ?: VisualizerConfig.DEFAULT_THICKNESS,
            height = numbers[KEY_HEIGHT] ?: VisualizerConfig.DEFAULT_HEIGHT,
            sensitivity = numbers[KEY_SENSITIVITY] ?: VisualizerConfig.DEFAULT_SENSITIVITY,
            smoothing = numbers[KEY_SMOOTHING] ?: VisualizerConfig.DEFAULT_SMOOTHING,
            direction = enumOrDefault(texts[KEY_DIRECTION], VisualizerDirection.CLOCKWISE),
            extras = numbers
                .filterKeys { it.startsWith(EXTRA_PREFIX) }
                .mapKeys { it.key.removePrefix(EXTRA_PREFIX) }
        )
    }.getOrNull()
}

private fun AodConfig.toNumbers(): Map<String, Float> {
    val numbers = mutableMapOf(KEY_CLOCK_WEIGHT to clockWeight.toFloat(), KEY_SYMBOL_SCALE to symbolScale)
    extras.forEach { (key, value) -> numbers[EXTRA_PREFIX + key] = value }
    return numbers
}

private fun AodConfig.toTexts(): Map<String, String> {
    val texts = mutableMapOf(
        KEY_SHOW_MUSIC to showMusicCard.toString(),
        KEY_SHOW_STATUS to showStatusRow.toString()
    )
    symbolId?.let { texts[KEY_SYMBOL_ID] = it }
    backgroundUri?.let { texts[KEY_BACKGROUND_URI] = it }
    choices.forEach { (key, value) -> texts[CHOICE_PREFIX + key] = value }
    return texts
}

private fun aodFrom(
    designId: String?,
    paletteId: String?,
    numbers: Map<String, Float>,
    texts: Map<String, String>
): AodConfig? {
    if (designId == null || paletteId == null) return null
    return runCatching {
        AodConfig(
            designId = designId,
            paletteId = paletteId,
            clockWeight = numbers[KEY_CLOCK_WEIGHT]?.toInt() ?: AodConfig.DEFAULT_CLOCK_WEIGHT,
            symbolId = texts[KEY_SYMBOL_ID],
            symbolScale = numbers[KEY_SYMBOL_SCALE] ?: AodConfig.DEFAULT_SYMBOL_SCALE,
            showMusicCard = texts[KEY_SHOW_MUSIC]?.toBooleanStrictOrNull() ?: true,
            showStatusRow = texts[KEY_SHOW_STATUS]?.toBooleanStrictOrNull() ?: true,
            backgroundUri = texts[KEY_BACKGROUND_URI],
            extras = numbers.filterKeys { it.startsWith(EXTRA_PREFIX) }.mapKeys { it.key.removePrefix(EXTRA_PREFIX) },
            choices = texts.filterKeys { it.startsWith(CHOICE_PREFIX) }.mapKeys { it.key.removePrefix(CHOICE_PREFIX) }
        )
    }.getOrNull()
}
