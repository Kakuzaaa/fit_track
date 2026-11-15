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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnesstracker.domain.model.WorkoutLog
import com.example.fitnesstracker.ui.LocalAppContainer
import com.example.fitnesstracker.ui.components.EmptyState
import com.example.fitnesstracker.ui.components.WorkoutLogRow
import com.example.fitnesstracker.ui.quicklog.QuickLogSheet
import com.example.fitnesstracker.ui.quicklog.QuickLogViewModel
import com.example.fitnesstracker.util.Formatters
import kotlinx.coroutines.launch

/**
 * Главный экран «Сегодня»: сводка за сутки, лента подходов,
 * свайп-удаление с отменой и FAB быстрого добавления.
 */
@Composable
fun TodayScreen(snackbarHostState: SnackbarHostState) {
    val container = LocalAppContainer.current
    val viewModel: TodayViewModel = viewModel { TodayViewModel(container.workoutRepository) }
    val quickLogViewModel: QuickLogViewModel = viewModel {
        QuickLogViewModel(
            exerciseRepository = container.exerciseRepository,
            workoutRepository = container.workoutRepository,
            settingsRepository = container.settingsRepository
        )
    }

    val logs by viewModel.logs.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var showQuickLog by remember { mutableStateOf(false) }

    // Реакция на сохранение из QuickLogSheet: закрыть шит и показать снекбар.
    LaunchedEffect(Unit) {
        quickLogViewModel.saved.collect { log ->
            showQuickLog = false
            snackbarHostState.showSnackbar(
                message = "Записано: ${log.exerciseNameSnapshot} · +${log.caloriesBurned} ккал"
            )
        }
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
                item(key = "summary") { TodaySummaryCard(summary = summary) }
                items(logs, key = { it.id }) { log ->
                    SwipeToDeleteWorkoutRow(
                        log = log,
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
            onClick = { showQuickLog = true },
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
 * Карточка-сводка за текущие сутки.
 */
@Composable
private fun TodaySummaryCard(summary: TodaySummary) {
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
 * Строка журнала с удалением свайпом влево и красным фоном-подсказкой.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteWorkoutRow(log: WorkoutLog, onDelete: () -> Unit) {
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
        WorkoutLogRow(log = log)
    }
}
