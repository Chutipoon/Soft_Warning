package com.softwarn.app.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.softwarn.app.ui.screen.AppLimitsScreen
import com.softwarn.app.ui.screen.HomeScreen
import com.softwarn.app.ui.screen.NotificationLogScreen
import com.softwarn.app.ui.screen.SettingsScreen
import com.softwarn.app.ui.screen.StatsScreen

private sealed class Destination(val route: String, val label: String, val icon: ImageVector) {
    data object Home : Destination("home", "หน้าหลัก", Icons.Filled.Home)
    data object Stats : Destination("stats", "สถิติ", Icons.Filled.DateRange)
    data object Limits : Destination("limits", "จำกัดแอป", Icons.AutoMirrored.Filled.List)
    data object Log : Destination("log", "ประวัติ", Icons.Filled.Notifications)
    data object Settings : Destination("settings", "ตั้งค่า", Icons.Filled.Settings)
}

private val bottomNavDestinations =
    listOf(Destination.Home, Destination.Stats, Destination.Limits, Destination.Log, Destination.Settings)

@Composable
fun SoftWarningNavHost() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route

            NavigationBar {
                bottomNavDestinations.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = destination.icon, contentDescription = destination.label) },
                        label = { Text(destination.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Destination.Home.route) { HomeScreen() }
            composable(Destination.Stats.route) { StatsScreen() }
            composable(Destination.Limits.route) { AppLimitsScreen() }
            composable(Destination.Log.route) { NotificationLogScreen() }
            composable(Destination.Settings.route) { SettingsScreen() }
        }
    }
}
