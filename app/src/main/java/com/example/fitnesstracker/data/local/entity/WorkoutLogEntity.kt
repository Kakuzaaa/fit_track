package com.example.fitnesstracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Запись о выполненном подходе / кардио-сессии.
 */
@Entity(
    tableName = "workout_logs",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("exerciseId"), Index("dateEpochDay")]
)
data class WorkoutLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val exerciseId: Long,
    /** Имя и хват на момент записи. */
    val exerciseNameSnapshot: String,
    /** Дата в формате LocalDate.toEpochDay(). */
    val dateEpochDay: Long,
    /** Точное время записи в миллисекундах. */
    val timestamp: Long,
    val reps: Int? = null,
    val weightKg: Float? = null,
    val durationMinutes: Float? = null,
    val speedKmh: Float? = null,
    val caloriesBurned: Int
)
