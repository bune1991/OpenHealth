package com.openhealth.openhealth.utils

import com.openhealth.openhealth.model.HealthData

object HealthPromptBuilder {

    fun buildDailySummaryPrompt(data: HealthData, hydrationMl: Int = 0): String {
        val sb = StringBuilder()
        sb.appendLine("You are a personal health analyst. Analyze the following daily health data and provide personalized insights. Be concise, actionable, and encouraging. Use simple language that anyone can understand.")
        sb.appendLine()
        sb.appendLine("IMPORTANT: The user may have an irregular sleep schedule (night owl). Do NOT assume they just woke up or that it's morning. Base your advice on the actual data, not time assumptions.")
        sb.appendLine()

        // Calculate and include readiness/stress
        val hrv = data.heartRateVariability.rmssdMs ?: 0.0
        val stressLevel = ((80.0 - hrv.coerceIn(10.0, 80.0)) / 70.0 * 100).toInt().coerceIn(0, 100)
        val hrvScore = ((hrv - 20.0) / 60.0 * 100.0).coerceIn(0.0, 100.0) * 0.40
        val sleepHours = data.sleep.totalDuration?.toMinutes()?.div(60.0) ?: 0.0
        val sleepScore = (if (sleepHours >= 8) 100.0 else if (sleepHours >= 7) 85.0 else if (sleepHours >= 6) 65.0 else if (sleepHours >= 5) 45.0 else 20.0) * 0.25
        val rhr = data.restingHeartRate.bpm ?: 70
        val rhrScore = (if (rhr <= 55) 90.0 else if (rhr <= 60) 80.0 else if (rhr <= 65) 70.0 else if (rhr <= 70) 55.0 else 30.0) * 0.10
        val readinessScore = (hrvScore + sleepScore + rhrScore + 50.0 * 0.25).toInt().coerceIn(5, 100)

        sb.appendLine("=== Recovery & Stress ===")
        sb.appendLine("Readiness Score: $readinessScore/100")
        sb.appendLine("Stress Level: $stressLevel/100")
        sb.appendLine()
        sb.appendLine("=== Today's Health Data ===")
        sb.appendLine()

        // Steps
        if (data.steps.count > 0) {
            sb.appendLine("Steps: ${data.steps.count} (goal: ${data.steps.goal})")
        }

        // Heart Rate
        data.heartRate.currentBpm?.let {
            sb.appendLine("Heart Rate: $it bpm (min: ${data.heartRate.minBpm}, max: ${data.heartRate.maxBpm})")
        }

        // Resting Heart Rate
        data.restingHeartRate.bpm?.let {
            sb.appendLine("Resting Heart Rate: $it bpm")
        }

        // HRV
        data.heartRateVariability.rmssdMs?.let {
            sb.appendLine("HRV (RMSSD): ${String.format("%.1f", it)} ms (avg today: ${String.format("%.1f", data.heartRateVariability.avgMs ?: it)} ms)")
        }

        // Sleep
        data.sleep.totalDuration?.let {
            val hours = it.toMinutes() / 60
            val mins = it.toMinutes() % 60
            sb.appendLine("Sleep: ${hours}h ${mins}m")
        }

        // Calories
        if (data.calories.totalBurned > 0) {
            sb.appendLine("Calories Burned: ${String.format("%.0f", data.calories.totalBurned)} kcal")
        }

        // Distance
        if (data.distance.kilometers > 0) {
            sb.appendLine("Distance: ${String.format("%.2f", data.distance.kilometers)} km")
        }

        // Exercise
        if (data.exercise.sessionCount > 0) {
            sb.appendLine("Exercise: ${data.exercise.sessionCount} sessions, ${data.exercise.totalDuration?.toMinutes() ?: 0} min")
            data.exercise.sessions.forEach { session ->
                sb.appendLine("  - ${session.exerciseType}: ${session.duration.toMinutes()} min")
            }
        }

        // Weight
        data.weight.kilograms?.let {
            sb.appendLine("Weight: ${String.format("%.1f", it)} kg")
        }

        // Body Fat
        data.bodyFat.percentage?.let {
            sb.appendLine("Body Fat: ${String.format("%.1f", it)}%")
        }

        // BMR
        data.basalMetabolicRate.caloriesPerDay?.let {
            sb.appendLine("BMR: ${String.format("%.0f", it)} kcal/day")
        }

        // SpO2
        data.oxygenSaturation.percentage?.let {
            sb.appendLine("Blood Oxygen (SpO2): ${String.format("%.0f", it)}%")
        }

        // Respiratory Rate
        data.respiratoryRate.ratePerMinute?.let {
            sb.appendLine("Respiratory Rate: ${String.format("%.1f", it)} breaths/min")
        }

        // Skin Temperature
        data.skinTemperature.temperatureCelsius?.let {
            sb.appendLine("Skin Temperature: ${String.format("%.1f", it)}°C variation")
        }

        // Nutrition
        data.nutrition.calories?.let {
            if (it > 0) {
                sb.appendLine("Nutrition: ${String.format("%.0f", it)} kcal (P:${String.format("%.0f", data.nutrition.proteinGrams ?: 0.0)}g C:${String.format("%.0f", data.nutrition.carbsGrams ?: 0.0)}g F:${String.format("%.0f", data.nutrition.fatGrams ?: 0.0)}g)")
            }
        }

        // Hydration
        if (hydrationMl > 0) {
            sb.appendLine("Hydration: ${hydrationMl}ml (${String.format("%.1f", hydrationMl / 1000.0)}L) of 2.5L goal (${(hydrationMl * 100 / 2500)}%)")
        }

        // Blood Pressure
        data.bloodPressure.systolicMmHg?.let {
            sb.appendLine("Blood Pressure: ${String.format("%.0f", it)}/${String.format("%.0f", data.bloodPressure.diastolicMmHg)} mmHg")
        }

        // Blood Glucose
        data.bloodGlucose.levelMgPerDl?.let {
            sb.appendLine("Blood Glucose: ${String.format("%.0f", it)} mg/dL")
        }

        sb.appendLine()
        sb.appendLine("Based on this data, provide:")
        sb.appendLine("1. **Overall Assessment** - How is my health today? (2-3 sentences)")
        sb.appendLine("2. **Key Observations** - What stands out? (bullet points)")
        sb.appendLine("3. **Recommendations** - What should I do? (bullet points)")
        sb.appendLine("4. **Focus for Today** - One specific thing to prioritize")
        sb.appendLine()
        sb.appendLine("Keep your response under 300 words. Be specific with numbers from my data.")

        return sb.toString()
    }
}
