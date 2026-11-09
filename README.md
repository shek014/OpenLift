# OpenLift

**Track your lifts, see the muscles you worked, and watch your progress. Free and open source, for Android.**

[![License: GPL-3.0](https://img.shields.io/badge/License-GPL--3.0-blue.svg)](LICENSE)
![Platform: Android 8.0+](https://img.shields.io/badge/Platform-Android%208.0%2B-3DDC84.svg)
![Status: early development](https://img.shields.io/badge/Status-early%20development-orange.svg)

> **OpenLift is in early development.** All the core features on the roadmap are in place, but it hasn't had much real-world use yet. Test builds are on the [releases page](https://github.com/shek014/OpenLift/releases).

## What works today

- **Home:** your latest workouts in order, showing the time, volume and exercises of each, with a menu to share it, save it as a routine, copy it into a new workout, edit it or delete it.
- **Workout logging:** add exercises and log sets, reps and weight, with your previous numbers alongside and your recent exercises suggested first. Add notes under each exercise, and change its rep range right there. Tap a set's number to mark it as warm-up, drop set or failure, or to remove it. You can add several exercises at once. Each exercise has a menu to reorder, replace, pair in a superset or remove it. Workouts save as you go, and finishing one shows a summary.
- **Timed exercises:** planks, hangs and carries are tracked by time, with a built-in stopwatch on each set, or type the time yourself.
- **Timer and stopwatch:** at the top of a workout, next to Finish.
- **Warm-up and plate calculators:** get warm-up sets worked out from your working weight, and see what to load on each side of the bar.
- **Routines:** build your own with target sets and reps, or add a recommended split such as Full Body, Upper/Lower or Push/Pull/Legs, then start a workout in one tap.
- **Rest timer:** off until you turn it on for an exercise. Counts down between sets, with a notification that alerts you even with the app closed and the screen off. Set the time for each exercise, or turn it off.
- **History:** look back through finished workouts, edit or delete them, and see your volume, workouts and sets week by week.
- **Progress:** a graph and personal records for each exercise, going back as far as you've logged, with a trophy beside any set that breaks a record.
- **Statistics:** totals, averages, streaks, your busiest days, sets per muscle and your most done exercises, plus graphs of estimated 1 rep max, best set volume, session volume and total reps for each exercise, and your best weight for each rep count.
- **Body measurements:** optionally track body weight, height, body fat and measurements such as waist and arms, with a graph for each. BMI is off unless you turn it on.
- **Exercise library:** 314 exercises, including versions of the same movement with different equipment and attachments, plus as many custom ones as you like, each linked to the primary and secondary muscles it trains. Search by name, equipment or muscle, or filter by muscle group, individual muscle and equipment.
- **Body map:** the Body screen shows the body from the front and back, filled in with what you've trained this week. Tap a muscle to see the exercises that work it.
- **Exercise pages:** one page for each exercise, showing its muscles on the body map, with step-by-step instructions for all of them, animations for 144, and a log of every set you've done.
- **Profile:** your totals, and the way to History, Statistics, Exercises, Body and Settings.
- **Settings:** dark or light theme, kilograms or pounds, centimeters or inches, BMI, and the sounds for personal records and the rest timer.
- **Backups:** save everything to a file you keep wherever you like, and restore it later or on another phone.

## What's planned

- **Animate every exercise:** fill in the 170 exercises that don't have an animation yet.

## Roadmap

- [x] App structure and tab navigation
- [x] Exercise library with muscle mapping
- [x] Exercise pages with animations and how-to steps
- [x] Body map for browsing exercises by muscle
- [x] Workout logging, with a summary and rest timer
- [x] Fill in the body map with the muscles you've trained
- [x] Routines and recommended splits
- [x] Workout history, with editing
- [x] Progress graphs and personal records
- [x] Home page with a menu on each workout
- [x] Set types, timed exercises and record notifications
- [x] Custom exercises
- [x] Warm-up and plate calculators
- [x] Advanced statistics
- [x] Body measurements and optional BMI

## Try it

**On your phone:** download the APK from the [latest pre-release](https://github.com/shek014/OpenLift/releases) and open it. You'll need Android 8.0 or newer, and Android may ask you to allow installs from your browser. These are early test builds, signed with a shared test key.

**From source:**

You'll need:

- [Android Studio](https://developer.android.com/studio)
- A phone running Android 8.0 (API 26) or newer, or an emulator

Then:

1. Clone the repository:

   ```bash
   git clone https://github.com/shek014/OpenLift.git
   ```

2. In Android Studio, choose **File > Open**, select the `OpenLift` folder and wait for the Gradle sync to finish. The first sync downloads a lot and can take several minutes.
3. Connect a phone with USB debugging turned on, or create an emulator in **Device Manager**.
4. Press **Run**.

## Built with

- [Kotlin](https://kotlinlang.org/)
- [Jetpack Compose](https://developer.android.com/compose) and Material 3 for the interface
- [Navigation Compose](https://developer.android.com/develop/ui/compose/navigation) for moving between screens
- [Room](https://developer.android.com/training/data-storage/room) for storing data on the device
- [Coil](https://coil-kt.github.io/coil/) for playing the animations
- [Inter](https://github.com/rsms/inter) for the typeface
- The amethyst accent color from the Black & Amethyst variant of [Bearded Theme](https://github.com/BeardedBear/bearded-theme)

Library versions are listed in [`gradle/libs.versions.toml`](gradle/libs.versions.toml).

## Project layout

```text
app/src/main/java/io/github/shek014/openlift/
├── MainActivity.kt          App entry point
├── OpenLiftApplication.kt   Objects shared across the app
├── data/
│   ├── ExerciseCatalog.kt   The built-in exercises and their muscles
│   ├── ExerciseInstructions.kt  The How To steps
│   ├── ExerciseMedia.kt     Finds each exercise's animation
│   ├── ExerciseRepository.kt
│   ├── AppSettings.kt       Theme, units, BMI and rest length
│   ├── DatabaseBackup.kt    Saving and restoring backups
│   ├── MeasurementRepository.kt
│   ├── RecommendedSplits.kt The ready-made programs
│   ├── RestTimer.kt
│   ├── RestTimerService.kt  Keeps the timer running outside the app
│   ├── RoutineRepository.kt
│   ├── Sounds.kt            The sounds and what plays them
│   ├── WorkoutRepository.kt
│   ├── local/               Room database
│   └── model/               Exercises, workouts, routines, progress and statistics
└── ui/
    ├── OpenLiftApp.kt       Bottom bar and tab navigation
    ├── body/                Body map and muscle screens
    ├── charts/              Line and bar charts
    ├── exercises/           Exercise library and exercise pages
    ├── history/             Past workouts, editing them and statistics
    ├── home/                The Home page
    ├── measurements/        Body measurements and BMI
    ├── profile/             The Profile tab
    ├── routines/            Routines and recommended splits
    ├── settings/            Settings
    ├── theme/               Colors and theming
    └── workout/             Workout logging, summary and calculators
```

The animations are in `app/src/main/assets/exercise_media/`. `tools/build_exercise_gifs.py` builds them, using the list in `tools/everkinetic_gifs.tsv`.

The sounds in `app/src/main/res/raw/` are made from scratch by `tools/make_sounds.py`.

## Running the tests

```bash
./gradlew testDebugUnitTest
```

## Contributing

OpenLift is at an early stage and its structure is still changing, so please [open an issue](https://github.com/shek014/OpenLift/issues) to discuss an idea before starting on a large change.

## License

OpenLift is free software, released under the [GNU General Public License v3.0](LICENSE). You can use, study, change and share it. If you distribute a modified version, you must release its source code under the same license.

The exercise animations are licensed separately, under [CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/). They're adapted from drawings by [Everkinetic](https://github.com/everkinetic/data). See the [notice](app/src/main/assets/exercise_media/NOTICE.txt) for details.

The Inter typeface is licensed under the [SIL Open Font License](app/src/main/assets/licenses/inter-OFL.txt).
