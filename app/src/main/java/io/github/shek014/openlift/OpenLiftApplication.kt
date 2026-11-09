package io.github.shek014.openlift

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import io.github.shek014.openlift.data.AppSettings
import io.github.shek014.openlift.data.DatabaseBackup
import io.github.shek014.openlift.data.ExerciseMedia
import io.github.shek014.openlift.data.ExerciseRepository
import io.github.shek014.openlift.data.MeasurementRepository
import io.github.shek014.openlift.data.RestTimer
import io.github.shek014.openlift.data.RoutineRepository
import io.github.shek014.openlift.data.SoundPlayer
import io.github.shek014.openlift.data.WorkoutRepository
import io.github.shek014.openlift.data.local.OpenLiftDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Created once when the app starts. Holds the objects shared by all screens. */
class OpenLiftApplication : Application(), SingletonImageLoader.Factory {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val database by lazy { OpenLiftDatabase.create(this) }

    val exerciseRepository by lazy { ExerciseRepository(database.exerciseDao(), applicationScope) }

    val workoutRepository by lazy { WorkoutRepository(database.workoutDao(), exerciseRepository) }

    val routineRepository by lazy {
        RoutineRepository(database.routineDao(), database.workoutDao(), exerciseRepository)
    }

    val measurementRepository by lazy { MeasurementRepository(database.measurementDao()) }

    val soundPlayer by lazy { SoundPlayer(this) }

    val restTimer by lazy { RestTimer(this, applicationScope) { soundPlayer.play(settings.restSound.value) } }

    val exerciseMedia by lazy { ExerciseMedia(assets) }

    val settings by lazy { AppSettings(this) }

    val databaseBackup by lazy { DatabaseBackup(this, database) }

    /** Starts the app afresh, which it needs in order to pick up a restored database. */
    fun restart() {
        restTimer.stop()
        startActivity(Intent.makeRestartActivityTask(ComponentName(this, MainActivity::class.java)))
        Runtime.getRuntime().exit(0)
    }

    /** Teaches the image library to play GIFs. Android 9 added a faster way to do it. */
    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(AnimatedImageDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
}
