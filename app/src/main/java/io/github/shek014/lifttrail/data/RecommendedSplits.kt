package io.github.shek014.lifttrail.data

import io.github.shek014.lifttrail.data.model.ExercisePlan

data class SplitExercise(val exerciseId: String, val plan: ExercisePlan)

data class SplitDay(val name: String, val exercises: List<SplitExercise>)

/**
 * A ready-made training program, which adding copies into someone's own routines.
 *
 * @property id never changes, because the screen for a split is found by it.
 * @property shortName goes before each day's name, so that routines from different splits can be told apart.
 */
data class Split(
    val id: String,
    val name: String,
    val shortName: String,
    val schedule: String,
    val description: String,
    val days: List<SplitDay>,
)

private fun exercise(id: String, sets: Int, repsMin: Int, repsMax: Int = repsMin) =
    SplitExercise(id, ExercisePlan(sets, repsMin, repsMax))

private val Push = SplitDay(
    "Push",
    listOf(
        exercise("barbell_bench_press", 4, 6, 10),
        exercise("overhead_press", 3, 6, 10),
        exercise("incline_dumbbell_bench_press", 3, 8, 12),
        exercise("dumbbell_lateral_raise", 3, 12, 15),
        exercise("triceps_pushdown", 3, 10, 12),
        exercise("overhead_cable_triceps_extension", 2, 10, 12),
    ),
)

private val Pull = SplitDay(
    "Pull",
    listOf(
        exercise("pull_up", 3, 6, 10),
        exercise("barbell_row", 3, 6, 10),
        exercise("seated_cable_row", 3, 8, 12),
        exercise("face_pull", 3, 12, 15),
        exercise("barbell_curl", 3, 8, 12),
        exercise("hammer_curl", 2, 10, 12),
    ),
)

private val Legs = SplitDay(
    "Legs",
    listOf(
        exercise("barbell_back_squat", 4, 6, 10),
        exercise("romanian_deadlift", 3, 8, 10),
        exercise("leg_press", 3, 10, 12),
        exercise("lying_leg_curl", 3, 10, 12),
        exercise("standing_calf_raise", 4, 10, 15),
    ),
)

private val UpperA = SplitDay(
    "Upper A",
    listOf(
        exercise("barbell_bench_press", 4, 6, 10),
        exercise("barbell_row", 4, 6, 10),
        exercise("dumbbell_shoulder_press", 3, 8, 12),
        exercise("lat_pulldown", 3, 8, 12),
        exercise("dumbbell_curl", 2, 10, 12),
        exercise("triceps_pushdown", 2, 10, 12),
    ),
)

private val LowerA = SplitDay(
    "Lower A",
    listOf(
        exercise("barbell_back_squat", 4, 6, 10),
        exercise("romanian_deadlift", 3, 8, 10),
        exercise("leg_extension", 3, 10, 12),
        exercise("seated_leg_curl", 3, 10, 12),
        exercise("standing_calf_raise", 3, 10, 15),
        exercise("cable_crunch", 3, 10, 15),
    ),
)

private val UpperB = SplitDay(
    "Upper B",
    listOf(
        exercise("overhead_press", 4, 6, 10),
        exercise("pull_up", 4, 6, 10),
        exercise("incline_dumbbell_bench_press", 3, 8, 12),
        exercise("seated_cable_row", 3, 8, 12),
        exercise("dumbbell_lateral_raise", 3, 12, 15),
        exercise("hammer_curl", 2, 10, 12),
        exercise("overhead_cable_triceps_extension", 2, 10, 12),
    ),
)

private val LowerB = SplitDay(
    "Lower B",
    listOf(
        exercise("deadlift", 3, 5, 8),
        exercise("leg_press", 4, 8, 12),
        exercise("bulgarian_split_squat", 3, 8, 12),
        exercise("lying_leg_curl", 3, 10, 12),
        exercise("seated_calf_raise", 3, 10, 15),
        exercise("hanging_leg_raise", 3, 8, 12),
    ),
)

/** The programs offered on the Routines tab. */
val RecommendedSplits: List<Split> = listOf(
    Split(
        id = "full_body",
        name = "Full Body",
        shortName = "Full Body",
        schedule = "3 days a week",
        description = "Trains your whole body each session, with a rest day between sessions. A good place to start, and easy to fit around a busy week.",
        days = listOf(
            SplitDay(
                "A",
                listOf(
                    exercise("barbell_back_squat", 3, 6, 10),
                    exercise("barbell_bench_press", 3, 6, 10),
                    exercise("barbell_row", 3, 6, 10),
                    exercise("dumbbell_lateral_raise", 2, 12, 15),
                    exercise("dumbbell_curl", 2, 10, 12),
                ),
            ),
            SplitDay(
                "B",
                listOf(
                    exercise("romanian_deadlift", 3, 8, 10),
                    exercise("overhead_press", 3, 6, 10),
                    exercise("lat_pulldown", 3, 8, 12),
                    exercise("leg_extension", 2, 10, 12),
                    exercise("triceps_pushdown", 2, 10, 12),
                ),
            ),
            SplitDay(
                "C",
                listOf(
                    exercise("leg_press", 3, 10, 12),
                    exercise("incline_dumbbell_bench_press", 3, 8, 12),
                    exercise("seated_cable_row", 3, 8, 12),
                    exercise("lying_leg_curl", 2, 10, 12),
                    exercise("face_pull", 2, 12, 15),
                ),
            ),
        ),
    ),
    Split(
        id = "upper_lower",
        name = "Upper/Lower",
        shortName = "Upper/Lower",
        schedule = "4 days a week",
        description = "Alternates upper and lower body, so each muscle gets trained twice a week. A common next step after full-body training.",
        days = listOf(UpperA, LowerA, UpperB, LowerB),
    ),
    Split(
        id = "push_pull_legs",
        name = "Push/Pull/Legs",
        shortName = "PPL",
        schedule = "3 or 6 days a week",
        description = "Groups muscles by how they move: pressing, pulling, then legs. Run it once a week, or twice a week if you can train six days.",
        days = listOf(Push, Pull, Legs),
    ),
    Split(
        id = "ppl_upper_lower",
        name = "Push/Pull/Legs + Upper/Lower",
        shortName = "PPLUL",
        schedule = "5 days a week",
        description = "Combines both splits so each muscle gets trained about twice in five sessions. Suits people who can train most days but not six.",
        days = listOf(Push, Pull, Legs, UpperA, LowerA),
    ),
    Split(
        id = "power_hypertrophy",
        name = "Power and Size",
        shortName = "Power/Size",
        schedule = "4 days a week",
        description = "An upper/lower split with one heavy day and one higher-rep day for each half of the body, for building both strength and muscle.",
        days = listOf(
            SplitDay(
                "Upper Power",
                listOf(
                    exercise("barbell_bench_press", 4, 3, 5),
                    exercise("barbell_row", 4, 3, 5),
                    exercise("overhead_press", 3, 5, 8),
                    exercise("pull_up", 3, 5, 8),
                    exercise("barbell_curl", 2, 6, 10),
                    exercise("skull_crusher", 2, 6, 10),
                ),
            ),
            SplitDay(
                "Lower Power",
                listOf(
                    exercise("barbell_back_squat", 4, 3, 5),
                    exercise("deadlift", 3, 3, 5),
                    exercise("leg_press", 3, 8, 10),
                    exercise("lying_leg_curl", 3, 6, 10),
                    exercise("standing_calf_raise", 4, 8, 10),
                ),
            ),
            SplitDay(
                "Upper Size",
                listOf(
                    exercise("incline_dumbbell_bench_press", 4, 8, 12),
                    exercise("seated_cable_row", 4, 8, 12),
                    exercise("dumbbell_fly", 3, 10, 15),
                    exercise("lat_pulldown", 3, 10, 12),
                    exercise("dumbbell_lateral_raise", 3, 12, 15),
                    exercise("incline_dumbbell_curl", 3, 10, 12),
                    exercise("triceps_pushdown", 3, 10, 12),
                ),
            ),
            SplitDay(
                "Lower Size",
                listOf(
                    exercise("front_squat", 4, 8, 12),
                    exercise("bulgarian_split_squat", 3, 10, 12),
                    exercise("leg_extension", 3, 12, 15),
                    exercise("seated_leg_curl", 3, 12, 15),
                    exercise("seated_calf_raise", 4, 12, 15),
                ),
            ),
        ),
    ),
    Split(
        id = "chest_back_arms",
        name = "Antagonist Split",
        shortName = "Antagonist",
        schedule = "6 days a week",
        description = "Pairs opposing muscles, like chest with back, across three sessions that repeat twice a week. High in volume, so better suited to experienced lifters.",
        days = listOf(
            SplitDay(
                "Chest and Back",
                listOf(
                    exercise("barbell_bench_press", 4, 6, 10),
                    exercise("pull_up", 4, 6, 10),
                    exercise("incline_dumbbell_fly", 3, 10, 12),
                    exercise("t_bar_row", 3, 8, 12),
                    exercise("cable_crossover", 3, 12, 15),
                    exercise("lat_pulldown", 3, 10, 12),
                ),
            ),
            SplitDay(
                "Shoulders and Arms",
                listOf(
                    exercise("overhead_press", 4, 6, 10),
                    exercise("dumbbell_lateral_raise", 3, 12, 15),
                    exercise("reverse_dumbbell_fly", 3, 12, 15),
                    exercise("barbell_curl", 3, 8, 12),
                    exercise("skull_crusher", 3, 8, 12),
                    exercise("hammer_curl", 3, 10, 12),
                    exercise("triceps_pushdown", 3, 10, 12),
                ),
            ),
            SplitDay(
                "Legs",
                listOf(
                    exercise("barbell_back_squat", 4, 6, 10),
                    exercise("romanian_deadlift", 3, 8, 10),
                    exercise("dumbbell_lunge", 3, 10, 12),
                    exercise("lying_leg_curl", 3, 10, 12),
                    exercise("standing_calf_raise", 4, 10, 15),
                    exercise("hanging_leg_raise", 3, 8, 12),
                ),
            ),
        ),
    ),
    Split(
        id = "body_part",
        name = "Body Part Split",
        shortName = "Body Part",
        schedule = "5 days a week",
        description = "Gives each major muscle group its own day of high-volume work, with about a week before training it again.",
        days = listOf(
            SplitDay(
                "Chest",
                listOf(
                    exercise("barbell_bench_press", 4, 6, 10),
                    exercise("incline_dumbbell_bench_press", 3, 8, 12),
                    exercise("machine_chest_press", 3, 10, 12),
                    exercise("cable_crossover", 3, 12, 15),
                    exercise("chest_dip", 2, 8, 12),
                ),
            ),
            SplitDay(
                "Back",
                listOf(
                    exercise("deadlift", 3, 5, 8),
                    exercise("pull_up", 3, 6, 10),
                    exercise("barbell_row", 3, 8, 10),
                    exercise("seated_cable_row", 3, 10, 12),
                    exercise("straight_arm_pulldown", 3, 12, 15),
                ),
            ),
            SplitDay(
                "Shoulders",
                listOf(
                    exercise("overhead_press", 4, 6, 10),
                    exercise("dumbbell_lateral_raise", 4, 12, 15),
                    exercise("reverse_pec_deck", 3, 12, 15),
                    exercise("dumbbell_front_raise", 2, 10, 12),
                    exercise("barbell_shrug", 3, 10, 12),
                ),
            ),
            SplitDay(
                "Legs",
                listOf(
                    exercise("barbell_back_squat", 4, 6, 10),
                    exercise("leg_press", 3, 10, 12),
                    exercise("romanian_deadlift", 3, 8, 10),
                    exercise("leg_extension", 3, 12, 15),
                    exercise("lying_leg_curl", 3, 10, 12),
                    exercise("standing_calf_raise", 4, 10, 15),
                ),
            ),
            SplitDay(
                "Arms",
                listOf(
                    exercise("close_grip_bench_press", 3, 6, 10),
                    exercise("barbell_curl", 3, 8, 12),
                    exercise("overhead_cable_triceps_extension", 3, 10, 12),
                    exercise("incline_dumbbell_curl", 3, 10, 12),
                    exercise("triceps_pushdown", 3, 10, 15),
                    exercise("hammer_curl", 3, 10, 12),
                ),
            ),
        ),
    ),
    Split(
        id = "dumbbells_at_home",
        name = "Dumbbells at Home",
        shortName = "Home",
        schedule = "3 days a week",
        description = "Full-body sessions that only need dumbbells, a bench or sturdy chair, and something to hang from for chin-ups.",
        days = listOf(
            SplitDay(
                "A",
                listOf(
                    exercise("bulgarian_split_squat", 3, 8, 12),
                    exercise("dumbbell_bench_press", 3, 8, 12),
                    exercise("dumbbell_row", 3, 8, 12),
                    exercise("dumbbell_lateral_raise", 2, 12, 15),
                    exercise("lying_leg_raise", 3, 10, 15),
                ),
            ),
            SplitDay(
                "B",
                listOf(
                    exercise("dumbbell_romanian_deadlift", 3, 8, 12),
                    exercise("dumbbell_shoulder_press", 3, 8, 12),
                    exercise("chin_up", 3, 5, 10),
                    exercise("dumbbell_curl", 2, 10, 12),
                    exercise("bench_dip", 2, 10, 15),
                ),
            ),
            SplitDay(
                "C",
                listOf(
                    exercise("dumbbell_lunge", 3, 10, 12),
                    exercise("push_up", 3, 8, 15),
                    exercise("reverse_dumbbell_fly", 3, 12, 15),
                    exercise("glute_bridge", 3, 12, 15),
                    exercise("hammer_curl", 2, 10, 12),
                ),
            ),
        ),
    ),
)
