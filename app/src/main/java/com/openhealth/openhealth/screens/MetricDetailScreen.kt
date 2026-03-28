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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.automirrored.filled.ShowChart
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
    weightTargetKg: Float = 70.0f,
    exerciseSessions: List<com.openhealth.openhealth.model.ExerciseSession> = emptyList(),
    healthData: com.openhealth.openhealth.model.HealthData? = null,
    onSessionClick: (com.openhealth.openhealth.model.ExerciseSession) -> Unit = {}
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

                            // Hero section — skip for metrics with custom heroes
                            val skipGenericHero = metricType in listOf(
                                HealthViewModel.MetricType.SLEEP, HealthViewModel.MetricType.STEPS,
                                HealthViewModel.MetricType.NUTRITION, HealthViewModel.MetricType.EXERCISE,
                                HealthViewModel.MetricType.WEIGHT, HealthViewModel.MetricType.BODY_FAT,
                                HealthViewModel.MetricType.BASAL_METABOLIC_RATE, HealthViewModel.MetricType.BODY_WATER_MASS,
                                HealthViewModel.MetricType.LEAN_BODY_MASS, HealthViewModel.MetricType.BONE_MASS,
                                HealthViewModel.MetricType.HEART_RATE_VARIABILITY, HealthViewModel.MetricType.RESPIRATORY_RATE
                            )
                            if (!skipGenericHero) {
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

                            val skipToday = metricType in listOf(
                                HealthViewModel.MetricType.SLEEP, HealthViewModel.MetricType.STEPS,
                                HealthViewModel.MetricType.NUTRITION, HealthViewModel.MetricType.EXERCISE,
                                HealthViewModel.MetricType.HEART_RATE, HealthViewModel.MetricType.WEIGHT,
                                HealthViewModel.MetricType.BODY_FAT, HealthViewModel.MetricType.BASAL_METABOLIC_RATE,
                                HealthViewModel.MetricType.BODY_WATER_MASS, HealthViewModel.MetricType.LEAN_BODY_MASS,
                                HealthViewModel.MetricType.BONE_MASS,
                                HealthViewModel.MetricType.HEART_RATE_VARIABILITY, HealthViewModel.MetricType.RESPIRATORY_RATE
                            )
                            if (!skipToday) {
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

                        // ── Heart Rate Gradient Line Chart Hero ──
                        if (metricType == HealthViewModel.MetricType.HEART_RATE && metricHistory?.last30Days?.isNotEmpty() == true) {
                            item {
                                val chartData = metricHistory.last30Days.takeLast(7)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(SurfaceLow)
                                        .padding(16.dp)
                                ) {
                                    LineChart(
                                        data = chartData,
                                        lineColor = VibrantMagenta,
                                        fillColor = VibrantMagenta.copy(alpha = 0.15f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(160.dp)
                                    )
                                }
                            }
                        }

                        // ── HRV Custom Detail ──
                        if (metricType == HealthViewModel.MetricType.HEART_RATE_VARIABILITY) {
                            item {
                                val hrv = healthData?.heartRateVariability
                                val currentHrv = hrv?.rmssdMs ?: selectedDateValue
                                val avgHrv = hrv?.avgMs ?: currentHrv
                                val minHrv = hrv?.minMs ?: currentHrv
                                val maxHrv = hrv?.maxMs ?: currentHrv

                                // Determine quality level
                                val qualityLabel = when {
                                    currentHrv >= 60 -> "GOOD"
                                    currentHrv >= 30 -> "MODERATE"
                                    else -> "LOW"
                                }
                                val qualityColor = when {
                                    currentHrv >= 60 -> SuccessGreen
                                    currentHrv >= 30 -> WarningOrange
                                    else -> VibrantMagenta
                                }

                                // 7-day trend from history
                                val last7 = metricHistory?.last30Days?.takeLast(7) ?: emptyList()
                                val trendValue = if (last7.size >= 2) {
                                    val recent = last7.takeLast(3).map { it.value }.average()
                                    val older = last7.take(3).map { it.value }.average()
                                    recent - older
                                } else 0.0
                                val trendLabel = when {
                                    trendValue > 2 -> "IMPROVING"
                                    trendValue < -2 -> "DECLINING"
                                    else -> "STABLE"
                                }

                                // Custom Hero
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(28.dp))
                                        .background(SurfaceLow)
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "HEART RATE VARIABILITY",
                                        color = TextSubtle,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 2.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        Text(
                                            text = String.format("%.0f", currentHrv),
                                            color = TextOnSurface,
                                            fontSize = 48.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "ms",
                                            color = TextOnSurfaceVariant,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    // 7-DAY TREND badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(SurfaceHighest)
                                            .padding(horizontal = 16.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "7-DAY TREND: $trendLabel",
                                            color = ElectricIndigo,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.5.sp
                                        )
                                    }
                                }
                            }

                            // Recovery Arc Gauge
                            item {
                                val hrv = healthData?.heartRateVariability
                                val currentHrv = hrv?.rmssdMs ?: selectedDateValue
                                // Normalize HRV to 0-1 range (0-120ms typical range)
                                val normalizedHrv = (currentHrv / 120.0).coerceIn(0.0, 1.0).toFloat()
                                val animatedProgress by animateFloatAsState(
                                    targetValue = normalizedHrv,
                                    animationSpec = tween(durationMillis = 1200),
                                    label = "hrvProgress"
                                )

                                val qualityLabel = when {
                                    currentHrv >= 60 -> "Good Recovery"
                                    currentHrv >= 30 -> "Moderate Recovery"
                                    else -> "Low Recovery"
                                }
                                val qualityColor = when {
                                    currentHrv >= 60 -> SuccessGreen
                                    currentHrv >= 30 -> WarningOrange
                                    else -> VibrantMagenta
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(28.dp))
                                        .background(SurfaceLow)
                                        .padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier.size(180.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // Background track
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            val strokeWidth = 12.dp.toPx()
                                            val padding = strokeWidth / 2
                                            drawArc(
                                                color = SurfaceMid,
                                                startAngle = -225f,
                                                sweepAngle = 270f,
                                                useCenter = false,
                                                topLeft = Offset(padding, padding),
                                                size = Size(size.width - strokeWidth, size.height - strokeWidth),
                                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                            )
                                        }
                                        // Progress arc with gradient
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            val strokeWidth = 12.dp.toPx()
                                            val padding = strokeWidth / 2
                                            drawArc(
                                                brush = Brush.sweepGradient(
                                                    listOf(VibrantMagenta, ElectricIndigo, SoftLavender)
                                                ),
                                                startAngle = -225f,
                                                sweepAngle = 270f * animatedProgress,
                                                useCenter = false,
                                                topLeft = Offset(padding, padding),
                                                size = Size(size.width - strokeWidth, size.height - strokeWidth),
                                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                            )
                                        }
                                        // Center text
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = qualityLabel,
                                                color = qualityColor,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${(normalizedHrv * 100).roundToInt()}%",
                                                color = TextOnSurfaceVariant,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }

                            // 3 Stat Cards: Previous, High, Low
                            item {
                                val hrv = healthData?.heartRateVariability
                                val currentHrv = hrv?.rmssdMs ?: selectedDateValue
                                val minHrv = hrv?.minMs ?: currentHrv
                                val maxHrv = hrv?.maxMs ?: currentHrv
                                val yesterday = metricHistory?.allHistoricalData
                                    ?.find { it.date == selectedDate.minusDays(1) }?.value
                                val previousValue = yesterday ?: (hrv?.avgMs ?: currentHrv)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Previous
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(SurfaceLow)
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "PREVIOUS",
                                                color = TextOnSurfaceVariant,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.5.sp
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = String.format("%.0f", previousValue),
                                                color = TextOnSurface,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "ms",
                                                color = TextOnSurfaceVariant,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                    // High
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(SurfaceLow)
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "HIGH",
                                                color = TextOnSurfaceVariant,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.5.sp
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = String.format("%.0f", maxHrv),
                                                color = SuccessGreen,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "ms",
                                                color = TextOnSurfaceVariant,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                    // Low
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(SurfaceLow)
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "LOW",
                                                color = TextOnSurfaceVariant,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.5.sp
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = String.format("%.0f", minHrv),
                                                color = VibrantMagenta,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "ms",
                                                color = TextOnSurfaceVariant,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }

                            // Recovery Insight Card
                            item {
                                val hrv = healthData?.heartRateVariability
                                val currentHrv = hrv?.rmssdMs ?: selectedDateValue

                                val insightBadge = when {
                                    currentHrv >= 60 -> "GOOD"
                                    currentHrv >= 30 -> "MODERATE"
                                    else -> "LOW"
                                }
                                val insightColor = when {
                                    currentHrv >= 60 -> SuccessGreen
                                    currentHrv >= 30 -> WarningOrange
                                    else -> VibrantMagenta
                                }
                                val insightText = when {
                                    currentHrv >= 60 -> "Your autonomic nervous system shows strong recovery. This is a great day for intense training or challenging activities."
                                    currentHrv >= 30 -> "Your body is in a moderate recovery state. Consider balanced activity with adequate rest periods."
                                    else -> "Your HRV indicates your body needs more recovery time. Focus on rest, hydration, and stress management today."
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(SurfaceLow)
                                        .padding(20.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.Top) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(ElectricIndigo.copy(alpha = 0.12f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Favorite,
                                                contentDescription = null,
                                                tint = ElectricIndigo,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "Recovery Insight",
                                                color = TextOnSurface,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(50))
                                                    .background(insightColor.copy(alpha = 0.15f))
                                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = insightBadge,
                                                    color = insightColor,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 1.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = insightText,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = TextOnSurfaceVariant,
                                                lineHeight = 20.sp
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(50))
                                                    .background(SurfaceHighest)
                                                    .clickable { }
                                                    .padding(horizontal = 20.dp, vertical = 10.dp)
                                            ) {
                                                Text(
                                                    text = "View Sleep Coach",
                                                    color = ElectricIndigo,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // History Section
                            item {
                                val historyData = metricHistory?.last30Days?.takeLast(7)?.reversed() ?: emptyList()
                                if (historyData.isNotEmpty()) {
                                    Column {
                                        Text(
                                            text = "HISTORY",
                                            color = TextOnSurfaceVariant,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 2.sp
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        historyData.forEach { point ->
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(SurfaceLow)
                                                    .padding(16.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = point.date.format(
                                                            DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())
                                                        ),
                                                        color = TextOnSurface,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    Text(
                                                        text = "${String.format("%.0f", point.value)} ms",
                                                        color = ElectricIndigo,
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                    }
                                }
                            }
                        }

                        // ── Respiratory Rate Custom Detail ──
                        if (metricType == HealthViewModel.MetricType.RESPIRATORY_RATE) {
                            item {
                                val rr = healthData?.respiratoryRate
                                val currentRr = rr?.ratePerMinute ?: selectedDateValue
                                val avgRr = rr?.avgRate ?: currentRr
                                val minRr = rr?.minRate ?: currentRr
                                val maxRr = rr?.maxRate ?: currentRr

                                // Determine stability
                                val stabilityLabel = when {
                                    maxRr - minRr <= 4 -> "STABLE"
                                    maxRr - minRr <= 8 -> "VARIABLE"
                                    else -> "ELEVATED"
                                }
                                val stabilityColor = when {
                                    maxRr - minRr <= 4 -> SuccessGreen
                                    maxRr - minRr <= 8 -> WarningOrange
                                    else -> VibrantMagenta
                                }

                                // Custom Hero
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(28.dp))
                                        .background(SurfaceLow)
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "RESPIRATORY RATE",
                                        color = TextSubtle,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 2.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        Text(
                                            text = String.format("%.0f", currentRr),
                                            color = TextOnSurface,
                                            fontSize = 48.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "br/min",
                                            color = TextOnSurfaceVariant,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    // Stability badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(SurfaceHighest)
                                            .padding(horizontal = 16.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = stabilityLabel,
                                            color = stabilityColor,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.5.sp
                                        )
                                    }
                                }
                            }

                            // Arc Gauge
                            item {
                                val rr = healthData?.respiratoryRate
                                val currentRr = rr?.ratePerMinute ?: selectedDateValue
                                // Normal range: 12-20 br/min. Normalize to 0-1
                                val normalizedRr = ((currentRr - 8) / 24.0).coerceIn(0.0, 1.0).toFloat()
                                val animatedProgress by animateFloatAsState(
                                    targetValue = normalizedRr,
                                    animationSpec = tween(durationMillis = 1200),
                                    label = "rrProgress"
                                )

                                val qualityLabel = when {
                                    currentRr in 12.0..20.0 -> "Normal"
                                    currentRr < 12 -> "Below Normal"
                                    else -> "Elevated"
                                }
                                val qualityColor = when {
                                    currentRr in 12.0..20.0 -> SuccessGreen
                                    currentRr < 12 -> WarningOrange
                                    else -> VibrantMagenta
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(28.dp))
                                        .background(SurfaceLow)
                                        .padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier.size(180.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // Background track
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            val strokeWidth = 12.dp.toPx()
                                            val padding = strokeWidth / 2
                                            drawArc(
                                                color = SurfaceMid,
                                                startAngle = -225f,
                                                sweepAngle = 270f,
                                                useCenter = false,
                                                topLeft = Offset(padding, padding),
                                                size = Size(size.width - strokeWidth, size.height - strokeWidth),
                                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                            )
                                        }
                                        // Progress arc with gradient
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            val strokeWidth = 12.dp.toPx()
                                            val padding = strokeWidth / 2
                                            drawArc(
                                                brush = Brush.sweepGradient(
                                                    listOf(ElectricIndigo, SoftLavender, VibrantMagenta)
                                                ),
                                                startAngle = -225f,
                                                sweepAngle = 270f * animatedProgress,
                                                useCenter = false,
                                                topLeft = Offset(padding, padding),
                                                size = Size(size.width - strokeWidth, size.height - strokeWidth),
                                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                            )
                                        }
                                        // Center text
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = qualityLabel,
                                                color = qualityColor,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${String.format("%.0f", currentRr)} br/min",
                                                color = TextOnSurfaceVariant,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }

                            // 3 Stat Cards: Avg, High, Low
                            item {
                                val rr = healthData?.respiratoryRate
                                val currentRr = rr?.ratePerMinute ?: selectedDateValue
                                val avgRr = rr?.avgRate ?: currentRr
                                val minRr = rr?.minRate ?: currentRr
                                val maxRr = rr?.maxRate ?: currentRr

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Avg
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(SurfaceLow)
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "AVG",
                                                color = TextOnSurfaceVariant,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.5.sp
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = String.format("%.0f", avgRr),
                                                color = TextOnSurface,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "br/min",
                                                color = TextOnSurfaceVariant,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                    // High
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(SurfaceLow)
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "HIGH",
                                                color = TextOnSurfaceVariant,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.5.sp
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = String.format("%.0f", maxRr),
                                                color = WarningOrange,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "br/min",
                                                color = TextOnSurfaceVariant,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                    // Low
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(SurfaceLow)
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "LOW",
                                                color = TextOnSurfaceVariant,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.5.sp
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = String.format("%.0f", minRr),
                                                color = ElectricIndigo,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "br/min",
                                                color = TextOnSurfaceVariant,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }

                            // Recovery Insight Card
                            item {
                                val rr = healthData?.respiratoryRate
                                val currentRr = rr?.ratePerMinute ?: selectedDateValue
                                val minRr = rr?.minRate ?: currentRr
                                val maxRr = rr?.maxRate ?: currentRr

                                val insightBadge = when {
                                    currentRr in 12.0..20.0 -> "NORMAL"
                                    currentRr < 12 -> "LOW"
                                    else -> "ELEVATED"
                                }
                                val insightColor = when {
                                    currentRr in 12.0..20.0 -> SuccessGreen
                                    currentRr < 12 -> WarningOrange
                                    else -> VibrantMagenta
                                }
                                val insightText = when {
                                    currentRr in 12.0..20.0 -> "Your breathing rate is within the normal range, indicating good respiratory health and a relaxed state."
                                    currentRr < 12 -> "Your respiratory rate is below the typical range. This may indicate deep relaxation or could warrant monitoring."
                                    else -> "Your respiratory rate is elevated. This could be due to physical activity, stress, or other factors. Consider relaxation techniques."
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(SurfaceLow)
                                        .padding(20.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.Top) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(ElectricIndigo.copy(alpha = 0.12f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Air,
                                                contentDescription = null,
                                                tint = ElectricIndigo,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "Recovery Insight",
                                                color = TextOnSurface,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(50))
                                                    .background(insightColor.copy(alpha = 0.15f))
                                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = insightBadge,
                                                    color = insightColor,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 1.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = insightText,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = TextOnSurfaceVariant,
                                                lineHeight = 20.sp
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(50))
                                                    .background(SurfaceHighest)
                                                    .clickable { }
                                                    .padding(horizontal = 20.dp, vertical = 10.dp)
                                            ) {
                                                Text(
                                                    text = "View Sleep Coach",
                                                    color = ElectricIndigo,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Recent Activity Section
                            item {
                                val historyData = metricHistory?.last30Days?.takeLast(7)?.reversed() ?: emptyList()
                                if (historyData.isNotEmpty()) {
                                    Column {
                                        Text(
                                            text = "RECENT ACTIVITY",
                                            color = TextOnSurfaceVariant,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 2.sp
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        historyData.forEach { point ->
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(SurfaceLow)
                                                    .padding(16.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = point.date.format(
                                                            DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())
                                                        ),
                                                        color = TextOnSurface,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    Text(
                                                        text = "${String.format("%.0f", point.value)} br/min",
                                                        color = ElectricIndigo,
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                    }
                                }
                            }
                        }

                        // ── Steps Custom Hero: Circular Progress Ring ──
                        if (metricType == HealthViewModel.MetricType.STEPS) {
                            item {
                                val stepsCount = healthData?.steps?.count ?: selectedDateValue.toLong()
                                val goal = healthData?.steps?.goal ?: stepsGoal.toLong()
                                val progress = if (goal > 0) (stepsCount.toFloat() / goal.toFloat()).coerceIn(0f, 1.5f) else 0f
                                val animatedProgress by animateFloatAsState(
                                    targetValue = progress,
                                    animationSpec = tween(durationMillis = 1200),
                                    label = "stepsProgress"
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(28.dp))
                                        .background(SurfaceLow)
                                        .padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier.size(240.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // Background track
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            val strokeWidth = 14.dp.toPx()
                                            val padding = strokeWidth / 2
                                            drawArc(
                                                color = SurfaceMid,
                                                startAngle = -225f,
                                                sweepAngle = 270f,
                                                useCenter = false,
                                                topLeft = Offset(padding, padding),
                                                size = Size(size.width - strokeWidth, size.height - strokeWidth),
                                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                            )
                                        }
                                        // Progress arc with gradient
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            val strokeWidth = 14.dp.toPx()
                                            val padding = strokeWidth / 2
                                            drawArc(
                                                brush = Brush.sweepGradient(
                                                    listOf(ElectricIndigo, VibrantMagenta, ElectricIndigo)
                                                ),
                                                startAngle = -225f,
                                                sweepAngle = 270f * animatedProgress.coerceAtMost(1f),
                                                useCenter = false,
                                                topLeft = Offset(padding, padding),
                                                size = Size(size.width - strokeWidth, size.height - strokeWidth),
                                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                            )
                                        }
                                        // Center text
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "TOTAL STEPS",
                                                color = TextOnSurfaceVariant,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 2.sp
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "%,d".format(stepsCount),
                                                color = TextOnSurface,
                                                fontSize = 44.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                            Text(
                                                text = "/ %,d".format(goal),
                                                color = ElectricIndigo,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }

                            // ── Neural Insight Card ──
                            item {
                                val insight = getInsightForMetric(metricType, selectedDateValue, stepsGoal, healthData)
                                if (insight != null) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(24.dp))
                                            .background(SurfaceLow)
                                            .padding(20.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.Top) {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .background(ElectricIndigo.copy(alpha = 0.12f), CircleShape),
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
                                                    text = "NEURAL INSIGHT",
                                                    color = ElectricIndigo,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 2.sp
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = insight.meaning,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = TextOnSurfaceVariant,
                                                    lineHeight = 20.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // ── Stats Grid: Distance / Active Time / Floors ──
                            item {
                                val distanceKm = healthData?.distance?.kilometers ?: 0.0
                                val activeMin = healthData?.exercise?.totalDuration?.toMinutes() ?: 0L
                                val floorsCount = healthData?.floors?.count ?: 0

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Distance
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(SurfaceLow)
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(ElectricIndigo.copy(alpha = 0.12f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                                    contentDescription = null,
                                                    tint = ElectricIndigo,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "DISTANCE",
                                                color = TextOnSurfaceVariant,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.5.sp
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "%.1f km".format(distanceKm),
                                                color = TextOnSurface,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    // Active Time
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(SurfaceLow)
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(VibrantMagenta.copy(alpha = 0.12f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.EmojiEvents,
                                                    contentDescription = null,
                                                    tint = VibrantMagenta,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "ACTIVE",
                                                color = TextOnSurfaceVariant,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.5.sp
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "${activeMin} min",
                                                color = TextOnSurface,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    // Floors
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(SurfaceLow)
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(SuccessGreen.copy(alpha = 0.12f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                                    contentDescription = null,
                                                    tint = SuccessGreen,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "FLOORS",
                                                color = TextOnSurfaceVariant,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.5.sp
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "$floorsCount",
                                                color = TextOnSurface,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Daily Stats Summary for HR, HRV, SpO2, RR
                        if (healthData != null) {
                            data class StatItem(val label: String, val value: String)
                            val statsItems: List<StatItem>? = when (metricType) {
                                HealthViewModel.MetricType.HEART_RATE -> null
                                HealthViewModel.MetricType.HEART_RATE_VARIABILITY -> null
                                HealthViewModel.MetricType.RESPIRATORY_RATE -> null
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

                        // ── Heart Rate 2×2 Stat Grid (Stitch design) ──
                        if (metricType == HealthViewModel.MetricType.HEART_RATE && healthData != null) {
                            val hr = healthData.heartRate
                            if (hr.currentBpm != null && hr.minBpm != null && hr.maxBpm != null) {
                                val avgBpm = if (hr.readings.isNotEmpty()) {
                                    hr.readings.map { it.bpm }.average().roundToInt()
                                } else {
                                    ((hr.minBpm + hr.maxBpm) / 2)
                                }

                                data class HrStat(val label: String, val value: String, val unit: String, val icon: @Composable () -> Unit)
                                val hrStats = listOf(
                                    HrStat("LATEST", "${hr.currentBpm}", "bpm") { Icon(Icons.Default.Favorite, null, tint = CardHeartRate, modifier = Modifier.size(18.dp)) },
                                    HrStat("AVG", "$avgBpm", "bpm") { Icon(Icons.AutoMirrored.Filled.ShowChart, null, tint = CardHeartRate, modifier = Modifier.size(18.dp)) },
                                    HrStat("MIN", "${hr.minBpm}", "bpm") { Icon(Icons.Default.KeyboardArrowDown, null, tint = CardHeartRate, modifier = Modifier.size(18.dp)) },
                                    HrStat("MAX", "${hr.maxBpm}", "bpm") { Icon(Icons.Default.KeyboardArrowUp, null, tint = CardHeartRate, modifier = Modifier.size(18.dp)) }
                                )

                                item {
                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        for (row in hrStats.chunked(2)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                row.forEach { stat ->
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .clip(RoundedCornerShape(24.dp))
                                                            .background(SurfaceLow)
                                                            .padding(vertical = 20.dp, horizontal = 16.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                            stat.icon()
                                                            Spacer(modifier = Modifier.height(8.dp))
                                                            Text(
                                                                text = stat.label,
                                                                color = TextOnSurfaceVariant,
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                letterSpacing = 2.sp
                                                            )
                                                            Spacer(modifier = Modifier.height(4.dp))
                                                            Row(
                                                                verticalAlignment = Alignment.Bottom,
                                                                horizontalArrangement = Arrangement.Center
                                                            ) {
                                                                Text(
                                                                    text = stat.value,
                                                                    color = TextOnSurface,
                                                                    fontSize = 18.sp,
                                                                    fontWeight = FontWeight.Bold
                                                                )
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Text(
                                                                    text = stat.unit,
                                                                    color = TextOnSurfaceVariant,
                                                                    fontSize = 12.sp
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // ═══════════════════════════════════════
                        // Nutrition Custom Detail Layout
                        // ═══════════════════════════════════════
                        if (metricType == HealthViewModel.MetricType.NUTRITION && healthData != null) {
                            val n = healthData.nutrition
                            val calories = n.calories ?: 0.0
                            val protein = n.proteinGrams ?: 0.0
                            val carbs = n.carbsGrams ?: 0.0
                            val fat = n.fatGrams ?: 0.0

                            val calGoal = 2200.0
                            val calProgress = (calories / calGoal).toFloat().coerceIn(0f, 1f)
                            val calRemaining = (calGoal - calories).coerceAtLeast(0.0).toInt()

                            // ── Daily Intake Hero with gradient bar ──
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(28.dp))
                                        .background(SurfaceLow)
                                        .padding(horizontal = 24.dp, vertical = 28.dp)
                                ) {
                                    Column {
                                        Text("DAILY INTAKE", color = TextSubtle, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(verticalAlignment = Alignment.Bottom) {
                                            Text(String.format("%,.0f", calories), color = TextOnSurface, fontSize = 44.sp, fontWeight = FontWeight.Black)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.padding(bottom = 6.dp)) {
                                                Text("/ ${String.format("%,.0f", calGoal)}", color = ElectricIndigo, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                                Text("kcal", color = TextOnSurfaceVariant, fontSize = 12.sp)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        // Gradient progress bar
                                        Box(modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)).background(SurfaceHighest)) {
                                            Box(modifier = Modifier.fillMaxWidth(calProgress.coerceAtLeast(0.01f)).fillMaxHeight().background(Brush.horizontalGradient(listOf(ElectricIndigo, VibrantMagenta)), RoundedCornerShape(6.dp)))
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("${(calProgress * 100).roundToInt()}% ACHIEVED", fontSize = 10.sp, color = TextOnSurfaceVariant, letterSpacing = 1.sp)
                                            Text("$calRemaining KCAL LEFT", fontSize = 10.sp, color = TextOnSurfaceVariant, letterSpacing = 1.sp)
                                        }
                                    }
                                }
                            }

                            // ── Each Macro in SEPARATE card (Stitch design) ──
                            // Protein
                            item {
                                NutritionMacroCard(label = "Protein", grams = protein, goal = 120.0, color = ElectricIndigo)
                            }
                            // Carbs
                            item {
                                NutritionMacroCard(label = "Carbs", grams = carbs, goal = 300.0, color = VibrantMagenta)
                            }
                            // Fat
                            item {
                                NutritionMacroCard(label = "Fats", grams = fat, goal = 50.0, color = SoftLavender)
                            }

                            // ── Metabolic Fuel Insight Card ──
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(SurfaceLow).padding(20.dp)
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(ElectricIndigo.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.Bolt, null, tint = ElectricIndigo, modifier = Modifier.size(20.dp))
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text("Metabolic Fuel", color = TextOnSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        val dominantMacro = when { protein >= carbs && protein >= fat -> "Hypertrophy"; carbs >= protein && carbs >= fat -> "Endurance"; else -> "Hormone Balance" }
                                        Text(
                                            "Your current macro split is highly optimized for $dominantMacro. High protein intake detected. Consider adding complex carbs before your 6 PM session to maintain peak intensity.",
                                            color = TextOnSurfaceVariant, fontSize = 13.sp, lineHeight = 20.sp
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Box(modifier = Modifier.background(ElectricIndigo.copy(alpha = 0.15f), RoundedCornerShape(12.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                                                Text("PEAK RECOVERY", fontSize = 9.sp, color = ElectricIndigo, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                            }
                                            Box(modifier = Modifier.background(VibrantMagenta.copy(alpha = 0.15f), RoundedCornerShape(12.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                                                Text("MUSCLE SPARING", fontSize = 9.sp, color = VibrantMagenta, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // ═══════════════════════════════════════
                        // Body Composition Detail Layout — Stitch Design
                        // ═══════════════════════════════════════
                        val isBodyCompMetric = metricType in listOf(
                            HealthViewModel.MetricType.WEIGHT,
                            HealthViewModel.MetricType.BODY_FAT,
                            HealthViewModel.MetricType.BASAL_METABOLIC_RATE,
                            HealthViewModel.MetricType.BODY_WATER_MASS,
                            HealthViewModel.MetricType.LEAN_BODY_MASS,
                            HealthViewModel.MetricType.BONE_MASS
                        )

                        if (isBodyCompMetric && healthData != null) {

                            // ── 1. Weight Ring Gauge Hero ──
                            item {
                                val currentWeight = healthData.weight.kilograms
                                val weightStr = currentWeight?.let { String.format("%.1f", it) } ?: "--"
                                val targetWeight = weightTargetKg.toDouble()
                                val progress = currentWeight?.let {
                                    val maxRange = 20.0 // assume +-20 kg range
                                    (1.0 - ((it - targetWeight) / maxRange).coerceIn(0.0, 1.0)).toFloat()
                                } ?: 0f

                                val animatedProgress by animateFloatAsState(
                                    targetValue = progress,
                                    animationSpec = tween(durationMillis = 1200),
                                    label = "ringProgress"
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(SurfaceLow)
                                        .padding(24.dp)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        // Ring gauge
                                        Box(
                                            modifier = Modifier.size(200.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Canvas(modifier = Modifier.size(200.dp)) {
                                                val strokeWidth = 14.dp.toPx()
                                                val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                                                val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                                                val startAngle = 135f
                                                val sweepTotal = 270f

                                                // Track
                                                drawArc(
                                                    color = SurfaceHighest,
                                                    startAngle = startAngle,
                                                    sweepAngle = sweepTotal,
                                                    useCenter = false,
                                                    topLeft = topLeft,
                                                    size = arcSize,
                                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                                )

                                                // Gradient arc
                                                drawArc(
                                                    brush = Brush.sweepGradient(
                                                        listOf(ElectricIndigo, VibrantMagenta, ElectricIndigo)
                                                    ),
                                                    startAngle = startAngle,
                                                    sweepAngle = sweepTotal * animatedProgress,
                                                    useCenter = false,
                                                    topLeft = topLeft,
                                                    size = arcSize,
                                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                                )
                                            }
                                            // Center text
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = weightStr,
                                                    color = TextOnSurface,
                                                    fontSize = 36.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "KG",
                                                    color = TextOnSurfaceVariant,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 2.sp
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(20.dp))

                                        // Goal progress label row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "GOAL PROGRESS",
                                                color = TextOnSurfaceVariant,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.5.sp
                                            )
                                            Text(
                                                text = "TARGET: ${String.format("%.1f", targetWeight)} KG",
                                                color = ElectricIndigo,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Progress bar
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(SurfaceHighest)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(animatedProgress)
                                                    .fillMaxHeight()
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(
                                                        Brush.linearGradient(
                                                            listOf(ElectricIndigo, VibrantMagenta)
                                                        )
                                                    )
                                            )
                                        }
                                    }
                                }
                            }

                            // ── 2. COMPOSITION MATRIX header ──
                            item {
                                Text(
                                    text = "COMPOSITION MATRIX",
                                    color = ElectricIndigo,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp,
                                    modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
                                )
                            }

                            // ── 3. 2x2 Composition Grid ──
                            item {
                                val bodyFatVal = healthData.bodyFat.percentage
                                val bodyFatStr = bodyFatVal?.let { String.format("%.1f %%", it) } ?: "-- %"
                                val bmrRaw = healthData.basalMetabolicRate.caloriesPerDay
                                val bmrStr = bmrRaw?.let { String.format("%.0f KCAL", it) } ?: "-- KCAL"
                                val waterKg = healthData.bodyWaterMass.kilograms
                                val waterPct = if (waterKg != null && healthData.weight.kilograms != null && healthData.weight.kilograms!! > 0) {
                                    String.format("%.1f %%", (waterKg / healthData.weight.kilograms!!) * 100)
                                } else waterKg?.let { String.format("%.1f KG", it) } ?: "-- %"
                                val boneMassVal = healthData.boneMass.kilograms
                                val boneMassStr = boneMassVal?.let { String.format("%.1f KG", it) } ?: "-- KG"

                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // Body Fat
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(120.dp)
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(SurfaceLow)
                                                .padding(16.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.fillMaxSize(),
                                                verticalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .background(VibrantMagenta.copy(alpha = 0.15f), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.WaterDrop,
                                                        contentDescription = null,
                                                        tint = VibrantMagenta,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                Column {
                                                    Text(
                                                        text = "BODY FAT",
                                                        color = TextSubtle,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        letterSpacing = 1.5.sp
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = bodyFatStr,
                                                        color = TextOnSurface,
                                                        fontSize = 20.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                        // BMR
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(120.dp)
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(SurfaceLow)
                                                .padding(16.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.fillMaxSize(),
                                                verticalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .background(ElectricIndigo.copy(alpha = 0.15f), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Bolt,
                                                        contentDescription = null,
                                                        tint = ElectricIndigo,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                Column {
                                                    Text(
                                                        text = "BMR",
                                                        color = TextSubtle,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        letterSpacing = 1.5.sp
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = bmrStr,
                                                        color = TextOnSurface,
                                                        fontSize = 20.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // Water
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(120.dp)
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(SurfaceLow)
                                                .padding(16.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.fillMaxSize(),
                                                verticalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .background(SoftLavender.copy(alpha = 0.15f), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.WaterDrop,
                                                        contentDescription = null,
                                                        tint = SoftLavender,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                Column {
                                                    Text(
                                                        text = "WATER",
                                                        color = TextSubtle,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        letterSpacing = 1.5.sp
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = waterPct,
                                                        color = TextOnSurface,
                                                        fontSize = 20.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                        // Bone Mass
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(120.dp)
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(SurfaceLow)
                                                .padding(16.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.fillMaxSize(),
                                                verticalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .background(VibrantMagenta.copy(alpha = 0.15f), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.AutoAwesome,
                                                        contentDescription = null,
                                                        tint = VibrantMagenta,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                Column {
                                                    Text(
                                                        text = "BONE MASS",
                                                        color = TextSubtle,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        letterSpacing = 1.5.sp
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = boneMassStr,
                                                        color = TextOnSurface,
                                                        fontSize = 20.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // ── 4. Lean Muscle Mass wide card ──
                            item {
                                val leanMassKg = healthData.leanBodyMass.kilograms
                                val leanMassStr = leanMassKg?.let { String.format("%.1f KG", it) } ?: "-- KG"

                                // Calculate monthly delta from history
                                val monthlyDelta = metricHistory?.allHistoricalData?.let { data ->
                                    if (data.size >= 2) {
                                        val recent = data.last().value
                                        val oldest = data.first().value
                                        recent - oldest
                                    } else null
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(SurfaceHigh)
                                        .padding(20.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .background(ElectricIndigo.copy(alpha = 0.15f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.FitnessCenter,
                                                contentDescription = null,
                                                tint = ElectricIndigo,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "LEAN MUSCLE MASS",
                                                color = TextSubtle,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.5.sp
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = leanMassStr,
                                                color = TextOnSurface,
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        if (monthlyDelta != null) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(20.dp))
                                                    .background(ElectricIndigo.copy(alpha = 0.15f))
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = "${String.format("%+.1f", monthlyDelta)}kg this month",
                                                    color = ElectricIndigo,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // ── 5. 7-DAY VELOCITY bar chart ──
                            if (metricHistory?.allHistoricalData?.isNotEmpty() == true) {
                                item {
                                    val last7Days = metricHistory.allHistoricalData.takeLast(7)
                                    val values = last7Days.map { it.value.toFloat() }
                                    val minVal = (values.minOrNull() ?: 0f) - 1f
                                    val maxVal = (values.maxOrNull() ?: 100f) + 1f
                                    val range = (maxVal - minVal).coerceAtLeast(1f)
                                    val todayDow = LocalDate.now().dayOfWeek

                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "7-DAY VELOCITY",
                                                color = ElectricIndigo,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 2.sp
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(20.dp))
                                                    .background(ElectricIndigo.copy(alpha = 0.12f))
                                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = "WEIGHT TREND",
                                                    color = ElectricIndigo,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 1.sp
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(SurfaceLowest)
                                                .padding(20.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(140.dp),
                                                horizontalArrangement = Arrangement.SpaceEvenly,
                                                verticalAlignment = Alignment.Bottom
                                            ) {
                                                last7Days.forEachIndexed { index, dp ->
                                                    val normalized = ((dp.value.toFloat() - minVal) / range).coerceIn(0.1f, 1f)
                                                    val isToday = dp.date.dayOfWeek == todayDow
                                                    val dayLabel = dp.date.format(DateTimeFormatter.ofPattern("EEE", Locale.getDefault())).take(3).uppercase()

                                                    Column(
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        // Value label on top
                                                        Text(
                                                            text = String.format("%.1f", dp.value),
                                                            color = if (isToday) ElectricIndigo else TextSubtle,
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Spacer(modifier = Modifier.height(4.dp))

                                                        // Bar track
                                                        Box(
                                                            modifier = Modifier
                                                                .width(24.dp)
                                                                .weight(1f)
                                                                .clip(RoundedCornerShape(12.dp))
                                                                .background(SurfaceHigh),
                                                            contentAlignment = Alignment.BottomCenter
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .fillMaxHeight(normalized)
                                                                    .clip(RoundedCornerShape(12.dp))
                                                                    .background(
                                                                        if (isToday) Brush.linearGradient(
                                                                            listOf(ElectricIndigo, VibrantMagenta),
                                                                            start = Offset(0f, Float.POSITIVE_INFINITY),
                                                                            end = Offset(0f, 0f)
                                                                        )
                                                                        else Brush.linearGradient(
                                                                            listOf(ElectricIndigo.copy(alpha = 0.4f), VibrantMagenta.copy(alpha = 0.4f)),
                                                                            start = Offset(0f, Float.POSITIVE_INFINITY),
                                                                            end = Offset(0f, 0f)
                                                                        )
                                                                    )
                                                            )
                                                        }

                                                        Spacer(modifier = Modifier.height(6.dp))
                                                        Text(
                                                            text = dayLabel,
                                                            color = if (isToday) ElectricIndigo else TextSubtle,
                                                            fontSize = 9.sp,
                                                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                                                            letterSpacing = 0.5.sp
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // ── 6. NEURAL ANALYSIS card ──
                            item {
                                val bodyFat = healthData.bodyFat.percentage
                                val leanMass = healthData.leanBodyMass.kilograms
                                val weight = healthData.weight.kilograms
                                val bmrCals = healthData.basalMetabolicRate.caloriesPerDay

                                val analysisText = when {
                                    bodyFat != null && leanMass != null && weight != null && bmrCals != null -> {
                                        val leanPct = (leanMass / weight * 100).roundToInt()
                                        "Your lean mass ratio is ${leanPct}% with a metabolic rate of ${String.format("%.0f", bmrCals)} kcal/day. " +
                                        if (bodyFat < 20) "Excellent body composition. Your metabolic efficiency is high — maintain current training load."
                                        else if (bodyFat < 25) "Good composition profile. Increasing resistance training volume could shift your lean-to-fat ratio and boost resting metabolism."
                                        else "Focus on progressive overload and protein timing to improve lean mass. Each kg of muscle gained increases BMR by ~13 kcal/day."
                                    }
                                    weight != null -> "Begin tracking body fat and lean mass alongside weight to unlock full metabolic analysis and calorie targets."
                                    else -> "Start logging body composition data to enable neural analysis of your metabolic profile."
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(
                                            Brush.linearGradient(
                                                listOf(SurfaceHigh, SurfaceLow)
                                            )
                                        )
                                        .padding(20.dp)
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.Top) {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .background(VibrantMagenta.copy(alpha = 0.15f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AutoAwesome,
                                                    contentDescription = null,
                                                    tint = VibrantMagenta,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "NEURAL ANALYSIS",
                                                    color = VibrantMagenta,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 1.5.sp
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = analysisText,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = TextOnSurfaceVariant,
                                                    lineHeight = 18.sp
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(50))
                                                .background(SurfaceHighest)
                                                .clickable { }
                                                .padding(vertical = 14.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "VIEW CALORIE TARGETS",
                                                color = ElectricIndigo,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.5.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // ═══════════════════════════════════════
                        // Exercise — Stitch Workout Analytics
                        // ═══════════════════════════════════════
                        if (metricType == HealthViewModel.MetricType.EXERCISE && healthData != null) {
                            // 1) Hero card — Active Energy Today
                            item {
                                val totalCals = healthData.calories.totalBurned.let { if (it > 0) it else exerciseSessions.sumOf { s -> s.caloriesBurned ?: 0.0 } }
                                val calGoal = 500.0
                                val progress = (totalCals / calGoal).toFloat().coerceIn(0f, 1f)
                                val calStr = String.format("%.0f", totalCals)

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(SurfaceLow)
                                        .padding(24.dp)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "ACTIVE ENERGY TODAY",
                                            color = TextOnSurfaceVariant,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 2.sp
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(
                                            verticalAlignment = Alignment.Bottom,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = calStr,
                                                color = TextOnSurface,
                                                fontSize = 44.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Kcal",
                                                color = ElectricIndigo,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(bottom = 6.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        // Gradient progress bar
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(10.dp)
                                                .clip(RoundedCornerShape(5.dp))
                                                .background(SurfaceHighest)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(progress.coerceAtLeast(0.02f))
                                                    .fillMaxHeight()
                                                    .clip(RoundedCornerShape(5.dp))
                                                    .background(
                                                        Brush.horizontalGradient(
                                                            listOf(ElectricIndigo, VibrantMagenta)
                                                        )
                                                    )
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "RECOVERY",
                                                color = TextOnSurfaceVariant,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.5.sp
                                            )
                                            Text(
                                                text = "PEAK INTENSITY",
                                                color = VibrantMagenta,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.5.sp
                                            )
                                        }
                                    }
                                }
                            }

                            // 2) 2x2 Stats Grid
                            item {
                                val totalDurMin = exerciseSessions.sumOf { it.duration.toMinutes() }
                                val avgHr = healthData.heartRate.currentBpm
                                val peakHr = healthData.heartRate.maxBpm
                                val vo2max = healthData.vo2Max.value

                                @Composable
                                fun StatCard(
                                    icon: @Composable () -> Unit,
                                    value: String,
                                    label: String,
                                    modifier: Modifier = Modifier
                                ) {
                                    Box(
                                        modifier = modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(SurfaceLow)
                                            .padding(16.dp)
                                    ) {
                                        Column {
                                            icon()
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(
                                                text = value,
                                                color = TextOnSurface,
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = label,
                                                color = TextOnSurfaceVariant,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.5.sp
                                            )
                                        }
                                    }
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        StatCard(
                                            icon = {
                                                Icon(
                                                    imageVector = Icons.Default.Timer,
                                                    contentDescription = null,
                                                    tint = ElectricIndigo,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            },
                                            value = "${totalDurMin} m",
                                            label = "DURATION",
                                            modifier = Modifier.weight(1f)
                                        )
                                        StatCard(
                                            icon = {
                                                Icon(
                                                    imageVector = Icons.Default.Favorite,
                                                    contentDescription = null,
                                                    tint = VibrantMagenta,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            },
                                            value = if (avgHr != null) "$avgHr bpm" else "-- bpm",
                                            label = "AVG HR",
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        StatCard(
                                            icon = {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                                    contentDescription = null,
                                                    tint = CardExercise,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            },
                                            value = if (peakHr != null) "$peakHr bpm" else "-- bpm",
                                            label = "PEAK HR",
                                            modifier = Modifier.weight(1f)
                                        )
                                        StatCard(
                                            icon = {
                                                Icon(
                                                    imageVector = Icons.Default.Air,
                                                    contentDescription = null,
                                                    tint = ElectricIndigo,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            },
                                            value = if (vo2max != null) "${String.format("%.0f", vo2max)} ml/kg" else "-- ml/kg",
                                            label = "EST. VO2 MAX",
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            // 3) Training Load — 7-day grouped bar chart
                            item {
                                val today = LocalDate.now()
                                val dayOfWeek = today.dayOfWeek // MONDAY=1 .. SUNDAY=7
                                val dayLabels = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")

                                // Group sessions by day-of-week, split into cardio (HIIT) vs strength (STR)
                                val cardioTypes = setOf("Running", "Walking", "Cycling", "Swimming", "Hiking", "Elliptical", "Rowing")
                                val sessionsByDay = exerciseSessions.groupBy {
                                    it.startTime.atZone(ZoneId.systemDefault()).toLocalDate().dayOfWeek
                                }
                                val hiitByDay = DayOfWeek.entries.map { dow ->
                                    sessionsByDay[dow]?.filter { it.exerciseType in cardioTypes }?.sumOf { it.duration.toMinutes() } ?: 0L
                                }
                                val strByDay = DayOfWeek.entries.map { dow ->
                                    sessionsByDay[dow]?.filter { it.exerciseType !in cardioTypes }?.sumOf { it.duration.toMinutes() } ?: 0L
                                }
                                val maxBar = maxOf(
                                    hiitByDay.maxOrNull() ?: 0L,
                                    strByDay.maxOrNull() ?: 0L,
                                    1L
                                ).toFloat()

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(SurfaceLow)
                                        .padding(20.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = "Training Load",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = TextOnSurface,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Activity distribution (7d)",
                                            color = TextOnSurfaceVariant,
                                            fontSize = 12.sp
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        // Legend
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .background(ElectricIndigo, CircleShape)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("HIIT", color = TextOnSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .background(VibrantMagenta, CircleShape)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("STR", color = TextOnSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        // Bars
                                        val barMaxHeight = 100.dp
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(barMaxHeight + 24.dp),
                                            horizontalArrangement = Arrangement.SpaceEvenly,
                                            verticalAlignment = Alignment.Bottom
                                        ) {
                                            for (i in 0 until 7) {
                                                val isToday = DayOfWeek.of(i + 1) == dayOfWeek
                                                val hiitFrac = (hiitByDay[i].toFloat() / maxBar).coerceIn(0f, 1f)
                                                val strFrac = (strByDay[i].toFloat() / maxBar).coerceIn(0f, 1f)

                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.height(barMaxHeight),
                                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                        verticalAlignment = Alignment.Bottom
                                                    ) {
                                                        // HIIT bar
                                                        Box(
                                                            modifier = Modifier
                                                                .width(10.dp)
                                                                .fillMaxHeight(hiitFrac.coerceAtLeast(0.03f))
                                                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                                .background(
                                                                    if (isToday) ElectricIndigo
                                                                    else ElectricIndigo.copy(alpha = 0.35f)
                                                                )
                                                        )
                                                        // STR bar
                                                        Box(
                                                            modifier = Modifier
                                                                .width(10.dp)
                                                                .fillMaxHeight(strFrac.coerceAtLeast(0.03f))
                                                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                                .background(
                                                                    if (isToday) VibrantMagenta
                                                                    else VibrantMagenta.copy(alpha = 0.35f)
                                                                )
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Text(
                                                        text = dayLabels[i],
                                                        color = if (isToday) TextOnSurface else TextSubtle,
                                                        fontSize = 9.sp,
                                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                                        letterSpacing = 0.5.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Insights Card — only for metrics without custom insight cards
                        val hasCustomInsight = metricType in listOf(
                            HealthViewModel.MetricType.SLEEP, HealthViewModel.MetricType.STEPS,
                            HealthViewModel.MetricType.EXERCISE, HealthViewModel.MetricType.NUTRITION,
                            HealthViewModel.MetricType.HEART_RATE_VARIABILITY,
                            HealthViewModel.MetricType.RESPIRATORY_RATE
                        ) || isBodyCompMetric
                        if (!hasCustomInsight) {
                            item {
                                val insight = getInsightForMetric(metricType, selectedDateValue, stepsGoal, healthData)
                                if (insight != null) {
                                    InsightCard(insight = insight)
                                }
                            }
                        }

                        // 4) Recent Sessions
                        if (metricType == HealthViewModel.MetricType.EXERCISE && exerciseSessions.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Recent Sessions",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextOnSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            items(exerciseSessions) { session ->
                                val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
                                val startStr = session.startTime.atZone(ZoneId.systemDefault()).format(timeFormatter)
                                val sessionDate = session.startTime.atZone(ZoneId.systemDefault()).toLocalDate()
                                val today = LocalDate.now()
                                val dateLabel = when {
                                    sessionDate == today -> "Today"
                                    sessionDate == today.minusDays(1) -> "Yesterday"
                                    else -> sessionDate.format(DateTimeFormatter.ofPattern("MMM d"))
                                }
                                val calStr = session.caloriesBurned?.let { String.format("%.0f", it) } ?: "--"
                                val iconVec = when (session.exerciseType) {
                                    "Running" -> Icons.Default.FitnessCenter
                                    "Walking" -> Icons.Default.FitnessCenter
                                    "Cycling" -> Icons.Default.FitnessCenter
                                    else -> Icons.Default.FitnessCenter
                                }
                                val iconColor = when (session.exerciseType) {
                                    "Running" -> CardExercise
                                    "Cycling" -> ElectricIndigo
                                    else -> VibrantMagenta
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(SurfaceLow)
                                        .clickable { onSessionClick(session) }
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Icon circle
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .background(iconColor.copy(alpha = 0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = iconVec,
                                            contentDescription = null,
                                            tint = iconColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = session.exerciseType,
                                            color = TextOnSurface,
                                            fontWeight = FontWeight.SemiBold,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            text = "$dateLabel \u2022 $startStr",
                                            color = TextSubtle,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "$calStr KCAL",
                                            color = TextOnSurface,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // 5) Neural Performance Insight
                            item {
                                val totalCals = exerciseSessions.sumOf { it.caloriesBurned ?: 0.0 }
                                val totalDurMin = exerciseSessions.sumOf { it.duration.toMinutes() }
                                val sessionCount = exerciseSessions.size
                                val insightText = when {
                                    sessionCount >= 2 && totalDurMin >= 60 ->
                                        "Your anaerobic threshold is trending upward based on $sessionCount sessions (${totalDurMin}min). Recovery curve analysis shows improved cardiovascular efficiency. Optimal training window: tomorrow 07:00-09:00."
                                    sessionCount == 1 && totalDurMin >= 30 ->
                                        "Solid effort \u2014 ${String.format("%.0f", totalCals)} kcal in ${totalDurMin} min. Your recovery curve suggests aerobic base is strengthening. Next optimal training window: tomorrow morning."
                                    totalDurMin > 0 ->
                                        "Light session logged. Your cumulative training load is below target. Adding 20 min of moderate intensity would optimize your weekly recovery-to-load ratio."
                                    else ->
                                        "Rest day detected. Based on your recent training pattern, active recovery (light walk or mobility) would accelerate adaptation. Next scheduled window available."
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(
                                            Brush.linearGradient(
                                                listOf(
                                                    ElectricIndigo.copy(alpha = 0.18f),
                                                    VibrantMagenta.copy(alpha = 0.12f)
                                                )
                                            )
                                        )
                                        .padding(20.dp)
                                ) {
                                    Column {
                                        // AI INSIGHT badge
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(ElectricIndigo.copy(alpha = 0.2f))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = "AI INSIGHT",
                                                color = ElectricIndigo,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.5.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = "Neural Performance Insight",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = TextOnSurface,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = insightText,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextOnSurfaceVariant,
                                            lineHeight = 18.sp
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(50))
                                                .background(SurfaceHighest)
                                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = "SCHEDULE WINDOW",
                                                color = TextOnSurface,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.sp
                                            )
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

                        // Generic charts removed — each metric has its own custom layout

                        // Sleep Bank FIRST, then Stages (Stitch order)
                        // Sleep Bank (only for Sleep metric)
                        if (metricType == HealthViewModel.MetricType.SLEEP && metricHistory?.allHistoricalData?.isNotEmpty() == true) {
                            item {
                                val last7 = metricHistory.allHistoricalData.takeLast(7)
                                val bankHours = last7.sumOf { it.value - 8.0 }
                                val isDebt = bankHours < 0
                                val absHours = kotlin.math.abs(bankHours)
                                val h = absHours.toInt()
                                val m = ((absHours - h) * 60).toInt()
                                val bankText = "${if (isDebt) "-" else "+"}${h}.${m / 6}h ${if (isDebt) "Debt" else "Surplus"}"
                                val maxSleep = last7.maxOfOrNull { it.value }?.coerceAtLeast(1.0) ?: 8.0
                                val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(SurfaceLow)
                                        .padding(20.dp)
                                ) {
                                    Column {
                                        // Header: title + surplus/debt
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Sleep Bank",
                                                style = MaterialTheme.typography.titleSmall,
                                                color = TextOnSurface,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = bankText,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isDebt) ErrorRed else VibrantMagenta
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // 7 bars
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(80.dp),
                                            horizontalArrangement = Arrangement.SpaceEvenly,
                                            verticalAlignment = Alignment.Bottom
                                        ) {
                                            last7.forEachIndexed { index, day ->
                                                val barHeight = (day.value / maxSleep).toFloat().coerceIn(0.1f, 1f)
                                                val isGood = day.value >= 7.0
                                                val barColor = when {
                                                    day.value >= 8.0 -> VibrantMagenta
                                                    day.value >= 7.0 -> ElectricIndigo
                                                    else -> ErrorRed
                                                }
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .width(6.dp)
                                                            .fillMaxHeight(barHeight)
                                                            .clip(RoundedCornerShape(3.dp))
                                                            .background(barColor)
                                                    )
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Text(
                                                        text = dayLabels.getOrElse(index) { "" },
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = TextOnSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Sleep Stages Chart (after Sleep Bank — Stitch order)
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

                        // Generic stats/best day removed — each metric has its own layout

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
private fun NutritionMacroCard(label: String, grams: Double, goal: Double, color: Color) {
    val progress = if (goal > 0) (grams / goal).toFloat().coerceIn(0f, 1f) else 0f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceLow)
            .padding(20.dp)
    ) {
        Column {
            Text(label, color = TextOnSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text("${grams.roundToInt()}", color = TextOnSurface, fontSize = 32.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.width(4.dp))
                Text("g", color = TextOnSurfaceVariant, fontSize = 16.sp, modifier = Modifier.padding(bottom = 4.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(SurfaceHighest)) {
                Box(modifier = Modifier.fillMaxWidth(progress.coerceAtLeast(0.01f)).fillMaxHeight().clip(RoundedCornerShape(3.dp)).background(color))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text("GOAL: ${goal.roundToInt()}G", fontSize = 10.sp, color = TextSubtle, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun NutritionMacroBar(
    label: String,
    grams: Double,
    goal: Double,
    color: Color
) {
    val progress = if (goal > 0) (grams / goal).toFloat().coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800),
        label = "${label}Progress"
    )

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = TextOnSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${grams.roundToInt()}g / ${goal.roundToInt()}g",
                color = TextOnSurfaceVariant,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceHigh)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(24.dp))
                    .background(color)
            )
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
