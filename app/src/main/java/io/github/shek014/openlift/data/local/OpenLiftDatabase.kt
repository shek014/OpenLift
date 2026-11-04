package io.github.shek014.openlift.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ExerciseEntity::class, ExerciseMuscleEntity::class],
    version = 1,
)
abstract class OpenLiftDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao

    companion object {
        fun create(context: Context): OpenLiftDatabase =
            Room.databaseBuilder(context, OpenLiftDatabase::class.java, "openlift.db").build()
    }
}
