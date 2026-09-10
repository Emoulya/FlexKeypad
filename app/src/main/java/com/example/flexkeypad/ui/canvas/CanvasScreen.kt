package com.example.flexkeypad.ui.canvas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.flexkeypad.domain.model.CanvasMode
import com.example.flexkeypad.ui.components.ButtonEditorDialog
import com.example.flexkeypad.ui.components.CanvasTopMenu
import com.example.flexkeypad.ui.components.ConnectionDialog
import com.example.flexkeypad.ui.components.ProfileDialog
import com.example.flexkeypad.ui.theme.AmoledBlack
import com.example.flexkeypad.ui.viewmodel.KeypadViewModel

@Composable
fun CanvasScreen(
    viewModel: KeypadViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.infoMessage) {
        val msg = state.infoMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearInfoMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = if (state.isFullScreen) androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0) else androidx.compose.material3.ScaffoldDefaults.contentWindowInsets,
        containerColor = AmoledBlack,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Color(0xFF1E293B),
                    contentColor = Color.White
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (state.isFullScreen) androidx.compose.foundation.layout.PaddingValues(0.dp) else innerPadding)
                .background(AmoledBlack)
        ) {
            // Main Canvas Area
            if (state.canvasMode == CanvasMode.PLAY) {
                PlayModeCanvas(
                    buttons = state.activeProfile.buttons,
                    pressedButtonIds = state.pressedButtonIds,
                    onButtonPressed = { viewModel.onButtonPressed(it) },
                    onButtonReleased = { viewModel.onButtonReleased(it) },
                    onReleaseAll = { viewModel.releaseAllPressedButtons() },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                EditModeCanvas(
                    buttons = state.activeProfile.buttons,
                    selectedButtonId = state.selectedButtonId,
                    isSnapToGrid = state.isSnapToGrid,
                    gridSize = state.gridSize,
                    onSelectButton = { viewModel.selectButton(it) },
                    onMoveButtonLive = { id, x, y -> viewModel.updateButtonPositionLive(id, x, y) },
                    onCommitMoveButton = { id, x, y -> viewModel.commitButtonPosition(id, x, y) },
                    onResizeButtonLive = { id, w, h -> viewModel.updateButtonSizeLive(id, w, h) },
                    onCommitResizeButton = { id, w, h -> viewModel.commitButtonSize(id, w, h) },
                    onEditButton = { viewModel.openButtonEditor(it) },
                    onDuplicateButton = { viewModel.duplicateButton(it) },
                    onDeleteButton = { viewModel.deleteButton(it) },
                    modifier = Modifier.fillMaxSize()
                )

            }

            // Top Right Expand/Collapse Side Menu
            CanvasTopMenu(
                state = state,
                onToggleMode = { viewModel.toggleCanvasMode() },
                onToggleFullScreen = { viewModel.toggleFullScreen() },
                onToggleHaptic = { viewModel.toggleHaptic() },
                onAddNewButton = { viewModel.addNewButton() },
                onToggleGrid = { viewModel.toggleSnapToGrid() },
                onOpenProfileDialog = { viewModel.openProfileDialog() },
                onOpenConnectionDialog = { viewModel.openConnectionDialog() },
                modifier = Modifier.fillMaxSize()
            )

            // Button Editor Dialog
            if (state.isButtonEditorOpen && state.editingButton != null) {
                ButtonEditorDialog(
                    button = state.editingButton!!,
                    onSave = { viewModel.saveButtonDetails(it) },
                    onDelete = { viewModel.deleteButton(it) },
                    onDismiss = { viewModel.closeButtonEditor() }
                )
            }

            // Profile Dialog
            if (state.isProfileDialogOpen) {
                ProfileDialog(
                    profiles = state.profiles,
                    activeProfileId = state.activeProfile.profileId,
                    onSelectProfile = { viewModel.selectProfile(it) },
                    onCreateProfile = { viewModel.createNewProfile(it) },
                    onDeleteProfile = { viewModel.deleteProfile(it) },
                    onExportProfile = { profileId, callback -> viewModel.exportProfileJson(profileId, callback) },
                    onImportProfile = { json, callback -> viewModel.importProfileJson(json, callback) },
                    onDismiss = { viewModel.closeProfileDialog() }
                )
            }

            // Connection Dialog
            if (state.isConnectionDialogOpen) {
                ConnectionDialog(
                    status = state.connectionStatus,
                    connectedDeviceName = state.connectedDeviceName,
                    usbClientsCount = state.usbClientsCount,
                    onDismiss = { viewModel.closeConnectionDialog() }
                )
            }
        }
    }
}
