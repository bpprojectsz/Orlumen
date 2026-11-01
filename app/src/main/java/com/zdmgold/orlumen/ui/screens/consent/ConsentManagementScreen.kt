package com.zdmgold.orlumen.ui.screens.consent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zdmgold.orlumen.R
import com.zdmgold.orlumen.core.theme.OrlumenTheme
import com.zdmgold.orlumen.core.theme.Spacing
import com.zdmgold.orlumen.core.viewmodel.ConsentViewModel
import com.zdmgold.orlumen.platform.compliance.ConsentStatus
import com.zdmgold.orlumen.ui.components.AppCard
import com.zdmgold.orlumen.ui.components.AppPrimaryButton
import com.zdmgold.orlumen.ui.components.ScreenHeader
import com.zdmgold.orlumen.ui.components.findActivity

@Composable
fun ConsentManagementScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConsentViewModel = hiltViewModel()
) {
    val consent by viewModel.consent.collectAsStateWithLifecycle()
    val activity = LocalContext.current.findActivity()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.lg)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        ScreenHeader(title = stringResource(R.string.consent_title), onBack = onBack)
        AppCard {
            Text(
                text = stringResource(
                    when (consent.status) {
                        ConsentStatus.OBTAINED -> R.string.consent_status_obtained
                        ConsentStatus.REQUIRED -> R.string.consent_status_required
                        ConsentStatus.NOT_REQUIRED -> R.string.consent_status_not_required
                        ConsentStatus.UNKNOWN -> R.string.consent_status_unknown
                    }
                ),
                style = OrlumenTheme.typography.body,
                color = OrlumenTheme.colors.textPrimary
            )
        }
        Text(
            text = stringResource(R.string.consent_body),
            style = OrlumenTheme.typography.caption,
            color = OrlumenTheme.colors.textTertiary
        )
        if (consent.status == ConsentStatus.REQUIRED || consent.status == ConsentStatus.UNKNOWN) {
            AppPrimaryButton(
                text = stringResource(R.string.consent_review),
                enabled = activity != null,
                onClick = { activity?.let(viewModel::gatherConsent) }
            )
        }
        if (consent.privacyOptionsRequired) {
            AppPrimaryButton(
                text = stringResource(R.string.consent_privacy_options),
                enabled = activity != null,
                onClick = { activity?.let(viewModel::showPrivacyOptions) }
            )
        }
    }
}
