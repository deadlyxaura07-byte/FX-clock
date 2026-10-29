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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClockFaceType
import com.example.ui.components.FxAnalogClock
import com.example.ui.components.FxDigitalClock
import com.example.ui.theme.LocalCustomThemeConfig
import com.example.ui.theme.getClockFontFamily
import com.example.ui.viewmodel.ClockAppViewModel

@Composable
fun ClockScreen(
    viewModel: ClockAppViewModel,
    modifier: Modifier = Modifier
) {
    val themeConfig = LocalCustomThemeConfig.current
    val fontFamily = getClockFontFamily(themeConfig.fontFamily)
    val liveTime by viewModel.liveTime.collectAsState()
    val is24Hour by viewModel.is24Hour.collectAsState()

    var activeFace by remember { mutableStateOf(themeConfig.clockFaceType) }
    var isZenMode by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(themeConfig.backgroundColor)
            .statusBarsPadding()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header (hidden in Zen mode)
        if (!isZenMode) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TIMEPIECE",
                        color = themeConfig.textPrimaryColor,
                        fontSize = 20.sp,
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "ACTUAL SYSTEM TIME",
                        color = themeConfig.textMutedColor,
                        fontSize = 11.sp,
                        fontFamily = fontFamily,
                        letterSpacing = 1.sp
                    )
                }

                // Zen Fullscreen toggle
                IconButton(
                    onClick = { isZenMode = !isZenMode },
                    modifier = Modifier
                        .size(42.dp)
                        .background(themeConfig.surfaceElevatedColor, CircleShape)
                        .border(1.dp, themeConfig.borderColor, CircleShape)
                        .testTag("clock_zen_mode_button")
                ) {
                    Icon(
                        imageVector = if (isZenMode) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                        contentDescription = "Zen Mode",
                        tint = themeConfig.accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        } else {
            // Minimal escape button in zen mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = { isZenMode = false },
                    modifier = Modifier
                        .size(36.dp)
                        .background(themeConfig.surfaceElevatedColor.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.FullscreenExit,
                        contentDescription = "Exit Zen Mode",
                        tint = themeConfig.textMutedColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Center Timepiece Display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            when (activeFace) {
                ClockFaceType.DIGITAL -> {
                    FxDigitalClock(
                        timeState = liveTime,
                        is24Hour = is24Hour,
                        showSeconds = themeConfig.showSeconds,
                        showAmPm = themeConfig.showAmPm,
                        showDate = true
                    )
                }
                ClockFaceType.ANALOG -> {
                    FxAnalogClock(
                        hour = liveTime.hour12,
                        minute = liveTime.minute,
                        second = liveTime.second,
                        millis = liveTime.millis,
                        dialSize = 280.dp,
                        showSeconds = themeConfig.showSeconds
                    )
                }
                ClockFaceType.HYBRID -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        FxAnalogClock(
                            hour = liveTime.hour12,
                            minute = liveTime.minute,
                            second = liveTime.second,
                            millis = liveTime.millis,
                            dialSize = 220.dp,
                            showSeconds = themeConfig.showSeconds
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        FxDigitalClock(
                            timeState = liveTime,
                            is24Hour = is24Hour,
                            showSeconds = true,
                            showAmPm = true,
                            showDate = false
                        )
                    }
                }
            }
        }

        // Bottom Controls Bar (hidden in Zen mode)
        if (!isZenMode) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Face Selector Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    FilterChip(
                        selected = activeFace == ClockFaceType.DIGITAL,
                        onClick = { activeFace = ClockFaceType.DIGITAL },
                        label = { Text("Digital", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Numbers, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = themeConfig.accentColor,
                            selectedLabelColor = androidx.compose.ui.graphics.Color.Black,
                            selectedLeadingIconColor = androidx.compose.ui.graphics.Color.Black
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.size(8.dp))

                    FilterChip(
                        selected = activeFace == ClockFaceType.ANALOG,
                        onClick = { activeFace = ClockFaceType.ANALOG },
                        label = { Text("Analog", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.AvTimer, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = themeConfig.accentColor,
                            selectedLabelColor = androidx.compose.ui.graphics.Color.Black,
                            selectedLeadingIconColor = androidx.compose.ui.graphics.Color.Black
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.size(8.dp))

                    FilterChip(
                        selected = activeFace == ClockFaceType.HYBRID,
                        onClick = { activeFace = ClockFaceType.HYBRID },
                        label = { Text("Hybrid", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.ViewCarousel, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = themeConfig.accentColor,
                            selectedLabelColor = androidx.compose.ui.graphics.Color.Black,
                            selectedLeadingIconColor = androidx.compose.ui.graphics.Color.Black
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Time Format Toggle Pill
                Row(
                    modifier = Modifier
                        .background(themeConfig.surfaceElevatedColor, RoundedCornerShape(20.dp))
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (!is24Hour) themeConfig.accentColor else androidx.compose.ui.graphics.Color.Transparent,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .clickable { viewModel.preferences.set24HourFormat(false) }
                    ) {
                        Text(
                            text = "12-HOUR",
                            color = if (!is24Hour) androidx.compose.ui.graphics.Color.Black else themeConfig.textMutedColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(
                                color = if (is24Hour) themeConfig.accentColor else androidx.compose.ui.graphics.Color.Transparent,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .clickable { viewModel.preferences.set24HourFormat(true) }
                    ) {
                        Text(
                            text = "24-HOUR",
                            color = if (is24Hour) androidx.compose.ui.graphics.Color.Black else themeConfig.textMutedColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
