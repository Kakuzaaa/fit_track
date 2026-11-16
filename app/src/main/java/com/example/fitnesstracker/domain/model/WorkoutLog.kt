package com.example.fitnesstracker.domain.model

/**
 * Запись о выполненном подходе / кардио-сессии.
 *
 * [exerciseNameSnapshot] сохраняет имя и хват на момент записи, чтобы
 * история оставалась корректной даже после правки справочника.
 */
data class WorkoutLog(
    val id: Long = 0L,
    val exerciseId: Long,
    val exerciseNameSnapshot: String,
    /** Дата в формате LocalDate.toEpochDay() — удобная группировка по суткам. */
    val dateEpochDay: Long,
    /** Точное время записи в миллисекундах. */
    val timestamp: Long,
    val reps: Int? = null,
    val weightKg: Float? = null,
    /** Длительность в минутах (для кардио — необязательна). */
    val durationMinutes: Float? = null,
    val speedKmh: Float? = null,
    /** Пройденное расстояние в км (для кардио). */
    val distanceKm: Float? = null,
    val caloriesBurned: Int
)
