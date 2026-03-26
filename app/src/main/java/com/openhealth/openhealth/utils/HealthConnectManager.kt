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

    // Core permissions required for OpenHealth
    private val REQUIRED_PERMISSIONS = setOf(
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
        HealthPermission.getReadPermission(PowerRecord::class),
        HealthPermission.getReadPermission(NutritionRecord::class)
    )

    // Optional permissions (newer APIs, may not be available on all devices)
    @OptIn(androidx.health.connect.client.feature.ExperimentalMindfulnessSessionApi::class)
    private val OPTIONAL_PERMISSIONS = setOf(
        HealthPermission.getReadPermission(SkinTemperatureRecord::class),
        HealthPermission.getReadPermission(MindfulnessSessionRecord::class)
    )

    // All permissions to request from user
    val PERMISSIONS = REQUIRED_PERMISSIONS + OPTIONAL_PERMISSIONS

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
        val hasRequired = granted?.containsAll(REQUIRED_PERMISSIONS) ?: false
        Log.d("HealthConnectManager", "Required permissions granted: $hasRequired")
        return hasRequired
    }

    // Get today's steps data
    suspend fun getTodaySteps(): StepsData {
        return try {
            val client = healthConnectClient ?: run {
                Log.e("OpenHealth_Steps", "HealthConnectClient is null")
                return StepsData(0)
            }

            val startOfDay = LocalDate.now(ZoneId.systemDefault()).atStartOfDay(ZoneId.systemDefault())
            val now = ZonedDateTime.now(ZoneId.systemDefault())

            Log.d("OpenHealth_Steps", "Query: $startOfDay to $now")

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

            Log.d("OpenHealth_Steps", "Today: $steps steps, Records: 1 (aggregated)")

            StepsData(
                count = steps,
                lastUpdated = now.toInstant()
            )
        } catch (e: Exception) {
            Log.e("OpenHealth_Steps", "Error reading steps: ${e.message}", e)
            StepsData(0)
        }
    }

    // Get today's heart rate data
    suspend fun getTodayHeartRate(): HeartRateData {
        return try {
            val client = healthConnectClient ?: run {
                Log.e("OpenHealth_HeartRate", "HealthConnectClient is null")
                return HeartRateData(currentBpm = 0)
            }

            // Query only last 1 hour to get the most recent sample
            val now = ZonedDateTime.now(ZoneId.systemDefault())
            val oneHourAgo = now.minusHours(1)

            Log.d("OpenHealth_HeartRate", "Query: $oneHourAgo to $now")

            // Read heart rate records for the last hour
            val response = client.readRecords(
                ReadRecordsRequest(
                    HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(
                        oneHourAgo.toInstant(),
                        now.toInstant()
                    )
                )
            )

            var records = response.records
            Log.d("OpenHealth_HeartRate", "Records (1h): ${records.size}")

            // Fallback: if no records in last hour, try last 24 hours
            if (records.isEmpty()) {
                val startOfDay = now.minusHours(24)
                Log.d("OpenHealth_HeartRate", "Fallback query: $startOfDay to $now")
                val fallbackResponse = client.readRecords(
                    ReadRecordsRequest(
                        HeartRateRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(
                            startOfDay.toInstant(),
                            now.toInstant()
                        )
                    )
                )
                records = fallbackResponse.records
                Log.d("OpenHealth_HeartRate", "Records (24h fallback): ${records.size}")
            }

            if (records.isEmpty()) {
                return HeartRateData(currentBpm = 0)
            }

            // Extract all BPM samples and sort by timestamp to get the most recent
            val allSamples = records.flatMap { it.samples }
                .sortedBy { it.time }

            // Filter to today only for min/max/range stats
            val todayStartHR = LocalDate.now(ZoneId.systemDefault()).atStartOfDay(ZoneId.systemDefault()).toInstant()
            val todaySamples = allSamples.filter { it.time >= todayStartHR }
            val samplesToUse = if (todaySamples.isNotEmpty()) todaySamples else allSamples

            val bpmValues = samplesToUse.map { it.beatsPerMinute }

            // Get the most recent reading (last in sorted list), not max
            val currentBpm = allSamples.lastOrNull()?.beatsPerMinute?.toInt() ?: 0
            val minBpm = bpmValues.minOrNull()?.toInt() ?: 0
            val maxBpm = bpmValues.maxOrNull()?.toInt() ?: 0

            // Estimate resting heart rate (minimum during non-active periods)
            val restingBpm = minBpm

            Log.d("OpenHealth_HeartRate", "Today: $currentBpm bpm (latest), Range: $minBpm-$maxBpm, Samples: ${allSamples.size} (today: ${todaySamples.size})")

            HeartRateData(
                currentBpm = currentBpm,
                restingBpm = restingBpm,
                maxBpm = maxBpm,
                minBpm = minBpm,
                readings = allSamples.map { HeartRateReading(it.beatsPerMinute.toInt(), it.time) }
            )
        } catch (e: Exception) {
            Log.e("OpenHealth_HeartRate", "Error reading heart rate: ${e.message}", e)
            HeartRateData(currentBpm = 0)
        }
    }

    // Get sleep data for last night
    suspend fun getLastNightSleep(): SleepData {
        return try {
            val client = healthConnectClient ?: run {
                Log.e("OpenHealth_Sleep", "HealthConnectClient is null")
                return SleepData()
            }

            // Look for sleep from yesterday evening to today evening (covers late sleepers)
            val yesterday = LocalDate.now(ZoneId.systemDefault()).minusDays(1).atStartOfDay(ZoneId.systemDefault())
            val todayEvening = LocalDate.now(ZoneId.systemDefault()).atTime(LocalTime.of(20, 0)).atZone(ZoneId.systemDefault())

            Log.d("OpenHealth_Sleep", "Query: $yesterday to $todayEvening")

            val response = client.readRecords(
                ReadRecordsRequest(
                    SleepSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(
                        yesterday.toInstant(),
                        todayEvening.toInstant()
                    )
                )
            )

            val records = response.records
            val lastSession = records.maxByOrNull { it.endTime }
            val totalDuration = lastSession?.let { Duration.between(it.startTime, it.endTime) }
            val hours = totalDuration?.toMinutes()?.div(60.0) ?: 0.0

            Log.d("OpenHealth_Sleep", "Records: ${records.size}, Today: ${String.format("%.1f", hours)}h, First: ${records.minByOrNull { it.startTime }?.startTime}, Last: ${lastSession?.endTime}")

            if (records.isEmpty()) {
                return SleepData()
            }

            val sessions = records.map { session ->
                SleepSession(
                    startTime = session.startTime,
                    endTime = session.endTime,
                    duration = Duration.between(session.startTime, session.endTime),
                    stage = SleepStage.UNKNOWN
                )
            }

            SleepData(
                totalDuration = totalDuration,
                sessions = sessions
            )
        } catch (e: Exception) {
            Log.e("OpenHealth_Sleep", "Error reading sleep: ${e.message}", e)
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

            val startOfDay = LocalDate.now(ZoneId.systemDefault()).atStartOfDay(ZoneId.systemDefault())
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
            val client = healthConnectClient ?: run {
                Log.e("OpenHealth_Calories", "HealthConnectClient is null")
                return CaloriesData()
            }

            val startOfDay = LocalDate.now(ZoneId.systemDefault()).atStartOfDay(ZoneId.systemDefault())
            val now = ZonedDateTime.now(ZoneId.systemDefault())

            Log.d("OpenHealth_Calories", "Query: $startOfDay to $now")

            // Read individual TotalCaloriesBurnedRecord records to check data source
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = TotalCaloriesBurnedRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(
                        startOfDay.toInstant(),
                        now.toInstant()
                    )
                )
            )

            // Check for Garmin Connect data first (package name: com.garmin.android.apps.connectmobile)
            val garminRecords = response.records.filter { record ->
                val dataOrigin = record.metadata.dataOrigin.packageName
                dataOrigin.contains("garmin", ignoreCase = true)
            }

            val totalBurned: Double
            val activeBurned: Double

            if (garminRecords.isNotEmpty()) {
                // Use only Garmin Connect data (daily total from Garmin)
                val garminTotal = garminRecords.sumOf { it.energy.inKilocalories }
                totalBurned = garminTotal
                activeBurned = garminTotal
                Log.d("OpenHealth_Calories", "Records: ${response.records.size} (Garmin: ${garminRecords.size}), Today: ${String.format("%.0f", totalBurned)} kcal")
            } else {
                // No Garmin data, fall back to aggregating all sources
                val aggregateResponse = client.aggregate(
                    AggregateRequest(
                        metrics = setOf(
                            TotalCaloriesBurnedRecord.ENERGY_TOTAL,
                            ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL
                        ),
                        timeRangeFilter = TimeRangeFilter.between(
                            startOfDay.toInstant(),
                            now.toInstant()
                        )
                    )
                )

                totalBurned = aggregateResponse[TotalCaloriesBurnedRecord.ENERGY_TOTAL]?.inKilocalories ?: 0.0
                activeBurned = aggregateResponse[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]?.inKilocalories ?: 0.0
                Log.d("OpenHealth_Calories", "Records: ${response.records.size}, Today: ${String.format("%.0f", totalBurned)} kcal (aggregated)")
            }

            CaloriesData(
                totalBurned = totalBurned,
                activeBurned = activeBurned,
                basalBurned = 0.0
            )
        } catch (e: Exception) {
            Log.e("OpenHealth_Calories", "Error reading calories: ${e.message}", e)
            CaloriesData()
        }
    }

    // Get today's distance data
    suspend fun getTodayDistance(): DistanceData {
        return try {
            val client = healthConnectClient ?: run {
                Log.e("OpenHealth_Distance", "HealthConnectClient is null")
                return DistanceData()
            }

            val startOfDay = LocalDate.now(ZoneId.systemDefault()).atStartOfDay(ZoneId.systemDefault())
            val now = ZonedDateTime.now(ZoneId.systemDefault())

            Log.d("OpenHealth_Distance", "Query: $startOfDay to $now")

            val response = client.aggregate(
                AggregateRequest(
                    metrics = setOf(DistanceRecord.DISTANCE_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(
                        startOfDay.toInstant(),
                        now.toInstant()
                    )
                )
            )

            val distanceKm = response[DistanceRecord.DISTANCE_TOTAL]?.inKilometers ?: 0.0
            Log.d("OpenHealth_Distance", "Today: ${String.format("%.2f", distanceKm)} km, Records: 1 (aggregated)")

            DistanceData(
                totalMeters = response[DistanceRecord.DISTANCE_TOTAL]?.inMeters ?: 0.0
            )
        } catch (e: Exception) {
            Log.e("OpenHealth_Distance", "Error reading distance: ${e.message}", e)
            DistanceData()
        }
    }

    // Get today's floors climbed
    suspend fun getTodayFloors(): FloorsData {
        return try {
            val client = healthConnectClient ?: return FloorsData()

            val startOfDay = LocalDate.now(ZoneId.systemDefault()).atStartOfDay(ZoneId.systemDefault())
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
            val client = healthConnectClient ?: run {
                Log.e("OpenHealth_RestingHeartRate", "HealthConnectClient is null")
                return RestingHeartRateData()
            }

            val startTime = LocalDate.now(ZoneId.systemDefault()).minusDays(7).atStartOfDay(ZoneId.systemDefault())
            val endTime = ZonedDateTime.now(ZoneId.systemDefault())

            Log.d("OpenHealth_RestingHeartRate", "Query: $startTime to $endTime")

            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = RestingHeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime.toInstant(), endTime.toInstant())
                )
            )

            val records = response.records
            val latestRecord = records.maxByOrNull { it.time }
            val todayValue = latestRecord?.beatsPerMinute?.toInt() ?: 0

            Log.d("OpenHealth_RestingHeartRate", "Records: ${records.size}, Today: $todayValue bpm, First: ${records.minByOrNull { it.time }?.time}, Last: ${latestRecord?.time}")

            if (latestRecord == null) return RestingHeartRateData()

            RestingHeartRateData(
                bpm = todayValue,
                measurementTime = latestRecord.time
            )
        } catch (e: Exception) {
            Log.e("OpenHealth_RestingHeartRate", "Error reading resting heart rate: ${e.message}", e)
            RestingHeartRateData()
        }
    }

    // Get latest body fat
    suspend fun getBodyFat(): BodyFatData {
        return try {
            val client = healthConnectClient ?: run {
                Log.e("OpenHealth_BodyFat", "HealthConnectClient is null")
                return BodyFatData()
            }

            val startTime = LocalDate.now(ZoneId.systemDefault()).minusDays(180).atStartOfDay(ZoneId.systemDefault())
            val endTime = ZonedDateTime.now(ZoneId.systemDefault())

            Log.d("OpenHealth_BodyFat", "Query: $startTime to $endTime")

            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = BodyFatRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime.toInstant(), endTime.toInstant())
                )
            )

            val records = response.records
            val latestRecord = records.maxByOrNull { it.time }
            val todayValue = latestRecord?.percentage?.value ?: 0.0

            Log.d("OpenHealth_BodyFat", "Records: ${records.size}, Today: ${String.format("%.1f", todayValue)}%, First: ${records.minByOrNull { it.time }?.time}, Last: ${latestRecord?.time}")

            if (latestRecord == null) return BodyFatData()

            BodyFatData(
                percentage = todayValue,
                measurementTime = latestRecord.time
            )
        } catch (e: Exception) {
            Log.e("OpenHealth_BodyFat", "Error reading body fat: ${e.message}", e)
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
                        LocalDate.now(ZoneId.systemDefault()).minusDays(180).atStartOfDay(ZoneId.systemDefault()).toInstant(),
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
                        LocalDate.now(ZoneId.systemDefault()).minusDays(180).atStartOfDay(ZoneId.systemDefault()).toInstant(),
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
                        LocalDate.now(ZoneId.systemDefault()).minusDays(180).atStartOfDay(ZoneId.systemDefault()).toInstant(),
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
                        LocalDate.now(ZoneId.systemDefault()).minusDays(180).atStartOfDay(ZoneId.systemDefault()).toInstant(),
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

            val startOfMonth = LocalDate.now(ZoneId.systemDefault()).minusDays(30).atStartOfDay(ZoneId.systemDefault())
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
                Log.e("OpenHealth_BodyWeight", "HealthConnectClient is null")
                return WeightData()
            }

            val startOf30Days = LocalDate.now(ZoneId.systemDefault()).minusDays(180).atStartOfDay(ZoneId.systemDefault())
            val now = ZonedDateTime.now(ZoneId.systemDefault())

            Log.d("OpenHealth_BodyWeight", "Query: $startOf30Days to $now")

            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = WeightRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(
                        startOf30Days.toInstant(),
                        now.toInstant()
                    )
                )
            )

            val records = response.records
            val latestRecord = records.maxByOrNull { it.time }
            val todayValue = latestRecord?.weight?.inKilograms ?: 0.0

            Log.d("OpenHealth_BodyWeight", "Records: ${records.size}, Today: ${String.format("%.1f", todayValue)} kg, First: ${records.minByOrNull { it.time }?.time}, Last: ${latestRecord?.time}")

            if (latestRecord == null) return WeightData()

            return WeightData(
                kilograms = todayValue,
                measurementTime = latestRecord.time
            )
        } catch (e: Exception) {
            Log.e("OpenHealth_BodyWeight", "Error reading weight: ${e.message}", e)
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
        },
        lowerIsBetter = true
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
        valueExtractor = { it.floors.toDouble() }
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
        valueExtractor = { it.beatsPerMinute.toDouble() },
        lowerIsBetter = true
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
                    // Find the longest sleep session for this day (main sleep)
                    val mainSession = records.maxByOrNull { 
                        Duration.between(it.startTime, it.endTime).toMinutes() 
                    }
                    // Calculate duration from the main session only
                    val durationMinutes = if (mainSession != null) {
                        Duration.between(mainSession.startTime, mainSession.endTime).toMinutes()
                    } else 0L
                    DailyDataPoint(
                        date = date,
                        value = durationMinutes.toDouble() / 60.0,
                        unit = "hours",
                        sleepStartTime = mainSession?.startTime,
                        sleepEndTime = mainSession?.endTime
                    )
                }
                .sortedBy { it.date }

            // "Today" value should be the most recent sleep session (which is likely from last night)
            val todayValue = dailySleep.lastOrNull()?.value ?: 0.0
            val allTimeAverage = if (dailySleep.isNotEmpty()) dailySleep.map { it.value }.average() else 0.0
            // Longest sleep day
            val bestDay = dailySleep.maxByOrNull { it.value }

            // Get today's sleep start and end times from the most recent session
            val todaySleepStartTime = mostRecentSession?.startTime
            val todaySleepEndTime = mostRecentSession?.endTime

            MetricHistory(
                todayValue = todayValue,
                unit = "hours",
                last30Days = dailySleep,
                monthlyAverage = allTimeAverage,
                bestDay = bestDay,
                bestDayLabel = "Longest",
                allHistoricalData = dailySleep,
                sleepStages = sleepStages,
                todaySleepStartTime = todaySleepStartTime,
                todaySleepEndTime = todaySleepEndTime
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
        valueExtractor: (T) -> Double,
        lowerIsBetter: Boolean = false
    ): MetricHistory {
        return try {
            val client = healthConnectClient ?: return MetricHistory(0.0, unit, emptyList(), 0.0, null)

            val metricName = recordClass.simpleName ?: "Unknown"

            // Query 30 days back using LocalDateTime for proper local-day alignment
            val today = LocalDate.now(ZoneId.systemDefault())
            val startDate = today.minusDays(30)
            val localStart = startDate.atStartOfDay()
            val localEnd = today.plusDays(1).atStartOfDay()

            Log.d("OpenHealth_History", "[$metricName] Query: $localStart to $localEnd (local time)")

            // Try to get aggregated daily data first
            val allDailyData = if (metric != null) {
                try {
                    val response = client.aggregateGroupByPeriod(
                        AggregateGroupByPeriodRequest(
                            metrics = setOf(metric),
                            timeRangeFilter = TimeRangeFilter.between(
                                localStart,
                                localEnd
                            ),
                            timeRangeSlicer = Period.ofDays(1)
                        )
                    )

                    Log.d("OpenHealth_History", "[$metricName] Aggregation returned ${response.size} periods")

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
                    }.sortedBy { it.date }
                } catch (e: Exception) {
                    Log.w("OpenHealth_History", "[$metricName] Aggregation failed: ${e.message}")
                    emptyList()
                }
            } else {
                emptyList()
            }

            // If aggregation didn't work, read individual records with pagination
            val allHistoricalData = if (allDailyData.isEmpty()) {
                val allRecords = mutableListOf<T>()
                var pageToken: String? = null

                do {
                    val request = ReadRecordsRequest(
                        recordType = recordClass,
                        timeRangeFilter = TimeRangeFilter.between(localStart, localEnd),
                        pageToken = pageToken
                    )
                    val response = client.readRecords(request)
                    allRecords.addAll(response.records)
                    pageToken = response.pageToken
                    Log.d("OpenHealth_History", "[$metricName] readRecords page: ${response.records.size} records, hasMore: ${pageToken != null}")
                } while (pageToken != null)

                Log.d("OpenHealth_History", "[$metricName] Total records read: ${allRecords.size}")

                // Group records by date and calculate daily values
                allRecords
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

            Log.d("OpenHealth_History", "[$metricName] Final: ${allHistoricalData.size} days, First: ${allHistoricalData.firstOrNull()?.date}, Last: ${allHistoricalData.lastOrNull()?.date}")

            // Calculate today's value from history
            val todayValue = allHistoricalData.find { it.date == today }?.value
                ?: allHistoricalData.lastOrNull()?.value
                ?: 0.0

            // Calculate average
            val average = if (allHistoricalData.isNotEmpty()) {
                allHistoricalData.map { it.value }.average()
            } else 0.0

            // Find best day (lowest for HR/resting HR, highest for everything else)
            val bestDay = if (lowerIsBetter) {
                allHistoricalData.filter { it.value > 0 }.minByOrNull { it.value }
            } else {
                allHistoricalData.maxByOrNull { it.value }
            }

            MetricHistory(
                todayValue = todayValue,
                unit = unit,
                last30Days = allHistoricalData,
                monthlyAverage = average,
                bestDay = bestDay,
                bestDayLabel = if (lowerIsBetter) "Lowest" else "Best Day",
                allHistoricalData = allHistoricalData
            )
        } catch (e: Exception) {
            Log.e("OpenHealth_History", "Error reading metric history: ${e.message}", e)
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
                        LocalDate.now(ZoneId.systemDefault()).minusDays(7).atStartOfDay(ZoneId.systemDefault()).toInstant(),
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
                        LocalDate.now(ZoneId.systemDefault()).minusDays(7).atStartOfDay(ZoneId.systemDefault()).toInstant(),
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
                        LocalDate.now(ZoneId.systemDefault()).minusDays(7).atStartOfDay(ZoneId.systemDefault()).toInstant(),
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
            val client = healthConnectClient ?: run {
                Log.e("OpenHealth_HRV", "HealthConnectClient is null")
                return HeartRateVariabilityData()
            }

            val startTime = LocalDate.now(ZoneId.systemDefault()).minusDays(7).atStartOfDay(ZoneId.systemDefault())
            val endTime = ZonedDateTime.now(ZoneId.systemDefault())

            Log.d("OpenHealth_HRV", "Query: $startTime to $endTime")

            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = HeartRateVariabilityRmssdRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime.toInstant(), endTime.toInstant())
                )
            )

            val records = response.records
            val latestRecord = records.maxByOrNull { it.time }
            val latestValue = latestRecord?.heartRateVariabilityMillis ?: 0.0

            // Stats for today only
            val todayStart = LocalDate.now(ZoneId.systemDefault()).atStartOfDay(ZoneId.systemDefault()).toInstant()
            val todayRecords = records.filter { it.time >= todayStart }
            val todayValues = todayRecords.map { it.heartRateVariabilityMillis }
            val avg = if (todayValues.isNotEmpty()) todayValues.average() else latestValue
            val min = todayValues.minOrNull() ?: latestValue
            val max = todayValues.maxOrNull() ?: latestValue

            Log.d("OpenHealth_HRV", "Records: ${records.size} (today: ${todayRecords.size}), Latest: ${String.format("%.1f", latestValue)}, Avg: ${String.format("%.1f", avg)}, Range: ${String.format("%.0f", min)}-${String.format("%.0f", max)} ms")

            if (latestRecord == null) return HeartRateVariabilityData()

            HeartRateVariabilityData(
                rmssdMs = latestValue,
                avgMs = avg,
                minMs = min,
                maxMs = max,
                readingCount = todayRecords.size,
                measurementTime = latestRecord.time
            )
        } catch (e: Exception) {
            Log.e("OpenHealth_HRV", "Error reading HRV: ${e.message}", e)
            HeartRateVariabilityData()
        }
    }

    // Get latest SpO2
    suspend fun getOxygenSaturation(): OxygenSaturationData {
        return try {
            val client = healthConnectClient ?: run {
                Log.e("OpenHealth_SpO2", "HealthConnectClient is null")
                return OxygenSaturationData()
            }

            val startTime = LocalDate.now(ZoneId.systemDefault()).minusDays(7).atStartOfDay(ZoneId.systemDefault())
            val endTime = ZonedDateTime.now(ZoneId.systemDefault())

            Log.d("OpenHealth_SpO2", "Query: $startTime to $endTime")

            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = OxygenSaturationRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime.toInstant(), endTime.toInstant())
                )
            )

            val records = response.records
            val latestRecord = records.maxByOrNull { it.time }
            val latestValue = latestRecord?.percentage?.value ?: 0.0

            // Stats for today only
            val todayStartSpo2 = LocalDate.now(ZoneId.systemDefault()).atStartOfDay(ZoneId.systemDefault()).toInstant()
            val todayRecords = records.filter { it.time >= todayStartSpo2 }
            val todayValues = todayRecords.map { it.percentage.value }
            val avg = if (todayValues.isNotEmpty()) todayValues.average() else latestValue
            val min = todayValues.minOrNull() ?: latestValue
            val max = todayValues.maxOrNull() ?: latestValue

            Log.d("OpenHealth_SpO2", "Records: ${records.size} (today: ${todayRecords.size}), Latest: ${String.format("%.1f", latestValue)}%, Avg: ${String.format("%.1f", avg)}%")

            if (latestRecord == null) return OxygenSaturationData()

            OxygenSaturationData(
                percentage = latestValue,
                avgPercentage = avg,
                minPercentage = min,
                maxPercentage = max,
                readingCount = todayRecords.size,
                measurementTime = latestRecord.time
            )
        } catch (e: Exception) {
            Log.e("OpenHealth_SpO2", "Error reading SpO2: ${e.message}", e)
            OxygenSaturationData()
        }
    }

    // Get latest respiratory rate
    suspend fun getRespiratoryRate(): RespiratoryRateData {
        return try {
            val client = healthConnectClient ?: run {
                Log.e("OpenHealth_RespiratoryRate", "HealthConnectClient is null")
                return RespiratoryRateData()
            }

            // Query last 1 day with pagination (Health Sync generates 1000+ records/day)
            val endTime = ZonedDateTime.now(ZoneId.systemDefault())
            var startTime = endTime.minusDays(1)

            Log.d("OpenHealth_RespiratoryRate", "Query: $startTime to $endTime")

            val allRecords = mutableListOf<RespiratoryRateRecord>()
            var pageToken: String? = null
            do {
                val response = client.readRecords(
                    ReadRecordsRequest(
                        recordType = RespiratoryRateRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime.toInstant(), endTime.toInstant()),
                        pageToken = pageToken
                    )
                )
                allRecords.addAll(response.records)
                pageToken = response.pageToken
            } while (pageToken != null)

            // If no records in last day, try last 7 days
            if (allRecords.isEmpty()) {
                startTime = endTime.minusDays(7)
                Log.d("OpenHealth_RespiratoryRate", "Fallback query: $startTime to $endTime")
                val response = client.readRecords(
                    ReadRecordsRequest(
                        recordType = RespiratoryRateRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime.toInstant(), endTime.toInstant())
                    )
                )
                allRecords.addAll(response.records)
            }

            val records = allRecords.toList()
            val latestRecord = records.maxByOrNull { it.time }
            val latestValue = latestRecord?.rate?.toDouble() ?: 0.0

            // Stats for today only
            val todayStartRR = LocalDate.now(ZoneId.systemDefault()).atStartOfDay(ZoneId.systemDefault()).toInstant()
            val todayRecords = records.filter { it.time >= todayStartRR }
            val todayRates = todayRecords.map { it.rate }
            val avgRate = if (todayRates.isNotEmpty()) todayRates.average() else latestValue
            val minRate = todayRates.minOrNull() ?: latestValue
            val maxRate = todayRates.maxOrNull() ?: latestValue

            Log.d("OpenHealth_RespiratoryRate", "Records: ${records.size} (today: ${todayRecords.size}), Latest: $latestValue, Avg: ${String.format("%.1f", avgRate)}, Range: ${String.format("%.0f", minRate)}-${String.format("%.0f", maxRate)} breaths/min")

            if (latestRecord == null) return RespiratoryRateData()

            RespiratoryRateData(
                ratePerMinute = latestValue,
                avgRate = avgRate,
                minRate = minRate,
                maxRate = maxRate,
                readingCount = todayRecords.size,
                measurementTime = latestRecord.time
            )
        } catch (e: Exception) {
            Log.e("OpenHealth_RespiratoryRate", "Error reading respiratory rate: ${e.message}", e)
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
        valueExtractor = { record: RespiratoryRateRecord -> record.rate.toDouble() }
    )

    // Get latest skin temperature
    suspend fun getSkinTemperature(): SkinTemperatureData {
        return try {
            val client = healthConnectClient ?: run {
                Log.e("OpenHealth_SkinTemp", "HealthConnectClient is null")
                return SkinTemperatureData()
            }

            val startTime = LocalDate.now(ZoneId.systemDefault()).minusDays(7).atStartOfDay(ZoneId.systemDefault())
            val endTime = ZonedDateTime.now(ZoneId.systemDefault())

            Log.d("OpenHealth_SkinTemp", "Query: $startTime to $endTime")

            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = SkinTemperatureRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime.toInstant(), endTime.toInstant())
                )
            )

            val records = response.records
            val latestRecord = records.maxByOrNull { it.startTime }

            Log.d("OpenHealth_SkinTemp", "Records: ${records.size}")

            if (latestRecord == null) return SkinTemperatureData()

            // Average the temperature deltas (variations from baseline)
            val avgDelta = if (latestRecord.deltas.isNotEmpty()) {
                latestRecord.deltas.map { it.delta.inCelsius }.average()
            } else {
                0.0
            }

            Log.d("OpenHealth_SkinTemp", "Latest avg delta: ${String.format("%.1f", avgDelta)}°C")

            SkinTemperatureData(
                temperatureCelsius = avgDelta,
                measurementTime = latestRecord.startTime
            )
        } catch (e: Exception) {
            Log.e("OpenHealth_SkinTemp", "Error reading skin temperature: ${e.message}", e)
            SkinTemperatureData()
        }
    }

    suspend fun getSkinTemperatureHistory(): MetricHistory {
        return try {
            val client = healthConnectClient ?: return MetricHistory(0.0, "°C", emptyList(), 0.0, null)

            val today = LocalDate.now(ZoneId.systemDefault())
            val startDate = today.minusDays(30)
            val startOfDay = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
            val now = ZonedDateTime.now(ZoneId.systemDefault()).toInstant()

            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = SkinTemperatureRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startOfDay, now)
                )
            )

            val allHistoricalData = response.records
                .groupBy { it.startTime.atZone(ZoneId.systemDefault()).toLocalDate() }
                .map { (date, records) ->
                    val avgValue = records.map { record ->
                        if (record.deltas.isNotEmpty()) {
                            record.deltas.map { it.delta.inCelsius }.average()
                        } else 0.0
                    }.average()
                    DailyDataPoint(date, avgValue, "°C")
                }
                .sortedBy { it.date }

            val todayValue = allHistoricalData.find { it.date == today }?.value
                ?: allHistoricalData.lastOrNull()?.value
                ?: 0.0

            val monthlyAverage = if (allHistoricalData.isNotEmpty()) allHistoricalData.map { it.value }.average() else 0.0
            val bestDay = allHistoricalData.maxByOrNull { it.value }

            MetricHistory(
                todayValue = todayValue,
                unit = "°C",
                last30Days = allHistoricalData,
                monthlyAverage = monthlyAverage,
                bestDay = bestDay,
                allHistoricalData = allHistoricalData
            )
        } catch (e: Exception) {
            Log.e("OpenHealth_SkinTemp", "Error reading skin temperature history: ${e.message}", e)
            MetricHistory(0.0, "°C", emptyList(), 0.0, null)
        }
    }

    // Get today's nutrition data
    suspend fun getTodayNutrition(): NutritionData {
        return try {
            val client = healthConnectClient ?: return NutritionData()

            val startOfDay = LocalDate.now(ZoneId.systemDefault()).atStartOfDay(ZoneId.systemDefault())
            val now = ZonedDateTime.now(ZoneId.systemDefault())

            Log.d("OpenHealth_Nutrition", "Query: $startOfDay to $now")

            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = NutritionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(
                        startOfDay.toInstant(),
                        now.toInstant()
                    )
                )
            )

            val records = response.records
            if (records.isEmpty()) {
                Log.d("OpenHealth_Nutrition", "Records: 0")
                return NutritionData()
            }

            val totalCalories = records.sumOf { it.energy?.inKilocalories ?: 0.0 }
            val totalProtein = records.sumOf { it.protein?.inGrams ?: 0.0 }
            val totalCarbs = records.sumOf { it.totalCarbohydrate?.inGrams ?: 0.0 }
            val totalFat = records.sumOf { it.totalFat?.inGrams ?: 0.0 }

            Log.d("OpenHealth_Nutrition", "Records: ${records.size}, Calories: ${String.format("%.0f", totalCalories)} kcal, P: ${String.format("%.0f", totalProtein)}g, C: ${String.format("%.0f", totalCarbs)}g, F: ${String.format("%.0f", totalFat)}g")

            NutritionData(
                calories = totalCalories,
                proteinGrams = totalProtein,
                carbsGrams = totalCarbs,
                fatGrams = totalFat,
                measurementTime = records.maxByOrNull { it.endTime }?.endTime
            )
        } catch (e: Exception) {
            Log.e("OpenHealth_Nutrition", "Error reading nutrition: ${e.message}", e)
            NutritionData()
        }
    }

    suspend fun getNutritionForDate(date: LocalDate): NutritionData {
        return try {
            val client = healthConnectClient ?: return NutritionData()
            val (start, end) = getDateTimeRange(date)

            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = NutritionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )

            val records = response.records
            if (records.isEmpty()) return NutritionData()

            NutritionData(
                calories = records.sumOf { it.energy?.inKilocalories ?: 0.0 },
                proteinGrams = records.sumOf { it.protein?.inGrams ?: 0.0 },
                carbsGrams = records.sumOf { it.totalCarbohydrate?.inGrams ?: 0.0 },
                fatGrams = records.sumOf { it.totalFat?.inGrams ?: 0.0 },
                measurementTime = records.maxByOrNull { it.endTime }?.endTime
            )
        } catch (e: Exception) {
            Log.e("OpenHealth_Nutrition", "Error reading nutrition for date $date: ${e.message}", e)
            NutritionData()
        }
    }

    suspend fun getNutritionHistory() = getMetricHistory(
        metric = NutritionRecord.ENERGY_TOTAL,
        recordClass = NutritionRecord::class,
        unit = "kcal",
        timeExtractor = { it.startTime },
        valueExtractor = { it.energy?.inKilocalories ?: 0.0 }
    )

    // Get today's mindfulness data
    @OptIn(androidx.health.connect.client.feature.ExperimentalMindfulnessSessionApi::class)
    suspend fun getTodayMindfulness(): MindfulnessSessionData {
        return try {
            val client = healthConnectClient ?: return MindfulnessSessionData()

            val startOfDay = LocalDate.now(ZoneId.systemDefault()).atStartOfDay(ZoneId.systemDefault())
            val now = ZonedDateTime.now(ZoneId.systemDefault())

            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = MindfulnessSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startOfDay.toInstant(), now.toInstant())
                )
            )

            val records = response.records
            if (records.isEmpty()) return MindfulnessSessionData()

            val totalDuration = records.fold(Duration.ZERO) { acc, r ->
                acc.plus(Duration.between(r.startTime, r.endTime))
            }
            val latest = records.maxByOrNull { it.endTime }
            val sessionType = latest?.let { getMindfulnessTypeName(it.mindfulnessSessionType) }

            Log.d("OpenHealth_Mindfulness", "Records: ${records.size}, Total: ${totalDuration.toMinutes()}m, Type: $sessionType")

            MindfulnessSessionData(
                duration = totalDuration,
                startTime = latest?.startTime,
                endTime = latest?.endTime,
                sessionType = sessionType
            )
        } catch (e: Exception) {
            Log.e("OpenHealth_Mindfulness", "Error: ${e.message}", e)
            MindfulnessSessionData()
        }
    }

    @OptIn(androidx.health.connect.client.feature.ExperimentalMindfulnessSessionApi::class)
    private fun getMindfulnessTypeName(type: Int): String {
        return when (type) {
            MindfulnessSessionRecord.MINDFULNESS_SESSION_TYPE_MEDITATION -> "Meditation"
            MindfulnessSessionRecord.MINDFULNESS_SESSION_TYPE_BREATHING -> "Breathing"
            MindfulnessSessionRecord.MINDFULNESS_SESSION_TYPE_MUSIC -> "Music"
            MindfulnessSessionRecord.MINDFULNESS_SESSION_TYPE_MOVEMENT -> "Movement"
            MindfulnessSessionRecord.MINDFULNESS_SESSION_TYPE_UNGUIDED -> "Unguided"
            else -> "Mindfulness"
        }
    }

    suspend fun getExerciseHistory(): MetricHistory {
        return try {
            val client = healthConnectClient ?: return MetricHistory(0.0, "min", emptyList(), 0.0, null)

            val today = LocalDate.now(ZoneId.systemDefault())
            val startDate = today.minusDays(30)
            val localStart = startDate.atStartOfDay()
            val localEnd = today.plusDays(1).atStartOfDay()

            Log.d("OpenHealth_History", "[ExerciseSession] Query: $localStart to $localEnd (local time)")

            val allRecords = mutableListOf<ExerciseSessionRecord>()
            var pageToken: String? = null

            do {
                val request = ReadRecordsRequest(
                    recordType = ExerciseSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(localStart, localEnd),
                    pageToken = pageToken
                )
                val response = client.readRecords(request)
                allRecords.addAll(response.records)
                pageToken = response.pageToken
            } while (pageToken != null)

            Log.d("OpenHealth_History", "[ExerciseSession] Total records: ${allRecords.size}")

            // Group by date, sum duration in minutes per day
            val allHistoricalData = allRecords
                .groupBy { it.startTime.atZone(ZoneId.systemDefault()).toLocalDate() }
                .map { (date, records) ->
                    val totalMinutes = records.sumOf {
                        Duration.between(it.startTime, it.endTime).toMinutes()
                    }.toDouble()
                    DailyDataPoint(date, totalMinutes, "min")
                }
                .sortedBy { it.date }

            val todayValue = allHistoricalData.find { it.date == today }?.value
                ?: allHistoricalData.lastOrNull()?.value
                ?: 0.0

            val average = if (allHistoricalData.isNotEmpty()) {
                allHistoricalData.map { it.value }.average()
            } else 0.0

            val bestDay = allHistoricalData.maxByOrNull { it.value }

            Log.d("OpenHealth_History", "[ExerciseSession] Final: ${allHistoricalData.size} days")

            MetricHistory(
                todayValue = todayValue,
                unit = "min",
                last30Days = allHistoricalData,
                monthlyAverage = average,
                bestDay = bestDay,
                allHistoricalData = allHistoricalData
            )
        } catch (e: Exception) {
            Log.e("OpenHealth_History", "Error reading exercise history: ${e.message}", e)
            MetricHistory(0.0, "min", emptyList(), 0.0, null)
        }
    }

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

            // Use the longest session as the main sleep (not sum of all naps)
            val mainSession = records.maxByOrNull {
                Duration.between(it.startTime, it.endTime).toMinutes()
            }
            val totalDuration = mainSession?.let { Duration.between(it.startTime, it.endTime) }

            val sessions = records.map { session ->
                SleepSession(
                    startTime = session.startTime,
                    endTime = session.endTime,
                    duration = Duration.between(session.startTime, session.endTime),
                    stage = SleepStage.UNKNOWN
                )
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

    suspend fun getSkinTemperatureForDate(date: LocalDate): SkinTemperatureData {
        return try {
            val client = healthConnectClient ?: return SkinTemperatureData()
            val (start, end) = getDateTimeRange(date)

            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = SkinTemperatureRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )

            val latestRecord = response.records.maxByOrNull { it.startTime } ?: return SkinTemperatureData()

            val avgDelta = if (latestRecord.deltas.isNotEmpty()) {
                latestRecord.deltas.map { it.delta.inCelsius }.average()
            } else 0.0

            SkinTemperatureData(
                temperatureCelsius = avgDelta,
                measurementTime = latestRecord.startTime
            )
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading skin temperature for date $date: ${e.message}", e)
            SkinTemperatureData()
        }
    }
}
