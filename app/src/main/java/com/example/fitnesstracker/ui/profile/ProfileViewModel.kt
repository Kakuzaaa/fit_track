package com.example.fitnesstracker.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.domain.model.Exercise
import com.example.fitnesstracker.domain.model.ExerciseCategory
import com.example.fitnesstracker.domain.model.ThemeMode
import com.example.fitnesstracker.domain.model.TrackingType
import com.example.fitnesstracker.domain.model.WorkoutLog
import com.example.fitnesstracker.domain.repository.ExerciseRepository
import com.example.fitnesstracker.domain.repository.SettingsRepository
import com.example.fitnesstracker.domain.repository.WorkoutRepository
import com.example.fitnesstracker.util.CalorieCalculator
import com.example.fitnesstracker.util.Formatters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val exporting: Boolean = false
)

class ProfileViewModel(
    private val settingsRepository: SettingsRepository,
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                _state.update {
                    it.copy(
                        weightText = Formatters.number(settings.bodyWeightKg.toFloat()),
                        dailyGoalText = settings.dailyGoalKcal.toString(),
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

    fun saveWeight() {
        val parsed = _state.value.weightText.replace(',', '.').toDoubleOrNull()
        if (parsed == null || parsed < 30.0 || parsed > 250.0) {
            _state.update { it.copy(weightError = true) }
            return
        }
        viewModelScope.launch { settingsRepository.setBodyWeight(parsed) }
    }

    fun onDailyGoalChange(text: String) {
        _state.update { it.copy(dailyGoalText = text, dailyGoalError = false) }
    }

    /** Сохранение дневной цели; 0 — выключить функцию. */
    fun saveDailyGoal() {
        val parsed = _state.value.dailyGoalText.toIntOrNull()
        if (parsed == null || parsed < 0 || parsed > 10_000) {
            _state.update { it.copy(dailyGoalError = true) }
            return
        }
        viewModelScope.launch { settingsRepository.setDailyGoalKcal(parsed) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
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
        var imported = 0

        for (i in 0 until workouts.length()) {
            val item = workouts.getJSONObject(i)
            val name = item.optString("exercise", "").trim()
            if (name.isEmpty()) continue

            val reps = if (item.has("reps")) item.optInt("reps").takeIf { it > 0 } else null
            val weight = if (item.has("weightKg")) item.optDouble("weightKg").toFloat() else null
            val duration = if (item.has("durationMinutes")) item.optDouble("durationMinutes").toFloat() else null
            val speed = if (item.has("speedKmh")) item.optDouble("speedKmh").toFloat() else null
            val distance = if (item.has("distanceKm")) item.optDouble("distanceKm").toFloat() else null

            val localDateTime = runCatching { LocalDateTime.parse(item.getString("time"), timeFormat) }
                .getOrElse {
                    runCatching { LocalDate.parse(item.getString("date"), dateFormat).atStartOfDay() }
                        .getOrElse { LocalDateTime.now() }
                }
            val timestamp = localDateTime.atZone(zone).toInstant().toEpochMilli()
            val dateEpochDay = localDateTime.toLocalDate().toEpochDay()

            val trackingType = when {
                speed != null -> TrackingType.CARDIO_SPEED
                duration != null && reps == null -> TrackingType.DURATION
                weight != null -> TrackingType.REPS_WEIGHT
                else -> TrackingType.REPS
            }
            val exercise = resolveExercise(name, trackingType)

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

    /** Поиск упражнения по полному имени «название · хват» или по названию; иначе создание своего. */
    private suspend fun resolveExercise(name: String, trackingType: TrackingType): Exercise {
        val exercises = exerciseRepository.observeExercises().first()
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
        return Exercise(
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
}
