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

/** The kind of a set. These names are saved in the database, so renaming one needs a migration. */
enum class SetType(val label: String, val badge: String) {
    Normal("Normal set", ""),
    Warmup("Warm-up set", "W"),
    Superset("Superset", "S"),
    Drop("Drop set", "D"),
    Failure("Failure set", "F"),
}

data class WorkoutSet(
    val id: Long,
    val weightKg: Double?,
    val reps: Int?,
    val completed: Boolean,
    val setType: SetType = SetType.Normal,
    /** How long it lasted, for an exercise tracked by time. */
    val durationSeconds: Int? = null,
) {
    /** Warm-ups are logged, but don't count toward totals, records or suggestions. */
    val isWarmup: Boolean
        get() = setType == SetType.Warmup
}

data class WorkoutExercise(
    val id: Long,
    val exercise: Exercise,
    val sets: List<WorkoutSet>,
    /** How long to rest after a set: null means the default from Settings, and 0 means no timer. */
    val restSeconds: Int? = null,
    val targetRepsMin: Int? = null,
    val targetRepsMax: Int? = null,
) {
    /** The completed sets that count, leaving out warm-ups. */
    val workSets: List<WorkoutSet>
        get() = sets.filter { it.completed && !it.isWarmup }
}

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
        get() = exercises.filter { entry -> entry.workSets.isNotEmpty() }

    val completedSets: List<WorkoutSet>
        get() = exercises.flatMap { entry -> entry.workSets }

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
    /** Zero for a set tracked by time. */
    val reps: Int,
    val durationSeconds: Int? = null,
    val setType: SetType = SetType.Normal,
)

/** What to suggest for a blank set: the same numbers as a set you've done before. */
data class SetHint(val weightKg: Double?, val reps: Int?, val durationSeconds: Int? = null)

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
    previous.getOrNull(index)?.let { return SetHint(it.weightKg, it.reps.takeIf { reps -> reps > 0 }, it.durationSeconds) }
    return sets.take(index).lastOrNull { it.weightKg != null || it.reps != null || it.durationSeconds != null }
        ?.let { SetHint(it.weightKg, it.reps, it.durationSeconds) }
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
fun formatSet(
    weightKg: Double?,
    reps: Int?,
    unit: WeightUnit = WeightUnit.Kg,
    durationSeconds: Int? = null,
): String {
    val amount = if (durationSeconds != null) formatDuration(durationSeconds.toLong()) else "× ${reps ?: "-"}"
    val weight = weightKg?.takeIf { it > 0 }
    return if (weight == null) amount else "${formatWeight(weight, unit)} ${unit.label} $amount"
}

/** The set's label in a list: "1", "2D" for a drop set, "W1" for the first warm-up. */
fun setLabel(type: SetType, number: Int): String =
    if (type == SetType.Warmup) "W$number" else "$number${type.badge}"

/** Allows seconds, or minutes and seconds like "1:30", while someone is typing. */
fun isValidDurationInput(text: String): Boolean = Regex("""\d{0,4}(:\d{0,2})?""").matches(text)

/** Reads "90" or "1:30" as a number of seconds, or null if it's blank or zero. */
fun parseDuration(text: String): Int? {
    val parts = text.split(":")
    val seconds = when (parts.size) {
        1 -> parts[0].toIntOrNull()
        else -> (parts[0].toIntOrNull() ?: 0) * 60 + (parts[1].toIntOrNull() ?: 0)
    }
    return seconds?.takeIf { it > 0 }
}

/** The workout as plain text, for sharing to other apps. */
fun Workout.toShareText(unit: WeightUnit): String = buildString {
    appendLine(name ?: "Workout")
    appendLine(formatDate(finishedAt ?: startedAt))
    appendLine("${formatDuration(durationSeconds)} · ${completedSets.size} sets · ${formatVolume(volumeKg, unit)}")
    completedExercises.forEach { entry ->
        appendLine()
        appendLine(entry.exercise.name)
        entry.workSets.forEachIndexed { index, set ->
            val type = if (set.setType == SetType.Normal) "" else " (${set.setType.label})"
            appendLine("${index + 1}. ${formatSet(set.weightKg, set.reps, unit, set.durationSeconds)}$type")
        }
    }
    appendLine()
    append("Logged with OpenLift")
}

/** What this workout would be as a routine: the same exercises, sets and range of reps. */
fun Workout.toRoutinePlans(): List<Pair<String, ExercisePlan>> =
    completedExercises.map { entry ->
        val reps = entry.workSets.mapNotNull { it.reps }
        entry.exercise.id to ExercisePlan(entry.workSets.size, reps.minOrNull(), reps.maxOrNull())
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
