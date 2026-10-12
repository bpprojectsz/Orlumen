package com.zdmgold.orlumen.designs.visualizer

import com.zdmgold.orlumen.core.model.SpectrumFrame
import com.zdmgold.orlumen.platform.overlay.CornerRadii
import com.zdmgold.orlumen.platform.overlay.EdgeGeometry
import kotlin.math.PI
import kotlin.math.hypot
import kotlin.math.sqrt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EdgePathTest {
    private val width = 1000f
    private val height = 2000f
    private val rounded = EdgeGeometry(width, height, CornerRadii.uniform(100f))
    private val square = EdgeGeometry(width, height, CornerRadii.uniform(0f))

    private fun frameOf(vararg levels: Float) = SpectrumFrame(levels, 0L)

    @Test
    fun lengthMatchesTheGeometryPerimeter() {
        assertEquals(rounded.perimeterPx, EdgePath(rounded).length, 0.05f)
        assertEquals(6000f, EdgePath(square).length, 0.01f)
    }

    @Test
    fun startsOnTheTopEdgeWithAnInwardNormal() {
        val start = EdgePath(rounded).pointAt(0f)
        assertEquals(100f, start.x, 0.01f)
        assertEquals(0f, start.y, 0.01f)
        assertEquals(1f, start.inwardY, 0.001f)
    }

    @Test
    fun wrapsAroundInBothDirections() {
        val path = EdgePath(rounded)
        val start = path.pointAt(0f)
        val end = path.pointAt(path.length)
        assertEquals(start.x, end.x, 0.05f)
        assertEquals(start.y, end.y, 0.05f)
        assertEquals(path.pointAt(path.length - 10f).x, path.pointAt(-10f).x, 0.05f)
    }

    @Test
    fun squareCornersMeetAtTheScreenCorners() {
        val path = EdgePath(square)
        val corner = path.pointAt(width)
        assertEquals(width, corner.x, 0.01f)
        assertEquals(0f, corner.y, 0.01f)
    }

    @Test
    fun pointsStayOnScreenWithUnitNormals() {
        val path = EdgePath(rounded)
        for (index in 0..500) {
            val point = path.pointAtFraction(index / 500f)
            assertTrue(point.x in -0.01f..width + 0.01f)
            assertTrue(point.y in -0.01f..height + 0.01f)
            assertEquals(1f, sqrt(point.inwardX * point.inwardX + point.inwardY * point.inwardY), 0.001f)
        }
    }

    @Test
    fun consecutivePointsNeverJump() {
        val path = EdgePath(rounded)
        val step = path.length / 3000f
        var previous = path.pointAt(0f)
        for (index in 1..3000) {
            val point = path.pointAt(index * step)
            assertTrue(hypot(point.x - previous.x, point.y - previous.y) <= step * 1.05f)
            previous = point
        }
    }

    @Test
    fun arcsStayOnTheCornerRadius() {
        val path = EdgePath(rounded)
        val arcStart = width - 200f
        val middle = path.pointAt(arcStart + 100f * (PI / 4).toFloat())
        assertEquals(100f, hypot(middle.x - (width - 100f), middle.y - 100f), 0.05f)
    }

    @Test
    fun bottomCenterSitsOnTheBottomEdgeAtTheMiddle() {
        val path = EdgePath(rounded)
        val point = path.pointAtFraction(path.bottomCenterFraction)
        assertEquals(height, point.y, 0.05f)
        assertEquals(width / 2f, point.x, 0.05f)
    }

    @Test
    fun perimeterLevelPutsLowsAtTheBottomAndHighsAtTheTop() {
        val frame = frameOf(0.1f, 0.4f, 0.7f, 1f)
        val bottom = 0.58f
        assertEquals(0.1f, perimeterLevel(frame, bottom, bottom), 0.001f)
        assertEquals(1f, perimeterLevel(frame, wrapUnit(bottom + 0.5f), bottom), 0.001f)
        assertEquals(
            perimeterLevel(frame, wrapUnit(bottom + 0.2f), bottom),
            perimeterLevel(frame, wrapUnit(bottom - 0.2f), bottom),
            0.001f
        )
    }

    @Test
    fun interpolatedBandBlendsNeighbours() {
        val frame = frameOf(0f, 1f, 0f)
        assertEquals(0.5f, interpolatedBand(frame, 0.25f), 0.001f)
        assertEquals(1f, interpolatedBand(frame, 0.5f), 0.001f)
        assertEquals(0f, interpolatedBand(frame, 2f), 0.001f)
    }

    @Test
    fun sideLevelFlipsWithDirection() {
        val frame = frameOf(0f, 1f)
        assertEquals(1f, sideLevel(frame, 0f, clockwise = true), 0.001f)
        assertEquals(0f, sideLevel(frame, 0f, clockwise = false), 0.001f)
    }

    @Test
    fun averageLevelHandlesEmptyFrames() {
        assertEquals(0.5f, averageLevel(frameOf(0f, 1f)), 0.001f)
        assertEquals(0f, averageLevel(SpectrumFrame(FloatArray(0), 0L)), 0f)
    }
}
