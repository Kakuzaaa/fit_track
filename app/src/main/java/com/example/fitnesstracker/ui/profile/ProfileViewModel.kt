package com.example.fitnesstracker.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.domain.model.Exercise
import com.example.fitnesstracker.domain.model.ExerciseCategory
import com.example.fitnesstracker.domain.model.ThemeMode
import com.example.fitnesstracker.domain.model.TrackingType
import com.example.fitnesstracker.domain.model.UserSettings
import com.example.fitnesstracker.domain.model.WeightLog
import com.example.fitnesstracker.domain.model.WorkoutLog
import com.example.fitnesstracker.domain.repository.ExerciseRepository
import com.example.fitnesstracker.domain.repository.SettingsRepository
import com.example.fitnesstracker.domain.repository.WeightRepository
import com.example.fitnesstracker.domain.repository.WorkoutRepository
import com.example.fitnesstracker.util.AppUpdateManager
import com.example.fitnesstracker.util.CalorieCalculator
import com.example.fitnesstracker.util.Formatters
import com.example.fitnesstracker.util.ReleaseInfo
import com.example.fitnesstracker.util.UpdateUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class ExportFormat(val fileName: String) {
    JSON("fitness_history.json"),
    CSV("fitness_history.csv")
}

data class ProfileUiState(
    val weightText: String = "",
    val weightError: Boolean = false,
    val dailyGoalText: String = "0",
    val dailyGoalError: Boolean = false,
    val restTimerSeconds: Int = 0,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val exporting: Boolean = false
)

class ProfileViewModel(
    private val settingsRepository: SettingsRepository,
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    private val weightRepository: WeightRepository,
    private val appUpdateManager: AppUpdateManager
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    val weightHistory: StateFlow<List<WeightLog>> = weightRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val updateState: StateFlow<UpdateUiState> = appUpdateManager.state

    fun checkForUpdates() {
        viewModelScope.launch {
            appUpdateManager.checkForUpdates()
        }
    }

    fun downloadUpdate(release: ReleaseInfo) {
        viewModelScope.launch {
            appUpdateManager.downloadUpdate(release)
        }
    }

    fun installUpdate(apkFile: java.io.File): Boolean {
        return appUpdateManager.installUpdate(apkFile)
    }

    fun getCurrentVersionName(): String = appUpdateManager.getCurrentVersionName()

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                _state.update {
                    it.copy(
                        weightText = Formatters.number(settings.bodyWeightKg.toFloat()),
                        dailyGoalText = settings.dailyGoalKcal.toString(),
                        restTimerSeconds = settings.restTimerSeconds,
                        themeMode = settings.themeMode,
                        weightError = false,
                        dailyGoalError = false
                    )
                }
            }
        }
    }

    fun onWeightChange(text: String) {
        _state.update { it.copy(weightText = text, weightError = false) }
    }

    /** Сохранение веса; возвращает true, если валидация прошла и сохранение запущено. */
    fun saveWeight(): Boolean {
        val parsed = _state.value.weightText.replace(',', '.').toDoubleOrNull()
        if (parsed == null || parsed < 30.0 || parsed > 250.0) {
            _state.update { it.copy(weightError = true) }
            return false
        }
        viewModelScope.launch {
            settingsRepository.setBodyWeight(parsed)
            weightRepository.addWeight(parsed, LocalDate.now().toEpochDay())
        }
        return true
    }

    fun addWeightEntry(weightKg: Double, dateEpochDay: Long = LocalDate.now().toEpochDay()) {
        viewModelScope.launch {
            weightRepository.addWeight(weightKg, dateEpochDay)
            settingsRepository.setBodyWeight(weightKg)
        }
    }

    fun deleteWeightEntry(id: Long) {
        viewModelScope.launch {
            weightRepository.deleteWeight(id)
            val latest = weightRepository.observeLatest().first()
            val newBodyWeight = latest?.weightKg ?: UserSettings.DEFAULT_BODY_WEIGHT_KG
            settingsRepository.setBodyWeight(newBodyWeight)
        }
    }

    fun onDailyGoalChange(text: String) {
        _state.update { it.copy(dailyGoalText = text, dailyGoalError = false) }
    }

    /** Сохранение дневной цели; 0 — выключить. Возвращает true при успешной валидации. */
    fun saveDailyGoal(): Boolean {
        val parsed = _state.value.dailyGoalText.toIntOrNull()
        if (parsed == null || parsed < 0 || parsed > 10_000) {
            _state.update { it.copy(dailyGoalError = true) }
            return false
        }
        viewModelScope.launch { settingsRepository.setDailyGoalKcal(parsed) }
        return true
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    /** Длительность таймера отдыха; 0 — выключить. */
    fun setRestTimer(seconds: Int) {
        viewModelScope.launch { settingsRepository.setRestTimerSeconds(seconds) }
    }

    /** Асинхронно строит экспорт истории в JSON или CSV. */
    suspend fun exportData(format: ExportFormat): String = withContext(Dispatchers.IO) {
        val logs = workoutRepository.getAllLogs().sortedBy { it.timestamp }
        when (format) {
            ExportFormat.JSON -> {
                val exercises = exerciseRepository.observeExercises().first()
                buildJsonExport(logs, exercises)
            }
            ExportFormat.CSV -> buildCsvExport(logs)
        }
    }

    /**
     * Импорт истории из JSON ранее экспортированного формата.
     * Упражнения сопоставляются по имени; отсутствующие создаются как свои.
     * Возвращает число импортированных записей.
     */
    suspend fun importFromJson(jsonText: String): Int = withContext(Dispatchers.IO) {
        val root = JSONObject(jsonText)
        val workouts = root.optJSONArray("workouts") ?: return@withContext 0
        val timeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val zone = ZoneId.systemDefault()
        val bodyWeight = settingsRepository.settings.first().bodyWeightKg
        // Справочник читаем ОДИН раз; созданные за импорт упражнения кладём в кэш,
        // чтобы одно и то же имя не создалось дважды и не было N запросов к БД.
        val exercises = exerciseRepository.observeExercises().first()
        val createdDuringImport = mutableMapOf<String, Exercise>()
        var imported = 0

        for (i in 0 until workouts.length()) {
            val item = workouts.getJSONObject(i)
            val name = item.optString("exercise", "").trim()
            if (name.isEmpty()) continue

            val reps = item.optIntOrNull("reps")
            val weight = item.optFloatOrNull("weightKg")
            val duration = item.optFloatOrNull("durationMinutes")
            val speed = item.optFloatOrNull("speedKmh")
            val distance = item.optFloatOrNull("distanceKm")

            val localDateTime = runCatching { LocalDateTime.parse(item.getString("time"), timeFormat) }
                .getOrElse {
                    runCatching { LocalDate.parse(item.getString("date"), dateFormat).atStartOfDay() }
                        .getOrElse { LocalDateTime.now() }
                }
            val timestamp = localDateTime.atZone(zone).toInstant().toEpochMilli()
            val dateEpochDay = localDateTime.toLocalDate().toEpochDay()

            val trackingType = when {
                speed != null || distance != null -> TrackingType.CARDIO_SPEED
                duration != null && reps == null -> TrackingType.DURATION
                weight != null -> TrackingType.REPS_WEIGHT
                else -> TrackingType.REPS
            }
            val exercise = resolveExercise(name, trackingType, exercises, createdDuringImport)

            val calories = item.optInt("calories", 0).takeIf { it > 0 } ?: estimateCalories(
                trackingType, exercise.baseMetOrKcalFactor, bodyWeight, reps, weight, duration, speed, distance
            )

            workoutRepository.insert(
                WorkoutLog(
                    exerciseId = exercise.id,
                    exerciseNameSnapshot = name,
                    dateEpochDay = dateEpochDay,
                    timestamp = timestamp,
                    reps = reps,
                    weightKg = weight,
                    durationMinutes = duration,
                    speedKmh = speed,
                    distanceKm = distance,
                    caloriesBurned = calories
                )
            )
            imported++
        }
        imported
    }

    suspend fun clearAllLogs() {
        workoutRepository.clearAllLogs()
    }

    /**
     * Поиск упражнения по полному имени «название · хват» или по названию;
     * отсутствующие создаются как свои — один раз за импорт (кэш [created]).
     */
    private suspend fun resolveExercise(
        name: String,
        trackingType: TrackingType,
        exercises: List<Exercise>,
        created: MutableMap<String, Exercise>
    ): Exercise {
        created[name]?.let { return it }
        val existing = exercises.firstOrNull { it.displayName.equals(name, ignoreCase = true) }
            ?: run {
                val baseName = name.substringBefore(" · ").trim()
                val grip = if (name.contains(" · ")) name.substringAfter(" · ").trim() else null
                exercises.firstOrNull {
                    it.name.equals(baseName, ignoreCase = true) &&
                        (grip == null || it.gripType.equals(grip, ignoreCase = true))
                }
            }
        if (existing != null) return existing

        val category = when (trackingType) {
            TrackingType.CARDIO_SPEED -> ExerciseCategory.CARDIO
            TrackingType.DURATION -> ExerciseCategory.ABS
            TrackingType.REPS_WEIGHT -> ExerciseCategory.DUMBBELLS
            TrackingType.REPS -> ExerciseCategory.BODYWEIGHT
        }
        val id = exerciseRepository.createCustomExercise(
            name = name.substringBefore(" · ").trim(),
            category = category,
            gripType = if (name.contains(" · ")) name.substringAfter(" · ").trim() else null,
            trackingType = trackingType
        )
        val createdExercise = Exercise(
            id = id,
            name = name.substringBefore(" · ").trim(),
            category = category,
            gripType = if (name.contains(" · ")) name.substringAfter(" · ").trim() else null,
            trackingType = trackingType,
            baseMetOrKcalFactor = when (trackingType) {
                TrackingType.REPS -> 0.35
                TrackingType.REPS_WEIGHT -> 0.30
                TrackingType.DURATION -> 3.8
                TrackingType.CARDIO_SPEED -> 0.0
            },
            isCustom = true
        )
        created[name] = createdExercise
        return createdExercise
    }

    private fun estimateCalories(
        trackingType: TrackingType,
        baseFactor: Double,
        bodyWeight: Double,
        reps: Int?,
        weight: Float?,
        duration: Float?,
        speed: Float?,
        distance: Float?
    ): Int = when (trackingType) {
        TrackingType.REPS ->
            CalorieCalculator.repsKcal(bodyWeight, reps ?: 0, baseFactor)
        TrackingType.REPS_WEIGHT ->
            CalorieCalculator.repsKcal(bodyWeight, reps ?: 0, baseFactor, weight)
        TrackingType.CARDIO_SPEED -> {
            val minutes = duration?.toDouble()
                ?: CalorieCalculator.minutesFromDistance(
                    (distance ?: 0f).toDouble(),
                    (speed ?: 0f).toDouble()
                )
            CalorieCalculator.cardioKcal(bodyWeight, minutes, (speed ?: 0f).toDouble())
        }
        TrackingType.DURATION ->
            CalorieCalculator.staticHoldKcal(bodyWeight, (duration ?: 0f).toDouble(), baseFactor)
    }

    private fun buildJsonExport(logs: List<WorkoutLog>, exercises: List<Exercise>): String {
        val root = JSONObject()
        root.put("app", "Фитнес-трекер")
        root.put("exportedAt", Instant.now().toString())
        val workouts = JSONArray()
        val timeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        logs.forEach { log ->
            val item = JSONObject()
            item.put("date", LocalDate.ofEpochDay(log.dateEpochDay).toString())
            item.put("time", Instant.ofEpochMilli(log.timestamp).atZone(ZoneId.systemDefault()).format(timeFormat))
            item.put("exercise", log.exerciseNameSnapshot)
            log.reps?.let { item.put("reps", it) }
            log.weightKg?.let { item.put("weightKg", it.toDouble()) }
            log.durationMinutes?.let { item.put("durationMinutes", it.toDouble()) }
            log.speedKmh?.let { item.put("speedKmh", it.toDouble()) }
            log.distanceKm?.let { item.put("distanceKm", it.toDouble()) }
            item.put("calories", log.caloriesBurned)
            workouts.put(item)
        }
        root.put("workouts", workouts)
        val exercisesArray = JSONArray()
        exercises.forEach { exercise ->
            val item = JSONObject()
            item.put("id", exercise.id)
            item.put("name", exercise.name)
            item.put("category", exercise.category.name)
            exercise.gripType?.let { item.put("grip", it) }
            item.put("tracking", exercise.trackingType.name)
            exercisesArray.put(item)
        }
        root.put("exercises", exercisesArray)
        return root.toString(2)
    }

    private fun buildCsvExport(logs: List<WorkoutLog>): String {
        val header = listOf(
            "Дата", "Время", "Упражнение", "Повторения",
            "Вес,кг", "Длительность,мин", "Скорость,км/ч", "Расстояние,км", "Ккал"
        )
        val builder = StringBuilder(header.joinToString(",") { csvEscape(it) }).append("\n")
        val dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        val timeFormat = DateTimeFormatter.ofPattern("HH:mm")
        val zone = ZoneId.systemDefault()
        logs.forEach { log ->
            val row = listOf(
                LocalDate.ofEpochDay(log.dateEpochDay).format(dateFormat),
                Instant.ofEpochMilli(log.timestamp).atZone(zone).format(timeFormat),
                log.exerciseNameSnapshot,
                log.reps?.toString() ?: "",
                log.weightKg?.let { Formatters.number(it) } ?: "",
                log.durationMinutes?.let { Formatters.number(it) } ?: "",
                log.speedKmh?.let { Formatters.number(it) } ?: "",
                log.distanceKm?.let { Formatters.number(it) } ?: "",
                log.caloriesBurned.toString()
            )
            builder.append(row.joinToString(",") { csvEscape(it) }).append("\n")
        }
        return builder.toString()
    }

    private fun csvEscape(value: String): String =
        if (value.contains(',') || value.contains('"') || value.contains('\n')) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }

    /** Безопасное чтение числа: отсутствующее или null-значение JSON даёт null (а не NaN). */
    private fun JSONObject.optFloatOrNull(key: String): Float? =
        if (has(key) && !isNull(key)) {
            optDouble(key).takeIf { !it.isNaN() }?.toFloat()
        } else {
            null
        }

    private fun JSONObject.optIntOrNull(key: String): Int? =
        if (has(key) && !isNull(key)) {
            optInt(key).takeIf { it > 0 }
        } else {
            null
        }
}
