package io.github.shek014.openlift.ui

import androidx.compose.foundation.layout.consumeWindowInsets
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
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import io.github.shek014.openlift.ui.exercises.ExerciseDetailScreen
import io.github.shek014.openlift.ui.exercises.ExerciseIdArg
import io.github.shek014.openlift.ui.exercises.ExerciseListScreen
import io.github.shek014.openlift.ui.screens.HistoryScreen
import io.github.shek014.openlift.ui.screens.RoutinesScreen
import io.github.shek014.openlift.ui.screens.WorkoutScreen

private const val ExerciseListRoute = "exercises/list"

/**
 * The four tabs in the bottom bar. [route] is the tab's internal address for navigation, and
 * [homeRoute] is the screen that tapping the tab returns to.
 */
private enum class Tab(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val homeRoute: String = route,
) {
    Workout("workout", "Workout", Icons.Filled.FitnessCenter),
    Routines("routines", "Routines", Icons.AutoMirrored.Filled.List),
    History("history", "History", Icons.Filled.History),
    Exercises("exercises", "Exercises", Icons.Filled.Accessibility, homeRoute = ExerciseListRoute),
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
                    val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (selected) {
                                navController.popBackStack(tab.homeRoute, inclusive = false)
                            } else {
                                navController.navigate(tab.route) {
                                    // Keep one copy of each tab and remember where you were in it.
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
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
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        ) {
            composable(Tab.Workout.route) { WorkoutScreen() }
            composable(Tab.Routines.route) { RoutinesScreen() }
            composable(Tab.History.route) { HistoryScreen() }
            navigation(startDestination = ExerciseListRoute, route = Tab.Exercises.route) {
                composable(ExerciseListRoute) {
                    ExerciseListScreen(
                        onExerciseClick = { id -> navController.navigate("exercises/detail/$id") },
                    )
                }
                composable("exercises/detail/{$ExerciseIdArg}") {
                    ExerciseDetailScreen(onBack = navController::popBackStack)
                }
            }
        }
    }
}
