package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// ===== Purple + Pink Modern Palette =====

// Dark Theme Colors
val DarkBackground = Color(0xFF0C0A1A)
val DarkSurface = Color(0xFF1A1433)
val DarkSurfaceVariant = Color(0xFF231D40)
val DarkOnBackground = Color(0xFFF1EFF5)
val DarkOnSurface = Color(0xFFF1EFF5)
val DarkOnSurfaceVariant = Color(0xFFA09AB0)
val DarkBorder = Color(0x1AFFFFFF)

// Light Theme Colors
val LightBackground = Color(0xFFF7F5FC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF0ECF9)
val LightOnBackground = Color(0xFF1A1433)
val LightOnSurface = Color(0xFF1A1433)
val LightOnSurfaceVariant = Color(0xFF6B6580)
val LightBorder = Color(0x12000000)

// Primary - Vibrant Purple
val Purple80 = Color(0xFFCFBCFF)
val Purple40 = Color(0xFF7C5CFC)
val Purple30 = Color(0xFF5B3FD9)
val PurpleDark = Color(0xFF9A7AFF)
val PurpleLight = Color(0xFFB89EFF)

// Secondary - Modern Pink/Magenta
val Pink80 = Color(0xFFFFB1D2)
val Pink40 = Color(0xFFFF6B9D)
val Pink30 = Color(0xFFE94F82)
val PinkDark = Color(0xFFFF85B1)
val PinkLight = Color(0xFFFFD0E4)

// Accent Gradient Colors
val GradientPurple = Color(0xFF7C5CFC)
val GradientPink = Color(0xFFFF6B9D)
val GradientBlue = Color(0xFF4FACFE)

// Status Colors
val SuccessGreen = Color(0xFF34D399)
val ErrorRed = Color(0xFFFB7185)
val WarningAmber = Color(0xFFFBBF24)

// Design Tokens
val RadiusXL = 20.dp
val RadiusLG = 14.dp
val RadiusMD = 10.dp
val RadiusSM = 6.dp

// Legacy Composable Aliases (for backward compatibility)
val GoldGold: Color
    @Composable get() = MaterialTheme.colorScheme.primary

val BgDark: Color
    @Composable get() = MaterialTheme.colorScheme.background

val CardDark: Color
    @Composable get() = MaterialTheme.colorScheme.surface

val TextPrimary: Color
    @Composable get() = MaterialTheme.colorScheme.onBackground

val TextBody: Color
    @Composable get() = MaterialTheme.colorScheme.onBackground

val TextSecondary: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

val GlassBg: Color
    @Composable
    get() = if (isSystemInDarkTheme()) Color(0x1A7C5CFC) else Color(0x0D7C5CFC)

// Gradient helper
val PurplePinkGradient = listOf(GradientPurple, GradientPink)
