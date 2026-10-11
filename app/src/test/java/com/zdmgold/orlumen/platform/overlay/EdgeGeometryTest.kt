package com.zdmgold.orlumen.platform.overlay

import kotlin.math.PI
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class EdgeGeometryTest {
    @Test
    fun squareCornersGiveTheRectanglePerimeter() {
        val geometry = EdgeGeometry(1000f, 2000f, CornerRadii.uniform(0f))
        assertEquals(6000f, geometry.perimeterPx, 0.01f)
    }

    @Test
    fun uniformRadiusReplacesCornersWithArcs() {
        val radius = 100f
        val geometry = EdgeGeometry(1000f, 2000f, CornerRadii.uniform(radius))
        val expected = 6000f - 8f * radius + (2.0 * PI * radius).toFloat()
        assertEquals(expected, geometry.perimeterPx, 0.05f)
    }

    @Test
    fun mixedRadiiAreSummedPerCorner() {
        val corners = CornerRadii(topLeft = 10f, topRight = 20f, bottomRight = 30f, bottomLeft = 40f)
        val geometry = EdgeGeometry(500f, 800f, corners)
        val expected = 2600f - 2f * 100f + (100f * (PI / 2)).toFloat()
        assertEquals(expected, geometry.perimeterPx, 0.05f)
    }

    @Test
    fun radiiAreClampedToTheLimit() {
        val clamped = CornerRadii(10f, 80f, 200f, 50f).coercedTo(60f)
        assertEquals(CornerRadii(10f, 60f, 60f, 50f), clamped)
    }

    @Test
    fun rejectsNonPositiveSizes() {
        assertThrows(IllegalArgumentException::class.java) { EdgeGeometry(0f, 100f, CornerRadii.uniform(0f)) }
        assertThrows(IllegalArgumentException::class.java) { EdgeGeometry(100f, -1f, CornerRadii.uniform(0f)) }
    }
}
