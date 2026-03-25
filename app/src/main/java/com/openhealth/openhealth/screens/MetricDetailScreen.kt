package com.openhealth.openhealth.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.time.LocalDate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.openhealth.openhealth.model.DailyDataPoint
import com.openhealth.openhealth.model.MetricHistory
import com.openhealth.openhealth.model.SleepStagesData
import com.openhealth.openhealth.ui.theme.BackgroundDark
import com.openhealth.openhealth.ui.theme.CardSteps
import com.openhealth.openhealth.ui.theme.CardHeartRate
import com.openhealth.openhealth.ui.theme.CardSleep
import com.openhealth.openhealth.ui.theme.CardCalories
import com.openhealth.openhealth.ui.theme.CardDistance
import com.openhealth.openhealth.ui.theme.CardFloors
import com.openhealth.openhealth.ui.theme.CardVo2Max
import com.openhealth.openhealth.ui.theme.CardBodyFat
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
import com.openhealth.openhealth.ui.theme.SurfaceDark
import com.openhealth.openhealth.ui.theme.TextPrimary
import com.openhealth.openhealth.ui.theme.TextSecondary
import com.openhealth.openhealth.viewmodel.HealthViewModel
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetricDetailScreen(
    metricType: HealthViewModel.MetricType,
    metricHistory: MetricHistory?,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onDateChange: ((LocalDate) -> Unit)? = null
) {
    val metricInfo = getMetricInfo(metricType)

    // Track the currently selected date (default to today)
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    // Get the value for the selected date from history
    val selectedDateValue = remember(metricHistory, selectedDate) {
        metricHistory?.allHistoricalData?.find { it.date == selectedDate }?.value
            ?: metricHistory?.todayValue
            ?: 0.0
    }

    // Determine if we should show skeleton (no cached data) or content with refresh indicator
    val showSkeleton = isLoading && metricHistory == null
    val showContent = metricHistory != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = metricInfo.title,
                            color = TextPrimary,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = selectedDate.format(
                                DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
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
                actions = {
                    // Previous day button
                    IconButton(
                        onClick = {
                            selectedDate = selectedDate.minusDays(1)
                            onDateChange?.invoke(selectedDate)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowLeft,
                            contentDescription = "Previous Day",
                            tint = TextPrimary
                        )
                    }
                    // Next day button (disabled if today)
                    IconButton(
                        onClick = {
                            if (selectedDate.isBefore(LocalDate.now())) {
                                selectedDate = selectedDate.plusDays(1)
                                onDateChange?.invoke(selectedDate)
                            }
                        },
                        enabled = selectedDate.isBefore(LocalDate.now())
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                            contentDescription = "Next Day",
                            tint = if (selectedDate.isBefore(LocalDate.now())) TextPrimary else TextSecondary.copy(alpha = 0.3f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark
                )
            )
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .padding(paddingValues)
        ) {
            when {
                showSkeleton -> {
                    // Show skeleton loading UI
                    SkeletonLoadingContent(metricInfo.color)
                }
                showContent -> {
                    // Show content with optional refresh indicator
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Selected Date Value Card
                        item {
                            val isToday = selectedDate == LocalDate.now()
                            val dateLabel = if (isToday) "Today" else selectedDate.format(
                                DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
                            )
                            TodayValueCard(
                                value = formatValue(selectedDateValue, metricInfo.decimalPlaces),
                                unit = metricHistory!!.unit,
                                color = metricInfo.color,
                                dateLabel = dateLabel,
                                isLoading = isLoading // Show subtle shimmer if refreshing
                            )
                        }

                        // Statistics Cards
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StatCard(
                                    title = "All-Time Avg",
                                    value = formatValue(metricHistory!!.monthlyAverage, metricInfo.decimalPlaces),
                                    unit = metricHistory.unit,
                                    icon = Icons.Default.TrendingUp,
                                    color = metricInfo.color,
                                    modifier = Modifier.weight(1f),
                                    isLoading = isLoading
                                )

                                StatCard(
                                    title = "Best Day",
                                    value = metricHistory.bestDay?.let {
                                        formatValue(it.value, metricInfo.decimalPlaces)
                                    } ?: "--",
                                    unit = metricHistory.unit,
                                    icon = Icons.Default.EmojiEvents,
                                    color = metricInfo.color,
                                    modifier = Modifier.weight(1f),
                                    isLoading = isLoading
                                )
                            }
                        }

                        // Best Day Info
                        metricHistory!!.bestDay?.let { bestDay ->
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = SurfaceDark
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            tint = metricInfo.color,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "Best Day",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = TextSecondary
                                            )
                                            Text(
                                                text = bestDay.date.format(
                                                    DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())
                                                ),
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = TextPrimary,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Sleep Stages Chart (only for Sleep metric)
                        if (metricType == HealthViewModel.MetricType.SLEEP && metricHistory.sleepStages != null) {
                            item {
                                SleepStagesChart(
                                    sleepStages = metricHistory.sleepStages
                                )
                            }
                        }

                        // All History Header
                        val totalRecords = metricHistory.allHistoricalData.size
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (totalRecords > 0) "All History ($totalRecords days)" else "All History",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                                // Show loading indicator if refreshing in background
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = metricInfo.color,
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        }

                        // History List - Show ALL available data
                        items(metricHistory.allHistoricalData.reversed()) { dayData ->
                            HistoryItem(
                                dayData = dayData,
                                color = metricInfo.color,
                                decimalPlaces = metricInfo.decimalPlaces,
                                isSleep = metricType == HealthViewModel.MetricType.SLEEP
                            )
                        }

                        // Bottom spacing
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
                else -> {
                    // No data available
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No data available",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SkeletonLoadingContent(color: Color) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Skeleton for Today Value Card
        item {
            SkeletonCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                color = color
            )
        }

        // Skeleton for Statistics Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SkeletonCard(
                    modifier = Modifier
                        .weight(1f)
                        .height(120.dp),
                    color = color
                )
                SkeletonCard(
                    modifier = Modifier
                        .weight(1f)
                        .height(120.dp),
                    color = color
                )
            }
        }

        // Skeleton for Best Day Card
        item {
            SkeletonCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                color = color
            )
        }

        // Skeleton for History Header
        item {
            SkeletonText(
                modifier = Modifier
                    .width(150.dp)
                    .height(24.dp),
                color = color
            )
        }

        // Skeleton for History Items
        items(5) {
            SkeletonCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
                color = color
            )
        }
    }
}

@Composable
private fun SkeletonCard(
    modifier: Modifier = Modifier,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceDark
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Shimmer effect using a pulsing box
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = color.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    )
            )
        }
    }
}

@Composable
private fun SkeletonText(
    modifier: Modifier = Modifier,
    color: Color
) {
    Box(
        modifier = modifier
            .background(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp)
            )
    )
}

@Composable
private fun TodayValueCard(
    value: String,
    unit: String,
    color: Color,
    dateLabel: String = "Today",
    isLoading: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLoading) SurfaceDark else color.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = dateLabel,
                style = MaterialTheme.typography.titleMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                if (isLoading) {
                    // Show skeleton shimmer effect
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(48.dp)
                            .background(
                                color = color.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                    )
                } else {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.displayMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    unit: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceDark
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                if (isLoading) {
                    // Show skeleton shimmer effect
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(28.dp)
                            .background(
                                color = color.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                } else {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
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

@Composable
private fun HistoryItem(
    dayData: DailyDataPoint,
    color: Color,
    decimalPlaces: Int,
    isSleep: Boolean = false
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
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = dayData.date.format(
                        DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = dayData.date.format(
                        DateTimeFormatter.ofPattern("EEEE", Locale.getDefault())
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            // Format value - sleep shows hours and minutes, others show normal format
            val displayValue = if (isSleep) {
                formatHoursAndMinutes(dayData.value)
            } else {
                formatValue(dayData.value, decimalPlaces)
            }

            Text(
                text = displayValue,
                style = MaterialTheme.typography.titleLarge,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Format hours as "8h 47m" format
private fun formatHoursAndMinutes(hours: Double): String {
    val totalMinutes = (hours * 60).roundToInt()
    val h = totalMinutes / 60
    val m = totalMinutes % 60
    return "${h}h ${m}m"
}

@Composable
private fun SleepStagesChart(
    sleepStages: SleepStagesData
) {
    val totalMinutes = sleepStages.deepSleepMinutes + sleepStages.lightSleepMinutes + 
                      sleepStages.remSleepMinutes + sleepStages.awakeMinutes
    
    if (totalMinutes == 0L) return

    val deepPercent = sleepStages.deepSleepMinutes.toFloat() / totalMinutes
    val lightPercent = sleepStages.lightSleepMinutes.toFloat() / totalMinutes
    val remPercent = sleepStages.remSleepMinutes.toFloat() / totalMinutes
    val awakePercent = sleepStages.awakeMinutes.toFloat() / totalMinutes

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
                text = "Last Night's Sleep Stages",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Stacked bar chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .background(
                        color = BackgroundDark,
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Deep sleep (dark blue/purple)
                    if (deepPercent > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(deepPercent)
                                .background(
                                    color = Color(0xFF4A148C),
                                    shape = RoundedCornerShape(
                                        topStart = 8.dp,
                                        bottomStart = 8.dp
                                    )
                                )
                        )
                    }
                    // Light sleep (medium blue)
                    if (lightPercent > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(lightPercent)
                                .background(color = Color(0xFF1976D2))
                        )
                    }
                    // REM sleep (cyan)
                    if (remPercent > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(remPercent)
                                .background(color = Color(0xFF00BCD4))
                        )
                    }
                    // Awake (orange)
                    if (awakePercent > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(awakePercent)
                                .background(
                                    color = Color(0xFFFF9800),
                                    shape = RoundedCornerShape(
                                        topEnd = 8.dp,
                                        bottomEnd = 8.dp
                                    )
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Legend
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SleepStageLegendItem(
                    color = Color(0xFF4A148C),
                    label = "Deep Sleep",
                    value = sleepStages.deepSleepHours,
                    percent = (deepPercent * 100).roundToInt()
                )
                SleepStageLegendItem(
                    color = Color(0xFF1976D2),
                    label = "Light Sleep",
                    value = sleepStages.lightSleepHours,
                    percent = (lightPercent * 100).roundToInt()
                )
                SleepStageLegendItem(
                    color = Color(0xFF00BCD4),
                    label = "REM Sleep",
                    value = sleepStages.remSleepHours,
                    percent = (remPercent * 100).roundToInt()
                )
                SleepStageLegendItem(
                    color = Color(0xFFFF9800),
                    label = "Awake",
                    value = sleepStages.awakeHours,
                    percent = (awakePercent * 100).roundToInt()
                )
            }
        }
    }
}

@Composable
private fun SleepStageLegendItem(
    color: Color,
    label: String,
    value: String,
    percent: Int
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
                    .size(12.dp)
                    .background(color, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$percent%",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

private fun formatValue(value: Double, decimalPlaces: Int): String {
    return if (decimalPlaces == 0) {
        value.roundToInt().toString()
    } else {
        String.format("%.${decimalPlaces}f", value)
    }
}

private data class MetricInfo(
    val title: String,
    val color: Color,
    val decimalPlaces: Int = 0
)

private fun getMetricInfo(metricType: HealthViewModel.MetricType): MetricInfo {
    return when (metricType) {
        HealthViewModel.MetricType.STEPS -> MetricInfo("Steps", CardSteps)
        HealthViewModel.MetricType.HEART_RATE -> MetricInfo("Heart Rate", CardHeartRate)
        HealthViewModel.MetricType.RESTING_HEART_RATE -> MetricInfo("Resting Heart Rate", CardHeartRate)
        HealthViewModel.MetricType.SLEEP -> MetricInfo("Sleep", CardSleep)
        HealthViewModel.MetricType.CALORIES -> MetricInfo("Total Calories", CardCalories)
        HealthViewModel.MetricType.ACTIVE_CALORIES -> MetricInfo("Active Calories", CardCalories)
        HealthViewModel.MetricType.DISTANCE -> MetricInfo("Distance", CardDistance, 2)
        HealthViewModel.MetricType.FLOORS -> MetricInfo("Floors", CardFloors)
        HealthViewModel.MetricType.VO2_MAX -> MetricInfo("VO2 Max", CardVo2Max, 1)
        HealthViewModel.MetricType.BODY_FAT -> MetricInfo("Body Fat", CardBodyFat, 1)
        HealthViewModel.MetricType.WEIGHT -> MetricInfo("Weight", CardWeight, 1)
        HealthViewModel.MetricType.BASAL_METABOLIC_RATE -> MetricInfo("BMR", CardBMR)
        HealthViewModel.MetricType.BODY_WATER_MASS -> MetricInfo("Body Water", CardBodyWater, 1)
        HealthViewModel.MetricType.BONE_MASS -> MetricInfo("Bone Mass", CardBoneMass, 1)
        HealthViewModel.MetricType.LEAN_BODY_MASS -> MetricInfo("Lean Body Mass", CardLeanBodyMass, 1)
        HealthViewModel.MetricType.BLOOD_GLUCOSE -> MetricInfo("Blood Glucose", CardBloodGlucose, 0)
        HealthViewModel.MetricType.BLOOD_PRESSURE -> MetricInfo("Blood Pressure", CardBloodPressure, 0)
        HealthViewModel.MetricType.BODY_TEMPERATURE -> MetricInfo("Body Temperature", CardBodyTemperature, 1)
        HealthViewModel.MetricType.HEART_RATE_VARIABILITY -> MetricInfo("Heart Rate Variability", CardHRV, 0)
        HealthViewModel.MetricType.OXYGEN_SATURATION -> MetricInfo("Oxygen Saturation", CardSpO2, 0)
        HealthViewModel.MetricType.RESPIRATORY_RATE -> MetricInfo("Respiratory Rate", CardRespiratoryRate, 0)
    }
}
