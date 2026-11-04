package io.github.shek014.openlift.ui.routines

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.shek014.openlift.OpenLiftApplication
import io.github.shek014.openlift.data.RecommendedSplits
import io.github.shek014.openlift.data.RoutineRepository
import io.github.shek014.openlift.data.Split
import io.github.shek014.openlift.data.WorkoutRepository
import io.github.shek014.openlift.data.model.Exercise
import io.github.shek014.openlift.data.model.ExercisePlan
import io.github.shek014.openlift.data.model.Routine
import io.github.shek014.openlift.data.model.parseExercisePlan
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** The names of the navigation arguments on the routine screens. */
const val RoutineIdArg = "routineId"
const val SplitIdArg = "splitId"

/** The value of [RoutineIdArg] when the editor is making a new routine. */
const val NewRoutineId = "new"

class RoutinesViewModel(repository: RoutineRepository) : ViewModel() {
    /** The saved routines, or null until they've loaded. */
    val routines: StateFlow<List<Routine>?> =
        repository.routines.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                RoutinesViewModel((this[APPLICATION_KEY] as OpenLiftApplication).routineRepository)
            }
        }
    }
}

class RoutineDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: RoutineRepository,
) : ViewModel() {
    private val routineId: Long = checkNotNull(savedStateHandle.get<String>(RoutineIdArg)).toLong()

    val routine: StateFlow<Routine?> =
        repository.routine(routineId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun delete() {
        viewModelScope.launch { repository.deleteRoutine(routineId) }
    }

    /** Calls [onResult] with false if there's already a workout in progress. */
    fun startWorkout(onResult: (Boolean) -> Unit) {
        viewModelScope.launch { onResult(repository.startWorkout(routineId)) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                RoutineDetailViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    repository = (this[APPLICATION_KEY] as OpenLiftApplication).routineRepository,
                )
            }
        }
    }
}

class SplitDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: RoutineRepository,
) : ViewModel() {
    val split: Split = RecommendedSplits.first { it.id == savedStateHandle.get<String>(SplitIdArg) }

    /** The app's exercises by id, or empty until they've loaded. */
    val exercises: StateFlow<Map<String, Exercise>> =
        repository.exercises.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    fun addToRoutines(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.addSplit(split)
            onDone()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SplitDetailViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    repository = (this[APPLICATION_KEY] as OpenLiftApplication).routineRepository,
                )
            }
        }
    }
}

/** One exercise in the editor, holding what's been typed so far. [key] identifies it on screen. */
data class DraftExercise(
    val key: Long,
    val exercise: Exercise,
    val setsText: String,
    val repsMinText: String,
    val repsMaxText: String,
) {
    val plan: ExercisePlan?
        get() = parseExercisePlan(setsText, repsMinText, repsMaxText)
}

/** Changes stay in here until they're saved, so that backing out leaves the routine as it was. */
class RoutineEditorViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: RoutineRepository,
    workoutRepository: WorkoutRepository,
) : ViewModel() {
    private val routineId: Long? = checkNotNull(savedStateHandle.get<String>(RoutineIdArg)).toLongOrNull()
    private var nextKey = 0L
    private var exercisesById: Map<String, Exercise> = emptyMap()

    val isNew: Boolean = routineId == null

    var name by mutableStateOf("")
        private set
    var exercises by mutableStateOf<List<DraftExercise>>(emptyList())
        private set
    var hasChanges by mutableStateOf(false)
        private set

    val canSave: Boolean
        get() = name.isNotBlank() && exercises.isNotEmpty() && exercises.all { it.plan != null }

    val recentExercises: StateFlow<List<Exercise>> =
        workoutRepository.recentExercises.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            exercisesById = repository.exercises.first()
            routineId?.let { id ->
                repository.routine(id).first()?.let { routine ->
                    name = routine.name
                    exercises = routine.exercises.map {
                        DraftExercise(
                            key = nextKey++,
                            exercise = it.exercise,
                            setsText = it.plan.sets.toString(),
                            repsMinText = it.plan.repsMin?.toString().orEmpty(),
                            repsMaxText = it.plan.repsMax?.toString().orEmpty(),
                        )
                    }
                }
            }
        }
    }

    fun updateName(text: String) {
        name = text
        hasChanges = true
    }

    fun addExercise(exerciseId: String) {
        val exercise = exercisesById[exerciseId] ?: return
        exercises = exercises + DraftExercise(nextKey++, exercise, "3", "8", "12")
        hasChanges = true
    }

    fun removeExercise(key: Long) {
        exercises = exercises.filterNot { it.key == key }
        hasChanges = true
    }

    fun updateExercise(key: Long, update: (DraftExercise) -> DraftExercise) {
        exercises = exercises.map { if (it.key == key) update(it) else it }
        hasChanges = true
    }

    /** Puts the exercises in the order of these keys. */
    fun reorder(keys: List<Long>) {
        val moved = keys.mapNotNull { key -> exercises.find { it.key == key } }
        if (moved.size == exercises.size && moved != exercises) {
            exercises = moved
            hasChanges = true
        }
    }

    fun save(onSaved: () -> Unit) {
        if (!canSave) return
        val planned = exercises.map { it.exercise.id to checkNotNull(it.plan) }
        viewModelScope.launch {
            if (routineId == null) {
                repository.createRoutine(name.trim(), planned)
            } else {
                repository.updateRoutine(routineId, name.trim(), planned)
            }
            onSaved()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as OpenLiftApplication
                RoutineEditorViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    repository = application.routineRepository,
                    workoutRepository = application.workoutRepository,
                )
            }
        }
    }
}
