package com.example.fitnesstracker.util

import kotlin.math.roundToInt

/**
 * Утилита примерного расчёта расхода калорий.
 *
 * Опорный вес тела пользователя — [DEFAULT_BODY_WEIGHT_KG] кг (настраивается в профиле).
 */
object CalorieCalculator {

    const val DEFAULT_BODY_WEIGHT_KG = 75.0

    /** MET для статических упражнений типа планки. */
    const val PLANK_MET = 3.8

    /**
     * Опорная таблица «скорость → MET» (Compendium of Physical Activities,
     * усреднённые значения). Якорные точки заданы в ТЗ:
     * 4 км/ч ≈ 3.0, 6 км/ч ≈ 4.3, 8 км/ч ≈ 8.3, 10 км/ч ≈ 9.8, 12 км/ч ≈ 11.5.
     */
    private val metSpeedTable: List<Pair<Double, Double>> = listOf(
        2.0 to 2.0,   // очень медленная ходьба
        3.0 to 2.5,
        4.0 to 3.0,   // медленная ходьба
        5.5 to 3.8,   // средняя ходьба
        6.0 to 4.3,   // быстрая ходьба
        6.5 to 5.0,   // очень быстрая ходьба
        7.0 to 6.0,   // джоггинг
        8.0 to 8.3,   // бег трусцой
        10.0 to 9.8,  // средний темп бега
        12.0 to 11.5, // быстрый бег
        14.0 to 13.2,
        16.0 to 15.0  // спринт
    )

    /**
     * MET для произвольной скорости: линейная интерполяция между
     * якорными точками таблицы; за пределами таблицы значения клампятся.
     */
    fun metForSpeed(speedKmh: Double): Double {
        if (speedKmh <= metSpeedTable.first().first) return metSpeedTable.first().second
        if (speedKmh >= metSpeedTable.last().first) return metSpeedTable.last().second
        for (i in 0 until metSpeedTable.lastIndex) {
            val (speed1, met1) = metSpeedTable[i]
            val (speed2, met2) = metSpeedTable[i + 1]
            if (speedKmh in speed1..speed2) {
                val t = (speedKmh - speed1) / (speed2 - speed1)
                return met1 + (met2 - met1) * t
            }
        }
        return metSpeedTable.last().second
    }

    /**
     * Кардио (ходьба / бег): Kcal = MET × вес(кг) × время(часы).
     */
    fun cardioKcal(bodyWeightKg: Double, durationMinutes: Double, speedKmh: Double): Int {
        if (durationMinutes <= 0.0 || speedKmh <= 0.0) return 0
        val kcal = metForSpeed(speedKmh) * bodyWeightKg * (durationMinutes / 60.0)
        return kcal.roundToInt().coerceAtLeast(1)
    }

    /**
     * Длительность (в минутах), рассчитанная из расстояния и скорости:
     * t = S / V × 60.
     */
    fun minutesFromDistance(distanceKm: Double, speedKmh: Double): Double =
        if (speedKmh > 0.0 && distanceKm > 0.0) distanceKm / speedKmh * 60.0 else 0.0

    /**
     * Силовые упражнения на повторения:
     * ккал = (базовая энергоёмкость 1 повтора × поправка на вес тела
     * + добавка за вес гантелей) × число повторений.
     *
     * @param kcalPerRepReference базовая энергоёмкость 1 повторения при весе 75 кг.
     * @param extraWeightKg вес гантелей (или null, если упражнение без отягощения).
     */
    fun repsKcal(
        bodyWeightKg: Double,
        reps: Int,
        kcalPerRepReference: Double,
        extraWeightKg: Float? = null
    ): Int {
        if (reps <= 0) return 0
        val bodyWeightFactor = bodyWeightKg / DEFAULT_BODY_WEIGHT_KG
        // ~0.005 ккал за 1 повторение на каждый кг отягощения (механическая работа с КПД мышц).
        val extraPerRep = (extraWeightKg ?: 0f) * 0.005
        val kcal = (kcalPerRepReference * bodyWeightFactor + extraPerRep) * reps
        return kcal.roundToInt().coerceAtLeast(1)
    }

    /**
     * Статические удержания (планка): Kcal = MET × вес(кг) × время(часы).
     */
    fun staticHoldKcal(bodyWeightKg: Double, durationMinutes: Double, met: Double = PLANK_MET): Int {
        if (durationMinutes <= 0.0) return 0
        val kcal = met * bodyWeightKg * (durationMinutes / 60.0)
        return kcal.roundToInt().coerceAtLeast(1)
    }
}
