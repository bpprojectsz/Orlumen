package com.zdmgold.orlumen.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zdmgold.orlumen.core.theme.OrlumenTheme
import com.zdmgold.orlumen.core.viewmodel.MainViewModel
import com.zdmgold.orlumen.core.viewmodel.SettingsViewModel
import com.zdmgold.orlumen.navigation.AppNavHost
import com.zdmgold.orlumen.navigation.NavRoutes

@Composable
fun OrlumenApp() {
    val settings: SettingsViewModel = hiltViewModel()
    val main: MainViewModel = hiltViewModel()
    val preferences by settings.preferences.collectAsStateWithLifecycle()
    val mainState by main.uiState.collectAsStateWithLifecycle()
    OrlumenTheme(themeMode = preferences.themeMode) {
        Surface(modifier = Modifier.fillMaxSize(), color = OrlumenTheme.colors.bgPrimary) {
            // The start destination is decided once, when saved settings first arrive, so finishing
            // onboarding does not rebuild the navigation graph underneath the user.
            val startDestination = remember(mainState == null) {
                mainState?.let { if (it.onboardingCompleted) NavRoutes.DIAGNOSTICS else NavRoutes.ONBOARDING }
            }
            if (startDestination != null) {
                AppNavHost(
                    startDestination = startDestination,
                    onOnboardingFinished = main::completeOnboarding,
                    modifier = Modifier.systemBarsPadding()
                )
            }
        }
    }
}
