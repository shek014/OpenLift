package io.github.shek014.openlift.ui.exercises

import io.github.shek014.openlift.data.model.Equipment
import io.github.shek014.openlift.data.model.Exercise
import io.github.shek014.openlift.data.model.Muscle
import io.github.shek014.openlift.data.model.MuscleGroup
import org.junit.Assert.assertEquals
import org.junit.Test

class ExerciseFilterTest {
    private val bench = Exercise(
        id = "bench",
        name = "Barbell Bench Press",
        equipment = Equipment.Barbell,
        primaryMuscles = listOf(Muscle.MidChest),
        secondaryMuscles = listOf(Muscle.Triceps),
    )
    private val pullUp = Exercise(
        id = "pull_up",
        name = "Pull-Up",
        equipment = Equipment.Bodyweight,
        primaryMuscles = listOf(Muscle.Lats),
        secondaryMuscles = listOf(Muscle.Biceps),
    )
    private val curl = Exercise(
        id = "curl",
        name = "Dumbbell Curl",
        equipment = Equipment.Dumbbell,
        primaryMuscles = listOf(Muscle.Biceps),
    )
    private val all = listOf(bench, pullUp, curl)

    private fun ids(filter: ExerciseFilter) = all.filteredBy(filter).map { it.id }

    @Test
    fun `an empty filter keeps everything`() {
        assertEquals(listOf("bench", "pull_up", "curl"), ids(ExerciseFilter()))
    }

    @Test
    fun `search ignores case and word order`() {
        assertEquals(listOf("bench"), ids(ExerciseFilter(query = "PRESS bench")))
    }

    @Test
    fun `search ignores extra spaces`() {
        assertEquals(listOf("curl"), ids(ExerciseFilter(query = "  dumbbell   curl ")))
    }

    @Test
    fun `search finds hyphenated names with or without the hyphen`() {
        assertEquals(listOf("pull_up"), ids(ExerciseFilter(query = "pull-up")))
        assertEquals(listOf("pull_up"), ids(ExerciseFilter(query = "pull up")))
        assertEquals(listOf("pull_up"), ids(ExerciseFilter(query = "pullup")))
    }

    @Test
    fun `search also looks at secondary muscles`() {
        assertEquals(listOf("pull_up", "curl"), ids(ExerciseFilter(query = "biceps")))
    }

    @Test
    fun `muscle group filter only looks at primary muscles`() {
        assertEquals(listOf("curl"), ids(ExerciseFilter(group = MuscleGroup.Arms)))
    }

    @Test
    fun `muscle filter only looks at primary muscles`() {
        assertEquals(emptyList<String>(), ids(ExerciseFilter(muscle = Muscle.Triceps)))
        assertEquals(listOf("bench"), ids(ExerciseFilter(muscle = Muscle.MidChest)))
    }

    @Test
    fun `specific muscle wins over its group`() {
        assertEquals(
            emptyList<String>(),
            ids(ExerciseFilter(group = MuscleGroup.Arms, muscle = Muscle.Triceps)),
        )
    }

    @Test
    fun `equipment filter matches exactly`() {
        assertEquals(listOf("curl"), ids(ExerciseFilter(equipment = Equipment.Dumbbell)))
    }

    @Test
    fun `all filters have to match together`() {
        assertEquals(
            emptyList<String>(),
            ids(ExerciseFilter(query = "curl", equipment = Equipment.Barbell)),
        )
    }
}
