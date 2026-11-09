package io.github.shek014.openlift.ui.workout

import android.os.SystemClock
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
import io.github.shek014.openlift.data.RoutineRepository
import io.github.shek014.openlift.data.SoundPlayer
import io.github.shek014.openlift.data.WorkoutRepository
import io.github.shek014.openlift.data.model.Exercise
import io.github.shek014.openlift.data.model.LoggedSet
import io.github.shek014.openlift.data.model.Routine
import io.github.shek014.openlift.data.model.differsFrom
import io.github.shek014.openlift.data.model.plannedExercises
import io.github.shek014.openlift.data.model.SetType
import io.github.shek014.openlift.data.model.orderForSuperset
import io.github.shek014.openlift.data.model.recordSetIds
import io.github.shek014.openlift.data.model.Workout
import io.github.shek014.openlift.data.model.previousSetsByExercise
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The name of the navigation argument that says which workout to show. */
const val WorkoutIdArg = "workoutId"

/** A stopwatch timing a set. [startedAt] comes from the elapsed-time clock, which can't be changed. */
data class SetTimer(val setId: Long, val startedAt: Long)

/** A stopwatch for anything. [startedAt] is from the elapsed-time clock, or null while it's paused. */
data class Stopwatch(val startedAt: Long? = null, val elapsedBefore: Long = 0) {
    fun elapsedMillis(now: Long): Long = elapsedBefore + (startedAt?.let { now - it } ?: 0)
}

/** What to do with the routine a workout came from, when the workout was changed. */
sealed interface RoutineUpdate {
    data class Replace(val routine: Routine) : RoutineUpdate
    data class SaveAsNew(val name: String) : RoutineUpdate
}

data class WorkoutUiState(
    val workout: Workout?,
    val previousSets: Map<String, List<LoggedSet>>,
    /** The sets in this workout that broke a personal record. */
    val recordSetIds: Set<Long> = emptySet(),
)

/** What the workout editing screen needs, whether it's editing a live workout or a saved one. */
abstract class WorkoutEditorViewModel(protected val repository: WorkoutRepository) : ViewModel() {
    /** The workout being edited, or null until it has loaded. */
    abstract val state: StateFlow<WorkoutUiState?>

    /** What to suggest first when adding an exercise. */
    val recentExercises: StateFlow<List<Exercise>> =
        repository.recentExercises.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    protected abstract suspend fun insertExercise(exerciseId: String)

    fun addExercises(exerciseIds: List<String>) {
        viewModelScope.launch { exerciseIds.forEach { insertExercise(it) } }
    }

    protected fun run(action: suspend WorkoutRepository.() -> Unit) {
        viewModelScope.launch { repository.action() }
    }

    fun removeExercise(workoutExerciseId: Long) = run { removeExercise(workoutExerciseId) }

    fun reorderExercises(workoutExerciseIds: List<Long>) = run { reorderExercises(workoutExerciseIds) }

    fun replaceExercise(workoutExerciseId: Long, exerciseId: String) = run { replaceExercise(workoutExerciseId, exerciseId) }

    /** Pairs two exercises in a superset, and moves them next to each other. */
    fun addToSuperset(workoutExerciseId: Long, partnerId: Long) {
        val workout = state.value?.workout ?: return
        val partner = workout.exercises.find { it.id == partnerId } ?: return
        run {
            setSuperset(listOf(workoutExerciseId, partnerId), partner.supersetId ?: partnerId)
            reorderExercises(workout.orderForSuperset(workoutExerciseId, partnerId))
        }
    }

    fun removeFromSuperset(workoutExerciseId: Long) = run { setSuperset(listOf(workoutExerciseId), null) }

    fun updateNotes(workoutExerciseId: Long, notes: String) =
        run { updateNotes(workoutExerciseId, notes.trim().ifEmpty { null }) }

    fun updateRepTarget(workoutExerciseId: Long, repsMin: Int?, repsMax: Int?) =
        run { updateRepTarget(workoutExerciseId, repsMin, repsMax) }

    fun addSet(workoutExerciseId: Long) = run { addSet(workoutExerciseId) }

    /** Adds warm-up sets given as weight in kilograms and reps. */
    fun addWarmupSets(workoutExerciseId: Long, sets: List<Pair<Double, Int>>) =
        run { addWarmupSets(workoutExerciseId, sets) }

    fun removeSet(setId: Long) = run { removeSet(setId) }

    fun updateWeight(setId: Long, weightKg: Double?) = run { updateWeight(setId, weightKg) }

    fun updateReps(setId: Long, reps: Int?) = run { updateReps(setId, reps) }

    /** [restSeconds] is how long to rest after this set, if a rest timer applies at all. */
    open fun completeSet(setId: Long, weightKg: Double?, reps: Int?, restSeconds: Int?, durationSeconds: Int? = null) =
        run { completeSet(setId, weightKg, reps, durationSeconds) }

    fun updateDuration(setId: Long, seconds: Int?) = run { updateDuration(setId, seconds) }

    fun updateSetType(setId: Long, type: SetType) = run { updateSetType(setId, type) }

    /** The stopwatch running for a timed set. Only live workouts have one. */
    open val setTimer: StateFlow<SetTimer?>? = null

    open fun startSetTimer(setId: Long) = Unit

    /** Stops the stopwatch and returns how many seconds it ran. */
    open fun stopSetTimer(): Int? = null

    fun updateRestSeconds(workoutExerciseId: Long, seconds: Int) = run { updateRestSeconds(workoutExerciseId, seconds) }

    fun uncompleteSet(setId: Long) = run { uncompleteSet(setId) }
}

/** The workout in progress. */
class WorkoutViewModel(
    repository: WorkoutRepository,
    private val restTimer: RestTimer,
    private val routineRepository: RoutineRepository,
    private val settings: AppSettings,
    private val soundPlayer: SoundPlayer,
) : WorkoutEditorViewModel(repository) {
    private val timer = MutableStateFlow<SetTimer?>(null)
    private val stopwatchState = MutableStateFlow(Stopwatch())

    val stopwatch: StateFlow<Stopwatch> = stopwatchState.asStateFlow()

    fun startStopwatch() {
        stopwatchState.update { if (it.startedAt == null) it.copy(startedAt = SystemClock.elapsedRealtime()) else it }
    }

    fun pauseStopwatch() {
        stopwatchState.update { Stopwatch(elapsedBefore = it.elapsedMillis(SystemClock.elapsedRealtime())) }
    }

    fun resetStopwatch() {
        stopwatchState.value = Stopwatch()
    }

    /** Counts down like the rest timer, with the same bar and sound. */
    fun startTimer(seconds: Int) = restTimer.start(seconds)

    override val setTimer: StateFlow<SetTimer?> = timer.asStateFlow()

    override fun startSetTimer(setId: Long) {
        timer.value = SetTimer(setId, SystemClock.elapsedRealtime())
    }

    override fun stopSetTimer(): Int? {
        val running = timer.value ?: return null
        timer.value = null
        return (((SystemClock.elapsedRealtime() - running.startedAt) / 1000).toInt()).coerceAtLeast(1)
    }

    val routines: StateFlow<List<Routine>> =
        routineRepository.routines.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun startRoutine(routineId: Long) {
        viewModelScope.launch { routineRepository.startWorkout(routineId) }
    }

    override val state: StateFlow<WorkoutUiState?> =
        combine(repository.activeWorkout, repository.loggedSetsForActiveWorkout) { workout, logged ->
            WorkoutUiState(workout, previousSetsByExercise(logged), workout?.let { recordSetIds(it, logged) }.orEmpty())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val restSeconds: StateFlow<Int?> = restTimer.secondsRemaining

    init {
        // Plays the record sound when a set newly breaks a record, but not for the ones already
        // showing when the workout opens.
        viewModelScope.launch {
            var seen: Set<Long>? = null
            state.filterNotNull().map { it.recordSetIds }.distinctUntilChanged().collect { records ->
                val before = seen
                if (before != null && (records - before).isNotEmpty()) soundPlayer.play(settings.recordSound.value)
                seen = records
            }
        }
    }

    /** The routine this workout came from, if it no longer matches it. */
    fun checkRoutine(onResult: (Routine?) -> Unit) {
        viewModelScope.launch {
            val workout = state.value?.workout
            val routine = workout?.routineId?.let { routineRepository.routine(it).first() }
            onResult(routine?.takeIf { it.differsFrom(workout) })
        }
    }

    fun startWorkout() = run { startWorkout() }

    override suspend fun insertExercise(exerciseId: String) = repository.addExercise(exerciseId)

    override fun completeSet(setId: Long, weightKg: Double?, reps: Int?, restSeconds: Int?, durationSeconds: Int?) {
        super.completeSet(setId, weightKg, reps, restSeconds, durationSeconds)
        // Warm-ups pass null, and leave any timer alone. A rest time of 0 means the timer is off.
        if (restSeconds != null) {
            if (restSeconds > 0) restTimer.start(restSeconds) else restTimer.stop()
        }
    }

    fun adjustRest(seconds: Int) = restTimer.adjust(seconds)

    fun skipRest() = restTimer.stop()

    fun discardWorkout() {
        restTimer.stop()
        timer.value = null
        resetStopwatch()
        run { discardActiveWorkout() }
    }

    /**
     * Calls [onSaved] with the saved workout's id, or null if there was nothing to save. The routine
     * is updated first, since finishing drops any sets that weren't checked off.
     */
    fun finishWorkout(routineUpdate: RoutineUpdate? = null, onSaved: (Long?) -> Unit) {
        restTimer.stop()
        timer.value = null
        resetStopwatch()
        val plan = state.value?.workout?.plannedExercises().orEmpty()
        viewModelScope.launch {
            when (routineUpdate) {
                is RoutineUpdate.Replace -> routineRepository.updateRoutine(routineUpdate.routine.id, routineUpdate.routine.name, plan)
                is RoutineUpdate.SaveAsNew -> routineRepository.createRoutine(routineUpdate.name, plan)
                null -> Unit
            }
            onSaved(repository.finishActiveWorkout())
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as OpenLiftApplication
                WorkoutViewModel(
                    application.workoutRepository,
                    application.restTimer,
                    application.routineRepository,
                    application.settings,
                    application.soundPlayer,
                )
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
            WorkoutUiState(workout, previousSetsByExercise(earlier), workout?.let { recordSetIds(it, earlier) }.orEmpty())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    override suspend fun insertExercise(exerciseId: String) = repository.addExerciseTo(workoutId, exerciseId)

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
