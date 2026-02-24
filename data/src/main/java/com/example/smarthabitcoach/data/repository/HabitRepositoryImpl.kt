package com.example.smarthabitcoach.data.repository

import com.example.smarthabitcoach.data.local.HabitDao
import com.example.smarthabitcoach.data.mapper.HabitMapper
import com.example.smarthabitcoach.domain.model.Habit
import com.example.smarthabitcoach.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HabitRepositoryImpl @Inject constructor(
    private val dao: HabitDao
) : HabitRepository {
    override fun observeHabits(): Flow<List<Habit>> = dao.observeAll().map { list ->
        list.map { HabitMapper.toDomain(it) }
    }

    override suspend fun getHabitById(id: String): Habit? = dao.getById(id)?.let { HabitMapper.toDomain(it) }

    override suspend fun createHabit(habit: Habit): String {
        dao.insert(HabitMapper.toEntity(habit))
        return habit.id
    }

    override suspend fun updateHabit(habit: Habit) {
        dao.update(HabitMapper.toEntity(habit))
    }

    override suspend fun deleteHabit(id: String) {
        dao.deleteById(id)
    }
}

