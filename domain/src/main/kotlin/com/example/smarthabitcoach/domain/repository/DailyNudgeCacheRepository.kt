package com.example.smarthabitcoach.domain.repository

import com.example.smarthabitcoach.domain.model.DailyNudge
import java.time.LocalDate

/**
 * Cache repository for AI-generated daily nudges.
 * Implemented in :data module with Room.
 * date-keyed: one nudge per calendar day — zero redundant API calls.
 */
interface DailyNudgeCacheRepository {
    /** Returns cached nudge for [date], or null if not cached. */
    suspend fun getNudgeForDate(date: LocalDate): DailyNudge?

    /** Persists a nudge. Replaces any existing entry for the same date. */
    suspend fun saveNudge(nudge: DailyNudge)
}

