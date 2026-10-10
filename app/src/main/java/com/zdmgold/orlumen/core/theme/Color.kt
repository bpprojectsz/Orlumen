package com.zdmgold.orlumen.core.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class OrlumenColors(
    val bgPrimary: Color,
    val bgSurface: Color,
    val bgElevated: Color,
    val accent: Color,
    val accentSecondary: Color,
    val onAccent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val border: Color,
    val error: Color
)

val darkOrlumenColors = OrlumenColors(
    bgPrimary = Color(0xFF050508),
    bgSurface = Color(0xFF0E0B16),
    bgElevated = Color(0xFF181325),
    accent = Color(0xFF9B6BFF),
    accentSecondary = Color(0xFFFF5FD2),
    onAccent = Color(0xFF0B0614),
    textPrimary = Color(0xFFF1EEFA),
    textSecondary = Color(0xFFB7B0C9),
    textTertiary = Color(0xFF7C7592),
    border = Color(0xFF2A2440),
    error = Color(0xFFFF6B7A)
)

val lightOrlumenColors = OrlumenColors(
    bgPrimary = Color(0xFFFBFAFF),
    bgSurface = Color(0xFFF1EEFA),
    bgElevated = Color(0xFFFFFFFF),
    accent = Color(0xFF6B3FE0),
    accentSecondary = Color(0xFFD6359E),
    onAccent = Color(0xFFFFFFFF),
    textPrimary = Color(0xFF14101F),
    textSecondary = Color(0xFF4A4360),
    textTertiary = Color(0xFF7A7390),
    border = Color(0xFFDAD4EC),
    error = Color(0xFFC62839)
)

val localOrlumenColors = staticCompositionLocalOf { darkOrlumenColors }
