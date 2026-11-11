package io.github.shek014.lifttrail.ui.exercises

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.decode.BitmapFactoryDecoder
import coil3.request.ImageRequest
import io.github.shek014.lifttrail.LiftTrailApplication

/** Redraws a black and white GIF in these two colors, so that it matches what's around it. */
fun inkAndPaperFilter(ink: Color, paper: Color) = ColorFilter.colorMatrix(
    ColorMatrix(
        floatArrayOf(
            paper.red - ink.red, 0f, 0f, 0f, ink.red * 255f,
            0f, paper.green - ink.green, 0f, 0f, ink.green * 255f,
            0f, 0f, paper.blue - ink.blue, 0f, ink.blue * 255f,
            0f, 0f, 0f, 1f, 0f,
        ),
    ),
)

/**
 * A small still picture of an exercise, or an icon if it doesn't have a drawing yet. It stays still
 * because a whole list of animations moving at once would be distracting and slow.
 */
@Composable
fun ExerciseThumbnail(exerciseId: String, size: Dp = 48.dp) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val uri = remember(exerciseId) {
        (context.applicationContext as LiftTrailApplication).exerciseMedia.animationUri(exerciseId)
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(MaterialTheme.shapes.small)
            .background(colors.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (uri == null) {
            Icon(
                imageVector = Icons.Filled.FitnessCenter,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
            )
        } else {
            val request = remember(uri) {
                ImageRequest.Builder(context)
                    .data(uri)
                    .decoderFactory(BitmapFactoryDecoder.Factory()) // Reads only the GIF's first frame.
                    .build()
            }
            AsyncImage(
                model = request,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(3.dp),
                contentScale = ContentScale.Fit,
                colorFilter = remember(colors.onSurface, colors.surfaceVariant) {
                    inkAndPaperFilter(colors.onSurface, colors.surfaceVariant)
                },
            )
        }
    }
}
