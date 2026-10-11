package com.zdmgold.orlumen.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zdmgold.orlumen.ui.screens.about.AboutAndLegalScreen

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = NavRoutes.ABOUT, modifier = modifier) {
        composable(NavRoutes.ABOUT) {
            AboutAndLegalScreen()
        }
    }
}
