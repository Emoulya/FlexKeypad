package com.example.flexkeypad.ui.viewmodel

import com.example.flexkeypad.domain.model.CanvasMode
import com.example.flexkeypad.domain.model.ConnectionStatus
import com.example.flexkeypad.domain.model.KeypadButton
import com.example.flexkeypad.domain.model.KeypadProfile

data class KeypadUiState(
    val activeProfile: KeypadProfile = KeypadProfile("default", "Default Keypad", emptyList()),
    val profiles: List<KeypadProfile> = emptyList(),
    val canvasMode: CanvasMode = CanvasMode.EDIT,
    val selectedButtonId: String? = null,
    val isSnapToGrid: Boolean = true,
    val gridSize: Float = 20f,
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val connectedDeviceName: String? = null,
    val usbClientsCount: Int = 0,
    val pressedButtonIds: Set<String> = emptySet(),
    val isButtonEditorOpen: Boolean = false,
    val editingButton: KeypadButton? = null,
    val isProfileDialogOpen: Boolean = false,
    val isConnectionDialogOpen: Boolean = false,
    val infoMessage: String? = null
)
