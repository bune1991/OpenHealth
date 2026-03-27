package com.openhealth.openhealth.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openhealth.openhealth.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiInsightsScreen(
    insightText: String?,
    isLoading: Boolean,
    error: String?,
    providerName: String,
    onRefreshClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = ElectricIndigo,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Nocturnal Energy",
                            color = ElectricIndigo,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextOnSurfaceVariant)
                    }
                },
                actions = {
                    if (!isLoading) {
                        IconButton(onClick = onRefreshClick) {
                            Icon(Icons.Default.Refresh, "Refresh", tint = TextOnSurfaceVariant)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceLowest)
            )
        },
        containerColor = SurfaceLowest
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when {
                // ─── Loading State ───
                isLoading -> {
                    Spacer(modifier = Modifier.height(40.dp))

                    // Pulsing circle with icon
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                        val pulseAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.1f,
                            targetValue = 0.3f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1500, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulse_alpha"
                        )
                        val ringScale by infiniteTransition.animateFloat(
                            initialValue = 0.8f,
                            targetValue = 1.0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1500, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "ring_scale"
                        )

                        // Outer glow
                        Canvas(modifier = Modifier.size(200.dp)) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        ElectricIndigo.copy(alpha = pulseAlpha),
                                        Color.Transparent
                                    )
                                ),
                                radius = size.minDimension / 2 * ringScale
                            )
                            // Ring
                            drawCircle(
                                color = ElectricIndigo.copy(alpha = 0.3f),
                                radius = size.minDimension / 3,
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }

                        // Center icon
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(
                                    ElectricIndigo.copy(alpha = 0.15f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = ElectricIndigo,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    // Title
                    Text(
                        text = "Analyzing Biometric\nData...",
                        style = MaterialTheme.typography.headlineLarge,
                        color = TextOnSurface,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Our AI is scanning your recent activity and heart rate patterns to generate your nocturnal performance report.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextOnSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }

                // ─── Error State ───
                error != null -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(ErrorRed.copy(alpha = 0.1f))
                            .padding(20.dp)
                    ) {
                        Column {
                            Text(
                                text = "Unable to get AI insights",
                                color = ErrorRed,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = error,
                                color = TextOnSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 22.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            TextButton(onClick = onRefreshClick) {
                                Text("Try Again", color = ElectricIndigo, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // ─── Setup State (no provider) ───
                insightText == null && !isLoading && error == null -> {
                    Spacer(modifier = Modifier.height(40.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(SurfaceMid)
                            .padding(28.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(ElectricIndigo.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = ElectricIndigo,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "Set Up AI Insights",
                                color = TextOnSurface,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Add your API key in Settings to get personalized AI-powered health analysis.",
                                color = TextOnSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 22.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Supports: Claude, Gemini, ChatGPT",
                                color = TextSubtle,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // ─── Success State ───
                insightText != null -> {
                    // Header
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = ElectricIndigo,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Analysis Complete",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextOnSurface,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Your latest biometric data has been processed by $providerName.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextOnSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Provider badge
                        Box(
                            modifier = Modifier
                                .background(
                                    Brush.horizontalGradient(listOf(ElectricIndigo, VibrantMagenta)),
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "AI-POWERED",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Parse and display insight sections
                    val sections = parseInsightSections(insightText)
                    sections.forEach { section ->
                        InsightSectionCard(
                            title = section.first,
                            content = section.second
                        )
                    }

                    // Disclaimer
                    Text(
                        text = "AI-generated analysis. Not medical advice. Data sent to your own $providerName account.",
                        color = TextSubtle,
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun InsightSectionCard(title: String, content: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceMid)
            .padding(20.dp)
    ) {
        Column {
            if (title.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                Brush.horizontalGradient(listOf(ElectricIndigo, VibrantMagenta)),
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextOnSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = TextOnSurfaceVariant,
                lineHeight = 22.sp
            )
        }
    }
}

// Split insight text into sections by headers (lines starting with ** or ##)
private fun parseInsightSections(text: String): List<Pair<String, String>> {
    val lines = text.split("\n")
    val sections = mutableListOf<Pair<String, String>>()
    var currentTitle = ""
    var currentContent = StringBuilder()

    for (line in lines) {
        val trimmed = line.trim()
        val isHeader = trimmed.startsWith("**") || trimmed.startsWith("##") || trimmed.startsWith("# ")

        if (isHeader) {
            // Save previous section
            if (currentTitle.isNotEmpty() || currentContent.isNotEmpty()) {
                sections.add(currentTitle to currentContent.toString().trim())
            }
            currentTitle = trimmed
                .removePrefix("## ").removePrefix("# ")
                .removePrefix("**").removeSuffix("**")
                .removeSuffix(":").trim()
            currentContent = StringBuilder()
        } else if (trimmed.isNotEmpty()) {
            if (currentContent.isNotEmpty()) currentContent.append("\n")
            currentContent.append(trimmed.removePrefix("- ").removePrefix("* "))
        }
    }

    // Last section
    if (currentTitle.isNotEmpty() || currentContent.isNotEmpty()) {
        sections.add(currentTitle to currentContent.toString().trim())
    }

    // If no sections found, treat whole text as one section
    if (sections.isEmpty()) {
        sections.add("" to text)
    }

    return sections
}
