package com.example.fitnesstracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitnesstracker.domain.model.DayStats
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private const val WEEKS_COUNT = 16
private val CELL_SIZE = 14.dp
private val CELL_GAP = 3.dp
private val CELL_SHAPE = RoundedCornerShape(3.dp)

private val COLOR_LEVEL_0 = Color(0x22FFFFFF)
private val COLOR_LEVEL_1 = Color(0xFF1B4D20) // 1-2 подхода
private val COLOR_LEVEL_2 = Color(0xFF2E7D32) // 3-5 подходов
private val COLOR_LEVEL_3 = Color(0xFF43A047) // 6-9 подходов
private val COLOR_LEVEL_4 = Color(0xFF76FF03) // 10+ подходов (неоновый лайм)

/**
 * Карта активности тренировок в стиле GitHub (Workout Contribution Heatmap).
 * Сетка из 7 строк (дни недели Пн-Вс) и [WEEKS_COUNT] столбцов (недели).
 */
@Composable
fun WorkoutHeatmap(
    statsByDay: Map<Long, DayStats>,
    selectedEpochDay: Long,
    onSelectDay: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val currentMonday = today.minusDays((today.dayOfWeek.value - 1).toLong())
    val startMonday = currentMonday.minusWeeks((WEEKS_COUNT - 1).toLong())

    val scrollState = rememberScrollState()
    LaunchedEffect(Unit) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    val totalActiveDays = androidx.compose.runtime.remember(statsByDay) {
        (0L until WEEKS_COUNT * 7).count { dayOffset ->
            val d = startMonday.plusDays(dayOffset)
            !d.isAfter(today) && (statsByDay[d.toEpochDay()]?.entries ?: 0) > 0
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            verticalAlignment = Alignment.Top
        ) {
            // Метки дней недели (Пн, Ср, Пт)
            Column(
                modifier = Modifier.padding(end = 6.dp, top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(CELL_GAP)
            ) {
                listOf(
                    0 to "Пн",
                    1 to "",
                    2 to "Ср",
                    3 to "",
                    4 to "Пт",
                    5 to "",
                    6 to ""
                ).forEach { (_, label) ->
                    Box(
                        modifier = Modifier.size(width = 18.dp, height = CELL_SIZE),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        if (label.isNotEmpty()) {
                            Text(
                                text = label,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // Столбцы недель
            Row(horizontalArrangement = Arrangement.spacedBy(CELL_GAP)) {
                for (weekIndex in 0 until WEEKS_COUNT) {
                    val weekMonday = startMonday.plusWeeks(weekIndex.toLong())

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(CELL_GAP)
                    ) {
                        // Заголовок месяца
                        val showMonthHeader = weekMonday.dayOfMonth in 1..7 || weekIndex == 0
                        val monthLabel = if (showMonthHeader) {
                            weekMonday.month.getDisplayName(TextStyle.SHORT, Locale("ru"))
                                .replaceFirstChar { it.uppercase() }
                        } else ""

                        Box(
                            modifier = Modifier
                                .width(CELL_SIZE)
                                .height(16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (monthLabel.isNotEmpty()) {
                                Text(
                                    text = monthLabel,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }

                        // 7 дней недели (строки)
                        for (dayOfWeekIndex in 0 until 7) {
                            val dayDate = weekMonday.plusDays(dayOfWeekIndex.toLong())
                            val epochDay = dayDate.toEpochDay()
                            val isFuture = dayDate.isAfter(today)
                            val isToday = dayDate == today
                            val isSelected = epochDay == selectedEpochDay

                            val stats = if (isFuture) null else statsByDay[epochDay]
                            val entries = stats?.entries ?: 0

                            val cellColor = when {
                                isFuture -> Color.Transparent
                                entries == 0 -> COLOR_LEVEL_0
                                entries in 1..2 -> COLOR_LEVEL_1
                                entries in 3..5 -> COLOR_LEVEL_2
                                entries in 6..9 -> COLOR_LEVEL_3
                                else -> COLOR_LEVEL_4
                            }

                            val borderModifier = when {
                                isSelected -> Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, CELL_SHAPE)
                                isToday -> Modifier.border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), CELL_SHAPE)
                                else -> Modifier
                            }

                            Box(
                                modifier = Modifier
                                    .size(CELL_SIZE)
                                    .clip(CELL_SHAPE)
                                    .background(cellColor)
                                    .then(borderModifier)
                                    .clickable(enabled = !isFuture) { onSelectDay(epochDay) }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Легенда и статистика активности
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Активных дней: $totalActiveDays за $WEEKS_COUNT нед.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Меньше",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                listOf(COLOR_LEVEL_0, COLOR_LEVEL_1, COLOR_LEVEL_2, COLOR_LEVEL_3, COLOR_LEVEL_4).forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(color)
                    )
                }
                Text(
                    text = "Больше",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
