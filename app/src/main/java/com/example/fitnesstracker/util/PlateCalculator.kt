package com.example.fitnesstracker.util

import androidx.compose.ui.graphics.Color

/**
 * Блин на штангу с весом, количеством на одну сторону, олимпийским цветом
 * и относительной высотой (пропорциональной диаметру диска).
 */
data class PlateItem(
    val weightKg: Float,
    val count: Int,
    val color: Color,
    val labelColor: Color,
    val heightFraction: Float // 1.0 = полный олимпийский диаметр 450 мм
)

data class PlateCalculationResult(
    val targetWeightKg: Float,
    val barWeightKg: Float,
    val weightPerSideKg: Float,
    val platesPerSide: List<PlateItem>,
    val totalAchievedWeightKg: Float,
    val isExact: Boolean
)

object PlateCalculator {

    // Стандартные олимпийские веса дисков по международным стандартам IWF/IPF
    private val STANDARD_PLATES = listOf(
        Triple(25f, Color(0xFFE53935), 1.00f), // Красный (450 мм)
        Triple(20f, Color(0xFF1E88E5), 1.00f), // Синий (450 мм)
        Triple(15f, Color(0xFFFDD835), 0.90f), // Жёлтый (400 мм)
        Triple(10f, Color(0xFF43A047), 0.78f), // Зелёный (350 мм)
        Triple(5f, Color(0xFFF5F5F5), 0.65f),  // Белый (250 мм)
        Triple(2.5f, Color(0xFF424242), 0.55f),// Чёрный (210 мм)
        Triple(1.25f, Color(0xFF90A4AE), 0.45f),// Серебристый (160 мм)
        Triple(0.5f, Color(0xFFB0BEC5), 0.38f) // Микро (130 мм)
    )

    /**
     * Рассчитывает оптимальный набор дисков на каждую сторону штанги.
     */
    fun calculate(
        targetWeightKg: Float,
        barWeightKg: Float = 20f,
        availablePlates: List<Float>? = null
    ): PlateCalculationResult {
        val neededPerSide = maxOf(0f, (targetWeightKg - barWeightKg) / 2f)
        var remaining = neededPerSide
        val resultPlates = mutableListOf<PlateItem>()

        val platesToUse = if (availablePlates != null) {
            STANDARD_PLATES.filter { it.first in availablePlates }
        } else {
            STANDARD_PLATES
        }

        for ((plateWeight, color, heightFraction) in platesToUse) {
            if (remaining <= 0.001f) break
            val count = (remaining / plateWeight).toInt()
            if (count > 0) {
                val labelColor = if (plateWeight == 15f || plateWeight == 5f) Color(0xFF212121) else Color.White
                resultPlates.add(
                    PlateItem(
                        weightKg = plateWeight,
                        count = count,
                        color = color,
                        labelColor = labelColor,
                        heightFraction = heightFraction
                    )
                )
                remaining -= count * plateWeight
                // Округление погрешностей float
                remaining = Math.round(remaining * 100f) / 100f
            }
        }

        val loadedPerSide = resultPlates.sumOf { (it.weightKg * it.count).toDouble() }.toFloat()
        val totalAchieved = barWeightKg + (loadedPerSide * 2f)
        val isExact = Math.abs(totalAchieved - targetWeightKg) < 0.01f

        return PlateCalculationResult(
            targetWeightKg = targetWeightKg,
            barWeightKg = barWeightKg,
            weightPerSideKg = neededPerSide,
            platesPerSide = resultPlates,
            totalAchievedWeightKg = totalAchieved,
            isExact = isExact
        )
    }
}
