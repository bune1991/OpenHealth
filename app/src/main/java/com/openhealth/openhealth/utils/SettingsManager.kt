package com.openhealth.openhealth.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.openhealth.openhealth.model.SettingsData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsManager private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val securePrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "openhealth_secure",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<SettingsData> = _settings.asStateFlow()

    companion object {
        private const val PREFS_NAME = "openhealth_settings"

        // Activity metrics
        private const val KEY_SHOW_STEPS = "show_steps"
        private const val KEY_SHOW_DISTANCE = "show_distance"
        private const val KEY_SHOW_FLOORS = "show_floors"

        // Calories metrics
        private const val KEY_SHOW_CALORIES = "show_calories"
        private const val KEY_SHOW_ACTIVE_CALORIES = "show_active_calories"

        // Heart metrics
        private const val KEY_SHOW_HEART_RATE = "show_heart_rate"
        private const val KEY_SHOW_RESTING_HEART_RATE = "show_resting_heart_rate"

        // Body metrics
        private const val KEY_SHOW_WEIGHT = "show_weight"
        private const val KEY_SHOW_BODY_FAT = "show_body_fat"
        private const val KEY_SHOW_BMR = "show_bmr"
        private const val KEY_SHOW_BODY_WATER = "show_body_water"
        private const val KEY_SHOW_BONE_MASS = "show_bone_mass"
        private const val KEY_SHOW_LEAN_BODY_MASS = "show_lean_body_mass"

        // Sleep metrics
        private const val KEY_SHOW_SLEEP = "show_sleep"

        // Exercise metrics
        private const val KEY_SHOW_EXERCISE = "show_exercise"
        private const val KEY_SHOW_VO2_MAX = "show_vo2_max"

        // Vitals
        private const val KEY_SHOW_BLOOD_GLUCOSE = "show_blood_glucose"
        private const val KEY_SHOW_BLOOD_PRESSURE = "show_blood_pressure"
        private const val KEY_SHOW_BODY_TEMPERATURE = "show_body_temperature"
        private const val KEY_SHOW_HRV = "show_hrv"
        private const val KEY_SHOW_OXYGEN_SATURATION = "show_oxygen_saturation"
        private const val KEY_SHOW_RESPIRATORY_RATE = "show_respiratory_rate"

        // Additional metrics
        private const val KEY_SHOW_SPEED = "show_speed"
        private const val KEY_SHOW_POWER = "show_power"
        private const val KEY_SHOW_NUTRITION = "show_nutrition"
        private const val KEY_SHOW_HYDRATION = "show_hydration"
        private const val KEY_SHOW_MINDFULNESS = "show_mindfulness"

        // AI Insights
        private const val KEY_AI_PROVIDER = "ai_provider"
        private const val KEY_AI_API_KEY = "ai_api_key"
        private const val KEY_AI_CLAUDE_KEY = "ai_claude_key"
        private const val KEY_AI_GEMINI_KEY = "ai_gemini_key"
        private const val KEY_AI_CHATGPT_KEY = "ai_chatgpt_key"
        private const val KEY_AI_CUSTOM_KEY = "ai_custom_key"
        private const val KEY_AI_CUSTOM_URL = "ai_custom_url"
        private const val KEY_AI_CUSTOM_MODEL = "ai_custom_model"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_THEME_NAME = "theme_name"
        private const val KEY_WEATHER_ENABLED = "weather_enabled"
        private const val KEY_WEATHER_CITY = "weather_city"
        private const val KEY_WEATHER_LAT = "weather_lat"
        private const val KEY_WEATHER_LON = "weather_lon"

        // Features
        private const val KEY_SHOW_STEPS_STREAK = "show_steps_streak"
        private const val KEY_DAILY_SUMMARY_NOTIFICATION = "daily_summary_notification"
        private const val KEY_WEEKLY_AI_SUMMARY = "weekly_ai_summary"

        // Daily Goals
        private const val KEY_STEPS_GOAL = "steps_goal"
        private const val KEY_FLOORS_GOAL = "floors_goal"
        private const val KEY_CALORIES_GOAL = "calories_goal"
        private const val KEY_DISTANCE_GOAL_KM = "distance_goal_km"
        private const val KEY_WEIGHT_TARGET_KG = "weight_target_kg"
        private const val KEY_HYDRATION_GOAL_ML = "hydration_goal_ml"
        private const val KEY_HAPTIC_FEEDBACK = "haptic_feedback"
        private const val KEY_HEALTH_CHAT = "health_chat"
        private const val KEY_CHAT_BUBBLE_MODE = "chat_bubble_mode"

        @Volatile
        private var instance: SettingsManager? = null

        fun getInstance(context: Context): SettingsManager {
            return instance ?: synchronized(this) {
                instance ?: SettingsManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    private fun loadSettings(): SettingsData {
        return SettingsData(
            // AI Insights
            aiProvider = try { com.openhealth.openhealth.model.AiProvider.valueOf(prefs.getString(KEY_AI_PROVIDER, "NONE") ?: "NONE") } catch (e: Exception) { com.openhealth.openhealth.model.AiProvider.NONE },
            aiApiKey = securePrefs.getString(KEY_AI_API_KEY, "") ?: "",
            aiClaudeKey = securePrefs.getString(KEY_AI_CLAUDE_KEY, "") ?: "",
            aiGeminiKey = securePrefs.getString(KEY_AI_GEMINI_KEY, "") ?: "",
            aiChatgptKey = securePrefs.getString(KEY_AI_CHATGPT_KEY, "") ?: "",
            aiCustomKey = securePrefs.getString(KEY_AI_CUSTOM_KEY, "") ?: "",
            aiCustomUrl = securePrefs.getString(KEY_AI_CUSTOM_URL, "") ?: "",
            aiCustomModel = securePrefs.getString(KEY_AI_CUSTOM_MODEL, "") ?: "",
            // App State
            onboardingCompleted = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false),
            // Theme
            themeName = prefs.getString(KEY_THEME_NAME, "nocturne") ?: "nocturne",
            // Weather
            weatherEnabled = prefs.getBoolean(KEY_WEATHER_ENABLED, false),
            weatherCity = prefs.getString(KEY_WEATHER_CITY, "") ?: "",
            weatherLat = prefs.getFloat(KEY_WEATHER_LAT, 0f).toDouble(),
            weatherLon = prefs.getFloat(KEY_WEATHER_LON, 0f).toDouble(),
            // Activity metrics
            showSteps = prefs.getBoolean(KEY_SHOW_STEPS, true),
            showDistance = prefs.getBoolean(KEY_SHOW_DISTANCE, true),
            showFloors = prefs.getBoolean(KEY_SHOW_FLOORS, true),

            // Calories metrics
            showCalories = prefs.getBoolean(KEY_SHOW_CALORIES, true),
            showActiveCalories = prefs.getBoolean(KEY_SHOW_ACTIVE_CALORIES, true),

            // Heart metrics
            showHeartRate = prefs.getBoolean(KEY_SHOW_HEART_RATE, true),
            showRestingHeartRate = prefs.getBoolean(KEY_SHOW_RESTING_HEART_RATE, true),

            // Body metrics
            showWeight = prefs.getBoolean(KEY_SHOW_WEIGHT, true),
            showBodyFat = prefs.getBoolean(KEY_SHOW_BODY_FAT, true),
            showBMR = prefs.getBoolean(KEY_SHOW_BMR, true),
            showBodyWater = prefs.getBoolean(KEY_SHOW_BODY_WATER, true),
            showBoneMass = prefs.getBoolean(KEY_SHOW_BONE_MASS, true),
            showLeanBodyMass = prefs.getBoolean(KEY_SHOW_LEAN_BODY_MASS, true),

            // Sleep metrics
            showSleep = prefs.getBoolean(KEY_SHOW_SLEEP, true),

            // Exercise metrics
            showExercise = prefs.getBoolean(KEY_SHOW_EXERCISE, true),
            showVO2Max = prefs.getBoolean(KEY_SHOW_VO2_MAX, true),

            // Vitals
            showBloodGlucose = prefs.getBoolean(KEY_SHOW_BLOOD_GLUCOSE, true),
            showBloodPressure = prefs.getBoolean(KEY_SHOW_BLOOD_PRESSURE, true),
            showBodyTemperature = prefs.getBoolean(KEY_SHOW_BODY_TEMPERATURE, true),
            showHRV = prefs.getBoolean(KEY_SHOW_HRV, true),
            showOxygenSaturation = prefs.getBoolean(KEY_SHOW_OXYGEN_SATURATION, true),
            showRespiratoryRate = prefs.getBoolean(KEY_SHOW_RESPIRATORY_RATE, true),

            // Additional metrics
            showSpeed = prefs.getBoolean(KEY_SHOW_SPEED, true),
            showPower = prefs.getBoolean(KEY_SHOW_POWER, true),
            showNutrition = prefs.getBoolean(KEY_SHOW_NUTRITION, true),
            showHydration = prefs.getBoolean(KEY_SHOW_HYDRATION, true),
            showMindfulness = prefs.getBoolean(KEY_SHOW_MINDFULNESS, true),

            // Daily Goals
            showStepsStreak = prefs.getBoolean(KEY_SHOW_STEPS_STREAK, true),
            dailySummaryNotification = prefs.getBoolean(KEY_DAILY_SUMMARY_NOTIFICATION, true),
            weeklyAiSummary = prefs.getBoolean(KEY_WEEKLY_AI_SUMMARY, false),
            stepsGoal = prefs.getInt(KEY_STEPS_GOAL, 10000),
            floorsGoal = prefs.getInt(KEY_FLOORS_GOAL, 10),
            caloriesGoal = prefs.getInt(KEY_CALORIES_GOAL, 500),
            distanceGoalKm = prefs.getFloat(KEY_DISTANCE_GOAL_KM, 5.0f),
            weightTargetKg = prefs.getFloat(KEY_WEIGHT_TARGET_KG, 70.0f),
            hydrationGoalMl = prefs.getInt(KEY_HYDRATION_GOAL_ML, 2500),
            hapticFeedback = prefs.getBoolean(KEY_HAPTIC_FEEDBACK, true),
            healthChatEnabled = prefs.getBoolean(KEY_HEALTH_CHAT, true),
            chatBubbleMode = prefs.getBoolean(KEY_CHAT_BUBBLE_MODE, false)
        )
    }

    private fun saveSettings(settings: SettingsData) {
        // Save sensitive API keys to encrypted storage
        securePrefs.edit().apply {
            putString(KEY_AI_API_KEY, settings.aiApiKey)
            putString(KEY_AI_CLAUDE_KEY, settings.aiClaudeKey)
            putString(KEY_AI_GEMINI_KEY, settings.aiGeminiKey)
            putString(KEY_AI_CHATGPT_KEY, settings.aiChatgptKey)
            putString(KEY_AI_CUSTOM_KEY, settings.aiCustomKey)
            putString(KEY_AI_CUSTOM_URL, settings.aiCustomUrl)
            putString(KEY_AI_CUSTOM_MODEL, settings.aiCustomModel)
            apply()
        }

        prefs.edit().apply {
            // AI Provider (not sensitive — just an enum name)
            putString(KEY_AI_PROVIDER, settings.aiProvider.name)
            // App State
            putBoolean(KEY_ONBOARDING_COMPLETED, settings.onboardingCompleted)
            // Theme
            putString(KEY_THEME_NAME, settings.themeName)
            // Weather
            putBoolean(KEY_WEATHER_ENABLED, settings.weatherEnabled)
            putString(KEY_WEATHER_CITY, settings.weatherCity)
            putFloat(KEY_WEATHER_LAT, settings.weatherLat.toFloat())
            putFloat(KEY_WEATHER_LON, settings.weatherLon.toFloat())
            // Activity metrics
            putBoolean(KEY_SHOW_STEPS, settings.showSteps)
            putBoolean(KEY_SHOW_DISTANCE, settings.showDistance)
            putBoolean(KEY_SHOW_FLOORS, settings.showFloors)

            // Calories metrics
            putBoolean(KEY_SHOW_CALORIES, settings.showCalories)
            putBoolean(KEY_SHOW_ACTIVE_CALORIES, settings.showActiveCalories)

            // Heart metrics
            putBoolean(KEY_SHOW_HEART_RATE, settings.showHeartRate)
            putBoolean(KEY_SHOW_RESTING_HEART_RATE, settings.showRestingHeartRate)

            // Body metrics
            putBoolean(KEY_SHOW_WEIGHT, settings.showWeight)
            putBoolean(KEY_SHOW_BODY_FAT, settings.showBodyFat)
            putBoolean(KEY_SHOW_BMR, settings.showBMR)
            putBoolean(KEY_SHOW_BODY_WATER, settings.showBodyWater)
            putBoolean(KEY_SHOW_BONE_MASS, settings.showBoneMass)
            putBoolean(KEY_SHOW_LEAN_BODY_MASS, settings.showLeanBodyMass)

            // Sleep metrics
            putBoolean(KEY_SHOW_SLEEP, settings.showSleep)

            // Exercise metrics
            putBoolean(KEY_SHOW_EXERCISE, settings.showExercise)
            putBoolean(KEY_SHOW_VO2_MAX, settings.showVO2Max)

            // Vitals
            putBoolean(KEY_SHOW_BLOOD_GLUCOSE, settings.showBloodGlucose)
            putBoolean(KEY_SHOW_BLOOD_PRESSURE, settings.showBloodPressure)
            putBoolean(KEY_SHOW_BODY_TEMPERATURE, settings.showBodyTemperature)
            putBoolean(KEY_SHOW_HRV, settings.showHRV)
            putBoolean(KEY_SHOW_OXYGEN_SATURATION, settings.showOxygenSaturation)
            putBoolean(KEY_SHOW_RESPIRATORY_RATE, settings.showRespiratoryRate)

            // Additional metrics
            putBoolean(KEY_SHOW_SPEED, settings.showSpeed)
            putBoolean(KEY_SHOW_POWER, settings.showPower)
            putBoolean(KEY_SHOW_NUTRITION, settings.showNutrition)
            putBoolean(KEY_SHOW_HYDRATION, settings.showHydration)
            putBoolean(KEY_SHOW_MINDFULNESS, settings.showMindfulness)

            // Daily Goals
            putBoolean(KEY_SHOW_STEPS_STREAK, settings.showStepsStreak)
            putBoolean(KEY_DAILY_SUMMARY_NOTIFICATION, settings.dailySummaryNotification)
            putBoolean(KEY_WEEKLY_AI_SUMMARY, settings.weeklyAiSummary)
            putInt(KEY_STEPS_GOAL, settings.stepsGoal)
            putInt(KEY_FLOORS_GOAL, settings.floorsGoal)
            putInt(KEY_CALORIES_GOAL, settings.caloriesGoal)
            putFloat(KEY_DISTANCE_GOAL_KM, settings.distanceGoalKm)
            putFloat(KEY_WEIGHT_TARGET_KG, settings.weightTargetKg)
            putInt(KEY_HYDRATION_GOAL_ML, settings.hydrationGoalMl)
            putBoolean(KEY_HAPTIC_FEEDBACK, settings.hapticFeedback)
            putBoolean(KEY_HEALTH_CHAT, settings.healthChatEnabled)
            putBoolean(KEY_CHAT_BUBBLE_MODE, settings.chatBubbleMode)

            apply()
        }
    }

    fun updateSettings(settings: SettingsData) {
        saveSettings(settings)
        _settings.value = settings
    }

    fun resetToDefaults() {
        val defaultSettings = SettingsData.DEFAULT
        saveSettings(defaultSettings)
        _settings.value = defaultSettings
    }
}
