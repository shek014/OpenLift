package io.github.shek014.openlift.data

import io.github.shek014.openlift.data.local.RoutineDao
import io.github.shek014.openlift.data.local.RoutineEntity
import io.github.shek014.openlift.data.local.RoutineExerciseEntity
import io.github.shek014.openlift.data.local.RoutineWithExercises
import io.github.shek014.openlift.data.local.WorkoutDao
import io.github.shek014.openlift.data.model.Exercise
import io.github.shek014.openlift.data.model.ExercisePlan
import io.github.shek014.openlift.data.model.Routine
import io.github.shek014.openlift.data.model.RoutineExercise
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class RoutineRepository(
    private val routineDao: RoutineDao,
    private val workoutDao: WorkoutDao,
    private val exerciseRepository: ExerciseRepository,
    private val now: () -> Long = System::currentTimeMillis,
) {
    private val exercisesById: Flow<Map<String, Exercise>> =
        exerciseRepository.exercises.map { list -> list.associateBy { it.id } }

    val routines: Flow<List<Routine>> =
        combine(routineDao.observeRoutines(), exercisesById) { rows, exercises ->
            rows.map { it.toRoutine(exercises) }
        }

    fun routine(id: Long): Flow<Routine?> =
        combine(routineDao.observeRoutine(id), exercisesById) { row, exercises -> row?.toRoutine(exercises) }

    /** Exercises for a split's screen, found by id. */
    val exercises: Flow<Map<String, Exercise>> = exercisesById

    suspend fun createRoutine(name: String, exercises: List<Pair<String, ExercisePlan>>) {
        routineDao.createRoutine(RoutineEntity(name = name, createdAt = now()), exercises.toEntities())
    }

    suspend fun updateRoutine(id: Long, name: String, exercises: List<Pair<String, ExercisePlan>>) {
        routineDao.updateRoutine(id, name, exercises.toEntities())
    }

    suspend fun deleteRoutine(id: Long) = routineDao.deleteRoutine(id)

    /** Copies each day of a split into its own routine, which can then be changed freely. */
    suspend fun addSplit(split: Split) {
        val createdAt = now()
        routineDao.createRoutines(
            split.days.map { day ->
                RoutineEntity(name = "${split.shortName}: ${day.name}", createdAt = createdAt) to
                    day.exercises.map { it.exerciseId to it.plan }.toEntities()
            },
        )
    }

    /** Starts a workout from a routine. Returns false if there's already a workout in progress. */
    suspend fun startWorkout(routineId: Long): Boolean {
        // Makes sure the built-in exercises are saved, since workouts refer to them.
        exerciseRepository.exercises.first()
        val row = routineDao.routine(routineId) ?: return false
        return workoutDao.startWorkoutFromRoutine(
            routineId = routineId,
            name = row.routine.name,
            startedAt = now(),
            exercises = row.exercises.sortedBy { it.position },
        )
    }
}

private fun List<Pair<String, ExercisePlan>>.toEntities() = mapIndexed { index, (exerciseId, plan) ->
    RoutineExerciseEntity(
        routineId = 0,
        exerciseId = exerciseId,
        position = index,
        sets = plan.sets,
        repsMin = plan.repsMin,
        repsMax = plan.repsMax,
    )
}

private fun RoutineWithExercises.toRoutine(exercises: Map<String, Exercise>) = Routine(
    id = routine.id,
    name = routine.name,
    exercises = this.exercises
        .sortedBy { it.position }
        .mapNotNull { row ->
            exercises[row.exerciseId]?.let { RoutineExercise(it, ExercisePlan(row.sets, row.repsMin, row.repsMax)) }
        },
)
