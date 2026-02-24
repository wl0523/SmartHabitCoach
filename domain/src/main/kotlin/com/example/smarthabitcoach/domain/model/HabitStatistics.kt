package com.example.smarthabitcoach.domain.model

/**
 * Immutable domain model for habit statistics.
 * Calculated from individual habits.
 */
data class HabitStatistics(
    val currentStreak: Int = 0, // Consecutive days completed
    val longestStreak: Int = 0, // Historical longest streak
    val weeklyCompletionRate: Float = 0f, // 0-1 (0% - 100%)
    val totalHabits: Int = 0,
    val completedToday: Int = 0,
    val totalCompleted: Int = 0, // All-time completed count
)

