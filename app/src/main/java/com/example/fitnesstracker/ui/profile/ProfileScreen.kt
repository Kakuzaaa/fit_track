package com.example.fitnesstracker.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnesstracker.domain.model.ThemeMode
import com.example.fitnesstracker.ui.LocalAppContainer
import com.example.fitnesstracker.ui.components.shareText
import kotlinx.coroutines.launch

/**
 * Экран «Профиль»: вес тела, выбор темы, экспорт и очистка данных.
 */
@Composable
fun ProfileScreen(snackbarHostState: SnackbarHostState) {
    val container = LocalAppContainer.current
    val viewModel: ProfileViewModel = viewModel {
        ProfileViewModel(
            settingsRepository = container.settingsRepository,
            workoutRepository = container.workoutRepository,
            exerciseRepository = container.exerciseRepository
        )
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showClearDialog by remember { mutableStateOf(false) }

    fun showSnackbar(message: String) {
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    // Системный выбор файла для импорта истории (без разрешений на хранилище).
    val importPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            scope.launch {
                val text = runCatching {
                    context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                }.getOrNull()
                if (text.isNullOrBlank()) {
                    showSnackbar("Не удалось прочитать файл")
                } else {
                    runCatching { viewModel.importFromJson(text) }
                        .onSuccess { count -> showSnackbar("Импортировано записей: $count") }
                        .onFailure { showSnackbar("Ошибка импорта: файл не распознан") }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Профиль",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // ── Вес тела ──
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Вес тела",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Влияет на расчёт калорий в новых записях",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = state.weightText,
                        onValueChange = viewModel::onWeightChange,
                        suffix = { Text(text = "кг") },
                        isError = state.weightError,
                        supportingText = {
                            if (state.weightError) {
                                Text(text = "Введите вес от 30 до 250 кг")
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { if (viewModel.saveWeight()) showSnackbar("Вес сохранён") },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "Сохранить")
                    }
                }
            }
        }

        // ── Цель на день (необязательно) ──
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Цель на день",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Прогресс сгорит ккал в сводке «Сегодня». 0 — выключить",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = state.dailyGoalText,
                        onValueChange = viewModel::onDailyGoalChange,
                        suffix = { Text(text = "ккал") },
                        isError = state.dailyGoalError,
                        supportingText = {
                            if (state.dailyGoalError) {
                                Text(text = "Введите число от 0 до 10000")
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { if (viewModel.saveDailyGoal()) showSnackbar("Цель сохранена") },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "Сохранить")
                    }
                }
            }
        }

        // ── Отдых между подходами ──
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Отдых между подходами",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Таймер всплывает после сохранения подхода или «+1»",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0 to "Выкл", 60 to "60 сек", 90 to "90 сек", 120 to "120 сек").forEach { (seconds, label) ->
                        FilterChip(
                            selected = state.restTimerSeconds == seconds,
                            onClick = { viewModel.setRestTimer(seconds) },
                            label = { Text(text = label) }
                        )
                    }
                }
            }
        }

        // ── Тема ──
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Тема",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = state.themeMode == mode,
                            onClick = { viewModel.setThemeMode(mode) },
                            label = { Text(text = mode.label) }
                        )
                    }
                }
            }
        }

        // ── Данные ──
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Данные",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            runCatching { viewModel.exportData(ExportFormat.JSON) }
                                .onSuccess { content ->
                                    shareText(context, ExportFormat.JSON.fileName, content)
                                }
                                .onFailure { showSnackbar("Не удалось подготовить JSON") }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.exporting
                ) {
                    Text(text = "Экспорт истории (JSON)")
                }
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            runCatching { viewModel.exportData(ExportFormat.CSV) }
                                .onSuccess { content ->
                                    shareText(context, ExportFormat.CSV.fileName, content)
                                }
                                .onFailure { showSnackbar("Не удалось подготовить CSV") }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.exporting
                ) {
                    Text(text = "Экспорт истории (CSV)")
                }
                OutlinedButton(
                    onClick = { importPicker.launch("*/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Импорт истории (JSON)")
                }
                HorizontalDivider()
                OutlinedButton(
                    onClick = { showClearDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(text = "Очистить историю тренировок")
                }
            }
        }

        Icon(
            imageVector = Icons.Outlined.PersonOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .height(48.dp)
        )
        Text(
            text = "Фитнес-трекер · полностью офлайн, все данные хранятся только на устройстве",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(text = "Очистить историю?") },
            text = {
                Text(
                    text = "Все записи тренировок будут удалены безвозвратно. " +
                        "Справочник упражнений останется."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDialog = false
                        scope.launch {
                            viewModel.clearAllLogs()
                            showSnackbar("История тренировок очищена")
                        }
                    }
                ) {
                    Text(text = "Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(text = "Отмена")
                }
            }
        )
    }
}
