package com.example.data.network

import com.example.data.model.WeatherState
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class OpenMeteoCurrent(
    @Json(name = "temperature_2m") val temperature2m: Double?,
    @Json(name = "relative_humidity_2m") val relativeHumidity2m: Double?,
    @Json(name = "apparent_temperature") val apparentTemperature: Double?,
    @Json(name = "weather_code") val weatherCode: Int?,
    @Json(name = "wind_speed_10m") val windSpeed10m: Double?
)

@JsonClass(generateAdapter = true)
data class OpenMeteoDaily(
    @Json(name = "temperature_2m_max") val temperatureMax: List<Double>?,
    @Json(name = "temperature_2m_min") val temperatureMin: List<Double>?
)

@JsonClass(generateAdapter = true)
data class OpenMeteoResponse(
    @Json(name = "current") val current: OpenMeteoCurrent?,
    @Json(name = "daily") val daily: OpenMeteoDaily?
)

@JsonClass(generateAdapter = true)
data class GeocodingResult(
    @Json(name = "name") val name: String,
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "country") val country: String?
)

@JsonClass(generateAdapter = true)
data class GeocodingResponse(
    @Json(name = "results") val results: List<GeocodingResult>?
)

interface WeatherApi {
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m",
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min",
        @Query("timezone") timezone: String = "auto"
    ): OpenMeteoResponse

    @GET("https://geocoding-api.open-meteo.com/v1/search")
    suspend fun searchCity(
        @Query("name") name: String,
        @Query("count") count: Int = 5,
        @Query("language") language: String = "en",
        @Query("format") format: String = "json"
    ): GeocodingResponse
}

object WeatherClient {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val api: WeatherApi = retrofit.create(WeatherApi::class.java)

    suspend fun fetchWeather(cityName: String, lat: Double, lon: Double): Result<WeatherState> = withContext(Dispatchers.IO) {
        try {
            val response = api.getForecast(latitude = lat, longitude = lon)
            val current = response.current
            val daily = response.daily

            val temp = current?.temperature2m ?: 20.0
            val feelsLike = current?.apparentTemperature ?: temp
            val humidity = current?.relativeHumidity2m?.toInt() ?: 50
            val wind = current?.windSpeed10m ?: 10.0
            val code = current?.weatherCode ?: 0
            val max = daily?.temperatureMax?.firstOrNull() ?: (temp + 3.0)
            val min = daily?.temperatureMin?.firstOrNull() ?: (temp - 3.0)

            Result.success(
                WeatherState(
                    cityName = cityName,
                    latitude = lat,
                    longitude = lon,
                    temperatureC = temp,
                    feelsLikeC = feelsLike,
                    minTempC = min,
                    maxTempC = max,
                    humidityPercent = humidity,
                    windSpeedKmh = wind,
                    weatherCode = code,
                    conditionText = WeatherState.getWeatherDescription(code),
                    lastUpdatedMillis = System.currentTimeMillis(),
                    isLoading = false,
                    errorMessage = null
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchLocations(query: String): Result<List<GeocodingResult>> = withContext(Dispatchers.IO) {
        try {
            val response = api.searchCity(name = query)
            Result.success(response.results ?: emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
