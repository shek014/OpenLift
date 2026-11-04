package io.github.shek014.openlift.data

import io.github.shek014.openlift.data.local.LoggedSetRow
import io.github.shek014.openlift.data.local.WorkoutDao
import io.github.shek014.openlift.data.local.WorkoutEntity
import io.github.shek014.openlift.data.local.WorkoutWithExercises
import io.github.shek014.openlift.data.model.Exercise
import io.github.shek014.openlift.data.model.LoggedSet
import io.github.shek014.openlift.data.model.Workout
import io.github.shek014.openlift.data.model.WorkoutExercise
import io.github.shek014.openlift.data.model.WorkoutSet
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class WorkoutRepository(
    private val dao: WorkoutDao,
    private val exerciseRepository: ExerciseRepository,
    private val now: () -> Long = System::currentTimeMillis,
) {
    private val exercisesById: Flow<Map<String, Exercise>> =
        exerciseRepository.exercises.map { list -> list.associateBy { it.id } }

    /** The workout in progress, or null if there isn't one. */
    val activeWorkout: Flow<Workout?> =
        combine(dao.observeActiveWorkout(), exercisesById) { row, exercises -> row?.toWorkout(exercises) }

    fun workout(id: Long): Flow<Workout?> =
        combine(dao.observeWorkout(id), exercisesById) { row, exercises -> row?.toWorkout(exercises) }

    /** Finished workouts, newest first. */
    val finishedWorkouts: Flow<List<Workout>> =
        combine(dao.observeFinishedWorkouts(), exercisesById) { rows, exercises ->
            rows.map { it.toWorkout(exercises) }
        }

    /** Completed sets of these exercises from finished workouts, newest workout first. */
    fun loggedSets(exerciseIds: List<String>): Flow<List<LoggedSet>> =
        dao.observeLoggedSets(exerciseIds).map { rows -> rows.map { it.toLoggedSet() } }

    /** The same as [loggedSets], for whichever exercises are in the workout in progress. */
    val loggedSetsForActiveWorkout: Flow<List<LoggedSet>> = loggedSetsFor(activeWorkout)

    /** The same as [loggedSets], for whichever exercises are in this workout. */
    fun loggedSetsInWorkout(id: Long): Flow<List<LoggedSet>> = loggedSetsFor(workout(id))

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loggedSetsFor(workout: Flow<Workout?>): Flow<List<LoggedSet>> =
        workout
            .map { current -> current?.exercises?.map { it.exercise.id }?.distinct().orEmpty() }
            .distinctUntilChanged()
            .flatMapLatest { loggedSets(it) }

    /** What someone's likely to want to add next: the exercises they've done most recently. */
    val recentExercises: Flow<List<Exercise>> =
        combine(dao.observeRecentExerciseIds(RecentExerciseLimit), exercisesById) { ids, exercises ->
            ids.mapNotNull { exercises[it] }
        }

    fun exercisesFinishedSince(since: Long): Flow<List<Exercise>> =
        combine(dao.observeExerciseIdsFinishedSince(since), exercisesById) { ids, exercises ->
            ids.mapNotNull { exercises[it] }
        }

    suspend fun startWorkout() {
        if (dao.activeWorkoutId() == null) dao.insertWorkout(WorkoutEntity(startedAt = now()))
    }

    suspend fun addExercise(exerciseId: String) {
        val workoutId = dao.activeWorkoutId() ?: dao.insertWorkout(WorkoutEntity(startedAt = now()))
        dao.addExercise(workoutId, exerciseId)
    }

    suspend fun addExerciseTo(workoutId: Long, exerciseId: String) = dao.addExercise(workoutId, exerciseId)

    suspend fun reorderExercises(workoutExerciseIds: List<Long>) = dao.reorderExercises(workoutExerciseIds)

    suspend fun updateRestSeconds(workoutExerciseId: Long, seconds: Int) =
        dao.updateRestSeconds(workoutExerciseId, seconds)

    suspend fun removeExercise(workoutExerciseId: Long) = dao.deleteWorkoutExercise(workoutExerciseId)

    suspend fun addSet(workoutExerciseId: Long) = dao.addSet(workoutExerciseId)

    suspend fun removeSet(setId: Long) = dao.deleteSet(setId)

    suspend fun updateWeight(setId: Long, weightKg: Double?) = dao.updateWeight(setId, weightKg)

    suspend fun updateReps(setId: Long, reps: Int?) = dao.updateReps(setId, reps)

    suspend fun completeSet(setId: Long, weightKg: Double?, reps: Int) = dao.completeSet(setId, weightKg, reps)

    suspend fun uncompleteSet(setId: Long) = dao.uncompleteSet(setId)

    suspend fun discardActiveWorkout() {
        dao.activeWorkoutId()?.let { dao.deleteWorkout(it) }
    }

    suspend fun deleteWorkout(id: Long) = dao.deleteWorkout(id)

    /** Tidies up a saved workout after editing. Returns false if that left nothing in it. */
    suspend fun finishEditing(id: Long): Boolean = dao.removeUncompleted(id)

    /** Returns the saved workout's id, or null if nothing had been completed so nothing was saved. */
    suspend fun finishActiveWorkout(): Long? {
        val id = dao.activeWorkoutId() ?: return null
        return if (dao.finishWorkout(id, now())) id else null
    }
}

private const val RecentExerciseLimit = 6

private fun WorkoutWithExercises.toWorkout(exercises: Map<String, Exercise>) = Workout(
    id = workout.id,
    startedAt = workout.startedAt,
    finishedAt = workout.finishedAt,
    exercises = this.exercises
        .sortedBy { it.workoutExercise.position }
        .mapNotNull { row ->
            val exercise = exercises[row.workoutExercise.exerciseId] ?: return@mapNotNull null
            WorkoutExercise(
                id = row.workoutExercise.id,
                exercise = exercise,
                restSeconds = row.workoutExercise.restSeconds,
                sets = row.sets
                    .sortedBy { it.position }
                    .map { WorkoutSet(it.id, it.weightKg, it.reps, it.completed) },
            )
        },
)

private fun LoggedSetRow.toLoggedSet() = LoggedSet(workoutId, finishedAt, exerciseId, weightKg, reps)
