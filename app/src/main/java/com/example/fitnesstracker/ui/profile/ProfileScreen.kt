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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnesstracker.domain.model.ThemeMode
import com.example.fitnesstracker.ui.LocalAppContainer
import com.example.fitnesstracker.ui.components.shareText
import com.example.fitnesstracker.util.Formatters
import com.example.fitnesstracker.util.ReleaseInfo
import com.example.fitnesstracker.util.UpdateUiState
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
            exerciseRepository = container.exerciseRepository,
            appUpdateManager = container.appUpdateManager
        )
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val updateState by viewModel.updateState.collectAsStateWithLifecycle()
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

        // ── Обновление приложения ──
        AppUpdateCard(
            updateState = updateState,
            currentVersion = viewModel.getCurrentVersionName(),
            onCheck = { viewModel.checkForUpdates() },
            onDownload = { release -> viewModel.downloadUpdate(release) },
            onInstall = { apkFile ->
                val launched = viewModel.installUpdate(apkFile)
                if (!launched) {
                    showSnackbar("Разрешите установку приложений в настройках устройства")
                }
            }
        )

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

// ─────────────────────────── Карточка обновления приложения ───────────────────────────

@Composable
private fun AppUpdateCard(
    updateState: UpdateUiState,
    currentVersion: String,
    onCheck: () -> Unit,
    onDownload: (ReleaseInfo) -> Unit,
    onInstall: (java.io.File) -> Unit
) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Обновление приложения",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Установлена: v$currentVersion",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (updateState is UpdateUiState.Idle || updateState is UpdateUiState.UpToDate || updateState is UpdateUiState.Error) {
                    FilledTonalButton(
                        onClick = onCheck,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Проверить")
                    }
                }
            }

            when (updateState) {
                is UpdateUiState.Idle -> {
                    Text(
                        text = "Проверьте наличие новых версий на GitHub. Обновление скачивается и устанавливается прямо внутри приложения.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                is UpdateUiState.Checking -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Text(
                            text = "Проверка новых версий на GitHub...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                is UpdateUiState.UpToDate -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "У вас установлена последняя версия!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                is UpdateUiState.Available -> {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Доступно: ${updateState.release.tagName}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f)
                                )
                                if (updateState.release.apkSize > 0) {
                                    val sizeMb = updateState.release.apkSize / (1024f * 1024f)
                                    Text(
                                        text = "${Formatters.number(sizeMb)} МБ",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (updateState.release.changelog.isNotBlank()) {
                                Text(
                                    text = updateState.release.changelog.take(250) +
                                        if (updateState.release.changelog.length > 250) "..." else "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Button(
                                onClick = { onDownload(updateState.release) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Скачать обновление")
                            }
                        }
                    }
                }
                is UpdateUiState.Downloading -> {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Загрузка обновления...",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${updateState.progressPercent}% (${Formatters.number(updateState.downloadedMb)} / ${Formatters.number(updateState.totalMb)} МБ)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        LinearProgressIndicator(
                            progress = { updateState.progressPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    }
                }
                is UpdateUiState.ReadyToInstall -> {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Версия ${updateState.release.tagName} скачана и готова к установке!",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Button(
                                onClick = { onInstall(updateState.apkFile) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.SystemUpdate,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Установить сейчас")
                            }
                        }
                    }
                }
                is UpdateUiState.Error -> {
                    Text(
                        text = updateState.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
