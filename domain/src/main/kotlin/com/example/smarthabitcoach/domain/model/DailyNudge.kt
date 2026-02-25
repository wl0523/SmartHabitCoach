package com.example.smarthabitcoach.domain.model

import java.time.Instant
import java.time.LocalDate

/**
 * AI-generated daily coaching nudge.
 * date-keyed: one nudge per calendar day, cached in Room.
 */
data class DailyNudge(
    val date: LocalDate,        // yyyy-MM-dd key — one per day
    val message: String,        // GPT-4o-mini coaching message
    val generatedAt: Instant,
    val source: InsightSource   // AI or FALLBACK
)

