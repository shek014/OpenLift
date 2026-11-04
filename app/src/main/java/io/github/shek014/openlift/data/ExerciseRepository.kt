package io.github.shek014.openlift.data

import io.github.shek014.openlift.data.local.ExerciseDao
import io.github.shek014.openlift.data.local.toEntity
import io.github.shek014.openlift.data.local.toExercise
import io.github.shek014.openlift.data.local.toMuscleEntities
import io.github.shek014.openlift.data.model.Exercise
import io.github.shek014.openlift.data.model.ExerciseDraft
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.UUID

/** The rest of the app gets its exercises from here and doesn't touch the database directly. */
class ExerciseRepository(
    private val dao: ExerciseDao,
    scope: CoroutineScope,
) {
    // Runs on every launch, so exercises added to the catalog in an app update reach existing users.
    private val catalogSaved = scope.async {
        dao.saveExercises(
            exercises = ExerciseCatalog.map { it.toEntity() },
            muscles = ExerciseCatalog.flatMap { it.toMuscleEntities() },
        )
    }

    val exercises: Flow<List<Exercise>> = flow {
        catalogSaved.await()
        emitAll(dao.observeAll().map { rows -> rows.map { it.toExercise() } })
    }

    /** The exercises to offer in lists, leaving out any custom ones that have been deleted. */
    val selectableExercises: Flow<List<Exercise>> = exercises.map { all -> all.filterNot { it.isArchived } }

    /** Adds a custom exercise, or updates it if [id] is set. There's no limit on how many there can be. */
    suspend fun saveCustomExercise(id: String?, draft: ExerciseDraft) {
        val exercise = Exercise(
            id = id ?: "custom_${UUID.randomUUID().toString().replace("-", "")}",
            name = draft.name.trim(),
            equipment = draft.equipment,
            primaryMuscles = draft.primaryMuscles,
            secondaryMuscles = draft.secondaryMuscles,
            isCustom = true,
            notes = draft.notes.trim().ifEmpty { null },
            isTimed = draft.isTimed,
        )
        catalogSaved.await()
        dao.saveExercises(listOf(exercise.toEntity()), exercise.toMuscleEntities())
    }

    /** Takes a custom exercise out of the lists. Workouts already logged with it stay as they are. */
    suspend fun deleteCustomExercise(id: String) {
        catalogSaved.await()
        dao.archive(id)
    }

    fun exercise(id: String): Flow<Exercise?> = flow {
        catalogSaved.await()
        emitAll(dao.observeById(id).map { it?.toExercise() })
    }
}
