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
        val today = LocalDate.now()
        val todayStr = today.format(dateFmt)

        val completedToday = habits.count { todayStr in it.completedDates }
        val currentStreak = habits.maxOfOrNull { it.streak } ?: 0
        val longestStreak = habits.maxOfOrNull { it.longestStreak } ?: 0
        val totalCompleted = habits.sumOf { it.completedDates.size }

        // Mon~Sun 7일 일별 달성률 계산
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val dailyRates = List(7) { i ->
            val day = weekStart.plusDays(i.toLong())
            if (day.isAfter(today)) return@List 0f
            val dayStr = day.format(dateFmt)
            if (habits.isEmpty()) 0f
            else habits.count { dayStr in it.completedDates }.toFloat() / habits.size
        }

        // Weekly completion rate = average of daily rates for days up to and including today
        val weeklyCompletionRate = if (habits.isEmpty()) 0f
        else {
            val daysElapsed = (today.dayOfWeek.value) // Mon=1 … Sun=7
            val ratesUpToToday = dailyRates.take(daysElapsed)
            if (ratesUpToToday.isEmpty()) 0f else ratesUpToToday.average().toFloat()
        }

        HabitStatistics(
            totalHabits = habits.size,
            completedToday = completedToday,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            weeklyCompletionRate = weeklyCompletionRate,
            weeklyDailyRates = dailyRates,
            totalCompleted = totalCompleted
        )
    }
}



