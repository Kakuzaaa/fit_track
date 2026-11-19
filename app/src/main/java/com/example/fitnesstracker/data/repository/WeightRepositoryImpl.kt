package com.example.fitnesstracker.data.repository

import com.example.fitnesstracker.data.local.dao.WeightLogDao
import com.example.fitnesstracker.data.local.entity.WeightLogEntity
import com.example.fitnesstracker.domain.model.WeightLog
import com.example.fitnesstracker.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WeightRepositoryImpl(private val dao: WeightLogDao) : WeightRepository {

    override fun observeAll(): Flow<List<WeightLog>> = dao.observeAll().map { list ->
        list.map { WeightLog(it.id, it.dateEpochDay, it.weightKg, it.timestamp) }
    }

    override fun observeLatest(): Flow<WeightLog?> = dao.observeLatest().map { entity ->
        entity?.let { WeightLog(it.id, it.dateEpochDay, it.weightKg, it.timestamp) }
    }

    override suspend fun addWeight(weightKg: Double, dateEpochDay: Long): Long {
        return dao.insert(
            WeightLogEntity(
                dateEpochDay = dateEpochDay,
                weightKg = weightKg,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    override suspend fun deleteWeight(id: Long) {
        dao.deleteById(id)
    }
}
