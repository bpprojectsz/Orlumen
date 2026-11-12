package com.zdmgold.orlumen.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zdmgold.orlumen.core.theme.OrlumenTheme

private const val SHRINK_STEP = 0.5f

/** A round, gradient-ringed button for the header. */
@Composable
fun RingChip(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val colors = OrlumenTheme.colors
    Box(
        modifier = modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(colors.accent.copy(alpha = 0.12f))
            .border(1.4.dp, Brush.linearGradient(listOf(colors.accent, colors.accentSecondary)), CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) { content() }
}

/** The main call-to-action: a full-width pill filled with the brand gradient. */
@Composable
fun GradientButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = OrlumenTheme.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(CircleShape)
            .background(Brush.horizontalGradient(listOf(colors.accent, colors.accentSecondary)))
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = colors.onAccent,
            fontFamily = BrandFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

/** Text that wraps to [maxLines] and shrinks toward [minSp] before it would ever be cut off. */
@Composable
fun ShrinkText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    startSp: Float = 14f,
    minSp: Float = 12f,
    maxLines: Int = 2,
    fontFamily: FontFamily = BrandFontFamily,
    fontWeight: FontWeight = FontWeight.Bold,
    textAlign: TextAlign? = null
) {
    var size by remember(text) { mutableFloatStateOf(startSp) }
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontFamily = fontFamily,
        fontWeight = fontWeight,
        fontSize = size.sp,
        maxLines = maxLines,
        textAlign = textAlign,
        onTextLayout = { if (it.hasVisualOverflow && size > minSp) size = (size - SHRINK_STEP).coerceAtLeast(minSp) }
    )
}
