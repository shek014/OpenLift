package io.github.shek014.openlift

import android.app.Application
import io.github.shek014.openlift.data.ExerciseRepository
import io.github.shek014.openlift.data.local.OpenLiftDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Created once when the app starts. Holds the objects shared by all screens. */
class OpenLiftApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val database by lazy { OpenLiftDatabase.create(this) }

    val exerciseRepository by lazy { ExerciseRepository(database.exerciseDao(), applicationScope) }
}
