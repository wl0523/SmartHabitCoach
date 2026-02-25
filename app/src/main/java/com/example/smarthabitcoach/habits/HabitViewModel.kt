package com.example.smarthabitcoach.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthabitcoach.domain.model.DailyNudge
import com.example.smarthabitcoach.domain.model.Habit
import com.example.smarthabitcoach.domain.model.WeeklyInsight
import com.example.smarthabitcoach.domain.usecase.CompleteHabitUseCase
import com.example.smarthabitcoach.domain.usecase.CreateHabitUseCase
import com.example.smarthabitcoach.domain.usecase.DeleteHabitUseCase
import com.example.smarthabitcoach.domain.usecase.DetectAtRiskHabitsUseCase
import com.example.smarthabitcoach.domain.usecase.GenerateDailyNudgeUseCase
import com.example.smarthabitcoach.domain.usecase.GenerateWeeklyInsightUseCase
import com.example.smarthabitcoach.domain.usecase.GetHabitsUseCase
import com.example.smarthabitcoach.domain.usecase.GetStatisticsUseCase
import com.example.smarthabitcoach.domain.usecase.UpdateHabitUseCase
import com.example.smarthabitcoach.habits.ui.HabitUiEvent
import com.example.smarthabitcoach.habits.ui.HabitUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Habit management feature.
 * Combines 5 MutableStateFlow sources + domain Flows into a single StateFlow<HabitUiState>.
 */
@HiltViewModel
class HabitViewModel @Inject constructor(
    private val getHabits: GetHabitsUseCase,
    private val createHabit: CreateHabitUseCase,
    private val completeHabit: CompleteHabitUseCase,
    private val deleteHabit: DeleteHabitUseCase,
    private val updateHabit: UpdateHabitUseCase,
    private val getStatistics: GetStatisticsUseCase,
    private val detectAtRiskHabits: DetectAtRiskHabitsUseCase,
    private val generateWeeklyInsight: GenerateWeeklyInsightUseCase,
    private val generateDailyNudge: GenerateDailyNudgeUseCase
) : ViewModel() {

    private val _createDialogVisible = MutableStateFlow(false)
    private val _newHabitTitle       = MutableStateFlow("")
    private val _newHabitDescription = MutableStateFlow("")
    private val _editDialogHabit     = MutableStateFlow<Habit?>(null)
    private val _editHabitTitle      = MutableStateFlow("")
    private val _editHabitDescription = MutableStateFlow("")
    private val _error               = MutableStateFlow<String?>(null)
    private val _isLoading           = MutableStateFlow(false)

    // AI content — Room-cached, loaded once per session
    private val _weeklyInsight = MutableStateFlow<WeeklyInsight?>(null)
    private val _dailyNudge    = MutableStateFlow<DailyNudge?>(null)

    init {
        loadWeeklyInsight()
        loadDailyNudge()
    }

    private fun loadWeeklyInsight() {
        viewModelScope.launch {
            try {
                val habits = getHabits().first()
                val stats  = getStatistics().first()
                _weeklyInsight.value = generateWeeklyInsight(habits, stats)
            } catch (_: Exception) { /* non-critical */ }
        }
    }

    private fun loadDailyNudge() {
        viewModelScope.launch {
            try {
                val habits = getHabits().first()
                val stats  = getStatistics().first()
                _dailyNudge.value = generateDailyNudge(habits, stats)
            } catch (_: Exception) { /* non-critical */ }
        }
    }

    // Single StateFlow combining all state sources
    val uiState: StateFlow<HabitUiState> = combine(
        getHabits(),
        getStatistics(),
        combine(
            _createDialogVisible,
            _newHabitTitle,
            _newHabitDescription,
            _editDialogHabit,
            _editHabitTitle,
        ) { dialogVisible, title, desc, editHabit, editTitle ->
            Triple(dialogVisible, title to desc, editHabit to editTitle)
        },
        combine(_editHabitDescription, _error, _isLoading) { editDesc, error, loading ->
            Triple(editDesc, error, loading)
        },
        combine(_dailyNudge, _weeklyInsight) { nudge, insight -> nudge to insight }
    ) { habits, statistics, uiTop, uiBottom, aiContent ->
        val (dialogVisible, createFields, editFields) = uiTop
        val (createTitle, createDesc) = createFields
        val (editHabit, editTitle) = editFields
        val (editDesc, error, loading) = uiBottom
        val (dailyNudge, weeklyInsight) = aiContent
        HabitUiState(
            habits           = habits,
            statistics       = statistics,
            atRiskHabits     = detectAtRiskHabits(habits),
            dailyNudge       = dailyNudge,
            weeklyInsight    = weeklyInsight,
            isLoading        = loading,
            error            = error,
            createDialogVisible  = dialogVisible,
            newHabitTitle        = createTitle,
            newHabitDescription  = createDesc,
            editDialogHabit      = editHabit,
            editHabitTitle       = editTitle,
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

