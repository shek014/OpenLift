package io.github.shek014.openlift.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Locale

class WarmupTest {
    @Test
    fun `a barbell warm-up starts with the empty bar and builds up`() {
        assertEquals(
            listOf(WarmupSet(20.0, 10), WarmupSet(40.0, 5), WarmupSet(60.0, 3), WarmupSet(80.0, 2)),
            warmupSets(100.0, 2.5, barWeight = 20.0),
        )
    }

    @Test
    fun `weights round to what can be loaded`() {
        assertEquals(
            listOf(WarmupSet(45.0, 10), WarmupSet(55.0, 5), WarmupSet(80.0, 3), WarmupSet(110.0, 2)),
            warmupSets(135.0, 5.0, barWeight = 45.0),
        )
    }

    @Test
    fun `without a bar it skips straight to the percentages`() {
        assertEquals(
            listOf(WarmupSet(12.5, 5), WarmupSet(17.5, 3), WarmupSet(25.0, 2)),
            warmupSets(30.0, 2.5, barWeight = null),
        )
    }

    @Test
    fun `steps lighter than the bar are left out`() {
        assertEquals(listOf(WarmupSet(20.0, 10), WarmupSet(25.0, 2)), warmupSets(30.0, 2.5, barWeight = 20.0).filter { it.reps != 3 })
        warmupSets(30.0, 2.5, barWeight = 20.0).forEach { assertTrue(it.weight >= 20.0) }
    }

    @Test
    fun `lifting the empty bar or less needs no warm-up`() {
        assertEquals(emptyList<WarmupSet>(), warmupSets(20.0, 2.5, barWeight = 20.0))
        assertEquals(emptyList<WarmupSet>(), warmupSets(0.0, 2.5, barWeight = 20.0))
    }

    @Test
    fun `plates add up per side`() {
        assertEquals(listOf(25.0, 15.0), platesPerSide(100.0, 20.0, WeightUnit.Kg))
        assertEquals(listOf(20.0, 2.5), platesPerSide(65.0, 20.0, WeightUnit.Kg))
        assertEquals(listOf(45.0), platesPerSide(135.0, 45.0, WeightUnit.Lb))
        assertEquals(emptyList<Double>(), platesPerSide(20.0, 20.0, WeightUnit.Kg))
    }

    @Test
    fun `plates read clearly`() {
        assertEquals("Empty bar", formatPlates(emptyList()))
        assertEquals("20 + 2.5 + 1.25", formatPlates(listOf(20.0, 2.5, 1.25)))
    }
}

class MeasurementTest {
    private val metric = MeasurementUnits(WeightUnit.Kg, LengthUnit.Cm)
    private val imperial = MeasurementUnits(WeightUnit.Lb, LengthUnit.In)

    @Test
    fun `values read in the chosen units`() {
        assertEquals("82.5 kg", MeasurementType.BodyWeight.format(82.5, metric))
        assertEquals("180 lb", MeasurementType.BodyWeight.format(81.6466266, imperial))
        assertEquals("80 cm", MeasurementType.Waist.format(80.0, metric))
        assertEquals("32 in", MeasurementType.Waist.format(81.28, imperial))
        assertEquals("18.5%", MeasurementType.BodyFat.format(18.5, imperial))
    }

    @Test
    fun `typed values convert to how they're stored and back`() {
        val stored = MeasurementType.Chest.toStored(40.0, imperial)
        assertEquals(101.6, stored, 0.001)
        assertEquals(40.0, MeasurementType.Chest.toShown(stored, imperial), 0.001)
        assertEquals(18.0, MeasurementType.BodyFat.toStored(18.0, imperial), 0.001)
    }

    @Test
    fun `changes show their direction`() {
        assertEquals("+1.5 kg", MeasurementType.BodyWeight.formatChange(81.5, 80.0, metric))
        assertEquals("-2 cm", MeasurementType.Waist.formatChange(78.0, 80.0, metric))
        assertEquals("0 cm", MeasurementType.Waist.formatChange(80.0, 80.0, metric))
        assertEquals("-0.5%", MeasurementType.BodyFat.formatChange(18.0, 18.5, metric))
    }

    @Test
    fun `latest takes the newest by date, whatever order they were added`() {
        val old = Measurement(2, MeasurementType.Waist, 90.0, measuredAt = 1000)
        val new = Measurement(1, MeasurementType.Waist, 80.0, measuredAt = 2000)

        val latest = latestMeasurements(listOf(old, new)).getValue(MeasurementType.Waist)

        assertEquals(new, latest.latest)
        assertEquals(old, latest.previous)
        assertNull(latestMeasurements(listOf(new)).getValue(MeasurementType.Waist).previous)
    }

    @Test
    fun `picked dates stay on the same day in any time zone`() {
        val picked = dateToPickerMillis(LocalDate.of(2026, 11, 4))
        listOf("Asia/Kolkata", "America/Los_Angeles", "Pacific/Auckland", "UTC").forEach { id ->
            val zone = java.time.ZoneId.of(id)
            val millis = pickedDateToMillis(picked, zone)
            assertEquals(id, LocalDate.of(2026, 11, 4), java.time.Instant.ofEpochMilli(millis).atZone(zone).toLocalDate())
        }
    }
}

class CustomExerciseTest {
    @Test
    fun `a muscle can't be both primary and secondary`() {
        val draft = ExerciseDraft().togglePrimary(Muscle.Lats).toggleSecondary(Muscle.Lats)
        assertEquals(emptyList<Muscle>(), draft.primaryMuscles)
        assertEquals(listOf(Muscle.Lats), draft.secondaryMuscles)

        val back = draft.togglePrimary(Muscle.Lats)
        assertEquals(listOf(Muscle.Lats), back.primaryMuscles)
        assertEquals(emptyList<Muscle>(), back.secondaryMuscles)
    }

    @Test
    fun `tapping a selected muscle clears it`() {
        assertEquals(emptyList<Muscle>(), ExerciseDraft().togglePrimary(Muscle.Lats).togglePrimary(Muscle.Lats).primaryMuscles)
    }

    @Test
    fun `saving needs a unique name and a primary muscle`() {
        val valid = ExerciseDraft(name = "  Landmine Press ", primaryMuscles = listOf(Muscle.UpperChest))

        assertNull(valid.problem(listOf("Bench Press")))
        assertEquals("Enter a name.", ExerciseDraft(name = "  ").problem(emptyList()))
        assertEquals("You already have an exercise with this name.", valid.problem(listOf("landmine press")))
        assertEquals("Choose at least one primary muscle.", ExerciseDraft(name = "Press").problem(emptyList()))
    }

    @Test
    fun `notes become one step per line`() {
        assertEquals(listOf("Set up.", "Lift."), notesToSteps(" Set up.\n\n  Lift.  \n"))
        assertEquals(emptyList<String>(), notesToSteps(null))
    }
}

class WarmupInWorkoutTest {
    private val exercise = Exercise("bench", "Bench", Equipment.Barbell, listOf(Muscle.MidChest))

    @Test
    fun `warm-up sets don't count toward totals`() {
        val entry = WorkoutExercise(
            id = 1,
            exercise = exercise,
            sets = listOf(
                WorkoutSet(1, 40.0, 5, completed = true, setType = SetType.Warmup),
                WorkoutSet(2, 100.0, 5, completed = true),
            ),
        )
        val workout = Workout(1, 0, 1000, listOf(entry))

        assertEquals(1, workout.completedSets.size)
        assertEquals(500.0, workout.volumeKg, 0.001)
    }

    @Test
    fun `an exercise with only warm-ups done isn't counted as done`() {
        val entry = WorkoutExercise(1, exercise, listOf(WorkoutSet(1, 40.0, 5, completed = true, setType = SetType.Warmup)))

        assertEquals(emptyList<WorkoutExercise>(), Workout(1, 0, 1000, listOf(entry)).completedExercises)
    }
}

class StatisticsTest {
    private val zone = ZoneOffset.UTC
    private val today = LocalDate.of(2026, 11, 4) // A Wednesday.
    private val bench = Exercise("bench", "Bench", Equipment.Barbell, listOf(Muscle.MidChest), listOf(Muscle.Triceps))
    private val curl = Exercise("curl", "Curl", Equipment.Dumbbell, listOf(Muscle.Biceps))

    private fun workout(id: Long, date: LocalDate, minutes: Long, vararg entries: Pair<Exercise, List<Pair<Double?, Int>>>): Workout {
        val finishedAt = date.atTime(18, 0).toInstant(zone).toEpochMilli()
        return Workout(
            id = id,
            startedAt = finishedAt - minutes * 60_000,
            finishedAt = finishedAt,
            exercises = entries.mapIndexed { index, (exercise, sets) ->
                WorkoutExercise(index.toLong(), exercise, sets.map { (kg, reps) -> WorkoutSet(0, kg, reps, completed = true) })
            },
        )
    }

    private fun stats(workouts: List<Workout>, range: StatsRange = StatsRange.All) =
        workoutStatistics(workouts, range, today, zone, Locale.UK)

    @Test
    fun `records are counted over all time and within a range`() {
        val workouts = listOf(
            workout(1, today, 60, bench to listOf(120.0 to 5)),
            workout(2, today.minusDays(100), 60, bench to listOf(100.0 to 5)),
            workout(3, today.minusDays(200), 60, bench to listOf(80.0 to 5)),
        )

        assertEquals(8, stats(workouts).recordsSet)
        assertEquals(4, stats(workouts, StatsRange.NinetyDays).recordsSet)
    }

    @Test
    fun `totals and averages add up`() {
        val result = stats(
            listOf(
                workout(1, today, 60, bench to listOf(100.0 to 5, 100.0 to 5)),
                workout(2, today.minusDays(2), 30, curl to listOf(20.0 to 10)),
            ),
        )

        assertEquals(2, result.workouts)
        assertEquals(3, result.sets)
        assertEquals(20, result.reps)
        assertEquals(1200.0, result.volumeKg, 0.001)
        assertEquals(5400L, result.totalSeconds)
        assertEquals(2700L, result.averageSeconds)
        assertEquals(1.5, result.averageSets, 0.001)
        assertEquals(600.0, result.averageVolumeKg, 0.001)
    }

    @Test
    fun `nothing logged gives zeros rather than a crash`() {
        val result = stats(emptyList())

        assertEquals(0, result.workouts)
        assertEquals(0.0, result.workoutsPerWeek, 0.001)
        assertEquals(0L, result.averageSeconds)
        assertEquals(0, result.currentStreakWeeks)
        assertNull(result.volumeChangePercent)
    }

    @Test
    fun `the range leaves out older workouts and compares with the period before`() {
        val result = stats(
            listOf(
                workout(1, today, 60, bench to listOf(100.0 to 10)),
                workout(2, today.minusDays(40), 60, bench to listOf(100.0 to 5)),
                workout(3, today.minusDays(200), 60, bench to listOf(100.0 to 1)),
            ),
            StatsRange.ThirtyDays,
        )

        assertEquals(1, result.workouts)
        assertEquals(1000.0, result.volumeKg, 0.001)
        assertEquals(100.0, result.volumeChangePercent!!, 0.001)
        assertNull(stats(emptyList(), StatsRange.All).volumeChangePercent)
    }

    @Test
    fun `secondary muscles count as half a set`() {
        val result = stats(listOf(workout(1, today, 60, bench to listOf(100.0 to 5, 100.0 to 5))))

        assertEquals(2.0, result.setsByMuscle.getValue(Muscle.MidChest), 0.001)
        assertEquals(1.0, result.setsByMuscle.getValue(Muscle.Triceps), 0.001)
        assertEquals(2.0, result.setsByGroup.getValue(MuscleGroup.Chest), 0.001)
    }

    @Test
    fun `top exercises are by number of sets`() {
        val result = stats(
            listOf(
                workout(1, today, 60, curl to listOf(20.0 to 10), bench to listOf(100.0 to 5, 100.0 to 5)),
                workout(2, today.minusDays(1), 60, bench to listOf(100.0 to 5)),
            ),
        )

        assertEquals(listOf(ExerciseCount(bench, 3), ExerciseCount(curl, 1)), result.topExercises)
    }

    @Test
    fun `workouts are counted by day of the week`() {
        val result = stats(
            listOf(
                workout(1, today, 60, bench to listOf(100.0 to 5)),
                workout(2, today.minusWeeks(1), 60, bench to listOf(100.0 to 5)),
                workout(3, today.minusDays(1), 60, bench to listOf(100.0 to 5)),
            ),
        )

        assertEquals(2, result.byWeekday[DayOfWeek.WEDNESDAY])
        assertEquals(1, result.byWeekday[DayOfWeek.TUESDAY])
    }

    @Test
    fun `streaks count weeks in a row`() {
        val workouts = listOf(
            workout(1, today, 60, bench to listOf(100.0 to 5)),
            workout(2, today.minusWeeks(1), 60, bench to listOf(100.0 to 5)),
            workout(3, today.minusWeeks(5), 60, bench to listOf(100.0 to 5)),
            workout(4, today.minusWeeks(6), 60, bench to listOf(100.0 to 5)),
            workout(5, today.minusWeeks(7), 60, bench to listOf(100.0 to 5)),
        )

        assertEquals(2 to 3, workoutStreaks(workouts, today, zone, Locale.UK))
    }

    @Test
    fun `a streak survives until a whole week is missed`() {
        val lastWeek = listOf(workout(1, today.minusWeeks(1), 60, bench to listOf(100.0 to 5)))
        val twoWeeksAgo = listOf(workout(1, today.minusWeeks(2), 60, bench to listOf(100.0 to 5)))

        assertEquals(1 to 1, workoutStreaks(lastWeek, today, zone, Locale.UK))
        assertEquals(0 to 1, workoutStreaks(twoWeeksAgo, today, zone, Locale.UK))
    }

    @Test
    fun `records count improvements, not the first attempt`() {
        fun set(id: Long, kg: Double, reps: Int) = LoggedSet(id, id * 1000, "bench", kg, reps)

        assertEquals(0, countRecords(listOf(set(1, 100.0, 5))))
        // Every weight metric improves in the second workout. The rep metrics don't change.
        assertEquals(4, countRecords(listOf(set(1, 100.0, 5), set(2, 110.0, 5))))
        // Nothing improves in the third.
        assertEquals(4, countRecords(listOf(set(1, 100.0, 5), set(2, 110.0, 5), set(3, 110.0, 5))))
    }

    @Test
    fun `weekdays start on the locale's first day`() {
        assertEquals(DayOfWeek.MONDAY, orderedWeekdays(Locale.UK).first())
        assertEquals(DayOfWeek.SUNDAY, orderedWeekdays(Locale.US).first())
        assertEquals(7, orderedWeekdays(Locale.UK).size)
    }
}

class PeriodTotalsTest {
    private val zone = ZoneOffset.UTC
    private val today = LocalDate.of(2026, 11, 4)
    private val bench = Exercise("bench", "Bench", Equipment.Barbell, listOf(Muscle.MidChest))

    private fun workout(id: Long, date: LocalDate) = Workout(
        id = id,
        startedAt = date.atStartOfDay(zone).toInstant().toEpochMilli(),
        finishedAt = date.atStartOfDay(zone).toInstant().toEpochMilli(),
        exercises = listOf(WorkoutExercise(id, bench, listOf(WorkoutSet(id, 100.0, 5, completed = true)))),
    )

    @Test
    fun `all time stays weekly for a couple of years of history`() {
        val totals = allTimeTotals(listOf(workout(1, today.minusWeeks(30))), WeeklyMetric.Workouts, today, zone, Locale.UK)

        assertTrue(totals.none { it.monthly })
        assertEquals(31, totals.size)
        assertEquals(1.0, totals.first().value, 0.001)
    }

    @Test
    fun `all time goes monthly for a long history, losing nothing`() {
        val workouts = listOf(
            workout(1, LocalDate.of(2020, 1, 15)),
            workout(2, LocalDate.of(2020, 1, 20)),
            workout(3, LocalDate.of(2026, 11, 2)),
        )

        val totals = allTimeTotals(workouts, WeeklyMetric.Workouts, today, zone, Locale.UK)

        assertTrue(totals.all { it.monthly })
        assertEquals(LocalDate.of(2020, 1, 1), totals.first().start)
        assertEquals(LocalDate.of(2026, 11, 1), totals.last().start)
        assertEquals(2.0, totals.first().value, 0.001)
        assertEquals(3.0, totals.sumOf { it.value }, 0.001)
    }

    @Test
    fun `no workouts still gives something to draw`() {
        assertTrue(allTimeTotals(emptyList(), WeeklyMetric.Volume, today, zone, Locale.UK).isNotEmpty())
    }
}

class SetDetailsTest {
    @Test
    fun `labels combine the number and the type`() {
        assertEquals("1", setLabel(SetType.Normal, 1))
        assertEquals("W2", setLabel(SetType.Warmup, 2))
        assertEquals("3D", setLabel(SetType.Drop, 3))
        assertEquals("4F", setLabel(SetType.Failure, 4))
        assertEquals("5S", setLabel(SetType.Superset, 5))
    }

    @Test
    fun `durations read from seconds or minutes and seconds`() {
        assertEquals(90, parseDuration("90"))
        assertEquals(90, parseDuration("1:30"))
        assertEquals(60, parseDuration("1:"))
        assertNull(parseDuration(""))
        assertNull(parseDuration("0"))
        assertTrue(isValidDurationInput("1:3"))
        assertTrue(!isValidDurationInput("1:300"))
        assertTrue(!isValidDurationInput("abc"))
    }

    @Test
    fun `timed sets read as a time`() {
        assertEquals("1:30", formatSet(null, null, WeightUnit.Kg, durationSeconds = 90))
        assertEquals("20 kg 0:45", formatSet(20.0, null, WeightUnit.Kg, durationSeconds = 45))
    }

    @Test
    fun `timed sets count toward time metrics only`() {
        val sets = listOf(
            LoggedSet(1, 1000, "plank", null, 0, durationSeconds = 60),
            LoggedSet(1, 1000, "plank", null, 0, durationSeconds = 90),
        )

        assertEquals(listOf(ProgressMetric.LongestTime, ProgressMetric.TotalTime), availableMetrics(sets))
        assertEquals(90.0, ProgressMetric.LongestTime.valueFor(sets)!!, 0.001)
        assertEquals(150.0, ProgressMetric.TotalTime.valueFor(sets)!!, 0.001)
        assertEquals("1:30", ProgressMetric.LongestTime.format(90.0, WeightUnit.Kg))
    }

    private fun logged(weight: Double?, reps: Int, seconds: Int? = null) =
        LoggedSet(1, 1000, "bench", weight, reps, seconds)

    @Test
    fun `a set only counts as a record once there's history to beat`() {
        assertEquals(emptyList<SetRecord>(), setRecords(logged(100.0, 5), emptyList()))
    }

    @Test
    fun `a heavier set breaks weight records, and a matching one doesn't`() {
        val earlier = listOf(logged(100.0, 5))

        val metrics = setRecords(logged(110.0, 5), earlier).map { it.metric }
        assertTrue(ProgressMetric.HeaviestWeight in metrics)
        assertTrue(ProgressMetric.EstimatedMax in metrics)
        assertTrue(ProgressMetric.MostReps !in metrics)
        assertEquals(emptyList<SetRecord>(), setRecords(logged(100.0, 5), earlier))
    }

    @Test
    fun `a longer hold is a record`() {
        val records = setRecords(logged(null, 0, seconds = 90), listOf(logged(null, 0, seconds = 60)))

        assertEquals(listOf(SetRecord(ProgressMetric.LongestTime, 90.0)), records)
    }

    @Test
    fun `rep bests keep the heaviest for each rep count and when it was first done`() {
        val bests = repBests(
            listOf(
                LoggedSet(2, 2000, "bench", 100.0, 5),
                LoggedSet(1, 1000, "bench", 100.0, 5),
                LoggedSet(1, 1000, "bench", 90.0, 5),
                LoggedSet(3, 3000, "bench", 120.0, 1),
                LoggedSet(3, 3000, "bench", null, 20),
            ),
        )

        assertEquals(listOf(RepBest(1, 120.0, 3000), RepBest(5, 100.0, 1000)), bests)
    }

    @Test
    fun `statistics graphs follow what applies to the exercise`() {
        assertEquals(
            listOf(ProgressMetric.EstimatedMax, ProgressMetric.BestSetVolume, ProgressMetric.WorkoutVolume, ProgressMetric.TotalReps),
            statisticsMetrics(listOf(logged(100.0, 5))),
        )
        assertEquals(listOf(ProgressMetric.TotalReps), statisticsMetrics(listOf(logged(null, 10))))
        assertEquals(
            listOf(ProgressMetric.LongestTime, ProgressMetric.TotalTime),
            statisticsMetrics(listOf(logged(null, 0, seconds = 60))),
        )
    }

    @Test
    fun `BMI is weight over height squared`() {
        assertEquals(24.69, bodyMassIndex(80.0, 180.0)!!, 0.01)
        assertNull(bodyMassIndex(80.0, 0.0))
    }

    private val bench = Exercise("bench", "Bench Press", Equipment.Barbell, listOf(Muscle.MidChest))
    private val workout = Workout(
        id = 1,
        startedAt = 0,
        finishedAt = 3_600_000,
        name = "Push",
        exercises = listOf(
            WorkoutExercise(
                id = 1,
                exercise = bench,
                sets = listOf(
                    WorkoutSet(1, 40.0, 5, completed = true, setType = SetType.Warmup),
                    WorkoutSet(2, 100.0, 5, completed = true),
                    WorkoutSet(3, 80.0, 10, completed = true, setType = SetType.Drop),
                    WorkoutSet(4, 80.0, 8, completed = false),
                ),
            ),
        ),
    )

    @Test
    fun `shared workouts list the sets that count`() {
        val text = workout.toShareText(WeightUnit.Kg)

        assertTrue(text, text.startsWith("Push\n"))
        assertTrue(text, "60:00 · 2 sets · 1300 kg" in text || "1:00:00 · 2 sets · 1300 kg" in text)
        assertTrue(text, "1. 100 kg × 5\n" in text)
        assertTrue(text, "2. 80 kg × 10 (Drop set)" in text)
        assertTrue(text, "40 kg" !in text)
    }

    @Test
    fun `saving as a routine keeps the exercises, sets and rep range`() {
        assertEquals(listOf("bench" to ExercisePlan(2, 5, 10)), workout.toRoutinePlans())
    }
}
