package com.example.hydrate.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PastelRose,
    secondary = Pink80,
    tertiary = BabyBlue,
    background = PastelBlushPink,
    surface = WhiteCard,
    onPrimary = DarkPlum,
    onSecondary = DarkPlum,
    onTertiary = DarkPlum,
    onBackground = DarkPlum,
    onSurface = DarkPlum
)

@Composable
fun HydrateTheme(
    content: @Composable () -> Unit
) {
    // We enforce light color scheme and ignore dark mode
    val colorScheme = LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
