package com.tkachukmo.bandresearchapp.core.ui.theme

import androidx.compose.ui.graphics.Color

// Custom BandMatch palette (Новий дизайн)
val NeonPurple = Color(0xFFB288FF)
val DeepPurple = Color(0xFF4A148C)
val DarkBackground = Color(0xFF121212)
val SurfaceDark = Color(0xFF1E1E1E)
val SurfaceVariantDark = Color(0xFF2A2A2A)
val TextWhite = Color(0xFFFFFFFF)
val TextGray = Color(0xFFA0A0A0)
val ErrorRed = Color(0xFFFF5252)

// Підв'язка під Material 3 (Примусово темні кольори для всього додатку)
val BandPrimaryDark = NeonPurple
val BandOnPrimaryDark = DarkBackground
val BandPrimaryContainerDark = DeepPurple
val BandOnPrimaryContainerDark = TextWhite
val BandSecondaryDark = Color(0xFFBB86FC)
val BandSecondaryContainerDark = SurfaceVariantDark
val BandSurfaceDark = SurfaceDark
val BandBackgroundDark = DarkBackground
val BandError = ErrorRed

// (Залишаємо для сумісності зі старим кодом, але вони дублюють темну тему)
val BandPrimary = BandPrimaryDark
val BandOnPrimary = BandOnPrimaryDark
val BandPrimaryContainer = BandPrimaryContainerDark
val BandOnPrimaryContainer = BandOnPrimaryContainerDark
val BandSecondary = BandSecondaryDark
val BandSecondaryContainer = BandSecondaryContainerDark
val BandOnSecondaryContainer = TextWhite
val BandTertiary = Color(0xFF03DAC6)
val BandTertiaryContainer = Color(0xFF00544F)
val BandSurface = BandSurfaceDark
val BandBackground = BandBackgroundDark