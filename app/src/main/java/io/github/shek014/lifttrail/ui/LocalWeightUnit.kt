package io.github.shek014.lifttrail.ui

import androidx.compose.runtime.staticCompositionLocalOf
import io.github.shek014.lifttrail.data.model.WeightUnit

/** The unit to show weights in, available to every screen. */
val LocalWeightUnit = staticCompositionLocalOf { WeightUnit.Kg }
