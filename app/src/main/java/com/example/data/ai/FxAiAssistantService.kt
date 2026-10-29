package com.example.data.ai

import com.example.BuildConfig
import com.example.data.model.ThemePreset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.regex.Pattern

sealed class AiClockAction {
    data class CreateAlarm(val hour: Int, val minute: Int, val label: String) : AiClockAction()
    data class StartTimer(val minutes: Int, val seconds: Int = 0) : AiClockAction()
    data class StartFocus(val minutes: Int) : AiClockAction()
    data class ChangeTheme(val preset: ThemePreset) : AiClockAction()
    data class SetTimeFormat(val is24Hour: Boolean) : AiClockAction()
    data class CheckWorldTime(val city: String, val timeZoneId: String) : AiClockAction()
    object None : AiClockAction()
}

data class AiMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val proposedAction: AiClockAction = AiClockAction.None,
    val actionExecuted: Boolean = false
)

object FxAiAssistantService {

    suspend fun processQuery(query: String): AiMessage = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        val directAction = parseClockIntent(trimmed)

        // If a Gemini API key is configured in BuildConfig, query the Gemini API
        val apiKey = try {
            BuildConfig::class.java.getField("GEMINI_API_KEY").get(null) as? String
        } catch (e: Exception) {
            null
        }

        if (!apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val geminiResponse = callGeminiApi(trimmed, apiKey)
                if (geminiResponse != null) {
                    return@withContext AiMessage(
                        text = geminiResponse,
                        isUser = false,
                        proposedAction = directAction
                    )
                }
            } catch (e: Exception) {
                // Fall back to intelligent local engine
            }
        }

        // Real Intelligent FX Clock Engine response
        val responseText = when (val action = directAction) {
            is AiClockAction.CreateAlarm -> {
                val amPm = if (action.hour >= 12) "PM" else "AM"
                val h = if (action.hour == 0) 12 else if (action.hour > 12) action.hour - 12 else action.hour
                val timeStr = String.format("%d:%02d %s", h, action.minute, amPm)
                "I've configured an alarm for $timeStr with the label \"${action.label}\". Tap 'Confirm & Set' below to activate it."
            }
            is AiClockAction.StartTimer -> {
                "Ready to start a ${action.minutes} minute timer. Tap 'Confirm' to begin the countdown."
            }
            is AiClockAction.StartFocus -> {
                "Ready to begin a ${action.minutes} minute deep focus session with the FX Pomodoro engine. Tap 'Confirm' to start."
            }
            is AiClockAction.ChangeTheme -> {
                "Switching luxury theme preset to ${action.preset.title}. Tap 'Apply Theme' to activate."
            }
            is AiClockAction.SetTimeFormat -> {
                val mode = if (action.is24Hour) "24-hour military format" else "12-hour AM/PM format"
                "Ready to switch clock display to $mode. Tap 'Confirm' to update."
            }
            is AiClockAction.CheckWorldTime -> {
                try {
                    val zdt = ZonedDateTime.now(ZoneId.of(action.timeZoneId))
                    val timeStr = zdt.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault()))
                    val dateStr = zdt.format(DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault()))
                    "Current time in ${action.city}: $timeStr ($dateStr)."
                } catch (e: Exception) {
                    "Unable to resolve exact time zone for ${action.city}."
                }
            }
            AiClockAction.None -> {
                generateHelpfulResponse(trimmed)
            }
        }

        AiMessage(
            text = responseText,
            isUser = false,
            proposedAction = directAction
        )
    }

    private fun parseClockIntent(query: String): AiClockAction {
        val lower = query.lowercase(Locale.ROOT)

        // 1. Alarm intent
        if (lower.contains("alarm") || lower.contains("wake me up")) {
            val timeMatcher = Pattern.compile("(\\d{1,2})[:.]?(\\d{2})?\\s*(am|pm)?").matcher(lower)
            if (timeMatcher.find()) {
                val hourStr = timeMatcher.group(1) ?: "7"
                val minStr = timeMatcher.group(2) ?: "00"
                val amPm = timeMatcher.group(3)

                var hour = hourStr.toIntOrNull() ?: 7
                val minute = minStr.toIntOrNull() ?: 0

                if (amPm == "pm" && hour < 12) hour += 12
                if (amPm == "am" && hour == 12) hour = 0

                var label = "Alarm"
                if (lower.contains("for") && !lower.endsWith("am") && !lower.endsWith("pm")) {
                    val labelPart = lower.substringAfter("for").trim()
                    if (labelPart.isNotBlank() && !labelPart.first().isDigit()) {
                        label = labelPart.replaceFirstChar { it.uppercase() }
                    }
                }
                if (lower.contains("workout") || lower.contains("gym")) label = "Workout"
                if (lower.contains("meeting") || lower.contains("work")) label = "Morning Meeting"

                return AiClockAction.CreateAlarm(hour, minute, label)
            }
        }

        // 2. Timer intent
        if (lower.contains("timer") || lower.contains("countdown")) {
            val numMatcher = Pattern.compile("(\\d+)\\s*(min|minute|sec|second|hour)").matcher(lower)
            if (numMatcher.find()) {
                val value = numMatcher.group(1)?.toIntOrNull() ?: 5
                val unit = numMatcher.group(2) ?: "min"
                val minutes = when {
                    unit.startsWith("hour") -> value * 60
                    unit.startsWith("sec") -> Math.max(1, value / 60)
                    else -> value
                }
                return AiClockAction.StartTimer(minutes)
            }
        }

        // 3. Focus / Pomodoro intent
        if (lower.contains("focus") || lower.contains("pomodoro") || lower.contains("deep work")) {
            val numMatcher = Pattern.compile("(\\d+)\\s*(min|minute)").matcher(lower)
            val minutes = if (numMatcher.find()) {
                numMatcher.group(1)?.toIntOrNull() ?: 25
            } else 25
            return AiClockAction.StartFocus(minutes)
        }

        // 4. Theme intent
        if (lower.contains("theme") || lower.contains("switch to") || lower.contains("change palette")) {
            when {
                lower.contains("abyss") || lower.contains("gold") -> return AiClockAction.ChangeTheme(ThemePreset.ABYSS)
                lower.contains("obsidian") || lower.contains("silver") -> return AiClockAction.ChangeTheme(ThemePreset.OBSIDIAN)
                lower.contains("ivory") || lower.contains("cream") || lower.contains("light") -> return AiClockAction.ChangeTheme(ThemePreset.IVORY)
                lower.contains("blue") || lower.contains("midnight") -> return AiClockAction.ChangeTheme(ThemePreset.MIDNIGHT_BLUE)
                lower.contains("forest") || lower.contains("green") -> return AiClockAction.ChangeTheme(ThemePreset.FOREST)
                lower.contains("monochrome") || lower.contains("mono") || lower.contains("black") -> return AiClockAction.ChangeTheme(ThemePreset.MONOCHROME)
            }
        }

        // 5. 24-hour vs 12-hour format
        if (lower.contains("24 hour") || lower.contains("military")) {
            return AiClockAction.SetTimeFormat(true)
        }
        if (lower.contains("12 hour") || lower.contains("am/pm") || lower.contains("standard time")) {
            return AiClockAction.SetTimeFormat(false)
        }

        // 6. World time query
        if (lower.contains("time in") || lower.contains("time at")) {
            val cities = mapOf(
                "tokyo" to ("Tokyo" to "Asia/Tokyo"),
                "london" to ("London" to "Europe/London"),
                "new york" to ("New York" to "America/New_York"),
                "paris" to ("Paris" to "Europe/Paris"),
                "sydney" to ("Sydney" to "Australia/Sydney"),
                "dubai" to ("Dubai" to "Asia/Dubai"),
                "san francisco" to ("San Francisco" to "America/Los_Angeles"),
                "singapore" to ("Singapore" to "Asia/Singapore"),
                "zurich" to ("Zurich" to "Europe/Zurich"),
                "berlin" to ("Berlin" to "Europe/Berlin"),
                "mumbai" to ("Mumbai" to "Asia/Kolkata"),
                "delhi" to ("Delhi" to "Asia/Kolkata")
            )
            for ((key, pair) in cities) {
                if (lower.contains(key)) {
                    return AiClockAction.CheckWorldTime(pair.first, pair.second)
                }
            }
        }

        return AiClockAction.None
    }

    private fun generateHelpfulResponse(query: String): String {
        val lower = query.lowercase(Locale.ROOT)
        return when {
            lower.contains("hello") || lower.contains("hi") || lower.contains("hey") ->
                "Hello. I am the FX AI Assistant. I can set alarms, start precision countdown timers, launch focus sessions, query world time, and switch luxury themes. How may I assist you?"
            lower.contains("who are you") || lower.contains("fakeexx") ->
                "FAKEEXX SMART CLOCK is engineered by FX GROUP. Tagline: 'THE FUTURE STARTS HERE'. I am your dedicated timekeeper intelligence."
            lower.contains("help") ->
                "You can command me to:\n• \"Set alarm for 6:30 AM for Workout\"\n• \"Start a 15 minute timer\"\n• \"Launch 25 min focus\"\n• \"What time is it in Tokyo?\"\n• \"Switch theme to Abyss\"\n• \"Switch to 24 hour mode\""
            else ->
                "Understood. You can ask me to schedule alarms, run countdown timers, trigger focus intervals, or query world clock times for any major capital."
        }
    }

    private fun callGeminiApi(prompt: String, apiKey: String): String? {
        try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$apiKey"
            val url = URL(endpoint)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true

            val sysInstruction = "You are the FX AI Smart Clock Assistant for FAKEEXX SMART CLOCK (THE FUTURE STARTS HERE). Be concise, ultra-refined, premium, and helpful. If the user asks to set an alarm, timer, focus session, theme, or world time, answer concisely and acknowledge the request."

            val jsonBody = JSONObject().apply {
                val contents = JSONArray().apply {
                    put(JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply { put("text", "$sysInstruction\n\nUser request: $prompt") })
                        }
                        put("parts", parts)
                    })
                }
                put("contents", contents)
            }

            OutputStreamWriter(conn.outputStream).use { it.write(jsonBody.toString()) }

            if (conn.responseCode in 200..299) {
                val responseText = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                val root = JSONObject(responseText)
                val candidates = root.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return parts.getJSONObject(0).optString("text")
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
