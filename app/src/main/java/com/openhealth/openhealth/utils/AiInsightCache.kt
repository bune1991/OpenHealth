package com.openhealth.openhealth.utils

import android.content.Context
import com.openhealth.openhealth.model.AiProvider
import java.time.LocalDate

class AiInsightCache(context: Context) {
    private val prefs = context.getSharedPreferences("ai_insight_cache", Context.MODE_PRIVATE)

    fun getCached(date: LocalDate): String? {
        val cachedDate = prefs.getString("cached_date", null)
        return if (cachedDate == date.toString()) {
            prefs.getString("cached_response", null)
        } else null
    }

    fun save(date: LocalDate, response: String, provider: AiProvider) {
        prefs.edit()
            .putString("cached_date", date.toString())
            .putString("cached_response", response)
            .putString("cached_provider", provider.name)
            .apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
