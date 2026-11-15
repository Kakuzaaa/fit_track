package com.example.fitnesstracker.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.domain.model.WorkoutLog
import com.example.fitnesstracker.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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

class TodayViewModel(private val workoutRepository: WorkoutRepository) : ViewModel() {

    private val todayEpochDay: Long = LocalDate.now().toEpochDay()

    val logs: StateFlow<List<WorkoutLog>> = workoutRepository.observeLogsForDay(todayEpochDay)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val summary: StateFlow<TodaySummary> = logs.map { list ->
        TodaySummary(
            totalKcal = list.sumOf { it.caloriesBurned },
            totalReps = list.sumOf { it.reps ?: 0 },
            cardioMinutes = list.filter { it.speedKmh != null }
                .sumOf { (it.durationMinutes ?: 0f).toDouble() }.toFloat()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodaySummary(0, 0, 0f))

    fun delete(log: WorkoutLog) {
        viewModelScope.launch { workoutRepository.delete(log) }
    }

    /** Восстановление записи после свайпа (undo из Snackbar). */
    fun restore(log: WorkoutLog) {
        viewModelScope.launch { workoutRepository.insert(log) }
    }
}
