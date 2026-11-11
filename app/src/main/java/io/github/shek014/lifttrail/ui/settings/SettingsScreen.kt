package io.github.shek014.lifttrail.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import io.github.shek014.lifttrail.data.AppSettings
import io.github.shek014.lifttrail.data.BackupRejectedException
import io.github.shek014.lifttrail.data.Sound
import io.github.shek014.lifttrail.data.backupFileName
import io.github.shek014.lifttrail.data.model.LengthUnit
import io.github.shek014.lifttrail.data.model.WeightUnit
import io.github.shek014.lifttrail.data.model.formatRest
import java.io.IOException
import java.time.LocalDate
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onBack: () -> Unit,
    backupViewModel: BackupViewModel = viewModel(factory = BackupViewModel.Factory),
) {
    val darkTheme by settings.darkTheme.collectAsStateWithLifecycle()
    val weightUnit by settings.weightUnit.collectAsStateWithLifecycle()
    val lengthUnit by settings.lengthUnit.collectAsStateWithLifecycle()
    val showBmi by settings.showBmi.collectAsStateWithLifecycle()
    val restSeconds by settings.restSeconds.collectAsStateWithLifecycle()
    val recordSound by settings.recordSound.collectAsStateWithLifecycle()
    val restSound by settings.restSound.collectAsStateWithLifecycle()
    val player = (LocalContext.current.applicationContext as LiftTrailApplication).soundPlayer

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
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Section("Appearance") {
                Choices(
                    title = "Theme",
                    choices = listOf(true, false),
                    selected = darkTheme,
                    label = { if (it) "Dark" else "Light" },
                    onSelect = settings::setDarkTheme,
                )
            }
            Section("Units") {
                Choices(
                    title = "Weight",
                    note = "Your existing workouts convert automatically.",
                    choices = WeightUnit.entries,
                    selected = weightUnit,
                    label = { it.label },
                    onSelect = settings::setWeightUnit,
                )
                Choices(
                    title = "Length",
                    note = "For body measurements. Your existing entries convert automatically.",
                    choices = LengthUnit.entries,
                    selected = lengthUnit,
                    label = { it.label },
                    onSelect = settings::setLengthUnit,
                )
            }
            Section("Workouts") {
                Choices(
                    title = "Rest timer",
                    note = "The countdown after each set. A workout or an exercise can still set its own.",
                    choices = AppSettings.RestSecondsChoices,
                    selected = restSeconds,
                    label = { formatRest(it) },
                    onSelect = settings::setRestSeconds,
                )
            }
            Section("Sounds") {
                Choices(
                    title = "Personal record",
                    note = "Plays when a set beats your best. Choosing one plays it.",
                    choices = Sound.entries,
                    selected = recordSound,
                    label = { it.label },
                    onSelect = {
                        settings.setRecordSound(it)
                        player.play(it)
                    },
                )
                Choices(
                    title = "Rest over",
                    note = "Plays when your rest is over. Both sounds follow your phone's notification volume.",
                    choices = Sound.entries,
                    selected = restSound,
                    label = { it.label },
                    onSelect = {
                        settings.setRestSound(it)
                        player.play(it)
                    },
                )
            }
            Section("Body") {
                Choices(
                    title = "BMI",
                    note = "Shows on Body measurements once you've added your height and body weight.",
                    choices = listOf(false, true),
                    selected = showBmi,
                    label = { if (it) "Show" else "Hide" },
                    onSelect = settings::setShowBmi,
                )
            }
            Section("Data") {
                BackupSection(backupViewModel)
            }
        }
    }
}

/** A group of related settings under one heading. */
@Composable
private fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column {
            Text(
                text = title,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                style = MaterialTheme.typography.titleMedium,
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.onBackground)
        }
        content()
    }
}

@Composable
private fun BackupSection(viewModel: BackupViewModel) {
    val saveFile = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        uri?.let(viewModel::save)
    }
    val openFile = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let(viewModel::restore)
    }
    var confirmingRestore by rememberSaveable { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Backup",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "Your workouts are only stored on this phone. Save a backup somewhere safe, like Google Drive, " +
                "to keep your workouts, routines, custom exercises and measurements.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedButton(
            onClick = { saveFile.launch(backupFileName(LocalDate.now())) },
            enabled = !viewModel.working,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Save a backup")
        }
        OutlinedButton(
            onClick = { confirmingRestore = true },
            enabled = !viewModel.working,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Restore from a backup")
        }
        viewModel.message?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium)
        }
    }

    if (confirmingRestore) {
        AlertDialog(
            onDismissRequest = { confirmingRestore = false },
            title = { Text("Replace what's on this phone?") },
            text = {
                Text(
                    "Everything in LiftTrail, including any workout in progress, will be replaced by the backup " +
                        "you choose. This can't be undone. The app will restart when it's done.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmingRestore = false
                        openFile.launch(arrayOf("*/*"))
                    },
                ) {
                    Text("Choose backup")
                }
            },
            dismissButton = { TextButton(onClick = { confirmingRestore = false }) { Text("Cancel") } },
        )
    }
}

class BackupViewModel(private val application: LiftTrailApplication) : ViewModel() {
    var working by mutableStateOf(false)
        private set
    var message by mutableStateOf<String?>(null)
        private set

    fun save(destination: Uri) = run {
        application.databaseBackup.save(destination)
        message = "Backup saved."
    }

    fun restore(source: Uri) = run {
        application.databaseBackup.restore(source)
        application.restart()
    }

    private fun run(action: suspend () -> Unit) {
        working = true
        message = null
        viewModelScope.launch {
            try {
                action()
            } catch (_: BackupRejectedException) {
                message = "That file isn't an LiftTrail backup, or it's from a newer version of the app. Nothing was changed."
            } catch (_: IOException) {
                message = "Couldn't use that file. Nothing was changed."
            }
            working = false
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { BackupViewModel(this[APPLICATION_KEY] as LiftTrailApplication) }
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
