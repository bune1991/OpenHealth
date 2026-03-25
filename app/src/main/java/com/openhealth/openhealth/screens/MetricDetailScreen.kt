package com.openhealth.openhealth.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.openhealth.openhealth.ui.theme.BackgroundBlack
import com.openhealth.openhealth.ui.theme.CardBloodGlucose
import com.openhealth.openhealth.ui.theme.CardBloodPressure
import com.openhealth.openhealth.ui.theme.CardBodyFat
import com.openhealth.openhealth.ui.theme.CardBodyTemperature
import com.openhealth.openhealth.ui.theme.CardBodyWater
import com.openhealth.openhealth.ui.theme.CardBoneMass
import com.openhealth.openhealth.ui.theme.CardBMR
import com.openhealth.openhealth.ui.theme.CardCalories
import com.openhealth.openhealth.ui.theme.CardDistance
import com.openhealth.openhealth.ui.theme.CardExercise
import com.openhealth.openhealth.ui.theme.CardFloors
import com.openhealth.openhealth.ui.theme.CardHeartRate
import com.openhealth.openhealth.ui.theme.CardHRV
import com.openhealth.openhealth.ui.theme.CardLeanBodyMass
import com.openhealth.openhealth.ui.theme.CardRespiratoryRate
import com.openhealth.openhealth.ui.theme.CardSleep
import com.openhealth.openhealth.ui.theme.CardSpO2
import com.openhealth.openhealth.ui.theme.CardSteps
import com.openhealth.openhealth.ui.theme.CardVo2Max
import com.openhealth.openhealth.ui.theme.CardWeight
import com.openhealth.openhealth.ui.theme.SurfaceDark
import com.openhealth.openhealth.ui.theme.SurfaceVariant
import com.openhealth.openhealth.ui.theme.TextPrimary
import com.openhealth.openhealth.ui.theme.TextSecondary
import com.openhealth.openhealth.ui.theme.TextTertiary
import com.openhealth.openhealth.viewmodel.HealthViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
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
    onDateChange: ((LocalDate) -> Unit)? = null
) {
    val metricInfo = getMetricInfo(metricType)

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    val selectedDateValue = remember(metricHistory, selectedDate) {
        metricHistory?.allHistoricalData?.find { it.date == selectedDate }?.value
            ?: metricHistory?.todayValue
            ?: 0.0
    }

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
                            tint = if (selectedDate.isBefore(LocalDate.now())) TextPrimary else TextTertiary
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundBlack)
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
                        // Today's Value Card
                        item {
                            val isToday = selectedDate == LocalDate.now()
                            val dateLabel = if (isToday) "Today" else selectedDate.format(
                                DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
                            )
                            TodayValueCard(
                                value = formatValue(selectedDateValue, metricInfo.decimalPlaces),
                                unit = metricHistory?.unit ?: "",
                                color = metricInfo.color,
                                dateLabel = dateLabel,
                                isLoading = isLoading
                            )
                        }

                        // Line Chart - 30 Day Trend
                        if (metricHistory?.last30Days?.isNotEmpty() == true) {
                            item {
                                LineChartCard(
                                    data = metricHistory.last30Days,
                                    color = metricInfo.color,
                                    title = "30 Day Trend",
                                    isSleep = metricType == HealthViewModel.MetricType.SLEEP
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
                                    isSleep = metricType == HealthViewModel.MetricType.SLEEP
                                )
                            }
                        }

                        // Statistics Cards
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StatCard(
                                    title = "30-Day Avg",
                                    value = formatValue(metricHistory?.monthlyAverage ?: 0.0, metricInfo.decimalPlaces),
                                    unit = metricHistory?.unit ?: "",
                                    icon = Icons.Default.TrendingUp,
                                    color = metricInfo.color,
                                    modifier = Modifier.weight(1f),
                                    isLoading = isLoading
                                )

                                StatCard(
                                    title = "Best Day",
                                    value = metricHistory?.bestDay?.let {
                                        formatValue(it.value, metricInfo.decimalPlaces)
                                    } ?: "--",
                                    unit = metricHistory?.unit ?: "",
                                    icon = Icons.Default.EmojiEvents,
                                    color = metricInfo.color,
                                    modifier = Modifier.weight(1f),
                                    isLoading = isLoading
                                )
                            }
                        }

                        // Best Day Info
                        metricHistory?.bestDay?.let { bestDay ->
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
                        if (metricType == HealthViewModel.MetricType.SLEEP && metricHistory?.sleepStages != null) {
                            item {
                                SleepStagesChart(
                                    sleepStages = metricHistory.sleepStages
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
                                    text = if (totalRecords > 0) "All History ($totalRecords days)" else "All History",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
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
private fun LineChartCard(
    data: List<DailyDataPoint>,
    color: Color,
    title: String,
    isSleep: Boolean = false
) {
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
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
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
                val labels = listOf(
                    data.firstOrNull()?.date?.format(DateTimeFormatter.ofPattern("MMM d")) ?: "",
                    data.getOrNull(data.size / 2)?.date?.format(DateTimeFormatter.ofPattern("MMM d")) ?: "",
                    data.lastOrNull()?.date?.format(DateTimeFormatter.ofPattern("MMM d")) ?: ""
                )
                labels.forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
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
    isSleep: Boolean = false
) {
    if (data.size < 2) return

    val values = data.map { it.value }
    val maxValue = values.maxOrNull() ?: 1.0
    val minValue = values.minOrNull() ?: 0.0
    val range = maxValue - minValue

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
                    val lastY = padding + chartHeight * (1 - if (range > 0) (data.last().value - minValue) / range else 0.5).toFloat()

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
                        colors = listOf(fillColor, fillColor.copy(alpha = 0.0f)),
                        startY = padding,
                        endY = height - padding
                    )
                )

                // Draw line
                drawPath(
                    path = path,
                    color = lineColor,
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
                            color = BackgroundBlack,
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
    isSleep: Boolean = false
) {
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
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
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
                isSleep = isSleep
            )
        }
    }
}

@Composable
private fun BarChart(
    data: List<DailyDataPoint>,
    barColor: Color,
    modifier: Modifier = Modifier,
    isSleep: Boolean = false
) {
    if (data.isEmpty()) return

    val values = data.map { it.value }
    val maxValue = values.maxOrNull() ?: 1.0
    val minValue = 0.0

    val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEachIndexed { index, point ->
            val dayOfWeek = point.date.dayOfWeek.value % 7
            val label = dayLabels.getOrNull(dayOfWeek) ?: ""

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                // Value label on top of bar
                val displayValue = if (isSleep) {
                    "${point.value.toInt()}h"
                } else {
                    if (point.value >= 1000) {
                        "${(point.value / 1000).roundToInt()}k"
                    } else {
                        point.value.roundToInt().toString()
                    }
                }

                Text(
                    text = displayValue,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 10.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Animated bar
                val targetHeight = if (maxValue > 0) (point.value / maxValue).toFloat() else 0f
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
                    color = TextTertiary,
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
            containerColor = SurfaceDark
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
                text = "Sleep Stages",
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
                        color = BackgroundBlack,
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
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
                    if (lightPercent > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(lightPercent)
                                .background(color = Color(0xFF1976D2))
                        )
                    }
                    if (remPercent > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(remPercent)
                                .background(color = Color(0xFF00BCD4))
                        )
                    }
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
