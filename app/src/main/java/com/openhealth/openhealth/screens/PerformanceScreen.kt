package com.openhealth.openhealth.screens

import com.openhealth.openhealth.ui.theme.*
import com.openhealth.openhealth.model.ExerciseSession
import com.openhealth.openhealth.model.HealthData
import com.openhealth.openhealth.model.SettingsData

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceScreen(
    healthData: HealthData,
    settings: SettingsData,
    onBackClick: () -> Unit,
    onSessionClick: (ExerciseSession) -> Unit = {}
) {
    // Derive data
    val activeCalories = healthData.calories.activeBurned
    val totalCalories = healthData.calories.totalBurned
    val exerciseMinutes = healthData.exercise.totalDuration?.toMinutes() ?: 0L
    val steps = healthData.steps.count
    val sessions = healthData.exercise.sessions

    // Goals
    val moveGoal = 600.0 // kcal
    val exerciseGoal = 30L // minutes
    val standGoal = 12 // hours (represented as steps goal fraction)

    // Progress calculations
    val moveProgress = (activeCalories / moveGoal).toFloat().coerceIn(0f, 1f)
    val exerciseProgress = (exerciseMinutes.toFloat() / exerciseGoal).coerceIn(0f, 1f)
    val stepsProgress = healthData.steps.progress
    val overallPercent = ((moveProgress + exerciseProgress + stepsProgress) / 3f * 100).roundToInt().coerceIn(0, 100)

    // Intensity flow — fake hourly data based on real activity
    val intensityBars = remember(sessions, steps) {
        val bars = MutableList(12) { 0f }
        // Distribute activity across the last 12 hours
        val now = Instant.now()
        val zoneId = ZoneId.systemDefault()
        val currentHour = now.atZone(zoneId).hour
        sessions.forEach { session ->
            val sessionHour = session.startTime.atZone(zoneId).hour
            val hoursAgo = (currentHour - sessionHour + 24) % 24
            if (hoursAgo < 12) {
                val idx = 11 - hoursAgo
                if (idx in 0..11) {
                    val durationMin = session.duration.toMinutes().toFloat()
                    bars[idx] = (bars[idx] + (durationMin / 60f).coerceAtMost(1f)).coerceAtMost(1f)
                }
            }
        }
        // Add baseline from steps if any
        if (steps > 0) {
            for (i in bars.indices) {
                if (bars[i] < 0.1f) bars[i] = 0.05f + (steps.toFloat() / 30000f).coerceAtMost(0.15f)
            }
        }
        bars.toList()
    }

    // Neural insight text
    val insightText = remember(activeCalories, exerciseMinutes, steps) {
        when {
            overallPercent >= 80 -> "Outstanding performance today. Your metabolic efficiency is above average with a balanced mix of movement and exercise intensity."
            overallPercent >= 50 -> "Solid progress. Your active energy expenditure suggests a healthy metabolic rate. Consider adding a short high-intensity session to maximize calorie burn."
            overallPercent >= 25 -> "Moderate activity detected. Your body is warming up — a brisk walk or light workout would push your performance metrics into the optimal zone."
            sessions.isNotEmpty() -> "You've started moving! Keep building momentum. Consistent activity throughout the day improves cardiovascular efficiency."
            else -> "No significant activity recorded yet. Even a 10-minute walk can boost your metabolic rate and improve your daily performance score."
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Performance",
                        color = TextOnSurface,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ElectricIndigo
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
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Multi-Ring Donut
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(280.dp)
                        ) {
                            Canvas(modifier = Modifier.size(280.dp)) {
                                val centerX = size.width / 2
                                val centerY = size.height / 2

                                // Ring definitions: outer (Move/pink), middle (Exercise/indigo), inner (Stand/lavender)
                                val outerStroke = 16.dp.toPx()
                                val middleStroke = 14.dp.toPx()
                                val innerStroke = 12.dp.toPx()
                                val ringGap = 10.dp.toPx()

                                val outerRadius = (size.width / 2) - (outerStroke / 2) - 4.dp.toPx()
                                val middleRadius = outerRadius - outerStroke / 2 - ringGap - middleStroke / 2
                                val innerRadius = middleRadius - middleStroke / 2 - ringGap - innerStroke / 2

                                // === GLOW EFFECTS ===
                                // Outer ring glow (Move — magenta)
                                if (moveProgress > 0f) {
                                    drawCircle(
                                        color = VibrantMagenta.copy(alpha = 0.12f),
                                        radius = outerRadius + 12.dp.toPx(),
                                        center = Offset(centerX, centerY)
                                    )
                                }
                                // Middle ring glow (Exercise — indigo)
                                if (exerciseProgress > 0f) {
                                    drawCircle(
                                        color = ElectricIndigo.copy(alpha = 0.10f),
                                        radius = middleRadius + 10.dp.toPx(),
                                        center = Offset(centerX, centerY)
                                    )
                                }
                                // Inner ring glow (Stand — lavender)
                                if (stepsProgress > 0f) {
                                    drawCircle(
                                        color = SoftLavender.copy(alpha = 0.08f),
                                        radius = innerRadius + 8.dp.toPx(),
                                        center = Offset(centerX, centerY)
                                    )
                                }

                                // === TRACK BACKGROUNDS ===
                                // Outer track
                                drawArc(
                                    color = SurfaceHigh,
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(outerStroke, cap = StrokeCap.Round),
                                    topLeft = Offset(centerX - outerRadius, centerY - outerRadius),
                                    size = Size(outerRadius * 2, outerRadius * 2)
                                )
                                // Middle track
                                drawArc(
                                    color = SurfaceHigh,
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(middleStroke, cap = StrokeCap.Round),
                                    topLeft = Offset(centerX - middleRadius, centerY - middleRadius),
                                    size = Size(middleRadius * 2, middleRadius * 2)
                                )
                                // Inner track
                                drawArc(
                                    color = SurfaceHigh,
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(innerStroke, cap = StrokeCap.Round),
                                    topLeft = Offset(centerX - innerRadius, centerY - innerRadius),
                                    size = Size(innerRadius * 2, innerRadius * 2)
                                )

                                // === PROGRESS ARCS WITH GRADIENTS ===
                                // Outer arc (Move — pink/magenta gradient)
                                if (moveProgress > 0f) {
                                    drawArc(
                                        brush = Brush.sweepGradient(
                                            colors = listOf(
                                                CardHeartRate,
                                                VibrantMagenta,
                                                CardHeartRate
                                            )
                                        ),
                                        startAngle = -90f,
                                        sweepAngle = 360f * moveProgress,
                                        useCenter = false,
                                        style = Stroke(outerStroke, cap = StrokeCap.Round),
                                        topLeft = Offset(centerX - outerRadius, centerY - outerRadius),
                                        size = Size(outerRadius * 2, outerRadius * 2)
                                    )
                                    // Glow along the end of the arc
                                    val endAngle = Math.toRadians((-90.0 + 360.0 * moveProgress))
                                    val glowX = centerX + outerRadius * cos(endAngle).toFloat()
                                    val glowY = centerY + outerRadius * sin(endAngle).toFloat()
                                    drawCircle(
                                        color = VibrantMagenta.copy(alpha = 0.35f),
                                        radius = outerStroke * 1.2f,
                                        center = Offset(glowX, glowY)
                                    )
                                }

                                // Middle arc (Exercise — indigo gradient)
                                if (exerciseProgress > 0f) {
                                    drawArc(
                                        brush = Brush.sweepGradient(
                                            colors = listOf(
                                                ElectricIndigo,
                                                ElectricIndigoDim,
                                                ElectricIndigo
                                            )
                                        ),
                                        startAngle = -90f,
                                        sweepAngle = 360f * exerciseProgress,
                                        useCenter = false,
                                        style = Stroke(middleStroke, cap = StrokeCap.Round),
                                        topLeft = Offset(centerX - middleRadius, centerY - middleRadius),
                                        size = Size(middleRadius * 2, middleRadius * 2)
                                    )
                                    val endAngle = Math.toRadians((-90.0 + 360.0 * exerciseProgress))
                                    val glowX = centerX + middleRadius * cos(endAngle).toFloat()
                                    val glowY = centerY + middleRadius * sin(endAngle).toFloat()
                                    drawCircle(
                                        color = ElectricIndigo.copy(alpha = 0.35f),
                                        radius = middleStroke * 1.2f,
                                        center = Offset(glowX, glowY)
                                    )
                                }

                                // Inner arc (Stand — lavender gradient)
                                if (stepsProgress > 0f) {
                                    drawArc(
                                        brush = Brush.sweepGradient(
                                            colors = listOf(
                                                SoftLavender,
                                                Tertiary,
                                                SoftLavender
                                            )
                                        ),
                                        startAngle = -90f,
                                        sweepAngle = 360f * stepsProgress,
                                        useCenter = false,
                                        style = Stroke(innerStroke, cap = StrokeCap.Round),
                                        topLeft = Offset(centerX - innerRadius, centerY - innerRadius),
                                        size = Size(innerRadius * 2, innerRadius * 2)
                                    )
                                    val endAngle = Math.toRadians((-90.0 + 360.0 * stepsProgress))
                                    val glowX = centerX + innerRadius * cos(endAngle).toFloat()
                                    val glowY = centerY + innerRadius * sin(endAngle).toFloat()
                                    drawCircle(
                                        color = SoftLavender.copy(alpha = 0.35f),
                                        radius = innerStroke * 1.2f,
                                        center = Offset(glowX, glowY)
                                    )
                                }
                            }

                            // Center text
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$overallPercent%",
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextOnSurface
                                )
                                Text(
                                    text = "DAILY GOAL",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSubtle,
                                    letterSpacing = 2.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Legend dots
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LegendDot(color = VibrantMagenta, label = "MOVE")
                            LegendDot(color = ElectricIndigo, label = "EXERCISE")
                            LegendDot(color = SoftLavender, label = "STAND")
                        }
                    }
                }
            }

            // Active Energy Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceHigh)
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ACTIVE ENERGY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSubtle,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text(
                                    text = "${activeCalories.roundToInt()}",
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextOnSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Kcal",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ElectricIndigo,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                        }

                        // Bolt icon with glow
                        Box(contentAlignment = Alignment.Center) {
                            // Glow behind icon
                            Canvas(modifier = Modifier.size(56.dp)) {
                                drawCircle(
                                    color = ElectricIndigo.copy(alpha = 0.15f),
                                    radius = size.width / 2
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(ElectricIndigo.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = ElectricIndigo,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Exercise + Steps side by side
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Exercise card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(SurfaceHigh)
                            .padding(20.dp)
                    ) {
                        Column {
                            Text(
                                text = "EXERCISE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSubtle,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "${exerciseMinutes}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextOnSurface
                            )
                            Text(
                                text = "min",
                                fontSize = 13.sp,
                                color = TextOnSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            // Progress bar with glow
                            GlowProgressBar(
                                progress = exerciseProgress,
                                color = VibrantMagenta,
                                glowColor = VibrantMagenta.copy(alpha = 0.3f)
                            )
                        }
                    }

                    // Steps card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(SurfaceHigh)
                            .padding(20.dp)
                    ) {
                        Column {
                            Text(
                                text = "STEPS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSubtle,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "${steps}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextOnSurface
                            )
                            Text(
                                text = "steps",
                                fontSize = 13.sp,
                                color = TextOnSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            // Progress bar with glow
                            GlowProgressBar(
                                progress = stepsProgress,
                                color = ElectricIndigo,
                                glowColor = ElectricIndigo.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }

            // Neural Insight Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    ElectricIndigo.copy(alpha = 0.10f),
                                    VibrantMagenta.copy(alpha = 0.10f)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Box(contentAlignment = Alignment.Center) {
                            // Glow behind icon
                            Canvas(modifier = Modifier.size(48.dp)) {
                                drawCircle(
                                    color = ElectricIndigo.copy(alpha = 0.12f),
                                    radius = size.width / 2
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(ElectricIndigo.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = ElectricIndigo,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Neural Insight",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextOnSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = insightText,
                                fontSize = 13.sp,
                                color = TextOnSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Intensity Flow Section
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Intensity Flow",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextOnSurface
                        )
                        Text(
                            text = "LAST 12 HOURS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSubtle,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(SurfaceLow)
                            .padding(horizontal = 16.dp, vertical = 20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            intensityBars.forEachIndexed { index, value ->
                                val isActive = value > 0.15f
                                val barHeight = (value * 80).coerceAtLeast(8f)
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        // Bar with optional glow
                                        Box(contentAlignment = Alignment.BottomCenter) {
                                            if (isActive) {
                                                // Glow behind active bar
                                                Canvas(
                                                    modifier = Modifier
                                                        .width(8.dp)
                                                        .height(barHeight.dp + 4.dp)
                                                ) {
                                                    drawRoundRect(
                                                        color = ElectricIndigo.copy(alpha = 0.2f),
                                                        size = Size(size.width + 4.dp.toPx(), size.height),
                                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                                                    )
                                                }
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .width(6.dp)
                                                    .height(barHeight.dp)
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(
                                                        if (isActive)
                                                            Brush.verticalGradient(
                                                                colors = listOf(ElectricIndigo, ElectricIndigoDim)
                                                            )
                                                        else
                                                            Brush.verticalGradient(
                                                                colors = listOf(SurfaceHigh, SurfaceHigh)
                                                            )
                                                    )
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        // Hour label for every 3rd bar
                                        if (index % 3 == 0) {
                                            val now = Instant.now().atZone(ZoneId.systemDefault()).hour
                                            val hourLabel = ((now - 11 + index) % 24 + 24) % 24
                                            Text(
                                                text = "${hourLabel}h",
                                                fontSize = 9.sp,
                                                color = TextSubtle
                                            )
                                        } else {
                                            Text(
                                                text = "",
                                                fontSize = 9.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Latest Sessions Section
            item {
                if (sessions.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Latest Sessions",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextOnSurface
                        )
                        Text(
                            text = "VIEW ALL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ElectricIndigo,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // Session rows
            items(sessions.take(5)) { session ->
                SessionRow(
                    session = session,
                    onClick = { onSessionClick(session) }
                )
            }

            // Bottom spacer
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSubtle,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun GlowProgressBar(
    progress: Float,
    color: Color,
    glowColor: Color,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
        ) {
            val barHeight = size.height
            val cornerRadius = barHeight / 2

            // Track background
            drawRoundRect(
                color = SurfaceLow,
                size = Size(size.width, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius)
            )

            // Glow behind progress
            if (progress > 0f) {
                drawRoundRect(
                    color = glowColor,
                    size = Size(size.width * progress + 4.dp.toPx(), barHeight + 4.dp.toPx()),
                    topLeft = Offset(0f, -2.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius + 2.dp.toPx())
                )
            }

            // Progress fill
            if (progress > 0f) {
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(color, color.copy(alpha = 0.7f))
                    ),
                    size = Size(size.width * progress, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius)
                )
            }
        }
    }
}

@Composable
private fun SessionRow(
    session: ExerciseSession,
    onClick: () -> Unit
) {
    val durationMin = session.duration.toMinutes()
    val calories = session.caloriesBurned?.roundToInt() ?: 0
    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
    val sessionTime = session.startTime.atZone(ZoneId.systemDefault()).format(timeFormatter)

    val icon: ImageVector = when {
        session.exerciseType.contains("run", ignoreCase = true) -> Icons.AutoMirrored.Filled.DirectionsRun
        session.exerciseType.contains("walk", ignoreCase = true) -> Icons.AutoMirrored.Filled.DirectionsWalk
        session.exerciseType.contains("yoga", ignoreCase = true) ||
            session.exerciseType.contains("meditation", ignoreCase = true) -> Icons.Default.SelfImprovement
        session.exerciseType.contains("strength", ignoreCase = true) ||
            session.exerciseType.contains("weight", ignoreCase = true) -> Icons.Default.FitnessCenter
        else -> Icons.Default.FitnessCenter
    }

    val iconColor = when {
        session.exerciseType.contains("run", ignoreCase = true) -> VibrantMagenta
        session.exerciseType.contains("walk", ignoreCase = true) -> SuccessGreen
        session.exerciseType.contains("yoga", ignoreCase = true) -> SoftLavender
        else -> ElectricIndigo
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceHigh)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Icon with glow
                Box(contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.size(48.dp)) {
                        drawCircle(
                            color = iconColor.copy(alpha = 0.12f),
                            radius = size.width / 2
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(iconColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = session.exerciseType,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextOnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${durationMin}min • $sessionTime",
                        fontSize = 12.sp,
                        color = TextSubtle
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (calories > 0) {
                    Text(
                        text = "${calories} kcal",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ElectricIndigo
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextSubtle,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
