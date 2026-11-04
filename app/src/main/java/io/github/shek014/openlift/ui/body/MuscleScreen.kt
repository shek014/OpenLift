package io.github.shek014.openlift.ui.body

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.shek014.openlift.data.model.Exercise
import io.github.shek014.openlift.ui.exercises.ExerciseRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MuscleScreen(
    onBack: () -> Unit,
    onExerciseClick: (exerciseId: String) -> Unit,
    viewModel: MuscleViewModel = viewModel(factory = MuscleViewModel.Factory),
) {
    val exercises by viewModel.exercises.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(viewModel.muscle.label) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
        )
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item(key = "map") {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    BodyMap(
                        emphasis = mapOf(viewModel.muscle to MuscleEmphasis.Primary),
                        modifier = Modifier
                            .width(240.dp)
                            .padding(16.dp),
                    )
                }
            }
            exercises?.let {
                exerciseSection("Main exercises", it.main, onExerciseClick)
                exerciseSection("Also works it", it.assisting, onExerciseClick)
            }
        }
    }
}

private fun LazyListScope.exerciseSection(
    title: String,
    exercises: List<Exercise>,
    onExerciseClick: (String) -> Unit,
) {
    if (exercises.isEmpty()) return
    item(key = title) {
        Text(
            text = title,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.onBackground)
    }
    items(exercises, key = { it.id }) { exercise ->
        ExerciseRow(exercise = exercise, onClick = { onExerciseClick(exercise.id) })
    }
}
