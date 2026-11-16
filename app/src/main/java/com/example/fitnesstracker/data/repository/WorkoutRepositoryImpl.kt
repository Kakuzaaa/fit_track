package com.example.fitnesstracker.data.repository

import com.example.fitnesstracker.data.local.dao.WorkoutLogDao
import com.example.fitnesstracker.data.mapper.toDomain
import com.example.fitnesstracker.data.mapper.toEntity
import com.example.fitnesstracker.domain.model.DayStats
import com.example.fitnesstracker.domain.model.WorkoutLog
import com.example.fitnesstracker.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WorkoutRepositoryImpl(private val dao: WorkoutLogDao) : WorkoutRepository {

    override fun observeLogsForDay(dateEpochDay: Long): Flow<List<WorkoutLog>> =
        dao.observeLogsForDay(dateEpochDay).map { list -> list.map { it.toDomain() } }

    override fun observeDayStats(): Flow<List<DayStats>> =
        dao.observeDayStats().map { list -> list.map { it.toDomain() } }

    override suspend fun getAllLogs(): List<WorkoutLog> =
        dao.getAllLogs().map { it.toDomain() }

    override suspend fun getLastUsedWeight(exerciseId: Long): Float? =
        dao.getLastUsedWeight(exerciseId)

    override suspend fun insert(log: WorkoutLog): Long =
        dao.insert(log.toEntity())

    override suspend fun update(log: WorkoutLog) {
        dao.update(log.toEntity())
    }

    override suspend fun delete(log: WorkoutLog) {
        dao.delete(log.toEntity())
    }

    override suspend fun clearAllLogs() {
        dao.clearAll()
    }
}
