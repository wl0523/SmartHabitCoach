package com.example.smarthabitcoach.domain.usecase

import com.example.smarthabitcoach.domain.model.Habit
import com.example.smarthabitcoach.domain.model.HabitStatistics
import com.example.smarthabitcoach.domain.model.InsightSource
import com.example.smarthabitcoach.domain.model.WeeklyInsight
import com.example.smarthabitcoach.domain.repository.AiRepository
import com.example.smarthabitcoach.domain.repository.WeeklyInsightCacheRepository
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

class GenerateWeeklyInsightUseCase @Inject constructor(
    private val aiRepository: AiRepository,
    private val cacheRepository: WeeklyInsightCacheRepository
) {
    suspend operator fun invoke(
        habits: List<Habit>,
        stats: HabitStatistics
    ): WeeklyInsight {
        val weekOf = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

        // 캐시에 이번 주 데이터가 있으면 재사용 (API 비용 절약)
        cacheRepository.getInsightForWeek(weekOf)?.let { return it }

        val result = aiRepository.generateWeeklyInsight(habits, stats, weekOf)

        return if (result.isSuccess) {
            val insight = result.getOrThrow()
            cacheRepository.saveInsight(insight)
            insight
        } else {
            buildFallback(habits, stats, weekOf)
        }
    }

    private fun buildFallback(
        habits: List<Habit>,
        stats: HabitStatistics,
        weekOf: LocalDate
    ): WeeklyInsight {
        val pct = (stats.weeklyCompletionRate * 100).toInt()
        val recommendation = when {
            pct >= 80 -> "훌륭해요! 이 페이스를 유지하세요 🔥"
            pct >= 50 -> "절반 이상 달성! 조금만 더 노력해봐요 💪"
            habits.isEmpty() -> "아직 습관이 없어요. 첫 습관을 등록해보세요!"
            else -> "시작이 반이에요. 작은 습관부터 다시 도전해봐요 🌱"
        }
        return WeeklyInsight(
            weekOf = weekOf,
            summary = "이번 주 습관 달성률: $pct%",
            topPerformingHabit = habits.maxByOrNull { it.streak }?.title,
            mostAtRiskHabit = habits.firstOrNull { !it.isCompleted }?.title,
            recommendation = recommendation,
            overallScore = pct,
            generatedAt = Instant.now(),
            source = InsightSource.FALLBACK
        )
    }
}
