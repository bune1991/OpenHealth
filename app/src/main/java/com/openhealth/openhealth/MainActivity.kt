package com.openhealth.openhealth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.openhealth.openhealth.screens.DashboardScreen
import com.openhealth.openhealth.screens.HydrationScreen
import com.openhealth.openhealth.screens.MetricDetailScreen
import com.openhealth.openhealth.screens.ReadinessDetailScreen
import com.openhealth.openhealth.screens.ReportsScreen
import com.openhealth.openhealth.screens.StressDetailScreen
import com.openhealth.openhealth.screens.AiInsightsScreen
import com.openhealth.openhealth.screens.OnboardingScreen
import com.openhealth.openhealth.viewmodel.ReportsData
import com.openhealth.openhealth.screens.SettingsScreen
import com.openhealth.openhealth.screens.PerformanceScreen
import com.openhealth.openhealth.screens.WorkoutDetailScreen
import com.openhealth.openhealth.ui.theme.*
import com.openhealth.openhealth.viewmodel.HealthViewModel

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: HealthViewModel

    // Permission launcher for Health Connect
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            Log.d("MainActivity", "All permissions granted")
            viewModel.onPermissionsGranted()
        } else {
            Log.w("MainActivity", "Some permissions denied")
            viewModel.onPermissionsDenied()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Schedule periodic widget updates and daily summary notification
        com.openhealth.openhealth.widget.WidgetUpdateWorker.enqueue(this)
        com.openhealth.openhealth.widget.DailySummaryWorker.enqueue(this)

        // Back press handling is done in setContent via LaunchedEffect

        setContent {
            viewModel = viewModel()
            val settings by viewModel.settings.collectAsState()

            // Re-enable back handler whenever a sub-screen is showing
            val hasSubScreen = viewModel.selectedMetric.collectAsState().value != null ||
                viewModel.showSettings.collectAsState().value ||
                viewModel.showReadinessDetail.collectAsState().value ||
                viewModel.showReports.collectAsState().value ||
                viewModel.showStressDetail.collectAsState().value ||
                viewModel.showAiInsights.collectAsState().value ||
                viewModel.showHydration.collectAsState().value ||
                viewModel.showPerformance.collectAsState().value ||
                viewModel.showWorkoutDetail.collectAsState().value
            androidx.compose.runtime.LaunchedEffect(hasSubScreen) {
                onBackPressedDispatcher.addCallback(this@MainActivity) {
                    if (::viewModel.isInitialized) {
                        when {
                            viewModel.showWorkoutDetail.value -> viewModel.hideWorkoutDetail()
                            viewModel.showPerformance.value -> viewModel.hidePerformance()
                            viewModel.showHydration.value -> viewModel.hideHydration()
                            viewModel.showSettings.value -> viewModel.hideSettings()
                            viewModel.showAiInsights.value -> viewModel.hideAiInsights()
                            viewModel.showStressDetail.value -> viewModel.hideStressDetail()
                            viewModel.showReports.value -> viewModel.hideReports()
                            viewModel.showReadinessDetail.value -> viewModel.hideReadinessDetail()
                            viewModel.selectedMetric.value != null -> viewModel.clearSelectedMetric()
                            else -> {
                                isEnabled = false
                                onBackPressedDispatcher.onBackPressed()
                            }
                        }
                    } else {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }

            OpenHealthTheme(themeName = settings.themeName) {

                val uiState by viewModel.uiState.collectAsState()
                val healthData by viewModel.healthData.collectAsState()
                val isLoading by viewModel.isLoading.collectAsState()
                val selectedMetric by viewModel.selectedMetric.collectAsState()
                val metricHistory by viewModel.metricHistory.collectAsState()
                val isMetricDetailLoading by viewModel.isMetricDetailLoading.collectAsState()
                val showSettings by viewModel.showSettings.collectAsState()
                val showReadinessDetail by viewModel.showReadinessDetail.collectAsState()
                val showReports by viewModel.showReports.collectAsState()
                val showStressDetail by viewModel.showStressDetail.collectAsState()
                val showAiInsights by viewModel.showAiInsights.collectAsState()
                val showHydration by viewModel.showHydration.collectAsState()
                val showPerformance by viewModel.showPerformance.collectAsState()
                val showWorkoutDetail by viewModel.showWorkoutDetail.collectAsState()
                val selectedWorkoutSession by viewModel.selectedWorkoutSession.collectAsState()
                val hydrationEntries by viewModel.hydrationEntries.collectAsState()
                val hydrationDailyTotal by viewModel.hydrationDailyTotal.collectAsState()
                val aiInsightText by viewModel.aiInsightText.collectAsState()
                val aiInsightLoading by viewModel.aiInsightLoading.collectAsState()
                val aiInsightError by viewModel.aiInsightError.collectAsState()
                val reportsData by viewModel.reportsData.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = SurfaceLowest
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(SurfaceLowest)
                            .padding(paddingValues)
                    ) {
                        when (uiState) {
                            is HealthViewModel.UiState.HealthConnectNotAvailable -> {
                                HealthConnectNotAvailableScreen(
                                    onInstallClick = { openHealthConnectPlayStore() }
                                )
                            }
                            is HealthViewModel.UiState.PermissionsRequired -> {
                                PermissionsRequiredScreen(
                                    onGrantPermissionsClick = { requestHealthConnectPermissions() }
                                )
                            }
                            is HealthViewModel.UiState.Ready -> {
                                when {
                                    !settings.onboardingCompleted -> {
                                        OnboardingScreen(
                                            onGetStarted = { viewModel.completeOnboarding() }
                                        )
                                    }
                                    showWorkoutDetail && selectedWorkoutSession != null -> {
                                        SlideInScreen {
                                            WorkoutDetailScreen(
                                                session = selectedWorkoutSession!!,
                                                healthData = healthData,
                                                onBackClick = { viewModel.hideWorkoutDetail() }
                                            )
                                        }
                                    }
                                    showPerformance -> {
                                        SlideInScreen {
                                            PerformanceScreen(
                                                healthData = healthData,
                                                settings = settings,
                                                onBackClick = { viewModel.hidePerformance() },
                                                onMetricClick = { metricType ->
                                                    viewModel.hidePerformance()
                                                    viewModel.selectMetric(metricType)
                                                },
                                                onSessionClick = { viewModel.showWorkoutDetail(it) }
                                            )
                                        }
                                    }
                                    showHydration -> {
                                        SlideInScreen {
                                            HydrationScreen(
                                                hydrationEntries = hydrationEntries,
                                                dailyTotal = hydrationDailyTotal,
                                                goal = settings.hydrationGoalMl,
                                                onAddWater = { viewModel.addWaterEntry(it) },
                                                onRemoveEntry = { viewModel.removeWaterEntry(it) },
                                                onClearAll = { viewModel.clearAllWaterEntries() },
                                                onBackClick = { viewModel.hideHydration() }
                                            )
                                        }
                                    }
                                    showSettings -> {
                                        SlideInScreen {
                                            // Show settings screen
                                            SettingsScreen(
                                                settings = settings,
                                                onSettingsChanged = { viewModel.updateSettings(it) },
                                                onBackClick = { viewModel.hideSettings() },
                                                onExportClick = {
                                                    val file = com.openhealth.openhealth.utils.DataExporter.exportToCsv(this@MainActivity, healthData)
                                                    file?.let { com.openhealth.openhealth.utils.DataExporter.shareFile(this@MainActivity, it) }
                                                }
                                            )
                                        }
                                    }
                                    showAiInsights -> {
                                        SlideInScreen {
                                            // Calculate readiness for AI screen
                                            val hrv = healthData.heartRateVariability.rmssdMs ?: 30.0
                                            val sleepH = healthData.sleep.totalDuration?.toMinutes()?.div(60.0) ?: 0.0
                                            val rhr = healthData.restingHeartRate.bpm ?: 70
                                            val hrvS = ((hrv - 20.0) / 60.0 * 100.0).coerceIn(0.0, 100.0) * 0.40
                                            val sleepS = (if (sleepH >= 8) 100.0 else if (sleepH >= 7) 85.0 else if (sleepH >= 6) 65.0 else if (sleepH >= 5) 45.0 else 20.0) * 0.25
                                            val rhrS = (if (rhr <= 55) 90.0 else if (rhr <= 60) 80.0 else if (rhr <= 65) 70.0 else if (rhr <= 70) 55.0 else 30.0) * 0.10
                                            val aiReadiness = (hrvS + sleepS + rhrS + 50.0 * 0.25).toInt().coerceIn(5, 100)
                                            AiInsightsScreen(
                                                insightText = aiInsightText,
                                                isLoading = aiInsightLoading,
                                                error = aiInsightError,
                                                providerName = settings.aiProvider.name,
                                                readinessScore = aiReadiness,
                                                onRefreshClick = { viewModel.refreshAiInsight() },
                                                onBackClick = { viewModel.hideAiInsights() }
                                            )
                                        }
                                    }
                                    showStressDetail -> {
                                        SlideInScreen {
                                            StressDetailScreen(
                                                healthData = healthData,
                                                onBackClick = { viewModel.hideStressDetail() },
                                                onStartExercise = {
                                                    viewModel.hideStressDetail()
                                                    viewModel.selectMetric(HealthViewModel.MetricType.EXERCISE)
                                                }
                                            )
                                        }
                                    }
                                    showReports -> {
                                        SlideInScreen {
                                            ReportsScreen(
                                                reportsData = reportsData,
                                                onBackClick = { viewModel.hideReports() },
                                                onMetricClick = { metricType ->
                                                    viewModel.hideReports()
                                                    viewModel.selectMetric(metricType)
                                                }
                                            )
                                        }
                                    }
                                    selectedMetric != null -> {
                                        SlideInScreen {
                                            // Show detail screen
                                            MetricDetailScreen(
                                                metricType = selectedMetric!!,
                                                metricHistory = metricHistory,
                                                isLoading = isMetricDetailLoading,
                                                onBackClick = { viewModel.clearSelectedMetric() },
                                                onHomeClick = { viewModel.clearSelectedMetric() },
                                                onDateChange = { _ -> },
                                                stepsGoal = settings.stepsGoal,
                                                weightTargetKg = settings.weightTargetKg,
                                                exerciseSessions = healthData.exercise.sessions,
                                                healthData = healthData,
                                                onSessionClick = { viewModel.showWorkoutDetail(it) },
                                                onMetricNavigate = { viewModel.selectMetric(it) }
                                            )
                                        }
                                    }
                                    showReadinessDetail -> {
                                        SlideInScreen {
                                            ReadinessDetailScreen(
                                                healthData = healthData,
                                                onBackClick = { viewModel.hideReadinessDetail() },
                                                onMetricClick = { metricType ->
                                                    viewModel.hideReadinessDetail()
                                                    viewModel.selectMetricFromReadiness(metricType)
                                                },
                                                onStartSession = {
                                                    viewModel.hideReadinessDetail()
                                                    viewModel.selectMetricFromReadiness(HealthViewModel.MetricType.EXERCISE)
                                                }
                                            )
                                        }
                                    }
                                    else -> {
                                        // Show dashboard with restored scroll position
                                        val (scrollIndex, scrollOffset) = viewModel.getScrollPosition()
                                        val selectedDate by viewModel.selectedDate.collectAsState()
                                        val weatherData by viewModel.weatherData.collectAsState()
                                        val stepsCalendarData by viewModel.stepsCalendarData.collectAsState()
                                        val stepsStreak by viewModel.stepsStreak.collectAsState()
                                        val bodyExpanded by viewModel.bodyExpanded.collectAsState()
                                        val vitalsExpanded by viewModel.vitalsExpanded.collectAsState()
                                        DashboardScreen(
                                            healthData = healthData,
                                            isLoading = isLoading,
                                            settings = settings,
                                            selectedDate = selectedDate,
                                            onRefresh = { viewModel.refreshData() },
                                            onMetricClick = { metricType ->
                                                // The scroll position is already being saved via onScrollPositionChanged callback
                                                // Just navigate to the detail screen
                                                viewModel.selectMetric(metricType)
                                            },
                                            onSettingsClick = { viewModel.showSettings() },
                                            onShareClick = {
                                                val hrv = healthData.heartRateVariability.rmssdMs ?: 30.0
                                                val sleepH = healthData.sleep.totalDuration?.toMinutes()?.div(60.0) ?: 0.0
                                                val rhr = healthData.restingHeartRate.bpm ?: 70
                                                val hrvS = ((hrv - 20.0) / 60.0 * 100.0).coerceIn(0.0, 100.0) * 0.40
                                                val sleepS = (if (sleepH >= 8) 100.0 else if (sleepH >= 7) 85.0 else if (sleepH >= 6) 65.0 else if (sleepH >= 5) 45.0 else 20.0) * 0.25
                                                val rhrS = (if (rhr <= 55) 90.0 else if (rhr <= 60) 80.0 else if (rhr <= 65) 70.0 else if (rhr <= 70) 55.0 else 30.0) * 0.10
                                                val readiness = (hrvS + sleepS + rhrS + 50.0 * 0.25).toInt().coerceIn(5, 100)
                                                val readinessLabel = when {
                                                    readiness >= 80 -> "Peak"
                                                    readiness >= 60 -> "Good"
                                                    readiness >= 40 -> "Fair"
                                                    else -> "Low"
                                                }
                                                val shareText = buildString {
                                                    appendLine("OpenHealth Daily Report")
                                                    appendLine("Readiness: $readiness/100 ($readinessLabel)")
                                                    appendLine("Steps: ${java.text.NumberFormat.getNumberInstance().format(healthData.steps.count)} / ${java.text.NumberFormat.getNumberInstance().format(settings.stepsGoal)}")
                                                    healthData.heartRate.currentBpm?.let { appendLine("Heart Rate: $it bpm") }
                                                    healthData.heartRateVariability.rmssdMs?.let { appendLine("HRV: ${it.toInt()} ms") }
                                                    if (healthData.sleep.hours > 0 || healthData.sleep.minutes > 0) {
                                                        appendLine("Sleep: ${healthData.sleep.hours}h ${healthData.sleep.minutes}m")
                                                    }
                                                    appendLine("#OpenHealth")
                                                }
                                                val intent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "text/plain"
                                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                                }
                                                startActivity(Intent.createChooser(intent, "Share Health Stats"))
                                            },
                                        onReadinessClick = { viewModel.showReadinessDetail() },
                                            onPreviousDay = { viewModel.navigateToPreviousDay() },
                                            onNextDay = { viewModel.navigateToNextDay() },
                                            onToday = { viewModel.navigateToToday() },
                                            onDateSelected = { date -> viewModel.navigateToDate(date) },
                                            onReportsClick = { viewModel.showReports() },
                                            onStressClick = { viewModel.showStressDetail() },
                                            onAiInsightsClick = { viewModel.showAiInsights() },
                                            onHydrationClick = { viewModel.showHydration() },
                                            onPerformanceClick = { viewModel.showPerformance() },
                                            selectedTab = viewModel.selectedTab.collectAsState().value,
                                            onTabChanged = { viewModel.setSelectedTab(it) },
                                            hydrationDailyTotalMl = hydrationDailyTotal,
                                            onSessionClick = { viewModel.showWorkoutDetail(it) },
                                            weatherData = weatherData,
                                            stepsCalendarData = stepsCalendarData,
                                            stepsStreak = stepsStreak,
                                            bodyExpanded = bodyExpanded,
                                            vitalsExpanded = vitalsExpanded,
                                            onBodyExpandedChange = { viewModel.setBodyExpanded(it) },
                                            onVitalsExpandedChange = { viewModel.setVitalsExpanded(it) },
                                            initialScrollIndex = scrollIndex,
                                            initialScrollOffset = scrollOffset,
                                            onScrollPositionChanged = { index, offset ->
                                                viewModel.saveDashboardScrollPosition(index, offset)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun requestHealthConnectPermissions() {
        val permissions = viewModel.requiredPermissions.map { it.toString() }.toTypedArray()
        requestPermissionLauncher.launch(permissions)
    }

    private fun openHealthConnectPlayStore() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("market://details?id=com.google.android.apps.healthdata")
        }
        startActivity(intent)
    }
}

@Composable
fun PermissionsRequiredScreen(
    onGrantPermissionsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceLowest)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = SurfaceDark
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Permissions Required",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "OpenHealth needs permission to access your health data from Health Connect. This includes:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PermissionItem("Steps & Distance")
                    PermissionItem("Heart Rate")
                    PermissionItem("Sleep Data")
                    PermissionItem("Exercise Sessions")
                    PermissionItem("Calories & Body Metrics")
                }
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onGrantPermissionsClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        contentColor = BackgroundBlack
                    )
                ) {
                    Text(
                        text = "Grant Permissions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            tint = SuccessGreen,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

@Composable
fun HealthConnectNotAvailableScreen(
    onInstallClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceLowest)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = SurfaceDark
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    tint = ErrorRed,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Health Connect Required",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "OpenHealth requires the Health Connect app to access your health data. Please install it from the Play Store.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onInstallClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        contentColor = BackgroundBlack
                    )
                ) {
                    Text(
                        text = "Install Health Connect",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SlideInScreen(content: @Composable () -> Unit) {
    val c = com.openhealth.openhealth.ui.theme.LocalAppColors.current
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    // Fill background to prevent black gap during slide
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(c.background)
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(150)) + slideInHorizontally { it / 5 }
        ) {
            content()
        }
    }
}
