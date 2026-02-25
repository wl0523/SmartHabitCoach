package com.example.smarthabitcoach.data.repository

import com.example.smarthabitcoach.data.ai.ChatCompletionRequest
import com.example.smarthabitcoach.data.ai.ChatMessage
import com.example.smarthabitcoach.data.ai.OpenAiService
import com.example.smarthabitcoach.data.ai.WeeklyInsightApiResponse
import com.example.smarthabitcoach.domain.model.Habit
import com.example.smarthabitcoach.domain.model.HabitStatistics
import com.example.smarthabitcoach.domain.model.InsightSource
import com.example.smarthabitcoach.domain.model.WeeklyInsight
import com.example.smarthabitcoach.domain.repository.AiRepository
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

class AiRepositoryImpl @Inject constructor(
    private val openAiService: OpenAiService,
    @com.example.smarthabitcoach.data.di.ApiKey private val apiKey: String
) : AiRepository {

    override suspend fun generateWeeklyInsight(
        habits: List<Habit>,
        statistics: HabitStatistics,
        weekOf: LocalDate
    ): Result<WeeklyInsight> = runCatching {

        val prompt = buildPrompt(habits, statistics)

        val response = openAiService.chatCompletion(
            authorization = "Bearer $apiKey",
            request = ChatCompletionRequest(
                messages = listOf(
                    ChatMessage(
                        role = "system",
                        content = """You are a behavioral coach assistant. Analyze habit data and return a JSON object with these exact keys:
                            |"summary" (2-3 sentences), "topPerformingHabit" (string or null),
                            |"mostAtRiskHabit" (string or null), "recommendation" (1 specific action),
                            |"overallScore" (integer 0-100). No markdown, pure JSON only.""".trimMargin()
                    ),
                    ChatMessage(role = "user", content = prompt)
                )
            )
        )

        val content = response.choices.first().message.content
        val parsed = Json.decodeFromString<WeeklyInsightApiResponse>(content)

        WeeklyInsight(
            weekOf = weekOf,
            summary = parsed.summary,
            topPerformingHabit = parsed.topPerformingHabit,
            mostAtRiskHabit = parsed.mostAtRiskHabit,
            recommendation = parsed.recommendation,
            overallScore = parsed.overallScore.coerceIn(0, 100),
            generatedAt = Instant.now(),
            source = InsightSource.AI
        )
    }

    private fun buildPrompt(habits: List<Habit>, statistics: HabitStatistics): String {
        val habitSummaries = habits.joinToString(separator = "\n") { habit ->
            val completionCount = habit.completedDates.size
            "- \"${habit.title}\": $completionCount completions total, " +
                "streak ${statistics.currentStreak}d"
        }

        return """
            Weekly habit data for behavioral analysis:
            Total habits: ${habits.size}
            Completed today: ${statistics.completedToday}
            Current streak: ${statistics.currentStreak} days
            Longest streak: ${statistics.longestStreak} days
            Weekly completion rate: ${(statistics.weeklyCompletionRate * 100).toInt()}%
            
            Individual habits:
            $habitSummaries
            
            Provide a JSON behavioral coaching insight for this week.
        """.trimIndent()
    }
}

