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
        val normalized = (value * 100).roundToInt() / 100.0
        return if (normalized % 1.0 == 0.0) {
            normalized.toLong().toString()
        } else {
            normalized.toString().replace('.', ',')
        }
    }

    /** Длительность: 45.0 → «45 мин», 1.5 → «1,5 мин», 90 → «1 ч», 73.2 → «1 ч 13,2 мин».
     *  Вся арифметика в целых десятых минуты — без float-хвостов вида «13,200000000000003». */
    fun minutes(minutes: Float): String {
        val totalTenths = (minutes * 10).roundToInt()
        if (totalTenths <= 0) return "0 мин"
        val hours = totalTenths / 600
        val restTenths = totalTenths % 600
        val builder = StringBuilder()
        if (hours > 0) builder.append(hours).append(" ч")
        if (restTenths > 0) {
            if (hours > 0) builder.append(' ')
            val wholeMinutes = restTenths / 10
            val tenths = restTenths % 10
            val restText = if (tenths == 0) wholeMinutes.toString() else "$wholeMinutes,$tenths"
            builder.append(restText).append(" мин")
        }
        return builder.toString()
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
