package io.github.shek014.openlift.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import io.github.shek014.openlift.data.model.Equipment
import io.github.shek014.openlift.data.model.Exercise
import io.github.shek014.openlift.data.model.Muscle

/** One row of the `exercises` table. */
@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val equipment: Equipment,
)

enum class MuscleRole { Primary, Secondary }

/** Links an exercise to one muscle it works. An exercise has one row here per muscle. */
@Entity(
    tableName = "exercise_muscles",
    primaryKeys = ["exerciseId", "muscle"],
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("muscle")],
)
data class ExerciseMuscleEntity(
    val exerciseId: String,
    val muscle: Muscle,
    val role: MuscleRole,
)

/** An exercise row together with its muscle rows, which Room loads for us. */
data class ExerciseWithMuscles(
    @Embedded val exercise: ExerciseEntity,
    @Relation(parentColumn = "id", entityColumn = "exerciseId")
    val muscles: List<ExerciseMuscleEntity>,
)

fun ExerciseWithMuscles.toExercise() = Exercise(
    id = exercise.id,
    name = exercise.name,
    equipment = exercise.equipment,
    primaryMuscles = muscles.filter { it.role == MuscleRole.Primary }.map { it.muscle },
    secondaryMuscles = muscles.filter { it.role == MuscleRole.Secondary }.map { it.muscle },
)

fun Exercise.toEntity() = ExerciseEntity(id = id, name = name, equipment = equipment)

fun Exercise.toMuscleEntities(): List<ExerciseMuscleEntity> =
    primaryMuscles.map { ExerciseMuscleEntity(id, it, MuscleRole.Primary) } +
        secondaryMuscles.map { ExerciseMuscleEntity(id, it, MuscleRole.Secondary) }
