package io.github.shek014.openlift.data

import android.content.res.AssetManager

/**
 * Finds the animations bundled with the app. Not every exercise has one yet, and a variation may
 * borrow the animation of the movement it's based on.
 */
class ExerciseMedia(private val assets: AssetManager) {
    private val animatedExerciseIds by lazy {
        assets.list(Folder).orEmpty()
            .filter { it.endsWith(Extension) }
            .map { it.removeSuffix(Extension) }
            .toSet()
    }

    private val borrowedDrawings by lazy {
        ExerciseVariations.mapNotNull { variation -> variation.drawing?.let { variation.exercise.id to it } }.toMap()
    }

    /** The address of the exercise's animation, or null if it doesn't have one. */
    fun animationUri(exerciseId: String): String? {
        val drawing = if (exerciseId in animatedExerciseIds) exerciseId else borrowedDrawings[exerciseId]
        return drawing?.let { "file:///android_asset/$Folder/$it$Extension" }
    }

    private companion object {
        const val Folder = "exercise_media"
        const val Extension = ".gif"
    }
}
