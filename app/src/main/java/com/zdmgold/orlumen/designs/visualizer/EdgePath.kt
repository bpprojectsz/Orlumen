package com.zdmgold.orlumen.designs.visualizer

import com.zdmgold.orlumen.core.model.SpectrumFrame
import com.zdmgold.orlumen.platform.overlay.EdgeGeometry
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

private const val QUARTER_TURN = (PI / 2).toFloat()

data class PathPoint(val x: Float, val y: Float, val inwardX: Float, val inwardY: Float)

private class Segment(val start: Float, val length: Float, val sample: (Float) -> PathPoint)

/**
 * Walks the rounded rectangle that follows the screen edge, clockwise from the end of the top-left
 * corner. Every point also carries the unit normal that points toward the screen centre.
 */
class EdgePath(geometry: EdgeGeometry) {
    private val segments: List<Segment>
    val length: Float
    val bottomCenterFraction: Float

    init {
        val width = geometry.widthPx
        val height = geometry.heightPx
        val corners = geometry.corners
        val built = mutableListOf<Segment>()
        var cursor = 0f
        var bottomCenter = 0f

        fun line(segmentLength: Float, sample: (Float) -> PathPoint) {
            if (segmentLength > 0f) {
                built += Segment(cursor, segmentLength, sample)
                cursor += segmentLength
            }
        }

        fun arc(radius: Float, centerX: Float, centerY: Float, startAngle: Float) {
            line(radius * QUARTER_TURN) { travelled ->
                val angle = startAngle + travelled / radius
                PathPoint(
                    centerX + radius * cos(angle),
                    centerY + radius * sin(angle),
                    -cos(angle),
                    -sin(angle)
                )
            }
        }

        line(width - corners.topLeft - corners.topRight) { PathPoint(corners.topLeft + it, 0f, 0f, 1f) }
        arc(corners.topRight, width - corners.topRight, corners.topRight, -QUARTER_TURN)
        line(height - corners.topRight - corners.bottomRight) { PathPoint(width, corners.topRight + it, -1f, 0f) }
        arc(corners.bottomRight, width - corners.bottomRight, height - corners.bottomRight, 0f)
        val bottomLength = width - corners.bottomRight - corners.bottomLeft
        bottomCenter = cursor + bottomLength / 2f
        line(bottomLength) { PathPoint(width - corners.bottomRight - it, height, 0f, -1f) }
        arc(corners.bottomLeft, corners.bottomLeft, height - corners.bottomLeft, QUARTER_TURN)
        line(height - corners.bottomLeft - corners.topLeft) { PathPoint(0f, height - corners.bottomLeft - it, 1f, 0f) }
        arc(corners.topLeft, corners.topLeft, corners.topLeft, 2f * QUARTER_TURN)

        segments = built
        length = cursor
        bottomCenterFraction = bottomCenter / cursor
    }

    fun pointAt(distance: Float): PathPoint {
        val wrapped = ((distance % length) + length) % length
        val segment = segments.last { it.start <= wrapped }
        return segment.sample(wrapped - segment.start)
    }

    fun pointAtFraction(fraction: Float): PathPoint = pointAt(fraction * length)
}

fun wrapUnit(value: Float): Float = value - floor(value)

fun interpolatedBand(frame: SpectrumFrame, position: Float): Float {
    val scaled = position.coerceIn(0f, 1f) * (frame.size - 1)
    val lower = floor(scaled).toInt()
    val upper = minOf(lower + 1, frame.size - 1)
    val fraction = scaled - lower
    return frame[lower] + (frame[upper] - frame[lower]) * fraction
}

/** Level at a perimeter fraction: lows at the bottom centre, highs at the top centre, mirrored left/right. */
fun perimeterLevel(frame: SpectrumFrame, fraction: Float, bottomCenterFraction: Float): Float {
    val shifted = wrapUnit(fraction - bottomCenterFraction)
    return interpolatedBand(frame, 1f - abs(2f * shifted - 1f))
}

fun averageLevel(frame: SpectrumFrame): Float {
    if (frame.size == 0) return 0f
    var sum = 0f
    for (index in 0 until frame.size) sum += frame[index]
    return sum / frame.size
}

/** Level along a vertical side. [position] runs 0 at the top to 1 at the bottom. */
fun sideLevel(frame: SpectrumFrame, position: Float, clockwise: Boolean): Float {
    val bandPosition = if (clockwise) 1f - position else position
    return interpolatedBand(frame, bandPosition)
}
