package io.github.shek014.openlift.ui.exercises

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.shek014.openlift.OpenLiftApplication
import io.github.shek014.openlift.data.ExerciseInstructions
import io.github.shek014.openlift.data.ExerciseMedia
import io.github.shek014.openlift.data.ExerciseRepository
import io.github.shek014.openlift.data.WorkoutRepository
import io.github.shek014.openlift.data.model.Exercise
import io.github.shek014.openlift.data.model.LoggedSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** The name of the navigation argument that says which exercise to show. */
const val ExerciseIdArg = "exerciseId"

data class ExerciseDetails(
    val exercise: Exercise,
    val animationUri: String?,
    val howToSteps: List<String>,
)

class ExerciseDetailViewModel(
    savedStateHandle: SavedStateHandle,
    repository: ExerciseRepository,
    media: ExerciseMedia,
    workoutRepository: WorkoutRepository,
) : ViewModel() {
    private val exerciseId: String = checkNotNull(savedStateHandle[ExerciseIdArg])

    /** What to show about the exercise, or null until it has loaded. */
    val details: StateFlow<ExerciseDetails?> =
        repository.exercise(exerciseId)
            .map { exercise ->
                exercise?.let {
                    ExerciseDetails(
                        exercise = it,
                        animationUri = media.animationUri(it.id),
                        howToSteps = ExerciseInstructions[it.id].orEmpty(),
                    )
                }
            }
            .flowOn(Dispatchers.IO) // Finding the animation reads the app's files.
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Every completed set of this exercise, newest workout first. */
    val log: StateFlow<List<LoggedSet>> =
        workoutRepository.loggedSets(listOf(exerciseId))
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as OpenLiftApplication
                ExerciseDetailViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    repository = application.exerciseRepository,
                    media = application.exerciseMedia,
                    workoutRepository = application.workoutRepository,
                )
            }
        }
    }
}
