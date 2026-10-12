package com.zdmgold.orlumen.designs.visualizer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.zdmgold.orlumen.R
import com.zdmgold.orlumen.core.model.DesignDescriptor
import com.zdmgold.orlumen.core.model.DesignKind
import com.zdmgold.orlumen.core.model.ParamSpec

private const val KEY_REACH = "reach"
private const val MIN_REACH = 0.1f
private const val MAX_REACH = 0.6f
private const val DEFAULT_REACH = 0.3f
private const val MIN_WIDTH_SHARE = 0.25f
private const val MIN_ALPHA = 0.15f
private const val SLICE_OVERLAP = 1f

/** A colour wash that glows inward from both sides, reaching further as the music gets louder. */
object AuroraDesign : VisualizerDesign {
    override val descriptor = DesignDescriptor(
        id = "aurora",
        kind = DesignKind.VISUALIZER,
        nameRes = R.string.design_aurora,
        isPro = false,
        params = listOf(ParamSpec.Slider(KEY_REACH, R.string.param_reach, MIN_REACH, MAX_REACH, DEFAULT_REACH))
    )

    override fun draw(scope: DrawScope, input: DesignInput) {
        val reach = input.extra(KEY_REACH, DEFAULT_REACH).coerceIn(MIN_REACH, MAX_REACH)
        val width = input.geometry.widthPx
        val height = input.geometry.heightPx
        val bands = input.frame.size
        val sliceHeight = height / bands
        for (index in 0 until bands) {
            val position = (index + 0.5f) / bands
            val level = sideLevel(input.frame, position, input.clockwise)
            val sliceWidth = width * reach * (MIN_WIDTH_SHARE + (1f - MIN_WIDTH_SHARE) * level)
            val tint = gradientColor(input.colors, 1f - position)
                .copy(alpha = (MIN_ALPHA + (1f - MIN_ALPHA) * level).coerceIn(0f, 1f))
            val top = index * sliceHeight
            val size = Size(sliceWidth, sliceHeight + SLICE_OVERLAP)
            val leftBrush = Brush.horizontalGradient(listOf(tint, Color.Transparent), startX = 0f, endX = sliceWidth)
            scope.drawRect(leftBrush, Offset(0f, top), size)
            val rightBrush = Brush.horizontalGradient(
                listOf(tint, Color.Transparent),
                startX = width,
                endX = width - sliceWidth
            )
            scope.drawRect(rightBrush, Offset(width - sliceWidth, top), size)
        }
    }
}
