package com.example.smarthabitcoach.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for AI-generated daily nudges.
 * date-keyed (date = yyyy-MM-dd). One row per calendar day.
 * TTL: auto-evict entries older than 30 days.
 */
@Entity(tableName = "daily_nudges")
data class DailyNudgeEntity(
    @PrimaryKey
    val date: String,           // yyyy-MM-dd — cache key
    val message: String,
    val generatedAt: Long,      // epoch millis
    val source: String          // "AI" or "FALLBACK"
)

