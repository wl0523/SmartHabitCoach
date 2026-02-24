package com.example.smarthabitcoach.data.mapper

import com.example.smarthabitcoach.data.local.HabitEntity
import com.example.smarthabitcoach.domain.model.Habit

internal object HabitMapper {
    fun toDomain(entity: HabitEntity): Habit = Habit(
        id = entity.id,
        title = entity.title,
        description = entity.description,
        isCompleted = entity.isCompleted,
        createdAt = entity.createdAt,
        completedDates = entity.completedDates
    )

    fun toEntity(domain: Habit): HabitEntity = HabitEntity(
        id = domain.id,
        title = domain.title,
        description = domain.description,
        isCompleted = domain.isCompleted,
        createdAt = domain.createdAt,
        completedDates = domain.completedDates
    )
}

