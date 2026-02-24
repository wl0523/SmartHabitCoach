package com.example.smarthabitcoach.data.local

import androidx.room.TypeConverter

/**
 * Room TypeConverters for complex types.
 * Converts Set<String> (completedDates) to/from delimited string for Room storage.
 */
class HabitTypeConverters {
    @TypeConverter
    fun fromCompletedDates(dates: Set<String>): String {
        // Convert Set<String> to comma-delimited string
        return dates.joinToString(",")
    }

    @TypeConverter
    fun toCompletedDates(json: String): Set<String> {
        // Convert comma-delimited string back to Set<String>
        return if (json.isEmpty()) emptySet()
        else json.split(",").toSet()
    }
}


