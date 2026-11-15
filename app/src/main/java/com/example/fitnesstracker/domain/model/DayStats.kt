package com.example.fitnesstracker.domain.model

/**
 * Агрегированная статистика за один день — для точек-индикаторов
 * календаря и ленты истории.
 */
data class DayStats(
    val dateEpochDay: Long,
    val totalCalories: Int,
    val entries: Int
)
