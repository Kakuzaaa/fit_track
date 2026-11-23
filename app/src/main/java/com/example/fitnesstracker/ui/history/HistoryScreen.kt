package com.example.fitnesstracker.ui.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnesstracker.domain.model.DayStats
import com.example.fitnesstracker.domain.model.TrackingType
import com.example.fitnesstracker.domain.model.WorkoutLog
import com.example.fitnesstracker.ui.LocalAppContainer
import com.example.fitnesstracker.ui.components.EmptyState
import com.example.fitnesstracker.ui.components.ExerciseCategoryBadge
import com.example.fitnesstracker.ui.components.ExerciseVisuals
import com.example.fitnesstracker.ui.components.WeeklyBarChart
import com.example.fitnesstracker.ui.components.WorkoutHeatmap
import com.example.fitnesstracker.ui.components.workoutDetails
import com.example.fitnesstracker.ui.quicklog.QuickLogSheet
import com.example.fitnesstracker.ui.quicklog.QuickLogViewModel
import com.example.fitnesstracker.util.AthleteLevel
import com.example.fitnesstracker.util.AthleteStandards
import com.example.fitnesstracker.util.CalorieCalculator
import com.example.fitnesstracker.util.Formatters
import java.time.LocalDate
import kotlinx.coroutines.launch

/**
 * Экран «История»: горизонтальная лента дат с точками-индикаторами,
 * сводка выбранного дня, раскрывающийся детальный лог с возможностью
 * редактирования, повтора и удаления подходов, а также добавления
 * новых записей за выбранный день.
 */
@Composable
fun HistoryScreen(snackbarHostState: SnackbarHostState) {
    val container = LocalAppContainer.current
    val viewModel: HistoryViewModel = viewModel { HistoryViewModel(container.workoutRepository) }
    val quickLogViewModel: QuickLogViewModel = viewModel {
        QuickLogViewModel(
            exerciseRepository = container.exerciseRepository,
            workoutRepository = container.workoutRepository,
            settingsRepository = container.settingsRepository
        )
    }

    val dayStats by viewModel.dayStats.collectAsStateWithLifecycle()
    val selectedDay by viewModel.selectedDay.collectAsStateWithLifecycle()
    val dayLogs by viewModel.dayLogs.collectAsStateWithLifecycle()
    val summaries by viewModel.daySummaries.collectAsStateWithLifecycle()
    val weekStats by viewModel.weekStats.collectAsStateWithLifecycle()
    val bestStats by viewModel.bestStats.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    var showQuickLog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        quickLogViewModel.saved.collect { event ->
            showQuickLog = false
            val verb = if (event.isEdit) "Обновлено" else "Записано"
            val prPrefix = if (event.isPersonalRecord) "🏆 Новый рекорд! " + (event.prDescription?.let { "($it) · " } ?: "") else ""
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(
                message = "$prPrefix$verb: ${event.log.exerciseNameSnapshot} · +${event.log.caloriesBurned} ккал",
                duration = SnackbarDuration.Short,
                withDismissAction = true
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Поле поиска по всей истории тренировок
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
                placeholder = {
                    Text(
                        text = "Поиск (упражнение, хват, вес, дата)...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Поиск",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = viewModel::clearSearch) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Очистить поиск"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                )
            )

            if (searchQuery.isNotBlank()) {
                SearchResultsView(
                    resultGroups = searchResults,
                    onSelectDay = { day ->
                        viewModel.selectDay(day)
                        viewModel.clearSearch()
                    },
                    onEdit = { log ->
                        quickLogViewModel.startEditing(log)
                        showQuickLog = true
                    },
                    onRepeat = { log ->
                        viewModel.repeat(log)
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Повторено: ${log.exerciseNameSnapshot} · +${log.caloriesBurned} ккал"
                            )
                        }
                    },
                    onDelete = { log ->
                        viewModel.delete(log)
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "Удалено: ${log.exerciseNameSnapshot}",
                                actionLabel = "Отменить",
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                viewModel.restore(log)
                            }
                        }
                    }
                )
            } else {
                DateStrip(
                    statsByDay = dayStats.associateBy { it.dateEpochDay },
                    selectedDay = selectedDay,
                    onSelect = viewModel::selectDay
                )

                val totalKcal = dayLogs.sumOf { it.caloriesBurned }
                val cardioMinutes = dayLogs.filter { it.speedKmh != null }
                    .sumOf { log ->
                        log.durationMinutes?.toDouble()
                            ?: CalorieCalculator.minutesFromDistance(
                                (log.distanceKm ?: 0f).toDouble(),
                                (log.speedKmh ?: 0f).toDouble()
                            )
                    }.toFloat()

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item(key = "chart") {
                        WeeklyChartCard(
                            week = weekStats,
                            statsByDay = dayStats.associateBy { it.dateEpochDay },
                            selectedEpochDay = selectedDay,
                            onSelectDay = viewModel::selectDay,
                            selectedDate = LocalDate.ofEpochDay(selectedDay)
                        )
                    }

                    item(key = "summary") {
                        DaySummaryCard(
                            title = Formatters.dayTitle(selectedDay),
                            totalKcal = totalKcal,
                            entries = dayLogs.size,
                            cardioMinutes = cardioMinutes
                        )
                    }

                    if (summaries.isEmpty()) {
                        item(key = "empty") {
                            EmptyState(
                                icon = Icons.Filled.History,
                                title = "Нет записей за этот день",
                                subtitle = "Нажмите «+ Записать в день», чтобы внести тренировку"
                            )
                        }
                    } else {
                        items(summaries, key = { it.name }) { summary ->
                            val bestLevel = bestStats[summary.entries.first().exerciseId]?.let {
                                AthleteStandards.levelForStats(it.exerciseId, it.bestReps, it.bestSeconds)
                            }
                            ExpandableExerciseCard(
                                summary = summary,
                                bestLevel = bestLevel,
                                onEdit = { log ->
                                    quickLogViewModel.startEditing(log)
                                    showQuickLog = true
                                },
                                onRepeat = { log ->
                                    viewModel.repeat(log)
                                    scope.launch {
                                        snackbarHostState.currentSnackbarData?.dismiss()
                                        snackbarHostState.showSnackbar(
                                            message = "Повторено: ${log.exerciseNameSnapshot} · +${log.caloriesBurned} ккал",
                                            duration = SnackbarDuration.Short,
                                            withDismissAction = true
                                        )
                                    }
                                },
                                onDelete = { log ->
                                    viewModel.delete(log)
                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Удалено: ${log.exerciseNameSnapshot}",
                                            actionLabel = "Отменить",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.restore(log)
                                        }
                                    }
                                },
                                onAddSet = { exerciseId ->
                                    quickLogViewModel.openForExerciseAndDate(exerciseId, selectedDay)
                                    showQuickLog = true
                                }
                            )
                        }
                    }
                    item(key = "bottom_space") { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = {
                quickLogViewModel.openForDate(selectedDay)
                showQuickLog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = {
                val isToday = selectedDay == LocalDate.now().toEpochDay()
                Text(if (isToday) "Записать" else "Записать в день")
            }
        )
    }

    if (showQuickLog) {
        QuickLogSheet(
            viewModel = quickLogViewModel,
            onDismiss = { showQuickLog = false }
        )
    }
}

// ─────────────────────────── Лента дат ───────────────────────────

private const val DATE_STRIP_LENGTH = 60

@Composable
private fun DateStrip(statsByDay: Map<Long, DayStats>, selectedDay: Long, onSelect: (Long) -> Unit) {
    // Без remember: пересчитывается при каждом входе на экран (не замерзает после полуночи).
    val today = LocalDate.now()
    val days = (0L until DATE_STRIP_LENGTH).map { today.minusDays(it) }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(days, key = { it.toEpochDay() }) { date ->
            val epochDay = date.toEpochDay()
            val hasWorkout = statsByDay.containsKey(epochDay)
            val selected = epochDay == selectedDay
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                },
                modifier = Modifier.clickable { onSelect(epochDay) }
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = Formatters.weekdayShort(date),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Text(
                        text = "${date.dayOfMonth}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    !hasWorkout -> Color.Transparent
                                    selected -> MaterialTheme.colorScheme.onPrimary
                                    else -> MaterialTheme.colorScheme.primary
                                }
                            )
                    )
                }
            }
        }
    }
}

// ─────────────────────────── График за неделю ───────────────────────────

/**
 * Карточка статистики активности: переключение между недельным графиком (7 дней)
 * и GitHub-картой активности (16 недель).
 */
@Composable
private fun WeeklyChartCard(
    week: List<Pair<LocalDate, DayStats>>,
    statsByDay: Map<Long, DayStats>,
    selectedEpochDay: Long,
    onSelectDay: (Long) -> Unit,
    selectedDate: LocalDate? = null,
    modifier: Modifier = Modifier
) {
    var isCollapsed by rememberSaveable { mutableStateOf(false) }
    var viewMode by rememberSaveable { mutableIntStateOf(0) } // 0: 7 дней, 1: Карта активности
    var metric by rememberSaveable { mutableIntStateOf(0) } // 0: Ккал, 1: Повторения

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { isCollapsed = !isCollapsed },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (viewMode == 0) "Активность" else "Карта активности",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                if (!isCollapsed) {
                    FilterChip(
                        selected = viewMode == 0,
                        onClick = { viewMode = 0 },
                        label = { Text(text = "7 дней") }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    FilterChip(
                        selected = viewMode == 1,
                        onClick = { viewMode = 1 },
                        label = { Text(text = "Матрица") }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                } else {
                    val values = week.map { if (metric == 0) it.second.totalCalories else it.second.totalReps }
                    val average = values.sum() / 7
                    Text(
                        text = "В ср. $average ${if (metric == 0) "ккал" else "повт."}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }
                Icon(
                    imageVector = if (isCollapsed) Icons.Filled.ExpandMore else Icons.Filled.ExpandLess,
                    contentDescription = if (isCollapsed) "Развернуть график" else "Свернуть график",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!isCollapsed) {
                Spacer(modifier = Modifier.height(10.dp))

                if (viewMode == 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = metric == 0,
                            onClick = { metric = 0 },
                            label = { Text(text = "Ккал") }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        FilterChip(
                            selected = metric == 1,
                            onClick = { metric = 1 },
                            label = { Text(text = "Повторения") }
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    WeeklyBarChart(
                        days = week.map { it.first },
                        values = week.map { if (metric == 0) it.second.totalCalories else it.second.totalReps },
                        selectedDate = selectedDate,
                        onSelectDate = { date -> onSelectDay(date.toEpochDay()) }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val values = week.map { if (metric == 0) it.second.totalCalories else it.second.totalReps }
                    val average = values.sum() / 7
                    Text(
                        text = "В среднем: ${Formatters.number(average.toFloat())} " +
                            if (metric == 0) "ккал в день" else "повторений в день",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    WorkoutHeatmap(
                        statsByDay = statsByDay,
                        selectedEpochDay = selectedEpochDay,
                        onSelectDay = onSelectDay
                    )
                }
            }
        }
    }
}

// ─────────────────────────── Сводка дня ───────────────────────────

@Composable
private fun DaySummaryCard(
    title: String,
    totalKcal: Int,
    entries: Int,
    cardioMinutes: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                SummaryItem(
                    value = "$totalKcal",
                    label = "ккал",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                SummaryItem(
                    value = "$entries",
                    label = Formatters.plural(entries, "подход", "подхода", "подходов"),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                SummaryItem(
                    value = Formatters.minutes(cardioMinutes),
                    label = "кардио",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(value: String, label: String, color: Color) {
    Column {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = color.copy(alpha = 0.75f)
        )
    }
}

// ─────────────────────────── Раскрывающиеся карточки ───────────────────────────

private fun trackingIcon(trackingType: TrackingType): ImageVector = when (trackingType) {
    TrackingType.CARDIO_SPEED -> Icons.Filled.Timer
    TrackingType.DURATION -> Icons.Filled.Timer
    else -> Icons.Filled.History
}

@Composable
private fun ExpandableExerciseCard(
    summary: DayExerciseSummary,
    bestLevel: AthleteLevel?,
    onEdit: (WorkoutLog) -> Unit,
    onRepeat: (WorkoutLog) -> Unit,
    onDelete: (WorkoutLog) -> Unit,
    onAddSet: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val firstEntry = summary.entries.first()
    val visuals = ExerciseVisuals.forLog(firstEntry)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, visuals.color.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(visuals.containerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = visuals.icon,
                        contentDescription = visuals.label,
                        tint = visuals.color,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 2.dp)
                    ) {
                        ExerciseCategoryBadge(category = visuals.category)
                    }
                    Text(
                        text = summary.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Text(
                        text = aggregateText(summary),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "+${summary.totalKcal} ккал",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = visuals.color
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Свернуть" else "Развернуть",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (bestLevel != null) {
                        Text(
                            text = "🏅 Уровень атлета: ${bestLevel.label}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    summary.entries.forEach { entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                .padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = Formatters.time(entry.timestamp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(44.dp)
                            )
                            Text(
                                text = workoutDetails(entry),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { onEdit(entry) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Редактировать",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = { onRepeat(entry) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Repeat,
                                    contentDescription = "Повторить",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = { onDelete(entry) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DeleteOutline,
                                    contentDescription = "Удалить",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = { onAddSet(summary.entries.first().exerciseId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Добавить подход")
                    }
                }
            }
        }
    }
}

private fun aggregateText(summary: DayExerciseSummary): String = when (summary.trackingType) {
    TrackingType.REPS, TrackingType.REPS_WEIGHT ->
        "${summary.totalReps} раз · ${summary.sets} " +
            Formatters.plural(summary.sets, "подход", "подхода", "подходов")
    TrackingType.CARDIO_SPEED -> {
        val parts = mutableListOf<String>()
        summary.totalDistance.takeIf { it > 0f }?.let { parts += "${Formatters.number(it)} км" }
        summary.totalMinutes.takeIf { it > 0f }?.let { parts += Formatters.minutes(it) }
        parts += "${summary.sets} " + Formatters.plural(summary.sets, "запись", "записи", "записей")
        parts.joinToString(" · ")
    }
    TrackingType.DURATION ->
        "${Formatters.minutes(summary.totalMinutes)} всего"
}

// ─────────────────────────── Результаты поиска ───────────────────────────

@Composable
private fun SearchResultsView(
    resultGroups: List<HistorySearchResultGroup>,
    onSelectDay: (Long) -> Unit,
    onEdit: (WorkoutLog) -> Unit,
    onRepeat: (WorkoutLog) -> Unit,
    onDelete: (WorkoutLog) -> Unit
) {
    if (resultGroups.isEmpty()) {
        EmptyState(
            icon = Icons.Filled.SearchOff,
            title = "Ничего не найдено",
            subtitle = "Попробуйте изменить запрос: например, «жим», «подтягивания», «80» или «ноябрь»"
        )
    } else {
        val totalSets = resultGroups.sumOf { it.logs.size }
        val daysCount = resultGroups.size
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(key = "search_header") {
                Text(
                    text = "Найдено: $totalSets ${Formatters.plural(totalSets, "подход", "подхода", "подходов")} в $daysCount ${Formatters.plural(daysCount, "дне", "днях", "днях")}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            resultGroups.forEach { group ->
                item(key = "group_${group.dateEpochDay}") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CalendarMonth,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = group.dateTitle,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "+${group.totalCalories} ккал",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                FilledTonalButton(
                                    onClick = { onSelectDay(group.dateEpochDay) },
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text(text = "К этому дню →", style = MaterialTheme.typography.labelSmall)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            group.logs.forEach { log ->
                                val visuals = ExerciseVisuals.forLog(log)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                        .padding(start = 10.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = Formatters.time(log.timestamp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.width(42.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(bottom = 2.dp)
                                        ) {
                                            ExerciseCategoryBadge(category = visuals.category)
                                        }
                                        Text(
                                            text = log.exerciseNameSnapshot,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${workoutDetails(log)}  ·  +${log.caloriesBurned} ккал",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(
                                        onClick = { onEdit(log) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Edit,
                                            contentDescription = "Редактировать",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onRepeat(log) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Repeat,
                                            contentDescription = "Повторить",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDelete(log) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.DeleteOutline,
                                            contentDescription = "Удалить",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item(key = "bottom_space_search") { Spacer(modifier = Modifier.height(84.dp)) }
        }
    }
}
