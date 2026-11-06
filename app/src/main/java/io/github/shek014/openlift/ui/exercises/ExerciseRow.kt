package io.github.shek014.openlift.ui.exercises

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.shek014.openlift.data.model.Exercise

/**
 * An exercise as a ruled line in a list.
 *
 * @param selected whether it's been picked, when picking several at once.
 * @param onInfo opens the exercise's page, when tapping the row itself does something else.
 */
@Composable
fun ExerciseRow(
    exercise: Exercise,
    onClick: () -> Unit,
    selected: Boolean = false,
    onInfo: (() -> Unit)? = null,
) {
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
        leadingContent = {
            Box {
                ExerciseThumbnail(exercise.id)
                if (selected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 0.dp)
                            .size(18.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Selected",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
            }
        },
        trailingContent = onInfo?.let { info ->
            {
                IconButton(onClick = info) {
                    Icon(Icons.Outlined.Info, contentDescription = "About ${exercise.name}")
                }
            }
        },
        colors = if (selected) {
            ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            ListItemDefaults.colors()
        },
        modifier = Modifier.clickable(onClick = onClick),
    )
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}
