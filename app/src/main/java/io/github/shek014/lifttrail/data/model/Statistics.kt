package io.github.shek014.lifttrail.data.model

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale

/** How far back the statistics look. A null number of days means everything. */
enum class StatsRange(val label: String, val days: Long?) {
    ThirtyDays("30D", 30),
    NinetyDays("90D", 90),
    OneYear("1Y", 365),
    All("All", null),
}

data class ExerciseCount(val exercise: Exercise, val sets: Int)

data class WorkoutStatistics(
    val workouts: Int,
    val sets: Int,
    val reps: Int,
    val volumeKg: Double,
    val totalSeconds: Long,
    val averageSeconds: Long,
    val averageSets: Double,
    val averageVolumeKg: Double,
    val workoutsPerWeek: Double,
    val volumeChangePercent: Double?,
    val currentStreakWeeks: Int,
    val longestStreakWeeks: Int,
    val recordsSet: Int,
    val byWeekday: Map<DayOfWeek, Int>,
    val topExercises: List<ExerciseCount>,
    val setsByMuscle: Map<Muscle, Double>,
) {
    /** Sets per muscle group, using the same counting as [setsByMuscle]. */
    val setsByGroup: Map<MuscleGroup, Double>
        get() = setsByMuscle.entries.groupBy({ it.key.group }, { it.value }).mapValues { it.value.sum() }
}

private fun Workout.finishedDate(zone: ZoneId): LocalDate? =
    finishedAt?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }

/**
 * How a workout's sets were shared between muscle groups, as fractions that add up to 1, largest
 * first. Secondary muscles count as half a set, the same as in the statistics.
 */
fun muscleSplit(exercises: List<WorkoutExercise>): List<Pair<MuscleGroup, Double>> {
    val sets = mutableMapOf<MuscleGroup, Double>()
    exercises.forEach { entry ->
        val count = entry.workSets.size.toDouble()
        if (count == 0.0) return@forEach
        // Each set is shared out, so that an exercise working two groups doesn't count twice.
        val shares = entry.exercise.primaryMuscles.map { it.group to 1.0 } +
            entry.exercise.secondaryMuscles.map { it.group to 0.5 }
        val total = shares.sumOf { it.second }
        shares.forEach { (group, share) -> sets.merge(group, count * share / total, Double::plus) }
    }
    val total = sets.values.sum()
    if (total == 0.0) return emptyList()
    return sets.map { (group, value) -> group to value / total }.sortedByDescending { it.second }
}

/**
 * The longest run of weeks with at least one workout, and the run going on now. A streak still
 * counts as going on if the last workout was last week, since this week isn't over yet.
 */
fun workoutStreaks(workouts: List<Workout>, today: LocalDate, zone: ZoneId, locale: Locale): Pair<Int, Int> {
    val firstDay = WeekFields.of(locale).firstDayOfWeek
    fun weekOf(date: LocalDate) = date.with(TemporalAdjusters.previousOrSame(firstDay))
    val weeks = workouts.mapNotNull { it.finishedDate(zone) }.map(::weekOf).toSortedSet()
    if (weeks.isEmpty()) return 0 to 0

    var longest = 0
    var run = 0
    var previous: LocalDate? = null
    weeks.forEach { week ->
        run = if (previous?.plusWeeks(1) == week) run + 1 else 1
        longest = maxOf(longest, run)
        previous = week
    }
    val thisWeek = weekOf(today)
    val current = if (weeks.last() == thisWeek || weeks.last() == thisWeek.minusWeeks(1)) run else 0
    return current to longest
}

/** How many times a workout beat a previous best at an exercise, over a list of workouts. */
fun countRecords(logged: List<LoggedSet>): Int =
    logged.groupBy { it.exerciseId }.values.sumOf { sets ->
        val sessions = sets.groupBy { it.workoutId }.values.sortedBy { it.first().finishedAt }
        ProgressMetric.entries.sumOf { metric ->
            var best: Double? = null
            sessions.count { session ->
                val value = metric.valueFor(session) ?: return@count false
                val beaten = best?.let { value > it } ?: false
                if (best == null || value > best!!) best = value
                beaten
            }
        }
    }

/**
 * Statistics for the workouts in a range. Secondary muscles count as half a set, which is a common
 * way to reflect that they do some of the work but not most of it.
 */
fun workoutStatistics(
    allWorkouts: List<Workout>,
    range: StatsRange,
    today: LocalDate,
    zone: ZoneId,
    locale: Locale,
): WorkoutStatistics {
    val finished = allWorkouts.filter { it.finishedAt != null }
    val startDate = range.days?.let { today.minusDays(it - 1) }
    val workouts = finished.filter { workout -> startDate == null || !workout.finishedDate(zone)!!.isBefore(startDate) }

    val completed = workouts.flatMap { it.completedSets }
    val volume = workouts.sumOf { it.volumeKg }
    val totalSeconds = workouts.sumOf { it.durationSeconds }
    val count = workouts.size

    val previousVolume = range.days?.let { days ->
        val previousStart = startDate!!.minusDays(days)
        finished
            .filter { val date = it.finishedDate(zone)!!; !date.isBefore(previousStart) && date.isBefore(startDate) }
            .sumOf { it.volumeKg }
    }

    val firstDate = workouts.mapNotNull { it.finishedDate(zone) }.minOrNull()
    val spanDays = when {
        range.days != null -> range.days
        firstDate != null -> ChronoUnit.DAYS.between(firstDate, today) + 1
        else -> 1L
    }

    val entries = workouts.flatMap { it.completedExercises }
    val setsByExercise = entries
        .groupBy { it.exercise.id }
        .map { (_, sameExercise) ->
            ExerciseCount(sameExercise.first().exercise, sameExercise.sumOf { entry -> entry.workSets.size })
        }

    val setsByMuscle = mutableMapOf<Muscle, Double>()
    entries.forEach { entry ->
        val sets = entry.workSets.size.toDouble()
        entry.exercise.primaryMuscles.forEach { setsByMuscle.merge(it, sets, Double::plus) }
        entry.exercise.secondaryMuscles.forEach { setsByMuscle.merge(it, sets / 2, Double::plus) }
    }

    val logged = finished.flatMap { workout ->
        workout.completedExercises.flatMap { entry ->
            entry.workSets.map {
                LoggedSet(workout.id, workout.finishedAt!!, entry.exercise.id, it.weightKg, it.reps ?: 0, it.durationSeconds, it.setType)
            }
        }
    }
    val before = logged.filter { startDate != null && Instant.ofEpochMilli(it.finishedAt).atZone(zone).toLocalDate().isBefore(startDate) }

    val (current, longest) = workoutStreaks(finished, today, zone, locale)

    return WorkoutStatistics(
        workouts = count,
        sets = completed.size,
        reps = completed.sumOf { it.reps ?: 0 },
        volumeKg = volume,
        totalSeconds = totalSeconds,
        averageSeconds = if (count == 0) 0 else totalSeconds / count,
        averageSets = if (count == 0) 0.0 else completed.size.toDouble() / count,
        averageVolumeKg = if (count == 0) 0.0 else volume / count,
        workoutsPerWeek = count / (spanDays / 7.0).coerceAtLeast(1.0),
        volumeChangePercent = previousVolume?.takeIf { it > 0 }?.let { (volume - it) / it * 100 },
        currentStreakWeeks = current,
        longestStreakWeeks = longest,
        recordsSet = countRecords(logged) - countRecords(before),
        byWeekday = workouts.groupingBy { it.finishedDate(zone)!!.dayOfWeek }.eachCount(),
        topExercises = setsByExercise.sortedByDescending { it.sets }.take(TopExerciseCount),
        setsByMuscle = setsByMuscle,
    )
}

private const val TopExerciseCount = 5

/** Days of the week in the order the locale uses, starting on its first day. */
fun orderedWeekdays(locale: Locale): List<DayOfWeek> {
    val first = WeekFields.of(locale).firstDayOfWeek
    return (0L..6L).map { first.plus(it) }
}
