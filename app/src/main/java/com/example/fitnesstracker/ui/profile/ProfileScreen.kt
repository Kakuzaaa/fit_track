package com.example.fitnesstracker.ui.profile

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Scale
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
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnesstracker.domain.model.ThemeMode
import com.example.fitnesstracker.domain.model.WeightLog
import com.example.fitnesstracker.ui.LocalAppContainer
import com.example.fitnesstracker.ui.components.WeightChart
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
            weightRepository = container.weightRepository,
            appUpdateManager = container.appUpdateManager
        )
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val weightHistory by viewModel.weightHistory.collectAsStateWithLifecycle()
    val updateState by viewModel.updateState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showClearDialog by remember { mutableStateOf(false) }
    var showAddWeightDialog by remember { mutableStateOf(false) }
    var showWeightHistory by remember { mutableStateOf(false) }

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

        // ── Дневник веса тела ──
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "⚖️", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Дневник веса тела",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    if (weightHistory.size >= 2) {
                        val first = weightHistory.first().weightKg
                        val last = weightHistory.last().weightKg
                        val diff = last - first
                        val diffStr = if (diff > 0) "+${Formatters.number(diff.toFloat())} кг" else "${Formatters.number(diff.toFloat())} кг"
                        val badgeColor = if (diff <= 0) Color(0xFF4CAF50) else Color(0xFFFFA726)

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = badgeColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = diffStr,
                                color = badgeColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = "Влияет на расчёт калорий силовых и кардио",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // График веса (если есть история)
                if (weightHistory.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    WeightChart(weights = weightHistory)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Быстрое поле изменения веса
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = state.weightText,
                        onValueChange = viewModel::onWeightChange,
                        suffix = { Text(text = "кг") },
                        isError = state.weightError,
                        supportingText = if (state.weightError) {
                            { Text(text = "Введите вес от 30 до 250 кг") }
                        } else null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { if (viewModel.saveWeight()) showSnackbar("Вес сохранён и записан в дневник") },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "Сохранить")
                    }
                }

                // Раскрывающийся список истории взвешиваний
                if (weightHistory.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showWeightHistory = !showWeightHistory }
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "История измерений (${weightHistory.size})",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            imageVector = if (showWeightHistory) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (showWeightHistory) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            weightHistory.sortedByDescending { it.dateEpochDay }.take(10).forEach { item ->
                                val date = java.time.LocalDate.ofEpochDay(item.dateEpochDay)
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "${Formatters.number(item.weightKg.toFloat())} кг",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${date.dayOfMonth}.${date.monthValue.toString().padStart(2, '0')}.${date.year}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                viewModel.deleteWeightEntry(item.id)
                                                showSnackbar("Запись веса удалена")
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.DeleteOutline,
                                                contentDescription = "Удалить",
                                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
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

    if (showAddWeightDialog) {
        var inputWeight by remember { mutableStateOf(state.weightText.ifEmpty { "75.0" }) }
        var isErr by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddWeightDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "⚖️", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Внести вес тела", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Запись на сегодня (${Formatters.dayTitle(java.time.LocalDate.now().toEpochDay())})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = inputWeight,
                        onValueChange = {
                            inputWeight = it
                            isErr = false
                        },
                        suffix = { Text("кг") },
                        isError = isErr,
                        supportingText = { if (isErr) Text("Введите число от 30 до 250") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf(-1.0, -0.5, 0.5, 1.0).forEach { step ->
                            val label = if (step > 0) "+$step" else "$step"
                            FilledTonalButton(
                                onClick = {
                                    val current = inputWeight.replace(',', '.').toDoubleOrNull() ?: 75.0
                                    inputWeight = Formatters.number((current + step).toFloat())
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(text = label, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = inputWeight.replace(',', '.').toDoubleOrNull()
                        if (parsed == null || parsed < 30.0 || parsed > 250.0) {
                            isErr = true
                        } else {
                            viewModel.addWeightEntry(parsed)
                            showSnackbar("Вес сохранён: ${Formatters.number(parsed.toFloat())} кг")
                            showAddWeightDialog = false
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Записать")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddWeightDialog = false }) {
                    Text("Отмена")
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
    val context = LocalContext.current
    val openUrl = { urlString: String ->
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlString)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onDownload(updateState.release) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Download,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Скачать")
                                }
                                OutlinedButton(
                                    onClick = { openUrl(updateState.release.downloadUrl) },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("В браузере")
                                }
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
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = updateState.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (updateState.release != null) {
                                Button(
                                    onClick = { onDownload(updateState.release) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Повторить")
                                }
                                OutlinedButton(
                                    onClick = { openUrl(updateState.release.downloadUrl) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1.3f)
                                ) {
                                    Text("В браузере")
                                }
                            } else {
                                Button(
                                    onClick = onCheck,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Повторить")
                                }
                                OutlinedButton(
                                    onClick = { openUrl("https://github.com/Kakuzaaa/fit_track/releases/latest") },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1.3f)
                                ) {
                                    Text("GitHub")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
