package com.example.flexkeypad

import com.example.flexkeypad.domain.model.KeypadButton
import com.example.flexkeypad.domain.model.KeypadProfile
import com.example.flexkeypad.domain.model.ModifierKey
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KeypadSerializationTest {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    @Test
    fun testSerializationRoundTrip() {
        val button = KeypadButton(
            id = "btn_undo_01",
            label = "UNDO",
            positionX = 48.0f,
            positionY = 120.0f,
            width = 160.0f,
            height = 120.0f,
            backgroundColor = "#1E293B",
            textColor = "#FFFFFF",
            hidKeyCode = 29,
            modifiers = listOf(ModifierKey.LEFT_CTRL, ModifierKey.LEFT_ALT),
            hapticEnabled = true
        )

        val profile = KeypadProfile(
            profileId = "prof_photoshop_01",
            profileName = "Photoshop Shortcuts",
            buttons = listOf(button)
        )

        val serialized = json.encodeToString(profile)

        // Verify serial name matching PRD
        assertTrue(serialized.contains("\"MODIFIER_LEFT_CTRL\""))
        assertTrue(serialized.contains("\"MODIFIER_LEFT_ALT\""))

        val deserialized = json.decodeFromString<KeypadProfile>(serialized)
        assertEquals(profile.profileId, deserialized.profileId)
        assertEquals(profile.profileName, deserialized.profileName)
        assertEquals(1, deserialized.buttons.size)

        val desButton = deserialized.buttons[0]
        assertEquals(button.id, desButton.id)
        assertEquals(button.label, desButton.label)
        assertEquals(button.positionX, desButton.positionX)
        assertEquals(button.positionY, desButton.positionY)
        assertEquals(button.width, desButton.width)
        assertEquals(button.height, desButton.height)
        assertEquals(button.hidKeyCode, desButton.hidKeyCode)
        assertEquals(button.modifiers, desButton.modifiers)
        assertEquals(button.hapticEnabled, desButton.hapticEnabled)
    }
}
