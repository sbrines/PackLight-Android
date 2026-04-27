package com.stephenbrines.packlight.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.stephenbrines.packlight.ui.gear.GearListScreen
import com.stephenbrines.packlight.ui.trips.TripListScreen
import com.stephenbrines.packlight.ui.weight.WeightDashboardScreen

sealed class Screen(val route: String, val label: String) {
    object Gear : Screen("gear", "Gear")
    object Trips : Screen("trips", "Trips")
    object Weight : Screen("weight", "Weight")
}

@Composable
fun PackLightNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDest = navBackStackEntry?.destination

    val tabs = listOf(Screen.Gear, Screen.Trips, Screen.Weight)
    val tabIcons = listOf(Icons.Default.Backpack, Icons.Default.Map, Icons.Default.Scale)

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, screen ->
                    NavigationBarItem(
                        icon = { Icon(tabIcons[index], contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentDest?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(navController = navController, startDestination = Screen.Gear.route) {
            composable(Screen.Gear.route) { GearListScreen(navController, padding) }
            composable(Screen.Trips.route) { TripListScreen(navController, padding) }
            composable(Screen.Weight.route) { WeightDashboardScreen(padding) }
        }
    }
}
