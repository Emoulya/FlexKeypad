package com.example.flexkeypad

import com.example.flexkeypad.data.hid.HidReportDescriptor
import com.example.flexkeypad.domain.model.HidKeyCodes
import com.example.flexkeypad.domain.model.ModifierKey
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class HidReportDescriptorTest {

    @Test
    fun testBuildKeyboardReport_singleKey() {
        val report = HidReportDescriptor.buildKeyboardReport(
            modifiers = emptyList(),
            keyCodes = listOf(HidKeyCodes.KEY_A)
        )

        assertEquals(8, report.size)
        assertEquals(0.toByte(), report[0]) // No modifiers
        assertEquals(0.toByte(), report[1]) // Reserved
        assertEquals(HidKeyCodes.KEY_A.toByte(), report[2]) // Key A (0x04)
        for (i in 3 until 8) {
            assertEquals(0.toByte(), report[i])
        }
    }

    @Test
    fun testBuildKeyboardReport_withModifiers() {
        // Ctrl + Alt + Delete
        val report = HidReportDescriptor.buildKeyboardReport(
            modifiers = listOf(ModifierKey.LEFT_CTRL, ModifierKey.LEFT_ALT),
            keyCodes = listOf(HidKeyCodes.KEY_DELETE)
        )

        val expectedMod = (ModifierKey.LEFT_CTRL.bitmask.toInt() or ModifierKey.LEFT_ALT.bitmask.toInt()).toByte()
        assertEquals(expectedMod, report[0])
        assertEquals(0.toByte(), report[1])
        assertEquals(HidKeyCodes.KEY_DELETE.toByte(), report[2])
    }

    @Test
    fun testBuildReleaseReport() {
        val releaseReport = HidReportDescriptor.buildReleaseReport()
        val expected = ByteArray(8)

        assertArrayEquals(expected, releaseReport)
    }

    @Test
    fun testBuildKeyboardReport_capsAtSixKeys() {
        // 7 keys passed in, report should only contain the first 6
        val keys = listOf(
            HidKeyCodes.KEY_1,
            HidKeyCodes.KEY_2,
            HidKeyCodes.KEY_3,
            HidKeyCodes.KEY_4,
            HidKeyCodes.KEY_5,
            HidKeyCodes.KEY_6,
            HidKeyCodes.KEY_7
        )
        val report = HidReportDescriptor.buildKeyboardReport(keyCodes = keys)

        assertEquals(HidKeyCodes.KEY_1.toByte(), report[2])
        assertEquals(HidKeyCodes.KEY_2.toByte(), report[3])
        assertEquals(HidKeyCodes.KEY_3.toByte(), report[4])
        assertEquals(HidKeyCodes.KEY_4.toByte(), report[5])
        assertEquals(HidKeyCodes.KEY_5.toByte(), report[6])
        assertEquals(HidKeyCodes.KEY_6.toByte(), report[7])
    }
}
