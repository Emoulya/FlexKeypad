package com.emoulya.flexkeypad.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.emoulya.flexkeypad.domain.model.HidKeyCodes
import com.emoulya.flexkeypad.domain.model.KeyCategory
import com.emoulya.flexkeypad.domain.model.KeypadButton
import com.emoulya.flexkeypad.domain.model.ModifierKey
import com.emoulya.flexkeypad.util.parseColorHex
import com.emoulya.flexkeypad.ui.theme.AmoledBorder
import com.emoulya.flexkeypad.ui.theme.AmoledSurface
import com.emoulya.flexkeypad.ui.theme.AmoledSurfaceVariant
import com.emoulya.flexkeypad.ui.theme.NeonCyan
import com.emoulya.flexkeypad.ui.theme.NeonGreen
import com.emoulya.flexkeypad.ui.theme.NeonRed
import com.emoulya.flexkeypad.ui.theme.TextMuted
import com.emoulya.flexkeypad.ui.theme.TextPrimary
import com.emoulya.flexkeypad.ui.theme.TextSecondary

private val PRESET_COLORS = listOf(
    "#1E293B", "#0F172A", "#334155",
    "#1E3A8A", "#0C4A6E", "#065F46",
    "#713F12", "#831843", "#581C87"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ButtonEditorDialog(
    button: KeypadButton,
    onSave: (KeypadButton) -> Unit,
    onDelete: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var label by remember { mutableStateOf(button.label) }
    var selectedKeyCode by remember { mutableIntStateOf(button.hidKeyCode) }
    var selectedModifiers by remember { mutableStateOf(button.modifiers.toSet()) }
    var selectedBgColor by remember { mutableStateOf(button.backgroundColor) }
    var hapticEnabled by remember { mutableStateOf(button.hapticEnabled) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val initialCategory = remember(button.hidKeyCode) {
        HidKeyCodes.ALL_KEYS.find { it.code == button.hidKeyCode }?.category ?: KeyCategory.LETTERS
    }
    var selectedCategory by remember { mutableStateOf(initialCategory) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .fillMaxHeight(0.9f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, AmoledBorder, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = AmoledSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Customize Key",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Label Input
                    item {
                        Column {
                            Text(
                                text = "Key Label",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = label,
                                onValueChange = { label = it },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = AmoledBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedContainerColor = AmoledSurfaceVariant,
                                    unfocusedContainerColor = AmoledSurfaceVariant
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    // 2. Modifiers (Hotkeys)
                    item {
                        Column {
                            Text(
                                text = "Modifiers (Hotkey Combinations)",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val mods = listOf(
                                    ModifierKey.LEFT_CTRL to "CTRL",
                                    ModifierKey.LEFT_ALT to "ALT",
                                    ModifierKey.LEFT_SHIFT to "SHIFT",
                                    ModifierKey.LEFT_GUI to "WIN / CMD"
                                )
                                mods.forEach { (mod, name) ->
                                    val isChecked = selectedModifiers.contains(mod)
                                    FilterChip(
                                        selected = isChecked,
                                        onClick = {
                                            selectedModifiers = if (isChecked) {
                                                selectedModifiers - mod
                                            } else {
                                                selectedModifiers + mod
                                            }
                                        },
                                        label = { Text(name, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                                            selectedLabelColor = NeonCyan,
                                            containerColor = AmoledSurfaceVariant,
                                            labelColor = TextMuted
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 3. Keycode Selection (Category & Grid)
                    item {
                        Column {
                            Text(
                                text = "Assigned Key: ${HidKeyCodes.getKeyLabel(selectedKeyCode)}",
                                color = NeonCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Category tabs
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(KeyCategory.entries.toTypedArray()) { cat ->
                                    val isSelected = cat == selectedCategory
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else AmoledSurfaceVariant)
                                            .border(1.dp, if (isSelected) NeonCyan else Color.Transparent, RoundedCornerShape(8.dp))
                                            .clickable { selectedCategory = cat }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = cat.displayName,
                                            color = if (isSelected) NeonCyan else TextMuted,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Keys chips under chosen category
                            val keysInCategory = HidKeyCodes.ALL_KEYS.filter { it.category == selectedCategory }
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                keysInCategory.forEach { keyDef ->
                                    val isChosen = keyDef.code == selectedKeyCode
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isChosen) NeonGreen.copy(alpha = 0.25f) else AmoledSurfaceVariant)
                                            .border(1.dp, if (isChosen) NeonGreen else AmoledBorder, RoundedCornerShape(8.dp))
                                            .clickable {
                                                val prevKeyLabel = HidKeyCodes.getKeyLabel(selectedKeyCode)
                                                selectedKeyCode = keyDef.code
                                                if (label.isBlank() || label.startsWith("BTN") || label == prevKeyLabel) {
                                                    label = keyDef.label
                                                }
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = keyDef.label,
                                            color = if (isChosen) NeonGreen else TextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 4. Background Color Palette
                    item {
                        Column {
                            Text(
                                text = "Button Color",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                PRESET_COLORS.forEach { colorHex ->
                                    val color = parseColorHex(colorHex, Color.DarkGray)
                                    val isSelected = selectedBgColor.equals(colorHex, ignoreCase = true)
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .border(
                                                width = if (isSelected) 3.dp else 1.dp,
                                                color = if (isSelected) NeonCyan else AmoledBorder,
                                                shape = CircleShape
                                            )
                                            .clickable { selectedBgColor = colorHex }
                                    )
                                }
                            }
                        }
                    }

                    // 5. Tactile Haptic Switch
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(AmoledSurfaceVariant)
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tactile Haptic Feedback",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Switch(
                                checked = hapticEnabled,
                                onCheckedChange = { hapticEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = NeonCyan,
                                    checkedTrackColor = NeonCyan.copy(alpha = 0.3f),
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = AmoledSurface
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Footer Actions (Save & Delete)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { showDeleteConfirmation = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonRed.copy(alpha = 0.15f),
                            contentColor = NeonRed
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Delete", fontSize = 12.sp)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AmoledSurfaceVariant,
                                contentColor = TextMuted
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancel", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                val updated = button.copy(
                                    label = label.ifBlank { "KEY" },
                                    hidKeyCode = selectedKeyCode,
                                    modifiers = selectedModifiers.toList(),
                                    backgroundColor = selectedBgColor,
                                    hapticEnabled = hapticEnabled
                                )
                                onSave(updated)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonCyan,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Save",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Confirmation Dialog before Deleting Button
        if (showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = false },
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
                        text = "Apakah Anda yakin ingin menghapus tombol \"${button.label}\"?",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteConfirmation = false
                            onDelete(button.id)
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
                        onClick = { showDeleteConfirmation = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmoledSurfaceVariant,
                            contentColor = TextMuted
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Batal")
                    }
                },
                containerColor = AmoledSurface,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}
