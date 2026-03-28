package com.openhealth.openhealth.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.Vo2MaxRecord
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.openhealth.openhealth.model.CaloriesData
import com.openhealth.openhealth.model.DailyDataPoint
import com.openhealth.openhealth.model.DistanceData
import com.openhealth.openhealth.model.ExerciseData
import com.openhealth.openhealth.model.ExerciseSession
import com.openhealth.openhealth.model.HealthData
import com.openhealth.openhealth.model.HeartRateData
import com.openhealth.openhealth.model.HeartRateReading
import com.openhealth.openhealth.model.MetricHistory
import com.openhealth.openhealth.model.SleepData
import com.openhealth.openhealth.model.SleepSession
import com.openhealth.openhealth.model.SleepStage
import com.openhealth.openhealth.model.StepsData
import com.openhealth.openhealth.model.Vo2MaxData
import com.openhealth.openhealth.model.FloorsData
import com.openhealth.openhealth.model.RestingHeartRateData
import com.openhealth.openhealth.model.BodyFatData
import com.openhealth.openhealth.model.WeightData
import com.openhealth.openhealth.model.BasalMetabolicRateData
import com.openhealth.openhealth.model.BodyWaterMassData
import com.openhealth.openhealth.model.BoneMassData
import com.openhealth.openhealth.model.LeanBodyMassData
import com.openhealth.openhealth.model.SkinTemperatureData
import com.openhealth.openhealth.model.SettingsData
import com.openhealth.openhealth.model.SpeedData
import com.openhealth.openhealth.model.PowerData
import com.openhealth.openhealth.model.NutritionData
import com.openhealth.openhealth.model.HydrationData
import com.openhealth.openhealth.model.MindfulnessSessionData
import com.openhealth.openhealth.utils.AiHealthService
import com.openhealth.openhealth.utils.AiInsightCache
import com.openhealth.openhealth.utils.HealthConnectManager
import com.openhealth.openhealth.utils.HealthPromptBuilder
import com.openhealth.openhealth.utils.PermissionManager
import com.openhealth.openhealth.utils.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.roundToInt

class HealthViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private var healthConnectClient: HealthConnectClient? = null

    private val _uiState = MutableStateFlow<UiState>(UiState.HealthConnectNotAvailable)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _healthData = MutableStateFlow(HealthData())
    val healthData: StateFlow<HealthData> = _healthData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Kuwait timezone for consistent date handling
    private val kuwaitZone = ZoneId.systemDefault()

    // Current selected date for dashboard
    private val _selectedDate = MutableStateFlow(LocalDate.now(kuwaitZone))
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    // Selected metric for detail screen
    private val _selectedMetric = MutableStateFlow<MetricType?>(null)
    val selectedMetric: StateFlow<MetricType?> = _selectedMetric.asStateFlow()

    // Metric history for detail screen
    private val _metricHistory = MutableStateFlow<MetricHistory?>(null)
    val metricHistory: StateFlow<MetricHistory?> = _metricHistory.asStateFlow()

    // Loading state specifically for metric detail (separate from dashboard loading)
    private val _isMetricDetailLoading = MutableStateFlow(false)
    val isMetricDetailLoading: StateFlow<Boolean> = _isMetricDetailLoading.asStateFlow()

    // In-memory cache: date -> HealthData (avoids repeated API calls during date navigation)
    private val healthDataCache = mutableMapOf<LocalDate, HealthData>()

    // Steps history for calendar rings on Dashboard
    private val _stepsCalendarData = MutableStateFlow<List<com.openhealth.openhealth.model.DailyDataPoint>>(emptyList())
    val stepsCalendarData: StateFlow<List<com.openhealth.openhealth.model.DailyDataPoint>> = _stepsCalendarData.asStateFlow()

    // Steps streak (consecutive days meeting goal)
    private val _stepsStreak = MutableStateFlow(0)
    val stepsStreak: StateFlow<Int> = _stepsStreak.asStateFlow()

    // Scroll position for dashboard (saved when navigating to detail, restored when returning)
    private val _dashboardScrollIndex = MutableStateFlow(0)
    val dashboardScrollIndex: StateFlow<Int> = _dashboardScrollIndex.asStateFlow()

    private val _dashboardScrollOffset = MutableStateFlow(0)
    val dashboardScrollOffset: StateFlow<Int> = _dashboardScrollOffset.asStateFlow()

    // Dashboard card expanded states (persist across navigation)
    private val _bodyExpanded = MutableStateFlow(false)
    val bodyExpanded: StateFlow<Boolean> = _bodyExpanded.asStateFlow()
    private val _vitalsExpanded = MutableStateFlow(false)
    val vitalsExpanded: StateFlow<Boolean> = _vitalsExpanded.asStateFlow()

    fun setBodyExpanded(expanded: Boolean) { _bodyExpanded.value = expanded }
    fun setVitalsExpanded(expanded: Boolean) { _vitalsExpanded.value = expanded }

    // Weather
    private val _weatherData = MutableStateFlow(com.openhealth.openhealth.utils.WeatherData())
    val weatherData: StateFlow<com.openhealth.openhealth.utils.WeatherData> = _weatherData.asStateFlow()
    private val weatherService = com.openhealth.openhealth.utils.WeatherService()

    // AI Insights
    private val _showAiInsights = MutableStateFlow(false)
    val showAiInsights: StateFlow<Boolean> = _showAiInsights.asStateFlow()
    private val _aiInsightText = MutableStateFlow<String?>(null)
    val aiInsightText: StateFlow<String?> = _aiInsightText.asStateFlow()
    private val _aiInsightLoading = MutableStateFlow(false)
    val aiInsightLoading: StateFlow<Boolean> = _aiInsightLoading.asStateFlow()
    private val _aiInsightError = MutableStateFlow<String?>(null)
    val aiInsightError: StateFlow<String?> = _aiInsightError.asStateFlow()
    private val aiHealthService = AiHealthService()
    private lateinit var aiInsightCache: AiInsightCache

    // Settings
    private val settingsManager = SettingsManager.getInstance(context)
    val settings: StateFlow<SettingsData> = settingsManager.settings

    // Navigation state for stress detail screen
    private val _showStressDetail = MutableStateFlow(false)
    val showStressDetail: StateFlow<Boolean> = _showStressDetail.asStateFlow()

    // Navigation state for reports screen
    fun completeOnboarding() {
        settingsManager.updateSettings(settingsManager.settings.value.copy(onboardingCompleted = true))
    }

    private val _showReports = MutableStateFlow(false)
    val showReports: StateFlow<Boolean> = _showReports.asStateFlow()

    private val _reportsData = MutableStateFlow(ReportsData())
    val reportsData: StateFlow<ReportsData> = _reportsData.asStateFlow()

    // Navigation state for settings screen
    private val _showSettings = MutableStateFlow(false)
    val showSettings: StateFlow<Boolean> = _showSettings.asStateFlow()

    // Navigation state for readiness detail screen
    private val _showReadinessDetail = MutableStateFlow(false)
    val showReadinessDetail: StateFlow<Boolean> = _showReadinessDetail.asStateFlow()

    // Hydration tracking
    private val _showHydration = MutableStateFlow(false)
    val showHydration: StateFlow<Boolean> = _showHydration.asStateFlow()

    private val _hydrationEntries = MutableStateFlow<List<com.openhealth.openhealth.screens.HydrationEntry>>(emptyList())
    val hydrationEntries: StateFlow<List<com.openhealth.openhealth.screens.HydrationEntry>> = _hydrationEntries.asStateFlow()

    private val _hydrationDailyTotal = MutableStateFlow(0)
    val hydrationDailyTotal: StateFlow<Int> = _hydrationDailyTotal.asStateFlow()

    private val hydrationPrefs by lazy {
        context.getSharedPreferences("hydration_prefs", android.content.Context.MODE_PRIVATE)
    }

    // Required permissions - use all permissions from HealthConnectManager
    val requiredPermissions = HealthConnectManager.PERMISSIONS

    init {
        // Initialize PermissionManager
        PermissionManager.init(context)
        checkHealthConnectAvailability()
    }

    private fun checkHealthConnectAvailability() {
        viewModelScope.launch {
            val availabilityStatus = HealthConnectClient.getSdkStatus(context)
            when (availabilityStatus) {
                HealthConnectClient.SDK_AVAILABLE -> {
                    // Initialize HealthConnectManager
                    HealthConnectManager.checkForHealthConnectInstalled(context)
                    checkPermissions()
                }
                else -> {
                    _uiState.value = UiState.HealthConnectNotAvailable
                }
            }
        }
    }

    private fun checkPermissions() {
        viewModelScope.launch {
            val granted = HealthConnectManager.checkPermissions()
            if (granted) {
                // Save permission state to SharedPreferences
                PermissionManager.setPermissionsGranted(true)
                _uiState.value = UiState.Ready(_healthData.value, false)
                refreshData()
            } else {
                // Check if permissions were previously granted
                // If yes, don't show permission screen again (user may have revoked some permissions)
                // If no, show permission screen for first-time users
                if (PermissionManager.hasPermissionsBeenGranted()) {
                    // Permissions were granted before, user may have revoked some
                    // Still show the app but with limited data
                    _uiState.value = UiState.Ready(_healthData.value, false)
                    refreshData()
                } else {
                    // First time - show permission screen
                    _uiState.value = UiState.PermissionsRequired
                }
            }
        }
    }

    // Called from MainActivity when permissions are granted
    fun onPermissionsGranted() {
        viewModelScope.launch {
            // Save permission state
            PermissionManager.setPermissionsGranted(true)
            // Re-initialize HealthConnectManager
            HealthConnectManager.checkForHealthConnectInstalled(context)
            _uiState.value = UiState.Ready(_healthData.value, false)
            refreshData()
        }
    }

    // Called from MainActivity when permissions are denied
    fun onPermissionsDenied() {
        _uiState.value = UiState.PermissionsRequired
    }

    // Called from MainActivity to show permissions required screen
    fun onPermissionsRequired() {
        _uiState.value = UiState.PermissionsRequired
    }

    // Called from MainActivity when Health Connect is not available
    fun onHealthConnectNotAvailable() {
        _uiState.value = UiState.HealthConnectNotAvailable
    }

    fun openHealthConnectPlayStore() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("market://details?id=com.google.android.apps.healthdata")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun refreshData() {
        viewModelScope.launch {
            _isLoading.value = true
            
            // Ensure UI state is Ready before trying to update it
            if (_uiState.value !is UiState.Ready) {
                _uiState.value = UiState.Ready(_healthData.value, true)
            } else {
                updateUiState { copy(isLoading = true) }
            }

            try {
                // Clear today's cache entry so we get fresh data
                val today = LocalDate.now(kuwaitZone)
                healthDataCache.remove(today)
                Log.d("HealthViewModel", "Starting data refresh...")

                // Fetch all health data using HealthConnectManager
                val currentSettings = settingsManager.settings.value
                val steps = HealthConnectManager.getTodaySteps().copy(goal = currentSettings.stepsGoal.toLong())
                Log.d("HealthViewModel", "Steps fetched: ${steps.count}")

                val heartRate = HealthConnectManager.getTodayHeartRate()
                Log.d("HealthViewModel", "Heart rate fetched: ${heartRate.currentBpm}")

                val sleep = HealthConnectManager.getLastNightSleep()
                Log.d("HealthViewModel", "Sleep fetched: ${sleep.totalDuration}")

                val exercise = HealthConnectManager.getTodayExercise()
                Log.d("HealthViewModel", "Exercise sessions: ${exercise.sessions.size}")

                val vo2Max = HealthConnectManager.getLatestVO2Max()
                Log.d("HealthViewModel", "VO2 Max: ${vo2Max.value}")

                // Fetch new metrics
                val calories = HealthConnectManager.getTodayCalories()
                Log.d("HealthViewModel", "Calories - total: ${calories.totalBurned}")

                val distance = HealthConnectManager.getTodayDistance()
                Log.d("HealthViewModel", "Distance: ${distance.kilometers}km")

                val floors = HealthConnectManager.getTodayFloors()
                Log.d("HealthViewModel", "Floors: ${floors.count}")

                val restingHeartRate = HealthConnectManager.getRestingHeartRate()
                Log.d("HealthViewModel", "Resting HR: ${restingHeartRate.bpm}")

                val bodyFat = HealthConnectManager.getBodyFat()
                Log.d("HealthViewModel", "Body fat: ${bodyFat.percentage}%")

                val weight = HealthConnectManager.getWeight()
                Log.d("HealthViewModel", "Weight: ${weight.kilograms}kg")

                // Fetch body composition metrics
                val basalMetabolicRate = HealthConnectManager.getBasalMetabolicRate()
                Log.d("HealthViewModel", "BMR: ${basalMetabolicRate.caloriesPerDay} kcal/day")

                val bodyWaterMass = HealthConnectManager.getBodyWaterMass()
                Log.d("HealthViewModel", "Body water mass: ${bodyWaterMass.kilograms}kg")

                val boneMass = HealthConnectManager.getBoneMass()
                Log.d("HealthViewModel", "Bone mass: ${boneMass.kilograms}kg")

                val leanBodyMass = HealthConnectManager.getLeanBodyMass()
                Log.d("HealthViewModel", "Lean body mass: ${leanBodyMass.kilograms}kg")

                // Fetch vitals data
                val bloodGlucose = HealthConnectManager.getBloodGlucose()
                Log.d("HealthViewModel", "Blood glucose: ${bloodGlucose.levelMgPerDl} mg/dL")

                val bloodPressure = HealthConnectManager.getBloodPressure()
                Log.d("HealthViewModel", "Blood pressure: ${bloodPressure.systolicMmHg}/${bloodPressure.diastolicMmHg} mmHg")

                val bodyTemperature = HealthConnectManager.getBodyTemperature()
                Log.d("HealthViewModel", "Body temperature: ${bodyTemperature.temperatureCelsius}°C")

                val heartRateVariability = HealthConnectManager.getHeartRateVariability()
                Log.d("HealthViewModel", "HRV: ${heartRateVariability.rmssdMs} ms")

                val oxygenSaturation = HealthConnectManager.getOxygenSaturation()
                Log.d("HealthViewModel", "SpO2: ${oxygenSaturation.percentage}%")

                val respiratoryRate = HealthConnectManager.getRespiratoryRate()
                Log.d("HealthViewModel", "Respiratory rate: ${respiratoryRate.ratePerMinute} breaths/min")

                val skinTemperature = HealthConnectManager.getSkinTemperature()
                Log.d("HealthViewModel", "Skin temperature: ${skinTemperature.temperatureCelsius}°C")

                val nutrition = HealthConnectManager.getTodayNutrition()
                Log.d("HealthViewModel", "Nutrition: ${nutrition.calories} kcal")

                // Mindfulness: disabled until Health Connect supports the permission on all devices
                val mindfulness = com.openhealth.openhealth.model.MindfulnessSessionData()

                val newHealthData = HealthData(
                    steps = steps,
                    heartRate = heartRate,
                    sleep = sleep,
                    exercise = exercise,
                    vo2Max = vo2Max,
                    calories = calories,
                    distance = distance,
                    floors = floors,
                    restingHeartRate = restingHeartRate,
                    bodyFat = bodyFat,
                    weight = weight,
                    basalMetabolicRate = basalMetabolicRate,
                    bodyWaterMass = bodyWaterMass,
                    boneMass = boneMass,
                    leanBodyMass = leanBodyMass,
                    bloodGlucose = bloodGlucose,
                    bloodPressure = bloodPressure,
                    bodyTemperature = bodyTemperature,
                    heartRateVariability = heartRateVariability,
                    oxygenSaturation = oxygenSaturation,
                    respiratoryRate = respiratoryRate,
                    skinTemperature = skinTemperature,
                    nutrition = nutrition,
                    mindfulness = mindfulness
                )

                _healthData.value = newHealthData
                _isLoading.value = false

                // Cache today's data (reuses 'today' from above)
                healthDataCache[today] = newHealthData

                // Update UI state with new data and loading = false
                _uiState.value = UiState.Ready(newHealthData, false)

                Log.d("HealthViewModel", "Data refresh complete, cached for $today")

                // Comprehensive data summary
                Log.d("OpenHealth_Summary", "=== Dashboard Data Summary ===")
                Log.d("OpenHealth_Summary", "Steps: ${steps.count} (goal: ${steps.goal})")
                Log.d("OpenHealth_Summary", "Heart Rate: ${heartRate.currentBpm} bpm (range: ${heartRate.minBpm}-${heartRate.maxBpm}, resting: ${heartRate.restingBpm})")
                Log.d("OpenHealth_Summary", "Sleep: ${String.format("%.1f", sleep.totalDuration?.toMinutes()?.div(60.0) ?: 0.0)}h (sessions: ${sleep.sessions.size})")
                Log.d("OpenHealth_Summary", "Calories: ${String.format("%.0f", calories.totalBurned)} kcal (active: ${String.format("%.0f", calories.activeBurned)})")
                Log.d("OpenHealth_Summary", "Distance: ${String.format("%.2f", distance.kilometers)} km")
                Log.d("OpenHealth_Summary", "Floors: ${floors.count}")
                Log.d("OpenHealth_Summary", "Resting HR: ${restingHeartRate.bpm} bpm")
                Log.d("OpenHealth_Summary", "Weight: ${weight.kilograms} kg")
                Log.d("OpenHealth_Summary", "Body Fat: ${bodyFat.percentage}%")
                Log.d("OpenHealth_Summary", "BMR: ${basalMetabolicRate.caloriesPerDay} kcal/day")
                Log.d("OpenHealth_Summary", "Body Water: ${bodyWaterMass.kilograms} kg")
                Log.d("OpenHealth_Summary", "Bone Mass: ${boneMass.kilograms} kg")
                Log.d("OpenHealth_Summary", "Lean Mass: ${leanBodyMass.kilograms} kg")
                Log.d("OpenHealth_Summary", "HRV: ${heartRateVariability.rmssdMs} ms")
                Log.d("OpenHealth_Summary", "SpO2: ${oxygenSaturation.percentage}%")
                Log.d("OpenHealth_Summary", "Respiratory Rate: ${respiratoryRate.ratePerMinute} breaths/min")
                Log.d("OpenHealth_Summary", "Skin Temp: ${skinTemperature.temperatureCelsius}°C")
                Log.d("OpenHealth_Summary", "Nutrition: ${nutrition.calories} kcal (P:${nutrition.proteinGrams}g C:${nutrition.carbsGrams}g F:${nutrition.fatGrams}g)")
                Log.d("OpenHealth_Summary", "Exercise: ${exercise.sessionCount} sessions, ${exercise.totalDuration?.toMinutes() ?: 0} min")
                val hrvVal = heartRateVariability.rmssdMs ?: 0.0
                val stressLevel = ((80.0 - hrvVal.coerceIn(10.0, 80.0)) / 70.0 * 100).toInt().coerceIn(0, 100)
                val stressLabel = when { stressLevel < 25 -> "Low"; stressLevel < 50 -> "Moderate"; stressLevel < 75 -> "High"; else -> "Very High" }
                Log.d("OpenHealth_Summary", "Stress: $stressLevel ($stressLabel) [HRV: ${String.format("%.0f", hrvVal)} ms]")
                Log.d("OpenHealth_Summary", "=== End Summary ===")

                // Save data for widget and update it
                val sleepMinutesTotal = sleep.totalDuration?.toMinutes() ?: 0
                com.openhealth.openhealth.widget.HealthWidgetReceiver.saveWidgetData(
                    context,
                    steps = steps.count,
                    stepsGoal = currentSettings.stepsGoal.toLong(),
                    heartRate = heartRate.currentBpm ?: 0,
                    sleepHours = (sleepMinutesTotal / 60).toInt(),
                    sleepMinutes = (sleepMinutesTotal % 60).toInt()
                )
                com.openhealth.openhealth.widget.HealthWidgetReceiver.updateAllWidgets(context)

                // Load steps history for calendar rings and streak
                try {
                    val stepsHistory = HealthConnectManager.getStepsHistory()
                    val historyData = stepsHistory.allHistoricalData ?: emptyList()
                    _stepsCalendarData.value = historyData

                    // Log last 7 days steps
                    val last7 = historyData.takeLast(7)
                    Log.d("OpenHealth_Summary", "Steps last 7 days: ${last7.joinToString { "${it.date.dayOfMonth}=${it.value.toLong()}" }}")
                    Log.d("OpenHealth_Summary", "Steps 30-day total: ${historyData.sumOf { it.value }.toLong()}, avg: ${if (historyData.isNotEmpty()) historyData.map { it.value }.average().toLong() else 0}")

                    // Calculate streak: consecutive days ending at today/yesterday where steps >= goal
                    val goal = currentSettings.stepsGoal
                    val dataMap = historyData.associateBy { it.date }
                    var streak = 0
                    var checkDate = today
                    // If today's steps haven't met goal yet, start from yesterday
                    if ((dataMap[today]?.value ?: 0.0) < goal) {
                        checkDate = today.minusDays(1)
                    }
                    while (true) {
                        val daySteps = dataMap[checkDate]?.value ?: 0.0
                        if (daySteps >= goal) {
                            streak++
                            checkDate = checkDate.minusDays(1)
                        } else break
                    }
                    _stepsStreak.value = streak
                    Log.d("OpenHealth_Summary", "Steps streak: $streak days (goal: $goal)")
                } catch (e: Exception) {
                    Log.e("HealthViewModel", "Error loading steps calendar data: ${e.message}")
                }

                // Fetch weather if enabled
                try {
                    val ws = currentSettings
                    if (ws.weatherEnabled && ws.weatherLat != 0.0 && ws.weatherLon != 0.0) {
                        val weather = weatherService.getWeather(ws.weatherLat, ws.weatherLon)
                        _weatherData.value = weather
                        Log.d("OpenHealth_Weather", "${ws.weatherCity}: ${weather.temperature}°C, UV: ${weather.uvIndex}, AQI: ${weather.aqi} (${weather.aqiLabel})")
                    }
                } catch (e: Exception) {
                    Log.e("OpenHealth_Weather", "Error: ${e.message}")
                }
            } catch (e: Exception) {
                Log.e("HealthViewModel", "Error refreshing data: ${e.message}", e)
                _isLoading.value = false
                
                // Ensure UI state reflects that loading is done
                val currentState = _uiState.value
                if (currentState is UiState.Ready) {
                    _uiState.value = currentState.copy(isLoading = false)
                }
            }
        }
    }

    // Select a metric and load its history
    fun selectMetric(metricType: MetricType) {
        // Clear any existing metric history to force fresh data fetch
        // This ensures Dashboard and DetailScreen show consistent values
        _metricHistory.value = null
        _selectedMetric.value = metricType
        loadMetricHistory(metricType)
    }

    // Clear selected metric (go back to dashboard)
    fun clearSelectedMetric() {
        _selectedMetric.value = null
        _metricHistory.value = null
    }

    // Save dashboard scroll position before navigating to detail
    fun saveDashboardScrollPosition(index: Int, offset: Int = 0) {
        _dashboardScrollIndex.value = index
        _dashboardScrollOffset.value = offset
    }

    // Get the saved scroll position (called when returning to dashboard)
    fun getScrollPosition(): Pair<Int, Int> {
        return Pair(_dashboardScrollIndex.value, _dashboardScrollOffset.value)
    }

    // Clear the saved scroll position
    fun clearScrollPosition() {
        _dashboardScrollIndex.value = 0
        _dashboardScrollOffset.value = 0
    }

    // Settings navigation
    fun showSettings() {
        _showSettings.value = true
    }

    fun hideSettings() {
        _showSettings.value = false
    }

    // Readiness detail navigation
    fun showReadinessDetail() {
        _showReadinessDetail.value = true
    }

    fun hideReadinessDetail() {
        _showReadinessDetail.value = false
    }

    // AI Insights navigation
    fun showAiInsights() {
        if (!::aiInsightCache.isInitialized) aiInsightCache = AiInsightCache(context)
        _showAiInsights.value = true
        fetchAiInsight(forceRefresh = false)
    }

    fun hideAiInsights() {
        _showAiInsights.value = false
    }

    fun refreshAiInsight() {
        fetchAiInsight(forceRefresh = true)
    }

    private fun fetchAiInsight(forceRefresh: Boolean) {
        val currentSettings = settingsManager.settings.value
        val activeKey = when (currentSettings.aiProvider) {
            com.openhealth.openhealth.model.AiProvider.CLAUDE -> currentSettings.aiClaudeKey
            com.openhealth.openhealth.model.AiProvider.GEMINI -> currentSettings.aiGeminiKey
            com.openhealth.openhealth.model.AiProvider.CHATGPT -> currentSettings.aiChatgptKey
            com.openhealth.openhealth.model.AiProvider.CUSTOM -> currentSettings.aiCustomKey
            com.openhealth.openhealth.model.AiProvider.NONE -> ""
        }

        if (currentSettings.aiProvider == com.openhealth.openhealth.model.AiProvider.NONE || (activeKey.isBlank() && currentSettings.aiProvider != com.openhealth.openhealth.model.AiProvider.CUSTOM)) {
            _aiInsightError.value = "Configure an AI provider and API key in Settings"
            return
        }

        val today = LocalDate.now(kuwaitZone)

        if (!forceRefresh) {
            val cached = aiInsightCache.getCached(today)
            if (cached != null) {
                _aiInsightText.value = cached
                _aiInsightError.value = null
                return
            }
        }

        viewModelScope.launch {
            _aiInsightLoading.value = true
            _aiInsightError.value = null

            val prompt = HealthPromptBuilder.buildDailySummaryPrompt(_healthData.value)
            Log.d("AiInsights", "Prompt length: ${prompt.length} chars")

            val result = aiHealthService.getInsights(
                provider = currentSettings.aiProvider,
                apiKey = activeKey,
                healthSummaryPrompt = prompt,
                customUrl = currentSettings.aiCustomUrl,
                customModel = currentSettings.aiCustomModel
            )

            result.onSuccess { text: String ->
                _aiInsightText.value = text
                aiInsightCache.save(today, text, currentSettings.aiProvider)
                Log.d("AiInsights", "Success: ${text.take(100)}...")
            }.onFailure { err: Throwable ->
                _aiInsightError.value = err.message ?: "Unknown error"
                Log.e("AiInsights", "Error: ${err.message}")
            }

            _aiInsightLoading.value = false
        }
    }

    // Stress detail navigation
    fun showStressDetail() {
        _showStressDetail.value = true
    }

    fun hideStressDetail() {
        _showStressDetail.value = false
    }

    // Reports navigation
    fun showReports() {
        _showReports.value = true
        loadReportsData()
    }

    fun hideReports() {
        _showReports.value = false
    }

    // Hydration navigation & data
    fun showHydration() {
        _showHydration.value = true
        loadHydrationEntries()
    }

    fun hideHydration() {
        _showHydration.value = false
    }

    fun addWaterEntry(amountMl: Int) {
        val entry = com.openhealth.openhealth.screens.HydrationEntry(
            amount = amountMl,
            time = System.currentTimeMillis()
        )
        val updated = _hydrationEntries.value + entry
        _hydrationEntries.value = updated
        _hydrationDailyTotal.value = updated.sumOf { it.amount }
        saveHydrationEntries(updated)
    }

    private fun loadHydrationEntries() {
        val today = LocalDate.now(kuwaitZone)
        val key = "hydration_${today}"
        val json = hydrationPrefs.getString(key, null) ?: return
        try {
            val arr = org.json.JSONArray(json)
            val entries = mutableListOf<com.openhealth.openhealth.screens.HydrationEntry>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                entries.add(
                    com.openhealth.openhealth.screens.HydrationEntry(
                        amount = obj.getInt("amount"),
                        time = obj.getLong("time"),
                        type = obj.optString("type", "Glass of Water")
                    )
                )
            }
            _hydrationEntries.value = entries
            _hydrationDailyTotal.value = entries.sumOf { it.amount }
        } catch (e: Exception) {
            Log.e("HealthViewModel", "Failed to load hydration entries", e)
        }
    }

    private fun saveHydrationEntries(entries: List<com.openhealth.openhealth.screens.HydrationEntry>) {
        val today = LocalDate.now(kuwaitZone)
        val key = "hydration_${today}"
        val arr = org.json.JSONArray()
        entries.forEach { entry ->
            val obj = org.json.JSONObject().apply {
                put("amount", entry.amount)
                put("time", entry.time)
                put("type", entry.type)
            }
            arr.put(obj)
        }
        hydrationPrefs.edit().putString(key, arr.toString()).apply()
    }

    private fun loadReportsData() {
        viewModelScope.launch {
            _reportsData.value = ReportsData(isLoading = true)
            try {
                val today = LocalDate.now(kuwaitZone)
                val startOfThisWeek = today.with(java.time.DayOfWeek.MONDAY)
                val startOfLastWeek = startOfThisWeek.minusWeeks(1)
                val endOfLastWeek = startOfThisWeek.minusDays(1)

                // Fetch all histories in parallel
                // Fetch sequentially (Health Connect doesn't support concurrent reads well)
                val stepsHistory = HealthConnectManager.getStepsHistory()
                val heartRateHistory = HealthConnectManager.getHeartRateHistory()
                val sleepHistory = HealthConnectManager.getSleepHistory()
                val caloriesHistory = HealthConnectManager.getCaloriesHistory()
                val exerciseHistory = HealthConnectManager.getExerciseHistory()
                val distanceHistory = HealthConnectManager.getDistanceHistory()

                fun List<DailyDataPoint>.inRange(from: LocalDate, to: LocalDate) =
                    filter { it.date in from..to }

                fun List<DailyDataPoint>.total() = sumOf { it.value }
                fun List<DailyDataPoint>.avg() = if (isNotEmpty()) map { it.value }.average() else 0.0

                val summaries = listOf(
                    MetricSummary("Steps",
                        stepsHistory.last30Days.inRange(startOfThisWeek, today).total(),
                        stepsHistory.last30Days.inRange(startOfLastWeek, endOfLastWeek).total(),
                        stepsHistory.last30Days.total(), "steps"),
                    MetricSummary("Sleep",
                        sleepHistory.last30Days.inRange(startOfThisWeek, today).avg(),
                        sleepHistory.last30Days.inRange(startOfLastWeek, endOfLastWeek).avg(),
                        sleepHistory.last30Days.avg(), "hrs", isBetterWhenHigher = true, isAverage = true),
                    MetricSummary("Calories",
                        caloriesHistory.last30Days.inRange(startOfThisWeek, today).total(),
                        caloriesHistory.last30Days.inRange(startOfLastWeek, endOfLastWeek).total(),
                        caloriesHistory.last30Days.total(), "kcal"),
                    MetricSummary("Exercise",
                        exerciseHistory.last30Days.inRange(startOfThisWeek, today).total(),
                        exerciseHistory.last30Days.inRange(startOfLastWeek, endOfLastWeek).total(),
                        exerciseHistory.last30Days.total(), "min"),
                    MetricSummary("Heart Rate",
                        heartRateHistory.last30Days.inRange(startOfThisWeek, today).avg(),
                        heartRateHistory.last30Days.inRange(startOfLastWeek, endOfLastWeek).avg(),
                        heartRateHistory.last30Days.avg(), "bpm", isBetterWhenHigher = false, isAverage = true),
                    MetricSummary("Distance",
                        distanceHistory.last30Days.inRange(startOfThisWeek, today).total(),
                        distanceHistory.last30Days.inRange(startOfLastWeek, endOfLastWeek).total(),
                        distanceHistory.last30Days.total(), "km")
                )

                val weeklyStepsChart = stepsHistory.last30Days.inRange(startOfThisWeek, today)

                _reportsData.value = ReportsData(
                    summaries = summaries,
                    weeklyStepsChart = weeklyStepsChart,
                    isLoading = false
                )
            } catch (e: Exception) {
                Log.e("HealthViewModel", "Error loading reports: ${e.message}", e)
                _reportsData.value = ReportsData(isLoading = false)
            }
        }
    }

    fun updateSettings(settings: SettingsData) {
        settingsManager.updateSettings(settings)
    }

    // Date navigation for dashboard
    fun navigateToPreviousDay() {
        _selectedDate.value = _selectedDate.value.minusDays(1)
        refreshDataForDate(_selectedDate.value)
    }

    fun navigateToNextDay() {
        val today = LocalDate.now(kuwaitZone)
        if (_selectedDate.value.isBefore(today)) {
            _selectedDate.value = _selectedDate.value.plusDays(1)
            refreshDataForDate(_selectedDate.value)
        }
    }

    fun navigateToToday() {
        val today = LocalDate.now(kuwaitZone)
        healthDataCache.remove(today)  // Always fetch fresh for today
        _selectedDate.value = today
        refreshDataForDate(today)
    }

    fun navigateToDate(date: LocalDate) {
        _selectedDate.value = date
        refreshDataForDate(date)
    }

    fun refreshDataForDate(date: LocalDate) {
        viewModelScope.launch {
            // Check cache first - no API calls if already loaded
            val cached = healthDataCache[date]
            if (cached != null) {
                Log.d("HealthViewModel", "Cache hit for date: $date")
                _healthData.value = cached
                _uiState.value = UiState.Ready(cached, false)
                return@launch
            }

            _isLoading.value = true
            try {
                Log.d("HealthViewModel", "Cache miss, loading data for date: $date")

                // Fetch all health data for the specific date
                val dateSettings = settingsManager.settings.value
                val steps = HealthConnectManager.getStepsForDate(date).copy(goal = dateSettings.stepsGoal.toLong())
                val heartRate = HealthConnectManager.getHeartRateForDate(date)
                val sleep = HealthConnectManager.getSleepForDate(date)
                val exercise = HealthConnectManager.getExerciseForDate(date)
                val vo2Max = HealthConnectManager.getLatestVO2Max()
                val calories = HealthConnectManager.getCaloriesForDate(date)
                val distance = HealthConnectManager.getDistanceForDate(date)
                val floors = HealthConnectManager.getFloorsForDate(date)
                val restingHeartRate = HealthConnectManager.getRestingHeartRateForDate(date)
                val bodyFat = HealthConnectManager.getBodyFatForDate(date)
                val weight = HealthConnectManager.getWeightForDate(date)
                val basalMetabolicRate = HealthConnectManager.getBasalMetabolicRateForDate(date)
                val bodyWaterMass = HealthConnectManager.getBodyWaterMassForDate(date)
                val boneMass = HealthConnectManager.getBoneMassForDate(date)
                val leanBodyMass = HealthConnectManager.getLeanBodyMassForDate(date)
                val bloodGlucose = HealthConnectManager.getBloodGlucoseForDate(date)
                val bloodPressure = HealthConnectManager.getBloodPressureForDate(date)
                val bodyTemperature = HealthConnectManager.getBodyTemperatureForDate(date)
                val heartRateVariability = HealthConnectManager.getHeartRateVariabilityForDate(date)
                val oxygenSaturation = HealthConnectManager.getOxygenSaturationForDate(date)
                val respiratoryRate = HealthConnectManager.getRespiratoryRateForDate(date)
                val skinTemperature = HealthConnectManager.getSkinTemperatureForDate(date)
                val nutrition = HealthConnectManager.getNutritionForDate(date)

                val newHealthData = HealthData(
                    steps = steps,
                    heartRate = heartRate,
                    sleep = sleep,
                    exercise = exercise,
                    vo2Max = vo2Max,
                    calories = calories,
                    distance = distance,
                    floors = floors,
                    restingHeartRate = restingHeartRate,
                    bodyFat = bodyFat,
                    weight = weight,
                    basalMetabolicRate = basalMetabolicRate,
                    bodyWaterMass = bodyWaterMass,
                    boneMass = boneMass,
                    leanBodyMass = leanBodyMass,
                    bloodGlucose = bloodGlucose,
                    bloodPressure = bloodPressure,
                    bodyTemperature = bodyTemperature,
                    heartRateVariability = heartRateVariability,
                    oxygenSaturation = oxygenSaturation,
                    respiratoryRate = respiratoryRate,
                    skinTemperature = skinTemperature,
                    nutrition = nutrition
                )

                // Cache the result
                healthDataCache[date] = newHealthData

                _healthData.value = newHealthData
                _isLoading.value = false
                _uiState.value = UiState.Ready(newHealthData, false)

                Log.d("HealthViewModel", "Data loaded and cached for date: $date")
            } catch (e: Exception) {
                Log.e("HealthViewModel", "Error refreshing data for date: ${e.message}", e)
                _isLoading.value = false
                val currentState = _uiState.value
                if (currentState is UiState.Ready) {
                    _uiState.value = currentState.copy(isLoading = false)
                }
            }
        }
    }

    private fun loadMetricHistory(metricType: MetricType) {
        viewModelScope.launch {
            // Always show loading state - no caching
            _isMetricDetailLoading.value = true
            _metricHistory.value = null

            try {
                Log.d("HealthViewModel", "Loading fresh metric history for: $metricType")
                
                val history = when (metricType) {
                    MetricType.STEPS -> HealthConnectManager.getStepsHistory()
                    MetricType.DISTANCE -> HealthConnectManager.getDistanceHistory()
                    MetricType.CALORIES -> HealthConnectManager.getCaloriesHistory()
                    MetricType.ACTIVE_CALORIES -> HealthConnectManager.getActiveCaloriesHistory()
                    MetricType.FLOORS -> HealthConnectManager.getFloorsHistory()
                    MetricType.HEART_RATE -> HealthConnectManager.getHeartRateHistory()
                    MetricType.RESTING_HEART_RATE -> HealthConnectManager.getRestingHeartRateHistory()
                    MetricType.SLEEP -> HealthConnectManager.getSleepHistory()
                    MetricType.VO2_MAX -> HealthConnectManager.getVo2MaxHistory()
                    MetricType.BODY_FAT -> HealthConnectManager.getBodyFatHistory()
                    MetricType.WEIGHT -> HealthConnectManager.getWeightHistory()
                    MetricType.BASAL_METABOLIC_RATE -> HealthConnectManager.getBasalMetabolicRateHistory()
                    MetricType.BODY_WATER_MASS -> HealthConnectManager.getBodyWaterMassHistory()
                    MetricType.BONE_MASS -> HealthConnectManager.getBoneMassHistory()
                    MetricType.LEAN_BODY_MASS -> HealthConnectManager.getLeanBodyMassHistory()
                    MetricType.BLOOD_GLUCOSE -> HealthConnectManager.getBloodGlucoseHistory()
                    MetricType.BLOOD_PRESSURE -> HealthConnectManager.getBloodPressureHistory()
                    MetricType.BODY_TEMPERATURE -> HealthConnectManager.getBodyTemperatureHistory()
                    MetricType.HEART_RATE_VARIABILITY -> HealthConnectManager.getHeartRateVariabilityHistory()
                    MetricType.OXYGEN_SATURATION -> HealthConnectManager.getOxygenSaturationHistory()
                    MetricType.RESPIRATORY_RATE -> HealthConnectManager.getRespiratoryRateHistory()
                    MetricType.SKIN_TEMPERATURE -> HealthConnectManager.getSkinTemperatureHistory()
                    MetricType.EXERCISE -> HealthConnectManager.getExerciseHistory()
                    MetricType.NUTRITION -> HealthConnectManager.getNutritionHistory()
                }

                // Update UI with fresh data - no caching
                _metricHistory.value = history
                Log.d("HealthViewModel", "Metric history loaded: ${history.todayValue} ${history.unit}")
            } catch (e: Exception) {
                Log.e("HealthViewModel", "Error loading metric history: ${e.message}", e)
                _metricHistory.value = null
            } finally {
                _isMetricDetailLoading.value = false
            }
        }
    }

    private fun updateUiState(update: UiState.Ready.() -> UiState.Ready) {
        val currentState = _uiState.value
        if (currentState is UiState.Ready) {
            _uiState.value = currentState.update()
        }
    }

    sealed class UiState {
        object HealthConnectNotAvailable : UiState()
        object PermissionsRequired : UiState()
        data class Ready(
            val healthData: HealthData = HealthData(),
            val isLoading: Boolean = false
        ) : UiState()
    }

    enum class MetricType {
        STEPS,
        DISTANCE,
        CALORIES,
        ACTIVE_CALORIES,
        FLOORS,
        HEART_RATE,
        RESTING_HEART_RATE,
        SLEEP,
        VO2_MAX,
        BODY_FAT,
        WEIGHT,
        BASAL_METABOLIC_RATE,
        BODY_WATER_MASS,
        BONE_MASS,
        LEAN_BODY_MASS,
        BLOOD_GLUCOSE,
        BLOOD_PRESSURE,
        BODY_TEMPERATURE,
        HEART_RATE_VARIABILITY,
        OXYGEN_SATURATION,
        RESPIRATORY_RATE,
        SKIN_TEMPERATURE,
        EXERCISE,
        NUTRITION
    }
}

data class MetricSummary(
    val label: String,
    val thisWeekValue: Double,
    val lastWeekValue: Double,
    val monthValue: Double,
    val unit: String,
    val isBetterWhenHigher: Boolean = true,
    val isAverage: Boolean = false
)

data class ReportsData(
    val summaries: List<MetricSummary> = emptyList(),
    val weeklyStepsChart: List<com.openhealth.openhealth.model.DailyDataPoint> = emptyList(),
    val isLoading: Boolean = true
)
