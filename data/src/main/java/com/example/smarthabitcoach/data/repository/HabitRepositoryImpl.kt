package com.example.smarthabitcoach.data.repository

import com.example.smarthabitcoach.data.local.HabitDao
import com.example.smarthabitcoach.data.mapper.HabitMapper
import com.example.smarthabitcoach.domain.model.Habit
import com.example.smarthabitcoach.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class HabitRepositoryImpl @Inject constructor(
    private val dao: HabitDao
) : HabitRepository {

    private val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override fun getHabits(): Flow<List<Habit>> = observeHabits()

    override fun observeHabits(): Flow<List<Habit>> = dao.observeAll().map { list ->
        list.map { HabitMapper.toDomain(it) }
    }

    override suspend fun getHabitById(id: String): Habit? =
        dao.getById(id)?.let { HabitMapper.toDomain(it) }

    override suspend fun createHabit(habit: Habit): String {
        dao.insert(HabitMapper.toEntity(habit))
        return habit.id
    }

    override suspend fun updateHabit(habit: Habit) {
        dao.update(HabitMapper.toEntity(habit))
    }

    override suspend fun deleteHabit(habitId: String) {
        dao.deleteById(habitId)
    }

    override suspend fun completeHabit(habitId: String, completed: Boolean) {
        val entity = dao.getById(habitId) ?: return
        val today = LocalDate.now().format(dateFmt)
        val updatedDates = if (completed) entity.completedDates + today
                           else entity.completedDates - today
        dao.update(entity.copy(
            isCompleted = completed,
            completedDates = updatedDates
        ))
    }
}
