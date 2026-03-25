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
import com.openhealth.openhealth.model.SettingsData
import com.openhealth.openhealth.model.SpeedData
import com.openhealth.openhealth.model.PowerData
import com.openhealth.openhealth.model.NutritionData
import com.openhealth.openhealth.model.HydrationData
import com.openhealth.openhealth.model.MindfulnessSessionData
import com.openhealth.openhealth.utils.HealthConnectManager
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

    // Current selected date for dashboard
    private val _selectedDate = MutableStateFlow(LocalDate.now())
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

    // Cache for metric histories to enable instant display
    private val metricHistoryCache = mutableMapOf<MetricType, MetricHistory>()

    // Scroll position for dashboard (saved when navigating to detail, restored when returning)
    private val _dashboardScrollIndex = MutableStateFlow(0)
    val dashboardScrollIndex: StateFlow<Int> = _dashboardScrollIndex.asStateFlow()

    private val _dashboardScrollOffset = MutableStateFlow(0)
    val dashboardScrollOffset: StateFlow<Int> = _dashboardScrollOffset.asStateFlow()

    // Settings
    private val settingsManager = SettingsManager.getInstance(context)
    val settings: StateFlow<SettingsData> = settingsManager.settings

    // Navigation state for settings screen
    private val _showSettings = MutableStateFlow(false)
    val showSettings: StateFlow<Boolean> = _showSettings.asStateFlow()

    // Required permissions
    val requiredPermissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(Vo2MaxRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class)
    )

    init {
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
                _uiState.value = UiState.Ready(_healthData.value, false)
                refreshData()
            } else {
                _uiState.value = UiState.PermissionsRequired
            }
        }
    }

    // Called from MainActivity when permissions are granted
    fun onPermissionsGranted() {
        viewModelScope.launch {
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
                Log.d("HealthViewModel", "Starting data refresh...")

                // Fetch all health data using HealthConnectManager
                val steps = HealthConnectManager.getTodaySteps()
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
                    leanBodyMass = leanBodyMass
                )

                _healthData.value = newHealthData
                _isLoading.value = false
                
                // Update UI state with new data and loading = false
                _uiState.value = UiState.Ready(newHealthData, false)

                Log.d("HealthViewModel", "Data refresh complete")
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

    fun updateSettings(settings: SettingsData) {
        settingsManager.updateSettings(settings)
    }

    // Date navigation for dashboard
    fun navigateToPreviousDay() {
        _selectedDate.value = _selectedDate.value.minusDays(1)
        refreshDataForDate(_selectedDate.value)
    }

    fun navigateToNextDay() {
        val today = LocalDate.now()
        if (_selectedDate.value.isBefore(today)) {
            _selectedDate.value = _selectedDate.value.plusDays(1)
            refreshDataForDate(_selectedDate.value)
        }
    }

    fun navigateToToday() {
        _selectedDate.value = LocalDate.now()
        refreshDataForDate(_selectedDate.value)
    }

    fun refreshDataForDate(date: LocalDate) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d("HealthViewModel", "Loading data for date: $date")

                // Fetch all health data for the specific date
                val steps = HealthConnectManager.getStepsForDate(date)
                val heartRate = HealthConnectManager.getHeartRateForDate(date)
                val sleep = HealthConnectManager.getSleepForDate(date)
                val exercise = HealthConnectManager.getExerciseForDate(date)
                val vo2Max = HealthConnectManager.getLatestVO2Max() // VO2 Max is typically latest only
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
                    respiratoryRate = respiratoryRate
                )

                _healthData.value = newHealthData
                _isLoading.value = false
                _uiState.value = UiState.Ready(newHealthData, false)

                Log.d("HealthViewModel", "Data refresh complete for date: $date")
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
            // Show cached data immediately if available
            val cachedHistory = metricHistoryCache[metricType]
            if (cachedHistory != null) {
                _metricHistory.value = cachedHistory
                _isMetricDetailLoading.value = false
            } else {
                // No cache - show skeleton loading
                _isMetricDetailLoading.value = true
            }

            try {
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
                }

                // Cache the result and update UI
                metricHistoryCache[metricType] = history
                _metricHistory.value = history
            } catch (e: Exception) {
                Log.e("HealthViewModel", "Error loading metric history: ${e.message}", e)
                // If we have cached data, keep showing it even on error
                if (cachedHistory == null) {
                    _metricHistory.value = null
                }
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
        RESPIRATORY_RATE
    }
}
