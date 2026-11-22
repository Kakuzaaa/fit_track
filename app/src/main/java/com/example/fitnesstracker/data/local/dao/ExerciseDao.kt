package com.example.fitnesstracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.fitnesstracker.data.local.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    @Query("SELECT * FROM exercises ORDER BY category, name")
    fun observeAll(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getById(id: Long): ExerciseEntity?

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(exercises: List<ExerciseEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(exercise: ExerciseEntity): Long

    @Update
    suspend fun update(exercise: ExerciseEntity)

    @Delete
    suspend fun delete(exercise: ExerciseEntity)
}
