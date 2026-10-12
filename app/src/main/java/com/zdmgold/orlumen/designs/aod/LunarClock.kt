package com.zdmgold.orlumen.designs.aod

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zdmgold.orlumen.R
import com.zdmgold.orlumen.core.model.DesignDescriptor
import com.zdmgold.orlumen.core.model.DesignKind
import com.zdmgold.orlumen.core.model.ParamSpec
import com.zdmgold.orlumen.core.utils.MoonPhase
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.random.Random

private const val KEY_STARS = "stars"
private const val MIN_STARS = 20f
private const val MAX_STARS = 120f
private const val DEFAULT_STARS = 60f
private const val STAR_SEED = 7
private const val WIDTH_DP = 320
private const val HEIGHT_DP = 400
private const val MOON_RADIUS_DP = 52f
private const val MOON_CENTER_Y_SHARE = 0.28f
private const val DARK_MOON_ALPHA = 0.14f
private const val MIN_STAR_DP = 0.6f
private const val STAR_RANGE_DP = 1.0f
private const val MIN_STAR_ALPHA = 0.2f
private const val STAR_ALPHA_RANGE = 0.5f
private const val TIME_SIZE_SP = 56
private const val MARKER_SIZE_SP = 16
private const val SPACER_DP = 24
private const val HALF_TURN_SWEEP = 180f
private const val TOP_ANGLE = -90f
private const val BOTTOM_ANGLE = 90f
private const val TWO_PI = (2 * PI).toFloat()

private class Star(val x: Float, val y: Float, val size: Float)

/** A starfield with the real moon phase above the time. */
object LunarClock : AodDesign {
    override val descriptor = DesignDescriptor(
        id = "lunar",
        kind = DesignKind.AOD,
        nameRes = R.string.aod_lunar,
        isPro = false,
        params = listOf(ParamSpec.Slider(KEY_STARS, R.string.param_stars, MIN_STARS, MAX_STARS, DEFAULT_STARS))
    )

    @Composable
    override fun Clock(input: AodClockInput, modifier: Modifier) {
        val count = input.extra(KEY_STARS, DEFAULT_STARS).toInt().coerceIn(MIN_STARS.toInt(), MAX_STARS.toInt())
        val stars = remember(count) {
            val random = Random(STAR_SEED)
            List(count) { Star(random.nextFloat(), random.nextFloat(), random.nextFloat()) }
        }
        val phase = MoonPhase.fractionAt(input.instant.toEpochMilli())
        Box(modifier = modifier.size(width = WIDTH_DP.dp, height = HEIGHT_DP.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                stars.forEach { star ->
                    drawCircle(
                        color = Color.White.copy(alpha = MIN_STAR_ALPHA + STAR_ALPHA_RANGE * star.size),
                        radius = (MIN_STAR_DP + STAR_RANGE_DP * star.size).dp.toPx(),
                        center = Offset(star.x * size.width, star.y * size.height)
                    )
                }
                val radius = MOON_RADIUS_DP.dp.toPx()
                val center = Offset(size.width / 2f, size.height * MOON_CENTER_Y_SHARE)
                drawCircle(Color.White.copy(alpha = DARK_MOON_ALPHA), radius, center)
                drawPath(litMoonPath(center, radius, phase), input.secondary.copy(alpha = 1f))
            }
            Column(
                modifier = Modifier.align(Alignment.BottomCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${input.clock.hours}${input.separator}${input.clock.minutes}",
                    color = input.accent,
                    fontSize = TIME_SIZE_SP.sp,
                    fontWeight = FontWeight(input.config.clockWeight)
                )
                input.clock.marker?.let { Text(text = it, color = input.secondary, fontSize = MARKER_SIZE_SP.sp) }
                Spacer(modifier = Modifier.height(SPACER_DP.dp))
            }
        }
    }

    /** The lit part of the disc: a half circle on the lit side closed by the terminator ellipse. */
    private fun litMoonPath(center: Offset, radius: Float, phase: Float): Path {
        val cosine = cos(TWO_PI * phase)
        val waxing = MoonPhase.isWaxing(phase)
        val terminatorRadius = radius * abs(cosine)
        val circle = Rect(center.x - radius, center.y - radius, center.x + radius, center.y + radius)
        val ellipse = Rect(
            center.x - terminatorRadius,
            center.y - radius,
            center.x + terminatorRadius,
            center.y + radius
        )
        val crescent = cosine > 0f
        val limbSweep = if (waxing) HALF_TURN_SWEEP else -HALF_TURN_SWEEP
        val terminatorSweep = if (crescent == waxing) -HALF_TURN_SWEEP else HALF_TURN_SWEEP
        return Path().apply {
            arcTo(circle, TOP_ANGLE, limbSweep, true)
            arcTo(ellipse, BOTTOM_ANGLE, terminatorSweep, false)
            close()
        }
    }
}
