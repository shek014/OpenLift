package io.github.shek014.openlift.ui.home

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import io.github.shek014.openlift.data.RoutineRepository
import io.github.shek014.openlift.data.WorkoutRepository
import io.github.shek014.openlift.data.model.Muscle
import io.github.shek014.openlift.data.model.Workout
import io.github.shek014.openlift.data.model.formatDate
import io.github.shek014.openlift.data.model.formatDuration
import io.github.shek014.openlift.data.model.formatVolume
import io.github.shek014.openlift.data.model.toRoutinePlans
import io.github.shek014.openlift.data.model.toShareText
import io.github.shek014.openlift.ui.LocalWeightUnit
import io.github.shek014.openlift.ui.body.BodyMap
import io.github.shek014.openlift.ui.body.BodyViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val workoutRepository: WorkoutRepository,
    private val routineRepository: RoutineRepository,
) : ViewModel() {
    /** Finished workouts, newest first, or null until they've loaded. */
    val workouts: StateFlow<List<Workout>?> =
        workoutRepository.finishedWorkouts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun delete(workout: Workout) {
        viewModelScope.launch { workoutRepository.deleteWorkout(workout.id) }
    }

    fun saveAsRoutine(workout: Workout, name: String, onSaved: () -> Unit) {
        viewModelScope.launch {
            routineRepository.createRoutine(name.trim(), workout.toRoutinePlans())
            onSaved()
        }
    }

    /** Calls [onResult] with false if there's already a workout in progress. */
    fun copy(workout: Workout, onResult: (Boolean) -> Unit) {
        viewModelScope.launch { onResult(workoutRepository.copyWorkout(workout.id)) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as OpenLiftApplication
                HomeViewModel(application.workoutRepository, application.routineRepository)
            }
        }
    }
}

/** The first screen: this week's muscles, then the latest workouts in order. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenSettings: () -> Unit,
    onOpenBody: () -> Unit,
    onMuscleClick: (Muscle) -> Unit,
    onWorkoutClick: (Long) -> Unit,
    onEditWorkout: (Long) -> Unit,
    onWorkoutCopied: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
    bodyViewModel: BodyViewModel = viewModel(factory = BodyViewModel.Factory),
) {
    val workouts by viewModel.workouts.collectAsStateWithLifecycle()
    val trainedThisWeek by bodyViewModel.trainedThisWeek.collectAsStateWithLifecycle()
    var deleting by remember { mutableStateOf<Workout?>(null) }
    var savingAsRoutine by remember { mutableStateOf<Workout?>(null) }
    var workoutInProgress by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val unit = LocalWeightUnit.current

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Home") },
            actions = {
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings")
                }
            },
        )
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item(key = "this-week") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "This week",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        TextButton(onClick = onOpenBody) { Text("Body") }
                    }
                    BodyMap(
                        emphasis = trainedThisWeek,
                        onMuscleClick = onMuscleClick,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(220.dp),
                    )
                }
            }
            item(key = "workouts-title") {
                Text(
                    text = "Latest workouts",
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.onBackground)
            }
            val list = workouts
            if (list != null && list.isEmpty()) {
                item(key = "none") {
                    Text(
                        text = "Workouts you finish will appear here.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(list.orEmpty(), key = { it.id }) { workout ->
                WorkoutCard(
                    workout = workout,
                    onClick = { onWorkoutClick(workout.id) },
                    onShare = {
                        val send = Intent(Intent.ACTION_SEND)
                            .setType("text/plain")
                            .putExtra(Intent.EXTRA_TEXT, workout.toShareText(unit))
                        context.startActivity(Intent.createChooser(send, "Share workout"))
                    },
                    onSaveAsRoutine = { savingAsRoutine = workout },
                    onCopy = {
                        viewModel.copy(workout) { copied ->
                            if (copied) onWorkoutCopied() else workoutInProgress = true
                        }
                    },
                    onEdit = { onEditWorkout(workout.id) },
                    onDelete = { deleting = workout },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }

    deleting?.let { workout ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Delete this workout?") },
            text = { Text("It will be removed from your history and logs. This can't be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleting = null
                        viewModel.delete(workout)
                    },
                ) {
                    Text("Delete")
                }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Cancel") } },
        )
    }
    savingAsRoutine?.let { workout ->
        var name by remember(workout.id) {
            mutableStateOf(workout.name ?: "Workout from ${formatDate(workout.finishedAt ?: workout.startedAt)}")
        }
        AlertDialog(
            onDismissRequest = { savingAsRoutine = null },
            title = { Text("Save as routine") },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    enabled = name.isNotBlank(),
                    onClick = {
                        savingAsRoutine = null
                        viewModel.saveAsRoutine(workout, name) {
                            Toast.makeText(context, "Saved to your routines", Toast.LENGTH_SHORT).show()
                        }
                    },
                ) {
                    Text("Save")
                }
            },
            dismissButton = { TextButton(onClick = { savingAsRoutine = null }) { Text("Cancel") } },
        )
    }
    if (workoutInProgress) {
        AlertDialog(
            onDismissRequest = { workoutInProgress = false },
            title = { Text("Workout in progress") },
            text = { Text("Finish or discard your current workout before copying this one.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        workoutInProgress = false
                        onWorkoutCopied()
                    },
                ) {
                    Text("Go to workout")
                }
            },
            dismissButton = { TextButton(onClick = { workoutInProgress = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun WorkoutCard(
    workout: Workout,
    onClick: () -> Unit,
    onShare: () -> Unit,
    onSaveAsRoutine: () -> Unit,
    onCopy: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val unit = LocalWeightUnit.current
    var menuOpen by remember { mutableStateOf(false) }
    val exercises = workout.completedExercises
    val title = workout.name ?: formatDate(workout.finishedAt ?: workout.startedAt)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            if (workout.name != null) {
                Text(
                    text = formatDate(workout.finishedAt ?: workout.startedAt),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            val sets = workout.completedSets.size
            Text(
                text = "${formatDuration(workout.durationSeconds)} · " +
                    "${if (sets == 1) "1 set" else "$sets sets"} · ${formatVolume(workout.volumeKg, unit)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            exercises.take(ExercisesShown).forEach { entry ->
                Text(
                    text = "${entry.workSets.size} × ${entry.exercise.name}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (exercises.size > ExercisesShown) {
                Text(
                    text = "and ${exercises.size - ExercisesShown} more",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Box {
            IconButton(onClick = { menuOpen = true }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "Workout options for $title")
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                listOf(
                    "Share workout" to onShare,
                    "Save as routine" to onSaveAsRoutine,
                    "Copy workout" to onCopy,
                    "Edit workout" to onEdit,
                    "Delete workout" to onDelete,
                ).forEach { (label, action) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            menuOpen = false
                            action()
                        },
                    )
                }
            }
        }
    }
}

private const val ExercisesShown = 4
