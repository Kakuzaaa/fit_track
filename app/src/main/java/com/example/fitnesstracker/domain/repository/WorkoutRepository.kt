package com.example.fitnesstracker.domain.repository

import com.example.fitnesstracker.domain.model.BestExerciseStats
import com.example.fitnesstracker.domain.model.DayStats
import com.example.fitnesstracker.domain.model.WorkoutLog
import com.example.fitnesstracker.domain.model.WorkoutTemplate
import kotlinx.coroutines.flow.Flow

/**
 * Доступ к журналу тренировок и шаблонам программ.
 */
interface WorkoutRepository {

    fun observeLogsForDay(dateEpochDay: Long): Flow<List<WorkoutLog>>

    fun observeLogsForExercise(exerciseId: Long): Flow<List<WorkoutLog>>

    suspend fun getLogsForExercise(exerciseId: Long): List<WorkoutLog>

    fun observeDayStats(): Flow<List<DayStats>>

    /** Лучший результат по каждому упражнению за всю историю (нормативы). */
    suspend fun getBestStats(): List<BestExerciseStats>

    suspend fun getAllLogs(): List<WorkoutLog>

    fun observeAllLogs(): Flow<List<WorkoutLog>>

    fun searchLogs(query: String): Flow<List<WorkoutLog>>

    /** Последний использованный вес гантели для упражнения (для быстрого повтора). */
    suspend fun getLastUsedWeight(exerciseId: Long): Float?

    suspend fun insert(log: WorkoutLog): Long

    suspend fun update(log: WorkoutLog)

    suspend fun delete(log: WorkoutLog)

    suspend fun clearAllLogs()

    // ── Шаблоны тренировок ──────────────────────────────────────
    fun observeTemplates(): Flow<List<WorkoutTemplate>>

    suspend fun getTemplate(id: Long): WorkoutTemplate?

    suspend fun saveTemplate(template: WorkoutTemplate): Long

    suspend fun deleteTemplate(id: Long)
}
