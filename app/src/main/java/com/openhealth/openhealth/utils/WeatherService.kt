package com.openhealth.openhealth.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class WeatherData(
    val temperature: Double = 0.0,
    val feelsLike: Double = 0.0,
    val humidity: Int = 0,
    val uvIndex: Double = 0.0,
    val aqi: Int = 0, // Air Quality Index (1-5 scale)
    val isAvailable: Boolean = false
) {
    val aqiLabel: String get() = when (aqi) {
        1 -> "Good"
        2 -> "Fair"
        3 -> "Moderate"
        4 -> "Poor"
        5 -> "Very Poor"
        else -> "Unknown"
    }

    val uvLabel: String get() = when {
        uvIndex < 3 -> "Low"
        uvIndex < 6 -> "Moderate"
        uvIndex < 8 -> "High"
        uvIndex < 11 -> "Very High"
        else -> "Extreme"
    }

    val healthAdvisory: String get() {
        val advisories = mutableListOf<String>()

        // Temperature
        when {
            temperature > 40 -> advisories.add("Extreme heat — avoid outdoor activity, stay hydrated")
            temperature > 35 -> advisories.add("Hot weather — limit outdoor exercise, drink extra water")
            temperature < 0 -> advisories.add("Freezing — dress warmly for outdoor activity")
        }

        // UV
        when {
            uvIndex >= 8 -> advisories.add("Very high UV — avoid sun exposure, wear sunscreen SPF 50+")
            uvIndex >= 6 -> advisories.add("High UV — wear sunscreen and sunglasses outdoors")
            uvIndex >= 3 -> advisories.add("Moderate UV — sunscreen recommended for extended outdoor time")
        }

        // Air Quality
        when (aqi) {
            4 -> advisories.add("Poor air quality — avoid outdoor exercise, stay indoors")
            5 -> advisories.add("Hazardous air — stay indoors, close windows")
            3 -> advisories.add("Moderate air quality — sensitive groups should limit outdoor activity")
        }

        return if (advisories.isEmpty()) "Good conditions for outdoor activity"
        else advisories.joinToString(". ")
    }
}

class WeatherService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun getWeather(latitude: Double, longitude: Double): WeatherData = withContext(Dispatchers.IO) {
        try {
            // Open-Meteo: free, no API key, open source
            val weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=$latitude&longitude=$longitude&current=temperature_2m,relative_humidity_2m,apparent_temperature,uv_index&timezone=auto"
            val airQualityUrl = "https://air-quality-api.open-meteo.com/v1/air-quality?latitude=$latitude&longitude=$longitude&current=european_aqi"

            // Fetch weather
            val weatherRequest = Request.Builder().url(weatherUrl).build()
            val weatherResponse = client.newCall(weatherRequest).execute()
            val weatherBody = weatherResponse.body?.string() ?: ""

            if (!weatherResponse.isSuccessful) {
                Log.e("WeatherService", "Weather API error: ${weatherResponse.code}")
                return@withContext WeatherData()
            }

            val weatherJson = JSONObject(weatherBody)
            val current = weatherJson.getJSONObject("current")
            val temp = current.getDouble("temperature_2m")
            val feelsLike = current.getDouble("apparent_temperature")
            val humidity = current.getInt("relative_humidity_2m")
            val uvIndex = current.getDouble("uv_index")

            // Fetch air quality
            var aqi = 0
            try {
                val aqiRequest = Request.Builder().url(airQualityUrl).build()
                val aqiResponse = client.newCall(aqiRequest).execute()
                val aqiBody = aqiResponse.body?.string() ?: ""
                if (aqiResponse.isSuccessful) {
                    val aqiJson = JSONObject(aqiBody)
                    val aqiValue = aqiJson.getJSONObject("current").getInt("european_aqi")
                    // Convert European AQI (0-500) to 1-5 scale
                    aqi = when {
                        aqiValue <= 20 -> 1
                        aqiValue <= 40 -> 2
                        aqiValue <= 60 -> 3
                        aqiValue <= 80 -> 4
                        else -> 5
                    }
                }
            } catch (e: Exception) {
                Log.w("WeatherService", "Air quality fetch failed: ${e.message}")
            }

            Log.d("WeatherService", "Weather: ${temp}°C, Feels: ${feelsLike}°C, UV: $uvIndex, AQI: $aqi")

            WeatherData(
                temperature = temp,
                feelsLike = feelsLike,
                humidity = humidity,
                uvIndex = uvIndex,
                aqi = aqi,
                isAvailable = true
            )
        } catch (e: Exception) {
            Log.e("WeatherService", "Error: ${e.message}", e)
            WeatherData()
        }
    }
}
