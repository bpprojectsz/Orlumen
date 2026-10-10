package com.zdmgold.orlumen.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import com.zdmgold.orlumen.core.model.ThemeMode

private fun OrlumenColors.toMaterial(isDark: Boolean): ColorScheme {
    val base = if (isDark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = accent,
        onPrimary = onAccent,
        secondary = accentSecondary,
        background = bgPrimary,
        onBackground = textPrimary,
        surface = bgSurface,
        onSurface = textPrimary,
        surfaceVariant = bgElevated,
        onSurfaceVariant = textSecondary,
        outline = border,
        error = error
    )
}

@Composable
fun OrlumenTheme(themeMode: ThemeMode = ThemeMode.DARK, content: @Composable () -> Unit) {
    val useDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val colors = if (useDark) darkOrlumenColors else lightOrlumenColors
    CompositionLocalProvider(localOrlumenColors provides colors) {
        MaterialTheme(
            colorScheme = colors.toMaterial(useDark),
            typography = defaultOrlumenTypography.toMaterial(),
            content = content
        )
    }
}

object OrlumenTheme {
    val colors: OrlumenColors
        @Composable
        @ReadOnlyComposable
        get() = localOrlumenColors.current

    val typography: OrlumenTypography
        get() = defaultOrlumenTypography
}
