package com.zdmgold.orlumen.core.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.zdmgold.orlumen.core.database.ConfigDao
import com.zdmgold.orlumen.core.database.PresetEntity
import com.zdmgold.orlumen.core.model.AodConfig
import com.zdmgold.orlumen.core.model.DesignKind
import com.zdmgold.orlumen.core.model.VisualizerConfig
import com.zdmgold.orlumen.core.model.VisualizerDirection
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

private class FakeConfigDao : ConfigDao {
    private val rows = MutableStateFlow<List<PresetEntity>>(emptyList())
    private var nextId = 1L

    override fun observePresets(kind: DesignKind): Flow<List<PresetEntity>> = rows.map { list ->
        list.filter { it.kind == kind }
    }

    override suspend fun getPreset(id: Long): PresetEntity? = rows.value.firstOrNull { it.id == id }

    override suspend fun insert(preset: PresetEntity): Long {
        val id = nextId++
        rows.value = rows.value + preset.copy(id = id)
        return id
    }

    override suspend fun update(preset: PresetEntity) {
        rows.value = rows.value.map { if (it.id == preset.id) preset else it }
    }

    override suspend fun delete(id: Long) {
        rows.value = rows.value.filterNot { it.id == id }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ConfigRepositoryTest {
    @get:Rule
    val folder = TemporaryFolder()

    private fun repository(scope: TestScope): ConfigRepository {
        val store = PreferenceDataStoreFactory.create(scope = scope.backgroundScope) {
            folder.newFile("config.preferences_pb")
        }
        return ConfigRepository(store, FakeConfigDao())
    }

    @Test
    fun emptyStoreYieldsDefaults() = runTest(UnconfinedTestDispatcher()) {
        val repo = repository(this)
        assertEquals(VisualizerConfig.defaults(), repo.activeVisualizerConfig.first())
        assertEquals(AodConfig.defaults(), repo.activeAodConfig.first())
    }

    @Test
    fun activeVisualizerConfigRoundTrips() = runTest(UnconfinedTestDispatcher()) {
        val repo = repository(this)
        val config = VisualizerConfig(
            designId = "tide",
            paletteId = "deep_space",
            thickness = 6f,
            height = 48f,
            sensitivity = 0.8f,
            smoothing = 0.2f,
            direction = VisualizerDirection.ANTI_CLOCKWISE,
            extras = mapOf("spacing" to 3f)
        )
        repo.setActiveVisualizerConfig(config)
        assertEquals(config, repo.activeVisualizerConfig.first())
    }

    @Test
    fun activeAodConfigRoundTripsWithSpecialCharacters() = runTest(UnconfinedTestDispatcher()) {
        val repo = repository(this)
        val config = AodConfig(
            designId = "slab",
            paletteId = "monochrome",
            clockWeight = 700,
            symbolId = "heart",
            symbolScale = 1.5f,
            showMusicCard = false,
            showStatusRow = true,
            backgroundUri = "content://media/a/b?c=d&e=f",
            extras = mapOf("stars" to 80f),
            choices = mapOf("zone" to "Asia/Tokyo")
        )
        repo.setActiveAodConfig(config)
        assertEquals(config, repo.activeAodConfig.first())
    }

    @Test
    fun presetsAreSavedPerKindAndDeletable() = runTest(UnconfinedTestDispatcher()) {
        val repo = repository(this)
        val viz = VisualizerConfig.defaults().copy(thickness = 9f)
        val vizId = repo.saveVisualizerPreset("Night", viz)
        repo.saveAodPreset("Desk", AodConfig.defaults())

        val vizPresets = repo.observeVisualizerPresets().first()
        assertEquals(1, vizPresets.size)
        assertEquals("Night", vizPresets[0].name)
        assertEquals(viz, vizPresets[0].config)
        assertEquals(1, repo.observeAodPresets().first().size)

        repo.deletePreset(vizId)
        assertTrue(repo.observeVisualizerPresets().first().isEmpty())
        assertEquals(1, repo.observeAodPresets().first().size)
    }
}
