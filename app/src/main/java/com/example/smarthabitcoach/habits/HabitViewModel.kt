package com.example.smarthabitcoach.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthabitcoach.domain.model.Habit
import com.example.smarthabitcoach.domain.usecase.CreateHabitUseCase
import com.example.smarthabitcoach.domain.usecase.GetHabitsUseCase
import com.example.smarthabitcoach.domain.usecase.CompleteHabitUseCase
import com.example.smarthabitcoach.domain.usecase.DeleteHabitUseCase
import com.example.smarthabitcoach.domain.usecase.GetStatisticsUseCase
import com.example.smarthabitcoach.domain.usecase.UpdateHabitUseCase
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
    val editDialogHabit: com.example.smarthabitcoach.domain.model.Habit?,
    val editHabitTitle: String,
    val editHabitDescription: String,
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
    private val deleteHabit: DeleteHabitUseCase,
    private val updateHabit: UpdateHabitUseCase,
    private val getStatistics: GetStatisticsUseCase
) : ViewModel() {

    // Mutable state for UI-specific fields (dialog, input)
    private val _createDialogVisible = MutableStateFlow(false)
    private val _newHabitTitle = MutableStateFlow("")
    private val _newHabitDescription = MutableStateFlow("")
    private val _editDialogHabit = MutableStateFlow<Habit?>(null)
    private val _editHabitTitle = MutableStateFlow("")
    private val _editHabitDescription = MutableStateFlow("")
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
            _editDialogHabit,
            _editHabitTitle,
        ) { dialogVisible, title, description, editHabit, editTitle ->
            Triple(dialogVisible, title to description, editHabit to editTitle)
        },
        combine(_editHabitDescription, _error, _isLoading) { editDesc, error, loading ->
            Triple(editDesc, error, loading)
        }
    ) { habits, statistics, uiTop, uiBottom ->
        val (dialogVisible, createFields, editFields) = uiTop
        val (createTitle, createDesc) = createFields
        val (editHabit, editTitle) = editFields
        val (editDesc, error, loading) = uiBottom
        HabitUiState(
            habits = habits,
            statistics = statistics,
            isLoading = loading,
            error = error,
            createDialogVisible = dialogVisible,
            newHabitTitle = createTitle,
            newHabitDescription = createDesc,
            editDialogHabit = editHabit,
            editHabitTitle = editTitle,
            editHabitDescription = editDesc
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
            is HabitUiEvent.DeleteHabit -> performDeleteHabit(event.habitId)
            is HabitUiEvent.UpdateHabit -> performUpdateHabit(event.habitId, event.title, event.description)
            HabitUiEvent.ShowCreateDialog -> _createDialogVisible.value = true
            HabitUiEvent.HideCreateDialog -> resetCreateDialog()
            is HabitUiEvent.ShowEditDialog -> {
                _editDialogHabit.value = event.habit
                _editHabitTitle.value = event.habit.title
                _editHabitDescription.value = event.habit.description ?: ""
            }
            HabitUiEvent.HideEditDialog -> resetEditDialog()
            is HabitUiEvent.UpdateNewHabitTitle -> _newHabitTitle.value = event.title
            is HabitUiEvent.UpdateNewHabitDescription -> _newHabitDescription.value = event.description
            is HabitUiEvent.UpdateEditHabitTitle -> _editHabitTitle.value = event.title
            is HabitUiEvent.UpdateEditHabitDescription -> _editHabitDescription.value = event.description
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

    private fun performDeleteHabit(habitId: String) {
        viewModelScope.launch {
            try {
                deleteHabit(habitId)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to delete habit: ${e.message}"
            }
        }
    }

    private fun performUpdateHabit(habitId: String, title: String, description: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                updateHabit(habitId, title, description)
                resetEditDialog()
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to update habit: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun resetEditDialog() {
        _editDialogHabit.value = null
        _editHabitTitle.value = ""
        _editHabitDescription.value = ""
    }

    private fun resetCreateDialog() {
        _createDialogVisible.value = false
        _newHabitTitle.value = ""
        _newHabitDescription.value = ""
        _error.value = null
    }
}

