package com.example.smarthabitcoach.domain.usecase

import com.example.smarthabitcoach.domain.model.Habit
import com.example.smarthabitcoach.domain.repository.HabitRepository
import javax.inject.Inject

class UpdateHabitUseCase @Inject constructor(private val repository: HabitRepository) {
    suspend operator fun invoke(habitId: String, title: String, description: String) {
        val existing = repository.getHabitById(habitId) ?: return
        repository.updateHabit(existing.copy(title = title, description = description))
    }
}
