package io.github.shek014.openlift.ui.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import io.github.shek014.openlift.data.model.NewRecord
import io.github.shek014.openlift.data.model.SetType
import io.github.shek014.openlift.data.model.Workout
import io.github.shek014.openlift.data.model.format
import io.github.shek014.openlift.data.model.formatDuration
import io.github.shek014.openlift.data.model.formatSet
import io.github.shek014.openlift.data.model.formatVolume
import io.github.shek014.openlift.data.model.muscleSplit
import io.github.shek014.openlift.data.model.newRecordsIn
import io.github.shek014.openlift.ui.LocalWeightUnit
import io.github.shek014.openlift.ui.body.BodyMap
import io.github.shek014.openlift.ui.body.muscleEmphasis
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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

    /** The records this workout broke, with the name of each exercise. */
    val newRecords: StateFlow<List<Pair<String, NewRecord>>> =
        combine(repository.workout(workoutId), repository.loggedSetsInWorkout(workoutId)) { workout, logged ->
            val finishedAt = workout?.finishedAt ?: return@combine emptyList()
            val names = workout.exercises.associate { it.exercise.id to it.exercise.name }
            newRecordsIn(workoutId, finishedAt, logged).map { names.getValue(it.exerciseId) to it }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

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
    val newRecords by viewModel.newRecords.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Workout saved") },
            actions = { TextButton(onClick = onDone) { Text("Done") } },
        )
        workout?.let { WorkoutSummary(it, newRecords) }
    }
}

@Composable
fun WorkoutSummary(workout: Workout, newRecords: List<Pair<String, NewRecord>> = emptyList()) {
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
        val split = remember(workout) { muscleSplit(workout.completedExercises) }
        if (split.isNotEmpty()) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Muscle split", style = MaterialTheme.typography.titleMedium)
                split.forEach { (group, share) -> SplitBar(group.label, share) }
            }
        }
        if (newRecords.isNotEmpty()) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text("New personal records", style = MaterialTheme.typography.titleMedium)
                }
                newRecords.forEach { (name, record) ->
                    Text(
                        text = "$name: ${record.metric.label.lowercase()}, ${record.metric.format(record.value, unit)}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
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
                entry.notes?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                entry.workSets.forEachIndexed { index, set ->
                    Text(
                        text = "${index + 1}.  ${formatSet(set.weightKg, set.reps, unit, set.durationSeconds)}${if (set.setType == SetType.Normal) "" else " (${set.setType.label})"}",
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

/** One muscle group's share of the workout, as a bar and a percentage. */
@Composable
private fun SplitBar(label: String, share: Double) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(10.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(share.toFloat().coerceIn(0f, 1f))
                        .background(MaterialTheme.colorScheme.primary),
                )
            }
            Text(
                text = "${(share * 100).roundToInt()}%",
                modifier = Modifier.width(44.dp),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}
