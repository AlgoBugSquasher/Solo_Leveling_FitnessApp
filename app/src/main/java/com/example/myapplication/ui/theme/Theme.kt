package com.example.myapplication.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ExorkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    secondary = ManaPurple,
    tertiary = SuccessGreen,
    background = MonarchSlate,
    surface = TranslucentSlate,
    onPrimary = MonarchSlate,
    onSecondary = MonarchSlate,
    onTertiary = MonarchSlate,
    onBackground = Color.White,
    onSurface = Color.White,
    outline = ElectricCyan.copy(alpha = 0.2f)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // For eXork, we strictly use the Dark RPG Theme
    MaterialTheme(
        colorScheme = ExorkColorScheme,
        typography = ExorkTypography,
        content = content
    )
}
