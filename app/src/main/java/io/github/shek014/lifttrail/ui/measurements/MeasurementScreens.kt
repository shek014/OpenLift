package io.github.shek014.lifttrail.ui.measurements

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.shek014.lifttrail.LiftTrailApplication
import io.github.shek014.lifttrail.data.AppSettings
import io.github.shek014.lifttrail.data.MeasurementRepository
import io.github.shek014.lifttrail.data.model.ChartRange
import io.github.shek014.lifttrail.data.model.Measurement
import io.github.shek014.lifttrail.data.model.MeasurementType
import io.github.shek014.lifttrail.data.model.MeasurementUnits
import io.github.shek014.lifttrail.data.model.bodyMassIndex
import io.github.shek014.lifttrail.data.model.dateToPickerMillis
import io.github.shek014.lifttrail.data.model.format
import io.github.shek014.lifttrail.data.model.formatChange
import io.github.shek014.lifttrail.data.model.formatDate
import io.github.shek014.lifttrail.data.model.formatShortDate
import io.github.shek014.lifttrail.data.model.isValidMeasurement
import io.github.shek014.lifttrail.data.model.isValidWeightInput
import io.github.shek014.lifttrail.data.model.latestMeasurements
import io.github.shek014.lifttrail.data.model.parseWeight
import io.github.shek014.lifttrail.data.model.pickedDateToMillis
import io.github.shek014.lifttrail.data.model.since
import io.github.shek014.lifttrail.data.model.toShown
import io.github.shek014.lifttrail.data.model.toStored
import io.github.shek014.lifttrail.data.model.unitLabel
import io.github.shek014.lifttrail.ui.LocalWeightUnit
import io.github.shek014.lifttrail.ui.charts.LineChart
import io.github.shek014.lifttrail.ui.exercises.ChipRow
import io.github.shek014.lifttrail.ui.workout.NumberField
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Locale

/** The name of the navigation argument that says which measurement to show. */
const val MeasurementTypeArg = "measurementType"

class MeasurementsViewModel(repository: MeasurementRepository) : ViewModel() {
    val measurements: StateFlow<List<Measurement>> =
        repository.measurements.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MeasurementsViewModel((this[APPLICATION_KEY] as LiftTrailApplication).measurementRepository)
            }
        }
    }
}

class MeasurementDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: MeasurementRepository,
) : ViewModel() {
    val type: MeasurementType = MeasurementType.valueOf(checkNotNull(savedStateHandle[MeasurementTypeArg]))

    /** This type's entries, newest first. */
    val entries: StateFlow<List<Measurement>> =
        repository.measurements(type).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun add(value: Double, measuredAt: Long) {
        viewModelScope.launch { repository.add(type, value, measuredAt) }
    }

    fun delete(id: Long) {
        viewModelScope.launch { repository.delete(id) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MeasurementDetailViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    repository = (this[APPLICATION_KEY] as LiftTrailApplication).measurementRepository,
                )
            }
        }
    }
}

@Composable
private fun measurementUnits(settings: AppSettings): MeasurementUnits {
    val length by settings.lengthUnit.collectAsStateWithLifecycle()
    return MeasurementUnits(LocalWeightUnit.current, length)
}

/** Everything in here is optional. A measurement only appears once something's been logged for it. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeasurementsScreen(
    settings: AppSettings,
    onBack: () -> Unit,
    onTypeClick: (MeasurementType) -> Unit,
    viewModel: MeasurementsViewModel = viewModel(factory = MeasurementsViewModel.Factory),
) {
    val measurements by viewModel.measurements.collectAsStateWithLifecycle()
    val showBmi by settings.showBmi.collectAsStateWithLifecycle()
    val units = measurementUnits(settings)
    val latest = remember(measurements) { latestMeasurements(measurements) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Body measurements") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
        )
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            if (showBmi) {
                item(key = "bmi") { BmiRow(latest[MeasurementType.BodyWeight]?.latest, latest[MeasurementType.Height]?.latest) }
            }
            items(MeasurementType.entries, key = { it.name }) { type ->
                val entry = latest[type]
                ListItem(
                    headlineContent = { Text(type.label) },
                    supportingContent = {
                        Text(
                            text = entry?.let { "Last: ${formatDate(it.latest.measuredAt)}" } ?: "Not tracked yet",
                            style = MaterialTheme.typography.labelMedium,
                        )
                    },
                    trailingContent = {
                        if (entry != null) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = type.format(entry.latest.value, units),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                entry.previous?.let {
                                    Text(
                                        text = type.formatChange(entry.latest.value, it.value, units),
                                        style = MaterialTheme.typography.labelSmall,
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.clickable { onTypeClick(type) },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

@Composable
private fun BmiRow(weight: Measurement?, height: Measurement?) {
    val bmi = if (weight != null && height != null) bodyMassIndex(weight.value, height.value) else null
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text("BMI", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        if (bmi == null) {
            Text(
                text = "Add your height and body weight to see your BMI.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Text(String.format(Locale.ROOT, "%.1f", bmi), style = MaterialTheme.typography.headlineSmall)
            Text(
                text = "A rough guide from height and weight alone. It can't tell muscle from fat, so it often reads high for people who lift.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.onBackground)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeasurementDetailScreen(
    settings: AppSettings,
    onBack: () -> Unit,
    viewModel: MeasurementDetailViewModel = viewModel(factory = MeasurementDetailViewModel.Factory),
) {
    val type = viewModel.type
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    val units = measurementUnits(settings)
    var adding by rememberSaveable { mutableStateOf(false) }
    var range by rememberSaveable { mutableStateOf(ChartRange.All) }
    var deleting by remember { mutableStateOf<Measurement?>(null) }

    val points = remember(entries, range) {
        val since = range.since(System.currentTimeMillis())
        entries
            .filter { since == null || it.measuredAt >= since }
            .sortedWith(compareBy<Measurement> { it.measuredAt }.thenBy { it.id })
    }
    var selected by remember(points) { mutableStateOf(points.lastIndex) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(type.label) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
        )
        LazyColumn(modifier = Modifier.weight(1f)) {
            if (entries.isNotEmpty()) {
                item(key = "chart") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ChipRow(
                            items = ChartRange.entries,
                            selected = range,
                            label = { it.label },
                            onClick = { range = it },
                        )
                        if (points.isEmpty()) {
                            Text(
                                text = "Nothing logged in this time. Try a longer range.",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            val point = points[selected.coerceIn(points.indices)]
                            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                Text(type.format(point.value, units), style = MaterialTheme.typography.headlineSmall)
                                Text(
                                    text = formatDate(point.measuredAt),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            LineChart(
                                times = points.map { it.measuredAt },
                                values = points.map { type.toShown(it.value, units) },
                                selectedIndex = selected.coerceIn(points.indices),
                                firstLabel = formatShortDate(points.first().measuredAt),
                                lastLabel = formatShortDate(points.last().measuredAt),
                                description = "${type.label} over ${points.size} entries.",
                                onSelect = { selected = it },
                                modifier = Modifier.padding(horizontal = 8.dp),
                            )
                        }
                    }
                }
            } else {
                item(key = "none") {
                    Text(
                        text = "Nothing logged yet. Tracking this is optional.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(entries, key = { it.id }) { entry ->
                ListItem(
                    headlineContent = { Text(type.format(entry.value, units)) },
                    supportingContent = { Text(formatDate(entry.measuredAt), style = MaterialTheme.typography.labelMedium) },
                    trailingContent = {
                        IconButton(onClick = { deleting = entry }) {
                            Icon(Icons.Filled.Close, contentDescription = "Delete entry from ${formatDate(entry.measuredAt)}")
                        }
                    },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
        Button(
            onClick = { adding = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text("Add entry")
        }
    }

    if (adding) {
        AddMeasurementDialog(
            type = type,
            units = units,
            onAdd = { value, date ->
                adding = false
                viewModel.add(value, date)
            },
            onDismiss = { adding = false },
        )
    }
    deleting?.let { entry ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Delete this entry?") },
            text = { Text("${type.format(entry.value, units)} on ${formatDate(entry.measuredAt)}") },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleting = null
                        viewModel.delete(entry.id)
                    },
                ) {
                    Text("Delete")
                }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Cancel") } },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddMeasurementDialog(
    type: MeasurementType,
    units: MeasurementUnits,
    onAdd: (Double, Long) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf("") }
    var pickedDate by remember { mutableLongStateOf(dateToPickerMillis(LocalDate.now())) }
    var pickingDate by remember { mutableStateOf(false) }
    val value = parseWeight(text)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add ${type.label.lowercase()}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.width(96.dp)) {
                        NumberField(
                            value = text,
                            placeholder = "",
                            description = "Value",
                            keyboardType = KeyboardType.Decimal,
                            onValueChange = { if (isValidWeightInput(it)) text = it },
                        )
                    }
                    Text(type.unitLabel(units), style = MaterialTheme.typography.bodyLarge)
                }
                TextButton(onClick = { pickingDate = true }) {
                    Text(formatDate(pickedDateToMillis(pickedDate)))
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = isValidMeasurement(value),
                onClick = { value?.let { onAdd(type.toStored(it, units), pickedDateToMillis(pickedDate)) } },
            ) {
                Text("Add")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )

    if (pickingDate) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = pickedDate,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) =
                    utcTimeMillis <= dateToPickerMillis(LocalDate.now())
            },
        )
        DatePickerDialog(
            onDismissRequest = { pickingDate = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let { pickedDate = it }
                        pickingDate = false
                    },
                ) {
                    Text("OK")
                }
            },
            dismissButton = { TextButton(onClick = { pickingDate = false }) { Text("Cancel") } },
        ) {
            DatePicker(state = state)
        }
    }
}
