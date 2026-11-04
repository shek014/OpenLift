package io.github.shek014.openlift.data

import android.content.Context
import androidx.core.content.edit
import io.github.shek014.openlift.data.model.LengthUnit
import io.github.shek014.openlift.data.model.WeightUnit
import io.github.shek014.openlift.data.model.defaultBarWeight
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
        const val DefaultRestSeconds = 90

        /** The choices for a single exercise, where 0 turns the timer off. */
        val ExerciseRestSecondsChoices = listOf(0, 30, 45, 60, 75, 90, 120, 150, 180, 240, 300)

        private const val DarkThemeKey = "dark_theme"
        private const val WeightUnitKey = "weight_unit"
        private const val ShowBmiKey = "show_bmi"
        private const val LengthUnitKey = "length_unit"
        private const val BarWeightKey = "bar_weight_"
        private const val AskedForNotificationsKey = "asked_for_notifications"
    }
}
