package com.example.smarthabitcoach.habits.ui

import com.example.smarthabitcoach.domain.model.Habit
import com.example.smarthabitcoach.domain.model.HabitStatistics

/**
 * Immutable UI state for habit management screen.
 * Represents all possible states the screen can be in.
 */
data class HabitUiState(
    val habits: List<Habit> = emptyList(),
    val statistics: HabitStatistics = HabitStatistics(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val createDialogVisible: Boolean = false,
    val newHabitTitle: String = "",
    val newHabitDescription: String = "",
    val editDialogHabit: Habit? = null,
    val editHabitTitle: String = "",
    val editHabitDescription: String = "",
)

/**
 * UI events triggered by user interactions.
 * ViewModel listens to these events and updates state accordingly.
 */
sealed class HabitUiEvent {
    data class CreateHabit(val title: String, val description: String? = null) : HabitUiEvent()
    data class CompleteHabit(val habitId: String, val completed: Boolean) : HabitUiEvent()
    data class DeleteHabit(val habitId: String) : HabitUiEvent()
    data class UpdateHabit(val habitId: String, val title: String, val description: String) : HabitUiEvent()
    object ShowCreateDialog : HabitUiEvent()
    object HideCreateDialog : HabitUiEvent()
    data class ShowEditDialog(val habit: Habit) : HabitUiEvent()
    object HideEditDialog : HabitUiEvent()
    data class UpdateNewHabitTitle(val title: String) : HabitUiEvent()
    data class UpdateNewHabitDescription(val description: String) : HabitUiEvent()
    data class UpdateEditHabitTitle(val title: String) : HabitUiEvent()
    data class UpdateEditHabitDescription(val description: String) : HabitUiEvent()
    object ClearError : HabitUiEvent()
}

