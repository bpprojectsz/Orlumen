package com.zdmgold.orlumen.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.zdmgold.orlumen.platform.monetization.AdAdapter

private enum class BannerState {
    LOADING,
    LOADED,
    FAILED
}

/** An adaptive banner that takes no space unless ads are allowed, and none after a failed load. */
@Composable
fun AdBannerSlot(adsAllowed: Boolean, adAdapter: AdAdapter, modifier: Modifier = Modifier) {
    if (adsAllowed) AdBannerContent(adAdapter, modifier)
}

@Composable
private fun AdBannerContent(adAdapter: AdAdapter, modifier: Modifier) {
    val context = LocalContext.current
    val widthDp = LocalConfiguration.current.screenWidthDp
    var state by remember(widthDp) { mutableStateOf(BannerState.LOADING) }
    val adView = remember(widthDp) {
        adAdapter.createBanner(context, widthDp) { loaded ->
            state = if (loaded) BannerState.LOADED else BannerState.FAILED
        }
    }
    DisposableEffect(adView) { onDispose { adView.destroy() } }
    if (state != BannerState.FAILED) {
        AndroidView(factory = { adView }, modifier = modifier.fillMaxWidth())
    }
}
