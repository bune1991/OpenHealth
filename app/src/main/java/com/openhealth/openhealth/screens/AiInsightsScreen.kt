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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.openhealth.openhealth.viewmodel.HealthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiInsightsScreen(
    insightText: String?,
    isLoading: Boolean,
    error: String?,
    providerName: String,
    readinessScore: Int = 0,
    chatMessages: List<HealthViewModel.ChatMessage> = emptyList(),
    chatLoading: Boolean = false,
    chatEnabled: Boolean = true,
    chatBubbleMode: Boolean = false,
    onSendMessage: (String) -> Unit = {},
    onClearChat: () -> Unit = {},
    onRefreshClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var chatInput by remember { mutableStateOf("") }
    val c = LocalAppColors.current
    val scrollState = rememberScrollState()

    // Auto-scroll to bottom only when new chat messages arrive (not on initial load)
    var previousMessageCount by remember { mutableStateOf(chatMessages.size) }
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.size > previousMessageCount) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
        previousMessageCount = chatMessages.size
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = c.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Nocturnal Energy",
                            color = c.primary,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = c.onSurfaceVariant)
                    }
                },
                actions = {
                    if (!isLoading) {
                        IconButton(onClick = onRefreshClick) {
                            Icon(Icons.Default.Refresh, "Refresh", tint = c.onSurfaceVariant)
                        }
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
                .verticalScroll(scrollState),
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
                                        c.primary.copy(alpha = pulseAlpha),
                                        Color.Transparent
                                    )
                                ),
                                radius = size.minDimension / 2 * ringScale
                            )
                            // Ring
                            drawCircle(
                                color = c.primary.copy(alpha = 0.3f),
                                radius = size.minDimension / 3,
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }

                        // Center icon
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(
                                    c.primary.copy(alpha = 0.15f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = c.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    // Title
                    Text(
                        text = "Analyzing Biometric\nData...",
                        style = MaterialTheme.typography.headlineLarge,
                        color = c.onSurface,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Our AI is scanning your recent activity and heart rate patterns to generate your nocturnal performance report.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.onSurfaceVariant,
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
                            .background(c.error.copy(alpha = 0.1f))
                            .padding(20.dp)
                    ) {
                        Column {
                            Text(
                                text = "Unable to get AI insights",
                                color = c.error,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = error,
                                color = c.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 22.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            TextButton(onClick = onRefreshClick) {
                                Text("Try Again", color = c.primary, fontWeight = FontWeight.Bold)
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
                            .background(c.surface)
                            .padding(28.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(c.primary.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = c.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "Set Up AI Insights",
                                color = c.onSurface,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Add your API key in Settings to get personalized AI-powered health analysis.",
                                color = c.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 22.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Supports: Claude, Gemini, ChatGPT",
                                color = c.outline,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // ─── Success State ───
                insightText != null -> {
                    // Hero — check icon with glow
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        // Glow behind
                        Canvas(modifier = Modifier.size(120.dp)) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(c.primary.copy(alpha = 0.15f), Color.Transparent)
                                ),
                                radius = size.minDimension / 2
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(c.primary.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = c.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    // Title
                    Text(
                        text = "Analysis Complete",
                        style = MaterialTheme.typography.headlineLarge,
                        color = c.onSurface,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Your nocturnal recovery data has been processed by $providerName.",
                        style = MaterialTheme.typography.bodySmall,
                        color = c.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Recovery Energy Pill
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(c.surfaceLow)
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.82f)
                                .fillMaxHeight()
                                .background(
                                    Brush.horizontalGradient(listOf(c.primary, c.secondary)),
                                    RoundedCornerShape(24.dp)
                                ),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = "${readinessScore}% RECOVERY",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Main glass card with all sections
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(c.surfaceLow.copy(alpha = 0.6f))
                            .padding(24.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                            // Parse and display insight sections
                            val sections = parseInsightSections(insightText)
                            val isLastRecommendation = { title: String ->
                                title.contains("recommend", ignoreCase = true) ||
                                title.contains("action", ignoreCase = true) ||
                                sections.indexOf(title to "") == sections.size - 1
                            }

                            sections.forEachIndexed { index, section ->
                                if (section.first.contains("recommend", ignoreCase = true) ||
                                    section.first.contains("action", ignoreCase = true)) {
                                    // Recommended Action — special card with left magenta border
                                    RecommendedActionCard(
                                        title = section.first,
                                        content = section.second
                                    )
                                } else {
                                    // Normal section
                                    InsightSectionCard(
                                        title = section.first,
                                        content = section.second,
                                        accentColor = if (index == 0) c.secondary else c.primary
                                    )
                                }
                            }
                        }
                    }

                    // Provider badge — "POWERED BY GEMINI"
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(c.background)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(
                                            Brush.linearGradient(listOf(c.primary, c.secondary)),
                                            CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "POWERED BY ${providerName.uppercase()}",
                                    color = c.onSurfaceVariant,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }

                    // Disclaimer
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "AI-generated analysis. Not medical advice.",
                        color = c.outline,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // ─── Health Chat Section ───
            if (chatEnabled && insightText != null && !isLoading) {
                Spacer(modifier = Modifier.height(24.dp))

                // Section header with clear button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ASK YOUR HEALTH COACH",
                        style = MaterialTheme.typography.labelMedium,
                        color = c.tertiary,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (chatMessages.isNotEmpty()) {
                        IconButton(onClick = onClearChat, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear chat",
                                tint = c.outline,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Chat messages
                chatMessages.forEach { msg ->
                    if (msg.isUser) {
                        // User message — right aligned
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .clip(
                                        if (chatBubbleMode) RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
                                        else RoundedCornerShape(16.dp)
                                    )
                                    .background(c.primary)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = msg.text,
                                    color = c.onPrimary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    } else {
                        // AI response — left aligned
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .clip(
                                    if (chatBubbleMode) RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
                                    else RoundedCornerShape(16.dp)
                                )
                                .background(c.surfaceLow)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = msg.text,
                                color = c.onSurface,
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 22.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Loading indicator
                if (chatLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = c.primary,
                            strokeWidth = 2.dp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Input field + send button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = chatInput,
                        onValueChange = { chatInput = it },
                        placeholder = { Text("Ask about your health...", color = c.outline) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = c.surfaceLow,
                            unfocusedContainerColor = c.surfaceLow,
                            focusedBorderColor = c.primary.copy(alpha = 0.5f),
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = c.onSurface,
                            unfocusedTextColor = c.onSurface,
                            cursorColor = c.primary
                        ),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = c.onSurface)
                    )
                    IconButton(
                        onClick = {
                            if (chatInput.isNotBlank()) {
                                onSendMessage(chatInput)
                                chatInput = ""
                            }
                        },
                        enabled = chatInput.isNotBlank() && !chatLoading
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (chatInput.isNotBlank() && !chatLoading) c.primary else c.outline
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun InsightSectionCard(title: String, content: String, accentColor: Color = LocalAppColors.current.primary) {
    val c = LocalAppColors.current
    Column {
        if (title.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (accentColor == c.secondary) Icons.Default.Favorite else Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = accentColor,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = c.onSurfaceVariant,
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun RecommendedActionCard(title: String, content: String) {
    val c = LocalAppColors.current
    // Card with magenta left border — exact Stitch "Recommended Action"
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(c.surfaceHighest)
    ) {
        // Left magenta border
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(c.secondary)
                .align(Alignment.CenterStart)
        )
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = c.secondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = c.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = c.onSurfaceVariant,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Removed "Log Morning Session" — Health Connect is read-only
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
        val isHeader = trimmed.startsWith("###") || trimmed.startsWith("##") || trimmed.startsWith("# ") ||
            (trimmed.startsWith("**") && trimmed.endsWith("**") && trimmed.length < 80)

        if (isHeader) {
            // Save previous section
            if (currentTitle.isNotEmpty() || currentContent.isNotEmpty()) {
                sections.add(currentTitle to currentContent.toString().trim())
            }
            currentTitle = trimmed
                .removePrefix("### ").removePrefix("## ").removePrefix("# ")
                .replace("**", "")
                .replace(Regex("^\\d+\\.\\s*"), "") // Remove leading "1. ", "2. " etc
                .removeSuffix(":").trim()
            currentContent = StringBuilder()
        } else if (trimmed.isNotEmpty()) {
            if (currentContent.isNotEmpty()) currentContent.append("\n")
            // Strip markdown bold markers from content
            currentContent.append(
                trimmed.removePrefix("- ").removePrefix("* ")
                    .replace("**", "")
            )
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
