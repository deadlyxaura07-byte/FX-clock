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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClockFaceType
import com.example.ui.components.FxAnalogClock
import com.example.ui.components.FxDigitalClock
import com.example.ui.components.NavigationTab
import com.example.ui.theme.LocalCustomThemeConfig
import com.example.ui.theme.getClockFontFamily
import com.example.ui.viewmodel.ClockAppViewModel

@Composable
fun HomeScreen(
    viewModel: ClockAppViewModel,
    onNavigateToTab: (NavigationTab) -> Unit,
    onOpenTool: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val themeConfig = LocalCustomThemeConfig.current
    val fontFamily = getClockFontFamily(themeConfig.fontFamily)
    val liveTime by viewModel.liveTime.collectAsState()
    val is24Hour by viewModel.is24Hour.collectAsState()
    val alarms by viewModel.alarms.collectAsState()
    val weather by viewModel.weatherState.collectAsState()
    val nextAlarm = alarms.firstOrNull { it.isEnabled }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(themeConfig.backgroundColor)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Brand Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "FAKEEXX",
                    color = themeConfig.textPrimaryColor,
                    fontSize = 17.sp,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "THE FUTURE STARTS HERE",
                    color = themeConfig.accentColor,
                    fontSize = 10.sp,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.2.sp
                )
            }

            // Quick Customization Studio button
            IconButton(
                onClick = { onOpenTool("studio") },
                modifier = Modifier
                    .size(44.dp)
                    .background(themeConfig.surfaceElevatedColor, CircleShape)
                    .border(1.dp, themeConfig.borderColor, CircleShape)
                    .testTag("home_studio_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Customization Studio",
                    tint = themeConfig.accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Hero Live Clock Widget
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = themeConfig.surfaceColor,
                    shape = RoundedCornerShape(themeConfig.cornerRadiusDp.dp)
                )
                .border(
                    width = themeConfig.borderThicknessDp.dp,
                    color = themeConfig.borderColor,
                    shape = RoundedCornerShape(themeConfig.cornerRadiusDp.dp)
                )
                .clickable { onNavigateToTab(NavigationTab.CLOCK) }
                .padding(vertical = 32.dp, horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            when (themeConfig.clockFaceType) {
                ClockFaceType.DIGITAL -> {
                    FxDigitalClock(
                        timeState = liveTime,
                        is24Hour = is24Hour,
                        showSeconds = themeConfig.showSeconds,
                        showAmPm = themeConfig.showAmPm,
                        showDate = themeConfig.showDate
                    )
                }
                ClockFaceType.ANALOG -> {
                    FxAnalogClock(
                        hour = liveTime.hour12,
                        minute = liveTime.minute,
                        second = liveTime.second,
                        millis = liveTime.millis,
                        dialSize = 230.dp,
                        showSeconds = themeConfig.showSeconds
                    )
                }
                ClockFaceType.HYBRID -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        FxAnalogClock(
                            hour = liveTime.hour12,
                            minute = liveTime.minute,
                            second = liveTime.second,
                            millis = liveTime.millis,
                            dialSize = 180.dp,
                            showSeconds = themeConfig.showSeconds
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        FxDigitalClock(
                            timeState = liveTime,
                            is24Hour = is24Hour,
                            showSeconds = false,
                            showAmPm = true,
                            showDate = false
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Next Alarm Widget
        if (themeConfig.showHomeNextAlarm) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = themeConfig.surfaceColor,
                        shape = RoundedCornerShape(themeConfig.cornerRadiusDp.dp)
                    )
                    .border(
                        width = themeConfig.borderThicknessDp.dp,
                        color = themeConfig.borderColor,
                        shape = RoundedCornerShape(themeConfig.cornerRadiusDp.dp)
                    )
                    .clickable { onNavigateToTab(NavigationTab.ALARMS) }
                    .padding(18.dp)
                    .testTag("home_next_alarm_widget")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(themeConfig.surfaceElevatedColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = "Next Alarm",
                                tint = if (nextAlarm != null) themeConfig.accentColor else themeConfig.textMutedColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "NEXT ALARM",
                                color = themeConfig.textMutedColor,
                                fontSize = 11.sp,
                                fontFamily = fontFamily,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = nextAlarm?.let { "${it.formattedTime(is24Hour)} ${it.amPm()} • ${it.label}" } ?: "No active alarms",
                                color = themeConfig.textPrimaryColor,
                                fontSize = 15.sp,
                                fontFamily = fontFamily,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (nextAlarm != null) {
                        Box(
                            modifier = Modifier
                                .background(themeConfig.accentColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = nextAlarm.getRemainingTimeDescription(),
                                color = themeConfig.accentColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Live Real Weather Widget
        if (themeConfig.showHomeWeather) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = themeConfig.surfaceColor,
                        shape = RoundedCornerShape(themeConfig.cornerRadiusDp.dp)
                    )
                    .border(
                        width = themeConfig.borderThicknessDp.dp,
                        color = themeConfig.borderColor,
                        shape = RoundedCornerShape(themeConfig.cornerRadiusDp.dp)
                    )
                    .clickable { onOpenTool("weather") }
                    .padding(18.dp)
                    .testTag("home_weather_widget")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(themeConfig.surfaceElevatedColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (weather.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = themeConfig.accentColor,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.WbSunny,
                                    contentDescription = "Weather Condition",
                                    tint = themeConfig.accentColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = weather.cityName.uppercase(),
                                color = themeConfig.textMutedColor,
                                fontSize = 11.sp,
                                fontFamily = fontFamily,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${weather.displayTemperature()} • ${weather.conditionText}",
                                color = themeConfig.textPrimaryColor,
                                fontSize = 15.sp,
                                fontFamily = fontFamily,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.refreshWeather() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Weather",
                            tint = themeConfig.textMutedColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Quick Actions Grid
        if (themeConfig.showHomeQuickActions) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "QUICK ACTIONS",
                    color = themeConfig.textMutedColor,
                    fontSize = 12.sp,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.2.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Focus Sprint
                QuickActionCard(
                    title = "Focus 25m",
                    subtitle = "Pomodoro",
                    icon = Icons.Default.Psychology,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.selectFocusMode(com.example.data.model.FocusMode.WORK)
                        onNavigateToTab(NavigationTab.FOCUS)
                    }
                )

                // Timer 5m
                QuickActionCard(
                    title = "Timer 5m",
                    subtitle = "Quick Sprint",
                    icon = Icons.Default.HourglassTop,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.setTimerDuration(0, 5, 0)
                        viewModel.startTimer()
                        onOpenTool("timer")
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Stopwatch
                QuickActionCard(
                    title = "Stopwatch",
                    subtitle = "Precision Lap",
                    icon = Icons.Default.Timer,
                    modifier = Modifier.weight(1f),
                    onClick = { onOpenTool("stopwatch") }
                )

                // AI Assistant
                QuickActionCard(
                    title = "FX Assistant",
                    subtitle = "Voice & Text",
                    icon = Icons.Default.Bolt,
                    modifier = Modifier.weight(1f),
                    onClick = { onOpenTool("ai") }
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeConfig = LocalCustomThemeConfig.current
    val fontFamily = getClockFontFamily(themeConfig.fontFamily)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(themeConfig.cornerRadiusDp.dp))
            .background(themeConfig.surfaceColor)
            .border(
                width = themeConfig.borderThicknessDp.dp,
                color = themeConfig.borderColor,
                shape = RoundedCornerShape(themeConfig.cornerRadiusDp.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(themeConfig.surfaceElevatedColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = themeConfig.accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                color = themeConfig.textPrimaryColor,
                fontSize = 14.sp,
                fontFamily = fontFamily,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = themeConfig.textMutedColor,
                fontSize = 11.sp,
                fontFamily = fontFamily
            )
        }
    }
}
