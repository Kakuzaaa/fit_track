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
    fun percentagesTable_generatesSevenEntries() {
        val table = OneRepMaxCalculator.percentagesTable(100f)
        assertEquals(7, table.size)
        // 100% -> 100kg
        assertEquals(100f, table.first().third, 0.1f)
        // 70% -> 70kg
        assertEquals(70f, table.last().third, 0.1f)
    }
}
