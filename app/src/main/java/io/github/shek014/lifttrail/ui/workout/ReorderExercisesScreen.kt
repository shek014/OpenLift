package io.github.shek014.lifttrail.ui.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.shek014.lifttrail.data.model.moved
import kotlin.math.roundToInt

private val RowHeight = 64.dp

/** Something that can be dragged into a new position. */
data class ReorderItem(val id: Long, val name: String)

/** Reorders the exercises of a live or saved workout. */
@Composable
fun ReorderExercisesScreen(viewModel: WorkoutEditorViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ReorderScreen(
        items = state?.workout?.exercises.orEmpty().map { ReorderItem(it.id, it.exercise.name) },
        onSave = viewModel::reorderExercises,
        onBack = onBack,
    )
}

/** Drag an item by its handle to move it. [onSave] gets the new order each time one is let go. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReorderScreen(
    items: List<ReorderItem>,
    onSave: (List<Long>) -> Unit,
    onBack: () -> Unit,
    title: String = "Reorder exercises",
) {
    // The order on screen, which moves ahead of what's saved during a drag.
    var order by remember { mutableStateOf<List<ReorderItem>?>(null) }
    val shown = order ?: items

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(title) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            shown.forEachIndexed { index, item ->
                key(item.id) {
                    ReorderRow(
                        name = item.name,
                        canMoveUp = index > 0,
                        canMoveDown = index < shown.lastIndex,
                        onMove = { by ->
                            val current = order ?: items
                            val from = current.indexOfFirst { it.id == item.id }
                            val updated = current.moved(from, from + by)
                            order = updated
                            updated != current
                        },
                        onDrop = { onSave((order ?: items).map { it.id }) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ReorderRow(
    name: String,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMove: (by: Int) -> Boolean,
    onDrop: () -> Unit,
) {
    val rowHeight = with(LocalDensity.current) { RowHeight.toPx() }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var dragging by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme

    fun moveFromAccessibility(by: Int): Boolean {
        val changed = onMove(by)
        if (changed) onDrop()
        return changed
    }

    Column(
        modifier = Modifier
            .zIndex(if (dragging) 1f else 0f)
            .offset { IntOffset(0, dragOffset.roundToInt()) }
            .background(if (dragging) colors.primaryContainer else colors.background)
            .semantics {
                customActions = buildList {
                    if (canMoveUp) add(CustomAccessibilityAction("Move up") { moveFromAccessibility(-1) })
                    if (canMoveDown) add(CustomAccessibilityAction("Move down") { moveFromAccessibility(1) })
                }
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(RowHeight)
                .padding(start = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            Icon(
                imageVector = Icons.Filled.DragHandle,
                contentDescription = "Drag to move $name",
                tint = colors.onSurfaceVariant,
                modifier = Modifier
                    .padding(16.dp)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { dragging = true },
                            onDragEnd = {
                                dragging = false
                                dragOffset = 0f
                                onDrop()
                            },
                            onDragCancel = {
                                dragging = false
                                dragOffset = 0f
                                onDrop()
                            },
                        ) { change, amount ->
                            change.consume()
                            dragOffset += amount.y
                            // Past halfway into the next row, swap places with it.
                            while (dragOffset > rowHeight / 2 && onMove(1)) dragOffset -= rowHeight
                            while (dragOffset < -rowHeight / 2 && onMove(-1)) dragOffset += rowHeight
                        }
                    },
            )
        }
        HorizontalDivider(color = colors.outlineVariant)
    }
}
