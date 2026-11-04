package io.github.shek014.openlift.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.shek014.openlift.data.AppSettings
import io.github.shek014.openlift.data.model.LengthUnit
import io.github.shek014.openlift.data.model.WeightUnit
import io.github.shek014.openlift.data.model.formatDuration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(settings: AppSettings, onBack: () -> Unit) {
    val darkTheme by settings.darkTheme.collectAsStateWithLifecycle()
    val weightUnit by settings.weightUnit.collectAsStateWithLifecycle()
    val restSeconds by settings.restSeconds.collectAsStateWithLifecycle()
    val lengthUnit by settings.lengthUnit.collectAsStateWithLifecycle()
    val showBmi by settings.showBmi.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Settings") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Choices(
                title = "Theme",
                choices = listOf(true, false),
                selected = darkTheme,
                label = { if (it) "Dark" else "Light" },
                onSelect = settings::setDarkTheme,
            )
            Choices(
                title = "Weight unit",
                note = "Your existing workouts convert automatically.",
                choices = WeightUnit.entries,
                selected = weightUnit,
                label = { it.label },
                onSelect = settings::setWeightUnit,
            )
            Choices(
                title = "Length unit",
                note = "For body measurements. Your existing entries convert automatically.",
                choices = LengthUnit.entries,
                selected = lengthUnit,
                label = { it.label },
                onSelect = settings::setLengthUnit,
            )
            Choices(
                title = "BMI",
                note = "Shows on Body measurements once you've added your height and body weight.",
                choices = listOf(false, true),
                selected = showBmi,
                label = { if (it) "Show" else "Hide" },
                onSelect = settings::setShowBmi,
            )
            Choices(
                title = "Rest timer",
                note = "The default countdown after a set. You can change it for any exercise during a workout.",
                choices = AppSettings.RestSecondsChoices,
                selected = restSeconds,
                label = { formatDuration(it.toLong()) },
                onSelect = settings::setRestSeconds,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> Choices(
    title: String,
    choices: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    note: String? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        note?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            choices.forEach { choice ->
                FilterChip(
                    selected = choice == selected,
                    onClick = { onSelect(choice) },
                    label = { Text(label(choice)) },
                )
            }
        }
    }
}
