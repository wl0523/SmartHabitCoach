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
    val isCompleted: Boolean = false, // Overall completion status
    val isCompletedToday: Boolean = false, // Today's completion status
    val streak: Int = 0,
    val createdAt: Long = System.currentTimeMillis(), // Timestamp for when the habit was created
    val completedDates: Set<String> = emptySet(), // Set of dates when the habit was completed
    val completionHistory: List<Long> = emptyList() // List of completion timestamps
)
