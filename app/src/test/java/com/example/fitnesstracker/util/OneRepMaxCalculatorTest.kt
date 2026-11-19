package com.example.fitnesstracker.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OneRepMaxCalculatorTest {

    @Test
    fun calculate1RmEpley_oneRepReturnsWeight() {
        assertEquals(100f, OneRepMaxCalculator.calculate1RmEpley(100f, 1), 0.01f)
    }

    @Test
    fun calculate1RmEpley_tenRepsCalculatesCorrectly() {
        // 100 * (1 + 10/30) = 133.3f
        assertEquals(133.3f, OneRepMaxCalculator.calculate1RmEpley(100f, 10), 0.1f)
    }

    @Test
    fun calculate1RmBrzycki_oneRepReturnsWeight() {
        assertEquals(80f, OneRepMaxCalculator.calculate1RmBrzycki(80f, 1), 0.01f)
    }

    @Test
    fun calculate1RmBrzycki_largeRepsFallsBackToEpleyWithoutError() {
        // Brzycki formula with 36+ reps falls back to Epley to avoid division by zero or negative values
        val result36 = OneRepMaxCalculator.calculate1RmBrzycki(100f, 36)
        assertTrue(result36 > 0f)
        assertEquals(OneRepMaxCalculator.calculate1RmEpley(100f, 36), result36, 0.01f)

        val result50 = OneRepMaxCalculator.calculate1RmBrzycki(100f, 50)
        assertTrue(result50 > 0f)
    }

    @Test
    fun calculate1Rm_invalidOrNegativeValuesReturnZero() {
        assertEquals(0f, OneRepMaxCalculator.calculate1RmEpley(0f, 10), 0.01f)
        assertEquals(0f, OneRepMaxCalculator.calculate1RmEpley(-50f, 10), 0.01f)
        assertEquals(0f, OneRepMaxCalculator.calculate1RmEpley(100f, 0), 0.01f)
        assertEquals(0f, OneRepMaxCalculator.calculate1RmEpley(100f, -5), 0.01f)
        assertEquals(0f, OneRepMaxCalculator.calculate1RmEpley(Float.NaN, 10), 0.01f)
        assertEquals(0f, OneRepMaxCalculator.calculate1RmEpley(Float.POSITIVE_INFINITY, 10), 0.01f)

        assertEquals(0f, OneRepMaxCalculator.calculate1RmBrzycki(0f, 10), 0.01f)
        assertEquals(0f, OneRepMaxCalculator.calculate1RmBrzycki(-50f, 10), 0.01f)
        assertEquals(0f, OneRepMaxCalculator.calculate1RmBrzycki(100f, 0), 0.01f)
        assertEquals(0f, OneRepMaxCalculator.calculate1RmBrzycki(Float.NaN, 10), 0.01f)
    }

    @Test
    fun percentagesTable_invalidInputsReturnEmpty() {
        assertTrue(OneRepMaxCalculator.percentagesTable(0f).isEmpty())
        assertTrue(OneRepMaxCalculator.percentagesTable(-100f).isEmpty())
        assertTrue(OneRepMaxCalculator.percentagesTable(Float.NaN).isEmpty())
        assertTrue(OneRepMaxCalculator.percentagesTable(Float.POSITIVE_INFINITY).isEmpty())
    }

    @Test
    fun percentagesTable_generatesSevenEntries() {
        val table = OneRepMaxCalculator.percentagesTable(100f)
        assertEquals(7, table.size)
        // 100% -> 100kg
        assertEquals(100f, table.first().third, 0.1f)
        // 70% -> 70kg
        assertEquals(70f, table.last().third, 0.1f)
    }
}
