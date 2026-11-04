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
import io.github.shek014.openlift.data.ExerciseRepository
import io.github.shek014.openlift.data.model.Exercise
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/** The name of the navigation argument that says which exercise to show. */
const val ExerciseIdArg = "exerciseId"

class ExerciseDetailViewModel(
    savedStateHandle: SavedStateHandle,
    repository: ExerciseRepository,
) : ViewModel() {
    /** The exercise to show, or null until it has loaded. */
    val exercise: StateFlow<Exercise?> =
        repository.exercise(checkNotNull(savedStateHandle[ExerciseIdArg]))
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ExerciseDetailViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    repository = (this[APPLICATION_KEY] as OpenLiftApplication).exerciseRepository,
                )
            }
        }
    }
}
