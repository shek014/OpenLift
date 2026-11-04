# OpenLift

**Track your lifts, see the muscles you worked, and watch your progress. Free and open source, for Android.**

[![License: GPL-3.0](https://img.shields.io/badge/License-GPL--3.0-blue.svg)](LICENSE)
![Platform: Android 8.0+](https://img.shields.io/badge/Platform-Android%208.0%2B-3DDC84.svg)
![Status: early development](https://img.shields.io/badge/Status-early%20development-orange.svg)

> **OpenLift is in early development.** All the core features on the roadmap are in place, but it hasn't had much real-world use yet. There are no releases to download so far.

## What works today

- **Workout logging:** add exercises and log sets, reps and weight, with your previous numbers alongside and your recent exercises suggested first. Drag exercises to reorder them. Workouts save as you go, and finishing one shows a summary.
- **Routines:** build your own with target sets and reps, or add a recommended split such as Full Body, Upper/Lower or Push/Pull/Legs, then start a workout in one tap.
- **Rest timer:** counts down between sets, with a notification that alerts you even with the app closed and the screen off. Set the time for each exercise, or turn it off.
- **History:** look back through finished workouts, edit or delete them, and see your volume, workouts and sets week by week.
- **Progress:** a graph and personal records for each exercise, with new records called out when you finish a workout.
- **Exercise library:** 100 exercises, each linked to the primary and secondary muscles it trains. Search by name, equipment or muscle, or filter by muscle group, individual muscle and equipment.
- **Body map:** the home screen shows the body from the front and back, filled in with what you've trained this week. Tap a muscle to see the exercises that work it.
- **Exercise pages:** one page for each exercise, showing its muscles on the body map, with step-by-step instructions for all of them, animations for 72, and a log of every set you've done.
- **Settings:** dark or light theme, kilograms or pounds, and how long the rest timer runs.

## What's planned

- **Animate every exercise:** fill in the 28 exercises that don't have an animation yet.

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

## Try it

There's no installable release yet, so for now you'll need to build OpenLift from source.

**You'll need:**

- [Android Studio](https://developer.android.com/studio)
- A phone running Android 8.0 (API 26) or newer, or an emulator

**Steps:**

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
│   ├── AppSettings.kt       Theme, weight unit and rest length
│   ├── RecommendedSplits.kt The ready-made programs
│   ├── RestTimer.kt
│   ├── RestTimerService.kt  Keeps the timer running outside the app
│   ├── RoutineRepository.kt
│   ├── WorkoutRepository.kt
│   ├── local/               Room database
│   └── model/               Exercises, workouts, routines and progress
└── ui/
    ├── OpenLiftApp.kt       Bottom bar and tab navigation
    ├── body/                Body map and muscle screens
    ├── charts/              Line and bar charts
    ├── exercises/           Exercise library and exercise pages
    ├── history/             Past workouts and editing them
    ├── routines/            Routines and recommended splits
    ├── settings/            Settings
    ├── theme/               Colors and theming
    └── workout/             Workout logging and summary
```

The animations are in `app/src/main/assets/exercise_media/`. `tools/build_exercise_gifs.py` builds them, using the list in `tools/everkinetic_gifs.tsv`.

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
