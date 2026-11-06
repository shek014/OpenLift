package io.github.shek014.openlift.ui.exercises

import io.github.shek014.openlift.data.model.Equipment
import io.github.shek014.openlift.data.model.Exercise
import io.github.shek014.openlift.data.model.Muscle
import io.github.shek014.openlift.data.model.MuscleGroup

data class ExerciseFilter(
    val query: String = "",
    val group: MuscleGroup? = null,
    val muscle: Muscle? = null,
    val equipment: Equipment? = null,
)

/**
 * Muscle filters only look at primary muscles, so "Triceps" lists pushdowns rather than every press
 * that uses the triceps. Search also looks at secondary muscles, but lists those matches last.
 */
fun List<Exercise>.filteredBy(filter: ExerciseFilter): List<Exercise> {
    val searchWords = filter.query.lowercase().split(' ').filter { it.isNotEmpty() }
    return filter { exercise ->
        val matchesMuscle = when {
            filter.muscle != null -> filter.muscle in exercise.primaryMuscles
            filter.group != null -> exercise.primaryMuscles.any { it.group == filter.group }
            else -> true
        }
        val matchesEquipment = filter.equipment == null || filter.equipment == exercise.equipment
        val searchText = exercise.searchText()
        matchesMuscle && matchesEquipment && searchWords.all { it in searchText }
    }.sortedBy { exercise ->
        val mainText = exercise.searchText(includeSecondary = false)
        if (searchWords.all { it in mainText }) 0 else 1
    }
}

private fun Exercise.searchText(includeSecondary: Boolean = true): String = buildList {
    add(name)
    add(name.replace("-", "")) // So that "pullup" finds "Pull-Up".
    add(equipment.label)
    if (isCustom) add("custom")
    primaryMuscles.forEach { add(it.label) }
    if (includeSecondary) secondaryMuscles.forEach { add(it.label) }
}.joinToString(" ").lowercase()
