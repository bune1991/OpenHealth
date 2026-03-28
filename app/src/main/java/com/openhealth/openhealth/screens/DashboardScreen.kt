package com.openhealth.openhealth.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.ZoneId
import com.openhealth.openhealth.model.CaloriesData
import com.openhealth.openhealth.model.HealthData
import com.openhealth.openhealth.model.SettingsData
import com.openhealth.openhealth.ui.theme.*
import com.openhealth.openhealth.viewmodel.HealthViewModel
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

// ═══════════════════════════════════════════════════════════
// Electric Nocturne Design System — Dashboard v2.0
// ═══════════════════════════════════════════════════════════

// Readiness Score Data Class
private data class ReadinessScore(
    val score: Int,
    val label: String,
    val color: Color
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

    var selectedTab by rememberSaveable { mutableStateOf(0) }

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
        containerColor = SurfaceLowest
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SurfaceLowest)
                .padding(paddingValues)
        ) {
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 16.dp,
                    bottom = 100.dp  // Space for floating bottom nav
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ─── Header (all tabs) ───
                item {
                    DashboardHeader(
                        dateText = dateText,
                        isToday = isToday,
                        onPreviousDay = onPreviousDay,
                        onNextDay = onNextDay,
                        onToday = onToday,
                        onDateClick = { showDatePicker = true },
                        onSettingsClick = onSettingsClick,
                        onReportsClick = onReportsClick
                    )
                }

                // ═══════════════════════════════════════
                // TAB CONTENT
                // ═══════════════════════════════════════

                when (selectedTab) {
                    // ─── TAB 0: PULSE (Readiness & Daily Snapshot) ───
                    0 -> {
                        item { ReadinessHeroCard(readinessScore = readinessScore, healthData = healthData, onClick = onReadinessClick) }
                        item { RecoveryStatusCard(readinessScore = readinessScore, healthData = healthData) }
                        if (healthData.heartRateVariability.rmssdMs != null) {
                            item { HrvChartCard(healthData = healthData, onClick = { onMetricClick(HealthViewModel.MetricType.HEART_RATE_VARIABILITY) }) }
                        }
                        item { SleepEfficiencyPill(healthData = healthData, onClick = { onMetricClick(HealthViewModel.MetricType.SLEEP) }) }
                        if (settings.aiProvider != com.openhealth.openhealth.model.AiProvider.NONE) {
                            item {
                                NocturneCard(onClick = onAiInsightsClick) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Column {
                                            Text("AI Health Analysis", style = MaterialTheme.typography.titleMedium, color = TextOnSurface, fontWeight = FontWeight.SemiBold)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("Get insights from ${settings.aiProvider.name}", style = MaterialTheme.typography.bodySmall, color = TextOnSurfaceVariant)
                                        }
                                        Box(modifier = Modifier.background(Brush.horizontalGradient(listOf(ElectricIndigo, VibrantMagenta)), RoundedCornerShape(20.dp)).padding(horizontal = 16.dp, vertical = 8.dp)) {
                                            Text("Analyze", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ─── TAB 1: ACTIVITY (Movement & Energy) — Stitch match ───
                    1 -> {
                        // Hero: "Today's Resonance"
                        item {
                            Column {
                                Text("Today's", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = TextOnSurface, letterSpacing = (-1).sp)
                                Text("Resonance", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = ElectricIndigo, letterSpacing = (-1).sp)
                            }
                        }

                        // Activity rings
                        item {
                            val stepsProgress = (healthData.steps.count.toFloat() / settings.stepsGoal).coerceIn(0f, 1f)
                            val calProgress = (healthData.calories.totalBurned.toFloat() / settings.caloriesGoal).coerceIn(0f, 1f)
                            val exerciseMin = healthData.exercise.totalDuration?.toMinutes()?.toInt() ?: 0
                            val exerciseProgress = (exerciseMin / 30f).coerceIn(0f, 1f)

                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                                    // 3 concentric rings
                                    Canvas(modifier = Modifier.size(200.dp)) {
                                        val strokeW = 18.dp.toPx()
                                        // Move ring (outer)
                                        val r1 = size.width / 2 - strokeW / 2
                                        drawCircle(color = SurfaceHighest, radius = r1, style = Stroke(strokeW, cap = StrokeCap.Round))
                                        drawArc(color = ElectricIndigo, startAngle = -90f, sweepAngle = 360f * stepsProgress, useCenter = false, style = Stroke(strokeW, cap = StrokeCap.Round), topLeft = Offset(strokeW / 2, strokeW / 2), size = Size(r1 * 2, r1 * 2))
                                        // Exercise ring (middle)
                                        val r2 = r1 - strokeW - 4.dp.toPx()
                                        val off2 = size.width / 2 - r2
                                        drawCircle(color = SurfaceHighest, radius = r2, style = Stroke(strokeW, cap = StrokeCap.Round))
                                        drawArc(color = VibrantMagenta, startAngle = -90f, sweepAngle = 360f * exerciseProgress, useCenter = false, style = Stroke(strokeW, cap = StrokeCap.Round), topLeft = Offset(off2, off2), size = Size(r2 * 2, r2 * 2))
                                        // Stand ring (inner)
                                        val r3 = r2 - strokeW - 4.dp.toPx()
                                        val off3 = size.width / 2 - r3
                                        drawCircle(color = SurfaceHighest, radius = r3, style = Stroke(strokeW, cap = StrokeCap.Round))
                                        drawArc(color = SoftLavender, startAngle = -90f, sweepAngle = 360f * calProgress, useCenter = false, style = Stroke(strokeW, cap = StrokeCap.Round), topLeft = Offset(off3, off3), size = Size(r3 * 2, r3 * 2))
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("PROGRESS", fontSize = 10.sp, color = TextOnSurfaceVariant, letterSpacing = 2.sp)
                                        Text("${(stepsProgress * 100).roundToInt()}%", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = TextOnSurface)
                                    }
                                }
                            }
                        }

                        // 3 stat cards: Move / Exercise / Stand
                        item {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val exerciseMin = healthData.exercise.totalDuration?.toMinutes()?.toInt() ?: 0
                                listOf(
                                    Triple("MOVE", "${healthData.calories.totalBurned.roundToInt()}", "kcal") to ElectricIndigo,
                                    Triple("EXERCISE", "$exerciseMin", "min") to VibrantMagenta,
                                    Triple("STEPS", "${healthData.steps.count / 1000}k", "steps") to SoftLavender
                                ).forEach { (data, color) ->
                                    Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(24.dp)).background(SurfaceLow).padding(16.dp)) {
                                        Column {
                                            Text(data.first, fontSize = 10.sp, color = TextOnSurfaceVariant, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(verticalAlignment = Alignment.Bottom) {
                                                Text(data.second, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextOnSurface)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(data.third, fontSize = 12.sp, color = TextOnSurfaceVariant, modifier = Modifier.padding(bottom = 2.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Step Intensity energy pill
                        item {
                            val stepProgress = (healthData.steps.count.toFloat() / settings.stepsGoal).coerceIn(0f, 1f)
                            NocturneCard(surfaceColor = SurfaceHigh, onClick = { onMetricClick(HealthViewModel.MetricType.STEPS) }) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                                    Text("Step Intensity", style = MaterialTheme.typography.titleMedium, color = TextOnSurface, fontWeight = FontWeight.Bold)
                                    Text("${healthData.steps.count}", color = ElectricIndigo, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Box(modifier = Modifier.fillMaxWidth().height(20.dp).clip(RoundedCornerShape(10.dp)).background(SurfaceLowest).padding(3.dp)) {
                                    Box(modifier = Modifier.fillMaxWidth(stepProgress).fillMaxHeight().background(Brush.horizontalGradient(listOf(ElectricIndigo, VibrantMagenta)), RoundedCornerShape(8.dp)))
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Goal: ${settings.stepsGoal}", fontSize = 10.sp, color = TextOnSurfaceVariant, letterSpacing = 1.sp)
                                    Text("${(stepProgress * 100).roundToInt()}% Completed", fontSize = 10.sp, color = TextOnSurfaceVariant, letterSpacing = 1.sp)
                                }
                            }
                        }

                        // Distance energy pill
                        val hasDistance = settings.showDistance && healthData.distance.kilometers > 0
                        if (hasDistance) {
                            item {
                                val distProgress = (healthData.distance.kilometers.toFloat() / settings.distanceGoalKm).coerceIn(0f, 1f)
                                NocturneCard(surfaceColor = SurfaceHigh, onClick = { onMetricClick(HealthViewModel.MetricType.DISTANCE) }) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                                        Text("Distance Traveled", style = MaterialTheme.typography.titleMedium, color = TextOnSurface, fontWeight = FontWeight.Bold)
                                        Row(verticalAlignment = Alignment.Bottom) {
                                            Text(String.format("%.1f", healthData.distance.kilometers), color = SoftLavender, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("km", color = TextOnSurfaceVariant, fontSize = 14.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Box(modifier = Modifier.fillMaxWidth().height(20.dp).clip(RoundedCornerShape(10.dp)).background(SurfaceLowest).padding(3.dp)) {
                                        Box(modifier = Modifier.fillMaxWidth(distProgress).fillMaxHeight().background(Brush.horizontalGradient(listOf(SoftLavender, ElectricIndigo)), RoundedCornerShape(8.dp)))
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Goal: ${settings.distanceGoalKm} km", fontSize = 10.sp, color = TextOnSurfaceVariant, letterSpacing = 1.sp)
                                        Text("${(distProgress * 100).roundToInt()}% Completed", fontSize = 10.sp, color = TextOnSurfaceVariant, letterSpacing = 1.sp)
                                    }
                                }
                            }
                        }

                        // Metabolic Insight card
                        item {
                            NocturneCard(surfaceColor = SurfaceHigh) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Box(modifier = Modifier.size(44.dp).background(VibrantMagenta.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Assessment, null, tint = VibrantMagenta, modifier = Modifier.size(22.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Metabolic Efficiency", style = MaterialTheme.typography.titleMedium, color = TextOnSurface, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Your current activity flow is optimizing your resting metabolic rate. Keep this rhythm for best results.", style = MaterialTheme.typography.bodySmall, color = TextOnSurfaceVariant, lineHeight = 18.sp)
                                    }
                                }
                            }
                        }
                    }

                    // ─── TAB 2: VITALS (Biometric Health) — Stitch match ───
                    2 -> {
                        // Hero: "PULSE RHYTHM"
                        item {
                            Column {
                                Text("LIVE STATUS", fontSize = 10.sp, color = ElectricIndigo, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("PULSE", fontSize = 44.sp, fontWeight = FontWeight.ExtraBold, color = TextOnSurface, letterSpacing = (-1).sp)
                                Text("RHYTHM", fontSize = 44.sp, fontWeight = FontWeight.ExtraBold, color = VibrantMagenta, letterSpacing = (-1).sp)
                            }
                        }

                        // Resting HR card with magenta left border
                        item {
                            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(SurfaceLow)) {
                                // Left magenta border
                                Box(modifier = Modifier.width(4.dp).height(80.dp).background(VibrantMagenta).align(Alignment.CenterStart))
                                Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column {
                                        Text("Resting Heart Rate", fontSize = 14.sp, color = TextOnSurfaceVariant)
                                        Row(verticalAlignment = Alignment.Bottom) {
                                            Text(healthData.heartRate.currentBpm?.toString() ?: "--", fontSize = 36.sp, fontWeight = FontWeight.Black, color = TextOnSurface)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("BPM", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = VibrantMagenta, modifier = Modifier.padding(bottom = 4.dp))
                                        }
                                    }
                                    Icon(Icons.Default.Favorite, null, tint = CardHeartRate, modifier = Modifier.size(32.dp).clickable { onMetricClick(HealthViewModel.MetricType.HEART_RATE) })
                                }
                            }
                        }

                        // HRV bento card with bar chart
                        if (healthData.heartRateVariability.rmssdMs != null) {
                            item { HrvChartCard(healthData = healthData, onClick = { onMetricClick(HealthViewModel.MetricType.HEART_RATE_VARIABILITY) }) }
                        }

                        // SpO2 card — gradient magenta background
                        if (healthData.oxygenSaturation.percentage != null) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Brush.linearGradient(listOf(MagentaContainer, SurfaceHigh))).padding(24.dp).clickable { onMetricClick(HealthViewModel.MetricType.OXYGEN_SATURATION) }) {
                                    Column {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("SpO2", color = TextOnSurfaceVariant, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(verticalAlignment = Alignment.Bottom) {
                                            Text("${healthData.oxygenSaturation.percentage?.roundToInt()}", fontSize = 52.sp, fontWeight = FontWeight.Black, color = TextOnSurface)
                                            Text("%", fontSize = 24.sp, color = VibrantMagenta, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Optimal oxygenation detected", style = MaterialTheme.typography.bodySmall, color = TextOnSurfaceVariant)
                                    }
                                }
                            }
                        }

                        // Stress card
                        if (healthData.heartRateVariability.rmssdMs != null) {
                            item {
                                val hrv = healthData.heartRateVariability.rmssdMs!!
                                val stressLevel = ((80.0 - hrv.coerceIn(10.0, 80.0)) / 70.0 * 100).toInt().coerceIn(0, 100)
                                val stressLabel = when { stressLevel < 25 -> "Low"; stressLevel < 50 -> "Moderate"; stressLevel < 75 -> "High"; else -> "Very High" }
                                val stressColor = when { stressLevel < 25 -> SuccessGreen; stressLevel < 50 -> Color(0xFFFFCC00); stressLevel < 75 -> WarningOrange; else -> ErrorRed }
                                StressEnergyCard(stressLevel = stressLevel, stressLabel = stressLabel, stressColor = stressColor, energyPercent = readinessScore.score.coerceIn(0, 100), onClick = onStressClick)
                            }
                        }

                        // Sleep metric card
                        item {
                            val sleepHours = healthData.sleep.totalDuration?.toHours()?.toInt() ?: 0
                            val sleepMinutes = healthData.sleep.totalDuration?.let { ((it.toMinutes() % 60).toInt()) } ?: 0
                            MetricCard(title = "Sleep", value = if (sleepHours > 0 || sleepMinutes > 0) "${sleepHours}h ${sleepMinutes}m" else "--", unit = "", icon = Icons.Default.NightsStay, accentColor = CardSleep, sparklineData = generateSleepSparklineData(sleepHours * 60 + sleepMinutes), onClick = { onMetricClick(HealthViewModel.MetricType.SLEEP) })
                        }

                        // Vitals detail card
                        val hasHRV = settings.showHRV && healthData.heartRateVariability.rmssdMs != null
                        val hasBloodOxygen = settings.showOxygenSaturation && healthData.oxygenSaturation.percentage != null
                        val hasBloodPressure = settings.showBloodPressure && healthData.bloodPressure.systolicMmHg != null
                        val hasBodyTemp = settings.showBodyTemperature && healthData.bodyTemperature.temperatureCelsius != null
                        val hasRespiratoryRate = settings.showRespiratoryRate && healthData.respiratoryRate.ratePerMinute != null
                        val hasSkinTemp = settings.showSkinTemperature && healthData.skinTemperature.temperatureCelsius != null
                        val hasBloodGlucose = settings.showBloodGlucose && healthData.bloodGlucose.levelMgPerDl != null
                        val hasAnyVitals = hasHRV || hasBloodOxygen || hasBloodPressure || hasBodyTemp || hasRespiratoryRate || hasSkinTemp || hasBloodGlucose
                        if (hasAnyVitals) {
                            item { VitalsCard(healthData = healthData, hasHRV = hasHRV, hasBloodOxygen = hasBloodOxygen, hasBloodPressure = hasBloodPressure, hasBodyTemp = hasBodyTemp, hasRespiratoryRate = hasRespiratoryRate, hasSkinTemp = hasSkinTemp, hasBloodGlucose = hasBloodGlucose, expanded = vitalsExpanded, onExpandedChange = onVitalsExpandedChange, onMetricClick = onMetricClick) }
                        }

                        // Body composition
                        val hasWeight = settings.showWeight && healthData.weight.kilograms != null
                        val hasBodyFat = settings.showBodyFat && healthData.bodyFat.percentage != null
                        val hasBMR = settings.showBMR && healthData.basalMetabolicRate.caloriesPerDay != null
                        val hasBodyWater = settings.showBodyWater && healthData.bodyWaterMass.kilograms != null
                        val hasBoneMass = settings.showBoneMass && healthData.boneMass.kilograms != null
                        val hasLeanMass = settings.showLeanBodyMass && healthData.leanBodyMass.kilograms != null
                        val hasAnyBody = hasWeight || hasBodyFat || hasBMR || hasBodyWater || hasBoneMass || hasLeanMass
                        if (hasAnyBody) {
                            item { BodyCompositionCard(healthData = healthData, hasWeight = hasWeight, hasBodyFat = hasBodyFat, hasBMR = hasBMR, hasBodyWater = hasBodyWater, hasBoneMass = hasBoneMass, hasLeanMass = hasLeanMass, expanded = bodyExpanded, onExpandedChange = onBodyExpandedChange, onMetricClick = onMetricClick) }
                        }
                    }

                    // ─── TAB 3: PROGRESS (Trends & Milestones) — Stitch match ───
                    3 -> {
                        // Hero: Daily Consistency
                        item {
                            val consistency = readinessScore.score.coerceIn(0, 100)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                                Column {
                                    Text("DAILY CONSISTENCY", fontSize = 10.sp, color = TextOnSurfaceVariant, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("$consistency%", fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, color = TextOnSurface)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(modifier = Modifier.background(Brush.horizontalGradient(listOf(ElectricIndigo, VibrantMagenta)), RoundedCornerShape(12.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                                            Text("ACTIVE ZONE", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                        }
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Assessment, null, tint = SuccessGreen, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("+14%", color = SuccessGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Streak + stats row
                        item {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Streak
                                Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(24.dp)).background(SurfaceLow).padding(16.dp)) {
                                    Column {
                                        Text("STREAK", fontSize = 9.sp, color = ElectricIndigo, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(if (stepsStreak > 0) "$stepsStreak" else "--", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextOnSurface)
                                        Text("Days", fontSize = 11.sp, color = TextOnSurfaceVariant)
                                    }
                                }
                                // Status
                                Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(24.dp)).background(SurfaceLow).padding(16.dp)) {
                                    Column {
                                        Text("STATUS", fontSize = 9.sp, color = VibrantMagenta, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            when {
                                                readinessScore.score >= 80 -> "Elite"
                                                readinessScore.score >= 60 -> "Strong"
                                                readinessScore.score >= 40 -> "Building"
                                                else -> "Starting"
                                            },
                                            fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextOnSurface
                                        )
                                    }
                                }
                                // Avg Steps
                                Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(24.dp)).background(SurfaceLow).padding(16.dp)) {
                                    Column {
                                        Text("AVG STEPS", fontSize = 9.sp, color = SoftLavender, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        val avgK = if (healthData.steps.count > 0) String.format("%.1fk", healthData.steps.count / 1000f) else "--"
                                        Text(avgK, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextOnSurface)
                                    }
                                }
                            }
                        }

                        // Step Trends — density circle
                        item {
                            NocturneCard(surfaceColor = SurfaceLow) {
                                Text("Step Trends", style = MaterialTheme.typography.titleMedium, color = TextOnSurface, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Info text
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Density", fontSize = 12.sp, color = TextOnSurfaceVariant)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Peak activity times are clustered in the morning hours", fontSize = 12.sp, color = TextSubtle, lineHeight = 16.sp)
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    // Density gauge circle
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                                        Canvas(modifier = Modifier.size(80.dp)) {
                                            val strokeW = 8.dp.toPx()
                                            drawCircle(color = SurfaceHighest, radius = size.minDimension / 2 - strokeW / 2, style = Stroke(strokeW))
                                            drawArc(
                                                brush = Brush.sweepGradient(listOf(ElectricIndigo, VibrantMagenta, ElectricIndigo)),
                                                startAngle = -90f, sweepAngle = 270f,
                                                useCenter = false,
                                                style = Stroke(strokeW, cap = StrokeCap.Round),
                                                topLeft = Offset(strokeW / 2, strokeW / 2),
                                                size = Size(size.width - strokeW, size.height - strokeW)
                                            )
                                        }
                                        val avgStepsK = if (healthData.steps.count > 0) String.format("%.1f", healthData.steps.count / 1000f) else "0"
                                        Text(avgStepsK, fontSize = 18.sp, fontWeight = FontWeight.Black, color = TextOnSurface)
                                    }
                                }
                            }
                        }

                        // Milestones section
                        item {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Milestones", style = MaterialTheme.typography.titleMedium, color = TextOnSurface, fontWeight = FontWeight.Bold)
                                Text("View Gallery", color = ElectricIndigo, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Weekly Insight card with gradient border
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(SurfaceLow)
                                    .padding(20.dp)
                                    .clickable { onReportsClick() }
                            ) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Box(modifier = Modifier.size(44.dp).background(ElectricIndigo.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Assessment, null, tint = ElectricIndigo, modifier = Modifier.size(22.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Weekly Insight", style = MaterialTheme.typography.titleMedium, color = TextOnSurface, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "Your training consistency has increased by 14%. To optimize recovery, consider shifting your high-impact sessions to midweek.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextOnSurfaceVariant,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Weather
                        if (weatherData.isAvailable) {
                            item { WeatherCard(weatherData = weatherData) }
                        }
                    }
                }

                // Bottom spacing for nav bar
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

            // ─── Floating Bottom Nav Bar ───
            FloatingBottomNavBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Header
// ═══════════════════════════════════════════════════════════

@Composable
private fun DashboardHeader(
    dateText: String,
    isToday: Boolean,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onToday: () -> Unit,
    onDateClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onReportsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Navigation arrow + App title
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPreviousDay, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowLeft,
                    contentDescription = "Previous Day",
                    tint = TextOnSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column(modifier = Modifier.clickable(onClick = onDateClick)) {
                Text(
                    text = "OpenHealth",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextOnSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextOnSurfaceVariant
                )
            }
        }

        // Right: Actions
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!isToday) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(ElectricIndigo.copy(alpha = 0.15f))
                        .clickable { onToday() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Today",
                        color = ElectricIndigo,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }
            IconButton(
                onClick = onNextDay,
                enabled = !isToday,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                    contentDescription = "Next Day",
                    tint = if (isToday) TextSubtle else TextOnSurfaceVariant
                )
            }
            IconButton(onClick = onReportsClick, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.Assessment,
                    contentDescription = "Reports",
                    tint = TextOnSurfaceVariant
                )
            }
            IconButton(onClick = onSettingsClick, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = TextOnSurfaceVariant
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Readiness Hero Card — Arc gauge with hero number
// ═══════════════════════════════════════════════════════════

@Composable
private fun ReadinessHeroCard(
    readinessScore: ReadinessScore,
    healthData: HealthData,
    onClick: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 4 }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Readiness ring with outer glow
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(240.dp)
                ) {
                    val animatedScore by animateFloatAsState(
                        targetValue = readinessScore.score / 100f,
                        animationSpec = tween(1200),
                        label = "readiness_arc"
                    )
                    val animatedNumber by animateIntAsState(
                        targetValue = readinessScore.score,
                        animationSpec = tween(1000),
                        label = "readiness_number"
                    )

                    Canvas(modifier = Modifier.size(240.dp)) {
                        val strokeWidth = 12.dp.toPx()
                        val arcSize = Size(size.width - strokeWidth * 2, size.height - strokeWidth * 2)
                        val topLeft = Offset(strokeWidth, strokeWidth)

                        // Outer glow
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    ElectricIndigo.copy(alpha = 0.1f),
                                    Color.Transparent
                                )
                            ),
                            radius = size.minDimension / 2
                        )

                        // Background ring (full 360°)
                        drawArc(
                            color = SurfaceHighest,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                            topLeft = topLeft,
                            size = arcSize
                        )

                        // Progress ring (full circle based on score)
                        drawArc(
                            color = ElectricIndigo,
                            startAngle = -90f,
                            sweepAngle = 360f * animatedScore,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                            topLeft = topLeft,
                            size = arcSize
                        )
                    }

                    // Center content
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "READINESS",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextOnSurfaceVariant,
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = animatedNumber.toString(),
                            fontSize = 72.sp,
                            fontWeight = FontWeight.Black,
                            color = TextOnSurface,
                            letterSpacing = (-2).sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = ElectricIndigo,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = readinessScore.label.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricIndigo,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Recovery Status Card
// ═══════════════════════════════════════════════════════════

@Composable
private fun RecoveryStatusCard(
    readinessScore: ReadinessScore,
    healthData: HealthData
) {
    val recoveryLabel = when {
        readinessScore.score >= 80 -> "Peak"
        readinessScore.score >= 60 -> "Good"
        readinessScore.score >= 40 -> "Moderate"
        else -> "Low"
    }

    val recoveryDescription = when {
        readinessScore.score >= 80 -> "Your nervous system is primed for high-intensity training today."
        readinessScore.score >= 60 -> "Recovery is progressing well. Moderate activity recommended."
        readinessScore.score >= 40 -> "Your body is still recovering. Light activity is best today."
        else -> "Focus on rest and recovery today. Your body needs time to restore."
    }

    // Full magenta background card — exact Stitch design
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MagentaContainer)
            .padding(24.dp)
    ) {
        // Blurred white circle overlay (top-right)
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.08f),
                radius = 120.dp.toPx(),
                center = Offset(size.width + 20.dp.toPx(), -20.dp.toPx())
            )
        }

        Column {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFFFFF5F9),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Recovery Status",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFFFF5F9),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = recoveryDescription,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFFFF5F9).copy(alpha = 0.8f),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Bottom: "Peak" label + percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = recoveryLabel,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFF5F9),
                        letterSpacing = (-1).sp
                    )
                    Text(
                        text = "${readinessScore.score}% RESTORED",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFFF5F9).copy(alpha = 0.6f),
                        letterSpacing = 1.sp
                    )
                }

                // Trending icon circle
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(Color(0xFFFFF5F9), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = null,
                        tint = MagentaContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// HRV Bar Chart Card — weekly bars with day labels
// ═══════════════════════════════════════════════════════════

@Composable
private fun HrvChartCard(
    healthData: HealthData,
    onClick: () -> Unit
) {
    val hrvValue = healthData.heartRateVariability.rmssdMs
    if (hrvValue == null) return

    NocturneCard(
        onClick = onClick,
        surfaceColor = SurfaceHigh
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = ElectricIndigo,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Heart Rate\nVariability",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextOnSurface,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 22.sp
                )
            }
            Text(
                text = "${String.format("%.0f", hrvValue)} ms",
                style = MaterialTheme.typography.bodyMedium,
                color = TextOnSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bar chart — 7 bars for week
        val barHeights = listOf(0.4f, 0.55f, 0.7f, 0.95f, 0.6f, 0.45f, 0.5f)
        val dayLabels = listOf("Mon", "Tue", "Wed", "Today", "Fri", "Sat", "Sun")
        val todayIndex = 3

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            barHeights.forEachIndexed { index, height ->
                val isToday = index == todayIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 3.dp)
                        .fillMaxHeight(height)
                        .background(
                            if (isToday) ElectricIndigo else ElectricIndigo.copy(alpha = 0.2f),
                            RoundedCornerShape(50)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Day labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            dayLabels.forEachIndexed { index, label ->
                Text(
                    text = label,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (index == todayIndex) ElectricIndigo else TextSubtle,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Sleep Efficiency Pill — full width, Stitch "Energy Pill"
// ═══════════════════════════════════════════════════════════

@Composable
private fun SleepEfficiencyPill(
    healthData: HealthData,
    onClick: () -> Unit
) {
    val sleepHours = healthData.sleep.totalDuration?.toHours()?.toInt() ?: 0
    val sleepMinutes = healthData.sleep.totalDuration?.let { ((it.toMinutes() % 60).toInt()) } ?: 0
    if (sleepHours == 0 && sleepMinutes == 0) return

    val efficiency = ((sleepHours * 60 + sleepMinutes).toFloat() / 480f * 100).toInt().coerceIn(0, 100)
    val deepSleep = healthData.sleep.sessions.filter { it.stage == com.openhealth.openhealth.model.SleepStage.DEEP }.sumOf { it.duration.toMinutes() }.toInt()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(SurfaceLow)
            .clickable(onClick = onClick)
            .padding(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon circle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(SurfaceHighest, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NightsStay,
                    contentDescription = null,
                    tint = SoftLavender,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Sleep Efficiency",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextOnSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Deep Sleep: ${deepSleep / 60}h ${deepSleep % 60}m",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextOnSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Percentage
            Text(
                text = "$efficiency%",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = VibrantMagenta
            )
        }

        // Gradient progress bar at bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 4.dp)
                .align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(SurfaceHighest, RoundedCornerShape(2.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(efficiency / 100f)
                        .height(4.dp)
                        .background(
                            Brush.horizontalGradient(listOf(ElectricIndigo, VibrantMagenta)),
                            RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Weather Card
// ═══════════════════════════════════════════════════════════

@Composable
private fun WeatherCard(weatherData: com.openhealth.openhealth.utils.WeatherData) {
    NocturneCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${String.format("%.0f", weatherData.temperature)}°",
                    color = TextOnSurface,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    val uvColor = when(weatherData.uvLabel) { "Low" -> SuccessGreen; "Moderate" -> Color(0xFFFFCC00); else -> ErrorRed }
                    val aqiColor = when(weatherData.aqi) { 1, 2 -> SuccessGreen; 3 -> Color(0xFFFFCC00); else -> ErrorRed }
                    Text(text = "UV ${String.format("%.0f", weatherData.uvIndex)} (${weatherData.uvLabel})", color = uvColor, fontSize = 13.sp)
                    Text(text = "Air: ${weatherData.aqiLabel}", color = aqiColor, fontSize = 13.sp)
                }
            }
        }
        if (weatherData.healthAdvisory != "Good conditions for outdoor activity") {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = weatherData.healthAdvisory,
                color = WarningOrange,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Metric Card — Core metrics (Steps, HR, Sleep, Calories)
// ═══════════════════════════════════════════════════════════

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
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 3 }
    ) {
        NocturneCard(onClick = onClick) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Icon + title row
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(accentColor.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextOnSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Animated number
                    val targetNumber = value.filter { it.isDigit() }.toIntOrNull() ?: 0
                    val animatedNumber by animateIntAsState(
                        targetValue = targetNumber,
                        animationSpec = tween(durationMillis = 800),
                        label = "counter"
                    )
                    val displayValue = if (targetNumber > 0) value.replace(targetNumber.toString(), animatedNumber.toString()) else value

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = displayValue,
                            style = MaterialTheme.typography.displaySmall,
                            color = TextOnSurface,
                            fontWeight = FontWeight.Bold
                        )
                        if (unit.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = unit,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSubtle,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }

                    if (subtitle.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = accentColor.copy(alpha = 0.8f)
                        )
                    }
                }

                // Mini bar chart
                MiniBarChart(
                    data = sparklineData,
                    barColor = accentColor,
                    modifier = Modifier
                        .width(80.dp)
                        .height(44.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Stress & Energy Card
// ═══════════════════════════════════════════════════════════

@Composable
private fun StressEnergyCard(
    stressLevel: Int,
    stressLabel: String,
    stressColor: Color,
    energyPercent: Int,
    onClick: () -> Unit = {}
) {
    NocturneCard(onClick = onClick) {
        Text(
            text = "Stress & Energy",
            style = MaterialTheme.typography.titleMedium,
            color = TextOnSurface,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(text = "Stress", color = TextOnSurfaceVariant, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(text = "$stressLevel", color = stressColor, fontWeight = FontWeight.Bold, fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = stressLabel, color = stressColor, fontSize = 14.sp, modifier = Modifier.padding(bottom = 4.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Energy bar
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Energy", color = TextOnSurfaceVariant, fontSize = 13.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .background(SurfaceHigh, RoundedCornerShape(4.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(energyPercent / 100f)
                        .height(8.dp)
                        .background(
                            Brush.horizontalGradient(listOf(ElectricIndigo, SuccessGreen)),
                            RoundedCornerShape(4.dp)
                        )
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "$energyPercent%", color = TextOnSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Detail Card (Exercise, Distance, Floors, Nutrition, etc.)
// ═══════════════════════════════════════════════════════════

@Composable
private fun DetailCard(
    title: String,
    value: String,
    progress: Float? = null,
    statusColor: Color? = null,
    sparklineData: List<Float>? = null,
    sparklineColor: Color = ElectricIndigo,
    onClick: (() -> Unit)? = null
) {
    NocturneCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextOnSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (statusColor != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(statusColor, CircleShape)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextOnSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                if (progress != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(SurfaceHigh, RoundedCornerShape(3.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress.coerceIn(0f, 1f))
                                .height(6.dp)
                                .background(
                                    Brush.horizontalGradient(listOf(ElectricIndigo, SoftLavender)),
                                    RoundedCornerShape(3.dp)
                                )
                        )
                    }
                }
            }
            if (sparklineData != null && sparklineData.size >= 2) {
                Spacer(modifier = Modifier.width(12.dp))
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
                    drawPath(path = path, color = sparklineColor, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
                }
            }
            if (onClick != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View details",
                    tint = TextSubtle,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Body Composition Card (collapsible)
// ═══════════════════════════════════════════════════════════

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
    NocturneCard {
        Column(modifier = Modifier.animateContentSize(animationSpec = tween(200))) {
            // Header
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
                    color = TextOnSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextSubtle
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Summary — always visible
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (hasWeight) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = String.format("%.1f", healthData.weight.kilograms), color = TextOnSurface, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        Text(text = "kg", color = TextSubtle, fontSize = 12.sp)
                    }
                }
                if (hasBodyFat) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = String.format("%.1f", healthData.bodyFat.percentage), color = TextOnSurface, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        Text(text = "% fat", color = TextSubtle, fontSize = 12.sp)
                    }
                }
                if (hasLeanMass) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = String.format("%.1f", healthData.leanBodyMass.kilograms), color = TextOnSurface, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        Text(text = "kg lean", color = TextSubtle, fontSize = 12.sp)
                    }
                }
            }

            // Expanded details
            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))

                if (hasWeight) BodyMetricRow("Weight", String.format("%.1f kg", healthData.weight.kilograms)) { onMetricClick(HealthViewModel.MetricType.WEIGHT) }
                if (hasBodyFat) BodyMetricRow("Body Fat", String.format("%.1f%%", healthData.bodyFat.percentage)) { onMetricClick(HealthViewModel.MetricType.BODY_FAT) }
                if (hasBMR) BodyMetricRow("BMR", String.format("%.0f kcal", healthData.basalMetabolicRate.caloriesPerDay)) { onMetricClick(HealthViewModel.MetricType.BASAL_METABOLIC_RATE) }
                if (hasBodyWater) BodyMetricRow("Body Water", String.format("%.1f kg", healthData.bodyWaterMass.kilograms)) { onMetricClick(HealthViewModel.MetricType.BODY_WATER_MASS) }
                if (hasBoneMass) BodyMetricRow("Bone Mass", String.format("%.1f kg", healthData.boneMass.kilograms)) { onMetricClick(HealthViewModel.MetricType.BONE_MASS) }
                if (hasLeanMass) BodyMetricRow("Lean Mass", String.format("%.1f kg", healthData.leanBodyMass.kilograms)) { onMetricClick(HealthViewModel.MetricType.LEAN_BODY_MASS) }
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
        Text(text = label, color = TextOnSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = value, color = TextOnSurface, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = TextSubtle, modifier = Modifier.size(18.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Vitals Card (collapsible)
// ═══════════════════════════════════════════════════════════

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
    val hrvCheck = if (hasHRV) (healthData.heartRateVariability.avgMs ?: healthData.heartRateVariability.rmssdMs!!) >= 30 else true
    val spo2Check = if (hasBloodOxygen) (healthData.oxygenSaturation.avgPercentage ?: healthData.oxygenSaturation.percentage!!) >= 95 else true
    val rrCheck = if (hasRespiratoryRate) (healthData.respiratoryRate.avgRate ?: healthData.respiratoryRate.ratePerMinute!!) in 12.0..20.0 else true
    val bpCheck = if (hasBloodPressure) healthData.bloodPressure.systolicMmHg!! in 90.0..140.0 else true
    val bgCheck = if (hasBloodGlucose) healthData.bloodGlucose.levelMgPerDl!! in 60.0..140.0 else true
    val btCheck = if (hasBodyTemp) healthData.bodyTemperature.temperatureCelsius!! in 35.5..38.0 else true
    val allNormal = hrvCheck && spo2Check && rrCheck && bpCheck && bgCheck && btCheck
    val statusColor = if (allNormal) SuccessGreen else WarningOrange
    val statusText = if (allNormal) "All normal" else "Needs attention"

    NocturneCard {
        Column(modifier = Modifier.animateContentSize(animationSpec = tween(200))) {
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
                        color = TextOnSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(text = statusText, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextSubtle
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Summary — top 3 vitals
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (hasHRV) {
                    val hrvDisplay = healthData.heartRateVariability.avgMs ?: healthData.heartRateVariability.rmssdMs!!
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = String.format("%.0f", hrvDisplay), color = TextOnSurface, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        Text(text = "ms HRV", color = TextSubtle, fontSize = 12.sp)
                    }
                }
                if (hasBloodOxygen) {
                    val spo2Display = healthData.oxygenSaturation.avgPercentage ?: healthData.oxygenSaturation.percentage!!
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = String.format("%.0f%%", spo2Display), color = TextOnSurface, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        Text(text = "SpO2", color = TextSubtle, fontSize = 12.sp)
                    }
                }
                if (hasRespiratoryRate) {
                    val rrDisplay = healthData.respiratoryRate.avgRate ?: healthData.respiratoryRate.ratePerMinute!!
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = String.format("%.0f", rrDisplay), color = TextOnSurface, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        Text(text = "breaths", color = TextSubtle, fontSize = 12.sp)
                    }
                }
            }

            // Expanded details
            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))

                if (hasHRV) {
                    val hrvAvg = healthData.heartRateVariability.avgMs ?: healthData.heartRateVariability.rmssdMs!!
                    val dot = when { hrvAvg >= 30 -> SuccessGreen; hrvAvg >= 20 -> WarningOrange; else -> ErrorRed }
                    VitalMetricRow("Heart Rate Variability", String.format("%.0f ms", hrvAvg), dot) { onMetricClick(HealthViewModel.MetricType.HEART_RATE_VARIABILITY) }
                }
                if (hasBloodOxygen) {
                    val spo2Avg = healthData.oxygenSaturation.avgPercentage ?: healthData.oxygenSaturation.percentage!!
                    val dot = when { spo2Avg >= 95 -> SuccessGreen; spo2Avg >= 90 -> WarningOrange; else -> ErrorRed }
                    VitalMetricRow("Blood Oxygen", String.format("%.0f%%", spo2Avg), dot) { onMetricClick(HealthViewModel.MetricType.OXYGEN_SATURATION) }
                }
                if (hasBloodGlucose) {
                    val bg = healthData.bloodGlucose.levelMgPerDl!!
                    val dot = when { bg in 70.0..100.0 -> SuccessGreen; bg in 60.0..140.0 -> WarningOrange; else -> ErrorRed }
                    VitalMetricRow("Blood Glucose", String.format("%.0f mg/dL", bg), dot) { onMetricClick(HealthViewModel.MetricType.BLOOD_GLUCOSE) }
                }
                if (hasBloodPressure) {
                    val sys = healthData.bloodPressure.systolicMmHg!!
                    val dot = when { sys in 90.0..120.0 -> SuccessGreen; sys in 80.0..140.0 -> WarningOrange; else -> ErrorRed }
                    VitalMetricRow("Blood Pressure", String.format("%.0f/%.0f mmHg", sys, healthData.bloodPressure.diastolicMmHg), dot) { onMetricClick(HealthViewModel.MetricType.BLOOD_PRESSURE) }
                }
                if (hasBodyTemp) {
                    val temp = healthData.bodyTemperature.temperatureCelsius!!
                    val dot = when { temp in 36.1..37.2 -> SuccessGreen; temp in 35.5..38.0 -> WarningOrange; else -> ErrorRed }
                    VitalMetricRow("Body Temperature", String.format("%.1f°C", temp), dot) { onMetricClick(HealthViewModel.MetricType.BODY_TEMPERATURE) }
                }
                if (hasRespiratoryRate) {
                    val rrAvg = healthData.respiratoryRate.avgRate ?: healthData.respiratoryRate.ratePerMinute!!
                    val dot = when { rrAvg in 12.0..20.0 -> SuccessGreen; rrAvg in 8.0..25.0 -> WarningOrange; else -> ErrorRed }
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
            Text(text = label, color = TextOnSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
            if (statusDot != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.size(8.dp).background(statusDot, CircleShape))
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = value, color = TextOnSurface, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = TextSubtle, modifier = Modifier.size(18.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Floating Bottom Nav Bar — pill-shaped, glassmorphism
// ═══════════════════════════════════════════════════════════

@Composable
private fun FloatingBottomNavBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(SurfaceLow.copy(alpha = 0.85f))
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = "bolt",
                label = "Pulse",
                isActive = selectedTab == 0,
                onClick = { onTabSelected(0) }
            )
            BottomNavItem(
                icon = "fitness",
                label = "Activity",
                isActive = selectedTab == 1,
                onClick = { onTabSelected(1) }
            )
            BottomNavItem(
                icon = "heart",
                label = "Vitals",
                isActive = selectedTab == 2,
                onClick = { onTabSelected(2) }
            )
            BottomNavItem(
                icon = "chart",
                label = "Progress",
                isActive = selectedTab == 3,
                onClick = { onTabSelected(3) }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: String,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val iconVector = when (icon) {
        "bolt" -> Icons.Default.Favorite  // Readiness
        "fitness" -> Icons.AutoMirrored.Filled.DirectionsWalk  // Activity
        "heart" -> Icons.Default.Favorite  // Vitals
        "chart" -> Icons.Default.Assessment  // Progress
        else -> Icons.Default.Favorite
    }

    if (isActive) {
        // Active: gradient pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(ElectricIndigoDim, VibrantMagenta)
                    )
                )
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = label.uppercase(),
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    } else {
        // Inactive
        Box(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = label,
                    tint = TextOnSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = label.uppercase(),
                    color = TextOnSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Base Card — "NocturneCard" — the design system primitive
// No borders. Depth through surface tiers. Full rounding.
// ═══════════════════════════════════════════════════════════

@Composable
private fun NocturneCard(
    modifier: Modifier = Modifier,
    surfaceColor: Color = SurfaceMid,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(surfaceColor)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Mini Bar Chart
// ═══════════════════════════════════════════════════════════

@Composable
private fun MiniBarChart(
    data: List<Float>,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    val barWidth = 8.dp
    val gap = 3.dp
    val cornerRadius = 4.dp

    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas

        val canvasWidth = size.width
        val canvasHeight = size.height
        val barWidthPx = barWidth.toPx()
        val gapPx = gap.toPx()
        val cornerRadiusPx = cornerRadius.toPx()

        val totalBarsWidth = data.size * barWidthPx
        val totalGapsWidth = (data.size - 1) * gapPx
        val totalContentWidth = totalBarsWidth + totalGapsWidth
        val startX = (canvasWidth - totalContentWidth) / 2

        val maxValue = data.maxOrNull()?.coerceAtLeast(0.1f) ?: 1f

        data.forEachIndexed { index, value ->
            val normalizedHeight = if (value <= 0f) 0.1f
            else (value / maxValue).coerceIn(0.1f, 1f)

            val barHeight = normalizedHeight * canvasHeight
            val x = startX + index * (barWidthPx + gapPx)
            val y = canvasHeight - barHeight

            drawRoundRect(
                color = if (index == data.size - 1) barColor else barColor.copy(alpha = 0.4f),
                topLeft = Offset(x, y),
                size = Size(barWidthPx, barHeight),
                cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Readiness Score Calculation
// ═══════════════════════════════════════════════════════════

private fun calculateReadinessScore(healthData: HealthData): ReadinessScore {
    var score = 100
    val now = java.time.Instant.now()

    val hoursSinceLastSleep = healthData.sleep.sessions.maxByOrNull { it.endTime }?.endTime?.let { lastWakeTime ->
        java.time.Duration.between(lastWakeTime, now).toHours()
    }

    val awakePenalty = when {
        hoursSinceLastSleep == null -> 50
        hoursSinceLastSleep >= 16 -> 90
        hoursSinceLastSleep >= 14 -> 85
        hoursSinceLastSleep >= 12 -> 75
        hoursSinceLastSleep >= 10 -> 65
        hoursSinceLastSleep >= 8 -> 25
        hoursSinceLastSleep >= 2 -> 5
        else -> 0
    }
    score -= awakePenalty

    healthData.sleep.totalDuration?.let { sleepDuration ->
        val sleepHours = sleepDuration.toHours()
        when {
            sleepHours >= 7 -> { }
            sleepHours >= 5 -> score -= 10
            else -> score -= 20
        }
    } ?: run { score -= 15 }

    healthData.restingHeartRate.bpm?.let { rhr ->
        when {
            rhr <= 75 -> { }
            rhr <= 85 -> score -= 5
            else -> score -= 10
        }
    } ?: healthData.heartRate.restingBpm?.let { rhr ->
        when {
            rhr <= 75 -> { }
            rhr <= 85 -> score -= 5
            else -> score -= 10
        }
    }

    healthData.heartRateVariability.rmssdMs?.let { hrv ->
        when {
            hrv >= 40 -> { }
            hrv >= 30 -> score -= 3
            else -> score -= 8
        }
    }

    val steps = healthData.steps.count
    if (steps < 1000) score -= 2

    score = score.coerceIn(5, 100)

    val (label, color) = when {
        score >= 81 -> "Excellent" to SuccessGreen
        score >= 61 -> "Good" to SuccessGreen
        score >= 41 -> "Fair" to WarningOrange
        score >= 21 -> "Poor" to ErrorRed
        else -> "Exhausted" to ErrorRed
    }

    return ReadinessScore(score, label, color)
}

// ═══════════════════════════════════════════════════════════
// Sparkline Data Generators
// ═══════════════════════════════════════════════════════════

private fun generateSparklineData(current: Long, max: Long): List<Float> {
    val todayValue = (current.toFloat() / max).coerceIn(0f, 1f)
    val previousDays = List(6) { index ->
        val variation = when (index % 3) { 0 -> 0.8f; 1 -> 1.2f; else -> 1.0f }
        val value = todayValue * variation
        if (todayValue <= 0f) 0f else value.coerceIn(0f, 1f)
    }
    return previousDays + listOf(todayValue)
}

private fun generateHeartRateSparklineData(baseBpm: Int): List<Float> {
    val todayValue = (baseBpm.toFloat() / 120f).coerceIn(0f, 1f)
    val previousDays = List(6) { index ->
        val variation = when (index % 3) { 0 -> 0.9f; 1 -> 1.1f; else -> 1.0f }
        val value = todayValue * variation
        if (baseBpm <= 0) 0f else value.coerceIn(0f, 1f)
    }
    return previousDays + listOf(todayValue)
}

private fun generateSleepSparklineData(totalMinutes: Int): List<Float> {
    val todayValue = (totalMinutes.toFloat() / 480f).coerceIn(0f, 1f)
    val previousDays = List(6) { index ->
        val variation = when (index % 3) { 0 -> 0.7f; 1 -> 1.3f; else -> 1.0f }
        val value = todayValue * variation
        if (totalMinutes <= 0) 0f else value.coerceIn(0f, 1f)
    }
    return previousDays + listOf(todayValue)
}

private fun generateVariationSparkline(baseValue: Float, points: Int, variance: Float): List<Float> {
    val random = java.util.Random(baseValue.toLong())
    return (0 until points).map {
        val variation = 1f + (random.nextFloat() * 2 - 1) * variance
        baseValue * variation
    }
}
