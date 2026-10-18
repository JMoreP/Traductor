package com.example.traductorlsv.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DuoColorScheme = lightColorScheme(
    primary = DuoGreen,
    onPrimary = DuoSurface,
    primaryContainer = DuoGreenLight,
    onPrimaryContainer = DuoTextBlack,
    secondary = DuoBlue,
    onSecondary = DuoSurface,
    secondaryContainer = DuoBlueDark,
    tertiary = DuoYellow,
    onTertiary = DuoTextBlack,
    background = DuoBgLight,
    onBackground = DuoTextBlack,
    surface = DuoSurface,
    onSurface = DuoTextBlack,
    surfaceVariant = DuoBgLight,
    onSurfaceVariant = DuoTextGray,
    error = DuoRed,
    onError = DuoSurface,
    outline = DuoBorder
)

@Composable
fun TraductorLSVTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = DuoGreen.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = DuoColorScheme,
        typography = Typography,
        content = content
    )
}