package com.zdmgold.orlumen.ui.components

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ParamEditorPanelTest {
    @Test
    fun togglesAreStoredAsOneAndZero() {
        assertEquals(1f, toggleValue(true), 0f)
        assertEquals(0f, toggleValue(false), 0f)
        assertTrue(isToggleOn(1f))
        assertTrue(isToggleOn(0.5f))
        assertFalse(isToggleOn(0.49f))
        assertFalse(isToggleOn(0f))
    }

    @Test
    fun toggleValuesRoundTrip() {
        assertTrue(isToggleOn(toggleValue(true)))
        assertFalse(isToggleOn(toggleValue(false)))
    }

    @Test
    fun largeRangesShowWholeNumbers() {
        assertEquals("12", formatSliderValue(12f, 24f, Locale.US))
        assertEquals("7", formatSliderValue(6.6f, 12f, Locale.US))
    }

    @Test
    fun mediumRangesShowOneDecimal() {
        assertEquals("3.0", formatSliderValue(3f, 8f, Locale.US))
        assertEquals("2.5", formatSliderValue(2.5f, 3f, Locale.US))
    }

    @Test
    fun smallRangesShowTwoDecimals() {
        assertEquals("0.85", formatSliderValue(0.85f, 1.2f, Locale.US))
        assertEquals("0.30", formatSliderValue(0.3f, 0.6f, Locale.US))
    }

    @Test
    fun usesTheLocaleDecimalSeparator() {
        assertEquals("0,85", formatSliderValue(0.85f, 1.2f, Locale.GERMANY))
    }
}
