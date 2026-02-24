package com.example.smarthabitcoach.domain.usecase

import com.example.smarthabitcoach.domain.repository.HabitRepository
import javax.inject.Inject

/**
 * UpdateHabitUseCase: Updates an existing habit's title and description.
 *
 * Validates that the title is not blank before persisting.
 * Does not modify completedDates or streak-related fields —
 * those are managed exclusively by CompleteHabitUseCase.
 */
class UpdateHabitUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    suspend operator fun invoke(habitId: String, title: String, description: String) {
        require(title.isNotBlank()) { "Habit title must not be blank" }

        val existing = repository.getHabitById(habitId) ?: return

        val updated = existing.copy(
            title = title.trim(),
            description = description.trim()
        )
        repository.updateHabit(updated)
    }
}

