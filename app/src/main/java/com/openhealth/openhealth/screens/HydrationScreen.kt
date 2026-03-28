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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HydrationEntry(
    val amount: Int,
    val time: Long,
    val type: String = "Glass of Water"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HydrationScreen(
    hydrationEntries: List<HydrationEntry>,
    dailyTotal: Int,
    goal: Int,
    onAddWater: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    val progress = (dailyTotal.toFloat() / goal).coerceIn(0f, 1f)
    val percent = (progress * 100).toInt()
    val consumedLiters = dailyTotal / 1000.0
    val goalLiters = goal / 1000.0

    // Frequency label
    val frequencyLabel = when {
        hydrationEntries.size >= 8 -> "High"
        hydrationEntries.size >= 4 -> "Normal"
        else -> "Low"
    }

    // Last entry
    val lastEntry = hydrationEntries.maxByOrNull { it.time }
    val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())

    // Hydration IQ insight
    val insightText = when {
        percent >= 100 -> "Excellent! You've reached your daily hydration goal. Keep maintaining this habit for optimal health."
        percent >= 75 -> "Great progress! You're almost at your goal. A couple more glasses and you'll be fully hydrated."
        percent >= 50 -> "You're halfway there. Try to drink water regularly throughout the rest of the day."
        percent >= 25 -> "Your intake is below target. Set reminders to drink water every hour."
        hydrationEntries.isEmpty() -> "No water logged yet today. Start by drinking a glass of water now!"
        else -> "Low hydration detected. Dehydration can impact focus and energy. Drink water soon."
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Hydration",
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
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(ElectricIndigo, VibrantMagenta)
                        )
                    )
                    .clickable { showAddDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Water",
                    tint = TextOnSurface,
                    modifier = Modifier.size(28.dp)
                )
            }
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
            // Hero Ring
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(240.dp)
                        ) {
                            Canvas(modifier = Modifier.size(240.dp)) {
                                val strokeWidth = 14.dp.toPx()
                                val arcSize = size.width - strokeWidth
                                val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                                // Background track
                                drawArc(
                                    color = SurfaceHigh,
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(strokeWidth, cap = StrokeCap.Round),
                                    topLeft = topLeft,
                                    size = Size(arcSize, arcSize)
                                )

                                // Progress arc with gradient
                                if (progress > 0f) {
                                    drawArc(
                                        brush = Brush.sweepGradient(
                                            colors = listOf(ElectricIndigo, VibrantMagenta, ElectricIndigo)
                                        ),
                                        startAngle = -90f,
                                        sweepAngle = 360f * progress,
                                        useCenter = false,
                                        style = Stroke(strokeWidth, cap = StrokeCap.Round),
                                        topLeft = topLeft,
                                        size = Size(arcSize, arcSize)
                                    )
                                }
                            }

                            // Center text
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.WaterDrop,
                                    contentDescription = null,
                                    tint = ElectricIndigo,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "%.1fL".format(consumedLiters),
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextOnSurface
                                )
                                Text(
                                    text = "/ %.1fL GOAL".format(goalLiters),
                                    fontSize = 14.sp,
                                    color = TextOnSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Percentage pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            ElectricIndigo.copy(alpha = 0.2f),
                                            VibrantMagenta.copy(alpha = 0.2f)
                                        )
                                    )
                                )
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "$percent% Daily Intake",
                                color = ElectricIndigo,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Stat Cards Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // LAST ENTRY card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(SurfaceLow)
                            .padding(20.dp)
                    ) {
                        Column {
                            Text(
                                text = "LAST ENTRY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSubtle,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            if (lastEntry != null) {
                                Text(
                                    text = "${lastEntry.amount}ml",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextOnSurface
                                )
                                Text(
                                    text = timeFormatter.format(Date(lastEntry.time)),
                                    fontSize = 13.sp,
                                    color = TextOnSurfaceVariant
                                )
                            } else {
                                Text(
                                    text = "--",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextOnSurface
                                )
                                Text(
                                    text = "No entries",
                                    fontSize = 13.sp,
                                    color = TextOnSurfaceVariant
                                )
                            }
                        }
                    }

                    // FREQUENCY card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(SurfaceLow)
                            .padding(20.dp)
                    ) {
                        Column {
                            Text(
                                text = "FREQUENCY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSubtle,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = frequencyLabel,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (frequencyLabel) {
                                    "High" -> SuccessGreen
                                    "Normal" -> ElectricIndigo
                                    else -> VibrantMagenta
                                }
                            )
                            Text(
                                text = "${hydrationEntries.size} entries today",
                                fontSize = 13.sp,
                                color = TextOnSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Hydration IQ Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    ElectricIndigo.copy(alpha = 0.12f),
                                    VibrantMagenta.copy(alpha = 0.08f)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(ElectricIndigo.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = ElectricIndigo,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Hydration IQ",
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

            // TODAY'S HISTORY header
            item {
                Text(
                    text = "TODAY'S HISTORY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSubtle,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // History list
            if (hydrationEntries.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(SurfaceLow)
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.WaterDrop,
                                contentDescription = null,
                                tint = TextSubtle,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No entries yet",
                                fontSize = 16.sp,
                                color = TextOnSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Tap + to log your water intake",
                                fontSize = 13.sp,
                                color = TextSubtle
                            )
                        }
                    }
                }
            } else {
                items(hydrationEntries.sortedByDescending { it.time }) { entry ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(SurfaceLow)
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(ElectricIndigo.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WaterDrop,
                                        contentDescription = null,
                                        tint = ElectricIndigo,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = entry.type,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextOnSurface
                                    )
                                    Text(
                                        text = timeFormatter.format(Date(entry.time)),
                                        fontSize = 12.sp,
                                        color = TextSubtle
                                    )
                                }
                            }
                            Text(
                                text = "${entry.amount}ml",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricIndigo
                            )
                        }
                    }
                }
            }

            // Bottom spacer for FAB clearance
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Add Water Dialog
    if (showAddDialog) {
        AddWaterDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { amount ->
                onAddWater(amount)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AddWaterDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var customAmount by remember { mutableStateOf("") }
    val presets = listOf(250, 500, 750, 1000)

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceMid)
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "Add Water",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextOnSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Select a preset or enter a custom amount",
                    fontSize = 14.sp,
                    color = TextOnSurfaceVariant
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Preset buttons in 2x2 grid
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        presets.take(2).forEach { amount ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SurfaceHigh)
                                    .clickable { onConfirm(amount) }
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (amount >= 1000) "${amount / 1000}L" else "${amount}ml",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricIndigo
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        presets.drop(2).forEach { amount ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SurfaceHigh)
                                    .clickable { onConfirm(amount) }
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (amount >= 1000) "${amount / 1000}L" else "${amount}ml",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricIndigo
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Custom amount input
                OutlinedTextField(
                    value = customAmount,
                    onValueChange = { newVal ->
                        if (newVal.all { it.isDigit() } && newVal.length <= 5) {
                            customAmount = newVal
                        }
                    },
                    label = { Text("Custom amount (ml)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricIndigo,
                        unfocusedBorderColor = TextSubtle,
                        focusedLabelColor = ElectricIndigo,
                        unfocusedLabelColor = TextSubtle,
                        cursorColor = ElectricIndigo,
                        focusedTextColor = TextOnSurface,
                        unfocusedTextColor = TextOnSurface
                    ),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "Cancel",
                            color = TextOnSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (customAmount.isNotBlank() && (customAmount.toIntOrNull() ?: 0) > 0)
                                    Brush.linearGradient(listOf(ElectricIndigo, VibrantMagenta))
                                else
                                    Brush.linearGradient(listOf(SurfaceHigh, SurfaceHigh))
                            )
                            .clickable(enabled = customAmount.isNotBlank() && (customAmount.toIntOrNull() ?: 0) > 0) {
                                customAmount.toIntOrNull()?.let { if (it > 0) onConfirm(it) }
                            }
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "Add",
                            color = if (customAmount.isNotBlank() && (customAmount.toIntOrNull() ?: 0) > 0)
                                TextOnSurface else TextSubtle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
