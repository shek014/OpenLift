package io.github.shek014.openlift.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.shek014.openlift.OpenLiftApplication
import io.github.shek014.openlift.data.RestTimer
import io.github.shek014.openlift.data.WorkoutRepository
import io.github.shek014.openlift.data.model.LoggedSet
import io.github.shek014.openlift.data.model.Workout
import io.github.shek014.openlift.data.model.previousSetsByExercise
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WorkoutUiState(
    val workout: Workout?,
    val previousSets: Map<String, List<LoggedSet>>,
)

class WorkoutViewModel(
    private val repository: WorkoutRepository,
    private val restTimer: RestTimer,
) : ViewModel() {
    /** The workout in progress, or null until it has loaded. */
    val state: StateFlow<WorkoutUiState?> =
        combine(repository.activeWorkout, repository.loggedSetsForActiveWorkout) { workout, logged ->
            WorkoutUiState(workout, previousSetsByExercise(logged))
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val restSeconds: StateFlow<Int?> = restTimer.secondsRemaining

    private fun run(action: suspend WorkoutRepository.() -> Unit) {
        viewModelScope.launch { repository.action() }
    }

    fun startWorkout() = run { startWorkout() }

    fun addExercise(exerciseId: String) = run { addExercise(exerciseId) }

    fun removeExercise(workoutExerciseId: Long) = run { removeExercise(workoutExerciseId) }

    fun addSet(workoutExerciseId: Long) = run { addSet(workoutExerciseId) }

    fun removeSet(setId: Long) = run { removeSet(setId) }

    fun updateWeight(setId: Long, weightKg: Double?) = run { updateWeight(setId, weightKg) }

    fun updateReps(setId: Long, reps: Int?) = run { updateReps(setId, reps) }

    fun completeSet(setId: Long, weightKg: Double?, reps: Int) {
        run { completeSet(setId, weightKg, reps) }
        restTimer.start()
    }

    fun uncompleteSet(setId: Long) = run { uncompleteSet(setId) }

    fun adjustRest(seconds: Int) = restTimer.adjust(seconds)

    fun skipRest() = restTimer.stop()

    fun discardWorkout() {
        restTimer.stop()
        run { discardActiveWorkout() }
    }

    /** Calls [onSaved] with the saved workout's id, or null if there was nothing to save. */
    fun finishWorkout(onSaved: (Long?) -> Unit) {
        restTimer.stop()
        viewModelScope.launch { onSaved(repository.finishActiveWorkout()) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as OpenLiftApplication
                WorkoutViewModel(application.workoutRepository, application.restTimer)
            }
        }
    }
}
