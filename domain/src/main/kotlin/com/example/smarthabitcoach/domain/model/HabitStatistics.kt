package com.example.smarthabitcoach.domain.model

/**
 * Immutable domain model for habit statistics.
 * Calculated from individual habits.
 */
data class HabitStatistics(
    val totalHabits: Int = 0,
    val completedToday: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    /** Average daily completion rate for Mon–today this week (0f–1f). */
    val weeklyCompletionRate: Float = 0f,
    val weeklyDailyRates: List<Float> = List(7) { 0f },
    val totalCompleted: Int = 0
)
