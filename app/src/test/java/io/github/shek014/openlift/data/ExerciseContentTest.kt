package io.github.shek014.openlift.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ExerciseContentTest {
    private val catalogIds = ExerciseCatalog.map { it.id }.toSet()

    @Test
    fun `every exercise has how to steps`() {
        val missing = catalogIds.filter { ExerciseInstructions[it].isNullOrEmpty() }
        assertEquals(emptyList<String>(), missing)
    }

    @Test
    fun `how to steps only exist for exercises in the catalog`() {
        assertEquals(emptySet<String>(), ExerciseInstructions.keys - catalogIds)
    }

    // A typo in a file name would silently leave an exercise with no animation.
    @Test
    fun `every animation belongs to an exercise in the catalog`() {
        val gifs = File("src/main/assets/exercise_media").listFiles { file -> file.extension == "gif" }.orEmpty()
        assertTrue("No animations found", gifs.isNotEmpty())
        assertEquals(emptySet<String>(), gifs.map { it.nameWithoutExtension }.toSet() - catalogIds)
    }

    @Test
    fun `borrowed animations exist`() {
        val gifs = File("src/main/assets/exercise_media").listFiles { file -> file.extension == "gif" }.orEmpty()
            .map { it.nameWithoutExtension }
            .toSet()
        val missing = ExerciseVariations.mapNotNull { it.drawing }.toSet() - gifs
        assertEquals(emptySet<String>(), missing)
    }
}
