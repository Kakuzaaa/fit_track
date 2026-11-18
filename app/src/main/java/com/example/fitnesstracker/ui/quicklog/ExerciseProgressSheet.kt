package com.example.fitnesstracker.ui.quicklog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.domain.model.Exercise
import com.example.fitnesstracker.domain.model.TrackingType
import com.example.fitnesstracker.domain.model.WorkoutLog
import com.example.fitnesstracker.ui.components.WeeklyBarChart
import com.example.fitnesstracker.ui.components.categoryIcon
import com.example.fitnesstracker.util.Formatters
import com.example.fitnesstracker.util.OneRepMaxCalculator
import java.time.LocalDate

/**
 * Шторка аналитики прогресса по конкретному упражнению:
 * личные рекорды (PR), расчёт 1RM, таблица процентов рабочих весов
 * и график динамики подходов.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseProgressSheet(
    exercise: Exercise,
    logs: List<WorkoutLog>,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // Шапка
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon(exercise.category),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    exercise.gripType?.let { grip ->
                        Text(
                            text = grip,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = "Закрыть")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (logs.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Пока нет записей по этому упражнению",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Запишите первый подход, и здесь появятся ваши личные рекорды и расчёт 1RM!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // 1. Карточки личных рекордов (PR)
                RecordsSection(exercise = exercise, logs = logs)

                // 2. Расчёт 1RM и таблица процентов (для упражнений с весом)
                if (exercise.trackingType == TrackingType.REPS_WEIGHT) {
                    val max1Rm = logs.mapNotNull { log ->
                        val w = log.weightKg
                        val r = log.reps
                        if (w != null && w > 0f && r != null && r > 0) {
                            OneRepMaxCalculator.calculate1RmEpley(w, r)
                        } else null
                    }.maxOrNull() ?: 0f

                    if (max1Rm > 0f) {
                        Spacer(modifier = Modifier.height(16.dp))
                        OneRepMaxSection(oneRm = max1Rm)
                    }
                }

                // 3. График динамики по последним записям
                Spacer(modifier = Modifier.height(16.dp))
                ProgressChartSection(exercise = exercise, logs = logs)

                // 4. История последних подходов
                Spacer(modifier = Modifier.height(16.dp))
                RecentSetsSection(logs = logs.takeLast(10).reversed())
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun RecordsSection(exercise: Exercise, logs: List<WorkoutLog>) {
    Text(
        text = "Личные рекорды (PR)",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (exercise.trackingType) {
            TrackingType.REPS_WEIGHT -> {
                val maxWeight = logs.mapNotNull { it.weightKg }.maxOrNull() ?: 0f
                val maxReps = logs.mapNotNull { it.reps }.maxOrNull() ?: 0
                val totalTonnage = logs.sumOf { ((it.weightKg ?: 0f) * (it.reps ?: 0)).toDouble() }.toInt()

                RecordCard(
                    title = "Макс. вес",
                    value = "${Formatters.number(maxWeight)} кг",
                    subtitle = "Лучший вес",
                    modifier = Modifier.weight(1f)
                )
                RecordCard(
                    title = "Макс. повторов",
                    value = "$maxReps",
                    subtitle = "В одном подходе",
                    modifier = Modifier.weight(1f)
                )
                RecordCard(
                    title = "Общий объём",
                    value = "$totalTonnage кг",
                    subtitle = "Всего поднято",
                    modifier = Modifier.weight(1f)
                )
            }
            TrackingType.REPS -> {
                val maxReps = logs.mapNotNull { it.reps }.maxOrNull() ?: 0
                val totalReps = logs.sumOf { it.reps ?: 0 }

                RecordCard(
                    title = "Макс. за подход",
                    value = "$maxReps",
                    subtitle = "Рекорд повторений",
                    modifier = Modifier.weight(1f)
                )
                RecordCard(
                    title = "Всего повторов",
                    value = "$totalReps",
                    subtitle = "За всё время",
                    modifier = Modifier.weight(1f)
                )
                RecordCard(
                    title = "Подходов",
                    value = "${logs.size}",
                    subtitle = "Всего выполнено",
                    modifier = Modifier.weight(1f)
                )
            }
            TrackingType.DURATION -> {
                val maxDuration = logs.mapNotNull { it.durationMinutes }.maxOrNull() ?: 0f
                val totalDuration = logs.sumOf { (it.durationMinutes ?: 0f).toDouble() }.toFloat()

                RecordCard(
                    title = "Макс. время",
                    value = Formatters.minutes(maxDuration),
                    subtitle = "Рекорд удержания",
                    modifier = Modifier.weight(1f)
                )
                RecordCard(
                    title = "Всего времени",
                    value = Formatters.minutes(totalDuration),
                    subtitle = "Суммарно",
                    modifier = Modifier.weight(1f)
                )
            }
            TrackingType.CARDIO_SPEED -> {
                val maxDist = logs.mapNotNull { it.distanceKm }.maxOrNull() ?: 0f
                val maxSpeed = logs.mapNotNull { it.speedKmh }.maxOrNull() ?: 0f

                RecordCard(
                    title = "Макс. дистанция",
                    value = "${Formatters.number(maxDist)} км",
                    subtitle = "За одну тренировку",
                    modifier = Modifier.weight(1f)
                )
                RecordCard(
                    title = "Макс. скорость",
                    value = "${Formatters.number(maxSpeed)} км/ч",
                    subtitle = "Пиковый темп",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun RecordCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun OneRepMaxSection(oneRm: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.FitnessCenter,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Одноповторный максимум (1RM): ${Formatters.number(oneRm)} кг",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Расчёт по формуле Эпли на основе ваших лучших подходов. Таблица рабочих весов:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
            )

            val table = OneRepMaxCalculator.percentagesTable(oneRm)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                table.take(4).forEach { (pct, reps, weight) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$pct%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${Formatters.number(weight)} кг",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "~$reps повт",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressChartSection(exercise: Exercise, logs: List<WorkoutLog>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Динамика прогресса",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            val recentDays = logs.groupBy { it.dateEpochDay }
                .entries
                .sortedBy { it.key }
                .takeLast(7)
            val days = recentDays.map { LocalDate.ofEpochDay(it.key) }
            val values = recentDays.map { entry ->
                val dayLogs = entry.value
                when (exercise.trackingType) {
                    TrackingType.REPS_WEIGHT -> dayLogs.mapNotNull { it.weightKg }.maxOrNull()?.toInt() ?: 0
                    TrackingType.REPS -> dayLogs.sumOf { it.reps ?: 0 }
                    TrackingType.DURATION -> dayLogs.sumOf { ((it.durationMinutes ?: 0f) * 60).toInt() }
                    TrackingType.CARDIO_SPEED -> dayLogs.sumOf { (it.distanceKm ?: 0f).toInt() }
                }
            }

            WeeklyBarChart(
                days = days,
                values = values
            )
        }
    }
}

@Composable
private fun RecentSetsSection(logs: List<WorkoutLog>) {
    Text(
        text = "Последние подходы",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(modifier = Modifier.height(8.dp))

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        logs.forEach { log ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Formatters.dayTitle(log.dateEpochDay),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                val detail = when {
                    log.weightKg != null && log.reps != null ->
                        "${log.reps} раз × ${Formatters.number(log.weightKg)} кг"
                    log.reps != null -> "${log.reps} раз"
                    log.durationMinutes != null -> Formatters.minutes(log.durationMinutes)
                    log.distanceKm != null -> "${Formatters.number(log.distanceKm)} км"
                    else -> ""
                }
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "+${log.caloriesBurned} ккал",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
