package com.zdmgold.orlumen.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zdmgold.orlumen.R
import com.zdmgold.orlumen.core.theme.OrlumenTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val TICKS = 12
private const val MARK_DP = 28

/** The Orlumen brand mark: a ring of light with a gradient wordmark beside it. */
@Composable
fun Wordmark(modifier: Modifier = Modifier) {
    val colors = OrlumenTheme.colors
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Canvas(modifier = Modifier.size(MARK_DP.dp)) {
            val radius = size.minDimension / 2f
            for (i in 0 until TICKS) {
                val angle = i * 2 * PI / TICKS
                val dir = Offset(cos(angle).toFloat(), sin(angle).toFloat())
                drawLine(
                    color = lerp(colors.accent, colors.accentSecondary, i / TICKS.toFloat()),
                    start = center + dir * (radius * 0.52f),
                    end = center + dir * radius,
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
            drawCircle(colors.accentSecondary, radius = 2.4.dp.toPx())
        }
        Text(
            text = stringResource(R.string.app_name),
            style = TextStyle(
                brush = Brush.horizontalGradient(listOf(colors.accent, colors.accentSecondary)),
                fontFamily = BrandFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                letterSpacing = 1.sp
            )
        )
    }
}
