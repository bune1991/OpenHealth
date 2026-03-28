package com.openhealth.openhealth.screens

import com.openhealth.openhealth.ui.theme.*

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openhealth.openhealth.model.HealthData

data class ScoreFactor(
    val icon: ImageVector,
    val label: String,
    val value: String,
    val points: Int,
    val isPositive: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadinessDetailScreen(
    healthData: HealthData,
    onBackClick: () -> Unit
) {
    val readinessInfo = calculateReadinessInfo(healthData)

    // Derive recommendation based on score
    val recommendationTitle = when {
        readinessInfo.score >= 80 -> "High-Intensity Interval Session"
        readinessInfo.score >= 60 -> "Moderate Activity Session"
        readinessInfo.score >= 40 -> "Light Recovery Walk"
        else -> "Rest & Recovery"
    }
    val recommendationDescription = when {
        readinessInfo.score >= 80 -> "Your body is primed for peak performance. Push your limits with a challenging HIIT session to maximize today's recovery window."
        readinessInfo.score >= 60 -> "You have solid energy reserves. A moderate workout will keep your momentum without overloading your system."
        readinessInfo.score >= 40 -> "Your body needs gentle movement. A light walk will promote circulation and aid recovery without adding stress."
        else -> "Focus on rest today. Gentle stretching, hydration, and an early bedtime will help restore your readiness."
    }

    // Compute recovery time from sleep data
    val sleepHours = healthData.sleep.totalDuration?.toHours() ?: 0L
    val sleepMinutes = (healthData.sleep.totalDuration?.toMinutes()?.rem(60)) ?: 0L
    val recoveryHours = String.format("%02d", sleepHours)
    val recoveryMinutes = String.format("%02d", sleepMinutes)
    val recoveryProgress = (sleepHours * 60 + sleepMinutes).toFloat() / (8f * 60f) // target 8h

    // HRV status
    val hrv = healthData.heartRateVariability.rmssdMs
    val hrvStatus = when {
        hrv == null -> "No Data"
        hrv >= 50 -> "Excellent"
        hrv >= 40 -> "Stable"
        hrv >= 30 -> "Low"
        else -> "Very Low"
    }

    // Insight cards from tips
    val insightIcons = listOf(Icons.Default.Bedtime, Icons.Default.Psychology, Icons.Default.Thermostat)
    val insightTitles = listOf("Sleep Efficiency", "Cognitive Load", "Thermal Balance")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Readiness",
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
                .background(SurfaceLowest)
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ─── 1. Hero Section ───
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // "MORNING STATUS" magenta label
                    Text(
                        text = "MORNING STATUS",
                        style = MaterialTheme.typography.labelSmall,
                        color = VibrantMagenta,
                        letterSpacing = 3.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    // Big title line 1: label (white)
                    Text(
                        text = readinessInfo.label,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextOnSurface,
                        letterSpacing = (-1).sp,
                        lineHeight = 44.sp
                    )
                    // Big title line 2: "Condition" (primary)
                    Text(
                        text = "Condition",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ElectricIndigo,
                        letterSpacing = (-1).sp,
                        lineHeight = 44.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Description
                    Text(
                        text = readinessInfo.explanation,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextOnSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }

            // ─── 2. Score Ring ───
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .background(SurfaceHigh)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val animatedScore by animateFloatAsState(
                        targetValue = readinessInfo.score / 100f,
                        animationSpec = tween(1200),
                        label = "score_ring"
                    )

                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(180.dp)) {
                        Canvas(modifier = Modifier.size(180.dp)) {
                            val strokeWidth = 14.dp.toPx()
                            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                            // Background track
                            drawArc(
                                color = SurfaceLowest,
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                                topLeft = topLeft,
                                size = arcSize
                            )
                            // Gradient progress arc
                            drawArc(
                                brush = Brush.sweepGradient(
                                    listOf(ElectricIndigo, VibrantMagenta, ElectricIndigo)
                                ),
                                startAngle = -90f,
                                sweepAngle = 360f * animatedScore,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                                topLeft = topLeft,
                                size = arcSize
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = readinessInfo.score.toString(),
                                fontSize = 56.sp,
                                fontWeight = FontWeight.Black,
                                color = TextOnSurface
                            )
                            Text(
                                text = "READY",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextOnSurfaceVariant,
                                letterSpacing = 3.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // ─── 3. Bento Grid: HRV Card + Recovery Time Card ───
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // HRV Card (2/3 width)
                    Box(
                        modifier = Modifier
                            .weight(2f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceLow)
                            .padding(20.dp)
                    ) {
                        Column {
                            // Header
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = ElectricIndigo,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Heart Rate Variability",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextOnSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            // "Stable" badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(ElectricIndigo.copy(alpha = 0.10f))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = hrvStatus,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElectricIndigo,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            // 8-bar chart
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                val barHeights = listOf(0.4f, 0.6f, 0.5f, 0.7f, 0.55f, 0.85f, 0.65f, 0.75f)
                                val highlightIndex = 5 // highest bar gets the glow
                                barHeights.forEachIndexed { index, height ->
                                    val isHighlighted = index == highlightIndex
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight(height)
                                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                            .background(
                                                if (isHighlighted) {
                                                    Brush.verticalGradient(
                                                        listOf(VibrantMagenta, ElectricIndigo)
                                                    )
                                                } else {
                                                    Brush.verticalGradient(
                                                        listOf(SurfaceHighest, SurfaceHighest)
                                                    )
                                                }
                                            )
                                    )
                                }
                            }
                        }
                    }

                    // Recovery Time Card (1/3 width)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(MagentaContainer, SurfaceHigh)
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restore,
                                contentDescription = null,
                                tint = TextOnSurface,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            // Time display
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = recoveryHours,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextOnSurface
                                )
                                Text(
                                    text = "h ",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextOnSurface.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = recoveryMinutes,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextOnSurface
                                )
                                Text(
                                    text = "m",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextOnSurface.copy(alpha = 0.5f)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "RECOVERY\nNEEDED",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextOnSurface.copy(alpha = 0.7f),
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                lineHeight = 14.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            // Progress bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color.Black.copy(alpha = 0.20f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(recoveryProgress.coerceIn(0f, 1f))
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(TextOnSurface)
                                )
                            }
                        }
                    }
                }
            }

            // ─── 4. Deep Dive Insights ───
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Deep Dive Insights",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextOnSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.labelMedium,
                        color = ElectricIndigo,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Horizontal scrollable insight cards
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(end = 20.dp)
                ) {
                    val tips = readinessInfo.tips
                    items(minOf(tips.size, 3)) { index ->
                        val icon = insightIcons.getOrElse(index) { Icons.Default.AutoAwesome }
                        val title = insightTitles.getOrElse(index) { "Insight" }
                        val description = tips[index]

                        Box(
                            modifier = Modifier
                                .width(220.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(SurfaceHigh)
                                .padding(20.dp)
                        ) {
                            Column {
                                // Icon circle
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(ElectricIndigo.copy(alpha = 0.12f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = ElectricIndigo,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextOnSurface,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextOnSurfaceVariant,
                                    lineHeight = 18.sp,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // ─── 5. Recommendation Card ───
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceMid)
                        .padding(24.dp)
                ) {
                    Column {
                        // "RECOMMENDED ACTION" badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(VibrantMagenta)
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "RECOMMENDED ACTION",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnMagenta,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        // Title
                        Text(
                            text = recommendationTitle,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextOnSurface,
                            lineHeight = 30.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // Description
                        Text(
                            text = recommendationDescription,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextOnSurfaceVariant,
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        // "Start Session" pill button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(ElectricIndigo)
                                .padding(horizontal = 28.dp, vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Start Session",
                                style = MaterialTheme.typography.labelLarge,
                                color = OnIndigo,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Readiness Calculation Logic (unchanged)
// ═══════════════════════════════════════════════════════════

private data class ReadinessInfo(
    val score: Int,
    val label: String,
    val gradient: Brush,
    val factors: List<ScoreFactor>,
    val explanation: String,
    val tips: List<String>
)

private fun calculateReadinessInfo(healthData: HealthData): ReadinessInfo {
    val factors = mutableListOf<ScoreFactor>()
    val now = java.time.Instant.now()

    val hoursSinceLastSleep = healthData.sleep.sessions.maxByOrNull { it.endTime }?.endTime?.let { lastWakeTime ->
        java.time.Duration.between(lastWakeTime, now).toHours()
    }

    var score = 100

    val awakePenalty = when {
        hoursSinceLastSleep == null -> { factors.add(ScoreFactor(Icons.Default.AccessTime, "Awake Time", "No sleep data", -50, false)); 50 }
        hoursSinceLastSleep >= 16 -> { factors.add(ScoreFactor(Icons.Default.AccessTime, "Awake Time", "${hoursSinceLastSleep}h awake - exhausted", -90, false)); 90 }
        hoursSinceLastSleep >= 14 -> { factors.add(ScoreFactor(Icons.Default.AccessTime, "Awake Time", "${hoursSinceLastSleep}h awake - very tired", -85, false)); 85 }
        hoursSinceLastSleep >= 12 -> { factors.add(ScoreFactor(Icons.Default.AccessTime, "Awake Time", "${hoursSinceLastSleep}h awake - tired", -75, false)); 75 }
        hoursSinceLastSleep >= 10 -> { factors.add(ScoreFactor(Icons.Default.AccessTime, "Awake Time", "${hoursSinceLastSleep}h awake - fatigued", -65, false)); 65 }
        hoursSinceLastSleep >= 8 -> { factors.add(ScoreFactor(Icons.Default.AccessTime, "Awake Time", "${hoursSinceLastSleep}h awake - declining", -25, false)); 25 }
        hoursSinceLastSleep >= 2 -> { factors.add(ScoreFactor(Icons.Default.AccessTime, "Awake Time", "${hoursSinceLastSleep}h awake", -5, false)); 5 }
        else -> { factors.add(ScoreFactor(Icons.Default.AccessTime, "Awake Time", "Just woke up", 0, true)); 0 }
    }
    score -= awakePenalty

    healthData.sleep.totalDuration?.let { sleepDuration ->
        val sleepHours = sleepDuration.toHours()
        val sleepMinutes = (sleepDuration.toMinutes() % 60).toInt()
        when {
            sleepHours >= 7 -> factors.add(ScoreFactor(Icons.Default.Bedtime, "Sleep Duration", "${sleepHours}h ${sleepMinutes}m - good", 0, true))
            sleepHours >= 5 -> { factors.add(ScoreFactor(Icons.Default.Bedtime, "Sleep Duration", "${sleepHours}h ${sleepMinutes}m - short", -10, false)); score -= 10 }
            else -> { factors.add(ScoreFactor(Icons.Default.Bedtime, "Sleep Duration", "${sleepHours}h ${sleepMinutes}m - poor", -20, false)); score -= 20 }
        }
    } ?: run { factors.add(ScoreFactor(Icons.Default.Bedtime, "Sleep Duration", "No data", -15, false)); score -= 15 }

    val rhr = healthData.restingHeartRate.bpm ?: healthData.heartRate.restingBpm
    rhr?.let { rate ->
        when {
            rate <= 75 -> factors.add(ScoreFactor(Icons.Default.Favorite, "Resting Heart Rate", "$rate bpm - normal", 0, true))
            rate <= 85 -> { factors.add(ScoreFactor(Icons.Default.Favorite, "Resting Heart Rate", "$rate bpm - elevated", -5, false)); score -= 5 }
            else -> { factors.add(ScoreFactor(Icons.Default.Favorite, "Resting Heart Rate", "$rate bpm - high", -10, false)); score -= 10 }
        }
    } ?: factors.add(ScoreFactor(Icons.Default.Favorite, "Resting Heart Rate", "No data", 0, true))

    healthData.heartRateVariability.rmssdMs?.let { hrv ->
        when {
            hrv >= 40 -> factors.add(ScoreFactor(Icons.Default.FavoriteBorder, "Heart Rate Variability", "${hrv.toInt()}ms - normal", 0, true))
            hrv >= 30 -> { factors.add(ScoreFactor(Icons.Default.FavoriteBorder, "Heart Rate Variability", "${hrv.toInt()}ms - low", -3, false)); score -= 3 }
            else -> { factors.add(ScoreFactor(Icons.Default.FavoriteBorder, "Heart Rate Variability", "${hrv.toInt()}ms - very low", -8, false)); score -= 8 }
        }
    }

    val steps = healthData.steps.count
    when {
        steps >= 1000 -> factors.add(ScoreFactor(Icons.Default.DirectionsWalk, "Activity Level", "$steps steps - sufficient", 0, true))
        else -> { factors.add(ScoreFactor(Icons.Default.DirectionsWalk, "Activity Level", "$steps steps - very low", -2, false)); score -= 2 }
    }

    score = score.coerceIn(5, 100)

    val label = when {
        score >= 81 -> "Optimal"
        score >= 61 -> "Good"
        score >= 41 -> "Fair"
        score >= 21 -> "Poor"
        else -> "Exhausted"
    }
    val gradient = when {
        score >= 61 -> Brush.horizontalGradient(listOf(ElectricIndigo, VibrantMagenta))
        score >= 41 -> Brush.horizontalGradient(listOf(WarningOrange, WarningOrange.copy(alpha = 0.7f)))
        else -> Brush.horizontalGradient(listOf(ErrorRed, ErrorRed.copy(alpha = 0.7f)))
    }

    return ReadinessInfo(score, label, gradient, factors, generateExplanation(score, hoursSinceLastSleep, healthData), generateTips(score, hoursSinceLastSleep, healthData))
}

private fun generateExplanation(score: Int, hoursSinceLastSleep: Long?, healthData: HealthData): String {
    val sleepHours = healthData.sleep.totalDuration?.toMinutes()?.div(60.0) ?: 0.0
    val rhr = healthData.restingHeartRate.bpm
    val hrv = healthData.heartRateVariability.rmssdMs
    val awakeHours = hoursSinceLastSleep ?: 0
    val parts = mutableListOf<String>()

    when {
        sleepHours < 4 && score < 40 -> parts.add("With only ${String.format("%.0f", sleepHours)} hours of sleep, your body hasn't had enough time to recover.")
        sleepHours >= 7 && score >= 70 -> parts.add("Great sleep last night (${String.format("%.0f", sleepHours)}h) is powering a strong recovery today.")
        awakeHours >= 16 -> parts.add("You've been awake for ${awakeHours} hours — your body is running on depleted reserves.")
        else -> parts.add(when {
            score >= 80 -> "Your body is well-recovered and performing at its best today."
            score >= 60 -> "You're in a good state for normal activities and moderate exercise."
            score >= 40 -> "Your body is showing signs of fatigue — take it easier today."
            else -> "Your readiness is low. Rest and recovery should be your priority."
        })
    }

    if (rhr != null && hrv != null) {
        when {
            rhr < 60 && hrv > 40 -> parts.add("Your resting heart rate ($rhr bpm) and HRV (${String.format("%.0f", hrv)} ms) show good autonomic recovery.")
            rhr > 75 -> parts.add("Your elevated resting heart rate ($rhr bpm) suggests your body is under stress.")
            hrv < 25 -> parts.add("Low HRV (${String.format("%.0f", hrv)} ms) indicates your nervous system needs more recovery time.")
        }
    }

    return parts.joinToString(" ")
}

private fun generateTips(score: Int, hoursSinceLastSleep: Long?, healthData: HealthData): List<String> {
    val tips = mutableListOf<String>()
    val sleepHours = healthData.sleep.totalDuration?.toMinutes()?.div(60.0) ?: 0.0
    val awakeHours = hoursSinceLastSleep ?: 0
    val rhr = healthData.restingHeartRate.bpm ?: healthData.heartRate.restingBpm
    val hrv = healthData.heartRateVariability.rmssdMs

    when {
        awakeHours >= 18 -> tips.add("You've been up for ${awakeHours}h. Go to sleep now — your body needs it.")
        awakeHours >= 14 -> tips.add("Start winding down — dim lights, avoid screens, and prepare for bed.")
        awakeHours >= 10 -> tips.add("A consistent bedtime improves recovery by up to 20%.")
    }
    when {
        sleepHours < 4 -> tips.add("A 20-minute nap before 3 PM can help restore focus.")
        sleepHours in 4.0..5.9 -> tips.add("Try to sleep 1-2 hours earlier tonight to reach 7+ hours.")
    }
    rhr?.let { if (it > 80) tips.add("Try deep breathing — 5 minutes of box breathing can lower HR by 5-10 bpm.") }
    hrv?.let {
        when {
            it < 20 -> tips.add("Avoid intense exercise today — focus on stretching or yoga instead.")
            it > 60 && score >= 70 -> tips.add("Your body is primed for a challenging workout today.")
        }
    }

    if (tips.size < 3) {
        val fillers = listOf(
            "Stay hydrated — dehydration reduces HRV and increases heart rate.",
            "Morning sunlight strengthens your circadian rhythm.",
            "Limit caffeine after 2 PM to protect tonight's sleep quality."
        )
        for (filler in fillers) { if (tips.size >= 3) break; tips.add(filler) }
    }
    return tips.take(4)
}
