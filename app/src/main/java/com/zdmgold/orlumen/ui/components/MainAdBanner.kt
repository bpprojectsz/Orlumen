package com.zdmgold.orlumen.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zdmgold.orlumen.core.viewmodel.MainViewModel

/** The app-wide banner. It takes no space unless ads are allowed and a banner has loaded. */
@Composable
fun MainAdBanner(modifier: Modifier = Modifier) {
    val activity = LocalContext.current.findActivity() as? ComponentActivity ?: return
    val main: MainViewModel = hiltViewModel(viewModelStoreOwner = activity)
    val state by main.uiState.collectAsStateWithLifecycle()
    AdBannerSlot(adsAllowed = state?.adsAllowed == true, adAdapter = main.adAdapter, modifier = modifier)
}
