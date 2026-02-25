package com.example.smarthabitcoach.domain.usecase

import com.example.smarthabitcoach.domain.repository.HabitRepository
import javax.inject.Inject

class CompleteHabitUseCase @Inject constructor(private val repository: HabitRepository) {
    suspend operator fun invoke(habitId: String, completed: Boolean) =
        repository.completeHabit(habitId, completed)
}
