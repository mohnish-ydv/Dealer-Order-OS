package com.apex.dms.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val ShoppeBlue = Color(0xFF0B57F5)
val ShoppeBlueDark = Color(0xFF0744C7)
val ShoppeBlueSoft = Color(0xFFEAF1FF)
val ShoppeBackground = Color(0xFFF7F9FE)
val ShoppeSurface = Color(0xFFFFFFFF)
val ShoppeInk = Color(0xFF111827)
val ShoppeMuted = Color(0xFF7C8798)
val ShoppeStroke = Color(0xFFE8ECF4)
val ShoppeSuccess = Color(0xFF16A36A)
val ShoppeWarning = Color(0xFFF59E0B)
val ShoppeDanger = Color(0xFFE5484D)
val ShoppeLilac = Color(0xFFF1EEFF)
val ShoppeMint = Color(0xFFE9F8F1)
val ShoppePeach = Color(0xFFFFF2E8)
val ShoppeSky = Color(0xFFEAF7FF)

private val Colors = lightColorScheme(
    primary = ShoppeBlue,
    onPrimary = Color.White,
    primaryContainer = ShoppeBlueSoft,
    onPrimaryContainer = ShoppeBlueDark,
    secondary = ShoppeInk,
    onSecondary = Color.White,
    background = ShoppeBackground,
    onBackground = ShoppeInk,
    surface = ShoppeSurface,
    onSurface = ShoppeInk,
    surfaceVariant = Color(0xFFF1F4F9),
    onSurfaceVariant = ShoppeMuted,
    outline = ShoppeStroke,
    error = ShoppeDanger,
)

private val AppTypography = Typography(
    displaySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 36.sp),
    headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 32.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp),
    headlineSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 19.sp, lineHeight = 24.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 19.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 18.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
)

@Composable
fun ApexDmsTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, typography = AppTypography, content = content)
}
