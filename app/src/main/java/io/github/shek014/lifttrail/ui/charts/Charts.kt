package io.github.shek014.lifttrail.ui.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import io.github.shek014.lifttrail.data.model.AxisScale
import io.github.shek014.lifttrail.data.model.axisScale
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToLong

private val ChartHeight = 200.dp
private val AxisWidth = 44.dp
private val DateAxisHeight = 22.dp

private fun formatAxisValue(value: Double): String =
    when {
        abs(value) >= 10_000 -> "${(value / 1000).roundToLong()}k"
        value % 1.0 == 0.0 -> value.roundToLong().toString()
        else -> String.format(Locale.ROOT, "%.1f", value)
    }

/** The area the data is drawn in, leaving room for the axis labels. */
private class PlotArea(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    val width get() = right - left
    val height get() = bottom - top

    fun yOf(value: Double, scale: AxisScale): Float =
        bottom - ((value - scale.low) / (scale.high - scale.low)).toFloat() * height
}

private fun DrawScope.plotArea() = PlotArea(
    left = AxisWidth.toPx(),
    top = 8.dp.toPx(),
    right = size.width - 8.dp.toPx(),
    bottom = size.height - DateAxisHeight.toPx(),
)

private fun DrawScope.drawValueAxis(
    area: PlotArea,
    scale: AxisScale,
    measurer: TextMeasurer,
    labelStyle: TextStyle,
    gridColor: Color,
) {
    scale.ticks.forEach { tick ->
        val y = area.yOf(tick, scale)
        drawLine(gridColor, Offset(area.left, y), Offset(area.right, y), strokeWidth = 1.dp.toPx())
        val label = measurer.measure(formatAxisValue(tick), labelStyle)
        drawText(label, topLeft = Offset(area.left - label.size.width - 6.dp.toPx(), y - label.size.height / 2f))
    }
}

private fun DrawScope.drawDateLabels(
    area: PlotArea,
    firstLabel: String,
    lastLabel: String,
    measurer: TextMeasurer,
    labelStyle: TextStyle,
) {
    val y = area.bottom + 6.dp.toPx()
    drawText(measurer.measure(firstLabel, labelStyle), topLeft = Offset(area.left, y))
    if (lastLabel != firstLabel) {
        val last = measurer.measure(lastLabel, labelStyle)
        drawText(last, topLeft = Offset(area.right - last.size.width, y))
    }
}

/**
 * A line through values over time, spaced by when they happened. Tapping near a point calls
 * [onSelect] with its index.
 */
@Composable
fun LineChart(
    times: List<Long>,
    values: List<Double>,
    selectedIndex: Int,
    firstLabel: String,
    lastLabel: String,
    description: String,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val measurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = colors.onSurfaceVariant)
    val scale = axisScale(values)

    fun xOf(area: PlotArea, index: Int): Float {
        val span = times.last() - times.first()
        return if (span == 0L) {
            area.left + area.width / 2
        } else {
            area.left + area.width * (times[index] - times.first()).toFloat() / span
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(ChartHeight)
            .semantics { contentDescription = description }
            .pointerInput(times, values) {
                detectTapGestures { tap ->
                    val left = AxisWidth.toPx()
                    val right = size.width - 8.dp.toPx()
                    val span = (times.last() - times.first()).toFloat()
                    val nearest = times.indices.minBy { index ->
                        val x = if (span == 0f) (left + right) / 2 else left + (right - left) * (times[index] - times.first()) / span
                        abs(x - tap.x)
                    }
                    onSelect(nearest)
                }
            },
    ) {
        val area = plotArea()
        drawValueAxis(area, scale, measurer, labelStyle, colors.outlineVariant)
        drawDateLabels(area, firstLabel, lastLabel, measurer, labelStyle)

        val points = values.indices.map { Offset(xOf(area, it), area.yOf(values[it], scale)) }
        if (points.size > 1) {
            val line = Path().apply {
                moveTo(points.first().x, points.first().y)
                points.drop(1).forEach { lineTo(it.x, it.y) }
            }
            drawPath(line, colors.primary, style = Stroke(width = 2.dp.toPx(), join = StrokeJoin.Round, cap = StrokeCap.Round))
        }
        points.forEachIndexed { index, point ->
            if (index == selectedIndex) {
                drawLine(colors.outline, Offset(point.x, area.top), Offset(point.x, area.bottom), strokeWidth = 1.dp.toPx())
                drawCircle(colors.background, radius = 7.dp.toPx(), center = point)
                drawCircle(colors.primary, radius = 5.dp.toPx(), center = point)
            } else {
                drawCircle(colors.primary, radius = 3.dp.toPx(), center = point)
            }
        }
    }
}

/** Evenly spaced bars that start from zero. Tapping a bar calls [onSelect] with its index. */
@Composable
fun BarChart(
    values: List<Double>,
    selectedIndex: Int,
    firstLabel: String,
    lastLabel: String,
    description: String,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val measurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = colors.onSurfaceVariant)
    val scale = axisScale(values, startAtZero = true)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(ChartHeight)
            .semantics { contentDescription = description }
            .pointerInput(values) {
                detectTapGestures { tap ->
                    val left = AxisWidth.toPx()
                    val right = size.width - 8.dp.toPx()
                    val index = ((tap.x - left) / (right - left) * values.size).toInt()
                    onSelect(index.coerceIn(values.indices))
                }
            },
    ) {
        val area = plotArea()
        drawValueAxis(area, scale, measurer, labelStyle, colors.outlineVariant)
        drawDateLabels(area, firstLabel, lastLabel, measurer, labelStyle)

        val slot = area.width / values.size
        val barWidth = (slot * 0.7f).coerceAtLeast(2.dp.toPx())
        val corner = CornerRadius((barWidth / 3).coerceAtMost(4.dp.toPx()))
        values.forEachIndexed { index, value ->
            if (value > 0) {
                val top = area.yOf(value, scale)
                drawRoundRect(
                    color = if (index == selectedIndex) colors.primary else colors.primary.copy(alpha = 0.45f),
                    topLeft = Offset(area.left + slot * index + (slot - barWidth) / 2, top),
                    size = Size(barWidth, area.bottom - top),
                    cornerRadius = corner,
                )
            }
        }
    }
}
