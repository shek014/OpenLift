package io.github.shek014.openlift.ui.exercises

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import io.github.shek014.openlift.data.model.Exercise
import io.github.shek014.openlift.data.model.LoggedSet
import io.github.shek014.openlift.data.model.SetType
import io.github.shek014.openlift.data.model.formatDate
import io.github.shek014.openlift.data.model.formatSet
import io.github.shek014.openlift.ui.LocalWeightUnit
import io.github.shek014.openlift.ui.body.BodyMap
import io.github.shek014.openlift.ui.body.muscleEmphasis

/** One page for an exercise, read from top to bottom like a page in a training log. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    onBack: () -> Unit,
    onEdit: (exerciseId: String) -> Unit,
    viewModel: ExerciseDetailViewModel = viewModel(factory = ExerciseDetailViewModel.Factory),
) {
    val details by viewModel.details.collectAsStateWithLifecycle()
    val log by viewModel.log.collectAsStateWithLifecycle()
    val note by viewModel.note.collectAsStateWithLifecycle()
    var confirmingDelete by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {},
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                details?.exercise?.takeIf { it.isCustom && !it.isArchived }?.let { exercise ->
                    TextButton(onClick = { onEdit(exercise.id) }) { Text("Edit") }
                    TextButton(onClick = { confirmingDelete = true }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
        )
        details?.let {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp),
            ) {
                ExerciseHeading(it.exercise)
                ExerciseAnimation(it)
                note?.let { saved -> NoteSection(saved, viewModel::setNote) }
                MusclesSection(it.exercise)
                HowToSection(it.howToSteps)
                ProgressSection(log)
                LogSection(log)
            }
        }
    }

    if (confirmingDelete) {
        AlertDialog(
            onDismissRequest = { confirmingDelete = false },
            title = { Text("Delete this exercise?") },
            text = { Text("It won't appear in your lists any more. Workouts you've already logged with it are kept.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmingDelete = false
                        viewModel.deleteCustomExercise()
                        onBack()
                    },
                ) {
                    Text("Delete")
                }
            },
            dismissButton = { TextButton(onClick = { confirmingDelete = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun ExerciseHeading(exercise: Exercise) {
    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)) {
        Text(exercise.name, style = MaterialTheme.typography.headlineMedium)
        Text(
            text = exercise.equipment.label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

/** Draws the black and white GIF in the theme's ink and paper, so that it sits on the page. */
@Composable
private fun inkOnPage(): ColorFilter {
    val ink = MaterialTheme.colorScheme.onBackground
    val paper = MaterialTheme.colorScheme.background
    return remember(ink, paper) { inkAndPaperFilter(ink, paper) }
}

/** Shows nothing for exercises that don't have an animation yet. */
@Composable
private fun ExerciseAnimation(details: ExerciseDetails) {
    val uri = details.animationUri ?: return
    HorizontalDivider(color = MaterialTheme.colorScheme.onBackground)
    AsyncImage(
        model = uri,
        contentDescription = "Animation of ${details.exercise.name}",
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(8.dp),
        contentScale = ContentScale.Fit,
        colorFilter = inkOnPage(),
    )
    HorizontalDivider(color = MaterialTheme.colorScheme.onBackground)
    AnimationCredit()
}

/** The drawings' license requires this credit wherever they're shown. */
@Composable
private fun AnimationCredit() {
    val linkStyle = SpanStyle(color = MaterialTheme.colorScheme.primary)
    Text(
        text = buildAnnotatedString {
            append("Animated from drawings by ")
            withLink(LinkAnnotation.Url("https://github.com/everkinetic/data")) {
                withStyle(linkStyle) { append("Everkinetic") }
            }
            append(", ")
            withLink(LinkAnnotation.Url("https://creativecommons.org/licenses/by-sa/4.0/")) {
                withStyle(linkStyle) { append("CC BY-SA 4.0") }
            }
        },
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 4.dp),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
    )
    HorizontalDivider(color = MaterialTheme.colorScheme.onBackground)
}

/** The person's own note for the exercise, saved as it's typed. */
@Composable
private fun NoteSection(saved: String, onChange: (String) -> Unit) {
    var text by remember { mutableStateOf(saved) }
    SectionTitle("Your note")
    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
            onChange(it)
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp),
        placeholder = { Text("Seat height, grip, anything to remember") },
        supportingText = { Text("Shows here and every time this exercise is in a workout.") },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
    )
}

@Composable
private fun MusclesSection(exercise: Exercise) {
    SectionTitle("Muscles")
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        BodyMap(
            emphasis = listOf(exercise).muscleEmphasis(),
            modifier = Modifier
                .width(260.dp)
                .padding(16.dp),
        )
    }
    MuscleLine(
        label = "Primary",
        muscles = exercise.primaryMuscles.joinToString { it.label },
        color = MaterialTheme.colorScheme.primary,
    )
    if (exercise.secondaryMuscles.isNotEmpty()) {
        MuscleLine(
            label = "Secondary",
            muscles = exercise.secondaryMuscles.joinToString { it.label },
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
        )
    }
}

@Composable
private fun MuscleLine(label: String, muscles: String, color: Color) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(color = color, shape = CircleShape, modifier = Modifier.size(12.dp)) {}
        Text("$label: $muscles", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun HowToSection(steps: List<String>) {
    if (steps.isEmpty()) return
    SectionTitle("How to")
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        steps.forEachIndexed { index, step ->
            Row {
                Text(
                    text = "${index + 1}.",
                    modifier = Modifier.width(28.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(step, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

/** Only appears once there's something to show. */
@Composable
private fun ProgressSection(log: List<LoggedSet>) {
    if (log.isEmpty()) return
    SectionTitle("Progress")
    Column(modifier = Modifier.padding(top = 12.dp)) {
        ExerciseProgressChart(log)
    }
    SectionTitle("Personal records")
    PersonalRecordsList(log)
    RepBestsDropdown(log)
}

@Composable
private fun LogSection(log: List<LoggedSet>) {
    SectionTitle("Your log")
    if (log.isEmpty()) {
        Text(
            text = "Every set you log of this exercise will appear here, with your progress and personal records.",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    val unit = LocalWeightUnit.current
    val workouts = remember(log) { log.groupBy { it.workoutId }.values.toList() }
    var showAll by rememberSaveable { mutableStateOf(false) }
    val shown = if (showAll) workouts else workouts.take(RecentLogWorkouts)

    shown.forEach { sets ->
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = formatDate(sets.first().finishedAt),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            sets.forEachIndexed { index, set ->
                Text("${index + 1}.  ${formatSet(set.weightKg, set.reps, unit, set.durationSeconds)}${setTypeSuffix(set.setType)}", style = MaterialTheme.typography.bodyMedium)
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
    if (shown.size < workouts.size) {
        TextButton(
            onClick = { showAll = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            Text("Show all ${workouts.size} workouts")
        }
    }
}

private fun setTypeSuffix(type: SetType) = if (type == SetType.Normal) "" else " (${type.label})"

/** How many workouts the log shows at first, so that a long history doesn't bury the page. */
private const val RecentLogWorkouts = 5
