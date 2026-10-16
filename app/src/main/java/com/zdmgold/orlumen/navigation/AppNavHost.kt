package com.zdmgold.orlumen.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zdmgold.orlumen.ui.screens.about.AboutAndLegalScreen
import com.zdmgold.orlumen.ui.screens.diagnostics.DiagnosticsScreen
import com.zdmgold.orlumen.ui.screens.onboarding.OnboardingScreen
import com.zdmgold.orlumen.ui.screens.permissions.PermissionHubScreen

private const val AUTO_START_KEY = "autoStart"

@Composable
fun AppNavHost(startDestination: String, onOnboardingFinished: () -> Unit, modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = startDestination, modifier = modifier) {
        composable(NavRoutes.ONBOARDING) {
            OnboardingScreen(
                onFinished = {
                    onOnboardingFinished()
                    navController.navigate(NavRoutes.DIAGNOSTICS) {
                        popUpTo(NavRoutes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }
        composable(NavRoutes.PERMISSIONS) {
            PermissionHubScreen(
                onDone = {
                    navController.previousBackStackEntry?.savedStateHandle?.set(AUTO_START_KEY, true)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(NavRoutes.DIAGNOSTICS) { entry ->
            val autoStart by entry.savedStateHandle.getStateFlow(AUTO_START_KEY, false).collectAsStateWithLifecycle()
            DiagnosticsScreen(
                autoStart = autoStart,
                onAutoStartHandled = { entry.savedStateHandle[AUTO_START_KEY] = false },
                onNeedPermissions = { navController.navigate(NavRoutes.PERMISSIONS) },
                onOpenAbout = { navController.navigate(NavRoutes.ABOUT) }
            )
        }
        composable(NavRoutes.ABOUT) {
            AboutAndLegalScreen(onBack = { navController.popBackStack() })
        }
    }
}
