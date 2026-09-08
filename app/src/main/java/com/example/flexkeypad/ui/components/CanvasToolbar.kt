package com.example.flexkeypad.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.GridOff
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flexkeypad.domain.model.CanvasMode
import com.example.flexkeypad.domain.model.ConnectionStatus
import com.example.flexkeypad.ui.theme.AmoledBlack
import com.example.flexkeypad.ui.theme.AmoledBorder
import com.example.flexkeypad.ui.theme.AmoledSurface
import com.example.flexkeypad.ui.theme.NeonAmber
import com.example.flexkeypad.ui.theme.NeonCyan
import com.example.flexkeypad.ui.theme.NeonGreen
import com.example.flexkeypad.ui.theme.NeonRed
import com.example.flexkeypad.ui.theme.TextMuted
import com.example.flexkeypad.ui.theme.TextPrimary
import com.example.flexkeypad.ui.viewmodel.KeypadUiState

/**
 * Top-Right Hamburger Menu Drop-down.
 * Replaces the wide floating toolbar with a compact, ultra-clean trigger button.
 */
@Composable
fun CanvasTopMenu(
    state: KeypadUiState,
    onToggleMode: () -> Unit,
    onToggleFullScreen: () -> Unit,
    onToggleHaptic: () -> Unit,
    onAddNewButton: () -> Unit,
    onToggleGrid: () -> Unit,
    onOpenProfileDialog: () -> Unit,
    onOpenConnectionDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val (statusColor, statusIcon, statusText) = when (state.connectionStatus) {
        ConnectionStatus.CONNECTED_BLUETOOTH -> Triple(
            NeonGreen,
            Icons.Default.BluetoothConnected,
            state.connectedDeviceName?.take(14) ?: "Bluetooth"
        )
        ConnectionStatus.CONNECTED_USB -> Triple(
            NeonGreen,
            Icons.Default.Usb,
            "USB (${state.usbClientsCount})"
        )
        ConnectionStatus.CONNECTING -> Triple(
            NeonAmber,
            Icons.Default.Bluetooth,
            "Menghubungkan..."
        )
        ConnectionStatus.DISCONNECTED -> Triple(
            NeonRed,
            Icons.Default.Bluetooth,
            "Offline"
        )
    }

    Box(
        modifier = modifier.padding(top = 16.dp, end = 16.dp)
    ) {
        // Compact Glassmorphic Hamburger Trigger Button
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(AmoledSurface.copy(alpha = 0.92f))
                .border(1.dp, AmoledBorder, RoundedCornerShape(14.dp))
                .clickable { menuExpanded = !menuExpanded }
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Open Menu",
                tint = NeonCyan,
                modifier = Modifier
                    .size(22.dp)
                    .align(Alignment.Center)
            )

            // Dynamic Connection Status Dot Indicator
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = 4.dp)
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(statusColor)
                    .border(1.5.dp, AmoledBlack, CircleShape)
            )
        }

        // Modern Glassmorphic Dropdown Menu
        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            offset = DpOffset(x = 0.dp, y = 8.dp),
            modifier = Modifier
                .width(260.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(AmoledSurface)
                .border(1.dp, AmoledBorder, RoundedCornerShape(18.dp))
                .padding(vertical = 6.dp)
        ) {
            val isPlay = state.canvasMode == CanvasMode.PLAY

            // 1. Canvas Mode Switcher Card
            DropdownMenuItem(
                text = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isPlay) NeonGreen.copy(alpha = 0.15f) else NeonAmber.copy(alpha = 0.15f))
                            .border(
                                1.dp,
                                if (isPlay) NeonGreen.copy(alpha = 0.5f) else NeonAmber.copy(alpha = 0.5f),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isPlay) Icons.Default.Lock else Icons.Default.Edit,
                            contentDescription = "Mode",
                            tint = if (isPlay) NeonGreen else NeonAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isPlay) "PLAY (LOCKED)" else "EDIT MODE",
                                color = if (isPlay) NeonGreen else NeonAmber,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isPlay) "Klik untuk edit tombol" else "Klik untuk kunci & main",
                                color = TextMuted,
                                fontSize = 9.5.sp
                            )
                        }
                    }
                },
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                onClick = {
                    onToggleMode()
                    menuExpanded = false
                },
                colors = MenuDefaults.itemColors(textColor = TextPrimary)
            )

            HorizontalDivider(
                color = AmoledBorder.copy(alpha = 0.4f),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
            )

            // 2. Full Screen Toggle
            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        imageVector = if (state.isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                        contentDescription = "Full Screen",
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Layar Penuh",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (state.isFullScreen) "Sembunyikan status bar" else "Tampilkan status bar",
                            color = TextMuted,
                            fontSize = 9.5.sp
                        )
                    }
                },
                trailingIcon = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (state.isFullScreen) NeonGreen.copy(alpha = 0.2f) else AmoledBorder)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (state.isFullScreen) "ON" else "OFF",
                            color = if (state.isFullScreen) NeonGreen else TextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                modifier = Modifier.height(38.dp),
                onClick = {
                    onToggleFullScreen()
                },
                colors = MenuDefaults.itemColors(textColor = TextPrimary)
            )

            // 3. Haptic Feedback Toggle
            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Vibration,
                        contentDescription = "Haptic Feedback",
                        tint = if (state.isHapticEnabled) NeonCyan else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Getaran Haptik",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (state.isHapticEnabled) "Getar saat menekan tombol" else "Getaran dinonaktifkan",
                            color = TextMuted,
                            fontSize = 9.5.sp
                        )
                    }
                },
                trailingIcon = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (state.isHapticEnabled) NeonGreen.copy(alpha = 0.2f) else AmoledBorder)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (state.isHapticEnabled) "ON" else "OFF",
                            color = if (state.isHapticEnabled) NeonGreen else TextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                modifier = Modifier.height(38.dp),
                onClick = {
                    onToggleHaptic()
                },
                colors = MenuDefaults.itemColors(textColor = TextPrimary)
            )

            // 4. Connection Status Dialog Trigger
            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = "Connection",
                        tint = statusColor,
                        modifier = Modifier.size(18.dp)
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Status Koneksi",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = statusText,
                            color = statusColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Open Connection",
                        tint = TextMuted,
                        modifier = Modifier.size(15.dp)
                    )
                },
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                modifier = Modifier.height(38.dp),
                onClick = {
                    onOpenConnectionDialog()
                    menuExpanded = false
                },
                colors = MenuDefaults.itemColors(textColor = TextPrimary)
            )

            // 4. Keypad Profile Dialog Trigger
            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = "Profile",
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Profil Keypad",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = state.activeProfile.profileName,
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Open Profiles",
                        tint = TextMuted,
                        modifier = Modifier.size(15.dp)
                    )
                },
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                modifier = Modifier.height(38.dp),
                onClick = {
                    onOpenProfileDialog()
                    menuExpanded = false
                },
                colors = MenuDefaults.itemColors(textColor = TextPrimary)
            )

            // 5. Edit Mode Actions (Only shown in Edit Mode)
            if (state.canvasMode == CanvasMode.EDIT) {
                HorizontalDivider(
                    color = AmoledBorder.copy(alpha = 0.4f),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                )

                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Key",
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    text = {
                        Text(
                            text = "Tambah Tombol",
                            color = NeonCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(36.dp),
                    onClick = {
                        onAddNewButton()
                        menuExpanded = false
                    },
                    colors = MenuDefaults.itemColors(textColor = TextPrimary)
                )

                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            imageVector = if (state.isSnapToGrid) Icons.Default.GridOn else Icons.Default.GridOff,
                            contentDescription = "Grid",
                            tint = if (state.isSnapToGrid) NeonCyan else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    text = {
                        Text(
                            text = "Snap to Grid",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    trailingIcon = {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (state.isSnapToGrid) NeonCyan.copy(alpha = 0.2f) else AmoledBorder)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (state.isSnapToGrid) "ON" else "OFF",
                                color = if (state.isSnapToGrid) NeonCyan else TextMuted,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(36.dp),
                    onClick = {
                        onToggleGrid()
                    },
                    colors = MenuDefaults.itemColors(textColor = TextPrimary)
                )
            }
        }
    }
}
