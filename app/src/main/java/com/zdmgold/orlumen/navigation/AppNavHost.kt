package com.zdmgold.orlumen.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.zdmgold.orlumen.core.viewmodel.AppPickerTarget
import com.zdmgold.orlumen.ui.components.MainAdBanner
import com.zdmgold.orlumen.ui.screens.about.AboutAndLegalScreen
import com.zdmgold.orlumen.ui.screens.aod.AodEditorScreen
import com.zdmgold.orlumen.ui.screens.aod.AodGalleryScreen
import com.zdmgold.orlumen.ui.screens.consent.ConsentManagementScreen
import com.zdmgold.orlumen.ui.screens.diagnostics.DiagnosticsScreen
import com.zdmgold.orlumen.ui.screens.home.HomeActions
import com.zdmgold.orlumen.ui.screens.home.HomeScreen
import com.zdmgold.orlumen.ui.screens.onboarding.OnboardingScreen
import com.zdmgold.orlumen.ui.screens.palette.ColorPaletteScreen
import com.zdmgold.orlumen.ui.screens.paywall.PaywallScreen
import com.zdmgold.orlumen.ui.screens.permissions.PermissionHubScreen
import com.zdmgold.orlumen.ui.screens.settings.AppPickerScreen
import com.zdmgold.orlumen.ui.screens.settings.SettingsActions
import com.zdmgold.orlumen.ui.screens.settings.SettingsScreen
import com.zdmgold.orlumen.ui.screens.visualizer.VisualizerEditorScreen
import com.zdmgold.orlumen.ui.screens.visualizer.VisualizerGalleryScreen

private const val AUTO_START_KEY = "autoStart"

@Composable
fun AppNavHost(startDestination: String, onOnboardingFinished: () -> Unit, modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    Column(modifier = modifier) {
        AppNavGraph(
            navController = navController,
            startDestination = startDestination,
            onOnboardingFinished = onOnboardingFinished,
            modifier = Modifier.weight(1f)
        )
        if (backStackEntry?.destination?.route in AD_ROUTES) MainAdBanner()
    }
}

private val AD_ROUTES = setOf(
    NavRoutes.HOME,
    NavRoutes.VISUALIZER_GALLERY,
    NavRoutes.AOD_GALLERY,
    NavRoutes.PALETTES
)

@Composable
private fun AppNavGraph(
    navController: NavHostController,
    startDestination: String,
    onOnboardingFinished: () -> Unit,
    modifier: Modifier
) {
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
                    onOpenAbout = { navController.navigate(NavRoutes.ABOUT) },
                    onOpenSettings = { navController.navigate(NavRoutes.SETTINGS) },
                    onOpenPaywall = { navController.navigate(NavRoutes.PAYWALL) }
                )
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
        designDestinations(navController)
        settingsDestinations(navController)
        composable(NavRoutes.ABOUT) {
            AboutAndLegalScreen(onBack = { navController.popBackStack() })
        }
    }
}

private fun NavGraphBuilder.designDestinations(navController: NavHostController) {
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
}

private fun NavGraphBuilder.settingsDestinations(navController: NavHostController) {
    composable(NavRoutes.SETTINGS) {
        SettingsScreen(
            onBack = { navController.popBackStack() },
            actions = SettingsActions(
                onOpenVisualizerApps = {
                    navController.navigate(NavRoutes.appPicker(AppPickerTarget.VISUALIZER_APPS.name))
                },
                onOpenMusicSources = {
                    navController.navigate(NavRoutes.appPicker(AppPickerTarget.MUSIC_SOURCES.name))
                },
                onOpenConsent = { navController.navigate(NavRoutes.CONSENT) }
            )
        )
    }
    composable(
        NavRoutes.APP_PICKER,
        arguments = listOf(navArgument(NavRoutes.ARG_PICKER_TARGET) { type = NavType.StringType })
    ) { entry ->
        val target = entry.arguments?.getString(NavRoutes.ARG_PICKER_TARGET)
            ?.let { name -> AppPickerTarget.entries.firstOrNull { it.name == name } }
            ?: AppPickerTarget.VISUALIZER_APPS
        AppPickerScreen(target = target, onBack = { navController.popBackStack() })
    }
    composable(NavRoutes.PAYWALL) { PaywallScreen(onBack = { navController.popBackStack() }) }
    composable(NavRoutes.CONSENT) { ConsentManagementScreen(onBack = { navController.popBackStack() }) }
}
