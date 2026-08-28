package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Crisp White Theme with Light Sky Blue Dividers & Accents
private val AppColorScheme =
  lightColorScheme(
    primary = SkyBluePrimaryDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF0F9FF), // Sky-50 soft accent fill
    onPrimaryContainer = SkyBluePrimaryDark,
    secondary = SkyBluePrimary,
    onSecondary = Color.White,
    surface = Color.White,
    onSurface = Color(0xFF0F172A), // Crisp slate 900
    surfaceVariant = Color(0xFFF8FAFC), // Slate 50 ultra clean card fill
    onSurfaceVariant = Color(0xFF475569), // Slate 600 secondary text
    background = Color.White,
    onBackground = Color(0xFF0F172A),
    outline = SkyBlueAccent,
    outlineVariant = SkyBlueBorder
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = AppColorScheme,
    typography = Typography,
    content = content
  )
}

