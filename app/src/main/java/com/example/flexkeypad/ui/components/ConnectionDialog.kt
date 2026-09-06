package com.example.flexkeypad.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.flexkeypad.domain.model.ConnectionStatus
import com.example.flexkeypad.ui.theme.AmoledBorder
import com.example.flexkeypad.ui.theme.AmoledSurface
import com.example.flexkeypad.ui.theme.AmoledSurfaceVariant
import com.example.flexkeypad.ui.theme.NeonCyan
import com.example.flexkeypad.ui.theme.NeonGreen
import com.example.flexkeypad.ui.theme.NeonRed
import com.example.flexkeypad.ui.theme.TextMuted
import com.example.flexkeypad.ui.theme.TextPrimary
import com.example.flexkeypad.ui.theme.TextSecondary

@Composable
fun ConnectionDialog(
    status: ConnectionStatus,
    connectedDeviceName: String?,
    usbClientsCount: Int,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, AmoledBorder, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = AmoledSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Connectivity & Pairing",
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

                Spacer(modifier = Modifier.height(14.dp))

                // 1. Bluetooth HID Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AmoledSurfaceVariant),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Bluetooth,
                                    contentDescription = "Bluetooth",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Bluetooth HID (Driverless)",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            val isBtConnected = status == ConnectionStatus.CONNECTED_BLUETOOTH
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isBtConnected) NeonGreen else NeonRed)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isBtConnected) (connectedDeviceName ?: "Connected") else "Disconnected",
                                    color = if (isBtConnected) NeonGreen else TextMuted,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "How to connect via Bluetooth:\n" +
                                    "1. On your PC/Laptop, open Bluetooth settings.\n" +
                                    "2. Click 'Add Bluetooth device' and search for 'FlexKeypad'.\n" +
                                    "3. Pair and connect. Your PC will recognize it as a standard wireless keyboard instantly without any additional drivers.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. USB Wired Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AmoledSurfaceVariant),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Usb,
                                    contentDescription = "USB",
                                    tint = NeonGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "USB Cable Bridge (<= 5ms Latency)",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            val isUsbConnected = status == ConnectionStatus.CONNECTED_USB || usbClientsCount > 0
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isUsbConnected) NeonGreen else TextMuted)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isUsbConnected) "$usbClientsCount Host(s)" else "Listening :8899",
                                    color = if (isUsbConnected) NeonGreen else TextMuted,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "How to connect via USB Cable:\n" +
                                    "1. Connect Android phone/tablet to PC via USB cable.\n" +
                                    "2. Forward socket port via ADB command:\n",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black)
                                .border(1.dp, AmoledBorder, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "adb forward tcp:8899 tcp:8899",
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "3. Run the companion bridge on your PC:\n" +
                                    "   - Windows: Double-click tools/companion-bridge/start-bridge.bat\n" +
                                    "   - Node.js: node tools/companion-bridge/bridge.js\n" +
                                    "Enjoy lightning fast inputs with near-zero latency!",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )

                    }
                }
            }
        }
    }
}
