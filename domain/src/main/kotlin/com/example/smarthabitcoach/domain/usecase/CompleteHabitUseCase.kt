package com.example.smarthabitcoach.domain.usecase

import com.example.smarthabitcoach.domain.model.Habit
import com.example.smarthabitcoach.domain.repository.HabitRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class CompleteHabitUseCase @Inject constructor(private val repository: HabitRepository) {
    suspend operator fun invoke(habitId: String, completed: Boolean) {
        val habit = repository.getHabitById(habitId) ?: return

        val today = LocalDate.now().format(DATE_FORMATTER)
        val updatedCompletedDates = if (completed) {
            habit.completedDates + today
        } else {
            habit.completedDates - today
        }

        val updatedHabit = habit.copy(
            isCompleted = completed,
            completedDates = updatedCompletedDates
        )
        repository.updateHabit(updatedHabit)
    }

    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }
}
