package com.zdmgold.orlumen.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import com.zdmgold.orlumen.core.theme.IconSize
import com.zdmgold.orlumen.core.theme.OrlumenTheme
import com.zdmgold.orlumen.core.theme.Spacing

@Composable
fun EmptyState(icon: ImageVector, title: String, modifier: Modifier = Modifier, subtitle: String? = null) {
    val colors = OrlumenTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        IconWrapper(imageVector = icon, contentDescription = null, size = IconSize.lg, tint = colors.textTertiary)
        Spacer(modifier = Modifier.height(Spacing.md))
        Text(
            text = title,
            style = OrlumenTheme.typography.headline,
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = subtitle,
                style = OrlumenTheme.typography.body,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}
