package com.openhealth.openhealth.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.openhealth.openhealth.model.*
import com.openhealth.openhealth.ui.theme.OpenHealthTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: SettingsData,
    onSettingsChanged: (SettingsData) -> Unit,
    onBackClick: () -> Unit
) {
    OpenHealthTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Settings",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Dashboard Metrics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Toggle which metrics to show on your dashboard. Metrics with no data will be hidden automatically.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Group metrics by category
                val groupedMetrics = MetricType.values().groupBy { it.category() }

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

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = { onSettingsChanged(SettingsData.DEFAULT) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reset to Defaults")
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
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
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun MetricToggleItem(
    metric: MetricType,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                    text = metric.displayName(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }

            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle
            )
        }
    }
}
