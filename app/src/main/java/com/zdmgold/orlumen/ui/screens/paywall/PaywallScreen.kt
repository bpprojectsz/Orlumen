package com.zdmgold.orlumen.ui.screens.paywall

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zdmgold.orlumen.R
import com.zdmgold.orlumen.core.theme.OrlumenTheme
import com.zdmgold.orlumen.core.theme.Spacing
import com.zdmgold.orlumen.core.viewmodel.EntitlementViewModel
import com.zdmgold.orlumen.platform.monetization.PurchaseEvent
import com.zdmgold.orlumen.platform.monetization.PurchaseLaunchResult
import com.zdmgold.orlumen.ui.components.AppCard
import com.zdmgold.orlumen.ui.components.AppPrimaryButton
import com.zdmgold.orlumen.ui.components.ScreenHeader
import com.zdmgold.orlumen.ui.components.findActivity

@Composable
fun PaywallScreen(onBack: () -> Unit, modifier: Modifier = Modifier, viewModel: EntitlementViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalContext.current.findActivity()
    var message by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(state.lastEvent) {
        val event = state.lastEvent ?: return@LaunchedEffect
        message = when (event) {
            PurchaseEvent.PURCHASED -> R.string.paywall_msg_purchased
            PurchaseEvent.PENDING -> R.string.paywall_msg_pending
            PurchaseEvent.CANCELED -> R.string.paywall_msg_canceled
            PurchaseEvent.FAILED -> R.string.paywall_msg_failed
        }
        viewModel.consumeEvent()
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.lg)
            .verticalScroll(rememberScrollState())
    ) {
        ScreenHeader(title = stringResource(R.string.paywall_title), onBack = onBack)
        AppCard {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text(
                    text = stringResource(R.string.paywall_headline),
                    style = OrlumenTheme.typography.headline,
                    color = OrlumenTheme.colors.textPrimary
                )
                Text(
                    text = stringResource(R.string.paywall_body),
                    style = OrlumenTheme.typography.body,
                    color = OrlumenTheme.colors.textSecondary
                )
                Text(
                    text = stringResource(R.string.paywall_one_time),
                    style = OrlumenTheme.typography.caption,
                    color = OrlumenTheme.colors.textTertiary
                )
            }
        }
        Spacer(modifier = Modifier.height(Spacing.lg))
        if (state.isPro) {
            Text(
                text = stringResource(R.string.paywall_owned),
                style = OrlumenTheme.typography.headline,
                color = OrlumenTheme.colors.accent
            )
        } else {
            val product = state.product
            AppPrimaryButton(
                text = if (product != null) {
                    stringResource(R.string.paywall_buy, product.formattedPrice)
                } else {
                    stringResource(R.string.paywall_unavailable_button)
                },
                enabled = product != null && activity != null,
                onClick = {
                    val host = activity ?: return@AppPrimaryButton
                    message = when (viewModel.purchase(host)) {
                        PurchaseLaunchResult.LAUNCHED -> null
                        PurchaseLaunchResult.UNAVAILABLE -> R.string.paywall_msg_unavailable
                        PurchaseLaunchResult.ALREADY_OWNED -> R.string.paywall_msg_owned
                        PurchaseLaunchResult.FAILED -> R.string.paywall_msg_failed
                    }
                }
            )
            TextButton(onClick = viewModel::restore) {
                Text(text = stringResource(R.string.paywall_restore))
            }
        }
        message?.let {
            Text(
                text = stringResource(it),
                style = OrlumenTheme.typography.body,
                color = OrlumenTheme.colors.textSecondary,
                modifier = Modifier.padding(top = Spacing.md)
            )
        }
        Spacer(modifier = Modifier.height(Spacing.xl))
    }
}
