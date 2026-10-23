package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SlickDriftColorScheme = darkColorScheme(
    primary = CyanNeon,
    onPrimary = OceanAbyss,
    primaryContainer = CyanNeonDark,
    onPrimaryContainer = CyanNeon,
    secondary = HydroBlueLight,
    onSecondary = OceanAbyss,
    secondaryContainer = OceanCard,
    onSecondaryContainer = HydroBlueLight,
    tertiary = PetroleumAmber,
    onTertiary = OceanAbyss,
    tertiaryContainer = PetroleumDark,
    onTertiaryContainer = PetroleumAmber,
    background = OceanAbyss,
    onBackground = TextPrimary,
    surface = OceanSurface,
    onSurface = TextPrimary,
    surfaceVariant = OceanCard,
    onSurfaceVariant = TextSecondary,
    outline = OceanCardBorder,
    error = DistressRed,
    onError = TextPrimary
)

@Composable
fun SlickDriftTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = SlickDriftColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
