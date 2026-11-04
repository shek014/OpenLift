package io.github.shek014.openlift

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.shek014.openlift.ui.LocalWeightUnit
import io.github.shek014.openlift.ui.OpenLiftApp
import io.github.shek014.openlift.ui.theme.OpenLiftTheme

/** The app's single entry point. Every screen is a Compose function inside [OpenLiftApp]. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settings = (application as OpenLiftApplication).settings
        setContent {
            val darkTheme by settings.darkTheme.collectAsStateWithLifecycle()
            val weightUnit by settings.weightUnit.collectAsStateWithLifecycle()

            // The status bar icons follow the app's theme rather than the phone's.
            LaunchedEffect(darkTheme) {
                val style = if (darkTheme) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
            }

            OpenLiftTheme(darkTheme = darkTheme) {
                CompositionLocalProvider(LocalWeightUnit provides weightUnit) {
                    OpenLiftApp(settings)
                }
            }
        }
    }
}
