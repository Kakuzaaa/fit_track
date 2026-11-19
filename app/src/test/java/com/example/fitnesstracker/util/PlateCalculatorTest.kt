package com.example.fitnesstracker.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlateCalculatorTest {

    @Test
    fun `empty bar calculates zero plates`() {
        val res = PlateCalculator.calculate(targetWeightKg = 20f, barWeightKg = 20f)
        assertEquals(0f, res.weightPerSideKg, 0.01f)
        assertTrue(res.platesPerSide.isEmpty())
        assertEquals(20f, res.totalAchievedWeightKg, 0.01f)
        assertTrue(res.isExact)
    }

    @Test
    fun `60kg target on 20kg bar yields one 20kg plate per side`() {
        val res = PlateCalculator.calculate(targetWeightKg = 60f, barWeightKg = 20f)
        assertEquals(20f, res.weightPerSideKg, 0.01f)
        assertEquals(1, res.platesPerSide.size)
        assertEquals(20f, res.platesPerSide[0].weightKg, 0.01f)
        assertEquals(1, res.platesPerSide[0].count)
        assertEquals(60f, res.totalAchievedWeightKg, 0.01f)
        assertTrue(res.isExact)
    }

    @Test
    fun `100kg target on 20kg bar yields 25kg plus 15kg plate per side`() {
        val res = PlateCalculator.calculate(targetWeightKg = 100f, barWeightKg = 20f)
        assertEquals(40f, res.weightPerSideKg, 0.01f)
        assertEquals(2, res.platesPerSide.size)
        assertEquals(25f, res.platesPerSide[0].weightKg, 0.01f)
        assertEquals(1, res.platesPerSide[0].count)
        assertEquals(15f, res.platesPerSide[1].weightKg, 0.01f)
        assertEquals(1, res.platesPerSide[1].count)
        assertEquals(100f, res.totalAchievedWeightKg, 0.01f)
        assertTrue(res.isExact)
    }

    @Test
    fun `87_5kg target yields optimal breakdown`() {
        val res = PlateCalculator.calculate(targetWeightKg = 87.5f, barWeightKg = 20f)
        assertEquals(33.75f, res.weightPerSideKg, 0.01f)
        // 25 + 5 + 2.5 + 1.25 = 33.75
        val totalPerSide = res.platesPerSide.sumOf { (it.weightKg * it.count).toDouble() }.toFloat()
        assertEquals(33.75f, totalPerSide, 0.01f)
        assertEquals(87.5f, res.totalAchievedWeightKg, 0.01f)
        assertTrue(res.isExact)
    }

    @Test
    fun `15kg bar works correctly`() {
        val res = PlateCalculator.calculate(targetWeightKg = 35f, barWeightKg = 15f)
        assertEquals(10f, res.weightPerSideKg, 0.01f)
        assertEquals(1, res.platesPerSide.size)
        assertEquals(10f, res.platesPerSide[0].weightKg, 0.01f)
        assertEquals(35f, res.totalAchievedWeightKg, 0.01f)
        assertTrue(res.isExact)
    }
}
