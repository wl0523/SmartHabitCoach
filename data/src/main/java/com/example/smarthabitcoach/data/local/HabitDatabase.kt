package com.example.smarthabitcoach.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [HabitEntity::class, WeeklyInsightEntity::class, DailyNudgeEntity::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(HabitTypeConverters::class)
abstract class HabitDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun weeklyInsightDao(): WeeklyInsightDao
    abstract fun dailyNudgeDao(): DailyNudgeDao
}

