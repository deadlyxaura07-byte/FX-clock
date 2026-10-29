package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.ClockFaceType
import com.example.data.model.ClockFontFamily
import com.example.data.model.CustomThemeConfig
import com.example.data.model.ThemePreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

class FakeexxPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _themeConfigFlow = MutableStateFlow(loadThemeConfig())
    val themeConfigFlow: StateFlow<CustomThemeConfig> = _themeConfigFlow.asStateFlow()

    private val _is24HourFlow = MutableStateFlow(prefs.getBoolean(KEY_24_HOUR, false))
    val is24HourFlow: StateFlow<Boolean> = _is24HourFlow.asStateFlow()

    private val _userNameFlow = MutableStateFlow(prefs.getString(KEY_USER_NAME, "FX Collector") ?: "FX Collector")
    val userNameFlow: StateFlow<String> = _userNameFlow.asStateFlow()

    private val _isOnboardingCompletedFlow = MutableStateFlow(prefs.getBoolean(KEY_ONBOARDING_DONE, false))
    val isOnboardingCompletedFlow: StateFlow<Boolean> = _isOnboardingCompletedFlow.asStateFlow()

    fun updateThemeConfig(config: CustomThemeConfig) {
        prefs.edit().apply {
            putString(KEY_ACTIVE_PRESET, config.activePreset.name)
            putString(KEY_BG_HEX, config.customBackgroundHex)
            putString(KEY_SURFACE_HEX, config.customSurfaceHex)
            putString(KEY_SURFACE_ELEVATED_HEX, config.customSurfaceElevatedHex)
            putString(KEY_TEXT_PRIMARY_HEX, config.customTextPrimaryHex)
            putString(KEY_TEXT_MUTED_HEX, config.customTextMutedHex)
            putString(KEY_ACCENT_HEX, config.customAccentHex)
            putString(KEY_CLOCK_HANDS_HEX, config.customClockHandsHex)
            putString(KEY_BORDER_HEX, config.customBorderHex)
            putString(KEY_FONT_FAMILY, config.fontFamily.name)
            putFloat(KEY_DIGIT_SIZE_SCALE, config.clockDigitSizeScale)
            putInt(KEY_CORNER_RADIUS, config.cornerRadiusDp)
            putInt(KEY_BORDER_THICKNESS, config.borderThicknessDp)
            putString(KEY_CLOCK_FACE_TYPE, config.clockFaceType.name)
            putBoolean(KEY_SHOW_SECONDS, config.showSeconds)
            putBoolean(KEY_24_HOUR, config.is24HourFormat)
            putBoolean(KEY_SHOW_AM_PM, config.showAmPm)
            putBoolean(KEY_SHOW_DATE, config.showDate)
            putBoolean(KEY_ANALOG_HOUR_MARKERS, config.analogShowHourMarkers)
            putBoolean(KEY_ANALOG_NUMERALS, config.analogShowNumerals)
            putBoolean(KEY_HOME_NEXT_ALARM, config.showHomeNextAlarm)
            putBoolean(KEY_HOME_WEATHER, config.showHomeWeather)
            putBoolean(KEY_HOME_QUICK_ACTIONS, config.showHomeQuickActions)
            putBoolean(KEY_HOME_FOCUS_WIDGET, config.showHomeFocusWidget)
            putBoolean(KEY_REDUCED_MOTION, config.reducedMotion)
            putBoolean(KEY_HAPTIC, config.hapticFeedbackEnabled)
            putBoolean(KEY_SOUND, config.soundEffectsEnabled)
            apply()
        }
        _themeConfigFlow.value = config
        _is24HourFlow.value = config.is24HourFormat
    }

    fun set24HourFormat(is24: Boolean) {
        prefs.edit().putBoolean(KEY_24_HOUR, is24).apply()
        _is24HourFlow.value = is24
        val current = _themeConfigFlow.value
        updateThemeConfig(current.copy(is24HourFormat = is24))
    }

    fun setUserName(name: String) {
        prefs.edit().putString(KEY_USER_NAME, name).apply()
        _userNameFlow.value = name
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_DONE, completed).apply()
        _isOnboardingCompletedFlow.value = completed
    }

    fun exportThemeJson(config: CustomThemeConfig): String {
        val json = JSONObject().apply {
            put("preset", config.activePreset.name)
            put("bg", config.customBackgroundHex)
            put("surface", config.customSurfaceHex)
            put("surfaceElevated", config.customSurfaceElevatedHex)
            put("textPrimary", config.customTextPrimaryHex)
            put("textMuted", config.customTextMutedHex)
            put("accent", config.customAccentHex)
            put("clockHands", config.customClockHandsHex)
            put("border", config.customBorderHex)
            put("font", config.fontFamily.name)
            put("face", config.clockFaceType.name)
            put("radius", config.cornerRadiusDp)
        }
        return json.toString(2)
    }

    fun importThemeJson(jsonString: String): CustomThemeConfig? {
        return try {
            val obj = JSONObject(jsonString)
            val current = _themeConfigFlow.value
            current.copy(
                customBackgroundHex = obj.optString("bg", current.customBackgroundHex),
                customSurfaceHex = obj.optString("surface", current.customSurfaceHex),
                customSurfaceElevatedHex = obj.optString("surfaceElevated", current.customSurfaceElevatedHex),
                customTextPrimaryHex = obj.optString("textPrimary", current.customTextPrimaryHex),
                customTextMutedHex = obj.optString("textMuted", current.customTextMutedHex),
                customAccentHex = obj.optString("accent", current.customAccentHex),
                customClockHandsHex = obj.optString("clockHands", current.customClockHandsHex),
                customBorderHex = obj.optString("border", current.customBorderHex),
                cornerRadiusDp = obj.optInt("radius", current.cornerRadiusDp)
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun loadThemeConfig(): CustomThemeConfig {
        val presetName = prefs.getString(KEY_ACTIVE_PRESET, ThemePreset.ABYSS.name)
        val preset = try {
            ThemePreset.valueOf(presetName ?: ThemePreset.ABYSS.name)
        } catch (e: Exception) {
            ThemePreset.ABYSS
        }

        val fontName = prefs.getString(KEY_FONT_FAMILY, ClockFontFamily.SANS.name)
        val font = try {
            ClockFontFamily.valueOf(fontName ?: ClockFontFamily.SANS.name)
        } catch (e: Exception) {
            ClockFontFamily.SANS
        }

        val faceName = prefs.getString(KEY_CLOCK_FACE_TYPE, ClockFaceType.DIGITAL.name)
        val face = try {
            ClockFaceType.valueOf(faceName ?: ClockFaceType.DIGITAL.name)
        } catch (e: Exception) {
            ClockFaceType.DIGITAL
        }

        return CustomThemeConfig(
            activePreset = preset,
            customBackgroundHex = prefs.getString(KEY_BG_HEX, preset.backgroundHex) ?: preset.backgroundHex,
            customSurfaceHex = prefs.getString(KEY_SURFACE_HEX, preset.surfaceHex) ?: preset.surfaceHex,
            customSurfaceElevatedHex = prefs.getString(KEY_SURFACE_ELEVATED_HEX, preset.surfaceElevatedHex) ?: preset.surfaceElevatedHex,
            customTextPrimaryHex = prefs.getString(KEY_TEXT_PRIMARY_HEX, preset.textPrimaryHex) ?: preset.textPrimaryHex,
            customTextMutedHex = prefs.getString(KEY_TEXT_MUTED_HEX, preset.textMutedHex) ?: preset.textMutedHex,
            customAccentHex = prefs.getString(KEY_ACCENT_HEX, preset.accentHex) ?: preset.accentHex,
            customClockHandsHex = prefs.getString(KEY_CLOCK_HANDS_HEX, preset.clockHandsHex) ?: preset.clockHandsHex,
            customBorderHex = prefs.getString(KEY_BORDER_HEX, if (preset.isLight) "#D2D0C7" else "#262930") ?: "#262930",
            fontFamily = font,
            clockDigitSizeScale = prefs.getFloat(KEY_DIGIT_SIZE_SCALE, 1.0f),
            cornerRadiusDp = prefs.getInt(KEY_CORNER_RADIUS, 18),
            borderThicknessDp = prefs.getInt(KEY_BORDER_THICKNESS, 1),
            clockFaceType = face,
            showSeconds = prefs.getBoolean(KEY_SHOW_SECONDS, true),
            is24HourFormat = prefs.getBoolean(KEY_24_HOUR, false),
            showAmPm = prefs.getBoolean(KEY_SHOW_AM_PM, true),
            showDate = prefs.getBoolean(KEY_SHOW_DATE, true),
            analogShowHourMarkers = prefs.getBoolean(KEY_ANALOG_HOUR_MARKERS, true),
            analogShowNumerals = prefs.getBoolean(KEY_ANALOG_NUMERALS, false),
            showHomeNextAlarm = prefs.getBoolean(KEY_HOME_NEXT_ALARM, true),
            showHomeWeather = prefs.getBoolean(KEY_HOME_WEATHER, true),
            showHomeQuickActions = prefs.getBoolean(KEY_HOME_QUICK_ACTIONS, true),
            showHomeFocusWidget = prefs.getBoolean(KEY_HOME_FOCUS_WIDGET, true),
            reducedMotion = prefs.getBoolean(KEY_REDUCED_MOTION, false),
            hapticFeedbackEnabled = prefs.getBoolean(KEY_HAPTIC, true),
            soundEffectsEnabled = prefs.getBoolean(KEY_SOUND, true)
        )
    }

    companion object {
        private const val PREFS_NAME = "fakeexx_clock_prefs"
        private const val KEY_ACTIVE_PRESET = "active_preset"
        private const val KEY_BG_HEX = "bg_hex"
        private const val KEY_SURFACE_HEX = "surface_hex"
        private const val KEY_SURFACE_ELEVATED_HEX = "surface_elevated_hex"
        private const val KEY_TEXT_PRIMARY_HEX = "text_primary_hex"
        private const val KEY_TEXT_MUTED_HEX = "text_muted_hex"
        private const val KEY_ACCENT_HEX = "accent_hex"
        private const val KEY_CLOCK_HANDS_HEX = "clock_hands_hex"
        private const val KEY_BORDER_HEX = "border_hex"
        private const val KEY_FONT_FAMILY = "font_family"
        private const val KEY_DIGIT_SIZE_SCALE = "digit_scale"
        private const val KEY_CORNER_RADIUS = "corner_radius"
        private const val KEY_BORDER_THICKNESS = "border_thickness"
        private const val KEY_CLOCK_FACE_TYPE = "clock_face_type"
        private const val KEY_SHOW_SECONDS = "show_seconds"
        private const val KEY_24_HOUR = "is_24_hour"
        private const val KEY_SHOW_AM_PM = "show_am_pm"
        private const val KEY_SHOW_DATE = "show_date"
        private const val KEY_ANALOG_HOUR_MARKERS = "analog_hour_markers"
        private const val KEY_ANALOG_NUMERALS = "analog_numerals"
        private const val KEY_HOME_NEXT_ALARM = "home_next_alarm"
        private const val KEY_HOME_WEATHER = "home_weather"
        private const val KEY_HOME_QUICK_ACTIONS = "home_quick_actions"
        private const val KEY_HOME_FOCUS_WIDGET = "home_focus_widget"
        private const val KEY_REDUCED_MOTION = "reduced_motion"
        private const val KEY_HAPTIC = "haptic_enabled"
        private const val KEY_SOUND = "sound_enabled"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_ONBOARDING_DONE = "onboarding_done"
    }
}
