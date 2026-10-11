package com.zdmgold.orlumen.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.zdmgold.orlumen.core.theme.OrlumenTheme
import com.zdmgold.orlumen.core.theme.Spacing

@Composable
fun AppRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
    showChevron: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val colors = OrlumenTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .pressFeedback(onClick)
            .padding(vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            IconWrapper(imageVector = leadingIcon, contentDescription = null, tint = colors.textSecondary)
            Spacer(modifier = Modifier.width(Spacing.md))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = OrlumenTheme.typography.body, color = colors.textPrimary)
            if (subtitle != null) {
                Text(text = subtitle, style = OrlumenTheme.typography.caption, color = colors.textSecondary)
            }
        }
        if (showChevron) {
            IconWrapper(imageVector = iconChevronRight, contentDescription = null, tint = colors.textTertiary)
        }
    }
}
