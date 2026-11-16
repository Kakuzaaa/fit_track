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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.fitnesstracker.ui.LocalAppContainer
import com.example.fitnesstracker.ui.components.EmptyState
import com.example.fitnesstracker.ui.components.WeeklyBarChart
import com.example.fitnesstracker.ui.components.workoutDetails
import com.example.fitnesstracker.util.AthleteLevel
import com.example.fitnesstracker.util.AthleteStandards
import com.example.fitnesstracker.util.CalorieCalculator
import com.example.fitnesstracker.util.Formatters
import java.time.LocalDate

/**
 * Экран «История»: горизонтальная лента дат с точками-индикаторами,
 * сводка выбранного дня и раскрывающийся детальный лог.
 */
@Composable
fun HistoryScreen() {
    val container = LocalAppContainer.current
    val viewModel: HistoryViewModel = viewModel { HistoryViewModel(container.workoutRepository) }

    val dayStats by viewModel.dayStats.collectAsStateWithLifecycle()
    val selectedDay by viewModel.selectedDay.collectAsStateWithLifecycle()
    val dayLogs by viewModel.dayLogs.collectAsStateWithLifecycle()
    val summaries by viewModel.daySummaries.collectAsStateWithLifecycle()
    val weekStats by viewModel.weekStats.collectAsStateWithLifecycle()
    val bestStats by viewModel.bestStats.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        DateStrip(
            statsByDay = dayStats.associateBy { it.dateEpochDay },
            selectedDay = selectedDay,
            onSelect = viewModel::selectDay
        )

        WeeklyChartCard(week = weekStats)

        val totalKcal = dayLogs.sumOf { it.caloriesBurned }
        val cardioMinutes = dayLogs.filter { it.speedKmh != null }
            .sumOf { log ->
                log.durationMinutes?.toDouble()
                    ?: CalorieCalculator.minutesFromDistance(
                        (log.distanceKm ?: 0f).toDouble(),
                        (log.speedKmh ?: 0f).toDouble()
                    )
            }.toFloat()
        DaySummaryCard(
            title = Formatters.dayTitle(selectedDay),
            totalKcal = totalKcal,
            entries = dayLogs.size,
            cardioMinutes = cardioMinutes
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (summaries.isEmpty()) {
                item(key = "empty") {
                    EmptyState(
                        icon = Icons.Filled.History,
                        title = "Нет записей за этот день",
                        subtitle = "Запишите тренировку на экране «Сегодня»"
                    )
                }
            } else {
                items(summaries, key = { it.name }) { summary ->
                    val bestLevel = bestStats[summary.entries.first().exerciseId]?.let {
                        AthleteStandards.levelForStats(it.exerciseId, it.bestReps, it.bestSeconds)
                    }
                    ExpandableExerciseCard(summary = summary, bestLevel = bestLevel)
                }
            }
            item(key = "bottom_space") { Spacer(modifier = Modifier.height(16.dp)) }
        }
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
 * Карточка «Последние 7 дней» с переключателем метрик: ккал / повторения.
 */
@Composable
private fun WeeklyChartCard(week: List<Pair<LocalDate, DayStats>>) {
    var metric by rememberSaveable { mutableIntStateOf(0) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
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
                Text(
                    text = "Последние 7 дней",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
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
            Spacer(modifier = Modifier.height(12.dp))
            WeeklyBarChart(
                days = week.map { it.first },
                values = week.map { if (metric == 0) it.second.totalCalories else it.second.totalReps }
            )
            Spacer(modifier = Modifier.height(8.dp))
            val values = week.map { if (metric == 0) it.second.totalCalories else it.second.totalReps }
            val average = values.sum() / 7
            Text(
                text = "В среднем: ${Formatters.number(average.toFloat())} " +
                    if (metric == 0) "ккал в день" else "повторений в день",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─────────────────────────── Сводка дня ───────────────────────────

@Composable
private fun DaySummaryCard(title: String, totalKcal: Int, entries: Int, cardioMinutes: Float) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
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
private fun ExpandableExerciseCard(summary: DayExerciseSummary, bestLevel: AthleteLevel?) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = trackingIcon(summary.trackingType),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
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
                    color = MaterialTheme.colorScheme.primary
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
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (bestLevel != null) {
                        Text(
                            text = "🏅 Уровень атлета: ${bestLevel.label}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    summary.entries.forEach { entry ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = Formatters.time(entry.timestamp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(52.dp)
                            )
                            Text(
                                text = workoutDetails(entry),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
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
