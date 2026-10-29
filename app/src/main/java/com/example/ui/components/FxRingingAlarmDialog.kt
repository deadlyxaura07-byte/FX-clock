package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.receiver.ActiveAlarmState
import com.example.ui.theme.LocalCustomThemeConfig
import com.example.ui.theme.getClockFontFamily

@Composable
fun FxRingingAlarmDialog(
    alarmState: ActiveAlarmState,
    onStop: () -> Unit,
    onSnooze: () -> Unit
) {
    val themeConfig = LocalCustomThemeConfig.current
    val fontFamily = getClockFontFamily(themeConfig.fontFamily)

    val transition = rememberInfiniteTransition(label = "pulse_alarm")
    val pulseScale by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Dialog(
        onDismissRequest = { /* Modal must be dismissed via Stop or Snooze */ },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            themeConfig.backgroundColor,
                            themeConfig.surfaceColor,
                            themeConfig.backgroundColor
                        )
                    )
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Pulsing Icon
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .scale(pulseScale)
                        .background(
                            color = themeConfig.accentColor.copy(alpha = 0.15f),
                            shape = CircleShape
                        )
                        .border(
                            width = 2.dp,
                            color = themeConfig.accentColor,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = "Ringing Alarm",
                        tint = themeConfig.accentColor,
                        modifier = Modifier.size(54.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Brand badge
                Text(
                    text = "FAKEEXX ALARM",
                    color = themeConfig.accentColor,
                    fontSize = 12.sp,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Time
                Text(
                    text = alarmState.formattedTime,
                    color = themeConfig.textPrimaryColor,
                    fontSize = 64.sp,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-1.5).sp
                )

                // Label
                Text(
                    text = alarmState.label,
                    color = themeConfig.textMutedColor,
                    fontSize = 18.sp,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Actions: Stop & Snooze
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Snooze Button
                    OutlinedButton(
                        onClick = onSnooze,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .testTag("alarm_snooze_button"),
                        shape = RoundedCornerShape(themeConfig.cornerRadiusDp.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = themeConfig.textPrimaryColor
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.linearGradient(
                                listOf(themeConfig.borderColor, themeConfig.accentColor.copy(alpha = 0.5f))
                            )
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Snooze,
                            contentDescription = "Snooze",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Snooze (5m)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Stop Button
                    Button(
                        onClick = onStop,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .testTag("alarm_stop_button"),
                        shape = RoundedCornerShape(themeConfig.cornerRadiusDp.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = themeConfig.accentColor,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Stop",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Stop",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
