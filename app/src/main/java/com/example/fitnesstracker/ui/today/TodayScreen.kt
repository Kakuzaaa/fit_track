package com.example.fitnesstracker.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnesstracker.domain.model.WorkoutLog
import com.example.fitnesstracker.ui.LocalAppContainer
import com.example.fitnesstracker.ui.components.EmptyState
import com.example.fitnesstracker.ui.components.WorkoutLogRow
import com.example.fitnesstracker.ui.quicklog.QuickLogSheet
import com.example.fitnesstracker.ui.quicklog.QuickLogViewModel
import com.example.fitnesstracker.util.Formatters
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

/**
 * Главный экран «Сегодня»: сводка за сутки (ккал, повторения, кардио,
 * стрик и прогресс цели), лента подходов с «+1 подход» и редактированием,
 * свайп-удаление с отменой и FAB быстрого добавления.
 */
@Composable
fun TodayScreen(snackbarHostState: SnackbarHostState) {
    val container = LocalAppContainer.current
    val viewModel: TodayViewModel = viewModel {
        TodayViewModel(
            workoutRepository = container.workoutRepository,
            settingsRepository = container.settingsRepository
        )
    }
    val quickLogViewModel: QuickLogViewModel = viewModel {
        QuickLogViewModel(
            exerciseRepository = container.exerciseRepository,
            workoutRepository = container.workoutRepository,
            settingsRepository = container.settingsRepository
        )
    }

    val logs by viewModel.logs.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val streak by viewModel.streak.collectAsStateWithLifecycle()
    val dailyGoalKcal by viewModel.dailyGoalKcal.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var showQuickLog by remember { mutableStateOf(false) }

    // Реакция на сохранение из QuickLogSheet: закрыть шит и показать снекбар.
    LaunchedEffect(Unit) {
        quickLogViewModel.saved.collect { event ->
            showQuickLog = false
            val verb = if (event.isEdit) "Обновлено" else "Записано"
            snackbarHostState.showSnackbar(
                message = "$verb: ${event.log.exerciseNameSnapshot} · +${event.log.caloriesBurned} ккал"
            )
        }
    }

    // Обновляем «сегодня» при каждом входе на экран (иначе после полуночи список зависает на вчера).
    LaunchedEffect(Unit) {
        viewModel.refreshToday()
    }

    // И на каждый ON_RESUME: возврат из свёрнутого приложения через полночь тоже покрыт.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshToday()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (logs.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.Insights,
                title = "Сегодня ещё нет тренировок",
                subtitle = "Нажмите «Записать упражнение» и добавьте первый подход"
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item(key = "summary") {
                    TodaySummaryCard(summary = summary, dailyGoalKcal = dailyGoalKcal, streak = streak)
                }
                items(logs, key = { it.id }) { log ->
                    SwipeToDeleteWorkoutRow(
                        log = log,
                        onEdit = {
                            quickLogViewModel.startEditing(log)
                            showQuickLog = true
                        },
                        onRepeat = {
                            viewModel.repeat(log)
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Повторено: ${log.exerciseNameSnapshot} · +${log.caloriesBurned} ккал"
                                )
                            }
                        },
                        onDelete = {
                            viewModel.delete(log)
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "Удалено: ${log.exerciseNameSnapshot}",
                                    actionLabel = "Отменить",
                                    withDismissAction = true
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    viewModel.restore(log)
                                }
                            }
                        }
                    )
                }
                item(key = "bottom_space") { Spacer(modifier = Modifier.height(72.dp)) }
            }
        }

        ExtendedFloatingActionButton(
            onClick = {
                // Сбрасываем возможный зависший режим редактирования/диалог создания.
                quickLogViewModel.prepareForNewEntry()
                showQuickLog = true
            },
            icon = { Icon(imageVector = Icons.Filled.Add, contentDescription = null) },
            text = { Text(text = "Записать упражнение") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }

    if (showQuickLog) {
        QuickLogSheet(viewModel = quickLogViewModel, onDismiss = { showQuickLog = false })
    }
}

/**
 * Карточка-сводка за текущие сутки: ккал, повторения, кардио +
 * необязательные прогресс дневной цели и стрик.
 */
@Composable
private fun TodaySummaryCard(summary: TodaySummary, dailyGoalKcal: Int, streak: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Сожжено сегодня",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${summary.totalKcal}",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = " ккал",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatPill(
                    label = "Всего повторений",
                    value = "${summary.totalReps}",
                    modifier = Modifier.weight(1f)
                )
                StatPill(
                    label = "Время кардио",
                    value = Formatters.minutes(summary.cardioMinutes),
                    modifier = Modifier.weight(1f)
                )
            }

            if (dailyGoalKcal > 0) {
                Spacer(modifier = Modifier.height(14.dp))
                val progress = (summary.totalKcal.toFloat() / dailyGoalKcal).coerceIn(0f, 1f)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Цель: ${summary.totalKcal} / $dailyGoalKcal ккал",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${(progress * 100).roundToInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
                )
            }

            if (streak >= 2) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.08f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalFireDepartment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.height(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Стрик: $streak " +
                                Formatters.plural(streak, "день", "дня", "дней") + " подряд",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatPill(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.08f)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
            )
        }
    }
}

/**
 * Строка журнала: быстрое «+1 подход», редактирование и удаление свайпом влево.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteWorkoutRow(
    log: WorkoutLog,
    onEdit: () -> Unit,
    onRepeat: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Удалить",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(end = 24.dp)
                )
            }
        }
    ) {
        WorkoutLogRow(log = log, onEdit = onEdit, onRepeat = onRepeat)
    }
}
