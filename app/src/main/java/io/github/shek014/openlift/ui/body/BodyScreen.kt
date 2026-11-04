package io.github.shek014.openlift.ui.body

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.shek014.openlift.data.model.Muscle
import io.github.shek014.openlift.data.model.MuscleGroup

/** The home screen: the body, which you tap to find exercises for a muscle. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyScreen(
    onOpenSettings: () -> Unit,
    onMuscleClick: (Muscle) -> Unit,
    viewModel: BodyViewModel = viewModel(factory = BodyViewModel.Factory),
) {
    val trainedThisWeek by viewModel.trainedThisWeek.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("This week") },
            actions = {
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings")
                }
            },
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = "Muscles you've trained this week are filled in. Tap any muscle to see its exercises.",
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                BodyMap(
                    emphasis = trainedThisWeek,
                    onMuscleClick = onMuscleClick,
                    modifier = Modifier
                        .widthIn(max = 420.dp)
                        .padding(16.dp),
                )
            }
            MuscleGroup.entries.forEach { group ->
                Text(
                    text = group.label,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.onBackground)
                Muscle.entries.filter { it.group == group }.forEach { muscle ->
                    ListItem(
                        headlineContent = { Text(muscle.label) },
                        modifier = Modifier.clickable { onMuscleClick(muscle) },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}
