package com.example.smarthabitcoach.domain.repository

import com.example.smarthabitcoach.domain.model.WeeklyInsight
import java.time.LocalDate

/**
 * Cache repository for AI-generated weekly insights.
 * Implemented in :data module with Room.
 * Provides date-keyed access with automatic TTL via date comparison.
 */
interface WeeklyInsightCacheRepository {
    /** Returns the cached insight for the given week's Monday, or null if not cached. */
    suspend fun getInsightForWeek(weekOf: LocalDate): WeeklyInsight?

    /** Persists a generated insight. Replaces any existing entry for the same week. */
    suspend fun saveInsight(insight: WeeklyInsight)
}
