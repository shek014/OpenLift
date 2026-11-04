package io.github.shek014.openlift

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.shek014.openlift.ui.OpenLiftApp
import io.github.shek014.openlift.ui.theme.OpenLiftTheme

/** The app's single entry point. Every screen is a Compose function inside [OpenLiftApp]. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OpenLiftTheme {
                OpenLiftApp()
            }
        }
    }
}
