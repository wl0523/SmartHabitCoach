package com.example.smarthabitcoach.domain.usecase

import com.example.smarthabitcoach.domain.model.Habit
import com.example.smarthabitcoach.domain.model.HabitStatistics
import com.example.smarthabitcoach.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
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
        val weeklyDailyRates = calculateWeeklyDailyRates(habits)
        val completedToday = habits.count { it.isCompleted }
        val totalCompleted = habits.sumOf { it.completedDates.size }

        return HabitStatistics(
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            weeklyCompletionRate = weeklyCompletionRate,
            weeklyDailyRates = weeklyDailyRates,
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
     * Calculate weekly completion rate.
     *
     * Formula:
     *   rate = actualCompletions / possibleCompletions
     *
     * possibleCompletions per habit = min(days since habit was created, 7)
     * → A habit created today counts as 1 possible day, not 7.
     * → A habit created 3 days ago counts as 3 possible days.
     * → A habit created 7+ days ago counts as 7 possible days.
     */
    private fun calculateWeeklyCompletionRate(
        habits: List<Habit>,
        sevenDaysAgo: String,
        today: String
    ): Float {
        if (habits.isEmpty()) return 0f

        val sevenDaysAgoDate = LocalDate.parse(sevenDaysAgo, DATE_FORMATTER)
        val todayDate        = LocalDate.parse(today, DATE_FORMATTER)

        var totalPossibleCompletions = 0
        var actualCompletions = 0

        for (habit in habits) {
            // Earliest date to count: whichever is later — 7 days ago or habit creation date
            val habitCreatedDate = LocalDate.ofEpochDay(habit.createdAt / 86_400_000)
            val startDate = if (habitCreatedDate.isAfter(sevenDaysAgoDate)) habitCreatedDate
                            else sevenDaysAgoDate

            // Number of days this habit could have been completed in the window
            val possibleDays = ChronoUnit.DAYS.between(startDate, todayDate).toInt() + 1

            // Count actual completions within the window
            val completedInWeek = habit.completedDates.count { dateStr ->
                val date = LocalDate.parse(dateStr, DATE_FORMATTER)
                !date.isBefore(startDate) && !date.isAfter(todayDate)
            }

            totalPossibleCompletions += possibleDays.coerceAtLeast(1)
            actualCompletions        += completedInWeek
        }

        return if (totalPossibleCompletions == 0) 0f
        else (actualCompletions.toFloat() / totalPossibleCompletions).coerceIn(0f, 1f)
    }

    /**
     * Calculates the actual daily completion rate for each day of the current week (Mon–Sun, 7 days).
     *
     * - index 0 = Monday of this week, index 6 = Sunday of this week
     * - Completion rate per day = (number of habits completed that day) / (number of habits that existed that day)
     * - Future dates (not yet reached) return 0f
     * - Dates before a habit was created are excluded from the denominator
     */
    private fun calculateWeeklyDailyRates(habits: List<Habit>): List<Float> {
        if (habits.isEmpty()) return List(7) { 0f }

        val today = LocalDate.now()
        // Monday of this week (ISO week standard)
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

        return List(7) { dayIndex ->
            val day = weekStart.plusDays(dayIndex.toLong())

            // Future dates return 0f
            if (day.isAfter(today)) return@List 0f

            val dayStr = day.format(DATE_FORMATTER)
            val dayEpoch = day.toEpochDay() * 86_400_000L // in milliseconds

            // Only count habits that already existed on that day (createdAt <= day 23:59:59)
            val eligibleHabits = habits.filter { habit ->
                val habitCreatedDay = LocalDate.ofEpochDay(habit.createdAt / 86_400_000)
                !habitCreatedDay.isAfter(day)
            }

            if (eligibleHabits.isEmpty()) return@List 0f

            val completedOnDay = eligibleHabits.count { habit ->
                dayStr in habit.completedDates
            }

            (completedOnDay.toFloat() / eligibleHabits.size).coerceIn(0f, 1f)
        }
    }

    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }
}

