package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalCustomThemeConfig
import com.example.ui.theme.getClockFontFamily
import com.example.ui.viewmodel.ClockAppViewModel

@Composable
fun SettingsScreen(
    viewModel: ClockAppViewModel,
    onBack: () -> Unit,
    onOpenStudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val themeConfig = LocalCustomThemeConfig.current
    val fontFamily = getClockFontFamily(themeConfig.fontFamily)
    val userName by viewModel.userName.collectAsState()
    val is24Hour by viewModel.is24Hour.collectAsState()

    var editingName by remember(userName) { mutableStateOf(userName) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(themeConfig.backgroundColor)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(themeConfig.surfaceElevatedColor, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = themeConfig.textPrimaryColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "SETTINGS & SYSTEM",
                    color = themeConfig.textPrimaryColor,
                    fontSize = 18.sp,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "PREFERENCES & DATA MANAGEMENT",
                    color = themeConfig.textMutedColor,
                    fontSize = 10.sp,
                    fontFamily = fontFamily,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Profile Section
        SettingsCard(title = "PROFILE & IDENTITY") {
            OutlinedTextField(
                value = editingName,
                onValueChange = {
                    editingName = it
                    viewModel.preferences.setUserName(it)
                },
                label = { Text("Collector Name") },
                modifier = Modifier.fillMaxWidth().testTag("settings_username_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = themeConfig.textPrimaryColor,
                    unfocusedTextColor = themeConfig.textPrimaryColor,
                    focusedBorderColor = themeConfig.accentColor,
                    unfocusedBorderColor = themeConfig.borderColor
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Time & Display Preferences
        SettingsCard(title = "TIME & FORMAT") {
            SettingsToggle(
                label = "24-Hour Time Format",
                checked = is24Hour,
                onCheckedChange = { viewModel.preferences.set24HourFormat(it) }
            )
            SettingsToggle(
                label = "Show Live Seconds",
                checked = themeConfig.showSeconds,
                onCheckedChange = { viewModel.preferences.updateThemeConfig(themeConfig.copy(showSeconds = it)) }
            )
            SettingsToggle(
                label = "Reduced Motion Animations",
                checked = themeConfig.reducedMotion,
                onCheckedChange = { viewModel.preferences.updateThemeConfig(themeConfig.copy(reducedMotion = it)) }
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Home Widgets Visibility
        SettingsCard(title = "HOME SCREEN WIDGETS") {
            SettingsToggle(
                label = "Next Alarm Banner",
                checked = themeConfig.showHomeNextAlarm,
                onCheckedChange = { viewModel.preferences.updateThemeConfig(themeConfig.copy(showHomeNextAlarm = it)) }
            )
            SettingsToggle(
                label = "Live Weather Banner",
                checked = themeConfig.showHomeWeather,
                onCheckedChange = { viewModel.preferences.updateThemeConfig(themeConfig.copy(showHomeWeather = it)) }
            )
            SettingsToggle(
                label = "Quick Action Cards",
                checked = themeConfig.showHomeQuickActions,
                onCheckedChange = { viewModel.preferences.updateThemeConfig(themeConfig.copy(showHomeQuickActions = it)) }
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Sound & Haptics
        SettingsCard(title = "SOUND & HAPTICS") {
            SettingsToggle(
                label = "Haptic Vibration Feedback",
                checked = themeConfig.hapticFeedbackEnabled,
                onCheckedChange = { viewModel.preferences.updateThemeConfig(themeConfig.copy(hapticFeedbackEnabled = it)) }
            )
            SettingsToggle(
                label = "Audible Alarm & Chimes",
                checked = themeConfig.soundEffectsEnabled,
                onCheckedChange = { viewModel.preferences.updateThemeConfig(themeConfig.copy(soundEffectsEnabled = it)) }
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Theme Studio Quick Link
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenStudio() },
            shape = RoundedCornerShape(themeConfig.cornerRadiusDp.dp),
            colors = CardDefaults.cardColors(containerColor = themeConfig.surfaceColor),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    listOf(themeConfig.borderColor, themeConfig.accentColor.copy(alpha = 0.5f))
                )
            )
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Palette, contentDescription = null, tint = themeConfig.accentColor, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Theme & Customization Studio", color = themeConfig.textPrimaryColor, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Text("Active Preset: ${themeConfig.activePreset.title}", color = themeConfig.textMutedColor, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Data & Privacy Management
        SettingsCard(title = "DATA & PRIVACY") {
            Button(
                onClick = {
                    val exportText = "FAKEEXX SMART CLOCK BACKUP\nUser: $userName\nTheme: ${themeConfig.activePreset.title}\nTime Format: ${if (is24Hour) "24H" else "12H"}"
                    val clip = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clip.setPrimaryClip(ClipData.newPlainText("Fakeexx Backup", exportText))
                    Toast.makeText(context, "Settings exported to clipboard", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = themeConfig.surfaceElevatedColor, contentColor = themeConfig.accentColor),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export Clock Preferences")
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = { showDeleteConfirm = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE55353)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset All Data & Restore Defaults")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Brand Info Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(themeConfig.surfaceColor, RoundedCornerShape(themeConfig.cornerRadiusDp.dp))
                .border(1.dp, themeConfig.borderColor, RoundedCornerShape(themeConfig.cornerRadiusDp.dp))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "FAKEEXX SMART CLOCK",
                    color = themeConfig.textPrimaryColor,
                    fontSize = 15.sp,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "THE FUTURE STARTS HERE",
                    color = themeConfig.accentColor,
                    fontSize = 11.sp,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Engineered by FX GROUP • Version 1.0",
                    color = themeConfig.textMutedColor,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Reset All Data?") },
            text = { Text("This will clear all custom theme configurations, preferences, and restore all defaults.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetStudioToDefaults()
                        viewModel.preferences.setUserName("FX Collector")
                        viewModel.preferences.set24HourFormat(false)
                        showDeleteConfirm = false
                        Toast.makeText(context, "Defaults restored", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE55353), contentColor = Color.White)
                ) {
                    Text("Confirm Reset")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            },
            containerColor = themeConfig.surfaceColor
        )
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable () -> Unit
) {
    val themeConfig = LocalCustomThemeConfig.current
    val fontFamily = getClockFontFamily(themeConfig.fontFamily)

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
                text = title,
                color = themeConfig.textMutedColor,
                fontSize = 11.sp,
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun SettingsToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val themeConfig = LocalCustomThemeConfig.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = themeConfig.textPrimaryColor, fontSize = 14.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = themeConfig.accentColor
            )
        )
    }
}
