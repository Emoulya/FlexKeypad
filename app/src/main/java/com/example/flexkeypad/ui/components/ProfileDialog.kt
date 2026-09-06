package com.example.flexkeypad.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.flexkeypad.domain.model.KeypadProfile
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
fun ProfileDialog(
    profiles: List<KeypadProfile>,
    activeProfileId: String,
    onSelectProfile: (String) -> Unit,
    onCreateProfile: (String) -> Unit,
    onDeleteProfile: (String) -> Unit,
    onExportProfile: () -> String,
    onImportProfile: (String, (Boolean, String) -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    var newProfileName by remember { mutableStateOf("") }
    var importJsonText by remember { mutableStateOf("") }
    var isImportingMode by remember { mutableStateOf(false) }
    var exportResultJson by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current



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
                        text = "Profiles Management",
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

                Spacer(modifier = Modifier.height(10.dp))

                statusMessage?.let { msg ->
                    Text(
                        text = msg,
                        color = NeonGreen,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                if (isImportingMode) {
                    // Import JSON View
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Paste Layout JSON below to import:",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = importJsonText,
                            onValueChange = { importJsonText = it },
                            placeholder = { Text("{ ... }", color = TextMuted) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
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
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { isImportingMode = false },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AmoledSurfaceVariant,
                                    contentColor = TextMuted
                                )
                            ) {
                                Text("Cancel", fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    onImportProfile(importJsonText) { success, msg ->
                                        statusMessage = msg
                                        if (success) {
                                            isImportingMode = false
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonCyan,
                                    contentColor = Color.Black
                                )
                            ) {
                                Text("Import Layout", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else if (exportResultJson != null) {
                    // Export JSON View
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Exported Profile JSON:",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = exportResultJson ?: "",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
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
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { exportResultJson = null },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AmoledSurfaceVariant,
                                    contentColor = TextMuted
                                )
                            ) {
                                Text("Back", fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    cm?.setPrimaryClip(ClipData.newPlainText("FlexKeypad Profile", exportResultJson ?: ""))
                                    statusMessage = "JSON copied to clipboard!"

                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonCyan,
                                    contentColor = Color.Black
                                )
                            ) {
                                Icon(Icons.Default.ContentCopy, "Copy", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy to Clipboard", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // Profiles List & Create
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(profiles) { profile ->
                            val isActive = profile.profileId == activeProfileId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isActive) NeonCyan.copy(alpha = 0.12f) else AmoledSurfaceVariant)
                                    .border(
                                        1.dp,
                                        if (isActive) NeonCyan else AmoledBorder,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onSelectProfile(profile.profileId) }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isActive) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Active",
                                            tint = NeonCyan,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Column {
                                        Text(
                                            text = profile.profileName,
                                            color = if (isActive) NeonCyan else TextPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "${profile.buttons.size} buttons",
                                            color = TextMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                if (profiles.size > 1) {
                                    IconButton(
                                        onClick = { onDeleteProfile(profile.profileId) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Profile",
                                            tint = NeonRed.copy(alpha = 0.8f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Create New Profile
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newProfileName,
                            onValueChange = { newProfileName = it },
                            placeholder = { Text("New Profile Name", color = TextMuted, fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
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
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newProfileName.isNotBlank()) {
                                    onCreateProfile(newProfileName.trim())
                                    newProfileName = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonCyan.copy(alpha = 0.2f),
                                contentColor = NeonCyan
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, "Add", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Create", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Export / Import buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = {
                                exportResultJson = onExportProfile()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AmoledSurfaceVariant,
                                contentColor = TextSecondary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Upload, "Export", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export JSON", fontSize = 11.sp)
                        }

                        Button(
                            onClick = { isImportingMode = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AmoledSurfaceVariant,
                                contentColor = TextSecondary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Download, "Import", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Import JSON", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
