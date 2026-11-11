package io.github.shek014.lifttrail.data.model

/** These names are saved in the database, so renaming one needs a database migration. */
enum class Equipment(val label: String) {
    Barbell("Barbell"),
    Dumbbell("Dumbbell"),
    EzBar("EZ bar"),
    Kettlebell("Kettlebell"),
    Cable("Cable"),
    Machine("Machine"),
    SmithMachine("Smith machine"),
    Bodyweight("Bodyweight"),
    /** Bodyweight with added weight, such as a dip belt, a vest or a plate held to the chest. */
    Weighted("Weighted"),
    Band("Band"),
}

/**
 * @property id never changes, even if the exercise is renamed, because saved workouts will refer to it.
 * @property primaryMuscles the muscles doing most of the work.
 * @property secondaryMuscles the muscles that assist.
 */
data class Exercise(
    val id: String,
    val name: String,
    val equipment: Equipment,
    val primaryMuscles: List<Muscle>,
    val secondaryMuscles: List<Muscle> = emptyList(),
    /** Made by the person using the app, rather than built in. */
    val isCustom: Boolean = false,
    /** Deleted from the lists, but kept because saved workouts may still refer to it. */
    val isArchived: Boolean = false,
    /** A custom exercise's own how-to steps, one per line. */
    val notes: String? = null,
    /** Tracked by time, like a plank or a hang, rather than by reps. */
    val isTimed: Boolean = false,
) {
    /** Plain bodyweight exercises have no weight to log. Their weighted versions do. */
    val takesWeight: Boolean
        get() = equipment != Equipment.Bodyweight
}
