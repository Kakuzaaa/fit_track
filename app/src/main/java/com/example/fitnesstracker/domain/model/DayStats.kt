package com.example.fitnesstracker.domain.model

/**
 * Агрегированная статистика за один день — для точек-индикаторов
 * календаря, ленты истории и недельного графика.
 */
data class DayStats(
    val dateEpochDay: Long,
    val totalCalories: Int,
    val totalReps: Int,
    val entries: Int
)
