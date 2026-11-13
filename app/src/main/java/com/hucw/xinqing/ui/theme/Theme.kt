package com.hucw.xinqing.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Sage,
    onPrimary = Color.White,
    primaryContainer = SageLight,
    onPrimaryContainer = Ink,
    secondary = Lilac,
    onSecondary = Color.White,
    tertiary = Peach,
    onTertiary = Ink,
    background = Cream,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Sand,
    onSurfaceVariant = InkSoft
)

private val DarkColors = darkColorScheme(
    primary = SageDark,
    onPrimary = Night,
    primaryContainer = NightSurfaceAlt,
    onPrimaryContainer = Cream,
    secondary = LilacDark,
    onSecondary = Night,
    tertiary = PeachDark,
    onTertiary = Night,
    background = Night,
    onBackground = Cream,
    surface = NightSurface,
    onSurface = Cream,
    surfaceVariant = NightSurfaceAlt,
    onSurfaceVariant = Mist
)

@Composable
fun XinqingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
