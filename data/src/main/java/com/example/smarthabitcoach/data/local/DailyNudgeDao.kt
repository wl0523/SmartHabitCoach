package com.example.smarthabitcoach.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DailyNudgeDao {

    @Query("SELECT * FROM daily_nudges WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: String): DailyNudgeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DailyNudgeEntity)

    /** Evict nudges older than 30 days to keep the cache lean. */
    @Query("DELETE FROM daily_nudges WHERE date < :cutoff")
    suspend fun deleteOlderThan(cutoff: String)
}

