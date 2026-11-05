package io.github.shek014.openlift.data.model

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
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
    /** Exercises with the same number here are done back to back as a superset. */
    val supersetId: Long? = null,
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

/** The other exercises in the same superset as this one. */
fun Workout.supersetPartners(entry: WorkoutExercise): List<WorkoutExercise> {
    val id = entry.supersetId ?: return emptyList()
    return exercises.filter { it.supersetId == id && it.id != entry.id }
}

/** A superset has no rest between its exercises, only after the last one. */
fun Workout.restsAfter(entry: WorkoutExercise): Boolean {
    val position = exercises.indexOf(entry)
    return supersetPartners(entry).none { exercises.indexOf(it) > position }
}

/**
 * The order of the exercises once [entryId] is paired with [partnerId]. The partner moves to sit
 * right after it, unless the partner is already in a superset, which [entryId] then moves to join.
 */
fun Workout.orderForSuperset(entryId: Long, partnerId: Long): List<Long> {
    val partner = exercises.first { it.id == partnerId }
    val (moving, anchor) = if (partner.supersetId == null) partnerId to entryId else entryId to partnerId
    val others = exercises.filter { it.id != moving }
    val group = exercises.first { it.id == anchor }.supersetId
    val last = others.indexOfLast { it.id == anchor || (group != null && it.supersetId == group) }
    val ids = others.map { it.id }
    return ids.take(last + 1) + moving + ids.drop(last + 1)
}

/** The workout's name, or one from the time of day it started. */
fun Workout.displayName(zone: ZoneId = ZoneId.systemDefault()): String {
    name?.let { return it }
    return when (Instant.ofEpochMilli(startedAt).atZone(zone).hour) {
        in 5..11 -> "Morning workout"
        in 12..16 -> "Afternoon workout"
        in 17..20 -> "Evening workout"
        else -> "Night workout"
    }
}

/** A length of time in words: "1h 22min" or "50min". */
fun formatLongDuration(totalSeconds: Long): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    return if (hours > 0) "${hours}h ${minutes}min" else "${minutes}min"
}

/** How long ago something was: "7 hours ago", "Yesterday", or the date once it's a week old. */
fun formatTimeAgo(millis: Long, now: Long, zone: ZoneId = ZoneId.systemDefault()): String {
    val minutes = (now - millis) / 60_000
    val hours = minutes / 60
    val days = ChronoUnit.DAYS.between(
        Instant.ofEpochMilli(millis).atZone(zone).toLocalDate(),
        Instant.ofEpochMilli(now).atZone(zone).toLocalDate(),
    )
    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> if (minutes == 1L) "1 minute ago" else "$minutes minutes ago"
        hours < 24 -> if (hours == 1L) "1 hour ago" else "$hours hours ago"
        days <= 1 -> "Yesterday"
        days < 7 -> "$days days ago"
        else -> formatDate(millis, zone)
    }
}

/** The workout as plain text, for sharing to other apps. */
fun Workout.toShareText(unit: WeightUnit): String = buildString {
    appendLine(displayName())
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

fun formatDate(millis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
    Instant.ofEpochMilli(millis)
        .atZone(zone)
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
