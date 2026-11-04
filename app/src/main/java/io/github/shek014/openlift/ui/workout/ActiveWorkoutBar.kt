package io.github.shek014.openlift.ui.workout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
import io.github.shek014.openlift.data.RestTimer
import io.github.shek014.openlift.data.WorkoutRepository
import io.github.shek014.openlift.data.model.Workout
import io.github.shek014.openlift.data.model.formatDuration
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ActiveWorkoutBarViewModel(repository: WorkoutRepository, restTimer: RestTimer) : ViewModel() {
    val workout: StateFlow<Workout?> =
        repository.activeWorkout.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val restSeconds: StateFlow<Int?> = restTimer.secondsRemaining

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as OpenLiftApplication
                ActiveWorkoutBarViewModel(application.workoutRepository, application.restTimer)
            }
        }
    }
}

/** Shows that a workout is still going while on another tab. Does nothing if there isn't one. */
@Composable
fun ActiveWorkoutBar(
    onClick: () -> Unit,
    viewModel: ActiveWorkoutBarViewModel = viewModel(factory = ActiveWorkoutBarViewModel.Factory),
) {
    val workout by viewModel.workout.collectAsStateWithLifecycle()
    val restSeconds by viewModel.restSeconds.collectAsStateWithLifecycle()
    val active = workout ?: return

    Surface(onClick = onClick, color = MaterialTheme.colorScheme.primaryContainer) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(active.name ?: "Workout in progress", style = MaterialTheme.typography.titleSmall)
                Row {
                    ProvideTextStyle(MaterialTheme.typography.labelMedium) {
                        ElapsedTime(active.startedAt)
                        restSeconds?.let { Text(" · Rest ${formatDuration(it.toLong())}") }
                    }
                }
            }
            Icon(Icons.Filled.ExpandLess, contentDescription = "Back to workout")
        }
    }
}
