package com.openhealth.openhealth.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Spring
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.openhealth.openhealth.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepCoachScreen(
    healthData: HealthData,
    onBackClick: () -> Unit
) {
    val c = LocalAppColors.current
    val sleep = healthData.sleep
    val stages = sleep.stages
    val totalMinutes = sleep.totalDuration?.toMinutes() ?: 0
    val sleepScore = calculateSleepQuality(healthData)

    // Sleep debt: assume 8h target, calculate deficit
    val targetMinutes = 480L
    val debtMinutes = (targetMinutes - totalMinutes).coerceAtLeast(0)
    val debtHours = debtMinutes / 60
    val debtMins = debtMinutes % 60

    // Optimal bedtime suggestion (based on wake time + 8h)
    val lastWake = sleep.sessions.maxByOrNull { it.endTime }?.endTime
    val bedtimeSuggestion = if (lastWake != null) {
        val wakeHour = lastWake.atZone(java.time.ZoneId.systemDefault()).hour
        val suggestedBedHour = (wakeHour - 8 + 24) % 24
        String.format("%02d:00", suggestedBedHour)
    } else "23:00"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.NightsStay, null, tint = c.tertiary, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sleep Coach", color = c.onSurface, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = c.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = c.background)
            )
        },
        containerColor = c.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Sleep Score Ring ──
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                val animatedScore by animateFloatAsState(
                    targetValue = sleepScore / 100f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessVeryLow),
                    label = "sleep_arc"
                )
                val animatedNumber by animateIntAsState(
                    targetValue = sleepScore,
                    animationSpec = tween(1000),
                    label = "sleep_num"
                )

                // Glow
                Canvas(modifier = Modifier.size(220.dp)) {
                    drawCircle(color = c.tertiary.copy(alpha = 0.12f), radius = size.minDimension / 2)
                }

                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(180.dp)) {
                    Canvas(modifier = Modifier.size(180.dp)) {
                        val strokeWidth = 12.dp.toPx()
                        val arcSize = Size(size.width - strokeWidth * 2, size.height - strokeWidth * 2)
                        val topLeft = Offset(strokeWidth, strokeWidth)

                        drawArc(
                            color = c.surfaceHighest.copy(alpha = 0.6f),
                            startAngle = -90f, sweepAngle = 360f, useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                            topLeft = topLeft, size = arcSize
                        )
                        drawArc(
                            color = c.tertiary,
                            startAngle = -90f, sweepAngle = 360f * animatedScore, useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                            topLeft = topLeft, size = arcSize
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SLEEP SCORE", fontSize = 10.sp, color = c.onSurfaceVariant, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                        Text("$animatedNumber", fontSize = 56.sp, fontWeight = FontWeight.Black, color = c.onSurface)
                        Text(
                            when { sleepScore >= 80 -> "Excellent"; sleepScore >= 60 -> "Good"; sleepScore >= 40 -> "Fair"; else -> "Poor" },
                            fontSize = 13.sp, color = c.tertiary, fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // ── Sleep Duration ──
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(c.surfaceLow).padding(20.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${sleep.hours}h ${sleep.minutes}m", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = c.onSurface)
                        Text("SLEPT", fontSize = 10.sp, color = c.onSurfaceVariant, letterSpacing = 1.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("8h 00m", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = c.outline)
                        Text("TARGET", fontSize = 10.sp, color = c.onSurfaceVariant, letterSpacing = 1.sp)
                    }
                }
            }

            // ── Sleep Debt ──
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                    .background(if (debtMinutes > 60) c.error.copy(alpha = 0.1f) else c.success.copy(alpha = 0.1f))
                    .padding(20.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.WbTwilight, null,
                        tint = if (debtMinutes > 60) c.error else c.success,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            if (debtMinutes > 0) "Sleep Debt: ${debtHours}h ${debtMins}m" else "No Sleep Debt",
                            fontSize = 16.sp, fontWeight = FontWeight.Bold,
                            color = if (debtMinutes > 60) c.error else c.success
                        )
                        Text(
                            if (debtMinutes > 120) "You're running a significant deficit. Prioritize sleep tonight."
                            else if (debtMinutes > 0) "Minor deficit. An extra 30min tonight will help."
                            else "You're well-rested! Keep up this pattern.",
                            fontSize = 12.sp, color = c.onSurfaceVariant, lineHeight = 16.sp
                        )
                    }
                }
            }

            // ── Sleep Stages Breakdown ──
            if (stages != null && stages.totalMinutes > 0) {
                Text("SLEEP STAGES", fontSize = 10.sp, color = c.onSurfaceVariant, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)

                // Stage bar
                Box(
                    modifier = Modifier.fillMaxWidth().height(32.dp).clip(RoundedCornerShape(16.dp))
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        val total = stages.totalMinutes.toFloat()
                        if (stages.deepSleepMinutes > 0) {
                            Box(modifier = Modifier.weight(stages.deepSleepMinutes / total).fillMaxSize().background(Color(0xFF5B21B6)))
                        }
                        if (stages.remSleepMinutes > 0) {
                            Box(modifier = Modifier.weight(stages.remSleepMinutes / total).fillMaxSize().background(Color(0xFF7C3AED)))
                        }
                        if (stages.lightSleepMinutes > 0) {
                            Box(modifier = Modifier.weight(stages.lightSleepMinutes / total).fillMaxSize().background(Color(0xFFA78BFA)))
                        }
                        if (stages.awakeMinutes > 0) {
                            Box(modifier = Modifier.weight(stages.awakeMinutes / total).fillMaxSize().background(c.surfaceHighest))
                        }
                    }
                }

                // Stage details
                Column(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(c.surfaceLow).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StageRow("Deep", stages.deepSleepHours, "${(stages.deepSleepMinutes * 100 / stages.totalMinutes)}%", Color(0xFF5B21B6), "15-25% ideal", c)
                    StageRow("REM", stages.remSleepHours, "${(stages.remSleepMinutes * 100 / stages.totalMinutes)}%", Color(0xFF7C3AED), "20-25% ideal", c)
                    StageRow("Light", stages.lightSleepHours, "${(stages.lightSleepMinutes * 100 / stages.totalMinutes)}%", Color(0xFFA78BFA), "40-60% ideal", c)
                    StageRow("Awake", stages.awakeHours, "${(stages.awakeMinutes * 100 / stages.totalMinutes)}%", c.surfaceHighest, "<5% ideal", c)
                }
            }

            // ── Bedtime Suggestion ──
            Text("RECOMMENDATIONS", fontSize = 10.sp, color = c.onSurfaceVariant, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)

            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                    .background(Brush.horizontalGradient(listOf(c.tertiary.copy(alpha = 0.15f), c.primary.copy(alpha = 0.15f))))
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(48.dp).background(c.tertiary.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Bedtime, null, tint = c.tertiary, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Optimal Bedtime", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = c.onSurface)
                        Text(bedtimeSuggestion, fontSize = 28.sp, fontWeight = FontWeight.Black, color = c.tertiary)
                        Text("Based on your wake patterns", fontSize = 11.sp, color = c.onSurfaceVariant)
                    }
                }
            }

            // ── Sleep Tips ──
            val tips = buildSleepTips(healthData, sleepScore)
            tips.forEachIndexed { index, tip ->
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(c.surfaceLow).padding(16.dp)
                ) {
                    Row {
                        Text("${index + 1}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = c.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(tip.first, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = c.onSurface)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(tip.second, fontSize = 12.sp, color = c.onSurfaceVariant, lineHeight = 18.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StageRow(
    name: String, duration: String, percent: String,
    color: Color, ideal: String, c: AppColorScheme
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = c.onSurface)
                Text(ideal, fontSize = 10.sp, color = c.outline)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(duration, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = c.onSurface)
            Text(percent, fontSize = 11.sp, color = c.onSurfaceVariant)
        }
    }
}

private fun calculateSleepQuality(healthData: HealthData): Int {
    val sleep = healthData.sleep
    val totalMinutes = sleep.totalDuration?.toMinutes() ?: return 0
    if (totalMinutes < 30) return 0

    val hours = totalMinutes / 60.0
    val durationScore = when {
        hours >= 8.5 -> 95.0; hours >= 7.5 -> 100.0; hours >= 7.0 -> 90.0
        hours >= 6.5 -> 75.0; hours >= 6.0 -> 60.0; hours >= 5.0 -> 40.0; else -> 20.0
    }

    val stages = sleep.stages
    val deepPercent = if (stages != null && stages.totalMinutes > 0) stages.deepSleepMinutes.toDouble() / stages.totalMinutes * 100 else 15.0
    val deepScore = when { deepPercent >= 20 -> 100.0; deepPercent >= 15 -> 85.0; deepPercent >= 10 -> 65.0; deepPercent >= 5 -> 40.0; else -> 20.0 }

    val remPercent = if (stages != null && stages.totalMinutes > 0) stages.remSleepMinutes.toDouble() / stages.totalMinutes * 100 else 20.0
    val remScore = when { remPercent >= 22 -> 100.0; remPercent >= 18 -> 85.0; remPercent >= 12 -> 65.0; remPercent >= 5 -> 40.0; else -> 20.0 }

    val awakePercent = if (stages != null && stages.totalMinutes > 0) stages.awakeMinutes.toDouble() / stages.totalMinutes * 100 else 5.0
    val awakeScore = when { awakePercent <= 3 -> 100.0; awakePercent <= 5 -> 85.0; awakePercent <= 10 -> 65.0; awakePercent <= 15 -> 40.0; else -> 20.0 }

    return (durationScore * 0.40 + deepScore * 0.25 + remScore * 0.20 + awakeScore * 0.15).toInt().coerceIn(0, 100)
}

private fun buildSleepTips(healthData: HealthData, score: Int): List<Pair<String, String>> {
    val tips = mutableListOf<Pair<String, String>>()
    val sleep = healthData.sleep
    val hours = sleep.totalDuration?.toMinutes()?.div(60.0) ?: 0.0
    val stages = sleep.stages

    if (hours < 7) {
        tips.add("Extend Your Sleep" to "You slept ${String.format("%.1f", hours)}h — aim for 7-9 hours. Try going to bed 30 minutes earlier tonight.")
    }

    if (stages != null && stages.totalMinutes > 0) {
        val deepPct = stages.deepSleepMinutes * 100 / stages.totalMinutes
        if (deepPct < 15) {
            tips.add("Boost Deep Sleep" to "Your deep sleep was ${deepPct}% (ideal: 15-25%). Avoid alcohol and heavy meals 3 hours before bed. Keep your room cool (18-20°C).")
        }
        val remPct = stages.remSleepMinutes * 100 / stages.totalMinutes
        if (remPct < 18) {
            tips.add("Increase REM Sleep" to "REM was ${remPct}% (ideal: 20-25%). Maintain a consistent wake time — REM cycles are longer in later sleep hours.")
        }
        val awakePct = stages.awakeMinutes * 100 / stages.totalMinutes
        if (awakePct > 10) {
            tips.add("Reduce Awakenings" to "You were awake ${awakePct}% of the night. Limit screen time 1 hour before bed and keep your bedroom dark.")
        }
    }

    if (score >= 80) {
        tips.add("Keep It Up" to "Your sleep quality is excellent. Maintain your current routine — consistency is key to long-term health.")
    }

    if (tips.isEmpty()) {
        tips.add("Good Sleep Hygiene" to "Your sleep looks balanced. Keep a consistent schedule and limit caffeine after 2 PM for optimal recovery.")
    }

    return tips
}
