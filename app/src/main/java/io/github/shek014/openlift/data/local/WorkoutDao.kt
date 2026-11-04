package io.github.shek014.openlift.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Transaction
    @Query("SELECT * FROM workouts WHERE finishedAt IS NULL LIMIT 1")
    fun observeActiveWorkout(): Flow<WorkoutWithExercises?>

    @Transaction
    @Query("SELECT * FROM workouts WHERE id = :id")
    fun observeWorkout(id: Long): Flow<WorkoutWithExercises?>

    @Transaction
    @Query("SELECT * FROM workouts WHERE finishedAt IS NOT NULL ORDER BY finishedAt DESC")
    fun observeFinishedWorkouts(): Flow<List<WorkoutWithExercises>>

    /** Completed sets of these exercises from finished workouts, newest workout first. */
    @Query(
        """
        SELECT w.id AS workoutId, w.finishedAt AS finishedAt, e.exerciseId AS exerciseId,
            s.weightKg AS weightKg, s.reps AS reps
        FROM workout_sets s
        JOIN workout_exercises e ON s.workoutExerciseId = e.id
        JOIN workouts w ON e.workoutId = w.id
        WHERE w.finishedAt IS NOT NULL AND s.completed = 1 AND s.reps IS NOT NULL
            AND e.exerciseId IN (:exerciseIds)
        ORDER BY w.finishedAt DESC, e.position, s.position
        """,
    )
    fun observeLoggedSets(exerciseIds: List<String>): Flow<List<LoggedSetRow>>

    @Query(
        """
        SELECT DISTINCT e.exerciseId FROM workout_exercises e
        JOIN workouts w ON e.workoutId = w.id
        JOIN workout_sets s ON s.workoutExerciseId = e.id
        WHERE w.finishedAt >= :since AND s.completed = 1
        """,
    )
    fun observeExerciseIdsFinishedSince(since: Long): Flow<List<String>>

    /** The exercises from finished workouts, most recently done first. */
    @Query(
        """
        SELECT e.exerciseId FROM workout_exercises e
        JOIN workouts w ON e.workoutId = w.id
        JOIN workout_sets s ON s.workoutExerciseId = e.id
        WHERE w.finishedAt IS NOT NULL AND s.completed = 1
        GROUP BY e.exerciseId
        ORDER BY MAX(w.finishedAt) DESC
        LIMIT :limit
        """,
    )
    fun observeRecentExerciseIds(limit: Int): Flow<List<String>>

    /** The rest time last chosen for this exercise, if one ever was. */
    @Query(
        """
        SELECT restSeconds FROM workout_exercises
        WHERE exerciseId = :exerciseId AND restSeconds IS NOT NULL
        ORDER BY id DESC LIMIT 1
        """,
    )
    suspend fun lastRestSeconds(exerciseId: String): Int?

    @Query("UPDATE workout_exercises SET restSeconds = :restSeconds WHERE id = :workoutExerciseId")
    suspend fun updateRestSeconds(workoutExerciseId: Long, restSeconds: Int)

    @Query("SELECT id FROM workouts WHERE finishedAt IS NULL LIMIT 1")
    suspend fun activeWorkoutId(): Long?

    @Insert
    suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Insert
    suspend fun insertWorkoutExercise(workoutExercise: WorkoutExerciseEntity): Long

    @Insert
    suspend fun insertSet(set: WorkoutSetEntity): Long

    @Query("SELECT COALESCE(MAX(position) + 1, 0) FROM workout_exercises WHERE workoutId = :workoutId")
    suspend fun nextExercisePosition(workoutId: Long): Int

    @Query("SELECT COALESCE(MAX(position) + 1, 0) FROM workout_sets WHERE workoutExerciseId = :workoutExerciseId")
    suspend fun nextSetPosition(workoutExerciseId: Long): Int

    @Query("UPDATE workout_sets SET weightKg = :weightKg WHERE id = :setId")
    suspend fun updateWeight(setId: Long, weightKg: Double?)

    @Query("UPDATE workout_sets SET reps = :reps WHERE id = :setId")
    suspend fun updateReps(setId: Long, reps: Int?)

    @Query("UPDATE workout_sets SET weightKg = :weightKg, reps = :reps, completed = 1 WHERE id = :setId")
    suspend fun completeSet(setId: Long, weightKg: Double?, reps: Int)

    @Query("UPDATE workout_sets SET completed = 0 WHERE id = :setId")
    suspend fun uncompleteSet(setId: Long)

    @Query("DELETE FROM workout_sets WHERE id = :setId")
    suspend fun deleteSet(setId: Long)

    @Query("DELETE FROM workout_exercises WHERE id = :workoutExerciseId")
    suspend fun deleteWorkoutExercise(workoutExerciseId: Long)

    @Query("DELETE FROM workouts WHERE id = :workoutId")
    suspend fun deleteWorkout(workoutId: Long)

    @Query(
        """
        DELETE FROM workout_sets WHERE completed = 0 AND workoutExerciseId IN
            (SELECT id FROM workout_exercises WHERE workoutId = :workoutId)
        """,
    )
    suspend fun deleteUncompletedSets(workoutId: Long)

    @Query(
        """
        DELETE FROM workout_exercises WHERE workoutId = :workoutId AND id NOT IN
            (SELECT workoutExerciseId FROM workout_sets)
        """,
    )
    suspend fun deleteExercisesWithoutSets(workoutId: Long)

    @Query("SELECT COUNT(*) FROM workout_exercises WHERE workoutId = :workoutId")
    suspend fun exerciseCount(workoutId: Long): Int

    @Query("UPDATE workouts SET finishedAt = :finishedAt WHERE id = :workoutId")
    suspend fun markFinished(workoutId: Long, finishedAt: Long)

    @Query("UPDATE workout_exercises SET position = :position WHERE id = :workoutExerciseId")
    suspend fun updateExercisePosition(workoutExerciseId: Long, position: Int)

    @Transaction
    suspend fun reorderExercises(workoutExerciseIds: List<Long>) {
        workoutExerciseIds.forEachIndexed { position, id -> updateExercisePosition(id, position) }
    }

    @Transaction
    suspend fun addExercise(workoutId: Long, exerciseId: String) {
        val id = insertWorkoutExercise(
            WorkoutExerciseEntity(
                workoutId = workoutId,
                exerciseId = exerciseId,
                position = nextExercisePosition(workoutId),
                restSeconds = lastRestSeconds(exerciseId),
            ),
        )
        insertSet(WorkoutSetEntity(workoutExerciseId = id, position = 0))
    }

    @Transaction
    suspend fun addSet(workoutExerciseId: Long) {
        insertSet(
            WorkoutSetEntity(
                workoutExerciseId = workoutExerciseId,
                position = nextSetPosition(workoutExerciseId),
            ),
        )
    }

    /**
     * Keeps only what was completed. Returns false if that was nothing, in which case the whole
     * workout is deleted.
     */
    @Transaction
    suspend fun removeUncompleted(workoutId: Long): Boolean {
        deleteUncompletedSets(workoutId)
        deleteExercisesWithoutSets(workoutId)
        if (exerciseCount(workoutId) == 0) {
            deleteWorkout(workoutId)
            return false
        }
        return true
    }

    @Transaction
    suspend fun finishWorkout(workoutId: Long, finishedAt: Long): Boolean {
        if (!removeUncompleted(workoutId)) return false
        markFinished(workoutId, finishedAt)
        return true
    }
}
