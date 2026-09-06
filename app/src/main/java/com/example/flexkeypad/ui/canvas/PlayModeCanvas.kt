package com.example.flexkeypad.ui.canvas

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerId
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
import com.example.flexkeypad.ui.theme.NeonGreen
import kotlin.math.roundToInt

@Composable
fun PlayModeCanvas(
    buttons: List<KeypadButton>,
    pressedButtonIds: Set<String>,
    onButtonPressed: (KeypadButton) -> Unit,
    onButtonReleased: (KeypadButton) -> Unit,
    onReleaseAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current.density
    // Track pointer ID to currently pressed button ID for multi-touch accuracy
    val pointerToButtonMap = remember { mutableStateMapOf<PointerId, String>() }

    DisposableEffect(Unit) {
        onDispose {
            pointerToButtonMap.clear()
            onReleaseAll()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(buttons) {
                awaitEachGesture {
                    while (true) {
                        val event = awaitPointerEvent()
                        val changes = event.changes

                        for (change in changes) {
                            val pointerId = change.id
                            val currentPos = change.position

                            if (change.pressed && !change.previousPressed) {
                                // Pointer down: find touched button
                                val hitButton = buttons.findLast { button ->
                                    isPointInsideButton(currentPos, button, density)
                                }
                                if (hitButton != null) {
                                    pointerToButtonMap[pointerId] = hitButton.id
                                    onButtonPressed(hitButton)
                                    change.consume()
                                }
                            } else if (!change.pressed && change.previousPressed) {
                                // Pointer up: release corresponding button
                                val buttonId = pointerToButtonMap.remove(pointerId)
                                if (buttonId != null) {
                                    val button = buttons.find { it.id == buttonId }
                                    if (button != null) {
                                        onButtonReleased(button)
                                    }
                                    change.consume()
                                }
                            } else if (change.pressed && change.previousPressed) {
                                // Pointer drag: check if still inside original button
                                val assignedButtonId = pointerToButtonMap[pointerId]
                                if (assignedButtonId != null) {
                                    val button = buttons.find { it.id == assignedButtonId }
                                    if (button != null && !isPointInsideButton(currentPos, button, density)) {
                                        // Pointer slipped outside button bounding box
                                        pointerToButtonMap.remove(pointerId)
                                        onButtonReleased(button)
                                    }
                                }
                            }
                        }

                        // If no pointers remain active, clean up
                        if (changes.none { it.pressed } && pointerToButtonMap.isNotEmpty()) {
                            pointerToButtonMap.clear()
                            onReleaseAll()
                        }
                    }
                }
            }
    ) {
        buttons.forEach { button ->
            val isPressed = pressedButtonIds.contains(button.id)
            PlayModeButtonView(
                button = button,
                isPressed = isPressed,
                modifier = Modifier.offset {
                    IntOffset(
                        (button.positionX * density).roundToInt(),
                        (button.positionY * density).roundToInt()
                    )
                }
            )
        }
    }
}

private fun isPointInsideButton(point: Offset, button: KeypadButton, density: Float): Boolean {
    val leftPx = button.positionX * density
    val topPx = button.positionY * density
    val widthPx = button.width * density
    val heightPx = button.height * density
    return point.x >= leftPx &&
            point.x <= (leftPx + widthPx) &&
            point.y >= topPx &&
            point.y <= (topPx + heightPx)
}


@Composable
fun PlayModeButtonView(
    button: KeypadButton,
    isPressed: Boolean,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        label = "ButtonScale"
    )

    val bgColor = remember(button.backgroundColor) {
        parseColorHex(button.backgroundColor, Color(0xFF1E293B))
    }
    val textColor = remember(button.textColor) {
        parseColorHex(button.textColor, Color.White)
    }

    val displayBgColor = if (isPressed) {
        bgColor.copy(alpha = 0.85f)
    } else {
        bgColor
    }

    val borderColor = if (isPressed) NeonGreen else AmoledBorder

    Box(
        modifier = modifier
            .size(button.width.dp, button.height.dp)
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 2.dp else 6.dp,
                shape = RoundedCornerShape(14.dp),
                spotColor = if (isPressed) NeonGreen.copy(alpha = 0.5f) else Color.Black
            )
            .clip(RoundedCornerShape(14.dp))
            .background(displayBgColor)
            .border(
                width = if (isPressed) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Modifiers badges if configured
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
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .background(NeonCyan.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 3.dp, vertical = 1.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Main Label
            Text(
                text = button.label,
                color = textColor,
                fontSize = if (button.height > 80) 16.sp else 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

fun parseColorHex(hex: String, defaultColor: Color): Color {
    return try {
        val clean = hex.removePrefix("#")
        val colorInt = clean.toLong(16)
        if (clean.length == 6) {
            Color(colorInt or 0x00000000FF000000L)
        } else {
            Color(colorInt)
        }
    } catch (_: Exception) {
        defaultColor
    }
}
