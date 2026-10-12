package com.zdmgold.orlumen.designs.aod

import com.zdmgold.orlumen.core.model.AodConfig
import com.zdmgold.orlumen.core.model.DesignKind
import com.zdmgold.orlumen.core.model.ParamSpec
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class AodRegistryTest {
    private val commonKeys = setOf(
        "clockWeight",
        "symbol",
        "symbolScale",
        "showMusicCard",
        "showStatusRow",
        "backgroundUri"
    )

    @Test
    fun shipsEightDesignsWithUniqueIdsAndNames() {
        val descriptors = AodRegistry.descriptors
        assertEquals(8, descriptors.size)
        assertEquals(8, descriptors.map { it.id }.toSet().size)
        assertEquals(8, descriptors.map { it.nameRes }.toSet().size)
    }

    @Test
    fun defaultDesignExistsAndComesFirst() {
        assertNotNull(AodRegistry.byId(AodConfig.DEFAULT_DESIGN_ID))
        assertEquals(AodConfig.DEFAULT_DESIGN_ID, AodRegistry.designs.first().descriptor.id)
    }

    @Test
    fun unknownIdsFallBackToTheFirstDesign() {
        assertNull(AodRegistry.byId("missing"))
        assertSame(AodRegistry.designs.first(), AodRegistry.resolve("missing"))
    }

    @Test
    fun everyDescriptorIsAnAodDesignAndFree() {
        AodRegistry.descriptors.forEach {
            assertEquals(DesignKind.AOD, it.kind)
            assertFalse(it.isPro)
        }
    }

    @Test
    fun designSpecificKeysNeverShadowCommonSettings() {
        val keys = AodRegistry.descriptors.flatMap { it.params }.map { it.key }
        assertTrue(keys.none { it in commonKeys })
    }

    @Test
    fun sliderDefaultsAndChoiceDefaultsAreValid() {
        val params = AodRegistry.descriptors.flatMap { it.params }
        params.filterIsInstance<ParamSpec.Slider>().forEach { assertTrue(it.defaultValue in it.min..it.max) }
        params.filterIsInstance<ParamSpec.Choice>().forEach { choice ->
            assertTrue(choice.options.any { it.id == choice.defaultOptionId })
        }
    }

    @Test
    fun duetOffersOnlyRealTimeZones() {
        val duet = AodRegistry.byId("duet")!!.descriptor
        val choice = duet.params.filterIsInstance<ParamSpec.Choice>().single()
        choice.options.forEach { assertTrue(it.id in ZoneId.getAvailableZoneIds() || it.id == "UTC") }
        assertEquals(choice.options.size, choice.options.map { it.id }.toSet().size)
    }

    @Test
    fun symbolsAreUniqueAndLookUpById() {
        val symbols = AodSymbolSet.symbols
        assertEquals(symbols.size, symbols.map { it.id }.toSet().size)
        assertTrue(symbols.all { it.glyph.isNotBlank() })
        assertEquals("heart", AodSymbolSet.byId("heart")?.id)
        assertNull(AodSymbolSet.byId(null))
        assertNull(AodSymbolSet.byId("missing"))
    }

    @Test
    fun everyBitmapGlyphIsWellFormed() {
        "0123456789".forEach { digit ->
            val rows = bitmapGlyph(digit)
            assertEquals(7, rows.size)
            assertTrue(rows.all { it.length == 5 && it.all { cell -> cell == '#' || cell == '.' } })
            assertTrue(rows.any { it.contains('#') })
        }
        val colon = bitmapGlyph(':')
        assertEquals(7, colon.size)
        assertTrue(colon.all { it.length == 1 })
    }

    @Test
    fun bitmapDigitsAreAllDistinct() {
        val patterns = "0123456789".map { bitmapGlyph(it) }
        assertEquals(10, patterns.toSet().size)
    }

    @Test
    fun unknownBitmapCharactersFallBackToZero() {
        assertEquals(bitmapGlyph('0'), bitmapGlyph('x'))
    }
}
