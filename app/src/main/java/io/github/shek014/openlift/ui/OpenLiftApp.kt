package io.github.shek014.openlift.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import io.github.shek014.openlift.data.AppSettings
import io.github.shek014.openlift.ui.body.BodyScreen
import io.github.shek014.openlift.ui.body.MuscleArg
import io.github.shek014.openlift.ui.body.MuscleScreen
import io.github.shek014.openlift.ui.exercises.ExerciseDetailScreen
import io.github.shek014.openlift.ui.exercises.ExerciseIdArg
import io.github.shek014.openlift.ui.exercises.ExerciseListScreen
import io.github.shek014.openlift.ui.history.EditWorkoutScreen
import io.github.shek014.openlift.ui.history.HistoryScreen
import io.github.shek014.openlift.ui.history.SavedWorkoutScreen
import io.github.shek014.openlift.ui.routines.NewRoutineId
import io.github.shek014.openlift.ui.routines.RoutineDetailScreen
import io.github.shek014.openlift.ui.routines.RoutineEditorScreen
import io.github.shek014.openlift.ui.routines.RoutineEditorViewModel
import io.github.shek014.openlift.ui.routines.RoutineIdArg
import io.github.shek014.openlift.ui.routines.RoutinesScreen
import io.github.shek014.openlift.ui.routines.SplitDetailScreen
import io.github.shek014.openlift.ui.routines.SplitIdArg
import io.github.shek014.openlift.ui.settings.SettingsScreen
import io.github.shek014.openlift.ui.workout.ReorderExercisesScreen
import io.github.shek014.openlift.ui.workout.ReorderItem
import io.github.shek014.openlift.ui.workout.ReorderScreen
import io.github.shek014.openlift.ui.workout.SavedWorkoutViewModel
import io.github.shek014.openlift.ui.workout.WorkoutIdArg
import io.github.shek014.openlift.ui.workout.WorkoutScreen
import io.github.shek014.openlift.ui.workout.WorkoutSummaryScreen
import io.github.shek014.openlift.ui.workout.WorkoutViewModel

private const val BodyHomeRoute = "body/home"
private const val WorkoutHomeRoute = "workout/home"
private const val HistoryHomeRoute = "history/home"
private const val RoutinesHomeRoute = "routines/home"
private const val EditRoutineRoute = "routines/edit/{$RoutineIdArg}"
private const val EditWorkoutRoute = "history/edit/{$WorkoutIdArg}"
private const val ExerciseListRoute = "exercises/list"

/**
 * The tabs in the bottom bar. [route] is the tab's internal address for navigation, and
 * [homeRoute] is the screen that tapping the tab returns to.
 */
private enum class Tab(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val homeRoute: String = route,
) {
    Body("body", "Body", Icons.Filled.Accessibility, homeRoute = BodyHomeRoute),
    Workout("workout", "Workout", Icons.Filled.FitnessCenter, homeRoute = WorkoutHomeRoute),
    Routines("routines", "Routines", Icons.AutoMirrored.Filled.List, homeRoute = RoutinesHomeRoute),
    History("history", "History", Icons.Filled.History, homeRoute = HistoryHomeRoute),
    Exercises("exercises", "Exercises", Icons.AutoMirrored.Filled.MenuBook, homeRoute = ExerciseListRoute),
}

@Composable
fun OpenLiftApp(settings: AppSettings) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Scaffold(
        bottomBar = {
            Column {
                HorizontalDivider(color = MaterialTheme.colorScheme.onBackground)
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
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Tab.Body.route,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        ) {
            navigation(startDestination = BodyHomeRoute, route = Tab.Body.route) {
                composable(BodyHomeRoute) {
                    BodyScreen(
                        onOpenSettings = { navController.navigate("body/settings") },
                        onMuscleClick = { muscle -> navController.navigate("body/muscle/${muscle.name}") },
                    )
                }
                composable("body/settings") {
                    SettingsScreen(settings = settings, onBack = navController::popBackStack)
                }
                composable("body/muscle/{$MuscleArg}") {
                    MuscleScreen(
                        onBack = navController::popBackStack,
                        onExerciseClick = { id -> navController.navigate("body/exercise/$id") },
                    )
                }
                exerciseDetail("body/exercise", navController)
            }
            navigation(startDestination = WorkoutHomeRoute, route = Tab.Workout.route) {
                composable(WorkoutHomeRoute) { entry ->
                    WorkoutScreen(
                        viewModel = workoutViewModel(navController, entry),
                        settings = settings,
                        onAddExercise = { navController.navigate("workout/add") },
                        onReorder = { navController.navigate("workout/reorder") },
                        onExerciseClick = { id -> navController.navigate("workout/exercise/$id") },
                        onWorkoutSaved = { id -> navController.navigate("workout/summary/$id") },
                    )
                }
                composable("workout/add") { entry ->
                    val viewModel = workoutViewModel(navController, entry)
                    val recentExercises by viewModel.recentExercises.collectAsStateWithLifecycle()
                    ExerciseListScreen(
                        title = "Add exercise",
                        recentExercises = recentExercises,
                        onBack = navController::popBackStack,
                        onExerciseClick = { id ->
                            viewModel.addExercise(id)
                            navController.popBackStack()
                        },
                    )
                }
                composable("workout/reorder") { entry ->
                    ReorderExercisesScreen(
                        viewModel = workoutViewModel(navController, entry),
                        onBack = navController::popBackStack,
                    )
                }
                composable("workout/summary/{$WorkoutIdArg}") {
                    WorkoutSummaryScreen(onDone = { navController.popBackStack(WorkoutHomeRoute, inclusive = false) })
                }
                exerciseDetail("workout/exercise", navController)
            }
            navigation(startDestination = RoutinesHomeRoute, route = Tab.Routines.route) {
                composable(RoutinesHomeRoute) {
                    RoutinesScreen(
                        onNewRoutine = { navController.navigate("routines/edit/$NewRoutineId") },
                        onRoutineClick = { id -> navController.navigate("routines/routine/$id") },
                        onSplitClick = { id -> navController.navigate("routines/split/$id") },
                    )
                }
                composable("routines/routine/{$RoutineIdArg}") { entry ->
                    RoutineDetailScreen(
                        onBack = navController::popBackStack,
                        onEdit = {
                            navController.navigate("routines/edit/${entry.arguments?.getString(RoutineIdArg)}")
                        },
                        onExerciseClick = { id -> navController.navigate("routines/exercise/$id") },
                        onWorkoutStarted = {
                            navController.navigate(Tab.Workout.route) {
                                popUpTo(navController.graph.findStartDestination().id)
                                launchSingleTop = true
                            }
                        },
                    )
                }
                composable("routines/split/{$SplitIdArg}") {
                    SplitDetailScreen(
                        onBack = navController::popBackStack,
                        onExerciseClick = { id -> navController.navigate("routines/exercise/$id") },
                        onAdded = { navController.popBackStack(RoutinesHomeRoute, inclusive = false) },
                    )
                }
                composable(EditRoutineRoute) { entry ->
                    RoutineEditorScreen(
                        viewModel = routineEditorViewModel(navController, entry),
                        onBack = navController::popBackStack,
                        onAddExercise = {
                            navController.navigate("routines/edit/${entry.arguments?.getString(RoutineIdArg)}/add")
                        },
                        onReorder = {
                            navController.navigate("routines/edit/${entry.arguments?.getString(RoutineIdArg)}/reorder")
                        },
                        onSaved = navController::popBackStack,
                    )
                }
                composable("$EditRoutineRoute/add") { entry ->
                    val viewModel = routineEditorViewModel(navController, entry)
                    val recentExercises by viewModel.recentExercises.collectAsStateWithLifecycle()
                    ExerciseListScreen(
                        title = "Add exercise",
                        recentExercises = recentExercises,
                        onBack = navController::popBackStack,
                        onExerciseClick = { id ->
                            viewModel.addExercise(id)
                            navController.popBackStack()
                        },
                    )
                }
                composable("$EditRoutineRoute/reorder") { entry ->
                    val viewModel = routineEditorViewModel(navController, entry)
                    ReorderScreen(
                        items = viewModel.exercises.map { ReorderItem(it.key, it.exercise.name) },
                        onSave = viewModel::reorder,
                        onBack = navController::popBackStack,
                    )
                }
                exerciseDetail("routines/exercise", navController)
            }
            navigation(startDestination = HistoryHomeRoute, route = Tab.History.route) {
                composable(HistoryHomeRoute) {
                    HistoryScreen(onWorkoutClick = { id -> navController.navigate("history/workout/$id") })
                }
                composable("history/workout/{$WorkoutIdArg}") { entry ->
                    SavedWorkoutScreen(
                        onBack = navController::popBackStack,
                        onEdit = {
                            navController.navigate("history/edit/${entry.arguments?.getString(WorkoutIdArg)}")
                        },
                    )
                }
                composable(EditWorkoutRoute) { entry ->
                    EditWorkoutScreen(
                        viewModel = editWorkoutViewModel(navController, entry),
                        onAddExercise = {
                            navController.navigate("history/edit/${entry.arguments?.getString(WorkoutIdArg)}/add")
                        },
                        onReorder = {
                            navController.navigate("history/edit/${entry.arguments?.getString(WorkoutIdArg)}/reorder")
                        },
                        onExerciseClick = { id -> navController.navigate("history/exercise/$id") },
                        onDone = { stillExists ->
                            if (stillExists) {
                                navController.popBackStack()
                            } else {
                                navController.popBackStack(HistoryHomeRoute, inclusive = false)
                            }
                        },
                    )
                }
                composable("$EditWorkoutRoute/add") { entry ->
                    val viewModel = editWorkoutViewModel(navController, entry)
                    val recentExercises by viewModel.recentExercises.collectAsStateWithLifecycle()
                    ExerciseListScreen(
                        title = "Add exercise",
                        recentExercises = recentExercises,
                        onBack = navController::popBackStack,
                        onExerciseClick = { id ->
                            viewModel.addExercise(id)
                            navController.popBackStack()
                        },
                    )
                }
                composable("$EditWorkoutRoute/reorder") { entry ->
                    ReorderExercisesScreen(
                        viewModel = editWorkoutViewModel(navController, entry),
                        onBack = navController::popBackStack,
                    )
                }
                exerciseDetail("history/exercise", navController)
            }
            navigation(startDestination = ExerciseListRoute, route = Tab.Exercises.route) {
                composable(ExerciseListRoute) {
                    ExerciseListScreen(
                        onExerciseClick = { id -> navController.navigate("exercises/detail/$id") },
                    )
                }
                exerciseDetail("exercises/detail", navController)
            }
        }
    }
}

/** The workout's state, shared by all the workout screens so that they stay in step. */
@Composable
private fun workoutViewModel(navController: NavController, entry: NavBackStackEntry): WorkoutViewModel {
    val workoutGraph = remember(entry) { navController.getBackStackEntry(Tab.Workout.route) }
    return viewModel(workoutGraph, factory = WorkoutViewModel.Factory)
}

/** The routine being edited, shared by the editor and the screens it opens. */
@Composable
private fun routineEditorViewModel(navController: NavController, entry: NavBackStackEntry): RoutineEditorViewModel {
    val editEntry = remember(entry) { navController.getBackStackEntry(EditRoutineRoute) }
    return viewModel(editEntry, factory = RoutineEditorViewModel.Factory)
}

/** The saved workout being edited, shared by the edit screen and the screens it opens. */
@Composable
private fun editWorkoutViewModel(navController: NavController, entry: NavBackStackEntry): SavedWorkoutViewModel {
    val editEntry = remember(entry) { navController.getBackStackEntry(EditWorkoutRoute) }
    return viewModel(editEntry, factory = SavedWorkoutViewModel.Factory)
}

/** The exercise page, added to each tab that opens it so that the tab stays selected. */
private fun NavGraphBuilder.exerciseDetail(route: String, navController: NavController) {
    composable("$route/{$ExerciseIdArg}") {
        ExerciseDetailScreen(onBack = navController::popBackStack)
    }
}
