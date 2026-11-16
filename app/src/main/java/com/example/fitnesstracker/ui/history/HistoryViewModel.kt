package com.example.fitnesstracker.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.domain.model.DayStats
import com.example.fitnesstracker.domain.model.TrackingType
import com.example.fitnesstracker.domain.model.WorkoutLog
import com.example.fitnesstracker.domain.repository.WorkoutRepository
import com.example.fitnesstracker.util.CalorieCalculator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

/**
 * Сводка по одному упражнению за выбранный день
 * («Отжимания · Классические: 45 раз, 3 подхода»).
 */
data class DayExerciseSummary(
    val name: String,
    val trackingType: TrackingType,
    val sets: Int,
    val totalReps: Int,
    val totalMinutes: Float,
    val totalDistance: Float,
    val totalKcal: Int,
    val entries: List<WorkoutLog>
)

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(private val workoutRepository: WorkoutRepository) : ViewModel() {

    val dayStats: StateFlow<List<DayStats>> = workoutRepository.observeDayStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedDay = MutableStateFlow(LocalDate.now().toEpochDay())
    val selectedDay: StateFlow<Long> = _selectedDay.asStateFlow()

    val dayLogs: StateFlow<List<WorkoutLog>> = _selectedDay
        .flatMapLatest { day -> workoutRepository.observeLogsForDay(day) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Последние 7 дней (включая сегодня) с нулевым заполнением — для недельного графика. */
    val weekStats: StateFlow<List<Pair<LocalDate, DayStats>>> = dayStats.map { all ->
        val byDay = all.associateBy { it.dateEpochDay }
        val today = LocalDate.now()
        (6 downTo 0).map { offset ->
            val date = today.minusDays(offset.toLong())
            val stats = byDay[date.toEpochDay()] ?: DayStats(date.toEpochDay(), 0, 0, 0)
            date to stats
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val daySummaries: StateFlow<List<DayExerciseSummary>> = dayLogs.map { logs ->
        logs.groupBy { it.exerciseNameSnapshot }.map { (name, entries) ->
            val first = entries.first()
            DayExerciseSummary(
                name = name,
                trackingType = when {
                    first.speedKmh != null -> TrackingType.CARDIO_SPEED
                    first.durationMinutes != null && first.reps == null -> TrackingType.DURATION
                    else -> TrackingType.REPS
                },
                sets = entries.size,
                totalReps = entries.sumOf { it.reps ?: 0 },
                totalMinutes = entries.sumOf { effectiveMinutes(it) }.toFloat(),
                totalDistance = entries.sumOf { (it.distanceKm ?: 0f).toDouble() }.toFloat(),
                totalKcal = entries.sumOf { it.caloriesBurned },
                entries = entries.sortedBy { it.timestamp }
            )
        }.sortedBy { summary -> summary.entries.minOf { it.timestamp } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun selectDay(epochDay: Long) {
        _selectedDay.value = epochDay
    }

    /** Эффективная длительность: указанная или рассчитанная из расстояния и скорости. */
    private fun effectiveMinutes(log: WorkoutLog): Double =
        log.durationMinutes?.toDouble()
            ?: CalorieCalculator.minutesFromDistance(
                (log.distanceKm ?: 0f).toDouble(),
                (log.speedKmh ?: 0f).toDouble()
            )
}
