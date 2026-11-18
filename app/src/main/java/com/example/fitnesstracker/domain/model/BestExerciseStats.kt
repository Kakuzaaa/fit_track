package com.example.fitnesstracker.domain.model

/**
 * Лучший результат в упражнении за всю историю (для нормативов «уровня атлета»).
 */
data class BestExerciseStats(
    val exerciseId: Long,
    val bestReps: Int,
    /** Лучший результат по длительности в секундах. */
    val bestSeconds: Int,
    /** Максимальный поднятый вес (для упражнений с отягощением). */
    val bestWeightKg: Float? = null,
    /** Лучший расчётный одноповторный максимум (1RM). */
    val best1RmKg: Float? = null
)
