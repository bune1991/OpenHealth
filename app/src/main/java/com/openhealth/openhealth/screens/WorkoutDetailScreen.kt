package com.openhealth.openhealth.screens

import com.openhealth.openhealth.ui.theme.*
import com.openhealth.openhealth.model.ExerciseSession
import com.openhealth.openhealth.model.HealthData

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Speed
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    session: ExerciseSession,
    healthData: HealthData,
    onBackClick: () -> Unit
) {
    val zone = ZoneId.systemDefault()
    val startZoned = session.startTime.atZone(zone)
    val endZoned = session.endTime.atZone(zone)
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
    val dateStr = startZoned.format(dateFormatter)
    val timeStr = "${startZoned.format(timeFormatter)} - ${endZoned.format(timeFormatter)}"

    val durationMin = session.duration.toMinutes()
    val durationSec = session.duration.seconds % 60
    val durationStr = "${durationMin}:${String.format("%02d", durationSec)}"

    val distanceKm = session.distance?.let { it / 1000.0 }
    val calories = session.caloriesBurned?.let { String.format("%.0f", it) } ?: "${durationMin * 7}"

    // Estimate steps from duration for walking/running, fallback to healthData steps
    val estimatedSteps = when {
        session.exerciseType.contains("Walk", ignoreCase = true) -> (durationMin * 100).toString()
        session.exerciseType.contains("Run", ignoreCase = true) -> (durationMin * 160).toString()
        healthData.steps.count > 0 -> healthData.steps.count.toString()
        else -> "--"
    }

    // Calculate pace if distance is available
    val paceStr = if (distanceKm != null && distanceKm > 0 && durationMin > 0) {
        val paceMinPerKm = durationMin.toDouble() / distanceKm
        val paceMins = paceMinPerKm.toInt()
        val paceSecs = ((paceMinPerKm - paceMins) * 60).toInt()
        "${paceMins}:${String.format("%02d", paceSecs)}/km"
    } else null

    // Heart rate data
    val avgHr = healthData.heartRate.readings
        .filter { it.timestamp >= session.startTime && it.timestamp <= session.endTime }
        .let { readings ->
            if (readings.isNotEmpty()) readings.map { it.bpm }.average().toInt()
            else healthData.heartRate.currentBpm ?: healthData.heartRate.restingBpm
        }

    Scaffold(
        containerColor = SurfaceLowest,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Workout Detail",
                        color = TextOnSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextOnSurface
                        )
                    }
                },
                actions = {
                    // Balance the back arrow for centering
                    Spacer(modifier = Modifier.size(48.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceLowest
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(SurfaceLowest)
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Exercise type badge
            item {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(VibrantMagenta.copy(alpha = 0.15f))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = session.exerciseType.uppercase(),
                        color = VibrantMagenta,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }
            }

            // Session name (exercise type as title)
            item {
                Text(
                    text = session.exerciseType,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextOnSurface
                )
            }

            // Date + time subtitle
            item {
                Column {
                    Text(
                        text = dateStr,
                        fontSize = 14.sp,
                        color = TextOnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = timeStr,
                        fontSize = 13.sp,
                        color = TextSubtle
                    )
                }
            }

            // Pace badge (if available)
            if (paceStr != null) {
                item {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(ElectricIndigo.copy(alpha = 0.12f))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Speed,
                                contentDescription = null,
                                tint = ElectricIndigo,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = paceStr,
                                color = ElectricIndigo,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // 2x2 Stats Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Route,
                            iconColor = CardDistance,
                            label = "Distance",
                            value = distanceKm?.let { String.format("%.2f", it) } ?: "--",
                            unit = "km"
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                            iconColor = CardSteps,
                            label = "Steps",
                            value = estimatedSteps,
                            unit = ""
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Timer,
                            iconColor = SoftLavender,
                            label = "Duration",
                            value = durationStr,
                            unit = ""
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.LocalFireDepartment,
                            iconColor = CardCalories,
                            label = "Calories Burned",
                            value = calories,
                            unit = "kcal"
                        )
                    }
                }
            }

            // Vitals & Trends section header
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Vitals & Trends",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextOnSurface
                )
            }

            // Average Heart Rate card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceHigh)
                        .padding(20.dp)
                ) {
                    Column {
                        Text(
                            text = "AVERAGE HEART RATE",
                            fontSize = 11.sp,
                            color = TextSubtle,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = avgHr?.toString() ?: "--",
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold,
                                color = VibrantMagenta
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "BPM",
                                fontSize = 14.sp,
                                color = TextSubtle,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        // Bar chart visualization
                        HeartRateBarChart(
                            readings = healthData.heartRate.readings
                                .filter { it.timestamp >= session.startTime && it.timestamp <= session.endTime }
                                .map { it.bpm },
                            avgBpm = avgHr
                        )
                    }
                }
            }

            // AI Health Insight card
            item {
                val insightText = generateWorkoutInsight(session, avgHr, distanceKm, durationMin)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    ElectricIndigo.copy(alpha = 0.2f),
                                    VibrantMagenta.copy(alpha = 0.15f)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = ElectricIndigo,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AI HEALTH INSIGHT",
                                fontSize = 11.sp,
                                color = ElectricIndigo,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = insightText,
                            fontSize = 14.sp,
                            color = TextOnSurfaceVariant,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            // Bottom spacer
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconColor: androidx.compose.ui.graphics.Color,
    label: String,
    value: String,
    unit: String
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceLow)
            .padding(16.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = TextSubtle,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextOnSurface
                )
                if (unit.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unit,
                        fontSize = 12.sp,
                        color = TextSubtle,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HeartRateBarChart(
    readings: List<Int>,
    avgBpm: Int?
) {
    // If we have real readings, bucket them; otherwise generate a sample visualization
    val bars = if (readings.size >= 2) {
        val bucketCount = 12.coerceAtMost(readings.size)
        val bucketSize = readings.size / bucketCount
        (0 until bucketCount).map { i ->
            val start = i * bucketSize
            val end = if (i == bucketCount - 1) readings.size else start + bucketSize
            readings.subList(start, end).average().toFloat()
        }
    } else {
        // Sample visualization when no per-session HR readings are available
        val base = (avgBpm ?: 90).toFloat()
        listOf(0.6f, 0.7f, 0.85f, 0.75f, 0.95f, 1.0f, 0.9f, 0.8f, 0.7f, 0.65f, 0.55f, 0.5f)
            .map { it * base }
    }

    val maxVal = bars.maxOrNull() ?: 1f
    val minVal = bars.minOrNull() ?: 0f
    val range = (maxVal - minVal).coerceAtLeast(1f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        bars.forEach { barVal ->
            val fraction = ((barVal - minVal) / range).coerceIn(0.1f, 1f)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp)
                    .fillMaxHeight(fraction)
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (fraction > 0.75f) VibrantMagenta
                        else VibrantMagenta.copy(alpha = 0.4f)
                    )
            )
        }
    }
}

private fun generateWorkoutInsight(
    session: ExerciseSession,
    avgHr: Int?,
    distanceKm: Double?,
    durationMin: Long
): String {
    val type = session.exerciseType
    val cal = session.caloriesBurned?.let { String.format("%.0f", it) } ?: "${durationMin * 7}"

    return when {
        avgHr != null && avgHr > 140 && durationMin > 20 ->
            "High-intensity $type session detected. Your sustained heart rate of ${avgHr} BPM over ${durationMin} minutes suggests strong cardiovascular engagement. Post-exercise metabolic recovery will elevate calorie burn for the next 2-4 hours."

        distanceKm != null && distanceKm > 3.0 ->
            "Great distance coverage at ${String.format("%.1f", distanceKm)} km. Your $type session burned approximately $cal kcal. Consistent sessions at this distance improve aerobic base and promote fat oxidation efficiency."

        durationMin > 30 ->
            "Solid ${durationMin}-minute $type session. Your body entered the aerobic fat-burning zone, optimizing metabolic recovery. Maintaining this duration 3-4 times per week supports long-term cardiovascular health."

        avgHr != null ->
            "Your $type session averaged ${avgHr} BPM over ${durationMin} minutes. This moderate effort supports recovery-phase training and helps maintain baseline fitness between higher-intensity sessions."

        else ->
            "Your ${durationMin}-minute $type session contributed $cal kcal to your daily energy expenditure. Regular activity at this level supports metabolic health and overall well-being."
    }
}
