package com.example.fitnesstracker.domain.repository

import com.example.fitnesstracker.domain.model.WeightLog
import kotlinx.coroutines.flow.Flow

interface WeightRepository {
    fun observeAll(): Flow<List<WeightLog>>
    fun observeLatest(): Flow<WeightLog?>
    suspend fun addWeight(weightKg: Double, dateEpochDay: Long): Long
    suspend fun deleteWeight(id: Long)
}
