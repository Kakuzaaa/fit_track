package com.example.fitnesstracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Запись в дневнике веса тела.
 */
@Entity(tableName = "weight_logs")
data class WeightLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateEpochDay: Long,
    val weightKg: Double,
    val timestamp: Long = System.currentTimeMillis()
)
