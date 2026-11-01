package com.zdmgold.orlumen.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.zdmgold.orlumen.R
import com.zdmgold.orlumen.core.theme.OrlumenTheme
import com.zdmgold.orlumen.core.theme.Spacing

@Composable
fun ScreenHeader(title: String, modifier: Modifier = Modifier, onBack: (() -> Unit)? = null) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                IconWrapper(imageVector = iconBack, contentDescription = stringResource(R.string.action_back))
            }
        }
        Text(
            text = title,
            style = OrlumenTheme.typography.headline,
            color = OrlumenTheme.colors.textPrimary,
            modifier = Modifier.padding(vertical = Spacing.lg)
        )
    }
}
