package com.zdmgold.orlumen.core.viewmodel

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.zdmgold.orlumen.core.model.AodConfig
import com.zdmgold.orlumen.core.repository.ConfigRepository
import com.zdmgold.orlumen.core.repository.PaletteRepository
import com.zdmgold.orlumen.testing.FakeConfigDao
import com.zdmgold.orlumen.testing.FakePaletteDao
import com.zdmgold.orlumen.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class AodViewModelTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    @get:Rule
    val folder = TemporaryFolder()

    private fun TestScope.viewModel(): AodViewModel {
        val store = PreferenceDataStoreFactory.create(scope = backgroundScope) {
            folder.newFile("aod.preferences_pb")
        }
        val model = AodViewModel(ConfigRepository(store, FakeConfigDao()), PaletteRepository(FakePaletteDao()))
        backgroundScope.launch { model.uiState.collect {} }
        return model
    }

    private fun AodViewModel.state(): AodUiEditorState = uiState.value!!

    @Test
    fun startsFromTheSavedConfiguration() = runTest(UnconfinedTestDispatcher()) {
        val model = viewModel()
        assertEquals(AodConfig.defaults(), model.state().draft)
        assertFalse(model.state().isDirty)
    }

    @Test
    fun clockWeightAndSymbolScaleAreClamped() = runTest(UnconfinedTestDispatcher()) {
        val model = viewModel()
        model.setClockWeight(5000)
        model.setSymbolScale(0.01f)
        assertEquals(AodConfig.MAX_CLOCK_WEIGHT, model.state().draft.clockWeight)
        assertEquals(AodConfig.MIN_SYMBOL_SCALE, model.state().draft.symbolScale, 0f)
        assertTrue(model.state().isDirty)
    }

    @Test
    fun symbolAndDisplayOptionsCanBeChangedAndCleared() = runTest(UnconfinedTestDispatcher()) {
        val model = viewModel()
        model.setSymbol("heart")
        model.setShowMusicCard(false)
        model.setShowStatusRow(false)
        assertEquals("heart", model.state().draft.symbolId)
        assertFalse(model.state().draft.showMusicCard)
        assertFalse(model.state().draft.showStatusRow)
        model.setSymbol(null)
        assertNull(model.state().draft.symbolId)
    }

    @Test
    fun switchingDesignDropsDesignSettingsButKeepsCommonOnes() = runTest(UnconfinedTestDispatcher()) {
        val model = viewModel()
        model.setClockWeight(700)
        model.selectDesign("duet")
        model.setChoice("zone", "Asia/Tokyo")
        model.setExtra("pixel", 9f)
        assertEquals(mapOf("zone" to "Asia/Tokyo"), model.state().draft.choices)

        model.selectDesign("bitmap")
        assertEquals("bitmap", model.state().draft.designId)
        assertEquals(700, model.state().draft.clockWeight)
        assertTrue(model.state().draft.extras.isEmpty())
        assertTrue(model.state().draft.choices.isEmpty())

        model.selectDesign("not-a-design")
        assertEquals("bitmap", model.state().draft.designId)
    }

    @Test
    fun applyAndPresetsWork() = runTest(UnconfinedTestDispatcher()) {
        val model = viewModel()
        model.selectDesign("lunar")
        model.apply()
        assertEquals("lunar", model.state().active.designId)

        model.savePreset("Night stand")
        val preset = model.state().presets.single()
        model.selectDesign("slab")
        model.loadPreset(preset)
        assertEquals("lunar", model.state().draft.designId)
        model.deletePreset(preset.id)
        assertTrue(model.state().presets.isEmpty())
    }
}
