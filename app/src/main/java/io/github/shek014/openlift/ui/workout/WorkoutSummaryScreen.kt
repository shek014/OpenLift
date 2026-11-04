package io.github.shek014.openlift.ui.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.shek014.openlift.OpenLiftApplication
import io.github.shek014.openlift.data.WorkoutRepository
import io.github.shek014.openlift.data.model.Workout
import io.github.shek014.openlift.data.model.formatDuration
import io.github.shek014.openlift.data.model.formatSet
import io.github.shek014.openlift.data.model.formatVolume
import io.github.shek014.openlift.ui.LocalWeightUnit
import io.github.shek014.openlift.ui.body.BodyMap
import io.github.shek014.openlift.ui.body.muscleEmphasis
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkoutSummaryViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: WorkoutRepository,
) : ViewModel() {
    private val workoutId: Long = checkNotNull(savedStateHandle.get<String>(WorkoutIdArg)).toLong()

    val workout: StateFlow<Workout?> =
        repository.workout(workoutId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun deleteWorkout() {
        viewModelScope.launch { repository.deleteWorkout(workoutId) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                WorkoutSummaryViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    repository = (this[APPLICATION_KEY] as OpenLiftApplication).workoutRepository,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSummaryScreen(
    onDone: () -> Unit,
    viewModel: WorkoutSummaryViewModel = viewModel(factory = WorkoutSummaryViewModel.Factory),
) {
    val workout by viewModel.workout.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Workout saved") },
            actions = { TextButton(onClick = onDone) { Text("Done") } },
        )
        workout?.let { WorkoutSummary(it) }
    }
}

@Composable
fun WorkoutSummary(workout: Workout) {
    val unit = LocalWeightUnit.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Stat("Time", formatDuration(workout.durationSeconds))
            Stat("Sets", workout.completedSets.size.toString())
            Stat("Volume", formatVolume(workout.volumeKg, unit))
        }
        BodyMap(
            emphasis = workout.completedExercises.map { it.exercise }.muscleEmphasis(),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .widthIn(max = 260.dp)
                .padding(16.dp),
        )
        workout.completedExercises.forEach { entry ->
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = entry.exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                entry.sets.filter { it.completed }.forEachIndexed { index, set ->
                    Text(
                        text = "${index + 1}.  ${formatSet(set.weightKg, set.reps, unit)}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(value, style = MaterialTheme.typography.headlineSmall)
    }
}
