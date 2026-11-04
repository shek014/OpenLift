package io.github.shek014.openlift.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
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
import io.github.shek014.openlift.data.model.WeeklyMetric
import io.github.shek014.openlift.data.model.Workout
import io.github.shek014.openlift.data.model.chartValue
import io.github.shek014.openlift.data.model.format
import io.github.shek014.openlift.data.model.formatShortDate
import io.github.shek014.openlift.data.model.weeklyTotals
import io.github.shek014.openlift.ui.LocalWeightUnit
import io.github.shek014.openlift.ui.charts.BarChart
import io.github.shek014.openlift.ui.exercises.ChipRow
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

private enum class WeekRange(val label: String, val weeks: Int) {
    ThreeMonths("3M", 13),
    SixMonths("6M", 26),
    OneYear("1Y", 52),
}

/** A graph of all workouts, week by week. */
@Composable
fun OverallProgressChart(workouts: List<Workout>) {
    val unit = LocalWeightUnit.current
    var metric by rememberSaveable { mutableStateOf(WeeklyMetric.Volume) }
    var range by rememberSaveable { mutableStateOf(WeekRange.ThreeMonths) }

    val totals = remember(workouts, metric, range) {
        weeklyTotals(workouts, metric, LocalDate.now(), range.weeks, ZoneId.systemDefault(), Locale.getDefault())
    }
    var selected by remember(totals) { mutableStateOf(totals.lastIndex) }
    val week = totals[selected.coerceIn(totals.indices)]

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ChipRow(
            items = WeeklyMetric.entries,
            selected = metric,
            label = { it.label },
            onClick = { metric = it },
        )
        ChipRow(
            items = WeekRange.entries,
            selected = range,
            label = { it.label },
            onClick = { range = it },
        )
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(metric.format(week.value, unit), style = MaterialTheme.typography.headlineSmall)
            Text(
                text = "Week of ${formatShortDate(week.weekStart)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        BarChart(
            values = totals.map { metric.chartValue(it.value, unit) },
            selectedIndex = selected.coerceIn(totals.indices),
            firstLabel = formatShortDate(totals.first().weekStart),
            lastLabel = formatShortDate(totals.last().weekStart),
            description = "${metric.label} per week. This week: ${metric.format(totals.last().value, unit)}.",
            onSelect = { selected = it },
            modifier = Modifier.padding(horizontal = 8.dp),
        )
    }
}
