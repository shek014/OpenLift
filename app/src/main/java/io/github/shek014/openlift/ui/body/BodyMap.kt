package io.github.shek014.openlift.ui.body

import android.graphics.Region
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.shek014.openlift.data.model.Muscle

enum class MuscleEmphasis { Primary, Secondary }

/**
 * The body from the front and the back. Muscles in [emphasis] are colored in. If [onMuscleClick]
 * is set, tapping a muscle calls it.
 */
@Composable
fun BodyMap(
    emphasis: Map<Muscle, MuscleEmphasis>,
    modifier: Modifier = Modifier,
    onMuscleClick: ((Muscle) -> Unit)? = null,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        BodyView.entries.forEach { view ->
            BodyFigure(view, emphasis, onMuscleClick, Modifier.weight(1f))
        }
    }
}

/** A region of the map ready to draw, with both of its mirrored sides. */
private class DrawnRegion(val muscle: Muscle?, val paths: List<Path>) {
    private val touchAreas = paths.map { path ->
        Region().apply {
            setPath(path.asAndroidPath(), Region(0, 0, BodyMapWidth.toInt(), BodyMapHeight.toInt()))
        }
    }

    fun contains(x: Int, y: Int) = touchAreas.any { it.contains(x, y) }
}

private fun drawnRegions(view: BodyView): List<DrawnRegion> {
    val mirror = Matrix().apply {
        translate(BodyMapWidth, 0f)
        scale(-1f, 1f)
    }
    return BodyMapRegions.filter { it.view == view }.map { region ->
        val left = PathParser().parsePathString(region.path).toPath()
        val right = Path().apply {
            addPath(left)
            transform(mirror)
        }
        DrawnRegion(region.muscle, listOf(left, right))
    }
}

@Composable
private fun BodyFigure(
    view: BodyView,
    emphasis: Map<Muscle, MuscleEmphasis>,
    onMuscleClick: ((Muscle) -> Unit)?,
    modifier: Modifier,
) {
    val regions = remember(view) { drawnRegions(view) }
    val colors = MaterialTheme.colorScheme
    val highlighted = emphasis.keys.joinToString { it.label }
    val outline = Stroke(width = 1.1f, join = StrokeJoin.Round)

    Canvas(
        modifier = modifier
            .aspectRatio(BodyMapWidth / BodyMapHeight)
            .semantics {
                contentDescription = buildString {
                    append("Body map, ${view.name.lowercase()} view.")
                    if (highlighted.isNotEmpty()) append(" Highlighted: $highlighted.")
                }
            }
            .pointerInput(onMuscleClick) {
                if (onMuscleClick == null) return@pointerInput
                detectTapGestures { tap ->
                    val scale = size.width / BodyMapWidth
                    val muscle = regions.firstOrNull {
                        it.muscle != null && it.contains((tap.x / scale).toInt(), (tap.y / scale).toInt())
                    }?.muscle
                    if (muscle != null) onMuscleClick(muscle)
                }
            },
    ) {
        val scale = size.width / BodyMapWidth
        withTransform({ scale(scale, scale, pivot = Offset.Zero) }) {
            regions.forEach { region ->
                val fill: Color = when {
                    region.muscle == null -> colors.background
                    else -> when (emphasis[region.muscle]) {
                        MuscleEmphasis.Primary -> colors.primary
                        MuscleEmphasis.Secondary -> colors.primary.copy(alpha = 0.4f)
                        null -> colors.surfaceVariant
                    }
                }
                region.paths.forEach { path ->
                    drawPath(path, fill)
                    drawPath(path, colors.onBackground, style = outline)
                }
            }
        }
    }
}
