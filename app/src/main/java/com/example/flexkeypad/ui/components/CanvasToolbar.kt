package com.example.flexkeypad.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridOff
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flexkeypad.domain.model.CanvasMode
import com.example.flexkeypad.domain.model.ConnectionStatus
import com.example.flexkeypad.ui.theme.AmoledBorder
import com.example.flexkeypad.ui.theme.AmoledSurface
import com.example.flexkeypad.ui.theme.NeonAmber
import com.example.flexkeypad.ui.theme.NeonCyan
import com.example.flexkeypad.ui.theme.NeonGreen
import com.example.flexkeypad.ui.theme.NeonRed
import com.example.flexkeypad.ui.theme.TextMuted
import com.example.flexkeypad.ui.theme.TextPrimary
import com.example.flexkeypad.ui.viewmodel.KeypadUiState

@Composable
fun CanvasToolbar(
    state: KeypadUiState,
    onToggleMode: () -> Unit,
    onAddNewButton: () -> Unit,
    onToggleGrid: () -> Unit,
    onOpenProfileDialog: () -> Unit,
    onOpenConnectionDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(AmoledSurface.copy(alpha = 0.92f))
                .border(1.dp, AmoledBorder, RoundedCornerShape(24.dp))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Profile Name & Selector
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onOpenProfileDialog() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = "Profiles",
                    tint = NeonCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = state.activeProfile.profileName,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Center: Actions (Add Button, Snap to Grid)
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedVisibility(visible = state.canvasMode == CanvasMode.EDIT) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = onAddNewButton,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonCyan.copy(alpha = 0.15f),
                                contentColor = NeonCyan
                            ),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Button",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Add Key", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = onToggleGrid,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    if (state.isSnapToGrid) NeonCyan.copy(alpha = 0.15f) else Color.Transparent
                                )
                        ) {
                            Icon(
                                imageVector = if (state.isSnapToGrid) Icons.Default.GridOn else Icons.Default.GridOff,
                                contentDescription = "Snap to Grid",
                                tint = if (state.isSnapToGrid) NeonCyan else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Right: Connection Status & Mode Switcher
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Connection Indicator Button
                ConnectionBadge(
                    status = state.connectionStatus,
                    connectedName = state.connectedDeviceName,
                    usbCount = state.usbClientsCount,
                    onClick = onOpenConnectionDialog
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Mode Toggle Button (Edit vs Play)
                val isPlay = state.canvasMode == CanvasMode.PLAY
                val buttonBg by animateColorAsState(
                    targetValue = if (isPlay) NeonGreen.copy(alpha = 0.2f) else NeonAmber.copy(alpha = 0.2f),
                    label = "ModeColor"
                )
                val buttonText = if (isPlay) NeonGreen else NeonAmber
                val buttonBorder = if (isPlay) NeonGreen.copy(alpha = 0.6f) else NeonAmber.copy(alpha = 0.6f)

                Row(
                    modifier = Modifier
                        .height(34.dp)
                        .clip(RoundedCornerShape(17.dp))
                        .background(buttonBg)
                        .border(1.dp, buttonBorder, RoundedCornerShape(17.dp))
                        .clickable { onToggleMode() }
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isPlay) Icons.Default.Lock else Icons.Default.Edit,
                        contentDescription = "Mode",
                        tint = buttonText,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPlay) "PLAY (LOCKED)" else "EDIT MODE",
                        color = buttonText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ConnectionBadge(
    status: ConnectionStatus,
    connectedName: String?,
    usbCount: Int,
    onClick: () -> Unit
) {
    val (color, icon, text) = when (status) {
        ConnectionStatus.CONNECTED_BLUETOOTH -> Triple(
            NeonGreen,
            Icons.Default.BluetoothConnected,
            connectedName?.take(10) ?: "BT Connected"
        )
        ConnectionStatus.CONNECTED_USB -> Triple(
            NeonGreen,
            Icons.Default.Usb,
            "USB ($usbCount)"
        )
        ConnectionStatus.CONNECTING -> Triple(
            NeonAmber,
            Icons.Default.Bluetooth,
            "Connecting..."
        )
        ConnectionStatus.DISCONNECTED -> Triple(
            NeonRed,
            Icons.Default.Bluetooth,
            "Offline"
        )
    }

    Row(
        modifier = Modifier
            .height(30.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(15.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            imageVector = icon,
            contentDescription = "Connection",
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
