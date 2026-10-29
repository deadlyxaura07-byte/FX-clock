package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiClockAction
import com.example.data.ai.AiMessage
import com.example.data.ai.FxAiAssistantService
import com.example.data.database.AppDatabase
import com.example.data.model.AlarmItem
import com.example.data.model.ClockFaceType
import com.example.data.model.CustomThemeConfig
import com.example.data.model.FocusMode
import com.example.data.model.FocusSession
import com.example.data.model.LapItem
import com.example.data.model.ThemePreset
import com.example.data.model.WeatherState
import com.example.data.model.WorldClockCity
import com.example.data.network.WeatherClient
import com.example.data.preferences.FakeexxPreferences
import com.example.receiver.ActiveAlarmState
import com.example.receiver.AlarmStateManager
import com.example.service.AlarmScheduler
import com.example.service.SoundVibrateHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

data class LiveTimeState(
    val hour24: Int = 12,
    val hour12: Int = 12,
    val minute: Int = 0,
    val second: Int = 0,
    val millis: Int = 0,
    val isPm: Boolean = false,
    val formattedTime12: String = "12:00",
    val formattedTime24: String = "12:00",
    val formattedSeconds: String = "00",
    val formattedDate: String = "Thursday, Oct 29",
    val dayOfWeek: String = "Thursday"
)

class ClockAppViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context get() = getApplication()
    private val database = AppDatabase.getDatabase(context)
    val preferences = FakeexxPreferences(context)
    private val alarmScheduler = AlarmScheduler(context)

    // 1. Live Time State
    private val _liveTime = MutableStateFlow(calculateCurrentLiveTime())
    val liveTime: StateFlow<LiveTimeState> = _liveTime.asStateFlow()

    // 2. Preferences & Theme
    val themeConfig: StateFlow<CustomThemeConfig> = preferences.themeConfigFlow
    val is24Hour: StateFlow<Boolean> = preferences.is24HourFlow
    val userName: StateFlow<String> = preferences.userNameFlow
    val isOnboardingCompleted: StateFlow<Boolean> = preferences.isOnboardingCompletedFlow

    // 3. Alarms
    val alarms: StateFlow<List<AlarmItem>> = database.alarmDao().getAllAlarms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeRingingAlarm: StateFlow<ActiveAlarmState?> = AlarmStateManager.ringingAlarm

    // 4. Stopwatch State
    private val _stopwatchElapsedMillis = MutableStateFlow(0L)
    val stopwatchElapsedMillis: StateFlow<Long> = _stopwatchElapsedMillis.asStateFlow()

    private val _isStopwatchRunning = MutableStateFlow(false)
    val isStopwatchRunning: StateFlow<Boolean> = _isStopwatchRunning.asStateFlow()

    private val _stopwatchLaps = MutableStateFlow<List<LapItem>>(emptyList())
    val stopwatchLaps: StateFlow<List<LapItem>> = _stopwatchLaps.asStateFlow()

    private var stopwatchBaseTime = 0L
    private var stopwatchAccumulated = 0L
    private var stopwatchJob: Job? = null
    private var lastLapTimestamp = 0L

    // 5. Timer State
    private val _timerTotalSeconds = MutableStateFlow(5 * 60)
    val timerTotalSeconds: StateFlow<Int> = _timerTotalSeconds.asStateFlow()

    private val _timerRemainingSeconds = MutableStateFlow(5 * 60)
    val timerRemainingSeconds: StateFlow<Int> = _timerRemainingSeconds.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _isTimerPaused = MutableStateFlow(false)
    val isTimerPaused: StateFlow<Boolean> = _isTimerPaused.asStateFlow()

    private var timerEndTimestamp = 0L
    private var timerJob: Job? = null

    // 6. Focus (Pomodoro) State
    private val _focusMode = MutableStateFlow(FocusMode.WORK)
    val focusMode: StateFlow<FocusMode> = _focusMode.asStateFlow()

    private val _focusRemainingSeconds = MutableStateFlow(FocusMode.WORK.defaultMinutes * 60)
    val focusRemainingSeconds: StateFlow<Int> = _focusRemainingSeconds.asStateFlow()

    private val _isFocusRunning = MutableStateFlow(false)
    val isFocusRunning: StateFlow<Boolean> = _isFocusRunning.asStateFlow()

    private val _focusCycle = MutableStateFlow(1)
    val focusCycle: StateFlow<Int> = _focusCycle.asStateFlow()

    private var focusEndTimestamp = 0L
    private var focusJob: Job? = null

    val completedFocusSessions: StateFlow<Int> = database.focusDao().getCompletedSessionCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalFocusMinutes: StateFlow<Int?> = database.focusDao().getTotalFocusMinutes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // 7. World Clock
    val worldCities: StateFlow<List<WorldClockCity>> = database.worldClockDao().getAllCities()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 8. Weather
    private val _weatherState = MutableStateFlow(WeatherState())
    val weatherState: StateFlow<WeatherState> = _weatherState.asStateFlow()

    // 9. FX AI Assistant
    private val _aiMessages = MutableStateFlow<List<AiMessage>>(
        listOf(
            AiMessage(
                text = "Welcome to FAKEEXX SMART CLOCK. I am the FX Assistant. Speak or type your request (e.g. \"Set alarm for 6:30 AM\", \"Start 25 min focus\", \"Time in Tokyo\", or \"Switch to Midnight Blue\").",
                isUser = false
            )
        )
    )
    val aiMessages: StateFlow<List<AiMessage>> = _aiMessages.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // 10. Customization Studio Undo/Redo & Live Draft
    private val _studioDraft = MutableStateFlow(preferences.themeConfigFlow.value)
    val studioDraft: StateFlow<CustomThemeConfig> = _studioDraft.asStateFlow()

    private val undoStack = mutableListOf<CustomThemeConfig>()
    private val redoStack = mutableListOf<CustomThemeConfig>()

    init {
        startLiveTimeClock()
        refreshWeather()
    }

    // --- Clock Engine ---
    private fun startLiveTimeClock() {
        viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                _liveTime.value = calculateCurrentLiveTime()
                delay(200L) // Smooth 5 updates/sec for responsive seconds & analog sweeps
            }
        }
    }

    private fun calculateCurrentLiveTime(): LiveTimeState {
        val now = LocalTime.now()
        val today = LocalDate.now()
        val hour24 = now.hour
        val hour12 = if (hour24 == 0) 12 else if (hour24 > 12) hour24 - 12 else hour24
        val minute = now.minute
        val second = now.second
        val millis = now.nano / 1_000_000

        val dateFormatted = today.format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault()))
        val dayOfWeek = today.format(DateTimeFormatter.ofPattern("EEEE", Locale.getDefault()))

        return LiveTimeState(
            hour24 = hour24,
            hour12 = hour12,
            minute = minute,
            second = second,
            millis = millis,
            isPm = hour24 >= 12,
            formattedTime12 = String.format("%02d:%02d", hour12, minute),
            formattedTime24 = String.format("%02d:%02d", hour24, minute),
            formattedSeconds = String.format("%02d", second),
            formattedDate = dateFormatted,
            dayOfWeek = dayOfWeek
        )
    }

    // --- Alarms Management ---
    fun toggleAlarm(alarm: AlarmItem) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = alarm.copy(isEnabled = !alarm.isEnabled)
            database.alarmDao().updateAlarm(updated)
            if (updated.isEnabled) {
                alarmScheduler.schedule(updated)
            } else {
                alarmScheduler.cancel(updated.id)
            }
        }
    }

    fun saveAlarm(alarm: AlarmItem) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = if (alarm.id == 0L) {
                database.alarmDao().insertAlarm(alarm)
            } else {
                database.alarmDao().updateAlarm(alarm)
                alarm.id
            }
            if (alarm.isEnabled) {
                alarmScheduler.schedule(alarm.copy(id = id))
            } else {
                alarmScheduler.cancel(id)
            }
        }
    }

    fun duplicateAlarm(alarm: AlarmItem) {
        viewModelScope.launch(Dispatchers.IO) {
            val copy = alarm.copy(
                id = 0,
                label = "${alarm.label} (Copy)",
                isEnabled = true
            )
            val newId = database.alarmDao().insertAlarm(copy)
            alarmScheduler.schedule(copy.copy(id = newId))
        }
    }

    fun deleteAlarm(alarm: AlarmItem) {
        viewModelScope.launch(Dispatchers.IO) {
            alarmScheduler.cancel(alarm.id)
            database.alarmDao().deleteAlarm(alarm)
        }
    }

    fun stopRingingAlarm() {
        AlarmStateManager.dismissRinging()
    }

    fun snoozeRingingAlarm(snoozeMinutes: Int = 5) {
        val current = activeRingingAlarm.value ?: return
        AlarmStateManager.dismissRinging()
        val nextMillis = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)
        val cal = Calendar.getInstance().apply { timeInMillis = nextMillis }

        val snoozeAlarm = AlarmItem(
            id = System.currentTimeMillis(),
            hour = cal.get(Calendar.HOUR_OF_DAY),
            minute = cal.get(Calendar.MINUTE),
            label = "${current.label} (Snoozed)",
            isEnabled = true,
            isOneTime = true
        )
        alarmScheduler.schedule(snoozeAlarm)
    }

    fun triggerTestAlarmRinging() {
        SoundVibrateHelper.playAlarmSound(context)
        SoundVibrateHelper.vibrateDevice(context, 1000L)
        AlarmStateManager.triggerRinging(
            ActiveAlarmState(
                alarmId = 999999L,
                label = "Morning Rise Preview",
                formattedTime = _liveTime.value.formattedTime12,
                isRinging = true
            )
        )
    }

    // --- Stopwatch ---
    fun startStopwatch() {
        if (_isStopwatchRunning.value) return
        _isStopwatchRunning.value = true
        stopwatchBaseTime = System.currentTimeMillis() - stopwatchAccumulated
        lastLapTimestamp = stopwatchAccumulated

        stopwatchJob = viewModelScope.launch(Dispatchers.Default) {
            while (isActive && _isStopwatchRunning.value) {
                val currentElapsed = System.currentTimeMillis() - stopwatchBaseTime
                _stopwatchElapsedMillis.value = currentElapsed
                delay(16L) // ~60fps smooth timer update
            }
        }
    }

    fun pauseStopwatch() {
        if (!_isStopwatchRunning.value) return
        _isStopwatchRunning.value = false
        stopwatchJob?.cancel()
        stopwatchAccumulated = _stopwatchElapsedMillis.value
    }

    fun resetStopwatch() {
        _isStopwatchRunning.value = false
        stopwatchJob?.cancel()
        stopwatchAccumulated = 0L
        stopwatchBaseTime = 0L
        lastLapTimestamp = 0L
        _stopwatchElapsedMillis.value = 0L
        _stopwatchLaps.value = emptyList()
    }

    fun recordStopwatchLap() {
        val total = _stopwatchElapsedMillis.value
        val lapDuration = if (_stopwatchLaps.value.isEmpty()) total else total - lastLapTimestamp
        lastLapTimestamp = total

        val currentLaps = _stopwatchLaps.value.toMutableList()
        val lapNum = currentLaps.size + 1
        currentLaps.add(0, LapItem(lapNumber = lapNum, lapTimeMillis = lapDuration, totalTimeMillis = total))

        // Recalculate fastest and slowest
        if (currentLaps.size >= 2) {
            val minLap = currentLaps.minOf { it.lapTimeMillis }
            val maxLap = currentLaps.maxOf { it.lapTimeMillis }
            val updated = currentLaps.map { lap ->
                lap.copy(
                    isFastest = lap.lapTimeMillis == minLap,
                    isSlowest = lap.lapTimeMillis == maxLap && minLap != maxLap
                )
            }
            _stopwatchLaps.value = updated
        } else {
            _stopwatchLaps.value = currentLaps
        }
    }

    fun getLapsSummaryText(): String {
        val sb = StringBuilder("FAKEEXX Stopwatch Session\n")
        sb.append("Total Time: ${LapItem.formatMillis(_stopwatchElapsedMillis.value)}\n\n")
        _stopwatchLaps.value.forEach { lap ->
            sb.append("Lap ${lap.lapNumber}: ${lap.formattedLapTime()} (Total: ${lap.formattedTotalTime()})\n")
        }
        return sb.toString()
    }

    // --- Countdown Timer ---
    fun setTimerDuration(hours: Int, minutes: Int, seconds: Int) {
        val total = (hours * 3600) + (minutes * 60) + seconds
        if (total > 0) {
            _timerTotalSeconds.value = total
            _timerRemainingSeconds.value = total
            _isTimerRunning.value = false
            _isTimerPaused.value = false
            timerJob?.cancel()
        }
    }

    fun addTimerSeconds(extraSeconds: Int) {
        val newTotal = _timerRemainingSeconds.value + extraSeconds
        _timerTotalSeconds.value = Math.max(_timerTotalSeconds.value, newTotal)
        _timerRemainingSeconds.value = newTotal
    }

    fun startTimer() {
        if (_timerRemainingSeconds.value <= 0) return
        _isTimerRunning.value = true
        _isTimerPaused.value = false
        timerEndTimestamp = System.currentTimeMillis() + (_timerRemainingSeconds.value * 1000L)

        timerJob?.cancel()
        timerJob = viewModelScope.launch(Dispatchers.Default) {
            while (isActive && _isTimerRunning.value) {
                val remainingMillis = timerEndTimestamp - System.currentTimeMillis()
                val remSeconds = Math.max(0, Math.ceil(remainingMillis / 1000.0).toInt())
                _timerRemainingSeconds.value = remSeconds

                if (remSeconds <= 0) {
                    _isTimerRunning.value = false
                    _isTimerPaused.value = false
                    withContext(Dispatchers.Main) {
                        SoundVibrateHelper.playTimerDoneSound(context)
                        SoundVibrateHelper.vibrateDevice(context, 1500L)
                    }
                    break
                }
                delay(200L)
            }
        }
    }

    fun pauseTimer() {
        _isTimerRunning.value = false
        _isTimerPaused.value = true
        timerJob?.cancel()
    }

    fun resetTimer() {
        _isTimerRunning.value = false
        _isTimerPaused.value = false
        timerJob?.cancel()
        _timerRemainingSeconds.value = _timerTotalSeconds.value
    }

    // --- Focus (Pomodoro) Engine ---
    fun selectFocusMode(mode: FocusMode) {
        _focusMode.value = mode
        _focusRemainingSeconds.value = mode.defaultMinutes * 60
        _isFocusRunning.value = false
        focusJob?.cancel()
    }

    fun startFocusSession() {
        if (_focusRemainingSeconds.value <= 0) return
        _isFocusRunning.value = true
        focusEndTimestamp = System.currentTimeMillis() + (_focusRemainingSeconds.value * 1000L)

        focusJob?.cancel()
        focusJob = viewModelScope.launch(Dispatchers.Default) {
            while (isActive && _isFocusRunning.value) {
                val remainingMillis = focusEndTimestamp - System.currentTimeMillis()
                val remSeconds = Math.max(0, Math.ceil(remainingMillis / 1000.0).toInt())
                _focusRemainingSeconds.value = remSeconds

                if (remSeconds <= 0) {
                    _isFocusRunning.value = false
                    // Record completed session in database
                    val currentMode = _focusMode.value
                    if (currentMode == FocusMode.WORK) {
                        database.focusDao().insertSession(
                            FocusSession(
                                durationMinutes = currentMode.defaultMinutes,
                                mode = currentMode.name,
                                isCompleted = true
                            )
                        )
                        _focusCycle.value = _focusCycle.value + 1
                    }

                    withContext(Dispatchers.Main) {
                        SoundVibrateHelper.playTimerDoneSound(context)
                        SoundVibrateHelper.vibrateDevice(context, 2000L)
                    }

                    // Auto switch between Focus and Break
                    if (currentMode == FocusMode.WORK) {
                        selectFocusMode(if (_focusCycle.value % 4 == 0) FocusMode.LONG_BREAK else FocusMode.SHORT_BREAK)
                    } else {
                        selectFocusMode(FocusMode.WORK)
                    }
                    break
                }
                delay(250L)
            }
        }
    }

    fun pauseFocusSession() {
        _isFocusRunning.value = false
        focusJob?.cancel()
    }

    fun skipFocusSession() {
        pauseFocusSession()
        val next = when (_focusMode.value) {
            FocusMode.WORK -> FocusMode.SHORT_BREAK
            FocusMode.SHORT_BREAK -> FocusMode.WORK
            FocusMode.LONG_BREAK -> FocusMode.WORK
        }
        selectFocusMode(next)
    }

    fun resetFocusSession() {
        pauseFocusSession()
        _focusRemainingSeconds.value = _focusMode.value.defaultMinutes * 60
    }

    // --- World Clock ---
    fun addWorldCity(city: WorldClockCity) {
        viewModelScope.launch(Dispatchers.IO) {
            database.worldClockDao().insertCity(city)
        }
    }

    fun removeWorldCity(city: WorldClockCity) {
        viewModelScope.launch(Dispatchers.IO) {
            database.worldClockDao().deleteCity(city)
        }
    }

    // --- Weather ---
    fun refreshWeather(city: String = _weatherState.value.cityName, lat: Double = _weatherState.value.latitude, lon: Double = _weatherState.value.longitude) {
        viewModelScope.launch {
            _weatherState.value = _weatherState.value.copy(isLoading = true, errorMessage = null)
            val result = WeatherClient.fetchWeather(city, lat, lon)
            result.onSuccess { state ->
                _weatherState.value = state.copy(
                    isCelsius = _weatherState.value.isCelsius
                )
            }.onFailure { err ->
                _weatherState.value = _weatherState.value.copy(
                    isLoading = false,
                    errorMessage = err.localizedMessage ?: "Unable to connect to weather service"
                )
            }
        }
    }

    fun toggleWeatherUnit() {
        _weatherState.value = _weatherState.value.copy(isCelsius = !_weatherState.value.isCelsius)
    }

    fun searchAndSelectWeatherCity(cityName: String, lat: Double, lon: Double) {
        refreshWeather(cityName, lat, lon)
    }

    // --- FX AI Assistant ---
    fun sendAiPrompt(promptText: String) {
        val userMsg = AiMessage(text = promptText, isUser = true)
        _aiMessages.value = _aiMessages.value + userMsg
        _isAiLoading.value = true

        viewModelScope.launch {
            val response = FxAiAssistantService.processQuery(promptText)
            _aiMessages.value = _aiMessages.value + response
            _isAiLoading.value = false
        }
    }

    fun executeAiAction(action: AiClockAction, messageId: String) {
        when (action) {
            is AiClockAction.CreateAlarm -> {
                saveAlarm(
                    AlarmItem(
                        hour = action.hour,
                        minute = action.minute,
                        label = action.label,
                        isEnabled = true
                    )
                )
            }
            is AiClockAction.StartTimer -> {
                setTimerDuration(0, action.minutes, action.seconds)
                startTimer()
            }
            is AiClockAction.StartFocus -> {
                selectFocusMode(FocusMode.WORK)
                _focusRemainingSeconds.value = action.minutes * 60
                startFocusSession()
            }
            is AiClockAction.ChangeTheme -> {
                applyStudioDraft(CustomThemeConfig.fromPreset(action.preset))
            }
            is AiClockAction.SetTimeFormat -> {
                preferences.set24HourFormat(action.is24Hour)
            }
            is AiClockAction.CheckWorldTime -> {
                // Already answered in chat
            }
            AiClockAction.None -> Unit
        }

        // Mark action executed in message
        _aiMessages.value = _aiMessages.value.map {
            if (it.id == messageId) it.copy(actionExecuted = true) else it
        }
    }

    // --- Customization Studio ---
    fun initStudioDraft() {
        _studioDraft.value = preferences.themeConfigFlow.value
        undoStack.clear()
        redoStack.clear()
    }

    fun updateStudioDraft(newConfig: CustomThemeConfig) {
        undoStack.add(_studioDraft.value)
        redoStack.clear()
        _studioDraft.value = newConfig
    }

    fun undoStudioDraft() {
        if (undoStack.isNotEmpty()) {
            val prev = undoStack.removeAt(undoStack.lastIndex)
            redoStack.add(_studioDraft.value)
            _studioDraft.value = prev
        }
    }

    fun redoStudioDraft() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.removeAt(redoStack.lastIndex)
            undoStack.add(_studioDraft.value)
            _studioDraft.value = next
        }
    }

    fun applyStudioDraft(config: CustomThemeConfig = _studioDraft.value) {
        preferences.updateThemeConfig(config)
        _studioDraft.value = config
    }

    fun resetStudioToDefaults() {
        val defaultAbyss = CustomThemeConfig.fromPreset(ThemePreset.ABYSS)
        updateStudioDraft(defaultAbyss)
        preferences.updateThemeConfig(defaultAbyss)
    }

    fun completeOnboarding() {
        preferences.setOnboardingCompleted(true)
    }
}
