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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.AiClockAction
import com.example.data.ai.AiMessage
import com.example.ui.theme.LocalCustomThemeConfig
import com.example.ui.theme.getClockFontFamily
import com.example.ui.viewmodel.ClockAppViewModel

@Composable
fun AiAssistantScreen(
    viewModel: ClockAppViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeConfig = LocalCustomThemeConfig.current
    val fontFamily = getClockFontFamily(themeConfig.fontFamily)
    val messages by viewModel.aiMessages.collectAsState()
    val isLoading by viewModel.isAiLoading.collectAsState()

    var inputPrompt by remember { mutableStateOf("") }

    val quickPrompts = listOf(
        "Wake me up at 6:30 AM",
        "Start 25 min focus",
        "Set 10 min timer",
        "Time in Tokyo",
        "Switch to Midnight Blue"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(themeConfig.backgroundColor)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Top Header
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "FX AI ASSISTANT",
                        color = themeConfig.textPrimaryColor,
                        fontSize = 18.sp,
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(themeConfig.accentColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "SMART",
                            color = themeConfig.accentColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = "TIMEKEEPER INTELLIGENCE",
                    color = themeConfig.textMutedColor,
                    fontSize = 10.sp,
                    fontFamily = fontFamily,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Suggestion Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickPrompts.take(3).forEach { prompt ->
                SuggestionChip(
                    onClick = {
                        inputPrompt = prompt
                        viewModel.sendAiPrompt(prompt)
                        inputPrompt = ""
                    },
                    label = { Text(prompt, fontSize = 11.sp, color = themeConfig.textMutedColor) },
                    colors = SuggestionChipDefaults.suggestionChipColors(containerColor = themeConfig.surfaceElevatedColor),
                    border = SuggestionChipDefaults.suggestionChipBorder(enabled = true, borderColor = themeConfig.borderColor),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Messages List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                AiMessageBubble(
                    message = msg,
                    onExecuteAction = { action ->
                        viewModel.executeAiAction(action, msg.id)
                    }
                )
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = themeConfig.accentColor,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "FX Assistant is analyzing...",
                            color = themeConfig.textMutedColor,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Input Field and Send Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputPrompt,
                onValueChange = { inputPrompt = it },
                placeholder = { Text("Ask FX Assistant...") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_prompt_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = themeConfig.textPrimaryColor,
                    unfocusedTextColor = themeConfig.textPrimaryColor,
                    focusedBorderColor = themeConfig.accentColor,
                    unfocusedBorderColor = themeConfig.borderColor,
                    focusedContainerColor = themeConfig.surfaceColor,
                    unfocusedContainerColor = themeConfig.surfaceColor
                ),
                shape = RoundedCornerShape(themeConfig.cornerRadiusDp.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            IconButton(
                onClick = {
                    if (inputPrompt.isNotBlank()) {
                        val text = inputPrompt
                        inputPrompt = ""
                        viewModel.sendAiPrompt(text)
                    }
                },
                modifier = Modifier
                    .size(52.dp)
                    .background(themeConfig.accentColor, CircleShape)
                    .testTag("ai_send_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun AiMessageBubble(
    message: AiMessage,
    onExecuteAction: (AiClockAction) -> Unit
) {
    val themeConfig = LocalCustomThemeConfig.current
    val fontFamily = getClockFontFamily(themeConfig.fontFamily)

    val isUser = message.isUser
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val bgColor = if (isUser) themeConfig.surfaceElevatedColor else themeConfig.surfaceColor
    val borderColor = if (isUser) themeConfig.accentColor.copy(alpha = 0.5f) else themeConfig.borderColor

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(if (isUser) 0.8f else 0.92f)
                .background(bgColor, RoundedCornerShape(16.dp))
                .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                if (!isUser) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = themeConfig.accentColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "FX CLOCK ENGINE",
                            color = themeConfig.accentColor,
                            fontSize = 10.sp,
                            fontFamily = fontFamily,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Text(
                    text = message.text,
                    color = themeConfig.textPrimaryColor,
                    fontSize = 14.sp,
                    fontFamily = fontFamily,
                    lineHeight = 20.sp
                )

                // Action Confirmation Button if actionable
                if (!isUser && message.proposedAction !is AiClockAction.None) {
                    Spacer(modifier = Modifier.height(14.dp))
                    if (message.actionExecuted) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF4EBA74),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Action Executed",
                                color = Color(0xFF4EBA74),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        Button(
                            onClick = { onExecuteAction(message.proposedAction) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = themeConfig.accentColor,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("ai_action_confirm_button")
                        ) {
                            val actionLabel = when (message.proposedAction) {
                                is AiClockAction.CreateAlarm -> "Confirm & Set Alarm"
                                is AiClockAction.StartTimer -> "Confirm & Start Timer"
                                is AiClockAction.StartFocus -> "Confirm & Launch Focus"
                                is AiClockAction.ChangeTheme -> "Apply Theme"
                                is AiClockAction.SetTimeFormat -> "Apply Time Format"
                                else -> "Confirm Action"
                            }
                            Text(actionLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
