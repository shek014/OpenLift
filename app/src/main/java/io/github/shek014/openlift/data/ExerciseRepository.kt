package io.github.shek014.openlift.data

import io.github.shek014.openlift.data.local.ExerciseDao
import io.github.shek014.openlift.data.local.toEntity
import io.github.shek014.openlift.data.local.toExercise
import io.github.shek014.openlift.data.local.toMuscleEntities
import io.github.shek014.openlift.data.model.Exercise
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

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

    fun exercise(id: String): Flow<Exercise?> = flow {
        catalogSaved.await()
        emitAll(dao.observeById(id).map { it?.toExercise() })
    }
}
