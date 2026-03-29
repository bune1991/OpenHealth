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
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Restaurant
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
    onHydrationClick: () -> Unit = {},
    onPerformanceClick: () -> Unit = {},
    selectedTab: Int = 0,
    onTabChanged: (Int) -> Unit = {},
    hydrationDailyTotalMl: Int = 0,
    onSessionClick: (com.openhealth.openhealth.model.ExerciseSession) -> Unit = {},
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
    val c = LocalAppColors.current
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

    // selectedTab is now controlled by ViewModel via parameters

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
        containerColor = c.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(c.background)
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
                        // ── 1. Readiness Ring with MASSIVE pulsing glow ──
                        item { ReadinessHeroCard(readinessScore = readinessScore, healthData = healthData, onClick = onReadinessClick) }

                        // ── 2. Recovery Status Card — FULL MAGENTA ──
                        item {
                            val recoveryLabel = when {
                                readinessScore.score >= 80 -> "PEAK"
                                readinessScore.score >= 60 -> "GOOD"
                                readinessScore.score >= 40 -> "FAIR"
                                else -> "LOW"
                            }
                            val recoverySubtitle = when {
                                readinessScore.score >= 80 -> "System performance at maximum"
                                readinessScore.score >= 60 -> "Body recovery progressing well"
                                readinessScore.score >= 40 -> "Recovery still in progress"
                                else -> "Your body needs rest today"
                            }

                            Box(modifier = Modifier.fillMaxWidth()) {
                                // Magenta glow behind card
                                Canvas(modifier = Modifier.matchParentSize()) {
                                    drawCircle(
                                        color = c.secondary.copy(alpha = 0.12f),
                                        radius = size.width * 0.55f,
                                        center = Offset(size.width * 0.5f, size.height * 0.5f)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(c.secondary)
                                        .bounceClick { onReadinessClick() }
                                        .padding(24.dp)
                                ) {
                                    // Semi-transparent bolt icon on the right
                                    Icon(
                                        Icons.Filled.Bolt,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.15f),
                                        modifier = Modifier
                                            .size(80.dp)
                                            .align(Alignment.CenterEnd)
                                    )

                                    Column {
                                        Text(
                                            "Recovery Status",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White.copy(alpha = 0.85f),
                                            letterSpacing = 1.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            recoverySubtitle,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        // PEAK badge pill
                                        Box(
                                            modifier = Modifier
                                                .background(Color.White, RoundedCornerShape(20.dp))
                                                .padding(horizontal = 14.dp, vertical = 5.dp)
                                        ) {
                                            Text(
                                                recoveryLabel,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Black,
                                                color = c.secondary,
                                                letterSpacing = 2.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // ── 3. Vitals Grid — SQUARE cards with glows ──
                        item {
                            val restingHr = healthData.heartRate.minBpm
                            val hrvVal = healthData.heartRateVariability.rmssdMs

                            // Animated count-up values
                            val animatedRestingHr by animateIntAsState(
                                targetValue = restingHr?.toInt() ?: 0,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessVeryLow),
                                label = "resting_hr_counter"
                            )
                            val animatedHrvVal by animateIntAsState(
                                targetValue = hrvVal?.toInt() ?: 0,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessVeryLow),
                                label = "hrv_counter"
                            )
                            val nutritionCal = healthData.nutrition.calories
                            val hydrationLiters: Double? = if (hydrationDailyTotalMl > 0) hydrationDailyTotalMl / 1000.0 else healthData.hydration.liters
                            val HydrationBlue = Color(0xFF4DABFF)

                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                // Row 1: Resting HR + HRV
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Resting HR — square card
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(c.surfaceLow)
                                            .bounceClick { onMetricClick(HealthViewModel.MetricType.HEART_RATE) }
                                            .padding(16.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                // Icon with glow
                                                Box(contentAlignment = Alignment.Center) {
                                                    Canvas(modifier = Modifier.size(36.dp)) {
                                                        drawCircle(
                                                            color = CardHeartRate.copy(alpha = 0.25f),
                                                            radius = size.minDimension / 2
                                                        )
                                                    }
                                                    Icon(
                                                        Icons.Filled.Favorite,
                                                        contentDescription = null,
                                                        tint = CardHeartRate,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                                Text(
                                                    "RESTING HR",
                                                    fontSize = 9.sp,
                                                    color = c.onSurfaceVariant,
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 1.5.sp
                                                )
                                            }
                                            Column {
                                                Text(
                                                    text = if (restingHr != null) "$animatedRestingHr" else "--",
                                                    fontSize = 36.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = c.onSurface,
                                                    letterSpacing = (-1).sp
                                                )
                                                Text(
                                                    "bpm",
                                                    fontSize = 12.sp,
                                                    color = c.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    // HRV — square card
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(c.surfaceLow)
                                            .bounceClick { onMetricClick(HealthViewModel.MetricType.HEART_RATE_VARIABILITY) }
                                            .padding(16.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                // Icon with glow
                                                Box(contentAlignment = Alignment.Center) {
                                                    Canvas(modifier = Modifier.size(36.dp)) {
                                                        drawCircle(
                                                            color = c.tertiary.copy(alpha = 0.25f),
                                                            radius = size.minDimension / 2
                                                        )
                                                    }
                                                    Icon(
                                                        Icons.Filled.Favorite,
                                                        contentDescription = null,
                                                        tint = c.tertiary,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                                Text(
                                                    "HRV",
                                                    fontSize = 9.sp,
                                                    color = c.onSurfaceVariant,
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 1.5.sp
                                                )
                                            }
                                            Column {
                                                Text(
                                                    text = if (hrvVal != null) "$animatedHrvVal" else "--",
                                                    fontSize = 36.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = c.onSurface,
                                                    letterSpacing = (-1).sp
                                                )
                                                Text(
                                                    "ms",
                                                    fontSize = 12.sp,
                                                    color = c.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }

                                // Row 2: Nutrition + Hydration — square with mini rings + pulsing dots
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Nutrition — square card
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(c.surfaceLow)
                                            .bounceClick { onMetricClick(HealthViewModel.MetricType.NUTRITION) }
                                            .padding(16.dp)
                                    ) {
                                        val nutCalories = nutritionCal ?: 0.0
                                        val nutGoal = 2200.0
                                        val nutProgress = (nutCalories / nutGoal).toFloat().coerceIn(0f, 1f)

                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                // Icon with glow
                                                Box(contentAlignment = Alignment.Center) {
                                                    Canvas(modifier = Modifier.size(36.dp)) {
                                                        drawCircle(
                                                            color = CardNutrition.copy(alpha = 0.25f),
                                                            radius = size.minDimension / 2
                                                        )
                                                    }
                                                    Icon(
                                                        Icons.Filled.Restaurant,
                                                        contentDescription = null,
                                                        tint = CardNutrition,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    // Pulsing status dot
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .background(CardNutrition, CircleShape)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        "NUTRITION",
                                                        fontSize = 9.sp,
                                                        color = c.onSurfaceVariant,
                                                        fontWeight = FontWeight.Bold,
                                                        letterSpacing = 1.5.sp
                                                    )
                                                }
                                            }

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.Bottom
                                            ) {
                                                Column {
                                                    Text(
                                                        text = if (nutritionCal != null) "%,.0f".format(nutritionCal) else "--",
                                                        fontSize = 36.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = c.onSurface,
                                                        letterSpacing = (-1).sp
                                                    )
                                                    Text(
                                                        "kcal",
                                                        fontSize = 12.sp,
                                                        color = c.onSurfaceVariant
                                                    )
                                                }
                                                // Mini radial progress ring
                                                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp)) {
                                                    Canvas(modifier = Modifier.size(40.dp)) {
                                                        val strokeW = 4.dp.toPx()
                                                        val arcDiameter = size.width - strokeW
                                                        drawArc(
                                                            color = c.surfaceHighest,
                                                            startAngle = -90f, sweepAngle = 360f,
                                                            useCenter = false,
                                                            style = Stroke(strokeW, cap = StrokeCap.Round),
                                                            topLeft = Offset(strokeW / 2, strokeW / 2),
                                                            size = Size(arcDiameter, arcDiameter)
                                                        )
                                                        drawArc(
                                                            color = CardNutrition,
                                                            startAngle = -90f, sweepAngle = 360f * nutProgress,
                                                            useCenter = false,
                                                            style = Stroke(strokeW, cap = StrokeCap.Round),
                                                            topLeft = Offset(strokeW / 2, strokeW / 2),
                                                            size = Size(arcDiameter, arcDiameter)
                                                        )
                                                    }
                                                    Text(
                                                        "${(nutProgress * 100).toInt()}%",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = CardNutrition
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Hydration — square card
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(c.surfaceLow)
                                            .bounceClick { onHydrationClick() }
                                            .padding(16.dp)
                                    ) {
                                        val hydLiters = hydrationLiters ?: 0.0
                                        val hydGoal = 2.5
                                        val hydProgress = (hydLiters / hydGoal).toFloat().coerceIn(0f, 1f)

                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                // Icon with glow
                                                Box(contentAlignment = Alignment.Center) {
                                                    Canvas(modifier = Modifier.size(36.dp)) {
                                                        drawCircle(
                                                            color = HydrationBlue.copy(alpha = 0.25f),
                                                            radius = size.minDimension / 2
                                                        )
                                                    }
                                                    Icon(
                                                        Icons.Filled.WaterDrop,
                                                        contentDescription = null,
                                                        tint = HydrationBlue,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    // Pulsing status dot
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .background(HydrationBlue, CircleShape)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        "HYDRATION",
                                                        fontSize = 9.sp,
                                                        color = c.onSurfaceVariant,
                                                        fontWeight = FontWeight.Bold,
                                                        letterSpacing = 1.5.sp
                                                    )
                                                }
                                            }

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.Bottom
                                            ) {
                                                Column {
                                                    Text(
                                                        text = if (hydrationLiters != null) "%.1f".format(hydLiters) else "--",
                                                        fontSize = 36.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = c.onSurface,
                                                        letterSpacing = (-1).sp
                                                    )
                                                    Text(
                                                        "liters",
                                                        fontSize = 12.sp,
                                                        color = c.onSurfaceVariant
                                                    )
                                                }
                                                // Mini radial progress ring
                                                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp)) {
                                                    Canvas(modifier = Modifier.size(40.dp)) {
                                                        val strokeW = 4.dp.toPx()
                                                        val arcDiameter = size.width - strokeW
                                                        drawArc(
                                                            color = c.surfaceHighest,
                                                            startAngle = -90f, sweepAngle = 360f,
                                                            useCenter = false,
                                                            style = Stroke(strokeW, cap = StrokeCap.Round),
                                                            topLeft = Offset(strokeW / 2, strokeW / 2),
                                                            size = Size(arcDiameter, arcDiameter)
                                                        )
                                                        drawArc(
                                                            color = HydrationBlue,
                                                            startAngle = -90f, sweepAngle = 360f * hydProgress,
                                                            useCenter = false,
                                                            style = Stroke(strokeW, cap = StrokeCap.Round),
                                                            topLeft = Offset(strokeW / 2, strokeW / 2),
                                                            size = Size(arcDiameter, arcDiameter)
                                                        )
                                                    }
                                                    Text(
                                                        "${(hydProgress * 100).toInt()}%",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = HydrationBlue
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // ── 4. Stress & Resilience — horizontal with mini arc gauge ──
                        if (healthData.heartRateVariability.rmssdMs != null) {
                            item {
                                val hrv = healthData.heartRateVariability.rmssdMs!!
                                val stressLevel = ((80.0 - hrv.coerceIn(10.0, 80.0)) / 70.0 * 100).toInt().coerceIn(0, 100)
                                val animatedStressLevel by animateIntAsState(
                                    targetValue = stressLevel,
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessVeryLow),
                                    label = "pulse_stress_counter"
                                )
                                val stressLabel = when {
                                    stressLevel < 25 -> "Stable Mindset"
                                    stressLevel < 50 -> "Balanced State"
                                    stressLevel < 75 -> "Elevated Tension"
                                    else -> "High Alert"
                                }
                                val stressDesc = when {
                                    stressLevel < 25 -> "Your autonomic nervous system is in a calm, recovered state. Ideal for focused work."
                                    stressLevel < 50 -> "Moderate sympathetic activation detected. Normal for an active day."
                                    stressLevel < 75 -> "Elevated stress markers present. Consider a recovery break."
                                    else -> "High sympathetic drive detected. Prioritize rest and breathwork."
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(c.surfaceLow)
                                        .bounceClick { onStressClick() }
                                        .padding(20.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                "STRESS & RESILIENCE",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = c.outline,
                                                letterSpacing = 2.sp
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                stressLabel,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = c.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                stressDesc,
                                                fontSize = 12.sp,
                                                color = c.onSurfaceVariant,
                                                lineHeight = 16.sp,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        // Mini half-arc gauge
                                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(64.dp)) {
                                            Canvas(modifier = Modifier.size(64.dp)) {
                                                val strokeW = 6.dp.toPx()
                                                val arcSize = Size(size.width - strokeW, size.height - strokeW)
                                                val topLeft = Offset(strokeW / 2, strokeW / 2)
                                                // Background arc (half circle)
                                                drawArc(
                                                    color = c.surfaceHighest,
                                                    startAngle = 180f, sweepAngle = 180f,
                                                    useCenter = false,
                                                    style = Stroke(strokeW, cap = StrokeCap.Round),
                                                    topLeft = topLeft, size = arcSize
                                                )
                                                // Progress arc
                                                drawArc(
                                                    brush = Brush.sweepGradient(listOf(c.primary, c.secondary)),
                                                    startAngle = 180f, sweepAngle = 180f * (stressLevel / 100f),
                                                    useCenter = false,
                                                    style = Stroke(strokeW, cap = StrokeCap.Round),
                                                    topLeft = topLeft, size = arcSize
                                                )
                                            }
                                            Text(
                                                "$animatedStressLevel",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Black,
                                                color = c.onSurface,
                                                modifier = Modifier.padding(top = 8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // ── 5. Neural Insight — gradient card with highlighted text ──
                        item {
                            val capacityPercent = when {
                                readinessScore.score >= 80 -> "92%"
                                readinessScore.score >= 60 -> "74%"
                                readinessScore.score >= 40 -> "56%"
                                else -> "38%"
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                c.primary.copy(alpha = 0.10f),
                                                c.secondary.copy(alpha = 0.10f)
                                            )
                                        )
                                    )
                                    .bounceClick { onAiInsightsClick() }
                                    .padding(24.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Psychology icon in circle
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(c.primary.copy(alpha = 0.2f), CircleShape)
                                        ) {
                                            Icon(
                                                Icons.Filled.Psychology,
                                                contentDescription = null,
                                                tint = c.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            "Neural Insight",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = c.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        // Pulsing dot
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(c.primary, CircleShape)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = buildAnnotatedString {
                                            append("Based on your deep sleep cycles and current HRV, you are operating at ")
                                            withStyle(SpanStyle(color = c.onSurface, fontWeight = FontWeight.Bold)) {
                                                append("$capacityPercent daily capacity")
                                            }
                                            append(". Your metabolic window for peak performance is between 18:00 and 20:30.")
                                        },
                                        fontSize = 14.sp,
                                        color = c.onSurfaceVariant,
                                        lineHeight = 21.sp
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    // "View Full Analysis" button
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(50))
                                            .background(c.primary)
                                            .bounceClick { onAiInsightsClick() }
                                            .padding(vertical = 14.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "View Full Analysis",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = c.onPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ─── TAB 1: ACTIVITY — Alive Stitch Design ───
                    1 -> {
                        // ── Activity Rings Hero ──
                        item {
                            val stepsProgress = (healthData.steps.count.toFloat() / settings.stepsGoal).coerceIn(0f, 1f)
                            val calProgress = (healthData.calories.totalBurned.toFloat() / settings.caloriesGoal).coerceIn(0f, 1f)
                            val exerciseMin = healthData.exercise.totalDuration?.toMinutes()?.toInt() ?: 0
                            val exerciseProgress = (exerciseMin / 30f).coerceIn(0f, 1f)

                            val animatedCal by animateFloatAsState(calProgress, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessVeryLow), label = "cal")
                            val animatedExc by animateFloatAsState(exerciseProgress, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessVeryLow), label = "exc")
                            val animatedStand by animateFloatAsState(stepsProgress, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessVeryLow), label = "stand")

                            val animatedCalories by animateIntAsState(
                                targetValue = healthData.calories.totalBurned.roundToInt(),
                                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessVeryLow),
                                label = "calories_counter"
                            )

                            val movePercent = (calProgress * 100).roundToInt()
                            val excPercent = (exerciseProgress * 100).roundToInt()
                            val standPercent = (stepsProgress * 100).roundToInt()

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(28.dp))
                                    .background(c.surfaceLow)
                                    .bounceClick { onPerformanceClick() }
                                    .padding(28.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Rings with glow + center stats
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.size(240.dp)
                                    ) {
                                        Canvas(modifier = Modifier.size(240.dp)) {
                                            val strokeW = 18.dp.toPx()
                                            val gap = 8.dp.toPx()
                                            val cx = size.width / 2
                                            val cy = size.height / 2

                                            // ── Outer ring: Move (c.primary gradient) ──
                                            val r1 = cx - strokeW / 2
                                            val arcSize1 = Size(r1 * 2, r1 * 2)
                                            val tl1 = Offset(cx - r1, cy - r1)
                                            // Track
                                            drawArc(
                                                color = c.surfaceHigh,
                                                startAngle = 0f, sweepAngle = 360f, useCenter = false,
                                                style = Stroke(strokeW, cap = StrokeCap.Round),
                                                topLeft = tl1, size = arcSize1
                                            )
                                            // Glow behind arc
                                            val outerSweep = 360f * animatedCal
                                            if (animatedCal > 0f) {
                                                drawArc(
                                                    brush = Brush.sweepGradient(
                                                        listOf(c.primary.copy(alpha = 0.35f), c.primaryDim.copy(alpha = 0.15f))
                                                    ),
                                                    startAngle = -90f, sweepAngle = outerSweep, useCenter = false,
                                                    style = Stroke(strokeW + 10.dp.toPx(), cap = StrokeCap.Round),
                                                    topLeft = Offset(tl1.x - 5.dp.toPx(), tl1.y - 5.dp.toPx()),
                                                    size = Size(arcSize1.width + 10.dp.toPx(), arcSize1.height + 10.dp.toPx())
                                                )
                                            }
                                            // Active arc
                                            drawArc(
                                                brush = Brush.sweepGradient(listOf(c.primary, c.primaryDim, c.primary)),
                                                startAngle = -90f, sweepAngle = outerSweep, useCenter = false,
                                                style = Stroke(strokeW, cap = StrokeCap.Round),
                                                topLeft = tl1, size = arcSize1
                                            )

                                            // ── Middle ring: Exercise (c.secondary gradient) ──
                                            val r2 = r1 - strokeW - gap
                                            val arcSize2 = Size(r2 * 2, r2 * 2)
                                            val tl2 = Offset(cx - r2, cy - r2)
                                            drawArc(
                                                color = c.surfaceHigh,
                                                startAngle = 0f, sweepAngle = 360f, useCenter = false,
                                                style = Stroke(strokeW, cap = StrokeCap.Round),
                                                topLeft = tl2, size = arcSize2
                                            )
                                            val midSweep = 360f * animatedExc
                                            if (animatedExc > 0f) {
                                                drawArc(
                                                    brush = Brush.sweepGradient(
                                                        listOf(c.secondary.copy(alpha = 0.3f), c.secondaryContainer.copy(alpha = 0.12f))
                                                    ),
                                                    startAngle = -90f, sweepAngle = midSweep, useCenter = false,
                                                    style = Stroke(strokeW + 10.dp.toPx(), cap = StrokeCap.Round),
                                                    topLeft = Offset(tl2.x - 5.dp.toPx(), tl2.y - 5.dp.toPx()),
                                                    size = Size(arcSize2.width + 10.dp.toPx(), arcSize2.height + 10.dp.toPx())
                                                )
                                            }
                                            drawArc(
                                                brush = Brush.sweepGradient(listOf(c.secondary, c.secondaryContainer, c.secondary)),
                                                startAngle = -90f, sweepAngle = midSweep, useCenter = false,
                                                style = Stroke(strokeW, cap = StrokeCap.Round),
                                                topLeft = tl2, size = arcSize2
                                            )

                                            // ── Inner ring: Stand (c.tertiary gradient) ──
                                            val r3 = r2 - strokeW - gap
                                            val arcSize3 = Size(r3 * 2, r3 * 2)
                                            val tl3 = Offset(cx - r3, cy - r3)
                                            drawArc(
                                                color = c.surfaceHigh,
                                                startAngle = 0f, sweepAngle = 360f, useCenter = false,
                                                style = Stroke(strokeW, cap = StrokeCap.Round),
                                                topLeft = tl3, size = arcSize3
                                            )
                                            val innerSweep = 360f * animatedStand
                                            if (animatedStand > 0f) {
                                                drawArc(
                                                    brush = Brush.sweepGradient(
                                                        listOf(c.tertiary.copy(alpha = 0.3f), c.onPrimary.copy(alpha = 0.12f))
                                                    ),
                                                    startAngle = -90f, sweepAngle = innerSweep, useCenter = false,
                                                    style = Stroke(strokeW + 10.dp.toPx(), cap = StrokeCap.Round),
                                                    topLeft = Offset(tl3.x - 5.dp.toPx(), tl3.y - 5.dp.toPx()),
                                                    size = Size(arcSize3.width + 10.dp.toPx(), arcSize3.height + 10.dp.toPx())
                                                )
                                            }
                                            drawArc(
                                                brush = Brush.sweepGradient(listOf(c.tertiary, c.onPrimary, c.tertiary)),
                                                startAngle = -90f, sweepAngle = innerSweep, useCenter = false,
                                                style = Stroke(strokeW, cap = StrokeCap.Round),
                                                topLeft = tl3, size = arcSize3
                                            )
                                        }

                                        // Center stats
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.bounceClick { onPerformanceClick() }
                                        ) {
                                            Text(
                                                "ACTIVE ENERGY",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = c.onSurfaceVariant,
                                                letterSpacing = 2.sp
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                "$animatedCalories",
                                                fontSize = 48.sp,
                                                fontWeight = FontWeight.Black,
                                                color = c.onSurface,
                                                letterSpacing = (-1).sp
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    "KCAL",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = c.primary,
                                                    letterSpacing = 2.sp
                                                )
                                                Icon(
                                                    Icons.Default.ChevronRight,
                                                    contentDescription = null,
                                                    tint = c.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))

                                    // ── 3 stat columns below rings ──
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        // Move
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.bounceClick { onPerformanceClick() }
                                        ) {
                                            Icon(
                                                Icons.Default.Bolt,
                                                contentDescription = null,
                                                tint = c.primary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                "MOVE",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = c.onSurfaceVariant,
                                                letterSpacing = 1.5.sp
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    "$movePercent%",
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = c.onSurface
                                                )
                                                Icon(
                                                    Icons.Default.ChevronRight,
                                                    contentDescription = null,
                                                    tint = c.outline,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }

                                        // Exercise
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.bounceClick { onPerformanceClick() }
                                        ) {
                                            Icon(
                                                Icons.Default.FitnessCenter,
                                                contentDescription = null,
                                                tint = c.secondary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                "EXC",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = c.onSurfaceVariant,
                                                letterSpacing = 1.5.sp
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    "$excPercent%",
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = c.onSurface
                                                )
                                                Icon(
                                                    Icons.Default.ChevronRight,
                                                    contentDescription = null,
                                                    tint = c.outline,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }

                                        // Stand
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.bounceClick { onMetricClick(HealthViewModel.MetricType.STEPS) }
                                        ) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.DirectionsWalk,
                                                contentDescription = null,
                                                tint = c.tertiary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                "STAND",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = c.onSurfaceVariant,
                                                letterSpacing = 1.5.sp
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    "$standPercent%",
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = c.onSurface
                                                )
                                                Icon(
                                                    Icons.Default.ChevronRight,
                                                    contentDescription = null,
                                                    tint = c.outline,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // ── Step Intensity Card ──
                        item {
                            val stepProgress = (healthData.steps.count.toFloat() / settings.stepsGoal).coerceIn(0f, 1f)
                            val intensityLabel = when {
                                stepProgress >= 0.8f -> "High"
                                stepProgress >= 0.5f -> "Moderate"
                                stepProgress >= 0.25f -> "Light"
                                else -> "Low"
                            }
                            val intensityDesc = when {
                                stepProgress >= 0.8f -> "Excellent pace! You're in a high-intensity movement burst."
                                stepProgress >= 0.5f -> "Good momentum. Keep it up to hit your daily target."
                                stepProgress >= 0.25f -> "Warming up — try a brisk walk to boost intensity."
                                else -> "Get moving to start building your step intensity."
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(c.surfaceLow)
                                    .bounceClick { onMetricClick(HealthViewModel.MetricType.STEPS) }
                                    .padding(20.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "Step Intensity",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = c.onSurface
                                        )
                                        Icon(
                                            Icons.AutoMirrored.Filled.TrendingUp,
                                            contentDescription = null,
                                            tint = c.secondary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "Current Burst",
                                            fontSize = 13.sp,
                                            color = c.onSurfaceVariant
                                        )
                                        Text(
                                            intensityLabel,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = c.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Gradient progress bar with glow
                                    Box(modifier = Modifier.fillMaxWidth().height(10.dp)) {
                                        // Glow layer behind bar
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            val barWidth = size.width * stepProgress.coerceAtLeast(0.02f)
                                            drawRoundRect(
                                                brush = Brush.horizontalGradient(
                                                    listOf(
                                                        c.primary.copy(alpha = 0.4f),
                                                        c.secondary.copy(alpha = 0.4f)
                                                    )
                                                ),
                                                cornerRadius = CornerRadius(8.dp.toPx()),
                                                size = Size(barWidth, size.height + 6.dp.toPx()),
                                                topLeft = Offset(0f, -3.dp.toPx())
                                            )
                                        }
                                        // Track
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(5.dp))
                                                .background(c.surfaceHigh)
                                        )
                                        // Fill
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(stepProgress.coerceAtLeast(0.02f))
                                                .fillMaxHeight()
                                                .clip(RoundedCornerShape(5.dp))
                                                .background(
                                                    Brush.horizontalGradient(
                                                        listOf(c.primary, c.secondary)
                                                    )
                                                )
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        intensityDesc,
                                        fontSize = 13.sp,
                                        color = c.onSurfaceVariant,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        // ── Distance Traveled Card ──
                        item {
                            val distanceKm = healthData.steps.count * 0.0007f // ~0.7m per step
                            val distanceGoalKm = settings.stepsGoal * 0.0007f
                            val distProgress = (distanceKm / distanceGoalKm).coerceIn(0f, 1f)
                            val distPercent = (distProgress * 100).roundToInt()

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(c.surfaceLow)
                                    .bounceClick { onMetricClick(HealthViewModel.MetricType.DISTANCE) }
                                    .padding(20.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "Distance Traveled",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = c.onSurface
                                        )
                                        Icon(
                                            Icons.Default.Place,
                                            contentDescription = null,
                                            tint = c.primary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(
                                            String.format("%.1f", distanceKm),
                                            fontSize = 36.sp,
                                            fontWeight = FontWeight.Black,
                                            color = c.onSurface,
                                            letterSpacing = (-1).sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "KM",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = c.primary,
                                            letterSpacing = 2.sp,
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Gradient progress bar with glow
                                    Box(modifier = Modifier.fillMaxWidth().height(8.dp)) {
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            val barW = size.width * distProgress.coerceAtLeast(0.02f)
                                            drawRoundRect(
                                                brush = Brush.horizontalGradient(
                                                    listOf(
                                                        c.primary.copy(alpha = 0.35f),
                                                        c.secondary.copy(alpha = 0.35f)
                                                    )
                                                ),
                                                cornerRadius = CornerRadius(6.dp.toPx()),
                                                size = Size(barW, size.height + 4.dp.toPx()),
                                                topLeft = Offset(0f, -2.dp.toPx())
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(c.surfaceHigh)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(distProgress.coerceAtLeast(0.02f))
                                                .fillMaxHeight()
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(
                                                    Brush.horizontalGradient(
                                                        listOf(c.primary, c.secondary)
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
                                            "Daily Goal: ${String.format("%.1f", distanceGoalKm)} KM",
                                            fontSize = 13.sp,
                                            color = c.onSurfaceVariant
                                        )
                                        Text(
                                            "$distPercent%",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = c.primary
                                        )
                                    }
                                }
                            }
                        }

                        // ── Peak Performance Insight Card ──
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(c.surfaceHigh)
                                    .padding(20.dp)
                            ) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(c.secondaryContainer.copy(alpha = 0.2f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Bolt,
                                            contentDescription = null,
                                            tint = c.secondary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Peak Performance",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = c.primary
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            "Your metabolic efficiency is currently peaking. This is the optimal window for a high-intensity session to maximize calorie afterburn.",
                                            fontSize = 13.sp,
                                            color = c.onSurfaceVariant,
                                            lineHeight = 19.sp
                                        )
                                    }
                                }
                            }
                        }

                        // ── Latest Sessions ──
                        if (healthData.exercise.sessions.isNotEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Latest Sessions",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = c.onSurface
                                    )
                                    Text(
                                        "VIEW ALL",
                                        fontSize = 10.sp,
                                        color = c.primary,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                            items(healthData.exercise.sessions.take(3).size) { index ->
                                val session = healthData.exercise.sessions[index]
                                val durMin = session.duration.toMinutes()
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(c.surfaceHigh)
                                        .clickable { onSessionClick(session) }
                                        .padding(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(c.surfaceLow),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Bolt, null, tint = c.primary, modifier = Modifier.size(24.dp))
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(session.exerciseType, fontWeight = FontWeight.Bold, color = c.onSurface)
                                            Text("${durMin}m", fontSize = 12.sp, color = c.onSurfaceVariant)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                "${session.caloriesBurned?.roundToInt() ?: (durMin * 7)}",
                                                fontWeight = FontWeight.Bold,
                                                color = c.primary
                                            )
                                            Text("KCAL", fontSize = 10.sp, color = c.onSurfaceVariant)
                                        }
                                        Icon(Icons.Default.ChevronRight, null, tint = c.outline, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }

                    // ─── TAB 2: VITALS (Biometric Health) — Stitch remix ───
                    2 -> {
                        // Hero: SpO2 card with "CURRENT SATURATION" label
                        if (healthData.oxygenSaturation.percentage != null) {
                            item {
                                val animatedSpo2 by animateIntAsState(
                                    targetValue = healthData.oxygenSaturation.percentage?.roundToInt() ?: 0,
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessVeryLow),
                                    label = "spo2_counter"
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(c.surfaceLow)
                                        .bounceClick { onMetricClick(HealthViewModel.MetricType.OXYGEN_SATURATION) }
                                        .padding(vertical = 32.dp, horizontal = 24.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            "CURRENT SATURATION",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = c.primary,
                                            letterSpacing = 3.sp
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(verticalAlignment = Alignment.Bottom) {
                                            Text(
                                                "$animatedSpo2",
                                                fontSize = 72.sp,
                                                fontWeight = FontWeight.Black,
                                                color = c.onSurface,
                                                letterSpacing = (-2).sp
                                            )
                                            Text(
                                                "%",
                                                fontSize = 28.sp,
                                                color = c.primary,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(bottom = 10.dp, start = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        // "Optimal SpO2" badge
                                        Box(
                                            modifier = Modifier
                                                .background(c.surfaceHighest, RoundedCornerShape(20.dp))
                                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Favorite, null, tint = c.secondary, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Optimal SpO2", fontSize = 13.sp, color = c.onSurfaceVariant, fontWeight = FontWeight.Medium)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Daily Stress + Body Energy bento grid
                        if (healthData.heartRateVariability.rmssdMs != null) {
                            item {
                                val hrv = healthData.heartRateVariability.rmssdMs!!
                                val stressLevel = ((80.0 - hrv.coerceIn(10.0, 80.0)) / 70.0 * 100).toInt().coerceIn(0, 100)
                                val animatedVitalsStress by animateIntAsState(
                                    targetValue = stressLevel,
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessVeryLow),
                                    label = "vitals_stress_counter"
                                )
                                val stressLabel = when { stressLevel < 25 -> "Low Stress"; stressLevel < 50 -> "Moderate"; stressLevel < 75 -> "High Stress State"; else -> "Very High Stress" }
                                val energyPercent = readinessScore.score.coerceIn(0, 100)
                                val energyLabel = when { energyPercent >= 75 -> "Charging Rapidly"; energyPercent >= 50 -> "Stable Energy"; energyPercent >= 25 -> "Depleting"; else -> "Low Energy" }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Stress card
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(24.dp))
                                            .background(c.surfaceHigh)
                                            .bounceClick { onStressClick() }
                                            .padding(20.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Text("DAILY STRESS", fontSize = 10.sp, color = c.onSurfaceVariant, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                                                Icon(Icons.Default.Favorite, null, tint = c.secondary, modifier = Modifier.size(18.dp))
                                            }
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text("$animatedVitalsStress", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = c.onSurface)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(stressLabel, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = c.secondary)
                                            Spacer(modifier = Modifier.height(12.dp))
                                            // Stress progress bar
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(6.dp)
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(c.background)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth(stressLevel / 100f)
                                                        .fillMaxHeight()
                                                        .background(c.secondary, RoundedCornerShape(3.dp))
                                                )
                                            }
                                        }
                                    }

                                    // Body Energy card
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(24.dp))
                                            .background(c.surfaceHigh)
                                            .padding(20.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Text("BODY ENERGY", fontSize = 10.sp, color = c.onSurfaceVariant, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                                                Icon(Icons.Default.Assessment, null, tint = c.primary, modifier = Modifier.size(18.dp))
                                            }
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Row(verticalAlignment = Alignment.Bottom) {
                                                Text("$energyPercent", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = c.onSurface)
                                                Text("%", fontSize = 18.sp, color = c.primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp, start = 2.dp))
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(energyLabel, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = c.primary)
                                            Spacer(modifier = Modifier.height(12.dp))
                                            // Energy gradient bar
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(12.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(
                                                        Brush.horizontalGradient(listOf(c.primary, c.secondary)),
                                                        RoundedCornerShape(6.dp)
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Sleep Analysis card with stages breakdown
                        item {
                            val sleepHours = healthData.sleep.totalDuration?.toHours()?.toInt() ?: 0
                            val sleepMinutes = healthData.sleep.totalDuration?.let { ((it.toMinutes() % 60).toInt()) } ?: 0
                            val hasSleepData = sleepHours > 0 || sleepMinutes > 0
                            val stages = healthData.sleep.stages
                            val deepMin = stages?.deepSleepMinutes ?: 0L
                            val lightMin = stages?.lightSleepMinutes ?: 0L
                            val remMin = stages?.remSleepMinutes ?: 0L

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(c.surfaceLow)
                                    .bounceClick { onMetricClick(HealthViewModel.MetricType.SLEEP) }
                                    .padding(20.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        Column {
                                            Text("SLEEP QUALITY", fontSize = 10.sp, color = c.onSurfaceVariant, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                if (hasSleepData) "${sleepHours}h ${sleepMinutes}m" else "--",
                                                fontSize = 32.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = c.onSurface
                                            )
                                        }
                                        // Mini bar chart decoration
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                                            verticalAlignment = Alignment.Bottom,
                                            modifier = Modifier.height(40.dp)
                                        ) {
                                            listOf(0.6f, 0.8f, 0.4f, 1f, 0.6f, 0.8f, 0.4f).forEachIndexed { i, h ->
                                                val color = if (i % 2 == 1) c.primary else c.surfaceHighest
                                                Box(
                                                    modifier = Modifier
                                                        .width(3.dp)
                                                        .fillMaxHeight(h)
                                                        .background(color, RoundedCornerShape(2.dp))
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    // Sleep stages grid
                                    if (hasSleepData) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // Deep
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(c.surfaceHighest.copy(alpha = 0.5f))
                                                    .padding(10.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text("DEEP", fontSize = 9.sp, color = c.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        if (deepMin > 0) "${deepMin / 60}h ${deepMin % 60}m" else "--",
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = c.onSurface
                                                    )
                                                }
                                            }
                                            // REM
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(c.surfaceHighest.copy(alpha = 0.5f))
                                                    .drawBehind {
                                                        drawRoundRect(
                                                            color = c.primary,
                                                            cornerRadius = CornerRadius(12.dp.toPx()),
                                                            size = Size(size.width, 2.dp.toPx()),
                                                            topLeft = Offset(0f, size.height - 2.dp.toPx())
                                                        )
                                                    }
                                                    .padding(10.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text("REM", fontSize = 9.sp, color = c.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        if (remMin > 0) "${remMin / 60}h ${remMin % 60}m" else "--",
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = c.onSurface
                                                    )
                                                }
                                            }
                                            // Light
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(c.surfaceHighest.copy(alpha = 0.5f))
                                                    .padding(10.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text("LIGHT", fontSize = 9.sp, color = c.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        if (lightMin > 0) "${lightMin / 60}h ${lightMin % 60}m" else "--",
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = c.onSurface
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Key Vitals section header + grid
                        item {
                            Text(
                                "Key Vitals",
                                style = MaterialTheme.typography.titleMedium,
                                color = c.onSurface,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }

                        item {
                            @Composable
                            fun VitalTile(label: String, value: String, unit: String, icon: ImageVector, iconTint: Color, onClick: () -> Unit) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(c.surfaceHigh)
                                        .clickable(onClick = onClick)
                                        .padding(14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(icon, null, tint = iconTint, modifier = Modifier.size(22.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(label, fontSize = 10.sp, color = c.onSurfaceVariant, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.Bottom) {
                                            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = c.onSurface)
                                            if (unit.isNotEmpty()) {
                                                Text(unit, fontSize = 9.sp, color = c.onSurfaceVariant, modifier = Modifier.padding(bottom = 2.dp, start = 2.dp))
                                            }
                                        }
                                    }
                                }
                            }

                            // 3-column vitals grid
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // HRV
                                Box(modifier = Modifier.weight(1f)) {
                                    VitalTile(
                                        label = "HRV",
                                        value = healthData.heartRateVariability.rmssdMs?.toInt()?.toString() ?: "--",
                                        unit = "ms",
                                        icon = Icons.Default.Favorite,
                                        iconTint = c.primary,
                                        onClick = { onMetricClick(HealthViewModel.MetricType.HEART_RATE_VARIABILITY) }
                                    )
                                }
                                // Respiration
                                Box(modifier = Modifier.weight(1f)) {
                                    VitalTile(
                                        label = "RESP",
                                        value = healthData.respiratoryRate.ratePerMinute?.roundToInt()?.toString() ?: "--",
                                        unit = "brpm",
                                        icon = Icons.Default.Refresh,
                                        iconTint = c.secondary,
                                        onClick = { onMetricClick(HealthViewModel.MetricType.RESPIRATORY_RATE) }
                                    )
                                }
                                // Temp
                                Box(modifier = Modifier.weight(1f)) {
                                    VitalTile(
                                        label = "TEMP",
                                        value = healthData.bodyTemperature.temperatureCelsius?.let { String.format("%.1f", it) } ?: "--",
                                        unit = "\u00B0C",
                                        icon = Icons.Default.LocalFireDepartment,
                                        iconTint = c.tertiary,
                                        onClick = { onMetricClick(HealthViewModel.MetricType.BODY_TEMPERATURE) }
                                    )
                                }
                            }
                        }

                        // Body Composition section
                        val hasWeight = settings.showWeight && healthData.weight.kilograms != null
                        val hasBodyFat = settings.showBodyFat && healthData.bodyFat.percentage != null
                        val hasLeanMass = settings.showLeanBodyMass && healthData.leanBodyMass.kilograms != null
                        val hasBMR = settings.showBMR && healthData.basalMetabolicRate.caloriesPerDay != null
                        val hasBodyWater = settings.showBodyWater && healthData.bodyWaterMass.kilograms != null
                        val hasBoneMass = settings.showBoneMass && healthData.boneMass.kilograms != null
                        val hasAnyBody = hasWeight || hasBodyFat || hasLeanMass || hasBMR || hasBodyWater || hasBoneMass

                        if (hasAnyBody) {
                            item {
                                Text(
                                    "Body Composition",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = c.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                )
                            }

                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(c.surfaceLow)
                                ) {
                                    Column {
                                        // Weight row
                                        if (hasWeight) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .bounceClick { onMetricClick(HealthViewModel.MetricType.WEIGHT) }
                                                    .padding(16.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(40.dp)
                                                                .background(c.surfaceHighest, CircleShape),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(Icons.Default.Assessment, null, tint = c.primary, modifier = Modifier.size(20.dp))
                                                        }
                                                        Spacer(modifier = Modifier.width(12.dp))
                                                        Text("Total Weight", fontWeight = FontWeight.Medium, color = c.onSurface)
                                                    }
                                                    Text(
                                                        String.format("%.1f kg", healthData.weight.kilograms),
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = c.onSurface
                                                    )
                                                }
                                            }
                                        }

                                        // Body Fat row
                                        if (hasBodyFat) {
                                            if (hasWeight) {
                                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(c.surfaceHighest.copy(alpha = 0.3f)))
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { onMetricClick(HealthViewModel.MetricType.BODY_FAT) }
                                                    .padding(16.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(40.dp)
                                                                .background(c.surfaceHighest, CircleShape),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(Icons.Default.Favorite, null, tint = c.secondary, modifier = Modifier.size(20.dp))
                                                        }
                                                        Spacer(modifier = Modifier.width(12.dp))
                                                        Text("Body Fat Percentage", fontWeight = FontWeight.Medium, color = c.onSurface)
                                                    }
                                                    Text(
                                                        String.format("%.1f%%", healthData.bodyFat.percentage),
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = c.onSurface
                                                    )
                                                }
                                            }
                                        }

                                        // Lean Muscle Mass row
                                        if (hasLeanMass) {
                                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(c.surfaceHighest.copy(alpha = 0.3f)))
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { onMetricClick(HealthViewModel.MetricType.LEAN_BODY_MASS) }
                                                    .padding(16.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(40.dp)
                                                                .background(c.surfaceHighest, CircleShape),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(Icons.Default.Assessment, null, tint = c.tertiary, modifier = Modifier.size(20.dp))
                                                        }
                                                        Spacer(modifier = Modifier.width(12.dp))
                                                        Text("Lean Muscle Mass", fontWeight = FontWeight.Medium, color = c.onSurface)
                                                    }
                                                    Text(
                                                        String.format("%.1f kg", healthData.leanBodyMass.kilograms),
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = c.onSurface
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ─── TAB 3: PROGRESS (Trends & Milestones) — Stitch match ───
                    3 -> {
                        // Hero: Active Zone — Stitch progress_remix
                        item {
                            val consistency = readinessScore.score.coerceIn(0, 100)
                            Column {
                                Text("CONSISTENCY SCORE", fontSize = 10.sp, color = c.primary, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${consistency}% Active Zone", fontSize = 36.sp, fontWeight = FontWeight.Black, color = c.onSurface, letterSpacing = (-1).sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("You're maintaining a steady rhythm. Your physical output is optimizing recovery.", style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant, lineHeight = 18.sp)
                            }
                        }

                        // Stats bento grid — taller cards with icons
                        item {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                // Streak → Steps detail
                                Box(modifier = Modifier.weight(1f).height(140.dp).clip(RoundedCornerShape(24.dp)).background(c.surfaceLow).bounceClick { onMetricClick(HealthViewModel.MetricType.STEPS) }.padding(16.dp)) {
                                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                                        Icon(Icons.Default.LocalFireDepartment, null, tint = c.secondary, modifier = Modifier.size(24.dp))
                                        Column {
                                            Text(if (stepsStreak > 0) "$stepsStreak" else "--", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = c.onSurface)
                                            Text("STREAK DAYS", fontSize = 9.sp, color = c.onSurfaceVariant, letterSpacing = 1.sp)
                                        }
                                    }
                                }
                                // Status → Readiness detail
                                Box(modifier = Modifier.weight(1f).height(140.dp).clip(RoundedCornerShape(24.dp)).background(c.surfaceLow).bounceClick { onReadinessClick() }.padding(16.dp)) {
                                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                                        Icon(Icons.Default.Favorite, null, tint = c.primary, modifier = Modifier.size(24.dp))
                                        Column {
                                            Text(when { readinessScore.score >= 80 -> "Strong"; readinessScore.score >= 60 -> "Good"; else -> "Building" }, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = c.onSurface)
                                            Text("STATUS", fontSize = 9.sp, color = c.onSurfaceVariant, letterSpacing = 1.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // Avg Steps wide card
                        item {
                            val animatedStepsCount by animateIntAsState(
                                targetValue = healthData.steps.count.toInt(),
                                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessVeryLow),
                                label = "steps_counter"
                            )
                            Box(modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(24.dp)).background(c.surfaceHigh).bounceClick { onMetricClick(HealthViewModel.MetricType.STEPS) }.padding(16.dp)) {
                                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                                    Icon(Icons.AutoMirrored.Filled.DirectionsWalk, null, tint = c.tertiary, modifier = Modifier.size(24.dp))
                                    Column {
                                        val avgK = if (animatedStepsCount > 0) String.format("%.1fk", animatedStepsCount / 1000f) else "--"
                                        Text(avgK, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = c.onSurface)
                                        Text("AVG STEPS", fontSize = 9.sp, color = c.onSurfaceVariant, letterSpacing = 1.sp)
                                    }
                                }
                            }
                        }

                        // Step Trends — density circle
                        item {
                            NocturneCard(surfaceColor = c.surfaceLow, onClick = { onMetricClick(HealthViewModel.MetricType.STEPS) }) {
                                Text("Step Trends", style = MaterialTheme.typography.titleMedium, color = c.onSurface, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Info text
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Density", fontSize = 12.sp, color = c.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Peak activity times are clustered in the morning hours", fontSize = 12.sp, color = c.outline, lineHeight = 16.sp)
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    // Density gauge circle
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                                        Canvas(modifier = Modifier.size(80.dp)) {
                                            val strokeW = 8.dp.toPx()
                                            drawCircle(color = c.surfaceHighest, radius = size.minDimension / 2 - strokeW / 2, style = Stroke(strokeW))
                                            drawArc(
                                                brush = Brush.sweepGradient(listOf(c.primary, c.secondary, c.primary)),
                                                startAngle = -90f, sweepAngle = 270f,
                                                useCenter = false,
                                                style = Stroke(strokeW, cap = StrokeCap.Round),
                                                topLeft = Offset(strokeW / 2, strokeW / 2),
                                                size = Size(size.width - strokeW, size.height - strokeW)
                                            )
                                        }
                                        val avgStepsK = if (healthData.steps.count > 0) String.format("%.1f", healthData.steps.count / 1000f) else "0"
                                        Text(avgStepsK, fontSize = 18.sp, fontWeight = FontWeight.Black, color = c.onSurface)
                                    }
                                }
                            }
                        }

                        // Milestones section
                        item {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Milestones", style = MaterialTheme.typography.titleMedium, color = c.onSurface, fontWeight = FontWeight.Bold)
                                Text("View Gallery", color = c.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.bounceClick { onReportsClick() })
                            }
                        }

                        // Weekly Insight — c.primary background (Stitch progress_remix)
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(c.primary)
                                    .bounceClick { onReportsClick() }
                                    .padding(24.dp)
                            ) {
                                // Blur circle overlay
                                Canvas(modifier = Modifier.matchParentSize()) {
                                    drawCircle(color = Color.White.copy(alpha = 0.08f), radius = 120.dp.toPx(), center = Offset(size.width + 20.dp.toPx(), -20.dp.toPx()))
                                }
                                Column {
                                    Icon(Icons.Default.Lightbulb, null, tint = c.onPrimary, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("Weekly Insight", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = c.onPrimary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Your training consistency has improved by 12% since last month. High-intensity intervals significantly boosted your recovery rate.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = c.onPrimary.copy(alpha = 0.8f),
                                        lineHeight = 18.sp
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(c.onPrimary)
                                            .padding(horizontal = 20.dp, vertical = 10.dp)
                                    ) {
                                        Text("Read Analysis", color = c.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                onTabSelected = { onTabChanged(it) },
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
    val c = LocalAppColors.current
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
                    tint = c.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column(modifier = Modifier.clickable(onClick = onDateClick)) {
                Text(
                    text = "OpenHealth",
                    style = MaterialTheme.typography.headlineMedium,
                    color = c.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodySmall,
                    color = c.onSurfaceVariant
                )
            }
        }

        // Right: Actions
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!isToday) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(c.primary.copy(alpha = 0.15f))
                        .clickable { onToday() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Today",
                        color = c.primary,
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
                    tint = if (isToday) c.outline else c.onSurfaceVariant
                )
            }
            IconButton(onClick = onReportsClick, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.Assessment,
                    contentDescription = "Reports",
                    tint = c.onSurfaceVariant
                )
            }
            IconButton(onClick = onSettingsClick, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = c.onSurfaceVariant
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
    val c = LocalAppColors.current
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
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            // MASSIVE pulsing purple glow — 320dp behind the ring
            Canvas(modifier = Modifier.size(320.dp)) {
                drawCircle(
                    color = c.primary.copy(alpha = 0.20f),
                    radius = size.minDimension / 2
                )
                drawCircle(
                    color = c.primary.copy(alpha = 0.08f),
                    radius = size.minDimension / 2 * 1.15f
                )
            }

            // Readiness ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(240.dp)
            ) {
                val animatedScore by animateFloatAsState(
                    targetValue = readinessScore.score / 100f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessVeryLow),
                    label = "readiness_arc"
                )
                val animatedNumber by animateIntAsState(
                    targetValue = readinessScore.score,
                    animationSpec = tween(1000),
                    label = "readiness_number"
                )

                Canvas(modifier = Modifier.size(240.dp)) {
                    val strokeWidth = 14.dp.toPx()
                    val arcSize = Size(size.width - strokeWidth * 2, size.height - strokeWidth * 2)
                    val topLeft = Offset(strokeWidth, strokeWidth)

                    // Shadow glow ring (larger radius, behind the main ring)
                    drawCircle(
                        color = c.primary.copy(alpha = 0.15f),
                        radius = size.minDimension / 2 - strokeWidth / 2 + 6.dp.toPx(),
                        style = Stroke(width = strokeWidth + 12.dp.toPx())
                    )

                    // Background track ring (full 360)
                    drawArc(
                        color = c.surfaceHighest.copy(alpha = 0.6f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        topLeft = topLeft,
                        size = arcSize
                    )

                    // Progress ring (solid primary fill)
                    drawArc(
                        color = c.primary,
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
                        color = c.onSurfaceVariant,
                        letterSpacing = 3.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = animatedNumber.toString(),
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Black,
                        color = c.onSurface,
                        letterSpacing = (-2).sp
                    )
                    Text(
                        text = readinessScore.label.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = c.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
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
    val c = LocalAppColors.current
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
            .background(c.secondaryContainer)
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
                        tint = c.secondaryContainer,
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
    val c = LocalAppColors.current
    val hrvValue = healthData.heartRateVariability.rmssdMs
    if (hrvValue == null) return

    NocturneCard(
        onClick = onClick,
        surfaceColor = c.surfaceHigh
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
                    tint = c.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Heart Rate\nVariability",
                    style = MaterialTheme.typography.titleMedium,
                    color = c.onSurface,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 22.sp
                )
            }
            Text(
                text = "${String.format("%.0f", hrvValue)} ms",
                style = MaterialTheme.typography.bodyMedium,
                color = c.onSurfaceVariant,
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
                            if (isToday) c.primary else c.primary.copy(alpha = 0.2f),
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
                    color = if (index == todayIndex) c.primary else c.outline,
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
    val c = LocalAppColors.current
    val sleepHours = healthData.sleep.totalDuration?.toHours()?.toInt() ?: 0
    val sleepMinutes = healthData.sleep.totalDuration?.let { ((it.toMinutes() % 60).toInt()) } ?: 0
    if (sleepHours == 0 && sleepMinutes == 0) return

    val efficiency = ((sleepHours * 60 + sleepMinutes).toFloat() / 480f * 100).toInt().coerceIn(0, 100)
    val deepSleep = healthData.sleep.sessions.filter { it.stage == com.openhealth.openhealth.model.SleepStage.DEEP }.sumOf { it.duration.toMinutes() }.toInt()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(c.surfaceLow)
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
                    .background(c.surfaceHighest, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NightsStay,
                    contentDescription = null,
                    tint = c.tertiary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Sleep Efficiency",
                    style = MaterialTheme.typography.titleSmall,
                    color = c.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Deep Sleep: ${deepSleep / 60}h ${deepSleep % 60}m",
                    style = MaterialTheme.typography.labelSmall,
                    color = c.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Percentage
            Text(
                text = "$efficiency%",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = c.secondary
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
                    .background(c.surfaceHighest, RoundedCornerShape(2.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth((efficiency / 100f).coerceAtLeast(0.01f))
                        .height(4.dp)
                        .background(
                            Brush.horizontalGradient(listOf(c.primary, c.secondary)),
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
    val c = LocalAppColors.current
    NocturneCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${String.format("%.0f", weatherData.temperature)}°",
                    color = c.onSurface,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    val uvColor = when(weatherData.uvLabel) { "Low" -> c.success; "Moderate" -> Color(0xFFFFCC00); else -> c.error }
                    val aqiColor = when(weatherData.aqi) { 1, 2 -> c.success; 3 -> Color(0xFFFFCC00); else -> c.error }
                    Text(text = "UV ${String.format("%.0f", weatherData.uvIndex)} (${weatherData.uvLabel})", color = uvColor, fontSize = 13.sp)
                    Text(text = "Air: ${weatherData.aqiLabel}", color = aqiColor, fontSize = 13.sp)
                }
            }
        }
        if (weatherData.healthAdvisory != "Good conditions for outdoor activity") {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = weatherData.healthAdvisory,
                color = c.warning,
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
    val c = LocalAppColors.current
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val entranceScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.95f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessVeryLow),
        label = "metric_entrance"
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 3 }
    ) {
        Box(modifier = Modifier.graphicsLayer { scaleX = entranceScale; scaleY = entranceScale }) {
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
                            color = c.onSurfaceVariant
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
                            color = c.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        if (unit.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = unit,
                                style = MaterialTheme.typography.bodySmall,
                                color = c.outline,
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
    val c = LocalAppColors.current
    NocturneCard(onClick = onClick) {
        Text(
            text = "Stress & Energy",
            style = MaterialTheme.typography.titleMedium,
            color = c.onSurface,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(text = "Stress", color = c.onSurfaceVariant, fontSize = 13.sp)
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
            Text(text = "Energy", color = c.onSurfaceVariant, fontSize = 13.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .background(c.surfaceHigh, RoundedCornerShape(4.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth((energyPercent / 100f).coerceAtLeast(0.01f))
                        .height(8.dp)
                        .background(
                            Brush.horizontalGradient(listOf(c.primary, c.success)),
                            RoundedCornerShape(4.dp)
                        )
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "$energyPercent%", color = c.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
    val c = LocalAppColors.current
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
                        color = c.onSurface,
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
                    color = c.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                if (progress != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(c.surfaceHigh, RoundedCornerShape(3.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress.coerceIn(0f, 1f))
                                .height(6.dp)
                                .background(
                                    Brush.horizontalGradient(listOf(c.primary, c.tertiary)),
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
                    tint = c.outline,
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
    val c = LocalAppColors.current
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
                    color = c.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = c.outline
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
                        Text(text = String.format("%.1f", healthData.weight.kilograms), color = c.onSurface, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        Text(text = "kg", color = c.outline, fontSize = 12.sp)
                    }
                }
                if (hasBodyFat) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = String.format("%.1f", healthData.bodyFat.percentage), color = c.onSurface, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        Text(text = "% fat", color = c.outline, fontSize = 12.sp)
                    }
                }
                if (hasLeanMass) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = String.format("%.1f", healthData.leanBodyMass.kilograms), color = c.onSurface, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        Text(text = "kg lean", color = c.outline, fontSize = 12.sp)
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
    val c = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = c.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = value, color = c.onSurface, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = c.outline, modifier = Modifier.size(18.dp))
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
    val c = LocalAppColors.current
    val hrvCheck = if (hasHRV) (healthData.heartRateVariability.avgMs ?: healthData.heartRateVariability.rmssdMs!!) >= 30 else true
    val spo2Check = if (hasBloodOxygen) (healthData.oxygenSaturation.avgPercentage ?: healthData.oxygenSaturation.percentage!!) >= 95 else true
    val rrCheck = if (hasRespiratoryRate) (healthData.respiratoryRate.avgRate ?: healthData.respiratoryRate.ratePerMinute!!) in 12.0..20.0 else true
    val bpCheck = if (hasBloodPressure) healthData.bloodPressure.systolicMmHg!! in 90.0..140.0 else true
    val bgCheck = if (hasBloodGlucose) healthData.bloodGlucose.levelMgPerDl!! in 60.0..140.0 else true
    val btCheck = if (hasBodyTemp) healthData.bodyTemperature.temperatureCelsius!! in 35.5..38.0 else true
    val allNormal = hrvCheck && spo2Check && rrCheck && bpCheck && bgCheck && btCheck
    val statusColor = if (allNormal) c.success else c.warning
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
                        color = c.onSurface,
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
                    tint = c.outline
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
                        Text(text = String.format("%.0f", hrvDisplay), color = c.onSurface, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        Text(text = "ms HRV", color = c.outline, fontSize = 12.sp)
                    }
                }
                if (hasBloodOxygen) {
                    val spo2Display = healthData.oxygenSaturation.avgPercentage ?: healthData.oxygenSaturation.percentage!!
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = String.format("%.0f%%", spo2Display), color = c.onSurface, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        Text(text = "SpO2", color = c.outline, fontSize = 12.sp)
                    }
                }
                if (hasRespiratoryRate) {
                    val rrDisplay = healthData.respiratoryRate.avgRate ?: healthData.respiratoryRate.ratePerMinute!!
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = String.format("%.0f", rrDisplay), color = c.onSurface, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        Text(text = "breaths", color = c.outline, fontSize = 12.sp)
                    }
                }
            }

            // Expanded details
            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))

                if (hasHRV) {
                    val hrvAvg = healthData.heartRateVariability.avgMs ?: healthData.heartRateVariability.rmssdMs!!
                    val dot = when { hrvAvg >= 30 -> c.success; hrvAvg >= 20 -> c.warning; else -> c.error }
                    VitalMetricRow("Heart Rate Variability", String.format("%.0f ms", hrvAvg), dot) { onMetricClick(HealthViewModel.MetricType.HEART_RATE_VARIABILITY) }
                }
                if (hasBloodOxygen) {
                    val spo2Avg = healthData.oxygenSaturation.avgPercentage ?: healthData.oxygenSaturation.percentage!!
                    val dot = when { spo2Avg >= 95 -> c.success; spo2Avg >= 90 -> c.warning; else -> c.error }
                    VitalMetricRow("Blood Oxygen", String.format("%.0f%%", spo2Avg), dot) { onMetricClick(HealthViewModel.MetricType.OXYGEN_SATURATION) }
                }
                if (hasBloodGlucose) {
                    val bg = healthData.bloodGlucose.levelMgPerDl!!
                    val dot = when { bg in 70.0..100.0 -> c.success; bg in 60.0..140.0 -> c.warning; else -> c.error }
                    VitalMetricRow("Blood Glucose", String.format("%.0f mg/dL", bg), dot) { onMetricClick(HealthViewModel.MetricType.BLOOD_GLUCOSE) }
                }
                if (hasBloodPressure) {
                    val sys = healthData.bloodPressure.systolicMmHg!!
                    val dot = when { sys in 90.0..120.0 -> c.success; sys in 80.0..140.0 -> c.warning; else -> c.error }
                    VitalMetricRow("Blood Pressure", String.format("%.0f/%.0f mmHg", sys, healthData.bloodPressure.diastolicMmHg), dot) { onMetricClick(HealthViewModel.MetricType.BLOOD_PRESSURE) }
                }
                if (hasBodyTemp) {
                    val temp = healthData.bodyTemperature.temperatureCelsius!!
                    val dot = when { temp in 36.1..37.2 -> c.success; temp in 35.5..38.0 -> c.warning; else -> c.error }
                    VitalMetricRow("Body Temperature", String.format("%.1f°C", temp), dot) { onMetricClick(HealthViewModel.MetricType.BODY_TEMPERATURE) }
                }
                if (hasRespiratoryRate) {
                    val rrAvg = healthData.respiratoryRate.avgRate ?: healthData.respiratoryRate.ratePerMinute!!
                    val dot = when { rrAvg in 12.0..20.0 -> c.success; rrAvg in 8.0..25.0 -> c.warning; else -> c.error }
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
    val c = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = label, color = c.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
            if (statusDot != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.size(8.dp).background(statusDot, CircleShape))
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = value, color = c.onSurface, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = c.outline, modifier = Modifier.size(18.dp))
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
    val c = LocalAppColors.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) { awaitPointerEventScope { while (true) { awaitPointerEvent() } } } // Block touch passthrough
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(c.surfaceLow.copy(alpha = 0.95f))
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
    val c = LocalAppColors.current
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessVeryLow),
        label = "nav_press"
    )
    val activeScale by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.92f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessVeryLow),
        label = "nav_active"
    )
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
                .graphicsLayer { scaleX = scale * activeScale; scaleY = scale * activeScale }
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(c.primaryDim, c.secondary)
                    )
                )
                .clickable(onClick = onClick)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown()
                        pressed = true
                        waitForUpOrCancellation()
                        pressed = false
                    }
                }
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
                .graphicsLayer { scaleX = scale * activeScale; scaleY = scale * activeScale }
                .clickable(onClick = onClick)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown()
                        pressed = true
                        waitForUpOrCancellation()
                        pressed = false
                    }
                }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = label,
                    tint = c.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = label.uppercase(),
                    color = c.onSurfaceVariant.copy(alpha = 0.7f),
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
    surfaceColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val c = LocalAppColors.current
    val bgColor = surfaceColor ?: c.surface
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessVeryLow),
        label = "card_press"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(24.dp))
            .background(bgColor)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    pressed = true
                    waitForUpOrCancellation()
                    pressed = false
                }
            }
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
    // Weighted scoring system — HRV is the dominant factor (like Garmin/Bevel)
    // Each factor contributes a weighted score out of 100
    val now = java.time.Instant.now()

    // ── HRV Score (40% weight) — most important factor ──
    val hrvScore = healthData.heartRateVariability.rmssdMs?.let { hrv ->
        // Scale: 20ms=0, 40ms=50, 60ms=80, 80ms=100
        ((hrv - 20.0) / 60.0 * 100.0).coerceIn(0.0, 100.0)
    } ?: 30.0 // No data = assume low

    // ── Sleep Score (25% weight) ──
    val sleepScore = healthData.sleep.totalDuration?.let { duration ->
        val hours = duration.toMinutes() / 60.0
        when {
            hours >= 8 -> 100.0
            hours >= 7 -> 85.0
            hours >= 6 -> 65.0
            hours >= 5 -> 45.0
            hours >= 4 -> 25.0
            else -> 10.0
        }
    } ?: 20.0

    // ── Awake Time Score (20% weight) ──
    val hoursSinceLastSleep = healthData.sleep.sessions.maxByOrNull { it.endTime }?.endTime?.let { lastWakeTime ->
        java.time.Duration.between(lastWakeTime, now).toHours()
    }
    val awakeScore = when {
        hoursSinceLastSleep == null -> 20.0
        hoursSinceLastSleep >= 18 -> 5.0
        hoursSinceLastSleep >= 16 -> 10.0
        hoursSinceLastSleep >= 14 -> 20.0
        hoursSinceLastSleep >= 12 -> 30.0
        hoursSinceLastSleep >= 10 -> 45.0
        hoursSinceLastSleep >= 8 -> 60.0
        hoursSinceLastSleep >= 4 -> 80.0
        hoursSinceLastSleep >= 2 -> 90.0
        else -> 100.0
    }

    // ── Resting HR Score (10% weight) ──
    val rhr = healthData.restingHeartRate.bpm ?: healthData.heartRate.restingBpm ?: 70
    val rhrScore = when {
        rhr <= 50 -> 100.0
        rhr <= 55 -> 90.0
        rhr <= 60 -> 80.0
        rhr <= 65 -> 70.0
        rhr <= 70 -> 55.0
        rhr <= 75 -> 40.0
        rhr <= 80 -> 25.0
        else -> 10.0
    }

    // ── Activity Score (5% weight) ──
    val steps = healthData.steps.count
    val activityScore = when {
        steps >= 8000 -> 100.0
        steps >= 5000 -> 75.0
        steps >= 3000 -> 50.0
        steps >= 1000 -> 30.0
        else -> 10.0
    }

    // ── Weighted total ──
    val score = (
        hrvScore * 0.40 +
        sleepScore * 0.25 +
        awakeScore * 0.20 +
        rhrScore * 0.10 +
        activityScore * 0.05
    ).toInt().coerceIn(5, 100)

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

// ═══════════════════════════════════════════════════════════
// Bounce Click Modifier — spring scale on press
// ═══════════════════════════════════════════════════════════

@Composable
fun Modifier.bounceClick(onClick: () -> Unit): Modifier {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessVeryLow),
        label = "bounce"
    )
    return this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                pressed = true
                waitForUpOrCancellation()
                pressed = false
            }
        }
        .clickable(
            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
}
