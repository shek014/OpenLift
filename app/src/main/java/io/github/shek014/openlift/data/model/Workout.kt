package io.github.shek014.openlift.data.model

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToLong

enum class WeightUnit(val label: String, val kilogramsPerUnit: Double) {
    Kg("kg", 1.0),
    Lb("lb", 0.45359237),
}

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
    /** How long to rest after a set: null means the default from Settings, and 0 means no timer. */
    val restSeconds: Int? = null,
    val targetRepsMin: Int? = null,
    val targetRepsMax: Int? = null,
)

data class Workout(
    val id: Long,
    val startedAt: Long,
    val finishedAt: Long?,
    val exercises: List<WorkoutExercise>,
    /** The name of the routine it was started from, if any. */
    val name: String? = null,
) {
    /** The exercises that have something to show once unchecked sets are ignored. */
    val completedExercises: List<WorkoutExercise>
        get() = exercises.filter { entry -> entry.sets.any { it.completed } }

    val completedSets: List<WorkoutSet>
        get() = exercises.flatMap { entry -> entry.sets.filter { it.completed } }

    val durationSeconds: Long
        get() = (((finishedAt ?: startedAt) - startedAt) / 1000).coerceAtLeast(0)

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
 * such set, the numbers in the set above it, or else the low end of the routine's target.
 */
fun setHint(sets: List<WorkoutSet>, index: Int, previous: List<LoggedSet>, targetReps: Int? = null): SetHint? {
    previous.getOrNull(index)?.let { return SetHint(it.weightKg, it.reps) }
    return sets.take(index).lastOrNull { it.weightKg != null || it.reps != null }
        ?.let { SetHint(it.weightKg, it.reps) }
        ?: targetReps?.let { SetHint(null, it) }
}

/** Accepts up to four digits and two decimals while someone is typing, with . or , as the point. */
fun isValidWeightInput(text: String): Boolean = Regex("""\d{0,4}([.,]\d{0,2})?""").matches(text)

fun isValidRepsInput(text: String): Boolean = Regex("""\d{0,3}""").matches(text)

/** Reads a weight typed in [unit] as kilograms, which is how every weight is stored. */
fun parseWeight(text: String, unit: WeightUnit = WeightUnit.Kg): Double? =
    text.replace(',', '.').toDoubleOrNull()?.times(unit.kilogramsPerUnit)

/** Zero reps isn't a set, so it counts as blank. */
fun parseReps(text: String): Int? = text.toIntOrNull()?.takeIf { it > 0 }

/** Shows a weight stored in kilograms in [unit]. 20.0 becomes "20", and 22.5 stays "22.5". */
fun formatWeight(weightKg: Double, unit: WeightUnit = WeightUnit.Kg): String =
    String.format(Locale.ROOT, "%.2f", weightKg / unit.kilogramsPerUnit).trimEnd('0').trimEnd('.')

/** The total weight lifted, rounded to a whole number. */
fun formatVolume(volumeKg: Double, unit: WeightUnit = WeightUnit.Kg): String =
    "${(volumeKg / unit.kilogramsPerUnit).roundToLong()} ${unit.label}"

/** Whether two weights are the same once rounding is ignored. */
fun sameWeight(first: Double?, second: Double?): Boolean =
    if (first == null || second == null) first == second else abs(first - second) < 0.001

/** How a completed set reads in a log: "20 kg × 10", or "× 10" with no added weight. */
fun formatSet(weightKg: Double?, reps: Int?, unit: WeightUnit = WeightUnit.Kg): String {
    val repsText = "× ${reps ?: "-"}"
    return if (weightKg == null) repsText else "${formatWeight(weightKg, unit)} ${unit.label} $repsText"
}

fun formatRest(seconds: Int): String = if (seconds == 0) "Off" else formatDuration(seconds.toLong())

fun formatDate(millis: Long): String =
    Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))

/** A copy of the list with the item at [from] moved to [to]. Out-of-range moves change nothing. */
fun <T> List<T>.moved(from: Int, to: Int): List<T> {
    if (from !in indices || to !in indices || from == to) return this
    return toMutableList().apply { add(to, removeAt(from)) }
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
