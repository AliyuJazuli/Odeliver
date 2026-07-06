package com.hydr.odeliver.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// My colors
// Luna Color Palette

// =========================
// CORE BRAND COLORS
// =========================

val LightBlue = Color(0xFFA7EBF2)
val SkyBlue = Color(0xFF54ACBF)
val PrimaryBlue = Color(0xFF26658C)
val DeepBlue = Color(0xFF023859)
val DarkNavy = Color(0xFF011C40)

// =========================
// LIGHT THEME
// =========================

val LightPrimary = Color(0xFF26658C)//(0xFFA7EBF2)
val LightOnPrimary = Color(0xFF26658C)

val LightPrimaryContainer = Color(0xFF54ACBF)
val LightOnPrimaryContainer = Color(0xFF011C40)

val LightSecondary = Color(0xFF023859)
val LightOnSecondary = Color(0xFFA7EBF2)

val LightTertiary = Color(0xFF54ACBF)
val LightOnTertiary = Color(0xFF011C40)

val LightBackground = Color(0xFFF0F8FA)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFE6F2F6)

val LightOnSurface = Color(0xFF011C40)
val LightOutline = Color(0xFFFFFFFF)//(0xFFC5D9E2)

val LightInverseSurface = Color(0xFF023859)
val LightInverseOnSurface = Color(0xFF000000)

// =========================
// DARK THEME
// =========================

val DarkPrimary = Color(0xFF26658C)
val DarkOnPrimary = Color(0xFFA7EBF2)//(0xFF26658C)//(0xFF54ACBF)

val DarkPrimaryContainer = Color(0xFF54ACBF)
val DarkOnPrimaryContainer = Color(0xFF011C40)

val DarkSecondary = Color(0xFF023859)
val DarkOnSecondary = Color(0xFFA7EBF2)

val DarkTertiary = Color(0xFFA7EBF2)
val DarkOnTertiary = Color(0xFF011C40)

val DarkBackground = Color(0xFF011C40)
val DarkSurface = Color(0xFF023859)
val DarkSurfaceVariant = Color(0xFF022A47)

val DarkOnSurface = Color(0xFFA7EBF2)
val DarkOutline = Color(0xFF1E4A66)

val DarkInverseSurface = Color(0xFFE6F2F6)
val DarkInverseOnSurface = Color(0xFFFFFFFF)

val DarkInverseBackground = Color(0xFFF0F8FA)



 val LightColorScheme = lightColorScheme(

    primary = LightPrimary,
    onPrimary = LightOnPrimary,

    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,

    secondary = LightSecondary,
    onSecondary = LightOnSecondary,

    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,

    background = LightBackground,
    onBackground = LightOnSurface,

    surface = LightSurface,
    onSurface = LightOnSurface,

    surfaceVariant = LightSurfaceVariant,
    outline = LightOutline,

    inverseSurface = LightInverseSurface,
    inverseOnSurface = LightInverseOnSurface




)

 val DarkColorScheme = darkColorScheme(

    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,

    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,

    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,

    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,

    background = DarkBackground,
    onBackground = DarkOnSurface,

    surface = DarkSurface,
    onSurface = DarkOnSurface,

    surfaceVariant = DarkSurfaceVariant,
    outline = DarkOutline,

    inverseSurface = DarkInverseSurface,
    inverseOnSurface = DarkInverseOnSurface

)
