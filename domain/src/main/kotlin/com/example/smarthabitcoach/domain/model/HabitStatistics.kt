package com.example.smarthabitcoach.domain.model

/**
 * Immutable domain model for habit statistics.
 * Calculated from individual habits.
 *
 * [weeklyDailyRates] — Daily completion rate from Monday (index 0) to Sunday (index 6) of this week (0f~1f).
 * Returns 0f if there are no habits or if the habit was not yet created on that date.
 */
data class HabitStatistics(
    val currentStreak: Int = 1,
    val longestStreak: Int = 0,
    val weeklyCompletionRate: Float = 0f,
    val weeklyDailyRates: List<Float> = List(7) { 0f }, // Mon~Sun, 7 entries
    val totalHabits: Int = 0,
    val completedToday: Int = 0,
    val totalCompleted: Int = 0,
)

