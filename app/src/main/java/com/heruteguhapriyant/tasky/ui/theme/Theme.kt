package com.heruteguhapriyant.tasky.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = AccentSkyBlue,
    onPrimary = OnAccentSkyBlue,
    primaryContainer = CategoryPillBg,
    onPrimaryContainer = CategoryPillText,
    secondary = FilterActiveGreen,
    onSecondary = OnFilterActiveGreen,
    background = BackgroundCream,
    onBackground = TextPrimary,
    surface = SurfaceCream,
    onSurface = TextPrimary,
    surfaceVariant = BackgroundCream,
    onSurfaceVariant = TextSecondary,
    outline = BorderDark,
    outlineVariant = BorderSubtle
)

private val DarkColorScheme = darkColorScheme(
    primary = AccentSkyBlue,
    onPrimary = OnAccentSkyBlue,
    primaryContainer = PriorityHighBg,
    onPrimaryContainer = PriorityHighText,
    secondary = FilterActiveGreen,
    onSecondary = OnFilterActiveGreen,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceCardDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderDarkTheme,
    outlineVariant = BorderDarkTheme
)

@Composable
fun TaskyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
