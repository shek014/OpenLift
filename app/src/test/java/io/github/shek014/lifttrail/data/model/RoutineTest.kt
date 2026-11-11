package io.github.shek014.lifttrail.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RoutineTest {
    @Test
    fun `plans read clearly`() {
        assertEquals("3 × 8–12", ExercisePlan(3, 8, 12).format())
        assertEquals("3 × 8", ExercisePlan(3, 8, 8).format())
        assertEquals("3 sets", ExercisePlan(3).format())
        assertEquals("1 set", ExercisePlan(1).format())
    }

    @Test
    fun `plans need a usable number of sets`() {
        assertNull(parseExercisePlan("", "8", "12"))
        assertNull(parseExercisePlan("0", "8", "12"))
        assertNull(parseExercisePlan("21", "8", "12"))
        assertEquals(ExercisePlan(20, 8, 12), parseExercisePlan("20", "8", "12"))
    }

    @Test
    fun `reps are optional and forgiving`() {
        assertEquals(ExercisePlan(3), parseExercisePlan("3", "", ""))
        assertEquals(ExercisePlan(3, 8, 8), parseExercisePlan("3", "8", ""))
        assertEquals(ExercisePlan(3, 12, 12), parseExercisePlan("3", "", "12"))
        assertEquals(ExercisePlan(3, 8, 12), parseExercisePlan("3", "12", "8"))
    }

    @Test
    fun `hint falls back to the routine's target last`() {
        val blank = WorkoutSet(id = 0, weightKg = null, reps = null, completed = false)
        val done = WorkoutSet(id = 0, weightKg = 60.0, reps = 9, completed = true)
        val last = LoggedSet(1, 1000, "bench", 50.0, 10)

        assertEquals(SetHint(null, 8), setHint(listOf(blank), 0, emptyList(), targetReps = 8))
        assertEquals(SetHint(60.0, 9), setHint(listOf(done, blank), 1, emptyList(), targetReps = 8))
        assertEquals(SetHint(50.0, 10), setHint(listOf(blank), 0, listOf(last), targetReps = 8))
    }

    @Test
    fun `a routine counts its sets`() {
        val exercise = Exercise("bench", "Bench", Equipment.Barbell, listOf(Muscle.MidChest))
        val routine = Routine(
            id = 1,
            name = "Push",
            exercises = listOf(RoutineExercise(exercise, ExercisePlan(3)), RoutineExercise(exercise, ExercisePlan(4))),
        )
        assertEquals(7, routine.totalSets)
    }
}
