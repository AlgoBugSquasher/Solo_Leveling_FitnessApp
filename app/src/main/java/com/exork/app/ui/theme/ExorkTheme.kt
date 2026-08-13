package com.exork.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ExorkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    secondary = ManaPurple,
    tertiary = SuccessGreen,
    background = ObsidianVoid,
    surface = LeatherDeep,
    onPrimary = ObsidianVoid,
    onSecondary = ObsidianVoid,
    onTertiary = ObsidianVoid,
    onBackground = androidx.compose.ui.graphics.Color.White,
    onSurface = androidx.compose.ui.graphics.Color.White,
    outline = ChromeSilver.copy(alpha = 0.2f)
)

@Composable
fun ExorkTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = ObsidianVoid.toArgb()
            window.navigationBarColor = ObsidianVoid.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = ExorkColorScheme,
        typography = ExorkTypography,
        content = content
    )
}
