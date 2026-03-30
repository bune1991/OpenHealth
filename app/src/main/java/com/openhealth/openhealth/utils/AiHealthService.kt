package com.openhealth.openhealth.utils

import android.util.Log
import com.openhealth.openhealth.model.AiProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AiHealthService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMedia = "application/json; charset=utf-8".toMediaType()

    // On-device Gemini Nano client (Play Services)

    suspend fun getInsights(
        provider: AiProvider,
        apiKey: String,
        healthSummaryPrompt: String,
        customUrl: String = "",
        customModel: String = ""
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Handle on-device AI separately (no network needed)
            if (provider == AiProvider.ON_DEVICE) {
                return@withContext getOnDeviceInsights(healthSummaryPrompt)
            }

            val (request) = when (provider) {
                AiProvider.CLAUDE -> buildClaudeRequest(apiKey, healthSummaryPrompt)
                AiProvider.GEMINI -> buildGeminiRequest(apiKey, healthSummaryPrompt)
                AiProvider.CHATGPT -> buildOpenAiRequest(apiKey, healthSummaryPrompt)
                AiProvider.CUSTOM -> buildCustomRequest(apiKey, healthSummaryPrompt, customUrl, customModel)
                AiProvider.NONE -> return@withContext Result.failure(
                    IllegalStateException("No AI provider configured")
                )
                AiProvider.ON_DEVICE -> return@withContext Result.failure(
                    IllegalStateException("Handled above")
                )
            }

            Log.d("AiHealthService", "Sending request to ${provider.name}...")

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e("AiHealthService", "API error ${response.code}: $body")
                return@withContext Result.failure(
                    Exception("API error ${response.code}: ${extractErrorMessage(body)}")
                )
            }

            val text = when (provider) {
                AiProvider.CLAUDE -> parseClaudeResponse(body)
                AiProvider.GEMINI -> parseGeminiResponse(body)
                AiProvider.CHATGPT -> parseOpenAiResponse(body)
                AiProvider.CUSTOM -> parseOpenAiResponse(body)
                AiProvider.NONE, AiProvider.ON_DEVICE -> ""
            }

            Log.d("AiHealthService", "Got response: ${text.take(100)}...")
            Result.success(text)
        } catch (e: Exception) {
            Log.e("AiHealthService", "Error: ${e.message}", e)
            Result.failure(e)
        }
    }

    private suspend fun getOnDeviceInsights(prompt: String): Result<String> {
        return try {
            val client = com.google.mlkit.genai.prompt.Generation.getClient()

            // Check if on-device model is available (returns @FeatureStatus int)
            val status = client.checkStatus()
            when (status) {
                com.google.mlkit.genai.common.FeatureStatus.UNAVAILABLE ->
                    return Result.failure(Exception("Gemini Nano not available on this device. Requires Pixel 9+, Samsung S25+, or similar flagship."))
                com.google.mlkit.genai.common.FeatureStatus.DOWNLOADABLE -> {
                    // Start download and wait for it to complete
                    Log.d("AiHealthService", "Starting Gemini Nano model download...")
                    client.download().collect { downloadStatus ->
                        Log.d("AiHealthService", "Download status: $downloadStatus")
                    }
                    // After download completes, re-check status
                    val newStatus = client.checkStatus()
                    if (newStatus != com.google.mlkit.genai.common.FeatureStatus.AVAILABLE) {
                        return Result.failure(Exception("Gemini Nano model download finished but not ready yet. Try again."))
                    }
                }
                com.google.mlkit.genai.common.FeatureStatus.DOWNLOADING -> {
                    // Wait for ongoing download to complete
                    Log.d("AiHealthService", "Waiting for Gemini Nano download...")
                    client.download().collect { downloadStatus ->
                        Log.d("AiHealthService", "Download status: $downloadStatus")
                    }
                    val newStatus = client.checkStatus()
                    if (newStatus != com.google.mlkit.genai.common.FeatureStatus.AVAILABLE) {
                        return Result.failure(Exception("Gemini Nano model still downloading. Try again shortly."))
                    }
                }
                com.google.mlkit.genai.common.FeatureStatus.AVAILABLE -> { /* ready */ }
            }

            // Shorten prompt for on-device — Nano works better with concise input
            val truncatedPrompt = if (prompt.length > 4000) prompt.take(4000) + "\n\nKeep response concise, under 300 words." else prompt

            Log.d("AiHealthService", "Sending to Gemini Nano on-device...")
            val response = client.generateContent(truncatedPrompt)
            val text = response.candidates.firstOrNull()?.text ?: ""

            Log.d("AiHealthService", "On-device response: ${text.take(100)}...")
            Result.success(text)
        } catch (e: Exception) {
            Log.e("AiHealthService", "On-device AI error: ${e.message}", e)
            Result.failure(Exception("On-device AI error: ${e.message}"))
        }
    }

    // Claude (Anthropic)
    private fun buildClaudeRequest(apiKey: String, prompt: String): Pair<Request, Unit> {
        val json = JSONObject().apply {
            put("model", "claude-sonnet-4-20250514")
            put("max_tokens", 1024)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            })
        }

        val request = Request.Builder()
            .url("https://api.anthropic.com/v1/messages")
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("content-type", "application/json")
            .post(json.toString().toRequestBody(jsonMedia))
            .build()

        return Pair(request, Unit)
    }

    private fun parseClaudeResponse(body: String): String {
        val json = JSONObject(body)
        val content = json.getJSONArray("content")
        return content.getJSONObject(0).getString("text")
    }

    // Gemini (Google)
    private fun buildGeminiRequest(apiKey: String, prompt: String): Pair<Request, Unit> {
        val json = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
        }

        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$apiKey")
            .addHeader("content-type", "application/json")
            .post(json.toString().toRequestBody(jsonMedia))
            .build()

        return Pair(request, Unit)
    }

    private fun parseGeminiResponse(body: String): String {
        val json = JSONObject(body)
        return json.getJSONArray("candidates")
            .getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")
    }

    // ChatGPT (OpenAI)
    private fun buildOpenAiRequest(apiKey: String, prompt: String): Pair<Request, Unit> {
        val json = JSONObject().apply {
            put("model", "gpt-4o-mini")
            put("max_tokens", 1024)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            })
        }

        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("content-type", "application/json")
            .post(json.toString().toRequestBody(jsonMedia))
            .build()

        return Pair(request, Unit)
    }

    private fun parseOpenAiResponse(body: String): String {
        val json = JSONObject(body)
        return json.getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
    }

    // Custom (OpenAI-compatible: Ollama, OpenRouter, Fireworks, Groq, LM Studio)
    private fun buildCustomRequest(apiKey: String, prompt: String, baseUrl: String, model: String): Pair<Request, Unit> {
        val url = baseUrl.trimEnd('/') + "/chat/completions"

        val json = JSONObject().apply {
            put("model", model.ifBlank { "llama3" })
            put("max_tokens", 1024)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            })
        }

        val builder = Request.Builder()
            .url(url)
            .addHeader("content-type", "application/json")
            .post(json.toString().toRequestBody(jsonMedia))

        // Add auth header only if key is provided (Ollama doesn't need it)
        if (apiKey.isNotBlank()) {
            builder.addHeader("Authorization", "Bearer $apiKey")
        }

        return Pair(builder.build(), Unit)
    }

    private fun extractErrorMessage(body: String): String {
        return try {
            val json = JSONObject(body)
            json.optJSONObject("error")?.optString("message") ?: body.take(200)
        } catch (e: Exception) {
            body.take(200)
        }
    }
}
