package com.example.smarthabitcoach.domain.usecase

import com.example.smarthabitcoach.domain.model.HabitStatistics
import com.example.smarthabitcoach.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

class GetStatisticsUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    private val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    operator fun invoke(): Flow<HabitStatistics> = repository.getHabits().map { habits ->
        val today = LocalDate.now().format(dateFmt)
        val completedToday = habits.count { today in it.completedDates }
        val streak = habits.maxOfOrNull { it.streak } ?: 0
        val rate = if (habits.isEmpty()) 0f else completedToday.toFloat() / habits.size
        val totalCompleted = habits.sumOf { it.completedDates.size }

        // Mon~Sun 7일 일별 달성률 계산
        val weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val dailyRates = List(7) { i ->
            val day = weekStart.plusDays(i.toLong())
            if (day.isAfter(LocalDate.now())) return@List 0f
            val dayStr = day.format(dateFmt)
            if (habits.isEmpty()) 0f
            else habits.count { dayStr in it.completedDates }.toFloat() / habits.size
        }

        HabitStatistics(
            totalHabits = habits.size,
            completedToday = completedToday,
            currentStreak = streak,
            longestStreak = streak,
            weeklyCompletionRate = rate,
            averageCompletionRate = rate,
            weeklyDailyRates = dailyRates,
            totalCompleted = totalCompleted
        )
    }
}
