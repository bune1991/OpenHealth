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
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openhealth.openhealth.model.HealthData
import com.openhealth.openhealth.ui.theme.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FitnessCoachScreen(
    healthData: HealthData,
    readinessScore: Int,
    strainScore: Double,
    onBackClick: () -> Unit
) {
    val c = LocalAppColors.current

    // Determine training zone based on readiness
    val trainingZone = when {
        readinessScore >= 80 -> TrainingZone.PEAK
        readinessScore >= 60 -> TrainingZone.MODERATE
        readinessScore >= 40 -> TrainingZone.LIGHT
        else -> TrainingZone.RECOVERY
    }

    // Max HR estimate
    val maxHr = 190
    val rhr = healthData.restingHeartRate.bpm ?: healthData.heartRate.restingBpm ?: 65

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FitnessCenter, null, tint = c.secondary, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Fitness Coach", color = c.onSurface, fontWeight = FontWeight.Bold)
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
            // ── Readiness + Strain Hero ──
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Readiness Ring
                Box(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(20.dp)).background(c.surfaceLow).padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
                            val animatedReadiness by animateFloatAsState(
                                targetValue = readinessScore / 100f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessVeryLow),
                                label = "readiness"
                            )
                            Canvas(modifier = Modifier.size(100.dp)) {
                                val sw = 8.dp.toPx()
                                val arcSize = Size(size.width - sw * 2, size.height - sw * 2)
                                val tl = Offset(sw, sw)
                                drawArc(color = c.surfaceHighest, startAngle = -90f, sweepAngle = 360f, useCenter = false, style = Stroke(sw, cap = StrokeCap.Round), topLeft = tl, size = arcSize)
                                drawArc(color = c.primary, startAngle = -90f, sweepAngle = 360f * animatedReadiness, useCenter = false, style = Stroke(sw, cap = StrokeCap.Round), topLeft = tl, size = arcSize)
                            }
                            val animNum by animateIntAsState(targetValue = readinessScore, animationSpec = tween(1000), label = "rn")
                            Text("$animNum", fontSize = 28.sp, fontWeight = FontWeight.Black, color = c.onSurface)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("READINESS", fontSize = 9.sp, color = c.onSurfaceVariant, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Strain Ring
                Box(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(20.dp)).background(c.surfaceLow).padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
                            val animatedStrain by animateFloatAsState(
                                targetValue = (strainScore / 21f).toFloat(),
                                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessVeryLow),
                                label = "strain"
                            )
                            Canvas(modifier = Modifier.size(100.dp)) {
                                val sw = 8.dp.toPx()
                                val arcSize = Size(size.width - sw * 2, size.height - sw * 2)
                                val tl = Offset(sw, sw)
                                drawArc(color = c.surfaceHighest, startAngle = -90f, sweepAngle = 360f, useCenter = false, style = Stroke(sw, cap = StrokeCap.Round), topLeft = tl, size = arcSize)
                                drawArc(color = c.secondary, startAngle = -90f, sweepAngle = 360f * animatedStrain, useCenter = false, style = Stroke(sw, cap = StrokeCap.Round), topLeft = tl, size = arcSize)
                            }
                            Text(String.format("%.1f", strainScore), fontSize = 28.sp, fontWeight = FontWeight.Black, color = c.onSurface)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("STRAIN", fontSize = 9.sp, color = c.onSurfaceVariant, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // ── Training Zone Card ──
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                    .background(Brush.horizontalGradient(listOf(trainingZone.color.copy(alpha = 0.2f), trainingZone.color.copy(alpha = 0.05f))))
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(40.dp).background(trainingZone.color.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(trainingZone.icon, null, tint = trainingZone.color, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("TODAY'S TRAINING ZONE", fontSize = 10.sp, color = c.onSurfaceVariant, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                            Text(trainingZone.label, fontSize = 22.sp, fontWeight = FontWeight.Black, color = trainingZone.color)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(trainingZone.description, fontSize = 13.sp, color = c.onSurfaceVariant, lineHeight = 20.sp)
                }
            }

            // ── Recommended Workout ──
            Text("TODAY'S WORKOUT", fontSize = 10.sp, color = c.onSurfaceVariant, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)

            val workout = getRecommendedWorkout(trainingZone, strainScore)
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(c.surfaceLow).padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(workout.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = c.onSurface)
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        WorkoutTag(Icons.Default.Timer, workout.duration, c)
                        WorkoutTag(Icons.Default.Favorite, workout.heartRateZone, c)
                        WorkoutTag(Icons.Default.Bolt, workout.intensity, c)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(workout.description, fontSize = 13.sp, color = c.onSurfaceVariant, lineHeight = 20.sp)
                }
            }

            // ── HR Zones Guide ──
            Text("HEART RATE ZONES", fontSize = 10.sp, color = c.onSurfaceVariant, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)

            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(c.surfaceLow).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HrZoneRow("Zone 1 — Warm Up", "${(maxHr * 0.50).toInt()}-${(maxHr * 0.60).toInt()} bpm", Color(0xFF94A3B8), c)
                HrZoneRow("Zone 2 — Fat Burn", "${(maxHr * 0.60).toInt()}-${(maxHr * 0.70).toInt()} bpm", Color(0xFF22C55E), c)
                HrZoneRow("Zone 3 — Cardio", "${(maxHr * 0.70).toInt()}-${(maxHr * 0.80).toInt()} bpm", Color(0xFFEAB308), c)
                HrZoneRow("Zone 4 — Threshold", "${(maxHr * 0.80).toInt()}-${(maxHr * 0.90).toInt()} bpm", Color(0xFFF97316), c)
                HrZoneRow("Zone 5 — Max Effort", "${(maxHr * 0.90).toInt()}-${maxHr} bpm", Color(0xFFEF4444), c)

                Spacer(modifier = Modifier.height(4.dp))
                Text("Resting HR: $rhr bpm | Max HR: $maxHr bpm (estimated)", fontSize = 11.sp, color = c.outline)
            }

            // ── Recovery Status ──
            Text("RECOVERY STATUS", fontSize = 10.sp, color = c.onSurfaceVariant, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)

            val recoveryTips = buildRecoveryTips(healthData, readinessScore, strainScore)
            recoveryTips.forEach { tip ->
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(c.surfaceLow).padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier.size(32.dp).background(tip.color.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(tip.icon, null, tint = tip.color, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(tip.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = c.onSurface)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(tip.description, fontSize = 12.sp, color = c.onSurfaceVariant, lineHeight = 18.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun WorkoutTag(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, c: AppColorScheme) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = c.primary, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, fontSize = 11.sp, color = c.onSurfaceVariant, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun HrZoneRow(name: String, range: String, color: Color, c: AppColorScheme) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
            Spacer(modifier = Modifier.width(10.dp))
            Text(name, fontSize = 13.sp, color = c.onSurface)
        }
        Text(range, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = c.onSurfaceVariant)
    }
}

private enum class TrainingZone(
    val label: String,
    val description: String,
    val color: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    PEAK("Peak Performance", "Your body is fully recovered. Today is perfect for high-intensity training, personal records, or competitive efforts. Push your limits.", Color(0xFF22C55E), Icons.AutoMirrored.Filled.DirectionsRun),
    MODERATE("Active Training", "Good recovery level. Ideal for moderate cardio, strength training, or skill work. Stay in zones 2-3 for optimal adaptation.", Color(0xFFEAB308), Icons.Default.FitnessCenter),
    LIGHT("Light Activity", "Your body needs easier movement today. Focus on zone 1-2 work: walking, yoga, light stretching, or mobility drills.", Color(0xFFF97316), Icons.Default.SelfImprovement),
    RECOVERY("Recovery Day", "Your body is signaling it needs rest. Skip the workout and focus on sleep, nutrition, hydration, and gentle movement only.", Color(0xFFEF4444), Icons.Default.Favorite)
}

private data class WorkoutRecommendation(
    val name: String,
    val duration: String,
    val heartRateZone: String,
    val intensity: String,
    val description: String
)

private fun getRecommendedWorkout(zone: TrainingZone, strain: Double): WorkoutRecommendation {
    return when (zone) {
        TrainingZone.PEAK -> WorkoutRecommendation(
            "High-Intensity Intervals",
            "45 min",
            "Zone 4-5",
            "High",
            "Warm up 10min in Zone 2, then 6x3min intervals at Zone 4-5 with 2min recovery. Cool down 10min. This will push your cardiovascular fitness."
        )
        TrainingZone.MODERATE -> {
            if (strain < 8) WorkoutRecommendation(
                "Tempo Run or Strength",
                "40 min",
                "Zone 2-3",
                "Moderate",
                "Choose either a 40-minute tempo run at Zone 3 pace, or a full-body strength session. Keep heart rate below 80% max for steady adaptation."
            ) else WorkoutRecommendation(
                "Easy Cardio + Mobility",
                "30 min",
                "Zone 2",
                "Low-Moderate",
                "Your strain is already elevated. Do a 30-minute easy jog or bike ride in Zone 2, followed by 10 minutes of mobility work."
            )
        }
        TrainingZone.LIGHT -> WorkoutRecommendation(
            "Gentle Movement",
            "20-30 min",
            "Zone 1-2",
            "Low",
            "Go for a 20-30 minute walk, do yoga, or light stretching. Keep your heart rate below 60% max. Focus on breathing and relaxation."
        )
        TrainingZone.RECOVERY -> WorkoutRecommendation(
            "Active Recovery Only",
            "15 min",
            "Zone 1",
            "Very Low",
            "Skip structured training. A gentle 15-minute walk is enough. Prioritize sleep, hydration (3L+), and quality nutrition for recovery."
        )
    }
}

private data class RecoveryTip(
    val title: String,
    val description: String,
    val color: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private fun buildRecoveryTips(healthData: HealthData, readiness: Int, strain: Double): List<RecoveryTip> {
    val tips = mutableListOf<RecoveryTip>()

    // HRV-based
    val hrv = healthData.heartRateVariability.rmssdMs ?: 0.0
    if (hrv < 30) {
        tips.add(RecoveryTip("Low HRV Detected", "Your nervous system is under stress. Focus on deep breathing (4-7-8 pattern) and avoid intense training today.", Color(0xFFEF4444), Icons.Default.Favorite))
    } else if (hrv > 60) {
        tips.add(RecoveryTip("Strong Recovery", "Your HRV indicates excellent recovery. Your body can handle high training loads today.", Color(0xFF22C55E), Icons.Default.Favorite))
    }

    // Strain-based
    if (strain > 14) {
        tips.add(RecoveryTip("High Strain Alert", "You've accumulated significant strain (${String.format("%.1f", strain)}/21). Allow 24-48 hours before another hard session.", Color(0xFFF97316), Icons.Default.Bolt))
    }

    // Sleep-based
    val sleepHours = healthData.sleep.totalDuration?.toMinutes()?.div(60.0) ?: 0.0
    if (sleepHours < 6) {
        tips.add(RecoveryTip("Sleep Deficit", "Only ${String.format("%.1f", sleepHours)}h sleep — your performance will be reduced 10-20%. Consider an easier session.", Color(0xFFEAB308), Icons.Default.Bolt))
    }

    // RHR-based
    val rhr = healthData.restingHeartRate.bpm ?: healthData.heartRate.restingBpm
    if (rhr != null && rhr > 75) {
        tips.add(RecoveryTip("Elevated Resting HR", "Your RHR of $rhr bpm is higher than optimal. This could indicate stress, dehydration, or incomplete recovery.", Color(0xFFF97316), Icons.Default.Favorite))
    }

    if (tips.isEmpty()) {
        tips.add(RecoveryTip("Balanced State", "Your metrics look good. Listen to your body and train according to your plan.", Color(0xFF22C55E), Icons.Default.FitnessCenter))
    }

    return tips
}
