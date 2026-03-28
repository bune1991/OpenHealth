package com.openhealth.openhealth.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openhealth.openhealth.model.DailyDataPoint
import com.openhealth.openhealth.model.MetricHistory
import com.openhealth.openhealth.model.SleepStagesData
import com.openhealth.openhealth.ui.theme.*
import com.openhealth.openhealth.viewmodel.HealthViewModel
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.time.DayOfWeek
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetricDetailScreen(
    metricType: HealthViewModel.MetricType,
    metricHistory: MetricHistory?,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onHomeClick: (() -> Unit)? = null,
    onDateChange: ((LocalDate) -> Unit)? = null,
    stepsGoal: Int = 10000,
    exerciseSessions: List<com.openhealth.openhealth.model.ExerciseSession> = emptyList(),
    healthData: com.openhealth.openhealth.model.HealthData? = null
) {
    val metricInfo = getMetricInfo(metricType)

    var selectedDate by remember { mutableStateOf(LocalDate.now(ZoneId.systemDefault())) }
    var showDatePicker by remember { mutableStateOf(false) }

    val isToday = selectedDate == LocalDate.now(ZoneId.systemDefault())
    val selectedDateValue = remember(metricHistory, selectedDate) {
        val historicalValue = metricHistory?.allHistoricalData?.find { it.date == selectedDate }?.value
        when {
            historicalValue != null -> historicalValue
            isToday -> metricHistory?.todayValue ?: 0.0
            else -> 0.0
        }
    }

    val showSkeleton = isLoading && metricHistory == null
    val showContent = metricHistory != null

    // Date picker dialog
    if (showDatePicker) {
        CustomCalendarDialog(
            initialDate = selectedDate,
            onDismiss = { showDatePicker = false },
            onDateSelected = { picked ->
                selectedDate = picked
                onDateChange?.invoke(picked)
                showDatePicker = false
            },
            data = metricHistory?.allHistoricalData ?: emptyList(),
            goal = stepsGoal
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier.clickable { showDatePicker = true }
                    ) {
                        Text(
                            text = metricInfo.title,
                            color = TextOnSurface,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = selectedDate.format(
                                DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextOnSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ElectricIndigo
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            selectedDate = selectedDate.minusDays(1)
                            onDateChange?.invoke(selectedDate)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowLeft,
                            contentDescription = "Previous Day",
                            tint = TextOnSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = {
                            if (selectedDate.isBefore(LocalDate.now(ZoneId.systemDefault()))) {
                                selectedDate = selectedDate.plusDays(1)
                                onDateChange?.invoke(selectedDate)
                            }
                        },
                        enabled = selectedDate.isBefore(LocalDate.now(ZoneId.systemDefault()))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                            contentDescription = "Next Day",
                            tint = if (selectedDate.isBefore(LocalDate.now(ZoneId.systemDefault()))) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.outline
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceLowest
                )
            )
        },
        containerColor = SurfaceLowest
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SurfaceLowest)
                .padding(paddingValues)
        ) {
            when {
                showSkeleton -> {
                    SkeletonLoadingContent(metricInfo.color)
                }
                showContent -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Sleep time range (extracted outside item for reuse)
                        val (sleepStartTime, sleepEndTime) = if (metricType == HealthViewModel.MetricType.SLEEP) {
                            if (isToday) {
                                Pair(metricHistory?.todaySleepStartTime, metricHistory?.todaySleepEndTime)
                            } else {
                                val dayData = metricHistory?.allHistoricalData?.find { it.date == selectedDate }
                                Pair(dayData?.sleepStartTime, dayData?.sleepEndTime)
                            }
                        } else {
                            Pair(null, null)
                        }

                        // Hero Section
                        item {
                            val dateLabel = if (isToday) "Today" else selectedDate.format(
                                DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
                            )

                            // Blood pressure: show systolic/diastolic format
                            val displayValue = if (metricType == HealthViewModel.MetricType.BLOOD_PRESSURE && healthData != null) {
                                val bp = healthData.bloodPressure
                                if (bp.systolicMmHg != null && bp.diastolicMmHg != null) {
                                    "${String.format("%.0f", bp.systolicMmHg)}/${String.format("%.0f", bp.diastolicMmHg)}"
                                } else formatValue(selectedDateValue, metricInfo.decimalPlaces)
                            } else formatValue(selectedDateValue, metricInfo.decimalPlaces)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Text(
                                        text = "LAST 7 DAYS",
                                        color = TextOnSurfaceVariant,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 2.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Vitals Trend",
                                        color = TextOnSurface,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$displayValue ${metricHistory?.unit ?: ""}",
                                        color = VibrantMagenta,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "CURRENT",
                                        color = TextOnSurfaceVariant,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 2.sp
                                    )
                                }
                            }
                        }

                        // Today's Value Card
                        item {
                            val dateLabel = if (isToday) "Today" else selectedDate.format(
                                DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
                            )

                            // Blood pressure: show systolic/diastolic format
                            val displayValue = if (metricType == HealthViewModel.MetricType.BLOOD_PRESSURE && healthData != null) {
                                val bp = healthData.bloodPressure
                                if (bp.systolicMmHg != null && bp.diastolicMmHg != null) {
                                    "${String.format("%.0f", bp.systolicMmHg)}/${String.format("%.0f", bp.diastolicMmHg)}"
                                } else formatValue(selectedDateValue, metricInfo.decimalPlaces)
                            } else formatValue(selectedDateValue, metricInfo.decimalPlaces)

                            // Skip TodayValueCard for sleep — SleepClockCard is the hero
                            if (metricType != HealthViewModel.MetricType.SLEEP) {
                                TodayValueCard(
                                    value = selectedDateValue,
                                    valueFormatted = displayValue,
                                    unit = metricHistory?.unit ?: "",
                                    color = metricInfo.color,
                                    dateLabel = dateLabel,
                                    isLoading = isLoading,
                                    isSleep = false,
                                    sleepStartTime = null,
                                    sleepEndTime = null
                                )
                            }
                        }

                        // Daily Stats Summary for HR, HRV, SpO2, RR
                        if (healthData != null) {
                            data class StatItem(val label: String, val value: String)
                            val statsItems: List<StatItem>? = when (metricType) {
                                HealthViewModel.MetricType.HEART_RATE -> {
                                    val hr = healthData.heartRate
                                    val rhr = healthData.restingHeartRate.bpm
                                    if (hr.currentBpm != null && hr.minBpm != null && hr.maxBpm != null) {
                                        listOfNotNull(
                                            StatItem("Latest", "${hr.currentBpm} bpm"),
                                            StatItem("Range", "${hr.minBpm}-${hr.maxBpm} bpm"),
                                            rhr?.let { StatItem("Resting", "$it bpm") }
                                        )
                                    } else null
                                }
                                HealthViewModel.MetricType.HEART_RATE_VARIABILITY -> {
                                    val hrv = healthData.heartRateVariability
                                    if (hrv.avgMs != null && hrv.minMs != null && hrv.maxMs != null) {
                                        listOf(
                                            StatItem("Average", "${String.format("%.0f", hrv.avgMs)} ms"),
                                            StatItem("Range", "${String.format("%.0f", hrv.minMs)}-${String.format("%.0f", hrv.maxMs)} ms"),
                                            StatItem("Readings", "${hrv.readingCount}")
                                        )
                                    } else null
                                }
                                HealthViewModel.MetricType.OXYGEN_SATURATION -> {
                                    val spo2 = healthData.oxygenSaturation
                                    if (spo2.avgPercentage != null && spo2.minPercentage != null && spo2.maxPercentage != null) {
                                        listOf(
                                            StatItem("Average", "${String.format("%.0f", spo2.avgPercentage)}%"),
                                            StatItem("Range", "${String.format("%.0f", spo2.minPercentage)}-${String.format("%.0f", spo2.maxPercentage)}%"),
                                            StatItem("Readings", "${spo2.readingCount}")
                                        )
                                    } else null
                                }
                                HealthViewModel.MetricType.RESPIRATORY_RATE -> {
                                    val rr = healthData.respiratoryRate
                                    if (rr.avgRate != null && rr.minRate != null && rr.maxRate != null) {
                                        listOf(
                                            StatItem("Average", "${String.format("%.0f", rr.avgRate)} rpm"),
                                            StatItem("Range", "${String.format("%.0f", rr.minRate)}-${String.format("%.0f", rr.maxRate)} rpm"),
                                            StatItem("Readings", "${rr.readingCount}")
                                        )
                                    } else null
                                }
                                else -> null
                            }

                            if (statsItems != null) {
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(24.dp),
                                        colors = CardDefaults.cardColors(containerColor = SurfaceLow)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            statsItems.forEach { stat ->
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(
                                                        text = stat.label.uppercase(),
                                                        color = TextOnSurfaceVariant,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        letterSpacing = 2.sp
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(text = stat.value, color = TextOnSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Nutrition Macros Breakdown (only for Nutrition metric)
                        if (metricType == HealthViewModel.MetricType.NUTRITION && healthData != null) {
                            val n = healthData.nutrition
                            val protein = n.proteinGrams ?: 0.0
                            val carbs = n.carbsGrams ?: 0.0
                            val fat = n.fatGrams ?: 0.0
                            val totalMacros = protein + carbs + fat

                            if (totalMacros > 0) {
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(24.dp),
                                        colors = CardDefaults.cardColors(containerColor = SurfaceMid)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(
                                                text = "Macros Breakdown",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = TextOnSurface,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))

                                            // Stacked bar
                                            val pPct = (protein / totalMacros).toFloat()
                                            val cPct = (carbs / totalMacros).toFloat()
                                            val fPct = (fat / totalMacros).toFloat()

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(24.dp)
                                                    .clip(RoundedCornerShape(20.dp))
                                            ) {
                                                if (pPct > 0f) Box(modifier = Modifier.weight(pPct).fillMaxHeight().background(Color(0xFF4CAF50)))
                                                if (cPct > 0f) Box(modifier = Modifier.weight(cPct).fillMaxHeight().background(Color(0xFFFF9800)))
                                                if (fPct > 0f) Box(modifier = Modifier.weight(fPct).fillMaxHeight().background(Color(0xFFF44336)))
                                            }

                                            Spacer(modifier = Modifier.height(16.dp))

                                            // Legend rows
                                            MacroRow("Protein", protein, (pPct * 100).roundToInt(), Color(0xFF4CAF50))
                                            Spacer(modifier = Modifier.height(8.dp))
                                            MacroRow("Carbs", carbs, (cPct * 100).roundToInt(), Color(0xFFFF9800))
                                            Spacer(modifier = Modifier.height(8.dp))
                                            MacroRow("Fat", fat, (fPct * 100).roundToInt(), Color(0xFFF44336))
                                        }
                                    }
                                }
                            }
                        }

                        // Insights Card (non-sleep — sleep shows insight after stages)
                        if (metricType != HealthViewModel.MetricType.SLEEP) {
                            item {
                                val insight = getInsightForMetric(metricType, selectedDateValue, stepsGoal, healthData)
                                if (insight != null) {
                                    InsightCard(insight = insight)
                                }
                            }
                        }

                        // Exercise Sessions List (only for Exercise metric)
                        if (metricType == HealthViewModel.MetricType.EXERCISE && exerciseSessions.isNotEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(containerColor = SurfaceMid)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "Today's Sessions",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = TextOnSurface,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        exerciseSessions.forEach { session ->
                                            val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
                                            val startStr = session.startTime.atZone(ZoneId.systemDefault()).format(timeFormatter)
                                            val endStr = session.endTime.atZone(ZoneId.systemDefault()).format(timeFormatter)
                                            val durMin = session.duration.toMinutes()
                                            val icon = when (session.exerciseType) {
                                                "Running" -> "🏃"
                                                "Walking" -> "🚶"
                                                "Cycling" -> "🚴"
                                                "Swimming" -> "🏊"
                                                "Yoga" -> "🧘"
                                                "Hiking" -> "🥾"
                                                else -> "💪"
                                            }
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(text = icon, fontSize = 24.sp)
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = session.exerciseType,
                                                        color = TextOnSurface,
                                                        fontWeight = FontWeight.SemiBold,
                                                        style = MaterialTheme.typography.bodyLarge
                                                    )
                                                    Text(
                                                        text = "$startStr → $endStr",
                                                        color = TextSubtle,
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                }
                                                Text(
                                                    text = "${durMin}m",
                                                    color = metricInfo.color,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 18.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Sleep: show clock FIRST, then skip generic charts
                        if (metricType == HealthViewModel.MetricType.SLEEP && sleepStartTime != null && sleepEndTime != null) {
                            item {
                                SleepClockCard(
                                    sleepStart = sleepStartTime,
                                    sleepEnd = sleepEndTime
                                )
                            }
                        }

                        // Non-sleep metrics: show charts
                        if (metricType != HealthViewModel.MetricType.SLEEP) {
                            // Line Chart - 30 Day Trend
                            if (metricHistory?.last30Days?.isNotEmpty() == true) {
                                item {
                                    LineChartCard(
                                        data = metricHistory.last30Days,
                                        color = metricInfo.color,
                                        title = "30 Day Trend",
                                        isSleep = false
                                    )
                                }
                            }

                            // Bar Chart - Weekly Average
                            if ((metricHistory?.last30Days?.size ?: 0) >= 7) {
                                item {
                                    BarChartCard(
                                        data = metricHistory!!.last30Days.takeLast(7),
                                        color = metricInfo.color,
                                        title = "Last 7 Days",
                                        isSleep = false,
                                        decimalPlaces = metricInfo.decimalPlaces
                                    )
                                }
                            }
                        }

                        // Sleep Stages + Sleep Bank — BEFORE stats for sleep metric (Stitch order)
                        // Sleep Stages Chart (only for Sleep metric — per selected day)
                        if (metricType == HealthViewModel.MetricType.SLEEP) {
                            val dayStages = metricHistory?.allHistoricalData
                                ?.find { it.date == selectedDate }?.sleepStages
                                ?: if (isToday) metricHistory?.sleepStages else null

                            if (dayStages != null) {
                                item {
                                    SleepStagesChart(sleepStages = dayStages)
                                }
                            }
                        }

                        // Sleep Bank (only for Sleep metric)
                        if (metricType == HealthViewModel.MetricType.SLEEP && metricHistory?.allHistoricalData?.isNotEmpty() == true) {
                            item {
                                val last7 = metricHistory.allHistoricalData.takeLast(7)
                                // Sleep bank: sum of (actual - 8h target) for last 7 days
                                val bankHours = last7.sumOf { it.value - 8.0 }
                                val isDebt = bankHours < 0
                                val absHours = kotlin.math.abs(bankHours)
                                val h = absHours.toInt()
                                val m = ((absHours - h) * 60).toInt()

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(containerColor = SurfaceMid)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "Sleep Bank",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = TextOnSurface,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Last 7 days vs 8h target",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSubtle
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(verticalAlignment = Alignment.Bottom) {
                                            Text(
                                                text = "${if (isDebt) "-" else "+"}${h}h ${m}m",
                                                color = if (isDebt) Color(0xFFFF9500) else SuccessGreen,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 32.sp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (isDebt) "Debt" else "Surplus",
                                                color = if (isDebt) Color(0xFFFF9500) else SuccessGreen,
                                                fontSize = 16.sp,
                                                modifier = Modifier.padding(bottom = 4.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = if (isDebt) "You're behind on sleep. Try to get extra rest this week."
                                                   else "Great job! You're meeting or exceeding your sleep target.",
                                            color = TextOnSurfaceVariant,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }

                        // Pulse Insight for sleep (gradient card — Stitch design)
                        if (metricType == HealthViewModel.MetricType.SLEEP) {
                            item {
                                val insight = getInsightForMetric(metricType, selectedDateValue, stepsGoal, healthData)
                                if (insight != null) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(24.dp))
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(
                                                        ElectricIndigo.copy(alpha = 0.2f),
                                                        VibrantMagenta.copy(alpha = 0.2f)
                                                    )
                                                )
                                            )
                                            .padding(20.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.Top) {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .background(Color.White.copy(alpha = 0.1f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Lightbulb,
                                                    contentDescription = null,
                                                    tint = ElectricIndigo,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = "Pulse Insight",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = TextOnSurface,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = insight.meaning,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = TextOnSurfaceVariant,
                                                    lineHeight = 18.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Statistics Cards (non-sleep only)
                        if (metricType != HealthViewModel.MetricType.SLEEP) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    StatCard(
                                        title = "30-Day Avg",
                                        value = formatValue(metricHistory?.monthlyAverage ?: 0.0, metricInfo.decimalPlaces),
                                        unit = metricHistory?.unit ?: "",
                                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                                        color = metricInfo.color,
                                        modifier = Modifier.weight(1f),
                                        isLoading = isLoading
                                    )
                                    StatCard(
                                        title = metricHistory?.bestDayLabel ?: "Best Day",
                                        value = metricHistory?.bestDay?.let { formatValue(it.value, metricInfo.decimalPlaces) } ?: "--",
                                        unit = metricHistory?.unit ?: "",
                                        icon = Icons.Default.EmojiEvents,
                                        color = metricInfo.color,
                                        modifier = Modifier.weight(1f),
                                        isLoading = isLoading
                                    )
                                }
                            }

                            metricHistory?.bestDay?.let { bestDay ->
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(24.dp))
                                            .background(SurfaceMid)
                                            .padding(16.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.CalendarToday, null, tint = metricInfo.color, modifier = Modifier.size(24.dp))
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(metricHistory?.bestDayLabel ?: "Best Day", style = MaterialTheme.typography.bodyMedium, color = TextOnSurfaceVariant)
                                                Text(bestDay.date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())), style = MaterialTheme.typography.bodyLarge, color = TextOnSurface, fontWeight = FontWeight.Medium)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Calendar View with Step Rings (Steps only)
                        if (metricType == HealthViewModel.MetricType.STEPS && metricHistory?.allHistoricalData?.isNotEmpty() == true) {
                            item {
                                StepRingsCalendar(
                                    data = metricHistory.allHistoricalData,
                                    selectedDate = selectedDate,
                                    onDateSelected = { date ->
                                        selectedDate = date
                                        onDateChange?.invoke(date)
                                    },
                                    goal = stepsGoal
                                )
                            }
                        }

                        // All History Header
                        val totalRecords = metricHistory?.allHistoricalData?.size ?: 0
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (totalRecords > 0) "RECENT ACTIVITY ($totalRecords)" else "RECENT ACTIVITY",
                                    color = TextOnSurfaceVariant,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = metricInfo.color,
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        }

                        // History List
                        metricHistory?.allHistoricalData?.let { historicalData ->
                            items(historicalData.reversed()) { dayData ->
                                HistoryItem(
                                    dayData = dayData,
                                    color = metricInfo.color,
                                    decimalPlaces = metricInfo.decimalPlaces,
                                    isSleep = metricType == HealthViewModel.MetricType.SLEEP
                                )
                            }
                        }

                        // Bottom spacing
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No data available",
                            color = TextOnSurfaceVariant,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LineChartCard(
    data: List<DailyDataPoint>,
    color: Color,
    title: String,
    @Suppress("UNUSED_PARAMETER") isSleep: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceLow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextOnSurface,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Line Chart
            LineChart(
                data = data,
                lineColor = color,
                fillColor = color.copy(alpha = 0.2f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                isSleep = isSleep
            )

            // X-axis labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Use the actual data points' dates for labels
                val labels = listOf(
                    data.firstOrNull()?.date?.format(DateTimeFormatter.ofPattern("MMM d")) ?: "",
                    data.getOrNull(data.size / 2)?.date?.format(DateTimeFormatter.ofPattern("MMM d")) ?: "",
                    data.lastOrNull()?.date?.format(DateTimeFormatter.ofPattern("MMM d")) ?: ""
                )
                labels.forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSubtle
                    )
                }
            }
        }
    }
}

@Composable
private fun LineChart(
    data: List<DailyDataPoint>,
    lineColor: Color,
    fillColor: Color,
    modifier: Modifier = Modifier,
    @Suppress("UNUSED_PARAMETER") isSleep: Boolean = false
) {
    if (data.size < 2) return

    val values = data.map { it.value }
    val maxValue = values.maxOrNull() ?: 1.0
    val minValue = values.minOrNull() ?: 0.0
    val range = maxValue - minValue
    val backgroundColor = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .drawBehind {
                val width = size.width
                val height = size.height
                val padding = 8.dp.toPx()

                val chartWidth = width - 2 * padding
                val chartHeight = height - 2 * padding

                val stepX = chartWidth / (data.size - 1)

                // Draw grid lines
                for (i in 0..4) {
                    val y = padding + (chartHeight * i / 4)
                    drawLine(
                        color = SurfaceVariant,
                        start = Offset(padding, y),
                        end = Offset(width - padding, y),
                        strokeWidth = 1f
                    )
                }

                // Create path for the line
                val path = Path().apply {
                    data.forEachIndexed { index, point ->
                        val x = padding + index * stepX
                        val normalizedValue = if (range > 0) (point.value - minValue) / range else 0.5
                        val y = padding + chartHeight * (1 - normalizedValue).toFloat()

                        if (index == 0) {
                            moveTo(x, y)
                        } else {
                            lineTo(x, y)
                        }
                    }
                }

                // Create path for fill
                val fillPath = Path().apply {
                    val firstX = padding
                    val firstY = padding + chartHeight * (1 - if (range > 0) (data.first().value - minValue) / range else 0.5).toFloat()
                    val lastX = padding + (data.size - 1) * stepX

                    moveTo(firstX, height - padding)
                    lineTo(firstX, firstY)

                    data.forEachIndexed { index, point ->
                        val x = padding + index * stepX
                        val normalizedValue = if (range > 0) (point.value - minValue) / range else 0.5
                        val y = padding + chartHeight * (1 - normalizedValue).toFloat()
                        lineTo(x, y)
                    }

                    lineTo(lastX, height - padding)
                    close()
                }

                // Draw fill with gradient
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(VibrantMagenta.copy(alpha = 0.2f), ElectricIndigo.copy(alpha = 0.0f)),
                        startY = padding,
                        endY = height - padding
                    )
                )

                // Draw line with gradient
                drawPath(
                    path = path,
                    brush = Brush.horizontalGradient(
                        colors = listOf(VibrantMagenta, ElectricIndigo)
                    ),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Draw points
                data.forEachIndexed { index, point ->
                    if (index % 5 == 0 || index == data.size - 1) { // Draw every 5th point + last
                        val x = padding + index * stepX
                        val normalizedValue = if (range > 0) (point.value - minValue) / range else 0.5
                        val y = padding + chartHeight * (1 - normalizedValue).toFloat()

                        drawCircle(
                            color = lineColor,
                            radius = 4.dp.toPx(),
                            center = Offset(x, y)
                        )
                        drawCircle(
                            color = backgroundColor,
                            radius = 2.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }
                }
            }
    )
}

@Composable
private fun BarChartCard(
    data: List<DailyDataPoint>,
    color: Color,
    title: String,
    @Suppress("UNUSED_PARAMETER") isSleep: Boolean = false,
    decimalPlaces: Int = 0
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceMid
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextOnSurface,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Bar Chart
            BarChart(
                data = data,
                barColor = color,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                isSleep = isSleep,
                decimalPlaces = decimalPlaces
            )
        }
    }
}

@Composable
private fun BarChart(
    data: List<DailyDataPoint>,
    barColor: Color,
    modifier: Modifier = Modifier,
    @Suppress("UNUSED_PARAMETER") isSleep: Boolean = false,
    decimalPlaces: Int = 0
) {
    if (data.isEmpty()) return

    // FIX: Sort data by date (oldest first → today last) to ensure correct bar order
    val sortedData = data.sortedBy { it.date }

    val values = sortedData.map { it.value }
    val maxValue = values.maxOrNull() ?: 1.0
    val minValue = values.minOrNull() ?: 0.0
    val valueRange = maxValue - minValue

    // Check if all values are the same
    val allValuesSame = valueRange < 0.0001

    // Day name formatter - use short day names (Mon, Tue, Wed, etc.)
    val dayFormatter = DateTimeFormatter.ofPattern("EEE", Locale.getDefault())

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        sortedData.forEachIndexed { index, point ->
            // Get the actual day name from the date
            val label = point.date.format(dayFormatter)

            // Calculate bar height with variation
            val targetHeight = when {
                // If only 1 day has real data (others are 0 or minimal), show empty state
                maxValue > 0 && point.value < maxValue * 0.1 && index > 0 -> 0.1f // 10% height for empty days
                // If all values are the same, add slight variation based on index to show they're different days
                allValuesSame && maxValue > 0 -> {
                    val baseHeight = (point.value / maxValue).toFloat()
                    // Add small variation based on day index (-5% to +5%)
                    val variation = (index - 3) * 0.015f
                    (baseHeight + variation).coerceIn(0.15f, 1f)
                }
                maxValue > 0 -> (point.value / maxValue).toFloat()
                else -> 0.1f
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                // Value label on top of bar - FIX: Use decimalPlaces for proper formatting
                val displayValue = if (isSleep) {
                    "${point.value.toInt()}h"
                } else {
                    if (point.value >= 1000) {
                        "${(point.value / 1000).roundToInt()}k"
                    } else {
                        // FIX: Use decimalPlaces to format the value correctly
                        if (decimalPlaces > 0) {
                            String.format("%.${decimalPlaces}f", point.value)
                        } else {
                            point.value.roundToInt().toString()
                        }
                    }
                }

                Text(
                    text = displayValue,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextOnSurfaceVariant,
                    fontSize = 10.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Animated bar
                var animationPlayed by remember { mutableStateOf(false) }
                val animatedHeight by animateFloatAsState(
                    targetValue = if (animationPlayed) targetHeight else 0f,
                    animationSpec = tween(1000),
                    label = "bar_height"
                )

                LaunchedEffect(Unit) {
                    animationPlayed = true
                }

                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .fillMaxHeight(0.7f * animatedHeight.coerceIn(0.05f, 1f))
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    barColor,
                                    barColor.copy(alpha = 0.6f)
                                )
                            )
                        )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Day label
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSubtle,
                    fontSize = 10.sp
                )
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
        item {
            SkeletonCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                color = color
            )
        }

        item {
            SkeletonCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                color = color
            )
        }

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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceMid
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
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
private fun TodayValueCard(
    value: Double,
    valueFormatted: String,
    unit: String,
    color: Color,
    dateLabel: String = "Today",
    isLoading: Boolean = false,
    isSleep: Boolean = false,
    sleepStartTime: java.time.Instant? = null,
    sleepEndTime: java.time.Instant? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceMid
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
                color = TextOnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            // For sleep, show time range if available
            if (isSleep && sleepStartTime != null && sleepEndTime != null) {
                val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
                val startTimeStr = sleepStartTime.atZone(java.time.ZoneId.systemDefault()).format(timeFormatter)
                val endTimeStr = sleepEndTime.atZone(java.time.ZoneId.systemDefault()).format(timeFormatter)

                Text(
                    text = "$startTimeStr → $endTimeStr",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextOnSurface,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Show duration below time range using the raw double value
                val durationText = formatHoursAndMinutes(value)
                Text(
                    text = durationText,
                    style = MaterialTheme.typography.titleMedium,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    if (isLoading) {
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
                            text = valueFormatted,
                            style = MaterialTheme.typography.displayMedium,
                            color = TextOnSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextOnSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextOnSurfaceVariant,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (isLoading) {
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
                    color = TextOnSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = unit,
                fontSize = 12.sp,
                color = TextOnSurfaceVariant
            )
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
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side: Icon circle + Date stacked
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color.copy(alpha = 0.1f),
                            RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = dayData.date.format(
                            DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextOnSurface,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = dayData.date.format(
                            DateTimeFormatter.ofPattern("EEEE", Locale.getDefault())
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextOnSurfaceVariant
                    )
                }
            }

            // Right side: Value + trending icon
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isSleep) {
                    Column(horizontalAlignment = Alignment.End) {
                        if (dayData.sleepStartTime != null && dayData.sleepEndTime != null) {
                            val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
                            val startTimeStr = dayData.sleepStartTime.atZone(java.time.ZoneId.systemDefault()).format(timeFormatter)
                            val endTimeStr = dayData.sleepEndTime.atZone(java.time.ZoneId.systemDefault()).format(timeFormatter)
                            Text(
                                text = "$startTimeStr → $endTimeStr",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextOnSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                        Text(
                            text = formatHoursAndMinutes(dayData.value),
                            style = MaterialTheme.typography.titleMedium,
                            color = color,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    val displayValue = formatValue(dayData.value, decimalPlaces)
                    Text(
                        text = displayValue,
                        style = MaterialTheme.typography.titleLarge,
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = null,
                    tint = color.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

private fun formatHoursAndMinutes(hours: Double): String {
    val durationMillis = (hours * 60 * 60 * 1000).toLong()  // Convert hours to milliseconds
    val h = (durationMillis / 3600000).toInt()
    val m = ((durationMillis % 3600000) / 60000).toInt()
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

    // Stitch: bg-surface-container-high, stacked pill bar
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceHigh)
            .padding(20.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sleep Stages", style = MaterialTheme.typography.titleSmall, color = TextOnSurface, fontWeight = FontWeight.Bold)
                Text("Restorative", style = MaterialTheme.typography.labelSmall, color = ElectricIndigo, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stacked energy pill bar — rounded-full
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(SurfaceHighest)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    if (awakePercent > 0f) {
                        Box(modifier = Modifier.fillMaxHeight().weight(awakePercent).background(TextSubtle))
                    }
                    if (lightPercent > 0f) {
                        Box(modifier = Modifier.fillMaxHeight().weight(lightPercent).background(SoftLavender))
                    }
                    if (deepPercent > 0f) {
                        Box(modifier = Modifier.fillMaxHeight().weight(deepPercent).background(ElectricIndigo))
                    }
                    if (remPercent > 0f) {
                        Box(modifier = Modifier.fillMaxHeight().weight(remPercent).background(VibrantMagenta))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Legend — 2x2 grid matching Stitch
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SleepStageLegendItem(color = TextSubtle, label = "Awake", value = sleepStages.awakeHours, percent = (awakePercent * 100).roundToInt())
                    SleepStageLegendItem(color = ElectricIndigo, label = "Deep", value = sleepStages.deepSleepHours, percent = (deepPercent * 100).roundToInt())
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SleepStageLegendItem(color = SoftLavender, label = "Light", value = sleepStages.lightSleepHours, percent = (lightPercent * 100).roundToInt())
                    SleepStageLegendItem(color = VibrantMagenta, label = "REM", value = sleepStages.remSleepHours, percent = (remPercent * 100).roundToInt())
                }
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
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, fontSize = 12.sp, color = TextOnSurfaceVariant, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.weight(1f))
        Text(text = value, fontSize = 12.sp, color = TextOnSurface, fontWeight = FontWeight.Bold)
    }
}

private fun formatValue(value: Double, decimalPlaces: Int): String {
    return if (decimalPlaces == 0) {
        value.roundToInt().toString()
    } else {
        String.format("%.${decimalPlaces}f", value)
    }
}

private fun getInsightForMetric(metricType: HealthViewModel.MetricType, value: Double, stepsGoal: Int, healthData: com.openhealth.openhealth.model.HealthData? = null): com.openhealth.openhealth.utils.MetricInsight? {
    return when (metricType) {
        HealthViewModel.MetricType.HEART_RATE -> com.openhealth.openhealth.utils.HealthInsights.getHeartRateInsight(value.toInt())
        HealthViewModel.MetricType.RESTING_HEART_RATE -> com.openhealth.openhealth.utils.HealthInsights.getRestingHeartRateInsight(value.toInt())
        HealthViewModel.MetricType.HEART_RATE_VARIABILITY -> com.openhealth.openhealth.utils.HealthInsights.getHrvInsight(value)
        HealthViewModel.MetricType.SLEEP -> com.openhealth.openhealth.utils.HealthInsights.getSleepInsight(value)
        HealthViewModel.MetricType.OXYGEN_SATURATION -> com.openhealth.openhealth.utils.HealthInsights.getSpO2Insight(value)
        HealthViewModel.MetricType.RESPIRATORY_RATE -> com.openhealth.openhealth.utils.HealthInsights.getRespiratoryRateInsight(value)
        HealthViewModel.MetricType.WEIGHT -> com.openhealth.openhealth.utils.HealthInsights.getWeightInsight(value)
        HealthViewModel.MetricType.BODY_FAT -> com.openhealth.openhealth.utils.HealthInsights.getBodyFatInsight(value)
        HealthViewModel.MetricType.BASAL_METABOLIC_RATE -> com.openhealth.openhealth.utils.HealthInsights.getBmrInsight(value)
        HealthViewModel.MetricType.STEPS -> com.openhealth.openhealth.utils.HealthInsights.getStepsInsight(value.toLong(), stepsGoal.toLong())
        HealthViewModel.MetricType.SKIN_TEMPERATURE -> com.openhealth.openhealth.utils.HealthInsights.getSkinTempInsight(value)
        HealthViewModel.MetricType.CALORIES -> com.openhealth.openhealth.utils.HealthInsights.getCaloriesInsight(value)
        HealthViewModel.MetricType.DISTANCE -> com.openhealth.openhealth.utils.HealthInsights.getDistanceInsight(value)
        HealthViewModel.MetricType.NUTRITION -> {
            val n = healthData?.nutrition
            com.openhealth.openhealth.utils.HealthInsights.getNutritionInsight(
                n?.calories ?: value,
                n?.proteinGrams ?: 0.0,
                n?.carbsGrams ?: 0.0,
                n?.fatGrams ?: 0.0
            )
        }
        else -> null
    }
}

@Composable
private fun InsightCard(insight: com.openhealth.openhealth.utils.MetricInsight) {
    val dotColor = when (insight.statusColor) {
        "green" -> SuccessGreen
        "yellow" -> WarningOrange
        "red" -> ErrorRed
        else -> SuccessGreen
    }

    var showTips by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLow)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        ElectricIndigo.copy(alpha = 0.1f),
                        RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = null,
                    tint = ElectricIndigo,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Text content
            Column(modifier = Modifier.weight(1f)) {
                // Header with status
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Recovery Insight",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextOnSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(dotColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(text = insight.status, color = dotColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // What it means
                Text(
                    text = insight.meaning,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextOnSurfaceVariant,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Normal range
                Text(
                    text = "Normal range: ${insight.normalRange}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSubtle
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Tips toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTips = !showTips },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (showTips) "Hide Tips" else "Show Tips",
                        color = ElectricIndigo,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }

                if (showTips) {
                    Spacer(modifier = Modifier.height(8.dp))
                    insight.tips.forEach { tip ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(text = "•", color = ElectricIndigo, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = tip,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextOnSurfaceVariant,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                // Learn more
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = insight.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSubtle,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun MacroRow(label: String, grams: Double, percent: Int, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(12.dp).background(color, RoundedCornerShape(2.dp)))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, color = TextOnSurface, style = MaterialTheme.typography.bodyMedium)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "${grams.roundToInt()}g", color = TextOnSurface, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "$percent%", color = TextOnSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun SleepClockCard(
    sleepStart: java.time.Instant,
    sleepEnd: java.time.Instant
) {
    val zone = ZoneId.systemDefault()
    val startZoned = sleepStart.atZone(zone)
    val endZoned = sleepEnd.atZone(zone)

    val startHour = startZoned.hour + startZoned.minute / 60f
    val endHour = endZoned.hour + endZoned.minute / 60f

    val timeFormatter = java.time.format.DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
    val startTimeStr = startZoned.format(timeFormatter)
    val endTimeStr = endZoned.format(timeFormatter)

    val durationMs = java.time.Duration.between(sleepStart, sleepEnd).toMinutes()
    val sleepH = (durationMs / 60).toInt()
    val sleepM = (durationMs % 60).toInt()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Sleep Clock Ring
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(240.dp)
        ) {
            Canvas(modifier = Modifier.size(240.dp)) {
                val cx = size.width / 2
                val cy = size.height / 2
                val radius = size.width / 2 - 24.dp.toPx()
                val strokeW = 14.dp.toPx()

                // Track ring
                drawArc(
                    color = SurfaceHighest.copy(alpha = 0.3f),
                    startAngle = 0f, sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeW, cap = StrokeCap.Round),
                    topLeft = Offset(cx - radius, cy - radius),
                    size = Size(radius * 2, radius * 2)
                )

                // Sleep arc with gradient
                val startAngle = (startHour % 12) * 30f - 90f
                var sweep = ((endHour % 12) * 30f - 90f) - startAngle
                if (sweep <= 0) sweep += 360f

                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(ElectricIndigo, VibrantMagenta, ElectricIndigo)
                    ),
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = strokeW, cap = StrokeCap.Round),
                    topLeft = Offset(cx - radius, cy - radius),
                    size = Size(radius * 2, radius * 2)
                )
            }

            // Center content
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "TOTAL SLEEP",
                    fontSize = 10.sp,
                    color = TextOnSurfaceVariant,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${sleepH}h ${sleepM}m",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Black,
                    color = TextOnSurface,
                    letterSpacing = (-1).sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = ElectricIndigo,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (sleepH >= 7) "OPTIMAL RANGE" else "BELOW TARGET",
                        fontSize = 10.sp,
                        color = ElectricIndigo,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Clock markers
            Text(
                text = "12 PM",
                fontSize = 10.sp,
                color = TextOnSurfaceVariant,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp)
            )
            Text(
                text = "12 AM",
                fontSize = 10.sp,
                color = TextOnSurfaceVariant,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Bedtime / Wake Up
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("BEDTIME", fontSize = 10.sp, color = TextOnSurfaceVariant, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(startTimeStr, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextOnSurface)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("WAKE UP", fontSize = 10.sp, color = TextOnSurfaceVariant, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(endTimeStr, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextOnSurface)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Efficiency + Consistency stat cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val efficiency = ((sleepH * 60 + sleepM).toFloat() / 480f * 100).toInt().coerceIn(0, 100)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceLow)
                    .padding(20.dp)
            ) {
                Column {
                    Text("EFFICIENCY", fontSize = 10.sp, color = TextOnSurfaceVariant, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("$efficiency", fontSize = 32.sp, fontWeight = FontWeight.Black, color = VibrantMagenta)
                        Text("%", fontSize = 14.sp, color = TextOnSurfaceVariant, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
                    }
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceLow)
                    .padding(20.dp)
            ) {
                Column {
                    Text("CONSISTENCY", fontSize = 10.sp, color = TextOnSurfaceVariant, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (sleepH >= 7) "High" else if (sleepH >= 5) "Medium" else "Low",
                        fontSize = 28.sp, fontWeight = FontWeight.Black, color = ElectricIndigo
                    )
                }
            }
        }
    }
}

@Composable
private fun StepRingsCalendar(
    data: List<DailyDataPoint>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    goal: Int = 10000
) {
    val dataMap = remember(data) { data.associateBy { it.date } }
    val currentMonth = remember(selectedDate) { YearMonth.from(selectedDate) }
    val today = LocalDate.now(ZoneId.systemDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceMid)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())),
                style = MaterialTheme.typography.titleMedium,
                color = TextOnSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Day-of-week headers
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSubtle,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Calendar grid
            val firstOfMonth = currentMonth.atDay(1)
            val startDayOfWeek = firstOfMonth.dayOfWeek.value % 7 // Sunday = 0
            val daysInMonth = currentMonth.lengthOfMonth()

            var dayCounter = 1
            for (week in 0..5) {
                if (dayCounter > daysInMonth) break
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0..6) {
                        val cellIndex = week * 7 + col
                        if (cellIndex < startDayOfWeek || dayCounter > daysInMonth) {
                            Spacer(modifier = Modifier.weight(1f).height(48.dp))
                        } else {
                            val date = currentMonth.atDay(dayCounter)
                            val steps = dataMap[date]?.value ?: 0.0
                            val isSelected = date == selectedDate
                            val isFuture = date.isAfter(today)

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .then(
                                        if (!isFuture) Modifier.clickable { onDateSelected(date) }
                                        else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                val progress = (steps / goal).toFloat().coerceIn(0f, 1f)
                                val ringColor = when {
                                    isFuture -> Color.Transparent
                                    steps <= 0 -> Color.Gray.copy(alpha = 0.2f)
                                    steps < 5000 -> Color(0xFFE53935) // Red
                                    steps < 8000 -> Color(0xFFFFA726) // Orange/Yellow
                                    else -> Color(0xFF66BB6A) // Green
                                }

                                // Draw ring
                                if (!isFuture && steps > 0) {
                                    androidx.compose.foundation.Canvas(
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        val strokeW = 3.dp.toPx()
                                        drawArc(
                                            color = ringColor.copy(alpha = 0.2f),
                                            startAngle = -90f,
                                            sweepAngle = 360f,
                                            useCenter = false,
                                            style = Stroke(width = strokeW, cap = StrokeCap.Round),
                                            topLeft = Offset(strokeW / 2, strokeW / 2),
                                            size = Size(size.width - strokeW, size.height - strokeW)
                                        )
                                        drawArc(
                                            color = ringColor,
                                            startAngle = -90f,
                                            sweepAngle = 360f * progress,
                                            useCenter = false,
                                            style = Stroke(width = strokeW, cap = StrokeCap.Round),
                                            topLeft = Offset(strokeW / 2, strokeW / 2),
                                            size = Size(size.width - strokeW, size.height - strokeW)
                                        )
                                    }
                                }

                                // Day number
                                Text(
                                    text = dayCounter.toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = when {
                                        isSelected -> CardSteps
                                        isFuture -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                        else -> MaterialTheme.colorScheme.onBackground
                                    },
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            }
                            dayCounter++
                        }
                    }
                }
            }
        }
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
        HealthViewModel.MetricType.SKIN_TEMPERATURE -> MetricInfo("Skin Temperature", CardSkinTemperature, 1)
        HealthViewModel.MetricType.EXERCISE -> MetricInfo("Exercise", CardExercise)
        HealthViewModel.MetricType.NUTRITION -> MetricInfo("Nutrition", CardNutrition)
    }
}
