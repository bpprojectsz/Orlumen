package com.zdmgold.orlumen.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.zdmgold.orlumen.core.theme.OrlumenTheme
import com.zdmgold.orlumen.core.theme.Radius
import com.zdmgold.orlumen.core.theme.Spacing
import com.zdmgold.orlumen.core.theme.Stroke

@Composable
fun AppChip(label: String, modifier: Modifier = Modifier, selected: Boolean = false, onClick: (() -> Unit)? = null) {
    val colors = OrlumenTheme.colors
    val shape = RoundedCornerShape(Radius.pill)
    val background = if (selected) colors.accent else colors.bgElevated
    val borderColor = if (selected) colors.accent else colors.border
    val textColor = if (selected) colors.onAccent else colors.textSecondary
    Box(
        modifier = modifier
            .clip(shape)
            .background(background)
            .border(Stroke.hairline, borderColor, shape)
            .pressFeedback(onClick)
            .padding(horizontal = Spacing.md, vertical = Spacing.xs)
    ) {
        Text(text = label, style = OrlumenTheme.typography.caption, color = textColor)
    }
}
