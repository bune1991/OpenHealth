package com.openhealth.openhealth.screens
import com.openhealth.openhealth.ui.theme.*

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.openhealth.openhealth.model.DailyDataPoint
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DialogBg = Color(0xFF121212)

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
    val dataMap = remember(data) { data.associateBy { it.date } }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DialogBg
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                // Month header with arrows
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        displayedMonth = displayedMonth.minusMonths(1)
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowLeft,
                            contentDescription = "Previous Month",
                            tint = TextOnSurfaceVariant
                        )
                    }
                    Text(
                        text = displayedMonth.format(
                            DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextOnSurface,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = {
                            if (displayedMonth.isBefore(YearMonth.from(today))) {
                                displayedMonth = displayedMonth.plusMonths(1)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                            contentDescription = "Next Month",
                            tint = if (displayedMonth.isBefore(YearMonth.from(today)))
                                TextOnSurfaceVariant else TextSubtle.copy(alpha = 0.3f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Day-of-week headers
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                        Text(
                            text = day,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSubtle,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Calendar grid — exact same as StepRingsCalendar
                val firstOfMonth = displayedMonth.atDay(1)
                val startDayOfWeek = firstOfMonth.dayOfWeek.value % 7 // Sunday = 0
                val daysInMonth = displayedMonth.lengthOfMonth()

                var dayCounter = 1
                for (week in 0..5) {
                    if (dayCounter > daysInMonth) break
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (col in 0..6) {
                            val cellIndex = week * 7 + col
                            if (cellIndex < startDayOfWeek || dayCounter > daysInMonth) {
                                Spacer(modifier = Modifier.weight(1f).height(48.dp))
                            } else {
                                val date = displayedMonth.atDay(dayCounter)
                                val steps = dataMap[date]?.value ?: 0.0
                                val isSelected = date == initialDate
                                val isFuture = date.isAfter(today)
                                val dayNum = dayCounter

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .then(
                                            if (!isFuture) Modifier.clickable {
                                                onDateSelected(date)
                                            } else Modifier
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Ring drawing — copied from StepRingsCalendar
                                    val progress = (steps / goal).toFloat().coerceIn(0f, 1f)
                                    val ringColor = when {
                                        isFuture -> Color.Transparent
                                        steps <= 0 -> Color.Gray.copy(alpha = 0.2f)
                                        steps < 5000 -> Color(0xFFE53935)
                                        steps < 8000 -> Color(0xFFFFA726)
                                        else -> Color(0xFF66BB6A)
                                    }

                                    if (!isFuture) {
                                        Canvas(modifier = Modifier.size(36.dp)) {
                                            val strokeW = 3.dp.toPx()
                                            // Background ring
                                            drawArc(
                                                color = ringColor.copy(alpha = 0.2f),
                                                startAngle = -90f,
                                                sweepAngle = 360f,
                                                useCenter = false,
                                                style = Stroke(width = strokeW, cap = StrokeCap.Round),
                                                topLeft = Offset(strokeW / 2, strokeW / 2),
                                                size = Size(size.width - strokeW, size.height - strokeW)
                                            )
                                            // Progress ring
                                            if (steps > 0) {
                                                drawArc(
                                                    color = ringColor,
                                                    startAngle = -90f,
                                                    sweepAngle = 360f * progress,
                                                    useCenter = false,
                                                    style = Stroke(width = strokeW, cap = StrokeCap.Round),
                                                    topLeft = Offset(strokeW / 2, strokeW / 2),
                                                    size = Size(size.width - strokeW, size.height - strokeW)
                                                )
                                            }
                                            // Today: cyan outline on top
                                            if (date == today && !isSelected) {
                                                drawArc(
                                                    color = CardSteps,
                                                    startAngle = -90f,
                                                    sweepAngle = 360f,
                                                    useCenter = false,
                                                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
                                                    topLeft = Offset(strokeW / 2, strokeW / 2),
                                                    size = Size(size.width - strokeW, size.height - strokeW)
                                                )
                                            }
                                        }
                                    }

                                    // Day number
                                    Text(
                                        text = dayNum.toString(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = when {
                                            isSelected -> CardSteps
                                            isFuture -> TextSubtle.copy(alpha = 0.3f)
                                            else -> TextOnSurface
                                        },
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                }
                                dayCounter++
                            }
                        }
                    }
                }
            }
        }
    }
}
