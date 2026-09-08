package com.example.flexkeypad.util

import androidx.compose.ui.graphics.Color

/**
 * Utility functions for parsing and manipulating colors in Compose.
 */
object ColorUtils {

    /**
     * Parses a hex color string into a Compose [Color].
     * Supports formats:
     * - "#RRGGBB" or "RRGGBB" (alpha defaults to 0xFF)
     * - "#AARRGGBB" or "AARRGGBB"
     * - "#RGB" or "RGB" (expanded to #RRGGBB)
     *
     * Returns [defaultColor] if string is invalid or cannot be parsed.
     */
    fun parseColorHex(hex: String, defaultColor: Color = Color.Unspecified): Color {
        val clean = hex.trim().removePrefix("#")
        if (clean.isEmpty()) return defaultColor

        return try {
            when (clean.length) {
                3 -> {
                    // Expand RGB to RRGGBB
                    val r = clean[0]
                    val g = clean[1]
                    val b = clean[2]
                    val expanded = "$r$r$g$g$b$b"
                    val colorInt = expanded.toLong(16)
                    Color(colorInt or 0x00000000FF000000L)
                }
                6 -> {
                    val colorInt = clean.toLong(16)
                    Color(colorInt or 0x00000000FF000000L)
                }
                8 -> {
                    val colorInt = clean.toLong(16)
                    Color(colorInt)
                }
                else -> defaultColor
            }
        } catch (_: Exception) {
            defaultColor
        }
    }

    /**
     * Converts a Compose [Color] to a hex string representation.
     */
    fun Color.toHex(includeAlpha: Boolean = false): String {
        val a = (alpha * 255).toInt()
        val r = (red * 255).toInt()
        val g = (green * 255).toInt()
        val b = (blue * 255).toInt()
        return if (includeAlpha) {
            String.format("#%02X%02X%02X%02X", a, r, g, b)
        } else {
            String.format("#%02X%02X%02X", r, g, b)
        }
    }
}

/**
 * Convenient extension function to parse hex string into Compose [Color].
 */
fun String.toComposeColor(defaultColor: Color = Color.Unspecified): Color {
    return ColorUtils.parseColorHex(this, defaultColor)
}

/**
 * Top-level function for backward compatibility and concise access.
 */
fun parseColorHex(hex: String, defaultColor: Color = Color.Unspecified): Color {
    return ColorUtils.parseColorHex(hex, defaultColor)
}
