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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openhealth.openhealth.viewmodel.MetricSummary
import com.openhealth.openhealth.viewmodel.ReportsData
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

private val Accent = Color(0xFF00BCD4)
private val TrendUp = Color(0xFF4CD964)
private val TrendDown = Color(0xFFFF3B30)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    reportsData: ReportsData,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Reports",
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (reportsData.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Accent)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Weekly Summary
                item {
                    val today = LocalDate.now(ZoneId.systemDefault())
                    val startOfWeek = today.with(java.time.DayOfWeek.MONDAY)
                    val dateFormat = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())

                    SummaryCard(
                        title = "This Week",
                        subtitle = "${startOfWeek.format(dateFormat)} - ${today.format(dateFormat)}",
                        summaries = reportsData.summaries,
                        valueSelector = { it.thisWeekValue },
                        showComparison = true
                    )
                }

                // Weekly Steps Bar Chart
                if (reportsData.weeklyStepsChart.isNotEmpty()) {
                    item {
                        WeeklyStepsChart(data = reportsData.weeklyStepsChart)
                    }
                }

                // Monthly Summary
                item {
                    SummaryCard(
                        title = "30-Day Total",
                        subtitle = "Last 30 days",
                        summaries = reportsData.summaries,
                        valueSelector = { it.monthValue },
                        showComparison = false
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    subtitle: String,
    summaries: List<MetricSummary>,
    valueSelector: (MetricSummary) -> Double,
    showComparison: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(16.dp))

            summaries.forEachIndexed { index, summary ->
                SummaryRow(
                    summary = summary,
                    value = valueSelector(summary),
                    showComparison = showComparison
                )
                if (index < summaries.size - 1) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(
    summary: MetricSummary,
    value: Double,
    showComparison: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Label
        Text(
            text = summary.label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )

        // Value
        val formattedValue = when {
            summary.unit == "hrs" -> String.format("%.1f", value)
            summary.unit == "km" -> String.format("%.1f", value)
            summary.unit == "bpm" -> String.format("%.0f", value)
            value >= 10000 -> "${(value / 1000).roundToInt()}k"
            value >= 1000 -> String.format("%.1fk", value / 1000)
            else -> value.roundToInt().toString()
        }

        val avgPrefix = if (summary.isAverage) "avg " else ""
        Text(
            text = "$avgPrefix$formattedValue ${summary.unit}",
            style = MaterialTheme.typography.bodyLarge,
            color = Accent,
            fontWeight = FontWeight.Bold
        )

        // Comparison arrow (week over week)
        if (showComparison && summary.lastWeekValue > 0) {
            Spacer(modifier = Modifier.width(8.dp))
            val change = if (summary.lastWeekValue > 0) {
                ((summary.thisWeekValue - summary.lastWeekValue) / summary.lastWeekValue * 100).roundToInt()
            } else 0

            val isPositive = change > 0
            val isBetter = if (summary.isBetterWhenHigher) isPositive else !isPositive

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.width(70.dp)
            ) {
                Icon(
                    imageVector = when {
                        change > 0 -> Icons.AutoMirrored.Filled.TrendingUp
                        change < 0 -> Icons.AutoMirrored.Filled.TrendingDown
                        else -> Icons.Default.Remove
                    },
                    contentDescription = null,
                    tint = when {
                        change == 0 -> MaterialTheme.colorScheme.outline
                        isBetter -> TrendUp
                        else -> TrendDown
                    },
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "${if (change > 0) "+" else ""}$change%",
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        change == 0 -> MaterialTheme.colorScheme.outline
                        isBetter -> TrendUp
                        else -> TrendDown
                    },
                    fontSize = 11.sp
                )
            }
        } else if (showComparison) {
            Spacer(modifier = Modifier.width(78.dp))
        }
    }
}

@Composable
private fun WeeklyStepsChart(data: List<com.openhealth.openhealth.model.DailyDataPoint>) {
    val maxValue = data.maxOfOrNull { it.value } ?: 1.0
    val today = LocalDate.now(ZoneId.systemDefault())
    val dayFormatter = DateTimeFormatter.ofPattern("EEE", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Steps This Week",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                data.forEach { point ->
                    val heightFraction = if (maxValue > 0) (point.value / maxValue).toFloat() else 0f
                    val isToday = point.date == today

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Value label
                        Text(
                            text = if (point.value >= 1000) "${(point.value / 1000).roundToInt()}k"
                            else point.value.roundToInt().toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isToday) Accent else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        // Bar
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .fillMaxHeight(0.75f * heightFraction.coerceIn(0.03f, 1f))
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = if (isToday) listOf(Accent, Accent.copy(alpha = 0.7f))
                                        else listOf(Accent.copy(alpha = 0.7f), Accent.copy(alpha = 0.4f))
                                    )
                                )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Day label
                        Text(
                            text = point.date.format(dayFormatter),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isToday) Accent else MaterialTheme.colorScheme.outline,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
