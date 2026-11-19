package com.example.fitnesstracker.util

import org.junit.Assert.assertEquals
import org.junit.Test

class FormattersTest {

    @Test
    fun testMinutesFormatting() {
        assertEquals("0 мин", Formatters.minutes(0f))
        assertEquals("0 мин", Formatters.minutes(-5f))

        // Меньше 1 минуты: секунды
        assertEquals("15 сек", Formatters.minutes(0.25f))
        assertEquals("30 сек", Formatters.minutes(0.5f))
        assertEquals("45 сек", Formatters.minutes(0.75f))

        // Ровные минуты до 5 мин
        assertEquals("1 мин", Formatters.minutes(1.0f))
        assertEquals("2 мин", Formatters.minutes(2.0f))
        assertEquals("5 мин", Formatters.minutes(5.0f))

        // Минуты с секундами (планка/статика до 5 мин)
        assertEquals("1 мин 15 сек", Formatters.minutes(1.25f))
        assertEquals("1 мин 30 сек", Formatters.minutes(1.5f))
        assertEquals("2 мин 45 сек", Formatters.minutes(2.75f))

        // От 5 до 60 мин: целые округленные минуты
        assertEquals("6 мин", Formatters.minutes(5.8f))
        assertEquals("34 мин", Formatters.minutes(33.8f))
        assertEquals("45 мин", Formatters.minutes(45.0f))
        assertEquals("59 мин", Formatters.minutes(59.2f))

        // Часы и минуты (без нецелых 1 ч 9,6 мин)
        assertEquals("1 ч", Formatters.minutes(60.0f))
        assertEquals("1 ч 10 мин", Formatters.minutes(69.6f))
        assertEquals("1 ч 30 мин", Formatters.minutes(90.0f))
        assertEquals("2 ч", Formatters.minutes(120.0f))
        assertEquals("2 ч 15 мин", Formatters.minutes(135.0f))
    }

    @Test
    fun testNumberFormatting() {
        assertEquals("8", Formatters.number(8.0f))
        assertEquals("8,5", Formatters.number(8.5f))
        assertEquals("8,25", Formatters.number(8.25f))
        assertEquals("0", Formatters.number(0f))
        assertEquals("0", Formatters.number(Float.NaN))
        assertEquals("0", Formatters.number(Float.POSITIVE_INFINITY))
        assertEquals("0", Formatters.number(Float.NEGATIVE_INFINITY))
    }
}
