package io.github.shek014.lifttrail.ui.body

import io.github.shek014.lifttrail.data.model.Equipment
import io.github.shek014.lifttrail.data.model.Exercise
import io.github.shek014.lifttrail.data.model.Muscle
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Locale

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

    @Test
    fun `a muscle that's primary in any exercise shows as primary`() {
        val bench = Exercise("bench", "Bench", Equipment.Barbell, listOf(Muscle.MidChest), listOf(Muscle.Triceps))
        val pushdown = Exercise("pushdown", "Pushdown", Equipment.Cable, listOf(Muscle.Triceps))

        assertEquals(
            mapOf(Muscle.MidChest to MuscleEmphasis.Primary, Muscle.Triceps to MuscleEmphasis.Primary),
            listOf(bench, pushdown).muscleEmphasis(),
        )
        assertEquals(MuscleEmphasis.Secondary, listOf(bench).muscleEmphasis()[Muscle.Triceps])
    }

    @Test
    fun `the week starts on the locale's first day`() {
        val zone = ZoneOffset.UTC
        val wednesday = LocalDate.of(2026, 11, 4)

        assertEquals(
            LocalDate.of(2026, 11, 2).atStartOfDay(zone).toInstant().toEpochMilli(),
            startOfWeekMillis(wednesday, zone, Locale.UK),
        )
        assertEquals(
            LocalDate.of(2026, 11, 1).atStartOfDay(zone).toInstant().toEpochMilli(),
            startOfWeekMillis(wednesday, zone, Locale.US),
        )
    }
}
