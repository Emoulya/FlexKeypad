package com.example.flexkeypad.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.flexkeypad.data.feedback.HapticFeedbackHelper
import com.example.flexkeypad.data.hid.BluetoothHidController
import com.example.flexkeypad.data.hid.CompositeHidController
import com.example.flexkeypad.data.usb.UsbBridgeController
import com.example.flexkeypad.domain.model.CanvasMode
import com.example.flexkeypad.domain.model.KeypadButton
import com.example.flexkeypad.domain.model.KeypadProfile
import com.example.flexkeypad.domain.usecase.DispatchKeyStrokeUseCase
import com.example.flexkeypad.domain.usecase.ManageProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.round

class KeypadViewModel(
    private val manageProfileUseCase: ManageProfileUseCase,
    private val dispatchKeyStrokeUseCase: DispatchKeyStrokeUseCase,
    private val compositeHidController: CompositeHidController,
    private val hapticFeedbackHelper: HapticFeedbackHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(KeypadUiState())
    val uiState: StateFlow<KeypadUiState> = _uiState.asStateFlow()

    init {
        var isInitialProfileLoad = true
        viewModelScope.launch {
            manageProfileUseCase.getActiveProfile().collect { profile ->
                _uiState.update { current ->
                    val nextMode = if (isInitialProfileLoad && profile.buttons.isNotEmpty()) {
                        CanvasMode.PLAY
                    } else current.canvasMode
                    current.copy(activeProfile = profile, canvasMode = nextMode)
                }
                isInitialProfileLoad = false
            }
        }

        viewModelScope.launch {
            manageProfileUseCase.getProfiles().collect { list ->
                _uiState.update { it.copy(profiles = list) }
            }
        }

        viewModelScope.launch {
            compositeHidController.connectionStatus.collect { status ->
                _uiState.update { it.copy(connectionStatus = status) }
            }
        }

        viewModelScope.launch {
            compositeHidController.bluetoothController.connectedDeviceName.collect { name ->
                _uiState.update { it.copy(connectedDeviceName = name) }
            }
        }

        viewModelScope.launch {
            var prevCount = 0
            compositeHidController.usbController.connectedClientsCount.collect { count ->
                _uiState.update { it.copy(usbClientsCount = count) }
                if (count > prevCount && count > 0) {
                    hapticFeedbackHelper.performSuccess()
                }
                prevCount = count
            }
        }
    }

    fun toggleCanvasMode() {
        val newMode = if (_uiState.value.canvasMode == CanvasMode.EDIT) CanvasMode.PLAY else CanvasMode.EDIT
        if (newMode == CanvasMode.EDIT) {
            dispatchKeyStrokeUseCase.releaseAll()
            _uiState.update { it.copy(canvasMode = newMode, pressedButtonIds = emptySet()) }
        } else {
            _uiState.update { it.copy(canvasMode = newMode, selectedButtonId = null, isButtonEditorOpen = false) }
        }
    }

    fun toggleSnapToGrid() {
        _uiState.update { it.copy(isSnapToGrid = !it.isSnapToGrid) }
    }

    fun toggleFullScreen() {
        _uiState.update { it.copy(isFullScreen = !it.isFullScreen) }
    }

    fun selectButton(buttonId: String?) {
        if (_uiState.value.canvasMode == CanvasMode.EDIT) {
            _uiState.update { it.copy(selectedButtonId = buttonId) }
        }
    }

    fun addNewButton() {
        viewModelScope.launch {
            val profile = _uiState.value.activeProfile
            // Offset position below the floating toolbar (toolbar is ~56dp high)
            val count = profile.buttons.size
            val posX = 40f + (count % 4) * 30f
            val posY = 90f + (count % 3) * 25f

            manageProfileUseCase.addButton(
                currentProfile = profile,
                label = "BTN ${count + 1}",
                posX = posX,
                posY = posY,
                width = 140f,
                height = 100f
            )
        }
    }

    /**
     * Fast in-memory update for 60/120fps smooth drag without disk I/O bottleneck.
     */
    fun updateButtonPositionLive(buttonId: String, newX: Float, newY: Float) {
        val currentProfile = _uiState.value.activeProfile
        val updatedButtons = currentProfile.buttons.map { button ->
            if (button.id == buttonId) {
                button.copy(positionX = newX, positionY = newY)
            } else {
                button
            }
        }
        _uiState.update { it.copy(activeProfile = currentProfile.copy(buttons = updatedButtons)) }
    }

    /**
     * Persists final snapped position to disk repository when drag finishes.
     */
    fun commitButtonPosition(buttonId: String, finalX: Float, finalY: Float) {
        val currentProfile = _uiState.value.activeProfile
        val button = currentProfile.buttons.find { it.id == buttonId } ?: return

        val snappedX = if (_uiState.value.isSnapToGrid) {
            round(finalX / _uiState.value.gridSize) * _uiState.value.gridSize
        } else {
            finalX
        }.coerceAtLeast(0f)

        val snappedY = if (_uiState.value.isSnapToGrid) {
            round(finalY / _uiState.value.gridSize) * _uiState.value.gridSize
        } else {
            finalY
        }.coerceAtLeast(0f)

        val updated = button.copy(positionX = snappedX, positionY = snappedY)
        viewModelScope.launch {
            manageProfileUseCase.updateButton(currentProfile, updated)
        }
    }

    /**
     * Fast in-memory update for smooth resizing without disk I/O bottleneck.
     */
    fun updateButtonSizeLive(buttonId: String, newWidth: Float, newHeight: Float) {
        val currentProfile = _uiState.value.activeProfile
        val updatedButtons = currentProfile.buttons.map { button ->
            if (button.id == buttonId) {
                button.copy(
                    width = newWidth.coerceIn(60f, 800f),
                    height = newHeight.coerceIn(50f, 600f)
                )
            } else {
                button
            }
        }
        _uiState.update { it.copy(activeProfile = currentProfile.copy(buttons = updatedButtons)) }
    }

    /**
     * Persists final resized dimension to disk repository when resizing finishes.
     */
    fun commitButtonSize(buttonId: String, finalWidth: Float, finalHeight: Float) {
        val currentProfile = _uiState.value.activeProfile
        val button = currentProfile.buttons.find { it.id == buttonId } ?: return

        val snappedW = (if (_uiState.value.isSnapToGrid) {
            round(finalWidth / _uiState.value.gridSize) * _uiState.value.gridSize
        } else {
            finalWidth
        }).coerceIn(60f, 800f)

        val snappedH = (if (_uiState.value.isSnapToGrid) {
            round(finalHeight / _uiState.value.gridSize) * _uiState.value.gridSize
        } else {
            finalHeight
        }).coerceIn(50f, 600f)

        val updated = button.copy(width = snappedW, height = snappedH)
        viewModelScope.launch {
            manageProfileUseCase.updateButton(currentProfile, updated)
        }
    }


    fun saveButtonDetails(updatedButton: KeypadButton) {
        viewModelScope.launch {
            manageProfileUseCase.updateButton(_uiState.value.activeProfile, updatedButton)
            _uiState.update { it.copy(isButtonEditorOpen = false, editingButton = null) }
        }
    }

    fun deleteButton(buttonId: String) {
        viewModelScope.launch {
            manageProfileUseCase.deleteButton(_uiState.value.activeProfile, buttonId)
            _uiState.update {
                it.copy(
                    selectedButtonId = null,
                    isButtonEditorOpen = false,
                    editingButton = null
                )
            }
        }
    }

    // Play Mode Multi-Touch Handling
    fun onButtonPressed(button: KeypadButton) {
        if (_uiState.value.canvasMode != CanvasMode.PLAY) return

        _uiState.update { it.copy(pressedButtonIds = it.pressedButtonIds + button.id) }
        if (button.hapticEnabled) {
            hapticFeedbackHelper.performKeyClick()
        }
        dispatchKeyStrokeUseCase.onButtonPressed(button)
    }

    fun onButtonReleased(button: KeypadButton) {
        _uiState.update { it.copy(pressedButtonIds = it.pressedButtonIds - button.id) }
        dispatchKeyStrokeUseCase.onButtonReleased(button)
    }

    fun releaseAllPressedButtons() {
        _uiState.update { it.copy(pressedButtonIds = emptySet()) }
        dispatchKeyStrokeUseCase.releaseAll()
    }

    // Dialogs & Sheets
    fun openButtonEditor(button: KeypadButton) {
        _uiState.update { it.copy(isButtonEditorOpen = true, editingButton = button, selectedButtonId = button.id) }
    }

    fun closeButtonEditor() {
        _uiState.update { it.copy(isButtonEditorOpen = false, editingButton = null) }
    }

    fun openProfileDialog() {
        _uiState.update { it.copy(isProfileDialogOpen = true) }
    }

    fun closeProfileDialog() {
        _uiState.update { it.copy(isProfileDialogOpen = false) }
    }

    fun openConnectionDialog() {
        _uiState.update { it.copy(isConnectionDialogOpen = true) }
    }

    fun closeConnectionDialog() {
        _uiState.update { it.copy(isConnectionDialogOpen = false) }
    }

    fun clearInfoMessage() {
        _uiState.update { it.copy(infoMessage = null) }
    }

    // Profiles
    fun selectProfile(profileId: String) {
        viewModelScope.launch {
            manageProfileUseCase.selectProfile(profileId)
            _uiState.update { it.copy(selectedButtonId = null, isProfileDialogOpen = false) }
        }
    }

    fun createNewProfile(name: String) {
        viewModelScope.launch {
            manageProfileUseCase.createProfile(name)
            _uiState.update { it.copy(selectedButtonId = null, isProfileDialogOpen = false) }
        }
    }

    fun deleteProfile(profileId: String) {
        viewModelScope.launch {
            manageProfileUseCase.deleteProfile(profileId)
        }
    }

    fun exportProfileJson(): String {
        var jsonStr = ""
        viewModelScope.launch {
            jsonStr = manageProfileUseCase.exportProfile(_uiState.value.activeProfile)
        }
        return jsonStr
    }

    fun importProfileJson(json: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = manageProfileUseCase.importProfile(json)
            if (result.isSuccess) {
                onComplete(true, "Profile '${result.getOrNull()?.profileName}' successfully imported!")
            } else {
                onComplete(false, "Failed to import profile: Invalid JSON format.")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        dispatchKeyStrokeUseCase.releaseAll()
    }

    class Factory(
        private val manageProfileUseCase: ManageProfileUseCase,
        private val dispatchKeyStrokeUseCase: DispatchKeyStrokeUseCase,
        private val compositeHidController: CompositeHidController,
        private val hapticFeedbackHelper: HapticFeedbackHelper
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return KeypadViewModel(
                manageProfileUseCase,
                dispatchKeyStrokeUseCase,
                compositeHidController,
                hapticFeedbackHelper
            ) as T
        }
    }
}
