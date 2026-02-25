package com.example.smarthabitcoach.data.mapper

import com.example.smarthabitcoach.data.local.HabitEntity
import com.example.smarthabitcoach.domain.model.Habit
import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal object HabitMapper {

    private val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun toDomain(entity: HabitEntity): Habit {
        val today = LocalDate.now().format(dateFmt)
        return Habit(
            id = entity.id,
            title = entity.title,
            description = entity.description,
            isCompleted = entity.isCompleted,
            isCompletedToday = today in entity.completedDates,
            streak = entity.completedDates.size, // 단순 streak 계산
            createdAt = entity.createdAt,
            completedDates = entity.completedDates
        )
    }

    fun toEntity(domain: Habit): HabitEntity = HabitEntity(
        id = domain.id,
        title = domain.title,
        description = domain.description,
        isCompleted = domain.isCompleted,
        createdAt = domain.createdAt,
        completedDates = domain.completedDates
    )
}
