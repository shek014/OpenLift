package io.github.shek014.lifttrail.data.local

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import io.github.shek014.lifttrail.data.model.SetType

/** A workout. While [finishedAt] is null it's the one in progress. Times are in milliseconds. */
@Entity(tableName = "workouts", indices = [Index("finishedAt")])
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAt: Long,
    val finishedAt: Long? = null,
    /** The name of the routine it was started from, if any. */
    val name: String? = null,
    /** The routine it was started from, if any. */
    val routineId: Long? = null,
    /** The rest timer for this workout: null means the one from Settings, and 0 means no timer. */
    val restSeconds: Int? = null,
)

/** An exercise as done in one workout. The same exercise can appear more than once. */
@Entity(
    tableName = "workout_exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
        ),
    ],
    indices = [Index("workoutId"), Index("exerciseId")],
)
data class WorkoutExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutId: Long,
    val exerciseId: String,
    val position: Int,
    /** How long to rest after a set: null means the default from Settings, and 0 means no timer. */
    val restSeconds: Int? = null,
    /** The reps the routine called for, if it came from one. */
    val targetRepsMin: Int? = null,
    val targetRepsMax: Int? = null,
    /** Exercises with the same number here are done back to back as a superset. */
    val supersetId: Long? = null,
    val notes: String? = null,
)

/** One set. Weight is always stored in kilograms, and null means no added weight. */
@Entity(
    tableName = "workout_sets",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutExerciseId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("workoutExerciseId")],
)
data class WorkoutSetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutExerciseId: Long,
    val position: Int,
    val weightKg: Double? = null,
    val reps: Int? = null,
    val completed: Boolean = false,
    @ColumnInfo(defaultValue = "Normal") val setType: SetType = SetType.Normal,
    val durationSeconds: Int? = null,
)

data class WorkoutExerciseWithSets(
    @Embedded val workoutExercise: WorkoutExerciseEntity,
    @Relation(parentColumn = "id", entityColumn = "workoutExerciseId")
    val sets: List<WorkoutSetEntity>,
)

data class WorkoutWithExercises(
    @Embedded val workout: WorkoutEntity,
    @Relation(entity = WorkoutExerciseEntity::class, parentColumn = "id", entityColumn = "workoutId")
    val exercises: List<WorkoutExerciseWithSets>,
)

/** A completed set from a finished workout, read from three tables at once. */
data class LoggedSetRow(
    val workoutId: Long,
    val finishedAt: Long,
    val exerciseId: String,
    val weightKg: Double?,
    val reps: Int?,
    val durationSeconds: Int?,
    val setType: SetType,
)
