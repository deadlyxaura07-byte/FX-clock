package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Widgets
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

enum class NavigationTab(val route: String, val title: String, val icon: ImageVector) {
    HOME("home", "Home", Icons.Default.Dashboard),
    CLOCK("clock", "Clock", Icons.Default.AccessTime),
    ALARMS("alarms", "Alarms", Icons.Default.Alarm),
    FOCUS("focus", "Focus", Icons.Default.Psychology),
    TOOLS("tools", "Tools", Icons.Default.Widgets)
}

@Composable
fun FxNavigationBar(
    currentTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val themeConfig = LocalCustomThemeConfig.current
    val fontFamily = getClockFontFamily(themeConfig.fontFamily)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(themeConfig.surfaceColor)
            .border(
                width = 1.dp,
                color = themeConfig.borderColor.copy(alpha = 0.6f)
            )
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavigationTab.values().forEach { tab ->
                val isSelected = tab == currentTab
                val itemColor = if (isSelected) themeConfig.accentColor else themeConfig.textMutedColor

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clickable { onTabSelected(tab) }
                        .testTag("nav_tab_${tab.route}"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isSelected) themeConfig.accentColor.copy(alpha = 0.12f) else androidx.compose.ui.graphics.Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            tint = itemColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Text(
                        text = tab.title,
                        color = itemColor,
                        fontSize = 11.sp,
                        fontFamily = fontFamily,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        letterSpacing = 0.3.sp
                    )
                }
            }
        }
    }
}
