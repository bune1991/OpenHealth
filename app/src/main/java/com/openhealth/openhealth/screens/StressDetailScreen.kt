package com.openhealth.openhealth.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openhealth.openhealth.model.HealthData
import com.openhealth.openhealth.ui.theme.BackgroundBlack
import com.openhealth.openhealth.ui.theme.SurfaceDark
import com.openhealth.openhealth.ui.theme.TextPrimary
import com.openhealth.openhealth.ui.theme.TextSecondary
import com.openhealth.openhealth.ui.theme.TextTertiary
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private val StressLow = Color(0xFF4CD964)
private val StressMod = Color(0xFFFFCC00)
private val StressHigh = Color(0xFFFF9500)
private val StressVeryHigh = Color(0xFFFF3B30)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StressDetailScreen(
    healthData: HealthData,
    onBackClick: () -> Unit
) {
    val hrv = healthData.heartRateVariability.rmssdMs ?: 0.0
    val avgHr = healthData.heartRate.currentBpm ?: 0
    val rhr = healthData.restingHeartRate.bpm ?: 0

    // Stress: inverse of HRV (same formula as dashboard)
    val stressLevel = ((80.0 - hrv.coerceIn(10.0, 80.0)) / 70.0 * 100).toInt().coerceIn(0, 100)
    val stressLabel = when {
        stressLevel < 25 -> "Low"
        stressLevel < 50 -> "Moderate"
        stressLevel < 75 -> "High"
        else -> "Very High"
    }
    val stressColor = when {
        stressLevel < 25 -> StressLow
        stressLevel < 50 -> StressMod
        stressLevel < 75 -> StressHigh
        else -> StressVeryHigh
    }

    // Simulated stress breakdown based on level
    val highPct = (stressLevel * 0.7).roundToInt().coerceIn(0, 100)
    val medPct = ((100 - highPct) * 0.6).roundToInt()
    val lowPct = 100 - highPct - medPct

    // Coaching text
    val coaching = when {
        stressLevel < 25 -> "Your stress levels are low. Great job managing your stress! Keep up your current routine."
        stressLevel < 50 -> "You're experiencing moderate stress. Consider taking short breaks and practicing deep breathing to stay balanced."
        stressLevel < 75 -> "Your stress is elevated. Try a 10-minute walk, meditation, or breathing exercises to bring it down."
        else -> "You're experiencing high stress. Consider practicing mindfulness or meditation to lower your stress. Avoid intense exercise until stress decreases."
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Stress", color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.titleLarge)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stress Gauge
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Gauge
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(180.dp)
                        ) {
                            val arcBgColor = MaterialTheme.colorScheme.outlineVariant
                            Canvas(modifier = Modifier.size(180.dp)) {
                                val strokeW = 14.dp.toPx()
                                val arcSize = Size(size.width - strokeW, size.height - strokeW)
                                val topLeft = Offset(strokeW / 2, strokeW / 2)

                                // Background arc (180 degrees, bottom half open)
                                drawArc(
                                    color = arcBgColor,
                                    startAngle = 135f,
                                    sweepAngle = 270f,
                                    useCenter = false,
                                    style = Stroke(width = strokeW, cap = StrokeCap.Round),
                                    topLeft = topLeft,
                                    size = arcSize
                                )

                                // Stress arc
                                val sweepAngle = 270f * (stressLevel / 100f)
                                drawArc(
                                    color = stressColor,
                                    startAngle = 135f,
                                    sweepAngle = sweepAngle,
                                    useCenter = false,
                                    style = Stroke(width = strokeW, cap = StrokeCap.Round),
                                    topLeft = topLeft,
                                    size = arcSize
                                )

                                // Scale markers
                                val cx = size.width / 2
                                val cy = size.height / 2
                                val radius = (size.width - strokeW) / 2
                                for (i in 0..10) {
                                    val angle = Math.toRadians((135.0 + i * 27.0))
                                    val innerR = radius - strokeW
                                    val outerR = radius - strokeW - 6.dp.toPx()
                                    drawLine(
                                        color = Color(0xFF555555),
                                        start = Offset(
                                            cx + (innerR * cos(angle)).toFloat(),
                                            cy + (innerR * sin(angle)).toFloat()
                                        ),
                                        end = Offset(
                                            cx + (outerR * cos(angle)).toFloat(),
                                            cy + (outerR * sin(angle)).toFloat()
                                        ),
                                        strokeWidth = 1.5f
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = stressLevel.toString(),
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 42.sp
                                )
                                Text(
                                    text = stressLabel,
                                    color = stressColor,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Scale labels
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("0", color = MaterialTheme.colorScheme.outline, fontSize = 12.sp)
                            Text("100", color = MaterialTheme.colorScheme.outline, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Average HRV + Average HR cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Average HRV",
                        value = if (hrv > 0) String.format("%.0f", hrv) else "--",
                        unit = "ms",
                        color = Color(0xFF00B4D8),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Resting HR",
                        value = if (rhr > 0) rhr.toString() else "--",
                        unit = "bpm",
                        color = Color(0xFFFF6B6B),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Coaching
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Coaching",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = coaching,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            // Stress Breakdown
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Stress Breakdown",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        BreakdownRow("High", highPct, StressHigh)
                        Spacer(modifier = Modifier.height(12.dp))
                        BreakdownRow("Medium", medPct, StressMod)
                        Spacer(modifier = Modifier.height(12.dp))
                        BreakdownRow("Low", lowPct, StressLow)
                    }
                }
            }

            // What affects stress
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "What Affects Your Stress",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val factors = mutableListOf<Pair<String, String>>()

                        val sleepHours = healthData.sleep.totalDuration?.toMinutes()?.div(60.0) ?: 0.0
                        if (sleepHours < 6) factors.add("😴" to "Short sleep (${String.format("%.0f", sleepHours)}h) increases stress hormones")
                        else if (sleepHours >= 7) factors.add("😴" to "Good sleep (${String.format("%.0f", sleepHours)}h) helps manage stress")

                        if (rhr > 75) factors.add("❤️" to "Elevated resting HR ($rhr bpm) may indicate physical stress")
                        else if (rhr in 50..65) factors.add("❤️" to "Healthy resting HR ($rhr bpm) supports stress recovery")

                        if (hrv < 30) factors.add("📊" to "Low HRV (${String.format("%.0f", hrv)} ms) shows reduced stress resilience")
                        else if (hrv > 50) factors.add("📊" to "Good HRV (${String.format("%.0f", hrv)} ms) indicates strong stress resilience")

                        val steps = healthData.steps.count
                        if (steps < 1000) factors.add("🚶" to "Low activity ($steps steps) — movement helps reduce stress")
                        else if (steps > 5000) factors.add("🚶" to "Good activity ($steps steps) — exercise is a natural stress reliever")

                        if (factors.isEmpty()) {
                            factors.add("💡" to "Keep tracking to see what affects your stress levels")
                        }

                        factors.forEach { (emoji, text) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(text = emoji, fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = text,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun BreakdownRow(label: String, percent: Int, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 14.sp,
            modifier = Modifier.width(60.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .background(MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percent / 100f)
                    .height(8.dp)
                    .background(color, RoundedCornerShape(4.dp))
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$percent%",
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.width(40.dp)
        )
    }
}
