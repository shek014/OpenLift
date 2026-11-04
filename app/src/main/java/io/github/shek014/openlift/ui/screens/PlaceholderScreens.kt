package io.github.shek014.openlift.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.shek014.openlift.ui.theme.OpenLiftTheme

// Temporary screens. Each one gets replaced by the real feature as we build it.

@Composable
fun WorkoutScreen() = PlaceholderScreen(
    title = "Workout",
    description = "Start an empty workout or one of your routines, then log sets, reps and weight.",
)

@Composable
fun RoutinesScreen() = PlaceholderScreen(
    title = "Routines",
    description = "Your saved routines, plus ready-made splits like Upper/Lower and Push/Pull/Legs.",
)

@Composable
fun HistoryScreen() = PlaceholderScreen(
    title = "History",
    description = "Past workouts and graphs of your progress over time.",
)

@Composable
private fun PlaceholderScreen(title: String, description: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Text(
            description,
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WorkoutScreenPreview() {
    OpenLiftTheme { WorkoutScreen() }
}
