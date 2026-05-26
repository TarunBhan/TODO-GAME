package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = ForestGreenLight,
    onPrimary = NaturalDarkBg,
    primaryContainer = SageAccentDark,
    onPrimaryContainer = NaturalLightText,
    secondary = ForestGreenLight,
    onSecondary = NaturalDarkBg,
    secondaryContainer = SageAccentDark,
    onSecondaryContainer = NaturalLightText,
    tertiary = SlateGreenTextLight,
    onTertiary = NaturalDarkBg,
    background = NaturalDarkBg,
    onBackground = NaturalLightText,
    surface = NaturalDarkSurface,
    onSurface = NaturalLightText,
    surfaceVariant = NaturalBorderDark,
    onSurfaceVariant = SlateGreenTextLight,
    outline = SageAccentDark,
    outlineVariant = NaturalBorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = ForestGreen,
    onPrimary = Color.White,
    primaryContainer = SageAccent,
    onPrimaryContainer = NaturalDarkText,
    secondary = ForestGreen,
    onSecondary = Color.White,
    secondaryContainer = SageAccent,
    onSecondaryContainer = NaturalDarkText,
    tertiary = SlateGreenText,
    onTertiary = Color.White,
    background = NaturalBg,
    onBackground = NaturalDarkText,
    surface = Color.White,
    onSurface = NaturalDarkText,
    surfaceVariant = NaturalBorder,
    onSurfaceVariant = SlateGreenText,
    outline = SageDarkerAccent,
    outlineVariant = NaturalBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set dynamic color to false by default to ensure the elegant custom "Natural Tones" are always used
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
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
