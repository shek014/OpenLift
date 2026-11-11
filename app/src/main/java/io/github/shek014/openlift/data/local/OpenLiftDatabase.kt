package io.github.shek014.openlift.data.local

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Supersets became a pairing of exercises, so a single set can't be marked as one any more. */
private val RemoveSupersetSetType = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("UPDATE workout_sets SET setType = 'Normal' WHERE setType = 'Superset'")
    }
}

@Database(
    entities = [
        ExerciseEntity::class,
        ExerciseMuscleEntity::class,
        WorkoutEntity::class,
        WorkoutExerciseEntity::class,
        WorkoutSetEntity::class,
        RoutineEntity::class,
        RoutineExerciseEntity::class,
        MeasurementEntity::class,
        ExerciseNoteEntity::class,
    ],
    version = 10,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6),
        AutoMigration(from = 7, to = 8),
        AutoMigration(from = 8, to = 9),
        AutoMigration(from = 9, to = 10),
    ],
)
abstract class OpenLiftDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao

    abstract fun workoutDao(): WorkoutDao

    abstract fun routineDao(): RoutineDao

    abstract fun measurementDao(): MeasurementDao

    companion object {
        const val FileName = "openlift.db"

        fun create(context: Context, fileName: String = FileName): OpenLiftDatabase =
            Room.databaseBuilder(context, OpenLiftDatabase::class.java, fileName)
                .addMigrations(RemoveSupersetSetType)
                .build()
    }
}
