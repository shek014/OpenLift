# OpenLift

**Track your lifts, see the muscles you worked, and watch your progress. Free and open source, for Android.**

[![License: GPL-3.0](https://img.shields.io/badge/License-GPL--3.0-blue.svg)](LICENSE)
![Platform: Android 8.0+](https://img.shields.io/badge/Platform-Android%208.0%2B-3DDC84.svg)
![Status: early development](https://img.shields.io/badge/Status-early%20development-orange.svg)

> **OpenLift isn't ready for tracking workouts yet.** You can browse the exercise library, but the Workout, Routines and History tabs are still placeholders. There are no releases to download so far.

## What works today

- **Exercise library:** 100 exercises, each linked to the primary and secondary muscles it trains. Search by name, equipment or muscle, or filter by muscle group, individual muscle and equipment.

## What's planned

- **Log workouts:** record sets, reps and weight as you train, with a rest timer between sets.
- **See exactly what you worked:** the muscles for each exercise, shown on a body map.
- **Build routines:** save your own workouts so you can start them in one tap.
- **Follow a split:** start from a recommended split such as Upper/Lower, Push/Pull/Legs or Full Body.
- **Track your progress:** view graphs of your overall workout history and of each exercise.

## Roadmap

- [x] App structure and tab navigation
- [x] Exercise library with muscle mapping
- [ ] Workout logging
- [ ] Routines and recommended splits
- [ ] Workout history
- [ ] Progress graphs
- [ ] Muscle body map

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

Library versions are listed in [`gradle/libs.versions.toml`](gradle/libs.versions.toml).

## Project layout

```text
app/src/main/java/io/github/shek014/openlift/
├── MainActivity.kt          App entry point
├── OpenLiftApplication.kt   Objects shared across the app
├── data/
│   ├── ExerciseCatalog.kt   The built-in exercises and their muscles
│   ├── ExerciseRepository.kt
│   ├── local/               Room database
│   └── model/               Exercise, Muscle and Equipment
└── ui/
    ├── OpenLiftApp.kt       Bottom bar and tab navigation
    ├── exercises/           Exercise library screens
    ├── screens/             Placeholder screens
    └── theme/               Colors and theming
```

## Running the tests

```bash
./gradlew testDebugUnitTest
```

## Contributing

OpenLift is at an early stage and its structure is still changing, so please [open an issue](https://github.com/shek014/OpenLift/issues) to discuss an idea before starting on a large change.

## License

OpenLift is free software, released under the [GNU General Public License v3.0](LICENSE). You can use, study, change and share it. If you distribute a modified version, you must release its source code under the same license.
