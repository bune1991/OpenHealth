package com.openhealth.openhealth.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openhealth.openhealth.model.*
import com.openhealth.openhealth.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: SettingsData,
    onSettingsChanged: (SettingsData) -> Unit,
    onBackClick: () -> Unit,
    onExportClick: (() -> Unit)? = null
) {
    val c = LocalAppColors.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        color = c.onSurface,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = c.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = c.background)
            )
        },
        containerColor = c.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(c.background)
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ═══════════════════════════════════════════
            // SYSTEM OPTIMIZATION — Feature toggles
            // ═══════════════════════════════════════════
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader("System Optimization")
            }

            // AI Insights toggle
            item {
                FeatureTogglePill(
                    icon = Icons.Default.AutoAwesome,
                    iconColor = c.primary,
                    title = "AI Insights",
                    subtitle = "Predictive health analysis",
                    isEnabled = settings.aiProvider != com.openhealth.openhealth.model.AiProvider.NONE,
                    onToggle = {
                        if (it) onSettingsChanged(settings.copy(aiProvider = com.openhealth.openhealth.model.AiProvider.CLAUDE))
                        else onSettingsChanged(settings.copy(aiProvider = com.openhealth.openhealth.model.AiProvider.NONE))
                    }
                )
            }

            // Weather toggle
            item {
                FeatureTogglePill(
                    icon = Icons.Default.Cloud,
                    iconColor = c.secondary,
                    title = "Weather",
                    subtitle = "Sync environmental data",
                    isEnabled = settings.weatherEnabled,
                    onToggle = { onSettingsChanged(settings.copy(weatherEnabled = it)) }
                )
            }

            // Steps Streak toggle
            item {
                FeatureTogglePill(
                    icon = Icons.Default.Bolt,
                    iconColor = c.tertiary,
                    title = "Steps Streak",
                    subtitle = "Gamified movement tracking",
                    isEnabled = settings.showStepsStreak,
                    onToggle = { onSettingsChanged(settings.copy(showStepsStreak = it)) }
                )
            }

            // Daily Summary Notification
            item {
                FeatureTogglePill(
                    icon = Icons.Default.Notifications,
                    iconColor = c.primary,
                    title = "Daily Summary",
                    subtitle = "Evening notification",
                    isEnabled = settings.dailySummaryNotification,
                    onToggle = { onSettingsChanged(settings.copy(dailySummaryNotification = it)) }
                )
            }

            // Weekly AI Summary
            item {
                FeatureTogglePill(
                    icon = Icons.Default.Assessment,
                    iconColor = c.primary,
                    title = "Weekly AI Summary",
                    subtitle = "AI health report every Sunday",
                    isEnabled = settings.weeklyAiSummary,
                    onToggle = { onSettingsChanged(settings.copy(weeklyAiSummary = it)) }
                )
            }

            // Haptic Feedback
            item {
                FeatureTogglePill(
                    icon = Icons.Default.Vibration,
                    iconColor = c.tertiary,
                    title = "Haptic Feedback",
                    subtitle = "Vibrate on tap interactions",
                    isEnabled = settings.hapticFeedback,
                    onToggle = { onSettingsChanged(settings.copy(hapticFeedback = it)) }
                )
            }

            // ═══════════════════════════════════════════
            // DEVELOPER NODE — API Configuration
            // ═══════════════════════════════════════════
            if (settings.aiProvider != com.openhealth.openhealth.model.AiProvider.NONE) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    SectionHeader("Developer Node")
                }

                // AI Provider selector
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            com.openhealth.openhealth.model.AiProvider.CLAUDE to "Claude",
                            com.openhealth.openhealth.model.AiProvider.GEMINI to "Gemini",
                            com.openhealth.openhealth.model.AiProvider.CHATGPT to "ChatGPT",
                            com.openhealth.openhealth.model.AiProvider.CUSTOM to "Custom"
                        ).forEach { (provider, label) ->
                            val isSelected = settings.aiProvider == provider
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isSelected) c.primary.copy(alpha = 0.2f) else c.surfaceHighest
                                    )
                                    .clickable { onSettingsChanged(settings.copy(aiProvider = provider)) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) c.primary else c.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // API Key input
                item {
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

                    Spacer(modifier = Modifier.height(8.dp))
                    NocturneTextField(
                        label = "API Key",
                        value = currentKey,
                        onValueChange = onKeyChange,
                        placeholder = "Paste your API key here",
                        isPassword = true
                    )

                    // Custom provider: URL + Model
                    if (settings.aiProvider == com.openhealth.openhealth.model.AiProvider.CUSTOM) {
                        Spacer(modifier = Modifier.height(12.dp))
                        NocturneTextField(
                            label = "Base URL",
                            value = settings.aiCustomUrl,
                            onValueChange = { onSettingsChanged(settings.copy(aiCustomUrl = it)) },
                            placeholder = "e.g. http://192.168.1.100:11434/v1"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        NocturneTextField(
                            label = "Model Name",
                            value = settings.aiCustomModel,
                            onValueChange = { onSettingsChanged(settings.copy(aiCustomModel = it)) },
                            placeholder = "e.g. llama3, mistral, gemma2"
                        )
                    }
                }
            }

            // Weather coordinates
            if (settings.weatherEnabled) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Coordinates (Google Maps → long press → copy)",
                        style = MaterialTheme.typography.bodySmall,
                        color = c.outline,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NocturneTextField(
                            label = "",
                            value = if (settings.weatherLat != 0.0) settings.weatherLat.toString() else "",
                            onValueChange = { onSettingsChanged(settings.copy(weatherLat = it.toDoubleOrNull() ?: 0.0)) },
                            placeholder = "Latitude",
                            modifier = Modifier.weight(1f),
                            keyboardType = KeyboardType.Decimal
                        )
                        NocturneTextField(
                            label = "",
                            value = if (settings.weatherLon != 0.0) settings.weatherLon.toString() else "",
                            onValueChange = { onSettingsChanged(settings.copy(weatherLon = it.toDoubleOrNull() ?: 0.0)) },
                            placeholder = "Longitude",
                            modifier = Modifier.weight(1f),
                            keyboardType = KeyboardType.Decimal
                        )
                    }
                }
            }

            // ═══════════════════════════════════════════
            // VISUAL FREQUENCY — Theme picker
            // ═══════════════════════════════════════════
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader("Visual Frequency")
            }

            // Row 1: Dark themes
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ThemeCircle(
                        label = "Nocturne",
                        colors = listOf(Color(0xFF1A0033), Color(0xFF000000), Color(0xFF1A0040)),
                        isSelected = settings.themeName == "nocturne",
                        onClick = { onSettingsChanged(settings.copy(themeName = "nocturne")) }
                    )
                    ThemeCircle(
                        label = "Solar Dark",
                        colors = listOf(Color(0xFFFF8C00), Color(0xFF3D2A00), Color(0xFF1A1000)),
                        isSelected = settings.themeName == "solar_dark",
                        onClick = { onSettingsChanged(settings.copy(themeName = "solar_dark")) }
                    )
                    ThemeCircle(
                        label = "Ocean",
                        colors = listOf(Color(0xFF0077B6), Color(0xFF023E8A)),
                        isSelected = settings.themeName == "ocean",
                        onClick = { onSettingsChanged(settings.copy(themeName = "ocean")) }
                    )
                    ThemeCircle(
                        label = "Forest",
                        colors = listOf(Color(0xFF10B981), Color(0xFF065F46)),
                        isSelected = settings.themeName == "forest",
                        onClick = { onSettingsChanged(settings.copy(themeName = "forest")) }
                    )
                }
            }
            // Row 2: Light themes
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ThemeCircle(
                        label = "Light",
                        colors = listOf(Color(0xFFB89FFF), Color(0xFFF5F5F5)),
                        isSelected = settings.themeName == "light",
                        onClick = { onSettingsChanged(settings.copy(themeName = "light")) }
                    )
                    ThemeCircle(
                        label = "Solar",
                        colors = listOf(Color(0xFFFF8C00), Color(0xFFFFD700)),
                        isSelected = settings.themeName == "solar",
                        onClick = { onSettingsChanged(settings.copy(themeName = "solar")) }
                    )
                    ThemeCircle(
                        label = "Ocean Lt",
                        colors = listOf(Color(0xFF00B4D8), Color(0xFFF0F8FF)),
                        isSelected = settings.themeName == "ocean_light",
                        onClick = { onSettingsChanged(settings.copy(themeName = "ocean_light")) }
                    )
                    ThemeCircle(
                        label = "Forest Lt",
                        colors = listOf(Color(0xFF34D399), Color(0xFFF0FFF0)),
                        isSelected = settings.themeName == "forest_light",
                        onClick = { onSettingsChanged(settings.copy(themeName = "forest_light")) }
                    )
                }
            }

            // ═══════════════════════════════════════════
            // DAILY GOALS
            // ═══════════════════════════════════════════
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader("Daily Goals")
            }

            item {
                GoalInputPill("Steps Goal", settings.stepsGoal.toString(), "steps", c.primary) { onSettingsChanged(settings.copy(stepsGoal = it.toIntOrNull() ?: 10000)) }
            }
            item {
                GoalInputPill("Floors Goal", settings.floorsGoal.toString(), "floors", c.success) { onSettingsChanged(settings.copy(floorsGoal = it.toIntOrNull() ?: 10)) }
            }
            item {
                GoalInputPill("Calories Goal", settings.caloriesGoal.toString(), "kcal", CardCalories) { onSettingsChanged(settings.copy(caloriesGoal = it.toIntOrNull() ?: 500)) }
            }
            item {
                GoalInputPill("Distance Goal", settings.distanceGoalKm.toString(), "km", CardDistance) { onSettingsChanged(settings.copy(distanceGoalKm = it.toFloatOrNull() ?: 5.0f)) }
            }
            item {
                GoalInputPill("Weight Target", settings.weightTargetKg.toString(), "kg", CardWeight) { onSettingsChanged(settings.copy(weightTargetKg = it.toFloatOrNull() ?: 70.0f)) }
            }
            item {
                GoalInputPill("Hydration Goal", (settings.hydrationGoalMl / 1000f).toString(), "L", Color(0xFF4DABFF)) { onSettingsChanged(settings.copy(hydrationGoalMl = ((it.toFloatOrNull() ?: 2.5f) * 1000).toInt())) }
            }

            // ═══════════════════════════════════════════
            // DASHBOARD CARDS
            // ═══════════════════════════════════════════
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader("Dashboard Cards")
            }

            item {
                FeatureTogglePill(
                    icon = Icons.Default.Restaurant,
                    iconColor = c.tertiary,
                    title = "Nutrition",
                    subtitle = "Track food intake on dashboard",
                    isEnabled = settings.showNutrition,
                    onToggle = { onSettingsChanged(settings.copy(showNutrition = it)) }
                )
            }
            item {
                FeatureTogglePill(
                    icon = Icons.Default.WaterDrop,
                    iconColor = Color(0xFF4DABFF),
                    title = "Hydration",
                    subtitle = "Track water intake on dashboard",
                    isEnabled = settings.showHydration,
                    onToggle = { onSettingsChanged(settings.copy(showHydration = it)) }
                )
            }

            // ═══════════════════════════════════════════
            // EXPORT + RESET
            // ═══════════════════════════════════════════
            item {
                Spacer(modifier = Modifier.height(24.dp))

                // Export button — gradient pill
                if (onExportClick != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                Brush.horizontalGradient(listOf(c.primary, c.secondary))
                            )
                            .clickable { onExportClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FileUpload,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Export Data Cluster",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Encrypted data will be synthesized and delivered to your primary node.",
                        style = MaterialTheme.typography.bodySmall,
                        color = c.outline,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Reset
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(c.surface)
                        .clickable { onSettingsChanged(SettingsData.DEFAULT) }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Reset to Defaults", color = c.onSurfaceVariant, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Components
// ═══════════════════════════════════════════════════════════

@Composable
private fun SectionHeader(title: String) {
    val c = LocalAppColors.current
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = c.tertiary,
        letterSpacing = 2.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
    )
}

@Composable
private fun FeatureTogglePill(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val c = LocalAppColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(c.surfaceLow)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(c.surfaceHighest, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = title, color = c.onSurface, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                    Text(text = subtitle, color = c.onSurfaceVariant, fontSize = 11.sp)
                }
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = c.primary,
                    uncheckedThumbColor = c.outline,
                    uncheckedTrackColor = c.surfaceHighest
                )
            )
        }
    }
}

@Composable
private fun MetricTogglePill(
    label: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val c = LocalAppColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(c.surfaceLow)
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, color = c.onSurface, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = c.primary,
                    uncheckedThumbColor = c.outline,
                    uncheckedTrackColor = c.surfaceHighest
                )
            )
        }
    }
}

@Composable
private fun NocturneTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    val c = LocalAppColors.current
    Column(modifier = modifier) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = c.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = c.outline) },
            visualTransformation = if (isPassword) androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = c.surfaceHighest,
                unfocusedContainerColor = c.surfaceHighest,
                focusedBorderColor = c.primary.copy(alpha = 0.5f),
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = c.onSurface,
                unfocusedTextColor = c.onSurface,
                cursorColor = c.primary
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = c.onSurface)
        )
    }
}

@Composable
private fun ThemeCircle(
    label: String,
    colors: List<Color>,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val c = LocalAppColors.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .then(
                    if (isSelected) Modifier
                        .clip(CircleShape)
                        .background(c.primary.copy(alpha = 0.1f))
                        .padding(3.dp)
                    else Modifier
                )
                .clip(CircleShape)
                .background(Brush.linearGradient(colors))
        ) {
            if (isSelected) {
                // Ring border
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color.Transparent)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label.uppercase(),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = if (isSelected) c.primary else c.outline
        )
    }
}

@Composable
private fun GoalInputPill(
    title: String,
    value: String,
    unit: String,
    color: Color,
    onValueChange: (String) -> Unit
) {
    val c = LocalAppColors.current
    var textValue by remember(value) { mutableStateOf(value) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(c.surfaceLow)
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(color, CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = title, color = c.onSurface, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { textValue = it; onValueChange(it) },
                    modifier = Modifier.width(80.dp),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = c.onSurface, fontWeight = FontWeight.Bold),
                    keyboardOptions = KeyboardOptions(keyboardType = if (unit == "km") KeyboardType.Decimal else KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = c.surfaceHighest,
                        unfocusedContainerColor = c.surfaceHighest,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = c.onSurface,
                        unfocusedTextColor = c.onSurface,
                        cursorColor = c.primary
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = unit, color = c.outline, fontSize = 12.sp)
            }
        }
    }
}

// Helper to get metric enabled state
private fun getMetricEnabled(settings: SettingsData, metric: MetricType): Boolean = when (metric) {
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
    MetricType.NUTRITION -> settings.showNutrition
    MetricType.SPEED -> settings.showSpeed
    MetricType.POWER -> settings.showPower
    MetricType.HYDRATION -> settings.showHydration
    MetricType.MINDFULNESS -> settings.showMindfulness
}

// Helper to set metric enabled state
private fun setMetricEnabled(settings: SettingsData, metric: MetricType, enabled: Boolean): SettingsData = when (metric) {
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
    MetricType.NUTRITION -> settings.copy(showNutrition = enabled)
    MetricType.SPEED -> settings.copy(showSpeed = enabled)
    MetricType.POWER -> settings.copy(showPower = enabled)
    MetricType.HYDRATION -> settings.copy(showHydration = enabled)
    MetricType.MINDFULNESS -> settings.copy(showMindfulness = enabled)
}
