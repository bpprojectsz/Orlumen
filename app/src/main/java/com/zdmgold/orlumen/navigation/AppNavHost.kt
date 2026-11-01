package com.zdmgold.orlumen.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.zdmgold.orlumen.ui.screens.about.AboutAndLegalScreen
import com.zdmgold.orlumen.ui.screens.aod.AodEditorScreen
import com.zdmgold.orlumen.ui.screens.aod.AodGalleryScreen
import com.zdmgold.orlumen.ui.screens.diagnostics.DiagnosticsScreen
import com.zdmgold.orlumen.ui.screens.home.HomeActions
import com.zdmgold.orlumen.ui.screens.home.HomeScreen
import com.zdmgold.orlumen.ui.screens.onboarding.OnboardingScreen
import com.zdmgold.orlumen.ui.screens.palette.ColorPaletteScreen
import com.zdmgold.orlumen.ui.screens.permissions.PermissionHubScreen
import com.zdmgold.orlumen.ui.screens.visualizer.VisualizerEditorScreen
import com.zdmgold.orlumen.ui.screens.visualizer.VisualizerGalleryScreen

private const val AUTO_START_KEY = "autoStart"

@Composable
fun AppNavHost(startDestination: String, onOnboardingFinished: () -> Unit, modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = startDestination, modifier = modifier) {
        composable(NavRoutes.ONBOARDING) {
            OnboardingScreen(
                onFinished = {
                    onOnboardingFinished()
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }
        composable(NavRoutes.HOME) { entry ->
            val autoStart by entry.savedStateHandle.getStateFlow(AUTO_START_KEY, false).collectAsStateWithLifecycle()
            HomeScreen(
                autoStart = autoStart,
                onAutoStartHandled = { entry.savedStateHandle[AUTO_START_KEY] = false },
                actions = HomeActions(
                    onNeedPermissions = { navController.navigate(NavRoutes.PERMISSIONS) },
                    onOpenVisualizers = { navController.navigate(NavRoutes.VISUALIZER_GALLERY) },
                    onOpenAod = { navController.navigate(NavRoutes.AOD_GALLERY) },
                    onOpenPalettes = { navController.navigate(NavRoutes.PALETTES) },
                    onOpenDiagnostics = { navController.navigate(NavRoutes.DIAGNOSTICS) },
                    onOpenAbout = { navController.navigate(NavRoutes.ABOUT) }
                )
            )
        }
        composable(NavRoutes.VISUALIZER_GALLERY) {
            VisualizerGalleryScreen(
                onBack = { navController.popBackStack() },
                onOpenEditor = { navController.navigate(NavRoutes.visualizerEditor(it)) }
            )
        }
        composable(
            NavRoutes.VISUALIZER_EDITOR,
            arguments = listOf(navArgument(NavRoutes.ARG_DESIGN_ID) { type = NavType.StringType })
        ) { entry ->
            VisualizerEditorScreen(
                designId = entry.arguments?.getString(NavRoutes.ARG_DESIGN_ID).orEmpty(),
                onBack = { navController.popBackStack() }
            )
        }
        composable(NavRoutes.AOD_GALLERY) {
            AodGalleryScreen(
                onBack = { navController.popBackStack() },
                onOpenEditor = { navController.navigate(NavRoutes.aodEditor(it)) }
            )
        }
        composable(
            NavRoutes.AOD_EDITOR,
            arguments = listOf(navArgument(NavRoutes.ARG_DESIGN_ID) { type = NavType.StringType })
        ) { entry ->
            AodEditorScreen(
                designId = entry.arguments?.getString(NavRoutes.ARG_DESIGN_ID).orEmpty(),
                onBack = { navController.popBackStack() }
            )
        }
        composable(NavRoutes.PALETTES) {
            ColorPaletteScreen(onBack = { navController.popBackStack() })
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
