package io.github.shek014.openlift.ui.exercises

import androidx.compose.foundation.clickable
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.shek014.openlift.data.model.Exercise

/** An exercise as a ruled line in a list. */
@Composable
fun ExerciseRow(exercise: Exercise, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(exercise.name) },
        supportingContent = {
            Text(
                text = buildString {
                    if (exercise.isCustom) append("Custom · ")
                    append("${exercise.equipment.label} · ${exercise.primaryMuscles.joinToString { it.label }}")
                },
                style = MaterialTheme.typography.labelMedium,
            )
        },
        leadingContent = { ExerciseThumbnail(exercise.id) },
        modifier = Modifier.clickable(onClick = onClick),
    )
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}
