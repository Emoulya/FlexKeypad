package com.example.flexkeypad.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AmoledDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color.Black,
    primaryContainer = AmoledSurfaceVariant,
    onPrimaryContainer = NeonCyan,
    secondary = NeonViolet,
    onSecondary = Color.White,
    background = AmoledBlack,
    onBackground = TextPrimary,
    surface = AmoledSurface,
    onSurface = TextPrimary,
    surfaceVariant = AmoledSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = AmoledBorder
)

@Composable
fun FlexKeypadTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = AmoledDarkColorScheme,
        typography = Typography,
        content = content
    )
}