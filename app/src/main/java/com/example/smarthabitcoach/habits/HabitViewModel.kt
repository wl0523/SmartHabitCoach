package com.example.smarthabitcoach.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthabitcoach.domain.model.Habit
import com.example.smarthabitcoach.domain.usecase.CreateHabitUseCase
import com.example.smarthabitcoach.domain.usecase.GetHabitsUseCase
import com.example.smarthabitcoach.domain.usecase.CompleteHabitUseCase
import com.example.smarthabitcoach.domain.usecase.GetStatisticsUseCase
import com.example.smarthabitcoach.habits.ui.HabitUiEvent
import com.example.smarthabitcoach.habits.ui.HabitUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Helper data class to hold UI state components.
 * Used to reduce number of combine() arguments (max 6 Flow support).
 */
private data class UiStateComponents(
    val createDialogVisible: Boolean,
    val newHabitTitle: String,
    val newHabitDescription: String,
    val error: String?,
    val isLoading: Boolean
)

/**
 * ViewModel for Habit management feature.
 * Handles all business logic and state management.
 *
 * Integrates statistics (streak, completion rate) from GetStatisticsUseCase.
 * All calculations happen in Domain layer; ViewModel only combines and exposes state.
 */
@HiltViewModel
class HabitViewModel @Inject constructor(
    private val getHabits: GetHabitsUseCase,
    private val createHabit: CreateHabitUseCase,
    private val completeHabit: CompleteHabitUseCase,
    private val getStatistics: GetStatisticsUseCase
) : ViewModel() {

    // Mutable state for UI-specific fields (dialog, input)
    private val _createDialogVisible = MutableStateFlow(false)
    private val _newHabitTitle = MutableStateFlow("")
    private val _newHabitDescription = MutableStateFlow("")
    private val _error = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(false)

    // Combine domain state (habits + statistics) with UI state into single StateFlow
    val uiState: StateFlow<HabitUiState> = combine(
        getHabits(),
        getStatistics(),
        combine(
            _createDialogVisible,
            _newHabitTitle,
            _newHabitDescription,
            _error,
            _isLoading
        ) { dialogVisible, title, description, error, loading ->
            UiStateComponents(dialogVisible, title, description, error, loading)
        }
    ) { habits, statistics, uiComponents ->
        HabitUiState(
            habits = habits,
            statistics = statistics,
            isLoading = uiComponents.isLoading,
            error = uiComponents.error,
            createDialogVisible = uiComponents.createDialogVisible,
            newHabitTitle = uiComponents.newHabitTitle,
            newHabitDescription = uiComponents.newHabitDescription
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HabitUiState())

    /**
     * Central event handler.
     * All user interactions flow through this function.
     */
    fun onEvent(event: HabitUiEvent) {
        when (event) {
            is HabitUiEvent.CreateHabit -> createNewHabit(event.title, event.description)
            is HabitUiEvent.CompleteHabit -> toggleHabitCompletion(event.habitId, event.completed)
            is HabitUiEvent.DeleteHabit -> deleteHabit(event.habitId)
            HabitUiEvent.ShowCreateDialog -> _createDialogVisible.value = true
            HabitUiEvent.HideCreateDialog -> resetCreateDialog()
            is HabitUiEvent.UpdateNewHabitTitle -> _newHabitTitle.value = event.title
            is HabitUiEvent.UpdateNewHabitDescription -> _newHabitDescription.value = event.description
            HabitUiEvent.ClearError -> _error.value = null
        }
    }

    private fun createNewHabit(title: String, description: String?) {
        if (title.isBlank()) {
            _error.value = "Habit title cannot be empty"
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                val habit = Habit(title = title, description = description)
                createHabit(habit)
                resetCreateDialog()
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to create habit: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun toggleHabitCompletion(habitId: String, completed: Boolean) {
        viewModelScope.launch {
            try {
                completeHabit(habitId, completed)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to update habit: ${e.message}"
            }
        }
    }

    private fun deleteHabit(@Suppress("UNUSED_PARAMETER") habitId: String) {
        // TODO: Implement delete use case when available
    }

    private fun resetCreateDialog() {
        _createDialogVisible.value = false
        _newHabitTitle.value = ""
        _newHabitDescription.value = ""
        _error.value = null
    }
}

