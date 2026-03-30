package com.openhealth.openhealth.widget

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.openhealth.openhealth.MainActivity
import com.openhealth.openhealth.R
import com.openhealth.openhealth.utils.AiHealthService
import com.openhealth.openhealth.utils.HealthPromptBuilder
import com.openhealth.openhealth.utils.SettingsManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

class WeeklyAiSummaryWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val settingsManager = SettingsManager.getInstance(applicationContext)
        val settings = settingsManager.settings.value
        if (!settings.weeklyAiSummary) return Result.success()
        if (settings.aiProvider == com.openhealth.openhealth.model.AiProvider.NONE) return Result.success()

        // Read cached widget data for weekly summary
        val prefs = applicationContext.getSharedPreferences("openhealth_widget", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("available", false)) return Result.success()

        val steps = prefs.getLong("steps", 0)
        val hr = prefs.getInt("heart_rate", 0)
        val sleepH = prefs.getInt("sleep_hours", 0)
        val sleepM = prefs.getInt("sleep_minutes", 0)

        val weeklyPrompt = HealthPromptBuilder.buildWeeklySummaryPrompt(
            avgSteps = steps,
            avgHr = hr,
            avgSleepHours = sleepH,
            avgSleepMinutes = sleepM
        )

        try {
            val apiKey = when (settings.aiProvider) {
                com.openhealth.openhealth.model.AiProvider.CLAUDE -> settings.aiClaudeKey
                com.openhealth.openhealth.model.AiProvider.GEMINI -> settings.aiGeminiKey
                com.openhealth.openhealth.model.AiProvider.CHATGPT -> settings.aiChatgptKey
                com.openhealth.openhealth.model.AiProvider.CUSTOM -> settings.aiCustomKey
                else -> ""
            }
            if (apiKey.isBlank()) return Result.success()

            val service = AiHealthService()
            val result = service.getInsights(
                provider = settings.aiProvider,
                apiKey = apiKey,
                healthSummaryPrompt = weeklyPrompt,
                customUrl = settings.aiCustomUrl,
                customModel = settings.aiCustomModel
            )

            val response = result.getOrNull() ?: return Result.success()

            // Truncate for notification (max ~300 chars)
            val summary = response.take(300).let { text ->
                if (response.length > 300) "$text..." else text
            }

            showNotification(applicationContext, summary)
        } catch (e: Exception) {
            // Don't fail the worker, just skip this week
        }

        return Result.success()
    }

    private fun showNotification(context: Context, message: String) {
        val channelId = "openhealth_weekly_ai"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Weekly AI Summary",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Weekly AI-powered health analysis"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Weekly Health Report")
            .setContentText(message.take(100))
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1002, notification)
    }

    companion object {
        private const val WORK_NAME = "openhealth_weekly_ai_summary"

        fun enqueue(context: Context) {
            val request = PeriodicWorkRequestBuilder<WeeklyAiSummaryWorker>(
                7, TimeUnit.DAYS
            ).setInitialDelay(
                calculateDelayToSunday8PM(), TimeUnit.MILLISECONDS
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }

        private fun calculateDelayToSunday8PM(): Long {
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                set(Calendar.HOUR_OF_DAY, 20)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            if (target.before(now)) {
                target.add(Calendar.WEEK_OF_YEAR, 1)
            }
            return target.timeInMillis - now.timeInMillis
        }
    }
}
