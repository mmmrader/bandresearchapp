package com.tkachukmo.bandresearchapp.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = BandPrimary,
    onPrimary = BandOnPrimary,
    primaryContainer = BandPrimaryContainer,
    onPrimaryContainer = BandOnPrimaryContainer,
    secondary = BandSecondary,
    secondaryContainer = BandSecondaryContainer,
    onSecondaryContainer = BandOnSecondaryContainer,
    tertiary = BandTertiary,
    tertiaryContainer = BandTertiaryContainer,
    surface = BandSurface,
    background = BandBackground,
    error = BandError
)

private val DarkColorScheme = darkColorScheme(
    primary = BandPrimaryDark,
    onPrimary = BandOnPrimaryDark,
    primaryContainer = BandPrimaryContainerDark,
    onPrimaryContainer = BandOnPrimaryContainerDark,
    secondary = BandSecondaryDark,
    secondaryContainer = BandSecondaryContainerDark,
    surface = BandSurfaceDark,
    background = BandBackgroundDark
)

@Composable
fun BandResearchAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color — працює на Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}