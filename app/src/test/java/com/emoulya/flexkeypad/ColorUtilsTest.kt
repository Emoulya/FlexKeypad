package com.emoulya.flexkeypad

import androidx.compose.ui.graphics.Color
import com.emoulya.flexkeypad.util.ColorUtils
import com.emoulya.flexkeypad.util.ColorUtils.toHex
import com.emoulya.flexkeypad.util.parseColorHex
import com.emoulya.flexkeypad.util.toComposeColor
import org.junit.Assert.assertEquals
import org.junit.Test

class ColorUtilsTest {

    @Test
    fun parseColorHex_sixDigitWithHash_parsesCorrectly() {
        val color = parseColorHex("#FF0000")
        assertEquals(1f, color.red, 0.01f)
        assertEquals(0f, color.green, 0.01f)
        assertEquals(0f, color.blue, 0.01f)
        assertEquals(1f, color.alpha, 0.01f)
    }

    @Test
    fun parseColorHex_sixDigitWithoutHash_parsesCorrectly() {
        val color = parseColorHex("00FF00")
        assertEquals(0f, color.red, 0.01f)
        assertEquals(1f, color.green, 0.01f)
        assertEquals(0f, color.blue, 0.01f)
        assertEquals(1f, color.alpha, 0.01f)
    }

    @Test
    fun parseColorHex_eightDigitWithAlpha_parsesCorrectly() {
        // #800000FF: Alpha = 0x80 (~0.5), Blue = 0xFF (1.0)
        val color = parseColorHex("#800000FF")
        assertEquals(0.5f, color.alpha, 0.02f)
        assertEquals(0f, color.red, 0.01f)
        assertEquals(0f, color.green, 0.01f)
        assertEquals(1f, color.blue, 0.01f)
    }

    @Test
    fun parseColorHex_threeDigitShorthand_expandsAndParses() {
        // #F00 -> #FF0000
        val color = parseColorHex("#F00")
        assertEquals(1f, color.red, 0.01f)
        assertEquals(0f, color.green, 0.01f)
        assertEquals(0f, color.blue, 0.01f)
        assertEquals(1f, color.alpha, 0.01f)
    }

    @Test
    fun parseColorHex_invalidInput_returnsDefaultColor() {
        val fallback = Color.DarkGray
        val resultInvalid = parseColorHex("not_a_color", fallback)
        assertEquals(fallback, resultInvalid)

        val resultEmpty = parseColorHex("", fallback)
        assertEquals(fallback, resultEmpty)

        val resultTooShort = parseColorHex("#12", fallback)
        assertEquals(fallback, resultTooShort)
    }

    @Test
    fun toComposeColor_extensionFunction_worksIdentical() {
        val color = "#00FFFF".toComposeColor()
        assertEquals(0f, color.red, 0.01f)
        assertEquals(1f, color.green, 0.01f)
        assertEquals(1f, color.blue, 0.01f)
    }

    @Test
    fun toHex_formatsCorrectly() {
        val red = Color.Red
        assertEquals("#FF0000", red.toHex(includeAlpha = false))
        assertEquals("#FFFF0000", red.toHex(includeAlpha = true))
    }
}
