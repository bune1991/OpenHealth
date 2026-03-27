package com.openhealth.openhealth.screens

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openhealth.openhealth.ui.theme.*
import kotlinx.coroutines.launch

// ═══════════════════════════════════════════════════════════
// Onboarding — Electric Nocturne Design
// 4 pages: Welcome, Privacy, Permissions, Get Started
// ═══════════════════════════════════════════════════════════

private data class OnboardingPage(
    val icon: ImageVector,
    val iconColor: Color,
    val title: String,
    val highlight: String,
    val description: String,
    val buttonText: String
)

private val pages = listOf(
    OnboardingPage(
        icon = Icons.Default.Favorite,
        iconColor = VibrantMagenta,
        title = "Ignite Your",
        highlight = "Potential.",
        description = "Experience a new rhythm of health tracking with AI-driven nocturnal insights.",
        buttonText = "Get Started"
    ),
    OnboardingPage(
        icon = Icons.Default.Lock,
        iconColor = ElectricIndigo,
        title = "Your Data,",
        highlight = "Secured.",
        description = "We prioritize your privacy. All biometric data is encrypted and used only to provide your personalized health insights.",
        buttonText = "I Agree"
    ),
    OnboardingPage(
        icon = Icons.Default.PhoneAndroid,
        iconColor = SuccessGreen,
        title = "Connect Your",
        highlight = "Pulse.",
        description = "Enable Health Connect and notifications to receive real-time updates on your vital rhythms.",
        buttonText = "Allow Access"
    ),
    OnboardingPage(
        icon = Icons.Default.AutoAwesome,
        iconColor = SoftLavender,
        title = "Enter the",
        highlight = "Resonance.",
        description = "Your personalized dashboard is ready. Let's begin your journey to peak vitality.",
        buttonText = "Enter Dashboard"
    )
)

@Composable
fun OnboardingScreen(
    onGetStarted: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceLowest)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Top bar — Logo + Skip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = ElectricIndigo,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "OpenHealth",
                        color = ElectricIndigo,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (pagerState.currentPage < pages.size - 1) {
                    TextButton(onClick = onGetStarted) {
                        Text(
                            text = "Skip",
                            color = TextOnSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                val pageData = pages[page]
                OnboardingPageContent(pageData = pageData)
            }

            // Page indicator dots
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pages.size) { index ->
                    val isActive = index == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(
                                width = if (isActive) 24.dp else 8.dp,
                                height = 8.dp
                            )
                            .clip(CircleShape)
                            .background(
                                if (isActive) Brush.horizontalGradient(
                                    listOf(ElectricIndigo, VibrantMagenta)
                                )
                                else Brush.horizontalGradient(
                                    listOf(SurfaceHighest, SurfaceHighest)
                                )
                            )
                    )
                }
            }

            // Action button
            val currentPage = pages[pagerState.currentPage]
            val isLastPage = pagerState.currentPage == pages.size - 1

            Button(
                onClick = {
                    if (isLastPage) {
                        onGetStarted()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(ElectricIndigo, VibrantMagenta)
                            ),
                            RoundedCornerShape(28.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentPage.buttonText,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun OnboardingPageContent(pageData: OnboardingPage) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Glowing icon circle
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(160.dp)
        ) {
            // Outer glow ring
            Canvas(modifier = Modifier.size(160.dp)) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            pageData.iconColor.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    ),
                    radius = size.minDimension / 2
                )
                drawCircle(
                    color = pageData.iconColor.copy(alpha = 0.3f),
                    radius = size.minDimension / 2 - 8.dp.toPx(),
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // Inner circle with icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        pageData.iconColor.copy(alpha = 0.15f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = pageData.icon,
                    contentDescription = null,
                    tint = pageData.iconColor,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Title with highlighted word
        Text(
            text = pageData.title,
            color = TextOnSurface,
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            letterSpacing = (-0.5).sp,
            lineHeight = 42.sp
        )
        Text(
            text = pageData.highlight,
            color = ElectricIndigo,
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            letterSpacing = (-0.5).sp,
            lineHeight = 42.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Description
        Text(
            text = pageData.description,
            color = TextOnSurfaceVariant,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}
