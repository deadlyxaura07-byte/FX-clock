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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalCustomThemeConfig
import com.example.ui.theme.getClockFontFamily
import com.example.ui.viewmodel.LiveTimeState

@Composable
fun FxDigitalClock(
    timeState: LiveTimeState,
    modifier: Modifier = Modifier,
    is24Hour: Boolean = false,
    showSeconds: Boolean = true,
    showAmPm: Boolean = true,
    showDate: Boolean = true
) {
    val themeConfig = LocalCustomThemeConfig.current
    val fontFamily = getClockFontFamily(themeConfig.fontFamily)
    val scale = themeConfig.clockDigitSizeScale

    val infiniteTransition = rememberInfiniteTransition(label = "colon_blink")
    val colonAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (themeConfig.reducedMotion) 1f else 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "colon_alpha"
    )

    val timeText = if (is24Hour) timeState.formattedTime24 else timeState.formattedTime12
    val parts = timeText.split(":")
    val hours = parts.getOrNull(0) ?: "12"
    val minutes = parts.getOrNull(1) ?: "00"

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Date pill if enabled
        if (showDate && themeConfig.showDate) {
            Box(
                modifier = Modifier
                    .background(
                        color = themeConfig.surfaceElevatedColor,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = themeConfig.borderColor.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = timeState.formattedDate.uppercase(),
                    color = themeConfig.textMutedColor,
                    fontSize = 11.sp,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.5.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Main digits row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Hours
            Text(
                text = hours,
                color = themeConfig.textPrimaryColor,
                fontSize = (72 * scale).sp,
                fontFamily = fontFamily,
                fontWeight = FontWeight.Light,
                letterSpacing = (-2).sp
            )

            // Colon separator
            Text(
                text = ":",
                color = themeConfig.accentColor,
                fontSize = (68 * scale).sp,
                fontFamily = fontFamily,
                fontWeight = FontWeight.ExtraLight,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .alpha(colonAlpha)
            )

            // Minutes
            Text(
                text = minutes,
                color = themeConfig.textPrimaryColor,
                fontSize = (72 * scale).sp,
                fontFamily = fontFamily,
                fontWeight = FontWeight.Light,
                letterSpacing = (-2).sp
            )

            // Seconds & AM/PM stacked column
            if ((showSeconds && themeConfig.showSeconds) || (showAmPm && !is24Hour && themeConfig.showAmPm)) {
                Spacer(modifier = Modifier.width(10.dp))
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    if (showAmPm && !is24Hour && themeConfig.showAmPm) {
                        Text(
                            text = if (timeState.isPm) "PM" else "AM",
                            color = themeConfig.accentColor,
                            fontSize = (13 * scale).sp,
                            fontFamily = fontFamily,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    if (showSeconds && themeConfig.showSeconds) {
                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .background(
                                    color = themeConfig.surfaceElevatedColor,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = timeState.formattedSeconds,
                                color = themeConfig.textPrimaryColor,
                                fontSize = (12 * scale).sp,
                                fontFamily = fontFamily,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
