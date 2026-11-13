package com.zdmgold.orlumen.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zdmgold.orlumen.core.theme.OrlumenTheme

private const val CARD_RADIUS_DP = 16
private const val PRESSED_SCALE = 0.985f
private const val SOFT_ALPHA = 0.55f
private const val CHIP_RADIUS_FRACTION = 0.32f

/** The signature card of the home screen: a soft gradient surface with a thin violet-to-pink edge. */
@Composable
fun PremiumCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    strong: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = OrlumenTheme.colors
    val shape = RoundedCornerShape(CARD_RADIUS_DP.dp)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) PRESSED_SCALE else 1f, label = "cardPress")
    val edge = if (strong) 1f else SOFT_ALPHA
    val edgeBrush = Brush.linearGradient(listOf(colors.accent.copy(edge), colors.accentSecondary.copy(edge)))
    val tap = if (onClick != null) {
        Modifier.clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick)
    } else {
        Modifier
    }
    Column(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (strong) 10.dp else 4.dp,
                shape = shape,
                ambientColor = colors.accent.copy(alpha = 0.35f),
                spotColor = colors.accentSecondary.copy(alpha = 0.35f)
            )
            .clip(shape)
            .background(Brush.linearGradient(listOf(colors.bgElevated, colors.bgSurface)))
            .border(1.2.dp, edgeBrush, shape)
            .then(tap)
            .padding(14.dp),
        content = content
    )
}

/** A rounded tile with a gradient tint that holds an icon. */
@Composable
fun IconChip(modifier: Modifier = Modifier, size: Dp = 36.dp, warm: Boolean = false, content: @Composable () -> Unit) {
    val colors = OrlumenTheme.colors
    val dark = colors.bgPrimary.luminance() < 0.5f
    val strength = if (dark) 0.34f else 0.20f
    val first = if (warm) colors.accentSecondary else colors.accent
    val second = if (warm) colors.accent else colors.accentSecondary
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * CHIP_RADIUS_FRACTION))
            .background(Brush.linearGradient(listOf(first.copy(alpha = strength), second.copy(alpha = strength)))),
        contentAlignment = Alignment.Center
    ) { content() }
}

/** The tint icons should use on top of an [IconChip]. */
@Composable
fun chipIconTint(): Color {
    val colors = OrlumenTheme.colors
    return if (colors.bgPrimary.luminance() < 0.5f) colors.textPrimary else colors.accent
}

/** A small round "go" marker for full-width cards. */
@Composable
fun ChevronBadge(modifier: Modifier = Modifier) {
    val colors = OrlumenTheme.colors
    Box(
        modifier = modifier.size(22.dp).clip(CircleShape).background(colors.accent.copy(alpha = 0.25f)),
        contentAlignment = Alignment.Center
    ) {
        IconWrapper(imageVector = iconChevronRight, contentDescription = null, size = 14.dp)
    }
}
