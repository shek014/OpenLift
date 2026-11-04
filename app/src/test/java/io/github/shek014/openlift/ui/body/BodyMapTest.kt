package io.github.shek014.openlift.ui.body

import io.github.shek014.openlift.data.model.Equipment
import io.github.shek014.openlift.data.model.Exercise
import io.github.shek014.openlift.data.model.Muscle
import org.junit.Assert.assertEquals
import org.junit.Test

class BodyMapTest {
    @Test
    fun `every muscle is on the body map`() {
        val drawn = BodyMapRegions.mapNotNull { it.muscle }.toSet()
        assertEquals(emptySet<Muscle>(), Muscle.entries.toSet() - drawn)
    }

    @Test
    fun `exercises are sorted into main and assisting for a muscle`() {
        val bench = Exercise("bench", "Bench", Equipment.Barbell, listOf(Muscle.MidChest), listOf(Muscle.Triceps))
        val pushdown = Exercise("pushdown", "Pushdown", Equipment.Cable, listOf(Muscle.Triceps))
        val curl = Exercise("curl", "Curl", Equipment.Dumbbell, listOf(Muscle.Biceps))

        val triceps = listOf(bench, pushdown, curl).forMuscle(Muscle.Triceps)

        assertEquals(listOf(pushdown), triceps.main)
        assertEquals(listOf(bench), triceps.assisting)
    }
}
