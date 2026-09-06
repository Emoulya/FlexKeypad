package com.example.flexkeypad.data.hid

import com.example.flexkeypad.domain.model.ModifierKey

object HidReportDescriptor {
    const val KEYBOARD_REPORT_ID = 1

    /**
     * Standard USB HID Keyboard Report Descriptor.
     * Complies with USB HID 1.11 Specification.
     */
    val KEYBOARD_DESCRIPTOR = byteArrayOf(
        0x05.toByte(), 0x01.toByte(), // USAGE_PAGE (Generic Desktop)
        0x09.toByte(), 0x06.toByte(), // USAGE (Keyboard)
        0xA1.toByte(), 0x01.toByte(), // COLLECTION (Application)
        0x85.toByte(), KEYBOARD_REPORT_ID.toByte(), // REPORT_ID (1)
        // Modifiers (8 bits)
        0x05.toByte(), 0x07.toByte(), //   USAGE_PAGE (Keyboard/Keypad)
        0x19.toByte(), 0xE0.toByte(), //   USAGE_MINIMUM (Keyboard Left Control)
        0x29.toByte(), 0xE7.toByte(), //   USAGE_MAXIMUM (Keyboard Right GUI)
        0x15.toByte(), 0x00.toByte(), //   LOGICAL_MINIMUM (0)
        0x25.toByte(), 0x01.toByte(), //   LOGICAL_MAXIMUM (1)
        0x75.toByte(), 0x01.toByte(), //   REPORT_SIZE (1)
        0x95.toByte(), 0x08.toByte(), //   REPORT_COUNT (8)
        0x81.toByte(), 0x02.toByte(), //   INPUT (Data,Var,Abs)
        // Reserved byte (8 bits)
        0x95.toByte(), 0x01.toByte(), //   REPORT_COUNT (1)
        0x75.toByte(), 0x08.toByte(), //   REPORT_SIZE (8)
        0x81.toByte(), 0x01.toByte(), //   INPUT (Cnst,Ary,Abs)
        // LEDs (5 bits indicators + 3 bits padding)
        0x95.toByte(), 0x05.toByte(), //   REPORT_COUNT (5)
        0x75.toByte(), 0x01.toByte(), //   REPORT_SIZE (1)
        0x05.toByte(), 0x08.toByte(), //   USAGE_PAGE (LEDs)
        0x19.toByte(), 0x01.toByte(), //   USAGE_MINIMUM (Num Lock)
        0x29.toByte(), 0x05.toByte(), //   USAGE_MAXIMUM (Kana)
        0x91.toByte(), 0x02.toByte(), //   OUTPUT (Data,Var,Abs)
        0x95.toByte(), 0x01.toByte(), //   REPORT_COUNT (1)
        0x75.toByte(), 0x03.toByte(), //   REPORT_SIZE (3)
        0x91.toByte(), 0x01.toByte(), //   OUTPUT (Cnst,Ary,Abs)
        // 6-Key Rollover Array (6 bytes)
        0x95.toByte(), 0x06.toByte(), //   REPORT_COUNT (6)
        0x75.toByte(), 0x08.toByte(), //   REPORT_SIZE (8)
        0x15.toByte(), 0x00.toByte(), //   LOGICAL_MINIMUM (0)
        0x25.toByte(), 0x65.toByte(), //   LOGICAL_MAXIMUM (101)
        0x05.toByte(), 0x07.toByte(), //   USAGE_PAGE (Keyboard/Keypad)
        0x19.toByte(), 0x00.toByte(), //   USAGE_MINIMUM (Reserved)
        0x29.toByte(), 0x65.toByte(), //   USAGE_MAXIMUM (Keyboard Application)
        0x81.toByte(), 0x00.toByte(), //   INPUT (Data,Ary,Abs)
        0xC0.toByte()                  // END_COLLECTION
    )

    /**
     * Builds standard 8-byte HID keyboard input report:
     * Byte 0: Modifiers bitmask
     * Byte 1: Reserved (0x00)
     * Bytes 2..7: Up to 6 keycodes
     */
    fun buildKeyboardReport(
        modifiers: List<ModifierKey> = emptyList(),
        keyCodes: List<Int> = emptyList()
    ): ByteArray {
        val report = ByteArray(8)

        // Calculate modifier byte
        var modByte: Byte = 0
        for (mod in modifiers) {
            modByte = (modByte.toInt() or mod.bitmask.toInt()).toByte()
        }
        report[0] = modByte
        report[1] = 0 // Reserved

        // Fill up to 6 keycodes
        val count = minOf(keyCodes.size, 6)
        for (i in 0 until count) {
            report[2 + i] = keyCodes[i].toByte()
        }

        return report
    }

    /**
     * Builds empty release report (all zeroes).
     */
    fun buildReleaseReport(): ByteArray {
        return ByteArray(8)
    }
}
