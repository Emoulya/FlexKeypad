package com.emoulya.flexkeypad.domain.repository

import com.emoulya.flexkeypad.domain.model.ConnectionStatus
import com.emoulya.flexkeypad.domain.model.ModifierKey
import kotlinx.coroutines.flow.StateFlow

interface HidController {
    val connectionStatus: StateFlow<ConnectionStatus>
    fun sendKeyDown(keyCode: Int, modifiers: List<ModifierKey>)
    fun sendKeyUp(keyCode: Int)
    fun releaseAllKeys()
}
