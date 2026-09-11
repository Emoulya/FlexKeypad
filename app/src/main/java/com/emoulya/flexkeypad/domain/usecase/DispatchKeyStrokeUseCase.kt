package com.emoulya.flexkeypad.domain.usecase

import com.emoulya.flexkeypad.domain.model.KeypadButton
import com.emoulya.flexkeypad.domain.repository.HidController
import java.util.concurrent.ConcurrentHashMap

class DispatchKeyStrokeUseCase(
    private val hidController: HidController
) {
    // Track pressed buttons to guarantee release even during rapid gestures or cancellations
    private val activePressedButtons = ConcurrentHashMap<String, KeypadButton>()

    fun onButtonPressed(button: KeypadButton) {
        if (button.hidKeyCode <= 0 && button.modifiers.isEmpty()) return

        activePressedButtons[button.id] = button
        hidController.sendKeyDown(button.hidKeyCode, button.modifiers)
    }

    fun onButtonReleased(button: KeypadButton) {
        if (activePressedButtons.remove(button.id) != null) {
            hidController.sendKeyUp(button.hidKeyCode)
        }
    }

    fun onButtonReleasedById(buttonId: String) {
        val button = activePressedButtons.remove(buttonId)
        if (button != null) {
            hidController.sendKeyUp(button.hidKeyCode)
        }
    }

    fun releaseAll() {
        activePressedButtons.clear()
        hidController.releaseAllKeys()
    }
}
