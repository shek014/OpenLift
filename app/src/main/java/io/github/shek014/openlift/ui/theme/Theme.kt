package io.github.shek014.openlift.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.shek014.openlift.R

/** The amethyst accent comes from the Black & Amethyst variant of Bearded Theme, by BeardedBear (GPL-3.0). */
private val DarkColors = darkColorScheme(
    primary = Color(0xFFA85FF1),
    onPrimary = Color(0xFF111418),
    primaryContainer = Color(0xFF3A2A5C),
    onPrimaryContainer = Color(0xFFE6E8EA),
    secondary = Color(0xFFA6ABB1),
    onSecondary = Color(0xFF181B1E),
    secondaryContainer = Color(0xFF2B3036),
    onSecondaryContainer = Color(0xFFE6E8EA),
    background = Color(0xFF181B1E),
    onBackground = Color(0xFFE6E8EA),
    surface = Color(0xFF181B1E),
    onSurface = Color(0xFFE6E8EA),
    surfaceVariant = Color(0xFF2B3036),
    onSurfaceVariant = Color(0xFFA6ABB1),
    surfaceContainerLowest = Color(0xFF181B1E),
    surfaceContainerLow = Color(0xFF181B1E),
    surfaceContainer = Color(0xFF202428),
    surfaceContainerHigh = Color(0xFF202428),
    surfaceContainerHighest = Color(0xFF2B3036),
    outline = Color(0xFFA6ABB1),
    outlineVariant = Color(0xFF363B42),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF7C3AED),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEDE4FD),
    onPrimaryContainer = Color(0xFF171717),
    secondary = Color(0xFF5C5C5C),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFEDEDED),
    onSecondaryContainer = Color(0xFF171717),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF171717),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF171717),
    surfaceVariant = Color(0xFFEDEDED),
    onSurfaceVariant = Color(0xFF5C5C5C),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFFFFFF),
    surfaceContainer = Color(0xFFF5F5F5),
    surfaceContainerHigh = Color(0xFFF5F5F5),
    surfaceContainerHighest = Color(0xFFEDEDED),
    outline = Color(0xFF5C5C5C),
    outlineVariant = Color(0xFFE0E0E0),
)

/** One font file holds every weight of Inter, so each weight here points at the same file. */
@OptIn(ExperimentalTextApi::class)
private fun interFont(weight: FontWeight) = Font(
    resId = R.font.inter,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

private val Inter = FontFamily(
    interFont(FontWeight.Normal),
    interFont(FontWeight.Medium),
    interFont(FontWeight.SemiBold),
)

private val Default = Typography()

private fun TextStyle.inter(weight: FontWeight? = fontWeight) = copy(fontFamily = Inter, fontWeight = weight)

private val InterTypography = Typography(
    displayLarge = Default.displayLarge.inter(),
    displayMedium = Default.displayMedium.inter(),
    displaySmall = Default.displaySmall.inter(),
    headlineLarge = Default.headlineLarge.inter(FontWeight.SemiBold),
    headlineMedium = Default.headlineMedium.inter(FontWeight.SemiBold),
    headlineSmall = Default.headlineSmall.inter(FontWeight.SemiBold),
    titleLarge = Default.titleLarge.inter(FontWeight.SemiBold),
    titleMedium = Default.titleMedium.inter(),
    titleSmall = Default.titleSmall.inter(),
    bodyLarge = Default.bodyLarge.inter(),
    bodyMedium = Default.bodyMedium.inter(),
    bodySmall = Default.bodySmall.inter(),
    labelLarge = Default.labelLarge.inter(),
    labelMedium = Default.labelMedium.inter(),
    labelSmall = Default.labelSmall.inter(),
)

private val OpenLiftShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun OpenLiftTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = InterTypography,
        shapes = OpenLiftShapes,
        content = content,
    )
}
