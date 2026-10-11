package com.zdmgold.orlumen.core.database

import com.zdmgold.orlumen.core.model.DesignKind
import org.junit.Assert.assertEquals
import org.junit.Test

class ConvertersTest {
    private val converters = Converters()

    @Test
    fun intListRoundTrips() {
        val colors = listOf(-65536, 0, 16711680)
        assertEquals(colors, converters.toIntList(converters.fromIntList(colors)))
        assertEquals(emptyList<Int>(), converters.toIntList(converters.fromIntList(emptyList())))
    }

    @Test
    fun floatMapRoundTrips() {
        val map = mapOf("thickness" to 4f, "x.spacing" to 0.25f)
        assertEquals(map, converters.toFloatMap(converters.fromFloatMap(map)))
        assertEquals(emptyMap<String, Float>(), converters.toFloatMap(converters.fromFloatMap(emptyMap())))
    }

    @Test
    fun stringMapSurvivesSeparatorsAndUnicode() {
        val map = mapOf("uri" to "content://a/b?c=d&e=f", "name" to "Café 100% = ok", "empty" to "")
        assertEquals(map, converters.toStringMap(converters.fromStringMap(map)))
    }

    @Test
    fun malformedFloatsAreDropped() {
        assertEquals(mapOf("a" to 1f), converters.toFloatMap("a=1.0&b=notanumber&broken"))
    }

    @Test
    fun designKindRoundTrips() {
        DesignKind.entries.forEach { kind ->
            assertEquals(kind, converters.toDesignKind(converters.fromDesignKind(kind)))
        }
    }
}
