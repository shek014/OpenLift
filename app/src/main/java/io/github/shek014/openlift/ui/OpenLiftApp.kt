package io.github.shek014.openlift.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import io.github.shek014.openlift.ui.screens.ExercisesScreen
import io.github.shek014.openlift.ui.screens.HistoryScreen
import io.github.shek014.openlift.ui.screens.RoutinesScreen
import io.github.shek014.openlift.ui.screens.WorkoutScreen

/** The four tabs in the bottom bar. [route] is the tab's internal address for navigation. */
private enum class Tab(val route: String, val label: String, val icon: ImageVector) {
    Workout("workout", "Workout", Icons.Filled.FitnessCenter),
    Routines("routines", "Routines", Icons.AutoMirrored.Filled.List),
    History("history", "History", Icons.Filled.History),
    Exercises("exercises", "Exercises", Icons.Filled.Accessibility),
}

@Composable
fun OpenLiftApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true,
                        onClick = {
                            navController.navigate(tab.route) {
                                // Keep one copy of each tab and remember where you were in it.
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Tab.Workout.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Tab.Workout.route) { WorkoutScreen() }
            composable(Tab.Routines.route) { RoutinesScreen() }
            composable(Tab.History.route) { HistoryScreen() }
            composable(Tab.Exercises.route) { ExercisesScreen() }
        }
    }
}
