package com.openhealth.openhealth.screens

import com.openhealth.openhealth.ui.theme.*

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.openhealth.openhealth.model.DailyDataPoint
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CustomCalendarDialog(
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    data: List<DailyDataPoint> = emptyList(),
    goal: Int = 10000
) {
    val today = LocalDate.now(ZoneId.systemDefault())
    var displayedMonth by remember { mutableStateOf(YearMonth.from(initialDate)) }
    var selectedDate by remember { mutableStateOf(initialDate) }
    val dataMap = remember(data) { data.associateBy { it.date } }

    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(ElectricIndigo, VibrantMagenta)
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceLowest)
                .padding(20.dp)
        ) {
            Column {
                // ── Header: X close | "Health Pulse" | month nav ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Close button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextOnSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // "Health Pulse" title
                    Text(
                        text = "Health Pulse",
                        style = MaterialTheme.typography.titleMedium,
                        color = ElectricIndigo,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Month/year with chevron arrows
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { displayedMonth = displayedMonth.minusMonths(1) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Previous Month",
                                tint = TextOnSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Text(
                            text = displayedMonth.format(
                                DateTimeFormatter.ofPattern("MMM yyyy", Locale.getDefault())
                            ).uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = TextOnSurface,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        )

                        val canGoForward = displayedMonth.isBefore(YearMonth.from(today))
                        IconButton(
                            onClick = {
                                if (canGoForward) {
                                    displayedMonth = displayedMonth.plusMonths(1)
                                }
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Next Month",
                                tint = if (canGoForward) TextOnSurfaceVariant
                                else TextSubtle.copy(alpha = 0.3f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Day labels row: SUN MON TUE ... SAT ──
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT").forEach { day ->
                        Text(
                            text = day,
                            fontSize = 10.sp,
                            color = TextOnSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.2.sp,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ── Calendar grid ──
                val firstOfMonth = displayedMonth.atDay(1)
                val startDayOfWeek = firstOfMonth.dayOfWeek.value % 7 // Sunday = 0
                val daysInMonth = displayedMonth.lengthOfMonth()

                var dayCounter = 1
                for (week in 0..5) {
                    if (dayCounter > daysInMonth) break
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (col in 0..6) {
                            val cellIndex = week * 7 + col
                            if (cellIndex < startDayOfWeek || dayCounter > daysInMonth) {
                                Spacer(modifier = Modifier.weight(1f).height(46.dp))
                            } else {
                                val date = displayedMonth.atDay(dayCounter)
                                val steps = dataMap[date]?.value ?: 0.0
                                val isSelected = date == selectedDate
                                val isFuture = date.isAfter(today)
                                val dayNum = dayCounter
                                val progress = (steps / goal).toFloat().coerceIn(0f, 1f)

                                // Alternate ring color: even days indigo, odd days magenta
                                val ringColor = if (dayNum % 2 == 0) ElectricIndigo else VibrantMagenta

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                        .then(
                                            if (!isFuture) Modifier.clickable {
                                                selectedDate = date
                                            } else Modifier
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val ringSize = if (isSelected) 40.dp else 36.dp

                                    if (!isFuture) {
                                        // Glow effect behind selected day
                                        if (isSelected) {
                                            Canvas(modifier = Modifier.size(44.dp)) {
                                                drawCircle(
                                                    color = ElectricIndigo.copy(alpha = 0.20f),
                                                    radius = size.minDimension / 2f
                                                )
                                            }
                                        }

                                        Canvas(modifier = Modifier.size(ringSize)) {
                                            val strokeW = if (isSelected) 3.5.dp.toPx() else 3.dp.toPx()
                                            val arcOffset = Offset(strokeW / 2, strokeW / 2)
                                            val arcSize = Size(
                                                size.width - strokeW,
                                                size.height - strokeW
                                            )

                                            // Background track ring
                                            drawArc(
                                                color = SurfaceHighest,
                                                startAngle = -90f,
                                                sweepAngle = 360f,
                                                useCenter = false,
                                                style = Stroke(
                                                    width = strokeW,
                                                    cap = StrokeCap.Round
                                                ),
                                                topLeft = arcOffset,
                                                size = arcSize
                                            )

                                            // Progress ring
                                            if (steps > 0) {
                                                drawArc(
                                                    color = ringColor,
                                                    startAngle = -90f,
                                                    sweepAngle = 360f * progress,
                                                    useCenter = false,
                                                    style = Stroke(
                                                        width = strokeW,
                                                        cap = StrokeCap.Round
                                                    ),
                                                    topLeft = arcOffset,
                                                    size = arcSize
                                                )
                                            }

                                            // Selected: full ElectricIndigo ring on top
                                            if (isSelected) {
                                                drawArc(
                                                    color = ElectricIndigo,
                                                    startAngle = -90f,
                                                    sweepAngle = 360f,
                                                    useCenter = false,
                                                    style = Stroke(
                                                        width = strokeW,
                                                        cap = StrokeCap.Round
                                                    ),
                                                    topLeft = arcOffset,
                                                    size = arcSize
                                                )
                                            }
                                        }
                                    }

                                    // Day number
                                    Text(
                                        text = dayNum.toString(),
                                        fontSize = if (isSelected) 13.sp else 12.sp,
                                        color = when {
                                            isSelected -> ElectricIndigo
                                            isFuture -> TextSubtle.copy(alpha = 0.3f)
                                            else -> TextOnSurface
                                        },
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                dayCounter++
                            }
                        }
                    }
                }

                // ── Daily Recap Card ──
                val selectedData = dataMap[selectedDate]
                val selectedSteps = selectedData?.value ?: 0.0

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceMid)
                        .padding(16.dp)
                ) {
                    Column {
                        // Date label + "ACTIVE" badge row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // "FRIDAY OCT 4" style label
                            val dayName = selectedDate.dayOfWeek
                                .getDisplayName(TextStyle.FULL, Locale.getDefault())
                                .uppercase()
                            val monthDay = selectedDate.format(
                                DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
                            ).uppercase()
                            Text(
                                text = "$dayName $monthDay",
                                fontSize = 10.sp,
                                color = TextSubtle,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 1.2.sp
                            )

                            // "ACTIVE" gradient pill badge
                            if (selectedSteps > 0) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(gradientBrush)
                                        .padding(horizontal = 10.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "ACTIVE",
                                        fontSize = 9.sp,
                                        color = TextOnSurface,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // "Daily Recap" title
                        Text(
                            text = "Daily Recap",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextOnSurface,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 3 stat boxes
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            RecapStatItem(
                                icon = Icons.Default.DirectionsWalk,
                                iconColor = CardSteps,
                                label = "Steps",
                                value = formatStepCount(selectedSteps)
                            )
                            RecapStatItem(
                                icon = Icons.Default.LocalFireDepartment,
                                iconColor = CardCalories,
                                label = "Calories",
                                value = "${(selectedSteps * 0.04).toInt()} kcal"
                            )
                            RecapStatItem(
                                icon = Icons.Default.Bedtime,
                                iconColor = CardSleep,
                                label = "Sleep",
                                value = "--"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── "Confirm Date" button ──
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(50))
                        .background(gradientBrush)
                        .clickable { onDateSelected(selectedDate) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Confirm Date",
                        color = TextOnSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun RecapStatItem(
    icon: ImageVector,
    iconColor: Color,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Icon circle
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            color = TextOnSurface,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = TextSubtle,
            fontSize = 10.sp,
            letterSpacing = 0.5.sp
        )
    }
}

private fun formatStepCount(steps: Double): String {
    return when {
        steps >= 1000 -> String.format("%.1fk", steps / 1000)
        steps > 0 -> steps.toInt().toString()
        else -> "--"
    }
}
