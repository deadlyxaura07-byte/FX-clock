package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class ClockFaceType {
    DIGITAL,
    ANALOG,
    HYBRID
}

enum class ClockFontFamily {
    SANS,
    SERIF,
    MONOSPACE
}

enum class ThemePreset(
    val title: String,
    val backgroundHex: String,
    val surfaceHex: String,
    val surfaceElevatedHex: String,
    val textPrimaryHex: String,
    val textMutedHex: String,
    val accentHex: String,
    val clockHandsHex: String,
    val isLight: Boolean = false
) {
    ABYSS(
        title = "Abyss",
        backgroundHex = "#090A0C",
        surfaceHex = "#111317",
        surfaceElevatedHex = "#191C21",
        textPrimaryHex = "#F5F4F0",
        textMutedHex = "#A4A7AE",
        accentHex = "#C8A875",
        clockHandsHex = "#C8A875",
        isLight = false
    ),
    OBSIDIAN(
        title = "Obsidian",
        backgroundHex = "#111317",
        surfaceHex = "#181B20",
        surfaceElevatedHex = "#22252B",
        textPrimaryHex = "#F3F4F6",
        textMutedHex = "#9CA3AF",
        accentHex = "#E2E4E9",
        clockHandsHex = "#E2E4E9",
        isLight = false
    ),
    IVORY(
        title = "Ivory",
        backgroundHex = "#F7F6F2",
        surfaceHex = "#EDECE7",
        surfaceElevatedHex = "#E2E0D8",
        textPrimaryHex = "#121316",
        textMutedHex = "#666970",
        accentHex = "#24262C",
        clockHandsHex = "#C8A875",
        isLight = true
    ),
    MIDNIGHT_BLUE(
        title = "Midnight Blue",
        backgroundHex = "#0B111E",
        surfaceHex = "#121B2D",
        surfaceElevatedHex = "#1B263E",
        textPrimaryHex = "#E6ECFA",
        textMutedHex = "#8EA0C2",
        accentHex = "#4E82EA",
        clockHandsHex = "#6BA4FF",
        isLight = false
    ),
    FOREST(
        title = "Forest",
        backgroundHex = "#0B1410",
        surfaceHex = "#122019",
        surfaceElevatedHex = "#1B2C23",
        textPrimaryHex = "#E9F5EF",
        textMutedHex = "#8DAF9F",
        accentHex = "#52B788",
        clockHandsHex = "#74C69D",
        isLight = false
    ),
    MONOCHROME(
        title = "Monochrome",
        backgroundHex = "#000000",
        surfaceHex = "#121212",
        surfaceElevatedHex = "#1E1E1E",
        textPrimaryHex = "#FFFFFF",
        textMutedHex = "#888888",
        accentHex = "#FFFFFF",
        clockHandsHex = "#FFFFFF",
        isLight = false
    )
}

data class CustomThemeConfig(
    val activePreset: ThemePreset = ThemePreset.ABYSS,
    // Colors (can override active preset)
    val customBackgroundHex: String = ThemePreset.ABYSS.backgroundHex,
    val customSurfaceHex: String = ThemePreset.ABYSS.surfaceHex,
    val customSurfaceElevatedHex: String = ThemePreset.ABYSS.surfaceElevatedHex,
    val customTextPrimaryHex: String = ThemePreset.ABYSS.textPrimaryHex,
    val customTextMutedHex: String = ThemePreset.ABYSS.textMutedHex,
    val customAccentHex: String = ThemePreset.ABYSS.accentHex,
    val customClockHandsHex: String = ThemePreset.ABYSS.clockHandsHex,
    val customBorderHex: String = "#262930",

    // Typography & Sizing
    val fontFamily: ClockFontFamily = ClockFontFamily.SANS,
    val clockDigitSizeScale: Float = 1.0f, // 0.8f .. 1.4f
    val cornerRadiusDp: Int = 18, // 6 .. 32
    val borderThicknessDp: Int = 1, // 0 .. 3

    // Clock Face settings
    val clockFaceType: ClockFaceType = ClockFaceType.DIGITAL,
    val showSeconds: Boolean = true,
    val is24HourFormat: Boolean = false,
    val showAmPm: Boolean = true,
    val showDate: Boolean = true,
    val analogShowHourMarkers: Boolean = true,
    val analogShowNumerals: Boolean = false,

    // Home Widgets Toggles & Ordering
    val showHomeNextAlarm: Boolean = true,
    val showHomeWeather: Boolean = true,
    val showHomeQuickActions: Boolean = true,
    val showHomeFocusWidget: Boolean = true,

    // Animation & Motion
    val reducedMotion: Boolean = false,
    val hapticFeedbackEnabled: Boolean = true,
    val soundEffectsEnabled: Boolean = true
) {
    fun parseColor(hex: String, default: Color): Color {
        return try {
            val clean = hex.trim().replace("#", "")
            val parsed = when (clean.length) {
                6 -> clean.toLong(16) or 0x00000000FF000000L
                8 -> clean.toLong(16)
                else -> return default
            }
            Color(parsed)
        } catch (e: Exception) {
            default
        }
    }

    val backgroundColor: Color
        get() = parseColor(customBackgroundHex, Color(0xFF090A0C))

    val surfaceColor: Color
        get() = parseColor(customSurfaceHex, Color(0xFF111317))

    val surfaceElevatedColor: Color
        get() = parseColor(customSurfaceElevatedHex, Color(0xFF191C21))

    val textPrimaryColor: Color
        get() = parseColor(customTextPrimaryHex, Color(0xFFF5F4F0))

    val textMutedColor: Color
        get() = parseColor(customTextMutedHex, Color(0xFFA4A7AE))

    val accentColor: Color
        get() = parseColor(customAccentHex, Color(0xFFC8A875))

    val clockHandsColor: Color
        get() = parseColor(customClockHandsHex, Color(0xFFC8A875))

    val borderColor: Color
        get() = parseColor(customBorderHex, Color(0xFF262930))

    val isDark: Boolean
        get() = !activePreset.isLight

    companion object {
        fun fromPreset(preset: ThemePreset): CustomThemeConfig {
            return CustomThemeConfig(
                activePreset = preset,
                customBackgroundHex = preset.backgroundHex,
                customSurfaceHex = preset.surfaceHex,
                customSurfaceElevatedHex = preset.surfaceElevatedHex,
                customTextPrimaryHex = preset.textPrimaryHex,
                customTextMutedHex = preset.textMutedHex,
                customAccentHex = preset.accentHex,
                customClockHandsHex = preset.clockHandsHex,
                customBorderHex = if (preset.isLight) "#D2D0C7" else "#262930"
            )
        }
    }
}
