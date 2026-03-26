package com.openhealth.openhealth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.openhealth.openhealth.screens.DashboardScreen
import com.openhealth.openhealth.screens.MetricDetailScreen
import com.openhealth.openhealth.screens.ReadinessDetailScreen
import com.openhealth.openhealth.screens.ReportsScreen
import com.openhealth.openhealth.screens.StressDetailScreen
import com.openhealth.openhealth.viewmodel.ReportsData
import com.openhealth.openhealth.screens.SettingsScreen
import com.openhealth.openhealth.ui.theme.BackgroundBlack
import com.openhealth.openhealth.ui.theme.ErrorRed
import com.openhealth.openhealth.ui.theme.OpenHealthTheme
import com.openhealth.openhealth.ui.theme.PrimaryBlue
import com.openhealth.openhealth.ui.theme.SuccessGreen
import com.openhealth.openhealth.ui.theme.SurfaceDark
import com.openhealth.openhealth.ui.theme.TextPrimary
import com.openhealth.openhealth.ui.theme.TextSecondary
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
        super.onCreate(savedInstanceState)

        // Schedule periodic widget updates
        com.openhealth.openhealth.widget.WidgetUpdateWorker.enqueue(this)

        // Handle back press to navigate from detail/settings screen to dashboard
        onBackPressedDispatcher.addCallback(this) {
            if (::viewModel.isInitialized) {
                when {
                    viewModel.showSettings.value -> {
                        viewModel.hideSettings()
                    }
                    viewModel.showReadinessDetail.value -> {
                        viewModel.hideReadinessDetail()
                    }
                    viewModel.showStressDetail.value -> {
                        viewModel.hideStressDetail()
                    }
                    viewModel.showReports.value -> {
                        viewModel.hideReports()
                    }
                    viewModel.selectedMetric.value != null -> {
                        viewModel.clearSelectedMetric()
                    }
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

        setContent {
            OpenHealthTheme {
                viewModel = viewModel()

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
                val reportsData by viewModel.reportsData.collectAsState()
                val settings by viewModel.settings.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = BackgroundBlack
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(BackgroundBlack)
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
                                    showSettings -> {
                                        // Show settings screen
                                        SettingsScreen(
                                            settings = settings,
                                            onSettingsChanged = { viewModel.updateSettings(it) },
                                            onBackClick = { viewModel.hideSettings() }
                                        )
                                    }
                                    showStressDetail -> {
                                        StressDetailScreen(
                                            healthData = healthData,
                                            onBackClick = { viewModel.hideStressDetail() }
                                        )
                                    }
                                    showReports -> {
                                        ReportsScreen(
                                            reportsData = reportsData,
                                            onBackClick = { viewModel.hideReports() }
                                        )
                                    }
                                    showReadinessDetail -> {
                                        // Show readiness detail screen
                                        ReadinessDetailScreen(
                                            healthData = healthData,
                                            onBackClick = { viewModel.hideReadinessDetail() }
                                        )
                                    }
                                    selectedMetric != null -> {
                                        // Show detail screen
                                        MetricDetailScreen(
                                            metricType = selectedMetric!!,
                                            metricHistory = metricHistory,
                                            isLoading = isMetricDetailLoading,
                                            onBackClick = { viewModel.clearSelectedMetric() },
                                            onHomeClick = { viewModel.clearSelectedMetric() },
                                            onDateChange = { _ -> },
                                            stepsGoal = settings.stepsGoal
                                        )
                                    }
                                    else -> {
                                        // Show dashboard with restored scroll position
                                        val (scrollIndex, scrollOffset) = viewModel.getScrollPosition()
                                        val selectedDate by viewModel.selectedDate.collectAsState()
                                        val stepsCalendarData by viewModel.stepsCalendarData.collectAsState()
                                        val stepsStreak by viewModel.stepsStreak.collectAsState()
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
                                        onReadinessClick = { viewModel.showReadinessDetail() },
                                            onPreviousDay = { viewModel.navigateToPreviousDay() },
                                            onNextDay = { viewModel.navigateToNextDay() },
                                            onToday = { viewModel.navigateToToday() },
                                            onDateSelected = { date -> viewModel.navigateToDate(date) },
                                            onReportsClick = { viewModel.showReports() },
                                            onStressClick = { viewModel.showStressDetail() },
                                            stepsCalendarData = stepsCalendarData,
                                            stepsStreak = stepsStreak,
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
            .background(BackgroundBlack)
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
            .background(BackgroundBlack)
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
