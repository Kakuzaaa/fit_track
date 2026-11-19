package com.example.fitnesstracker.domain.model

/**
 * Доменная модель записи веса тела в дневнике взвешиваний.
 */
data class WeightLog(
    val id: Long,
    val dateEpochDay: Long,
    val weightKg: Double,
    val timestamp: Long
)
