package com.example.flexkeypad.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ModifierKey(val bitmask: Byte) {
    @SerialName("MODIFIER_LEFT_CTRL")
    LEFT_CTRL(0x01.toByte()),

    @SerialName("MODIFIER_LEFT_SHIFT")
    LEFT_SHIFT(0x02.toByte()),

    @SerialName("MODIFIER_LEFT_ALT")
    LEFT_ALT(0x04.toByte()),

    @SerialName("MODIFIER_LEFT_GUI")
    LEFT_GUI(0x08.toByte()),

    @SerialName("MODIFIER_RIGHT_CTRL")
    RIGHT_CTRL(0x10.toByte()),

    @SerialName("MODIFIER_RIGHT_SHIFT")
    RIGHT_SHIFT(0x20.toByte()),

    @SerialName("MODIFIER_RIGHT_ALT")
    RIGHT_ALT(0x40.toByte()),

    @SerialName("MODIFIER_RIGHT_GUI")
    RIGHT_GUI(0x80.toByte())
}

@Serializable
data class KeypadButton(
    val id: String,
    val label: String,
    val positionX: Float,
    val positionY: Float,
    val width: Float = 140f,
    val height: Float = 100f,
    val backgroundColor: String = "#1E293B",
    val textColor: String = "#FFFFFF",
    val hidKeyCode: Int = 0,
    val modifiers: List<ModifierKey> = emptyList(),
    val hapticEnabled: Boolean = true,
    val iconName: String? = null
)

@Serializable
data class KeypadProfile(
    val profileId: String,
    val profileName: String,
    val buttons: List<KeypadButton> = emptyList()
)

enum class CanvasMode {
    EDIT,
    PLAY
}

enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED_BLUETOOTH,
    CONNECTED_USB
}
