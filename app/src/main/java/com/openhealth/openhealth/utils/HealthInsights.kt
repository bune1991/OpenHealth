package com.openhealth.openhealth.utils

data class MetricInsight(
    val status: String,        // "Normal", "Low", "High", "Excellent"
    val statusColor: String,   // "green", "yellow", "red"
    val description: String,   // What this metric is
    val meaning: String,       // What your value means
    val normalRange: String,   // Normal range
    val tips: List<String>     // How to improve
)

object HealthInsights {

    fun getHeartRateInsight(bpm: Int): MetricInsight {
        val (status, color) = when {
            bpm in 60..100 -> "Normal" to "green"
            bpm in 50..59 -> "Athletic" to "green"
            bpm < 50 -> "Low" to "yellow"
            bpm in 101..120 -> "Elevated" to "yellow"
            else -> "High" to "red"
        }
        return MetricInsight(
            status = status,
            statusColor = color,
            description = "Heart rate measures how many times your heart beats per minute. It reflects your cardiovascular health and current activity level.",
            meaning = when {
                bpm < 50 -> "Your heart rate of $bpm bpm is unusually low. This could be normal for well-trained athletes, but consult a doctor if you feel dizzy or fatigued."
                bpm in 50..59 -> "Your heart rate of $bpm bpm indicates excellent cardiovascular fitness, typical of athletes and active individuals."
                bpm in 60..80 -> "Your heart rate of $bpm bpm is in the healthy range, indicating good cardiovascular function."
                bpm in 81..100 -> "Your heart rate of $bpm bpm is normal but on the higher side. Stress, caffeine, or lack of sleep may be factors."
                else -> "Your heart rate of $bpm bpm is elevated. This could be due to stress, exercise, caffeine, or dehydration."
            },
            normalRange = "60-100 bpm (resting)",
            tips = when {
                bpm > 100 -> listOf("Practice deep breathing exercises", "Reduce caffeine intake", "Stay hydrated", "Check for stress or anxiety")
                bpm > 80 -> listOf("Regular aerobic exercise lowers resting HR over time", "Get 7-9 hours of quality sleep", "Manage stress with meditation")
                else -> listOf("Maintain your exercise routine", "Your cardiovascular health is good", "Continue regular physical activity")
            }
        )
    }

    fun getRestingHeartRateInsight(bpm: Int): MetricInsight {
        val (status, color) = when {
            bpm < 50 -> "Athletic" to "green"
            bpm in 50..60 -> "Excellent" to "green"
            bpm in 61..70 -> "Good" to "green"
            bpm in 71..80 -> "Average" to "yellow"
            else -> "Above Average" to "red"
        }
        return MetricInsight(
            status = status,
            statusColor = color,
            description = "Resting heart rate is your heart rate when you're completely at rest. It's one of the best indicators of cardiovascular fitness. Lower is generally better.",
            meaning = when {
                bpm <= 60 -> "Your resting HR of $bpm bpm shows excellent cardiovascular fitness. Your heart is efficient at pumping blood."
                bpm in 61..70 -> "Your resting HR of $bpm bpm is good. Regular exercise can bring this even lower."
                bpm in 71..80 -> "Your resting HR of $bpm bpm is average. Increasing aerobic exercise can improve it."
                else -> "Your resting HR of $bpm bpm is above average. This may indicate a need for more cardiovascular exercise or better recovery."
            },
            normalRange = "60-100 bpm (athletes: 40-60 bpm)",
            tips = listOf("Zone 2 cardio (easy pace) is the best way to lower resting HR", "Consistent sleep schedule improves heart efficiency", "Reduce alcohol consumption — it raises resting HR for 24+ hours")
        )
    }

    fun getHrvInsight(ms: Double): MetricInsight {
        val (status, color) = when {
            ms >= 50 -> "Good" to "green"
            ms >= 30 -> "Moderate" to "yellow"
            else -> "Low" to "red"
        }
        return MetricInsight(
            status = status,
            statusColor = color,
            description = "Heart Rate Variability (HRV) measures the variation in time between heartbeats. Higher HRV indicates better autonomic nervous system function, stress resilience, and recovery capacity.",
            meaning = when {
                ms >= 50 -> "Your HRV of ${String.format("%.0f", ms)} ms is good. Your body shows strong stress resilience and recovery capacity."
                ms >= 30 -> "Your HRV of ${String.format("%.0f", ms)} ms is moderate. There's room for improvement through better sleep and stress management."
                else -> "Your HRV of ${String.format("%.0f", ms)} ms is low, suggesting your body is under stress or hasn't fully recovered. Prioritize rest today."
            },
            normalRange = "20-70 ms (varies by age, higher is better)",
            tips = listOf("Deep breathing exercises (box breathing) directly improve HRV", "Cold exposure (cold showers) can boost HRV over time", "Avoid alcohol — it drops HRV significantly for 24-48 hours", "Consistent sleep and wake times improve HRV trends")
        )
    }

    fun getSleepInsight(hours: Double): MetricInsight {
        val (status, color) = when {
            hours >= 7 && hours <= 9 -> "Optimal" to "green"
            hours >= 6 -> "Fair" to "yellow"
            hours >= 9 -> "Long" to "yellow"
            else -> "Short" to "red"
        }
        return MetricInsight(
            status = status,
            statusColor = color,
            description = "Sleep is essential for physical recovery, cognitive function, and immune health. Both quantity and quality matter.",
            meaning = when {
                hours < 5 -> "Only ${String.format("%.1f", hours)}h of sleep is critically low. Your cognitive performance, immune system, and mood are significantly impacted."
                hours < 7 -> "${String.format("%.1f", hours)}h of sleep is below the recommended 7-9 hours. You may experience reduced focus and slower recovery."
                hours in 7.0..9.0 -> "${String.format("%.1f", hours)}h of sleep is in the optimal range. Your body has had adequate time for recovery and memory consolidation."
                else -> "${String.format("%.1f", hours)}h of sleep is above average. Occasional long sleep is fine, but consistently oversleeping may indicate underlying issues."
            },
            normalRange = "7-9 hours for adults",
            tips = listOf("Keep a consistent bedtime, even on weekends", "Avoid screens 30 minutes before bed — blue light suppresses melatonin", "Keep your bedroom cool (18-20°C) for optimal sleep quality", "Caffeine has a 6-hour half-life — avoid after 2 PM")
        )
    }

    fun getSpO2Insight(percentage: Double): MetricInsight {
        val (status, color) = when {
            percentage >= 95 -> "Normal" to "green"
            percentage >= 90 -> "Low" to "yellow"
            else -> "Critical" to "red"
        }
        return MetricInsight(
            status = status,
            statusColor = color,
            description = "Blood oxygen saturation (SpO2) measures the percentage of hemoglobin in your blood carrying oxygen. It indicates how well your lungs are transferring oxygen to your bloodstream.",
            meaning = when {
                percentage >= 97 -> "Your SpO2 of ${String.format("%.0f", percentage)}% is excellent. Your blood is carrying optimal levels of oxygen."
                percentage >= 95 -> "Your SpO2 of ${String.format("%.0f", percentage)}% is normal. Your oxygen levels are healthy."
                percentage >= 90 -> "Your SpO2 of ${String.format("%.0f", percentage)}% is below normal. This could indicate respiratory issues. Consult a doctor if persistent."
                else -> "Your SpO2 of ${String.format("%.0f", percentage)}% is critically low. Seek medical attention."
            },
            normalRange = "95-100%",
            tips = listOf("Deep breathing exercises improve oxygen intake", "Regular cardio exercise strengthens lung capacity", "Avoid smoking — it directly reduces blood oxygen", "Sleep on your side rather than your back for better nighttime SpO2")
        )
    }

    fun getRespiratoryRateInsight(rate: Double): MetricInsight {
        val (status, color) = when {
            rate in 12.0..20.0 -> "Normal" to "green"
            rate in 8.0..25.0 -> "Borderline" to "yellow"
            else -> "Abnormal" to "red"
        }
        return MetricInsight(
            status = status,
            statusColor = color,
            description = "Respiratory rate is the number of breaths you take per minute. It reflects your lung function and can indicate stress, illness, or fitness level.",
            meaning = when {
                rate in 12.0..20.0 -> "Your respiratory rate of ${String.format("%.0f", rate)} breaths/min is normal, indicating healthy lung function."
                rate < 12 -> "Your respiratory rate of ${String.format("%.0f", rate)} breaths/min is low. This is common during deep sleep or in very fit individuals."
                else -> "Your respiratory rate of ${String.format("%.0f", rate)} breaths/min is elevated. Stress, illness, or anxiety may be factors."
            },
            normalRange = "12-20 breaths/min",
            tips = listOf("Practice slow breathing exercises to improve respiratory efficiency", "Cardio exercise strengthens respiratory muscles", "If consistently elevated, consult a healthcare provider")
        )
    }

    fun getWeightInsight(kg: Double): MetricInsight {
        return MetricInsight(
            status = "Tracked",
            statusColor = "green",
            description = "Body weight is a basic measure of overall mass. It's most useful when tracked over time as a trend, rather than focusing on any single measurement.",
            meaning = "Your current weight is ${String.format("%.1f", kg)} kg. Weight naturally fluctuates 1-2 kg daily due to hydration, meals, and activity.",
            normalRange = "Varies by height, age, and body composition",
            tips = listOf("Weigh yourself at the same time each day for consistency", "Weekly averages are more meaningful than daily readings", "Focus on body composition (fat vs muscle) rather than weight alone", "Hydration significantly affects daily weight")
        )
    }

    fun getBodyFatInsight(percentage: Double): MetricInsight {
        // Male ranges (adjust for female later)
        val (status, color) = when {
            percentage < 6 -> "Essential" to "yellow"
            percentage in 6.0..13.0 -> "Athletic" to "green"
            percentage in 14.0..17.0 -> "Fitness" to "green"
            percentage in 18.0..24.0 -> "Average" to "yellow"
            else -> "Above Average" to "red"
        }
        return MetricInsight(
            status = status,
            statusColor = color,
            description = "Body fat percentage measures the proportion of your body that is fat tissue. It's a better indicator of health than weight alone.",
            meaning = when {
                percentage < 14 -> "Your body fat of ${String.format("%.1f", percentage)}% is in the athletic range. You have a lean physique with good muscle definition."
                percentage in 14.0..17.0 -> "Your body fat of ${String.format("%.1f", percentage)}% is in the fitness range. This indicates a healthy, active body composition."
                percentage in 18.0..24.0 -> "Your body fat of ${String.format("%.1f", percentage)}% is in the average range. Increasing exercise and adjusting diet can lower it."
                else -> "Your body fat of ${String.format("%.1f", percentage)}% is above average. Consider increasing activity and reviewing your nutrition."
            },
            normalRange = "6-24% for males, 16-30% for females",
            tips = listOf("Strength training builds muscle which raises your base metabolic rate", "Protein intake of 1.6-2.2g/kg supports muscle preservation during fat loss", "Consistent caloric deficit of 300-500 kcal/day is sustainable for fat loss", "Sleep deprivation increases fat storage — prioritize 7+ hours")
        )
    }

    fun getBmrInsight(kcal: Double): MetricInsight {
        return MetricInsight(
            status = "Tracked",
            statusColor = "green",
            description = "Basal Metabolic Rate (BMR) is the number of calories your body burns at complete rest to maintain basic life functions like breathing, circulation, and cell production.",
            meaning = "Your BMR is ${String.format("%.0f", kcal)} kcal/day. This means your body burns about ${String.format("%.0f", kcal)} calories daily just to stay alive, before any activity.",
            normalRange = "1,200-2,400 kcal/day (varies by age, weight, muscle mass)",
            tips = listOf("More muscle mass = higher BMR (strength training helps)", "BMR decreases with age — staying active counteracts this", "Your total daily calorie needs = BMR × 1.2 to 1.9 depending on activity level", "Crash diets lower BMR — avoid extreme caloric restriction")
        )
    }

    fun getStepsInsight(count: Long, goal: Long): MetricInsight {
        val pct = (count.toFloat() / goal * 100).toInt()
        val (status, color) = when {
            count >= goal -> "Goal Met" to "green"
            count >= goal * 0.7 -> "Almost There" to "yellow"
            count >= goal * 0.3 -> "Getting Started" to "yellow"
            else -> "Low Activity" to "red"
        }
        return MetricInsight(
            status = status,
            statusColor = color,
            description = "Daily steps measure your overall physical activity throughout the day. Regular walking reduces risk of cardiovascular disease, diabetes, and depression.",
            meaning = "You've taken $count steps today ($pct% of your $goal goal). ${when {
                count >= goal -> "Great job hitting your daily target!"
                count >= goal * 0.7 -> "You're close to your goal — a short walk will get you there."
                else -> "Try to add more movement to your day."
            }}",
            normalRange = "7,000-10,000 steps/day recommended",
            tips = listOf("Take a 10-minute walk after each meal — it also helps blood sugar", "Park farther away or take stairs when possible", "Walking meetings boost creativity by 60%", "Even 4,000 steps/day reduces all-cause mortality significantly")
        )
    }

    fun getSkinTempInsight(delta: Double): MetricInsight {
        val (status, color) = when {
            delta in -1.0..1.0 -> "Normal" to "green"
            delta < -1.0 -> "Cool" to "yellow"
            else -> "Warm" to "yellow"
        }
        return MetricInsight(
            status = status,
            statusColor = color,
            description = "Skin temperature variation measures changes from your baseline wrist temperature. It can indicate illness, ovulation, or environmental factors.",
            meaning = "Your skin temperature variation is ${String.format("%.1f", delta)}°C from baseline. ${when {
                delta in -0.5..0.5 -> "This is very close to your normal baseline."
                delta < -1.0 -> "A cooler reading may be due to cold environment or reduced blood flow."
                delta > 1.0 -> "A warmer reading could indicate illness, stress, or hormonal changes."
                else -> "This is a minor variation within normal range."
            }}",
            normalRange = "-1.0 to +1.0°C variation from baseline",
            tips = listOf("Track trends over time rather than single readings", "Sudden increases may indicate oncoming illness", "Room temperature and clothing affect readings")
        )
    }

    fun getCaloriesInsight(total: Double): MetricInsight {
        return MetricInsight(
            status = "Tracked",
            statusColor = "green",
            description = "Total calories burned includes your BMR (resting metabolism) plus calories from physical activity. This represents your total daily energy expenditure.",
            meaning = "You've burned ${String.format("%.0f", total)} kcal today. This includes both resting metabolism and active calories from movement and exercise.",
            normalRange = "1,800-3,000 kcal/day (varies by size and activity)",
            tips = listOf("To lose weight: eat fewer calories than you burn", "To gain muscle: eat slightly more with adequate protein", "NEAT (non-exercise activity) accounts for 15-30% of daily burn", "Accurate tracking requires consistent wear of your device")
        )
    }

    fun getDistanceInsight(km: Double): MetricInsight {
        return MetricInsight(
            status = if (km >= 5) "Active" else "Tracked",
            statusColor = if (km >= 5) "green" else "yellow",
            description = "Distance traveled throughout the day from walking, running, and other activities.",
            meaning = "You've covered ${String.format("%.2f", km)} km today. ${if (km >= 5) "That's a solid amount of movement!" else "Try to add a walk to increase your daily distance."}",
            normalRange = "3-8 km/day for active adults",
            tips = listOf("Walking 5+ km daily is associated with lower cardiovascular risk", "Mix walking with occasional jogging for better fitness gains", "Track weekly distance trends rather than daily for consistency")
        )
    }
}
