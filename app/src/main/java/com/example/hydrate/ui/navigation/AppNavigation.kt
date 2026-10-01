package com.example.hydrate.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.Modifier
import com.example.hydrate.ui.screens.home.HomeScreen
import com.example.hydrate.ui.screens.messages.MessagesScreen
import com.example.hydrate.ui.screens.onboarding.OnboardingScreen
import com.example.hydrate.ui.screens.progress.ProgressScreen
import com.example.hydrate.ui.screens.settings.SettingsScreen
import com.example.hydrate.ui.screens.splash.SplashScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Progress : Screen("progress")
    object Messages : Screen("messages")
    object Settings : Screen("settings")
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Splash.route,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            SplashScreen()
        }
        composable(Screen.Onboarding.route) {
            OnboardingScreen()
        }
        composable(Screen.Home.route) {
            HomeScreen()
        }
        composable(Screen.Progress.route) {
            ProgressScreen()
        }
        composable(Screen.Messages.route) {
            MessagesScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
