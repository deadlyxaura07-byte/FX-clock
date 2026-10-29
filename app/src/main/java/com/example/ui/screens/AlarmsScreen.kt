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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlarmItem
import com.example.ui.theme.LocalCustomThemeConfig
import com.example.ui.theme.getClockFontFamily
import com.example.ui.viewmodel.ClockAppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmsScreen(
    viewModel: ClockAppViewModel,
    modifier: Modifier = Modifier
) {
    val themeConfig = LocalCustomThemeConfig.current
    val fontFamily = getClockFontFamily(themeConfig.fontFamily)
    val alarms by viewModel.alarms.collectAsState()
    val is24Hour by viewModel.is24Hour.collectAsState()

    var showAlarmDialog by remember { mutableStateOf(false) }
    var editingAlarm by remember { mutableStateOf<AlarmItem?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(themeConfig.backgroundColor)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ALARMS",
                        color = themeConfig.textPrimaryColor,
                        fontSize = 20.sp,
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${alarms.count { it.isEnabled }} active",
                        color = themeConfig.textMutedColor,
                        fontSize = 12.sp,
                        fontFamily = fontFamily
                    )
                }

                // Test Ringing UI preview button
                Button(
                    onClick = { viewModel.triggerTestAlarmRinging() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = themeConfig.surfaceElevatedColor,
                        contentColor = themeConfig.accentColor
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("alarms_test_ringing_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Test Ringing",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Test Ringing UI", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (alarms.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(themeConfig.surfaceElevatedColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = themeConfig.textMutedColor,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Alarms Scheduled",
                            color = themeConfig.textPrimaryColor,
                            fontSize = 16.sp,
                            fontFamily = fontFamily,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Tap the + button to create a new wake-up call",
                            color = themeConfig.textMutedColor,
                            fontSize = 13.sp,
                            fontFamily = fontFamily
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(alarms, key = { it.id }) { alarm ->
                        AlarmCard(
                            alarm = alarm,
                            is24Hour = is24Hour,
                            onToggle = { viewModel.toggleAlarm(alarm) },
                            onEdit = {
                                editingAlarm = alarm
                                showAlarmDialog = true
                            },
                            onDuplicate = { viewModel.duplicateAlarm(alarm) },
                            onDelete = { viewModel.deleteAlarm(alarm) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // Add Alarm FAB
        FloatingActionButton(
            onClick = {
                editingAlarm = null
                showAlarmDialog = true
            },
            containerColor = themeConfig.accentColor,
            contentColor = Color.Black,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_alarm_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Alarm", modifier = Modifier.size(24.dp))
        }

        // Edit / Create Bottom Sheet
        if (showAlarmDialog) {
            AlarmEditBottomSheet(
                initialAlarm = editingAlarm,
                is24Hour = is24Hour,
                onDismiss = { showAlarmDialog = false },
                onSave = { saved ->
                    viewModel.saveAlarm(saved)
                    showAlarmDialog = false
                }
            )
        }
    }
}

@Composable
private fun AlarmCard(
    alarm: AlarmItem,
    is24Hour: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    val themeConfig = LocalCustomThemeConfig.current
    val fontFamily = getClockFontFamily(themeConfig.fontFamily)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("alarm_card_${alarm.id}"),
        shape = RoundedCornerShape(themeConfig.cornerRadiusDp.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (alarm.isEnabled) themeConfig.surfaceColor else themeConfig.surfaceColor.copy(alpha = 0.6f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                listOf(themeConfig.borderColor, themeConfig.borderColor)
            )
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Time & Label
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = alarm.formattedTime(is24Hour),
                            color = if (alarm.isEnabled) themeConfig.textPrimaryColor else themeConfig.textMutedColor,
                            fontSize = 38.sp,
                            fontFamily = fontFamily,
                            fontWeight = FontWeight.Light
                        )
                        if (!is24Hour) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = alarm.amPm(),
                                color = if (alarm.isEnabled) themeConfig.accentColor else themeConfig.textMutedColor,
                                fontSize = 14.sp,
                                fontFamily = fontFamily,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                    }
                    Text(
                        text = alarm.label,
                        color = themeConfig.textMutedColor,
                        fontSize = 14.sp,
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Switch
                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = themeConfig.accentColor,
                        uncheckedThumbColor = themeConfig.textMutedColor,
                        uncheckedTrackColor = themeConfig.surfaceElevatedColor
                    ),
                    modifier = Modifier.testTag("alarm_switch_${alarm.id}")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Days & Next ring info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Days pills
                val daySet = alarm.getRepeatDaySet()
                val daysLabels = listOf("S" to 1, "M" to 2, "T" to 3, "W" to 4, "T" to 5, "F" to 6, "S" to 7)

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    daysLabels.forEach { (lbl, dayIdx) ->
                        val isDayActive = daySet.contains(dayIdx)
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    color = if (isDayActive && alarm.isEnabled) themeConfig.accentColor else themeConfig.surfaceElevatedColor,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = lbl,
                                color = if (isDayActive && alarm.isEnabled) Color.Black else themeConfig.textMutedColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Action buttons
                Row {
                    IconButton(onClick = onDuplicate, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", tint = themeConfig.textMutedColor, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = themeConfig.textMutedColor, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE55353), modifier = Modifier.size(16.dp))
                    }
                }
            }

            if (alarm.isEnabled) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = alarm.getRemainingTimeDescription(),
                    color = themeConfig.accentColor,
                    fontSize = 11.sp,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlarmEditBottomSheet(
    initialAlarm: AlarmItem?,
    is24Hour: Boolean,
    onDismiss: () -> Unit,
    onSave: (AlarmItem) -> Unit
) {
    val themeConfig = LocalCustomThemeConfig.current
    val fontFamily = getClockFontFamily(themeConfig.fontFamily)

    var hour by remember { mutableStateOf(initialAlarm?.hour ?: 7) }
    var minute by remember { mutableStateOf(initialAlarm?.minute ?: 0) }
    var label by remember { mutableStateOf(initialAlarm?.label ?: "Morning Alarm") }
    var selectedDays by remember { mutableStateOf(initialAlarm?.getRepeatDaySet() ?: setOf(2, 3, 4, 5, 6)) }
    var snoozeMinutes by remember { mutableStateOf(initialAlarm?.snoozeDurationMinutes ?: 5) }
    var isVibrate by remember { mutableStateOf(initialAlarm?.isVibrateEnabled ?: true) }
    var isOneTime by remember { mutableStateOf(initialAlarm?.isOneTime ?: false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = themeConfig.surfaceColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = if (initialAlarm == null) "NEW ALARM" else "EDIT ALARM",
                color = themeConfig.textPrimaryColor,
                fontSize = 18.sp,
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Time Selector (Hour & Minute Steppers)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hour
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("HOUR", color = themeConfig.textMutedColor, fontSize = 11.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { hour = (hour - 1 + 24) % 24 }) {
                            Text("-", color = themeConfig.accentColor, fontSize = 24.sp)
                        }
                        Text(
                            text = String.format("%02d", if (is24Hour) hour else (if (hour % 12 == 0) 12 else hour % 12)),
                            color = themeConfig.textPrimaryColor,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Light,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        IconButton(onClick = { hour = (hour + 1) % 24 }) {
                            Text("+", color = themeConfig.accentColor, fontSize = 24.sp)
                        }
                    }
                }

                Text(":", color = themeConfig.accentColor, fontSize = 32.sp, modifier = Modifier.padding(horizontal = 12.dp))

                // Minute
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("MINUTE", color = themeConfig.textMutedColor, fontSize = 11.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { minute = (minute - 5 + 60) % 60 }) {
                            Text("-", color = themeConfig.accentColor, fontSize = 24.sp)
                        }
                        Text(
                            text = String.format("%02d", minute),
                            color = themeConfig.textPrimaryColor,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Light,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        IconButton(onClick = { minute = (minute + 5) % 60 }) {
                            Text("+", color = themeConfig.accentColor, fontSize = 24.sp)
                        }
                    }
                }

                if (!is24Hour) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(
                        modifier = Modifier
                            .background(themeConfig.surfaceElevatedColor, RoundedCornerShape(12.dp))
                            .clickable { hour = (hour + 12) % 24 }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = if (hour >= 12) "PM" else "AM",
                            color = themeConfig.accentColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Label Input
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Alarm Label") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("alarm_label_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = themeConfig.textPrimaryColor,
                    unfocusedTextColor = themeConfig.textPrimaryColor,
                    focusedBorderColor = themeConfig.accentColor,
                    unfocusedBorderColor = themeConfig.borderColor
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Repeat Days
            Text("REPEAT DAYS", color = themeConfig.textMutedColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val days = listOf("Sun" to 1, "Mon" to 2, "Tue" to 3, "Wed" to 4, "Thu" to 5, "Fri" to 6, "Sat" to 7)
                days.forEach { (name, dayIdx) ->
                    val isSelected = selectedDays.contains(dayIdx)
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                color = if (isSelected) themeConfig.accentColor else themeConfig.surfaceElevatedColor,
                                shape = CircleShape
                            )
                            .clickable {
                                selectedDays = if (isSelected) selectedDays - dayIdx else selectedDays + dayIdx
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = name.take(1),
                            color = if (isSelected) Color.Black else themeConfig.textMutedColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Vibration & Snooze options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Vibrate", color = themeConfig.textPrimaryColor, fontSize = 14.sp)
                Switch(
                    checked = isVibrate,
                    onCheckedChange = { isVibrate = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = themeConfig.accentColor)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            Button(
                onClick = {
                    val repeatStr = selectedDays.sorted().joinToString(",")
                    val newAlarm = (initialAlarm ?: AlarmItem()).copy(
                        hour = hour,
                        minute = minute,
                        label = label.ifBlank { "Alarm" },
                        repeatDays = repeatStr,
                        snoozeDurationMinutes = snoozeMinutes,
                        isVibrateEnabled = isVibrate,
                        isOneTime = selectedDays.isEmpty() || isOneTime,
                        isEnabled = true
                    )
                    onSave(newAlarm)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("alarm_save_button"),
                shape = RoundedCornerShape(themeConfig.cornerRadiusDp.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeConfig.accentColor,
                    contentColor = Color.Black
                )
            ) {
                Text("Save Alarm", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
