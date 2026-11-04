package io.github.shek014.openlift.ui.exercises

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.shek014.openlift.data.model.Exercise
import io.github.shek014.openlift.data.model.Muscle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    onBack: () -> Unit,
    viewModel: ExerciseDetailViewModel = viewModel(factory = ExerciseDetailViewModel.Factory),
) {
    val exercise by viewModel.exercise.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {},
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
        )
        exercise?.let { ExerciseDetails(it) }
    }
}

@Composable
private fun ExerciseDetails(exercise: Exercise) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Column {
            Text(exercise.name, style = MaterialTheme.typography.headlineMedium)
            Text(
                text = exercise.equipment.label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        MuscleSection(
            title = "Primary muscles",
            muscles = exercise.primaryMuscles,
            color = MaterialTheme.colorScheme.primaryContainer,
        )
        if (exercise.secondaryMuscles.isNotEmpty()) {
            MuscleSection(
                title = "Secondary muscles",
                muscles = exercise.secondaryMuscles,
                color = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MuscleSection(title: String, muscles: List<Muscle>, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            muscles.forEach { muscle ->
                Surface(shape = CircleShape, color = color) {
                    Text(
                        text = muscle.label,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}
