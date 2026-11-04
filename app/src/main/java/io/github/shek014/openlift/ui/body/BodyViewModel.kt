package io.github.shek014.openlift.ui.body

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.shek014.openlift.OpenLiftApplication
import io.github.shek014.openlift.data.WorkoutRepository
import io.github.shek014.openlift.data.model.Exercise
import io.github.shek014.openlift.data.model.Muscle
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale

/** How each muscle is worked by these exercises. Being a primary muscle anywhere takes priority. */
fun List<Exercise>.muscleEmphasis(): Map<Muscle, MuscleEmphasis> =
    flatMap { it.secondaryMuscles }.associateWith { MuscleEmphasis.Secondary } +
        flatMap { it.primaryMuscles }.associateWith { MuscleEmphasis.Primary }

/** When the week containing [today] began, as milliseconds. */
fun startOfWeekMillis(today: LocalDate, zone: ZoneId, locale: Locale): Long =
    today
        .with(TemporalAdjusters.previousOrSame(WeekFields.of(locale).firstDayOfWeek))
        .atStartOfDay(zone)
        .toInstant()
        .toEpochMilli()

class BodyViewModel(repository: WorkoutRepository) : ViewModel() {
    /** The muscles trained so far this week. */
    val trainedThisWeek: StateFlow<Map<Muscle, MuscleEmphasis>> =
        repository
            .exercisesFinishedSince(startOfWeekMillis(LocalDate.now(), ZoneId.systemDefault(), Locale.getDefault()))
            .map { it.muscleEmphasis() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                BodyViewModel((this[APPLICATION_KEY] as OpenLiftApplication).workoutRepository)
            }
        }
    }
}
