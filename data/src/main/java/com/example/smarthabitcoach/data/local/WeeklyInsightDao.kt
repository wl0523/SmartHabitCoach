package com.example.smarthabitcoach.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WeeklyInsightDao {

    @Query("SELECT * FROM weekly_insights WHERE weekOf = :weekOf LIMIT 1")
    suspend fun getByWeek(weekOf: String): WeeklyInsightEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: WeeklyInsightEntity)

    /** Evict insights older than 30 days to keep the cache lean. */
    @Query("DELETE FROM weekly_insights WHERE weekOf < :cutoff")
    suspend fun deleteOlderThan(cutoff: String)
}

