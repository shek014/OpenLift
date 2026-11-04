package io.github.shek014.openlift.data

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Remembers the chosen theme between launches. The app is dark until someone picks light. */
class ThemePreference(context: Context) {
    private val preferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private val state = MutableStateFlow(preferences.getBoolean(DarkThemeKey, true))

    val darkTheme: StateFlow<Boolean> = state.asStateFlow()

    fun setDarkTheme(dark: Boolean) {
        preferences.edit { putBoolean(DarkThemeKey, dark) }
        state.value = dark
    }

    private companion object {
        const val DarkThemeKey = "dark_theme"
    }
}
