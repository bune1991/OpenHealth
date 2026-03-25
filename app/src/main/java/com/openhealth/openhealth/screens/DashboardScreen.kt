package com.openhealth.openhealth.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RunCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stairs
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WaterDrop
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import com.openhealth.openhealth.model.BodyFatData
import com.openhealth.openhealth.model.CaloriesData
import com.openhealth.openhealth.model.DistanceData
import com.openhealth.openhealth.model.ExerciseData
import com.openhealth.openhealth.model.FloorsData
import com.openhealth.openhealth.model.HealthData
import com.openhealth.openhealth.model.HeartRateData
import com.openhealth.openhealth.model.RestingHeartRateData
import com.openhealth.openhealth.model.SettingsData
import com.openhealth.openhealth.model.SleepData
import com.openhealth.openhealth.model.StepsData
import com.openhealth.openhealth.model.Vo2MaxData
import com.openhealth.openhealth.model.WeightData
import com.openhealth.openhealth.model.shouldShowMetric
import com.openhealth.openhealth.ui.theme.BackgroundDark
import com.openhealth.openhealth.ui.theme.CardBodyFat
import com.openhealth.openhealth.ui.theme.CardCalories
import com.openhealth.openhealth.ui.theme.CardDistance
import com.openhealth.openhealth.ui.theme.CardExercise
import com.openhealth.openhealth.ui.theme.CardFloors
import com.openhealth.openhealth.ui.theme.CardHeartRate
import com.openhealth.openhealth.ui.theme.CardSleep
import com.openhealth.openhealth.ui.theme.CardSteps
import com.openhealth.openhealth.ui.theme.CardVo2Max
import com.openhealth.openhealth.ui.theme.CardWeight
import com.openhealth.openhealth.ui.theme.CardBMR
import com.openhealth.openhealth.ui.theme.CardBodyWater
import com.openhealth.openhealth.ui.theme.CardBoneMass
import com.openhealth.openhealth.ui.theme.CardLeanBodyMass
import com.openhealth.openhealth.ui.theme.CardBloodGlucose
import com.openhealth.openhealth.ui.theme.CardBloodPressure
import com.openhealth.openhealth.ui.theme.CardBodyTemperature
import com.openhealth.openhealth.ui.theme.CardHRV
import com.openhealth.openhealth.ui.theme.CardSpO2
import com.openhealth.openhealth.ui.theme.CardRespiratoryRate
import com.openhealth.openhealth.ui.theme.PrimaryBlue
import com.openhealth.openhealth.ui.theme.SurfaceDark
import com.openhealth.openhealth.ui.theme.TextPrimary
import com.openhealth.openhealth.ui.theme.TextSecondary
import com.openhealth.openhealth.viewmodel.HealthViewModel
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
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
    // Create LazyListState with initial position from ViewModel
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = initialScrollIndex,
        initialFirstVisibleItemScrollOffset = initialScrollOffset
    )

    // Track scroll position changes and notify parent (ViewModel)
    LaunchedEffect(listState) {
        snapshotFlow { 
            Pair(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) 
        }
            .collect { (index, offset) ->
                onScrollPositionChanged(index, offset)
            }
    }

    val isToday = selectedDate == LocalDate.now()
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())
    val dateText = if (isToday) "Today" else selectedDate.format(dateFormatter)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "OpenHealth",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = dateText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    Row {
                        IconButton(onClick = onPreviousDay) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowLeft,
                                contentDescription = "Previous Day",
                                tint = PrimaryBlue
                            )
                        }
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
                            tint = if (isToday) TextSecondary.copy(alpha = 0.3f) else PrimaryBlue
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = PrimaryBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onRefresh,
                containerColor = PrimaryBlue,
                contentColor = BackgroundDark
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh"
                )
            }
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .padding(paddingValues)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Activity Section
                val showActivitySection = settings.showSteps || settings.showDistance || settings.showFloors
                if (showActivitySection) {
                    item { SectionHeader(title = "Activity") }

                    // Steps Card
                    if (settings.showSteps && healthData.steps.count > 0) {
                        item {
                            MetricCard(
                                title = "Steps",
                                value = healthData.steps.count.toString(),
                                unit = "steps",
                                icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                                color = CardSteps,
                                progress = healthData.steps.progress,
                                goal = "Goal: ${healthData.steps.goal}",
                                onClick = { onMetricClick(HealthViewModel.MetricType.STEPS) }
                            )
                        }
                    }

                    // Distance Card
                    if (settings.showDistance && healthData.distance.kilometers > 0) {
                        item {
                            MetricCard(
                                title = "Distance",
                                value = String.format("%.2f", healthData.distance.kilometers),
                                unit = "km",
                                icon = Icons.Default.Straighten,
                                color = CardDistance,
                                onClick = { onMetricClick(HealthViewModel.MetricType.DISTANCE) }
                            )
                        }
                    }

                    // Floors Card
                    if (settings.showFloors && healthData.floors.count > 0) {
                        item {
                            MetricCard(
                                title = "Floors",
                                value = healthData.floors.count.toString(),
                                unit = "floors",
                                icon = Icons.Default.Stairs,
                                color = CardFloors,
                                progress = healthData.floors.progress,
                                goal = "Goal: ${healthData.floors.goal}",
                                onClick = { onMetricClick(HealthViewModel.MetricType.FLOORS) }
                            )
                        }
                    }
                }

                // Calories Section
                val showCaloriesSection = settings.showCalories || settings.showActiveCalories
                if (showCaloriesSection) {
                    item { SectionHeader(title = "Calories") }

                    // Total Calories Card
                    if (settings.showCalories && healthData.calories.totalBurned > 0) {
                        item {
                            MetricCard(
                                title = "Total Calories",
                                value = healthData.calories.totalBurned.roundToInt().toString(),
                                unit = "kcal",
                                icon = Icons.Default.LocalFireDepartment,
                                color = CardCalories,
                                onClick = { onMetricClick(HealthViewModel.MetricType.CALORIES) }
                            )
                        }
                    }

                    // Active Calories Card
                    if (settings.showActiveCalories && healthData.calories.activeBurned > 0) {
                        item {
                            MetricCard(
                                title = "Active Calories",
                                value = healthData.calories.activeBurned.roundToInt().toString(),
                                unit = "kcal",
                                icon = Icons.AutoMirrored.Filled.TrendingUp,
                                color = CardCalories,
                                onClick = { onMetricClick(HealthViewModel.MetricType.ACTIVE_CALORIES) }
                            )
                        }
                    }
                }

                // Heart Section
                val showHeartSection = settings.showHeartRate || settings.showRestingHeartRate
                if (showHeartSection) {
                    item { SectionHeader(title = "Heart") }

                    // Heart Rate Card
                    if (settings.showHeartRate && healthData.heartRate.currentBpm != null) {
                        item {
                            MetricCard(
                                title = "Heart Rate",
                                value = healthData.heartRate.currentBpm.toString(),
                                unit = "bpm",
                                icon = Icons.Default.Favorite,
                                color = CardHeartRate,
                                subtitle = healthData.heartRate.restingBpm?.let { "Resting: $it bpm" },
                                onClick = { onMetricClick(HealthViewModel.MetricType.HEART_RATE) }
                            )
                        }
                    }

                    // Resting Heart Rate Card
                    if (settings.showRestingHeartRate && healthData.restingHeartRate.bpm != null) {
                        item {
                            MetricCard(
                                title = "Resting Heart Rate",
                                value = healthData.restingHeartRate.bpm.toString(),
                                unit = "bpm",
                                icon = Icons.Default.Favorite,
                                color = CardHeartRate,
                                onClick = { onMetricClick(HealthViewModel.MetricType.RESTING_HEART_RATE) }
                            )
                        }
                    }
                }

                // Body Section
                val showBodySection = settings.showWeight || settings.showBodyFat || settings.showBMR ||
                        settings.showBodyWater || settings.showBoneMass || settings.showLeanBodyMass
                if (showBodySection) {
                    item { SectionHeader(title = "Body") }

                    // Weight Card
                    if (settings.showWeight && healthData.weight.kilograms != null) {
                        item {
                            MetricCard(
                                title = "Weight",
                                value = String.format("%.1f", healthData.weight.kilograms),
                                unit = "kg",
                                icon = Icons.Default.FitnessCenter,
                                color = CardWeight,
                                onClick = { onMetricClick(HealthViewModel.MetricType.WEIGHT) }
                            )
                        }
                    }

                    // Body Fat Card
                    if (settings.showBodyFat && healthData.bodyFat.percentage != null) {
                        item {
                            MetricCard(
                                title = "Body Fat",
                                value = String.format("%.1f", healthData.bodyFat.percentage),
                                unit = "%",
                                icon = Icons.Default.Terrain,
                                color = CardBodyFat,
                                onClick = { onMetricClick(HealthViewModel.MetricType.BODY_FAT) }
                            )
                        }
                    }

                    // Basal Metabolic Rate Card
                    if (settings.showBMR && healthData.basalMetabolicRate.caloriesPerDay != null) {
                        item {
                            MetricCard(
                                title = "BMR",
                                value = String.format("%.0f", healthData.basalMetabolicRate.caloriesPerDay),
                                unit = "kcal/day",
                                icon = Icons.Default.LocalFireDepartment,
                                color = CardBMR,
                                onClick = { onMetricClick(HealthViewModel.MetricType.BASAL_METABOLIC_RATE) }
                            )
                        }
                    }

                    // Body Water Mass Card
                    if (settings.showBodyWater && healthData.bodyWaterMass.kilograms != null) {
                        item {
                            MetricCard(
                                title = "Body Water",
                                value = String.format("%.1f", healthData.bodyWaterMass.kilograms),
                                unit = "kg",
                                icon = Icons.Default.WaterDrop,
                                color = CardBodyWater,
                                onClick = { onMetricClick(HealthViewModel.MetricType.BODY_WATER_MASS) }
                            )
                        }
                    }

                    // Bone Mass Card
                    if (settings.showBoneMass && healthData.boneMass.kilograms != null) {
                        item {
                            MetricCard(
                                title = "Bone Mass",
                                value = String.format("%.1f", healthData.boneMass.kilograms),
                                unit = "kg",
                                icon = Icons.Default.FitnessCenter,
                                color = CardBoneMass,
                                onClick = { onMetricClick(HealthViewModel.MetricType.BONE_MASS) }
                            )
                        }
                    }

                    // Lean Body Mass Card
                    if (settings.showLeanBodyMass && healthData.leanBodyMass.kilograms != null) {
                        item {
                            MetricCard(
                                title = "Lean Body Mass",
                                value = String.format("%.1f", healthData.leanBodyMass.kilograms),
                                unit = "kg",
                                icon = Icons.AutoMirrored.Filled.TrendingUp,
                                color = CardLeanBodyMass,
                                onClick = { onMetricClick(HealthViewModel.MetricType.LEAN_BODY_MASS) }
                            )
                        }
                    }
                }

                // Sleep Section
                if (settings.showSleep && healthData.sleep.totalDuration != null) {
                    item { SectionHeader(title = "Sleep") }
                    item {
                        MetricCard(
                            title = "Sleep",
                            value = "${healthData.sleep.hours}h ${healthData.sleep.minutes}m",
                            unit = "",
                            icon = Icons.Default.NightsStay,
                            color = CardSleep,
                            onClick = { onMetricClick(HealthViewModel.MetricType.SLEEP) }
                        )
                    }
                }

                // Exercise Section
                val showExerciseSection = settings.showExercise || settings.showVO2Max
                if (showExerciseSection) {
                    item { SectionHeader(title = "Exercise") }

                    // Exercise Card
                    if (settings.showExercise && healthData.exercise.sessionCount > 0) {
                        item {
                            MetricCard(
                                title = "Exercise",
                                value = healthData.exercise.sessionCount.toString(),
                                unit = "sessions",
                                icon = Icons.Default.RunCircle,
                                color = CardExercise,
                                subtitle = healthData.exercise.totalDuration?.let {
                                    val hours = it.toHours()
                                    val minutes = (it.toMinutes() % 60).toInt()
                                    "${hours}h ${minutes}m total"
                                },
                                onClick = { onMetricClick(HealthViewModel.MetricType.HEART_RATE) }
                            )
                        }
                    }

                    // VO2 Max Card
                    if (settings.showVO2Max && healthData.vo2Max.value != null) {
                        item {
                            MetricCard(
                                title = "VO2 Max",
                                value = String.format("%.1f", healthData.vo2Max.value),
                                unit = "ml/kg/min",
                                icon = Icons.Default.Speed,
                                color = CardVo2Max,
                                onClick = { onMetricClick(HealthViewModel.MetricType.VO2_MAX) }
                            )
                        }
                    }
                }

                // Vitals Section
                val showVitalsSection = settings.showBloodGlucose || settings.showBloodPressure ||
                        settings.showBodyTemperature || settings.showHRV ||
                        settings.showOxygenSaturation || settings.showRespiratoryRate
                if (showVitalsSection) {
                    item { SectionHeader(title = "Vitals") }

                    // Blood Glucose Card
                    if (settings.showBloodGlucose && healthData.bloodGlucose.levelMgPerDl != null) {
                        item {
                            MetricCard(
                                title = "Blood Glucose",
                                value = String.format("%.0f", healthData.bloodGlucose.levelMgPerDl),
                                unit = "mg/dL",
                                icon = Icons.Default.WaterDrop,
                                color = CardBloodGlucose,
                                onClick = { onMetricClick(HealthViewModel.MetricType.BLOOD_GLUCOSE) }
                            )
                        }
                    }

                    // Blood Pressure Card
                    if (settings.showBloodPressure && healthData.bloodPressure.systolicMmHg != null &&
                        healthData.bloodPressure.diastolicMmHg != null) {
                        item {
                            MetricCard(
                                title = "Blood Pressure",
                                value = "${healthData.bloodPressure.systolicMmHg.toInt()}/${healthData.bloodPressure.diastolicMmHg.toInt()}",
                                unit = "mmHg",
                                icon = Icons.Default.Favorite,
                                color = CardBloodPressure,
                                onClick = { onMetricClick(HealthViewModel.MetricType.BLOOD_PRESSURE) }
                            )
                        }
                    }

                    // Body Temperature Card
                    if (settings.showBodyTemperature && healthData.bodyTemperature.temperatureCelsius != null) {
                        item {
                            MetricCard(
                                title = "Body Temperature",
                                value = String.format("%.1f", healthData.bodyTemperature.temperatureCelsius),
                                unit = "°C",
                                icon = Icons.Default.LocalFireDepartment,
                                color = CardBodyTemperature,
                                onClick = { onMetricClick(HealthViewModel.MetricType.BODY_TEMPERATURE) }
                            )
                        }
                    }

                    // Heart Rate Variability Card
                    if (settings.showHRV && healthData.heartRateVariability.rmssdMs != null) {
                        item {
                            MetricCard(
                                title = "HRV",
                                value = String.format("%.0f", healthData.heartRateVariability.rmssdMs),
                                unit = "ms",
                                icon = Icons.AutoMirrored.Filled.TrendingUp,
                                color = CardHRV,
                                onClick = { onMetricClick(HealthViewModel.MetricType.HEART_RATE_VARIABILITY) }
                            )
                        }
                    }

                    // Oxygen Saturation Card
                    if (settings.showOxygenSaturation && healthData.oxygenSaturation.percentage != null) {
                        item {
                            MetricCard(
                                title = "SpO2",
                                value = String.format("%.0f", healthData.oxygenSaturation.percentage),
                                unit = "%",
                                icon = Icons.Default.WaterDrop,
                                color = CardSpO2,
                                onClick = { onMetricClick(HealthViewModel.MetricType.OXYGEN_SATURATION) }
                            )
                        }
                    }

                    // Respiratory Rate Card
                    if (settings.showRespiratoryRate && healthData.respiratoryRate.ratePerMinute != null) {
                        item {
                            MetricCard(
                                title = "Respiratory Rate",
                                value = String.format("%.0f", healthData.respiratoryRate.ratePerMinute),
                                unit = "breaths/min",
                                icon = Icons.Default.Timer,
                                color = CardRespiratoryRate,
                                onClick = { onMetricClick(HealthViewModel.MetricType.RESPIRATORY_RATE) }
                            )
                        }
                    }
                }

                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = TextSecondary,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    color: Color,
    progress: Float? = null,
    goal: String? = null,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceDark
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
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
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(color.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (subtitle != null) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = value,
                            style = MaterialTheme.typography.headlineSmall,
                            color = color,
                            fontWeight = FontWeight.Bold
                        )
                        if (unit.isNotEmpty()) {
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

            if (progress != null) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = color,
                    trackColor = color.copy(alpha = 0.2f)
                )
                if (goal != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = goal,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
