package io.github.shek014.openlift.ui.routines

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.shek014.openlift.data.RecommendedSplits
import io.github.shek014.openlift.data.Split
import io.github.shek014.openlift.data.model.Exercise
import io.github.shek014.openlift.data.model.ExercisePlan
import io.github.shek014.openlift.data.model.Routine
import io.github.shek014.openlift.data.model.format
import io.github.shek014.openlift.data.model.isValidRepsInput
import io.github.shek014.openlift.ui.exercises.ExerciseThumbnail
import io.github.shek014.openlift.ui.workout.NumberField

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 4.dp),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
    )
    HorizontalDivider(color = MaterialTheme.colorScheme.onBackground)
}

@Composable
private fun PlannedExerciseRow(exercise: Exercise, plan: ExercisePlan, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(exercise.name) },
        supportingContent = { Text(plan.format(), style = MaterialTheme.typography.labelMedium) },
        leadingContent = { ExerciseThumbnail(exercise.id) },
        modifier = Modifier.clickable(onClick = onClick),
    )
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

/** One line about a routine, for lists of routines. */
fun Routine.summary(): String {
    val exerciseCount = if (exercises.size == 1) "1 exercise" else "${exercises.size} exercises"
    val setCount = if (totalSets == 1) "1 set" else "$totalSets sets"
    return "$exerciseCount · $setCount"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutinesScreen(
    onNewRoutine: () -> Unit,
    onRoutineClick: (Long) -> Unit,
    onSplitClick: (String) -> Unit,
    viewModel: RoutinesViewModel = viewModel(factory = RoutinesViewModel.Factory),
) {
    val routines by viewModel.routines.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Routines") })
        val saved = routines ?: return@Column
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item(key = "new") {
                Button(
                    onClick = onNewRoutine,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                ) {
                    Text("New routine")
                }
            }
            item(key = "mine-title") { SectionTitle("My routines") }
            if (saved.isEmpty()) {
                item(key = "mine-empty") {
                    Text(
                        text = "Build your own, or add a recommended split below.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(saved, key = { "routine-${it.id}" }) { routine ->
                ListItem(
                    headlineContent = { Text(routine.name) },
                    supportingContent = { Text(routine.summary(), style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier.clickable { onRoutineClick(routine.id) },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
            item(key = "splits-title") { SectionTitle("Recommended splits") }
            items(RecommendedSplits, key = { "split-${it.id}" }) { split ->
                ListItem(
                    headlineContent = { Text(split.name) },
                    supportingContent = { Text(split.schedule, style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier.clickable { onSplitClick(split.id) },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineDetailScreen(
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onExerciseClick: (String) -> Unit,
    onWorkoutStarted: () -> Unit,
    viewModel: RoutineDetailViewModel = viewModel(factory = RoutineDetailViewModel.Factory),
) {
    val routine by viewModel.routine.collectAsStateWithLifecycle()
    var confirmingDelete by remember { mutableStateOf(false) }
    var workoutInProgress by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {},
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                TextButton(onClick = onEdit) { Text("Edit") }
                TextButton(onClick = { confirmingDelete = true }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
        )
        val current = routine ?: return@Column
        LazyColumn(modifier = Modifier.weight(1f)) {
            item(key = "heading") {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)) {
                    Text(current.name, style = MaterialTheme.typography.headlineMedium)
                    Text(
                        text = current.summary(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.onBackground)
            }
            // By position, since a routine can include the same exercise twice.
            itemsIndexed(current.exercises, key = { index, _ -> index }) { _, planned ->
                PlannedExerciseRow(planned.exercise, planned.plan) { onExerciseClick(planned.exercise.id) }
            }
        }
        Button(
            onClick = {
                viewModel.startWorkout { started ->
                    if (started) onWorkoutStarted() else workoutInProgress = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text("Start workout")
        }
    }

    if (confirmingDelete) {
        AlertDialog(
            onDismissRequest = { confirmingDelete = false },
            title = { Text("Delete this routine?") },
            text = { Text("Workouts you've already logged from it are kept.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmingDelete = false
                        viewModel.delete()
                        onBack()
                    },
                ) {
                    Text("Delete")
                }
            },
            dismissButton = { TextButton(onClick = { confirmingDelete = false }) { Text("Cancel") } },
        )
    }
    if (workoutInProgress) {
        AlertDialog(
            onDismissRequest = { workoutInProgress = false },
            title = { Text("Workout in progress") },
            text = { Text("Finish or discard your current workout before starting another.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        workoutInProgress = false
                        onWorkoutStarted()
                    },
                ) {
                    Text("Go to workout")
                }
            },
            dismissButton = { TextButton(onClick = { workoutInProgress = false }) { Text("Cancel") } },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitDetailScreen(
    onBack: () -> Unit,
    onExerciseClick: (String) -> Unit,
    onAdded: () -> Unit,
    viewModel: SplitDetailViewModel = viewModel(factory = SplitDetailViewModel.Factory),
) {
    val split: Split = viewModel.split
    val exercises by viewModel.exercises.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {},
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
        )
        LazyColumn(modifier = Modifier.weight(1f)) {
            item(key = "heading") {
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(split.name, style = MaterialTheme.typography.headlineMedium)
                    Text(
                        text = split.schedule,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(split.description, style = MaterialTheme.typography.bodyLarge)
                }
            }
            split.days.forEach { day ->
                item(key = "day-${day.name}") { SectionTitle(day.name) }
                items(day.exercises, key = { "${day.name}-${it.exerciseId}" }) { planned ->
                    exercises[planned.exerciseId]?.let { exercise ->
                        PlannedExerciseRow(exercise, planned.plan) { onExerciseClick(exercise.id) }
                    }
                }
            }
        }
        val routineCount = if (split.days.size == 1) "1 routine" else "${split.days.size} routines"
        Button(
            onClick = { viewModel.addToRoutines(onAdded) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text("Add $routineCount")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineEditorScreen(
    viewModel: RoutineEditorViewModel,
    onBack: () -> Unit,
    onAddExercise: () -> Unit,
    onReorder: () -> Unit,
    onSaved: () -> Unit,
) {
    var confirmingDiscard by rememberSaveable { mutableStateOf(false) }
    val leave = { if (viewModel.hasChanges) confirmingDiscard = true else onBack() }

    BackHandler(onBack = leave)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
    ) {
        TopAppBar(
            title = { Text(if (viewModel.isNew) "New routine" else "Edit routine") },
            navigationIcon = {
                IconButton(onClick = leave) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                TextButton(enabled = viewModel.canSave, onClick = { viewModel.save(onSaved) }) {
                    Text("Save")
                }
            },
        )
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item(key = "name") {
                OutlinedTextField(
                    value = viewModel.name,
                    onValueChange = viewModel::updateName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    label = { Text("Name") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )
            }
            itemsIndexed(viewModel.exercises, key = { _, draft -> draft.key }) { _, draft ->
                DraftExerciseRow(
                    draft = draft,
                    onChange = { update -> viewModel.updateExercise(draft.key, update) },
                    onRemove = { viewModel.removeExercise(draft.key) },
                )
            }
            item(key = "actions") {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(onClick = onAddExercise, modifier = Modifier.fillMaxWidth()) {
                        Text("Add exercise")
                    }
                    if (viewModel.exercises.size > 1) {
                        OutlinedButton(onClick = onReorder, modifier = Modifier.fillMaxWidth()) {
                            Text("Reorder exercises")
                        }
                    }
                }
            }
        }
    }

    if (confirmingDiscard) {
        AlertDialog(
            onDismissRequest = { confirmingDiscard = false },
            title = { Text("Discard changes?") },
            text = { Text("What you've changed in this routine won't be saved.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmingDiscard = false
                        onBack()
                    },
                ) {
                    Text("Discard")
                }
            },
            dismissButton = { TextButton(onClick = { confirmingDiscard = false }) { Text("Keep editing") } },
        )
    }
}

@Composable
private fun DraftExerciseRow(
    draft: DraftExercise,
    onChange: ((DraftExercise) -> DraftExercise) -> Unit,
    onRemove: () -> Unit,
) {
    val name = draft.exercise.name
    Column(modifier = Modifier.padding(top = 12.dp)) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ExerciseThumbnail(draft.exercise.id, size = 40.dp)
            Text(
                text = name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Close, contentDescription = "Remove $name")
            }
        }
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PlanLabel("Sets")
            PlanField(draft.setsText, "Sets for $name") { text -> onChange { it.copy(setsText = text) } }
            if (!draft.exercise.isTimed) {
                PlanLabel("Reps")
                PlanField(draft.repsMinText, "Lowest reps for $name") { text -> onChange { it.copy(repsMinText = text) } }
                PlanLabel("to")
                PlanField(draft.repsMaxText, "Highest reps for $name") { text -> onChange { it.copy(repsMaxText = text) } }
            }
        }
        if (draft.plan == null) {
            Text(
                text = "Enter a number of sets from 1 to 20.",
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun PlanLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun PlanField(value: String, description: String, onChange: (String) -> Unit) {
    Column(modifier = Modifier.width(52.dp)) {
        NumberField(
            value = value,
            placeholder = "",
            description = description,
            keyboardType = KeyboardType.Number,
            onValueChange = { if (isValidRepsInput(it)) onChange(it) },
        )
    }
}
