package com.openhealth.openhealth.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.openhealth.openhealth.MainActivity
import com.openhealth.openhealth.R

class HealthWidgetReceiver : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        private const val PREFS_NAME = "openhealth_widget"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val widgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, HealthWidgetReceiver::class.java)
            )
            for (id in widgetIds) {
                updateWidget(context, appWidgetManager, id)
            }
        }

        private fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val isAvailable = prefs.getBoolean("available", false)

            val views = RemoteViews(context.packageName, R.layout.widget_layout)

            if (isAvailable) {
                val steps = prefs.getLong("steps", 0)
                val hr = prefs.getInt("heart_rate", 0)
                val sleepH = prefs.getInt("sleep_hours", 0)
                val sleepM = prefs.getInt("sleep_minutes", 0)

                views.setTextViewText(R.id.widget_steps_value,
                    if (steps >= 10000) "${steps / 1000}k" else steps.toString()
                )
                views.setTextViewText(R.id.widget_hr_value,
                    if (hr > 0) hr.toString() else "--"
                )
                views.setTextViewText(R.id.widget_sleep_value,
                    if (sleepH > 0 || sleepM > 0) "${sleepH}h${sleepM}m" else "--"
                )
            }

            // Tap opens app
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_steps_value, pendingIntent)
            views.setOnClickPendingIntent(R.id.widget_hr_value, pendingIntent)
            views.setOnClickPendingIntent(R.id.widget_sleep_value, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun saveWidgetData(
            context: Context,
            steps: Long,
            stepsGoal: Long,
            heartRate: Int,
            sleepHours: Int,
            sleepMinutes: Int
        ) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().apply {
                putLong("steps", steps)
                putLong("steps_goal", stepsGoal)
                putInt("heart_rate", heartRate)
                putInt("sleep_hours", sleepHours)
                putInt("sleep_minutes", sleepMinutes)
                putBoolean("available", true)
                apply()
            }
        }
    }
}
