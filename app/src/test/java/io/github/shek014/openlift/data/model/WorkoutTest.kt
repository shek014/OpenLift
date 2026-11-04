package io.github.shek014.openlift.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutTest {
    private fun logged(workoutId: Long, exerciseId: String, weightKg: Double?, reps: Int) =
        LoggedSet(workoutId, finishedAt = workoutId * 1000, exerciseId, weightKg, reps)

    private fun set(weightKg: Double? = null, reps: Int? = null, completed: Boolean = false) =
        WorkoutSet(id = 0, weightKg, reps, completed)

    @Test
    fun `previous sets come only from the latest workout with that exercise`() {
        val newest = logged(3, "bench", 60.0, 8)
        val older = logged(1, "bench", 50.0, 8)
        val squatInOlderWorkout = logged(1, "squat", 100.0, 5)

        val previous = previousSetsByExercise(listOf(newest, older, squatInOlderWorkout))

        assertEquals(listOf(newest), previous["bench"])
        assertEquals(listOf(squatInOlderWorkout), previous["squat"])
    }

    @Test
    fun `hint prefers the same set from last time`() {
        val previous = listOf(logged(1, "bench", 60.0, 8), logged(1, "bench", 65.0, 6))
        val sets = listOf(set(70.0, 5), set())

        assertEquals(SetHint(65.0, 6), setHint(sets, 1, previous))
    }

    @Test
    fun `hint falls back to the set above`() {
        val sets = listOf(set(70.0, 5), set())

        assertEquals(SetHint(70.0, 5), setHint(sets, 1, emptyList()))
    }

    @Test
    fun `first set of a new exercise has no hint`() {
        assertNull(setHint(listOf(set()), 0, emptyList()))
    }

    @Test
    fun `volume only counts completed sets`() {
        val entry = WorkoutExercise(
            id = 1,
            exercise = Exercise("bench", "Bench", Equipment.Barbell, listOf(Muscle.MidChest)),
            sets = listOf(set(60.0, 10, completed = true), set(100.0, 10, completed = false), set(null, 10, completed = true)),
        )
        val workout = Workout(id = 1, startedAt = 0, finishedAt = null, exercises = listOf(entry))

        assertEquals(600.0, workout.volumeKg, 0.001)
        assertEquals(2, workout.completedSets.size)
    }

    @Test
    fun `weight input allows typing decimals with a point or a comma`() {
        listOf("", "2", "22", "22.", "22.5", "22,5", "22.25", ".5", "1000").forEach {
            assertTrue(it, isValidWeightInput(it))
        }
        listOf("22.555", "10000", "1.2.3", "abc", "-5").forEach {
            assertFalse(it, isValidWeightInput(it))
        }
    }

    @Test
    fun `reps input only allows up to three digits`() {
        assertTrue(isValidRepsInput("12"))
        assertTrue(isValidRepsInput(""))
        assertFalse(isValidRepsInput("1000"))
        assertFalse(isValidRepsInput("1.5"))
    }

    @Test
    fun `parsing treats blank and partial input sensibly`() {
        assertEquals(22.5, parseWeight("22,5")!!, 0.001)
        assertEquals(22.0, parseWeight("22.")!!, 0.001)
        assertNull(parseWeight(""))
        assertNull(parseWeight("."))
        assertEquals(10, parseReps("10"))
        assertNull(parseReps("0"))
        assertNull(parseReps(""))
    }

    @Test
    fun `weights drop needless decimals`() {
        assertEquals("20", formatWeight(20.0))
        assertEquals("22.5", formatWeight(22.5))
        assertEquals("22.25", formatWeight(22.25))
    }

    @Test
    fun `sets read clearly with and without weight`() {
        assertEquals("20 kg × 10", formatSet(20.0, 10))
        assertEquals("× 10", formatSet(null, 10))
    }

    @Test
    fun `pounds convert both ways and read naturally`() {
        val kg = parseWeight("135", WeightUnit.Lb)!!
        assertEquals(61.235, kg, 0.001)
        assertEquals("135", formatWeight(kg, WeightUnit.Lb))
        assertEquals("137.79", formatWeight(62.5, WeightUnit.Lb))
        assertEquals("135 lb × 5", formatSet(kg, 5, WeightUnit.Lb))
        assertEquals("1000 lb", formatVolume(453.59237, WeightUnit.Lb))
    }

    @Test
    fun `unchanged weights compare the same despite rounding`() {
        assertTrue(sameWeight(parseWeight("137.79", WeightUnit.Lb), 62.5))
        assertFalse(sameWeight(62.5, 65.0))
        assertTrue(sameWeight(null, null))
        assertFalse(sameWeight(null, 60.0))
    }

    @Test
    fun `moving an item shifts the ones between`() {
        val list = listOf("a", "b", "c", "d")

        assertEquals(listOf("b", "c", "a", "d"), list.moved(0, 2))
        assertEquals(listOf("d", "a", "b", "c"), list.moved(3, 0))
        assertEquals(list, list.moved(1, 1))
        assertEquals(list, list.moved(0, 9))
        assertEquals(list, list.moved(-1, 0))
    }

    @Test
    fun `duration comes from the start and finish times`() {
        assertEquals(95L, Workout(1, startedAt = 5_000, finishedAt = 100_000, exercises = emptyList()).durationSeconds)
        assertEquals(0L, Workout(1, startedAt = 5_000, finishedAt = null, exercises = emptyList()).durationSeconds)
    }

    @Test
    fun `rest times read as a duration or off`() {
        assertEquals("Off", formatRest(0))
        assertEquals("0:45", formatRest(45))
        assertEquals("1:30", formatRest(90))
    }

    @Test
    fun `durations are shown as minutes or hours`() {
        assertEquals("0:05", formatDuration(5))
        assertEquals("1:35", formatDuration(95))
        assertEquals("1:01:35", formatDuration(3695))
    }
}
