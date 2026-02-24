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
    val isCompleted: Boolean = false, // Today's completion status
    val createdAt: Long = System.currentTimeMillis(),
    val completedDates: Set<String> = emptySet(), // Dates in YYYY-MM-DD format
)

