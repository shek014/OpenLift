package io.github.shek014.openlift.ui.exercises

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
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
    viewModel: ExerciseListViewModel = viewModel(factory = ExerciseListViewModel.Factory),
) {
    val exercises by viewModel.exercises.collectAsStateWithLifecycle()
    val filter = viewModel.filter
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
        )
        // The search and filters scroll with the list, so that the list still has room in landscape.
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(hideKeyboardOnScroll),
        ) {
            item(key = "search") {
                SearchField(query = filter.query, onQueryChange = viewModel::setQuery)
            }
            item(key = "groups") {
                ChipRow(
                    items = MuscleGroup.entries,
                    selected = filter.group,
                    label = { it.label },
                    onClick = viewModel::toggleGroup,
                )
            }
            filter.group?.let { group ->
                item(key = "muscles") {
                    ChipRow(
                        items = Muscle.entries.filter { it.group == group },
                        selected = filter.muscle,
                        label = { it.label },
                        onClick = viewModel::toggleMuscle,
                    )
                }
            }
            item(key = "equipment") {
                ChipRow(
                    items = Equipment.entries,
                    selected = filter.equipment,
                    label = { it.label },
                    onClick = viewModel::toggleEquipment,
                )
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
                            ExerciseRow(exercise = exercise, onClick = { onExerciseClick(exercise.id) })
                        }
                        item(key = "all-title") { ListSectionTitle("All exercises") }
                    }
                    items(results, key = { it.id }) { exercise ->
                        ExerciseRow(exercise = exercise, onClick = { onExerciseClick(exercise.id) })
                    }
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
private fun <T> ChipRow(
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
