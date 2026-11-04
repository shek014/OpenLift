package io.github.shek014.openlift.data.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToLong

/** The ways to measure an exercise over time. Weight metrics are in kilograms until shown. */
enum class ProgressMetric(val label: String, val usesWeight: Boolean) {
    HeaviestWeight("Heaviest weight", true),
    EstimatedMax("Estimated 1 rep max", true),
    BestSetVolume("Best set volume", true),
    WorkoutVolume("Workout volume", true),
    MostReps("Most reps in a set", false),
    TotalReps("Total reps", false),
}

/**
 * The most someone could probably lift once, estimated with the Epley formula from a set of several
 * reps. A set of one is simply its own weight.
 */
fun estimatedMax(weightKg: Double, reps: Int): Double =
    if (reps <= 1) weightKg else weightKg * (1 + reps / 30.0)

/** This metric's value for the sets of one workout, or null if it doesn't apply to them. */
fun ProgressMetric.valueFor(sets: List<LoggedSet>): Double? {
    val weighted = sets.filter { (it.weightKg ?: 0.0) > 0 }
    return when (this) {
        ProgressMetric.HeaviestWeight -> weighted.maxOfOrNull { it.weightKg!! }
        ProgressMetric.EstimatedMax -> weighted.maxOfOrNull { estimatedMax(it.weightKg!!, it.reps) }
        ProgressMetric.BestSetVolume -> weighted.maxOfOrNull { it.weightKg!! * it.reps }
        ProgressMetric.WorkoutVolume -> weighted.takeIf { it.isNotEmpty() }?.sumOf { it.weightKg!! * it.reps }
        ProgressMetric.MostReps -> sets.maxOfOrNull { it.reps.toDouble() }
        ProgressMetric.TotalReps -> sets.takeIf { it.isNotEmpty() }?.sumOf { it.reps.toDouble() }
    }
}

/** The metrics worth showing for these sets. Weight metrics only apply if weight was ever logged. */
fun availableMetrics(logged: List<LoggedSet>): List<ProgressMetric> {
    if (logged.isEmpty()) return emptyList()
    val hasWeight = logged.any { (it.weightKg ?: 0.0) > 0 }
    return ProgressMetric.entries.filter { !it.usesWeight || hasWeight }
}

data class ChartPoint(val time: Long, val value: Double)

/** One point per workout for this metric, oldest first, leaving out anything before [since]. */
fun progressPoints(logged: List<LoggedSet>, metric: ProgressMetric, since: Long? = null): List<ChartPoint> =
    logged
        .groupBy { it.workoutId }
        .values
        .filter { since == null || it.first().finishedAt >= since }
        .mapNotNull { sets -> metric.valueFor(sets)?.let { ChartPoint(sets.first().finishedAt, it) } }
        .sortedBy { it.time }

data class PersonalRecord(val metric: ProgressMetric, val value: Double, val achievedAt: Long)

/** The best ever for each metric. A tie keeps the first time it was reached. */
fun personalRecords(logged: List<LoggedSet>): List<PersonalRecord> =
    availableMetrics(logged).mapNotNull { metric ->
        val points = progressPoints(logged, metric)
        val best = points.maxByOrNull { it.value } ?: return@mapNotNull null
        val first = points.first { it.value == best.value }
        PersonalRecord(metric, first.value, first.time)
    }

data class NewRecord(val exerciseId: String, val metric: ProgressMetric, val value: Double)

/**
 * The records a workout broke compared with everything before it. An exercise with no earlier
 * history doesn't count, because everything would be a record the first time.
 */
fun newRecordsIn(workoutId: Long, finishedAt: Long, logged: List<LoggedSet>): List<NewRecord> =
    logged
        .filter { it.workoutId == workoutId }
        .groupBy { it.exerciseId }
        .flatMap { (exerciseId, sets) ->
            val earlier = logged.filter {
                it.exerciseId == exerciseId && it.workoutId != workoutId && it.finishedAt < finishedAt
            }
            if (earlier.isEmpty()) return@flatMap emptyList()
            ProgressMetric.entries.mapNotNull { metric ->
                val value = metric.valueFor(sets) ?: return@mapNotNull null
                val best = progressPoints(earlier, metric).maxOfOrNull { it.value }
                if (best == null || value > best) NewRecord(exerciseId, metric, value) else null
            }
        }

/** Shows a metric's value in the right units, such as "62.5 kg" or "12 reps". */
fun ProgressMetric.format(value: Double, unit: WeightUnit): String = when (this) {
    ProgressMetric.HeaviestWeight, ProgressMetric.EstimatedMax -> "${formatWeight(value, unit)} ${unit.label}"
    ProgressMetric.BestSetVolume, ProgressMetric.WorkoutVolume -> formatVolume(value, unit)
    ProgressMetric.MostReps, ProgressMetric.TotalReps -> {
        val reps = value.roundToLong()
        if (reps == 1L) "1 rep" else "$reps reps"
    }
}

/** A value as it's drawn on a chart, in the unit it's shown in. */
fun ProgressMetric.chartValue(value: Double, unit: WeightUnit): Double =
    if (usesWeight) value / unit.kilogramsPerUnit else value

/** How far back a chart looks. A null number of days means everything. */
enum class ChartRange(val label: String, val days: Long?) {
    OneMonth("1M", 30),
    ThreeMonths("3M", 90),
    SixMonths("6M", 180),
    OneYear("1Y", 365),
    All("All", null),
}

fun ChartRange.since(now: Long): Long? = days?.let { now - it * MillisPerDay }

private const val MillisPerDay = 24 * 60 * 60 * 1000L

enum class WeeklyMetric(val label: String) {
    Volume("Volume"),
    Workouts("Workouts"),
    Sets("Sets"),
}

data class WeekTotal(val weekStart: LocalDate, val value: Double)

/** A total for each of the last [weeks] weeks, oldest first, including weeks with nothing in them. */
fun weeklyTotals(
    workouts: List<Workout>,
    metric: WeeklyMetric,
    today: LocalDate,
    weeks: Int,
    zone: ZoneId,
    locale: Locale,
): List<WeekTotal> {
    val firstDay = WeekFields.of(locale).firstDayOfWeek
    val thisWeek = today.with(TemporalAdjusters.previousOrSame(firstDay))
    val starts = (weeks - 1 downTo 0).map { thisWeek.minusWeeks(it.toLong()) }
    val byWeek = workouts.filter { it.finishedAt != null }.groupBy { workout ->
        Instant.ofEpochMilli(workout.finishedAt!!).atZone(zone).toLocalDate()
            .with(TemporalAdjusters.previousOrSame(firstDay))
    }
    return starts.map { start ->
        val inWeek = byWeek[start].orEmpty()
        WeekTotal(
            weekStart = start,
            value = when (metric) {
                WeeklyMetric.Volume -> inWeek.sumOf { it.volumeKg }
                WeeklyMetric.Workouts -> inWeek.size.toDouble()
                WeeklyMetric.Sets -> inWeek.sumOf { it.completedSets.size }.toDouble()
            },
        )
    }
}

fun WeeklyMetric.format(value: Double, unit: WeightUnit): String = when (this) {
    WeeklyMetric.Volume -> formatVolume(value, unit)
    WeeklyMetric.Workouts -> if (value == 1.0) "1 workout" else "${value.roundToLong()} workouts"
    WeeklyMetric.Sets -> if (value == 1.0) "1 set" else "${value.roundToLong()} sets"
}

fun WeeklyMetric.chartValue(value: Double, unit: WeightUnit): Double =
    if (this == WeeklyMetric.Volume) value / unit.kilogramsPerUnit else value

/** The gridlines for a chart's vertical axis, on round numbers that include all the values. */
data class AxisScale(val low: Double, val high: Double, val ticks: List<Double>)

fun axisScale(values: List<Double>, startAtZero: Boolean = false, tickCount: Int = 4): AxisScale {
    val top = values.maxOrNull() ?: 0.0
    val bottom = if (startAtZero) 0.0 else values.minOrNull() ?: 0.0
    if (top == bottom) {
        // Every value is the same, so give the line some room around it.
        val margin = if (top == 0.0) 1.0 else kotlin.math.abs(top) * 0.1
        return axisScale(listOf((top - margin).coerceAtLeast(0.0), top + margin), startAtZero, tickCount)
    }
    val rough = (top - bottom) / (tickCount - 1)
    val magnitude = 10.0.pow(floor(log10(rough)))
    val step = listOf(1.0, 2.0, 2.5, 5.0, 10.0).map { it * magnitude }.first { it >= rough }
    val low = floor(bottom / step) * step
    val high = ceil(top / step) * step
    val count = ((high - low) / step).roundToLong().toInt()
    return AxisScale(low, high, (0..count).map { low + it * step })
}

/** A short date for chart labels, such as "Nov 4". */
fun formatShortDate(date: LocalDate): String = date.format(ShortDateFormat)

fun formatShortDate(millis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
    formatShortDate(Instant.ofEpochMilli(millis).atZone(zone).toLocalDate())

private val ShortDateFormat: DateTimeFormatter
    get() = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
