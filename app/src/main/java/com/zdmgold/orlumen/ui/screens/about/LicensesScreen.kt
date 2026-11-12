package com.zdmgold.orlumen.ui.screens.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.zdmgold.orlumen.R
import com.zdmgold.orlumen.core.theme.OrlumenTheme
import com.zdmgold.orlumen.core.theme.Spacing
import com.zdmgold.orlumen.ui.components.AppCard
import com.zdmgold.orlumen.ui.components.AppRow
import com.zdmgold.orlumen.ui.components.ScreenHeader

private const val APACHE = "Apache License 2.0"

private val LIBRARIES = listOf(
    "Jetpack Compose and Material 3" to APACHE,
    "AndroidX Lifecycle, Navigation, Activity, Core" to APACHE,
    "AndroidX Room and DataStore" to APACHE,
    "AndroidX Palette and SplashScreen" to APACHE,
    "Kotlin and kotlinx.coroutines" to APACHE,
    "Dagger and Hilt" to APACHE,
    "Poppins typeface" to "SIL Open Font License 1.1"
)

@Composable
fun LicensesScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.lg)
            .verticalScroll(rememberScrollState())
    ) {
        ScreenHeader(title = stringResource(R.string.licenses_title), onBack = onBack)
        Text(
            text = stringResource(R.string.licenses_intro),
            style = OrlumenTheme.typography.caption,
            color = OrlumenTheme.colors.textSecondary,
            modifier = Modifier.padding(bottom = Spacing.md)
        )
        AppCard {
            LIBRARIES.forEach { (name, license) ->
                AppRow(title = name, subtitle = license, showChevron = false)
            }
        }
        Text(
            text = stringResource(R.string.licenses_full_text),
            style = OrlumenTheme.typography.caption,
            color = OrlumenTheme.colors.textTertiary,
            modifier = Modifier.padding(top = Spacing.md)
        )
        Text(
            text = stringResource(R.string.licenses_google_note),
            style = OrlumenTheme.typography.caption,
            color = OrlumenTheme.colors.textTertiary,
            modifier = Modifier.padding(top = Spacing.sm)
        )
        Spacer(modifier = Modifier.height(Spacing.xl))
    }
}
