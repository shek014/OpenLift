package io.github.shek014.openlift.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecommendedSplitsTest {
    private val catalogIds = ExerciseCatalog.map { it.id }.toSet()

    @Test
    fun `split ids are unique`() {
        assertEquals(RecommendedSplits.size, RecommendedSplits.map { it.id }.toSet().size)
    }

    // A wrong id would quietly leave an exercise out of someone's routine.
    @Test
    fun `every exercise in a split is in the catalog`() {
        val unknown = RecommendedSplits.flatMap { split -> split.days.flatMap { day -> day.exercises } }
            .map { it.exerciseId }
            .filterNot { it in catalogIds }
        assertEquals(emptyList<String>(), unknown)
    }

    @Test
    fun `no day repeats an exercise or is empty`() {
        RecommendedSplits.forEach { split ->
            assertEquals(split.name, split.days.size, split.days.map { it.name }.toSet().size)
            split.days.forEach { day ->
                val ids = day.exercises.map { it.exerciseId }
                assertTrue("${split.name} ${day.name} is empty", ids.isNotEmpty())
                assertEquals("${split.name} ${day.name}", ids.size, ids.toSet().size)
            }
        }
    }

    @Test
    fun `plans in splits make sense`() {
        RecommendedSplits.flatMap { split -> split.days.flatMap { day -> day.exercises } }.forEach {
            assertTrue(it.exerciseId, it.plan.sets in 1..6)
            assertTrue(it.exerciseId, it.plan.repsMin!! <= it.plan.repsMax!!)
        }
    }
}
