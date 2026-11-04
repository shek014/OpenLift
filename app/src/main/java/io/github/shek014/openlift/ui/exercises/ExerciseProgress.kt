package io.github.shek014.openlift.ui.exercises

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.shek014.openlift.data.model.ChartRange
import io.github.shek014.openlift.data.model.LoggedSet
import io.github.shek014.openlift.data.model.ProgressMetric
import io.github.shek014.openlift.data.model.availableMetrics
import io.github.shek014.openlift.data.model.chartValue
import io.github.shek014.openlift.data.model.format
import io.github.shek014.openlift.data.model.formatDate
import io.github.shek014.openlift.data.model.formatShortDate
import io.github.shek014.openlift.data.model.personalRecords
import io.github.shek014.openlift.data.model.progressPoints
import io.github.shek014.openlift.data.model.since
import io.github.shek014.openlift.ui.LocalWeightUnit
import io.github.shek014.openlift.ui.charts.LineChart

/** A graph of one exercise over time, with a choice of what to measure and how far back to look. */
@Composable
fun ExerciseProgressChart(logged: List<LoggedSet>) {
    val unit = LocalWeightUnit.current
    val metrics = remember(logged) { availableMetrics(logged) }
    var chosenMetric by rememberSaveable { mutableStateOf(metrics.first()) }
    var range by rememberSaveable { mutableStateOf(ChartRange.ThreeMonths) }
    val metric = chosenMetric.takeIf { it in metrics } ?: metrics.first()

    val points = remember(logged, metric, range) {
        progressPoints(logged, metric, since = range.since(System.currentTimeMillis()))
    }
    var selected by remember(points) { mutableStateOf(points.lastIndex) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ChipRow(
            items = metrics,
            selected = metric,
            label = { it.label },
            onClick = { chosenMetric = it },
        )
        ChipRow(
            items = ChartRange.entries,
            selected = range,
            label = { it.label },
            onClick = { range = it },
        )
        if (points.isEmpty()) {
            Text(
                text = "No workouts with this exercise in this time. Try a longer range.",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            val point = points[selected.coerceIn(points.indices)]
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(metric.format(point.value, unit), style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = formatDate(point.time),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            LineChart(
                times = points.map { it.time },
                values = points.map { metric.chartValue(it.value, unit) },
                selectedIndex = selected.coerceIn(points.indices),
                firstLabel = formatShortDate(points.first().time),
                lastLabel = formatShortDate(points.last().time),
                description = "${metric.label} over ${points.size} workouts. " +
                    "Latest: ${metric.format(points.last().value, unit)}.",
                onSelect = { selected = it },
                modifier = Modifier.padding(horizontal = 8.dp),
            )
        }
    }
}

/** The best ever for each metric. */
@Composable
fun PersonalRecordsList(logged: List<LoggedSet>) {
    val unit = LocalWeightUnit.current
    val records = remember(logged) { personalRecords(logged) }

    Column {
        records.forEach { record ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(record.metric.label, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = formatDate(record.achievedAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = record.metric.format(record.value, unit),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}
