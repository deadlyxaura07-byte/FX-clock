package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FocusMode
import com.example.ui.components.FxProgressRing
import com.example.ui.theme.LocalCustomThemeConfig
import com.example.ui.theme.getClockFontFamily
import com.example.ui.viewmodel.ClockAppViewModel

@Composable
fun FocusScreen(
    viewModel: ClockAppViewModel,
    modifier: Modifier = Modifier
) {
    val themeConfig = LocalCustomThemeConfig.current
    val fontFamily = getClockFontFamily(themeConfig.fontFamily)

    val currentMode by viewModel.focusMode.collectAsState()
    val remainingSeconds by viewModel.focusRemainingSeconds.collectAsState()
    val isRunning by viewModel.isFocusRunning.collectAsState()
    val cycle by viewModel.focusCycle.collectAsState()
    val completedSessions by viewModel.completedFocusSessions.collectAsState()
    val totalMinutes by viewModel.totalFocusMinutes.collectAsState()

    val totalSec = currentMode.defaultMinutes * 60
    val progress = if (totalSec > 0) (totalSec - remainingSeconds).toFloat() / totalSec else 0f

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(themeConfig.backgroundColor)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "FOCUS ENGINE",
                    color = themeConfig.textPrimaryColor,
                    fontSize = 20.sp,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "POMODORO & DEEP WORK",
                    color = themeConfig.textMutedColor,
                    fontSize = 11.sp,
                    fontFamily = fontFamily,
                    letterSpacing = 1.sp
                )
            }

            // Cycle Indicator Badge
            Box(
                modifier = Modifier
                    .background(themeConfig.surfaceElevatedColor, RoundedCornerShape(16.dp))
                    .border(1.dp, themeConfig.borderColor, RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "CYCLE $cycle OF 4",
                    color = themeConfig.accentColor,
                    fontSize = 11.sp,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Mode Switcher Pills
        Row(
            modifier = Modifier
                .background(themeConfig.surfaceColor, RoundedCornerShape(20.dp))
                .border(1.dp, themeConfig.borderColor, RoundedCornerShape(20.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            FocusMode.values().forEach { mode ->
                val isSelected = mode == currentMode
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isSelected) themeConfig.accentColor else Color.Transparent,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { viewModel.selectFocusMode(mode) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("focus_mode_${mode.name}")
                ) {
                    Text(
                        text = mode.title,
                        color = if (isSelected) Color.Black else themeConfig.textMutedColor,
                        fontSize = 12.sp,
                        fontFamily = fontFamily,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Large Progress Ring
        FxProgressRing(
            progress = progress,
            ringSize = 270.dp,
            strokeWidth = 12.dp,
            activeColor = themeConfig.accentColor
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = timeFormatted,
                    color = themeConfig.textPrimaryColor,
                    fontSize = 58.sp,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-1.5).sp
                )
                Text(
                    text = currentMode.title.uppercase(),
                    color = themeConfig.accentColor,
                    fontSize = 12.sp,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Control Buttons (Start/Pause, Skip, Reset)
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset
            IconButton(
                onClick = { viewModel.resetFocusSession() },
                modifier = Modifier
                    .size(52.dp)
                    .background(themeConfig.surfaceElevatedColor, CircleShape)
                    .border(1.dp, themeConfig.borderColor, CircleShape)
                    .testTag("focus_reset_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Focus",
                    tint = themeConfig.textMutedColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Main Play/Pause Button
            Button(
                onClick = {
                    if (isRunning) viewModel.pauseFocusSession() else viewModel.startFocusSession()
                },
                modifier = Modifier
                    .height(60.dp)
                    .width(160.dp)
                    .testTag("focus_play_pause_button"),
                shape = RoundedCornerShape(themeConfig.cornerRadiusDp.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeConfig.accentColor,
                    contentColor = Color.Black
                )
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) "Pause" else "Start",
                    modifier = Modifier.size(24.dp)
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

            // Skip
            IconButton(
                onClick = { viewModel.skipFocusSession() },
                modifier = Modifier
                    .size(52.dp)
                    .background(themeConfig.surfaceElevatedColor, CircleShape)
                    .border(1.dp, themeConfig.borderColor, CircleShape)
                    .testTag("focus_skip_button")
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Skip Session",
                    tint = themeConfig.textMutedColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Productivity Statistics Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(themeConfig.cornerRadiusDp.dp),
            colors = CardDefaults.cardColors(containerColor = themeConfig.surfaceColor),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    listOf(themeConfig.borderColor, themeConfig.borderColor)
                )
            )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "FOCUS METRICS",
                    color = themeConfig.textMutedColor,
                    fontSize = 11.sp,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$completedSessions",
                            color = themeConfig.textPrimaryColor,
                            fontSize = 26.sp,
                            fontFamily = fontFamily,
                            fontWeight = FontWeight.Light
                        )
                        Text(
                            text = "Sessions Done",
                            color = themeConfig.textMutedColor,
                            fontSize = 12.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(themeConfig.borderColor)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${totalMinutes ?: 0}m",
                            color = themeConfig.accentColor,
                            fontSize = 26.sp,
                            fontFamily = fontFamily,
                            fontWeight = FontWeight.Light
                        )
                        Text(
                            text = "Deep Focus Time",
                            color = themeConfig.textMutedColor,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
