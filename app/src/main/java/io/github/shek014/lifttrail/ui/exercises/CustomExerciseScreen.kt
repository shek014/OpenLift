package io.github.shek014.lifttrail.ui.exercises

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.shek014.lifttrail.LiftTrailApplication
import io.github.shek014.lifttrail.data.ExerciseRepository
import io.github.shek014.lifttrail.data.model.Equipment
import io.github.shek014.lifttrail.data.model.ExerciseDraft
import io.github.shek014.lifttrail.data.model.Muscle
import io.github.shek014.lifttrail.data.model.MuscleGroup
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** The value of the exercise id argument when the editor is making a new exercise. */
const val NewExerciseId = "new"

class CustomExerciseViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: ExerciseRepository,
) : ViewModel() {
    private val exerciseId: String? =
        checkNotNull(savedStateHandle.get<String>(ExerciseIdArg)).takeUnless { it == NewExerciseId }
    private var takenNames: List<String> = emptyList()

    val isNew: Boolean = exerciseId == null

    var draft by mutableStateOf(ExerciseDraft())
        private set
    var hasChanges by mutableStateOf(false)
        private set

    val problem: String?
        get() = draft.problem(takenNames)

    init {
        viewModelScope.launch {
            takenNames = repository.selectableExercises.first()
                .filterNot { it.id == exerciseId }
                .map { it.name }
            exerciseId?.let { id ->
                repository.exercise(id).first()?.let {
                    draft = ExerciseDraft(
                        name = it.name,
                        equipment = it.equipment,
                        primaryMuscles = it.primaryMuscles,
                        secondaryMuscles = it.secondaryMuscles,
                        notes = it.notes.orEmpty(),
                        isTimed = it.isTimed,
                    )
                }
            }
        }
    }

    fun update(change: (ExerciseDraft) -> ExerciseDraft) {
        draft = change(draft)
        hasChanges = true
    }

    fun save(onSaved: () -> Unit) {
        if (problem != null) return
        viewModelScope.launch {
            repository.saveCustomExercise(exerciseId, draft)
            onSaved()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                CustomExerciseViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    repository = (this[APPLICATION_KEY] as LiftTrailApplication).exerciseRepository,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CustomExerciseScreen(
    onDone: () -> Unit,
    viewModel: CustomExerciseViewModel = viewModel(factory = CustomExerciseViewModel.Factory),
) {
    val draft = viewModel.draft
    var confirmingDiscard by remember { mutableStateOf(false) }
    val leave = { if (viewModel.hasChanges) confirmingDiscard = true else onDone() }

    BackHandler(onBack = leave)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
    ) {
        TopAppBar(
            title = { Text(if (viewModel.isNew) "New exercise" else "Edit exercise") },
            navigationIcon = {
                IconButton(onClick = leave) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                TextButton(enabled = viewModel.problem == null, onClick = { viewModel.save(onDone) }) {
                    Text("Save")
                }
            },
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            OutlinedTextField(
                value = draft.name,
                onValueChange = { text -> viewModel.update { it.copy(name = text) } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Name") },
                singleLine = true,
                supportingText = viewModel.problem?.let { message -> { Text(message) } },
            )
            Section("Tracked by") {
                listOf(false, true).forEach { timed ->
                    FilterChip(
                        selected = draft.isTimed == timed,
                        onClick = { viewModel.update { it.copy(isTimed = timed) } },
                        label = { Text(if (timed) "Time" else "Reps") },
                    )
                }
            }
            Section("Equipment") {
                Equipment.entries.forEach { equipment ->
                    FilterChip(
                        selected = draft.equipment == equipment,
                        onClick = { viewModel.update { it.copy(equipment = equipment) } },
                        label = { Text(equipment.label) },
                    )
                }
            }
            MuscleChoices("Primary muscles", draft.primaryMuscles) { muscle ->
                viewModel.update { it.togglePrimary(muscle) }
            }
            MuscleChoices("Secondary muscles (optional)", draft.secondaryMuscles) { muscle ->
                viewModel.update { it.toggleSecondary(muscle) }
            }
            OutlinedTextField(
                value = draft.notes,
                onValueChange = { text -> viewModel.update { it.copy(notes = text) } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("How to (optional, one step per line)") },
                minLines = 3,
            )
        }
    }

    if (confirmingDiscard) {
        AlertDialog(
            onDismissRequest = { confirmingDiscard = false },
            title = { Text("Discard changes?") },
            text = { Text("What you've entered for this exercise won't be saved.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmingDiscard = false
                        onDone()
                    },
                ) {
                    Text("Discard")
                }
            },
            dismissButton = { TextButton(onClick = { confirmingDiscard = false }) { Text("Keep editing") } },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { content() }
    }
}

@Composable
private fun MuscleChoices(title: String, selected: List<Muscle>, onToggle: (Muscle) -> Unit) {
    Section(title) {
        MuscleGroup.entries.forEach { group ->
            Muscle.entries.filter { it.group == group }.forEach { muscle ->
                FilterChip(
                    selected = muscle in selected,
                    onClick = { onToggle(muscle) },
                    label = { Text(muscle.label) },
                )
            }
        }
    }
}
