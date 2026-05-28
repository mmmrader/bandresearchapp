package com.tkachukmo.bandresearchapp.core.ui.theme

import androidx.compose.ui.graphics.Color

// Custom BandMatch palette (Новий дизайн)
val NeonPurple = Color(0xFFDAB9FF)
val DeepPurple = Color(0xFFBB86FC)
val DarkBackground = Color(0xFF131313)
val SurfaceDark = Color(0xFF1C1B1B)
val SurfaceVariantDark = Color(0xFF2A2A2A)
val TextWhite = Color(0xFFE5E2E1)
val TextGray = Color(0xFFCDC3D4)
val ErrorRed = Color(0xFFFFB4AB)

// Підв'язка під Material 3 (Примусово темні кольори для всього додатку)
val BandPrimaryDark = NeonPurple
val BandOnPrimaryDark = Color(0xFF460283)
val BandPrimaryContainerDark = DeepPurple
val BandOnPrimaryContainerDark = Color(0xFF4C0F89)
val BandSecondaryDark = Color(0xFFCCC4D0)
val BandSecondaryContainerDark = Color(0xFF4C4751)
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
val BandTertiary = Color(0xFF17DECA)
val BandTertiaryContainer = Color(0xFF00B2A1)
val BandSurface = BandSurfaceDark
val BandBackground = BandBackgroundDark
