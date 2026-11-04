package io.github.shek014.openlift.data

import android.content.Context
import androidx.core.content.edit
import io.github.shek014.openlift.data.model.WeightUnit
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
    private val restSecondsState = MutableStateFlow(preferences.getInt(RestSecondsKey, DefaultRestSeconds))

    val darkTheme: StateFlow<Boolean> = darkThemeState.asStateFlow()
    val weightUnit: StateFlow<WeightUnit> = weightUnitState.asStateFlow()
    val restSeconds: StateFlow<Int> = restSecondsState.asStateFlow()

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

    fun setRestSeconds(seconds: Int) {
        preferences.edit { putInt(RestSecondsKey, seconds) }
        restSecondsState.value = seconds
    }

    companion object {
        const val DefaultRestSeconds = 90
        val RestSecondsChoices = listOf(30, 60, 90, 120, 180, 300)

        private const val DarkThemeKey = "dark_theme"
        private const val WeightUnitKey = "weight_unit"
        private const val RestSecondsKey = "rest_seconds"
        private const val AskedForNotificationsKey = "asked_for_notifications"
    }
}
