package com.example.smarthabitcoach.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for AI-generated weekly insights.
 * date-keyed (weekOf = Monday of that week in yyyy-MM-dd format).
 * Acts as a TTL cache: one row per week, never regenerated for the same weekOf.
 */
@Entity(tableName = "weekly_insights")
data class WeeklyInsightEntity(
    @PrimaryKey
    val weekOf: String,               // yyyy-MM-dd (Monday of the week)
    val summary: String,
    val topPerformingHabit: String?,
    val mostAtRiskHabit: String?,
    val recommendation: String,
    val overallScore: Int,
    val generatedAt: Long,            // epoch millis
    val source: String                // "AI" or "FALLBACK"
)

