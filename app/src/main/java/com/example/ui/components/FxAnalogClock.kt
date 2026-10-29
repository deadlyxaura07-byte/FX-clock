package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalCustomThemeConfig
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FxAnalogClock(
    hour: Int,
    minute: Int,
    second: Int,
    millis: Int = 0,
    modifier: Modifier = Modifier,
    dialSize: Dp = 260.dp,
    showSeconds: Boolean = true
) {
    val themeConfig = LocalCustomThemeConfig.current
    val dialColor = themeConfig.surfaceColor
    val borderColor = themeConfig.borderColor
    val handsColor = themeConfig.clockHandsColor
    val accentColor = themeConfig.accentColor
    val textColor = themeConfig.textPrimaryColor
    val mutedColor = themeConfig.textMutedColor
    val showMarkers = themeConfig.analogShowHourMarkers
    val showNumerals = themeConfig.analogShowNumerals

    Box(
        modifier = modifier.size(dialSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().aspectRatio(1f)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = (size.minDimension / 2f) * 0.92f

            // Outer ring
            drawCircle(
                color = dialColor,
                radius = radius,
                center = center
            )
            drawCircle(
                color = borderColor,
                radius = radius,
                center = center,
                style = Stroke(width = (themeConfig.borderThicknessDp * 2).coerceAtLeast(1).toFloat().dp.toPx())
            )

            // Hour markers and subtle minute ticks
            for (i in 0 until 60) {
                val angle = (i * 6 - 90) * (PI / 180.0)
                val isHour = i % 5 == 0
                val tickLength = if (isHour) radius * 0.12f else radius * 0.05f
                val strokeW = if (isHour) 2.5.dp.toPx() else 1.dp.toPx()
                val tickColor = if (isHour) (if (i % 15 == 0) accentColor else textColor) else mutedColor.copy(alpha = 0.35f)

                if (showMarkers || isHour) {
                    val startX = center.x + ((radius - tickLength) * cos(angle)).toFloat()
                    val startY = center.y + ((radius - tickLength) * sin(angle)).toFloat()
                    val endX = center.x + (radius * cos(angle)).toFloat()
                    val endY = center.y + (radius * sin(angle)).toFloat()

                    drawLine(
                        color = tickColor,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = strokeW,
                        cap = StrokeCap.Round
                    )
                }

                // Optional Roman or Arabic numerals on cardinal hours (12, 3, 6, 9)
                if (showNumerals && isHour && (i % 15 == 0)) {
                    val numText = when (i) {
                        0 -> "12"
                        15 -> "3"
                        30 -> "6"
                        45 -> "9"
                        else -> ""
                    }
                    val textDist = radius * 0.72f
                    val tx = center.x + (textDist * cos(angle)).toFloat()
                    val ty = center.y + (textDist * sin(angle)).toFloat() + 5.dp.toPx()

                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            this.color = android.graphics.Color.argb(
                                (textColor.alpha * 255).toInt(),
                                (textColor.red * 255).toInt(),
                                (textColor.green * 255).toInt(),
                                (textColor.blue * 255).toInt()
                            )
                            this.textAlign = android.graphics.Paint.Align.CENTER
                            this.textSize = 12.dp.toPx()
                            this.isFakeBoldText = true
                        }
                        drawText(numText, tx, ty, paint)
                    }
                }
            }

            // FAKEEXX Brand Signature on dial
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    val c = accentColor
                    this.color = android.graphics.Color.argb(
                        (c.alpha * 255).toInt(),
                        (c.red * 255).toInt(),
                        (c.green * 255).toInt(),
                        (c.blue * 255).toInt()
                    )
                    this.textAlign = android.graphics.Paint.Align.CENTER
                    this.textSize = 9.dp.toPx()
                    this.letterSpacing = 0.25f
                    this.isFakeBoldText = true
                }
                drawText("FAKEEXX", center.x, center.y - (radius * 0.35f), paint)
            }

            // Continuous hand angles
            val hourAngle = ((hour % 12 + minute / 60f + second / 3600f) * 30 - 90) * (PI / 180.0)
            val minuteAngle = ((minute + second / 60f) * 6 - 90) * (PI / 180.0)
            val secondAngle = ((second + (millis / 1000f)) * 6 - 90) * (PI / 180.0)

            // Hour Hand
            val hourLen = radius * 0.52f
            drawLine(
                color = textColor,
                start = center - Offset((cos(hourAngle) * radius * 0.08f).toFloat(), (sin(hourAngle) * radius * 0.08f).toFloat()),
                end = Offset(center.x + (hourLen * cos(hourAngle)).toFloat(), center.y + (hourLen * sin(hourAngle)).toFloat()),
                strokeWidth = 4.5.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Minute Hand
            val minLen = radius * 0.76f
            drawLine(
                color = handsColor,
                start = center - Offset((cos(minuteAngle) * radius * 0.1f).toFloat(), (sin(minuteAngle) * radius * 0.1f).toFloat()),
                end = Offset(center.x + (minLen * cos(minuteAngle)).toFloat(), center.y + (minLen * sin(minuteAngle)).toFloat()),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Second Hand (Sweeping Gold / Accent)
            if (showSeconds) {
                val secLen = radius * 0.86f
                val tailLen = radius * 0.18f
                // Tail
                drawLine(
                    color = accentColor,
                    start = center - Offset((cos(secondAngle) * tailLen).toFloat(), (sin(secondAngle) * tailLen).toFloat()),
                    end = Offset(center.x + (secLen * cos(secondAngle)).toFloat(), center.y + (secLen * sin(secondAngle)).toFloat()),
                    strokeWidth = 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Second hand center dot
                drawCircle(
                    color = accentColor,
                    radius = 4.dp.toPx(),
                    center = center
                )
            }

            // Center Pin
            drawCircle(
                color = themeConfig.backgroundColor,
                radius = 2.dp.toPx(),
                center = center
            )
        }
    }
}
