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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CustomThemeConfig
import com.example.data.model.ThemePreset
import com.example.ui.theme.LocalCustomThemeConfig
import com.example.ui.theme.getClockFontFamily
import com.example.ui.viewmodel.ClockAppViewModel

@Composable
fun OnboardingScreen(
    viewModel: ClockAppViewModel,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeConfig = LocalCustomThemeConfig.current
    val fontFamily = getClockFontFamily(themeConfig.fontFamily)

    var selectedPreset by remember { mutableStateOf(ThemePreset.ABYSS) }
    var is24Hour by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(themeConfig.backgroundColor)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Skip Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = {
                viewModel.completeOnboarding()
                onFinish()
            }) {
                Text("Skip Setup", color = themeConfig.textMutedColor, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hero Intro
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(themeConfig.surfaceElevatedColor, CircleShape)
                    .border(2.dp, themeConfig.accentColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = themeConfig.accentColor,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "FAKEEXX SMART CLOCK",
                color = themeConfig.textPrimaryColor,
                fontSize = 24.sp,
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "THE FUTURE STARTS HERE",
                color = themeConfig.accentColor,
                fontSize = 12.sp,
                fontFamily = fontFamily,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "An ultra-premium, minimal timekeeping and productivity suite crafted with absolute restraint and architectural precision.",
                color = themeConfig.textMutedColor,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
                fontFamily = fontFamily
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Preference Selection
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "CHOOSE INITIAL PALETTE",
                color = themeConfig.textMutedColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(ThemePreset.ABYSS, ThemePreset.OBSIDIAN, ThemePreset.IVORY).forEach { preset ->
                    val isSel = selectedPreset == preset
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = if (isSel) themeConfig.surfaceElevatedColor else themeConfig.surfaceColor,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = if (isSel) 2.dp else 1.dp,
                                color = if (isSel) themeConfig.accentColor else themeConfig.borderColor,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedPreset = preset }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = preset.title,
                            color = if (isSel) themeConfig.accentColor else themeConfig.textPrimaryColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Time Format
            Text(
                text = "TIME FORMAT",
                color = themeConfig.textMutedColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = if (!is24Hour) themeConfig.accentColor else themeConfig.surfaceColor,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { is24Hour = false }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "12-HOUR (AM/PM)",
                        color = if (!is24Hour) Color.Black else themeConfig.textPrimaryColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = if (is24Hour) themeConfig.accentColor else themeConfig.surfaceColor,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { is24Hour = true }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "24-HOUR (MILITARY)",
                        color = if (is24Hour) Color.Black else themeConfig.textPrimaryColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Explaining Permissions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(themeConfig.surfaceColor, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = themeConfig.accentColor, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Requires alarm and notification access to sound alerts and keep accurate background timers.",
                    color = themeConfig.textMutedColor,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Enter Button
        Button(
            onClick = {
                viewModel.preferences.updateThemeConfig(CustomThemeConfig.fromPreset(selectedPreset))
                viewModel.preferences.set24HourFormat(is24Hour)
                viewModel.completeOnboarding()
                onFinish()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("onboarding_enter_button"),
            shape = RoundedCornerShape(themeConfig.cornerRadiusDp.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = themeConfig.accentColor,
                contentColor = Color.Black
            )
        ) {
            Text(
                text = "ENTER FAKEEXX",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}
