package io.github.shek014.openlift.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
    @Transaction
    @Query("SELECT * FROM routines ORDER BY position, createdAt, id")
    fun observeRoutines(): Flow<List<RoutineWithExercises>>

    @Transaction
    @Query("SELECT * FROM routines WHERE id = :id")
    fun observeRoutine(id: Long): Flow<RoutineWithExercises?>

    @Transaction
    @Query("SELECT * FROM routines WHERE id = :id")
    suspend fun routine(id: Long): RoutineWithExercises?

    @Insert
    suspend fun insertRoutine(routine: RoutineEntity): Long

    @Insert
    suspend fun insertExercises(exercises: List<RoutineExerciseEntity>)

    @Query("UPDATE routines SET name = :name WHERE id = :id")
    suspend fun rename(id: Long, name: String)

    @Query("SELECT COALESCE(MAX(position) + 1, 0) FROM routines")
    suspend fun nextPosition(): Int

    @Query("UPDATE routines SET position = :position WHERE id = :id")
    suspend fun updatePosition(id: Long, position: Int)

    /** Puts the routines in the order of these ids. */
    @Transaction
    suspend fun reorder(ids: List<Long>) {
        ids.forEachIndexed { position, id -> updatePosition(id, position) }
    }

    @Query("DELETE FROM routine_exercises WHERE routineId = :routineId")
    suspend fun deleteExercises(routineId: Long)

    @Query("DELETE FROM routines WHERE id = :id")
    suspend fun deleteRoutine(id: Long)

    @Transaction
    suspend fun createRoutine(routine: RoutineEntity, exercises: List<RoutineExerciseEntity>) {
        val id = insertRoutine(routine.copy(position = nextPosition()))
        insertExercises(exercises.map { it.copy(routineId = id) })
    }

    @Transaction
    suspend fun updateRoutine(id: Long, name: String, exercises: List<RoutineExerciseEntity>) {
        rename(id, name)
        deleteExercises(id)
        insertExercises(exercises.map { it.copy(routineId = id) })
    }

    /** Adds several routines together, so that a split is either added whole or not at all. */
    @Transaction
    suspend fun createRoutines(routines: List<Pair<RoutineEntity, List<RoutineExerciseEntity>>>) {
        routines.forEach { (routine, exercises) -> createRoutine(routine, exercises) }
    }
}
