package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FxProgressRing
import com.example.ui.theme.LocalCustomThemeConfig
import com.example.ui.theme.getClockFontFamily
import com.example.ui.viewmodel.ClockAppViewModel

@Composable
fun TimerScreen(
    viewModel: ClockAppViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeConfig = LocalCustomThemeConfig.current
    val fontFamily = getClockFontFamily(themeConfig.fontFamily)

    val remainingSeconds by viewModel.timerRemainingSeconds.collectAsState()
    val totalSeconds by viewModel.timerTotalSeconds.collectAsState()
    val isRunning by viewModel.isTimerRunning.collectAsState()
    val isPaused by viewModel.isTimerPaused.collectAsState()

    var pickerHours by remember { mutableStateOf(0) }
    var pickerMinutes by remember { mutableStateOf(5) }
    var pickerSeconds by remember { mutableStateOf(0) }

    val progress = if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds else 0f

    val hours = remainingSeconds / 3600
    val minutes = (remainingSeconds % 3600) / 60
    val seconds = remainingSeconds % 60
    val timeFormatted = if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(themeConfig.backgroundColor)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(themeConfig.surfaceElevatedColor, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = themeConfig.textPrimaryColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "COUNTDOWN TIMER",
                    color = themeConfig.textPrimaryColor,
                    fontSize = 18.sp,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "ACCURATE TIMESTAMP MATH",
                    color = themeConfig.textMutedColor,
                    fontSize = 10.sp,
                    fontFamily = fontFamily,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Big Progress Ring with Time
        FxProgressRing(
            progress = progress,
            ringSize = 270.dp,
            strokeWidth = 10.dp,
            activeColor = themeConfig.accentColor
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = timeFormatted,
                    color = themeConfig.textPrimaryColor,
                    fontSize = if (hours > 0) 42.sp else 54.sp,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-1).sp
                )
                Text(
                    text = if (isRunning) "RUNNING" else if (isPaused) "PAUSED" else "READY",
                    color = themeConfig.accentColor,
                    fontSize = 12.sp,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Quick Preset Adders
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            listOf("+1m" to 60, "+5m" to 300, "+15m" to 900, "+30m" to 1800).forEach { (label, sec) ->
                SuggestionChip(
                    onClick = { viewModel.addTimerSeconds(sec) },
                    label = { Text(label, fontSize = 12.sp, color = themeConfig.textPrimaryColor) },
                    colors = SuggestionChipDefaults.suggestionChipColors(containerColor = themeConfig.surfaceElevatedColor),
                    border = SuggestionChipDefaults.suggestionChipBorder(enabled = true, borderColor = themeConfig.borderColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Controls (Reset / Start / Pause)
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.resetTimer() },
                modifier = Modifier
                    .size(52.dp)
                    .background(themeConfig.surfaceElevatedColor, CircleShape)
                    .border(1.dp, themeConfig.borderColor, CircleShape)
                    .testTag("timer_reset_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Timer",
                    tint = themeConfig.textMutedColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Button(
                onClick = {
                    if (isRunning) viewModel.pauseTimer() else viewModel.startTimer()
                },
                modifier = Modifier
                    .height(58.dp)
                    .width(160.dp)
                    .testTag("timer_play_pause_button"),
                shape = RoundedCornerShape(themeConfig.cornerRadiusDp.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeConfig.accentColor,
                    contentColor = Color.Black
                )
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) "Pause" else "Start",
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isRunning) "PAUSE" else "START",
                    fontSize = 15.sp,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Custom Duration Setup Box (when not running)
        if (!isRunning && !isPaused) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(themeConfig.surfaceColor, RoundedCornerShape(themeConfig.cornerRadiusDp.dp))
                    .border(1.dp, themeConfig.borderColor, RoundedCornerShape(themeConfig.cornerRadiusDp.dp))
                    .padding(18.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "SET CUSTOM TIME",
                        color = themeConfig.textMutedColor,
                        fontSize = 11.sp,
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mins
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("MIN", color = themeConfig.textMutedColor, fontSize = 10.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { if (pickerMinutes > 0) pickerMinutes-- }) {
                                    Text("-", color = themeConfig.accentColor, fontSize = 22.sp)
                                }
                                Text("$pickerMinutes", color = themeConfig.textPrimaryColor, fontSize = 24.sp, fontWeight = FontWeight.Medium)
                                IconButton(onClick = { if (pickerMinutes < 99) pickerMinutes++ }) {
                                    Text("+", color = themeConfig.accentColor, fontSize = 22.sp)
                                }
                            }
                        }

                        // Secs
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("SEC", color = themeConfig.textMutedColor, fontSize = 10.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { if (pickerSeconds >= 5) pickerSeconds -= 5 }) {
                                    Text("-", color = themeConfig.accentColor, fontSize = 22.sp)
                                }
                                Text(String.format("%02d", pickerSeconds), color = themeConfig.textPrimaryColor, fontSize = 24.sp, fontWeight = FontWeight.Medium)
                                IconButton(onClick = { if (pickerSeconds <= 55) pickerSeconds += 5 }) {
                                    Text("+", color = themeConfig.accentColor, fontSize = 22.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.setTimerDuration(pickerHours, pickerMinutes, pickerSeconds) },
                        colors = ButtonDefaults.buttonColors(containerColor = themeConfig.surfaceElevatedColor, contentColor = themeConfig.accentColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Apply Duration", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
