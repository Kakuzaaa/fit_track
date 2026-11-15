package com.example.fitnesstracker.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.domain.model.Exercise
import com.example.fitnesstracker.domain.model.ThemeMode
import com.example.fitnesstracker.domain.model.WorkoutLog
import com.example.fitnesstracker.domain.repository.ExerciseRepository
import com.example.fitnesstracker.domain.repository.SettingsRepository
import com.example.fitnesstracker.domain.repository.WorkoutRepository
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
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class ExportFormat(val fileName: String) {
    JSON("fitness_history.json"),
    CSV("fitness_history.csv")
}

data class ProfileUiState(
    val weightText: String = "",
    val weightError: Boolean = false,
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
                        themeMode = settings.themeMode,
                        weightError = false
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

    suspend fun clearAllLogs() {
        workoutRepository.clearAllLogs()
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
            "Вес,кг", "Длительность,мин", "Скорость,км/ч", "Ккал"
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
