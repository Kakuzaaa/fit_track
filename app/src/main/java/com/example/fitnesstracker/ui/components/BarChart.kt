package com.example.fitnesstracker.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.util.Formatters
import java.time.LocalDate

private const val CHART_BAR_HEIGHT_DP = 96

/**
 * Современная столбчатая диаграмма за 7 дней (стиль спортивных трекеров):
 *  - полупрозрачные фоновые колонки (треки), придающие диаграмме глубину;
 *  - плавные скруглённые вершины заполненных столбцов;
 *  - чёткая подсветка текущего или выбранного в ленте дня.
 */
@Composable
fun WeeklyBarChart(
    days: List<LocalDate>,
    values: List<Int>,
    modifier: Modifier = Modifier,
    selectedDate: LocalDate? = null
) {
    val maxValue = (values.maxOrNull() ?: 0).coerceAtLeast(1)
    val today = LocalDate.now()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height((CHART_BAR_HEIGHT_DP + 54).dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        days.forEachIndexed { index, date ->
            val value = values.getOrNull(index) ?: 0
            val isToday = date == today
            val isSelected = selectedDate != null && date == selectedDate
            val isHighlighted = isSelected || (selectedDate == null && isToday)

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = if (value > 0) Formatters.number(value.toFloat()) else "0",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isHighlighted && value > 0) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        isHighlighted && value > 0 -> MaterialTheme.colorScheme.primary
                        value > 0 -> MaterialTheme.colorScheme.onSurface
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                    },
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(CHART_BAR_HEIGHT_DP.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    // Фоновый вертикальный трек-пилон для ощущения глубины
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(CHART_BAR_HEIGHT_DP.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    isHighlighted -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                }
                            )
                    )

                    // Анимированный заполненный столбец
                    val fraction by animateFloatAsState(
                        targetValue = if (maxValue > 0) value.toFloat() / maxValue else 0f,
                        animationSpec = tween(durationMillis = 450),
                        label = "bar"
                    )
                    if (value > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(
                                    (CHART_BAR_HEIGHT_DP.dp * fraction)
                                        .coerceAtLeast(8.dp)
                                )
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                                .background(
                                    when {
                                        isHighlighted -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                                    }
                                )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = Formatters.weekdayShort(date),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        isHighlighted -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}
