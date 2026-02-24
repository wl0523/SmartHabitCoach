package com.example.smarthabitcoach.domain.repository

import com.example.smarthabitcoach.domain.model.Habit
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Habits. Implemented in :data module.
 */
interface HabitRepository {
    fun observeHabits(): Flow<List<Habit>>
    suspend fun getHabitById(id: String): Habit?
    suspend fun createHabit(habit: Habit): String
    suspend fun updateHabit(habit: Habit)
    suspend fun deleteHabit(id: String)
}

