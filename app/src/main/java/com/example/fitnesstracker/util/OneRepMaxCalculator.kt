package com.example.fitnesstracker.util

import kotlin.math.roundToInt

/**
 * Расчёт одноповторного максимума (1RM) и планирование процентов от максимума.
 */
object OneRepMaxCalculator {

    /**
     * Формула Эпли (Epley formula): 1RM = w * (1 + r / 30).
     * Стандартная формула для большинства силовых приложений.
     */
    fun calculate1RmEpley(weightKg: Float, reps: Int): Float {
        if (weightKg <= 0f || reps <= 0 || weightKg.isNaN() || weightKg.isInfinite()) return 0f
        if (reps == 1) return weightKg
        val safeReps = minOf(reps, 100)
        val result = weightKg * (1f + safeReps / 30f)
        return (result * 10f).roundToInt() / 10f
    }

    /**
     * Формула Бржицки (Brzycki formula): 1RM = w / (1.0278 - 0.0278 * r).
     */
    fun calculate1RmBrzycki(weightKg: Float, reps: Int): Float {
        if (weightKg <= 0f || reps <= 0 || weightKg.isNaN() || weightKg.isInfinite()) return 0f
        if (reps == 1) return weightKg
        if (reps >= 36) return calculate1RmEpley(weightKg, reps)
        val result = weightKg / (1.0278f - 0.0278f * reps)
        return (result * 10f).roundToInt() / 10f
    }

    /**
     * Проценты от 1RM и соответствующее ориентировочное число повторений.
     */
    fun percentagesTable(oneRm: Float): List<Triple<Int, Int, Float>> {
        if (oneRm <= 0f || oneRm.isNaN() || oneRm.isInfinite()) return emptyList()
        val table = listOf(
            Triple(100, 1, 1.0f),
            Triple(95, 2, 0.95f),
            Triple(90, 4, 0.90f),
            Triple(85, 6, 0.85f),
            Triple(80, 8, 0.80f),
            Triple(75, 10, 0.75f),
            Triple(70, 12, 0.70f)
        )
        return table.map { (pct, targetReps, factor) ->
            val weight = (oneRm * factor * 2f).roundToInt() / 2f
            Triple(pct, targetReps, weight)
        }
    }
}
