package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LapItem
import com.example.ui.theme.LocalCustomThemeConfig
import com.example.ui.theme.getClockFontFamily
import com.example.ui.viewmodel.ClockAppViewModel

@Composable
fun StopwatchScreen(
    viewModel: ClockAppViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val themeConfig = LocalCustomThemeConfig.current
    val fontFamily = getClockFontFamily(themeConfig.fontFamily)

    val elapsedMillis by viewModel.stopwatchElapsedMillis.collectAsState()
    val isRunning by viewModel.isStopwatchRunning.collectAsState()
    val laps by viewModel.stopwatchLaps.collectAsState()

    val formattedDisplay = LapItem.formatMillis(elapsedMillis)
    val parts = formattedDisplay.split(".")
    val minSec = parts.getOrNull(0) ?: "00:00"
    val hundredths = parts.getOrNull(1) ?: "00"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(themeConfig.backgroundColor)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                        text = "PRECISION STOPWATCH",
                        color = themeConfig.textPrimaryColor,
                        fontSize = 18.sp,
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "10MS CHRONOGRAPH",
                        color = themeConfig.textMutedColor,
                        fontSize = 10.sp,
                        fontFamily = fontFamily,
                        letterSpacing = 1.sp
                    )
                }
            }

            if (laps.isNotEmpty()) {
                Row {
                    // Copy
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Stopwatch Laps", viewModel.getLapsSummaryText()))
                            Toast.makeText(context, "Laps copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Laps", tint = themeConfig.accentColor, modifier = Modifier.size(18.dp))
                    }
                    // Share
                    IconButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, viewModel.getLapsSummaryText())
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Stopwatch Session"))
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share Laps", tint = themeConfig.accentColor, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Time Display Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(themeConfig.surfaceColor, RoundedCornerShape(themeConfig.cornerRadiusDp.dp))
                .border(1.dp, themeConfig.borderColor, RoundedCornerShape(themeConfig.cornerRadiusDp.dp))
                .padding(vertical = 36.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = minSec,
                    color = themeConfig.textPrimaryColor,
                    fontSize = 58.sp,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-1).sp
                )
                Text(
                    text = ".$hundredths",
                    color = themeConfig.accentColor,
                    fontSize = 28.sp,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Primary Buttons (Lap / Reset & Play / Pause)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Lap / Reset button
            OutlinedButton(
                onClick = {
                    if (isRunning) viewModel.recordStopwatchLap() else viewModel.resetStopwatch()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .testTag("stopwatch_lap_reset_button"),
                shape = RoundedCornerShape(themeConfig.cornerRadiusDp.dp),
                enabled = elapsedMillis > 0,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = themeConfig.textPrimaryColor
                )
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Flag else Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isRunning) "LAP" else "RESET",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            // Start / Pause button
            Button(
                onClick = {
                    if (isRunning) viewModel.pauseStopwatch() else viewModel.startStopwatch()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .testTag("stopwatch_play_pause_button"),
                shape = RoundedCornerShape(themeConfig.cornerRadiusDp.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) Color(0xFFE55353) else themeConfig.accentColor,
                    contentColor = Color.Black
                )
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isRunning) "STOP" else "START",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Laps List
        if (laps.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("LAP", color = themeConfig.textMutedColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("SPLIT", color = themeConfig.textMutedColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("TOTAL", color = themeConfig.textMutedColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(laps) { lap ->
                    val lapColor = when {
                        lap.isFastest -> Color(0xFF4EBA74) // Green
                        lap.isSlowest -> Color(0xFFE55353) // Red
                        else -> themeConfig.textPrimaryColor
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = themeConfig.surfaceColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = String.format("Lap %02d", lap.lapNumber),
                                    color = lapColor,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                if (lap.isFastest) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("FASTEST", color = Color(0xFF4EBA74), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                } else if (lap.isSlowest) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("SLOWEST", color = Color(0xFFE55353), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Text(
                                text = lap.formattedLapTime(),
                                color = lapColor,
                                fontSize = 14.sp,
                                fontFamily = fontFamily,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = lap.formattedTotalTime(),
                                color = themeConfig.textMutedColor,
                                fontSize = 13.sp,
                                fontFamily = fontFamily
                            )
                        }
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
