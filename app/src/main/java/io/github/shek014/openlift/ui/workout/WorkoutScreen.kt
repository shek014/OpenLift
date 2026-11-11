package io.github.shek014.openlift.ui.workout

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.SystemClock
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.shek014.openlift.data.AppSettings
import io.github.shek014.openlift.data.model.Exercise
import io.github.shek014.openlift.data.model.LoggedSet
import io.github.shek014.openlift.data.model.NextSet
import io.github.shek014.openlift.data.model.Routine
import io.github.shek014.openlift.data.model.SetHint
import io.github.shek014.openlift.data.model.SetType
import io.github.shek014.openlift.data.model.WeightUnit
import io.github.shek014.openlift.data.model.Workout
import io.github.shek014.openlift.data.model.WorkoutExercise
import io.github.shek014.openlift.data.model.WorkoutSet
import io.github.shek014.openlift.data.model.formatDuration
import io.github.shek014.openlift.data.model.formatRepsTarget
import io.github.shek014.openlift.data.model.formatRest
import io.github.shek014.openlift.data.model.formatSet
import io.github.shek014.openlift.data.model.formatVolume
import io.github.shek014.openlift.data.model.formatWeight
import io.github.shek014.openlift.data.model.isRoutineNameTaken
import io.github.shek014.openlift.data.model.isValidDurationInput
import io.github.shek014.openlift.data.model.isValidRepsInput
import io.github.shek014.openlift.data.model.isValidWeightInput
import io.github.shek014.openlift.data.model.nextSet
import io.github.shek014.openlift.data.model.parseDuration
import io.github.shek014.openlift.data.model.parseRepRange
import io.github.shek014.openlift.data.model.parseReps
import io.github.shek014.openlift.data.model.parseWeight
import io.github.shek014.openlift.data.model.restsAfter
import io.github.shek014.openlift.data.model.sameWeight
import io.github.shek014.openlift.data.model.setHint
import io.github.shek014.openlift.data.model.setLabel
import io.github.shek014.openlift.data.model.supersetPartners
import io.github.shek014.openlift.data.model.uniqueRoutineName
import io.github.shek014.openlift.data.model.usesBar
import io.github.shek014.openlift.ui.LocalWeightUnit
import io.github.shek014.openlift.ui.exercises.ExerciseThumbnail
import io.github.shek014.openlift.ui.routines.summary
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun WorkoutScreen(
    viewModel: WorkoutViewModel,
    settings: AppSettings,
    onAddExercise: () -> Unit,
    onReorder: () -> Unit,
    onReplace: (workoutExerciseId: Long) -> Unit,
    onExerciseClick: (exerciseId: String) -> Unit,
    onWorkoutSaved: (workoutId: Long) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val restSeconds by viewModel.restSeconds.collectAsStateWithLifecycle()

    val current = state ?: return
    val workout = current.workout
    if (workout == null) {
        val routines by viewModel.routines.collectAsStateWithLifecycle()
        NoWorkout(
            routines = routines,
            onStart = viewModel::startWorkout,
            onStartRoutine = viewModel::startRoutine,
        )
    } else {
        ActiveWorkout(
            workout = workout,
            previousSets = current.previousSets,
            recordSetIds = current.recordSetIds,
            restSeconds = restSeconds,
            viewModel = viewModel,
            settings = settings,
            onAddExercise = onAddExercise,
            onReorder = onReorder,
            onReplace = onReplace,
            onExerciseClick = onExerciseClick,
            onWorkoutSaved = onWorkoutSaved,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoWorkout(
    routines: List<Routine>,
    onStart: () -> Unit,
    onStartRoutine: (Long) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Workout") })
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item(key = "start") {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = "Add exercises and log your sets as you train. Everything saves as you go, so it's safe to leave the app.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) {
                        Text("Start empty workout")
                    }
                }
            }
            if (routines.isNotEmpty()) {
                item(key = "routines-title") {
                    Text(
                        text = "Start a routine",
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.onBackground)
                }
                items(routines, key = { "routine-${it.id}" }) { routine ->
                    ListItem(
                        headlineContent = { Text(routine.name) },
                        supportingContent = { Text(routine.summary(), style = MaterialTheme.typography.labelMedium) },
                        modifier = Modifier.clickable { onStartRoutine(routine.id) },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

/** A question to confirm before doing something that can't be undone. */
private class Confirmation(
    val title: String,
    val message: String,
    val confirmLabel: String,
    val onConfirm: () -> Unit,
)

@Composable
private fun AskForNotificationsOnce(settings: AppSettings) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) {
        val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        if (needsPermission && !settings.askedForNotifications) {
            settings.askedForNotifications = true
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Composable
private fun ActiveWorkout(
    workout: Workout,
    previousSets: Map<String, List<LoggedSet>>,
    recordSetIds: Set<Long>,
    restSeconds: Int?,
    viewModel: WorkoutViewModel,
    settings: AppSettings,
    onAddExercise: () -> Unit,
    onReorder: () -> Unit,
    onReplace: (Long) -> Unit,
    onExerciseClick: (String) -> Unit,
    onWorkoutSaved: (Long) -> Unit,
) {
    var confirmation by remember { mutableStateOf<Confirmation?>(null) }
    var changedRoutine by remember { mutableStateOf<Routine?>(null) }
    var namingRoutine by remember { mutableStateOf<String?>(null) }
    val routines by viewModel.routines.collectAsStateWithLifecycle()
    var showingClock by rememberSaveable { mutableStateOf(false) }
    val globalRestSeconds by settings.restSeconds.collectAsStateWithLifecycle()
    val stopwatch by viewModel.stopwatch.collectAsStateWithLifecycle()
    val finish = { update: RoutineUpdate? ->
        viewModel.finishWorkout(update) { id -> if (id != null) onWorkoutSaved(id) }
    }
    val unit = LocalWeightUnit.current
    val setTimer by viewModel.setTimer.collectAsStateWithLifecycle()
    val next = remember(workout, previousSets) { nextSet(workout, previousSets) }
    val askToFinish = {
        val completed = workout.completedSets.size
        confirmation = if (completed == 0) {
            Confirmation(
                title = "Nothing to save",
                message = "You haven't completed any sets yet. Discard this workout?",
                confirmLabel = "Discard",
                onConfirm = viewModel::discardWorkout,
            )
        } else {
            Confirmation(
                title = "Finish workout?",
                message = "Saves ${setCount(completed)}. Any sets you haven't checked off won't be saved.",
                confirmLabel = "Finish",
                onConfirm = {
                    viewModel.checkRoutine { routine ->
                        if (routine == null) finish(null) else changedRoutine = routine
                    }
                },
            )
        }
    }

    AskForNotificationsOnce(settings)

    // So that the screen doesn't turn off in the middle of a set or a rest.
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    WorkoutEditor(
        workout = workout,
        previousSets = previousSets,
        recordSetIds = recordSetIds,
        viewModel = viewModel,
        title = {
            Column {
                ElapsedTime(workout.startedAt)
                workout.name?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        defaultRestSeconds = workout.restSeconds ?: globalRestSeconds,
        settings = settings,
        header = {
            WorkoutRestControl(
                workoutRest = workout.restSeconds,
                globalRest = globalRestSeconds,
                onChange = viewModel::setWorkoutRestSeconds,
            )
        },
        actions = {
            ClockButton(stopwatch) { showingClock = true }
            TextButton(onClick = askToFinish) {
                Text("Finish")
            }
        },
        nextSetId = next?.set?.id,
        onAddExercise = onAddExercise,
        onReorder = onReorder,
        onReplace = onReplace,
        onExerciseClick = onExerciseClick,
        extraActions = {
            TextButton(
                onClick = {
                    confirmation = Confirmation(
                        title = "Discard this workout?",
                        message = "Everything you've logged in it will be deleted.",
                        confirmLabel = "Discard",
                        onConfirm = viewModel::discardWorkout,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Discard workout", color = MaterialTheme.colorScheme.error)
            }
        },
        bottomBar = {
            val resting = restSeconds
            when {
                resting != null -> RestBar(
                    seconds = resting,
                    nextLabel = next?.let { "Next: ${it.entry.exercise.name}, ${it.label()}" },
                    onAdjust = viewModel::adjustRest,
                    onSkip = viewModel::skipRest,
                )
                workout.exercises.isNotEmpty() -> SetDoneBar(
                    next = next,
                    unit = unit,
                    timer = setTimer,
                    workout = workout,
                    onLog = { target ->
                        val rest = (workout.restSeconds ?: globalRestSeconds)
                            .let { target.entry.restSeconds ?: it }
                            .takeIf { workout.restsAfter(target.entry) && !target.set.isWarmup }
                        viewModel.completeSet(target.set.id, target.weightKg, target.reps, rest, target.durationSeconds)
                    },
                    onStartTimer = { viewModel.startSetTimer(it.set.id) },
                    onStopTimer = { target ->
                        val seconds = viewModel.stopSetTimer() ?: return@SetDoneBar
                        val rest = (workout.restSeconds ?: globalRestSeconds)
                            .let { target.entry.restSeconds ?: it }
                            .takeIf { workout.restsAfter(target.entry) }
                        viewModel.completeSet(target.set.id, target.weightKg, null, rest, seconds)
                    },
                    onFinish = askToFinish,
                )
            }
        },
    )

    confirmation?.let { ConfirmationDialog(it, onDismiss = { confirmation = null }) }
    changedRoutine?.let { routine ->
        RoutineChangedDialog(
            routineName = routine.name,
            onUpdate = {
                changedRoutine = null
                finish(RoutineUpdate.Replace(routine))
            },
            onSaveAsNew = {
                changedRoutine = null
                namingRoutine = uniqueRoutineName(routine.name, routines.map { it.name })
            },
            onSkip = {
                changedRoutine = null
                finish(null)
            },
            onDismiss = { changedRoutine = null },
        )
    }
    namingRoutine?.let { suggested ->
        NewRoutineDialog(
            suggestedName = suggested,
            otherNames = routines.map { it.name },
            onSave = { name ->
                namingRoutine = null
                finish(RoutineUpdate.SaveAsNew(name))
            },
            onDismiss = { namingRoutine = null },
        )
    }
    if (showingClock) {
        ClockSheet(viewModel = viewModel, stopwatch = stopwatch, onDismiss = { showingClock = false })
    }
}

/** The set as the bar names it: "set 2 of 3", or "warm-up 1". */
private fun NextSet.label(): String {
    val total = entry.sets.count { !it.isWarmup }
    return if (number == null) "warm-up" else "set $number of $total"
}

/**
 * A big button for the next set, so that logging doesn't need the small tick. While a timed set
 * runs it shows the time, and once every set is done it offers to finish.
 */
@Composable
private fun SetDoneBar(
    next: NextSet?,
    unit: WeightUnit,
    timer: SetTimer?,
    workout: Workout,
    onLog: (NextSet) -> Unit,
    onStartTimer: (NextSet) -> Unit,
    onStopTimer: (NextSet) -> Unit,
    onFinish: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val timing = timer != null && timer.setId == next?.set?.id
            Column(modifier = Modifier.weight(1f)) {
                when {
                    next == null -> {
                        Text(
                            text = "All ${setCount(workout.completedSets.size)} done",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "${formatDuration(elapsedSeconds(workout.startedAt))} · ${formatVolume(workout.volumeKg, unit)}",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                    else -> {
                        Text(
                            text = "${next.entry.exercise.name} · ${next.label()}" + if (timing) " · timing" else "",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                        Text(
                            text = when {
                                timing -> formatDuration(runningSeconds(timer!!.startedAt))
                                !next.canLog -> if (next.entry.exercise.isTimed) "Enter a time first" else "Enter the reps first"
                                else -> formatSet(next.weightKg, next.reps, unit, next.durationSeconds)
                            },
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                        )
                    }
                }
            }
            when {
                next == null -> Button(onClick = onFinish, modifier = Modifier.height(52.dp)) { Text("Finish") }
                timing -> Button(onClick = { onStopTimer(next) }, modifier = Modifier.height(52.dp)) {
                    Icon(Icons.Filled.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Stop")
                }
                next.entry.exercise.isTimed -> Button(onClick = { onStartTimer(next) }, modifier = Modifier.height(52.dp)) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Start")
                }
                else -> Button(
                    onClick = { onLog(next) },
                    enabled = next.canLog,
                    modifier = Modifier
                        .height(52.dp)
                        .semantics { contentDescription = "Set done: ${next.entry.exercise.name}, ${next.label()}" },
                ) {
                    Text("Set done")
                }
            }
        }
    }
}

/** Seconds since [startedAt] on the wall clock, kept up to date. */
@Composable
private fun elapsedSeconds(startedAt: Long): Long {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(startedAt) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1_000)
        }
    }
    return ((now - startedAt) / 1000).coerceAtLeast(0)
}

/** Seconds since [startedAt] on the elapsed-time clock, kept up to date. */
@Composable
private fun runningSeconds(startedAt: Long): Long {
    var now by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    LaunchedEffect(startedAt) {
        while (true) {
            now = SystemClock.elapsedRealtime()
            delay(200)
        }
    }
    return ((now - startedAt) / 1000).coerceAtLeast(0)
}


/** Opens the timer and stopwatch. While the stopwatch runs, it shows the time instead. */
@Composable
private fun ClockButton(stopwatch: Stopwatch, onClick: () -> Unit) {
    if (stopwatch.startedAt == null) {
        IconButton(onClick = onClick) {
            Icon(Icons.Filled.Timer, contentDescription = "Timer and stopwatch")
        }
    } else {
        TextButton(onClick = onClick) {
            Icon(Icons.Filled.Timer, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(" ${formatDuration(stopwatchSeconds(stopwatch))}")
        }
    }
}

/** The stopwatch's time in whole seconds, kept up to date while it runs. */
@Composable
private fun stopwatchSeconds(stopwatch: Stopwatch): Long {
    var now by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    LaunchedEffect(stopwatch) {
        while (stopwatch.startedAt != null) {
            now = SystemClock.elapsedRealtime()
            delay(200)
        }
    }
    return stopwatch.elapsedMillis(now) / 1000
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ClockSheet(viewModel: WorkoutViewModel, stopwatch: Stopwatch, onDismiss: () -> Unit) {
    var showingStopwatch by rememberSaveable { mutableStateOf(stopwatch.startedAt != null) }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = !showingStopwatch, onClick = { showingStopwatch = false }, label = { Text("Timer") })
                FilterChip(selected = showingStopwatch, onClick = { showingStopwatch = true }, label = { Text("Stopwatch") })
            }
            if (showingStopwatch) {
                Text(
                    text = formatDuration(stopwatchSeconds(stopwatch)),
                    style = MaterialTheme.typography.displayMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = { if (stopwatch.startedAt == null) viewModel.startStopwatch() else viewModel.pauseStopwatch() },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(if (stopwatch.startedAt == null) "Start" else "Pause")
                    }
                    OutlinedButton(onClick = viewModel::resetStopwatch, modifier = Modifier.weight(1f)) {
                        Text("Reset")
                    }
                }
            } else {
                Text(
                    text = "Counts down at the bottom of the screen, like the rest timer, and plays the rest timer sound.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TimerChoices.forEach { seconds ->
                        OutlinedButton(
                            onClick = {
                                viewModel.startTimer(seconds)
                                onDismiss()
                            },
                        ) {
                            Text(formatDuration(seconds.toLong()))
                        }
                    }
                }
            }
        }
    }
}

private val TimerChoices = listOf(30, 60, 90, 120, 180, 300, 600)

@Composable
private fun RoutineChangedDialog(
    routineName: String,
    onUpdate: () -> Unit,
    onSaveAsNew: () -> Unit,
    onSkip: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update $routineName?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("This workout's exercises, sets or rep ranges are different from the routine.")
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onUpdate, modifier = Modifier.fillMaxWidth()) { Text("Update $routineName") }
                TextButton(onClick = onSaveAsNew, modifier = Modifier.fillMaxWidth()) { Text("Save as a new routine") }
                TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) { Text("Don't change routines") }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Back to workout") } },
    )
}

@Composable
private fun NewRoutineDialog(
    suggestedName: String,
    otherNames: List<String>,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(suggestedName) }
    val taken = isRoutineNameTaken(name, otherNames)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New routine") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Routine name") },
                supportingText = if (taken) {
                    { Text("You already have a routine with this name.") }
                } else {
                    null
                },
                isError = taken,
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(enabled = name.isNotBlank() && !taken, onClick = { onSave(name.trim()) }) { Text("Save and finish") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun RepRangeDialog(
    name: String,
    repsMin: Int?,
    repsMax: Int?,
    onSave: (Int?, Int?) -> Unit,
    onDismiss: () -> Unit,
) {
    var minText by rememberSaveable { mutableStateOf(repsMin?.toString().orEmpty()) }
    var maxText by rememberSaveable { mutableStateOf(repsMax?.toString().orEmpty()) }
    val range = parseRepRange(minText, maxText)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rep range") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "For $name in this workout. Leave the second box empty for a single target.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.width(64.dp)) {
                        NumberField(minText, "8", "Lowest reps", KeyboardType.Number) { if (isValidRepsInput(it)) minText = it }
                    }
                    Text("to")
                    Box(Modifier.width(64.dp)) {
                        NumberField(maxText, "12", "Highest reps", KeyboardType.Number) { if (isValidRepsInput(it)) maxText = it }
                    }
                    Text("reps")
                }
                if (range == null) {
                    Text(
                        text = "The highest can't be lower than the lowest.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(enabled = range != null, onClick = { range?.let { onSave(it.first, it.second) } }) { Text("Save") }
        },
        dismissButton = {
            Row {
                if (repsMin != null) TextButton(onClick = { onSave(null, null) }) { Text("Clear") }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        },
    )
}

/** Edits the note kept with an exercise, as opposed to the notes for this workout only. */
@Composable
private fun ExerciseNoteDialog(name: String, note: String, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    var text by rememberSaveable { mutableStateOf(note) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Note for $name") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Kept with the exercise and shown every time it's in a workout, unlike the notes for this workout.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("Seat height 4, back pad 3") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )
            }
        },
        confirmButton = { TextButton(onClick = { onSave(text) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

/** Notes for one exercise in this workout, saved as they're typed. */
@Composable
private fun ExerciseNotes(notes: String?, name: String, onChange: (String) -> Unit) {
    var text by remember { mutableStateOf(notes.orEmpty()) }
    BasicTextField(
        value = text,
        onValueChange = {
            text = it
            onChange(it)
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 16.dp, top = 4.dp, bottom = 4.dp)
            .semantics { contentDescription = "Notes for $name" },
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = { field ->
            Box {
                if (text.isEmpty()) {
                    Text(
                        text = "Add notes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                field()
            }
        },
    )
}

@Composable
private fun ConfirmationDialog(question: Confirmation, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(question.title) },
        text = { Text(question.message) },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    question.onConfirm()
                },
            ) {
                Text(question.confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

/** The list of exercises and sets, used for both a live workout and editing a saved one. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutEditor(
    workout: Workout,
    previousSets: Map<String, List<LoggedSet>>,
    viewModel: WorkoutEditorViewModel,
    title: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit,
    onAddExercise: () -> Unit,
    onReorder: () -> Unit,
    onReplace: (Long) -> Unit,
    onExerciseClick: (String) -> Unit,
    recordSetIds: Set<Long> = emptySet(),
    defaultRestSeconds: Int? = null,
    settings: AppSettings? = null,
    extraActions: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    header: @Composable () -> Unit = {},
    nextSetId: Long? = null,
) {
    var confirmation by remember { mutableStateOf<Confirmation?>(null) }
    var warmupFor by remember { mutableStateOf<Long?>(null) }
    var platesFor by remember { mutableStateOf<Long?>(null) }
    var supersetFor by remember { mutableStateOf<Long?>(null) }
    var repRangeFor by remember { mutableStateOf<Long?>(null) }
    var exerciseNoteFor by remember { mutableStateOf<Exercise?>(null) }
    val exerciseNotes by viewModel.exerciseNotes.collectAsStateWithLifecycle()
    val unit = LocalWeightUnit.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
    ) {
        TopAppBar(title = title, actions = actions)
        LazyColumn(modifier = Modifier.weight(1f)) {
            item(key = "header") { header() }
            workout.exercises.forEach { entry ->
                exerciseSection(
                    workout = workout,
                    entry = entry,
                    previous = previousSets[entry.exercise.id].orEmpty(),
                    recordSetIds = recordSetIds,
                    nextSetId = nextSetId,
                    unit = unit,
                    restSeconds = defaultRestSeconds?.let { entry.restSeconds ?: it },
                    defaultRestSeconds = defaultRestSeconds,
                    viewModel = viewModel,
                    onExerciseClick = onExerciseClick,
                    onWarmup = settings?.takeUnless { entry.exercise.isTimed || !entry.exercise.takesWeight }
                        ?.let { { warmupFor = entry.id } },
                    onPlates = settings?.takeIf { entry.exercise.equipment.usesBar }?.let { { platesFor = entry.id } },
                    onReorder = onReorder,
                    onReplace = {
                        if (entry.sets.any { it.completed }) {
                            confirmation = Confirmation(
                                title = "Replace ${entry.exercise.name}?",
                                message = "The sets you've logged for it will be cleared.",
                                confirmLabel = "Replace",
                                onConfirm = { onReplace(entry.id) },
                            )
                        } else {
                            onReplace(entry.id)
                        }
                    },
                    onAddToSuperset = { supersetFor = entry.id },
                    onRepRange = { repRangeFor = entry.id },
                    exerciseNote = exerciseNotes[entry.exercise.id],
                    onExerciseNote = { exerciseNoteFor = entry.exercise },
                    onRemove = {
                        confirmation = Confirmation(
                            title = "Remove ${entry.exercise.name}?",
                            message = "Its sets in this workout will be deleted.",
                            confirmLabel = "Remove",
                            onConfirm = { viewModel.removeExercise(entry.id) },
                        )
                    },
                )
            }
            item(key = "actions") {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(onClick = onAddExercise, modifier = Modifier.fillMaxWidth()) {
                        Text("Add exercise")
                    }
                    extraActions()
                }
            }
        }
        bottomBar()
    }

    confirmation?.let { ConfirmationDialog(it, onDismiss = { confirmation = null }) }
    exerciseNoteFor?.let { exercise ->
        ExerciseNoteDialog(
            name = exercise.name,
            note = exerciseNotes[exercise.id].orEmpty(),
            onSave = {
                exerciseNoteFor = null
                viewModel.setExerciseNote(exercise.id, it)
            },
            onDismiss = { exerciseNoteFor = null },
        )
    }
    workout.exercises.find { it.id == repRangeFor }?.let { entry ->
        RepRangeDialog(
            name = entry.exercise.name,
            repsMin = entry.targetRepsMin,
            repsMax = entry.targetRepsMax,
            onSave = { min, max ->
                repRangeFor = null
                viewModel.updateRepTarget(entry.id, min, max)
            },
            onDismiss = { repRangeFor = null },
        )
    }
    val supersetEntry = workout.exercises.find { it.id == supersetFor }
    if (supersetEntry != null) {
        SupersetDialog(
            choices = workout.exercises.filter { it.id != supersetEntry.id },
            onChoose = {
                supersetFor = null
                viewModel.addToSuperset(supersetEntry.id, it.id)
            },
            onDismiss = { supersetFor = null },
        )
    }
    val warmupEntry = workout.exercises.find { it.id == warmupFor }
    if (warmupEntry != null && settings != null) {
        WarmupDialog(
            entry = warmupEntry,
            previous = previousSets[warmupEntry.exercise.id].orEmpty(),
            settings = settings,
            onAdd = { viewModel.addWarmupSets(warmupEntry.id, it) },
            onDismiss = { warmupFor = null },
        )
    }
    val platesEntry = workout.exercises.find { it.id == platesFor }
    if (platesEntry != null && settings != null) {
        PlateCalculatorDialog(
            startingWeightKg = platesEntry.sets.lastOrNull { it.weightKg != null }?.weightKg
                ?: previousSets[platesEntry.exercise.id]?.firstOrNull()?.weightKg,
            settings = settings,
            onDismiss = { platesFor = null },
        )
    }
}

private fun setCount(count: Int) = if (count == 1) "1 set" else "$count sets"

@Composable
fun ElapsedTime(startedAt: Long) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1_000)
        }
    }
    Text(formatDuration(((now - startedAt) / 1000).coerceAtLeast(0)))
}

private fun LazyListScope.exerciseSection(
    workout: Workout,
    entry: WorkoutExercise,
    previous: List<LoggedSet>,
    recordSetIds: Set<Long>,
    nextSetId: Long?,
    unit: WeightUnit,
    restSeconds: Int?,
    defaultRestSeconds: Int?,
    viewModel: WorkoutEditorViewModel,
    onExerciseClick: (String) -> Unit,
    onWarmup: (() -> Unit)?,
    onPlates: (() -> Unit)?,
    onReorder: () -> Unit,
    onReplace: () -> Unit,
    onAddToSuperset: () -> Unit,
    onRepRange: () -> Unit,
    exerciseNote: String?,
    onExerciseNote: () -> Unit,
    onRemove: () -> Unit,
) {
    val partners = workout.supersetPartners(entry)
    val workSets = entry.sets.filterNot { it.isWarmup }

    item(key = "exercise-${entry.id}") {
        Column {
            HorizontalDivider(color = MaterialTheme.colorScheme.onBackground)
            Row(
                modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onExerciseClick(entry.exercise.id) },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ExerciseThumbnail(entry.exercise.id, size = 40.dp)
                    Text(
                        text = entry.exercise.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                ExerciseMenu(
                    name = entry.exercise.name,
                    hasOtherExercises = workout.exercises.size > 1,
                    inSuperset = partners.isNotEmpty(),
                    onReorder = onReorder,
                    onReplace = onReplace,
                    onAddToSuperset = onAddToSuperset,
                    onRepRange = onRepRange.takeUnless { entry.exercise.isTimed },
                    hasExerciseNote = exerciseNote != null,
                    onExerciseNote = onExerciseNote,
                    onRemoveFromSuperset = { viewModel.removeFromSuperset(entry.id) },
                    onRemove = onRemove,
                )
            }
            if (partners.isNotEmpty()) {
                Row(
                    modifier = Modifier.padding(start = 20.dp, top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Link,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "Superset with ${partners.joinToString { it.exercise.name }}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            exerciseNote?.let { note ->
                Row(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, top = 4.dp)
                        .clip(MaterialTheme.shapes.small)
                        .clickable(onClick = onExerciseNote)
                        .padding(4.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.PushPin,
                        contentDescription = "Note kept with ${entry.exercise.name}",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(note, style = MaterialTheme.typography.bodyMedium)
                }
            }
            ExerciseNotes(entry.notes, entry.exercise.name) { viewModel.updateNotes(entry.id, it) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                restSeconds?.let { seconds ->
                    RestTimerControl(
                        seconds = seconds,
                        defaultSeconds = defaultRestSeconds,
                        isOwn = entry.restSeconds != null,
                        onChange = { viewModel.updateRestSeconds(entry.id, it) },
                    )
                }
                onWarmup?.let { TextButton(onClick = it) { Text("Warm-up") } }
                onPlates?.let { TextButton(onClick = it) { Text("Plates") } }
            }
            formatRepsTarget(entry.targetRepsMin, entry.targetRepsMax)?.takeUnless { entry.exercise.isTimed }?.let { target ->
                Text(
                    text = "Target: $target reps",
                    modifier = Modifier
                        .padding(start = 16.dp, bottom = 4.dp)
                        .clip(MaterialTheme.shapes.small)
                        .clickable(onClick = onRepRange)
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            SetColumns(
                modifier = Modifier.padding(horizontal = 16.dp),
                hasTimer = entry.exercise.isTimed && viewModel.setTimer != null,
                number = { ColumnLabel("Set") },
                previous = { ColumnLabel("Previous") },
                weight = { if (entry.exercise.takesWeight) ColumnLabel(unit.label, TextAlign.Center) },
                reps = { ColumnLabel(if (entry.exercise.isTimed) "Time" else "Reps", TextAlign.Center) },
                actions = {},
            )
        }
    }
    itemsIndexed(entry.sets, key = { _, set -> "set-${set.id}" }) { _, set ->
        // Warm-ups have their own label, and don't shift the numbering or suggestions of the rest.
        val workIndex = workSets.indexOf(set)
        val warmupIndex = entry.sets.filter { it.isWarmup }.indexOf(set)
        SetRow(
            label = setLabel(set.setType, if (set.isWarmup) warmupIndex + 1 else workIndex + 1),
            number = if (set.isWarmup) null else workIndex + 1,
            set = set,
            isRecord = set.id in recordSetIds,
            isNext = set.id == nextSetId,
            isTimed = entry.exercise.isTimed,
            takesWeight = entry.exercise.takesWeight,
            previous = if (set.isWarmup) null else previous.getOrNull(workIndex),
            hint = if (set.isWarmup) null else setHint(workSets, workIndex, previous, entry.targetRepsMin.takeUnless { entry.exercise.isTimed }),
            unit = unit,
            restSeconds = restSeconds.takeIf { workout.restsAfter(entry) },
            viewModel = viewModel,
        )
    }
    item(key = "add-set-${entry.id}") {
        OutlinedButton(
            onClick = { viewModel.addSet(entry.id) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text("Add set")
        }
    }
}

/** The options for an exercise, behind three dots. */
@Composable
private fun ExerciseMenu(
    name: String,
    hasOtherExercises: Boolean,
    inSuperset: Boolean,
    onReorder: () -> Unit,
    onReplace: () -> Unit,
    onAddToSuperset: () -> Unit,
    onRepRange: (() -> Unit)?,
    hasExerciseNote: Boolean,
    onExerciseNote: () -> Unit,
    onRemoveFromSuperset: () -> Unit,
    onRemove: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }

    @Composable
    fun Option(label: String, enabled: Boolean = true, color: Color = Color.Unspecified, action: () -> Unit) {
        DropdownMenuItem(
            text = { Text(label, color = if (enabled) color else Color.Unspecified) },
            enabled = enabled,
            onClick = {
                menuOpen = false
                action()
            },
        )
    }

    Box {
        IconButton(onClick = { menuOpen = true }) {
            Icon(Icons.Filled.MoreVert, contentDescription = "Options for $name")
        }
        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
            Option("Reorder exercises", enabled = hasOtherExercises, action = onReorder)
            Option("Replace exercise", action = onReplace)
            onRepRange?.let { Option("Set rep range", action = it) }
            Option(if (hasExerciseNote) "Edit exercise note" else "Add exercise note", action = onExerciseNote)
            if (inSuperset) {
                Option("Remove from superset", action = onRemoveFromSuperset)
            } else {
                Option("Add to superset", enabled = hasOtherExercises, action = onAddToSuperset)
            }
            Option("Remove exercise", color = MaterialTheme.colorScheme.error, action = onRemove)
        }
    }
}

/** Chooses the exercise to pair this one with. */
@Composable
private fun SupersetDialog(
    choices: List<WorkoutExercise>,
    onChoose: (WorkoutExercise) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Superset with") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                choices.forEach { choice ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .clickable { onChoose(choice) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        ExerciseThumbnail(choice.exercise.id, size = 40.dp)
                        Text(choice.exercise.name, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

/** The rest for the whole workout, above the exercises. Exercises can still set their own. */
@Composable
private fun WorkoutRestControl(workoutRest: Int?, globalRest: Int, onChange: (Int?) -> Unit) {
    var menuOpen by remember { mutableStateOf(false) }
    Box(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp)) {
        Row(
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .clickable { menuOpen = true }
                .padding(vertical = 6.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Timer,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Rest for this workout: " +
                    if (workoutRest == null) "Settings (${formatRest(globalRest)})" else formatRest(workoutRest),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
            DropdownMenuItem(
                text = { Text("Settings (${formatRest(globalRest)})") },
                onClick = {
                    menuOpen = false
                    onChange(null)
                },
            )
            AppSettings.RestSecondsChoices.forEach { choice ->
                DropdownMenuItem(
                    text = { Text(formatRest(choice)) },
                    onClick = {
                        menuOpen = false
                        onChange(choice)
                    },
                )
            }
        }
    }
}

/**
 * Shows how long this exercise rests for, and lets it be changed from a menu. [defaultSeconds] is
 * what applies when the exercise has no rest of its own, and [isOwn] whether it has one.
 */
@Composable
private fun RestTimerControl(seconds: Int, defaultSeconds: Int?, isOwn: Boolean, onChange: (Int?) -> Unit) {
    var menuOpen by remember { mutableStateOf(false) }

    Box(modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp)) {
        Row(
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .clickable { menuOpen = true }
                .padding(vertical = 6.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Timer,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "Rest timer: ${formatRest(seconds)}" + if (isOwn) "" else " (default)",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
            defaultSeconds?.let { default ->
                DropdownMenuItem(
                    text = { Text("Default (${formatRest(default)})") },
                    onClick = {
                        menuOpen = false
                        onChange(null)
                    },
                )
            }
            AppSettings.RestSecondsChoices.forEach { choice ->
                DropdownMenuItem(
                    text = { Text(formatRest(choice)) },
                    onClick = {
                        menuOpen = false
                        onChange(choice)
                    },
                )
            }
        }
    }
}

@Composable
private fun ColumnLabel(text: String, align: TextAlign = TextAlign.Start) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = align,
    )
}

/** Lays out a row of the set table, so that the labels line up with the sets below them. */
@Composable
private fun SetColumns(
    modifier: Modifier,
    hasTimer: Boolean,
    number: @Composable () -> Unit,
    previous: @Composable () -> Unit,
    weight: @Composable () -> Unit,
    reps: @Composable () -> Unit,
    actions: @Composable () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(Modifier.width(48.dp)) { number() }
        Box(Modifier.weight(1f)) { previous() }
        Box(Modifier.width(62.dp)) { weight() }
        Box(Modifier.width(50.dp)) { reps() }
        Row(Modifier.width(if (hasTimer) 88.dp else 48.dp), horizontalArrangement = Arrangement.End) { actions() }
    }
}

@Composable
private fun SetRow(
    label: String,
    number: Int?,
    set: WorkoutSet,
    isRecord: Boolean,
    isNext: Boolean,
    isTimed: Boolean,
    takesWeight: Boolean,
    previous: LoggedSet?,
    hint: SetHint?,
    unit: WeightUnit,
    restSeconds: Int?,
    viewModel: WorkoutEditorViewModel,
) {
    fun shown(weightKg: Double?) = weightKg?.let { formatWeight(it, unit) }.orEmpty()
    fun shownTime(seconds: Int?) = seconds?.let { formatDuration(it.toLong()) }.orEmpty()

    var weightText by remember(set.id, unit) { mutableStateOf(shown(set.weightKg)) }
    var repsText by remember(set.id) { mutableStateOf(set.reps?.toString().orEmpty()) }
    var timeText by remember(set.id) { mutableStateOf(shownTime(set.durationSeconds)) }

    // Picks up the suggested numbers once a blank set is checked off. It leaves text like "22."
    // alone, because that already means the same weight as what's saved.
    LaunchedEffect(set.weightKg) {
        if (!sameWeight(parseWeight(weightText, unit), set.weightKg)) weightText = shown(set.weightKg)
    }
    LaunchedEffect(set.reps) {
        if (parseReps(repsText) != set.reps) repsText = set.reps?.toString().orEmpty()
    }
    LaunchedEffect(set.durationSeconds) {
        if (parseDuration(timeText) != set.durationSeconds) timeText = shownTime(set.durationSeconds)
    }

    val running by (viewModel.setTimer ?: remember { MutableStateFlow(null) }).collectAsStateWithLifecycle()
    val timingThisSet = running?.setId == set.id

    val repsToLog = parseReps(repsText) ?: hint?.reps
    val timeToLog = parseDuration(timeText) ?: hint?.durationSeconds
    val canComplete = if (isTimed) timeToLog != null else repsToLog != null
    val name = if (number == null) "warm-up set" else "set $number"
    val rowColor = if (set.completed) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val rest = restSeconds.takeUnless { set.isWarmup }

    fun complete(seconds: Int?) {
        val weight = if (takesWeight) parseWeight(weightText, unit) ?: hint?.weightKg else null
        if (isTimed) {
            viewModel.completeSet(set.id, weight, null, rest, seconds)
        } else {
            viewModel.completeSet(set.id, weight, repsToLog, rest)
        }
    }

    SetColumns(
        modifier = Modifier
            .background(rowColor)
            .then(if (isNext) Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small) else Modifier)
            .padding(horizontal = 16.dp),
        hasTimer = isTimed && viewModel.setTimer != null,
        number = {
            SetOptions(
                label = label,
                name = name,
                type = set.setType,
                isRecord = isRecord,
                onChange = { viewModel.updateSetType(set.id, it) },
                onRemove = {
                    if (timingThisSet) viewModel.stopSetTimer()
                    viewModel.removeSet(set.id)
                },
            )
        },
        previous = {
            Text(
                text = previous?.let { formatSet(it.weightKg, it.reps, unit, it.durationSeconds) } ?: "-",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        weight = {
            if (takesWeight) NumberField(
                value = weightText,
                placeholder = shown(hint?.weightKg),
                description = "Weight for $name",
                keyboardType = KeyboardType.Decimal,
                onValueChange = { text ->
                    if (isValidWeightInput(text)) {
                        weightText = text
                        viewModel.updateWeight(set.id, parseWeight(text, unit))
                    }
                },
            )
        },
        reps = {
            when {
                timingThisSet -> RunningTime(running?.startedAt ?: 0)
                isTimed -> NumberField(
                    value = timeText,
                    placeholder = shownTime(hint?.durationSeconds).ifEmpty { "sec" },
                    description = "Time for $name",
                    keyboardType = KeyboardType.Number,
                    onValueChange = { text ->
                        if (isValidDurationInput(text)) {
                            timeText = text
                            viewModel.updateDuration(set.id, parseDuration(text))
                        }
                    },
                )
                else -> NumberField(
                    value = repsText,
                    placeholder = hint?.reps?.toString().orEmpty(),
                    description = "Reps for $name",
                    keyboardType = KeyboardType.Number,
                    onValueChange = { text ->
                        if (isValidRepsInput(text)) {
                            repsText = text
                            viewModel.updateReps(set.id, parseReps(text))
                        }
                    },
                )
            }
        },
        actions = {
            if (isTimed && viewModel.setTimer != null && !set.completed) {
                IconButton(
                    onClick = {
                        if (timingThisSet) {
                            viewModel.stopSetTimer()?.let { complete(it) }
                        } else {
                            viewModel.startSetTimer(set.id)
                        }
                    },
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        imageVector = if (timingThisSet) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                        contentDescription = if (timingThisSet) "Stop timer for $name" else "Start timer for $name",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            IconButton(
                enabled = set.completed || canComplete,
                onClick = {
                    if (set.completed) {
                        viewModel.uncompleteSet(set.id)
                    } else if (canComplete) {
                        complete(timeToLog)
                    }
                },
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = if (set.completed) "Undo $name" else "Complete $name",
                    tint = if (set.completed) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else if (canComplete) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    },
                )
            }
        },
    )
}

/** A set's number and type. Tapping it opens a menu to change the type or remove the set. */
@Composable
private fun SetOptions(
    label: String,
    name: String,
    type: SetType,
    isRecord: Boolean,
    onChange: (SetType) -> Unit,
    onRemove: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .clickable { menuOpen = true }
                .semantics {
                    contentDescription = "Options for $name: ${type.label}" + if (isRecord) ", personal record" else ""
                }
                .padding(vertical = 10.dp, horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (type == SetType.Normal) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary,
            )
            if (isRecord) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
            SetType.entries.forEach { choice ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(choice.label)
                            if (choice == SetType.Warmup) {
                                Text(
                                    text = "Not counted in totals or records",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    },
                    onClick = {
                        menuOpen = false
                        onChange(choice)
                    },
                )
            }
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("Remove set", color = MaterialTheme.colorScheme.error) },
                onClick = {
                    menuOpen = false
                    onRemove()
                },
            )
        }
    }
}

/** The stopwatch inside a set's row, counting up from [startedAt]. */
@Composable
private fun RunningTime(startedAt: Long) {
    var now by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    LaunchedEffect(startedAt) {
        while (true) {
            now = SystemClock.elapsedRealtime()
            delay(200)
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.small),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = formatDuration(((now - startedAt) / 1000).coerceAtLeast(0)),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
fun NumberField(
    value: String,
    placeholder: String,
    description: String,
    keyboardType: KeyboardType,
    onValueChange: (String) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = description },
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.merge(
            TextStyle(color = colors.onSurface, textAlign = TextAlign.Center),
        ),
        cursorBrush = SolidColor(colors.primary),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Next),
        decorationBox = { field ->
            Box(
                modifier = Modifier
                    .height(40.dp)
                    .background(colors.surfaceVariant, MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center,
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.onSurfaceVariant,
                    )
                }
                field()
            }
        },
    )
}

@Composable
private fun RestBar(seconds: Int, nextLabel: String?, onAdjust: (Int) -> Unit, onSkip: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.primaryContainer) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                nextLabel?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                    )
                }
                Text(
                    text = "Rest ${formatDuration(seconds.toLong())}",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            val buttonColors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            TextButton(onClick = { onAdjust(-15) }, colors = buttonColors) { Text("-15s") }
            TextButton(onClick = { onAdjust(15) }, colors = buttonColors) { Text("+15s") }
            TextButton(onClick = onSkip, colors = buttonColors) { Text("Skip") }
        }
    }
}
