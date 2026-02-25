package com.example.smarthabitcoach.data.mapper

import com.example.smarthabitcoach.data.local.HabitEntity
import com.example.smarthabitcoach.domain.model.Habit
import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal object HabitMapper {

    private val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun toDomain(entity: HabitEntity): Habit {
        val today = LocalDate.now()
        val todayStr = today.format(dateFmt)
        val currentStreak = calculateCurrentStreak(entity.completedDates, today)
        // longestStreak = max of stored value and current streak (handles newly computed streaks)
        val longestStreak = maxOf(entity.longestStreak, currentStreak)
        return Habit(
            id = entity.id,
            title = entity.title,
            description = entity.description,
            isCompleted = entity.isCompleted,
            isCompletedToday = todayStr in entity.completedDates,
            streak = currentStreak,
            longestStreak = longestStreak,
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
        completedDates = domain.completedDates,
        longestStreak = domain.longestStreak
    )

    /**
     * Calculates the current consecutive streak ending on [today] (or yesterday).
     * Counts backward from today: if today is completed, start from today;
     * otherwise start from yesterday (grace: streak not broken until midnight passes).
     */
    private fun calculateCurrentStreak(completedDates: Set<String>, today: LocalDate): Int {
        if (completedDates.isEmpty()) return 0

        // Start from today if completed, otherwise from yesterday
        var checkDate = if (today.format(dateFmt) in completedDates) today else today.minusDays(1)

        var streak = 0
        while (checkDate.format(dateFmt) in completedDates) {
            streak++
            checkDate = checkDate.minusDays(1)
        }
        return streak
    }
}

