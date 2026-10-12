package com.zdmgold.orlumen.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.zdmgold.orlumen.core.theme.OrlumenTheme
import com.zdmgold.orlumen.core.theme.Radius
import com.zdmgold.orlumen.core.theme.Spacing
import com.zdmgold.orlumen.core.theme.Stroke

@Composable
fun DesignCard(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    preview: @Composable () -> Unit
) {
    val colors = OrlumenTheme.colors
    val shape = RoundedCornerShape(Radius.md)
    Column(
        modifier = modifier
            .clip(shape)
            .background(colors.bgSurface)
            .border(
                if (selected) Stroke.emphasis else Stroke.hairline,
                if (selected) colors.accent else colors.border,
                shape
            )
            .pressFeedback(onClick)
            .padding(Spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        preview()
        Spacer(modifier = Modifier.height(Spacing.sm))
        Text(
            text = title,
            style = OrlumenTheme.typography.chrome,
            color = if (selected) colors.accent else colors.textSecondary
        )
    }
}
