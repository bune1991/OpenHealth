package com.openhealth.openhealth.screens

import com.openhealth.openhealth.ui.theme.*

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lightbulb
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openhealth.openhealth.model.HealthData
import kotlin.math.roundToInt

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

    // Contributing factors analysis
    val factors = mutableListOf<Triple<String, String, String>>() // icon, label, status
    val sleepHours = healthData.sleep.totalDuration?.toMinutes()?.div(60.0) ?: 0.0
    if (sleepHours >= 7) factors.add(Triple("sleep", "Sleep Quality", "Positive"))
    else if (sleepHours >= 5) factors.add(Triple("sleep", "Sleep Quality", "Balanced"))
    else if (sleepHours > 0) factors.add(Triple("sleep", "Sleep Quality", "High"))

    if (hrv > 50) factors.add(Triple("hrv", "HRV Balance", "Positive"))
    else if (hrv > 30) factors.add(Triple("hrv", "HRV Balance", "Balanced"))
    else if (hrv > 0) factors.add(Triple("hrv", "HRV Balance", "High"))

    if (rhr in 50..65) factors.add(Triple("heart", "Resting Heart Rate", "Positive"))
    else if (rhr in 66..75) factors.add(Triple("heart", "Resting Heart Rate", "Balanced"))
    else if (rhr > 75) factors.add(Triple("heart", "Resting Heart Rate", "High"))

    val steps = healthData.steps.count
    if (steps > 5000) factors.add(Triple("activity", "Physical Activity", "Positive"))
    else if (steps > 2000) factors.add(Triple("activity", "Physical Activity", "Balanced"))
    else if (steps > 0) factors.add(Triple("activity", "Physical Activity", "High"))

    if (factors.isEmpty()) {
        factors.add(Triple("info", "Data Collection", "Balanced"))
    }

    // Simulated hourly stress bars for the last 6 hours
    val stressBars = listOf(
        (stressLevel * 0.6f).coerceIn(5f, 95f),
        (stressLevel * 0.8f).coerceIn(5f, 95f),
        (stressLevel * 1.0f).coerceIn(5f, 95f),
        (stressLevel * 0.7f).coerceIn(5f, 95f),
        (stressLevel * 0.9f).coerceIn(5f, 95f),
        (stressLevel * 0.5f).coerceIn(5f, 95f)
    )
    val maxBar = stressBars.max()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Stress Analysis",
                        color = ElectricIndigo,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = ElectricIndigo)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceLowest)
            )
        },
        containerColor = SurfaceLowest
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Arc Gauge Hero ──
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(220.dp)
                    ) {
                        val gradientColors = listOf(VibrantMagenta, ElectricIndigo)
                        val bgArcColor = SurfaceHighest
                        Canvas(modifier = Modifier.size(220.dp)) {
                            val strokeW = 16.dp.toPx()
                            val arcSize = Size(size.width - strokeW, size.height - strokeW)
                            val topLeft = Offset(strokeW / 2, strokeW / 2)

                            // Background arc 270 degrees
                            drawArc(
                                color = bgArcColor,
                                startAngle = 135f,
                                sweepAngle = 270f,
                                useCenter = false,
                                style = Stroke(width = strokeW, cap = StrokeCap.Round),
                                topLeft = topLeft,
                                size = arcSize
                            )

                            // Gradient stress arc
                            val sweepAngle = 270f * (stressLevel / 100f)
                            drawArc(
                                brush = Brush.sweepGradient(gradientColors),
                                startAngle = 135f,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(width = strokeW, cap = StrokeCap.Round),
                                topLeft = topLeft,
                                size = arcSize
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stressLevel.toString(),
                                color = TextOnSurface,
                                fontWeight = FontWeight.Black,
                                fontSize = 56.sp
                            )
                            Text(
                                text = stressLabel.uppercase(),
                                color = VibrantMagenta,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // "Optimal Recovery Zone" pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(SurfaceLow)
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Optimal Recovery Zone",
                            color = TextOnSurfaceVariant,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // ── Stats Grid: HRV + Resting HR ──
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // HRV Card — centered, uppercase label
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(SurfaceLow)
                            .padding(20.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "HRV",
                                color = ElectricIndigo,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = if (hrv > 0) String.format("%.0f", hrv) else "--",
                                    color = TextOnSurface,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 32.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "ms",
                                    color = TextOnSurfaceVariant,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            val hrvTrend = if (hrv > 50) "+12%" else if (hrv > 30) "Stable" else "-8%"
                            val hrvTrendColor = if (hrv > 50) SuccessGreen else if (hrv > 30) TextOnSurfaceVariant else ErrorRed
                            Text(
                                text = hrvTrend,
                                color = hrvTrendColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Resting HR Card — centered, uppercase label
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(SurfaceLow)
                            .padding(20.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "RESTING HR",
                                color = VibrantMagenta,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = if (rhr > 0) rhr.toString() else "--",
                                    color = TextOnSurface,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 32.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "bpm",
                                    color = TextOnSurfaceVariant,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            val rhrTrend = if (rhr in 50..65) "Stable" else if (rhr in 66..75) "Stable" else "+4%"
                            val rhrTrendColor = if (rhr <= 75) TextOnSurfaceVariant else ErrorRed
                            Text(
                                text = rhrTrend,
                                color = rhrTrendColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // ── Resilience Coaching Card ──
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceHigh)
                        .padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Gradient icon circle
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(ElectricIndigo, VibrantMagenta)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "\uD83E\uDDD8",
                                fontSize = 20.sp
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Resilience Coaching",
                                color = TextOnSurface,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = coaching,
                                color = TextOnSurfaceVariant,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            // "Start Exercise" pill button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(ElectricIndigo)
                                    .padding(horizontal = 20.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Start Exercise",
                                    color = OnIndigo,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // ── Stress Intensity Chart ──
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceLow)
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Stress Intensity",
                                color = TextOnSurface,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "LAST 6 HOURS",
                                color = TextSubtle,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Bar chart
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            val hours = listOf("6h", "5h", "4h", "3h", "2h", "1h")
                            stressBars.forEachIndexed { index, value ->
                                val isMax = value == maxBar
                                val barHeight = (value / 100f * 100).dp
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(28.dp)
                                            .height(barHeight)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isMax) Brush.verticalGradient(
                                                    listOf(VibrantMagenta, ElectricIndigo)
                                                )
                                                else Brush.verticalGradient(
                                                    listOf(
                                                        ElectricIndigo.copy(alpha = 0.4f),
                                                        VibrantMagenta.copy(alpha = 0.6f)
                                                    )
                                                )
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = hours[index],
                                        color = TextSubtle,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Contributing Factors ──
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Contributing Factors",
                        color = TextOnSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    factors.forEach { (iconType, label, status) ->
                        StressFactorRow(
                            iconType = iconType,
                            label = label,
                            status = status
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun StressFactorRow(
    iconType: String,
    label: String,
    status: String
) {
    val icon = when (iconType) {
        "sleep" -> Icons.Default.Bedtime
        "hrv" -> Icons.Default.FavoriteBorder
        "heart" -> Icons.Default.Favorite
        "activity" -> Icons.Default.FitnessCenter
        else -> Icons.Default.Lightbulb
    }
    val iconColor = when (iconType) {
        "sleep" -> ElectricIndigo
        "hrv" -> SoftLavender
        "heart" -> VibrantMagenta
        "activity" -> SoftLavender
        else -> ElectricIndigo
    }

    val (badgeBg, badgeText, badgeLabel) = when (status) {
        "Positive" -> Triple(ElectricIndigo.copy(alpha = 0.12f), ElectricIndigo, "POSITIVE")
        "High" -> Triple(VibrantMagenta.copy(alpha = 0.12f), VibrantMagenta, "HIGH")
        else -> Triple(SurfaceHighest, TextOnSurfaceVariant, "BALANCED")
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(SurfaceHigh.copy(alpha = 0.5f))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = label,
                    color = TextOnSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(badgeBg)
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Text(
                    text = badgeLabel,
                    color = badgeText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
