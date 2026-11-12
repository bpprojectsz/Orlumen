package com.zdmgold.orlumen.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val GLYPH_DP = 22
private const val STROKE = 0.09f
private const val RAYS = 8

/** Small drawn icons that the standard icon set does not have. */
enum class Glyph { MOON, SUN, AUTO, GLOBE, NOTE, CLOCK, PULSE }

@Composable
fun GlyphIcon(glyph: Glyph, tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(GLYPH_DP.dp)) {
        val s = size.minDimension
        val stroke = Stroke(width = s * STROKE, cap = StrokeCap.Round, join = StrokeJoin.Round)
        when (glyph) {
            Glyph.MOON -> drawMoon(tint, s)
            Glyph.SUN -> drawSun(tint, s)
            Glyph.AUTO -> drawAuto(tint, s, stroke)
            Glyph.GLOBE -> drawGlobe(tint, s, stroke)
            Glyph.NOTE -> drawNote(tint, s)
            Glyph.CLOCK -> drawClock(tint, s, stroke)
            Glyph.PULSE -> drawPulse(tint, s, stroke)
        }
    }
}

private fun DrawScope.drawMoon(tint: Color, s: Float) {
    val outer = Path().apply { addOval(Rect(center, s * 0.40f)) }
    val cut = Path().apply { addOval(Rect(Offset(center.x + s * 0.2f, center.y - s * 0.14f), s * 0.34f)) }
    drawPath(Path.combine(PathOperation.Difference, outer, cut), tint)
}

private fun DrawScope.drawSun(tint: Color, s: Float) {
    drawCircle(tint, radius = s * 0.2f)
    for (i in 0 until RAYS) {
        val angle = i * PI / (RAYS / 2)
        val dir = Offset(cos(angle).toFloat(), sin(angle).toFloat())
        line(tint, center + dir * (s * 0.32f), center + dir * (s * 0.46f), s * STROKE)
    }
}

private fun DrawScope.drawAuto(tint: Color, s: Float, stroke: Stroke) {
    drawCircle(tint, radius = s * 0.38f, style = stroke)
    drawArc(
        tint,
        startAngle = 90f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(center.x - s * 0.38f, center.y - s * 0.38f),
        size = Size(s * 0.76f, s * 0.76f)
    )
}

private fun DrawScope.drawGlobe(tint: Color, s: Float, stroke: Stroke) {
    drawCircle(tint, radius = s * 0.4f, style = stroke)
    drawOval(tint, Offset(center.x - s * 0.16f, center.y - s * 0.4f), Size(s * 0.32f, s * 0.8f), style = stroke)
    line(tint, Offset(center.x - s * 0.4f, center.y), Offset(center.x + s * 0.4f, center.y), stroke.width)
}

private fun DrawScope.drawNote(tint: Color, s: Float) {
    val stemX = center.x + s * 0.1f
    drawOval(tint, Offset(center.x - s * 0.3f, center.y + s * 0.08f), Size(s * 0.4f, s * 0.3f))
    line(tint, Offset(stemX, center.y + s * 0.2f), Offset(stemX, center.y - s * 0.36f), s * STROKE)
    line(tint, Offset(stemX, center.y - s * 0.36f), Offset(stemX + s * 0.26f, center.y - s * 0.2f), s * 0.14f)
}

private fun DrawScope.drawClock(tint: Color, s: Float, stroke: Stroke) {
    drawCircle(tint, radius = s * 0.4f, style = stroke)
    line(tint, center, Offset(center.x, center.y - s * 0.24f), stroke.width)
    line(tint, center, Offset(center.x + s * 0.17f, center.y + s * 0.09f), stroke.width)
}

private fun DrawScope.drawPulse(tint: Color, s: Float, stroke: Stroke) {
    val path = Path().apply {
        moveTo(center.x - s * 0.46f, center.y)
        lineTo(center.x - s * 0.2f, center.y)
        lineTo(center.x - s * 0.1f, center.y - s * 0.34f)
        lineTo(center.x + s * 0.08f, center.y + s * 0.34f)
        lineTo(center.x + s * 0.18f, center.y)
        lineTo(center.x + s * 0.46f, center.y)
    }
    drawPath(path, tint, style = stroke)
}

private fun DrawScope.line(tint: Color, start: Offset, end: Offset, width: Float) =
    drawLine(tint, start, end, strokeWidth = width, cap = StrokeCap.Round)
