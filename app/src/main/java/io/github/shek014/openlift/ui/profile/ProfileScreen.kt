package io.github.shek014.openlift.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.shek014.openlift.OpenLiftApplication
import io.github.shek014.openlift.data.WorkoutRepository
import io.github.shek014.openlift.data.model.formatVolume
import io.github.shek014.openlift.data.model.workoutStreaks
import io.github.shek014.openlift.ui.LocalWeightUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

data class ProfileSummary(val workouts: Int, val volumeKg: Double, val streakWeeks: Int)

class ProfileViewModel(repository: WorkoutRepository) : ViewModel() {
    val summary: StateFlow<ProfileSummary> =
        repository.finishedWorkouts
            .map { workouts ->
                val (streak, _) = workoutStreaks(workouts, LocalDate.now(), ZoneId.systemDefault(), Locale.getDefault())
                ProfileSummary(workouts.size, workouts.sumOf { it.volumeKg }, streak)
            }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileSummary(0, 0.0, 0))

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ProfileViewModel((this[APPLICATION_KEY] as OpenLiftApplication).workoutRepository)
            }
        }
    }
}

/** Your totals, and the way to everything that isn't part of a workout. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onHistory: () -> Unit,
    onStatistics: () -> Unit,
    onExercises: () -> Unit,
    onBody: () -> Unit,
    onMeasurements: () -> Unit,
    onSettings: () -> Unit,
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory),
) {
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val unit = LocalWeightUnit.current

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Profile") })
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
            ) {
                Total("Workouts", summary.workouts.toString(), Modifier.weight(1f))
                Total("Volume", formatVolume(summary.volumeKg, unit), Modifier.weight(1f))
                Total(
                    label = "Streak",
                    value = if (summary.streakWeeks == 1) "1 week" else "${summary.streakWeeks} weeks",
                    modifier = Modifier.weight(1f),
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.onBackground)
            ProfileRow("History", Icons.Filled.History, onHistory)
            ProfileRow("Statistics", Icons.Filled.BarChart, onStatistics)
            ProfileRow("Exercises", Icons.AutoMirrored.Filled.MenuBook, onExercises)
            ProfileRow("Body", Icons.Filled.Accessibility, onBody)
            ProfileRow("Body measurements", Icons.Filled.Straighten, onMeasurements)
            ProfileRow("Settings", Icons.Filled.Settings, onSettings)
        }
    }
}

@Composable
private fun Total(label: String, value: String, modifier: Modifier) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun ProfileRow(label: String, icon: ImageVector, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(label) },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
        modifier = Modifier.clickable(onClick = onClick),
    )
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}
