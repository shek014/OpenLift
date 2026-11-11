package io.github.shek014.lifttrail.data

import io.github.shek014.lifttrail.data.local.LoggedSetRow
import io.github.shek014.lifttrail.data.local.WorkoutDao
import io.github.shek014.lifttrail.data.local.WorkoutEntity
import io.github.shek014.lifttrail.data.local.WorkoutWithExercises
import io.github.shek014.lifttrail.data.model.Exercise
import io.github.shek014.lifttrail.data.model.LoggedSet
import io.github.shek014.lifttrail.data.model.SetType
import io.github.shek014.lifttrail.data.model.Workout
import io.github.shek014.lifttrail.data.model.WorkoutExercise
import io.github.shek014.lifttrail.data.model.WorkoutSet
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
            ids.mapNotNull { exercises[it] }.filterNot { it.isArchived }
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

    suspend fun setWorkoutRestSeconds(workoutId: Long, seconds: Int?) = dao.setWorkoutRestSeconds(workoutId, seconds)

    suspend fun updateRestSeconds(workoutExerciseId: Long, seconds: Int?) =
        dao.updateRestSeconds(workoutExerciseId, seconds)

    suspend fun removeExercise(workoutExerciseId: Long) = dao.deleteWorkoutExercise(workoutExerciseId)

    suspend fun replaceExercise(workoutExerciseId: Long, exerciseId: String) =
        dao.replaceExercise(workoutExerciseId, exerciseId)

    val exerciseNotes: Flow<Map<String, String>> = exerciseRepository.exerciseNotes

    suspend fun setExerciseNote(exerciseId: String, note: String) = exerciseRepository.setExerciseNote(exerciseId, note)

    suspend fun updateNotes(workoutExerciseId: Long, notes: String?) = dao.updateNotes(workoutExerciseId, notes)

    suspend fun updateRepTarget(workoutExerciseId: Long, repsMin: Int?, repsMax: Int?) =
        dao.updateRepTarget(workoutExerciseId, repsMin, repsMax)

    suspend fun setSuperset(workoutExerciseIds: List<Long>, supersetId: Long?) =
        dao.setSuperset(workoutExerciseIds, supersetId)

    suspend fun addSet(workoutExerciseId: Long) = dao.addSet(workoutExerciseId)

    suspend fun addWarmupSets(workoutExerciseId: Long, sets: List<Pair<Double, Int>>) =
        dao.addWarmupSets(workoutExerciseId, sets)

    suspend fun removeSet(setId: Long) = dao.deleteSet(setId)

    suspend fun updateWeight(setId: Long, weightKg: Double?) = dao.updateWeight(setId, weightKg)

    suspend fun updateReps(setId: Long, reps: Int?) = dao.updateReps(setId, reps)

    suspend fun completeSet(setId: Long, weightKg: Double?, reps: Int?, durationSeconds: Int?) =
        dao.completeSet(setId, weightKg, reps, durationSeconds)

    suspend fun updateDuration(setId: Long, seconds: Int?) = dao.updateDuration(setId, seconds)

    suspend fun updateSetType(setId: Long, type: SetType) = dao.updateSetType(setId, type)

    /** Starts a workout from a past one. Returns false if there's already one in progress. */
    suspend fun copyWorkout(id: Long): Boolean {
        val source = dao.workoutOnce(id) ?: return false
        return dao.copyWorkout(source, now())
    }

    suspend fun uncompleteSet(setId: Long) = dao.uncompleteSet(setId)

    suspend fun discardActiveWorkout() {
        dao.activeWorkoutId()?.let { dao.deleteWorkout(it) }
    }

    suspend fun deleteWorkout(id: Long) = dao.deleteWorkout(id)

    /** A blank name goes back to the automatic one. */
    suspend fun renameWorkout(id: Long, name: String) = dao.renameWorkout(id, name.trim().ifEmpty { null })

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
    name = workout.name,
    routineId = workout.routineId,
    restSeconds = workout.restSeconds,
    exercises = this.exercises
        .sortedBy { it.workoutExercise.position }
        .mapNotNull { row ->
            val exercise = exercises[row.workoutExercise.exerciseId] ?: return@mapNotNull null
            WorkoutExercise(
                id = row.workoutExercise.id,
                exercise = exercise,
                restSeconds = row.workoutExercise.restSeconds,
                targetRepsMin = row.workoutExercise.targetRepsMin,
                targetRepsMax = row.workoutExercise.targetRepsMax,
                supersetId = row.workoutExercise.supersetId,
                notes = row.workoutExercise.notes,
                sets = row.sets
                    .sortedBy { it.position }
                    .map { WorkoutSet(it.id, it.weightKg, it.reps, it.completed, it.setType, it.durationSeconds) },
            )
        },
)

private fun LoggedSetRow.toLoggedSet() =
    LoggedSet(workoutId, finishedAt, exerciseId, weightKg, reps ?: 0, durationSeconds, setType)
