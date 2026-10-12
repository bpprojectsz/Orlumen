package com.zdmgold.orlumen.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.zdmgold.orlumen.R
import com.zdmgold.orlumen.core.theme.IconSize
import com.zdmgold.orlumen.core.theme.OrlumenTheme
import com.zdmgold.orlumen.core.theme.Radius
import com.zdmgold.orlumen.core.theme.Spacing

@Composable
fun EntitlementBadge(isPro: Boolean, modifier: Modifier = Modifier) {
    if (!isPro) return
    val colors = OrlumenTheme.colors
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(Radius.pill))
            .background(colors.accent)
            .padding(horizontal = Spacing.sm, vertical = Spacing.xxs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconWrapper(imageVector = iconStar, contentDescription = null, size = IconSize.sm, tint = colors.onAccent)
        Spacer(modifier = Modifier.width(Spacing.xs))
        Text(
            text = stringResource(R.string.badge_pro),
            style = OrlumenTheme.typography.caption,
            color = colors.onAccent
        )
    }
}
