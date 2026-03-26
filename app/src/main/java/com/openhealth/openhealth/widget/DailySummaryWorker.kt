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
import com.openhealth.openhealth.utils.SettingsManager
import java.util.concurrent.TimeUnit

class DailySummaryWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val settings = SettingsManager.getInstance(applicationContext).settings.value
        if (!settings.dailySummaryNotification) return Result.success()

        // Read cached widget data for the summary
        val prefs = applicationContext.getSharedPreferences("openhealth_widget", Context.MODE_PRIVATE)
        val isAvailable = prefs.getBoolean("available", false)
        if (!isAvailable) return Result.success()

        val steps = prefs.getLong("steps", 0)
        val hr = prefs.getInt("heart_rate", 0)
        val sleepH = prefs.getInt("sleep_hours", 0)
        val sleepM = prefs.getInt("sleep_minutes", 0)

        val sleepText = if (sleepH > 0 || sleepM > 0) "${sleepH}h${sleepM}m" else "No data"
        val hrText = if (hr > 0) "$hr bpm" else "No data"

        val message = "Yesterday: $steps steps, $sleepText sleep, HR $hrText"

        showNotification(applicationContext, message)
        return Result.success()
    }

    private fun showNotification(context: Context, message: String) {
        val channelId = "openhealth_daily_summary"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Daily Summary",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Daily health summary notification"
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
            .setContentTitle("Good morning! ☀️")
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }

    companion object {
        private const val WORK_NAME = "openhealth_daily_summary"

        fun enqueue(context: Context) {
            val request = PeriodicWorkRequestBuilder<DailySummaryWorker>(
                24, TimeUnit.HOURS
            ).setInitialDelay(
                calculateInitialDelay(), TimeUnit.MILLISECONDS
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

        private fun calculateInitialDelay(): Long {
            val now = java.util.Calendar.getInstance()
            val target = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, 8)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
            }
            if (target.before(now)) {
                target.add(java.util.Calendar.DAY_OF_YEAR, 1)
            }
            return target.timeInMillis - now.timeInMillis
        }
    }
}
