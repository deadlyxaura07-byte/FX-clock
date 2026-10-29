package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.data.model.CustomThemeConfig

val LocalCustomThemeConfig = staticCompositionLocalOf { CustomThemeConfig() }

@Composable
fun FakeexxClockTheme(
    config: CustomThemeConfig = CustomThemeConfig(),
    content: @Composable () -> Unit
) {
    val isLight = !config.isDark

    val colorScheme = if (isLight) {
        lightColorScheme(
            primary = config.accentColor,
            onPrimary = config.backgroundColor,
            primaryContainer = config.surfaceElevatedColor,
            onPrimaryContainer = config.textPrimaryColor,
            secondary = config.accentColor,
            onSecondary = config.backgroundColor,
            background = config.backgroundColor,
            onBackground = config.textPrimaryColor,
            surface = config.surfaceColor,
            onSurface = config.textPrimaryColor,
            surfaceVariant = config.surfaceElevatedColor,
            onSurfaceVariant = config.textMutedColor,
            outline = config.borderColor
        )
    } else {
        darkColorScheme(
            primary = config.accentColor,
            onPrimary = Color.Black,
            primaryContainer = config.surfaceElevatedColor,
            onPrimaryContainer = config.textPrimaryColor,
            secondary = config.accentColor,
            onSecondary = Color.Black,
            background = config.backgroundColor,
            onBackground = config.textPrimaryColor,
            surface = config.surfaceColor,
            onSurface = config.textPrimaryColor,
            surfaceVariant = config.surfaceElevatedColor,
            onSurfaceVariant = config.textMutedColor,
            outline = config.borderColor
        )
    }

    val typography = createFakeexxTypography(getClockFontFamily(config.fontFamily))

    CompositionLocalProvider(
        LocalCustomThemeConfig provides config
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}

// Backward compatibility alias
@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    FakeexxClockTheme(content = content)
}
