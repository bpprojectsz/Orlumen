package com.zdmgold.orlumen.ui.screens.about

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.zdmgold.orlumen.BuildConfig
import com.zdmgold.orlumen.R
import com.zdmgold.orlumen.core.theme.OrlumenTheme
import com.zdmgold.orlumen.core.theme.Spacing
import com.zdmgold.orlumen.ui.components.AppCard
import com.zdmgold.orlumen.ui.components.AppRow
import com.zdmgold.orlumen.ui.components.IconWrapper
import com.zdmgold.orlumen.ui.components.SectionHeader
import com.zdmgold.orlumen.ui.components.iconBack
import com.zdmgold.orlumen.ui.components.iconInfo
import com.zdmgold.orlumen.ui.components.iconLock

private const val TAG = "AboutAndLegalScreen"
private const val SUPPORT_EMAIL = "stmakarios@gmail.com"

@Composable
fun AboutAndLegalScreen(modifier: Modifier = Modifier, onBack: (() -> Unit)? = null) {
    val context = LocalContext.current
    val colors = OrlumenTheme.colors
    val hasWebsite = BuildConfig.WEBSITE_BASE_URL.isNotBlank()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.lg)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    IconWrapper(imageVector = iconBack, contentDescription = stringResource(R.string.action_back))
                }
            }
            Text(
                text = stringResource(R.string.about_title),
                style = OrlumenTheme.typography.headline,
                color = colors.textPrimary,
                modifier = Modifier.padding(vertical = Spacing.lg)
            )
        }
        SectionHeader(text = stringResource(R.string.about_section_app))
        AppCard {
            AppRow(
                title = stringResource(R.string.about_version),
                subtitle = BuildConfig.VERSION_NAME,
                showChevron = false
            )
        }
        SectionHeader(text = stringResource(R.string.about_section_support))
        AppCard {
            AppRow(
                title = stringResource(R.string.about_contact_support),
                subtitle = SUPPORT_EMAIL,
                leadingIcon = iconInfo,
                onClick = { openUri(context, "mailto:$SUPPORT_EMAIL", Intent.ACTION_SENDTO) }
            )
        }
        if (hasWebsite) {
            SectionHeader(text = stringResource(R.string.about_section_legal))
            AppCard {
                AppRow(
                    title = stringResource(R.string.about_privacy_policy),
                    leadingIcon = iconLock,
                    onClick = { openUri(context, "${BuildConfig.WEBSITE_BASE_URL}/privacy.html", Intent.ACTION_VIEW) }
                )
                AppRow(
                    title = stringResource(R.string.about_terms_of_service),
                    leadingIcon = iconInfo,
                    onClick = { openUri(context, "${BuildConfig.WEBSITE_BASE_URL}/terms.html", Intent.ACTION_VIEW) }
                )
            }
        }
    }
}

private fun openUri(context: Context, uri: String, action: String) {
    val intent = Intent(action, Uri.parse(uri))
    try {
        context.startActivity(intent)
    } catch (notFound: ActivityNotFoundException) {
        Log.w(TAG, "No app can handle $uri", notFound)
    }
}
