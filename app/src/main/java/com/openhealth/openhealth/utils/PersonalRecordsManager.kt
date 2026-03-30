package com.openhealth.openhealth.utils

import android.content.Context
import android.content.SharedPreferences
import com.openhealth.openhealth.model.HealthData

data class PersonalRecord(
    val label: String,
    val value: String,
    val date: String,
    val rawValue: Double
)

class PersonalRecordsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("openhealth_records", Context.MODE_PRIVATE)

    fun checkAndUpdate(healthData: HealthData): List<String> {
        val newRecords = mutableListOf<String>()

        // Best HRV
        healthData.heartRateVariability.rmssdMs?.let { hrv ->
            if (hrv > prefs.getFloat("best_hrv", 0f)) {
                prefs.edit().putFloat("best_hrv", hrv.toFloat()).putString("best_hrv_date", todayStr()).apply()
                newRecords.add("New HRV record: ${hrv.toInt()} ms!")
            }
        }

        // Most Steps
        val steps = healthData.steps.count
        if (steps > prefs.getLong("most_steps", 0)) {
            prefs.edit().putLong("most_steps", steps).putString("most_steps_date", todayStr()).apply()
            if (steps > 100) newRecords.add("New steps record: ${steps}!")
        }

        // Longest Sleep
        healthData.sleep.totalDuration?.toMinutes()?.let { minutes ->
            if (minutes > prefs.getLong("longest_sleep", 0)) {
                prefs.edit().putLong("longest_sleep", minutes).putString("longest_sleep_date", todayStr()).apply()
                if (minutes > 60) newRecords.add("New sleep record: ${minutes / 60}h ${minutes % 60}m!")
            }
        }

        // Lowest RHR (lower is better)
        val rhr = healthData.restingHeartRate.bpm ?: healthData.heartRate.restingBpm
        if (rhr != null && rhr > 0) {
            val currentBest = prefs.getInt("lowest_rhr", Int.MAX_VALUE)
            if (rhr < currentBest) {
                prefs.edit().putInt("lowest_rhr", rhr).putString("lowest_rhr_date", todayStr()).apply()
                newRecords.add("New resting HR record: $rhr bpm!")
            }
        }

        // Most Calories
        val cals = healthData.calories.totalBurned.toInt()
        if (cals > prefs.getInt("most_calories", 0)) {
            prefs.edit().putInt("most_calories", cals).putString("most_calories_date", todayStr()).apply()
            if (cals > 100) newRecords.add("New calorie burn record: $cals kcal!")
        }

        // Longest Exercise
        healthData.exercise.sessions.maxByOrNull { it.duration }?.let { session ->
            val minutes = session.duration.toMinutes()
            if (minutes > prefs.getLong("longest_exercise", 0)) {
                prefs.edit().putLong("longest_exercise", minutes).putString("longest_exercise_date", todayStr()).apply()
                if (minutes > 5) newRecords.add("New exercise record: ${minutes}min ${session.exerciseType}!")
            }
        }

        return newRecords
    }

    fun getRecords(): List<PersonalRecord> {
        val records = mutableListOf<PersonalRecord>()

        val bestHrv = prefs.getFloat("best_hrv", 0f)
        if (bestHrv > 0) records.add(PersonalRecord("Best HRV", "${bestHrv.toInt()} ms", prefs.getString("best_hrv_date", "") ?: "", bestHrv.toDouble()))

        val mostSteps = prefs.getLong("most_steps", 0)
        if (mostSteps > 0) records.add(PersonalRecord("Most Steps", "${mostSteps}", prefs.getString("most_steps_date", "") ?: "", mostSteps.toDouble()))

        val longestSleep = prefs.getLong("longest_sleep", 0)
        if (longestSleep > 0) records.add(PersonalRecord("Longest Sleep", "${longestSleep / 60}h ${longestSleep % 60}m", prefs.getString("longest_sleep_date", "") ?: "", longestSleep.toDouble()))

        val lowestRhr = prefs.getInt("lowest_rhr", Int.MAX_VALUE)
        if (lowestRhr < Int.MAX_VALUE) records.add(PersonalRecord("Lowest RHR", "$lowestRhr bpm", prefs.getString("lowest_rhr_date", "") ?: "", lowestRhr.toDouble()))

        val mostCals = prefs.getInt("most_calories", 0)
        if (mostCals > 0) records.add(PersonalRecord("Most Calories", "$mostCals kcal", prefs.getString("most_calories_date", "") ?: "", mostCals.toDouble()))

        val longestExercise = prefs.getLong("longest_exercise", 0)
        if (longestExercise > 0) records.add(PersonalRecord("Longest Workout", "${longestExercise}min", prefs.getString("longest_exercise_date", "") ?: "", longestExercise.toDouble()))

        return records
    }

    private fun todayStr(): String {
        val cal = java.util.Calendar.getInstance()
        return "${cal.get(java.util.Calendar.MONTH) + 1}/${cal.get(java.util.Calendar.DAY_OF_MONTH)}/${cal.get(java.util.Calendar.YEAR)}"
    }
}
