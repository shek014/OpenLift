package io.github.shek014.openlift.ui.workout

import android.content.Context
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
import io.github.shek014.openlift.data.RestNotifications
import io.github.shek014.openlift.data.RestTimer
import io.github.shek014.openlift.data.RoutineRepository
import io.github.shek014.openlift.data.WorkoutRepository
import io.github.shek014.openlift.data.model.Exercise
import io.github.shek014.openlift.data.model.LoggedSet
import io.github.shek014.openlift.data.model.Routine
import io.github.shek014.openlift.data.model.SetType
import io.github.shek014.openlift.data.model.format
import io.github.shek014.openlift.data.model.setRecords
import io.github.shek014.openlift.data.model.Workout
import io.github.shek014.openlift.data.model.previousSetsByExercise
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** The name of the navigation argument that says which workout to show. */
const val WorkoutIdArg = "workoutId"

/** A stopwatch timing a set. [startedAt] comes from the elapsed-time clock, which can't be changed. */
data class SetTimer(val setId: Long, val startedAt: Long)

data class WorkoutUiState(
    val workout: Workout?,
    val previousSets: Map<String, List<LoggedSet>>,
)

/** What the workout editing screen needs, whether it's editing a live workout or a saved one. */
abstract class WorkoutEditorViewModel(protected val repository: WorkoutRepository) : ViewModel() {
    /** The workout being edited, or null until it has loaded. */
    abstract val state: StateFlow<WorkoutUiState?>

    /** What to suggest first when adding an exercise. */
    val recentExercises: StateFlow<List<Exercise>> =
        repository.recentExercises.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    abstract fun addExercise(exerciseId: String)

    protected fun run(action: suspend WorkoutRepository.() -> Unit) {
        viewModelScope.launch { repository.action() }
    }

    fun removeExercise(workoutExerciseId: Long) = run { removeExercise(workoutExerciseId) }

    fun reorderExercises(workoutExerciseIds: List<Long>) = run { reorderExercises(workoutExerciseIds) }

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
    private val context: Context,
) : WorkoutEditorViewModel(repository) {
    private val timer = MutableStateFlow<SetTimer?>(null)
    private val recordBanner = MutableStateFlow<String?>(null)
    private var bannerJob: Job? = null

    override val setTimer: StateFlow<SetTimer?> = timer.asStateFlow()

    /** A record just broken, shown briefly on the workout screen. */
    val latestRecord: StateFlow<String?> = recordBanner.asStateFlow()

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
            WorkoutUiState(workout, previousSetsByExercise(logged))
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val restSeconds: StateFlow<Int?> = restTimer.secondsRemaining

    fun startWorkout() = run { startWorkout() }

    override fun addExercise(exerciseId: String) = run { addExercise(exerciseId) }

    override fun completeSet(setId: Long, weightKg: Double?, reps: Int?, restSeconds: Int?, durationSeconds: Int?) {
        super.completeSet(setId, weightKg, reps, restSeconds, durationSeconds)
        checkForRecords(setId, weightKg, reps, durationSeconds)
        // Warm-ups pass null, and leave any timer alone. A rest time of 0 means the timer is off.
        if (restSeconds != null) {
            if (restSeconds > 0) restTimer.start(restSeconds) else restTimer.stop()
        }
    }

    /** Compares a completed set with everything before it, and announces any record it broke. */
    private fun checkForRecords(setId: Long, weightKg: Double?, reps: Int?, durationSeconds: Int?) {
        val entry = state.value?.workout?.exercises?.find { exercise -> exercise.sets.any { it.id == setId } } ?: return
        val type = entry.sets.first { it.id == setId }.setType
        if (type == SetType.Warmup) return
        val others = entry.workSets.filterNot { it.id == setId }
        viewModelScope.launch {
            val history = repository.loggedSets(listOf(entry.exercise.id)).first()
            val earlier = history + others.map {
                LoggedSet(0, 0, entry.exercise.id, it.weightKg, it.reps ?: 0, it.durationSeconds, it.setType)
            }
            val set = LoggedSet(0, 0, entry.exercise.id, weightKg, reps ?: 0, durationSeconds, type)
            val records = setRecords(set, earlier)
            if (records.isEmpty()) return@launch
            val unit = settings.weightUnit.value
            val details = records.joinToString { "${it.metric.label.lowercase()} ${it.metric.format(it.value, unit)}" }
            RestNotifications.showRecord(context, entry.exercise.name, details)
            recordBanner.value = "New record: ${entry.exercise.name}, $details"
            bannerJob?.cancel()
            bannerJob = launch {
                delay(RecordBannerMillis)
                recordBanner.value = null
            }
        }
    }

    fun adjustRest(seconds: Int) = restTimer.adjust(seconds)

    fun skipRest() = restTimer.stop()

    fun discardWorkout() {
        restTimer.stop()
        timer.value = null
        run { discardActiveWorkout() }
    }

    /** Calls [onSaved] with the saved workout's id, or null if there was nothing to save. */
    fun finishWorkout(onSaved: (Long?) -> Unit) {
        restTimer.stop()
        timer.value = null
        viewModelScope.launch { onSaved(repository.finishActiveWorkout()) }
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
                    application,
                )
            }
        }
    }
}

private const val RecordBannerMillis = 5_000L

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
