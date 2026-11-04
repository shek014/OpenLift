package io.github.shek014.openlift.data

import io.github.shek014.openlift.data.model.Equipment
import io.github.shek014.openlift.data.model.Muscle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExerciseCatalogTest {
    @Test
    fun `ids are unique`() {
        val duplicates = ExerciseCatalog.groupBy { it.id }.filterValues { it.size > 1 }.keys
        assertEquals(emptySet<String>(), duplicates)
    }

    @Test
    fun `names are unique`() {
        val duplicates = ExerciseCatalog.groupBy { it.name }.filterValues { it.size > 1 }.keys
        assertEquals(emptySet<String>(), duplicates)
    }

    @Test
    fun `every exercise has a primary muscle`() {
        val missing = ExerciseCatalog.filter { it.primaryMuscles.isEmpty() }.map { it.id }
        assertEquals(emptyList<String>(), missing)
    }

    // The database allows one row per exercise and muscle, so a repeat would crash on saving.
    @Test
    fun `no exercise lists the same muscle twice`() {
        val repeated = ExerciseCatalog.filter { exercise ->
            val muscles = exercise.primaryMuscles + exercise.secondaryMuscles
            muscles.size != muscles.toSet().size
        }.map { it.id }
        assertEquals(emptyList<String>(), repeated)
    }

    // Otherwise a filter chip in the app would show no results.
    @Test
    fun `every muscle is the primary muscle of some exercise`() {
        val covered = ExerciseCatalog.flatMap { it.primaryMuscles }.toSet()
        assertEquals(emptySet<Muscle>(), Muscle.entries.toSet() - covered)
    }

    @Test
    fun `every kind of equipment is used`() {
        val used = ExerciseCatalog.map { it.equipment }.toSet()
        assertEquals(emptySet<Equipment>(), Equipment.entries.toSet() - used)
    }

    @Test
    fun `ids use only lowercase letters, digits and underscores`() {
        // Ids are placed in navigation routes, so they can't contain special characters.
        val invalid = ExerciseCatalog.map { it.id }.filterNot { it.matches(Regex("[a-z0-9_]+")) }
        assertTrue("Invalid ids: $invalid", invalid.isEmpty())
    }
}
