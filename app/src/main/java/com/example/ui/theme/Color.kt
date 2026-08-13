package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Static Colors from MSR design tokens
val LightBackground = Color(0xFFF4F4F5)
val LightSidebarBg = Color(0xFFFFFFFF)
val LightPanelBg = Color(0xFFFFFFFF)
val LightBorder = Color(0x1409090B) // rgba(9, 9, 11, 0.08)
val LightTextPrimary = Color(0xFF09090B)
val LightTextSecondary = Color(0xFF52525B)
val LightPrimaryAccent = Color(0xFF0284C7)
val LightPrimaryHover = Color(0xFF0369A1)
val LightPrimaryGlow = Color(0x140284C7) // rgba(2, 132, 199, 0.08)

val DarkBackground = Color(0xFF09090B)
val DarkSidebarBg = Color(0xFF121214)
val DarkPanelBg = Color(0xFF18181B)
val DarkBorder = Color(0x0FFFFFFF) // rgba(255, 255, 255, 0.06)
val DarkTextPrimary = Color(0xFFFAFAFA)
val DarkTextSecondary = Color(0xFFA1A1AA)
val DarkPrimaryAccent = Color(0xFF0EA5E9)
val DarkPrimaryHover = Color(0xFF0284C7)
val DarkPrimaryGlow = Color(0x260EA5E9) // rgba(14, 165, 233, 0.15)

// Shared Design Tokens (Radii mapped to dp)
val RadiusLG = 12.dp
val RadiusMD = 8.dp
val RadiusSM = 6.dp

// System / Status Colors
val SuccessGreen = Color(0xFF10B981) // Emerald Green
val ErrorRed = Color(0xFFF43F5E)     // Rose Pink-Red (Danger)
val WarningOrange = Color(0xFFF59E0B) // Amber Orange

// Dynamically mapped property-getters for legacy usage in Composable screens
val GoldGold: Color
    @Composable
    get() = MaterialTheme.colorScheme.primary

val BgDark: Color
    @Composable
    get() = MaterialTheme.colorScheme.background

val CardDark: Color
    @Composable
    get() = MaterialTheme.colorScheme.surface

val TextPrimary: Color
    @Composable
    get() = MaterialTheme.colorScheme.onBackground

val TextBody: Color
    @Composable
    get() = MaterialTheme.colorScheme.onBackground

val TextSecondary: Color
    @Composable
    get() = MaterialTheme.colorScheme.onSurfaceVariant

val GlassBg: Color
    @Composable
    get() = if (isSystemInDarkTheme()) Color(0x1F000000) else Color(0x0FFFFFFF)
