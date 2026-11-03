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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.zdmgold.orlumen.R
import com.zdmgold.orlumen.core.theme.OrlumenTheme
import com.zdmgold.orlumen.core.theme.Spacing
import com.zdmgold.orlumen.ui.components.ScreenHeader

/** Shows a bundled legal text. Lines starting with "# " or "## " are headings and "- " are bullets. */
@Composable
fun LegalDocumentScreen(doc: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val rawId = if (doc == LEGAL_TERMS) R.raw.terms_of_use else R.raw.privacy_policy
    val titleRes = if (doc == LEGAL_TERMS) R.string.about_terms_of_service else R.string.about_privacy_policy
    val lines = remember(rawId) {
        context.resources.openRawResource(rawId).bufferedReader().use { it.readLines() }
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.lg)
            .verticalScroll(rememberScrollState())
    ) {
        ScreenHeader(title = stringResource(titleRes), onBack = onBack)
        lines.filter { it.isNotBlank() }.forEach { line ->
            when {
                line.startsWith("## ") -> Text(
                    text = line.removePrefix("## "),
                    style = OrlumenTheme.typography.body,
                    color = OrlumenTheme.colors.textPrimary,
                    modifier = Modifier.padding(top = Spacing.lg, bottom = Spacing.xs)
                )
                line.startsWith("# ") -> Unit
                line.startsWith("- ") -> Text(
                    text = "\u2022 " + line.removePrefix("- "),
                    style = OrlumenTheme.typography.caption,
                    color = OrlumenTheme.colors.textSecondary,
                    modifier = Modifier.padding(top = Spacing.xs)
                )
                else -> Text(
                    text = line,
                    style = OrlumenTheme.typography.caption,
                    color = OrlumenTheme.colors.textSecondary,
                    modifier = Modifier.padding(top = Spacing.sm)
                )
            }
        }
        Spacer(modifier = Modifier.height(Spacing.xl))
    }
}
