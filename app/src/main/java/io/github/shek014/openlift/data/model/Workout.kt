package io.github.shek014.openlift.data.model

import java.util.Locale

data class WorkoutSet(
    val id: Long,
    val weightKg: Double?,
    val reps: Int?,
    val completed: Boolean,
)

data class WorkoutExercise(
    val id: Long,
    val exercise: Exercise,
    val sets: List<WorkoutSet>,
)

data class Workout(
    val id: Long,
    val startedAt: Long,
    val finishedAt: Long?,
    val exercises: List<WorkoutExercise>,
) {
    val completedSets: List<WorkoutSet>
        get() = exercises.flatMap { entry -> entry.sets.filter { it.completed } }

    /** The total weight lifted: weight times reps, added up over the completed sets. */
    val volumeKg: Double
        get() = completedSets.sumOf { (it.weightKg ?: 0.0) * (it.reps ?: 0) }
}

/** A completed set from a finished workout. */
data class LoggedSet(
    val workoutId: Long,
    val finishedAt: Long,
    val exerciseId: String,
    val weightKg: Double?,
    val reps: Int,
)

/** What to suggest for a blank set: the same numbers as a set you've done before. */
data class SetHint(val weightKg: Double?, val reps: Int?)

/**
 * The sets of the most recent workout containing each exercise. [logged] must be newest first,
 * which is how the database returns them.
 */
fun previousSetsByExercise(logged: List<LoggedSet>): Map<String, List<LoggedSet>> {
    val latestWorkout = mutableMapOf<String, Long>()
    logged.forEach { latestWorkout.putIfAbsent(it.exerciseId, it.workoutId) }
    return logged
        .filter { latestWorkout[it.exerciseId] == it.workoutId }
        .groupBy { it.exerciseId }
}

/**
 * The suggestion for one set in a workout: what you did for that set last time, or if there's no
 * such set, the numbers in the set above it.
 */
fun setHint(sets: List<WorkoutSet>, index: Int, previous: List<LoggedSet>): SetHint? {
    previous.getOrNull(index)?.let { return SetHint(it.weightKg, it.reps) }
    return sets.take(index).lastOrNull { it.weightKg != null || it.reps != null }
        ?.let { SetHint(it.weightKg, it.reps) }
}

/** Accepts up to four digits and two decimals while someone is typing, with . or , as the point. */
fun isValidWeightInput(text: String): Boolean = Regex("""\d{0,4}([.,]\d{0,2})?""").matches(text)

fun isValidRepsInput(text: String): Boolean = Regex("""\d{0,3}""").matches(text)

fun parseWeight(text: String): Double? = text.replace(',', '.').toDoubleOrNull()

/** Zero reps isn't a set, so it counts as blank. */
fun parseReps(text: String): Int? = text.toIntOrNull()?.takeIf { it > 0 }

/** 20.0 becomes "20", and 22.5 stays "22.5". */
fun formatWeight(weightKg: Double): String =
    if (weightKg % 1.0 == 0.0) {
        weightKg.toLong().toString()
    } else {
        String.format(Locale.ROOT, "%.2f", weightKg).trimEnd('0')
    }

/** How a completed set reads in a log: "20 kg × 10", or "× 10" with no added weight. */
fun formatSet(weightKg: Double?, reps: Int?): String {
    val repsText = "× ${reps ?: "-"}"
    return if (weightKg == null) repsText else "${formatWeight(weightKg)} kg $repsText"
}

/** 95 seconds becomes "1:35", and an hour or more becomes "1:01:35". */
fun formatDuration(totalSeconds: Long): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.ROOT, "%d:%02d", minutes, seconds)
    }
}
