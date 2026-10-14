package com.zdmgold.orlumen.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zdmgold.orlumen.ui.screens.about.AboutAndLegalScreen
import com.zdmgold.orlumen.ui.screens.diagnostics.DiagnosticsScreen
import com.zdmgold.orlumen.ui.screens.onboarding.OnboardingScreen
import com.zdmgold.orlumen.ui.screens.permissions.PermissionHubScreen

@Composable
fun AppNavHost(startDestination: String, onOnboardingFinished: () -> Unit, modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = startDestination, modifier = modifier) {
        composable(NavRoutes.ONBOARDING) {
            OnboardingScreen(
                onFinished = {
                    onOnboardingFinished()
                    navController.navigate(NavRoutes.PERMISSIONS) {
                        popUpTo(NavRoutes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }
        composable(NavRoutes.PERMISSIONS) {
            val canGoBack = navController.previousBackStackEntry != null
            PermissionHubScreen(
                onDone = {
                    navController.navigate(NavRoutes.DIAGNOSTICS) {
                        popUpTo(NavRoutes.PERMISSIONS) { inclusive = true }
                    }
                },
                onBack = if (canGoBack) ({ navController.popBackStack() }) else null
            )
        }
        composable(NavRoutes.DIAGNOSTICS) {
            DiagnosticsScreen(
                onOpenPermissions = { navController.navigate(NavRoutes.PERMISSIONS) },
                onOpenAbout = { navController.navigate(NavRoutes.ABOUT) }
            )
        }
        composable(NavRoutes.ABOUT) {
            AboutAndLegalScreen(onBack = { navController.popBackStack() })
        }
    }
}
