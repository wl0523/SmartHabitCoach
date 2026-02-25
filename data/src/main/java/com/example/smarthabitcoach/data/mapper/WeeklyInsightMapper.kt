package com.example.smarthabitcoach.data.mapper

import com.example.smarthabitcoach.data.local.WeeklyInsightEntity
import com.example.smarthabitcoach.domain.model.InsightSource
import com.example.smarthabitcoach.domain.model.WeeklyInsight
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal object WeeklyInsightMapper {

    private val DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun toDomain(entity: WeeklyInsightEntity): WeeklyInsight = WeeklyInsight(
        weekOf = LocalDate.parse(entity.weekOf, DATE_FMT),
        summary = entity.summary,
        topPerformingHabit = entity.topPerformingHabit,
        mostAtRiskHabit = entity.mostAtRiskHabit,
        recommendation = entity.recommendation,
        overallScore = entity.overallScore,
        generatedAt = Instant.ofEpochMilli(entity.generatedAt),
        source = InsightSource.valueOf(entity.source)
    )

    fun toEntity(domain: WeeklyInsight): WeeklyInsightEntity = WeeklyInsightEntity(
        weekOf = domain.weekOf.format(DATE_FMT),
        summary = domain.summary,
        topPerformingHabit = domain.topPerformingHabit,
        mostAtRiskHabit = domain.mostAtRiskHabit,
        recommendation = domain.recommendation,
        overallScore = domain.overallScore,
        generatedAt = domain.generatedAt.toEpochMilli(),
        source = domain.source.name
    )
}

