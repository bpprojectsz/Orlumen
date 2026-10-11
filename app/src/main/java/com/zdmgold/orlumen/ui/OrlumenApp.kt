package com.zdmgold.orlumen.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.zdmgold.orlumen.core.model.ThemeMode
import com.zdmgold.orlumen.core.theme.OrlumenTheme
import com.zdmgold.orlumen.navigation.AppNavHost

@Composable
fun OrlumenApp() {
    OrlumenTheme(themeMode = ThemeMode.DARK) {
        Surface(modifier = Modifier.fillMaxSize(), color = OrlumenTheme.colors.bgPrimary) {
            AppNavHost(modifier = Modifier.systemBarsPadding())
        }
    }
}
