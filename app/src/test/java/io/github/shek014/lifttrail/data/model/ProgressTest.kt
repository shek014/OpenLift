package io.github.shek014.lifttrail.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Locale

class ProgressTest {
    private fun set(workoutId: Long, weightKg: Double?, reps: Int, exerciseId: String = "bench") =
        LoggedSet(workoutId, finishedAt = workoutId * 1000, exerciseId, weightKg, reps)

    @Test
    fun `estimated max uses the weight alone for a single rep`() {
        assertEquals(100.0, estimatedMax(100.0, 1), 0.001)
        assertEquals(133.333, estimatedMax(100.0, 10), 0.001)
    }

    @Test
    fun `each metric reads the sets of a workout`() {
        val sets = listOf(set(1, 60.0, 10), set(1, 80.0, 3))

        assertEquals(80.0, ProgressMetric.HeaviestWeight.valueFor(sets)!!, 0.001)
        assertEquals(88.0, ProgressMetric.EstimatedMax.valueFor(sets)!!, 0.001)
        assertEquals(600.0, ProgressMetric.BestSetVolume.valueFor(sets)!!, 0.001)
        assertEquals(840.0, ProgressMetric.WorkoutVolume.valueFor(sets)!!, 0.001)
        assertEquals(10.0, ProgressMetric.MostReps.valueFor(sets)!!, 0.001)
        assertEquals(13.0, ProgressMetric.TotalReps.valueFor(sets)!!, 0.001)
    }

    @Test
    fun `bodyweight sets only offer rep metrics`() {
        val pullUps = listOf(set(1, null, 10))

        assertEquals(listOf(ProgressMetric.MostReps, ProgressMetric.TotalReps), availableMetrics(pullUps))
        assertEquals(null, ProgressMetric.HeaviestWeight.valueFor(pullUps))
    }

    @Test
    fun `no sets means no metrics`() {
        assertEquals(emptyList<ProgressMetric>(), availableMetrics(emptyList()))
    }

    @Test
    fun `points are one per workout, oldest first, within the range`() {
        val logged = listOf(set(3, 70.0, 5), set(2, 65.0, 5), set(2, 60.0, 5), set(1, 50.0, 5))

        val all = progressPoints(logged, ProgressMetric.HeaviestWeight)
        assertEquals(listOf(ChartPoint(1000, 50.0), ChartPoint(2000, 65.0), ChartPoint(3000, 70.0)), all)

        val recent = progressPoints(logged, ProgressMetric.HeaviestWeight, since = 2000)
        assertEquals(listOf(ChartPoint(2000, 65.0), ChartPoint(3000, 70.0)), recent)
    }

    @Test
    fun `records keep the first time the best was reached`() {
        val logged = listOf(set(3, 70.0, 5), set(2, 70.0, 5), set(1, 50.0, 12))

        val records = personalRecords(logged).associateBy { it.metric }

        assertEquals(70.0, records.getValue(ProgressMetric.HeaviestWeight).value, 0.001)
        assertEquals(2000L, records.getValue(ProgressMetric.HeaviestWeight).achievedAt)
        assertEquals(12.0, records.getValue(ProgressMetric.MostReps).value, 0.001)
        assertEquals(1000L, records.getValue(ProgressMetric.MostReps).achievedAt)
    }

    @Test
    fun `a first attempt at an exercise isn't a record`() {
        assertEquals(emptyList<NewRecord>(), newRecordsIn(1, 1000, listOf(set(1, 60.0, 10))))
    }

    @Test
    fun `beating an earlier workout is a record, matching one isn't`() {
        val logged = listOf(set(2, 70.0, 5), set(1, 60.0, 5))
        val metrics = newRecordsIn(2, 2000, logged).map { it.metric }

        assertTrue(ProgressMetric.HeaviestWeight in metrics)
        assertFalse(ProgressMetric.MostReps in metrics)
    }

    @Test
    fun `later workouts don't take away a workout's records`() {
        val logged = listOf(set(3, 90.0, 5), set(2, 70.0, 5), set(1, 60.0, 5))

        assertTrue(newRecordsIn(2, 2000, logged).any { it.metric == ProgressMetric.HeaviestWeight })
    }

    @Test
    fun `other exercises don't count as history`() {
        val logged = listOf(set(2, 70.0, 5, "bench"), set(1, 200.0, 5, "squat"))

        assertEquals(emptyList<NewRecord>(), newRecordsIn(2, 2000, logged))
    }

    @Test
    fun `metrics read in the chosen unit`() {
        assertEquals("62.5 kg", ProgressMetric.HeaviestWeight.format(62.5, WeightUnit.Kg))
        assertEquals("135 lb", ProgressMetric.HeaviestWeight.format(61.23496995, WeightUnit.Lb))
        assertEquals("1 rep", ProgressMetric.MostReps.format(1.0, WeightUnit.Lb))
        assertEquals("12 reps", ProgressMetric.TotalReps.format(12.0, WeightUnit.Kg))
        assertEquals(135.0, ProgressMetric.HeaviestWeight.chartValue(61.23496995, WeightUnit.Lb), 0.001)
        assertEquals(12.0, ProgressMetric.TotalReps.chartValue(12.0, WeightUnit.Lb), 0.001)
    }

    private fun workout(id: Long, finishedAt: Long, weightKg: Double, reps: Int) = Workout(
        id = id,
        startedAt = finishedAt,
        finishedAt = finishedAt,
        exercises = listOf(
            WorkoutExercise(
                id = id,
                exercise = Exercise("bench", "Bench", Equipment.Barbell, listOf(Muscle.MidChest)),
                sets = listOf(WorkoutSet(id, weightKg, reps, completed = true)),
            ),
        ),
    )

    @Test
    fun `weekly totals include empty weeks and start on the locale's first day`() {
        val zone = ZoneOffset.UTC
        val today = LocalDate.of(2026, 11, 4) // A Wednesday.
        fun at(date: LocalDate) = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val workouts = listOf(
            workout(1, at(LocalDate.of(2026, 11, 2)), 100.0, 5), // Monday this week.
            workout(2, at(LocalDate.of(2026, 11, 3)), 50.0, 10), // Tuesday this week.
            workout(3, at(LocalDate.of(2026, 10, 19)), 60.0, 10), // Two weeks earlier.
        )

        val totals = weeklyTotals(workouts, WeeklyMetric.Volume, today, weeks = 3, zone, Locale.UK)

        assertEquals(
            listOf(LocalDate.of(2026, 10, 19), LocalDate.of(2026, 10, 26), LocalDate.of(2026, 11, 2)),
            totals.map { it.weekStart },
        )
        assertEquals(listOf(600.0, 0.0, 1000.0), totals.map { it.value })
        assertEquals(
            listOf(1.0, 0.0, 2.0),
            weeklyTotals(workouts, WeeklyMetric.Workouts, today, weeks = 3, zone, Locale.UK).map { it.value },
        )
        assertEquals(
            listOf(1.0, 0.0, 2.0),
            weeklyTotals(workouts, WeeklyMetric.Sets, today, weeks = 3, zone, Locale.UK).map { it.value },
        )
    }

    @Test
    fun `axis lands on round numbers around the values`() {
        val scale = axisScale(listOf(62.0, 87.0))

        assertTrue(scale.low <= 62.0)
        assertTrue(scale.high >= 87.0)
        assertEquals(scale.low, scale.ticks.first(), 0.001)
        assertEquals(scale.high, scale.ticks.last(), 0.001)
        scale.ticks.forEach { assertEquals(0.0, it % 2.5, 0.001) }
    }

    @Test
    fun `axis can start at zero`() {
        assertEquals(0.0, axisScale(listOf(40.0, 100.0), startAtZero = true).low, 0.001)
    }

    @Test
    fun `axis copes with one value and with nothing`() {
        val same = axisScale(listOf(60.0, 60.0))
        assertTrue(same.low < 60.0 && same.high > 60.0)

        val zero = axisScale(listOf(0.0), startAtZero = true)
        assertTrue(zero.high > 0.0)
        assertEquals(0.0, zero.low, 0.001)

        assertTrue(axisScale(emptyList()).high > 0.0)
    }
}
