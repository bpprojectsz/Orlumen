package com.zdmgold.orlumen.core.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Immutable
data class OrlumenTypography(
    val display: TextStyle,
    val headline: TextStyle,
    val chrome: TextStyle,
    val body: TextStyle,
    val caption: TextStyle,
    val monospace: TextStyle
)

private fun textStyle(
    size: Int,
    lineHeight: Int,
    weight: FontWeight,
    family: FontFamily = FontFamily.SansSerif,
    tracking: Float = 0f
): TextStyle = TextStyle(
    fontFamily = family,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = tracking.sp
)

val defaultOrlumenTypography = OrlumenTypography(
    display = textStyle(size = 48, lineHeight = 52, weight = FontWeight.Light, tracking = -0.5f),
    headline = textStyle(size = 22, lineHeight = 28, weight = FontWeight.Medium),
    chrome = textStyle(size = 13, lineHeight = 16, weight = FontWeight.Medium, tracking = 1.2f),
    body = textStyle(size = 16, lineHeight = 24, weight = FontWeight.Normal),
    caption = textStyle(size = 12, lineHeight = 16, weight = FontWeight.Normal, tracking = 0.2f),
    monospace = textStyle(size = 14, lineHeight = 20, weight = FontWeight.Normal, family = FontFamily.Monospace)
)

fun OrlumenTypography.toMaterial(): Typography = Typography(
    displayLarge = display,
    titleLarge = headline,
    labelLarge = chrome,
    bodyLarge = body,
    bodySmall = caption
)
