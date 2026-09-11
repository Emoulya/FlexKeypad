package com.emoulya.flexkeypad.ui.canvas

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emoulya.flexkeypad.domain.model.KeypadButton
import com.emoulya.flexkeypad.util.ButtonSnappingHelper
import com.emoulya.flexkeypad.ui.theme.AmoledBorder
import com.emoulya.flexkeypad.ui.theme.NeonCyan
import com.emoulya.flexkeypad.ui.theme.NeonRed
import com.emoulya.flexkeypad.ui.theme.TextMuted
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MenuDefaults
import androidx.compose.runtime.mutableStateOf
import com.emoulya.flexkeypad.ui.theme.AmoledSurface
import com.emoulya.flexkeypad.ui.theme.NeonGreen
import com.emoulya.flexkeypad.ui.theme.TextPrimary
import com.emoulya.flexkeypad.util.parseColorHex
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import kotlin.math.roundToInt

@Composable
fun EditModeCanvas(
    buttons: List<KeypadButton>,
    selectedButtonId: String?,
    showGrid: Boolean,
    isSnapToButtons: Boolean,
    gridSize: Float,
    onSelectButton: (String?) -> Unit,
    onMoveButtonLive: (buttonId: String, newX: Float, newY: Float) -> Unit,
    onCommitMoveButton: (buttonId: String, finalX: Float, finalY: Float) -> Unit,
    onResizeButtonLive: (buttonId: String, newWidth: Float, newHeight: Float) -> Unit,
    onCommitResizeButton: (buttonId: String, finalWidth: Float, finalHeight: Float) -> Unit,
    onEditButton: (KeypadButton) -> Unit,
    onDuplicateButton: (KeypadButton) -> Unit,
    onDeleteButton: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current.density
    var buttonPendingDelete by remember { mutableStateOf<KeypadButton?>(null) }
    var activeGuideLineX by remember { mutableStateOf<Float?>(null) }
    var activeGuideLineY by remember { mutableStateOf<Float?>(null) }

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
        if (showGrid) {
            GridOverlay(gridSizePx = gridSize * density)
        }

        // Render dynamic magnetic snap guidelines
        if (isSnapToButtons && (activeGuideLineX != null || activeGuideLineY != null)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                activeGuideLineX?.let { gx ->
                    val px = gx * density
                    drawLine(
                        color = NeonCyan.copy(alpha = 0.8f),
                        start = Offset(px, 0f),
                        end = Offset(px, size.height),
                        strokeWidth = 1.5f * density,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f * density, 4f * density))
                    )
                }
                activeGuideLineY?.let { gy ->
                    val py = gy * density
                    drawLine(
                        color = NeonCyan.copy(alpha = 0.8f),
                        start = Offset(0f, py),
                        end = Offset(size.width, py),
                        strokeWidth = 1.5f * density,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f * density, 4f * density))
                    )
                }
            }
        }

        buttons.forEach { button ->
            val isSelected = button.id == selectedButtonId

            EditModeButtonItem(
                button = button,
                allButtons = buttons,
                isSnapToButtons = isSnapToButtons,
                isSelected = isSelected,
                density = density,
                onSelect = { onSelectButton(button.id) },
                onMoveLive = { x, y ->
                    onMoveButtonLive(button.id, x, y)
                },
                onCommitMove = { x, y ->
                    onCommitMoveButton(button.id, x, y)
                },
                onUpdateGuidelines = { gx, gy ->
                    activeGuideLineX = gx
                    activeGuideLineY = gy
                },
                onResizeLive = { w, h ->
                    onResizeButtonLive(button.id, w, h)
                },
                onCommitResize = { w, h ->
                    onCommitResizeButton(button.id, w, h)
                },
                onEdit = { onEditButton(button) },
                onDuplicate = { onDuplicateButton(button) },
                onDelete = { buttonPendingDelete = button },
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

        // Confirmation Dialog before Deleting Button
        buttonPendingDelete?.let { btn ->
            AlertDialog(
                onDismissRequest = { buttonPendingDelete = null },
                title = {
                    Text(
                        text = "Hapus Tombol",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                },
                text = {
                    Text(
                        text = "Apakah Anda yakin ingin menghapus tombol \"${btn.label}\"?",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onDeleteButton(btn.id)
                            buttonPendingDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Hapus", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { buttonPendingDelete = null },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmoledSurface,
                            contentColor = TextMuted
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Batal")
                    }
                },
                containerColor = Color(0xFF0F172A),
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
fun EditModeButtonItem(
    button: KeypadButton,
    allButtons: List<KeypadButton>,
    isSnapToButtons: Boolean,
    isSelected: Boolean,
    density: Float,
    onSelect: () -> Unit,
    onMoveLive: (newX: Float, newY: Float) -> Unit,
    onCommitMove: (finalX: Float, finalY: Float) -> Unit,
    onUpdateGuidelines: (guideX: Float?, guideY: Float?) -> Unit,
    onResizeLive: (newW: Float, newH: Float) -> Unit,
    onCommitResize: (finalW: Float, finalH: Float) -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
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
    val currentAllButtons by rememberUpdatedState(allButtons)
    val currentIsSnapToButtons by rememberUpdatedState(isSnapToButtons)
    val currentOnSelect by rememberUpdatedState(onSelect)
    val currentOnMoveLive by rememberUpdatedState(onMoveLive)
    val currentOnCommitMove by rememberUpdatedState(onCommitMove)
    val currentOnUpdateGuidelines by rememberUpdatedState(onUpdateGuidelines)
    val currentOnResizeLive by rememberUpdatedState(onResizeLive)
    val currentOnCommitResize by rememberUpdatedState(onCommitResize)
    val currentOnDuplicate by rememberUpdatedState(onDuplicate)

    var showDuplicateMenu by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(button.width.dp, button.height.dp)
    ) {
        // Main Button Surface with Smooth Drag Gesture & Hold-to-Duplicate
        var dragAccumulatedX by remember(button.id) { mutableFloatStateOf(0f) }
        var dragAccumulatedY by remember(button.id) { mutableFloatStateOf(0f) }

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
                    detectTapGestures(
                        onTap = {
                            currentOnSelect()
                        },
                        onLongPress = {
                            currentOnSelect()
                            showDuplicateMenu = true
                        }
                    )
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

                            val (liveX, liveY) = if (currentIsSnapToButtons) {
                                val snap = ButtonSnappingHelper.calculateSnap(
                                    buttonId = currentButton.id,
                                    currentX = dragAccumulatedX,
                                    currentY = dragAccumulatedY,
                                    width = currentButton.width,
                                    height = currentButton.height,
                                    otherButtons = currentAllButtons
                                )
                                currentOnUpdateGuidelines(snap.guideLineX, snap.guideLineY)
                                snap.snappedX to snap.snappedY
                            } else {
                                currentOnUpdateGuidelines(null, null)
                                dragAccumulatedX to dragAccumulatedY
                            }

                            currentOnMoveLive(liveX, liveY)
                        },
                        onDragEnd = {
                            currentOnUpdateGuidelines(null, null)
                            val (finalX, finalY) = if (currentIsSnapToButtons) {
                                val snap = ButtonSnappingHelper.calculateSnap(
                                    buttonId = currentButton.id,
                                    currentX = dragAccumulatedX,
                                    currentY = dragAccumulatedY,
                                    width = currentButton.width,
                                    height = currentButton.height,
                                    otherButtons = currentAllButtons
                                )
                                snap.snappedX to snap.snappedY
                            } else {
                                dragAccumulatedX to dragAccumulatedY
                            }
                            currentOnCommitMove(finalX, finalY)
                        },
                        onDragCancel = {
                            currentOnUpdateGuidelines(null, null)
                            val (finalX, finalY) = if (currentIsSnapToButtons) {
                                val snap = ButtonSnappingHelper.calculateSnap(
                                    buttonId = currentButton.id,
                                    currentX = dragAccumulatedX,
                                    currentY = dragAccumulatedY,
                                    width = currentButton.width,
                                    height = currentButton.height,
                                    otherButtons = currentAllButtons
                                )
                                snap.snappedX to snap.snappedY
                            } else {
                                dragAccumulatedX to dragAccumulatedY
                            }
                            currentOnCommitMove(finalX, finalY)
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

        // Contextual Hold/Long-Press Duplicate Menu
        DropdownMenu(
            expanded = showDuplicateMenu,
            onDismissRequest = { showDuplicateMenu = false },
            modifier = Modifier
                .width(220.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(AmoledSurface)
                .border(1.dp, AmoledBorder, RoundedCornerShape(14.dp))
                .padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(NeonCyan)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tombol: ${button.label}",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider(color = AmoledBorder.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))

            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Duplikat",
                        tint = NeonGreen,
                        modifier = Modifier.size(18.dp)
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Duplikat Tombol",
                            color = NeonGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Salin ukuran & fungsi tombol",
                            color = TextMuted,
                            fontSize = 9.5.sp
                        )
                    }
                },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                onClick = {
                    showDuplicateMenu = false
                    currentOnDuplicate()
                },
                colors = MenuDefaults.itemColors(textColor = TextPrimary)
            )

            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                },
                text = {
                    Text(text = "Edit Konfigurasi", color = TextPrimary, fontSize = 12.sp)
                },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                onClick = {
                    showDuplicateMenu = false
                    onEdit()
                },
                colors = MenuDefaults.itemColors(textColor = TextPrimary)
            )

            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus",
                        tint = NeonRed,
                        modifier = Modifier.size(18.dp)
                    )
                },
                text = {
                    Text(text = "Hapus Tombol", color = NeonRed, fontSize = 12.sp)
                },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                onClick = {
                    showDuplicateMenu = false
                    onDelete()
                },
                colors = MenuDefaults.itemColors(textColor = TextPrimary)
            )
        }

        // Action handles when button is selected
        if (isSelected) {
            // Quick Duplicate, Edit & Delete pill at top-right inside button bounds
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 4.dp, end = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F172A).copy(alpha = 0.95f))
                    .border(1.dp, AmoledBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDuplicate,
                    modifier = Modifier.size(22.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Duplicate Button",
                        tint = NeonGreen,
                        modifier = Modifier.size(12.dp)
                    )
                }

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(22.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Details",
                        tint = NeonCyan,
                        modifier = Modifier.size(12.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(22.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Button",
                        tint = NeonRed,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            // Resize Handle at Bottom-Right inside button bounds
            var resizeAccumulatedW by remember(button.id) { mutableFloatStateOf(0f) }
            var resizeAccumulatedH by remember(button.id) { mutableFloatStateOf(0f) }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 4.dp, end = 4.dp)
                    .size(22.dp)
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
