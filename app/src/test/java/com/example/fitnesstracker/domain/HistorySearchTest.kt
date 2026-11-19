package com.example.fitnesstracker.domain

import com.example.fitnesstracker.domain.model.WorkoutLog
import com.example.fitnesstracker.util.Formatters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class HistorySearchTest {

    private val sampleLogs = listOf(
        WorkoutLog(
            id = 1L,
            exerciseId = 1L,
            exerciseNameSnapshot = "Жим штанги лёжа · Средний хват",
            dateEpochDay = LocalDate.of(2026, 11, 18).toEpochDay(),
            timestamp = 1731930000000L,
            reps = 10,
            weightKg = 80f,
            caloriesBurned = 70
        ),
        WorkoutLog(
            id = 2L,
            exerciseId = 1L,
            exerciseNameSnapshot = "Жим штанги лёжа · Узкий хват",
            dateEpochDay = LocalDate.of(2026, 11, 18).toEpochDay(),
            timestamp = 1731930300000L,
            reps = 8,
            weightKg = 75f,
            caloriesBurned = 55
        ),
        WorkoutLog(
            id = 3L,
            exerciseId = 2L,
            exerciseNameSnapshot = "Приседания со штангой",
            dateEpochDay = LocalDate.of(2026, 11, 15).toEpochDay(),
            timestamp = 1731670000000L,
            reps = 12,
            weightKg = 100f,
            caloriesBurned = 110
        ),
        WorkoutLog(
            id = 4L,
            exerciseId = 3L,
            exerciseNameSnapshot = "Подтягивания · Прямой хват",
            dateEpochDay = LocalDate.of(2026, 11, 10).toEpochDay(),
            timestamp = 1731238000000L,
            reps = 15,
            weightKg = null,
            caloriesBurned = 45
        ),
        WorkoutLog(
            id = 5L,
            exerciseId = 4L,
            exerciseNameSnapshot = "Бег",
            dateEpochDay = LocalDate.of(2026, 11, 5).toEpochDay(),
            timestamp = 1730800000000L,
            reps = null,
            weightKg = null,
            durationMinutes = 30f,
            speedKmh = 10f,
            distanceKm = 5f,
            caloriesBurned = 350
        )
    )

    private fun searchLogs(query: String, logs: List<WorkoutLog>): List<WorkoutLog> {
        val q = query.trim().lowercase()
        if (q.isBlank()) return emptyList()
        return logs.filter { log ->
            log.exerciseNameSnapshot.lowercase().contains(q) ||
                Formatters.dayTitle(log.dateEpochDay).lowercase().contains(q) ||
                LocalDate.ofEpochDay(log.dateEpochDay).toString().contains(q) ||
                (log.weightKg != null && (Formatters.number(log.weightKg).contains(q) || "${log.weightKg.toInt()}".contains(q))) ||
                (log.reps != null && "${log.reps}".contains(q)) ||
                (log.distanceKm != null && Formatters.number(log.distanceKm).contains(q))
        }
    }

    @Test
    fun search_matchesExerciseNameCaseInsensitively() {
        val results = searchLogs("жим", sampleLogs)
        assertEquals(2, results.size)
        assertTrue(results.all { it.exerciseNameSnapshot.contains("Жим") })

        val upper = searchLogs("ЖИМ", sampleLogs)
        assertEquals(2, upper.size)
    }

    @Test
    fun search_matchesGripType() {
        val results = searchLogs("узкий", sampleLogs)
        assertEquals(1, results.size)
        assertEquals(2L, results.first().id)
    }

    @Test
    fun search_matchesWeight() {
        val results100 = searchLogs("100", sampleLogs)
        assertEquals(1, results100.size)
        assertEquals("Приседания со штангой", results100.first().exerciseNameSnapshot)
    }

    @Test
    fun search_matchesCardioDistance() {
        val results = searchLogs("5", sampleLogs)
        assertTrue(results.any { it.exerciseNameSnapshot == "Бег" })
    }

    @Test
    fun search_matchesDateIsoString() {
        val results = searchLogs("2026-11-18", sampleLogs)
        assertEquals(2, results.size)
    }

    @Test
    fun search_blankQueryReturnsEmpty() {
        assertTrue(searchLogs("", sampleLogs).isEmpty())
        assertTrue(searchLogs("   ", sampleLogs).isEmpty())
    }
}
