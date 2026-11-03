package com.zdmgold.orlumen.ui.screens.about

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.zdmgold.orlumen.BuildConfig
import com.zdmgold.orlumen.R
import com.zdmgold.orlumen.core.theme.Spacing
import com.zdmgold.orlumen.ui.components.AppCard
import com.zdmgold.orlumen.ui.components.AppRow
import com.zdmgold.orlumen.ui.components.ScreenHeader
import com.zdmgold.orlumen.ui.components.SectionHeader
import com.zdmgold.orlumen.ui.components.iconInfo
import com.zdmgold.orlumen.ui.components.iconLock
import com.zdmgold.orlumen.ui.components.iconStar

private const val TAG = "AboutAndLegalScreen"
private const val SUPPORT_EMAIL = "stmakarios@gmail.com"
private const val PLAY_WEB_URL = "https://play.google.com/store/apps/details?id="

const val LEGAL_PRIVACY = "privacy"
const val LEGAL_TERMS = "terms"

class AboutActions(val onOpenLegal: (String) -> Unit, val onOpenLicenses: () -> Unit)

@Composable
fun AboutAndLegalScreen(actions: AboutActions, modifier: Modifier = Modifier, onBack: (() -> Unit)? = null) {
    val context = LocalContext.current
    val website = BuildConfig.WEBSITE_BASE_URL
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.lg)
            .verticalScroll(rememberScrollState())
    ) {
        ScreenHeader(title = stringResource(R.string.about_title), onBack = onBack)
        SectionHeader(text = stringResource(R.string.about_section_app))
        AppCard {
            AppRow(
                title = stringResource(R.string.about_version),
                subtitle = BuildConfig.VERSION_NAME,
                showChevron = false
            )
            AppRow(
                title = stringResource(R.string.about_rate),
                subtitle = stringResource(R.string.about_rate_sub),
                leadingIcon = iconStar,
                onClick = { openStoreListing(context) }
            )
            AppRow(
                title = stringResource(R.string.about_share),
                subtitle = stringResource(R.string.about_share_sub),
                leadingIcon = iconInfo,
                onClick = { shareApp(context) }
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
        SectionHeader(text = stringResource(R.string.about_section_legal))
        AppCard {
            AppRow(
                title = stringResource(R.string.about_privacy_policy),
                leadingIcon = iconLock,
                onClick = {
                    if (website.isNotBlank()) {
                        openUri(context, "$website/privacy", Intent.ACTION_VIEW)
                    } else {
                        actions.onOpenLegal(LEGAL_PRIVACY)
                    }
                }
            )
            AppRow(
                title = stringResource(R.string.about_terms_of_service),
                leadingIcon = iconInfo,
                onClick = {
                    if (website.isNotBlank()) {
                        openUri(context, "$website/terms", Intent.ACTION_VIEW)
                    } else {
                        actions.onOpenLegal(LEGAL_TERMS)
                    }
                }
            )
            AppRow(
                title = stringResource(R.string.about_licenses),
                leadingIcon = iconInfo,
                onClick = actions.onOpenLicenses
            )
        }
        Spacer(modifier = Modifier.height(Spacing.xl))
    }
}

private fun storeUrl() = PLAY_WEB_URL + BuildConfig.APPLICATION_ID

private fun openStoreListing(context: Context) {
    val market = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${BuildConfig.APPLICATION_ID}"))
    try {
        context.startActivity(market)
    } catch (notFound: ActivityNotFoundException) {
        Log.w(TAG, "Play Store app not available, opening the web listing", notFound)
        openUri(context, storeUrl(), Intent.ACTION_VIEW)
    }
}

private fun shareApp(context: Context) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_text, storeUrl()))
    }
    try {
        context.startActivity(Intent.createChooser(send, context.getString(R.string.share_chooser_title)))
    } catch (notFound: ActivityNotFoundException) {
        Log.w(TAG, "Nothing can share text", notFound)
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
