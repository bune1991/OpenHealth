package com.openhealth.openhealth.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import com.openhealth.openhealth.model.HealthData
import java.io.File
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DataExporter {

    fun exportToCsv(context: Context, healthData: HealthData): File? {
        return try {
            val today = LocalDate.now(ZoneId.systemDefault())
            val filename = "openhealth_${today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}.csv"

            val file = File(context.cacheDir, filename)
            file.writeText(buildCsvContent(healthData, today))

            Log.d("DataExporter", "CSV exported: ${file.absolutePath}")
            file
        } catch (e: Exception) {
            Log.e("DataExporter", "Export failed: ${e.message}", e)
            null
        }
    }

    fun shareFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "OpenHealth Data Export")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(Intent.createChooser(intent, "Share Health Data").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    private fun buildCsvContent(data: HealthData, date: LocalDate): String {
        val sb = StringBuilder()

        sb.appendLine("OpenHealth Daily Report - $date")
        sb.appendLine()
        sb.appendLine("Metric,Value,Unit")

        // Steps
        sb.appendLine("Steps,${data.steps.count},steps")
        sb.appendLine("Steps Goal,${data.steps.goal},steps")

        // Heart Rate
        data.heartRate.currentBpm?.let { sb.appendLine("Heart Rate,$it,bpm") }
        data.heartRate.minBpm?.let { sb.appendLine("Heart Rate Min,$it,bpm") }
        data.heartRate.maxBpm?.let { sb.appendLine("Heart Rate Max,$it,bpm") }
        data.restingHeartRate.bpm?.let { sb.appendLine("Resting Heart Rate,$it,bpm") }

        // HRV
        data.heartRateVariability.rmssdMs?.let { sb.appendLine("HRV (RMSSD),${String.format("%.1f", it)},ms") }
        data.heartRateVariability.avgMs?.let { sb.appendLine("HRV Average,${String.format("%.1f", it)},ms") }

        // Sleep
        data.sleep.totalDuration?.let {
            val hours = it.toMinutes() / 60.0
            sb.appendLine("Sleep,${String.format("%.1f", hours)},hours")
        }

        // Calories
        if (data.calories.totalBurned > 0) {
            sb.appendLine("Total Calories,${String.format("%.0f", data.calories.totalBurned)},kcal")
            sb.appendLine("Active Calories,${String.format("%.0f", data.calories.activeBurned)},kcal")
        }

        // Distance
        if (data.distance.kilometers > 0) sb.appendLine("Distance,${String.format("%.2f", data.distance.kilometers)},km")

        // Floors
        if (data.floors.count > 0) sb.appendLine("Floors,${data.floors.count},floors")

        // Exercise
        if (data.exercise.sessionCount > 0) {
            sb.appendLine("Exercise Sessions,${data.exercise.sessionCount},sessions")
            sb.appendLine("Exercise Duration,${data.exercise.totalDuration?.toMinutes() ?: 0},min")
        }

        // Body Composition
        data.weight.kilograms?.let { sb.appendLine("Weight,${String.format("%.1f", it)},kg") }
        data.bodyFat.percentage?.let { sb.appendLine("Body Fat,${String.format("%.1f", it)},%") }
        data.basalMetabolicRate.caloriesPerDay?.let { sb.appendLine("BMR,${String.format("%.0f", it)},kcal/day") }
        data.bodyWaterMass.kilograms?.let { sb.appendLine("Body Water,${String.format("%.1f", it)},kg") }
        data.boneMass.kilograms?.let { sb.appendLine("Bone Mass,${String.format("%.1f", it)},kg") }
        data.leanBodyMass.kilograms?.let { sb.appendLine("Lean Mass,${String.format("%.1f", it)},kg") }

        // Vitals
        data.oxygenSaturation.percentage?.let { sb.appendLine("SpO2,${String.format("%.0f", it)},%") }
        data.respiratoryRate.ratePerMinute?.let { sb.appendLine("Respiratory Rate,${String.format("%.1f", it)},breaths/min") }
        data.skinTemperature.temperatureCelsius?.let { sb.appendLine("Skin Temperature,${String.format("%.1f", it)},°C") }
        data.bloodPressure.systolicMmHg?.let { sb.appendLine("Blood Pressure Systolic,${String.format("%.0f", it)},mmHg") }
        data.bloodPressure.diastolicMmHg?.let { sb.appendLine("Blood Pressure Diastolic,${String.format("%.0f", it)},mmHg") }
        data.bloodGlucose.levelMgPerDl?.let { sb.appendLine("Blood Glucose,${String.format("%.0f", it)},mg/dL") }
        data.bodyTemperature.temperatureCelsius?.let { sb.appendLine("Body Temperature,${String.format("%.1f", it)},°C") }

        // Nutrition
        data.nutrition.calories?.let { sb.appendLine("Nutrition Calories,${String.format("%.0f", it)},kcal") }
        data.nutrition.proteinGrams?.let { sb.appendLine("Protein,${String.format("%.0f", it)},g") }
        data.nutrition.carbsGrams?.let { sb.appendLine("Carbs,${String.format("%.0f", it)},g") }
        data.nutrition.fatGrams?.let { sb.appendLine("Fat,${String.format("%.0f", it)},g") }

        return sb.toString()
    }
}
