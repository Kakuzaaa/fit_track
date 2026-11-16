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
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.util.Formatters
import java.time.LocalDate

private const val CHART_BAR_HEIGHT_DP = 96

/**
 * Столбчатая диаграмма за 7 дней: подпись значения над столбцом,
 * день недели под ним, «сегодня» выделено цветом.
 */
@Composable
fun WeeklyBarChart(
    days: List<LocalDate>,
    values: List<Int>,
    modifier: Modifier = Modifier
) {
    val maxValue = (values.maxOrNull() ?: 0).coerceAtLeast(1)
    val today = LocalDate.now()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height((CHART_BAR_HEIGHT_DP + 52).dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        days.forEachIndexed { index, date ->
            val value = values.getOrNull(index) ?: 0
            val isToday = date == today
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = Formatters.number(value.toFloat()),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (value > 0) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
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
                    val fraction by animateFloatAsState(
                        targetValue = value.toFloat() / maxValue,
                        animationSpec = tween(durationMillis = 450),
                        label = "bar"
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(
                                (CHART_BAR_HEIGHT_DP.dp * fraction)
                                    .coerceAtLeast(if (value > 0) 6.dp else 3.dp)
                            )
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when {
                                    value == 0 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                                    isToday -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                                }
                            )
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = Formatters.weekdayShort(date),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isToday) androidx.compose.ui.text.font.FontWeight.SemiBold else null,
                    color = if (isToday) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}
