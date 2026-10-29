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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalCustomThemeConfig
import com.example.ui.theme.getClockFontFamily
import com.example.ui.viewmodel.ClockAppViewModel

data class ToolItemData(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val badge: String? = null
)

@Composable
fun ToolsScreen(
    viewModel: ClockAppViewModel,
    onOpenTool: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val themeConfig = LocalCustomThemeConfig.current
    val fontFamily = getClockFontFamily(themeConfig.fontFamily)

    val toolsList = listOf(
        ToolItemData("timer", "Countdown Timer", "Precision hours, minutes, and seconds", Icons.Default.HourglassBottom),
        ToolItemData("stopwatch", "Stopwatch", "10ms accuracy with lap metrics & export", Icons.Default.Timer),
        ToolItemData("world_clock", "World Clock", "Real IANA global timezones & offsets", Icons.Default.Public),
        ToolItemData("weather", "Global Weather", "Live conditions, forecasts, and humidity", Icons.Default.Cloud, badge = "LIVE"),
        ToolItemData("ai", "FX AI Assistant", "Intelligent voice & text timekeeper commands", Icons.Default.AutoAwesome, badge = "SMART"),
        ToolItemData("studio", "Customization Studio", "Themes, dial styles, fonts, and palettes", Icons.Default.Palette, badge = "PREMIUM"),
        ToolItemData("settings", "Preferences & System", "Profile, sound, haptics, and data reset", Icons.Default.Settings)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(themeConfig.backgroundColor)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Header
        Column {
            Text(
                text = "TOOLS & UTILITIES",
                color = themeConfig.textPrimaryColor,
                fontSize = 20.sp,
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "ENGINEERED BY FAKEEXX",
                color = themeConfig.textMutedColor,
                fontSize = 11.sp,
                fontFamily = fontFamily,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Tool cards list
        toolsList.forEach { tool ->
            ToolRowCard(
                tool = tool,
                onClick = { onOpenTool(tool.id) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ToolRowCard(
    tool: ToolItemData,
    onClick: () -> Unit
) {
    val themeConfig = LocalCustomThemeConfig.current
    val fontFamily = getClockFontFamily(themeConfig.fontFamily)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("tool_card_${tool.id}"),
        shape = RoundedCornerShape(themeConfig.cornerRadiusDp.dp),
        colors = CardDefaults.cardColors(containerColor = themeConfig.surfaceColor),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                listOf(themeConfig.borderColor, themeConfig.borderColor)
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(themeConfig.surfaceElevatedColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = tool.icon,
                        contentDescription = tool.title,
                        tint = themeConfig.accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = tool.title,
                            color = themeConfig.textPrimaryColor,
                            fontSize = 15.sp,
                            fontFamily = fontFamily,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (tool.badge != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(themeConfig.accentColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = tool.badge,
                                    color = themeConfig.accentColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                    Text(
                        text = tool.subtitle,
                        color = themeConfig.textMutedColor,
                        fontSize = 12.sp,
                        fontFamily = fontFamily
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = themeConfig.textMutedColor,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
