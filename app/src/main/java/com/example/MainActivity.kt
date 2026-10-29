package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.components.FxNavigationBar
import com.example.ui.components.FxRingingAlarmDialog
import com.example.ui.components.NavigationTab
import com.example.ui.screens.AiAssistantScreen
import com.example.ui.screens.AlarmsScreen
import com.example.ui.screens.ClockScreen
import com.example.ui.screens.CustomizationStudioScreen
import com.example.ui.screens.FocusScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StopwatchScreen
import com.example.ui.screens.TimerScreen
import com.example.ui.screens.ToolsScreen
import com.example.ui.screens.WeatherScreen
import com.example.ui.screens.WorldClockScreen
import com.example.ui.theme.FakeexxClockTheme
import com.example.ui.viewmodel.ClockAppViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ClockAppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeConfig by viewModel.themeConfig.collectAsState()
            val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsState()
            val ringingAlarm by viewModel.activeRingingAlarm.collectAsState()

            var currentTab by remember { mutableStateOf(NavigationTab.HOME) }
            var activeSubTool by remember { mutableStateOf<String?>(null) }

            // Handle incoming notification nav extra
            val initialTabExtra = intent?.getStringExtra("EXTRA_NAV_TAB")
            if (initialTabExtra == "alarms") {
                currentTab = NavigationTab.ALARMS
            }

            FakeexxClockTheme(config = themeConfig) {
                // Back press handling
                BackHandler(enabled = activeSubTool != null || (!isOnboardingCompleted) || currentTab != NavigationTab.HOME) {
                    when {
                        activeSubTool != null -> activeSubTool = null
                        currentTab != NavigationTab.HOME -> currentTab = NavigationTab.HOME
                    }
                }

                if (!isOnboardingCompleted) {
                    OnboardingScreen(
                        viewModel = viewModel,
                        onFinish = { currentTab = NavigationTab.HOME }
                    )
                } else {
                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(themeConfig.backgroundColor),
                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
                        bottomBar = {
                            if (activeSubTool == null) {
                                FxNavigationBar(
                                    currentTab = currentTab,
                                    onTabSelected = { tab ->
                                        currentTab = tab
                                        activeSubTool = null
                                    }
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            AnimatedContent(
                                targetState = activeSubTool to currentTab,
                                transitionSpec = {
                                    fadeIn() togetherWith fadeOut()
                                },
                                label = "screen_transition"
                            ) { (subTool, tab) ->
                                if (subTool != null) {
                                    when (subTool) {
                                        "timer" -> TimerScreen(
                                            viewModel = viewModel,
                                            onBack = { activeSubTool = null }
                                        )
                                        "stopwatch" -> StopwatchScreen(
                                            viewModel = viewModel,
                                            onBack = { activeSubTool = null }
                                        )
                                        "world_clock" -> WorldClockScreen(
                                            viewModel = viewModel,
                                            onBack = { activeSubTool = null }
                                        )
                                        "weather" -> WeatherScreen(
                                            viewModel = viewModel,
                                            onBack = { activeSubTool = null }
                                        )
                                        "ai" -> AiAssistantScreen(
                                            viewModel = viewModel,
                                            onBack = { activeSubTool = null }
                                        )
                                        "studio" -> {
                                            viewModel.initStudioDraft()
                                            CustomizationStudioScreen(
                                                viewModel = viewModel,
                                                onBack = { activeSubTool = null }
                                            )
                                        }
                                        "settings" -> SettingsScreen(
                                            viewModel = viewModel,
                                            onBack = { activeSubTool = null },
                                            onOpenStudio = { activeSubTool = "studio" }
                                        )
                                        else -> HomeScreen(
                                            viewModel = viewModel,
                                            onNavigateToTab = { currentTab = it },
                                            onOpenTool = { activeSubTool = it }
                                        )
                                    }
                                } else {
                                    when (tab) {
                                        NavigationTab.HOME -> HomeScreen(
                                            viewModel = viewModel,
                                            onNavigateToTab = { currentTab = it },
                                            onOpenTool = { activeSubTool = it }
                                        )
                                        NavigationTab.CLOCK -> ClockScreen(
                                            viewModel = viewModel
                                        )
                                        NavigationTab.ALARMS -> AlarmsScreen(
                                            viewModel = viewModel
                                        )
                                        NavigationTab.FOCUS -> FocusScreen(
                                            viewModel = viewModel
                                        )
                                        NavigationTab.TOOLS -> ToolsScreen(
                                            viewModel = viewModel,
                                            onOpenTool = { toolId: String ->
                                                activeSubTool = toolId
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // High-priority Ringing Alert Dialog with working Stop and Snooze
                ringingAlarm?.let { alarm ->
                    FxRingingAlarmDialog(
                        alarmState = alarm,
                        onStop = { viewModel.stopRingingAlarm() },
                        onSnooze = { viewModel.snoozeRingingAlarm() }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}
