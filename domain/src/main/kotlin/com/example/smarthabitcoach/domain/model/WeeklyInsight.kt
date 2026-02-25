package com.example.smarthabitcoach.domain.model

import java.time.Instant
import java.time.LocalDate

data class WeeklyInsight(
    val weekOf: LocalDate,
    val summary: String,
    val topPerformingHabit: String?,
    val mostAtRiskHabit: String?,
    val recommendation: String,
    val overallScore: Int,
    val generatedAt: Instant,
    val source: InsightSource
)

enum class InsightSource { AI, FALLBACK }
