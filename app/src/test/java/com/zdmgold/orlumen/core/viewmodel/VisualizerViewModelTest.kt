package com.zdmgold.orlumen.core.viewmodel

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.zdmgold.orlumen.core.model.VisualizerConfig
import com.zdmgold.orlumen.core.model.VisualizerDirection
import com.zdmgold.orlumen.core.repository.ConfigRepository
import com.zdmgold.orlumen.core.repository.PaletteRepository
import com.zdmgold.orlumen.testing.FakeConfigDao
import com.zdmgold.orlumen.testing.FakePaletteDao
import com.zdmgold.orlumen.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class VisualizerViewModelTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    @get:Rule
    val folder = TemporaryFolder()

    private fun TestScope.viewModel(): VisualizerViewModel {
        val store = PreferenceDataStoreFactory.create(scope = backgroundScope) {
            folder.newFile("visualizer.preferences_pb")
        }
        val model = VisualizerViewModel(ConfigRepository(store, FakeConfigDao()), PaletteRepository(FakePaletteDao()))
        backgroundScope.launch { model.uiState.collect {} }
        return model
    }

    private fun VisualizerViewModel.state(): VisualizerUiState = uiState.value!!

    @Test
    fun loadsTheSavedConfigurationAsTheDraft() = runTest(UnconfinedTestDispatcher()) {
        val model = viewModel()
        val state = model.uiState.first { it != null }!!
        assertEquals(VisualizerConfig.defaults(), state.draft)
        assertFalse(state.isDirty)
    }

    @Test
    fun editsAreClampedAndMarkTheDraftDirty() = runTest(UnconfinedTestDispatcher()) {
        val model = viewModel()
        model.setThickness(99f)
        model.setHeight(-5f)
        model.setSensitivity(-1f)
        model.setSmoothing(7f)
        model.setDirection(VisualizerDirection.ANTI_CLOCKWISE)
        model.selectPalette("jade")
        val draft = model.state().draft
        assertEquals(VisualizerConfig.MAX_THICKNESS, draft.thickness, 0f)
        assertEquals(VisualizerConfig.MIN_HEIGHT, draft.height, 0f)
        assertEquals(0f, draft.sensitivity, 0f)
        assertEquals(1f, draft.smoothing, 0f)
        assertEquals(VisualizerDirection.ANTI_CLOCKWISE, draft.direction)
        assertEquals("jade", draft.paletteId)
        assertTrue(model.state().isDirty)
    }

    @Test
    fun switchingDesignKeepsCommonSettingsAndDropsDesignSettings() = runTest(UnconfinedTestDispatcher()) {
        val model = viewModel()
        model.setThickness(8f)
        model.setExtra("spacing", 9f)
        model.selectDesign("filament")
        assertEquals(mapOf("spacing" to 9f), model.state().draft.extras)

        model.selectDesign("tide")
        assertEquals("tide", model.state().draft.designId)
        assertEquals(8f, model.state().draft.thickness, 0f)
        assertTrue(model.state().draft.extras.isEmpty())

        model.selectDesign("not-a-design")
        assertEquals("tide", model.state().draft.designId)
    }

    @Test
    fun applyPersistsTheDraftAndClearsTheDirtyFlag() = runTest(UnconfinedTestDispatcher()) {
        val model = viewModel()
        model.selectDesign("ember")
        model.setThickness(6f)
        assertTrue(model.state().isDirty)
        model.apply()
        assertEquals("ember", model.state().active.designId)
        assertFalse(model.state().isDirty)
    }

    @Test
    fun resetDiscardsUnsavedEdits() = runTest(UnconfinedTestDispatcher()) {
        val model = viewModel()
        model.setThickness(12f)
        model.resetDraft()
        assertEquals(VisualizerConfig.DEFAULT_THICKNESS, model.state().draft.thickness, 0f)
        assertFalse(model.state().isDirty)
    }

    @Test
    fun presetsAreSavedLoadedAndDeleted() = runTest(UnconfinedTestDispatcher()) {
        val model = viewModel()
        model.setThickness(9f)
        model.savePreset("  ")
        assertTrue(model.state().presets.isEmpty())

        model.savePreset(" Night ")
        val preset = model.state().presets.single()
        assertEquals("Night", preset.name)

        model.setThickness(2f)
        model.loadPreset(preset)
        assertEquals(9f, model.state().draft.thickness, 0f)

        model.deletePreset(preset.id)
        assertTrue(model.state().presets.isEmpty())
    }
}
