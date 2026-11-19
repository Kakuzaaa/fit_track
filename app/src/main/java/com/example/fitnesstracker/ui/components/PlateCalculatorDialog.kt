package com.example.fitnesstracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitnesstracker.util.Formatters
import com.example.fitnesstracker.util.PlateCalculationResult
import com.example.fitnesstracker.util.PlateCalculator
import com.example.fitnesstracker.util.PlateItem

private val BARBELL_OPTIONS = listOf(
    20f to "20 кг (Олимпийский)",
    15f to "15 кг (Женский)",
    10f to "10 кг (EZ-гриф)",
    8f to "8 кг (Лёгкий)"
)

/**
 * Интерактивный калькулятор дисков на штангу (Barbell Plate Calculator)
 * с визуализацией втулки грифа и цветных олимпийских дисков.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlateCalculatorDialog(
    initialWeightKg: Float,
    onApplyWeight: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    var targetWeight by remember { mutableFloatStateOf(maxOf(20f, initialWeightKg)) }
    var barWeight by remember { mutableFloatStateOf(20f) }

    val result = remember(targetWeight, barWeight) {
        PlateCalculator.calculate(targetWeightKg = targetWeight, barWeightKg = barWeight)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🏋️", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Калькулятор блинов",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = "Закрыть")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Визуализация штанги в Canvas
                BarbellVisualizer(result = result)

                Spacer(modifier = Modifier.height(14.dp))

                // Целевой вес: поле ввода + кнопки шага
                OutlinedTextField(
                    value = Formatters.number(targetWeight),
                    onValueChange = { input ->
                        input.replace(',', '.').toFloatOrNull()?.let {
                            targetWeight = maxOf(0f, it)
                        }
                    },
                    suffix = { Text("кг", fontWeight = FontWeight.SemiBold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = LocalTextStyle.current.copy(
                        textAlign = TextAlign.Center,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.width(180.dp)
                )

                Text(
                    text = "На сторону: ${Formatters.number(result.weightPerSideKg)} кг  ·  Гриф: ${Formatters.number(barWeight)} кг",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Кнопки быстрого изменения веса
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(-10f, -2.5f, 2.5f, 10f).forEach { step ->
                        val label = if (step > 0) "+${Formatters.number(step)}" else Formatters.number(step)
                        FilledTonalButton(
                            onClick = {
                                targetWeight = maxOf(barWeight, targetWeight + step)
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = label, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Выбор веса грифа
                Text(
                    text = "Вес грифа:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BARBELL_OPTIONS.forEach { (weight, label) ->
                        FilterChip(
                            selected = barWeight == weight,
                            onClick = {
                                barWeight = weight
                                if (targetWeight < weight) targetWeight = weight
                            },
                            label = { Text(text = label, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Набор дисков на сторону
                if (result.platesPerSide.isEmpty()) {
                    Text(
                        text = "Только пустой гриф",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Блины на каждую сторону:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        result.platesPerSide.forEach { plate ->
                            PlateBadge(plate = plate)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onApplyWeight(targetWeight)
                    onDismiss()
                },
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(text = "Применить ${Formatters.number(targetWeight)} кг")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Отмена")
            }
        }
    )
}

/**
 * Отрисовка втулки штанги и надетых блинов в Canvas.
 */
@Composable
private fun BarbellVisualizer(result: PlateCalculationResult) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2125))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(90.dp)) {
                val centerY = size.height / 2f
                val sleeveHeight = 14.dp.toPx()
                val collarWidth = 14.dp.toPx()
                val collarHeight = 36.dp.toPx()

                // Гриф (слева)
                val shaftWidth = 40.dp.toPx()
                val shaftHeight = 10.dp.toPx()
                drawRoundRect(
                    color = Color(0xFF757575),
                    topLeft = Offset(0f, centerY - shaftHeight / 2f),
                    size = Size(shaftWidth, shaftHeight),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )

                // Стопорное кольцо (Collar ring)
                val collarLeft = shaftWidth
                drawRoundRect(
                    color = Color(0xFFB0BEC5),
                    topLeft = Offset(collarLeft, centerY - collarHeight / 2f),
                    size = Size(collarWidth, collarHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )

                // Втулка (Sleeve)
                val sleeveLeft = collarLeft + collarWidth
                val sleeveWidth = size.width - sleeveLeft - 8.dp.toPx()
                drawRoundRect(
                    color = Color(0xFF90A4AE),
                    topLeft = Offset(sleeveLeft, centerY - sleeveHeight / 2f),
                    size = Size(sleeveWidth, sleeveHeight),
                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                )

                // Надетые блины от стопора наружу
                var currentX = sleeveLeft + 2.dp.toPx()
                val maxPlateHeight = size.height - 8.dp.toPx()

                // Разворачиваем блины: каждый блин отрисовывается отдельно
                for (plate in result.platesPerSide) {
                    for (i in 0 until plate.count) {
                        val plateWidth = 12.dp.toPx()
                        val plateHeight = maxPlateHeight * plate.heightFraction
                        val plateTop = centerY - plateHeight / 2f

                        drawRoundRect(
                            color = plate.color,
                            topLeft = Offset(currentX, plateTop),
                            size = Size(plateWidth, plateHeight),
                            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                        )
                        // Обводка диска для разделения
                        drawRoundRect(
                            color = Color(0x55000000),
                            topLeft = Offset(currentX, plateTop),
                            size = Size(plateWidth, plateHeight),
                            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                        )
                        currentX += plateWidth + 2.dp.toPx()
                    }
                }

                // Замок на конце (если есть блины)
                if (result.platesPerSide.isNotEmpty() && currentX + 8.dp.toPx() < size.width) {
                    val clampWidth = 8.dp.toPx()
                    val clampHeight = 26.dp.toPx()
                    drawRoundRect(
                        color = Color(0xFFE53935),
                        topLeft = Offset(currentX, centerY - clampHeight / 2f),
                        size = Size(clampWidth, clampHeight),
                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                    )
                }
            }
        }
    }
}

/**
 * Бейдж отдельного номинала диска с олимпийским цветом.
 */
@Composable
private fun PlateBadge(plate: PlateItem) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = plate.color,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33000000))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${Formatters.number(plate.weightKg)} кг",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = plate.labelColor
            )
            Spacer(modifier = Modifier.width(4.dp))
            Surface(
                shape = CircleShape,
                color = plate.labelColor.copy(alpha = 0.25f)
            ) {
                Text(
                    text = "×${plate.count}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = plate.labelColor,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                )
            }
        }
    }
}
