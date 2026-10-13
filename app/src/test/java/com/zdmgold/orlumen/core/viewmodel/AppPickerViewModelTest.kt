package com.zdmgold.orlumen.core.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Test

class AppPickerViewModelTest {
    @Test
    fun togglingAddsAnAppThatIsNotSelected() {
        assertEquals(setOf("a", "b"), setOf("a").toggled("b"))
    }

    @Test
    fun togglingRemovesAnAppThatIsSelected() {
        assertEquals(setOf("a"), setOf("a", "b").toggled("b"))
    }

    @Test
    fun togglingTheOnlyAppLeavesAnEmptySetMeaningEveryApp() {
        assertEquals(emptySet<String>(), setOf("a").toggled("a"))
    }
}
