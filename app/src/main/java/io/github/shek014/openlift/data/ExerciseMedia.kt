package io.github.shek014.openlift.data

import android.content.res.AssetManager

/** Finds the animations bundled with the app. Not every exercise has one yet. */
class ExerciseMedia(private val assets: AssetManager) {
    private val animatedExerciseIds by lazy {
        assets.list(Folder).orEmpty()
            .filter { it.endsWith(Extension) }
            .map { it.removeSuffix(Extension) }
            .toSet()
    }

    /** The address of the exercise's animation, or null if it doesn't have one. */
    fun animationUri(exerciseId: String): String? =
        if (exerciseId in animatedExerciseIds) "file:///android_asset/$Folder/$exerciseId$Extension" else null

    private companion object {
        const val Folder = "exercise_media"
        const val Extension = ".gif"
    }
}
