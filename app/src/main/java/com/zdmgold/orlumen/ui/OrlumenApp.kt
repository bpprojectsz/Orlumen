package com.zdmgold.orlumen.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zdmgold.orlumen.core.model.ThemeMode
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
    SystemBarIcons(themeMode = preferences.themeMode)
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

@Composable
private fun SystemBarIcons(themeMode: ThemeMode) {
    val dark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val view = LocalView.current
    SideEffect {
        val window = view.context.findActivity()?.window ?: return@SideEffect
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !dark
            isAppearanceLightNavigationBars = !dark
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
