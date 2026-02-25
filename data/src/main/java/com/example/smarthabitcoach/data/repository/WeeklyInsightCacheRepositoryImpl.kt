package com.example.smarthabitcoach.data.repository

import com.example.smarthabitcoach.data.local.WeeklyInsightDao
import com.example.smarthabitcoach.data.mapper.WeeklyInsightMapper
import com.example.smarthabitcoach.domain.model.WeeklyInsight
import com.example.smarthabitcoach.domain.repository.WeeklyInsightCacheRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class WeeklyInsightCacheRepositoryImpl @Inject constructor(
    private val dao: WeeklyInsightDao
) : WeeklyInsightCacheRepository {

    private val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override suspend fun getInsightForWeek(weekOf: LocalDate): WeeklyInsight? {
        val cutoff = weekOf.minusDays(30).format(dateFmt)
        dao.deleteOlderThan(cutoff)
        return dao.getByWeek(weekOf.format(dateFmt))?.let {
            WeeklyInsightMapper.toDomain(it)
        }
    }

    override suspend fun saveInsight(insight: WeeklyInsight) {
        dao.insert(WeeklyInsightMapper.toEntity(insight))
    }
}

