package com.example.smarthabitcoach.domain.usecase

import com.example.smarthabitcoach.domain.model.DailyNudge
import com.example.smarthabitcoach.domain.model.Habit
import com.example.smarthabitcoach.domain.model.HabitStatistics
import com.example.smarthabitcoach.domain.model.InsightSource
import com.example.smarthabitcoach.domain.repository.AiRepository
import com.example.smarthabitcoach.domain.repository.DailyNudgeCacheRepository
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

/**
 * GenerateDailyNudgeUseCase
 *
 * Combines reactive Flow<HabitStatistics> with a Room-backed TTL cache layer
 * to deliver GPT-4o-mini-generated daily nudges.
 *
 * Cache strategy: date-keyed (today's date = cache key).
 * - Cache HIT  → return immediately, zero API call
 * - Cache MISS → call GPT-4o-mini, save to Room, return result
 * - API FAIL   → deterministic fallback, no crash
 */
class GenerateDailyNudgeUseCase @Inject constructor(
    private val aiRepository: AiRepository,
    private val cacheRepository: DailyNudgeCacheRepository
) {
    suspend operator fun invoke(
        habits: List<Habit>,
        stats: HabitStatistics,
        date: LocalDate = LocalDate.now()
    ): DailyNudge {
        // date-keyed cache check — zero redundant API calls
        cacheRepository.getNudgeForDate(date)?.let { return it }

        val result = aiRepository.generateDailyNudge(habits, stats, date)

        return if (result.isSuccess) {
            val nudge = result.getOrThrow()
            cacheRepository.saveNudge(nudge)
            nudge
        } else {
            buildFallback(habits, stats, date)
        }
    }

    private fun buildFallback(
        habits: List<Habit>,
        stats: HabitStatistics,
        date: LocalDate
    ): DailyNudge {
        val message = when {
            habits.isEmpty() -> "Start your first habit today — small steps lead to big changes 🌱"
            stats.currentStreak >= 7 -> "🔥 ${stats.currentStreak}-day streak! You're on fire. Keep it going today."
            stats.currentStreak >= 3 -> "💪 ${stats.currentStreak} days in a row! Don't break the chain today."
            stats.completedToday == stats.totalHabits && stats.totalHabits > 0 ->
                "🎉 All habits done today! Amazing consistency."
            stats.completedToday == 0 -> "Your habits are waiting — completing even one today keeps the momentum alive."
            else -> "You've completed ${stats.completedToday}/${stats.totalHabits} habits today. Finish strong! 💫"
        }
        return DailyNudge(
            date = date,
            message = message,
            generatedAt = Instant.now(),
            source = InsightSource.FALLBACK
        )
    }
}


