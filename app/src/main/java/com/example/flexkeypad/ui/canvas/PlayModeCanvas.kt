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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.input.pointer.PointerEventPass
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

    val currentButtons by rememberUpdatedState(buttons)
    val currentOnPressed by rememberUpdatedState(onButtonPressed)
    val currentOnReleased by rememberUpdatedState(onButtonReleased)
    val currentOnReleaseAll by rememberUpdatedState(onReleaseAll)

    // Map pointer ID to the button ID it is currently pressing
    val activePointers = remember { mutableStateMapOf<PointerId, String>() }

    DisposableEffect(Unit) {
        onDispose {
            activePointers.clear()
            currentOnReleaseAll()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitEachGesture {
                    do {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val changes = event.changes

                        for (change in changes) {
                            val pointerId = change.id
                            val isPressed = change.pressed
                            val prevButtonId = activePointers[pointerId]

                            if (isPressed) {
                                val hitButton = findTouchedButton(
                                    point = change.position,
                                    buttons = currentButtons,
                                    density = density,
                                    paddingDp = 6f
                                )
                                val hitId = hitButton?.id

                                if (hitId != prevButtonId) {
                                    // Pointer moved to a new button or just touched down
                                    if (prevButtonId != null) {
                                        activePointers.remove(pointerId)
                                        // Only release button if no other pointer is also holding it
                                        if (!activePointers.values.contains(prevButtonId)) {
                                            currentButtons.find { it.id == prevButtonId }?.let {
                                                currentOnReleased(it)
                                            }
                                        }
                                    }

                                    if (hitButton != null) {
                                        val isAlreadyHeld = activePointers.values.contains(hitButton.id)
                                        activePointers[pointerId] = hitButton.id
                                        if (!isAlreadyHeld) {
                                            currentOnPressed(hitButton)
                                        }
                                    }
                                }
                                change.consume()
                            } else {
                                // Pointer lifted (UP)
                                if (prevButtonId != null) {
                                    activePointers.remove(pointerId)
                                    // Only release button if no other pointer is still holding it
                                    if (!activePointers.values.contains(prevButtonId)) {
                                        currentButtons.find { it.id == prevButtonId }?.let {
                                            currentOnReleased(it)
                                        }
                                    }
                                }
                                change.consume()
                            }
                        }
                    } while (event.changes.any { it.pressed })

                    // All pointers in this gesture cycle have been lifted
                    if (activePointers.isNotEmpty()) {
                        activePointers.clear()
                        currentOnReleaseAll()
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

private fun isPointInsideButton(
    point: Offset,
    button: KeypadButton,
    density: Float,
    paddingDp: Float = 6f
): Boolean {
    val padPx = paddingDp * density
    val leftPx = button.positionX * density - padPx
    val topPx = button.positionY * density - padPx
    val widthPx = button.width * density + (padPx * 2)
    val heightPx = button.height * density + (padPx * 2)
    return point.x >= leftPx &&
            point.x <= (leftPx + widthPx) &&
            point.y >= topPx &&
            point.y <= (topPx + heightPx)
}

private fun findTouchedButton(
    point: Offset,
    buttons: List<KeypadButton>,
    density: Float,
    paddingDp: Float = 6f
): KeypadButton? {
    val candidates = buttons.filter { button ->
        isPointInsideButton(point, button, density, paddingDp)
    }
    if (candidates.isEmpty()) return null
    if (candidates.size == 1) return candidates.first()

    // If multiple buttons fall within the padding tolerance,
    // pick the button whose geometric center is closest to the touch point
    return candidates.minByOrNull { button ->
        val centerX = (button.positionX + button.width / 2f) * density
        val centerY = (button.positionY + button.height / 2f) * density
        val dx = point.x - centerX
        val dy = point.y - centerY
        dx * dx + dy * dy
    }
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
