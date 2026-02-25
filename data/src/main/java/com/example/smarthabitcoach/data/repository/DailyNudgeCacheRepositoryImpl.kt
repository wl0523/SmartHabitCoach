package com.example.smarthabitcoach.data.repository

import com.example.smarthabitcoach.data.local.DailyNudgeDao
import com.example.smarthabitcoach.data.mapper.DailyNudgeMapper
import com.example.smarthabitcoach.domain.model.DailyNudge
import com.example.smarthabitcoach.domain.repository.DailyNudgeCacheRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class DailyNudgeCacheRepositoryImpl @Inject constructor(
    private val dao: DailyNudgeDao
) : DailyNudgeCacheRepository {

    private val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override suspend fun getNudgeForDate(date: LocalDate): DailyNudge? {
        // Evict stale cache (>30 days) on each read
        val cutoff = date.minusDays(30).format(dateFmt)
        dao.deleteOlderThan(cutoff)
        return dao.getByDate(date.format(dateFmt))?.let {
            DailyNudgeMapper.toDomain(it)
        }
    }

    override suspend fun saveNudge(nudge: DailyNudge) {
        dao.insert(DailyNudgeMapper.toEntity(nudge))
    }
}

