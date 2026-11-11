package io.github.shek014.lifttrail.data

import android.content.Context
import androidx.core.content.edit
import io.github.shek014.lifttrail.data.model.LengthUnit
import io.github.shek014.lifttrail.data.model.WeightUnit
import io.github.shek014.lifttrail.data.model.defaultBarWeight
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** The choices someone makes in Settings, remembered between launches. */
class AppSettings(context: Context) {
    private val preferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private val darkThemeState = MutableStateFlow(preferences.getBoolean(DarkThemeKey, true))
    private val weightUnitState = MutableStateFlow(
        WeightUnit.entries.find { it.name == preferences.getString(WeightUnitKey, null) } ?: WeightUnit.Kg,
    )
    private val lengthUnitState = MutableStateFlow(
        LengthUnit.entries.find { it.name == preferences.getString(LengthUnitKey, null) } ?: LengthUnit.Cm,
    )

    val darkTheme: StateFlow<Boolean> = darkThemeState.asStateFlow()
    val weightUnit: StateFlow<WeightUnit> = weightUnitState.asStateFlow()
    val lengthUnit: StateFlow<LengthUnit> = lengthUnitState.asStateFlow()

    private val restSecondsState = MutableStateFlow(preferences.getInt(RestSecondsKey, DefaultRestSeconds))

    /** The rest timer for every workout, unless a workout or an exercise sets its own. 0 means off. */
    val restSeconds: StateFlow<Int> = restSecondsState.asStateFlow()

    fun setRestSeconds(seconds: Int) {
        preferences.edit { putInt(RestSecondsKey, seconds) }
        restSecondsState.value = seconds
    }

    private val recordSoundState = MutableStateFlow(savedSound(RecordSoundKey, Sound.Fanfare))
    private val restSoundState = MutableStateFlow(savedSound(RestSoundKey, Sound.Chime))

    /** Played when a set breaks a personal record. */
    val recordSound: StateFlow<Sound> = recordSoundState.asStateFlow()

    /** Played when the rest timer runs out. */
    val restSound: StateFlow<Sound> = restSoundState.asStateFlow()

    fun setRecordSound(sound: Sound) {
        preferences.edit { putString(RecordSoundKey, sound.name) }
        recordSoundState.value = sound
    }

    fun setRestSound(sound: Sound) {
        preferences.edit { putString(RestSoundKey, sound.name) }
        restSoundState.value = sound
    }

    private fun savedSound(key: String, default: Sound) =
        Sound.entries.find { it.name == preferences.getString(key, null) } ?: default

    private val showBmiState = MutableStateFlow(preferences.getBoolean(ShowBmiKey, false))

    /** BMI is off until someone turns it on. */
    val showBmi: StateFlow<Boolean> = showBmiState.asStateFlow()

    fun setShowBmi(show: Boolean) {
        preferences.edit { putBoolean(ShowBmiKey, show) }
        showBmiState.value = show
    }

    /** The bar someone uses for warm-ups, in [unit], remembered separately for each unit. */
    fun barWeight(unit: WeightUnit): Double =
        preferences.getFloat(BarWeightKey + unit.name, unit.defaultBarWeight().toFloat()).toDouble()

    fun setBarWeight(unit: WeightUnit, weight: Double) {
        preferences.edit { putFloat(BarWeightKey + unit.name, weight.toFloat()) }
    }

    fun setLengthUnit(unit: LengthUnit) {
        preferences.edit { putString(LengthUnitKey, unit.name) }
        lengthUnitState.value = unit
    }

    /** Whether the app has already asked for permission to show notifications. */
    var askedForNotifications: Boolean
        get() = preferences.getBoolean(AskedForNotificationsKey, false)
        set(value) = preferences.edit { putBoolean(AskedForNotificationsKey, value) }

    fun setDarkTheme(dark: Boolean) {
        preferences.edit { putBoolean(DarkThemeKey, dark) }
        darkThemeState.value = dark
    }

    fun setWeightUnit(unit: WeightUnit) {
        preferences.edit { putString(WeightUnitKey, unit.name) }
        weightUnitState.value = unit
    }

    companion object {
        /** The rest timer starts off, until it's turned on in Settings, for a workout or for an exercise. */
        const val DefaultRestSeconds = 0

        /** The rest times to choose from, where 0 turns the timer off. */
        val RestSecondsChoices = listOf(0, 30, 45, 60, 75, 90, 120, 150, 180, 240, 300)

        private const val DarkThemeKey = "dark_theme"
        private const val WeightUnitKey = "weight_unit"
        private const val ShowBmiKey = "show_bmi"
        private const val RestSecondsKey = "rest_seconds"
        private const val RecordSoundKey = "record_sound"
        private const val RestSoundKey = "rest_sound"
        private const val LengthUnitKey = "length_unit"
        private const val BarWeightKey = "bar_weight_"
        private const val AskedForNotificationsKey = "asked_for_notifications"
    }
}
