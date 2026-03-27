package com.openhealth.openhealth.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
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
import java.time.ZoneId
import com.openhealth.openhealth.model.CaloriesData
import com.openhealth.openhealth.model.HealthData
import com.openhealth.openhealth.model.SettingsData
import com.openhealth.openhealth.viewmodel.HealthViewModel
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

// Fitbit Premium Colors (dark defaults, overridden in composable)
private val PureBlack = Color(0xFF000000)
private val CardBackground = Color(0xFF1A1A1A)
private val StepsCyan = Color(0xFF00BCD4)
private val HeartRed = Color(0xFFF44336)
private val SleepPurple = Color(0xFF9C27B0)
private val CaloriesOrange = Color(0xFFFF9800)
private val ReadinessRed = Color(0xFFE53935)
private val ReadinessYellow = Color(0xFFFFB300)
private val ReadinessGreen = Color(0xFF43A047)

// Readiness Score Data Class
private data class ReadinessScore(
    val score: Int,
    val label: String,
    val gradient: Brush
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    healthData: HealthData,
    isLoading: Boolean,
    settings: SettingsData,
    selectedDate: LocalDate = LocalDate.now(ZoneId.systemDefault()),
    onRefresh: () -> Unit,
    onMetricClick: (HealthViewModel.MetricType) -> Unit,
    onSettingsClick: () -> Unit,
    onReadinessClick: () -> Unit,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onToday: () -> Unit,
    onDateSelected: ((LocalDate) -> Unit)? = null,
    onReportsClick: () -> Unit = {},
    onStressClick: () -> Unit = {},
    onAiInsightsClick: () -> Unit = {},
    weatherData: com.openhealth.openhealth.utils.WeatherData = com.openhealth.openhealth.utils.WeatherData(),
    stepsCalendarData: List<com.openhealth.openhealth.model.DailyDataPoint> = emptyList(),
    stepsStreak: Int = 0,
    bodyExpanded: Boolean = false,
    vitalsExpanded: Boolean = false,
    onBodyExpandedChange: (Boolean) -> Unit = {},
    onVitalsExpandedChange: (Boolean) -> Unit = {},
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

    val isToday = selectedDate == LocalDate.now(ZoneId.systemDefault())
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())
    val dateText = if (isToday) "Today" else selectedDate.format(dateFormatter)

    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        CustomCalendarDialog(
            initialDate = selectedDate,
            onDismiss = { showDatePicker = false },
            onDateSelected = { picked ->
                onDateSelected?.invoke(picked)
                showDatePicker = false
            },
            data = stepsCalendarData,
            goal = settings.stepsGoal
        )
    }

    // Calculate readiness score
    val readinessScore = calculateReadinessScore(healthData)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier.clickable { showDatePicker = true }
                    ) {
                        Text(
                            text = "OpenHealth",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = dateText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onPreviousDay) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowLeft,
                            contentDescription = "Previous Day",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    if (!isToday) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF00BCD4).copy(alpha = 0.15f))
                                .clickable { onToday() }
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Today",
                                color = Color(0xFF00BCD4),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    IconButton(
                        onClick = onNextDay,
                        enabled = !isToday
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                            contentDescription = "Next Day",
                            tint = if (isToday) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = onReportsClick) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = "Reports",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onRefresh,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = StepsCyan,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh"
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Readiness Score Card
                item {
                    ReadinessScoreCard(
                        readinessScore = readinessScore,
                        onClick = onReadinessClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Weather Health Advisory
                if (weatherData.isAvailable) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "${String.format("%.0f", weatherData.temperature)}°", color = MaterialTheme.colorScheme.onBackground, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(text = "UV ${String.format("%.0f", weatherData.uvIndex)} (${weatherData.uvLabel})", color = when(weatherData.uvLabel) { "Low" -> Color(0xFF4CD964); "Moderate" -> Color(0xFFFFCC00); else -> Color(0xFFFF3B30) }, fontSize = 13.sp)
                                            Text(text = "Air: ${weatherData.aqiLabel}", color = when(weatherData.aqi) { 1 -> Color(0xFF4CD964); 2 -> Color(0xFF4CD964); 3 -> Color(0xFFFFCC00); else -> Color(0xFFFF3B30) }, fontSize = 13.sp)
                                        }
                                    }
                                }
                                if (weatherData.healthAdvisory != "Good conditions for outdoor activity") {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = weatherData.healthAdvisory, color = Color(0xFFFFCC00), fontSize = 12.sp, lineHeight = 16.sp)
                                }
                            }
                        }
                    }
                }

                // Stress & Energy (estimated from HRV)
                if (healthData.heartRateVariability.rmssdMs != null) {
                    item {
                        val hrv = healthData.heartRateVariability.rmssdMs!!
                        // Stress: inverse of HRV. HRV 60+ = low stress, HRV 20- = high stress
                        val stressLevel = ((80.0 - hrv.coerceIn(10.0, 80.0)) / 70.0 * 100).toInt().coerceIn(0, 100)
                        val stressLabel = when {
                            stressLevel < 25 -> "Low"
                            stressLevel < 50 -> "Moderate"
                            stressLevel < 75 -> "High"
                            else -> "Very High"
                        }
                        val stressColor = when {
                            stressLevel < 25 -> Color(0xFF4CD964)
                            stressLevel < 50 -> Color(0xFFFFCC00)
                            stressLevel < 75 -> Color(0xFFFF9500)
                            else -> Color(0xFFFF3B30)
                        }
                        // Energy: based on readiness score
                        val energyPct = readinessScore.score.coerceIn(0, 100)

                        StressEnergyCard(
                            stressLevel = stressLevel,
                            stressLabel = stressLabel,
                            stressColor = stressColor,
                            energyPercent = energyPct,
                            onClick = onStressClick
                        )
                    }
                }

                // AI Insights Card (only if provider is configured)
                if (settings.aiProvider != com.openhealth.openhealth.model.AiProvider.NONE) {
                    item {
                        DetailCard(
                            title = "✨ AI Health Analysis",
                            value = "Get personalized insights from ${settings.aiProvider.name}",
                            onClick = onAiInsightsClick
                        )
                    }
                }

                // Steps Card - Full width with left accent
                item {
                    MetricCard(
                        title = "Steps",
                        value = healthData.steps.count.toString(),
                        unit = "steps",
                        icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                        accentColor = StepsCyan,
                        sparklineData = generateSparklineData(healthData.steps.count, 20000),
                        onClick = { onMetricClick(HealthViewModel.MetricType.STEPS) },
                        subtitle = if (settings.showStepsStreak && stepsStreak > 0) "🔥 $stepsStreak day streak!" else ""
                    )
                }

                // Heart Rate Card - Full width with left accent
                item {
                    MetricCard(
                        title = "Heart Rate",
                        value = healthData.heartRate.currentBpm?.toString() ?: "--",
                        unit = "bpm",
                        icon = Icons.Default.Favorite,
                        accentColor = HeartRed,
                        sparklineData = generateHeartRateSparklineData(healthData.heartRate.currentBpm ?: 70),
                        onClick = { onMetricClick(HealthViewModel.MetricType.HEART_RATE) }
                    )
                }

                // Sleep Card - Full width with left accent
                item {
                    val sleepHours = healthData.sleep.totalDuration?.toHours()?.toInt() ?: 0
                    val sleepMinutes = healthData.sleep.totalDuration?.let { ((it.toMinutes() % 60).toInt()) } ?: 0
                    val sleepTimeRange = healthData.sleep.sessions.maxByOrNull { it.endTime }?.let { session ->
                        val formatter = java.time.format.DateTimeFormatter.ofPattern("h:mm a")
                        val startTime = session.startTime.atZone(java.time.ZoneId.systemDefault()).format(formatter)
                        val endTime = session.endTime.atZone(java.time.ZoneId.systemDefault()).format(formatter)
                        "$startTime → $endTime"
                    } ?: ""
                    MetricCard(
                        title = "Sleep",
                        value = if (sleepHours > 0 || sleepMinutes > 0) "${sleepHours}h ${sleepMinutes}m" else "--",
                        unit = "",
                        subtitle = sleepTimeRange,
                        icon = Icons.Default.NightsStay,
                        accentColor = SleepPurple,
                        sparklineData = generateSleepSparklineData(sleepHours * 60 + sleepMinutes),
                        onClick = { onMetricClick(HealthViewModel.MetricType.SLEEP) }
                    )
                }

                // Calories Card - Full width with left accent
                item {
                    MetricCard(
                        title = "Calories",
                        value = healthData.calories.totalBurned.roundToInt().toString(),
                        unit = "kcal",
                        icon = Icons.Default.LocalFireDepartment,
                        accentColor = CaloriesOrange,
                        sparklineData = generateSparklineData(healthData.calories.totalBurned.toLong(), 3000),
                        onClick = { onMetricClick(HealthViewModel.MetricType.CALORIES) }
                    )
                }

                // Activity Section
                val hasDistance = settings.showDistance && healthData.distance.kilometers > 0
                val hasFloors = settings.showFloors && healthData.floors.count > 0
                val hasExercise = settings.showExercise && healthData.exercise.sessions.isNotEmpty()

                if (hasDistance || hasFloors || hasExercise) {
                    item {
                        SectionHeader(title = "Activity")
                    }
                }

                if (hasExercise) {
                    item {
                        val totalMin = healthData.exercise.totalDuration?.toMinutes() ?: 0
                        val sessionText = if (healthData.exercise.sessionCount == 1) "1 session" else "${healthData.exercise.sessionCount} sessions"
                        val durationText = if (totalMin >= 60) "${totalMin / 60}h ${totalMin % 60}m" else "${totalMin}m"
                        DetailCard(
                            title = "Exercise",
                            value = "$durationText ($sessionText)",
                            onClick = { onMetricClick(HealthViewModel.MetricType.EXERCISE) }
                        )
                    }
                }

                if (hasDistance) {
                    item {
                        DetailCard(
                            title = "Distance",
                            value = String.format("%.2f km", healthData.distance.kilometers),
                            progress = (healthData.distance.kilometers.toFloat() / settings.distanceGoalKm).coerceIn(0f, 1f),
                            onClick = { onMetricClick(HealthViewModel.MetricType.DISTANCE) }
                        )
                    }
                }

                if (hasFloors) {
                    item {
                        DetailCard(
                            title = "Floors Climbed",
                            value = "${healthData.floors.count}",
                            progress = (healthData.floors.count.toFloat() / settings.floorsGoal).coerceIn(0f, 1f),
                            onClick = { onMetricClick(HealthViewModel.MetricType.FLOORS) }
                        )
                    }
                }

                // VO2 Max
                val hasVO2Max = settings.showVO2Max && healthData.vo2Max.value != null && healthData.vo2Max.value > 0

                if (hasVO2Max) {
                    item {
                        DetailCard(
                            title = "VO2 Max",
                            value = String.format("%.1f ml/kg/min", healthData.vo2Max.value),
                            onClick = { onMetricClick(HealthViewModel.MetricType.VO2_MAX) }
                        )
                    }
                }

                // Body Composition (collapsible card)
                val hasWeight = settings.showWeight && healthData.weight.kilograms != null
                val hasBodyFat = settings.showBodyFat && healthData.bodyFat.percentage != null
                val hasBMR = settings.showBMR && healthData.basalMetabolicRate.caloriesPerDay != null
                val hasBodyWater = settings.showBodyWater && healthData.bodyWaterMass.kilograms != null
                val hasBoneMass = settings.showBoneMass && healthData.boneMass.kilograms != null
                val hasLeanMass = settings.showLeanBodyMass && healthData.leanBodyMass.kilograms != null
                val hasAnyBody = hasWeight || hasBodyFat || hasBMR || hasBodyWater || hasBoneMass || hasLeanMass

                if (hasAnyBody) {
                    item {
                        BodyCompositionCard(
                            healthData = healthData,
                            hasWeight = hasWeight,
                            hasBodyFat = hasBodyFat,
                            hasBMR = hasBMR,
                            hasBodyWater = hasBodyWater,
                            hasBoneMass = hasBoneMass,
                            hasLeanMass = hasLeanMass,
                            expanded = bodyExpanded,
                            onExpandedChange = onBodyExpandedChange,
                            onMetricClick = onMetricClick
                        )
                    }
                }

                // Vitals Section
                val hasHRV = settings.showHRV && healthData.heartRateVariability.rmssdMs != null
                val hasBloodOxygen = settings.showOxygenSaturation && healthData.oxygenSaturation.percentage != null
                val hasBloodPressure = settings.showBloodPressure && healthData.bloodPressure.systolicMmHg != null
                val hasBodyTemp = settings.showBodyTemperature && healthData.bodyTemperature.temperatureCelsius != null
                val hasRespiratoryRate = settings.showRespiratoryRate && healthData.respiratoryRate.ratePerMinute != null
                val hasSkinTemp = settings.showSkinTemperature && healthData.skinTemperature.temperatureCelsius != null
                val hasBloodGlucose = settings.showBloodGlucose && healthData.bloodGlucose.levelMgPerDl != null

                val hasAnyVitals = hasHRV || hasBloodOxygen || hasBloodPressure || hasBodyTemp || hasRespiratoryRate || hasSkinTemp || hasBloodGlucose

                if (hasAnyVitals) {
                    item {
                        VitalsCard(
                            healthData = healthData,
                            hasHRV = hasHRV,
                            hasBloodOxygen = hasBloodOxygen,
                            hasBloodPressure = hasBloodPressure,
                            hasBodyTemp = hasBodyTemp,
                            hasRespiratoryRate = hasRespiratoryRate,
                            hasSkinTemp = hasSkinTemp,
                            hasBloodGlucose = hasBloodGlucose,
                            expanded = vitalsExpanded,
                            onExpandedChange = onVitalsExpandedChange,
                            onMetricClick = onMetricClick
                        )
                    }
                }

                // Nutrition Section
                val hasNutrition = settings.showNutrition && healthData.nutrition.calories != null && healthData.nutrition.calories > 0

                if (hasNutrition) {
                    item {
                        SectionHeader(title = "Nutrition")
                    }
                    item {
                        val cal = healthData.nutrition.calories?.roundToInt() ?: 0
                        val protein = healthData.nutrition.proteinGrams?.roundToInt() ?: 0
                        val carbs = healthData.nutrition.carbsGrams?.roundToInt() ?: 0
                        val fat = healthData.nutrition.fatGrams?.roundToInt() ?: 0
                        DetailCard(
                            title = "Nutrition",
                            value = "$cal kcal  P:${protein}g  C:${carbs}g  F:${fat}g",
                            onClick = { onMetricClick(HealthViewModel.MetricType.NUTRITION) }
                        )
                    }
                }

                // Wellness Section (Mindfulness)
                val hasMindfulness = settings.showMindfulness && healthData.mindfulness.duration != null

                if (hasMindfulness) {
                    item {
                        SectionHeader(title = "Wellness")
                    }
                    item {
                        val typeText = healthData.mindfulness.sessionType ?: "Session"
                        DetailCard(
                            title = "Mindfulness",
                            value = "${healthData.mindfulness.minutes}m ($typeText)",
                            onClick = { }
                        )
                    }
                }

                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

        }
    }
}

// Calculate Readiness Score based on recovery state
// Score starts at 100, only penalties apply (no bonuses)
// Final score = 100 - all penalties
// Minimum score = 5 (never show 0)
private fun calculateReadinessScore(healthData: HealthData): ReadinessScore {
    var score = 100
    val factors = mutableListOf<String>()
    val now = java.time.Instant.now()

    val hoursSinceLastSleep = healthData.sleep.sessions.maxByOrNull { it.endTime }?.endTime?.let { lastWakeTime ->
        java.time.Duration.between(lastWakeTime, now).toHours()
    }

    // Awake time penalties - hard caps on maximum score
    val awakePenalty = when {
        hoursSinceLastSleep == null -> {
            factors.add("No sleep data")
            50
        }
        hoursSinceLastSleep >= 16 -> {
            factors.add("Awake ${hoursSinceLastSleep}h - exhausted")
            90  // Score max 10
        }
        hoursSinceLastSleep >= 14 -> {
            factors.add("Awake ${hoursSinceLastSleep}h - very tired")
            85  // Score max 15
        }
        hoursSinceLastSleep >= 12 -> {
            factors.add("Awake ${hoursSinceLastSleep}h - tired")
            75  // Score max 25
        }
        hoursSinceLastSleep >= 10 -> {
            factors.add("Awake ${hoursSinceLastSleep}h - fatigued")
            65  // Score max 35
        }
        hoursSinceLastSleep >= 8 -> {
            factors.add("Awake ${hoursSinceLastSleep}h - declining")
            25
        }
        hoursSinceLastSleep >= 2 -> {
            factors.add("Awake ${hoursSinceLastSleep}h")
            5
        }
        else -> {
            factors.add("Just woke up")
            0
        }
    }
    score -= awakePenalty

    // Sleep duration - only penalties
    healthData.sleep.totalDuration?.let { sleepDuration ->
        val sleepHours = sleepDuration.toHours()
        when {
            sleepHours >= 7 -> {
                // Good sleep - no penalty
            }
            sleepHours >= 5 -> {
                factors.add("Short sleep ${sleepHours}h")
                score -= 10
            }
            else -> {
                factors.add("Poor sleep ${sleepHours}h")
                score -= 20
            }
        }
    } ?: run {
        factors.add("No sleep data")
        score -= 15
    }

    // Resting Heart Rate - only penalties for elevated RHR
    healthData.restingHeartRate.bpm?.let { rhr ->
        when {
            rhr <= 75 -> {
                // Normal RHR - no penalty
            }
            rhr <= 85 -> {
                factors.add("Elevated RHR $rhr")
                score -= 5
            }
            else -> {
                factors.add("High RHR $rhr")
                score -= 10
            }
        }
    } ?: healthData.heartRate.restingBpm?.let { rhr ->
        when {
            rhr <= 75 -> {
                // Normal RHR - no penalty
            }
            rhr <= 85 -> {
                factors.add("Elevated RHR $rhr")
                score -= 5
            }
            else -> {
                factors.add("High RHR $rhr")
                score -= 10
            }
        }
    } ?: run {
        factors.add("No RHR data")
    }

    // HRV - only penalties for low values
    healthData.heartRateVariability.rmssdMs?.let { hrv ->
        when {
            hrv >= 40 -> {
                // Normal HRV - no penalty
            }
            hrv >= 30 -> {
                factors.add("Low HRV ${hrv.toInt()}ms")
                score -= 3
            }
            else -> {
                factors.add("Very low HRV ${hrv.toInt()}ms")
                score -= 8
            }
        }
    } ?: run {
        factors.add("No HRV data")
    }

    // Activity - only penalty for very low activity
    val steps = healthData.steps.count
    when {
        steps >= 1000 -> {
            // Sufficient activity - no penalty
        }
        else -> {
            factors.add("Low activity")
            score -= 2
        }
    }

    // Apply minimum score of 5 (never show 0)
    score = score.coerceIn(5, 100)

    // Updated score labels
    val (label, gradient) = when {
        score >= 81 ->
            "Excellent" to Brush.verticalGradient(listOf(ReadinessGreen, ReadinessGreen.copy(alpha = 0.7f)))
        score >= 61 ->
            "Good" to Brush.verticalGradient(listOf(ReadinessGreen, ReadinessGreen.copy(alpha = 0.7f)))
        score >= 41 ->
            "Fair" to Brush.verticalGradient(listOf(ReadinessYellow, ReadinessYellow.copy(alpha = 0.7f)))
        score >= 21 ->
            "Poor" to Brush.verticalGradient(listOf(ReadinessRed, ReadinessRed.copy(alpha = 0.7f)))
        else ->
            "Exhausted" to Brush.verticalGradient(listOf(ReadinessRed, ReadinessRed.copy(alpha = 0.7f)))
    }

    return ReadinessScore(score, label, gradient)
}

@Composable
private fun ReadinessScoreCard(
    readinessScore: ReadinessScore,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(readinessScore.gradient)
                .padding(vertical = 32.dp, horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = readinessScore.score.toString(),
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 72.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = readinessScore.label,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Readiness Score",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun ReadinessWithRingsCard(
    readinessScore: ReadinessScore,
    activityPercent: Float,
    recoveryPercent: Float,
    sleepPercent: Float,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(readinessScore.gradient)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Readiness score
            Text(
                text = readinessScore.score.toString(),
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 56.sp
            )
            Text(
                text = readinessScore.label,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Readiness Score",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Three rings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MiniRingItem(percent = activityPercent, label = "Activity", color = StepsCyan)
                MiniRingItem(percent = recoveryPercent, label = "Recovery", color = Color(0xFF4CD964))
                MiniRingItem(percent = sleepPercent, label = "Sleep", color = SleepPurple)
            }
        }
    }
}

@Composable
private fun MiniRingItem(percent: Float, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(56.dp)) {
            Canvas(modifier = Modifier.size(56.dp)) {
                val strokeW = 5.dp.toPx()
                val arcSize = Size(size.width - strokeW, size.height - strokeW)
                val topLeft = Offset(strokeW / 2, strokeW / 2)
                drawArc(color = Color.White.copy(alpha = 0.2f), startAngle = -90f, sweepAngle = 360f, useCenter = false, style = Stroke(width = strokeW, cap = StrokeCap.Round), topLeft = topLeft, size = arcSize)
                drawArc(color = color, startAngle = -90f, sweepAngle = 360f * percent, useCenter = false, style = Stroke(width = strokeW, cap = StrokeCap.Round), topLeft = topLeft, size = arcSize)
            }
            Text(
                text = "${(percent * 100).roundToInt()}%",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    unit: String,
    subtitle: String = "",
    icon: ImageVector,
    accentColor: Color,
    sparklineData: List<Float>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Left accent border
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(100.dp)
                    .background(accentColor)
                    .align(Alignment.CenterStart)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: Icon, title, and large value
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = value,
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 48.sp
                        )
                        if (unit.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = unit,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                    }
                    if (subtitle.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }

                // Right side: Mini bar chart
                MiniBarChart(
                    data = sparklineData,
                    barColor = accentColor,
                    modifier = Modifier
                        .width(80.dp)
                        .height(40.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun DetailCard(
    title: String,
    value: String,
    progress: Float? = null,
    statusColor: Color? = null,
    sparklineData: List<Float>? = null,
    sparklineColor: Color = StepsCyan,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (statusColor != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(statusColor, RoundedCornerShape(4.dp))
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold
                )
                if (progress != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = StepsCyan,
                        trackColor = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
            // Mini sparkline
            if (sparklineData != null && sparklineData.size >= 2) {
                Spacer(modifier = Modifier.width(8.dp))
                Canvas(
                    modifier = Modifier
                        .width(60.dp)
                        .height(30.dp)
                ) {
                    val maxVal = sparklineData.max()
                    val minVal = sparklineData.min()
                    val range = (maxVal - minVal).coerceAtLeast(0.01f)
                    val stepX = size.width / (sparklineData.size - 1)
                    val path = Path()

                    sparklineData.forEachIndexed { i, v ->
                        val x = i * stepX
                        val y = size.height - ((v - minVal) / range * size.height)
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }

                    drawPath(
                        path = path,
                        color = sparklineColor,
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
            if (onClick != null) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View details",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun MetricRow(
    label: String,
    value: String,
    progress: Float? = null,
    showDivider: Boolean = false
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )
        }
        if (progress != null) {
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = StepsCyan,
                trackColor = MaterialTheme.colorScheme.outlineVariant
            )
        }
        if (showDivider) {
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun MetricRowWithDivider(
    label: String,
    value: String,
    showDivider: Boolean = false
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )
        }
        if (showDivider) {
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun MiniBarChart(
    data: List<Float>,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    val barWidth = 10.dp
    val gap = 3.dp
    val cornerRadius = 2.dp

    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas

        val canvasWidth = size.width
        val canvasHeight = size.height
        val barWidthPx = barWidth.toPx()
        val gapPx = gap.toPx()
        val cornerRadiusPx = cornerRadius.toPx()

        // Calculate total width needed for all bars and gaps
        val totalBarsWidth = data.size * barWidthPx
        val totalGapsWidth = (data.size - 1) * gapPx
        val totalContentWidth = totalBarsWidth + totalGapsWidth

        // Center the chart horizontally
        val startX = (canvasWidth - totalContentWidth) / 2

        // Find max value for normalization (or use 1.0 if all zeros)
        val maxValue = data.maxOrNull()?.coerceAtLeast(0.1f) ?: 1f

        data.forEachIndexed { index, value ->
            // Normalize height: value of 0 = 10% height, otherwise proportional
            val normalizedHeight = if (value <= 0f) {
                0.1f  // 10% for no data
            } else {
                (value / maxValue).coerceIn(0.1f, 1f)
            }

            val barHeight = normalizedHeight * canvasHeight
            val x = startX + index * (barWidthPx + gapPx)
            val y = canvasHeight - barHeight

            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidthPx, barHeight),
                cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
            )
        }
    }
}

// Generate 7 days of realistic sample data with today's value being the actual current value
// Returns list of 7 floats: [day1, day2, day3, day4, day5, day6, today]
private fun generateSparklineData(current: Long, max: Long): List<Float> {
    val todayValue = (current.toFloat() / max).coerceIn(0f, 1f)

    // Generate 6 previous days with realistic variations around the today's value
    val previousDays = List(6) { index ->
        // Create some variation - some days higher, some lower
        val variation = when (index % 3) {
            0 -> 0.8f  // Lower day
            1 -> 1.2f  // Higher day
            else -> 1.0f  // Average day
        }
        val value = todayValue * variation
        // If today has no data, previous days also have no data
        if (todayValue <= 0f) 0f else value.coerceIn(0f, 1f)
    }

    return previousDays + listOf(todayValue)
}

private fun generateHeartRateSparklineData(baseBpm: Int): List<Float> {
    val todayValue = (baseBpm.toFloat() / 120f).coerceIn(0f, 1f)

    val previousDays = List(6) { index ->
        val variation = when (index % 3) {
            0 -> 0.9f
            1 -> 1.1f
            else -> 1.0f
        }
        val value = todayValue * variation
        if (baseBpm <= 0) 0f else value.coerceIn(0f, 1f)
    }

    return previousDays + listOf(todayValue)
}

private fun generateSleepSparklineData(totalMinutes: Int): List<Float> {
    val todayValue = (totalMinutes.toFloat() / 480f).coerceIn(0f, 1f)

    val previousDays = List(6) { index ->
        val variation = when (index % 3) {
            0 -> 0.7f  // Short sleep
            1 -> 1.3f  // Long sleep
            else -> 1.0f  // Average
        }
        val value = todayValue * variation
        if (totalMinutes <= 0) 0f else value.coerceIn(0f, 1f)
    }

    return previousDays + listOf(todayValue)
}

@Composable
private fun ThreeRingGauge(
    activityPercent: Float,
    recoveryPercent: Float,
    sleepPercent: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RingGaugeItem(percent = activityPercent, label = "Activity", color = StepsCyan)
            RingGaugeItem(percent = recoveryPercent, label = "Recovery", color = Color(0xFF4CD964))
            RingGaugeItem(percent = sleepPercent, label = "Sleep", color = SleepPurple)
        }
    }
}

@Composable
private fun RingGaugeItem(percent: Float, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
            Canvas(modifier = Modifier.size(80.dp)) {
                val strokeW = 8.dp.toPx()
                val arcSize = Size(size.width - strokeW, size.height - strokeW)
                val topLeft = Offset(strokeW / 2, strokeW / 2)
                drawArc(color = color.copy(alpha = 0.15f), startAngle = -90f, sweepAngle = 360f, useCenter = false, style = Stroke(width = strokeW, cap = StrokeCap.Round), topLeft = topLeft, size = arcSize)
                drawArc(color = color, startAngle = -90f, sweepAngle = 360f * percent, useCenter = false, style = Stroke(width = strokeW, cap = StrokeCap.Round), topLeft = topLeft, size = arcSize)
            }
            Text(text = "${(percent * 100).roundToInt()}%", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
    }
}

@Composable
private fun StressEnergyCard(stressLevel: Int, stressLabel: String, stressColor: Color, energyPercent: Int, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Stress & Energy", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = "Stress", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(text = "$stressLevel", color = stressColor, fontWeight = FontWeight.Bold, fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = stressLabel, color = stressColor, fontSize = 14.sp, modifier = Modifier.padding(bottom = 4.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(text = "⚡", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f).height(8.dp).background(MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(4.dp))) {
                    Box(modifier = Modifier.fillMaxWidth(energyPercent / 100f).height(8.dp).background(Color(0xFF4CD964), RoundedCornerShape(4.dp)))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "$energyPercent%", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun BodyCompositionCard(
    healthData: HealthData,
    hasWeight: Boolean,
    hasBodyFat: Boolean,
    hasBMR: Boolean,
    hasBodyWater: Boolean,
    hasBoneMass: Boolean,
    hasLeanMass: Boolean,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onMetricClick: (HealthViewModel.MetricType) -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(150)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header — always visible
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandedChange(!expanded) },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Body Composition",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Summary row — always visible (Weight + Body Fat)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (hasWeight) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = String.format("%.1f", healthData.weight.kilograms), color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        Text(text = "kg", color = MaterialTheme.colorScheme.outline, fontSize = 12.sp)
                    }
                }
                if (hasBodyFat) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = String.format("%.1f", healthData.bodyFat.percentage), color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        Text(text = "% fat", color = MaterialTheme.colorScheme.outline, fontSize = 12.sp)
                    }
                }
                if (hasLeanMass) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = String.format("%.1f", healthData.leanBodyMass.kilograms), color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        Text(text = "kg lean", color = MaterialTheme.colorScheme.outline, fontSize = 12.sp)
                    }
                }
            }

            // Expanded details
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(8.dp))

                if (hasWeight) {
                    BodyMetricRow("Weight", String.format("%.1f kg", healthData.weight.kilograms)) {
                        onMetricClick(HealthViewModel.MetricType.WEIGHT)
                    }
                }
                if (hasBodyFat) {
                    BodyMetricRow("Body Fat", String.format("%.1f%%", healthData.bodyFat.percentage)) {
                        onMetricClick(HealthViewModel.MetricType.BODY_FAT)
                    }
                }
                if (hasBMR) {
                    BodyMetricRow("BMR", String.format("%.0f kcal", healthData.basalMetabolicRate.caloriesPerDay)) {
                        onMetricClick(HealthViewModel.MetricType.BASAL_METABOLIC_RATE)
                    }
                }
                if (hasBodyWater) {
                    BodyMetricRow("Body Water", String.format("%.1f kg", healthData.bodyWaterMass.kilograms)) {
                        onMetricClick(HealthViewModel.MetricType.BODY_WATER_MASS)
                    }
                }
                if (hasBoneMass) {
                    BodyMetricRow("Bone Mass", String.format("%.1f kg", healthData.boneMass.kilograms)) {
                        onMetricClick(HealthViewModel.MetricType.BONE_MASS)
                    }
                }
                if (hasLeanMass) {
                    BodyMetricRow("Lean Mass", String.format("%.1f kg", healthData.leanBodyMass.kilograms)) {
                        onMetricClick(HealthViewModel.MetricType.LEAN_BODY_MASS)
                    }
                }
            }
        }
    }
}

@Composable
private fun BodyMetricRow(label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = value, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun VitalsCard(
    healthData: HealthData,
    hasHRV: Boolean,
    hasBloodOxygen: Boolean,
    hasBloodPressure: Boolean,
    hasBodyTemp: Boolean,
    hasRespiratoryRate: Boolean,
    hasSkinTemp: Boolean,
    hasBloodGlucose: Boolean,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onMetricClick: (HealthViewModel.MetricType) -> Unit
) {

    // Check if all vitals are normal
    val allNormal = listOf(
        !hasHRV || healthData.heartRateVariability.rmssdMs!! >= 30,
        !hasBloodOxygen || healthData.oxygenSaturation.percentage!! >= 95,
        !hasRespiratoryRate || healthData.respiratoryRate.ratePerMinute!! in 12.0..20.0
    ).all { it }
    val statusColor = if (allNormal) Color(0xFF4CD964) else Color(0xFFFFCC00)
    val statusText = if (allNormal) "All normal" else "Needs attention"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandedChange(!expanded) },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Vitals",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(text = statusText, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Summary row — always visible (top 3 vitals)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (hasHRV) {
                    val hrvDisplay = healthData.heartRateVariability.avgMs ?: healthData.heartRateVariability.rmssdMs!!
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = String.format("%.0f", hrvDisplay), color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        Text(text = "ms HRV", color = MaterialTheme.colorScheme.outline, fontSize = 12.sp)
                    }
                }
                if (hasBloodOxygen) {
                    val spo2Display = healthData.oxygenSaturation.avgPercentage ?: healthData.oxygenSaturation.percentage!!
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = String.format("%.0f%%", spo2Display), color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        Text(text = "SpO2", color = MaterialTheme.colorScheme.outline, fontSize = 12.sp)
                    }
                }
                if (hasRespiratoryRate) {
                    val rrDisplay = healthData.respiratoryRate.avgRate ?: healthData.respiratoryRate.ratePerMinute!!
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = String.format("%.0f", rrDisplay), color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        Text(text = "breaths", color = MaterialTheme.colorScheme.outline, fontSize = 12.sp)
                    }
                }
            }

            // Expanded details
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(8.dp))

                if (hasHRV) {
                    val hrvAvg = healthData.heartRateVariability.avgMs ?: healthData.heartRateVariability.rmssdMs!!
                    val dot = when { hrvAvg >= 30 -> Color(0xFF4CD964); hrvAvg >= 20 -> Color(0xFFFFCC00); else -> Color(0xFFFF3B30) }
                    VitalMetricRow("Heart Rate Variability", String.format("%.0f ms", hrvAvg), dot) { onMetricClick(HealthViewModel.MetricType.HEART_RATE_VARIABILITY) }
                }
                if (hasBloodOxygen) {
                    val spo2Avg = healthData.oxygenSaturation.avgPercentage ?: healthData.oxygenSaturation.percentage!!
                    val dot = when { spo2Avg >= 95 -> Color(0xFF4CD964); spo2Avg >= 90 -> Color(0xFFFFCC00); else -> Color(0xFFFF3B30) }
                    VitalMetricRow("Blood Oxygen", String.format("%.0f%%", spo2Avg), dot) { onMetricClick(HealthViewModel.MetricType.OXYGEN_SATURATION) }
                }
                if (hasBloodGlucose) {
                    val bg = healthData.bloodGlucose.levelMgPerDl!!
                    val dot = when { bg in 70.0..100.0 -> Color(0xFF4CD964); bg in 60.0..140.0 -> Color(0xFFFFCC00); else -> Color(0xFFFF3B30) }
                    VitalMetricRow("Blood Glucose", String.format("%.0f mg/dL", bg), dot) { onMetricClick(HealthViewModel.MetricType.BLOOD_GLUCOSE) }
                }
                if (hasBloodPressure) {
                    val sys = healthData.bloodPressure.systolicMmHg!!
                    val dot = when { sys in 90.0..120.0 -> Color(0xFF4CD964); sys in 80.0..140.0 -> Color(0xFFFFCC00); else -> Color(0xFFFF3B30) }
                    VitalMetricRow("Blood Pressure", String.format("%.0f/%.0f mmHg", sys, healthData.bloodPressure.diastolicMmHg), dot) { onMetricClick(HealthViewModel.MetricType.BLOOD_PRESSURE) }
                }
                if (hasBodyTemp) {
                    val temp = healthData.bodyTemperature.temperatureCelsius!!
                    val dot = when { temp in 36.1..37.2 -> Color(0xFF4CD964); temp in 35.5..38.0 -> Color(0xFFFFCC00); else -> Color(0xFFFF3B30) }
                    VitalMetricRow("Body Temperature", String.format("%.1f°C", temp), dot) { onMetricClick(HealthViewModel.MetricType.BODY_TEMPERATURE) }
                }
                if (hasRespiratoryRate) {
                    val rrAvg = healthData.respiratoryRate.avgRate ?: healthData.respiratoryRate.ratePerMinute!!
                    val dot = when { rrAvg in 12.0..20.0 -> Color(0xFF4CD964); rrAvg in 8.0..25.0 -> Color(0xFFFFCC00); else -> Color(0xFFFF3B30) }
                    VitalMetricRow("Respiratory Rate", String.format("%.0f breaths/min", rrAvg), dot) { onMetricClick(HealthViewModel.MetricType.RESPIRATORY_RATE) }
                }
                if (hasSkinTemp) {
                    VitalMetricRow("Skin Temp", String.format("%.1f°C", healthData.skinTemperature.temperatureCelsius), null) { onMetricClick(HealthViewModel.MetricType.SKIN_TEMPERATURE) }
                }
            }
        }
    }
}

@Composable
private fun VitalMetricRow(label: String, value: String, statusDot: Color?, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
            if (statusDot != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(modifier = Modifier.size(8.dp).background(statusDot, RoundedCornerShape(4.dp)))
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = value, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
        }
    }
}

private fun generateVariationSparkline(baseValue: Float, points: Int, variance: Float): List<Float> {
    val random = java.util.Random(baseValue.toLong())
    return (0 until points).map { i ->
        val variation = 1f + (random.nextFloat() * 2 - 1) * variance
        baseValue * variation
    }
}
