package com.example.fitnesstracker.ui.quicklog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.domain.model.Exercise
import com.example.fitnesstracker.domain.model.ExerciseCategory
import com.example.fitnesstracker.domain.model.ExercisePackage
import com.example.fitnesstracker.domain.model.TrackingType
import com.example.fitnesstracker.domain.model.WorkoutLog
import com.example.fitnesstracker.domain.repository.ExerciseRepository
import com.example.fitnesstracker.domain.repository.SettingsRepository
import com.example.fitnesstracker.domain.repository.WorkoutRepository
import com.example.fitnesstracker.util.CalorieCalculator
import com.example.fitnesstracker.util.Formatters
import com.example.fitnesstracker.util.OneRepMaxCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * Событие успешного сохранения: новая запись или редактирование существующей,
 * с флагом установления личного рекорда (PR).
 */
data class SavedEvent(
    val log: WorkoutLog,
    val isEdit: Boolean,
    val isPersonalRecord: Boolean = false,
    val prDescription: String? = null
)

/**
 * Состояние BottomSheet быстрого добавления / редактирования подхода.
 */
data class QuickLogUiState(
    val query: String = "",
    val selectedCategory: ExerciseCategory? = null,
    val allExercises: List<Exercise> = emptyList(),
    val filteredExercises: List<Exercise> = emptyList(),
    val selected: Exercise? = null,
    val reps: Int = 10,
    val weightKg: Float = 8f,
    /** Длительность для статики (планка) — обязательна. */
    val durationMinutes: Float = 1f,
    /** Расстояние для кардио (км). */
    val distanceKm: Float = 3f,
    /** Длительность для кардио — необязательна: если null, считаем из расстояния и скорости. */
    val cardioDurationMinutes: Float? = null,
    val speedKmh: Float = 5.5f,
    val lastUsedWeightKg: Float? = null,
    val previewKcal: Int = 0,
    val showCreateDialog: Boolean = false,
    /** Запись в режиме редактирования (null — обычное добавление). */
    val editingLog: WorkoutLog? = null,
    /** Целевой день добавления записи (null — сегодня). */
    val targetDateEpochDay: Long? = null,
    /** Своё упражнение, открытое long-press'ом для правки/удаления. */
    val editingExercise: Exercise? = null,
    val showEditExerciseDialog: Boolean = false
)

class QuickLogViewModel(
    private val exerciseRepository: ExerciseRepository,
    private val workoutRepository: WorkoutRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(QuickLogUiState())
    val state: StateFlow<QuickLogUiState> = _state.asStateFlow()

    private val _saved = MutableSharedFlow<SavedEvent>(extraBufferCapacity = 1)
    val saved: SharedFlow<SavedEvent> = _saved.asSharedFlow()

    /** Все доступные пакеты упражнений (показываются, пока не импортированы). */
    val packages: List<ExercisePackage> = exerciseRepository.packages

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
                        editingLog = null,
                        reps = 10,
                        weightKg = lastWeight ?: 8f,
                        lastUsedWeightKg = lastWeight,
                        durationMinutes = if (exercise.trackingType == TrackingType.DURATION) 1f else 30f,
                        distanceKm = defaultDistanceFor(exercise),
                        cardioDurationMinutes = null,
                        speedKmh = defaultSpeedFor(exercise)
                    )
                )
            }
        }
    }

    /**
     * Вход в режим редактирования существующей записи.
     */
    fun startEditing(log: WorkoutLog) {
        viewModelScope.launch {
            val exercise = exerciseRepository.getExercise(log.exerciseId) ?: return@launch
            _state.update {
                recalculate(
                    it.copy(
                        selected = exercise,
                        editingLog = log,
                        targetDateEpochDay = log.dateEpochDay,
                        reps = log.reps ?: 10,
                        weightKg = log.weightKg ?: 8f,
                        lastUsedWeightKg = log.weightKg,
                        durationMinutes = log.durationMinutes ?: 1f,
                        cardioDurationMinutes = log.durationMinutes
                            .takeIf { exercise.trackingType == TrackingType.CARDIO_SPEED },
                        speedKmh = log.speedKmh ?: defaultSpeedFor(exercise),
                        distanceKm = log.distanceKm ?: defaultDistanceFor(exercise)
                    )
                )
            }
        }
    }

    fun clearSelection() {
        _state.update { recalculate(it.copy(selected = null, editingLog = null)) }
    }

    /**
     * Сброс перед открытием шита для НОВОЙ записи на сегодня.
     */
    fun prepareForNewEntry() {
        _state.update {
            recalculate(
                applyFilters(
                    it.copy(
                        selected = null,
                        editingLog = null,
                        targetDateEpochDay = null,
                        showCreateDialog = false,
                        showEditExerciseDialog = false,
                        editingExercise = null
                    )
                )
            )
        }
    }

    /**
     * Открытие для добавления записи в конкретный прошлый день (из экрана истории).
     */
    fun openForDate(dateEpochDay: Long) {
        _state.update {
            recalculate(
                applyFilters(
                    it.copy(
                        selected = null,
                        editingLog = null,
                        targetDateEpochDay = dateEpochDay,
                        showCreateDialog = false,
                        showEditExerciseDialog = false,
                        editingExercise = null
                    )
                )
            )
        }
    }

    /**
     * Открытие для добавления ещё одного подхода конкретного упражнения в выбранный день.
     */
    fun openForExerciseAndDate(exerciseId: Long, dateEpochDay: Long) {
        viewModelScope.launch {
            val exercise = exerciseRepository.getExercise(exerciseId) ?: return@launch
            val lastWeight = if (exercise.trackingType == TrackingType.REPS_WEIGHT) {
                workoutRepository.getLastUsedWeight(exercise.id)
            } else {
                null
            }
            _state.update {
                recalculate(
                    it.copy(
                        selected = exercise,
                        editingLog = null,
                        targetDateEpochDay = dateEpochDay,
                        reps = 10,
                        weightKg = lastWeight ?: 8f,
                        lastUsedWeightKg = lastWeight,
                        durationMinutes = if (exercise.trackingType == TrackingType.DURATION) 1f else 30f,
                        distanceKm = defaultDistanceFor(exercise),
                        cardioDurationMinutes = null,
                        speedKmh = defaultSpeedFor(exercise),
                        showCreateDialog = false,
                        showEditExerciseDialog = false,
                        editingExercise = null
                    )
                )
            }
        }
    }

    /**
     * Открытие для записи упражнения из программы / шаблона тренировок с целевыми параметрами.
     */
    fun openForExerciseWithTargets(
        exerciseId: Long,
        targetReps: Int?,
        targetWeightKg: Float?,
        targetDurationMinutes: Float?
    ) {
        viewModelScope.launch {
            val exercise = exerciseRepository.getExercise(exerciseId) ?: return@launch
            _state.update {
                recalculate(
                    it.copy(
                        selected = exercise,
                        editingLog = null,
                        targetDateEpochDay = null,
                        reps = targetReps ?: 10,
                        weightKg = targetWeightKg ?: 8f,
                        durationMinutes = targetDurationMinutes ?: 1f,
                        distanceKm = defaultDistanceFor(exercise),
                        cardioDurationMinutes = null,
                        speedKmh = defaultSpeedFor(exercise),
                        showCreateDialog = false,
                        showEditExerciseDialog = false,
                        editingExercise = null
                    )
                )
            }
        }
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

    fun setDistance(distanceKm: Float) {
        _state.update { recalculate(it.copy(distanceKm = distanceKm.coerceIn(0f, 200f))) }
    }

    fun setCardioDuration(minutes: Float?) {
        _state.update {
            // 0 и отрицательные значения трактуем как «не указано» — иначе степпер «минус»
            // обнулял бы расчёт по расстоянию и блокировал сохранение.
            recalculate(it.copy(cardioDurationMinutes = minutes?.takeIf { m -> m > 0f }?.coerceIn(0f, 600f)))
        }
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

    /** Long-press по своему упражнению в списке — открыть правку/удаление. */
    fun onExerciseLongPress(exercise: Exercise) {
        if (!exercise.isCustom) return
        _state.update { it.copy(editingExercise = exercise, showEditExerciseDialog = true) }
    }

    fun dismissEditExerciseDialog() {
        _state.update { it.copy(showEditExerciseDialog = false, editingExercise = null) }
    }

    fun saveEditedExercise(
        name: String,
        category: ExerciseCategory,
        gripType: String?,
        trackingType: TrackingType
    ) {
        val original = _state.value.editingExercise ?: return
        // При смене типа учёта фактор пересчитывается под новый тип, иначе сохраняется прежний.
        val factor = if (trackingType == original.trackingType) {
            original.baseMetOrKcalFactor
        } else {
            when (trackingType) {
                TrackingType.REPS -> 0.35
                TrackingType.REPS_WEIGHT -> 0.30
                TrackingType.DURATION -> 3.8
                TrackingType.CARDIO_SPEED -> 0.0
            }
        }
        viewModelScope.launch {
            exerciseRepository.updateCustomExercise(
                original.copy(
                    name = name.trim(),
                    category = category,
                    gripType = gripType?.trim()?.takeIf { it.isNotEmpty() },
                    trackingType = trackingType,
                    baseMetOrKcalFactor = factor
                )
            )
            dismissEditExerciseDialog()
        }
    }

    fun deleteEditedExercise() {
        val exercise = _state.value.editingExercise ?: return
        viewModelScope.launch {
            exerciseRepository.deleteCustomExercise(exercise)
            dismissEditExerciseDialog()
        }
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

    /** Импорт пакета упражнений в справочник (список обновится через Flow). */
    fun importPackage(packageId: String) {
        viewModelScope.launch { exerciseRepository.importExercisePackage(packageId) }
    }

    /** Наблюдение за историей подходов конкретного упражнения для экрана прогресса. */
    fun observeLogsForExercise(exerciseId: Long): Flow<List<WorkoutLog>> =
        workoutRepository.observeLogsForExercise(exerciseId)

    fun save() {
        val current = _state.value
        val exercise = current.selected ?: return
        val kcal = current.previewKcal
        if (kcal <= 0) return
        val original = current.editingLog
        val targetDay = original?.dateEpochDay ?: current.targetDateEpochDay ?: LocalDate.now().toEpochDay()
        val timestamp = when {
            original != null -> original.timestamp
            current.targetDateEpochDay != null -> {
                val targetDate = LocalDate.ofEpochDay(current.targetDateEpochDay)
                targetDate.atTime(LocalTime.now()).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            }
            else -> System.currentTimeMillis()
        }
        viewModelScope.launch {
            val log = WorkoutLog(
                id = original?.id ?: 0L,
                exerciseId = exercise.id,
                exerciseNameSnapshot = exercise.displayName,
                dateEpochDay = targetDay,
                timestamp = timestamp,
                reps = if (exercise.trackingType == TrackingType.REPS ||
                    exercise.trackingType == TrackingType.REPS_WEIGHT
                ) {
                    current.reps.takeIf { it > 0 }
                } else {
                    null
                },
                weightKg = if (exercise.trackingType == TrackingType.REPS_WEIGHT) current.weightKg else null,
                durationMinutes = when (exercise.trackingType) {
                    TrackingType.CARDIO_SPEED -> current.cardioDurationMinutes
                    TrackingType.DURATION -> current.durationMinutes.takeIf { it > 0 }
                    else -> null
                },
                speedKmh = if (exercise.trackingType == TrackingType.CARDIO_SPEED) current.speedKmh else null,
                distanceKm = if (exercise.trackingType == TrackingType.CARDIO_SPEED) {
                    current.distanceKm.takeIf { it > 0 }
                } else {
                    null
                },
                caloriesBurned = kcal
            )
            var isPr = false
            var prDesc: String? = null

            if (original == null) {
                val previousLogs = workoutRepository.getLogsForExercise(exercise.id)
                if (previousLogs.isNotEmpty()) {
                    val currentWeight = log.weightKg ?: 0f
                    val currentReps = log.reps ?: 0
                    val current1Rm = if (currentWeight > 0f && currentReps > 0) {
                        OneRepMaxCalculator.calculate1RmEpley(currentWeight, currentReps)
                    } else 0f

                    val prevMaxWeight = previousLogs.mapNotNull { it.weightKg }.maxOrNull() ?: 0f
                    val prevMaxReps = previousLogs.mapNotNull { it.reps }.maxOrNull() ?: 0
                    val prevMax1Rm = previousLogs.mapNotNull {
                        val w = it.weightKg ?: 0f
                        val r = it.reps ?: 0
                        if (w > 0f && r > 0) OneRepMaxCalculator.calculate1RmEpley(w, r) else null
                    }.maxOrNull() ?: 0f

                    when {
                        current1Rm > 0f && prevMax1Rm > 0f && current1Rm > prevMax1Rm -> {
                            isPr = true
                            prDesc = "1RM: ${Formatters.number(current1Rm)} кг (ранее ${Formatters.number(prevMax1Rm)} кг)"
                        }
                        currentWeight > 0f && prevMaxWeight > 0f && currentWeight > prevMaxWeight -> {
                            isPr = true
                            prDesc = "Вес: ${Formatters.number(currentWeight)} кг (ранее ${Formatters.number(prevMaxWeight)} кг)"
                        }
                        currentReps > 0 && prevMaxReps > 0 && currentReps > prevMaxReps && (currentWeight >= prevMaxWeight) -> {
                            isPr = true
                            prDesc = "Повторения: $currentReps раз (ранее $prevMaxReps)"
                        }
                    }
                }
                workoutRepository.insert(log)
            } else {
                workoutRepository.update(log)
            }
            _saved.tryEmit(
                SavedEvent(
                    log = log,
                    isEdit = original != null,
                    isPersonalRecord = isPr,
                    prDescription = prDesc
                )
            )
            // Сбрасываем выбор, но оставляем поиск и фильтр — удобно записать следующий подход.
            _state.update {
                recalculate(applyFilters(it.copy(selected = null, query = "", editingLog = null)))
            }
        }
    }

    /** Пресеты скорости в зависимости от вида кардио. */
    fun speedPresets(exercise: Exercise): List<Float> = when {
        exercise.name.contains("ходьб", ignoreCase = true) -> listOf(4f, 5.5f, 6.5f)
        exercise.name.contains("бег", ignoreCase = true) -> listOf(8f, 10f, 12.5f)
        else -> listOf(4f, 5.5f, 6.5f, 8f, 10f, 12.5f)
    }

    /** Пресеты расстояния (км) в зависимости от вида кардио. */
    fun distancePresets(exercise: Exercise): List<Float> = when {
        exercise.name.contains("ходьб", ignoreCase = true) -> listOf(2f, 3f, 5f)
        exercise.name.contains("бег", ignoreCase = true) -> listOf(3f, 5f, 10f)
        else -> listOf(2f, 3f, 5f, 10f)
    }

    /** Эффективная длительность кардио: указанная или рассчитанная из расстояния. */
    fun effectiveCardioMinutes(state: QuickLogUiState): Double =
        state.cardioDurationMinutes?.toDouble()
            ?: CalorieCalculator.minutesFromDistance(state.distanceKm.toDouble(), state.speedKmh.toDouble())

    private fun defaultSpeedFor(exercise: Exercise): Float = when {
        exercise.name.contains("ходьб", ignoreCase = true) -> 5.5f
        exercise.name.contains("бег", ignoreCase = true) -> 10f
        else -> 8f
    }

    private fun defaultDistanceFor(exercise: Exercise): Float = when {
        exercise.name.contains("ходьб", ignoreCase = true) -> 3f
        exercise.name.contains("бег", ignoreCase = true) -> 5f
        else -> 4f
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
                CalorieCalculator.cardioKcal(
                    bodyWeightKg,
                    effectiveCardioMinutes(state),
                    state.speedKmh.toDouble()
                )
            TrackingType.DURATION ->
                CalorieCalculator.staticHoldKcal(bodyWeightKg, state.durationMinutes.toDouble(), exercise.baseMetOrKcalFactor)
        }
    }
}
