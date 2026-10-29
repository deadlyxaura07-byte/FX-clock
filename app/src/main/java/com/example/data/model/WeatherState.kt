package com.example.data.model

data class WeatherState(
    val cityName: String = "Tokyo",
    val latitude: Double = 35.6762,
    val longitude: Double = 139.6503,
    val temperatureC: Double = 21.0,
    val feelsLikeC: Double = 20.5,
    val minTempC: Double = 16.0,
    val maxTempC: Double = 23.0,
    val humidityPercent: Int = 55,
    val windSpeedKmh: Double = 12.0,
    val weatherCode: Int = 0,
    val conditionText: String = "Clear Sky",
    val isCelsius: Boolean = true,
    val lastUpdatedMillis: Long = System.currentTimeMillis(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    fun displayTemperature(): String {
        return if (isCelsius) {
            "${Math.round(temperatureC)}°C"
        } else {
            val f = (temperatureC * 9 / 5) + 32
            "${Math.round(f)}°F"
        }
    }

    fun displayFeelsLike(): String {
        return if (isCelsius) {
            "${Math.round(feelsLikeC)}°C"
        } else {
            val f = (feelsLikeC * 9 / 5) + 32
            "${Math.round(f)}°F"
        }
    }

    fun displayMinMax(): String {
        return if (isCelsius) {
            "L: ${Math.round(minTempC)}°  H: ${Math.round(maxTempC)}°"
        } else {
            val minF = (minTempC * 9 / 5) + 32
            val maxF = (maxTempC * 9 / 5) + 32
            "L: ${Math.round(minF)}°  H: ${Math.round(maxF)}°"
        }
    }

    fun displayWind(): String {
        return if (isCelsius) {
            "${Math.round(windSpeedKmh)} km/h"
        } else {
            val mph = windSpeedKmh * 0.621371
            "${Math.round(mph)} mph"
        }
    }

    companion object {
        fun getWeatherDescription(code: Int): String {
            return when (code) {
                0 -> "Clear Sky"
                1, 2, 3 -> "Partly Cloudy"
                45, 48 -> "Foggy"
                51, 53, 55 -> "Drizzle"
                61, 63, 65 -> "Rain"
                71, 73, 75 -> "Snowfall"
                77 -> "Snow Grains"
                80, 81, 82 -> "Rain Showers"
                85, 86 -> "Snow Showers"
                95 -> "Thunderstorm"
                96, 99 -> "Severe Thunderstorm"
                else -> "Fair"
            }
        }
    }
}
