package io.github.shek014.openlift.data.model

/** How much of an exercise to do. Null reps mean there's no target. */
data class ExercisePlan(
    val sets: Int,
    val repsMin: Int? = null,
    val repsMax: Int? = null,
)

data class RoutineExercise(
    val exercise: Exercise,
    val plan: ExercisePlan,
)

data class Routine(
    val id: Long,
    val name: String,
    val exercises: List<RoutineExercise>,
) {
    val totalSets: Int
        get() = exercises.sumOf { it.plan.sets }
}

/** The target reps alone: "8–12", or "8" if there's no range. */
fun formatRepsTarget(repsMin: Int?, repsMax: Int?): String? = when {
    repsMin == null -> null
    repsMax == null || repsMax == repsMin -> "$repsMin"
    else -> "$repsMin–$repsMax"
}

/** "3 × 8–12", or "3 sets" with no rep target. */
fun ExercisePlan.format(): String {
    val reps = formatRepsTarget(repsMin, repsMax)
    return when {
        reps != null -> "$sets × $reps"
        sets == 1 -> "1 set"
        else -> "$sets sets"
    }
}

const val MaxPlannedSets = 20

/**
 * Reads a plan from what's typed in the routine editor, or null if the sets aren't a usable number.
 * Reps are optional. A lone number is a fixed target, and a backwards range gets turned around.
 */
fun parseExercisePlan(setsText: String, repsMinText: String, repsMaxText: String): ExercisePlan? {
    val sets = setsText.toIntOrNull()?.takeIf { it in 1..MaxPlannedSets } ?: return null
    val first = parseReps(repsMinText)
    val second = parseReps(repsMaxText)
    val low = listOfNotNull(first, second).minOrNull()
    val high = listOfNotNull(first, second).maxOrNull()
    return ExercisePlan(sets, low, high)
}
