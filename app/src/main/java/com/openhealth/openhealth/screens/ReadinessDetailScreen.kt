package com.openhealth.openhealth.screens

import com.openhealth.openhealth.ui.theme.*

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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lightbulb
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
            // ─── Hero: "Optimal Condition" + Score Ring ───
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Status label
                    Text(
                        text = VibrantMagenta.let { "Morning Status" }.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = VibrantMagenta,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Big title
                    Text(
                        text = "${readinessInfo.label}",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextOnSurface,
                        letterSpacing = (-1).sp,
                        lineHeight = 44.sp
                    )
                    Text(
                        text = "Condition",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ElectricIndigo,
                        letterSpacing = (-1).sp,
                        lineHeight = 44.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = readinessInfo.explanation.take(100) + if (readinessInfo.explanation.length > 100) "..." else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextOnSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }

            // ─── Score Ring ───
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceHigh)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val animatedScore by animateFloatAsState(
                        targetValue = readinessInfo.score / 100f,
                        animationSpec = tween(1200),
                        label = "score_ring"
                    )

                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
                        Canvas(modifier = Modifier.size(160.dp)) {
                            val strokeWidth = 12.dp.toPx()
                            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                            // Background
                            drawArc(
                                color = SurfaceLowest,
                                startAngle = -90f, sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                                topLeft = topLeft, size = arcSize
                            )
                            // Progress with gradient
                            drawArc(
                                brush = Brush.sweepGradient(
                                    listOf(ElectricIndigo, VibrantMagenta, ElectricIndigo)
                                ),
                                startAngle = -90f,
                                sweepAngle = 360f * animatedScore,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                                topLeft = topLeft, size = arcSize
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = readinessInfo.score.toString(),
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Black,
                                color = TextOnSurface
                            )
                            Text(
                                text = "Ready",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextOnSurfaceVariant,
                                letterSpacing = 2.sp
                            )
                        }
                    }
                }
            }

            // ─── Score Breakdown ───
            item {
                Text(
                    text = "SCORE BREAKDOWN",
                    style = MaterialTheme.typography.labelMedium,
                    color = ElectricIndigo,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(readinessInfo.factors.size) { index ->
                val factor = readinessInfo.factors[index]
                FactorRow(factor)
            }

            // ─── Deep Dive Insights ───
            item {
                Text(
                    text = "DEEP DIVE INSIGHTS",
                    style = MaterialTheme.typography.labelMedium,
                    color = ElectricIndigo,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Explanation card
            item {
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
                                .size(48.dp)
                                .background(ElectricIndigo.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = ElectricIndigo,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Recovery Insight",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextOnSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = readinessInfo.explanation,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextOnSurfaceVariant,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }

            // ─── Tips ───
            item {
                Text(
                    text = "RECOMMENDED ACTIONS",
                    style = MaterialTheme.typography.labelMedium,
                    color = ElectricIndigo,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(readinessInfo.tips.size) { index ->
                val tip = readinessInfo.tips[index]
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceMid)
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    when (index) {
                                        0 -> ElectricIndigo.copy(alpha = 0.15f)
                                        1 -> VibrantMagenta.copy(alpha = 0.15f)
                                        else -> SoftLavender.copy(alpha = 0.15f)
                                    },
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = when (index) {
                                    0 -> ElectricIndigo
                                    1 -> VibrantMagenta
                                    else -> SoftLavender
                                },
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = tip,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextOnSurfaceVariant,
                            lineHeight = 22.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun FactorRow(factor: ScoreFactor) {
    val pointsColor = if (factor.isPositive) SuccessGreen else ErrorRed
    val pointsText = if (factor.points == 0) "OK" else if (factor.isPositive) "+${factor.points}" else "${factor.points}"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceMid)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (factor.isPositive) SuccessGreen.copy(alpha = 0.12f) else ErrorRed.copy(alpha = 0.12f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = factor.icon,
                        contentDescription = null,
                        tint = if (factor.isPositive) SuccessGreen else ErrorRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = factor.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextOnSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = factor.value,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextOnSurfaceVariant
                    )
                }
            }

            // Points badge
            Box(
                modifier = Modifier
                    .background(pointsColor.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = pointsText,
                    color = pointsColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
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
