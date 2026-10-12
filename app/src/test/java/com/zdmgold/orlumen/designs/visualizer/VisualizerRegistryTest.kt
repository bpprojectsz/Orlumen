package com.zdmgold.orlumen.designs.visualizer

import com.zdmgold.orlumen.core.model.DesignKind
import com.zdmgold.orlumen.core.model.ParamSpec
import com.zdmgold.orlumen.core.model.VisualizerConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class VisualizerRegistryTest {
    private val commonKeys = setOf("thickness", "height", "sensitivity", "smoothing", "direction")

    @Test
    fun shipsSixDesignsWithUniqueIdsAndNames() {
        val descriptors = VisualizerRegistry.descriptors
        assertEquals(6, descriptors.size)
        assertEquals(6, descriptors.map { it.id }.toSet().size)
        assertEquals(6, descriptors.map { it.nameRes }.toSet().size)
    }

    @Test
    fun defaultDesignExists() {
        assertNotNull(VisualizerRegistry.byId(VisualizerConfig.DEFAULT_DESIGN_ID))
        assertEquals(VisualizerConfig.DEFAULT_DESIGN_ID, VisualizerRegistry.designs.first().descriptor.id)
    }

    @Test
    fun unknownIdsFallBackToTheFirstDesign() {
        assertNull(VisualizerRegistry.byId("missing"))
        assertSame(VisualizerRegistry.designs.first(), VisualizerRegistry.resolve("missing"))
    }

    @Test
    fun everyDescriptorIsAVisualizerAndFree() {
        VisualizerRegistry.descriptors.forEach {
            assertEquals(DesignKind.VISUALIZER, it.kind)
            assertFalse(it.isPro)
        }
    }

    @Test
    fun designSpecificKeysNeverShadowCommonSettings() {
        val keys = VisualizerRegistry.descriptors.flatMap { it.params }.map { it.key }
        assertTrue(keys.none { it in commonKeys })
    }

    @Test
    fun sliderDefaultsSitInsideTheirRanges() {
        VisualizerRegistry.descriptors.flatMap { it.params }.filterIsInstance<ParamSpec.Slider>().forEach {
            assertTrue(it.defaultValue in it.min..it.max)
        }
    }
}
