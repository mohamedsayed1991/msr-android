package com.example.ui.theme

import android.app.Activity
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowCompat

private val DarkColorScheme =
  darkColorScheme(
    primary = DarkPrimaryAccent,
    secondary = DarkPrimaryAccent,
    tertiary = DarkPrimaryAccent,
    background = DarkBackground,
    surface = DarkPanelBg,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    onSurfaceVariant = DarkTextSecondary,
    outlineVariant = DarkBorder,
    error = ErrorRed,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = LightPrimaryAccent,
    secondary = LightPrimaryAccent,
    tertiary = LightPrimaryAccent,
    background = LightBackground,
    surface = LightPanelBg,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
    onSurfaceVariant = LightTextSecondary,
    outlineVariant = LightBorder,
    error = ErrorRed,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  val view = LocalView.current

  if (!view.isInEditMode) {
    SideEffect {
      var context = view.context
      var activity: Activity? = null
      while (context is ContextWrapper) {
        if (context is Activity) {
          activity = context
          break
        }
        context = context.baseContext
      }
      activity?.let { act ->
        val window = act.window
        val insetsController = WindowCompat.getInsetsController(window, view)
        insetsController.isAppearanceLightStatusBars = !darkTheme
        insetsController.isAppearanceLightNavigationBars = !darkTheme
      }
    }
  }

  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
  }
}
