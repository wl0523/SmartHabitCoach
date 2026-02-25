package com.example.smarthabitcoach.data.ai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Request ───────────────────────────────────────────────────────────────────

@Serializable
data class ChatCompletionRequest(
    val model: String = "gpt-4o-mini",
    val messages: List<ChatMessage>,
    @SerialName("max_tokens") val maxTokens: Int = 300,
    val temperature: Double = 0.7,
    @SerialName("response_format") val responseFormat: ResponseFormat = ResponseFormat("json_object")
)

@Serializable
data class ChatMessage(val role: String, val content: String)

@Serializable
data class ResponseFormat(val type: String)

// ── Response ──────────────────────────────────────────────────────────────────

@Serializable
data class ChatCompletionResponse(
    val choices: List<Choice>
)

@Serializable
data class Choice(val message: ChatMessage)

// ── Parsed domain-specific response ──────────────────────────────────────────

@Serializable
data class WeeklyInsightApiResponse(
    val summary: String,
    @SerialName("topPerformingHabit") val topPerformingHabit: String? = null,
    @SerialName("mostAtRiskHabit") val mostAtRiskHabit: String? = null,
    val recommendation: String,
    val overallScore: Int
)

@Serializable
data class DailyNudgeApiResponse(
    val message: String  // single coaching nudge message
)

