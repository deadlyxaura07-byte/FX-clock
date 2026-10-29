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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.network.GeocodingResult
import com.example.data.network.WeatherClient
import com.example.ui.theme.LocalCustomThemeConfig
import com.example.ui.theme.getClockFontFamily
import com.example.ui.viewmodel.ClockAppViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WeatherScreen(
    viewModel: ClockAppViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeConfig = LocalCustomThemeConfig.current
    val fontFamily = getClockFontFamily(themeConfig.fontFamily)
    val weather by viewModel.weatherState.collectAsState()
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<GeocodingResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(themeConfig.backgroundColor)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
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
                        text = "LIVE WEATHER",
                        color = themeConfig.textPrimaryColor,
                        fontSize = 18.sp,
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "OPEN-METEO METEOROLOGICAL DATA",
                        color = themeConfig.textMutedColor,
                        fontSize = 10.sp,
                        fontFamily = fontFamily,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Unit Switcher (°C / °F)
            Box(
                modifier = Modifier
                    .background(themeConfig.surfaceElevatedColor, RoundedCornerShape(14.dp))
                    .clickable { viewModel.toggleWeatherUnit() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (weather.isCelsius) "°C | °F" else "°F | °C",
                    color = themeConfig.accentColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // City Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { query ->
                searchQuery = query
                if (query.length >= 2) {
                    isSearching = true
                    scope.launch {
                        val res = WeatherClient.searchLocations(query)
                        searchResults = res.getOrDefault(emptyList())
                        isSearching = false
                    }
                } else {
                    searchResults = emptyList()
                }
            },
            label = { Text("Search location (e.g. New York, London, Paris)") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = themeConfig.textMutedColor)
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("weather_search_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = themeConfig.textPrimaryColor,
                unfocusedTextColor = themeConfig.textPrimaryColor,
                focusedBorderColor = themeConfig.accentColor,
                unfocusedBorderColor = themeConfig.borderColor
            ),
            shape = RoundedCornerShape(12.dp)
        )

        // Dropdown Search Results
        if (searchResults.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = themeConfig.surfaceElevatedColor)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    searchResults.forEach { loc ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    searchQuery = ""
                                    searchResults = emptyList()
                                    viewModel.searchAndSelectWeatherCity(loc.name, loc.latitude, loc.longitude)
                                }
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "${loc.name}, ${loc.country ?: ""}",
                                color = themeConfig.textPrimaryColor,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Main Weather Card
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
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // City Name
                Text(
                    text = weather.cityName.uppercase(),
                    color = themeConfig.textPrimaryColor,
                    fontSize = 22.sp,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Condition Icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(themeConfig.surfaceElevatedColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (weather.isLoading) {
                        CircularProgressIndicator(color = themeConfig.accentColor, strokeWidth = 2.dp)
                    } else {
                        Icon(
                            imageVector = if (weather.weatherCode in 51..82) Icons.Default.WaterDrop else if (weather.weatherCode in 1..3) Icons.Default.Cloud else Icons.Default.WbSunny,
                            contentDescription = null,
                            tint = themeConfig.accentColor,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Temperature
                Text(
                    text = weather.displayTemperature(),
                    color = themeConfig.textPrimaryColor,
                    fontSize = 62.sp,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-1.5).sp
                )

                // Condition Text & High/Low
                Text(
                    text = weather.conditionText,
                    color = themeConfig.accentColor,
                    fontSize = 16.sp,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = weather.displayMinMax(),
                    color = themeConfig.textMutedColor,
                    fontSize = 13.sp,
                    fontFamily = fontFamily
                )

                if (weather.errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = weather.errorMessage ?: "",
                        color = Color(0xFFE55353),
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Weather Metrics Grid (Feels Like, Humidity, Wind)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Feels Like
            MetricBox(
                title = "FEELS LIKE",
                value = weather.displayFeelsLike(),
                icon = Icons.Default.Thermostat,
                modifier = Modifier.weight(1f)
            )

            // Humidity
            MetricBox(
                title = "HUMIDITY",
                value = "${weather.humidityPercent}%",
                icon = Icons.Default.WaterDrop,
                modifier = Modifier.weight(1f)
            )

            // Wind
            MetricBox(
                title = "WIND",
                value = weather.displayWind(),
                icon = Icons.Default.Air,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Last Updated timestamp & reload
        val timeFmt = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(weather.lastUpdatedMillis))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Last updated: $timeFmt",
                color = themeConfig.textMutedColor,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { viewModel.refreshWeather() },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = themeConfig.accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun MetricBox(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    val themeConfig = LocalCustomThemeConfig.current
    val fontFamily = getClockFontFamily(themeConfig.fontFamily)

    Box(
        modifier = modifier
            .background(themeConfig.surfaceColor, RoundedCornerShape(themeConfig.cornerRadiusDp.dp))
            .border(1.dp, themeConfig.borderColor, RoundedCornerShape(themeConfig.cornerRadiusDp.dp))
            .padding(14.dp)
    ) {
        Column {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = themeConfig.accentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                color = themeConfig.textMutedColor,
                fontSize = 10.sp,
                fontFamily = fontFamily,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = themeConfig.textPrimaryColor,
                fontSize = 16.sp,
                fontFamily = fontFamily,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
