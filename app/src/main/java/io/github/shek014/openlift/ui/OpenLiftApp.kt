package io.github.shek014.openlift.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
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
import io.github.shek014.openlift.ui.exercises.CustomExerciseScreen
import io.github.shek014.openlift.ui.exercises.ExerciseDetailScreen
import io.github.shek014.openlift.ui.exercises.ExerciseIdArg
import io.github.shek014.openlift.ui.exercises.ExerciseListScreen
import io.github.shek014.openlift.ui.exercises.NewExerciseId
import io.github.shek014.openlift.ui.history.EditWorkoutScreen
import io.github.shek014.openlift.ui.history.HistoryScreen
import io.github.shek014.openlift.ui.history.SavedWorkoutScreen
import io.github.shek014.openlift.ui.history.StatisticsScreen
import io.github.shek014.openlift.ui.home.HomeScreen
import io.github.shek014.openlift.ui.measurements.MeasurementDetailScreen
import io.github.shek014.openlift.ui.measurements.MeasurementTypeArg
import io.github.shek014.openlift.ui.measurements.MeasurementsScreen
import io.github.shek014.openlift.ui.profile.ProfileScreen
import io.github.shek014.openlift.ui.routines.NewRoutineId
import io.github.shek014.openlift.ui.routines.RoutineDetailScreen
import io.github.shek014.openlift.ui.routines.RoutineEditorScreen
import io.github.shek014.openlift.ui.routines.RoutineEditorViewModel
import io.github.shek014.openlift.ui.routines.RoutineIdArg
import io.github.shek014.openlift.ui.routines.RoutinesScreen
import io.github.shek014.openlift.ui.routines.SplitDetailScreen
import io.github.shek014.openlift.ui.routines.SplitIdArg
import io.github.shek014.openlift.ui.settings.SettingsScreen
import io.github.shek014.openlift.ui.workout.ActiveWorkoutBar
import io.github.shek014.openlift.ui.workout.ReorderExercisesScreen
import io.github.shek014.openlift.ui.workout.ReorderItem
import io.github.shek014.openlift.ui.workout.ReorderScreen
import io.github.shek014.openlift.ui.workout.SavedWorkoutViewModel
import io.github.shek014.openlift.ui.workout.WorkoutIdArg
import io.github.shek014.openlift.ui.workout.WorkoutScreen
import io.github.shek014.openlift.ui.workout.WorkoutSummaryScreen
import io.github.shek014.openlift.ui.workout.WorkoutViewModel

private const val HomeRoute = "home/home"
private const val BodyRoute = "body/home"
private const val WorkoutHomeRoute = "workout/home"
private const val ProfileRoute = "profile/home"
private const val HistoryHomeRoute = "history/home"
private const val RoutinesHomeRoute = "routines/home"
private const val EditRoutineRoute = "routines/edit/{$RoutineIdArg}"
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
    Home("home", "Home", Icons.Filled.Home, homeRoute = HomeRoute),
    Workout("workout", "Workout", Icons.Filled.FitnessCenter, homeRoute = WorkoutHomeRoute),
    Routines("routines", "Routines", Icons.AutoMirrored.Filled.List, homeRoute = RoutinesHomeRoute),
    Profile("profile", "Profile", Icons.Filled.Person, homeRoute = ProfileRoute),
}

@Composable
fun OpenLiftApp(settings: AppSettings) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Scaffold(
        bottomBar = {
            Column {
                val onWorkoutTab = currentDestination?.hierarchy?.any { it.route == Tab.Workout.route } == true
                if (!onWorkoutTab) {
                    ActiveWorkoutBar(onClick = { navController.openTab(Tab.Workout) })
                }
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
                                    navController.openTab(tab)
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
            startDestination = Tab.Home.route,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        ) {
            navigation(startDestination = HomeRoute, route = Tab.Home.route) {
                composable(HomeRoute) {
                    HomeScreen(
                        onWorkoutClick = { id -> navController.navigate("home/workout/$id") },
                        onEditWorkout = { id -> navController.navigate("home/edit/$id") },
                        onWorkoutCopied = { navController.openTab(Tab.Workout) },
                    )
                }
                savedWorkoutScreens("home", HomeRoute, navController)
            }
            navigation(startDestination = WorkoutHomeRoute, route = Tab.Workout.route) {
                composable(WorkoutHomeRoute) { entry ->
                    WorkoutScreen(
                        viewModel = workoutViewModel(navController, entry),
                        settings = settings,
                        onAddExercise = { navController.navigate("workout/add") },
                        onReorder = { navController.navigate("workout/reorder") },
                        onReplace = { id -> navController.navigate("workout/replace/$id") },
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
                        onNewExercise = { navController.navigate("workout/custom-exercise/$NewExerciseId") },
                        onExerciseClick = { id -> navController.navigate("workout/exercise/$id") },
                        onAddExercises = { ids ->
                            viewModel.addExercises(ids)
                            navController.popBackStack()
                        },
                    )
                }
                composable("workout/replace/{$WorkoutExerciseIdArg}") { entry ->
                    val viewModel = workoutViewModel(navController, entry)
                    val recentExercises by viewModel.recentExercises.collectAsStateWithLifecycle()
                    ExerciseListScreen(
                        title = "Replace exercise",
                        recentExercises = recentExercises,
                        onBack = navController::popBackStack,
                        onNewExercise = { navController.navigate("workout/custom-exercise/$NewExerciseId") },
                        onExerciseClick = { id ->
                            viewModel.replaceExercise(entry.workoutExerciseId(), id)
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
                exerciseDetail("workout/exercise", "workout", navController)
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
                            navController.openTab(Tab.Workout)
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
                        onNewExercise = { navController.navigate("routines/custom-exercise/$NewExerciseId") },
                        onExerciseClick = { id -> navController.navigate("routines/exercise/$id") },
                        onAddExercises = { ids ->
                            ids.forEach(viewModel::addExercise)
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
                exerciseDetail("routines/exercise", "routines", navController)
            }
            navigation(startDestination = ProfileRoute, route = Tab.Profile.route) {
                composable(ProfileRoute) {
                    ProfileScreen(
                        onHistory = { navController.navigate(HistoryHomeRoute) },
                        onStatistics = { navController.navigate("history/statistics") },
                        onExercises = { navController.navigate(ExerciseListRoute) },
                        onBody = { navController.navigate(BodyRoute) },
                        onMeasurements = { navController.navigate("body/measurements") },
                        onSettings = { navController.navigate("body/settings") },
                    )
                }
                composable(BodyRoute) {
                    BodyScreen(
                        onBack = navController::popBackStack,
                        onOpenMeasurements = { navController.navigate("body/measurements") },
                        onMuscleClick = { muscle -> navController.navigate("body/muscle/${muscle.name}") },
                    )
                }
                composable("body/measurements") {
                    MeasurementsScreen(
                        settings = settings,
                        onBack = navController::popBackStack,
                        onTypeClick = { type -> navController.navigate("body/measurements/${type.name}") },
                    )
                }
                composable("body/measurements/{$MeasurementTypeArg}") {
                    MeasurementDetailScreen(settings = settings, onBack = navController::popBackStack)
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
                exerciseDetail("body/exercise", "body", navController)
                composable(HistoryHomeRoute) {
                    HistoryScreen(
                        onBack = navController::popBackStack,
                        onWorkoutClick = { id -> navController.navigate("history/workout/$id") },
                        onStatistics = { navController.navigate("history/statistics") },
                    )
                }
                composable("history/statistics") {
                    StatisticsScreen(onBack = navController::popBackStack)
                }
                savedWorkoutScreens("history", HistoryHomeRoute, navController)
                composable(ExerciseListRoute) {
                    ExerciseListScreen(
                        onBack = navController::popBackStack,
                        onExerciseClick = { id -> navController.navigate("exercises/detail/$id") },
                        onNewExercise = { navController.navigate("exercises/custom-exercise/$NewExerciseId") },
                    )
                }
                exerciseDetail("exercises/detail", "exercises", navController)
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
private fun editWorkoutViewModel(
    navController: NavController,
    entry: NavBackStackEntry,
    editRoute: String,
): SavedWorkoutViewModel {
    val editEntry = remember(entry) { navController.getBackStackEntry(editRoute) }
    return viewModel(editEntry, factory = SavedWorkoutViewModel.Factory)
}

private const val WorkoutExerciseIdArg = "workoutExerciseId"

private fun NavBackStackEntry.workoutExerciseId(): Long = checkNotNull(arguments?.getString(WorkoutExerciseIdArg)).toLong()

/** Goes to a tab, keeping one copy of each tab and remembering where you were in it. */
private fun NavController.openTab(tab: Tab) {
    navigate(tab.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** A saved workout and its editor, added to each tab that opens them so that the tab stays selected. */
private fun NavGraphBuilder.savedWorkoutScreens(root: String, homeRoute: String, navController: NavController) {
    val editRoute = "$root/edit/{$WorkoutIdArg}"
    composable("$root/workout/{$WorkoutIdArg}") { entry ->
        SavedWorkoutScreen(
            onBack = navController::popBackStack,
            onEdit = {
                navController.navigate("$root/edit/${entry.arguments?.getString(WorkoutIdArg)}")
            },
        )
    }
    composable(editRoute) { entry ->
        EditWorkoutScreen(
            viewModel = editWorkoutViewModel(navController, entry, editRoute),
            onAddExercise = {
                navController.navigate("$root/edit/${entry.arguments?.getString(WorkoutIdArg)}/add")
            },
            onReorder = {
                navController.navigate("$root/edit/${entry.arguments?.getString(WorkoutIdArg)}/reorder")
            },
            onReplace = { id ->
                navController.navigate("$root/edit/${entry.arguments?.getString(WorkoutIdArg)}/replace/$id")
            },
            onExerciseClick = { id -> navController.navigate("$root/exercise/$id") },
            onDone = { stillExists ->
                if (stillExists) {
                    navController.popBackStack()
                } else {
                    navController.popBackStack(homeRoute, inclusive = false)
                }
            },
        )
    }
    composable("$editRoute/add") { entry ->
        val viewModel = editWorkoutViewModel(navController, entry, editRoute)
        val recentExercises by viewModel.recentExercises.collectAsStateWithLifecycle()
        ExerciseListScreen(
            title = "Add exercise",
            recentExercises = recentExercises,
            onBack = navController::popBackStack,
            onNewExercise = { navController.navigate("$root/custom-exercise/$NewExerciseId") },
            onExerciseClick = { id -> navController.navigate("$root/exercise/$id") },
            onAddExercises = { ids ->
                viewModel.addExercises(ids)
                navController.popBackStack()
            },
        )
    }
    composable("$editRoute/replace/{$WorkoutExerciseIdArg}") { entry ->
        val viewModel = editWorkoutViewModel(navController, entry, editRoute)
        val recentExercises by viewModel.recentExercises.collectAsStateWithLifecycle()
        ExerciseListScreen(
            title = "Replace exercise",
            recentExercises = recentExercises,
            onBack = navController::popBackStack,
            onNewExercise = { navController.navigate("$root/custom-exercise/$NewExerciseId") },
            onExerciseClick = { id ->
                viewModel.replaceExercise(entry.workoutExerciseId(), id)
                navController.popBackStack()
            },
        )
    }
    composable("$editRoute/reorder") { entry ->
        ReorderExercisesScreen(
            viewModel = editWorkoutViewModel(navController, entry, editRoute),
            onBack = navController::popBackStack,
        )
    }
    exerciseDetail("$root/exercise", root, navController)
}

/** The exercise page, added to each tab that opens it so that the tab stays selected. */
private fun NavGraphBuilder.exerciseDetail(route: String, root: String, navController: NavController) {
    composable("$route/{$ExerciseIdArg}") {
        ExerciseDetailScreen(
            onBack = navController::popBackStack,
            onEdit = { id -> navController.navigate("$root/custom-exercise/$id") },
        )
    }
    composable("$root/custom-exercise/{$ExerciseIdArg}") {
        CustomExerciseScreen(onDone = navController::popBackStack)
    }
}
