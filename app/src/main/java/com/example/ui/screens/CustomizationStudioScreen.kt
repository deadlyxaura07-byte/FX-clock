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
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.RestartAlt
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.example.data.model.ClockFaceType
import com.example.data.model.ClockFontFamily
import com.example.data.model.CustomThemeConfig
import com.example.data.model.ThemePreset
import com.example.ui.components.FxAnalogClock
import com.example.ui.components.FxDigitalClock
import com.example.ui.theme.FakeexxClockTheme
import com.example.ui.theme.getClockFontFamily
import com.example.ui.viewmodel.ClockAppViewModel

@Composable
fun CustomizationStudioScreen(
    viewModel: ClockAppViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val draftConfig by viewModel.studioDraft.collectAsState()
    val liveTime by viewModel.liveTime.collectAsState()
    val fontFamily = getClockFontFamily(draftConfig.fontFamily)

    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showJsonExportDialog by remember { mutableStateOf(false) }
    var showJsonImportDialog by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }

    // Wrap studio content in the draft theme so all preview elements respond dynamically!
    FakeexxClockTheme(config = draftConfig) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(draftConfig.backgroundColor)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(38.dp)
                            .background(draftConfig.surfaceElevatedColor, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = draftConfig.textPrimaryColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "CUSTOMIZATION STUDIO",
                            color = draftConfig.textPrimaryColor,
                            fontSize = 17.sp,
                            fontFamily = fontFamily,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "LIVE TIMEPIECE PREVIEW",
                            color = draftConfig.textMutedColor,
                            fontSize = 10.sp,
                            fontFamily = fontFamily,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Undo / Redo / Reset Actions
                Row {
                    IconButton(
                        onClick = { viewModel.undoStudioDraft() },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo", tint = draftConfig.textMutedColor, modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = { viewModel.redoStudioDraft() },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo", tint = draftConfig.textMutedColor, modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = { showResetConfirmDialog = true },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Reset Defaults", tint = draftConfig.textMutedColor, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sticky Live Preview Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(draftConfig.cornerRadiusDp.dp),
                colors = CardDefaults.cardColors(containerColor = draftConfig.surfaceColor),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        listOf(draftConfig.borderColor, draftConfig.borderColor)
                    )
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "LIVE PREVIEW • ${draftConfig.activePreset.title.uppercase()}",
                        color = draftConfig.accentColor,
                        fontSize = 11.sp,
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    when (draftConfig.clockFaceType) {
                        ClockFaceType.DIGITAL -> {
                            FxDigitalClock(
                                timeState = liveTime,
                                is24Hour = draftConfig.is24HourFormat,
                                showSeconds = draftConfig.showSeconds,
                                showAmPm = draftConfig.showAmPm,
                                showDate = draftConfig.showDate
                            )
                        }
                        ClockFaceType.ANALOG -> {
                            FxAnalogClock(
                                hour = liveTime.hour12,
                                minute = liveTime.minute,
                                second = liveTime.second,
                                millis = liveTime.millis,
                                dialSize = 200.dp,
                                showSeconds = draftConfig.showSeconds
                            )
                        }
                        ClockFaceType.HYBRID -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                FxAnalogClock(
                                    hour = liveTime.hour12,
                                    minute = liveTime.minute,
                                    second = liveTime.second,
                                    millis = liveTime.millis,
                                    dialSize = 150.dp,
                                    showSeconds = draftConfig.showSeconds
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                FxDigitalClock(
                                    timeState = liveTime,
                                    is24Hour = draftConfig.is24HourFormat,
                                    showSeconds = false,
                                    showAmPm = true,
                                    showDate = false
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 1. Theme Presets Selector
            Text("THEME PRESETS (6 SIGNATURE PALETTES)", color = draftConfig.textMutedColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemePreset.values().take(3).forEach { preset ->
                    PresetCard(
                        preset = preset,
                        isSelected = draftConfig.activePreset == preset,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.updateStudioDraft(CustomThemeConfig.fromPreset(preset)) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemePreset.values().drop(3).forEach { preset ->
                    PresetCard(
                        preset = preset,
                        isSelected = draftConfig.activePreset == preset,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.updateStudioDraft(CustomThemeConfig.fromPreset(preset)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Clock Face & Style Controls
            Text("CLOCK FACE & DIAL", color = draftConfig.textMutedColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ClockFaceType.values().forEach { face ->
                    val isSelected = draftConfig.clockFaceType == face
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = if (isSelected) draftConfig.accentColor else draftConfig.surfaceColor,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(1.dp, draftConfig.borderColor, RoundedCornerShape(12.dp))
                            .clickable { viewModel.updateStudioDraft(draftConfig.copy(clockFaceType = face)) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = face.name,
                            color = if (isSelected) Color.Black else draftConfig.textPrimaryColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Toggles (Seconds, 24-Hour, Markers, Numerals)
            StudioToggleRow(
                label = "Display Live Seconds",
                checked = draftConfig.showSeconds,
                onCheckedChange = { viewModel.updateStudioDraft(draftConfig.copy(showSeconds = it)) }
            )
            StudioToggleRow(
                label = "24-Hour Military Format",
                checked = draftConfig.is24HourFormat,
                onCheckedChange = { viewModel.updateStudioDraft(draftConfig.copy(is24HourFormat = it)) }
            )
            StudioToggleRow(
                label = "Analog Hour Markers",
                checked = draftConfig.analogShowHourMarkers,
                onCheckedChange = { viewModel.updateStudioDraft(draftConfig.copy(analogShowHourMarkers = it)) }
            )
            StudioToggleRow(
                label = "Cardinal Numerals (12, 3, 6, 9)",
                checked = draftConfig.analogShowNumerals,
                onCheckedChange = { viewModel.updateStudioDraft(draftConfig.copy(analogShowNumerals = it)) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Color Customization (Hex inputs & quick swatches)
            Text("PALETTE & ACCENTS", color = draftConfig.textMutedColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(10.dp))

            ColorSettingRow(
                label = "Accent / Champagne Gold",
                hex = draftConfig.customAccentHex,
                onHexChanged = { viewModel.updateStudioDraft(draftConfig.copy(customAccentHex = it)) }
            )
            ColorSettingRow(
                label = "Background Tone",
                hex = draftConfig.customBackgroundHex,
                onHexChanged = { viewModel.updateStudioDraft(draftConfig.copy(customBackgroundHex = it)) }
            )
            ColorSettingRow(
                label = "Surface Tone",
                hex = draftConfig.customSurfaceHex,
                onHexChanged = { viewModel.updateStudioDraft(draftConfig.copy(customSurfaceHex = it)) }
            )
            ColorSettingRow(
                label = "Analog Hands Tint",
                hex = draftConfig.customClockHandsHex,
                onHexChanged = { viewModel.updateStudioDraft(draftConfig.copy(customClockHandsHex = it)) }
            )
            ColorSettingRow(
                label = "Border & Dividers",
                hex = draftConfig.customBorderHex,
                onHexChanged = { viewModel.updateStudioDraft(draftConfig.copy(customBorderHex = it)) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Typography & Shape Sliders
            Text("TYPOGRAPHY & GEOMETRY", color = draftConfig.textMutedColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(10.dp))

            // Font Family Choice
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ClockFontFamily.values().forEach { ff ->
                    val isSelected = draftConfig.fontFamily == ff
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = if (isSelected) draftConfig.accentColor else draftConfig.surfaceColor,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(1.dp, draftConfig.borderColor, RoundedCornerShape(12.dp))
                            .clickable { viewModel.updateStudioDraft(draftConfig.copy(fontFamily = ff)) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = ff.name,
                            color = if (isSelected) Color.Black else draftConfig.textPrimaryColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Corner Radius Slider
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Corner Radius", color = draftConfig.textPrimaryColor, fontSize = 13.sp)
                    Text("${draftConfig.cornerRadiusDp} dp", color = draftConfig.accentColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = draftConfig.cornerRadiusDp.toFloat(),
                    onValueChange = { viewModel.updateStudioDraft(draftConfig.copy(cornerRadiusDp = it.toInt())) },
                    valueRange = 4f..32f,
                    colors = SliderDefaults.colors(thumbColor = draftConfig.accentColor, activeTrackColor = draftConfig.accentColor)
                )
            }

            // Digit Size Scale Slider
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Clock Digit Scale", color = draftConfig.textPrimaryColor, fontSize = 13.sp)
                    Text("${(draftConfig.clockDigitSizeScale * 100).toInt()}%", color = draftConfig.accentColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = draftConfig.clockDigitSizeScale,
                    onValueChange = { viewModel.updateStudioDraft(draftConfig.copy(clockDigitSizeScale = it)) },
                    valueRange = 0.8f..1.3f,
                    colors = SliderDefaults.colors(thumbColor = draftConfig.accentColor, activeTrackColor = draftConfig.accentColor)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5. JSON Theme Import / Export Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { showJsonExportDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export JSON", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { showJsonImportDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Import JSON", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 6. Save & Apply Button
            Button(
                onClick = {
                    viewModel.applyStudioDraft()
                    Toast.makeText(context, "Theme changes saved and applied", Toast.LENGTH_SHORT).show()
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("studio_save_button"),
                shape = RoundedCornerShape(draftConfig.cornerRadiusDp.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = draftConfig.accentColor,
                    contentColor = Color.Black
                )
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("SAVE & APPLY THEME", fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }

            Spacer(modifier = Modifier.height(30.dp))
        }

        // Reset to Defaults Confirmation Dialog
        if (showResetConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showResetConfirmDialog = false },
                title = { Text("Restore Default Theme?") },
                text = { Text("This will reset all colors, typography, and clock styles back to the signature FAKEEXX Abyss theme.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.resetStudioToDefaults()
                            showResetConfirmDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = draftConfig.accentColor, contentColor = Color.Black)
                    ) {
                        Text("Reset All")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showResetConfirmDialog = false }) {
                        Text("Cancel")
                    }
                },
                containerColor = draftConfig.surfaceColor
            )
        }

        // Export JSON Dialog
        if (showJsonExportDialog) {
            val exported = viewModel.preferences.exportThemeJson(draftConfig)
            AlertDialog(
                onDismissRequest = { showJsonExportDialog = false },
                title = { Text("Theme JSON Export") },
                text = {
                    Column {
                        Text("Copy your personalized theme schema:", fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .background(draftConfig.surfaceElevatedColor, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(exported, fontSize = 11.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val clip = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clip.setPrimaryClip(ClipData.newPlainText("Fakeexx Theme", exported))
                            Toast.makeText(context, "Theme JSON copied to clipboard", Toast.LENGTH_SHORT).show()
                            showJsonExportDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = draftConfig.accentColor, contentColor = Color.Black)
                    ) {
                        Text("Copy to Clipboard")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showJsonExportDialog = false }) {
                        Text("Close")
                    }
                },
                containerColor = draftConfig.surfaceColor
            )
        }

        // Import JSON Dialog
        if (showJsonImportDialog) {
            AlertDialog(
                onDismissRequest = { showJsonImportDialog = false },
                title = { Text("Import Theme JSON") },
                text = {
                    Column {
                        Text("Paste your valid theme JSON configuration:", fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = importJsonText,
                            onValueChange = { importJsonText = it },
                            placeholder = { Text("{\n  \"bg\": \"#090A0C\",\n  \"accent\": \"#C8A875\" ...\n}") },
                            modifier = Modifier.fillMaxWidth().height(150.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val parsed = viewModel.preferences.importThemeJson(importJsonText)
                            if (parsed != null) {
                                viewModel.updateStudioDraft(parsed)
                                Toast.makeText(context, "Theme imported successfully", Toast.LENGTH_SHORT).show()
                                showJsonImportDialog = false
                            } else {
                                Toast.makeText(context, "Invalid JSON format", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = draftConfig.accentColor, contentColor = Color.Black)
                    ) {
                        Text("Apply Import")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showJsonImportDialog = false }) {
                        Text("Cancel")
                    }
                },
                containerColor = draftConfig.surfaceColor
            )
        }
    }
}

@Composable
private fun PresetCard(
    preset: ThemePreset,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeConfig = com.example.ui.theme.LocalCustomThemeConfig.current
    val fontFamily = getClockFontFamily(themeConfig.fontFamily)

    Box(
        modifier = modifier
            .background(
                color = if (isSelected) themeConfig.surfaceElevatedColor else themeConfig.surfaceColor,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) themeConfig.accentColor else themeConfig.borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Color circles row preview
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(themeConfig.parseColor(preset.backgroundHex, Color.Black), CircleShape)
                        .border(0.5.dp, Color.Gray, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(themeConfig.parseColor(preset.accentHex, Color.Yellow), CircleShape)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = preset.title,
                color = if (isSelected) themeConfig.accentColor else themeConfig.textPrimaryColor,
                fontSize = 11.sp,
                fontFamily = fontFamily,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun StudioToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val themeConfig = com.example.ui.theme.LocalCustomThemeConfig.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = themeConfig.textPrimaryColor, fontSize = 13.sp)
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

@Composable
private fun ColorSettingRow(
    label: String,
    hex: String,
    onHexChanged: (String) -> Unit
) {
    val themeConfig = com.example.ui.theme.LocalCustomThemeConfig.current
    var editingHex by remember(hex) { mutableStateOf(hex) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(themeConfig.parseColor(hex, Color.Gray), CircleShape)
                    .border(1.dp, themeConfig.borderColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(label, color = themeConfig.textPrimaryColor, fontSize = 13.sp)
        }

        OutlinedTextField(
            value = editingHex,
            onValueChange = {
                editingHex = it
                if (it.startsWith("#") && (it.length == 7 || it.length == 9)) {
                    onHexChanged(it)
                }
            },
            modifier = Modifier.width(110.dp).height(48.dp),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = themeConfig.textPrimaryColor),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = themeConfig.accentColor,
                unfocusedBorderColor = themeConfig.borderColor
            )
        )
    }
}
