package com.openhealth.openhealth.screens
import com.openhealth.openhealth.ui.theme.*

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openhealth.openhealth.model.HealthData

// Readiness Score Colors
private val ReadinessRed = Color(0xFFE53935)
private val ReadinessYellow = Color(0xFFFFB300)
private val ReadinessGreen = Color(0xFF43A047)

// Data class for score breakdown items
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
                        text = "Readiness Score",
                        color = TextOnSurface,
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(SurfaceLowest)
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Large Score Card with Gradient
            item {
                ScoreHeroCard(
                    score = readinessInfo.score,
                    label = readinessInfo.label,
                    gradient = readinessInfo.gradient
                )
            }

            // Score Breakdown Section
            item {
                SectionTitle("Score Breakdown")
            }

            items(readinessInfo.factors.size) { index ->
                val factor = readinessInfo.factors[index]
                FactorRow(
                    icon = factor.icon,
                    label = factor.label,
                    value = factor.value,
                    points = factor.points,
                    isPositive = factor.isPositive
                )
            }

            // Explanation Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionTitle("Explanation")
                ExplanationCard(readinessInfo.explanation)
            }

            // Tips Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionTitle("Tips")
                TipsCard(readinessInfo.tips)
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ScoreHeroCard(
    score: Int,
    label: String,
    gradient: Brush
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = score.toString(),
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 80.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Readiness Score",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = TextOnSurface,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun FactorRow(
    icon: ImageVector,
    label: String,
    value: String,
    points: Int,
    isPositive: Boolean
) {
    val pointsColor = if (isPositive) ReadinessGreen else ReadinessRed
    val pointsText = if (isPositive) "+${points} pts" else "${points} pts"
    val pointsIcon = if (isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceMid
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextOnSurface,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextOnSurfaceVariant
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = pointsIcon,
                    contentDescription = null,
                    tint = pointsColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = pointsText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = pointsColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ExplanationCard(explanation: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceMid
        )
    ) {
        Text(
            text = explanation,
            style = MaterialTheme.typography.bodyMedium,
            color = TextOnSurface,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun TipsCard(tips: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceMid
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            tips.forEach { tip ->
                Row(
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = ReadinessYellow,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextOnSurface
                    )
                }
            }
        }
    }
}

// Data class to hold all readiness information
private data class ReadinessInfo(
    val score: Int,
    val label: String,
    val gradient: Brush,
    val factors: List<ScoreFactor>,
    val explanation: String,
    val tips: List<String>
)

// Calculate readiness info with detailed breakdown
private fun calculateReadinessInfo(healthData: HealthData): ReadinessInfo {
    val factors = mutableListOf<ScoreFactor>()
    val now = java.time.Instant.now()

    val hoursSinceLastSleep = healthData.sleep.sessions.maxByOrNull { it.endTime }?.endTime?.let { lastWakeTime ->
        java.time.Duration.between(lastWakeTime, now).toHours()
    }

    var score = 100

    // Awake time penalty - always shown
    val awakePenalty = when {
        hoursSinceLastSleep == null -> {
            factors.add(ScoreFactor(
                icon = Icons.Default.AccessTime,
                label = "Awake Time",
                value = "No sleep data",
                points = -50,
                isPositive = false
            ))
            50
        }
        hoursSinceLastSleep >= 16 -> {
            factors.add(ScoreFactor(
                icon = Icons.Default.AccessTime,
                label = "Awake Time",
                value = "${hoursSinceLastSleep}h awake - exhausted",
                points = -90,
                isPositive = false
            ))
            90
        }
        hoursSinceLastSleep >= 14 -> {
            factors.add(ScoreFactor(
                icon = Icons.Default.AccessTime,
                label = "Awake Time",
                value = "${hoursSinceLastSleep}h awake - very tired",
                points = -85,
                isPositive = false
            ))
            85
        }
        hoursSinceLastSleep >= 12 -> {
            factors.add(ScoreFactor(
                icon = Icons.Default.AccessTime,
                label = "Awake Time",
                value = "${hoursSinceLastSleep}h awake - tired",
                points = -75,
                isPositive = false
            ))
            75
        }
        hoursSinceLastSleep >= 10 -> {
            factors.add(ScoreFactor(
                icon = Icons.Default.AccessTime,
                label = "Awake Time",
                value = "${hoursSinceLastSleep}h awake - fatigued",
                points = -65,
                isPositive = false
            ))
            65
        }
        hoursSinceLastSleep >= 8 -> {
            factors.add(ScoreFactor(
                icon = Icons.Default.AccessTime,
                label = "Awake Time",
                value = "${hoursSinceLastSleep}h awake - declining",
                points = -25,
                isPositive = false
            ))
            25
        }
        hoursSinceLastSleep >= 2 -> {
            factors.add(ScoreFactor(
                icon = Icons.Default.AccessTime,
                label = "Awake Time",
                value = "${hoursSinceLastSleep}h awake",
                points = -5,
                isPositive = false
            ))
            5
        }
        else -> {
            factors.add(ScoreFactor(
                icon = Icons.Default.AccessTime,
                label = "Awake Time",
                value = "Just woke up",
                points = 0,
                isPositive = true
            ))
            0
        }
    }
    score -= awakePenalty

    // Sleep duration - always shown
    healthData.sleep.totalDuration?.let { sleepDuration ->
        val sleepHours = sleepDuration.toHours()
        val sleepMinutes = (sleepDuration.toMinutes() % 60).toInt()
        when {
            sleepHours >= 7 -> {
                // Good sleep - show with 0 points
                factors.add(ScoreFactor(
                    icon = Icons.Default.Bedtime,
                    label = "Sleep Duration",
                    value = "${sleepHours}h ${sleepMinutes}m - good",
                    points = 0,
                    isPositive = true
                ))
            }
            sleepHours >= 5 -> {
                factors.add(ScoreFactor(
                    icon = Icons.Default.Bedtime,
                    label = "Sleep Duration",
                    value = "${sleepHours}h ${sleepMinutes}m - short",
                    points = -10,
                    isPositive = false
                ))
                score -= 10
            }
            else -> {
                factors.add(ScoreFactor(
                    icon = Icons.Default.Bedtime,
                    label = "Sleep Duration",
                    value = "${sleepHours}h ${sleepMinutes}m - poor",
                    points = -20,
                    isPositive = false
                ))
                score -= 20
            }
        }
    } ?: run {
        factors.add(ScoreFactor(
            icon = Icons.Default.Bedtime,
            label = "Sleep Duration",
            value = "No data available",
            points = -15,
            isPositive = false
        ))
        score -= 15
    }

    // Resting Heart Rate - always shown
    val rhr = healthData.restingHeartRate.bpm ?: healthData.heartRate.restingBpm
    rhr?.let { rate ->
        when {
            rate <= 75 -> {
                // Normal RHR - show with 0 points
                factors.add(ScoreFactor(
                    icon = Icons.Default.Favorite,
                    label = "Resting Heart Rate",
                    value = "$rate bpm - normal",
                    points = 0,
                    isPositive = true
                ))
            }
            rate <= 85 -> {
                factors.add(ScoreFactor(
                    icon = Icons.Default.Favorite,
                    label = "Resting Heart Rate",
                    value = "$rate bpm - elevated",
                    points = -5,
                    isPositive = false
                ))
                score -= 5
            }
            else -> {
                factors.add(ScoreFactor(
                    icon = Icons.Default.Favorite,
                    label = "Resting Heart Rate",
                    value = "$rate bpm - high",
                    points = -10,
                    isPositive = false
                ))
                score -= 10
            }
        }
    } ?: run {
        // No RHR data - show as unavailable
        factors.add(ScoreFactor(
            icon = Icons.Default.Favorite,
            label = "Resting Heart Rate",
            value = "No data available",
            points = 0,
            isPositive = true
        ))
    }

    // HRV - show if available
    healthData.heartRateVariability.rmssdMs?.let { hrv ->
        when {
            hrv >= 40 -> {
                // Normal HRV - show with 0 points
                factors.add(ScoreFactor(
                    icon = Icons.Default.FavoriteBorder,
                    label = "Heart Rate Variability",
                    value = "${hrv.toInt()}ms - normal",
                    points = 0,
                    isPositive = true
                ))
            }
            hrv >= 30 -> {
                factors.add(ScoreFactor(
                    icon = Icons.Default.FavoriteBorder,
                    label = "Heart Rate Variability",
                    value = "${hrv.toInt()}ms - low",
                    points = -3,
                    isPositive = false
                ))
                score -= 3
            }
            else -> {
                factors.add(ScoreFactor(
                    icon = Icons.Default.FavoriteBorder,
                    label = "Heart Rate Variability",
                    value = "${hrv.toInt()}ms - very low",
                    points = -8,
                    isPositive = false
                ))
                score -= 8
            }
        }
    }

    // Activity - always shown
    val steps = healthData.steps.count
    when {
        steps >= 1000 -> {
            // Sufficient activity - show with 0 points
            factors.add(ScoreFactor(
                icon = Icons.Default.TrendingUp,
                label = "Activity Level",
                value = "$steps steps - sufficient",
                points = 0,
                isPositive = true
            ))
        }
        else -> {
            factors.add(ScoreFactor(
                icon = Icons.Default.TrendingUp,
                label = "Activity Level",
                value = "$steps steps - very low",
                points = -2,
                isPositive = false
            ))
            score -= 2
        }
    }

    // Apply minimum score of 5
    score = score.coerceIn(5, 100)

    // Determine label and gradient
    val (label, gradient) = when {
        score >= 81 ->
            "Excellent" to Brush.verticalGradient(listOf(ReadinessGreen, ReadinessGreen.copy(alpha = 0.7f)))
        score >= 61 ->
            "Good" to Brush.verticalGradient(listOf(ReadinessGreen, ReadinessGreen.copy(alpha = 0.7f)))
        score >= 41 ->
            "Fair" to Brush.verticalGradient(listOf(ReadinessYellow, ReadinessYellow.copy(alpha = 0.7f)))
        score >= 21 ->
            "Poor" to Brush.verticalGradient(listOf(ReadinessRed, ReadinessRed.copy(alpha = 0.7f)))
        else ->
            "Exhausted" to Brush.verticalGradient(listOf(ReadinessRed, ReadinessRed.copy(alpha = 0.7f)))
    }

    // Generate explanation
    val explanation = generateExplanation(score, hoursSinceLastSleep, healthData)

    // Generate tips
    val tips = generateTips(score, hoursSinceLastSleep, healthData)

    return ReadinessInfo(
        score = score,
        label = label,
        gradient = gradient,
        factors = factors,
        explanation = explanation,
        tips = tips
    )
}

private fun generateExplanation(
    score: Int,
    hoursSinceLastSleep: Long?,
    healthData: HealthData
): String {
    val sleepHours = healthData.sleep.totalDuration?.toMinutes()?.div(60.0) ?: 0.0
    val rhr = healthData.restingHeartRate.bpm
    val hrv = healthData.heartRateVariability.rmssdMs
    val steps = healthData.steps.count
    val awakeHours = hoursSinceLastSleep ?: 0

    val parts = mutableListOf<String>()

    // Lead with the most impactful data point
    when {
        sleepHours < 4 && score < 40 -> {
            parts.add("With only ${String.format("%.0f", sleepHours)} hours of sleep, your body hasn't had enough time to recover.")
        }
        sleepHours >= 7 && score >= 70 -> {
            parts.add("Great sleep last night (${String.format("%.0f", sleepHours)}h) is powering a strong recovery today.")
        }
        sleepHours >= 7 && score < 50 -> {
            parts.add("You got ${String.format("%.0f", sleepHours)} hours of sleep, but other factors are affecting your recovery.")
        }
        sleepHours < 6 && score >= 60 -> {
            parts.add("Despite only ${String.format("%.0f", sleepHours)} hours of sleep, your body is recovering well.")
        }
        awakeHours >= 16 -> {
            parts.add("You've been awake for ${awakeHours} hours — your body is running on depleted reserves.")
        }
        awakeHours >= 12 && score < 50 -> {
            parts.add("After ${awakeHours} hours awake, fatigue is setting in and affecting your readiness.")
        }
        else -> {
            parts.add(when {
                score >= 80 -> "Your body is well-recovered and performing at its best today."
                score >= 60 -> "You're in a good state for normal activities and moderate exercise."
                score >= 40 -> "Your body is showing signs of fatigue — take it easier today."
                else -> "Your readiness is low. Rest and recovery should be your priority."
            })
        }
    }

    // Add secondary insight based on HR/HRV
    if (rhr != null && hrv != null) {
        when {
            rhr < 60 && hrv > 40 -> parts.add("Your resting heart rate ($rhr bpm) and HRV (${String.format("%.0f", hrv)} ms) show good autonomic recovery.")
            rhr > 75 -> parts.add("Your elevated resting heart rate ($rhr bpm) suggests your body is under stress.")
            hrv < 25 -> parts.add("Low HRV (${String.format("%.0f", hrv)} ms) indicates your nervous system needs more recovery time.")
        }
    }

    // Add activity context
    when {
        steps > 8000 && score >= 60 -> parts.add("Your activity level (${steps} steps) is contributing positively to your overall health.")
        steps < 500 && awakeHours > 6 -> parts.add("Very low activity so far — even a short walk could boost your energy.")
    }

    return parts.joinToString(" ")
}

private fun generateTips(
    score: Int,
    hoursSinceLastSleep: Long?,
    healthData: HealthData
): List<String> {
    val tips = mutableListOf<String>()
    val sleepHours = healthData.sleep.totalDuration?.toMinutes()?.div(60.0) ?: 0.0
    val awakeHours = hoursSinceLastSleep ?: 0
    val rhr = healthData.restingHeartRate.bpm ?: healthData.heartRate.restingBpm
    val hrv = healthData.heartRateVariability.rmssdMs
    val steps = healthData.steps.count

    // Awake time tips
    when {
        awakeHours >= 18 -> tips.add("You've been up for ${awakeHours}h. Go to sleep now — your body needs it.")
        awakeHours >= 14 -> tips.add("${awakeHours} hours awake. Start winding down — dim lights, avoid screens, and prepare for bed.")
        awakeHours >= 10 -> tips.add("Consider when you'll sleep tonight. A consistent bedtime improves recovery by up to 20%.")
    }

    // Sleep quality tips
    when {
        sleepHours < 4 -> tips.add("Only ${String.format("%.0f", sleepHours)}h of sleep is critical. A 20-minute nap before 3 PM can help restore focus.")
        sleepHours in 4.0..5.9 -> tips.add("${String.format("%.0f", sleepHours)}h of sleep is below optimal. Try to sleep 1-2 hours earlier tonight to reach 7+ hours.")
        sleepHours >= 9 -> tips.add("${String.format("%.0f", sleepHours)}h of sleep is above average. If you feel groggy, you may be oversleeping — try setting an alarm.")
    }

    // Heart rate tips
    rhr?.let {
        when {
            it > 80 -> tips.add("Resting HR of $it bpm is elevated. Try deep breathing exercises — 5 minutes of box breathing can lower it by 5-10 bpm.")
            it < 55 && score >= 70 -> tips.add("Resting HR of $it bpm shows excellent cardiovascular fitness. Keep up your training!")
            else -> {}
        }
    }

    // HRV tips
    hrv?.let {
        when {
            it < 20 -> tips.add("HRV of ${String.format("%.0f", it)} ms is very low. Avoid intense exercise today — focus on stretching or yoga instead.")
            it < 30 && score < 50 -> tips.add("Low HRV (${String.format("%.0f", it)} ms) suggests stress. Try a 10-minute walk outside to reset your nervous system.")
            it > 60 && score >= 70 -> tips.add("Strong HRV of ${String.format("%.0f", it)} ms — your body is primed for a challenging workout today.")
            else -> {}
        }
    }

    // Activity tips
    when {
        steps < 500 && awakeHours > 4 -> tips.add("Only $steps steps so far. Even 10 minutes of walking boosts mood, energy, and circulation.")
        steps in 500..2999 -> tips.add("$steps steps is a start. Try to reach ${(steps / 1000 + 1) * 1000} by adding a short walk after your next meal.")
        steps > 10000 -> tips.add("$steps steps — excellent activity! Make sure to stay hydrated and stretch to prevent muscle tightness.")
    }

    // Ensure at least 3 tips
    if (tips.size < 3) {
        val fillers = listOf(
            "Drink water regularly — dehydration reduces HRV and increases heart rate.",
            "Expose yourself to natural light in the morning to strengthen your circadian rhythm.",
            "Limit caffeine after 2 PM to protect tonight's sleep quality.",
            "A 5-minute cold shower can boost alertness and improve circulation.",
            "Eating protein within 30 minutes of waking supports sustained energy levels."
        )
        for (filler in fillers.shuffled()) {
            if (tips.size >= 3) break
            tips.add(filler)
        }
    }

    return tips.take(4)
}
