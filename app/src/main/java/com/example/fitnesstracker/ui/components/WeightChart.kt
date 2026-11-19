package com.example.fitnesstracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitnesstracker.domain.model.WeightLog
import com.example.fitnesstracker.util.Formatters
import java.time.LocalDate

/**
 * График динамики изменения веса тела во времени (Canvas line chart).
 */
@Composable
fun WeightChart(
    weights: List<WeightLog>,
    modifier: Modifier = Modifier
) {
    if (weights.isEmpty()) return

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface

    val sorted = weights.sortedBy { it.dateEpochDay }
    val minVal = (sorted.minOf { it.weightKg } - 0.8).toFloat()
    val maxVal = (sorted.maxOf { it.weightKg } + 0.8).toFloat()
    val range = maxOf(0.1f, maxVal - minVal)

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            val width = size.width
            val height = size.height

            if (sorted.size == 1) {
                // Одна точка
                val cy = height / 2f
                val cx = width / 2f
                drawCircle(color = primaryColor, radius = 6.dp.toPx(), center = Offset(cx, cy))
                drawCircle(color = surfaceColor, radius = 3.dp.toPx(), center = Offset(cx, cy))
                return@Canvas
            }

            val stepX = width / (sorted.size - 1)
            val points = sorted.mapIndexed { index, item ->
                val x = index * stepX
                val normalizedY = ((item.weightKg - minVal) / range).toFloat()
                val y = height - (normalizedY * height)
                Offset(x, y)
            }

            // Фоновая сетка: верхняя и нижняя линии
            drawLine(
                color = Color.White.copy(alpha = 0.08f),
                start = Offset(0f, 0f),
                end = Offset(width, 0f),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = Color.White.copy(alpha = 0.08f),
                start = Offset(0f, height),
                end = Offset(width, height),
                strokeWidth = 1.dp.toPx()
            )

            // Градиентная заливка под графиком
            val fillPath = Path().apply {
                moveTo(points.first().x, height)
                points.forEach { lineTo(it.x, it.y) }
                lineTo(points.last().x, height)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.35f), Color.Transparent),
                    startY = 0f,
                    endY = height
                )
            )

            // Основная линия графика
            val linePath = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    val prev = points[i - 1]
                    val curr = points[i]
                    val midX = (prev.x + curr.x) / 2f
                    cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                }
            }
            drawPath(
                path = linePath,
                color = primaryColor,
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Точки измерений
            points.forEach { pt ->
                drawCircle(
                    color = primaryColor,
                    radius = 4.5.dp.toPx(),
                    center = pt
                )
                drawCircle(
                    color = surfaceColor,
                    radius = 2.dp.toPx(),
                    center = pt
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Даты первой и последней записи
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val firstDate = LocalDate.ofEpochDay(sorted.first().dateEpochDay)
            val lastDate = LocalDate.ofEpochDay(sorted.last().dateEpochDay)
            Text(
                text = "${firstDate.dayOfMonth}.${firstDate.monthValue.toString().padStart(2, '0')}: ${Formatters.number(sorted.first().weightKg.toFloat())} кг",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${lastDate.dayOfMonth}.${lastDate.monthValue.toString().padStart(2, '0')}: ${Formatters.number(sorted.last().weightKg.toFloat())} кг",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
