package io.github.shek014.openlift.ui.workout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.shek014.openlift.OpenLiftApplication
import io.github.shek014.openlift.data.AppSettings
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

/** The name of the navigation argument that says which workout to show. */
const val WorkoutIdArg = "workoutId"

data class WorkoutUiState(
    val workout: Workout?,
    val previousSets: Map<String, List<LoggedSet>>,
)

/** What the workout editing screen needs, whether it's editing a live workout or a saved one. */
abstract class WorkoutEditorViewModel(protected val repository: WorkoutRepository) : ViewModel() {
    /** The workout being edited, or null until it has loaded. */
    abstract val state: StateFlow<WorkoutUiState?>

    abstract fun addExercise(exerciseId: String)

    protected fun run(action: suspend WorkoutRepository.() -> Unit) {
        viewModelScope.launch { repository.action() }
    }

    fun removeExercise(workoutExerciseId: Long) = run { removeExercise(workoutExerciseId) }

    fun reorderExercises(workoutExerciseIds: List<Long>) = run { reorderExercises(workoutExerciseIds) }

    fun addSet(workoutExerciseId: Long) = run { addSet(workoutExerciseId) }

    fun removeSet(setId: Long) = run { removeSet(setId) }

    fun updateWeight(setId: Long, weightKg: Double?) = run { updateWeight(setId, weightKg) }

    fun updateReps(setId: Long, reps: Int?) = run { updateReps(setId, reps) }

    open fun completeSet(setId: Long, weightKg: Double?, reps: Int) = run { completeSet(setId, weightKg, reps) }

    fun uncompleteSet(setId: Long) = run { uncompleteSet(setId) }
}

/** The workout in progress. */
class WorkoutViewModel(
    repository: WorkoutRepository,
    private val restTimer: RestTimer,
    private val settings: AppSettings,
) : WorkoutEditorViewModel(repository) {
    override val state: StateFlow<WorkoutUiState?> =
        combine(repository.activeWorkout, repository.loggedSetsForActiveWorkout) { workout, logged ->
            WorkoutUiState(workout, previousSetsByExercise(logged))
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val restSeconds: StateFlow<Int?> = restTimer.secondsRemaining

    fun startWorkout() = run { startWorkout() }

    override fun addExercise(exerciseId: String) = run { addExercise(exerciseId) }

    override fun completeSet(setId: Long, weightKg: Double?, reps: Int) {
        super.completeSet(setId, weightKg, reps)
        restTimer.start(settings.restSeconds.value)
    }

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
                WorkoutViewModel(application.workoutRepository, application.restTimer, application.settings)
            }
        }
    }
}

/** A workout that's already been saved, being edited from History. */
class SavedWorkoutViewModel(
    savedStateHandle: SavedStateHandle,
    repository: WorkoutRepository,
) : WorkoutEditorViewModel(repository) {
    private val workoutId: Long = checkNotNull(savedStateHandle.get<String>(WorkoutIdArg)).toLong()

    override val state: StateFlow<WorkoutUiState?> =
        combine(repository.workout(workoutId), repository.loggedSetsInWorkout(workoutId)) { workout, logged ->
            // What came before this workout, not after it, and not this workout itself.
            val earlier = logged.filter { it.workoutId != workoutId && it.finishedAt < (workout?.finishedAt ?: 0) }
            WorkoutUiState(workout, previousSetsByExercise(earlier))
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    override fun addExercise(exerciseId: String) = run { addExerciseTo(workoutId, exerciseId) }

    /** Tidies up the edit, calling [onDone] with whether the workout still has anything in it. */
    fun finishEditing(onDone: (Boolean) -> Unit) {
        viewModelScope.launch { onDone(repository.finishEditing(workoutId)) }
    }

    fun deleteWorkout() = run { deleteWorkout(workoutId) }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SavedWorkoutViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    repository = (this[APPLICATION_KEY] as OpenLiftApplication).workoutRepository,
                )
            }
        }
    }
}
