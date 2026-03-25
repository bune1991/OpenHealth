package com.openhealth.openhealth.model

import java.time.Duration
import java.time.Instant
import java.time.LocalDate

data class HealthData(
    val steps: StepsData = StepsData(),
    val heartRate: HeartRateData = HeartRateData(),
    val sleep: SleepData = SleepData(),
    val exercise: ExerciseData = ExerciseData(),
    val vo2Max: Vo2MaxData = Vo2MaxData(),
    val calories: CaloriesData = CaloriesData(),
    val distance: DistanceData = DistanceData(),
    val floors: FloorsData = FloorsData(),
    val restingHeartRate: RestingHeartRateData = RestingHeartRateData(),
    val bodyFat: BodyFatData = BodyFatData(),
    val weight: WeightData = WeightData(),
    val basalMetabolicRate: BasalMetabolicRateData = BasalMetabolicRateData(),
    val bodyWaterMass: BodyWaterMassData = BodyWaterMassData(),
    val boneMass: BoneMassData = BoneMassData(),
    val leanBodyMass: LeanBodyMassData = LeanBodyMassData(),
    // Vitals
    val bloodGlucose: BloodGlucoseData = BloodGlucoseData(),
    val bloodPressure: BloodPressureData = BloodPressureData(),
    val bodyTemperature: BodyTemperatureData = BodyTemperatureData(),
    val heartRateVariability: HeartRateVariabilityData = HeartRateVariabilityData(),
    val oxygenSaturation: OxygenSaturationData = OxygenSaturationData(),
    val respiratoryRate: RespiratoryRateData = RespiratoryRateData(),
    // Additional metrics
    val speed: SpeedData = SpeedData(),
    val power: PowerData = PowerData(),
    val nutrition: NutritionData = NutritionData(),
    val hydration: HydrationData = HydrationData(),
    val mindfulness: MindfulnessSessionData = MindfulnessSessionData()
)

data class HealthSummary(
    val steps: StepsData,
    val heartRate: HeartRateData,
    val sleep: SleepData,
    val weight: WeightData
)

// Daily data point for history
data class DailyDataPoint(
    val date: LocalDate,
    val value: Double,
    val unit: String
)

// Metric history for detail screen
data class MetricHistory(
    val todayValue: Double,
    val unit: String,
    val last30Days: List<DailyDataPoint>,
    val monthlyAverage: Double,
    val bestDay: DailyDataPoint?,
    val allHistoricalData: List<DailyDataPoint> = emptyList(),
    val sleepStages: SleepStagesData? = null
)

// Sleep stages data for detail screen
data class SleepStagesData(
    val deepSleepMinutes: Long,
    val lightSleepMinutes: Long,
    val remSleepMinutes: Long,
    val awakeMinutes: Long
) {
    val totalMinutes: Long
        get() = deepSleepMinutes + lightSleepMinutes + remSleepMinutes + awakeMinutes
    
    val deepSleepHours: String
        get() = formatMinutes(deepSleepMinutes)
    
    val lightSleepHours: String
        get() = formatMinutes(lightSleepMinutes)
    
    val remSleepHours: String
        get() = formatMinutes(remSleepMinutes)
    
    val awakeHours: String
        get() = formatMinutes(awakeMinutes)
    
    private fun formatMinutes(minutes: Long): String {
        val hours = minutes / 60
        val mins = minutes % 60
        return if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
    }
}

data class StepsData(
    val count: Long = 0,
    val goal: Long = 10000,
    val lastUpdated: Instant? = null
) {
    val progress: Float
        get() = if (goal > 0) (count.toFloat() / goal.toFloat()).coerceIn(0f, 1f) else 0f
}

data class HeartRateData(
    val currentBpm: Int? = null,
    val restingBpm: Int? = null,
    val minBpm: Int? = null,
    val maxBpm: Int? = null,
    val readings: List<HeartRateReading> = emptyList()
)

data class HeartRateReading(
    val bpm: Int,
    val timestamp: Instant
)

data class SleepData(
    val totalDuration: Duration? = null,
    val sessions: List<SleepSession> = emptyList()
) {
    val hours: Int
        get() = totalDuration?.toHours()?.toInt() ?: 0
    val minutes: Int
        get() = totalDuration?.let { ((it.toMinutes() % 60).toInt()) } ?: 0
}

data class SleepSession(
    val startTime: Instant,
    val endTime: Instant,
    val duration: Duration,
    val stage: SleepStage
)

enum class SleepStage {
    AWAKE, LIGHT, DEEP, REM, UNKNOWN
}

data class ExerciseData(
    val sessions: List<ExerciseSession> = emptyList(),
    val totalDuration: Duration? = null
) {
    val sessionCount: Int
        get() = sessions.size
}

data class ExerciseSession(
    val exerciseType: String,
    val startTime: Instant,
    val endTime: Instant,
    val duration: Duration,
    val caloriesBurned: Double? = null,
    val distance: Double? = null
)

data class Vo2MaxData(
    val value: Double? = null,
    val measurementTime: Instant? = null,
    val unit: String = "ml/kg/min"
)

data class CaloriesData(
    val totalBurned: Double = 0.0,
    val activeBurned: Double = 0.0,
    val basalBurned: Double = 0.0
)

data class DistanceData(
    val totalMeters: Double = 0.0
) {
    val kilometers: Double
        get() = totalMeters / 1000.0
    val miles: Double
        get() = totalMeters / 1609.344
}

// New metric: Floors Climbed
data class FloorsData(
    val count: Int = 0,
    val goal: Int = 10
) {
    val progress: Float
        get() = if (goal > 0) (count.toFloat() / goal.toFloat()).coerceIn(0f, 1f) else 0f
}

// New metric: Resting Heart Rate
data class RestingHeartRateData(
    val bpm: Int? = null,
    val measurementTime: Instant? = null
)

// New metric: Body Fat Percentage
data class BodyFatData(
    val percentage: Double? = null,
    val measurementTime: Instant? = null
)

// New metric: Weight
data class WeightData(
    val kilograms: Double? = null,
    val measurementTime: Instant? = null
) {
    val pounds: Double?
        get() = kilograms?.times(2.20462)
}

// New metric: Basal Metabolic Rate (BMR)
data class BasalMetabolicRateData(
    val caloriesPerDay: Double? = null,
    val measurementTime: Instant? = null
)

// New metric: Body Water Mass
data class BodyWaterMassData(
    val kilograms: Double? = null,
    val measurementTime: Instant? = null
) {
    val percentage: Double?
        get() = kilograms?.let { weightKg ->
            // Body water percentage = (body water mass / total body weight) * 100
            // This is an approximation; actual calculation would need total weight
            null // Will be calculated when we have both values
        }
}

// New metric: Bone Mass
data class BoneMassData(
    val kilograms: Double? = null,
    val measurementTime: Instant? = null
) {
    val pounds: Double?
        get() = kilograms?.times(2.20462)
}

// New metric: Lean Body Mass
data class LeanBodyMassData(
    val kilograms: Double? = null,
    val measurementTime: Instant? = null
) {
    val pounds: Double?
        get() = kilograms?.times(2.20462)
}

// Blood Glucose Data
data class BloodGlucoseData(
    val levelMgPerDl: Double? = null,
    val measurementTime: Instant? = null
)

// Blood Pressure Data
data class BloodPressureData(
    val systolicMmHg: Double? = null,
    val diastolicMmHg: Double? = null,
    val measurementTime: Instant? = null
)

// Body Temperature Data
data class BodyTemperatureData(
    val temperatureCelsius: Double? = null,
    val measurementTime: Instant? = null
)

// Heart Rate Variability (HRV) Data
data class HeartRateVariabilityData(
    val rmssdMs: Double? = null,
    val measurementTime: Instant? = null
)

// Oxygen Saturation (SpO2) Data
data class OxygenSaturationData(
    val percentage: Double? = null,
    val measurementTime: Instant? = null
)

// Respiratory Rate Data
data class RespiratoryRateData(
    val ratePerMinute: Double? = null,
    val measurementTime: Instant? = null
)

// Speed Data
data class SpeedData(
    val metersPerSecond: Double? = null,
    val measurementTime: Instant? = null
) {
    val kmPerHour: Double?
        get() = metersPerSecond?.times(3.6)
    val milesPerHour: Double?
        get() = metersPerSecond?.times(2.23694)
}

// Power Data
data class PowerData(
    val watts: Double? = null,
    val measurementTime: Instant? = null
)

// Nutrition Data
data class NutritionData(
    val calories: Double? = null,
    val proteinGrams: Double? = null,
    val carbsGrams: Double? = null,
    val fatGrams: Double? = null,
    val measurementTime: Instant? = null
)

// Hydration Data
data class HydrationData(
    val volumeMilliliters: Double? = null,
    val measurementTime: Instant? = null
) {
    val liters: Double?
        get() = volumeMilliliters?.div(1000.0)
    val ounces: Double?
        get() = volumeMilliliters?.times(0.033814)
}

// Mindfulness Session Data
data class MindfulnessSessionData(
    val duration: Duration? = null,
    val startTime: Instant? = null,
    val endTime: Instant? = null,
    val sessionType: String? = null
) {
    val minutes: Int
        get() = duration?.toMinutes()?.toInt() ?: 0
}
