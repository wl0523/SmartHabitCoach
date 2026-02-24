package com.example.smarthabitcoach.domain.usecase

import com.example.smarthabitcoach.domain.model.Habit
import com.example.smarthabitcoach.domain.model.HabitStatistics
import com.example.smarthabitcoach.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * GetStatisticsUseCase: Pure business logic for calculating habit statistics.
 *
 * Calculates:
 * - Current streak: consecutive days completed (ending today or yesterday)
 * - Longest streak: historical longest consecutive completion streak
 * - Weekly completion rate: % of habits completed in last 7 days
 * - Today's completions: how many habits are completed today
 *
 * All date calculations happen here (Domain layer), not in UI.
 */
class GetStatisticsUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    operator fun invoke(): Flow<HabitStatistics> = repository.observeHabits().map { habits ->
        calculateStatistics(habits)
    }

    private fun calculateStatistics(habits: List<Habit>): HabitStatistics {
        val today = LocalDate.now().format(DATE_FORMATTER)
        val sevenDaysAgo = LocalDate.now().minusDays(7).format(DATE_FORMATTER)

        val currentStreak = calculateCurrentStreak(habits)
        val longestStreak = calculateLongestStreak(habits)
        val weeklyCompletionRate = calculateWeeklyCompletionRate(habits, sevenDaysAgo, today)
        val completedToday = habits.count { it.isCompleted }
        val totalCompleted = habits.sumOf { it.completedDates.size }

        return HabitStatistics(
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            weeklyCompletionRate = weeklyCompletionRate,
            totalHabits = habits.size,
            completedToday = completedToday,
            totalCompleted = totalCompleted
        )
    }

    /**
     * Calculate current streak: consecutive days from today or yesterday going backward.
     * Returns the count of consecutive days where the habit was completed.
     */
    private fun calculateCurrentStreak(habits: List<Habit>): Int {
        if (habits.isEmpty()) return 0

        // Aggregate all completed dates across all habits
        val allCompletedDates = habits
            .flatMap { it.completedDates }
            .map { LocalDate.parse(it, DATE_FORMATTER) }
            .sorted()
            .toSet()

        if (allCompletedDates.isEmpty()) return 0

        val today = LocalDate.now()
        var currentDate = today

        // Start from today, go backward checking each day
        var streak = 0
        while (currentDate in allCompletedDates) {
            streak++
            currentDate = currentDate.minusDays(1)
        }

        // If streak ends yesterday, it's still valid (count it)
        // If streak ends before yesterday, check if today has no completion (streak is 0)
        if (streak == 0 && today.minusDays(1) in allCompletedDates) {
            streak = 1
            currentDate = today.minusDays(2)
            while (currentDate in allCompletedDates) {
                streak++
                currentDate = currentDate.minusDays(1)
            }
        }

        return streak
    }

    /**
     * Calculate longest streak: find the longest consecutive completion streak historically.
     */
    private fun calculateLongestStreak(habits: List<Habit>): Int {
        if (habits.isEmpty()) return 0

        val allCompletedDates = habits
            .flatMap { it.completedDates }
            .map { LocalDate.parse(it, DATE_FORMATTER) }
            .sorted()

        if (allCompletedDates.isEmpty()) return 0

        var longestStreak = 1
        var currentStreak = 1

        for (i in 1 until allCompletedDates.size) {
            val daysBetween = ChronoUnit.DAYS.between(allCompletedDates[i - 1], allCompletedDates[i])
            if (daysBetween == 1L) {
                currentStreak++
                longestStreak = maxOf(longestStreak, currentStreak)
            } else {
                currentStreak = 1
            }
        }

        return longestStreak
    }

    /**
     * Calculate weekly completion rate: % of habits completed in last 7 days.
     * Rate = (sum of completions in 7 days) / (total habits * 7 days)
     */
    private fun calculateWeeklyCompletionRate(
        habits: List<Habit>,
        sevenDaysAgo: String,
        today: String
    ): Float {
        if (habits.isEmpty()) return 0f

        val sevenDaysAgoDate = LocalDate.parse(sevenDaysAgo, DATE_FORMATTER)
        val todayDate = LocalDate.parse(today, DATE_FORMATTER)

        var totalPossibleCompletions = 0
        var actualCompletions = 0

        for (habit in habits) {
            val completedInWeek = habit.completedDates.filter { dateStr ->
                val date = LocalDate.parse(dateStr, DATE_FORMATTER)
                date.isAfter(sevenDaysAgoDate) && !date.isAfter(todayDate)
            }
            totalPossibleCompletions += 7 // 7 days for this habit
            actualCompletions += completedInWeek.size
        }

        return if (totalPossibleCompletions == 0) 0f
        else actualCompletions.toFloat() / totalPossibleCompletions
    }

    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }
}

