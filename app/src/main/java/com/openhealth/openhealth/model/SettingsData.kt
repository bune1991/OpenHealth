package com.openhealth.openhealth.model

enum class AiProvider {
    NONE, CLAUDE, GEMINI, CHATGPT, CUSTOM, ON_DEVICE
}

data class SettingsData(
    // AI Health Insights
    val aiProvider: AiProvider = AiProvider.NONE,
    val aiApiKey: String = "",
    val aiClaudeKey: String = "",
    val aiGeminiKey: String = "",
    val aiChatgptKey: String = "",
    val aiCustomKey: String = "",
    val aiCustomUrl: String = "",
    val aiCustomModel: String = "",

    // App State
    val onboardingCompleted: Boolean = false,

    // Theme
    val themeName: String = "nocturne",

    // Weather
    val weatherEnabled: Boolean = false,
    val weatherCity: String = "",
    val weatherLat: Double = 0.0,
    val weatherLon: Double = 0.0,
    // Activity metrics
    val showSteps: Boolean = true,
    val showDistance: Boolean = true,
    val showFloors: Boolean = true,

    // Calories metrics
    val showCalories: Boolean = true,
    val showActiveCalories: Boolean = true,

    // Heart metrics
    val showHeartRate: Boolean = true,
    val showRestingHeartRate: Boolean = true,

    // Body metrics
    val showWeight: Boolean = true,
    val showBodyFat: Boolean = true,
    val showBMR: Boolean = true,
    val showBodyWater: Boolean = true,
    val showBoneMass: Boolean = true,
    val showLeanBodyMass: Boolean = true,

    // Sleep metrics
    val showSleep: Boolean = true,

    // Exercise metrics
    val showExercise: Boolean = true,
    val showVO2Max: Boolean = true,

    // Vitals
    val showBloodGlucose: Boolean = true,
    val showBloodPressure: Boolean = true,
    val showBodyTemperature: Boolean = true,
    val showHRV: Boolean = true,
    val showOxygenSaturation: Boolean = true,
    val showRespiratoryRate: Boolean = true,
    val showSkinTemperature: Boolean = true,

    // Additional metrics
    val showSpeed: Boolean = true,
    val showPower: Boolean = true,
    val showNutrition: Boolean = true,
    val showHydration: Boolean = true,
    val showMindfulness: Boolean = true,

    // Features
    val showStepsStreak: Boolean = true,
    val dailySummaryNotification: Boolean = true,
    val weeklyAiSummary: Boolean = false,

    // Daily Goals
    val stepsGoal: Int = 10000,
    val floorsGoal: Int = 10,
    val caloriesGoal: Int = 500,
    val distanceGoalKm: Float = 5.0f,
    val weightTargetKg: Float = 70.0f,
    val hydrationGoalMl: Int = 2500,
    val hapticFeedback: Boolean = true,
    val healthChatEnabled: Boolean = true,
    val chatBubbleMode: Boolean = false
) {
    companion object {
        val DEFAULT = SettingsData()
    }
}

// Extension function to check if a metric should be shown based on settings and data availability
fun SettingsData.shouldShowMetric(
    metricType: MetricType,
    hasData: Boolean
): Boolean {
    if (!hasData) return false

    return when (metricType) {
        MetricType.STEPS -> showSteps
        MetricType.DISTANCE -> showDistance
        MetricType.FLOORS -> showFloors
        MetricType.CALORIES -> showCalories
        MetricType.ACTIVE_CALORIES -> showActiveCalories
        MetricType.HEART_RATE -> showHeartRate
        MetricType.RESTING_HEART_RATE -> showRestingHeartRate
        MetricType.WEIGHT -> showWeight
        MetricType.BODY_FAT -> showBodyFat
        MetricType.BASAL_METABOLIC_RATE -> showBMR
        MetricType.BODY_WATER_MASS -> showBodyWater
        MetricType.BONE_MASS -> showBoneMass
        MetricType.LEAN_BODY_MASS -> showLeanBodyMass
        MetricType.SLEEP -> showSleep
        MetricType.VO2_MAX -> showVO2Max
        MetricType.BLOOD_GLUCOSE -> showBloodGlucose
        MetricType.BLOOD_PRESSURE -> showBloodPressure
        MetricType.BODY_TEMPERATURE -> showBodyTemperature
        MetricType.HEART_RATE_VARIABILITY -> showHRV
        MetricType.OXYGEN_SATURATION -> showOxygenSaturation
        MetricType.RESPIRATORY_RATE -> showRespiratoryRate
        MetricType.SKIN_TEMPERATURE -> showSkinTemperature
        MetricType.SPEED -> showSpeed
        MetricType.POWER -> showPower
        MetricType.NUTRITION -> showNutrition
        MetricType.HYDRATION -> showHydration
        MetricType.MINDFULNESS -> showMindfulness
    }
}

// Metric type enum for settings
enum class MetricType {
    STEPS,
    DISTANCE,
    FLOORS,
    CALORIES,
    ACTIVE_CALORIES,
    HEART_RATE,
    RESTING_HEART_RATE,
    WEIGHT,
    BODY_FAT,
    BASAL_METABOLIC_RATE,
    BODY_WATER_MASS,
    BONE_MASS,
    LEAN_BODY_MASS,
    SLEEP,
    VO2_MAX,
    BLOOD_GLUCOSE,
    BLOOD_PRESSURE,
    BODY_TEMPERATURE,
    HEART_RATE_VARIABILITY,
    OXYGEN_SATURATION,
    RESPIRATORY_RATE,
    SKIN_TEMPERATURE,
    SPEED,
    POWER,
    NUTRITION,
    HYDRATION,
    MINDFULNESS
}

// Extension to get display name for metric type
fun MetricType.displayName(): String = when (this) {
    MetricType.STEPS -> "Steps"
    MetricType.DISTANCE -> "Distance"
    MetricType.FLOORS -> "Floors"
    MetricType.CALORIES -> "Calories"
    MetricType.ACTIVE_CALORIES -> "Active Calories"
    MetricType.HEART_RATE -> "Heart Rate"
    MetricType.RESTING_HEART_RATE -> "Resting Heart Rate"
    MetricType.WEIGHT -> "Weight"
    MetricType.BODY_FAT -> "Body Fat"
    MetricType.BASAL_METABOLIC_RATE -> "BMR"
    MetricType.BODY_WATER_MASS -> "Body Water"
    MetricType.BONE_MASS -> "Bone Mass"
    MetricType.LEAN_BODY_MASS -> "Lean Body Mass"
    MetricType.SLEEP -> "Sleep"
    MetricType.VO2_MAX -> "VO2 Max"
    MetricType.BLOOD_GLUCOSE -> "Blood Glucose"
    MetricType.BLOOD_PRESSURE -> "Blood Pressure"
    MetricType.BODY_TEMPERATURE -> "Body Temperature"
    MetricType.HEART_RATE_VARIABILITY -> "HRV"
    MetricType.OXYGEN_SATURATION -> "SpO2"
    MetricType.RESPIRATORY_RATE -> "Respiratory Rate"
    MetricType.SKIN_TEMPERATURE -> "Skin Temperature"
    MetricType.SPEED -> "Speed"
    MetricType.POWER -> "Power"
    MetricType.NUTRITION -> "Nutrition"
    MetricType.HYDRATION -> "Hydration"
    MetricType.MINDFULNESS -> "Mindfulness"
}

// Extension to get category for metric type
fun MetricType.category(): String = when (this) {
    MetricType.STEPS, MetricType.DISTANCE, MetricType.FLOORS -> "Activity"
    MetricType.CALORIES, MetricType.ACTIVE_CALORIES -> "Calories"
    MetricType.HEART_RATE, MetricType.RESTING_HEART_RATE -> "Heart"
    MetricType.WEIGHT, MetricType.BODY_FAT, MetricType.BASAL_METABOLIC_RATE,
    MetricType.BODY_WATER_MASS, MetricType.BONE_MASS, MetricType.LEAN_BODY_MASS -> "Body"
    MetricType.SLEEP -> "Sleep"
    MetricType.VO2_MAX -> "Exercise"
    MetricType.BLOOD_GLUCOSE, MetricType.BLOOD_PRESSURE, MetricType.BODY_TEMPERATURE,
    MetricType.HEART_RATE_VARIABILITY, MetricType.OXYGEN_SATURATION, MetricType.RESPIRATORY_RATE,
    MetricType.SKIN_TEMPERATURE -> "Vitals"
    MetricType.SPEED, MetricType.POWER -> "Performance"
    MetricType.NUTRITION, MetricType.HYDRATION -> "Nutrition"
    MetricType.MINDFULNESS -> "Wellness"
}
