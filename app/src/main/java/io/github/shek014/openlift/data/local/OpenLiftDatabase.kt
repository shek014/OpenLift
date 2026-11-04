package io.github.shek014.openlift.data.local

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ExerciseEntity::class,
        ExerciseMuscleEntity::class,
        WorkoutEntity::class,
        WorkoutExerciseEntity::class,
        WorkoutSetEntity::class,
    ],
    version = 2,
    autoMigrations = [AutoMigration(from = 1, to = 2)],
)
abstract class OpenLiftDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao

    abstract fun workoutDao(): WorkoutDao

    companion object {
        fun create(context: Context): OpenLiftDatabase =
            Room.databaseBuilder(context, OpenLiftDatabase::class.java, "openlift.db").build()
    }
}
