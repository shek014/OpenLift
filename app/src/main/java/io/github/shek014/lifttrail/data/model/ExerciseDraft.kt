package io.github.shek014.lifttrail.data.model

/** What's been entered so far for a custom exercise. */
data class ExerciseDraft(
    val name: String = "",
    val equipment: Equipment = Equipment.Barbell,
    val primaryMuscles: List<Muscle> = emptyList(),
    val secondaryMuscles: List<Muscle> = emptyList(),
    val notes: String = "",
    val isTimed: Boolean = false,
) {
    /** Selects or clears a primary muscle. A muscle can't be both primary and secondary. */
    fun togglePrimary(muscle: Muscle): ExerciseDraft =
        if (muscle in primaryMuscles) {
            copy(primaryMuscles = primaryMuscles - muscle)
        } else {
            copy(primaryMuscles = primaryMuscles + muscle, secondaryMuscles = secondaryMuscles - muscle)
        }

    /** Selects or clears a secondary muscle. A muscle can't be both primary and secondary. */
    fun toggleSecondary(muscle: Muscle): ExerciseDraft =
        if (muscle in secondaryMuscles) {
            copy(secondaryMuscles = secondaryMuscles - muscle)
        } else {
            copy(secondaryMuscles = secondaryMuscles + muscle, primaryMuscles = primaryMuscles - muscle)
        }

    /** Why it can't be saved yet, or null if it can. [takenNames] holds the other exercises' names. */
    fun problem(takenNames: Collection<String>): String? {
        val trimmed = name.trim()
        return when {
            trimmed.isEmpty() -> "Enter a name."
            takenNames.any { it.equals(trimmed, ignoreCase = true) } -> "You already have an exercise with this name."
            primaryMuscles.isEmpty() -> "Choose at least one primary muscle."
            else -> null
        }
    }
}

/** Splits a custom exercise's notes into steps, one per line, for its page. */
fun notesToSteps(notes: String?): List<String> =
    notes.orEmpty().lines().map { it.trim() }.filter { it.isNotEmpty() }
