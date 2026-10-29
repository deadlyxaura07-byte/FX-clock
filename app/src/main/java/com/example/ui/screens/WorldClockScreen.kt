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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.data.model.WorldClockCity
import com.example.ui.theme.LocalCustomThemeConfig
import com.example.ui.theme.getClockFontFamily
import com.example.ui.viewmodel.ClockAppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldClockScreen(
    viewModel: ClockAppViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeConfig = LocalCustomThemeConfig.current
    val fontFamily = getClockFontFamily(themeConfig.fontFamily)
    val is24Hour by viewModel.is24Hour.collectAsState()
    val cities by viewModel.worldCities.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

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
                            text = "WORLD CLOCK",
                            color = themeConfig.textPrimaryColor,
                            fontSize = 18.sp,
                            fontFamily = fontFamily,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "REAL IANA TIMEZONES",
                            color = themeConfig.textMutedColor,
                            fontSize = 10.sp,
                            fontFamily = fontFamily,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Cities List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cities, key = { it.id }) { city ->
                    WorldCityCard(
                        city = city,
                        is24Hour = is24Hour,
                        onDelete = { viewModel.removeWorldCity(city) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        // Add City FAB
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = themeConfig.accentColor,
            contentColor = Color.Black,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_city_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add City", modifier = Modifier.size(24.dp))
        }

        if (showAddDialog) {
            AddCityBottomSheet(
                onDismiss = { showAddDialog = false },
                onCitySelected = { city ->
                    viewModel.addWorldCity(city)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
private fun WorldCityCard(
    city: WorldClockCity,
    is24Hour: Boolean,
    onDelete: () -> Unit
) {
    val themeConfig = LocalCustomThemeConfig.current
    val fontFamily = getClockFontFamily(themeConfig.fontFamily)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("world_city_${city.cityName}"),
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
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // City info & offset
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = city.cityName,
                        color = themeConfig.textPrimaryColor,
                        fontSize = 17.sp,
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = city.getDayDifference(),
                        color = themeConfig.accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${city.country} • ${city.getOffsetString()}",
                    color = themeConfig.textMutedColor,
                    fontSize = 12.sp,
                    fontFamily = fontFamily
                )
            }

            // Local Time & Delete
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = city.getFormattedTime(is24Hour),
                        color = themeConfig.textPrimaryColor,
                        fontSize = 28.sp,
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Light
                    )
                    if (!is24Hour) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = city.getAmPm(is24Hour),
                            color = themeConfig.accentColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete City",
                        tint = themeConfig.textMutedColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCityBottomSheet(
    onDismiss: () -> Unit,
    onCitySelected: (WorldClockCity) -> Unit
) {
    val themeConfig = LocalCustomThemeConfig.current
    val fontFamily = getClockFontFamily(themeConfig.fontFamily)

    var searchQuery by remember { mutableStateOf("") }

    val majorCities = remember {
        listOf(
            WorldClockCity("America/New_York", "New York", "United States", "America/New_York"),
            WorldClockCity("Europe/London", "London", "United Kingdom", "Europe/London"),
            WorldClockCity("Asia/Tokyo", "Tokyo", "Japan", "Asia/Tokyo"),
            WorldClockCity("Europe/Paris", "Paris", "France", "Europe/Paris"),
            WorldClockCity("Australia/Sydney", "Sydney", "Australia", "Australia/Sydney"),
            WorldClockCity("Asia/Dubai", "Dubai", "UAE", "Asia/Dubai"),
            WorldClockCity("America/Los_Angeles", "San Francisco", "United States", "America/Los_Angeles"),
            WorldClockCity("Asia/Singapore", "Singapore", "Singapore", "Asia/Singapore"),
            WorldClockCity("Asia/Hong_Kong", "Hong Kong", "China", "Asia/Hong_Kong"),
            WorldClockCity("Europe/Berlin", "Berlin", "Germany", "Europe/Berlin"),
            WorldClockCity("Europe/Zurich", "Zurich", "Switzerland", "Europe/Zurich"),
            WorldClockCity("America/Chicago", "Chicago", "United States", "America/Chicago"),
            WorldClockCity("America/Toronto", "Toronto", "Canada", "America/Toronto"),
            WorldClockCity("Asia/Seoul", "Seoul", "South Korea", "Asia/Seoul"),
            WorldClockCity("Asia/Shanghai", "Shanghai", "China", "Asia/Shanghai"),
            WorldClockCity("Asia/Kolkata", "Mumbai", "India", "Asia/Kolkata"),
            WorldClockCity("America/Sao_Paulo", "Sao Paulo", "Brazil", "America/Sao_Paulo"),
            WorldClockCity("Pacific/Auckland", "Auckland", "New Zealand", "Pacific/Auckland")
        )
    }

    val filtered = remember(searchQuery) {
        if (searchQuery.isBlank()) majorCities
        else majorCities.filter {
            it.cityName.contains(searchQuery, ignoreCase = true) ||
            it.country.contains(searchQuery, ignoreCase = true)
        }
    }

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
                text = "ADD WORLD CITY",
                color = themeConfig.textPrimaryColor,
                fontSize = 18.sp,
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search city or country") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = themeConfig.textMutedColor)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("world_city_search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = themeConfig.textPrimaryColor,
                    unfocusedTextColor = themeConfig.textPrimaryColor,
                    focusedBorderColor = themeConfig.accentColor,
                    unfocusedBorderColor = themeConfig.borderColor
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtered) { city ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(themeConfig.surfaceElevatedColor, RoundedCornerShape(10.dp))
                            .clickable { onCitySelected(city) }
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = city.cityName,
                                    color = themeConfig.textPrimaryColor,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = city.country,
                                    color = themeConfig.textMutedColor,
                                    fontSize = 12.sp
                                )
                            }
                            Text(
                                text = city.getOffsetString(),
                                color = themeConfig.accentColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
