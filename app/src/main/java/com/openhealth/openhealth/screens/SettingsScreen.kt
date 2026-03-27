package com.openhealth.openhealth.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.sp
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
    onBackClick: () -> Unit,
    onExportClick: (() -> Unit)? = null
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

            // Theme Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                CategoryHeader("Appearance")
            }
            item {
                MetricToggleItem(
                    metric = null,
                    label = "Light Theme",
                    isEnabled = settings.useLightTheme,
                    onToggle = { onSettingsChanged(settings.copy(useLightTheme = it)) }
                )
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
            item {
                MetricToggleItem(
                    metric = null,
                    label = "Daily Summary Notification",
                    isEnabled = settings.dailySummaryNotification,
                    onToggle = { onSettingsChanged(settings.copy(dailySummaryNotification = it)) }
                )
            }

            // AI Health Insights Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                CategoryHeader("AI Health Insights")
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Bring Your Own Key",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Add your API key to get AI-powered health analysis. Your data goes directly to your own account — we never see it.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Provider selector
                        Text(text = "AI Provider", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                com.openhealth.openhealth.model.AiProvider.NONE to "None",
                                com.openhealth.openhealth.model.AiProvider.CLAUDE to "Claude",
                                com.openhealth.openhealth.model.AiProvider.GEMINI to "Gemini",
                                com.openhealth.openhealth.model.AiProvider.CHATGPT to "ChatGPT",
                                com.openhealth.openhealth.model.AiProvider.CUSTOM to "Custom"
                            ).forEach { (provider, label) ->
                                val isSelected = settings.aiProvider == provider
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (isSelected) Color(0xFF00B4D8).copy(alpha = 0.2f) else Color(0xFF2A2A2A),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { onSettingsChanged(settings.copy(aiProvider = provider)) }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) Color(0xFF00B4D8) else TextSecondary,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        // API Key input (per-provider keys)
                        if (settings.aiProvider != com.openhealth.openhealth.model.AiProvider.NONE) {
                            val currentKey = when (settings.aiProvider) {
                                com.openhealth.openhealth.model.AiProvider.CLAUDE -> settings.aiClaudeKey
                                com.openhealth.openhealth.model.AiProvider.GEMINI -> settings.aiGeminiKey
                                com.openhealth.openhealth.model.AiProvider.CHATGPT -> settings.aiChatgptKey
                                com.openhealth.openhealth.model.AiProvider.CUSTOM -> settings.aiCustomKey
                                else -> ""
                            }
                            val onKeyChange: (String) -> Unit = { newKey ->
                                when (settings.aiProvider) {
                                    com.openhealth.openhealth.model.AiProvider.CLAUDE -> onSettingsChanged(settings.copy(aiClaudeKey = newKey))
                                    com.openhealth.openhealth.model.AiProvider.GEMINI -> onSettingsChanged(settings.copy(aiGeminiKey = newKey))
                                    com.openhealth.openhealth.model.AiProvider.CHATGPT -> onSettingsChanged(settings.copy(aiChatgptKey = newKey))
                                    com.openhealth.openhealth.model.AiProvider.CUSTOM -> onSettingsChanged(settings.copy(aiCustomKey = newKey))
                                    else -> {}
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = "API Key", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            androidx.compose.material3.OutlinedTextField(
                                value = currentKey,
                                onValueChange = onKeyChange,
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Paste your API key here", color = TextTertiary) },
                                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                singleLine = true,
                                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = Color(0xFF00B4D8),
                                    unfocusedBorderColor = Color(0xFF444444),
                                    cursorColor = Color(0xFF00B4D8)
                                )
                            )

                            // Custom provider: URL + Model fields
                            if (settings.aiProvider == com.openhealth.openhealth.model.AiProvider.CUSTOM) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(text = "Base URL", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.height(4.dp))
                                androidx.compose.material3.OutlinedTextField(
                                    value = settings.aiCustomUrl,
                                    onValueChange = { onSettingsChanged(settings.copy(aiCustomUrl = it)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("e.g. http://192.168.1.100:11434/v1", color = TextTertiary) },
                                    singleLine = true,
                                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        focusedBorderColor = Color(0xFF00B4D8),
                                        unfocusedBorderColor = Color(0xFF444444),
                                        cursorColor = Color(0xFF00B4D8)
                                    )
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(text = "Model Name", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.height(4.dp))
                                androidx.compose.material3.OutlinedTextField(
                                    value = settings.aiCustomModel,
                                    onValueChange = { onSettingsChanged(settings.copy(aiCustomModel = it)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("e.g. llama3, mistral, gemma2", color = TextTertiary) },
                                    singleLine = true,
                                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        focusedBorderColor = Color(0xFF00B4D8),
                                        unfocusedBorderColor = Color(0xFF444444),
                                        cursorColor = Color(0xFF00B4D8)
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Works with: Ollama, OpenRouter, Fireworks AI, Groq, LM Studio, or any OpenAI-compatible API",
                                    color = TextTertiary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            // Weather Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                CategoryHeader("Weather")
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Weather Health Advisory", color = TextPrimary, fontWeight = FontWeight.Medium)
                            Switch(
                                checked = settings.weatherEnabled,
                                onCheckedChange = { onSettingsChanged(settings.copy(weatherEnabled = it)) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = CardSteps,
                                    checkedTrackColor = CardSteps.copy(alpha = 0.5f)
                                )
                            )
                        }
                        if (settings.weatherEnabled) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "No GPS needed — enter your coordinates manually", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "How: Google Maps → search your city → long press → copy coordinates", color = TextTertiary, style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = "Coordinates", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = if (settings.weatherLat != 0.0) settings.weatherLat.toString() else "",
                                    onValueChange = { onSettingsChanged(settings.copy(weatherLat = it.toDoubleOrNull() ?: 0.0)) },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text("Lat", color = TextTertiary) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = Color(0xFF00B4D8), unfocusedBorderColor = Color(0xFF444444))
                                )
                                OutlinedTextField(
                                    value = if (settings.weatherLon != 0.0) settings.weatherLon.toString() else "",
                                    onValueChange = { onSettingsChanged(settings.copy(weatherLon = it.toDoubleOrNull() ?: 0.0)) },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text("Lon", color = TextTertiary) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = Color(0xFF00B4D8), unfocusedBorderColor = Color(0xFF444444))
                                )
                            }
                        }
                    }
                }
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

                // Export Data
                if (onExportClick != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onExportClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00B4D8).copy(alpha = 0.2f),
                            contentColor = Color(0xFF00B4D8)
                        )
                    ) {
                        Text("Export Health Data (CSV)", modifier = Modifier.padding(vertical = 8.dp))
                    }
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
