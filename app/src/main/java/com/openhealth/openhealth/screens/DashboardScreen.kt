package com.openhealth.openhealth.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import com.openhealth.openhealth.model.BodyFatData
import com.openhealth.openhealth.model.CaloriesData
import com.openhealth.openhealth.model.DistanceData
import com.openhealth.openhealth.model.ExerciseData
import com.openhealth.openhealth.model.FloorsData
import com.openhealth.openhealth.model.HealthData
import com.openhealth.openhealth.model.HeartRateData
import com.openhealth.openhealth.model.HeartRateVariabilityData
import com.openhealth.openhealth.model.RestingHeartRateData
import com.openhealth.openhealth.model.SettingsData
import com.openhealth.openhealth.model.SleepData
import com.openhealth.openhealth.model.StepsData
import com.openhealth.openhealth.model.Vo2MaxData
import com.openhealth.openhealth.ui.theme.BackgroundBlack
import com.openhealth.openhealth.ui.theme.CardBackground
import com.openhealth.openhealth.ui.theme.CardBloodGlucose
import com.openhealth.openhealth.ui.theme.CardBloodPressure
import com.openhealth.openhealth.ui.theme.CardBodyFat
import com.openhealth.openhealth.ui.theme.CardBodyTemperature
import com.openhealth.openhealth.ui.theme.CardBodyWater
import com.openhealth.openhealth.ui.theme.CardBoneMass
import com.openhealth.openhealth.ui.theme.CardBMR
import com.openhealth.openhealth.ui.theme.CardCalories
import com.openhealth.openhealth.ui.theme.CardDistance
import com.openhealth.openhealth.ui.theme.CardExercise
import com.openhealth.openhealth.ui.theme.CardFloors
import com.openhealth.openhealth.ui.theme.CardHeartRate
import com.openhealth.openhealth.ui.theme.CardHRV
import com.openhealth.openhealth.ui.theme.CardLeanBodyMass
import com.openhealth.openhealth.ui.theme.CardRespiratoryRate
import com.openhealth.openhealth.ui.theme.CardSleep
import com.openhealth.openhealth.ui.theme.CardSpO2
import com.openhealth.openhealth.ui.theme.CardSteps
import com.openhealth.openhealth.ui.theme.CardVo2Max
import com.openhealth.openhealth.ui.theme.CardWeight
import com.openhealth.openhealth.ui.theme.ChartFillCalories
import com.openhealth.openhealth.ui.theme.ChartFillHeartRate
import com.openhealth.openhealth.ui.theme.ChartFillSleep
import com.openhealth.openhealth.ui.theme.ChartFillSteps
import com.openhealth.openhealth.ui.theme.ChartLineCalories
import com.openhealth.openhealth.ui.theme.ChartLineHeartRate
import com.openhealth.openhealth.ui.theme.ChartLineSleep
import com.openhealth.openhealth.ui.theme.ChartLineSteps
import com.openhealth.openhealth.ui.theme.FabColor
import com.openhealth.openhealth.ui.theme.ReadinessExcellent
import com.openhealth.openhealth.ui.theme.ReadinessFair
import com.openhealth.openhealth.ui.theme.ReadinessGood
import com.openhealth.openhealth.ui.theme.ReadinessPoor
import com.openhealth.openhealth.ui.theme.SurfaceDark
import com.openhealth.openhealth.ui.theme.SurfaceVariant
import com.openhealth.openhealth.ui.theme.TextPrimary
import com.openhealth.openhealth.ui.theme.TextSecondary
import com.openhealth.openhealth.ui.theme.TextTertiary
import com.openhealth.openhealth.viewmodel.HealthViewModel
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

// Readiness Score Data Class
data class ReadinessScore(
    val score: Int,
    val label: String,
    val color: Color,
    val factors: List<String>
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    healthData: HealthData,
    isLoading: Boolean,
    settings: SettingsData,
    selectedDate: LocalDate = LocalDate.now(),
    onRefresh: () -> Unit,
    onMetricClick: (HealthViewModel.MetricType) -> Unit,
    onSettingsClick: () -> Unit,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onToday: () -> Unit,
    initialScrollIndex: Int = 0,
    initialScrollOffset: Int = 0,
    onScrollPositionChanged: (Int, Int) -> Unit = { _, _ -> }
) {
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = initialScrollIndex,
        initialFirstVisibleItemScrollOffset = initialScrollOffset
    )

    LaunchedEffect(listState) {
        snapshotFlow {
            Pair(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset)
        }.collect { (index, offset) ->
            onScrollPositionChanged(index, offset)
        }
    }

    val isToday = selectedDate == LocalDate.now()
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())
    val dateText = if (isToday) "Today" else selectedDate.format(dateFormatter)

    // Calculate readiness score
    val readinessScore = calculateReadinessScore(healthData)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "OpenHealth",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = dateText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onPreviousDay) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowLeft,
                            contentDescription = "Previous Day",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNextDay,
                        enabled = !isToday
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                            contentDescription = "Next Day",
                            tint = if (isToday) TextTertiary else TextPrimary
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundBlack
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onRefresh,
                containerColor = FabColor,
                contentColor = BackgroundBlack,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh"
                )
            }
        },
        containerColor = BackgroundBlack
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundBlack)
                .padding(paddingValues)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Readiness Score Section
                item {
                    ReadinessScoreCard(
                        readinessScore = readinessScore,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 2x2 Grid - Main Metrics with Sparklines
                item {
                    MainMetricsGrid(
                        healthData = healthData,
                        onMetricClick = onMetricClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Activity Section
                val showActivitySection = settings.showSteps || settings.showDistance || settings.showFloors
                if (showActivitySection) {
                    item { SectionHeader(title = "Activity") }

                    if (settings.showSteps && healthData.steps.count > 0) {
                        item {
                            val stepsProgress = (healthData.steps.count.toFloat() / settings.stepsGoal).coerceIn(0f, 1f)
                            ActivityCard(
                                title = "Steps",
                                value = healthData.steps.count.toString(),
                                unit = "steps",
                                progress = stepsProgress,
                                goal = "${settings.stepsGoal}",
                                color = CardSteps,
                                onClick = { onMetricClick(HealthViewModel.MetricType.STEPS) }
                            )
                        }
                    }

                    if (settings.showDistance && healthData.distance.kilometers > 0) {
                        item {
                            val distanceProgress = (healthData.distance.kilometers.toFloat() / settings.distanceGoalKm).coerceIn(0f, 1f)
                            ActivityCard(
                                title = "Distance",
                                value = String.format("%.2f", healthData.distance.kilometers),
                                unit = "km",
                                progress = distanceProgress,
                                goal = String.format("%.1f km", settings.distanceGoalKm),
                                color = CardDistance,
                                onClick = { onMetricClick(HealthViewModel.MetricType.DISTANCE) }
                            )
                        }
                    }

                    if (settings.showFloors && healthData.floors.count > 0) {
                        item {
                            val floorsProgress = (healthData.floors.count.toFloat() / settings.floorsGoal).coerceIn(0f, 1f)
                            ActivityCard(
                                title = "Floors",
                                value = healthData.floors.count.toString(),
                                unit = "floors",
                                progress = floorsProgress,
                                goal = "${settings.floorsGoal}",
                                color = CardFloors,
                                onClick = { onMetricClick(HealthViewModel.MetricType.FLOORS) }
                            )
                        }
                    }
                }

                // Calories Section
                if (settings.showCalories && healthData.calories.totalBurned > 0) {
                    item { SectionHeader(title = "Calories") }
                    item {
                        CaloriesDetailCard(
                            calories = healthData.calories,
                            onClick = { onMetricClick(HealthViewModel.MetricType.CALORIES) }
                        )
                    }
                }

                // Heart Section
                val showHeartSection = settings.showHeartRate || settings.showRestingHeartRate || settings.showHRV
                if (showHeartSection) {
                    item { SectionHeader(title = "Heart") }

                    if (settings.showHeartRate && healthData.heartRate.currentBpm != null) {
                        item {
                            HeartRateCard(
                                currentBpm = healthData.heartRate.currentBpm,
                                restingBpm = healthData.heartRate.restingBpm,
                                onClick = { onMetricClick(HealthViewModel.MetricType.HEART_RATE) }
                            )
                        }
                    }

                    if (settings.showRestingHeartRate && healthData.restingHeartRate.bpm != null) {
                        item {
                            SimpleMetricCard(
                                title = "Resting Heart Rate",
                                value = healthData.restingHeartRate.bpm.toString(),
                                unit = "bpm",
                                color = CardHeartRate,
                                onClick = { onMetricClick(HealthViewModel.MetricType.RESTING_HEART_RATE) }
                            )
                        }
                    }

                    if (settings.showHRV && healthData.heartRateVariability.rmssdMs != null) {
                        item {
                            SimpleMetricCard(
                                title = "Heart Rate Variability",
                                value = String.format("%.0f", healthData.heartRateVariability.rmssdMs),
                                unit = "ms",
                                color = CardHRV,
                                onClick = { onMetricClick(HealthViewModel.MetricType.HEART_RATE_VARIABILITY) }
                            )
                        }
                    }
                }

                // Sleep Section
                if (settings.showSleep && healthData.sleep.totalDuration != null) {
                    item { SectionHeader(title = "Sleep") }
                    item {
                        SleepDetailCard(
                            sleep = healthData.sleep,
                            onClick = { onMetricClick(HealthViewModel.MetricType.SLEEP) }
                        )
                    }
                }

                // Body Section
                val showBodySection = settings.showWeight || settings.showBodyFat || settings.showBMR ||
                        settings.showBodyWater || settings.showBoneMass || settings.showLeanBodyMass
                if (showBodySection) {
                    item { SectionHeader(title = "Body") }

                    item {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            maxItemsInEachRow = 2
                        ) {
                            if (settings.showWeight && healthData.weight.kilograms != null) {
                                CompactBodyMetricCard(
                                    title = "Weight",
                                    value = String.format("%.1f", healthData.weight.kilograms),
                                    unit = "kg",
                                    color = CardWeight,
                                    onClick = { onMetricClick(HealthViewModel.MetricType.WEIGHT) },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (settings.showBodyFat && healthData.bodyFat.percentage != null) {
                                CompactBodyMetricCard(
                                    title = "Body Fat",
                                    value = String.format("%.1f", healthData.bodyFat.percentage),
                                    unit = "%",
                                    color = CardBodyFat,
                                    onClick = { onMetricClick(HealthViewModel.MetricType.BODY_FAT) },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (settings.showBMR && healthData.basalMetabolicRate.caloriesPerDay != null) {
                                CompactBodyMetricCard(
                                    title = "BMR",
                                    value = String.format("%.0f", healthData.basalMetabolicRate.caloriesPerDay),
                                    unit = "kcal",
                                    color = CardBMR,
                                    onClick = { onMetricClick(HealthViewModel.MetricType.BASAL_METABOLIC_RATE) },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (settings.showBodyWater && healthData.bodyWaterMass.kilograms != null) {
                                CompactBodyMetricCard(
                                    title = "Body Water",
                                    value = String.format("%.1f", healthData.bodyWaterMass.kilograms),
                                    unit = "kg",
                                    color = CardBodyWater,
                                    onClick = { onMetricClick(HealthViewModel.MetricType.BODY_WATER_MASS) },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (settings.showBoneMass && healthData.boneMass.kilograms != null) {
                                CompactBodyMetricCard(
                                    title = "Bone Mass",
                                    value = String.format("%.1f", healthData.boneMass.kilograms),
                                    unit = "kg",
                                    color = CardBoneMass,
                                    onClick = { onMetricClick(HealthViewModel.MetricType.BONE_MASS) },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (settings.showLeanBodyMass && healthData.leanBodyMass.kilograms != null) {
                                CompactBodyMetricCard(
                                    title = "Lean Mass",
                                    value = String.format("%.1f", healthData.leanBodyMass.kilograms),
                                    unit = "kg",
                                    color = CardLeanBodyMass,
                                    onClick = { onMetricClick(HealthViewModel.MetricType.LEAN_BODY_MASS) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Exercise Section
                val showExerciseSection = settings.showExercise || settings.showVO2Max
                if (showExerciseSection) {
                    item { SectionHeader(title = "Exercise") }

                    if (settings.showExercise && healthData.exercise.sessionCount > 0) {
                        item {
                            SimpleMetricCard(
                                title = "Workouts",
                                value = healthData.exercise.sessionCount.toString(),
                                unit = "sessions",
                                subtitle = healthData.exercise.totalDuration?.let {
                                    val hours = it.toHours()
                                    val minutes = (it.toMinutes() % 60).toInt()
                                    "${hours}h ${minutes}m"
                                },
                                color = CardExercise,
                                onClick = { }
                            )
                        }
                    }

                    if (settings.showVO2Max && healthData.vo2Max.value != null) {
                        item {
                            SimpleMetricCard(
                                title = "VO2 Max",
                                value = String.format("%.1f", healthData.vo2Max.value),
                                unit = "ml/kg/min",
                                color = CardVo2Max,
                                onClick = { onMetricClick(HealthViewModel.MetricType.VO2_MAX) }
                            )
                        }
                    }
                }

                // Vitals Section
                val showVitalsSection = settings.showBloodGlucose || settings.showBloodPressure ||
                        settings.showBodyTemperature || settings.showOxygenSaturation || settings.showRespiratoryRate
                if (showVitalsSection) {
                    item { SectionHeader(title = "Vitals") }

                    item {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            maxItemsInEachRow = 2
                        ) {
                            if (settings.showBloodGlucose && healthData.bloodGlucose.levelMgPerDl != null) {
                                CompactBodyMetricCard(
                                    title = "Glucose",
                                    value = String.format("%.0f", healthData.bloodGlucose.levelMgPerDl),
                                    unit = "mg/dL",
                                    color = CardBloodGlucose,
                                    onClick = { onMetricClick(HealthViewModel.MetricType.BLOOD_GLUCOSE) },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (settings.showBloodPressure && healthData.bloodPressure.systolicMmHg != null) {
                                CompactBodyMetricCard(
                                    title = "Blood Pressure",
                                    value = "${healthData.bloodPressure.systolicMmHg.toInt()}/${healthData.bloodPressure.diastolicMmHg?.toInt() ?: "--"}",
                                    unit = "mmHg",
                                    color = CardBloodPressure,
                                    onClick = { onMetricClick(HealthViewModel.MetricType.BLOOD_PRESSURE) },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (settings.showBodyTemperature && healthData.bodyTemperature.temperatureCelsius != null) {
                                CompactBodyMetricCard(
                                    title = "Temperature",
                                    value = String.format("%.1f", healthData.bodyTemperature.temperatureCelsius),
                                    unit = "°C",
                                    color = CardBodyTemperature,
                                    onClick = { onMetricClick(HealthViewModel.MetricType.BODY_TEMPERATURE) },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (settings.showOxygenSaturation && healthData.oxygenSaturation.percentage != null) {
                                CompactBodyMetricCard(
                                    title = "SpO2",
                                    value = String.format("%.0f", healthData.oxygenSaturation.percentage),
                                    unit = "%",
                                    color = CardSpO2,
                                    onClick = { onMetricClick(HealthViewModel.MetricType.OXYGEN_SATURATION) },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (settings.showRespiratoryRate && healthData.respiratoryRate.ratePerMinute != null) {
                                CompactBodyMetricCard(
                                    title = "Respiratory",
                                    value = String.format("%.0f", healthData.respiratoryRate.ratePerMinute),
                                    unit = "breaths/min",
                                    color = CardRespiratoryRate,
                                    onClick = { onMetricClick(HealthViewModel.MetricType.RESPIRATORY_RATE) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Loading indicator
            AnimatedVisibility(
                visible = isLoading,
                enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)),
                exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = CardSteps,
                        strokeWidth = 3.dp
                    )
                }
            }
        }
    }
}

// Calculate Readiness Score based on recovery state, sleep recency, and physiological markers
private fun calculateReadinessScore(healthData: HealthData): ReadinessScore {
    var score = 100 // Start at max, deduct based on fatigue
    val factors = mutableListOf<String>()

    // Get current time for calculations
    val now = java.time.Instant.now()

    // Calculate hours since last sleep ended (awake time)
    val hoursSinceLastSleep = healthData.sleep.sessions.maxByOrNull { it.endTime }?.endTime?.let { lastWakeTime ->
        java.time.Duration.between(lastWakeTime, now).toHours()
    }

    // AWAKE TIME is the PRIMARY factor - start reducing after 8 hours
    // This ensures score drops significantly as the day progresses
    val awakePenalty = when {
        hoursSinceLastSleep == null -> {
            factors.add("No sleep data")
            50 // Heavy penalty for no sleep data
        }
        hoursSinceLastSleep >= 20 -> {
            factors.add("Awake ${hoursSinceLastSleep}h - exhausted")
            85 // Critical - approaching 24h awake
        }
        hoursSinceLastSleep >= 16 -> {
            factors.add("Awake ${hoursSinceLastSleep}h - very tired")
            70 // Very tired, score below 30
        }
        hoursSinceLastSleep >= 14 -> {
            factors.add("Awake ${hoursSinceLastSleep}h - tired")
            55 // Tired, score below 45
        }
        hoursSinceLastSleep >= 12 -> {
            factors.add("Awake ${hoursSinceLastSleep}h - fatigued")
            40 // Fatigued
        }
        hoursSinceLastSleep >= 10 -> {
            factors.add("Awake ${hoursSinceLastSleep}h - declining")
            65 // Score below 70
        }
        hoursSinceLastSleep >= 8 -> {
            factors.add("Awake ${hoursSinceLastSleep}h")
            15 // Start reducing
        }
        hoursSinceLastSleep >= 2 -> {
            factors.add("Awake ${hoursSinceLastSleep}h")
            5 // Small reduction after 2 hours
        }
        else -> {
            factors.add("Just woke up")
            0 // Freshly awake (within 2 hours)
        }
    }
    score -= awakePenalty

    // Sleep Duration Factor - only helps if you got enough sleep
    healthData.sleep.totalDuration?.let { sleepDuration ->
        val sleepHours = sleepDuration.toHours()
        when {
            sleepHours >= 8 -> {
                factors.add("Good sleep (${sleepHours}h)")
                // Good sleep - no bonus added to avoid inflating score
            }
            sleepHours >= 6 -> {
                factors.add("Fair sleep (${sleepHours}h)")
                // Neutral
            }
            sleepHours >= 5 -> {
                factors.add("Short sleep (${sleepHours}h)")
                score -= 10 // Extra penalty for short sleep
            }
            else -> {
                factors.add("Poor sleep (${sleepHours}h)")
                score -= 20 // Heavy penalty for very poor sleep
            }
        }
    } ?: run {
        factors.add("No sleep duration data")
        score -= 15
    }

    // Resting Heart Rate factor - can indicate fatigue
    healthData.restingHeartRate.bpm?.let { rhr ->
        when {
            rhr <= 55 -> {
                factors.add("Excellent RHR ($rhr)")
            }
            rhr <= 65 -> {
                factors.add("Good RHR ($rhr)")
            }
            rhr <= 75 -> {
                factors.add("Normal RHR ($rhr)")
                // Neutral
            }
            rhr <= 85 -> {
                factors.add("Elevated RHR ($rhr)")
                score -= 5 // Indicates some fatigue
            }
            else -> {
                factors.add("High RHR ($rhr)")
                score -= 10 // Significant fatigue indicator
            }
        }
    } ?: healthData.heartRate.restingBpm?.let { rhr ->
        when {
            rhr <= 55 -> {
                factors.add("Excellent RHR ($rhr)")
            }
            rhr <= 65 -> {
                factors.add("Good RHR ($rhr)")
            }
            rhr <= 75 -> {
                factors.add("Normal RHR ($rhr)")
                // Neutral
            }
            rhr <= 85 -> {
                factors.add("Elevated RHR ($rhr)")
                score -= 5
            }
            else -> {
                factors.add("High RHR ($rhr)")
                score -= 10
            }
        }
    } ?: run {
        factors.add("No RHR data")
    }

    // HRV factor - indicates recovery state
    healthData.heartRateVariability.rmssdMs?.let { hrv ->
        when {
            hrv >= 55 -> {
                factors.add("Good HRV (${hrv.toInt()}ms)")
                score += 2
            }
            hrv >= 40 -> {
                factors.add("Normal HRV (${hrv.toInt()}ms)")
                // Neutral
            }
            hrv >= 30 -> {
                factors.add("Low HRV (${hrv.toInt()}ms)")
                score -= 3
            }
            else -> {
                factors.add("Very low HRV (${hrv.toInt()}ms)")
                score -= 8 // Poor recovery
            }
        }
    } ?: run {
        factors.add("No HRV data")
    }

    // Activity factor - minimal impact, mainly informational
    val steps = healthData.steps.count
    when {
        steps >= 10000 -> {
            factors.add("Active today")
        }
        steps >= 5000 -> {
            factors.add("Moderate activity")
        }
        steps < 1000 -> {
            factors.add("Low activity")
            score -= 2 // Very sedentary
        }
    }

    // Clamp score to 0-100
    score = score.coerceIn(0, 100)

    // Determine label and color - ONLY "Ready" if just woke up (within 2 hours)
    val (label, color) = when {
        score >= 85 && (hoursSinceLastSleep != null && hoursSinceLastSleep < 3) -> "Ready" to ReadinessExcellent
        score >= 70 -> "Good" to ReadinessGood
        score >= 50 -> "Fair" to ReadinessFair
        score >= 30 -> "Tired" to ReadinessPoor
        else -> "Exhausted" to ReadinessPoor
    }

    return ReadinessScore(score, label, color, factors)
}

@Composable
private fun ReadinessScoreCard(
    readinessScore: ReadinessScore,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceDark
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Readiness Score",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = readinessScore.label,
                    style = MaterialTheme.typography.headlineMedium,
                    color = readinessScore.color,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = readinessScore.factors.take(2).joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }

            // Circular Score Indicator
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background circle
                CircularProgressIndicator(
                    progress = 1f,
                    modifier = Modifier.fillMaxSize(),
                    color = SurfaceVariant,
                    strokeWidth = 8.dp,
                    trackColor = SurfaceVariant
                )
                // Progress circle
                CircularProgressIndicator(
                    progress = readinessScore.score / 100f,
                    modifier = Modifier.fillMaxSize(),
                    color = readinessScore.color,
                    strokeWidth = 8.dp,
                    strokeCap = StrokeCap.Round
                )
                // Score text
                Text(
                    text = readinessScore.score.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun MainMetricsGrid(
    healthData: HealthData,
    onMetricClick: (HealthViewModel.MetricType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // First row: Steps and Heart Rate
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Steps Card with Sparkline
            MetricTileWithSparkline(
                title = "Steps",
                value = healthData.steps.count.toString(),
                unit = "steps",
                icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                color = CardSteps,
                chartColor = ChartLineSteps,
                fillColor = ChartFillSteps,
                data = generateSparklineData(healthData.steps.count, 20),
                onClick = { onMetricClick(HealthViewModel.MetricType.STEPS) },
                modifier = Modifier.weight(1f)
            )

            // Heart Rate Card with Sparkline
            MetricTileWithSparkline(
                title = "Heart Rate",
                value = healthData.heartRate.currentBpm?.toString() ?: "--",
                unit = "bpm",
                icon = Icons.Default.Favorite,
                color = CardHeartRate,
                chartColor = ChartLineHeartRate,
                fillColor = ChartFillHeartRate,
                data = generateHeartRateSparklineData(healthData.heartRate),
                onClick = { onMetricClick(HealthViewModel.MetricType.HEART_RATE) },
                modifier = Modifier.weight(1f)
            )
        }

        // Second row: Sleep and Calories
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Sleep Card with Sparkline
            val sleepHours = healthData.sleep.totalDuration?.toHours()?.toInt() ?: 0
            val sleepMinutes = healthData.sleep.totalDuration?.let { ((it.toMinutes() % 60).toInt()) } ?: 0
            MetricTileWithSparkline(
                title = "Sleep",
                value = if (sleepHours > 0 || sleepMinutes > 0) "${sleepHours}h ${sleepMinutes}m" else "--",
                unit = "",
                icon = Icons.Default.NightsStay,
                color = CardSleep,
                chartColor = ChartLineSleep,
                fillColor = ChartFillSleep,
                data = generateSleepSparklineData(sleepHours, sleepMinutes),
                onClick = { onMetricClick(HealthViewModel.MetricType.SLEEP) },
                modifier = Modifier.weight(1f)
            )

            // Calories Card with Sparkline
            MetricTileWithSparkline(
                title = "Calories",
                value = healthData.calories.totalBurned.roundToInt().toString(),
                unit = "kcal",
                icon = Icons.Default.LocalFireDepartment,
                color = CardCalories,
                chartColor = ChartLineCalories,
                fillColor = ChartFillCalories,
                data = generateSparklineData(healthData.calories.totalBurned.toLong(), 3000),
                onClick = { onMetricClick(HealthViewModel.MetricType.CALORIES) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// Generate sample sparkline data based on current value
private fun generateSparklineData(current: Long, max: Long): List<Float> {
    return List(20) { index ->
        val variation = (Math.random() * 0.4 - 0.2).toFloat() // ±20% variation
        ((current.toFloat() / max) * (1 + variation)).coerceIn(0f, 1f)
    }
}

private fun generateHeartRateSparklineData(heartRate: HeartRateData): List<Float> {
    val baseBpm = heartRate.currentBpm ?: 70
    return List(20) { index ->
        val variation = (Math.random() * 0.3 - 0.15).toFloat()
        ((baseBpm.toFloat() / 150f) * (1 + variation)).coerceIn(0.3f, 1f)
    }
}

private fun generateSleepSparklineData(hours: Int, minutes: Int): List<Float> {
    val totalMinutes = hours * 60 + minutes
    return List(20) { index ->
        val variation = (Math.random() * 0.3 - 0.15).toFloat()
        ((totalMinutes.toFloat() / 600f) * (1 + variation)).coerceIn(0.2f, 1f)
    }
}

@Composable
private fun MetricTileWithSparkline(
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    color: Color,
    chartColor: Color,
    fillColor: Color,
    data: List<Float>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceDark
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Icon and Title
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Value
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                if (unit.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Sparkline Chart
            SparklineChart(
                data = data,
                lineColor = chartColor,
                fillColor = fillColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            )
        }
    }
}

@Composable
private fun SparklineChart(
    data: List<Float>,
    lineColor: Color,
    fillColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .drawBehind {
                if (data.size < 2) return@drawBehind

                val width = size.width
                val height = size.height
                val stepX = width / (data.size - 1)

                // Create path for the line
                val path = Path().apply {
                    moveTo(0f, height - (data[0] * height))
                    for (i in 1 until data.size) {
                        val x = i * stepX
                        val y = height - (data[i] * height)
                        lineTo(x, y)
                    }
                }

                // Create path for fill
                val fillPath = Path().apply {
                    moveTo(0f, height)
                    lineTo(0f, height - (data[0] * height))
                    for (i in 1 until data.size) {
                        val x = i * stepX
                        val y = height - (data[i] * height)
                        lineTo(x, y)
                    }
                    lineTo(width, height)
                    close()
                }

                // Draw fill
                drawPath(
                    path = fillPath,
                    color = fillColor
                )

                // Draw line
                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }
    )
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = TextPrimary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun ActivityCard(
    title: String,
    value: String,
    unit: String,
    progress: Float? = null,
    goal: String? = null,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceDark
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (title) {
                                "Steps" -> Icons.AutoMirrored.Filled.DirectionsWalk
                                "Distance" -> Icons.AutoMirrored.Filled.TrendingUp
                                "Floors" -> Icons.AutoMirrored.Filled.TrendingUp
                                else -> Icons.AutoMirrored.Filled.DirectionsWalk
                            },
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                }

                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }

            if (progress != null) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = color,
                    trackColor = SurfaceVariant
                )
                if (goal != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Goal",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            text = goal,
                            style = MaterialTheme.typography.bodySmall,
                            color = color
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CaloriesDetailCard(
    calories: CaloriesData,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceDark
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CardCalories.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = CardCalories,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Calories",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CalorieStat(
                    label = "Total",
                    value = calories.totalBurned.roundToInt().toString(),
                    unit = "kcal",
                    color = CardCalories
                )
                CalorieStat(
                    label = "Active",
                    value = calories.activeBurned.roundToInt().toString(),
                    unit = "kcal",
                    color = CardExercise
                )
                CalorieStat(
                    label = "BMR",
                    value = calories.basalBurned.roundToInt().toString(),
                    unit = "kcal",
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun CalorieStat(
    label: String,
    value: String,
    unit: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
    }
}

@Composable
private fun HeartRateCard(
    currentBpm: Int?,
    restingBpm: Int?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceDark
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CardHeartRate.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = CardHeartRate,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Heart Rate",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    restingBpm?.let {
                        Text(
                            text = "Resting: $it bpm",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            currentBpm?.let {
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = it.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = CardHeartRate,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "bpm",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SleepDetailCard(
    sleep: SleepData,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceDark
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CardSleep.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NightsStay,
                        contentDescription = null,
                        tint = CardSleep,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Sleep",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "Last night",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "${sleep.hours}h ${sleep.minutes}m",
                    style = MaterialTheme.typography.headlineMedium,
                    color = CardSleep,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SimpleMetricCard(
    title: String,
    value: String,
    unit: String,
    subtitle: String? = null,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceDark
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun CompactBodyMetricCard(
    title: String,
    value: String,
    unit: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceDark
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}
