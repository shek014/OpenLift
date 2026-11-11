package io.github.shek014.lifttrail.data

import io.github.shek014.lifttrail.data.model.Equipment
import io.github.shek014.lifttrail.data.model.Equipment.Band
import io.github.shek014.lifttrail.data.model.Equipment.Barbell
import io.github.shek014.lifttrail.data.model.Equipment.Bodyweight
import io.github.shek014.lifttrail.data.model.Equipment.Cable
import io.github.shek014.lifttrail.data.model.Equipment.Dumbbell
import io.github.shek014.lifttrail.data.model.Equipment.EzBar
import io.github.shek014.lifttrail.data.model.Equipment.Kettlebell
import io.github.shek014.lifttrail.data.model.Equipment.Machine
import io.github.shek014.lifttrail.data.model.Equipment.SmithMachine
import io.github.shek014.lifttrail.data.model.Equipment.Weighted
import io.github.shek014.lifttrail.data.model.Exercise
import io.github.shek014.lifttrail.data.model.Muscle
import io.github.shek014.lifttrail.data.model.Muscle.Abductors
import io.github.shek014.lifttrail.data.model.Muscle.Abs
import io.github.shek014.lifttrail.data.model.Muscle.Adductors
import io.github.shek014.lifttrail.data.model.Muscle.Biceps
import io.github.shek014.lifttrail.data.model.Muscle.Calves
import io.github.shek014.lifttrail.data.model.Muscle.Forearms
import io.github.shek014.lifttrail.data.model.Muscle.FrontDelts
import io.github.shek014.lifttrail.data.model.Muscle.Glutes
import io.github.shek014.lifttrail.data.model.Muscle.Hamstrings
import io.github.shek014.lifttrail.data.model.Muscle.Lats
import io.github.shek014.lifttrail.data.model.Muscle.LowerBack
import io.github.shek014.lifttrail.data.model.Muscle.LowerChest
import io.github.shek014.lifttrail.data.model.Muscle.MidBack
import io.github.shek014.lifttrail.data.model.Muscle.MidChest
import io.github.shek014.lifttrail.data.model.Muscle.Obliques
import io.github.shek014.lifttrail.data.model.Muscle.Quads
import io.github.shek014.lifttrail.data.model.Muscle.RearDelts
import io.github.shek014.lifttrail.data.model.Muscle.SideDelts
import io.github.shek014.lifttrail.data.model.Muscle.Traps
import io.github.shek014.lifttrail.data.model.Muscle.Triceps
import io.github.shek014.lifttrail.data.model.Muscle.UpperChest

/**
 * A version of a movement with different equipment, a different attachment or a different grip.
 *
 * @property drawing the exercise whose animation shows this one closely enough to borrow, if any.
 */
class ExerciseVariation(val exercise: Exercise, val steps: List<String>, val drawing: String? = null)

private fun variation(
    id: String,
    name: String,
    equipment: Equipment,
    primary: List<Muscle>,
    secondary: List<Muscle>,
    drawing: String?,
    vararg steps: String,
) = ExerciseVariation(Exercise(id, name, equipment, primary, secondary), steps.toList(), drawing)

/** A hold measured by time, like a plank, rather than by reps. */
private fun timedVariation(
    id: String,
    name: String,
    equipment: Equipment,
    primary: List<Muscle>,
    secondary: List<Muscle>,
    drawing: String?,
    vararg steps: String,
) = ExerciseVariation(Exercise(id, name, equipment, primary, secondary, isTimed = true), steps.toList(), drawing)

private fun pushdown(id: String, name: String, equipment: Equipment, drawing: String?, setup: String, press: String) =
    variation(
        id, name, equipment, listOf(Triceps), emptyList(), drawing,
        setup,
        "Tuck your elbows against your sides and lean forward very slightly.",
        press,
        "Let your hands rise slowly to about chest height, keeping your upper arms still.",
    )

private fun bicepCurl(id: String, name: String, equipment: Equipment, drawing: String?, setup: String) =
    variation(
        id, name, equipment, listOf(Biceps), listOf(Forearms), drawing,
        setup,
        "Keep your elbows close to your sides and curl your hands toward your shoulders.",
        "Squeeze your biceps at the top, then lower slowly until your arms are straight.",
    )

private fun calfRaise(id: String, name: String, equipment: Equipment, drawing: String?, setup: String) =
    variation(
        id, name, equipment, listOf(Calves), emptyList(), drawing,
        setup,
        "Rise as high as you can onto the balls of your feet and pause.",
        "Lower your heels slowly until you feel a stretch in your calves.",
    )

private val ChestVariations = listOf(
    variation(
        "cable_fly_crossover_mid", "Cable Fly Crossover (Mid)", Cable, listOf(MidChest), listOf(FrontDelts), "cable_crossover",
        "Set both pulleys at about chest height, take a handle in each hand and step forward between them.",
        "With a slight bend in your elbows, bring your hands together in front of your chest.",
        "Let your arms open slowly until you feel a stretch in your chest.",
    ),
    variation(
        "single_arm_cable_fly", "Single-Arm Cable Fly", Cable, listOf(MidChest), listOf(FrontDelts), "cable_crossover",
        "Set a pulley at about chest height and stand side-on to it, holding the handle in the nearer hand.",
        "With a slight bend in your elbow, bring your hand across to the middle of your chest.",
        "Let your arm open slowly, then finish your reps and switch sides.",
    ),
    variation(
        "incline_cable_fly", "Incline Cable Fly", Cable, listOf(UpperChest), listOf(FrontDelts), null,
        "Place an incline bench between two low pulleys and lie back with a handle in each hand.",
        "With a slight bend in your elbows, bring your hands together above your upper chest.",
        "Lower your arms slowly until you feel a stretch in your chest.",
    ),
    variation(
        "decline_dumbbell_fly", "Decline Dumbbell Fly", Dumbbell, listOf(LowerChest), listOf(FrontDelts), null,
        "Lie on a decline bench with the dumbbells above your chest and your palms facing each other.",
        "With a slight bend in your elbows, lower your arms out to the sides until you feel a stretch.",
        "Bring the dumbbells back together above your chest.",
    ),
    variation(
        "decline_dumbbell_bench_press", "Decline Dumbbell Bench Press", Dumbbell, listOf(LowerChest), listOf(Triceps), null,
        "Secure your legs on a decline bench and hold the dumbbells beside your lower chest.",
        "Press the dumbbells up until your arms are straight.",
        "Lower them with control until you feel a stretch in your chest.",
    ),
    variation(
        "single_arm_dumbbell_bench_press", "Single-Arm Dumbbell Bench Press", Dumbbell, listOf(MidChest), listOf(FrontDelts, Triceps, Abs), "dumbbell_bench_press",
        "Lie on a flat bench holding one dumbbell beside your chest, with your feet flat on the floor.",
        "Press it up until your arm is straight, without letting your body twist.",
        "Lower it with control, then finish your reps and switch sides.",
    ),
    variation(
        "incline_smith_machine_bench_press", "Incline Smith Machine Bench Press", SmithMachine, listOf(UpperChest), listOf(FrontDelts, Triceps), "incline_barbell_bench_press",
        "Place an incline bench in the Smith machine so that the bar lines up with your upper chest.",
        "Twist the bar to unhook it, then lower it with control to your upper chest.",
        "Press it back up, then twist it to hook it back on when you finish.",
    ),
    variation(
        "decline_smith_machine_bench_press", "Decline Smith Machine Bench Press", SmithMachine, listOf(LowerChest), listOf(Triceps), "decline_barbell_bench_press",
        "Place a decline bench in the Smith machine so that the bar lines up with your lower chest.",
        "Twist the bar to unhook it, then lower it with control to your lower chest.",
        "Press it back up, then twist it to hook it back on when you finish.",
    ),
    variation(
        "incline_machine_chest_press", "Incline Machine Chest Press", Machine, listOf(UpperChest), listOf(FrontDelts, Triceps), null,
        "Set the seat so that the handles line up with your upper chest, and sit with your back against the pad.",
        "Press the handles up and away until your arms are straight.",
        "Let them return slowly until you feel a stretch in your chest.",
    ),
    variation(
        "barbell_floor_press", "Barbell Floor Press", Barbell, listOf(MidChest), listOf(Triceps, FrontDelts), null,
        "Lie on the floor under a bar set low in a rack, with your knees bent.",
        "Lower the bar until your upper arms rest on the floor.",
        "Press the bar back up until your arms are straight.",
    ),
    variation(
        "dumbbell_floor_press", "Dumbbell Floor Press", Dumbbell, listOf(MidChest), listOf(Triceps, FrontDelts), null,
        "Lie on the floor with your knees bent and a dumbbell in each hand above your chest.",
        "Lower the dumbbells until your upper arms rest on the floor.",
        "Press them back up until your arms are straight.",
    ),
    variation(
        "kettlebell_floor_press", "Kettlebell Floor Press", Kettlebell, listOf(MidChest), listOf(Triceps, FrontDelts), null,
        "Lie on the floor with your knees bent, holding a kettlebell above your chest with the bell behind your wrist.",
        "Lower it until your upper arm rests on the floor.",
        "Press it back up, then finish your reps and switch sides.",
    ),
    variation(
        "cable_chest_press", "Cable Chest Press", Cable, listOf(MidChest), listOf(FrontDelts, Triceps), null,
        "Set both pulleys at about chest height, take a handle in each hand and step forward.",
        "Press the handles forward until your arms are straight.",
        "Let your hands return slowly to beside your chest.",
    ),
    variation(
        "band_chest_press", "Band Chest Press", Band, listOf(MidChest), listOf(FrontDelts, Triceps), null,
        "Anchor a band behind you at chest height and hold an end in each hand.",
        "Press your hands forward until your arms are straight.",
        "Let your hands return slowly to beside your chest.",
    ),
    variation(
        "incline_push_up", "Incline Push-Up", Bodyweight, listOf(LowerChest), listOf(FrontDelts, Triceps), null,
        "Place your hands on a bench or box and walk your feet back until your body is straight.",
        "Lower your chest to the bench, keeping your elbows at about 45 degrees to your body.",
        "Press back up until your arms are straight.",
    ),
    variation(
        "decline_push_up", "Decline Push-Up", Bodyweight, listOf(UpperChest), listOf(FrontDelts, Triceps, Abs), null,
        "Place your feet on a bench or box and your hands on the floor, with your body straight.",
        "Lower your chest toward the floor.",
        "Press back up until your arms are straight.",
    ),
    variation(
        "wide_push_up", "Wide Push-Up", Bodyweight, listOf(MidChest), listOf(FrontDelts, Triceps, Abs), "push_up",
        "Start in a push-up position with your hands wider than your shoulders.",
        "Lower your chest toward the floor, keeping your body straight.",
        "Press back up until your arms are straight.",
    ),
    variation(
        "band_push_up", "Band Push-Up", Band, listOf(MidChest), listOf(FrontDelts, Triceps, Abs), "push_up",
        "Loop a band across your upper back and hold an end under each hand in a push-up position.",
        "Lower your chest toward the floor, keeping your body straight.",
        "Press back up against the band until your arms are straight.",
    ),
    variation(
        "weighted_chest_dip", "Chest Dip (Weighted)", Weighted, listOf(LowerChest), listOf(Triceps, FrontDelts), "chest_dip",
        "Add weight with a dip belt, then hold yourself up on the bars with your arms straight.",
        "Lean forward and lower yourself until you feel a stretch in your chest.",
        "Press back up until your arms are straight.",
    ),
    variation(
        "assisted_chest_dip", "Assisted Chest Dip", Machine, listOf(LowerChest), listOf(Triceps, FrontDelts), "chest_dip",
        "Set the assistance weight, kneel on the pad and hold the bars with your arms straight.",
        "Lean forward and lower yourself until you feel a stretch in your chest.",
        "Press back up until your arms are straight.",
    ),
)

private val BackVariations = listOf(
    variation(
        "wide_grip_pull_up", "Wide-Grip Pull-Up", Bodyweight, listOf(Lats), listOf(Biceps, MidBack), "pull_up",
        "Hang from the bar with your hands well wider than your shoulders.",
        "Pull yourself up until your chin clears the bar, driving your elbows down.",
        "Lower yourself slowly until your arms are straight.",
    ),
    variation(
        "neutral_grip_pull_up", "Neutral-Grip Pull-Up", Bodyweight, listOf(Lats), listOf(Biceps, MidBack), "pull_up",
        "Hang from parallel handles with your palms facing each other.",
        "Pull yourself up until your chin clears your hands.",
        "Lower yourself slowly until your arms are straight.",
    ),
    variation(
        "weighted_pull_up", "Pull-Up (Weighted)", Weighted, listOf(Lats), listOf(Biceps, MidBack), "pull_up",
        "Add weight with a dip belt, then hang from the bar with your hands wider than your shoulders.",
        "Pull yourself up until your chin clears the bar.",
        "Lower yourself slowly until your arms are straight.",
    ),
    variation(
        "assisted_pull_up", "Assisted Pull-Up", Machine, listOf(Lats), listOf(Biceps, MidBack), "pull_up",
        "Set the assistance weight, kneel on the pad and hold the handles wider than your shoulders.",
        "Pull yourself up until your chin clears your hands.",
        "Lower yourself slowly until your arms are straight.",
    ),
    variation(
        "band_assisted_pull_up", "Band-Assisted Pull-Up", Band, listOf(Lats), listOf(Biceps, MidBack), "pull_up",
        "Loop a band over the bar and put one foot or knee in it, then hang with your arms straight.",
        "Pull yourself up until your chin clears the bar.",
        "Lower yourself slowly until your arms are straight.",
    ),
    variation(
        "weighted_chin_up", "Chin-Up (Weighted)", Weighted, listOf(Lats, Biceps), listOf(MidBack), "chin_up",
        "Add weight with a dip belt, then hang from the bar with your palms facing you.",
        "Pull yourself up until your chin clears the bar.",
        "Lower yourself slowly until your arms are straight.",
    ),
    variation(
        "assisted_chin_up", "Assisted Chin-Up", Machine, listOf(Lats, Biceps), listOf(MidBack), "chin_up",
        "Set the assistance weight, kneel on the pad and hold the handles with your palms facing you.",
        "Pull yourself up until your chin clears your hands.",
        "Lower yourself slowly until your arms are straight.",
    ),
    variation(
        "lat_pulldown_neutral_grip", "Lat Pulldown (Neutral Grip)", Cable, listOf(Lats), listOf(Biceps, MidBack), "close_grip_lat_pulldown",
        "Attach a wide neutral-grip bar, sit with your thighs under the pad and hold it with your palms facing each other.",
        "Pull the bar down to your upper chest, driving your elbows down and back.",
        "Let the bar rise slowly until your arms are straight.",
    ),
    variation(
        "lat_pulldown_reverse_grip", "Lat Pulldown (Reverse Grip)", Cable, listOf(Lats), listOf(Biceps, MidBack), "lat_pulldown",
        "Sit with your thighs under the pad and hold the bar at shoulder width with your palms facing you.",
        "Pull the bar down to your upper chest, keeping your elbows close to your sides.",
        "Let the bar rise slowly until your arms are straight.",
    ),
    variation(
        "single_arm_lat_pulldown", "Single-Arm Lat Pulldown", Cable, listOf(Lats), listOf(Biceps, MidBack), null,
        "Attach a handle to a high pulley and sit with your thighs under the pad, holding it in one hand.",
        "Pull your elbow down to your side.",
        "Let your arm rise slowly until it's straight, then finish your reps and switch sides.",
    ),
    variation(
        "machine_lat_pulldown", "Machine Lat Pulldown", Machine, listOf(Lats), listOf(Biceps, MidBack), null,
        "Set the seat so that your thighs fit under the pad, and hold the handles above you.",
        "Pull the handles down to your shoulders, driving your elbows down and back.",
        "Let them rise slowly until your arms are straight.",
    ),
    variation(
        "band_lat_pulldown", "Band Lat Pulldown", Band, listOf(Lats), listOf(Biceps, MidBack), null,
        "Anchor a band high up, kneel in front of it and hold an end in each hand.",
        "Pull your elbows down to your sides.",
        "Let your arms rise slowly until they're straight.",
    ),
    variation(
        "straight_arm_pulldown_rope", "Straight-Arm Pulldown (Rope)", Cable, listOf(Lats), listOf(Triceps), "straight_arm_pulldown",
        "Attach a rope to a high pulley, step back and hinge forward slightly with your arms almost straight.",
        "Pull the rope down in an arc to your thighs, spreading the ends apart at the bottom.",
        "Let your arms rise slowly until you feel a stretch in your lats.",
    ),
    variation(
        "straight_arm_pulldown_band", "Straight-Arm Pulldown (Band)", Band, listOf(Lats), listOf(Triceps), null,
        "Anchor a band high up, step back and hinge forward slightly with your arms almost straight.",
        "Pull the band down in an arc to your thighs.",
        "Let your arms rise slowly until you feel a stretch in your lats.",
    ),
    variation(
        "underhand_barbell_row", "Underhand Barbell Row", Barbell, listOf(Lats, MidBack), listOf(Biceps, RearDelts, LowerBack), null,
        "Hinge forward with your back flat and hold the bar with your palms facing away from your shins.",
        "Pull the bar to your lower stomach, keeping your elbows close to your sides.",
        "Lower it with control until your arms are straight.",
    ),
    variation(
        "pendlay_row", "Pendlay Row", Barbell, listOf(MidBack, Lats), listOf(RearDelts, Biceps, LowerBack), null,
        "Stand over a bar on the floor and hinge until your back is almost level with the floor.",
        "Pull the bar explosively to your stomach without lifting your chest.",
        "Lower it back to the floor and let it stop before the next rep.",
    ),
    variation(
        "smith_machine_row", "Smith Machine Row", SmithMachine, listOf(MidBack, Lats), listOf(RearDelts, Biceps), null,
        "Set the bar at about knee height and hinge forward under it with your back flat.",
        "Pull the bar to your lower stomach, squeezing your shoulder blades together.",
        "Lower it with control until your arms are straight.",
    ),
    variation(
        "chest_supported_dumbbell_row", "Chest-Supported Dumbbell Row", Dumbbell, listOf(MidBack), listOf(Lats, RearDelts, Biceps), null,
        "Set a bench to a low incline and lie face down on it with a dumbbell in each hand.",
        "Pull the dumbbells up beside your ribs, squeezing your shoulder blades together.",
        "Lower them slowly until your arms are straight.",
    ),
    variation(
        "kettlebell_row", "Kettlebell Row", Kettlebell, listOf(Lats), listOf(MidBack, RearDelts, Biceps), null,
        "Hinge forward with one hand on a bench and a kettlebell in the other.",
        "Pull the kettlebell up beside your ribs.",
        "Lower it slowly, then finish your reps and switch sides.",
    ),
    variation(
        "machine_t_bar_row", "T-Bar Row (Machine)", Machine, listOf(MidBack, Lats), listOf(RearDelts, Biceps), "t_bar_row",
        "Stand on the platform, rest your chest on the pad if there is one, and hold the handles.",
        "Pull the handles toward your chest, squeezing your shoulder blades together.",
        "Lower them slowly until your arms are straight.",
    ),
    variation(
        "seated_cable_row_wide_grip", "Seated Cable Row (Wide Grip)", Cable, listOf(MidBack), listOf(Lats, RearDelts, Biceps), "seated_cable_row",
        "Attach a long bar, sit with your knees slightly bent and hold the bar wider than your shoulders.",
        "Pull the bar to your upper stomach, with your elbows flaring out to the sides.",
        "Let your arms straighten slowly without rounding your back.",
    ),
    variation(
        "seated_cable_row_rope", "Seated Cable Row (Rope)", Cable, listOf(MidBack, Lats), listOf(RearDelts, Biceps), "seated_cable_row",
        "Attach a rope, sit with your knees slightly bent and hold an end in each hand.",
        "Pull the rope to your stomach, spreading the ends apart as they reach you.",
        "Let your arms straighten slowly without rounding your back.",
    ),
    variation(
        "single_arm_cable_row", "Single-Arm Cable Row", Cable, listOf(Lats), listOf(MidBack, RearDelts, Biceps), null,
        "Attach a handle at about waist height and hold it in one hand with your arm straight.",
        "Pull your elbow back past your ribs without twisting your body.",
        "Let your arm straighten slowly, then finish your reps and switch sides.",
    ),
    variation(
        "band_row", "Band Row", Band, listOf(MidBack, Lats), listOf(RearDelts, Biceps), null,
        "Anchor a band in front of you at waist height and hold an end in each hand.",
        "Pull your hands to your ribs, squeezing your shoulder blades together.",
        "Let your arms straighten slowly.",
    ),
    variation(
        "inverted_row", "Inverted Row", Bodyweight, listOf(MidBack, Lats), listOf(RearDelts, Biceps), null,
        "Lie under a bar set at about waist height and hold it, with your body straight and your heels on the floor.",
        "Pull your chest up to the bar.",
        "Lower yourself slowly until your arms are straight.",
    ),
    variation(
        "smith_machine_shrug", "Smith Machine Shrug", SmithMachine, listOf(Traps), listOf(Forearms), "barbell_shrug",
        "Stand with the bar at arm's length in front of your thighs and twist it to unhook it.",
        "Lift your shoulders straight up toward your ears and pause.",
        "Lower them slowly, without rolling your shoulders.",
    ),
    variation(
        "cable_shrug", "Cable Shrug", Cable, listOf(Traps), listOf(Forearms), null,
        "Attach a bar to a low pulley and stand close to it with your arms straight.",
        "Lift your shoulders straight up toward your ears and pause.",
        "Lower them slowly, without rolling your shoulders.",
    ),
    variation(
        "machine_shrug", "Machine Shrug", Machine, listOf(Traps), listOf(Forearms), null,
        "Stand or sit in the machine and hold the handles with your arms straight.",
        "Lift your shoulders straight up toward your ears and pause.",
        "Lower them slowly, without rolling your shoulders.",
    ),
    variation(
        "trap_bar_shrug", "Trap Bar Shrug", Barbell, listOf(Traps), listOf(Forearms), null,
        "Stand inside a trap bar and lift it with your arms straight.",
        "Lift your shoulders straight up toward your ears and pause.",
        "Lower them slowly, without rolling your shoulders.",
    ),
    variation(
        "trap_bar_deadlift", "Trap Bar Deadlift", Barbell, listOf(Glutes, Quads, Hamstrings), listOf(LowerBack, Traps, Forearms), null,
        "Stand inside a trap bar, hinge down and hold the handles with your back flat.",
        "Drive through your feet and stand up tall.",
        "Lower the bar with control by pushing your hips back and bending your knees.",
    ),
    variation(
        "dumbbell_deadlift", "Dumbbell Deadlift", Dumbbell, listOf(Glutes, Hamstrings), listOf(LowerBack, Quads, Forearms), null,
        "Stand with a dumbbell on the floor beside each foot, then hinge down and hold them with your back flat.",
        "Drive through your feet and stand up tall.",
        "Lower the dumbbells with control by pushing your hips back.",
    ),
    variation(
        "kettlebell_deadlift", "Kettlebell Deadlift", Kettlebell, listOf(Glutes, Hamstrings), listOf(LowerBack, Quads, Forearms), null,
        "Stand with a kettlebell on the floor between your feet, then hinge down and hold the handle with both hands.",
        "Drive through your feet and stand up tall.",
        "Lower the kettlebell with control by pushing your hips back.",
    ),
    variation(
        "smith_machine_deadlift", "Smith Machine Deadlift", SmithMachine, listOf(Glutes, Hamstrings, LowerBack), listOf(Quads, Traps, Forearms), "deadlift",
        "Set the bar at about mid-shin height and stand close to it, then hinge down and hold it with your back flat.",
        "Drive through your feet and stand up tall.",
        "Lower the bar with control by pushing your hips back.",
    ),
    variation(
        "smith_machine_good_morning", "Smith Machine Good Morning", SmithMachine, listOf(Hamstrings, LowerBack), listOf(Glutes), "good_morning",
        "Rest the bar across your upper back, with your feet at shoulder width, and twist it to unhook it.",
        "With your knees slightly bent, push your hips back until your back is nearly level with the floor.",
        "Drive your hips forward to stand up.",
    ),
    variation(
        "band_good_morning", "Band Good Morning", Band, listOf(Hamstrings, LowerBack), listOf(Glutes), null,
        "Stand on a band and loop the other end over your shoulders.",
        "With your knees slightly bent, push your hips back until you feel a stretch in your hamstrings.",
        "Drive your hips forward to stand up.",
    ),
    variation(
        "weighted_back_extension", "Back Extension (Weighted)", Weighted, listOf(LowerBack), listOf(Glutes, Hamstrings), "back_extension",
        "Set up on a back extension bench, holding a dumbbell or plate against your chest.",
        "Lower your upper body until you feel a stretch in your hamstrings.",
        "Raise yourself until your body is straight, without arching further.",
    ),
    variation(
        "machine_back_extension", "Machine Back Extension", Machine, listOf(LowerBack), listOf(Glutes, Hamstrings), null,
        "Sit in the machine with the pad across your upper back and your feet secure.",
        "Push back until your body is nearly straight.",
        "Return slowly to the start.",
    ),
)

private val ShoulderVariations = listOf(
    variation(
        "seated_barbell_press", "Seated Barbell Press", Barbell, listOf(FrontDelts), listOf(SideDelts, Triceps), "overhead_press",
        "Sit on an upright bench in a rack and lift the bar off at about collarbone height.",
        "Press the bar straight up until your arms are straight.",
        "Lower it with control to your collarbones.",
    ),
    variation(
        "smith_machine_shoulder_press", "Smith Machine Shoulder Press", SmithMachine, listOf(FrontDelts), listOf(SideDelts, Triceps), "overhead_press",
        "Place an upright bench in the Smith machine and twist the bar to unhook it at about collarbone height.",
        "Press the bar up until your arms are straight.",
        "Lower it with control to your collarbones.",
    ),
    variation(
        "push_press", "Push Press", Barbell, listOf(FrontDelts), listOf(SideDelts, Triceps, Quads, Traps), "overhead_press",
        "Stand with the bar at your collarbones and your feet at shoulder width.",
        "Dip slightly at the knees, then drive up and use that push to press the bar overhead.",
        "Lower the bar with control to your collarbones.",
    ),
    variation(
        "landmine_press", "Landmine Press", Barbell, listOf(UpperChest, FrontDelts), listOf(Triceps), null,
        "Wedge one end of a bar in a corner or landmine, and hold the other end at your shoulder.",
        "Press the bar up and forward until your arm is straight.",
        "Lower it slowly, then finish your reps and switch sides.",
    ),
    variation(
        "standing_dumbbell_press", "Standing Dumbbell Press", Dumbbell, listOf(FrontDelts), listOf(SideDelts, Triceps, Abs), "dumbbell_shoulder_press",
        "Stand with the dumbbells at your shoulders and your core tight.",
        "Press them up until your arms are straight, without leaning back.",
        "Lower them with control to your shoulders.",
    ),
    variation(
        "single_arm_dumbbell_press", "Single-Arm Dumbbell Press", Dumbbell, listOf(FrontDelts), listOf(SideDelts, Triceps, Obliques), null,
        "Stand holding one dumbbell at your shoulder, with your core tight.",
        "Press it up until your arm is straight, without leaning to the side.",
        "Lower it with control, then finish your reps and switch sides.",
    ),
    variation(
        "kettlebell_press", "Kettlebell Press", Kettlebell, listOf(FrontDelts), listOf(SideDelts, Triceps, Abs), null,
        "Hold a kettlebell at your shoulder with the bell resting behind your wrist.",
        "Press it up until your arm is straight.",
        "Lower it with control, then finish your reps and switch sides.",
    ),
    variation(
        "cable_shoulder_press", "Cable Shoulder Press", Cable, listOf(FrontDelts), listOf(SideDelts, Triceps), null,
        "Set both pulleys low, sit or stand between them and hold the handles at your shoulders.",
        "Press the handles up until your arms are straight.",
        "Lower them with control to your shoulders.",
    ),
    variation(
        "band_shoulder_press", "Band Shoulder Press", Band, listOf(FrontDelts), listOf(SideDelts, Triceps), null,
        "Stand on a band and hold an end in each hand at your shoulders.",
        "Press your hands up until your arms are straight.",
        "Lower them with control to your shoulders.",
    ),
    variation(
        "single_arm_cable_lateral_raise", "Single-Arm Cable Lateral Raise", Cable, listOf(SideDelts), listOf(Traps), null,
        "Stand side-on to a low pulley and hold the handle in the farther hand, across your body.",
        "Raise your arm out to the side until it's level with your shoulder.",
        "Lower it slowly, then finish your reps and switch sides.",
    ),
    variation(
        "band_lateral_raise", "Band Lateral Raise", Band, listOf(SideDelts), listOf(Traps), null,
        "Stand on a band and hold an end in each hand at your sides.",
        "Raise your arms out to the sides until they're level with your shoulders.",
        "Lower them slowly.",
    ),
    variation(
        "kettlebell_lateral_raise", "Kettlebell Lateral Raise", Kettlebell, listOf(SideDelts), listOf(Traps), null,
        "Stand holding a light kettlebell in each hand at your sides.",
        "Raise your arms out to the sides until they're level with your shoulders.",
        "Lower them slowly.",
    ),
    variation(
        "barbell_front_raise", "Barbell Front Raise", Barbell, listOf(FrontDelts), emptyList(), null,
        "Stand holding a light bar against your thighs with your hands at shoulder width.",
        "Raise it in front of you until your arms are level with your shoulders.",
        "Lower it slowly.",
    ),
    variation(
        "cable_front_raise", "Cable Front Raise", Cable, listOf(FrontDelts), emptyList(), null,
        "Face away from a low pulley with the handle or rope between your legs.",
        "Raise your hands in front of you until your arms are level with your shoulders.",
        "Lower them slowly.",
    ),
    variation(
        "band_front_raise", "Band Front Raise", Band, listOf(FrontDelts), emptyList(), null,
        "Stand on a band and hold an end in each hand at your sides.",
        "Raise your hands in front of you until your arms are level with your shoulders.",
        "Lower them slowly.",
    ),
    variation(
        "kettlebell_front_raise", "Kettlebell Front Raise", Kettlebell, listOf(FrontDelts), emptyList(), null,
        "Stand holding a light kettlebell by the handle with both hands.",
        "Raise it in front of you until your arms are level with your shoulders.",
        "Lower it slowly.",
    ),
    variation(
        "dumbbell_upright_row", "Dumbbell Upright Row", Dumbbell, listOf(SideDelts, Traps), listOf(Biceps), null,
        "Stand holding the dumbbells against your thighs.",
        "Pull them up close to your body until your elbows are level with your shoulders.",
        "Lower them slowly.",
    ),
    variation(
        "ez_bar_upright_row", "EZ Bar Upright Row", EzBar, listOf(SideDelts, Traps), listOf(Biceps), "upright_row",
        "Stand holding an EZ bar against your thighs on the angled part of the grip.",
        "Pull it up close to your body until your elbows are level with your shoulders.",
        "Lower it slowly.",
    ),
    variation(
        "cable_upright_row", "Cable Upright Row", Cable, listOf(SideDelts, Traps), listOf(Biceps), null,
        "Attach a bar to a low pulley and stand close to it with the bar against your thighs.",
        "Pull it up close to your body until your elbows are level with your shoulders.",
        "Lower it slowly.",
    ),
    variation(
        "smith_machine_upright_row", "Smith Machine Upright Row", SmithMachine, listOf(SideDelts, Traps), listOf(Biceps), "upright_row",
        "Stand close to the bar with it against your thighs, and twist it to unhook it.",
        "Pull it straight up until your elbows are level with your shoulders.",
        "Lower it slowly.",
    ),
    variation(
        "band_upright_row", "Band Upright Row", Band, listOf(SideDelts, Traps), listOf(Biceps), null,
        "Stand on a band and hold an end in each hand in front of your thighs.",
        "Pull your hands up until your elbows are level with your shoulders.",
        "Lower them slowly.",
    ),
    variation(
        "cable_reverse_fly", "Cable Reverse Fly", Cable, listOf(RearDelts), listOf(MidBack), null,
        "Set both pulleys at shoulder height and hold the opposite handles or cables, with your arms crossed.",
        "With a slight bend in your elbows, open your arms out to the sides.",
        "Let your arms cross back over slowly.",
    ),
    variation(
        "incline_reverse_dumbbell_fly", "Incline Reverse Dumbbell Fly", Dumbbell, listOf(RearDelts), listOf(MidBack), null,
        "Lie face down on an incline bench with a dumbbell in each hand.",
        "With a slight bend in your elbows, raise your arms out to the sides.",
        "Lower them slowly.",
    ),
    variation(
        "band_face_pull", "Band Face Pull", Band, listOf(RearDelts), listOf(MidBack, Traps), null,
        "Anchor a band at face height and hold an end in each hand with your arms straight.",
        "Pull your hands toward your face, with your elbows high and out to the sides.",
        "Let your arms straighten slowly.",
    ),
)

private val BicepsVariations = listOf(
    bicepCurl("cable_curl_ez_bar", "Cable Curl (EZ Bar)", Cable, "cable_curl", "Attach an EZ bar to a low pulley and stand facing it with your arms straight."),
    bicepCurl("cable_hammer_curl", "Cable Hammer Curl (Rope)", Cable, null, "Attach a rope to a low pulley and hold an end in each hand with your palms facing each other."),
    bicepCurl("single_arm_cable_curl", "Single-Arm Cable Curl", Cable, null, "Attach a handle to a low pulley and hold it in one hand with your arm straight."),
    bicepCurl("band_hammer_curl", "Band Hammer Curl", Band, "band_curl", "Stand on a band and hold an end in each hand with your palms facing each other."),
    bicepCurl("kettlebell_curl", "Kettlebell Curl", Kettlebell, null, "Stand holding a kettlebell by the handle with both hands, your arms straight."),
    bicepCurl("machine_curl", "Machine Curl", Machine, null, "Set the seat so that your elbows line up with the machine's pivot, and hold the handles."),
    bicepCurl("cross_body_hammer_curl", "Cross-Body Hammer Curl", Dumbbell, "hammer_curl", "Stand holding the dumbbells at your sides, and curl each one toward the opposite shoulder in turn."),
    bicepCurl("incline_hammer_curl", "Incline Hammer Curl", Dumbbell, "incline_dumbbell_curl", "Sit back on an incline bench with your arms hanging down and your palms facing each other."),
    variation(
        "behind_the_body_cable_curl", "Behind-the-Body Cable Curl", Cable, listOf(Biceps), emptyList(), null,
        "Face away from a low pulley and hold the handle with your arm stretched slightly behind you.",
        "Curl your hand toward your shoulder, keeping your elbow still.",
        "Lower it slowly, then finish your reps and switch sides.",
    ),
    variation(
        "high_cable_curl", "High Cable Curl", Cable, listOf(Biceps), emptyList(), null,
        "Stand between two high pulleys with a handle in each hand and your arms out to the sides.",
        "Curl your hands toward your ears, keeping your upper arms still.",
        "Straighten your arms slowly.",
    ),
    variation(
        "cable_concentration_curl", "Cable Concentration Curl", Cable, listOf(Biceps), emptyList(), "concentration_curl",
        "Sit facing a low pulley, with one elbow braced against the inside of your thigh and the handle in that hand.",
        "Curl the handle toward your shoulder.",
        "Lower it slowly, then finish your reps and switch sides.",
    ),
    variation(
        "barbell_preacher_curl", "Preacher Curl (Barbell)", Barbell, listOf(Biceps), emptyList(), "preacher_curl",
        "Sit at a preacher bench with your upper arms on the pad, and hold a straight bar with your palms up.",
        "Curl the bar up until your forearms are nearly upright.",
        "Lower it slowly until your arms are almost straight.",
    ),
    variation(
        "dumbbell_preacher_curl", "Preacher Curl (Dumbbell)", Dumbbell, listOf(Biceps), emptyList(), "preacher_curl",
        "Sit at a preacher bench with your upper arm on the pad, holding a dumbbell with your palm up.",
        "Curl it up until your forearm is nearly upright.",
        "Lower it slowly, then finish your reps and switch sides.",
    ),
    variation(
        "machine_preacher_curl", "Preacher Curl (Machine)", Machine, listOf(Biceps), emptyList(), null,
        "Set the seat so that your upper arms rest on the pad, and hold the handles.",
        "Curl the handles up until your forearms are nearly upright.",
        "Lower them slowly until your arms are almost straight.",
    ),
    variation(
        "cable_preacher_curl", "Preacher Curl (Cable)", Cable, listOf(Biceps), emptyList(), null,
        "Place a preacher bench in front of a low pulley and rest your upper arms on the pad, holding the bar.",
        "Curl the bar up until your forearms are nearly upright.",
        "Lower it slowly until your arms are almost straight.",
    ),
    variation(
        "dumbbell_spider_curl", "Spider Curl (Dumbbell)", Dumbbell, listOf(Biceps), emptyList(), null,
        "Lie face down on an incline bench with your arms hanging straight down.",
        "Curl the dumbbells up, keeping your upper arms still.",
        "Lower them slowly until your arms are straight.",
    ),
    variation(
        "ez_bar_spider_curl", "Spider Curl (EZ Bar)", EzBar, listOf(Biceps), emptyList(), null,
        "Lie face down on an incline bench, holding an EZ bar with your arms hanging straight down.",
        "Curl the bar up, keeping your upper arms still.",
        "Lower it slowly until your arms are straight.",
    ),
    variation(
        "drag_curl", "Drag Curl", Barbell, listOf(Biceps), listOf(Forearms), null,
        "Stand holding a bar against your thighs with your palms facing away from you.",
        "Drag the bar up your body to your chest, letting your elbows move back.",
        "Slide it slowly back down.",
    ),
    variation(
        "zottman_curl", "Zottman Curl", Dumbbell, listOf(Biceps, Forearms), emptyList(), null,
        "Stand with the dumbbells at your sides and your palms facing forward.",
        "Curl them up, then turn your palms to face down at the top.",
        "Lower them slowly with your palms down, then turn them back at the bottom.",
    ),
)

private val TricepsVariations = listOf(
    pushdown(
        "triceps_pushdown_rope", "Triceps Pushdown (Rope)", Cable, "triceps_pushdown",
        "Attach a rope to a high pulley and hold an end in each hand with your palms facing each other.",
        "Push down until your arms are straight, spreading the ends of the rope apart at the bottom.",
    ),
    pushdown(
        "triceps_pushdown_v_bar", "Triceps Pushdown (V-Bar)", Cable, "triceps_pushdown",
        "Attach a V-shaped bar to a high pulley and hold it with both hands.",
        "Push down until your arms are straight.",
    ),
    pushdown(
        "triceps_pushdown_ez_bar", "Triceps Pushdown (EZ Bar)", Cable, "triceps_pushdown",
        "Attach an EZ bar to a high pulley and hold it on the angled part of the grip.",
        "Push down until your arms are straight.",
    ),
    pushdown(
        "triceps_pushdown_reverse_grip", "Triceps Pushdown (Reverse Grip)", Cable, "triceps_pushdown",
        "Attach a straight bar to a high pulley and hold it with your palms facing up.",
        "Push down until your arms are straight.",
    ),
    pushdown(
        "single_arm_triceps_pushdown", "Triceps Pushdown (Single Arm)", Cable, "triceps_pushdown",
        "Attach a handle to a high pulley and hold it in one hand. Finish your reps, then switch sides.",
        "Push down until your arm is straight.",
    ),
    pushdown(
        "triceps_pushdown_band", "Triceps Pushdown (Band)", Band, null,
        "Anchor a band high up and hold it with both hands.",
        "Push down until your arms are straight.",
    ),
    variation(
        "overhead_cable_triceps_extension_bar", "Overhead Cable Triceps Extension (Straight Bar)", Cable, listOf(Triceps), emptyList(), "overhead_cable_triceps_extension",
        "Attach a straight bar to a low pulley and stand facing away from the machine, holding it behind your head.",
        "Straighten your arms forward and up, keeping your upper arms still.",
        "Bend your elbows slowly to return to the start.",
    ),
    variation(
        "overhead_cable_triceps_extension_single_arm", "Overhead Cable Triceps Extension (Single Arm)", Cable, listOf(Triceps), emptyList(), "overhead_cable_triceps_extension",
        "Attach a handle to a low pulley and stand facing away from the machine, holding it behind your head in one hand.",
        "Straighten your arm up, keeping your upper arm still.",
        "Bend your elbow slowly, then finish your reps and switch sides.",
    ),
    variation(
        "single_arm_overhead_dumbbell_extension", "Overhead Dumbbell Triceps Extension (Single Arm)", Dumbbell, listOf(Triceps), emptyList(), "overhead_dumbbell_triceps_extension",
        "Sit or stand holding a dumbbell straight overhead in one hand.",
        "Lower it behind your head by bending your elbow, keeping your upper arm still.",
        "Straighten your arm, then finish your reps and switch sides.",
    ),
    variation(
        "overhead_ez_bar_triceps_extension", "Overhead EZ Bar Triceps Extension", EzBar, listOf(Triceps), emptyList(), null,
        "Sit or stand holding an EZ bar straight overhead on the angled part of the grip.",
        "Lower it behind your head by bending your elbows, keeping your upper arms still.",
        "Straighten your arms to press it back up.",
    ),
    variation(
        "overhead_barbell_triceps_extension", "Overhead Barbell Triceps Extension", Barbell, listOf(Triceps), emptyList(), null,
        "Sit or stand holding a light bar straight overhead with your hands close together.",
        "Lower it behind your head by bending your elbows, keeping your upper arms still.",
        "Straighten your arms to press it back up.",
    ),
    variation(
        "overhead_band_triceps_extension", "Overhead Band Triceps Extension", Band, listOf(Triceps), emptyList(), null,
        "Stand on a band with one foot back and hold it behind your head with your elbows bent.",
        "Straighten your arms up, keeping your upper arms still.",
        "Bend your elbows slowly to return to the start.",
    ),
    variation(
        "machine_triceps_extension", "Machine Triceps Extension", Machine, listOf(Triceps), emptyList(), null,
        "Set the seat so that your elbows line up with the machine's pivot, and hold the handles.",
        "Push the handles down until your arms are straight.",
        "Let them return slowly.",
    ),
    variation(
        "cable_triceps_kickback", "Cable Triceps Kickback", Cable, listOf(Triceps), emptyList(), null,
        "Attach a handle to a low pulley, hinge forward and hold it with your upper arm level with your back.",
        "Straighten your arm behind you, keeping your upper arm still.",
        "Bend your elbow slowly, then finish your reps and switch sides.",
    ),
    variation(
        "band_triceps_kickback", "Band Triceps Kickback", Band, listOf(Triceps), emptyList(), null,
        "Stand on a band, hinge forward and hold it with your upper arm level with your back.",
        "Straighten your arm behind you, keeping your upper arm still.",
        "Bend your elbow slowly, then finish your reps and switch sides.",
    ),
    variation(
        "barbell_skull_crusher", "Skull Crusher (Barbell)", Barbell, listOf(Triceps), emptyList(), "skull_crusher",
        "Lie on a flat bench holding a straight bar above your chest with your hands close together.",
        "Bend your elbows to lower the bar toward your forehead, keeping your upper arms still.",
        "Straighten your arms to press it back up.",
    ),
    variation(
        "dumbbell_skull_crusher", "Skull Crusher (Dumbbell)", Dumbbell, listOf(Triceps), emptyList(), null,
        "Lie on a flat bench holding the dumbbells above your chest with your palms facing each other.",
        "Bend your elbows to lower them beside your head, keeping your upper arms still.",
        "Straighten your arms to press them back up.",
    ),
    variation(
        "cable_skull_crusher", "Skull Crusher (Cable)", Cable, listOf(Triceps), emptyList(), null,
        "Lie on a flat bench with your head toward a low pulley, holding a bar or rope above your chest.",
        "Bend your elbows to lower your hands toward your forehead, keeping your upper arms still.",
        "Straighten your arms to press back up.",
    ),
    variation(
        "jm_press", "JM Press", Barbell, listOf(Triceps), listOf(MidChest, FrontDelts), null,
        "Lie on a flat bench holding a bar above your chest with your hands at shoulder width.",
        "Lower the bar toward your upper chest and chin, letting your elbows bend and move forward.",
        "Press it back up until your arms are straight.",
    ),
    variation(
        "close_grip_smith_machine_bench_press", "Close-Grip Smith Machine Bench Press", SmithMachine, listOf(Triceps), listOf(MidChest, FrontDelts), "close_grip_bench_press",
        "Place a flat bench in the Smith machine and grip the bar at about shoulder width.",
        "Twist the bar to unhook it, then lower it to your lower chest with your elbows close to your sides.",
        "Press it back up until your arms are straight.",
    ),
    variation(
        "close_grip_ez_bar_press", "Close-Grip EZ Bar Press", EzBar, listOf(Triceps), listOf(MidChest, FrontDelts), null,
        "Lie on a flat bench holding an EZ bar above your chest on the inner part of the grip.",
        "Lower it to your lower chest, with your elbows close to your sides.",
        "Press it back up until your arms are straight.",
    ),
    variation(
        "close_grip_dumbbell_press", "Close-Grip Dumbbell Press", Dumbbell, listOf(Triceps), listOf(MidChest, FrontDelts), null,
        "Lie on a flat bench with the dumbbells touching above your chest and your palms facing each other.",
        "Lower them to your chest, with your elbows close to your sides.",
        "Press them back up until your arms are straight.",
    ),
    variation(
        "tate_press", "Tate Press", Dumbbell, listOf(Triceps), emptyList(), null,
        "Lie on a flat bench with the dumbbells above your chest and your palms facing your feet.",
        "Bend your elbows out to the sides to lower the dumbbells to your chest.",
        "Straighten your arms to press them back up.",
    ),
    variation(
        "triceps_dip", "Triceps Dip", Bodyweight, listOf(Triceps), listOf(LowerChest, FrontDelts), "chest_dip",
        "Hold yourself up on the bars with your arms straight and your body upright.",
        "Bend your elbows to lower yourself, keeping them close to your sides.",
        "Press back up until your arms are straight.",
    ),
    variation(
        "weighted_triceps_dip", "Triceps Dip (Weighted)", Weighted, listOf(Triceps), listOf(LowerChest, FrontDelts), "chest_dip",
        "Add weight with a dip belt, then hold yourself up on the bars with your body upright.",
        "Bend your elbows to lower yourself, keeping them close to your sides.",
        "Press back up until your arms are straight.",
    ),
    variation(
        "assisted_triceps_dip", "Assisted Triceps Dip", Machine, listOf(Triceps), listOf(LowerChest, FrontDelts), "chest_dip",
        "Set the assistance weight, kneel on the pad and hold the bars with your body upright.",
        "Bend your elbows to lower yourself, keeping them close to your sides.",
        "Press back up until your arms are straight.",
    ),
    variation(
        "machine_triceps_dip", "Machine Triceps Dip", Machine, listOf(Triceps), listOf(LowerChest, FrontDelts), null,
        "Sit in the machine with your back against the pad, and hold the handles at your sides.",
        "Push the handles down until your arms are straight.",
        "Let them rise slowly.",
    ),
    variation(
        "diamond_push_up", "Diamond Push-Up", Bodyweight, listOf(Triceps), listOf(MidChest, FrontDelts, Abs), null,
        "Start in a push-up position with your hands together under your chest, your thumbs and fingers forming a diamond.",
        "Lower your chest to your hands, keeping your elbows close to your sides.",
        "Press back up until your arms are straight.",
    ),
)

private val ForearmVariations = listOf(
    variation(
        "barbell_wrist_curl", "Wrist Curl (Barbell)", Barbell, listOf(Forearms), emptyList(), null,
        "Sit with your forearms on your thighs, holding a bar with your palms up.",
        "Let the bar roll down toward your fingers, then curl your wrists up.",
        "Lower it slowly.",
    ),
    variation(
        "cable_wrist_curl", "Wrist Curl (Cable)", Cable, listOf(Forearms), emptyList(), null,
        "Attach a bar to a low pulley and sit or kneel facing it, with your forearms on your thighs and your palms up.",
        "Curl your wrists up.",
        "Lower them slowly.",
    ),
    variation(
        "barbell_reverse_wrist_curl", "Reverse Wrist Curl (Barbell)", Barbell, listOf(Forearms), emptyList(), null,
        "Sit with your forearms on your thighs, holding a bar with your palms down.",
        "Lift the backs of your hands up as far as you can.",
        "Lower them slowly.",
    ),
    variation(
        "barbell_reverse_curl", "Reverse Curl (Barbell)", Barbell, listOf(Forearms), listOf(Biceps), null,
        "Stand holding a straight bar with your palms facing down.",
        "Curl it toward your shoulders, keeping your elbows at your sides.",
        "Lower it slowly until your arms are straight.",
    ),
    variation(
        "dumbbell_reverse_curl", "Reverse Curl (Dumbbell)", Dumbbell, listOf(Forearms), listOf(Biceps), null,
        "Stand holding the dumbbells with your palms facing down.",
        "Curl them toward your shoulders, keeping your elbows at your sides.",
        "Lower them slowly until your arms are straight.",
    ),
    variation(
        "cable_reverse_curl", "Reverse Curl (Cable)", Cable, listOf(Forearms), listOf(Biceps), null,
        "Attach a bar to a low pulley and hold it with your palms facing down.",
        "Curl it toward your shoulders, keeping your elbows at your sides.",
        "Lower it slowly until your arms are straight.",
    ),
)

private val CoreVariations = listOf(
    variation(
        "weighted_crunch", "Crunch (Weighted)", Weighted, listOf(Abs), emptyList(), "crunch",
        "Lie on your back with your knees bent, holding a dumbbell or plate against your chest.",
        "Curl your shoulders up off the floor by tightening your abs.",
        "Lower yourself slowly.",
    ),
    variation(
        "decline_crunch", "Decline Crunch", Bodyweight, listOf(Abs), emptyList(), null,
        "Secure your legs on a decline bench and lie back.",
        "Curl your upper body toward your knees by tightening your abs.",
        "Lower yourself slowly.",
    ),
    variation(
        "hanging_knee_raise", "Hanging Knee Raise", Bodyweight, listOf(Abs), listOf(Obliques, Forearms), null,
        "Hang from a bar with your arms straight.",
        "Bring your knees up toward your chest, curling your hips forward.",
        "Lower your legs slowly without swinging.",
    ),
    variation(
        "captains_chair_leg_raise", "Captain's Chair Leg Raise", Machine, listOf(Abs), listOf(Obliques), null,
        "Rest your forearms on the pads with your back against the support and your legs hanging.",
        "Raise your knees toward your chest, curling your hips forward.",
        "Lower your legs slowly.",
    ),
    variation(
        "ab_wheel_rollout", "Ab Wheel Rollout", Bodyweight, listOf(Abs), listOf(Obliques, Lats), null,
        "Kneel and hold an ab wheel on the floor under your shoulders.",
        "Roll forward slowly, keeping your back from sagging, as far as you can control.",
        "Pull back to the start using your abs.",
    ),
    variation(
        "mountain_climber", "Mountain Climber", Bodyweight, listOf(Abs), listOf(Obliques, FrontDelts, Quads), null,
        "Start in a push-up position with your body straight.",
        "Drive one knee toward your chest, then switch legs quickly.",
        "Keep your hips low and keep alternating.",
    ),
    variation(
        "v_up", "V-Up", Bodyweight, listOf(Abs), listOf(Obliques), null,
        "Lie on your back with your arms and legs straight.",
        "Raise your arms and legs together until your hands reach toward your feet.",
        "Lower yourself slowly.",
    ),
    variation(
        "high_to_low_cable_woodchop", "Cable Woodchop (High to Low)", Cable, listOf(Obliques), listOf(Abs), null,
        "Set a pulley high and stand side-on to it, holding the handle with both hands.",
        "Pull the handle down and across your body to your opposite hip, turning your torso.",
        "Return slowly, then finish your reps and switch sides.",
    ),
    variation(
        "dumbbell_side_bend", "Side Bend (Dumbbell)", Dumbbell, listOf(Obliques), emptyList(), null,
        "Stand holding a dumbbell at your side.",
        "Lean toward the dumbbell, then use your opposite side to pull yourself upright.",
        "Finish your reps, then switch sides.",
    ),
    variation(
        "cable_side_bend", "Side Bend (Cable)", Cable, listOf(Obliques), emptyList(), null,
        "Stand side-on to a low pulley, holding the handle at your side.",
        "Lean toward the pulley, then use your opposite side to pull yourself upright.",
        "Finish your reps, then switch sides.",
    ),
    variation(
        "weighted_russian_twist", "Russian Twist (Weighted)", Weighted, listOf(Obliques), listOf(Abs), null,
        "Sit leaning back slightly with your knees bent, holding a dumbbell or plate at your chest.",
        "Turn your torso to one side, then the other.",
        "Keep your back straight and keep alternating.",
    ),
    variation(
        "landmine_twist", "Landmine Twist", Barbell, listOf(Obliques), listOf(Abs, FrontDelts), null,
        "Wedge one end of a bar in a corner or landmine, and hold the other end at arm's length in front of you.",
        "Swing the bar in an arc to one hip, turning your torso.",
        "Swing it across to the other hip and keep alternating.",
    ),
    variation(
        "band_pallof_press", "Pallof Press (Band)", Band, listOf(Obliques), listOf(Abs), null,
        "Anchor a band at chest height and stand side-on to it, holding it against your chest.",
        "Press your hands straight out, without letting your body turn toward the anchor.",
        "Bring your hands back slowly, then finish your reps and switch sides.",
    ),
)

private val LegVariations = listOf(
    variation(
        "bodyweight_squat", "Bodyweight Squat", Bodyweight, listOf(Quads, Glutes), listOf(Hamstrings), null,
        "Stand with your feet at about shoulder width.",
        "Bend your knees and push your hips back until your thighs are level with the floor.",
        "Stand back up through your whole foot.",
    ),
    variation(
        "box_squat", "Box Squat", Barbell, listOf(Quads, Glutes), listOf(Hamstrings, LowerBack), "barbell_back_squat",
        "Set a box behind you and rest the bar across your upper back.",
        "Sit back onto the box with control, without relaxing.",
        "Drive through your feet to stand up.",
    ),
    variation(
        "dumbbell_goblet_squat", "Goblet Squat (Dumbbell)", Dumbbell, listOf(Quads, Glutes), listOf(Abs), null,
        "Hold one end of a dumbbell against your chest, with your feet at about shoulder width.",
        "Squat down between your knees, keeping your chest up.",
        "Stand back up through your whole foot.",
    ),
    variation(
        "dumbbell_squat", "Dumbbell Squat", Dumbbell, listOf(Quads, Glutes), listOf(Hamstrings), null,
        "Stand holding a dumbbell at each side, with your feet at about shoulder width.",
        "Squat down until your thighs are level with the floor.",
        "Stand back up through your whole foot.",
    ),
    variation(
        "smith_machine_front_squat", "Smith Machine Front Squat", SmithMachine, listOf(Quads), listOf(Glutes, Abs), "front_squat",
        "Rest the bar on the front of your shoulders and twist it to unhook it.",
        "Squat down until your thighs are level with the floor, keeping your chest up.",
        "Stand back up through your whole foot.",
    ),
    variation(
        "pendulum_squat", "Pendulum Squat", Machine, listOf(Quads), listOf(Glutes), null,
        "Stand on the platform with your shoulders under the pads and release the safety.",
        "Bend your knees to lower yourself as far as you can control.",
        "Push through your feet to stand back up.",
    ),
    variation(
        "belt_squat", "Belt Squat", Machine, listOf(Quads, Glutes), listOf(Hamstrings), null,
        "Attach the belt around your hips and stand on the platform.",
        "Squat down until your thighs are level with the floor.",
        "Stand back up through your whole foot.",
    ),
    variation(
        "single_leg_press", "Leg Press (Single Leg)", Machine, listOf(Quads, Glutes), listOf(Hamstrings), "leg_press",
        "Sit in the leg press and place one foot in the middle of the platform.",
        "Lower the platform with control until your knee is well bent.",
        "Press it back up, then finish your reps and switch sides.",
    ),
    variation(
        "single_leg_extension", "Leg Extension (Single Leg)", Machine, listOf(Quads), emptyList(), "leg_extension",
        "Set the machine so that your knee lines up with its pivot, with the pad on one shin.",
        "Straighten your leg and squeeze at the top.",
        "Lower it slowly, then finish your reps and switch sides.",
    ),
    variation(
        "barbell_lunge", "Barbell Lunge", Barbell, listOf(Quads, Glutes), listOf(Hamstrings), null,
        "Stand with a bar across your upper back.",
        "Step forward and lower your back knee toward the floor.",
        "Push through your front foot to step back, and alternate legs.",
    ),
    variation(
        "kettlebell_lunge", "Kettlebell Lunge", Kettlebell, listOf(Quads, Glutes), listOf(Hamstrings), "dumbbell_lunge",
        "Stand holding a kettlebell at each side.",
        "Step forward and lower your back knee toward the floor.",
        "Push through your front foot to step back, and alternate legs.",
    ),
    variation(
        "smith_machine_lunge", "Smith Machine Lunge", SmithMachine, listOf(Quads, Glutes), listOf(Hamstrings), null,
        "Stand in a split stance under the bar, resting it across your upper back, and twist it to unhook it.",
        "Lower your back knee toward the floor.",
        "Push through your front foot to stand up, then finish your reps and switch sides.",
    ),
    variation(
        "walking_lunge", "Walking Lunge", Dumbbell, listOf(Quads, Glutes), listOf(Hamstrings), "dumbbell_lunge",
        "Stand holding a dumbbell at each side, with room to walk ahead of you.",
        "Step forward and lower your back knee toward the floor.",
        "Push through your front foot and step straight into the next lunge.",
    ),
    variation(
        "reverse_lunge", "Reverse Lunge", Dumbbell, listOf(Quads, Glutes), listOf(Hamstrings), "dumbbell_lunge",
        "Stand holding a dumbbell at each side.",
        "Step backward and lower your back knee toward the floor.",
        "Push through your front foot to stand up, and alternate legs.",
    ),
    variation(
        "bodyweight_lunge", "Bodyweight Lunge", Bodyweight, listOf(Quads, Glutes), listOf(Hamstrings), null,
        "Stand with your feet at about hip width.",
        "Step forward and lower your back knee toward the floor.",
        "Push through your front foot to step back, and alternate legs.",
    ),
    variation(
        "barbell_bulgarian_split_squat", "Bulgarian Split Squat (Barbell)", Barbell, listOf(Quads, Glutes), listOf(Hamstrings), null,
        "Rest a bar across your upper back and put the top of your back foot on a bench behind you.",
        "Lower your back knee toward the floor.",
        "Push through your front foot to stand up, then finish your reps and switch sides.",
    ),
    variation(
        "smith_machine_bulgarian_split_squat", "Bulgarian Split Squat (Smith Machine)", SmithMachine, listOf(Quads, Glutes), listOf(Hamstrings), null,
        "Rest the bar across your upper back, put your back foot on a bench behind you and twist the bar to unhook it.",
        "Lower your back knee toward the floor.",
        "Push through your front foot to stand up, then finish your reps and switch sides.",
    ),
    variation(
        "bodyweight_bulgarian_split_squat", "Bulgarian Split Squat (Bodyweight)", Bodyweight, listOf(Quads, Glutes), listOf(Hamstrings), null,
        "Put the top of your back foot on a bench behind you.",
        "Lower your back knee toward the floor.",
        "Push through your front foot to stand up, then finish your reps and switch sides.",
    ),
    variation(
        "barbell_step_up", "Barbell Step-Up", Barbell, listOf(Quads, Glutes), listOf(Hamstrings), null,
        "Rest a bar across your upper back and stand facing a sturdy box.",
        "Step up with one foot and drive through it until you're standing on the box.",
        "Step down with control, then finish your reps and switch sides.",
    ),
    variation(
        "kettlebell_step_up", "Kettlebell Step-Up", Kettlebell, listOf(Quads, Glutes), listOf(Hamstrings), "dumbbell_step_up",
        "Stand facing a sturdy box, holding a kettlebell at each side.",
        "Step up with one foot and drive through it until you're standing on the box.",
        "Step down with control, then finish your reps and switch sides.",
    ),
    variation(
        "bodyweight_step_up", "Bodyweight Step-Up", Bodyweight, listOf(Quads, Glutes), listOf(Hamstrings), null,
        "Stand facing a sturdy box or step.",
        "Step up with one foot and drive through it until you're standing on the box.",
        "Step down with control, then finish your reps and switch sides.",
    ),
    variation(
        "stiff_leg_deadlift", "Stiff-Leg Deadlift", Barbell, listOf(Hamstrings, Glutes), listOf(LowerBack, Forearms), "romanian_deadlift",
        "Stand holding a bar against your thighs, with your knees only very slightly bent.",
        "Push your hips back and lower the bar along your legs until you feel a strong stretch.",
        "Drive your hips forward to stand up.",
    ),
    variation(
        "smith_machine_romanian_deadlift", "Smith Machine Romanian Deadlift", SmithMachine, listOf(Hamstrings, Glutes), listOf(LowerBack), "romanian_deadlift",
        "Stand holding the bar against your thighs and twist it to unhook it.",
        "Push your hips back and lower the bar along your legs until you feel a stretch.",
        "Drive your hips forward to stand up.",
    ),
    variation(
        "cable_romanian_deadlift", "Cable Romanian Deadlift", Cable, listOf(Hamstrings, Glutes), listOf(LowerBack), null,
        "Attach a bar to a low pulley and stand facing it, holding the bar against your thighs.",
        "Push your hips back and lower the bar until you feel a stretch.",
        "Drive your hips forward to stand up.",
    ),
    variation(
        "kettlebell_romanian_deadlift", "Kettlebell Romanian Deadlift", Kettlebell, listOf(Hamstrings, Glutes), listOf(LowerBack, Forearms), null,
        "Stand holding a kettlebell by the handle with both hands.",
        "Push your hips back and lower it along your legs until you feel a stretch.",
        "Drive your hips forward to stand up.",
    ),
    variation(
        "single_leg_romanian_deadlift", "Single-Leg Romanian Deadlift", Dumbbell, listOf(Hamstrings, Glutes), listOf(LowerBack, Forearms), null,
        "Stand on one leg holding a dumbbell in the opposite hand.",
        "Hinge forward, letting your free leg rise behind you until you feel a stretch.",
        "Return to standing, then finish your reps and switch sides.",
    ),
    variation(
        "standing_leg_curl", "Standing Leg Curl", Machine, listOf(Hamstrings), listOf(Calves), null,
        "Stand in the machine with the pad behind one ankle.",
        "Curl your heel toward your glutes.",
        "Lower it slowly, then finish your reps and switch sides.",
    ),
    variation(
        "cable_leg_curl", "Cable Leg Curl", Cable, listOf(Hamstrings), listOf(Calves), null,
        "Attach an ankle strap to a low pulley and lie or stand facing it.",
        "Curl your heel toward your glutes.",
        "Straighten your leg slowly, then finish your reps and switch sides.",
    ),
    variation(
        "band_leg_curl", "Band Leg Curl", Band, listOf(Hamstrings), listOf(Calves), null,
        "Anchor a band low down, loop it around your ankle and lie face down.",
        "Curl your heel toward your glutes.",
        "Straighten your leg slowly, then finish your reps and switch sides.",
    ),
    variation(
        "dumbbell_leg_curl", "Dumbbell Leg Curl", Dumbbell, listOf(Hamstrings), listOf(Calves), null,
        "Lie face down on a bench and hold a dumbbell between your feet.",
        "Curl your heels toward your glutes.",
        "Straighten your legs slowly.",
    ),
    variation(
        "dumbbell_hip_thrust", "Dumbbell Hip Thrust", Dumbbell, listOf(Glutes), listOf(Hamstrings), null,
        "Rest your upper back on a bench and hold a dumbbell across your hips.",
        "Drive your hips up until your body is straight from shoulders to knees.",
        "Lower your hips with control.",
    ),
    variation(
        "smith_machine_hip_thrust", "Smith Machine Hip Thrust", SmithMachine, listOf(Glutes), listOf(Hamstrings), null,
        "Rest your upper back on a bench with the bar across your hips, and twist it to unhook it.",
        "Drive your hips up until your body is straight from shoulders to knees.",
        "Lower your hips with control.",
    ),
    variation(
        "machine_hip_thrust", "Machine Hip Thrust", Machine, listOf(Glutes), listOf(Hamstrings), null,
        "Sit in the machine with the pad across your hips and your feet flat.",
        "Drive your hips up until your body is straight from shoulders to knees.",
        "Lower your hips with control.",
    ),
    variation(
        "band_hip_thrust", "Band Hip Thrust", Band, listOf(Glutes), listOf(Hamstrings), null,
        "Rest your upper back on a bench, with a band across your hips held down at each side.",
        "Drive your hips up until your body is straight from shoulders to knees.",
        "Lower your hips with control.",
    ),
    variation(
        "barbell_glute_bridge", "Glute Bridge (Barbell)", Barbell, listOf(Glutes), listOf(Hamstrings), "glute_bridge",
        "Lie on your back with your knees bent and a bar across your hips.",
        "Drive your hips up until your body is straight from shoulders to knees.",
        "Lower your hips with control.",
    ),
    variation(
        "single_leg_glute_bridge", "Glute Bridge (Single Leg)", Bodyweight, listOf(Glutes), listOf(Hamstrings), "glute_bridge",
        "Lie on your back with one knee bent and the other leg raised.",
        "Drive through your planted foot until your hips are level and your body is straight.",
        "Lower your hips with control, then finish your reps and switch sides.",
    ),
    variation(
        "machine_glute_kickback", "Machine Glute Kickback", Machine, listOf(Glutes), listOf(Hamstrings), null,
        "Set up on the machine with one foot on the plate or pad.",
        "Push your leg back and up, squeezing your glute.",
        "Return slowly, then finish your reps and switch sides.",
    ),
    variation(
        "band_kickback", "Band Kickback", Band, listOf(Glutes), listOf(Hamstrings), null,
        "Anchor a band low down, loop it around one ankle and stand facing the anchor.",
        "Push your leg straight back, squeezing your glute.",
        "Return slowly, then finish your reps and switch sides.",
    ),
    variation(
        "cable_pull_through", "Cable Pull-Through", Cable, listOf(Glutes, Hamstrings), listOf(LowerBack), null,
        "Face away from a low pulley with a rope between your legs, holding it with both hands.",
        "Push your hips back until you feel a stretch in your hamstrings.",
        "Drive your hips forward to stand up.",
    ),
    variation(
        "dumbbell_swing", "Dumbbell Swing", Dumbbell, listOf(Glutes, Hamstrings), listOf(LowerBack, Abs), null,
        "Stand with your feet wider than your shoulders, holding one end of a dumbbell with both hands.",
        "Hinge back so that it swings between your legs, then snap your hips forward.",
        "Let it swing up to chest height and fall back into the next rep.",
    ),
    calfRaise("smith_machine_calf_raise", "Calf Raise (Smith Machine)", SmithMachine, "standing_calf_raise", "Stand with the balls of your feet on a block and the bar across your upper back, and twist it to unhook it."),
    calfRaise("barbell_calf_raise", "Calf Raise (Barbell)", Barbell, "standing_calf_raise", "Stand with a bar across your upper back and the balls of your feet on a low block."),
    calfRaise("dumbbell_calf_raise", "Calf Raise (Dumbbell)", Dumbbell, "standing_calf_raise", "Stand with the balls of your feet on a low block, holding a dumbbell at each side."),
    calfRaise("bodyweight_calf_raise", "Calf Raise (Bodyweight)", Bodyweight, "standing_calf_raise", "Stand with the balls of your feet on a step, holding something for balance."),
    calfRaise("leg_press_calf_raise", "Calf Raise (Leg Press)", Machine, null, "Sit in the leg press with your legs almost straight and the balls of your feet at the bottom of the platform."),
    calfRaise("seated_dumbbell_calf_raise", "Seated Calf Raise (Dumbbell)", Dumbbell, "seated_calf_raise", "Sit on a bench with the balls of your feet on a low block and the dumbbells resting on your knees."),
    variation(
        "cable_hip_adduction", "Cable Hip Adduction", Cable, listOf(Adductors), emptyList(), null,
        "Attach an ankle strap to a low pulley and stand side-on to it, with the strap on the nearer ankle.",
        "Pull that leg across in front of your other leg.",
        "Return slowly, then finish your reps and switch sides.",
    ),
    variation(
        "band_hip_adduction", "Band Hip Adduction", Band, listOf(Adductors), emptyList(), null,
        "Anchor a band low down, loop it around your ankle and stand side-on to the anchor.",
        "Pull that leg across in front of your other leg.",
        "Return slowly, then finish your reps and switch sides.",
    ),
    variation(
        "cable_hip_abduction", "Cable Hip Abduction", Cable, listOf(Abductors), listOf(Glutes), null,
        "Attach an ankle strap to a low pulley and stand side-on to it, with the strap on the farther ankle.",
        "Raise that leg out to the side.",
        "Return slowly, then finish your reps and switch sides.",
    ),
    variation(
        "seated_band_hip_abduction", "Seated Band Hip Abduction", Band, listOf(Abductors), listOf(Glutes), null,
        "Sit on a bench with a band looped around your legs, just above your knees.",
        "Push your knees apart against the band.",
        "Bring them back together slowly.",
    ),
)

private val LandmineVariations = listOf(
    variation(
        "landmine_row", "Landmine Row", Barbell, listOf(MidBack, Lats), listOf(RearDelts, Biceps), null,
        "Wedge one end of a bar in a landmine and straddle the other end, then hinge forward and hold it with both hands.",
        "Pull the end of the bar to your chest, squeezing your shoulder blades together.",
        "Lower it slowly until your arms are straight.",
    ),
    variation(
        "meadows_row", "Meadows Row (Landmine)", Barbell, listOf(Lats, MidBack), listOf(RearDelts, Biceps), null,
        "Stand side-on to the end of a landmine bar in a staggered stance, and hold the end in your nearer hand.",
        "Pull it up beside your hip, driving your elbow back and up.",
        "Lower it slowly, then finish your reps and switch sides.",
    ),
    variation(
        "landmine_squat", "Landmine Squat", Barbell, listOf(Quads, Glutes), listOf(Abs), null,
        "Hold the end of a landmine bar at your chest with both hands, your feet at about shoulder width.",
        "Squat down until your thighs are level with the floor, keeping your chest up.",
        "Stand back up through your whole foot.",
    ),
    variation(
        "landmine_romanian_deadlift", "Landmine Romanian Deadlift", Barbell, listOf(Hamstrings, Glutes), listOf(LowerBack), null,
        "Stand facing the end of a landmine bar and hold it in both hands with your arms straight.",
        "Push your hips back and lower it until you feel a stretch in your hamstrings.",
        "Drive your hips forward to stand up.",
    ),
    variation(
        "landmine_reverse_lunge", "Landmine Reverse Lunge", Barbell, listOf(Quads, Glutes), listOf(Hamstrings), null,
        "Hold the end of a landmine bar at your chest with both hands.",
        "Step backward and lower your back knee toward the floor.",
        "Push through your front foot to stand up, and alternate legs.",
    ),
    variation(
        "landmine_thruster", "Landmine Thruster", Barbell, listOf(Quads, FrontDelts), listOf(Glutes, Triceps), null,
        "Hold the end of a landmine bar at your chest with both hands.",
        "Squat down, then drive up and press the bar up and forward as you stand.",
        "Lower it back to your chest as you go into the next squat.",
    ),
    variation(
        "landmine_lateral_raise", "Landmine Lateral Raise", Barbell, listOf(SideDelts), listOf(Traps), null,
        "Stand side-on to the end of a landmine bar and hold the end in your farther hand, across your body.",
        "Raise your arm out to the side until it's level with your shoulder.",
        "Lower it slowly, then finish your reps and switch sides.",
    ),
    variation(
        "half_kneeling_landmine_press", "Landmine Press (Half Kneeling)", Barbell, listOf(UpperChest, FrontDelts), listOf(Triceps, Abs), null,
        "Kneel on one knee and hold the end of a landmine bar at your shoulder on the same side as that knee.",
        "Press the bar up and forward until your arm is straight.",
        "Lower it slowly, then finish your reps and switch sides.",
    ),
    variation(
        "landmine_chest_press", "Landmine Chest Press", Barbell, listOf(MidChest, UpperChest), listOf(Triceps, FrontDelts), null,
        "Stand facing the end of a landmine bar and hold it at your chest with both hands.",
        "Press it forward and up until your arms are straight.",
        "Lower it slowly back to your chest.",
    ),
)

private val WeightedVariations = listOf(
    variation(
        "weighted_push_up", "Push-Up (Weighted)", Weighted, listOf(MidChest), listOf(FrontDelts, Triceps, Abs), "push_up",
        "Wear a weighted vest, or have a plate placed across your upper back, and get into a push-up position.",
        "Lower your chest toward the floor, keeping your body straight.",
        "Press back up until your arms are straight.",
    ),
    variation(
        "weighted_bench_dip", "Bench Dip (Weighted)", Weighted, listOf(Triceps), listOf(FrontDelts, LowerChest), "bench_dip",
        "Sit on the edge of a bench with your hands beside your hips, and have a plate placed on your thighs.",
        "Slide forward and bend your elbows to lower yourself.",
        "Press back up until your arms are straight.",
    ),
    variation(
        "weighted_hanging_leg_raise", "Hanging Leg Raise (Weighted)", Weighted, listOf(Abs), listOf(Obliques, Forearms), null,
        "Hang from a bar with a light dumbbell held between your feet.",
        "Raise your legs until they're level with the floor, without swinging.",
        "Lower them slowly.",
    ),
    variation(
        "weighted_decline_crunch", "Decline Crunch (Weighted)", Weighted, listOf(Abs), emptyList(), null,
        "Secure your legs on a decline bench and lie back, holding a plate against your chest.",
        "Curl your upper body toward your knees by tightening your abs.",
        "Lower yourself slowly.",
    ),
    variation(
        "weighted_inverted_row", "Inverted Row (Weighted)", Weighted, listOf(MidBack, Lats), listOf(RearDelts, Biceps), null,
        "Wear a weighted vest, or rest a plate on your hips, and lie under a bar set at about waist height.",
        "Pull your chest up to the bar, keeping your body straight.",
        "Lower yourself slowly until your arms are straight.",
    ),
    timedVariation(
        "weighted_plank", "Plank (Weighted)", Weighted, listOf(Abs), listOf(Obliques, LowerBack), null,
        "Get into a plank on your forearms and have a plate placed on your upper back.",
        "Keep your body straight from head to heels, without letting your hips sag.",
        "Hold for as long as you can keep good form.",
    ),
    timedVariation(
        "weighted_dead_hang", "Dead Hang (Weighted)", Weighted, listOf(Forearms), listOf(Lats), null,
        "Add weight with a dip belt, then grip a pull-up bar with your hands about shoulder-width apart.",
        "Hang with your arms straight and your shoulders active.",
        "Hold for as long as your grip allows, then step down with control.",
    ),
)

val ExerciseVariations: List<ExerciseVariation> =
    ChestVariations + BackVariations + ShoulderVariations + BicepsVariations + TricepsVariations +
        ForearmVariations + CoreVariations + LegVariations + LandmineVariations + WeightedVariations
