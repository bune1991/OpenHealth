package com.openhealth.openhealth.utils

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.*
import androidx.health.connect.client.aggregate.AggregateMetric
import androidx.health.connect.client.request.AggregateGroupByPeriodRequest
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Length
import androidx.health.connect.client.units.Energy
import androidx.health.connect.client.units.Mass
import androidx.health.connect.client.units.Power
import com.openhealth.openhealth.model.*
import java.time.*
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import kotlin.reflect.KClass

object HealthConnectManager {
    private var healthConnectClient: HealthConnectClient? = null

    // All permissions needed for OpenHealth
    val PERMISSIONS = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(Vo2MaxRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(FloorsClimbedRecord::class),
        HealthPermission.getReadPermission(BodyFatRecord::class),
        HealthPermission.getReadPermission(WeightRecord::class),
        HealthPermission.getReadPermission(RestingHeartRateRecord::class),
        HealthPermission.getReadPermission(BasalMetabolicRateRecord::class),
        HealthPermission.getReadPermission(BodyWaterMassRecord::class),
        HealthPermission.getReadPermission(BoneMassRecord::class),
        HealthPermission.getReadPermission(LeanBodyMassRecord::class),
        HealthPermission.getReadPermission(BloodGlucoseRecord::class),
        HealthPermission.getReadPermission(BloodPressureRecord::class),
        HealthPermission.getReadPermission(BodyTemperatureRecord::class),
        HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class),
        HealthPermission.getReadPermission(OxygenSaturationRecord::class),
        HealthPermission.getReadPermission(RespiratoryRateRecord::class),
        HealthPermission.getReadPermission(SpeedRecord::class),
        HealthPermission.getReadPermission(PowerRecord::class)
    )

    fun checkForHealthConnectInstalled(context: Context): Int {
        val availabilityStatus =
            HealthConnectClient.getSdkStatus(context, "com.google.android.apps.healthdata")

        when (availabilityStatus) {
            HealthConnectClient.SDK_AVAILABLE -> {
                healthConnectClient = HealthConnectClient.getOrCreate(context)
                Log.d("HealthConnectManager", "Health Connect SDK is available")
            }
            else -> {
                Log.w("HealthConnectManager", "Health Connect SDK not available: $availabilityStatus")
            }
        }
        return availabilityStatus
    }

    suspend fun checkPermissions(): Boolean {
        val granted = healthConnectClient?.permissionController?.getGrantedPermissions()
        val hasAll = granted?.containsAll(PERMISSIONS) ?: false
        Log.d("HealthConnectManager", "Permissions granted: $hasAll")
        return hasAll
    }

    // Get today's steps data
    suspend fun getTodaySteps(): StepsData {
        return try {
            val client = healthConnectClient ?: run {
                Log.e("HealthConnectManager", "HealthConnectClient is null")
                return StepsData(0)
            }

            val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
            val now = ZonedDateTime.now(ZoneId.systemDefault())

            Log.d("HealthConnectManager", "Fetching steps from $startOfDay to $now")

            val response = client.aggregate(
                AggregateRequest(
                    metrics = setOf(
                        StepsRecord.COUNT_TOTAL,
                        DistanceRecord.DISTANCE_TOTAL
                    ),
                    timeRangeFilter = TimeRangeFilter.between(
                        startOfDay.toInstant(),
                        now.toInstant()
                    )
                )
            )

            val steps = response[StepsRecord.COUNT_TOTAL] ?: 0L

            Log.d("HealthConnectManager", "Steps fetched: $steps")

            StepsData(
                count = steps,
                goal = 10000,
                lastUpdated = now.toInstant()
            )
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading steps: ${e.message}", e)
            StepsData(0)
        }
    }

    // Get today's heart rate data
    suspend fun getTodayHeartRate(): HeartRateData {
        return try {
            val client = healthConnectClient ?: run {
                Log.e("HealthConnectManager", "HealthConnectClient is null")
                return HeartRateData(currentBpm = 0)
            }

            val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
            val now = ZonedDateTime.now(ZoneId.systemDefault())

            Log.d("HealthConnectManager", "Fetching heart rate from $startOfDay to $now")

            // Read heart rate records for today
            val response = client.readRecords(
                ReadRecordsRequest(
                    HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(
                        startOfDay.toInstant(),
                        now.toInstant()
                    )
                )
            )

            val records = response.records
            Log.d("HealthConnectManager", "Heart rate records found: ${records.size}")

            if (records.isEmpty()) {
                return HeartRateData(currentBpm = 0)
            }

            // Extract all BPM samples
            val allSamples = records.flatMap { it.samples }
            val bpmValues = allSamples.map { it.beatsPerMinute }

            val currentBpm = bpmValues.lastOrNull()?.toInt() ?: 0
            val minBpm = bpmValues.minOrNull()?.toInt() ?: 0
            val maxBpm = bpmValues.maxOrNull()?.toInt() ?: 0

            // Estimate resting heart rate (minimum during non-active periods)
            val restingBpm = minBpm

            Log.d("HealthConnectManager", "Heart rate - current: $currentBpm, min: $minBpm, max: $maxBpm")

            HeartRateData(
                currentBpm = currentBpm,
                restingBpm = restingBpm,
                maxBpm = maxBpm,
                minBpm = minBpm,
                readings = allSamples.map { HeartRateReading(it.beatsPerMinute.toInt(), it.time) }
            )
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading heart rate: ${e.message}", e)
            HeartRateData(currentBpm = 0)
        }
    }

    // Get sleep data for last night
    suspend fun getLastNightSleep(): SleepData {
        return try {
            val client = healthConnectClient ?: run {
                Log.e("HealthConnectManager", "HealthConnectClient is null")
                return SleepData()
            }

            // Look for sleep from yesterday evening to today afternoon
            val yesterday = LocalDate.now().minusDays(1).atStartOfDay(ZoneId.systemDefault())
            val todayAfternoon = LocalDate.now().atTime(LocalTime.NOON).atZone(ZoneId.systemDefault())

            Log.d("HealthConnectManager", "Fetching sleep from $yesterday to $todayAfternoon")

            val response = client.readRecords(
                ReadRecordsRequest(
                    SleepSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(
                        yesterday.toInstant(),
                        todayAfternoon.toInstant()
                    )
                )
            )

            val records = response.records
            Log.d("HealthConnectManager", "Sleep records found: ${records.size}")

            if (records.isEmpty()) {
                return SleepData()
            }

            // Get the most recent sleep session
            val lastSession = records.maxByOrNull { it.endTime } ?: return SleepData()

            val totalDuration = Duration.between(lastSession.startTime, lastSession.endTime)

            val sessions = records.map { session ->
                SleepSession(
                    startTime = session.startTime,
                    endTime = session.endTime,
                    duration = Duration.between(session.startTime, session.endTime),
                    stage = SleepStage.UNKNOWN
                )
            }

            Log.d("HealthConnectManager", "Sleep duration: $totalDuration")

            SleepData(
                totalDuration = totalDuration,
                sessions = sessions
            )
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading sleep: ${e.message}", e)
            SleepData()
        }
    }

    // Get today's exercise data
    suspend fun getTodayExercise(): ExerciseData {
        return try {
            val client = healthConnectClient ?: run {
                Log.e("HealthConnectManager", "HealthConnectClient is null")
                return ExerciseData()
            }

            val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
            val now = ZonedDateTime.now(ZoneId.systemDefault())

            Log.d("HealthConnectManager", "Fetching exercise from $startOfDay to $now")

            val response = client.readRecords(
                ReadRecordsRequest(
                    ExerciseSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(
                        startOfDay.toInstant(),
                        now.toInstant()
                    )
                )
            )

            val records = response.records
            Log.d("HealthConnectManager", "Exercise records found: ${records.size}")

            if (records.isEmpty()) {
                return ExerciseData()
            }

            val sessions = records.map { session ->
                ExerciseSession(
                    exerciseType = getExerciseTypeName(session.exerciseType),
                    startTime = session.startTime,
                    endTime = session.endTime,
                    duration = Duration.between(session.startTime, session.endTime),
                    caloriesBurned = null,
                    distance = null
                )
            }

            val totalDuration = sessions.fold(Duration.ZERO) { acc, session ->
                acc.plus(session.duration)
            }

            Log.d("HealthConnectManager", "Total exercise duration: $totalDuration")

            ExerciseData(
                sessions = sessions,
                totalDuration = totalDuration
            )
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading exercise: ${e.message}", e)
            ExerciseData()
        }
    }

    // Get today's calories data
    suspend fun getTodayCalories(): CaloriesData {
        return try {
            val client = healthConnectClient ?: return CaloriesData()

            val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
            val now = ZonedDateTime.now(ZoneId.systemDefault())

            val response = client.aggregate(
                AggregateRequest(
                    metrics = setOf(
                        TotalCaloriesBurnedRecord.ENERGY_TOTAL,
                        ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL,
                        BasalMetabolicRateRecord.BASAL_CALORIES_TOTAL
                    ),
                    timeRangeFilter = TimeRangeFilter.between(
                        startOfDay.toInstant(),
                        now.toInstant()
                    )
                )
            )

            CaloriesData(
                totalBurned = response[TotalCaloriesBurnedRecord.ENERGY_TOTAL]?.inKilocalories ?: 0.0,
                activeBurned = response[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]?.inKilocalories ?: 0.0,
                basalBurned = response[BasalMetabolicRateRecord.BASAL_CALORIES_TOTAL]?.inKilocalories ?: 0.0
            )
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading calories: ${e.message}", e)
            CaloriesData()
        }
    }

    // Get today's distance data
    suspend fun getTodayDistance(): DistanceData {
        return try {
            val client = healthConnectClient ?: return DistanceData()

            val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
            val now = ZonedDateTime.now(ZoneId.systemDefault())

            val response = client.aggregate(
                AggregateRequest(
                    metrics = setOf(DistanceRecord.DISTANCE_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(
                        startOfDay.toInstant(),
                        now.toInstant()
                    )
                )
            )

            DistanceData(
                totalMeters = response[DistanceRecord.DISTANCE_TOTAL]?.inMeters ?: 0.0
            )
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading distance: ${e.message}", e)
            DistanceData()
        }
    }

    // Get today's floors climbed
    suspend fun getTodayFloors(): FloorsData {
        return try {
            val client = healthConnectClient ?: return FloorsData()

            val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
            val now = ZonedDateTime.now(ZoneId.systemDefault())

            val response = client.aggregate(
                AggregateRequest(
                    metrics = setOf(FloorsClimbedRecord.FLOORS_CLIMBED_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(
                        startOfDay.toInstant(),
                        now.toInstant()
                    )
                )
            )

            FloorsData(
                count = response[FloorsClimbedRecord.FLOORS_CLIMBED_TOTAL]?.toInt() ?: 0
            )
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading floors: ${e.message}", e)
            FloorsData()
        }
    }

    // Get latest resting heart rate
    suspend fun getRestingHeartRate(): RestingHeartRateData {
        return try {
            val client = healthConnectClient ?: return RestingHeartRateData()

            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = RestingHeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(
                        LocalDate.now().minusDays(7).atStartOfDay(ZoneId.systemDefault()).toInstant(),
                        Instant.now()
                    )
                )
            )

            val latestRecord = response.records.maxByOrNull { it.time } ?: return RestingHeartRateData()

            RestingHeartRateData(
                bpm = latestRecord.beatsPerMinute.toInt(),
                measurementTime = latestRecord.time
            )
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading resting heart rate: ${e.message}", e)
            RestingHeartRateData()
        }
    }

    // Get latest body fat
    suspend fun getBodyFat(): BodyFatData {
        return try {
            val client = healthConnectClient ?: return BodyFatData()

            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = BodyFatRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(
                        LocalDate.now().minusYears(1).atStartOfDay(ZoneId.systemDefault()).toInstant(),
                        Instant.now()
                    )
                )
            )

            val latestRecord = response.records.maxByOrNull { it.time } ?: return BodyFatData()

            BodyFatData(
                percentage = latestRecord.percentage.value,
                measurementTime = latestRecord.time
            )
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading body fat: ${e.message}", e)
            BodyFatData()
        }
    }

    // Get latest BMR
    suspend fun getBasalMetabolicRate(): BasalMetabolicRateData {
        return try {
            val client = healthConnectClient ?: return BasalMetabolicRateData()

            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = BasalMetabolicRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(
                        LocalDate.now().minusDays(30).atStartOfDay(ZoneId.systemDefault()).toInstant(),
                        Instant.now()
                    )
                )
            )

            val latestRecord = response.records.maxByOrNull { it.time } ?: return BasalMetabolicRateData()

            BasalMetabolicRateData(
                caloriesPerDay = latestRecord.basalMetabolicRate.inKilocaloriesPerDay,
                measurementTime = latestRecord.time
            )
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading BMR: ${e.message}", e)
            BasalMetabolicRateData()
        }
    }

    // Get latest body water mass
    suspend fun getBodyWaterMass(): BodyWaterMassData {
        return try {
            val client = healthConnectClient ?: return BodyWaterMassData()

            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = BodyWaterMassRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(
                        LocalDate.now().minusYears(1).atStartOfDay(ZoneId.systemDefault()).toInstant(),
                        Instant.now()
                    )
                )
            )

            val latestRecord = response.records.maxByOrNull { it.time } ?: return BodyWaterMassData()

            BodyWaterMassData(
                kilograms = latestRecord.mass.inKilograms,
                measurementTime = latestRecord.time
            )
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading body water mass: ${e.message}", e)
            BodyWaterMassData()
        }
    }

    // Get latest bone mass
    suspend fun getBoneMass(): BoneMassData {
        return try {
            val client = healthConnectClient ?: return BoneMassData()

            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = BoneMassRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(
                        LocalDate.now().minusYears(1).atStartOfDay(ZoneId.systemDefault()).toInstant(),
                        Instant.now()
                    )
                )
            )

            val latestRecord = response.records.maxByOrNull { it.time } ?: return BoneMassData()

            BoneMassData(
                kilograms = latestRecord.mass.inKilograms,
                measurementTime = latestRecord.time
            )
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading bone mass: ${e.message}", e)
            BoneMassData()
        }
    }

    // Get latest lean body mass
    suspend fun getLeanBodyMass(): LeanBodyMassData {
        return try {
            val client = healthConnectClient ?: return LeanBodyMassData()

            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = LeanBodyMassRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(
                        LocalDate.now().minusYears(1).atStartOfDay(ZoneId.systemDefault()).toInstant(),
                        Instant.now()
                    )
                )
            )

            val latestRecord = response.records.maxByOrNull { it.time } ?: return LeanBodyMassData()

            LeanBodyMassData(
                kilograms = latestRecord.mass.inKilograms,
                measurementTime = latestRecord.time
            )
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading lean body mass: ${e.message}", e)
            LeanBodyMassData()
        }
    }

    // Get latest VO2 Max reading
    suspend fun getLatestVO2Max(): Vo2MaxData {
        return try {
            val client = healthConnectClient ?: run {
                Log.e("HealthConnectManager", "HealthConnectClient is null")
                return Vo2MaxData()
            }

            val startOfMonth = LocalDate.now().minusDays(30).atStartOfDay(ZoneId.systemDefault())
            val now = ZonedDateTime.now(ZoneId.systemDefault())

            Log.d("HealthConnectManager", "Fetching VO2 Max from $startOfMonth to $now")

            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = Vo2MaxRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(
                        startOfMonth.toInstant(),
                        now.toInstant()
                    )
                )
            )

            val records = response.records
            Log.d("HealthConnectManager", "VO2 Max records found: ${records.size}")

            if (records.isEmpty()) {
                return Vo2MaxData()
            }

            val latestRecord = records.maxByOrNull { it.time } ?: return Vo2MaxData()

            return Vo2MaxData(
                value = latestRecord.vo2MillilitersPerMinuteKilogram,
                measurementTime = latestRecord.time
            )
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading VO2 Max: ${e.message}", e)
            Vo2MaxData()
        }
    }

    // Get latest Weight reading
    suspend fun getWeight(): WeightData {
        return try {
            val client = healthConnectClient ?: run {
                Log.e("HealthConnectManager", "HealthConnectClient is null")
                return WeightData()
            }

            val startOfYear = LocalDate.now().minusYears(1).atStartOfDay(ZoneId.systemDefault())
            val now = ZonedDateTime.now(ZoneId.systemDefault())

            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = WeightRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(
                        startOfYear.toInstant(),
                        now.toInstant()
                    )
                )
            )

            val latestRecord = response.records.maxByOrNull { it.time } ?: return WeightData()

            return WeightData(
                kilograms = latestRecord.weight.inKilograms,
                measurementTime = latestRecord.time
            )
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading weight: ${e.message}", e)
            WeightData()
        }
    }

    // Generic function to get any metric history
    suspend fun getStepsHistory() = getMetricHistory(
        metric = StepsRecord.COUNT_TOTAL,
        recordClass = StepsRecord::class,
        unit = "steps",
        timeExtractor = { it.startTime },
        valueExtractor = { it.count.toDouble() }
    )

    suspend fun getHeartRateHistory() = getMetricHistory(
        metric = HeartRateRecord.BPM_AVG,
        recordClass = HeartRateRecord::class,
        unit = "bpm",
        timeExtractor = { it.startTime },
        valueExtractor = { 
            val avg = it.samples.map { sample -> sample.beatsPerMinute }.average()
            if (avg.isNaN()) 0.0 else avg
        }
    )

    suspend fun getDistanceHistory() = getMetricHistory(
        metric = DistanceRecord.DISTANCE_TOTAL,
        recordClass = DistanceRecord::class,
        unit = "km",
        timeExtractor = { it.startTime },
        valueExtractor = { it.distance.inKilometers }
    )

    suspend fun getCaloriesHistory() = getMetricHistory(
        metric = TotalCaloriesBurnedRecord.ENERGY_TOTAL,
        recordClass = TotalCaloriesBurnedRecord::class,
        unit = "kcal",
        timeExtractor = { it.startTime },
        valueExtractor = { it.energy.inKilocalories }
    )

    suspend fun getActiveCaloriesHistory() = getMetricHistory(
        metric = ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL,
        recordClass = ActiveCaloriesBurnedRecord::class,
        unit = "kcal",
        timeExtractor = { it.startTime },
        valueExtractor = { it.energy.inKilocalories }
    )

    suspend fun getFloorsHistory() = getMetricHistory(
        metric = FloorsClimbedRecord.FLOORS_CLIMBED_TOTAL,
        recordClass = FloorsClimbedRecord::class,
        unit = "floors",
        timeExtractor = { it.startTime },
        valueExtractor = { it.floors }
    )

    suspend fun getBodyFatHistory() = getMetricHistory(
        metric = null,
        recordClass = BodyFatRecord::class,
        unit = "%",
        timeExtractor = { it.time },
        valueExtractor = { it.percentage.value }
    )

    suspend fun getWeightHistory() = getMetricHistory(
        metric = WeightRecord.WEIGHT_AVG,
        recordClass = WeightRecord::class,
        unit = "kg",
        timeExtractor = { it.time },
        valueExtractor = { it.weight.inKilograms }
    )

    suspend fun getBasalMetabolicRateHistory() = getMetricHistory(
        metric = BasalMetabolicRateRecord.BASAL_CALORIES_TOTAL,
        recordClass = BasalMetabolicRateRecord::class,
        unit = "kcal",
        timeExtractor = { it.time },
        valueExtractor = { it.basalMetabolicRate.inKilocaloriesPerDay }
    )

    suspend fun getBodyWaterMassHistory() = getMetricHistory(
        metric = null,
        recordClass = BodyWaterMassRecord::class,
        unit = "kg",
        timeExtractor = { it.time },
        valueExtractor = { it.mass.inKilograms }
    )

    suspend fun getBoneMassHistory() = getMetricHistory(
        metric = null,
        recordClass = BoneMassRecord::class,
        unit = "kg",
        timeExtractor = { it.time },
        valueExtractor = { it.mass.inKilograms }
    )

    suspend fun getLeanBodyMassHistory() = getMetricHistory(
        metric = null,
        recordClass = LeanBodyMassRecord::class,
        unit = "kg",
        timeExtractor = { it.time },
        valueExtractor = { it.mass.inKilograms }
    )

    suspend fun getRestingHeartRateHistory() = getMetricHistory(
        metric = RestingHeartRateRecord.BPM_AVG,
        recordClass = RestingHeartRateRecord::class,
        unit = "bpm",
        timeExtractor = { it.time },
        valueExtractor = { it.beatsPerMinute.toDouble() }
    )

    suspend fun getVo2MaxHistory() = getMetricHistory(
        metric = null,
        recordClass = Vo2MaxRecord::class,
        unit = "ml/kg/min",
        timeExtractor = { it.time },
        valueExtractor = { it.vo2MillilitersPerMinuteKilogram }
    )

    suspend fun getSleepHistory(): MetricHistory {
        return try {
            val client = healthConnectClient ?: return MetricHistory(0.0, "hours", emptyList(), 0.0, null)

            // Get all historical sleep data (from January 1, 2024 to now)
            val startDate = LocalDate.of(2024, 1, 1)
            val startOfDay = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
            val now = Instant.now()

            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = SleepSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startOfDay, now)
                )
            )

            // Calculate sleep stages from the most recent sleep session
            var deepSleepMinutes = 0L
            var lightSleepMinutes = 0L
            var remSleepMinutes = 0L
            var awakeMinutes = 0L

            // Get the most recent sleep session for stage breakdown
            val mostRecentSession = response.records.maxByOrNull { it.endTime }
            mostRecentSession?.stages?.forEach { stage ->
                val durationMinutes = Duration.between(stage.startTime, stage.endTime).toMinutes()
                when (stage.stage) {
                    SleepSessionRecord.STAGE_TYPE_DEEP -> deepSleepMinutes += durationMinutes
                    SleepSessionRecord.STAGE_TYPE_LIGHT -> lightSleepMinutes += durationMinutes
                    SleepSessionRecord.STAGE_TYPE_REM -> remSleepMinutes += durationMinutes
                    SleepSessionRecord.STAGE_TYPE_AWAKE,
                    SleepSessionRecord.STAGE_TYPE_AWAKE_IN_BED -> awakeMinutes += durationMinutes
                }
            }

            val sleepStages = SleepStagesData(
                deepSleepMinutes = deepSleepMinutes,
                lightSleepMinutes = lightSleepMinutes,
                remSleepMinutes = remSleepMinutes,
                awakeMinutes = awakeMinutes
            )

            val dailySleep = response.records
                .groupBy { it.endTime.atZone(ZoneId.systemDefault()).toLocalDate() }
                .map { (date, records) ->
                    val totalDuration = records.sumOf { Duration.between(it.startTime, it.endTime).toMinutes() }
                    DailyDataPoint(date, totalDuration.toDouble() / 60.0, "hours")
                }
                .sortedBy { it.date }

            // "Today" value should be the most recent sleep session (which is likely from last night)
            val todayValue = dailySleep.lastOrNull()?.value ?: 0.0
            val allTimeAverage = if (dailySleep.isNotEmpty()) dailySleep.map { it.value }.average() else 0.0
            val bestDay = dailySleep.maxByOrNull { it.value }

            MetricHistory(
                todayValue = todayValue,
                unit = "hours",
                last30Days = dailySleep, // Now contains ALL data
                monthlyAverage = allTimeAverage, // Now shows all-time average
                bestDay = bestDay,
                allHistoricalData = dailySleep,
                sleepStages = sleepStages
            )
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading sleep history: ${e.message}", e)
            MetricHistory(0.0, "hours", emptyList(), 0.0, null)
        }
    }

    private suspend fun <T : Record> getMetricHistory(
        metric: AggregateMetric<*>?,
        recordClass: KClass<T>,
        unit: String,
        timeExtractor: (T) -> Instant,
        valueExtractor: (T) -> Double
    ): MetricHistory {
        return try {
            val client = healthConnectClient ?: return MetricHistory(0.0, unit, emptyList(), 0.0, null)

            // Get all historical data (from January 1, 2024 to now)
            val startDate = LocalDate.of(2024, 1, 1)
            val startOfDay = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
            val now = Instant.now()

            // Try to get aggregated daily data first
            val allDailyData = if (metric != null) {
                try {
                    val response = client.aggregateGroupByPeriod(
                        AggregateGroupByPeriodRequest(
                            metrics = setOf(metric),
                            timeRangeFilter = TimeRangeFilter.between(
                                startOfDay,
                                now
                            ),
                            timeRangeSlicer = Period.ofDays(1)
                        )
                    )

                    response.mapNotNull { result ->
                        val rawValue = result.result[metric] ?: return@mapNotNull null
                        val value = when (rawValue) {
                            is Long -> rawValue.toDouble()
                            is Double -> rawValue
                            is Energy -> rawValue.inKilocalories
                            is Power -> rawValue.inKilocaloriesPerDay
                            is Length -> rawValue.inKilometers
                            is Mass -> rawValue.inKilograms
                            else -> rawValue.toString().toDoubleOrNull() ?: 0.0
                        }
                        val date = result.startTime.toLocalDate()
                        DailyDataPoint(date, value, unit)
                    }
                } catch (e: Exception) {
                    Log.w("HealthConnectManager", "Aggregation failed, falling back to records: ${e.message}")
                    emptyList()
                }
            } else {
                emptyList()
            }

            // If aggregation didn't work, read individual records
            val allHistoricalData = if (allDailyData.isEmpty()) {
                val response = client.readRecords(
                    ReadRecordsRequest(
                        recordType = recordClass,
                        timeRangeFilter = TimeRangeFilter.between(
                            startOfDay,
                            now
                        )
                    )
                )

                // Group records by date and calculate daily values
                response.records
                    .groupBy { record ->
                        timeExtractor(record).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    .map { (date, records) ->
                        val avgValue = records.map { record -> valueExtractor(record) }.average()
                        DailyDataPoint(date, avgValue, unit)
                    }
                    .sortedBy { it.date }
            } else {
                allDailyData
            }

            // Calculate today's value
            val todayValue = allHistoricalData.lastOrNull()?.value ?: 0.0

            // Calculate average from ALL historical data
            val allTimeAverage = if (allHistoricalData.isNotEmpty()) {
                allHistoricalData.map { it.value }.average()
            } else 0.0

            // Find best day from all historical data
            val bestDay = allHistoricalData.maxByOrNull { it.value }

            MetricHistory(
                todayValue = todayValue,
                unit = unit,
                last30Days = allHistoricalData, // Now contains ALL data
                monthlyAverage = allTimeAverage, // Now shows all-time average
                bestDay = bestDay,
                allHistoricalData = allHistoricalData
            )
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading metric history: ${e.message}", e)
            MetricHistory(0.0, unit, emptyList(), 0.0, null)
        }
    }

    // Get latest blood glucose
    suspend fun getBloodGlucose(): BloodGlucoseData {
        return try {
            val client = healthConnectClient ?: return BloodGlucoseData()

            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = BloodGlucoseRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(
                        LocalDate.now().minusDays(7).atStartOfDay(ZoneId.systemDefault()).toInstant(),
                        Instant.now()
                    )
                )
            )

            val latestRecord = response.records.maxByOrNull { it.time } ?: return BloodGlucoseData()

            BloodGlucoseData(
                levelMgPerDl = latestRecord.level.inMilligramsPerDeciliter,
                measurementTime = latestRecord.time
            )
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading blood glucose: ${e.message}", e)
            BloodGlucoseData()
        }
    }

    // Get latest blood pressure
    suspend fun getBloodPressure(): BloodPressureData {
        return try {
            val client = healthConnectClient ?: return BloodPressureData()

            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = BloodPressureRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(
                        LocalDate.now().minusDays(7).atStartOfDay(ZoneId.systemDefault()).toInstant(),
                        Instant.now()
                    )
                )
            )

            val latestRecord = response.records.maxByOrNull { it.time } ?: return BloodPressureData()

            BloodPressureData(
                systolicMmHg = latestRecord.systolic.inMillimetersOfMercury,
                diastolicMmHg = latestRecord.diastolic.inMillimetersOfMercury,
                measurementTime = latestRecord.time
            )
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading blood pressure: ${e.message}", e)
            BloodPressureData()
        }
    }

    // Get latest body temperature
    suspend fun getBodyTemperature(): BodyTemperatureData {
        return try {
            val client = healthConnectClient ?: return BodyTemperatureData()

            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = BodyTemperatureRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(
                        LocalDate.now().minusDays(7).atStartOfDay(ZoneId.systemDefault()).toInstant(),
                        Instant.now()
                    )
                )
            )

            val latestRecord = response.records.maxByOrNull { it.time } ?: return BodyTemperatureData()

            BodyTemperatureData(
                temperatureCelsius = latestRecord.temperature.inCelsius,
                measurementTime = latestRecord.time
            )
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading body temperature: ${e.message}", e)
            BodyTemperatureData()
        }
    }

    // Get latest HRV
    suspend fun getHeartRateVariability(): HeartRateVariabilityData {
        return try {
            val client = healthConnectClient ?: return HeartRateVariabilityData()

            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = HeartRateVariabilityRmssdRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(
                        LocalDate.now().minusDays(7).atStartOfDay(ZoneId.systemDefault()).toInstant(),
                        Instant.now()
                    )
                )
            )

            val latestRecord = response.records.maxByOrNull { it.time } ?: return HeartRateVariabilityData()

            HeartRateVariabilityData(
                rmssdMs = latestRecord.heartRateVariabilityMillis,
                measurementTime = latestRecord.time
            )
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading HRV: ${e.message}", e)
            HeartRateVariabilityData()
        }
    }

    // Get latest SpO2
    suspend fun getOxygenSaturation(): OxygenSaturationData {
        return try {
            val client = healthConnectClient ?: return OxygenSaturationData()

            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = OxygenSaturationRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(
                        LocalDate.now().minusDays(7).atStartOfDay(ZoneId.systemDefault()).toInstant(),
                        Instant.now()
                    )
                )
            )

            val latestRecord = response.records.maxByOrNull { it.time } ?: return OxygenSaturationData()

            OxygenSaturationData(
                percentage = latestRecord.percentage.value,
                measurementTime = latestRecord.time
            )
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading SpO2: ${e.message}", e)
            OxygenSaturationData()
        }
    }

    // Get latest respiratory rate
    suspend fun getRespiratoryRate(): RespiratoryRateData {
        return try {
            val client = healthConnectClient ?: return RespiratoryRateData()

            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = RespiratoryRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(
                        LocalDate.now().minusDays(7).atStartOfDay(ZoneId.systemDefault()).toInstant(),
                        Instant.now()
                    )
                )
            )

            val latestRecord = response.records.maxByOrNull { it.time } ?: return RespiratoryRateData()

            RespiratoryRateData(
                ratePerMinute = latestRecord.rate,
                measurementTime = latestRecord.time
            )
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading respiratory rate: ${e.message}", e)
            RespiratoryRateData()
        }
    }

    // History methods for vitals
    suspend fun getBloodGlucoseHistory() = getMetricHistory(
        metric = null,
        recordClass = BloodGlucoseRecord::class,
        unit = "mg/dL",
        timeExtractor = { it.time },
        valueExtractor = { it.level.inMilligramsPerDeciliter }
    )

    suspend fun getBloodPressureHistory() = getMetricHistory(
        metric = null,
        recordClass = BloodPressureRecord::class,
        unit = "mmHg",
        timeExtractor = { it.time },
        valueExtractor = { it.systolic.inMillimetersOfMercury }
    )

    suspend fun getBodyTemperatureHistory() = getMetricHistory(
        metric = null,
        recordClass = BodyTemperatureRecord::class,
        unit = "°C",
        timeExtractor = { it.time },
        valueExtractor = { it.temperature.inCelsius }
    )

    suspend fun getHeartRateVariabilityHistory() = getMetricHistory(
        metric = null,
        recordClass = HeartRateVariabilityRmssdRecord::class,
        unit = "ms",
        timeExtractor = { it.time },
        valueExtractor = { it.heartRateVariabilityMillis }
    )

    suspend fun getOxygenSaturationHistory() = getMetricHistory(
        metric = null,
        recordClass = OxygenSaturationRecord::class,
        unit = "%",
        timeExtractor = { it.time },
        valueExtractor = { it.percentage.value }
    )

    suspend fun getRespiratoryRateHistory() = getMetricHistory(
        metric = null,
        recordClass = RespiratoryRateRecord::class,
        unit = "breaths/min",
        timeExtractor = { it.time },
        valueExtractor = { it.rate }
    )

    private fun getExerciseTypeName(type: Int): String {
        return when (type) {
            ExerciseSessionRecord.EXERCISE_TYPE_RUNNING -> "Running"
            ExerciseSessionRecord.EXERCISE_TYPE_WALKING -> "Walking"
            ExerciseSessionRecord.EXERCISE_TYPE_BIKING -> "Cycling"
            ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_POOL -> "Swimming"
            ExerciseSessionRecord.EXERCISE_TYPE_YOGA -> "Yoga"
            ExerciseSessionRecord.EXERCISE_TYPE_HIKING -> "Hiking"
            else -> "Workout"
        }
    }

    // Helper function to get start and end of a specific date
    private fun getDateTimeRange(date: LocalDate): Pair<Instant, Instant> {
        val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant()
        val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
        return Pair(startOfDay, endOfDay)
    }

    // Date-based query methods for dashboard navigation
    suspend fun getStepsForDate(date: LocalDate): StepsData {
        return try {
            val client = healthConnectClient ?: return StepsData(0)
            val (start, end) = getDateTimeRange(date)

            val response = client.aggregate(
                AggregateRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )

            StepsData(
                count = response[StepsRecord.COUNT_TOTAL] ?: 0L,
                goal = 10000,
                lastUpdated = end
            )
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading steps for date $date: ${e.message}", e)
            StepsData(0)
        }
    }

    suspend fun getHeartRateForDate(date: LocalDate): HeartRateData {
        return try {
            val client = healthConnectClient ?: return HeartRateData(currentBpm = 0)
            val (start, end) = getDateTimeRange(date)

            val response = client.readRecords(
                ReadRecordsRequest(
                    HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )

            val records = response.records
            if (records.isEmpty()) return HeartRateData(currentBpm = 0)

            val allSamples = records.flatMap { it.samples }
            val bpmValues = allSamples.map { it.beatsPerMinute }

            HeartRateData(
                currentBpm = bpmValues.lastOrNull()?.toInt() ?: 0,
                restingBpm = bpmValues.minOrNull()?.toInt() ?: 0,
                maxBpm = bpmValues.maxOrNull()?.toInt() ?: 0,
                minBpm = bpmValues.minOrNull()?.toInt() ?: 0,
                readings = allSamples.map { HeartRateReading(it.beatsPerMinute.toInt(), it.time) }
            )
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading heart rate for date $date: ${e.message}", e)
            HeartRateData(currentBpm = 0)
        }
    }

    suspend fun getSleepForDate(date: LocalDate): SleepData {
        return try {
            val client = healthConnectClient ?: return SleepData()
            // Sleep typically spans from evening to morning, so look at the night before
            val startOfDay = date.minusDays(1).atTime(18, 0).atZone(ZoneId.systemDefault()).toInstant()
            val endOfDay = date.atTime(18, 0).atZone(ZoneId.systemDefault()).toInstant()

            val response = client.readRecords(
                ReadRecordsRequest(
                    SleepSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startOfDay, endOfDay)
                )
            )

            val records = response.records
            if (records.isEmpty()) return SleepData()

            val sessions = records.map { session ->
                SleepSession(
                    startTime = session.startTime,
                    endTime = session.endTime,
                    duration = Duration.between(session.startTime, session.endTime),
                    stage = SleepStage.UNKNOWN
                )
            }

            val totalDuration = sessions.fold(Duration.ZERO) { acc, session ->
                acc.plus(session.duration)
            }

            SleepData(totalDuration = totalDuration, sessions = sessions)
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading sleep for date $date: ${e.message}", e)
            SleepData()
        }
    }

    suspend fun getExerciseForDate(date: LocalDate): ExerciseData {
        return try {
            val client = healthConnectClient ?: return ExerciseData()
            val (start, end) = getDateTimeRange(date)

            val response = client.readRecords(
                ReadRecordsRequest(
                    ExerciseSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )

            val records = response.records
            if (records.isEmpty()) return ExerciseData()

            val sessions = records.map { session ->
                ExerciseSession(
                    exerciseType = getExerciseTypeName(session.exerciseType),
                    startTime = session.startTime,
                    endTime = session.endTime,
                    duration = Duration.between(session.startTime, session.endTime),
                    caloriesBurned = null,
                    distance = null
                )
            }

            val totalDuration = sessions.fold(Duration.ZERO) { acc, session ->
                acc.plus(session.duration)
            }

            ExerciseData(sessions = sessions, totalDuration = totalDuration)
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading exercise for date $date: ${e.message}", e)
            ExerciseData()
        }
    }

    suspend fun getCaloriesForDate(date: LocalDate): CaloriesData {
        return try {
            val client = healthConnectClient ?: return CaloriesData()
            val (start, end) = getDateTimeRange(date)

            val response = client.aggregate(
                AggregateRequest(
                    metrics = setOf(
                        TotalCaloriesBurnedRecord.ENERGY_TOTAL,
                        ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL,
                        BasalMetabolicRateRecord.BASAL_CALORIES_TOTAL
                    ),
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )

            CaloriesData(
                totalBurned = response[TotalCaloriesBurnedRecord.ENERGY_TOTAL]?.inKilocalories ?: 0.0,
                activeBurned = response[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]?.inKilocalories ?: 0.0,
                basalBurned = response[BasalMetabolicRateRecord.BASAL_CALORIES_TOTAL]?.inKilocalories ?: 0.0
            )
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading calories for date $date: ${e.message}", e)
            CaloriesData()
        }
    }

    suspend fun getDistanceForDate(date: LocalDate): DistanceData {
        return try {
            val client = healthConnectClient ?: return DistanceData()
            val (start, end) = getDateTimeRange(date)

            val response = client.aggregate(
                AggregateRequest(
                    metrics = setOf(DistanceRecord.DISTANCE_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )

            DistanceData(totalMeters = response[DistanceRecord.DISTANCE_TOTAL]?.inMeters ?: 0.0)
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading distance for date $date: ${e.message}", e)
            DistanceData()
        }
    }

    suspend fun getFloorsForDate(date: LocalDate): FloorsData {
        return try {
            val client = healthConnectClient ?: return FloorsData()
            val (start, end) = getDateTimeRange(date)

            val response = client.aggregate(
                AggregateRequest(
                    metrics = setOf(FloorsClimbedRecord.FLOORS_CLIMBED_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )

            FloorsData(count = response[FloorsClimbedRecord.FLOORS_CLIMBED_TOTAL]?.toInt() ?: 0)
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading floors for date $date: ${e.message}", e)
            FloorsData()
        }
    }

    suspend fun getRestingHeartRateForDate(date: LocalDate): RestingHeartRateData {
        return try {
            val client = healthConnectClient ?: return RestingHeartRateData()
            val (start, end) = getDateTimeRange(date)

            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = RestingHeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )

            val latestRecord = response.records.maxByOrNull { it.time } ?: return RestingHeartRateData()

            RestingHeartRateData(
                bpm = latestRecord.beatsPerMinute.toInt(),
                measurementTime = latestRecord.time
            )
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading resting HR for date $date: ${e.message}", e)
            RestingHeartRateData()
        }
    }

    suspend fun getBodyFatForDate(date: LocalDate): BodyFatData {
        return try {
            val client = healthConnectClient ?: return BodyFatData()
            val (start, end) = getDateTimeRange(date)

            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = BodyFatRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )

            val latestRecord = response.records.maxByOrNull { it.time } ?: return BodyFatData()

            BodyFatData(
                percentage = latestRecord.percentage.value,
                measurementTime = latestRecord.time
            )
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading body fat for date $date: ${e.message}", e)
            BodyFatData()
        }
    }

    suspend fun getWeightForDate(date: LocalDate): WeightData {
        return try {
            val client = healthConnectClient ?: return WeightData()
            val (start, end) = getDateTimeRange(date)

            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = WeightRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )

            val latestRecord = response.records.maxByOrNull { it.time } ?: return WeightData()

            WeightData(
                kilograms = latestRecord.weight.inKilograms,
                measurementTime = latestRecord.time
            )
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading weight for date $date: ${e.message}", e)
            WeightData()
        }
    }

    suspend fun getBasalMetabolicRateForDate(date: LocalDate): BasalMetabolicRateData {
        return try {
            val client = healthConnectClient ?: return BasalMetabolicRateData()
            val (start, end) = getDateTimeRange(date)

            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = BasalMetabolicRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )

            val latestRecord = response.records.maxByOrNull { it.time } ?: return BasalMetabolicRateData()

            BasalMetabolicRateData(
                caloriesPerDay = latestRecord.basalMetabolicRate.inKilocaloriesPerDay,
                measurementTime = latestRecord.time
            )
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading BMR for date $date: ${e.message}", e)
            BasalMetabolicRateData()
        }
    }

    suspend fun getBodyWaterMassForDate(date: LocalDate): BodyWaterMassData {
        return try {
            val client = healthConnectClient ?: return BodyWaterMassData()
            val (start, end) = getDateTimeRange(date)

            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = BodyWaterMassRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )

            val latestRecord = response.records.maxByOrNull { it.time } ?: return BodyWaterMassData()

            BodyWaterMassData(
                kilograms = latestRecord.mass.inKilograms,
                measurementTime = latestRecord.time
            )
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading body water for date $date: ${e.message}", e)
            BodyWaterMassData()
        }
    }

    suspend fun getBoneMassForDate(date: LocalDate): BoneMassData {
        return try {
            val client = healthConnectClient ?: return BoneMassData()
            val (start, end) = getDateTimeRange(date)

            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = BoneMassRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )

            val latestRecord = response.records.maxByOrNull { it.time } ?: return BoneMassData()

            BoneMassData(
                kilograms = latestRecord.mass.inKilograms,
                measurementTime = latestRecord.time
            )
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading bone mass for date $date: ${e.message}", e)
            BoneMassData()
        }
    }

    suspend fun getLeanBodyMassForDate(date: LocalDate): LeanBodyMassData {
        return try {
            val client = healthConnectClient ?: return LeanBodyMassData()
            val (start, end) = getDateTimeRange(date)

            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = LeanBodyMassRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )

            val latestRecord = response.records.maxByOrNull { it.time } ?: return LeanBodyMassData()

            LeanBodyMassData(
                kilograms = latestRecord.mass.inKilograms,
                measurementTime = latestRecord.time
            )
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading lean body mass for date $date: ${e.message}", e)
            LeanBodyMassData()
        }
    }

    suspend fun getBloodGlucoseForDate(date: LocalDate): BloodGlucoseData {
        return try {
            val client = healthConnectClient ?: return BloodGlucoseData()
            val (start, end) = getDateTimeRange(date)

            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = BloodGlucoseRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )

            val latestRecord = response.records.maxByOrNull { it.time } ?: return BloodGlucoseData()

            BloodGlucoseData(
                levelMgPerDl = latestRecord.level.inMilligramsPerDeciliter,
                measurementTime = latestRecord.time
            )
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading blood glucose for date $date: ${e.message}", e)
            BloodGlucoseData()
        }
    }

    suspend fun getBloodPressureForDate(date: LocalDate): BloodPressureData {
        return try {
            val client = healthConnectClient ?: return BloodPressureData()
            val (start, end) = getDateTimeRange(date)

            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = BloodPressureRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )

            val latestRecord = response.records.maxByOrNull { it.time } ?: return BloodPressureData()

            BloodPressureData(
                systolicMmHg = latestRecord.systolic.inMillimetersOfMercury,
                diastolicMmHg = latestRecord.diastolic.inMillimetersOfMercury,
                measurementTime = latestRecord.time
            )
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading blood pressure for date $date: ${e.message}", e)
            BloodPressureData()
        }
    }

    suspend fun getBodyTemperatureForDate(date: LocalDate): BodyTemperatureData {
        return try {
            val client = healthConnectClient ?: return BodyTemperatureData()
            val (start, end) = getDateTimeRange(date)

            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = BodyTemperatureRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )

            val latestRecord = response.records.maxByOrNull { it.time } ?: return BodyTemperatureData()

            BodyTemperatureData(
                temperatureCelsius = latestRecord.temperature.inCelsius,
                measurementTime = latestRecord.time
            )
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading body temperature for date $date: ${e.message}", e)
            BodyTemperatureData()
        }
    }

    suspend fun getHeartRateVariabilityForDate(date: LocalDate): HeartRateVariabilityData {
        return try {
            val client = healthConnectClient ?: return HeartRateVariabilityData()
            val (start, end) = getDateTimeRange(date)

            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = HeartRateVariabilityRmssdRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )

            val latestRecord = response.records.maxByOrNull { it.time } ?: return HeartRateVariabilityData()

            HeartRateVariabilityData(
                rmssdMs = latestRecord.heartRateVariabilityMillis,
                measurementTime = latestRecord.time
            )
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading HRV for date $date: ${e.message}", e)
            HeartRateVariabilityData()
        }
    }

    suspend fun getOxygenSaturationForDate(date: LocalDate): OxygenSaturationData {
        return try {
            val client = healthConnectClient ?: return OxygenSaturationData()
            val (start, end) = getDateTimeRange(date)

            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = OxygenSaturationRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )

            val latestRecord = response.records.maxByOrNull { it.time } ?: return OxygenSaturationData()

            OxygenSaturationData(
                percentage = latestRecord.percentage.value,
                measurementTime = latestRecord.time
            )
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading SpO2 for date $date: ${e.message}", e)
            OxygenSaturationData()
        }
    }

    suspend fun getRespiratoryRateForDate(date: LocalDate): RespiratoryRateData {
        return try {
            val client = healthConnectClient ?: return RespiratoryRateData()
            val (start, end) = getDateTimeRange(date)

            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = RespiratoryRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )

            val latestRecord = response.records.maxByOrNull { it.time } ?: return RespiratoryRateData()

            RespiratoryRateData(
                ratePerMinute = latestRecord.rate,
                measurementTime = latestRecord.time
            )
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading respiratory rate for date $date: ${e.message}", e)
            RespiratoryRateData()
        }
    }
}
