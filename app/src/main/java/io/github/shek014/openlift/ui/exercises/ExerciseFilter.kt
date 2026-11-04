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
 * Muscle filters only look at primary muscles, so "Arms" lists curls and pushdowns rather than
 * every press and row that uses the arms. Search also looks at secondary muscles.
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
    }
}

private fun Exercise.searchText(): String = buildList {
    add(name)
    add(name.replace("-", "")) // So that "pullup" finds "Pull-Up".
    add(equipment.label)
    if (isCustom) add("custom")
    (primaryMuscles + secondaryMuscles).forEach { add(it.label) }
}.joinToString(" ").lowercase()
