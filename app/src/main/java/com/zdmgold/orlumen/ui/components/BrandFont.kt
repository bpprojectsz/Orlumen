package com.zdmgold.orlumen.ui.components

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.zdmgold.orlumen.R

/** Poppins (SIL Open Font License) for the wordmark and card titles. Other scripts fall back to the system font. */
val BrandFontFamily = FontFamily(
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_bold, FontWeight.Bold)
)
