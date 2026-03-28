package com.openhealth.openhealth.screens

import com.openhealth.openhealth.ui.theme.*

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openhealth.openhealth.viewmodel.MetricSummary
import com.openhealth.openhealth.viewmodel.ReportsData
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private val GradientBrush = Brush.horizontalGradient(
    colors = listOf(ElectricIndigo, VibrantMagenta)
)

private val TrendUp = SuccessGreen
private val TrendDown = ErrorRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    reportsData: ReportsData,
    onBackClick: () -> Unit,
    onMetricClick: (com.openhealth.openhealth.viewmodel.HealthViewModel.MetricType) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Weekly Summary",
                        color = TextOnSurface,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextOnSurfaceVariant
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
        if (reportsData.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ElectricIndigo)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── Section: Weekly Trends vs Last Week ──
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "WEEKLY TRENDS VS LAST WEEK",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextOnSurfaceVariant,
                        letterSpacing = 1.5.sp,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // ── 3 Trend Cards ──
                val trendMetrics = listOf("Sleep", "Steps", "Calories")
                    .mapNotNull { label -> reportsData.summaries.find { it.label == label } }

                // If we don't have all 3, fall back to first 3 summaries
                val displayTrends = if (trendMetrics.size >= 3) trendMetrics
                    else reportsData.summaries.take(3)

                val trendLabels = mapOf(
                    "Sleep" to "Sleep",
                    "Steps" to "Activity / Steps",
                    "Calories" to "Readiness"
                )

                displayTrends.forEach { summary ->
                    item {
                        TrendCard(
                            title = trendLabels[summary.label] ?: summary.label,
                            summary = summary
                        )
                    }
                }

                // ── Steps Breakdown Chart ──
                if (reportsData.weeklyStepsChart.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        StepsBreakdownChart(data = reportsData.weeklyStepsChart)
                    }
                }

                // ── Monthly Totals ──
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "MONTHLY TOTALS",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextOnSurfaceVariant,
                        letterSpacing = 1.5.sp,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    MonthlyTotalsCard(summaries = reportsData.summaries, onMetricClick = onMetricClick)
                }

                // ── Key Metrics Comparison ──
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "KEY METRICS COMPARISON",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextOnSurfaceVariant,
                        letterSpacing = 1.5.sp,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                reportsData.summaries.forEach { summary ->
                    item {
                        KeyMetricPill(summary = summary)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Trend Card
// ═══════════════════════════════════════════════════════════

@Composable
private fun TrendCard(
    title: String,
    summary: MetricSummary
) {
    val change = if (summary.lastWeekValue > 0) {
        ((summary.thisWeekValue - summary.lastWeekValue) / summary.lastWeekValue * 100).roundToInt()
    } else 0

    val isPositive = change > 0
    val isBetter = if (summary.isBetterWhenHigher) isPositive else !isPositive
    val trendColor = when {
        change == 0 -> TextSubtle
        isBetter -> TrendUp
        else -> TrendDown
    }

    val formattedValue = formatMetricValue(summary.thisWeekValue, summary.unit)
    val maxReasonable = summary.lastWeekValue * 2.0
    val progressFraction = if (maxReasonable > 0) {
        (summary.thisWeekValue / maxReasonable).toFloat().coerceIn(0.05f, 1f)
    } else 0.5f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceLow)
            .padding(24.dp)
    ) {
        Column {
            // Top row: metric name + trend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextOnSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )

                if (summary.lastWeekValue > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when {
                                change > 0 -> Icons.AutoMirrored.Filled.TrendingUp
                                change < 0 -> Icons.AutoMirrored.Filled.TrendingDown
                                else -> Icons.Default.Remove
                            },
                            contentDescription = null,
                            tint = trendColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${abs(change)}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = trendColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Big number
            Text(
                text = "$formattedValue ${summary.unit}",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextOnSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Thin gradient progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(SurfaceHighest)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressFraction.coerceAtLeast(0.01f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp))
                        .background(GradientBrush)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Steps Breakdown Chart
// ═══════════════════════════════════════════════════════════

@Composable
private fun StepsBreakdownChart(data: List<com.openhealth.openhealth.model.DailyDataPoint>) {
    val maxValue = data.maxOfOrNull { it.value } ?: 1.0
    val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceLow)
            .padding(28.dp)
    ) {
        Column {
            Text(
                text = "Steps Breakdown",
                style = MaterialTheme.typography.titleMedium,
                color = TextOnSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Pad data to 7 days if needed
                val paddedData = if (data.size < 7) {
                    val startOfWeek = LocalDate.now(ZoneId.systemDefault())
                        .with(java.time.DayOfWeek.MONDAY)
                    (0 until 7).map { offset ->
                        val date = startOfWeek.plusDays(offset.toLong())
                        data.find { it.date == date }
                            ?: com.openhealth.openhealth.model.DailyDataPoint(
                                date = date,
                                value = 0.0,
                                unit = "steps"
                            )
                    }
                } else data.take(7)

                paddedData.forEachIndexed { index, point ->
                    val heightFraction = if (maxValue > 0) {
                        (point.value / maxValue).toFloat().coerceIn(0f, 1f)
                    } else 0f

                    val label = dayLabels.getOrElse(index) { "" }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Bar track with gradient fill
                        Box(
                            modifier = Modifier
                                .width(32.dp)
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceHighest),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            if (heightFraction > 0f) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(heightFraction)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(GradientBrush)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Day label
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSubtle,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Monthly Totals Card
// ═══════════════════════════════════════════════════════════

@Composable
private fun MonthlyTotalsCard(summaries: List<MetricSummary>, onMetricClick: (com.openhealth.openhealth.viewmodel.HealthViewModel.MetricType) -> Unit = {}) {
    val caloriesSummary = summaries.find { it.label == "Calories" }
    val exerciseSummary = summaries.find { it.label == "Exercise" }
    val stepsSummary = summaries.find { it.label == "Steps" }

    data class MonthlyRow(
        val icon: ImageVector,
        val iconColor: Color,
        val label: String,
        val value: String,
        val metricType: com.openhealth.openhealth.viewmodel.HealthViewModel.MetricType
    )

    val rows = listOfNotNull(
        caloriesSummary?.let {
            MonthlyRow(
                Icons.Default.LocalFireDepartment,
                CardCalories,
                "Total Move",
                "${formatMetricValue(it.monthValue, it.unit)} kcal",
                com.openhealth.openhealth.viewmodel.HealthViewModel.MetricType.CALORIES
            )
        },
        exerciseSummary?.let {
            MonthlyRow(
                Icons.AutoMirrored.Filled.DirectionsRun,
                CardExercise,
                "Total Exercise",
                "${formatMetricValue(it.monthValue, it.unit)} min",
                com.openhealth.openhealth.viewmodel.HealthViewModel.MetricType.EXERCISE
            )
        },
        stepsSummary?.let {
            MonthlyRow(
                Icons.AutoMirrored.Filled.DirectionsWalk,
                CardSteps,
                "Total Stand",
                "${formatMetricValue(it.monthValue / 60.0, "hrs")} hrs",
                com.openhealth.openhealth.viewmodel.HealthViewModel.MetricType.STEPS
            )
        }
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceLow)
            .padding(vertical = 8.dp)
    ) {
        Column {
            rows.forEachIndexed { index, row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onMetricClick(row.metricType) }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icon circle
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(row.iconColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = row.icon,
                            contentDescription = null,
                            tint = row.iconColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Label
                    Text(
                        text = row.label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextOnSurface,
                        modifier = Modifier.weight(1f)
                    )

                    // Value
                    Text(
                        text = row.value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextOnSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Chevron
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = TextSubtle,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (index < rows.size - 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .height(1.dp)
                            .background(SurfaceHighest)
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Key Metrics Comparison — Pill Row
// ═══════════════════════════════════════════════════════════

@Composable
private fun KeyMetricPill(summary: MetricSummary) {
    val formattedValue = formatMetricValue(summary.thisWeekValue, summary.unit)

    // Determine status based on week-over-week change
    val change = if (summary.lastWeekValue > 0) {
        ((summary.thisWeekValue - summary.lastWeekValue) / summary.lastWeekValue * 100).roundToInt()
    } else 0
    val isPositive = change > 0
    val isBetter = if (summary.isBetterWhenHigher) isPositive else !isPositive

    val statusLabel = when {
        change == 0 || summary.lastWeekValue == 0.0 -> "Normal"
        isBetter -> "Optimal"
        else -> "Normal"
    }
    val statusColor = when (statusLabel) {
        "Optimal" -> SuccessGreen
        else -> TextOnSurfaceVariant
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceLow)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Label
            Text(
                text = summary.label,
                style = MaterialTheme.typography.bodyLarge,
                color = TextOnSurface,
                modifier = Modifier.weight(1f)
            )

            // Value
            Text(
                text = "$formattedValue ${summary.unit}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextOnSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Status badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(statusColor.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Helpers
// ═══════════════════════════════════════════════════════════

private fun formatMetricValue(value: Double, unit: String): String {
    return when {
        unit == "hrs" -> String.format("%.1f", value)
        unit == "km" -> String.format("%.1f", value)
        unit == "bpm" -> String.format("%.0f", value)
        value >= 10000 -> "${(value / 1000).roundToInt()}k"
        value >= 1000 -> String.format("%.1fk", value / 1000)
        else -> value.roundToInt().toString()
    }
}
