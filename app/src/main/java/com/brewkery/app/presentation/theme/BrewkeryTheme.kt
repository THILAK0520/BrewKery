package com.brewkery.app.presentation.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Espresso = Color(0xFF140B07)
val Terracotta = Color(0xFFD9532F)
val Cream = Color(0xFFFDFAF7)
val WarmBorder = Color(0xFFEBD8CB)
val MutedBrown = Color(0xFF786457)
val SelectionCream = Color(0xFFF7EBE1)
val Amber = Color(0xFFF59E0B)
val SuccessGreen = Color(0xFF207D59)

private val palette = lightColorScheme(
    primary = Terracotta, onPrimary = Color.White,
    secondary = Espresso, onSecondary = Color.White,
    background = Cream, onBackground = Espresso,
    surface = Color.White, onSurface = Espresso,
    surfaceVariant = SelectionCream, onSurfaceVariant = MutedBrown,
    outline = WarmBorder,
)

private val typography = Typography(
    headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, lineHeight = 34.sp),
    headlineSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, lineHeight = 25.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 21.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 12.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 14.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 0.6.sp),
)

@Composable
fun BrewkeryTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = palette, typography = typography, content = content)
}
