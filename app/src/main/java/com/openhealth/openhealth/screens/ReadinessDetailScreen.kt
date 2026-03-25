package com.openhealth.openhealth.screens

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
import com.openhealth.openhealth.ui.theme.BackgroundBlack
import com.openhealth.openhealth.ui.theme.SurfaceDark
import com.openhealth.openhealth.ui.theme.TextPrimary
import com.openhealth.openhealth.ui.theme.TextSecondary
import com.openhealth.openhealth.ui.theme.TextTertiary

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
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleLarge
                    )
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundBlack
                )
            )
        },
        containerColor = BackgroundBlack
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundBlack)
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
        color = TextPrimary,
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
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceDark
        )
    ) {
        Text(
            text = explanation,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun TipsCard(tips: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceDark
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
                        color = TextPrimary
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
    val sleepHours = healthData.sleep.totalDuration?.toHours() ?: 0

    return when {
        score >= 81 -> {
            "Your readiness score is excellent! You've had sufficient rest and your body is well-recovered. " +
            "This is a great time to tackle challenging tasks or engage in intense physical activity."
        }
        score >= 61 -> {
            "Your readiness score is good. You're reasonably well-rested and ready for most activities. " +
            "You can proceed with your normal routine, including moderate to intense exercise."
        }
        score >= 41 -> {
            "Your readiness score is fair. You may be experiencing some fatigue. " +
            "Consider taking it easy today and prioritize lighter activities. " +
            "Focus on recovery and getting quality sleep tonight."
        }
        score >= 21 -> {
            "Your readiness score is poor. You're likely feeling significant fatigue. " +
            "It's important to rest and avoid strenuous activities. " +
            "Consider taking a nap or going to bed early to recover."
        }
        else -> {
            "Your readiness score indicates exhaustion. Your body needs immediate rest. " +
            "Avoid any demanding physical or mental tasks. " +
            "Prioritize sleep and recovery above all else."
        }
    }
}

private fun generateTips(
    score: Int,
    hoursSinceLastSleep: Long?,
    healthData: HealthData
): List<String> {
    val tips = mutableListOf<String>()

    // Sleep-related tips
    when {
        hoursSinceLastSleep == null -> {
            tips.add("No sleep data available. Ensure your sleep tracking is enabled.")
        }
        hoursSinceLastSleep >= 14 -> {
            tips.add("You've been awake for a very long time. Consider taking a nap or going to bed soon.")
            tips.add("Avoid caffeine and heavy meals to help your body prepare for sleep.")
        }
        hoursSinceLastSleep >= 10 -> {
            tips.add("You've been awake for an extended period. Plan to wind down soon.")
            tips.add("Take short breaks and stay hydrated to maintain focus.")
        }
        hoursSinceLastSleep >= 8 -> {
            tips.add("Your energy levels may be declining. Consider a light activity or short rest.")
        }
    }

    // Sleep duration tips
    val sleepHours = healthData.sleep.totalDuration?.toHours() ?: 0
    when {
        sleepHours < 5 -> {
            tips.add("You had very little sleep last night. Prioritize getting more rest tonight.")
        }
        sleepHours < 7 -> {
            tips.add("Aim for 7-9 hours of sleep tonight to improve your readiness for tomorrow.")
        }
    }

    // RHR tips
    val rhr = healthData.restingHeartRate.bpm ?: healthData.heartRate.restingBpm
    rhr?.let {
        if (it > 85) {
            tips.add("Your resting heart rate is elevated. This may indicate stress or insufficient recovery.")
        }
    }

    // Activity tips
    if (healthData.steps.count < 1000) {
        tips.add("Try to get some light movement today, even a short walk can help.")
    }

    // General tips if list is short
    if (tips.size < 2) {
        tips.add("Maintain a consistent sleep schedule to improve your readiness scores.")
    }
    if (tips.size < 3) {
        tips.add("Stay hydrated throughout the day to support overall health and recovery.")
    }

    return tips.take(3)
}
