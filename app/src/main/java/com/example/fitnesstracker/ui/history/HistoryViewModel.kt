package com.example.fitnesstracker.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.domain.model.BestExerciseStats
import com.example.fitnesstracker.domain.model.DayStats
import com.example.fitnesstracker.domain.model.TrackingType
import com.example.fitnesstracker.domain.model.WorkoutLog
import com.example.fitnesstracker.domain.repository.WorkoutRepository
import com.example.fitnesstracker.util.CalorieCalculator
import com.example.fitnesstracker.util.Formatters
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Группа результатов поиска за один день.
 */
data class HistorySearchResultGroup(
    val dateEpochDay: Long,
    val dateTitle: String,
    val totalCalories: Int,
    val logs: List<WorkoutLog>
)

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

    /** Лучшие результаты по каждому упражнению за всю историю (нормативы уровня атлета). */
    private val _bestStats = MutableStateFlow<Map<Long, BestExerciseStats>>(emptyMap())
    val bestStats: StateFlow<Map<Long, BestExerciseStats>> = _bestStats.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    /**
     * Поиск по журналу тренировок: совпадение по имени упражнения, хвату,
     * названию дня/дате, весу, повторениям или дистанции.
     */
    val searchResults: StateFlow<List<HistorySearchResultGroup>> = combine(
        _searchQuery,
        workoutRepository.observeAllLogs()
    ) { query, allLogs ->
        val q = query.trim().lowercase()
        if (q.isBlank()) {
            emptyList()
        } else {
            val matched = allLogs.filter { log ->
                log.exerciseNameSnapshot.lowercase().contains(q) ||
                    Formatters.dayTitle(log.dateEpochDay).lowercase().contains(q) ||
                    LocalDate.ofEpochDay(log.dateEpochDay).toString().contains(q) ||
                    (log.weightKg != null && (Formatters.number(log.weightKg).contains(q) || "${log.weightKg.toInt()}".contains(q))) ||
                    (log.reps != null && "${log.reps}".contains(q)) ||
                    (log.distanceKm != null && Formatters.number(log.distanceKm).contains(q))
            }
            matched.groupBy { it.dateEpochDay }.map { (day, logs) ->
                HistorySearchResultGroup(
                    dateEpochDay = day,
                    dateTitle = Formatters.dayTitle(day),
                    totalCalories = logs.sumOf { it.caloriesBurned },
                    logs = logs.sortedByDescending { it.timestamp }
                )
            }.sortedByDescending { it.dateEpochDay }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            refreshBestStats()
        }
    }

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

    fun delete(log: WorkoutLog) {
        viewModelScope.launch {
            workoutRepository.delete(log)
            refreshBestStats()
        }
    }

    fun restore(log: WorkoutLog) {
        viewModelScope.launch {
            workoutRepository.insert(log)
            refreshBestStats()
        }
    }

    fun repeat(log: WorkoutLog) {
        viewModelScope.launch {
            val nowEpochDay = LocalDate.now().toEpochDay()
            val timestamp = if (log.dateEpochDay == nowEpochDay) {
                System.currentTimeMillis()
            } else {
                log.timestamp + 1_000L
            }
            val copy = log.copy(
                id = 0L,
                dateEpochDay = log.dateEpochDay,
                timestamp = timestamp
            )
            workoutRepository.insert(copy)
            refreshBestStats()
        }
    }

    private suspend fun refreshBestStats() {
        _bestStats.value = workoutRepository.getBestStats().associateBy { it.exerciseId }
    }

    /** Эффективная длительность: указанная или рассчитанная из расстояния и скорости. */
    private fun effectiveMinutes(log: WorkoutLog): Double =
        log.durationMinutes?.toDouble()
            ?: CalorieCalculator.minutesFromDistance(
                (log.distanceKm ?: 0f).toDouble(),
                (log.speedKmh ?: 0f).toDouble()
            )
}
