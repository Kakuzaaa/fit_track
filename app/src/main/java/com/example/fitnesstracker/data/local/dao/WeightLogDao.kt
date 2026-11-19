package com.example.fitnesstracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fitnesstracker.data.local.entity.WeightLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: WeightLogEntity): Long

    @Query("SELECT * FROM weight_logs ORDER BY dateEpochDay ASC, timestamp ASC")
    fun observeAll(): Flow<List<WeightLogEntity>>

    @Query("SELECT * FROM weight_logs ORDER BY dateEpochDay DESC, timestamp DESC LIMIT 1")
    fun observeLatest(): Flow<WeightLogEntity?>

    @Query("DELETE FROM weight_logs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM weight_logs")
    suspend fun count(): Int
}
