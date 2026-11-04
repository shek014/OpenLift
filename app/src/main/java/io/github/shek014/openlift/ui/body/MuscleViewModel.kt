package io.github.shek014.openlift.ui.body

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.shek014.openlift.OpenLiftApplication
import io.github.shek014.openlift.data.ExerciseRepository
import io.github.shek014.openlift.data.model.Exercise
import io.github.shek014.openlift.data.model.Muscle
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** The name of the navigation argument that says which muscle to show. */
const val MuscleArg = "muscle"

data class MuscleExercises(
    val main: List<Exercise>,
    val assisting: List<Exercise>,
)

fun List<Exercise>.forMuscle(muscle: Muscle) = MuscleExercises(
    main = filter { muscle in it.primaryMuscles },
    assisting = filter { muscle in it.secondaryMuscles },
)

class MuscleViewModel(
    savedStateHandle: SavedStateHandle,
    repository: ExerciseRepository,
) : ViewModel() {
    val muscle: Muscle = Muscle.valueOf(checkNotNull(savedStateHandle[MuscleArg]))

    /** The exercises that work [muscle], or null until they've loaded. */
    val exercises: StateFlow<MuscleExercises?> =
        repository.selectableExercises
            .map { it.forMuscle(muscle) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MuscleViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    repository = (this[APPLICATION_KEY] as OpenLiftApplication).exerciseRepository,
                )
            }
        }
    }
}
