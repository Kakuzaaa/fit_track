package com.example.fitnesstracker.domain.repository

import com.example.fitnesstracker.domain.model.BestExerciseStats
import com.example.fitnesstracker.domain.model.DayStats
import com.example.fitnesstracker.domain.model.WorkoutLog
import kotlinx.coroutines.flow.Flow

/**
 * Доступ к журналу тренировок.
 */
interface WorkoutRepository {

    fun observeLogsForDay(dateEpochDay: Long): Flow<List<WorkoutLog>>

    fun observeDayStats(): Flow<List<DayStats>>

    /** Лучший результат по каждому упражнению за всю историю (нормативы). */
    suspend fun getBestStats(): List<BestExerciseStats>

    suspend fun getAllLogs(): List<WorkoutLog>

    /** Последний использованный вес гантели для упражнения (для быстрого повтора). */
    suspend fun getLastUsedWeight(exerciseId: Long): Float?

    suspend fun insert(log: WorkoutLog): Long

    suspend fun update(log: WorkoutLog)

    suspend fun delete(log: WorkoutLog)

    suspend fun clearAllLogs()
}
