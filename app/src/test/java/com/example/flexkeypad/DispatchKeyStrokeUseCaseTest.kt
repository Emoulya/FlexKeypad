package com.example.flexkeypad

import com.example.flexkeypad.domain.model.ConnectionStatus
import com.example.flexkeypad.domain.model.HidKeyCodes
import com.example.flexkeypad.domain.model.KeypadButton
import com.example.flexkeypad.domain.model.ModifierKey
import com.example.flexkeypad.domain.repository.HidController
import com.example.flexkeypad.domain.usecase.DispatchKeyStrokeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DispatchKeyStrokeUseCaseTest {

    private class FakeHidController : HidController {
        override val connectionStatus: StateFlow<ConnectionStatus> = MutableStateFlow(ConnectionStatus.CONNECTED_BLUETOOTH)

        val keyDownEvents = mutableListOf<Pair<Int, List<ModifierKey>>>()
        val keyUpEvents = mutableListOf<Int>()
        var releaseAllCalled = false

        override fun sendKeyDown(keyCode: Int, modifiers: List<ModifierKey>) {
            keyDownEvents.add(keyCode to modifiers)
        }

        override fun sendKeyUp(keyCode: Int) {
            keyUpEvents.add(keyCode)
        }

        override fun releaseAllKeys() {
            releaseAllCalled = true
        }
    }

    private lateinit var fakeController: FakeHidController
    private lateinit var useCase: DispatchKeyStrokeUseCase

    @Before
    fun setUp() {
        fakeController = FakeHidController()
        useCase = DispatchKeyStrokeUseCase(fakeController)
    }

    @Test
    fun testOnButtonPressed_dispatchesKeyDown() {
        val button = KeypadButton(
            id = "btn_1",
            label = "COPY",
            positionX = 10f,
            positionY = 10f,
            hidKeyCode = HidKeyCodes.KEY_C,
            modifiers = listOf(ModifierKey.LEFT_CTRL)
        )

        useCase.onButtonPressed(button)

        assertEquals(1, fakeController.keyDownEvents.size)
        assertEquals(HidKeyCodes.KEY_C, fakeController.keyDownEvents[0].first)
        assertEquals(listOf(ModifierKey.LEFT_CTRL), fakeController.keyDownEvents[0].second)
    }

    @Test
    fun testOnButtonReleased_dispatchesKeyUp() {
        val button = KeypadButton(
            id = "btn_1",
            label = "COPY",
            positionX = 10f,
            positionY = 10f,
            hidKeyCode = HidKeyCodes.KEY_C
        )

        useCase.onButtonPressed(button)
        useCase.onButtonReleased(button)

        assertEquals(1, fakeController.keyUpEvents.size)
        assertEquals(HidKeyCodes.KEY_C, fakeController.keyUpEvents[0])
    }

    @Test
    fun testReleaseAll_invokesControllerReleaseAll() {
        useCase.releaseAll()
        assertTrue(fakeController.releaseAllCalled)
    }
}
