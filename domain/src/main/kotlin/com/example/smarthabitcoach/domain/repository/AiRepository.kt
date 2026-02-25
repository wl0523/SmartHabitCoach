package com.example.smarthabitcoach.domain.repository

import com.example.smarthabitcoach.domain.model.DailyNudge
import com.example.smarthabitcoach.domain.model.Habit
import com.example.smarthabitcoach.domain.model.HabitStatistics
import com.example.smarthabitcoach.domain.model.WeeklyInsight
import java.time.LocalDate

/**
 * Repository interface for AI-generated content.
 * Implemented in :data module (AiRepositoryImpl).
 * Domain layer stays framework-free — no Retrofit/OkHttp references here.
 */
interface AiRepository {
    /**
     * Generate a weekly behavioral insight via GPT-4o-mini.
     * Returns Result.failure on network/parse errors — caller handles fallback.
     */
    suspend fun generateWeeklyInsight(
        habits: List<Habit>,
        statistics: HabitStatistics,
        weekOf: LocalDate
    ): Result<WeeklyInsight>

    /**
     * Generate a daily coaching nudge via GPT-4o-mini.
     * Returns Result.failure on network/parse errors — caller handles fallback.
     */
    suspend fun generateDailyNudge(
        habits: List<Habit>,
        statistics: HabitStatistics,
        date: LocalDate
    ): Result<DailyNudge>
}
