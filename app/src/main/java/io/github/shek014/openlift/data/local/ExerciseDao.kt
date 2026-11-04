package io.github.shek014.openlift.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/** The database queries for exercises. Room generates the code behind this interface. */
@Dao
interface ExerciseDao {
    /** Emits the full list now, then again whenever the tables change. */
    @Transaction
    @Query("SELECT * FROM exercises ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<ExerciseWithMuscles>>

    @Transaction
    @Query("SELECT * FROM exercises WHERE id = :id")
    fun observeById(id: String): Flow<ExerciseWithMuscles?>

    @Query("UPDATE exercises SET isArchived = 1 WHERE id = :id AND isCustom = 1")
    suspend fun archive(id: String)

    @Upsert
    suspend fun upsertExercises(exercises: List<ExerciseEntity>)

    @Query("DELETE FROM exercise_muscles WHERE exerciseId IN (:exerciseIds)")
    suspend fun deleteMuscles(exerciseIds: List<String>)

    @Insert
    suspend fun insertMuscles(muscles: List<ExerciseMuscleEntity>)

    /** Adds or updates these exercises and replaces their muscles, all as one change. */
    @Transaction
    suspend fun saveExercises(exercises: List<ExerciseEntity>, muscles: List<ExerciseMuscleEntity>) {
        upsertExercises(exercises)
        deleteMuscles(exercises.map { it.id })
        insertMuscles(muscles)
    }
}
