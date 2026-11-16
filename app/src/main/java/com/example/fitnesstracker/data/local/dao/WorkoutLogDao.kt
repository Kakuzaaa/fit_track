package com.example.fitnesstracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.fitnesstracker.data.local.entity.WorkoutLogEntity
import kotlinx.coroutines.flow.Flow

/**
 * POJO для агрегированной статистики за день (результат GROUP BY-запроса).
 */
data class DayStatsRow(
    val dateEpochDay: Long,
    val totalCalories: Int,
    val totalReps: Int,
    val entries: Int
)

@Dao
interface WorkoutLogDao {

    @Query("SELECT * FROM workout_logs WHERE dateEpochDay = :dateEpochDay ORDER BY timestamp DESC")
    fun observeLogsForDay(dateEpochDay: Long): Flow<List<WorkoutLogEntity>>

    @Query(
        "SELECT dateEpochDay, SUM(caloriesBurned) AS totalCalories, " +
            "COALESCE(SUM(reps), 0) AS totalReps, COUNT(*) AS entries " +
            "FROM workout_logs GROUP BY dateEpochDay ORDER BY dateEpochDay DESC"
    )
    fun observeDayStats(): Flow<List<DayStatsRow>>

    @Query("SELECT * FROM workout_logs ORDER BY timestamp DESC")
    suspend fun getAllLogs(): List<WorkoutLogEntity>

    @Query(
        "SELECT weightKg FROM workout_logs WHERE exerciseId = :exerciseId AND weightKg IS NOT NULL " +
            "ORDER BY timestamp DESC LIMIT 1"
    )
    suspend fun getLastUsedWeight(exerciseId: Long): Float?

    @Insert
    suspend fun insert(log: WorkoutLogEntity): Long

    @Update
    suspend fun update(log: WorkoutLogEntity)

    @Delete
    suspend fun delete(log: WorkoutLogEntity)

    @Query("DELETE FROM workout_logs")
    suspend fun clearAll()
}
