package io.github.shek014.openlift.ui.history

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.shek014.openlift.OpenLiftApplication
import io.github.shek014.openlift.data.WorkoutRepository
import io.github.shek014.openlift.data.model.Workout
import io.github.shek014.openlift.data.model.formatDate
import io.github.shek014.openlift.data.model.formatDuration
import io.github.shek014.openlift.data.model.formatVolume
import io.github.shek014.openlift.ui.LocalWeightUnit
import io.github.shek014.openlift.ui.workout.WorkoutEditor
import io.github.shek014.openlift.ui.workout.SavedWorkoutViewModel
import io.github.shek014.openlift.ui.workout.WorkoutSummary
import io.github.shek014.openlift.ui.workout.WorkoutSummaryViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HistoryViewModel(repository: WorkoutRepository) : ViewModel() {
    /** Finished workouts, newest first, or null until they've loaded. */
    val workouts: StateFlow<List<Workout>?> =
        repository.finishedWorkouts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                HistoryViewModel((this[APPLICATION_KEY] as OpenLiftApplication).workoutRepository)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onWorkoutClick: (workoutId: Long) -> Unit,
    onStatistics: () -> Unit,
    viewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory),
) {
    val workouts by viewModel.workouts.collectAsStateWithLifecycle()
    val unit = LocalWeightUnit.current

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("History") },
            actions = {
                TextButton(onClick = onStatistics) { Text("Statistics") }
            },
        )
        val list = workouts ?: return@Column
        if (list.isEmpty()) {
            Text(
                text = "Workouts you finish will appear here.",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@Column
        }
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item(key = "progress") { OverallProgressChart(list) }
            item(key = "workouts-title") {
                Text(
                    text = "Workouts",
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.onBackground)
            }
            items(list, key = { it.id }) { workout ->
                ListItem(
                    overlineContent = { Text(formatDate(workout.finishedAt ?: workout.startedAt)) },
                    headlineContent = { Text(workout.name ?: workout.completedExercises.joinToString { it.exercise.name }) },
                    supportingContent = {
                        val sets = workout.completedSets.size
                        Text(
                            text = "${formatDuration(workout.durationSeconds)} · " +
                                "${if (sets == 1) "1 set" else "$sets sets"} · ${formatVolume(workout.volumeKg, unit)}",
                            style = MaterialTheme.typography.labelMedium,
                        )
                    },
                    modifier = Modifier.clickable { onWorkoutClick(workout.id) },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

/** A saved workout, with the options to edit or delete it. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedWorkoutScreen(
    onBack: () -> Unit,
    onEdit: () -> Unit,
    viewModel: WorkoutSummaryViewModel = viewModel(factory = WorkoutSummaryViewModel.Factory),
) {
    val workout by viewModel.workout.collectAsStateWithLifecycle()
    val newRecords by viewModel.newRecords.collectAsStateWithLifecycle()
    var confirmingDelete by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { workout?.let { Text(formatDate(it.finishedAt ?: it.startedAt)) } },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                TextButton(onClick = onEdit) { Text("Edit") }
                TextButton(onClick = { confirmingDelete = true }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
        )
        workout?.let { WorkoutSummary(it, newRecords) }
    }

    if (confirmingDelete) {
        AlertDialog(
            onDismissRequest = { confirmingDelete = false },
            title = { Text("Delete this workout?") },
            text = { Text("It will be removed from your history and logs. This can't be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmingDelete = false
                        viewModel.deleteWorkout()
                        onBack()
                    },
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmingDelete = false }) { Text("Cancel") }
            },
        )
    }
}

/** Changes save as they're made. Finishing just tidies up any sets left unchecked. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditWorkoutScreen(
    viewModel: SavedWorkoutViewModel,
    onAddExercise: () -> Unit,
    onReorder: () -> Unit,
    onExerciseClick: (exerciseId: String) -> Unit,
    onDone: (workoutStillExists: Boolean) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val current = state ?: return
    val workout = current.workout ?: return

    BackHandler { viewModel.finishEditing(onDone) }

    WorkoutEditor(
        workout = workout,
        previousSets = current.previousSets,
        viewModel = viewModel,
        title = { Text("Edit workout") },
        actions = {
            TextButton(onClick = { viewModel.finishEditing(onDone) }) { Text("Done") }
        },
        onAddExercise = onAddExercise,
        onReorder = onReorder,
        onExerciseClick = onExerciseClick,
    )
}
