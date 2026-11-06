package io.github.shek014.openlift.ui.exercises

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.shek014.openlift.data.model.Equipment
import io.github.shek014.openlift.data.model.Exercise
import io.github.shek014.openlift.data.model.Muscle
import io.github.shek014.openlift.data.model.MuscleGroup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseListScreen(
    onExerciseClick: (exerciseId: String) -> Unit,
    title: String = "Exercises",
    recentExercises: List<Exercise> = emptyList(),
    onBack: (() -> Unit)? = null,
    onNewExercise: () -> Unit = {},
    onAddExercises: ((List<String>) -> Unit)? = null,
    viewModel: ExerciseListViewModel = viewModel(factory = ExerciseListViewModel.Factory),
) {
    val exercises by viewModel.exercises.collectAsStateWithLifecycle()
    val filter = viewModel.filter
    var openFilter by rememberSaveable { mutableStateOf<FilterWindow?>(null) }
    val focusManager = LocalFocusManager.current
    val hideKeyboardOnScroll = remember(focusManager) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput && available.y != 0f) focusManager.clearFocus()
                return Offset.Zero
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
    ) {
        TopAppBar(
            title = { Text(title) },
            navigationIcon = {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            },
            actions = {
                IconButton(onClick = onNewExercise) {
                    Icon(Icons.Filled.Add, contentDescription = "New exercise")
                }
            },
        )
        // The search and filters scroll with the list, so that the list still has room in landscape.
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .nestedScroll(hideKeyboardOnScroll),
        ) {
            item(key = "search") {
                SearchField(query = filter.query, onQueryChange = viewModel::setQuery)
            }
            item(key = "filters") {
                Row(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterButton(
                        label = filter.equipment?.label ?: "All equipment",
                        active = filter.equipment != null,
                        onClick = { openFilter = FilterWindow.Equipment },
                        onClear = { viewModel.chooseEquipment(null) },
                        modifier = Modifier.weight(1f),
                    )
                    FilterButton(
                        label = filter.muscle?.label ?: filter.group?.label ?: "All muscles",
                        active = filter.group != null,
                        onClick = { openFilter = FilterWindow.Muscles },
                        onClear = { viewModel.chooseGroup(null) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            exercises?.let { results ->
                if (results.isEmpty()) {
                    item(key = "no_results") {
                        NoResults(
                            onClearFilter = {
                                focusManager.clearFocus()
                                viewModel.clearFilter()
                            },
                        )
                    }
                } else {
                    // Recent exercises only make sense before someone has started narrowing things down.
                    val showRecent = recentExercises.isNotEmpty() && filter == ExerciseFilter()
                    if (showRecent) {
                        item(key = "recent-title") { ListSectionTitle("Recent") }
                        items(recentExercises, key = { "recent-${it.id}" }) { exercise ->
                            ListedExercise(exercise, viewModel.selectedIds, onAddExercises != null, viewModel, onExerciseClick)
                        }
                        item(key = "all-title") { ListSectionTitle("All exercises") }
                    }
                    items(results, key = { it.id }) { exercise ->
                        ListedExercise(exercise, viewModel.selectedIds, onAddExercises != null, viewModel, onExerciseClick)
                    }
                }
            }
        }
        val selected = viewModel.selectedIds
        if (onAddExercises != null && selected.isNotEmpty()) {
            Button(
                onClick = { onAddExercises(selected) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(if (selected.size == 1) "Add 1 exercise" else "Add ${selected.size} exercises")
            }
        }
    }

    when (openFilter) {
        FilterWindow.Equipment -> EquipmentWindow(
            selected = filter.equipment,
            onChoose = {
                viewModel.chooseEquipment(it)
                openFilter = null
            },
            onDismiss = { openFilter = null },
        )
        FilterWindow.Muscles -> MuscleWindow(
            filter = filter,
            onChooseGroup = {
                viewModel.chooseGroup(it)
                openFilter = null
            },
            onChooseMuscle = {
                viewModel.chooseMuscle(it)
                openFilter = null
            },
            onDismiss = { openFilter = null },
        )
        null -> Unit
    }
}

private enum class FilterWindow { Equipment, Muscles }

/** A row that opens the exercise's page, or picks it when adding several at once. */
@Composable
private fun ListedExercise(
    exercise: Exercise,
    selectedIds: List<String>,
    picking: Boolean,
    viewModel: ExerciseListViewModel,
    onOpen: (String) -> Unit,
) {
    if (picking) {
        ExerciseRow(
            exercise = exercise,
            onClick = { viewModel.toggleSelected(exercise.id) },
            selected = exercise.id in selectedIds,
            onInfo = { onOpen(exercise.id) },
        )
    } else {
        ExerciseRow(exercise = exercise, onClick = { onOpen(exercise.id) })
    }
}

/** Shows the current choice and opens its window. When something's chosen, it can be cleared. */
@Composable
private fun FilterButton(
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = MaterialTheme.shapes.medium,
        color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = if (active) 0.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = label,
                modifier = Modifier.weight(1f, fill = active),
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (active) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Filled.Clear, contentDescription = "Clear $label")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterWindowSheet(title: String, onDismiss: () -> Unit, content: LazyListScope.() -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleLarge,
        )
        LazyColumn(content = content)
    }
}

@Composable
private fun FilterChoice(label: String, selected: Boolean, indented: Boolean = false, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = if (indented) 32.dp else 16.dp, end = 16.dp, top = 14.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = if (indented) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
        )
        if (selected) {
            Icon(Icons.Filled.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun EquipmentWindow(selected: Equipment?, onChoose: (Equipment?) -> Unit, onDismiss: () -> Unit) {
    FilterWindowSheet("Equipment", onDismiss) {
        item { FilterChoice("All equipment", selected == null) { onChoose(null) } }
        items(Equipment.entries) { equipment ->
            FilterChoice(equipment.label, equipment == selected) { onChoose(equipment) }
        }
    }
}

@Composable
private fun MuscleWindow(
    filter: ExerciseFilter,
    onChooseGroup: (MuscleGroup?) -> Unit,
    onChooseMuscle: (Muscle) -> Unit,
    onDismiss: () -> Unit,
) {
    FilterWindowSheet("Muscles", onDismiss) {
        item { FilterChoice("All muscles", filter.group == null) { onChooseGroup(null) } }
        MuscleGroup.entries.forEach { group ->
            item { FilterChoice(group.label, filter.group == group && filter.muscle == null) { onChooseGroup(group) } }
            // A group of one muscle, like Triceps, has nothing further to choose.
            val muscles = Muscle.entries.filter { it.group == group }
            if (muscles.size > 1) {
                items(muscles) { muscle ->
                    FilterChoice(muscle.label, filter.muscle == muscle, indented = true) { onChooseMuscle(muscle) }
                }
            }
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        placeholder = { Text("Search exercises") },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
    )
}

/** A sideways-scrolling row of chips where at most one is selected. */
@Composable
fun <T> ChipRow(
    items: List<T>,
    selected: T?,
    label: (T) -> String,
    onClick: (T) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items) { item ->
            FilterChip(
                selected = item == selected,
                onClick = { onClick(item) },
                label = { Text(label(item)) },
            )
        }
    }
}

@Composable
private fun ListSectionTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
    )
    HorizontalDivider(color = MaterialTheme.colorScheme.onBackground)
}

@Composable
private fun NoResults(onClearFilter: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "No exercises match your search",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TextButton(onClick = onClearFilter) {
            Text("Clear search and filters")
        }
    }
}
