package com.openhealth.openhealth.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.openhealth.openhealth.model.*
import com.openhealth.openhealth.ui.theme.BackgroundBlack
import com.openhealth.openhealth.ui.theme.CardCalories
import com.openhealth.openhealth.ui.theme.CardDistance
import com.openhealth.openhealth.ui.theme.CardFloors
import com.openhealth.openhealth.ui.theme.CardSteps
import com.openhealth.openhealth.ui.theme.SurfaceDark
import com.openhealth.openhealth.ui.theme.SurfaceVariant
import com.openhealth.openhealth.ui.theme.TextPrimary
import com.openhealth.openhealth.ui.theme.TextSecondary
import com.openhealth.openhealth.ui.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: SettingsData,
    onSettingsChanged: (SettingsData) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundBlack
                )
            )
        },
        containerColor = BackgroundBlack
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundBlack)
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SurfaceDark
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Dashboard Metrics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Toggle which metrics to show on your dashboard. Metrics with no data will be hidden automatically.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Group metrics by category (exclude metrics with no Health Connect data source)
            val unsupportedMetrics = setOf(MetricType.SPEED, MetricType.POWER, MetricType.HYDRATION, MetricType.MINDFULNESS)
            val groupedMetrics = MetricType.values()
                .filter { it !in unsupportedMetrics }
                .groupBy { it.category() }

            groupedMetrics.forEach { (category, metrics) ->
                item {
                    CategoryHeader(category)
                }

                items(metrics) { metric ->
                    MetricToggleItem(
                        metric = metric,
                        isEnabled = when (metric) {
                            MetricType.STEPS -> settings.showSteps
                            MetricType.DISTANCE -> settings.showDistance
                            MetricType.FLOORS -> settings.showFloors
                            MetricType.CALORIES -> settings.showCalories
                            MetricType.ACTIVE_CALORIES -> settings.showActiveCalories
                            MetricType.HEART_RATE -> settings.showHeartRate
                            MetricType.RESTING_HEART_RATE -> settings.showRestingHeartRate
                            MetricType.WEIGHT -> settings.showWeight
                            MetricType.BODY_FAT -> settings.showBodyFat
                            MetricType.BASAL_METABOLIC_RATE -> settings.showBMR
                            MetricType.BODY_WATER_MASS -> settings.showBodyWater
                            MetricType.BONE_MASS -> settings.showBoneMass
                            MetricType.LEAN_BODY_MASS -> settings.showLeanBodyMass
                            MetricType.SLEEP -> settings.showSleep
                            MetricType.VO2_MAX -> settings.showVO2Max
                            MetricType.BLOOD_GLUCOSE -> settings.showBloodGlucose
                            MetricType.BLOOD_PRESSURE -> settings.showBloodPressure
                            MetricType.BODY_TEMPERATURE -> settings.showBodyTemperature
                            MetricType.HEART_RATE_VARIABILITY -> settings.showHRV
                            MetricType.OXYGEN_SATURATION -> settings.showOxygenSaturation
                            MetricType.RESPIRATORY_RATE -> settings.showRespiratoryRate
                            MetricType.SKIN_TEMPERATURE -> settings.showSkinTemperature
                            MetricType.SPEED -> settings.showSpeed
                            MetricType.POWER -> settings.showPower
                            MetricType.NUTRITION -> settings.showNutrition
                            MetricType.HYDRATION -> settings.showHydration
                            MetricType.MINDFULNESS -> settings.showMindfulness
                        },
                        onToggle = { enabled ->
                            val newSettings = when (metric) {
                                MetricType.STEPS -> settings.copy(showSteps = enabled)
                                MetricType.DISTANCE -> settings.copy(showDistance = enabled)
                                MetricType.FLOORS -> settings.copy(showFloors = enabled)
                                MetricType.CALORIES -> settings.copy(showCalories = enabled)
                                MetricType.ACTIVE_CALORIES -> settings.copy(showActiveCalories = enabled)
                                MetricType.HEART_RATE -> settings.copy(showHeartRate = enabled)
                                MetricType.RESTING_HEART_RATE -> settings.copy(showRestingHeartRate = enabled)
                                MetricType.WEIGHT -> settings.copy(showWeight = enabled)
                                MetricType.BODY_FAT -> settings.copy(showBodyFat = enabled)
                                MetricType.BASAL_METABOLIC_RATE -> settings.copy(showBMR = enabled)
                                MetricType.BODY_WATER_MASS -> settings.copy(showBodyWater = enabled)
                                MetricType.BONE_MASS -> settings.copy(showBoneMass = enabled)
                                MetricType.LEAN_BODY_MASS -> settings.copy(showLeanBodyMass = enabled)
                                MetricType.SLEEP -> settings.copy(showSleep = enabled)
                                MetricType.VO2_MAX -> settings.copy(showVO2Max = enabled)
                                MetricType.BLOOD_GLUCOSE -> settings.copy(showBloodGlucose = enabled)
                                MetricType.BLOOD_PRESSURE -> settings.copy(showBloodPressure = enabled)
                                MetricType.BODY_TEMPERATURE -> settings.copy(showBodyTemperature = enabled)
                                MetricType.HEART_RATE_VARIABILITY -> settings.copy(showHRV = enabled)
                                MetricType.OXYGEN_SATURATION -> settings.copy(showOxygenSaturation = enabled)
                                MetricType.RESPIRATORY_RATE -> settings.copy(showRespiratoryRate = enabled)
                                MetricType.SKIN_TEMPERATURE -> settings.copy(showSkinTemperature = enabled)
                                MetricType.SPEED -> settings.copy(showSpeed = enabled)
                                MetricType.POWER -> settings.copy(showPower = enabled)
                                MetricType.NUTRITION -> settings.copy(showNutrition = enabled)
                                MetricType.HYDRATION -> settings.copy(showHydration = enabled)
                                MetricType.MINDFULNESS -> settings.copy(showMindfulness = enabled)
                            }
                            onSettingsChanged(newSettings)
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Features Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                CategoryHeader("Features")
            }
            item {
                MetricToggleItem(
                    metric = null,
                    label = "Steps Streak",
                    isEnabled = settings.showStepsStreak,
                    onToggle = { onSettingsChanged(settings.copy(showStepsStreak = it)) }
                )
            }

            // Daily Goals Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SurfaceDark
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Daily Goals",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Set your daily targets for activity metrics. These are used to calculate progress bars on the dashboard.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Steps Goal
            item {
                GoalInputItem(
                    title = "Steps Goal",
                    value = settings.stepsGoal.toString(),
                    unit = "steps",
                    color = CardSteps,
                    onValueChange = { newValue ->
                        val goal = newValue.toIntOrNull() ?: 10000
                        onSettingsChanged(settings.copy(stepsGoal = goal))
                    }
                )
            }

            // Floors Goal
            item {
                GoalInputItem(
                    title = "Floors Goal",
                    value = settings.floorsGoal.toString(),
                    unit = "floors",
                    color = CardFloors,
                    onValueChange = { newValue ->
                        val goal = newValue.toIntOrNull() ?: 10
                        onSettingsChanged(settings.copy(floorsGoal = goal))
                    }
                )
            }

            // Calories Goal
            item {
                GoalInputItem(
                    title = "Active Calories Goal",
                    value = settings.caloriesGoal.toString(),
                    unit = "kcal",
                    color = CardCalories,
                    onValueChange = { newValue ->
                        val goal = newValue.toIntOrNull() ?: 500
                        onSettingsChanged(settings.copy(caloriesGoal = goal))
                    }
                )
            }

            // Distance Goal
            item {
                GoalInputItem(
                    title = "Distance Goal",
                    value = settings.distanceGoalKm.toString(),
                    unit = "km",
                    color = CardDistance,
                    onValueChange = { newValue ->
                        val goal = newValue.toFloatOrNull() ?: 5.0f
                        onSettingsChanged(settings.copy(distanceGoalKm = goal))
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { onSettingsChanged(SettingsData.DEFAULT) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurfaceDark,
                        contentColor = TextPrimary
                    )
                ) {
                    Text(
                        "Reset to Defaults",
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun CategoryHeader(category: String) {
    Text(
        text = category,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = CardSteps,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun MetricToggleItem(
    metric: MetricType?,
    label: String = metric?.displayName() ?: "",
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceDark
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }

            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = CardSteps,
                    checkedTrackColor = CardSteps.copy(alpha = 0.5f),
                    uncheckedThumbColor = SurfaceVariant,
                    uncheckedTrackColor = SurfaceVariant.copy(alpha = 0.5f)
                )
            )
        }
    }
}

@Composable
private fun GoalInputItem(
    title: String,
    value: String,
    unit: String,
    color: Color,
    onValueChange: (String) -> Unit
) {
    var textValue by remember(value) { mutableStateOf(value) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceDark
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(color)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { newValue ->
                        textValue = newValue
                        onValueChange(newValue)
                    },
                    modifier = Modifier.width(100.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (unit == "km") KeyboardType.Decimal else KeyboardType.Number
                    ),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = color,
                        unfocusedBorderColor = SurfaceVariant,
                        focusedContainerColor = BackgroundBlack,
                        unfocusedContainerColor = BackgroundBlack,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}
