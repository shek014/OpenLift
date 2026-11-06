package io.github.shek014.openlift.ui.exercises

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.shek014.openlift.OpenLiftApplication
import io.github.shek014.openlift.data.ExerciseRepository
import io.github.shek014.openlift.data.model.Equipment
import io.github.shek014.openlift.data.model.Exercise
import io.github.shek014.openlift.data.model.Muscle
import io.github.shek014.openlift.data.model.MuscleGroup
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Holds the list screen's state, which survives things like rotating the phone. */
class ExerciseListViewModel(repository: ExerciseRepository) : ViewModel() {
    var filter by mutableStateOf(ExerciseFilter())
        private set

    /** The exercises matching [filter], or null until they've loaded. */
    val exercises: StateFlow<List<Exercise>?> =
        combine(repository.selectableExercises, snapshotFlow { filter }) { all, filter -> all.filteredBy(filter) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setQuery(query: String) {
        filter = filter.copy(query = query)
    }

    /** Choosing a whole group, or null for everything, also clears any single muscle. */
    fun chooseGroup(group: MuscleGroup?) {
        filter = filter.copy(group = group, muscle = null)
    }

    fun chooseMuscle(muscle: Muscle) {
        filter = filter.copy(group = muscle.group, muscle = muscle)
    }

    fun chooseEquipment(equipment: Equipment?) {
        filter = filter.copy(equipment = equipment)
    }

    /** The exercises picked so far when adding several at once, in the order they were picked. */
    var selectedIds by mutableStateOf(emptyList<String>())
        private set

    fun toggleSelected(exerciseId: String) {
        selectedIds = if (exerciseId in selectedIds) selectedIds - exerciseId else selectedIds + exerciseId
    }

    fun clearFilter() {
        filter = ExerciseFilter()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ExerciseListViewModel((this[APPLICATION_KEY] as OpenLiftApplication).exerciseRepository)
            }
        }
    }
}
