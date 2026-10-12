package com.zdmgold.orlumen.designs.aod

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class NumberWordsTest {
    @Test
    fun spellsRepresentativeValues() {
        assertEquals("zero", NumberWords.english(0))
        assertEquals("seven", NumberWords.english(7))
        assertEquals("thirteen", NumberWords.english(13))
        assertEquals("twenty", NumberWords.english(20))
        assertEquals("twenty-nine", NumberWords.english(29))
        assertEquals("forty", NumberWords.english(40))
        assertEquals("fifty-nine", NumberWords.english(59))
    }

    @Test
    fun everyValueHasADistinctSpelling() {
        val words = (0..59).map { NumberWords.english(it) }
        assertEquals(60, words.toSet().size)
    }

    @Test
    fun titleCaseCapitalisesTheFirstLetterOnly() {
        assertEquals("Twenty-nine", NumberWords.englishTitleCase(29))
        assertEquals("Seven", NumberWords.englishTitleCase(7))
    }

    @Test
    fun rejectsValuesOutsideAClock() {
        assertThrows(IllegalArgumentException::class.java) { NumberWords.english(-1) }
        assertThrows(IllegalArgumentException::class.java) { NumberWords.english(60) }
    }
}
