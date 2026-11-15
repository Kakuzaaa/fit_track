package com.example.fitnesstracker.ui.quicklog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.domain.model.Exercise
import com.example.fitnesstracker.domain.model.ExerciseCategory
import com.example.fitnesstracker.domain.model.TrackingType
import com.example.fitnesstracker.domain.model.WorkoutLog
import com.example.fitnesstracker.domain.repository.ExerciseRepository
import com.example.fitnesstracker.domain.repository.SettingsRepository
import com.example.fitnesstracker.domain.repository.WorkoutRepository
import com.example.fitnesstracker.util.CalorieCalculator
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Состояние BottomSheet быстрого добавления подхода.
 */
data class QuickLogUiState(
    val query: String = "",
    val selectedCategory: ExerciseCategory? = null,
    val allExercises: List<Exercise> = emptyList(),
    val filteredExercises: List<Exercise> = emptyList(),
    val selected: Exercise? = null,
    val reps: Int = 10,
    val weightKg: Float = 8f,
    val durationMinutes: Float = 30f,
    val speedKmh: Float = 5.5f,
    val lastUsedWeightKg: Float? = null,
    val previewKcal: Int = 0,
    val showCreateDialog: Boolean = false
)

class QuickLogViewModel(
    private val exerciseRepository: ExerciseRepository,
    private val workoutRepository: WorkoutRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(QuickLogUiState())
    val state: StateFlow<QuickLogUiState> = _state.asStateFlow()

    /** Событие успешного сохранения — экран закрывает шит и показывает снекбар. */
    private val _saved = MutableSharedFlow<WorkoutLog>(extraBufferCapacity = 1)
    val saved: SharedFlow<WorkoutLog> = _saved.asSharedFlow()

    private var bodyWeightKg = CalorieCalculator.DEFAULT_BODY_WEIGHT_KG

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { userSettings ->
                bodyWeightKg = userSettings.bodyWeightKg
                recalculate()
            }
        }
        viewModelScope.launch {
            exerciseRepository.observeExercises().collect { exercises ->
                _state.update { recalculate(applyFilters(it.copy(allExercises = exercises))) }
            }
        }
    }

    fun onQueryChange(query: String) {
        _state.update { recalculate(applyFilters(it.copy(query = query))) }
    }

    fun onCategorySelect(category: ExerciseCategory?) {
        _state.update { recalculate(applyFilters(it.copy(selectedCategory = category))) }
    }

    fun selectExercise(exercise: Exercise) {
        viewModelScope.launch {
            val lastWeight = if (exercise.trackingType == TrackingType.REPS_WEIGHT) {
                workoutRepository.getLastUsedWeight(exercise.id)
            } else {
                null
            }
            _state.update {
                recalculate(
                    it.copy(
                        selected = exercise,
                        reps = 10,
                        weightKg = lastWeight ?: 8f,
                        lastUsedWeightKg = lastWeight,
                        durationMinutes = if (exercise.trackingType == TrackingType.DURATION) 1f else 30f,
                        speedKmh = defaultSpeedFor(exercise)
                    )
                )
            }
        }
    }

    fun clearSelection() {
        _state.update { recalculate(it.copy(selected = null)) }
    }

    fun setReps(reps: Int) {
        _state.update { recalculate(it.copy(reps = reps.coerceIn(0, 1000))) }
    }

    fun setWeight(weightKg: Float) {
        _state.update { recalculate(it.copy(weightKg = weightKg.coerceIn(0f, 200f))) }
    }

    fun setDuration(minutes: Float) {
        _state.update { recalculate(it.copy(durationMinutes = minutes.coerceIn(0f, 600f))) }
    }

    fun setSpeed(speedKmh: Float) {
        _state.update { recalculate(it.copy(speedKmh = speedKmh.coerceIn(0.5f, 30f))) }
    }

    fun showCreateDialog() {
        _state.update { it.copy(showCreateDialog = true) }
    }

    fun dismissCreateDialog() {
        _state.update { it.copy(showCreateDialog = false) }
    }

    fun createCustomExercise(
        name: String,
        category: ExerciseCategory,
        gripType: String?,
        trackingType: TrackingType
    ) {
        viewModelScope.launch {
            val id = exerciseRepository.createCustomExercise(name, category, gripType, trackingType)
            dismissCreateDialog()
            exerciseRepository.getExercise(id)?.let { selectExercise(it) }
        }
    }

    fun save() {
        val current = _state.value
        val exercise = current.selected ?: return
        val kcal = current.previewKcal
        if (kcal <= 0) return
        viewModelScope.launch {
            val log = WorkoutLog(
                exerciseId = exercise.id,
                exerciseNameSnapshot = exercise.displayName,
                dateEpochDay = LocalDate.now().toEpochDay(),
                timestamp = System.currentTimeMillis(),
                reps = if (exercise.trackingType == TrackingType.REPS ||
                    exercise.trackingType == TrackingType.REPS_WEIGHT
                ) {
                    current.reps.takeIf { it > 0 }
                } else {
                    null
                },
                weightKg = if (exercise.trackingType == TrackingType.REPS_WEIGHT) current.weightKg else null,
                durationMinutes = if (exercise.trackingType == TrackingType.CARDIO_SPEED ||
                    exercise.trackingType == TrackingType.DURATION
                ) {
                    current.durationMinutes
                } else {
                    null
                },
                speedKmh = if (exercise.trackingType == TrackingType.CARDIO_SPEED) current.speedKmh else null,
                caloriesBurned = kcal
            )
            workoutRepository.insert(log)
            _saved.tryEmit(log)
            // Сбрасываем выбор, но оставляем поиск и фильтр — удобно записать следующий подход.
            _state.update { recalculate(applyFilters(it.copy(selected = null, query = ""))) }
        }
    }

    /** Пресеты скорости в зависимости от вида кардио. */
    fun speedPresets(exercise: Exercise): List<Float> = when {
        exercise.name.contains("ходьб", ignoreCase = true) -> listOf(4f, 5.5f, 6.5f)
        exercise.name.contains("бег", ignoreCase = true) -> listOf(8f, 10f, 12.5f)
        else -> listOf(4f, 5.5f, 6.5f, 8f, 10f, 12.5f)
    }

    private fun defaultSpeedFor(exercise: Exercise): Float = when {
        exercise.name.contains("ходьб", ignoreCase = true) -> 5.5f
        exercise.name.contains("бег", ignoreCase = true) -> 10f
        else -> 8f
    }

    private fun applyFilters(state: QuickLogUiState): QuickLogUiState {
        val query = state.query.trim()
        val filtered = state.allExercises.filter { exercise ->
            val matchesCategory = state.selectedCategory == null || exercise.category == state.selectedCategory
            val matchesQuery = query.isEmpty() ||
                exercise.name.contains(query, ignoreCase = true) ||
                (exercise.gripType?.contains(query, ignoreCase = true) == true)
            matchesCategory && matchesQuery
        }
        return state.copy(filteredExercises = filtered)
    }

    private fun recalculate(state: QuickLogUiState): QuickLogUiState =
        state.copy(previewKcal = computePreview(state))

    private fun recalculate() {
        _state.update { recalculate(it) }
    }

    private fun computePreview(state: QuickLogUiState): Int {
        val exercise = state.selected ?: return 0
        return when (exercise.trackingType) {
            TrackingType.REPS ->
                CalorieCalculator.repsKcal(bodyWeightKg, state.reps, exercise.baseMetOrKcalFactor)
            TrackingType.REPS_WEIGHT ->
                CalorieCalculator.repsKcal(bodyWeightKg, state.reps, exercise.baseMetOrKcalFactor, state.weightKg)
            TrackingType.CARDIO_SPEED ->
                CalorieCalculator.cardioKcal(bodyWeightKg, state.durationMinutes.toDouble(), state.speedKmh.toDouble())
            TrackingType.DURATION ->
                CalorieCalculator.staticHoldKcal(bodyWeightKg, state.durationMinutes.toDouble(), exercise.baseMetOrKcalFactor)
        }
    }
}
