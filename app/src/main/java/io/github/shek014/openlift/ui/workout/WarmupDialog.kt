package io.github.shek014.openlift.ui.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.shek014.openlift.data.AppSettings
import io.github.shek014.openlift.data.model.LoggedSet
import io.github.shek014.openlift.data.model.WorkoutExercise
import io.github.shek014.openlift.data.model.formatPlates
import io.github.shek014.openlift.data.model.formatWeight
import io.github.shek014.openlift.data.model.isValidWeightInput
import io.github.shek014.openlift.data.model.parseWeight
import io.github.shek014.openlift.data.model.platesPerSide
import io.github.shek014.openlift.data.model.usesBar
import io.github.shek014.openlift.data.model.warmupIncrement
import io.github.shek014.openlift.data.model.warmupSets
import io.github.shek014.openlift.ui.LocalWeightUnit

/**
 * Works out warm-up sets from the weight you'll be lifting. Weights in here are all in the unit
 * being shown, and only become kilograms when they're added to the workout.
 */
@Composable
fun WarmupDialog(
    entry: WorkoutExercise,
    previous: List<LoggedSet>,
    settings: AppSettings,
    onAdd: (List<Pair<Double, Int>>) -> Unit,
    onDismiss: () -> Unit,
) {
    val unit = LocalWeightUnit.current
    val usesBar = entry.exercise.equipment.usesBar
    // Starts from whatever's already been entered for this exercise, or else the last workout.
    val startingKg = entry.sets.firstOrNull { !it.isWarmup && it.weightKg != null }?.weightKg
        ?: previous.firstOrNull()?.weightKg
    var workingText by remember { mutableStateOf(startingKg?.let { formatWeight(it, unit) }.orEmpty()) }
    var barText by remember { mutableStateOf(formatWeight(settings.barWeight(unit) * unit.kilogramsPerUnit, unit)) }

    val working = parseWeight(workingText)
    val bar = if (usesBar) parseWeight(barText) ?: 0.0 else null
    val sets = if (working == null) emptyList() else warmupSets(working, unit.warmupIncrement(), bar)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Warm-up sets") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LabeledField("Working weight (${unit.label})", workingText, "Working weight") { workingText = it }
                if (usesBar) {
                    LabeledField("Bar (${unit.label})", barText, "Bar weight") { barText = it }
                }
                if (sets.isEmpty()) {
                    Text(
                        text = if (working == null) "Enter the weight you'll be lifting." else "This is light enough to skip warming up.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                sets.forEach { set ->
                    Column {
                        Text(
                            text = "${formatWeight(set.weight)} ${unit.label} × ${set.reps}",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        if (bar != null) {
                            Text(
                                text = "Each side: ${formatPlates(platesPerSide(set.weight, bar, unit))}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = sets.isNotEmpty(),
                onClick = {
                    if (bar != null && bar > 0) settings.setBarWeight(unit, bar)
                    onAdd(sets.map { it.weight * unit.kilogramsPerUnit to it.reps })
                    onDismiss()
                },
            ) {
                Text("Add to workout")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

/** Works out what to load on each side of the bar for a weight. */
@Composable
fun PlateCalculatorDialog(startingWeightKg: Double?, settings: AppSettings, onDismiss: () -> Unit) {
    val unit = LocalWeightUnit.current
    var weightText by remember { mutableStateOf(startingWeightKg?.let { formatWeight(it, unit) }.orEmpty()) }
    var barText by remember { mutableStateOf(formatWeight(settings.barWeight(unit) * unit.kilogramsPerUnit, unit)) }
    val weight = parseWeight(weightText)
    val bar = parseWeight(barText) ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Plate calculator") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LabeledField("Total weight (${unit.label})", weightText, "Total weight") { weightText = it }
                LabeledField("Bar (${unit.label})", barText, "Bar weight") { barText = it }
                when {
                    weight == null -> Text(
                        text = "Enter the weight you want on the bar.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    weight < bar -> Text(
                        text = "That's lighter than the bar.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                    else -> {
                        val plates = platesPerSide(weight, bar, unit)
                        val loaded = bar + plates.sum() * 2
                        Column {
                            Text("Each side", style = MaterialTheme.typography.labelMedium)
                            Text(formatPlates(plates), style = MaterialTheme.typography.titleLarge)
                            if (loaded < weight - 0.001) {
                                Text(
                                    text = "Closest with standard plates: ${formatWeight(loaded)} ${unit.label}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (bar > 0) settings.setBarWeight(unit, bar)
                    onDismiss()
                },
            ) {
                Text("Done")
            }
        },
    )
}

@Composable
private fun LabeledField(label: String, value: String, description: String, onChange: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Column(modifier = Modifier.width(84.dp)) {
            NumberField(
                value = value,
                placeholder = "",
                description = description,
                keyboardType = KeyboardType.Decimal,
                onValueChange = { if (isValidWeightInput(it)) onChange(it) },
            )
        }
    }
}
