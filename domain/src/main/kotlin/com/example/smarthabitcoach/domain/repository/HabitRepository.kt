package com.example.smarthabitcoach.domain.repository

import com.example.smarthabitcoach.domain.model.Habit
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Habits. Implemented in :data module.
 */
interface HabitRepository {
    fun getHabits(): Flow<List<Habit>>
    fun observeHabits(): Flow<List<Habit>>
    suspend fun getHabitById(id: String): Habit?
    suspend fun createHabit(habit: Habit): String
    suspend fun updateHabit(habit: Habit)
    suspend fun deleteHabit(habitId: String)
    suspend fun completeHabit(habitId: String, completed: Boolean)
}
