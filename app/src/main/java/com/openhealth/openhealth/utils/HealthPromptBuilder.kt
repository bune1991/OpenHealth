package com.openhealth.openhealth.utils

import com.openhealth.openhealth.model.HealthData

object HealthPromptBuilder {

    fun buildDailySummaryPrompt(
        data: HealthData,
        hydrationMl: Int = 0,
        weeklySteps: List<Pair<String, Long>> = emptyList(),
        readinessScore: Int = 0,
        stressLevel: Int = 0
    ): String {
        val sb = StringBuilder()
        sb.appendLine("You are an elite personal health coach and sports scientist. Analyze my health data and provide expert-level insights that rival WHOOP and Bevel premium analytics.")
        sb.appendLine()
        sb.appendLine("IMPORTANT CONTEXT:")
        sb.appendLine("- I may have an irregular sleep schedule (night owl). Don't assume morning routines.")
        sb.appendLine("- Be specific with numbers from my data. Don't be generic.")
        sb.appendLine("- Compare today vs my recent trends when possible.")
        sb.appendLine()

        sb.appendLine("=== RECOVERY STATUS ===")
        sb.appendLine("Readiness Score: $readinessScore/100")
        sb.appendLine("Stress Level: $stressLevel/100")
        sb.appendLine()
        sb.appendLine("=== TODAY'S HEALTH DATA ===")

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

        // Blood Pressure
        data.bloodPressure.systolicMmHg?.let {
            sb.appendLine("Blood Pressure: ${String.format("%.0f", it)}/${String.format("%.0f", data.bloodPressure.diastolicMmHg)} mmHg")
        }

        // Blood Glucose
        data.bloodGlucose.levelMgPerDl?.let {
            sb.appendLine("Blood Glucose: ${String.format("%.0f", it)} mg/dL")
        }

        // Weekly history
        if (weeklySteps.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("=== 7-DAY HISTORY ===")
            weeklySteps.forEach { (date, steps) ->
                sb.appendLine("$date: $steps steps")
            }
        }

        // Hydration
        if (hydrationMl > 0) {
            sb.appendLine()
            sb.appendLine("Hydration today: ${hydrationMl}ml (${String.format("%.1f", hydrationMl / 1000.0)}L)")
        }

        sb.appendLine()
        sb.appendLine("Based on ALL this data, provide a comprehensive analysis with these sections:")
        sb.appendLine()
        sb.appendLine("1. **Overall Recovery Assessment** - How recovered am I? Rate my readiness and explain why. (2-3 sentences)")
        sb.appendLine()
        sb.appendLine("2. **Key Observations** - What stands out in my data today? Compare to my weekly trends. (bullet points)")
        sb.appendLine()
        sb.appendLine("3. **Recovery Prediction** - Based on my current HRV and sleep trends, when will I be at peak recovery? Give a specific time estimate.")
        sb.appendLine()
        sb.appendLine("4. **Workout Recommendation** - Based on my readiness score of $readinessScore, what type of workout should I do TODAY? Be specific (e.g., '30-minute zone 2 run' or 'rest day with light stretching'). Include intensity level.")
        sb.appendLine()
        sb.appendLine("5. **Sleep Coaching** - Based on my recent sleep data, give ONE specific actionable tip to improve my sleep quality.")
        sb.appendLine()
        sb.appendLine("6. **Nutrition Insight** - Based on my calorie intake vs expenditure, am I in surplus or deficit? Give a specific meal suggestion.")
        sb.appendLine()
        sb.appendLine("7. **Hydration Status** - Rate my hydration and suggest adjustments.")
        sb.appendLine()
        sb.appendLine("8. **Focus for Today** - One single priority action that will have the biggest impact on my health today.")
        sb.appendLine()
        sb.appendLine("Keep each section concise. Use specific numbers from my data. Total response under 500 words.")

        return sb.toString()
    }

    fun buildWeeklySummaryPrompt(
        avgSteps: Long = 0,
        avgHr: Int = 0,
        avgSleepHours: Int = 0,
        avgSleepMinutes: Int = 0
    ): String {
        val sb = StringBuilder()
        sb.appendLine("You are an elite health coach. Write a brief weekly health summary for a notification (max 200 words).")
        sb.appendLine()
        sb.appendLine("This week's averages:")
        sb.appendLine("- Steps: $avgSteps/day")
        if (avgHr > 0) sb.appendLine("- Heart Rate: $avgHr bpm")
        if (avgSleepHours > 0 || avgSleepMinutes > 0) sb.appendLine("- Sleep: ${avgSleepHours}h ${avgSleepMinutes}m")
        sb.appendLine()
        sb.appendLine("Provide:")
        sb.appendLine("1. One sentence overall assessment")
        sb.appendLine("2. What went well this week")
        sb.appendLine("3. One focus area for next week")
        sb.appendLine()
        sb.appendLine("Be encouraging but specific. Use the actual numbers.")
        return sb.toString()
    }
}
