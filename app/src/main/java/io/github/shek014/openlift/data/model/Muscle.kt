package io.github.shek014.openlift.data.model

enum class MuscleGroup(val label: String) {
    Chest("Chest"),
    Back("Back"),
    Shoulders("Shoulders"),
    Arms("Arms"),
    Core("Core"),
    Legs("Legs"),
}

/**
 * The individual muscles an exercise can work.
 *
 * These names are saved in the database, so renaming one needs a database migration.
 * Changing a [label] is always safe.
 */
enum class Muscle(val label: String, val group: MuscleGroup) {
    UpperChest("Upper chest", MuscleGroup.Chest),
    MidChest("Mid chest", MuscleGroup.Chest),
    LowerChest("Lower chest", MuscleGroup.Chest),

    Lats("Lats", MuscleGroup.Back),
    Traps("Traps", MuscleGroup.Back),
    MidBack("Mid back", MuscleGroup.Back),
    LowerBack("Lower back", MuscleGroup.Back),

    FrontDelts("Front delts", MuscleGroup.Shoulders),
    SideDelts("Side delts", MuscleGroup.Shoulders),
    RearDelts("Rear delts", MuscleGroup.Shoulders),

    Biceps("Biceps", MuscleGroup.Arms),
    Triceps("Triceps", MuscleGroup.Arms),
    Forearms("Forearms", MuscleGroup.Arms),

    Abs("Abs", MuscleGroup.Core),
    Obliques("Obliques", MuscleGroup.Core),

    Quads("Quads", MuscleGroup.Legs),
    Hamstrings("Hamstrings", MuscleGroup.Legs),
    Glutes("Glutes", MuscleGroup.Legs),
    Calves("Calves", MuscleGroup.Legs),
    Adductors("Adductors", MuscleGroup.Legs),
    Abductors("Abductors", MuscleGroup.Legs),
}
