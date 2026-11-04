package io.github.shek014.openlift.data

import io.github.shek014.openlift.data.model.Equipment.Band
import io.github.shek014.openlift.data.model.Equipment.Barbell
import io.github.shek014.openlift.data.model.Equipment.Bodyweight
import io.github.shek014.openlift.data.model.Equipment.Cable
import io.github.shek014.openlift.data.model.Equipment.Dumbbell
import io.github.shek014.openlift.data.model.Equipment.EzBar
import io.github.shek014.openlift.data.model.Equipment.Kettlebell
import io.github.shek014.openlift.data.model.Equipment.Machine
import io.github.shek014.openlift.data.model.Equipment.SmithMachine
import io.github.shek014.openlift.data.model.Exercise
import io.github.shek014.openlift.data.model.Muscle.Abductors
import io.github.shek014.openlift.data.model.Muscle.Abs
import io.github.shek014.openlift.data.model.Muscle.Adductors
import io.github.shek014.openlift.data.model.Muscle.Biceps
import io.github.shek014.openlift.data.model.Muscle.Calves
import io.github.shek014.openlift.data.model.Muscle.Forearms
import io.github.shek014.openlift.data.model.Muscle.FrontDelts
import io.github.shek014.openlift.data.model.Muscle.Glutes
import io.github.shek014.openlift.data.model.Muscle.Hamstrings
import io.github.shek014.openlift.data.model.Muscle.Lats
import io.github.shek014.openlift.data.model.Muscle.LowerBack
import io.github.shek014.openlift.data.model.Muscle.LowerChest
import io.github.shek014.openlift.data.model.Muscle.MidBack
import io.github.shek014.openlift.data.model.Muscle.MidChest
import io.github.shek014.openlift.data.model.Muscle.Obliques
import io.github.shek014.openlift.data.model.Muscle.Quads
import io.github.shek014.openlift.data.model.Muscle.RearDelts
import io.github.shek014.openlift.data.model.Muscle.SideDelts
import io.github.shek014.openlift.data.model.Muscle.Traps
import io.github.shek014.openlift.data.model.Muscle.Triceps
import io.github.shek014.openlift.data.model.Muscle.UpperChest

/**
 * The exercises built into the app. To add one, add a line here; it reaches the database the
 * next time the app starts. Never change an existing id, and never remove an entry once it has
 * been released, because saved workouts will refer to it.
 */
val ExerciseCatalog: List<Exercise> = listOf(
    // Chest
    Exercise("barbell_bench_press", "Barbell Bench Press", Barbell, listOf(MidChest), listOf(FrontDelts, Triceps)),
    Exercise("incline_barbell_bench_press", "Incline Barbell Bench Press", Barbell, listOf(UpperChest), listOf(FrontDelts, Triceps)),
    Exercise("decline_barbell_bench_press", "Decline Barbell Bench Press", Barbell, listOf(LowerChest), listOf(Triceps)),
    Exercise("smith_machine_bench_press", "Smith Machine Bench Press", SmithMachine, listOf(MidChest), listOf(FrontDelts, Triceps)),
    Exercise("dumbbell_bench_press", "Dumbbell Bench Press", Dumbbell, listOf(MidChest), listOf(FrontDelts, Triceps)),
    Exercise("incline_dumbbell_bench_press", "Incline Dumbbell Bench Press", Dumbbell, listOf(UpperChest), listOf(FrontDelts, Triceps)),
    Exercise("dumbbell_fly", "Dumbbell Fly", Dumbbell, listOf(MidChest), listOf(FrontDelts)),
    Exercise("incline_dumbbell_fly", "Incline Dumbbell Fly", Dumbbell, listOf(UpperChest), listOf(FrontDelts)),
    Exercise("machine_chest_press", "Machine Chest Press", Machine, listOf(MidChest), listOf(FrontDelts, Triceps)),
    Exercise("pec_deck", "Pec Deck", Machine, listOf(MidChest), listOf(FrontDelts)),
    Exercise("cable_crossover", "Cable Crossover", Cable, listOf(LowerChest, MidChest), listOf(FrontDelts)),
    Exercise("low_cable_fly", "Low Cable Fly", Cable, listOf(UpperChest), listOf(FrontDelts)),
    Exercise("push_up", "Push-Up", Bodyweight, listOf(MidChest), listOf(FrontDelts, Triceps, Abs)),
    Exercise("chest_dip", "Chest Dip", Bodyweight, listOf(LowerChest), listOf(Triceps, FrontDelts)),

    // Back
    Exercise("pull_up", "Pull-Up", Bodyweight, listOf(Lats), listOf(Biceps, MidBack)),
    Exercise("chin_up", "Chin-Up", Bodyweight, listOf(Lats, Biceps), listOf(MidBack)),
    Exercise("lat_pulldown", "Lat Pulldown", Cable, listOf(Lats), listOf(Biceps, MidBack)),
    Exercise("close_grip_lat_pulldown", "Close-Grip Lat Pulldown", Cable, listOf(Lats), listOf(Biceps, MidBack)),
    Exercise("straight_arm_pulldown", "Straight-Arm Pulldown", Cable, listOf(Lats), listOf(Triceps)),
    Exercise("barbell_row", "Barbell Row", Barbell, listOf(Lats, MidBack), listOf(RearDelts, Biceps, LowerBack)),
    Exercise("t_bar_row", "T-Bar Row", Barbell, listOf(MidBack, Lats), listOf(RearDelts, Biceps, LowerBack)),
    Exercise("dumbbell_row", "Dumbbell Row", Dumbbell, listOf(Lats), listOf(MidBack, RearDelts, Biceps)),
    Exercise("seated_cable_row", "Seated Cable Row", Cable, listOf(MidBack, Lats), listOf(RearDelts, Biceps)),
    Exercise("chest_supported_machine_row", "Chest-Supported Machine Row", Machine, listOf(MidBack), listOf(Lats, RearDelts, Biceps)),
    Exercise("barbell_shrug", "Barbell Shrug", Barbell, listOf(Traps), listOf(Forearms)),
    Exercise("dumbbell_shrug", "Dumbbell Shrug", Dumbbell, listOf(Traps), listOf(Forearms)),
    Exercise("deadlift", "Deadlift", Barbell, listOf(Glutes, Hamstrings, LowerBack), listOf(Quads, Lats, Traps, Forearms)),
    Exercise("rack_pull", "Rack Pull", Barbell, listOf(LowerBack, Glutes), listOf(Hamstrings, Traps, Forearms)),
    Exercise("good_morning", "Good Morning", Barbell, listOf(Hamstrings, LowerBack), listOf(Glutes)),
    Exercise("back_extension", "Back Extension", Bodyweight, listOf(LowerBack), listOf(Glutes, Hamstrings)),

    // Shoulders
    Exercise("overhead_press", "Overhead Press", Barbell, listOf(FrontDelts), listOf(SideDelts, Triceps, Traps)),
    Exercise("dumbbell_shoulder_press", "Dumbbell Shoulder Press", Dumbbell, listOf(FrontDelts), listOf(SideDelts, Triceps, Traps)),
    Exercise("machine_shoulder_press", "Machine Shoulder Press", Machine, listOf(FrontDelts), listOf(SideDelts, Triceps)),
    Exercise("arnold_press", "Arnold Press", Dumbbell, listOf(FrontDelts, SideDelts), listOf(Triceps)),
    Exercise("dumbbell_lateral_raise", "Dumbbell Lateral Raise", Dumbbell, listOf(SideDelts), listOf(Traps)),
    Exercise("cable_lateral_raise", "Cable Lateral Raise", Cable, listOf(SideDelts), listOf(Traps)),
    Exercise("machine_lateral_raise", "Machine Lateral Raise", Machine, listOf(SideDelts)),
    Exercise("dumbbell_front_raise", "Dumbbell Front Raise", Dumbbell, listOf(FrontDelts)),
    Exercise("upright_row", "Upright Row", Barbell, listOf(SideDelts, Traps), listOf(Biceps)),
    Exercise("reverse_dumbbell_fly", "Reverse Dumbbell Fly", Dumbbell, listOf(RearDelts), listOf(MidBack)),
    Exercise("reverse_pec_deck", "Reverse Pec Deck", Machine, listOf(RearDelts), listOf(MidBack)),
    Exercise("face_pull", "Face Pull", Cable, listOf(RearDelts), listOf(MidBack, Traps)),
    Exercise("band_pull_apart", "Band Pull-Apart", Band, listOf(RearDelts), listOf(MidBack)),

    // Biceps
    Exercise("barbell_curl", "Barbell Curl", Barbell, listOf(Biceps), listOf(Forearms)),
    Exercise("ez_bar_curl", "EZ Bar Curl", EzBar, listOf(Biceps), listOf(Forearms)),
    Exercise("dumbbell_curl", "Dumbbell Curl", Dumbbell, listOf(Biceps), listOf(Forearms)),
    Exercise("hammer_curl", "Hammer Curl", Dumbbell, listOf(Biceps), listOf(Forearms)),
    Exercise("incline_dumbbell_curl", "Incline Dumbbell Curl", Dumbbell, listOf(Biceps)),
    Exercise("concentration_curl", "Concentration Curl", Dumbbell, listOf(Biceps)),
    Exercise("preacher_curl", "Preacher Curl", EzBar, listOf(Biceps)),
    Exercise("cable_curl", "Cable Curl", Cable, listOf(Biceps), listOf(Forearms)),
    Exercise("band_curl", "Band Curl", Band, listOf(Biceps), listOf(Forearms)),

    // Triceps
    Exercise("triceps_pushdown", "Triceps Pushdown", Cable, listOf(Triceps)),
    Exercise("overhead_cable_triceps_extension", "Overhead Cable Triceps Extension", Cable, listOf(Triceps)),
    Exercise("overhead_dumbbell_triceps_extension", "Overhead Dumbbell Triceps Extension", Dumbbell, listOf(Triceps)),
    Exercise("dumbbell_kickback", "Dumbbell Kickback", Dumbbell, listOf(Triceps)),
    Exercise("skull_crusher", "Skull Crusher", EzBar, listOf(Triceps)),
    Exercise("close_grip_bench_press", "Close-Grip Bench Press", Barbell, listOf(Triceps), listOf(MidChest, FrontDelts)),
    Exercise("bench_dip", "Bench Dip", Bodyweight, listOf(Triceps), listOf(FrontDelts, LowerChest)),

    // Forearms
    Exercise("wrist_curl", "Wrist Curl", Dumbbell, listOf(Forearms)),
    Exercise("reverse_wrist_curl", "Reverse Wrist Curl", Dumbbell, listOf(Forearms)),
    Exercise("reverse_curl", "Reverse Curl", EzBar, listOf(Forearms), listOf(Biceps)),
    Exercise("farmers_carry", "Farmer's Carry", Dumbbell, listOf(Forearms, Traps), listOf(Abs, Glutes), isTimed = true),

    Exercise("dead_hang", "Dead Hang", Bodyweight, listOf(Forearms), listOf(Lats), isTimed = true),

    // Core
    Exercise("crunch", "Crunch", Bodyweight, listOf(Abs)),
    Exercise("cable_crunch", "Cable Crunch", Cable, listOf(Abs), listOf(Obliques)),
    Exercise("machine_crunch", "Machine Crunch", Machine, listOf(Abs)),
    Exercise("bicycle_crunch", "Bicycle Crunch", Bodyweight, listOf(Abs, Obliques)),
    Exercise("hanging_leg_raise", "Hanging Leg Raise", Bodyweight, listOf(Abs), listOf(Obliques, Forearms)),
    Exercise("lying_leg_raise", "Lying Leg Raise", Bodyweight, listOf(Abs)),
    Exercise("plank", "Plank", Bodyweight, listOf(Abs), listOf(Obliques, LowerBack), isTimed = true),
    Exercise("side_plank", "Side Plank", Bodyweight, listOf(Obliques), listOf(Abs), isTimed = true),
    Exercise("russian_twist", "Russian Twist", Bodyweight, listOf(Obliques), listOf(Abs)),
    Exercise("cable_woodchop", "Cable Woodchop", Cable, listOf(Obliques), listOf(Abs)),
    Exercise("pallof_press", "Pallof Press", Cable, listOf(Obliques), listOf(Abs)),

    // Legs
    Exercise("barbell_back_squat", "Barbell Back Squat", Barbell, listOf(Quads, Glutes), listOf(Hamstrings, Adductors, LowerBack)),
    Exercise("front_squat", "Front Squat", Barbell, listOf(Quads), listOf(Glutes, Abs, LowerBack)),
    Exercise("smith_machine_squat", "Smith Machine Squat", SmithMachine, listOf(Quads, Glutes), listOf(Hamstrings)),
    Exercise("goblet_squat", "Goblet Squat", Kettlebell, listOf(Quads, Glutes), listOf(Abs)),
    Exercise("hack_squat", "Hack Squat", Machine, listOf(Quads), listOf(Glutes)),
    Exercise("leg_press", "Leg Press", Machine, listOf(Quads, Glutes), listOf(Hamstrings)),
    Exercise("leg_extension", "Leg Extension", Machine, listOf(Quads)),
    Exercise("dumbbell_lunge", "Dumbbell Lunge", Dumbbell, listOf(Quads, Glutes), listOf(Hamstrings)),
    Exercise("bulgarian_split_squat", "Bulgarian Split Squat", Dumbbell, listOf(Quads, Glutes), listOf(Hamstrings)),
    Exercise("dumbbell_step_up", "Dumbbell Step-Up", Dumbbell, listOf(Quads, Glutes), listOf(Hamstrings)),
    Exercise("romanian_deadlift", "Romanian Deadlift", Barbell, listOf(Hamstrings, Glutes), listOf(LowerBack, Forearms)),
    Exercise("dumbbell_romanian_deadlift", "Dumbbell Romanian Deadlift", Dumbbell, listOf(Hamstrings, Glutes), listOf(LowerBack, Forearms)),
    Exercise("sumo_deadlift", "Sumo Deadlift", Barbell, listOf(Glutes, Hamstrings, Adductors), listOf(Quads, LowerBack, Forearms)),
    Exercise("lying_leg_curl", "Lying Leg Curl", Machine, listOf(Hamstrings), listOf(Calves)),
    Exercise("seated_leg_curl", "Seated Leg Curl", Machine, listOf(Hamstrings), listOf(Calves)),
    Exercise("nordic_curl", "Nordic Curl", Bodyweight, listOf(Hamstrings), listOf(Glutes)),
    Exercise("barbell_hip_thrust", "Barbell Hip Thrust", Barbell, listOf(Glutes), listOf(Hamstrings)),
    Exercise("glute_bridge", "Glute Bridge", Bodyweight, listOf(Glutes), listOf(Hamstrings)),
    Exercise("cable_kickback", "Cable Kickback", Cable, listOf(Glutes), listOf(Hamstrings)),
    Exercise("kettlebell_swing", "Kettlebell Swing", Kettlebell, listOf(Glutes, Hamstrings), listOf(LowerBack, Abs)),
    Exercise("standing_calf_raise", "Standing Calf Raise", Machine, listOf(Calves)),
    Exercise("seated_calf_raise", "Seated Calf Raise", Machine, listOf(Calves)),
    Exercise("hip_adduction_machine", "Hip Adduction Machine", Machine, listOf(Adductors)),
    Exercise("copenhagen_plank", "Copenhagen Plank", Bodyweight, listOf(Adductors), listOf(Obliques), isTimed = true),
    Exercise("hip_abduction_machine", "Hip Abduction Machine", Machine, listOf(Abductors), listOf(Glutes)),
    Exercise("banded_lateral_walk", "Banded Lateral Walk", Band, listOf(Abductors), listOf(Glutes)),
)
