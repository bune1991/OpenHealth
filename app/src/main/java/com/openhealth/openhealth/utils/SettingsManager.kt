package com.openhealth.openhealth.utils

import android.content.Context
import android.content.SharedPreferences
import com.openhealth.openhealth.model.SettingsData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsManager private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
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

        // Features
        private const val KEY_SHOW_STEPS_STREAK = "show_steps_streak"

        // Daily Goals
        private const val KEY_STEPS_GOAL = "steps_goal"
        private const val KEY_FLOORS_GOAL = "floors_goal"
        private const val KEY_CALORIES_GOAL = "calories_goal"
        private const val KEY_DISTANCE_GOAL_KM = "distance_goal_km"

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
            stepsGoal = prefs.getInt(KEY_STEPS_GOAL, 10000),
            floorsGoal = prefs.getInt(KEY_FLOORS_GOAL, 10),
            caloriesGoal = prefs.getInt(KEY_CALORIES_GOAL, 500),
            distanceGoalKm = prefs.getFloat(KEY_DISTANCE_GOAL_KM, 5.0f)
        )
    }

    private fun saveSettings(settings: SettingsData) {
        prefs.edit().apply {
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
            putInt(KEY_STEPS_GOAL, settings.stepsGoal)
            putInt(KEY_FLOORS_GOAL, settings.floorsGoal)
            putInt(KEY_CALORIES_GOAL, settings.caloriesGoal)
            putFloat(KEY_DISTANCE_GOAL_KM, settings.distanceGoalKm)

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
