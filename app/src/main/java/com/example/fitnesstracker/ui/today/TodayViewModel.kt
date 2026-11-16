package com.example.fitnesstracker.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.domain.model.DayStats
import com.example.fitnesstracker.domain.model.WorkoutLog
import com.example.fitnesstracker.domain.repository.SettingsRepository
import com.example.fitnesstracker.domain.repository.WorkoutRepository
import com.example.fitnesstracker.util.CalorieCalculator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Сводка за текущие сутки для главной карточки.
 */
data class TodaySummary(
    val totalKcal: Int,
    val totalReps: Int,
    val cardioMinutes: Float
)

@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModel(
    private val workoutRepository: WorkoutRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    /**
     * «Сегодня» в отдельном потоке: обновляется при входе на экран
     * ([refreshToday]), иначе процесс, переживший полночь, показывал бы вчерашний список.
     */
    private val _todayEpochDay = MutableStateFlow(LocalDate.now().toEpochDay())

    val logs: StateFlow<List<WorkoutLog>> = _todayEpochDay
        .flatMapLatest { day -> workoutRepository.observeLogsForDay(day) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val summary: StateFlow<TodaySummary> = logs.map { list ->
        TodaySummary(
            totalKcal = list.sumOf { it.caloriesBurned },
            totalReps = list.sumOf { it.reps ?: 0 },
            cardioMinutes = list.filter { it.speedKmh != null }
                .sumOf { effectiveCardioMinutes(it) }.toFloat()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodaySummary(0, 0, 0f))

    /** Текущий стрик — сколько дней подряд были тренировки (сегодня или до вчерашнего). */
    val streak: StateFlow<Int> = workoutRepository.observeDayStats()
        .map { stats -> computeStreak(stats) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /** Дневная цель по калориям; 0 — функция выключена. */
    val dailyGoalKcal: StateFlow<Int> = settingsRepository.settings
        .map { it.dailyGoalKcal }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /** Вызывается при каждом входе на экран «Сегодня». */
    fun refreshToday() {
        _todayEpochDay.value = LocalDate.now().toEpochDay()
    }

    /** Быстрый повтор «+1 такой же подход» — копия записи с текущим временем. */
    fun repeat(log: WorkoutLog) {
        viewModelScope.launch {
            workoutRepository.insert(
                log.copy(
                    id = 0L,
                    timestamp = System.currentTimeMillis(),
                    dateEpochDay = LocalDate.now().toEpochDay()
                )
            )
        }
    }

    fun delete(log: WorkoutLog) {
        viewModelScope.launch { workoutRepository.delete(log) }
    }

    /** Восстановление записи после свайпа (undo из Snackbar). */
    fun restore(log: WorkoutLog) {
        viewModelScope.launch { workoutRepository.insert(log) }
    }

    private fun effectiveCardioMinutes(log: WorkoutLog): Double =
        log.durationMinutes?.toDouble()
            ?: CalorieCalculator.minutesFromDistance(
                (log.distanceKm ?: 0f).toDouble(),
                (log.speedKmh ?: 0f).toDouble()
            )

    private fun computeStreak(stats: List<DayStats>): Int {
        val activeDays = stats.filter { it.entries > 0 }.map { it.dateEpochDay }.toSet()
        var day = LocalDate.now().toEpochDay()
        if (day !in activeDays) day -= 1 // сегодня ещё может быть без тренировок
        var streak = 0
        while (day in activeDays) {
            streak++
            day -= 1
        }
        return streak
    }
}
