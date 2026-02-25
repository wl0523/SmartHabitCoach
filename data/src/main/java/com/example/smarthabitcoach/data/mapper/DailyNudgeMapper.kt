package com.example.smarthabitcoach.data.mapper

import com.example.smarthabitcoach.data.local.DailyNudgeEntity
import com.example.smarthabitcoach.domain.model.DailyNudge
import com.example.smarthabitcoach.domain.model.InsightSource
import java.time.Instant
import java.time.LocalDate

internal object DailyNudgeMapper {

    fun toDomain(entity: DailyNudgeEntity): DailyNudge = DailyNudge(
        date = LocalDate.parse(entity.date),
        message = entity.message,
        generatedAt = Instant.ofEpochMilli(entity.generatedAt),
        source = if (entity.source == "AI") InsightSource.AI else InsightSource.FALLBACK
    )

    fun toEntity(domain: DailyNudge): DailyNudgeEntity = DailyNudgeEntity(
        date = domain.date.toString(),
        message = domain.message,
        generatedAt = domain.generatedAt.toEpochMilli(),
        source = domain.source.name
    )
}

