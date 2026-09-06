package com.example.flexkeypad.ui.canvas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flexkeypad.domain.model.KeypadButton
import com.example.flexkeypad.ui.theme.AmoledBorder
import com.example.flexkeypad.ui.theme.NeonCyan
import com.example.flexkeypad.ui.theme.NeonRed
import com.example.flexkeypad.ui.theme.TextMuted
import kotlin.math.roundToInt

@Composable
fun EditModeCanvas(
    buttons: List<KeypadButton>,
    selectedButtonId: String?,
    isSnapToGrid: Boolean,
    gridSize: Float,
    onSelectButton: (String?) -> Unit,
    onMoveButtonLive: (buttonId: String, newX: Float, newY: Float) -> Unit,
    onCommitMoveButton: (buttonId: String, finalX: Float, finalY: Float) -> Unit,
    onResizeButtonLive: (buttonId: String, newWidth: Float, newHeight: Float) -> Unit,
    onCommitResizeButton: (buttonId: String, finalWidth: Float, finalHeight: Float) -> Unit,
    onEditButton: (KeypadButton) -> Unit,
    onDeleteButton: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current.density

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures {
                    onSelectButton(null)
                }
            }
    ) {
        // Render alignment grid if enabled
        if (isSnapToGrid) {
            GridOverlay(gridSizePx = gridSize * density)
        }

        buttons.forEach { button ->
            val isSelected = button.id == selectedButtonId

            EditModeButtonItem(
                button = button,
                isSelected = isSelected,
                density = density,
                onSelect = { onSelectButton(button.id) },
                onMoveLive = { x, y ->
                    onMoveButtonLive(button.id, x, y)
                },
                onCommitMove = { x, y ->
                    onCommitMoveButton(button.id, x, y)
                },
                onResizeLive = { w, h ->
                    onResizeButtonLive(button.id, w, h)
                },
                onCommitResize = { w, h ->
                    onCommitResizeButton(button.id, w, h)
                },
                onEdit = { onEditButton(button) },
                onDelete = { onDeleteButton(button.id) },
                modifier = Modifier.offset {
                    IntOffset(
                        (button.positionX * density).roundToInt(),
                        (button.positionY * density).roundToInt()
                    )
                }
            )
        }

        // Empty canvas helper message if no buttons exist yet
        if (buttons.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Canvas is empty",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tap '+ Add Key' above to create your first macro button.",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun EditModeButtonItem(
    button: KeypadButton,
    isSelected: Boolean,
    density: Float,
    onSelect: () -> Unit,
    onMoveLive: (newX: Float, newY: Float) -> Unit,
    onCommitMove: (finalX: Float, finalY: Float) -> Unit,
    onResizeLive: (newW: Float, newH: Float) -> Unit,
    onCommitResize: (finalW: Float, finalH: Float) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = remember(button.backgroundColor) {
        parseColorHex(button.backgroundColor, Color(0xFF1E293B))
    }
    val textColor = remember(button.textColor) {
        parseColorHex(button.textColor, Color.White)
    }

    // Capture latest lambdas and button data to avoid stale closures in pointerInput
    val currentButton by rememberUpdatedState(button)
    val currentOnSelect by rememberUpdatedState(onSelect)
    val currentOnMoveLive by rememberUpdatedState(onMoveLive)
    val currentOnCommitMove by rememberUpdatedState(onCommitMove)
    val currentOnResizeLive by rememberUpdatedState(onResizeLive)
    val currentOnCommitResize by rememberUpdatedState(onCommitResize)

    Box(
        modifier = modifier
            .size(button.width.dp, button.height.dp)
    ) {
        // Main Button Surface with Smooth Drag Gesture
        var dragAccumulatedX = 0f
        var dragAccumulatedY = 0f

        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(
                    elevation = if (isSelected) 8.dp else 4.dp,
                    shape = RoundedCornerShape(14.dp),
                    spotColor = if (isSelected) NeonCyan else Color.Black
                )
                .clip(RoundedCornerShape(14.dp))
                .background(bgColor)
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) NeonCyan else AmoledBorder,
                    shape = RoundedCornerShape(14.dp)
                )
                .pointerInput(button.id) {
                    detectTapGestures {
                        currentOnSelect()
                    }
                }
                .pointerInput(button.id) {
                    detectDragGestures(
                        onDragStart = {
                            currentOnSelect()
                            dragAccumulatedX = currentButton.positionX
                            dragAccumulatedY = currentButton.positionY
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val deltaDpX = dragAmount.x / density
                            val deltaDpY = dragAmount.y / density
                            dragAccumulatedX = (dragAccumulatedX + deltaDpX).coerceAtLeast(0f)
                            dragAccumulatedY = (dragAccumulatedY + deltaDpY).coerceAtLeast(0f)
                            currentOnMoveLive(dragAccumulatedX, dragAccumulatedY)
                        },
                        onDragEnd = {
                            currentOnCommitMove(dragAccumulatedX, dragAccumulatedY)
                        },
                        onDragCancel = {
                            currentOnCommitMove(dragAccumulatedX, dragAccumulatedY)
                        }
                    )
                }
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (button.modifiers.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        button.modifiers.forEach { mod ->
                            val modText = when (mod.name) {
                                "LEFT_CTRL", "RIGHT_CTRL" -> "CTRL"
                                "LEFT_ALT", "RIGHT_ALT" -> "ALT"
                                "LEFT_SHIFT", "RIGHT_SHIFT" -> "SHIFT"
                                "LEFT_GUI", "RIGHT_GUI" -> "WIN"
                                else -> mod.name
                            }
                            Text(
                                text = modText,
                                color = NeonCyan,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(horizontal = 1.dp)
                                    .background(NeonCyan.copy(alpha = 0.15f), RoundedCornerShape(3.dp))
                                    .padding(horizontal = 2.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                }

                Text(
                    text = button.label,
                    color = textColor,
                    fontSize = if (button.height > 80) 15.sp else 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Action handles when button is selected
        if (isSelected) {
            // Quick Edit & Delete pill at top-right
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-14).dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, AmoledBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Details",
                        tint = NeonCyan,
                        modifier = Modifier.size(13.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Button",
                        tint = NeonRed,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            // Resize Handle at Bottom-Right
            var resizeAccumulatedW = 0f
            var resizeAccumulatedH = 0f

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 8.dp, y = 8.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(NeonCyan)
                    .border(2.dp, Color.Black, CircleShape)
                    .pointerInput(button.id) {
                        detectDragGestures(
                            onDragStart = {
                                resizeAccumulatedW = currentButton.width
                                resizeAccumulatedH = currentButton.height
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val deltaDpW = dragAmount.x / density
                                val deltaDpH = dragAmount.y / density
                                resizeAccumulatedW = (resizeAccumulatedW + deltaDpW).coerceIn(60f, 800f)
                                resizeAccumulatedH = (resizeAccumulatedH + deltaDpH).coerceIn(50f, 600f)
                                currentOnResizeLive(resizeAccumulatedW, resizeAccumulatedH)
                            },
                            onDragEnd = {
                                currentOnCommitResize(resizeAccumulatedW, resizeAccumulatedH)
                            },
                            onDragCancel = {
                                currentOnCommitResize(resizeAccumulatedW, resizeAccumulatedH)
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.Black)
                )
            }
        }
    }
}
