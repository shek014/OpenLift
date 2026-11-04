package io.github.shek014.openlift.data.model

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
)
