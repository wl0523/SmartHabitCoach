package com.example.smarthabitcoach.domain.model

import java.util.UUID

/**
 * Immutable domain model for a Habit.
 * Tracks daily completion via completedDates (set of dates in YYYY-MM-DD format).
 */
data class Habit(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String? = null,
    val isCompleted: Boolean = false,         // Overall completion status (used by UI for visuals)
    val isCompletedToday: Boolean = false,    // Today's completion status
    val streak: Int = 0,                      // Current consecutive streak
    val longestStreak: Int = 0,               // All-time longest streak
    val createdAt: Long = System.currentTimeMillis(),
    val completedDates: Set<String> = emptySet()
)
