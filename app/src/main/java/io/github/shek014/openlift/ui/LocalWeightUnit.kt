package io.github.shek014.openlift.ui

import androidx.compose.runtime.staticCompositionLocalOf
import io.github.shek014.openlift.data.model.WeightUnit

/** The unit to show weights in, available to every screen. */
val LocalWeightUnit = staticCompositionLocalOf { WeightUnit.Kg }
