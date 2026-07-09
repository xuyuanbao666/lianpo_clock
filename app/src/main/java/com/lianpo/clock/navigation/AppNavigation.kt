package com.lianpo.clock.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lianpo.clock.ui.timer.TimerScreen
import com.lianpo.clock.ui.tasks.TaskListScreen
import com.lianpo.clock.ui.statistics.StatisticsScreen
import com.lianpo.clock.ui.settings.SettingsScreen
import com.lianpo.clock.ui.privatetracker.PrivateScreen

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Timer : Screen("timer", "计时器", Icons.Default.Schedule)
    data object Tasks : Screen("tasks", "任务", Icons.Default.Done)
    data object Statistics : Screen("statistics", "统计", Icons.Default.DateRange)
    data object Settings : Screen("settings", "设置", Icons.Default.Settings)
}

private val screens = listOf(
    Screen.Timer,
    Screen.Tasks,
    Screen.Statistics,
    Screen.Settings
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentScreen = screens.find { it.route == currentDestination?.route } ?: Screen.Timer

    Scaffold(
        topBar = {
            if (currentDestination?.route != "private") {
                TopAppBar(
                    title = { Text(currentScreen.title) }
                )
            }
        },
        bottomBar = {
            if (currentDestination?.route != "private") {
                NavigationBar {
                    screens.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
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
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Timer.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Timer.route) { TimerScreen() }
            composable(Screen.Tasks.route) { TaskListScreen() }
            composable(Screen.Statistics.route) {
                StatisticsScreen(
                    onNavigateToPrivate = {
                        navController.navigate("private")
                    }
                )
            }
            composable(Screen.Settings.route) { SettingsScreen() }
            composable("private") {
                PrivateScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}