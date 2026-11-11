package io.github.shek014.lifttrail.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.shek014.lifttrail.LiftTrailApplication
import io.github.shek014.lifttrail.data.WorkoutRepository
import io.github.shek014.lifttrail.data.model.ChartRange
import io.github.shek014.lifttrail.data.model.Exercise
import io.github.shek014.lifttrail.data.model.LoggedSet
import io.github.shek014.lifttrail.data.model.Muscle
import io.github.shek014.lifttrail.data.model.MuscleGroup
import io.github.shek014.lifttrail.data.model.StatsRange
import io.github.shek014.lifttrail.data.model.WorkoutStatistics
import io.github.shek014.lifttrail.data.model.chartValue
import io.github.shek014.lifttrail.data.model.format
import io.github.shek014.lifttrail.data.model.formatDate
import io.github.shek014.lifttrail.data.model.formatDuration
import io.github.shek014.lifttrail.data.model.formatShortDate
import io.github.shek014.lifttrail.data.model.formatVolume
import io.github.shek014.lifttrail.data.model.orderedWeekdays
import io.github.shek014.lifttrail.data.model.progressPoints
import io.github.shek014.lifttrail.data.model.repBests
import io.github.shek014.lifttrail.data.model.since
import io.github.shek014.lifttrail.data.model.statisticsMetrics
import io.github.shek014.lifttrail.data.model.ProgressMetric
import io.github.shek014.lifttrail.data.model.workoutStatistics
import io.github.shek014.lifttrail.ui.LocalWeightUnit
import io.github.shek014.lifttrail.ui.charts.LineChart
import io.github.shek014.lifttrail.ui.exercises.ChipRow
import io.github.shek014.lifttrail.ui.exercises.RepBestsDropdown
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

class StatisticsViewModel(private val repository: WorkoutRepository) : ViewModel() {
    var range by mutableStateOf(StatsRange.NinetyDays)
    var chosenExerciseId by mutableStateOf<String?>(null)

    /** The statistics for [range], or null until they've been worked out. */
    val statistics: StateFlow<WorkoutStatistics?> =
        combine(repository.finishedWorkouts, snapshotFlow { range }) { workouts, range ->
            workoutStatistics(workouts, range, LocalDate.now(), ZoneId.systemDefault(), Locale.getDefault())
        }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Everything ever done, most recent first, for choosing which exercise to graph. */
    val exercises: StateFlow<List<Exercise>> =
        repository.finishedWorkouts
            .map { workouts -> workouts.flatMap { it.completedExercises }.map { it.exercise }.distinctBy { it.id } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** The sets of the exercise being graphed: the chosen one, or else the most recent one. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val logged: StateFlow<List<LoggedSet>> =
        combine(exercises, snapshotFlow { chosenExerciseId }) { all, chosen -> chosen ?: all.firstOrNull()?.id }
            .filterNotNull()
            .flatMapLatest { repository.loggedSets(listOf(it)) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                StatisticsViewModel((this[APPLICATION_KEY] as LiftTrailApplication).workoutRepository)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onBack: () -> Unit,
    viewModel: StatisticsViewModel = viewModel(factory = StatisticsViewModel.Factory),
) {
    val statistics by viewModel.statistics.collectAsStateWithLifecycle()
    val exercises by viewModel.exercises.collectAsStateWithLifecycle()
    val logged by viewModel.logged.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Statistics") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
        ) {
            ChipRow(
                items = StatsRange.entries,
                selected = viewModel.range,
                label = { it.label },
                onClick = { viewModel.range = it },
            )
            statistics?.let { Overview(it) }
            if (exercises.isNotEmpty()) {
                SectionTitle("Exercise graphs")
                val current = exercises.find { it.id == viewModel.chosenExerciseId } ?: exercises.first()
                ExercisePicker(
                    current = current,
                    exercises = exercises,
                    onChoose = { viewModel.chosenExerciseId = it.id },
                )
                ExerciseGraphs(logged)
                if (repBests(logged).isNotEmpty()) {
                    SectionTitle("Personal bests")
                    RepBestsDropdown(logged)
                }
            }
        }
    }
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

@Composable
private fun Overview(stats: WorkoutStatistics) {
    val unit = LocalWeightUnit.current
    if (stats.workouts == 0) {
        Text(
            text = "No workouts in this time. Try a longer range.",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    SectionTitle("Totals")
    Tiles(
        "Workouts" to stats.workouts.toString(),
        "Time" to formatDuration(stats.totalSeconds),
        "Sets" to stats.sets.toString(),
        "Reps" to stats.reps.toString(),
        "Volume" to formatVolume(stats.volumeKg, unit),
        "Records set" to stats.recordsSet.toString(),
    )
    stats.volumeChangePercent?.let { change ->
        Text(
            text = "Volume ${if (change >= 0) "up" else "down"} ${kotlin.math.abs(change).roundToInt()}% on the period before",
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    SectionTitle("Averages")
    Tiles(
        "Per week" to String.format(Locale.ROOT, "%.1f workouts", stats.workoutsPerWeek),
        "Workout length" to formatDuration(stats.averageSeconds),
        "Sets per workout" to String.format(Locale.ROOT, "%.1f", stats.averageSets),
        "Volume per workout" to formatVolume(stats.averageVolumeKg, unit),
    )

    SectionTitle("Consistency")
    Tiles(
        "Current streak" to weeks(stats.currentStreakWeeks),
        "Longest streak" to weeks(stats.longestStreakWeeks),
    )
    val locale = Locale.getDefault()
    val busiest = stats.byWeekday.values.maxOrNull() ?: 0
    orderedWeekdays(locale).forEach { day ->
        val count = stats.byWeekday[day] ?: 0
        ShareBar(day.getDisplayName(TextStyle.SHORT, locale), count.toString(), count.toFloat() / busiest.coerceAtLeast(1))
    }

    if (stats.setsByMuscle.isNotEmpty()) {
        SectionTitle("Sets by muscle")
        Text(
            text = "Secondary muscles count as half a set.",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        val most = stats.setsByMuscle.values.max()
        MuscleGroup.entries.forEach { group ->
            val muscles = Muscle.entries.filter { it.group == group && it in stats.setsByMuscle }
            muscles.forEach { muscle ->
                val sets = stats.setsByMuscle.getValue(muscle)
                ShareBar(muscle.label, formatSets(sets), (sets / most).toFloat())
            }
        }
    }

    if (stats.topExercises.isNotEmpty()) {
        SectionTitle("Most done exercises")
        stats.topExercises.forEach { (exercise, sets) ->
            ShareBar(
                exercise.name,
                sets.toString(),
                sets.toFloat() / stats.topExercises.first().sets,
            )
        }
    }
}

private fun weeks(count: Int) = if (count == 1) "1 week" else "$count weeks"

private fun formatSets(sets: Double) = if (sets % 1.0 == 0.0) sets.toInt().toString() else sets.toString()

@Composable
private fun Tiles(vararg tiles: Pair<String, String>) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        tiles.toList().chunked(2).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEach { (label, value) ->
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(value, style = MaterialTheme.typography.titleLarge)
                    }
                }
                if (row.size == 1) Box(Modifier.weight(1f))
            }
        }
    }
}

/** A row with a bar showing [share] of the largest value in the list. */
@Composable
private fun ShareBar(label: String, value: String, share: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(label, modifier = Modifier.width(120.dp), style = MaterialTheme.typography.bodySmall, maxLines = 1)
        Box(
            modifier = Modifier
                .weight(1f)
                .height(12.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(share.coerceIn(0f, 1f))
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
        Text(value, modifier = Modifier.width(36.dp), style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun ExercisePicker(current: Exercise, exercises: List<Exercise>, onChoose: (Exercise) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box(modifier = Modifier.padding(16.dp)) {
        OutlinedButton(onClick = { open = true }, modifier = Modifier.fillMaxWidth()) {
            Text(current.name)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            exercises.forEach { exercise ->
                DropdownMenuItem(
                    text = { Text(exercise.name) },
                    onClick = {
                        open = false
                        onChoose(exercise)
                    },
                )
            }
        }
    }
}

/** One graph for each measure that applies to this exercise. */
@Composable
private fun ExerciseGraphs(logged: List<LoggedSet>) {
    var range by rememberSaveable { mutableStateOf(ChartRange.All) }
    ChipRow(
        items = ChartRange.entries,
        selected = range,
        label = { it.label },
        onClick = { range = it },
    )
    statisticsMetrics(logged).forEach { metric ->
        MetricGraph(logged, metric, range)
    }
}

@Composable
private fun MetricGraph(logged: List<LoggedSet>, metric: ProgressMetric, range: ChartRange) {
    val unit = LocalWeightUnit.current
    val points = remember(logged, metric, range) {
        progressPoints(logged, metric, since = range.since(System.currentTimeMillis()))
    }
    var selected by remember(points) { mutableStateOf(points.lastIndex) }

    Column(
        modifier = Modifier.padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = metric.label,
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.titleMedium,
        )
        if (points.isEmpty()) {
            Text(
                text = "Nothing in this time.",
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            val point = points[selected.coerceIn(points.indices)]
            Text(
                text = "${metric.format(point.value, unit)} · ${formatDate(point.time)}",
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            LineChart(
                times = points.map { it.time },
                values = points.map { metric.chartValue(it.value, unit) },
                selectedIndex = selected.coerceIn(points.indices),
                firstLabel = formatShortDate(points.first().time),
                lastLabel = formatShortDate(points.last().time),
                description = "${metric.label} over ${points.size} workouts.",
                onSelect = { selected = it },
                modifier = Modifier.padding(horizontal = 8.dp),
            )
        }
    }
}
