package com.zdmgold.orlumen.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class VisualizerConfigTest {
    @Test
    fun defaultsAreValidAndStable() {
        val config = VisualizerConfig.defaults()
        assertEquals(VisualizerConfig.DEFAULT_DESIGN_ID, config.designId)
        assertEquals(VisualizerConfig.DEFAULT_THICKNESS, config.thickness, 0f)
        assertEquals(VisualizerConfig.defaults(), config)
    }

    @Test
    fun copyChangesOnlyRequestedFields() {
        val original = VisualizerConfig.defaults()
        val changed = original.copy(thickness = 8f)
        assertNotEquals(original, changed)
        assertEquals(8f, changed.thickness, 0f)
        assertEquals(original.height, changed.height, 0f)
        assertEquals(original.paletteId, changed.paletteId)
    }

    @Test
    fun withExtraAddsAndReplacesValues() {
        val config = VisualizerConfig.defaults().withExtra("spacing", 2f).withExtra("spacing", 3f)
        assertEquals(mapOf("spacing" to 3f), config.extras)
    }

    @Test
    fun rejectsOutOfRangeValues() {
        assertThrows(IllegalArgumentException::class.java) {
            VisualizerConfig.defaults().copy(thickness = VisualizerConfig.MAX_THICKNESS + 1f)
        }
        assertThrows(IllegalArgumentException::class.java) {
            VisualizerConfig.defaults().copy(sensitivity = 1.5f)
        }
        assertThrows(IllegalArgumentException::class.java) {
            VisualizerConfig(designId = " ", paletteId = "p")
        }
    }
}
