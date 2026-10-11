package com.zdmgold.orlumen.platform.overlay

import kotlin.math.PI
import kotlin.math.min

data class CornerRadii(val topLeft: Float, val topRight: Float, val bottomRight: Float, val bottomLeft: Float) {
    fun coercedTo(maxRadius: Float): CornerRadii = CornerRadii(
        topLeft = min(topLeft, maxRadius),
        topRight = min(topRight, maxRadius),
        bottomRight = min(bottomRight, maxRadius),
        bottomLeft = min(bottomLeft, maxRadius)
    )

    companion object {
        fun uniform(radius: Float): CornerRadii = CornerRadii(radius, radius, radius, radius)
    }
}

data class EdgeGeometry(val widthPx: Float, val heightPx: Float, val corners: CornerRadii) {
    init {
        require(widthPx > 0f && heightPx > 0f) { "screen size must be positive" }
    }

    val perimeterPx: Float
        get() {
            val straight = (widthPx - corners.topLeft - corners.topRight) +
                (heightPx - corners.topRight - corners.bottomRight) +
                (widthPx - corners.bottomRight - corners.bottomLeft) +
                (heightPx - corners.bottomLeft - corners.topLeft)
            val arcs = (corners.topLeft + corners.topRight + corners.bottomRight + corners.bottomLeft) * QUARTER_TURN
            return straight + arcs
        }

    private companion object {
        const val QUARTER_TURN = (PI / 2).toFloat()
    }
}
