package com.example.smarthabitcoach.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey val id: String,
    @ColumnInfo("title") val title: String,
    @ColumnInfo("description") val description: String?,
    @ColumnInfo("is_completed") val isCompleted: Boolean,
    @ColumnInfo("created_at") val createdAt: Long,
    @ColumnInfo("completed_dates") val completedDates: Set<String> = emptySet(), // Stored as TEXT (JSON)
)

