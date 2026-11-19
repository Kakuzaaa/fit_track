package com.example.fitnesstracker.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Форматирование дат, времени, чисел и русского множественного числа.
 */
object Formatters {

    private val ruLocale: Locale = Locale("ru")
    private val timeFormat = DateTimeFormatter.ofPattern("HH:mm")
    private val dateFormat = DateTimeFormatter.ofPattern("d MMMM yyyy", ruLocale)

    fun time(timestamp: Long): String =
        Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).format(timeFormat)

    fun dayTitle(epochDay: Long): String {
        val date = LocalDate.ofEpochDay(epochDay)
        val today = LocalDate.now()
        return when (epochDay) {
            today.toEpochDay() -> "Сегодня"
            today.minusDays(1).toEpochDay() -> "Вчера"
            else -> date.format(dateFormat)
        }
    }

    fun weekdayShort(date: LocalDate): String =
        date.dayOfWeek.getDisplayName(TextStyle.SHORT, ruLocale)

    /** Число без лишних нулей: 8.0 → «8», 8.5 → «8,5». */
    fun number(value: Float): String {
        if (value.isNaN() || value.isInfinite()) return "0"
        val normalized = (value * 100).roundToInt() / 100.0
        return if (normalized % 1.0 == 0.0) {
            normalized.toLong().toString()
        } else {
            normalized.toString().replace('.', ',')
        }
    }

    /**
     * Понятная длительность без нецелых минут через запятую (например, «34 мин» вместо «33,8 мин»
     * и «1 ч 10 мин» вместо «1 ч 9,6 мин»):
     *  - меньше минуты: «30 сек», «45 сек»;
     *  - до 5 минут с секундами (планка/статика): «1 мин 30 сек»;
     *  - ровные минуты и всё от 5 минут: целые минуты («34 мин»);
     *  - от 60 минут: часы и целые минуты («1 ч», «1 ч 10 мин»).
     */
    fun minutes(minutes: Float): String {
        val totalSec = (minutes * 60).roundToInt()
        if (totalSec <= 0) return "0 мин"
        if (totalSec < 60) return "$totalSec сек"
        if (totalSec < 300 && totalSec % 60 != 0) {
            val m = totalSec / 60
            val s = totalSec % 60
            return "$m мин $s сек"
        }
        val totalMins = (totalSec + 30) / 60
        val hours = totalMins / 60
        val restMins = totalMins % 60
        return when {
            hours == 0 -> "$totalMins мин"
            restMins == 0 -> "$hours ч"
            else -> "$hours ч $restMins мин"
        }
    }

    /** Отсчёт таймера: «01:23». */
    fun countdown(millis: Long): String {
        val totalSeconds = (millis / 1000).coerceAtLeast(0)
        return String.format(java.util.Locale.US, "%02d:%02d", totalSeconds / 60, totalSeconds % 60)
    }

    /**
     * Русское множественное число: plural(3, "подход", "подхода", "подходов") → «подхода».
     */
    fun plural(count: Int, one: String, few: String, many: String): String {
        val mod10 = count % 10
        val mod100 = count % 100
        return when {
            mod10 == 1 && mod100 != 11 -> one
            mod10 in 2..4 && mod100 !in 12..14 -> few
            else -> many
        }
    }
}
