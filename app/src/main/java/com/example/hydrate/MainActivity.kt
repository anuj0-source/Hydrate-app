package com.example.hydrate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import com.example.hydrate.ui.navigation.AppNavigation
import com.example.hydrate.ui.navigation.Screen
import com.example.hydrate.ui.theme.DarkPlum
import com.example.hydrate.ui.theme.HydrateTheme
import com.example.hydrate.ui.theme.PastelRose
import com.example.hydrate.ui.theme.WhiteCard

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HydrateTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                
                val showBottomNav = currentRoute in listOf(
                    Screen.Home.route,
                    Screen.Progress.route,
                    Screen.Messages.route,
                    Screen.Settings.route
                )

                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = {
                        if (showBottomNav) {
                            NavigationBar(
                                containerColor = WhiteCard,
                                tonalElevation = 8.dp
                            ) {
                                val colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = WhiteCard,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                    unselectedTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                )
                                NavigationBarItem(
                                    selected = currentRoute == Screen.Home.route,
                                    onClick = { navController.navigateToTopLevel(Screen.Home.route) },
                                    icon = { Icon(Icons.Default.Home, "Home") },
                                    label = { Text("Home") },
                                    colors = colors
                                )
                                NavigationBarItem(
                                    selected = currentRoute == Screen.Progress.route,
                                    onClick = { navController.navigateToTopLevel(Screen.Progress.route) },
                                    icon = { Icon(Icons.Default.Timeline, "Progress") },
                                    label = { Text("Progress") },
                                    colors = colors
                                )
                                NavigationBarItem(
                                    selected = currentRoute == Screen.Messages.route,
                                    onClick = { navController.navigateToTopLevel(Screen.Messages.route) },
                                    icon = { Icon(Icons.AutoMirrored.Filled.Message, "Messages") },
                                    label = { Text("Messages") },
                                    colors = colors
                                )
                                NavigationBarItem(
                                    selected = currentRoute == Screen.Settings.route,
                                    onClick = { navController.navigateToTopLevel(Screen.Settings.route) },
                                    icon = { Icon(Icons.Default.Settings, "Settings") },
                                    label = { Text("Settings") },
                                    colors = colors
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    AppNavigation(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun androidx.navigation.NavHostController.navigateToTopLevel(route: String) {
        navigate(route) {
            popUpTo(graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
}
