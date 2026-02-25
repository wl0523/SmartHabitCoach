package com.example.smarthabitcoach.domain.model

/**
 * Immutable domain model for habit statistics.
 * Calculated from individual habits.
 */
data class HabitStatistics(
    /**
     * Total number of habits.
     */
    val totalHabits: Int = 0,

    /**
     * Number of habits completed today.
     */
    val completedToday: Int = 0,

    /**
     * Current streak of consecutive habit completions.
     */
    val currentStreak: Int = 0,

    /**
     * Longest streak of consecutive habit completions.
     */
    val longestStreak: Int = 0,

    /**
     * Weekly completion rate of habits.
     * Ranges from 0f to 1f.
     */
    val weeklyCompletionRate: Float = 0f,

    /**
     * Average completion rate of habits this week.
     * Ranges from 0f to 1f.
     */
    val averageCompletionRate: Float = 0f,

    /**
     * Daily completion rates of habits for the past week.
     * Ranges from 0f to 1f, for each day from Sunday to Saturday.
     */
    val weeklyDailyRates: List<Float> = List(7) { 0f },

    /**
     * Total number of times habits have been completed.
     */
    val totalCompleted: Int = 0
)
