package com.tkachukmo.bandresearchapp.core.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = BandPrimaryDark,
    onPrimary = BandOnPrimaryDark,
    primaryContainer = BandPrimaryContainerDark,
    onPrimaryContainer = BandOnPrimaryContainerDark,
    secondary = BandSecondaryDark,
    secondaryContainer = BandSecondaryContainerDark,
    surface = BandSurfaceDark,
    background = BandBackgroundDark,
    onBackground = TextWhite,
    onSurface = TextWhite,
    onSurfaceVariant = TextGray,
    error = BandError
)

// Робимо світлу тему ідентичною темній, щоб дизайн не ламався на телефонах зі світлою темою
private val LightColorScheme = DarkColorScheme

@Composable
fun BandResearchAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Вимикаємо динамічні кольори за замовчуванням, щоб зберегти фіолетовий дизайн!
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        else -> DarkColorScheme // Завжди використовуємо наш темний дизайн
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Фарбуємо верхню системну шторку (де годинник і батарея) в колір фону
            window.statusBarColor = DarkBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}