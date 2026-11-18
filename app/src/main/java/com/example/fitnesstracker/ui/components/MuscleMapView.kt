package com.example.fitnesstracker.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class MuscleGroup(val label: String, val isFront: Boolean) {
    CHEST("Грудь", true),
    FRONT_DELTOID("Передняя дельта", true),
    BICEPS("Бицепс", true),
    FOREARMS("Предплечья", true),
    ABS("Пресс", true),
    OBLIQUES("Косые мышцы", true),
    QUADS("Квадрицепсы", true),
    CALVES_FRONT("Икры (спереди)", true),

    TRAPS("Трапеции", false),
    REAR_DELTOID("Задняя дельта", false),
    TRICEPS("Трицепс", false),
    LATS("Широчайшие", false),
    LOWER_BACK("Поясница", false),
    GLUTES("Ягодицы", false),
    HAMSTRINGS("Бицепс бедра", false),
    CALVES_BACK("Икры (сзади)", false)
}

/**
 * Нормализация текстовых названий мышц в enum [MuscleGroup].
 */
fun parseMuscleGroups(names: List<String>): Set<MuscleGroup> {
    val result = mutableSetOf<MuscleGroup>()
    for (name in names) {
        val lower = name.lowercase()
        when {
            lower.contains("груд") -> result += MuscleGroup.CHEST
            lower.contains("бицепс бедра") -> result += MuscleGroup.HAMSTRINGS
            lower.contains("бицепс") || lower.contains("брахи") -> result += MuscleGroup.BICEPS
            lower.contains("трицепс") -> result += MuscleGroup.TRICEPS
            lower.contains("передн") && lower.contains("дельт") -> result += MuscleGroup.FRONT_DELTOID
            lower.contains("задн") && lower.contains("дельт") -> result += MuscleGroup.REAR_DELTOID
            lower.contains("плеч") || lower.contains("дельт") -> {
                result += MuscleGroup.FRONT_DELTOID
                result += MuscleGroup.REAR_DELTOID
            }
            lower.contains("трапец") || lower.contains("шраг") -> result += MuscleGroup.TRAPS
            lower.contains("широчайш") || (lower.contains("спин") && !lower.contains("поясниц")) -> result += MuscleGroup.LATS
            lower.contains("поясниц") || lower.contains("разгибател") -> result += MuscleGroup.LOWER_BACK
            lower.contains("ягодиц") || lower.contains("мостик") -> result += MuscleGroup.GLUTES
            lower.contains("квадрицепс") || lower.contains("передняя поверхност") -> result += MuscleGroup.QUADS
            lower.contains("бедер") || lower.contains("бедр") || lower.contains("ног") -> {
                result += MuscleGroup.QUADS
                result += MuscleGroup.HAMSTRINGS
            }
            lower.contains("икр") -> {
                result += MuscleGroup.CALVES_FRONT
                result += MuscleGroup.CALVES_BACK
            }
            lower.contains("кос") -> result += MuscleGroup.OBLIQUES
            lower.contains("пресс") || lower.contains("кор") || lower.contains("живот") -> {
                result += MuscleGroup.ABS
                result += MuscleGroup.OBLIQUES
            }
            lower.contains("предплеч") || lower.contains("хват") -> result += MuscleGroup.FOREARMS
        }
    }
    return result
}

/**
 * Интерактивная анатомическая карта тела (вид спереди и сзади)
 * с цветовой подсветкой основных и вспомогательных работающих мышц.
 */
@Composable
fun MuscleMapView(
    primaryMuscleNames: List<String>,
    secondaryMuscleNames: List<String>,
    modifier: Modifier = Modifier
) {
    val primaryGroups = parseMuscleGroups(primaryMuscleNames)
    val secondaryGroups = parseMuscleGroups(secondaryMuscleNames)

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val neutralColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val outlineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Анатомическая карта нагрузки",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                // Легенда
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(primaryColor))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Основная", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(secondaryColor))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Синергист", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Вид спереди
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Canvas(modifier = Modifier.size(width = 110.dp, height = 155.dp)) {
                        drawFrontBody(
                            primary = primaryGroups,
                            secondary = secondaryGroups,
                            primaryColor = primaryColor,
                            secondaryColor = secondaryColor,
                            neutralColor = neutralColor,
                            outlineColor = outlineColor
                        )
                    }
                    Text(
                        text = "Спереди",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Вид сзади
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Canvas(modifier = Modifier.size(width = 110.dp, height = 155.dp)) {
                        drawBackBody(
                            primary = primaryGroups,
                            secondary = secondaryGroups,
                            primaryColor = primaryColor,
                            secondaryColor = secondaryColor,
                            neutralColor = neutralColor,
                            outlineColor = outlineColor
                        )
                    }
                    Text(
                        text = "Сзади",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun colorForGroup(
    group: MuscleGroup,
    primary: Set<MuscleGroup>,
    secondary: Set<MuscleGroup>,
    primaryColor: Color,
    secondaryColor: Color,
    neutralColor: Color
): Color = when {
    group in primary -> primaryColor
    group in secondary -> secondaryColor
    else -> neutralColor
}

private fun DrawScope.drawFrontBody(
    primary: Set<MuscleGroup>,
    secondary: Set<MuscleGroup>,
    primaryColor: Color,
    secondaryColor: Color,
    neutralColor: Color,
    outlineColor: Color
) {
    val w = size.width
    val h = size.height

    // Голова
    drawCircle(
        color = neutralColor,
        radius = h * 0.07f,
        center = Offset(w * 0.5f, h * 0.08f)
    )
    drawCircle(
        color = outlineColor,
        radius = h * 0.07f,
        center = Offset(w * 0.5f, h * 0.08f),
        style = Stroke(width = 1.5f)
    )

    // Шея
    drawRoundRect(
        color = neutralColor,
        topLeft = Offset(w * 0.44f, h * 0.14f),
        size = Size(w * 0.12f, h * 0.05f),
        cornerRadius = CornerRadius(4f, 4f)
    )

    // Передние дельты (Левое и правое плечо)
    val shoulderColor = colorForGroup(MuscleGroup.FRONT_DELTOID, primary, secondary, primaryColor, secondaryColor, neutralColor)
    drawRoundRect(
        color = shoulderColor,
        topLeft = Offset(w * 0.22f, h * 0.18f),
        size = Size(w * 0.14f, h * 0.10f),
        cornerRadius = CornerRadius(10f, 10f)
    )
    drawRoundRect(
        color = shoulderColor,
        topLeft = Offset(w * 0.64f, h * 0.18f),
        size = Size(w * 0.14f, h * 0.10f),
        cornerRadius = CornerRadius(10f, 10f)
    )

    // Бицепсы
    val bicepColor = colorForGroup(MuscleGroup.BICEPS, primary, secondary, primaryColor, secondaryColor, neutralColor)
    drawRoundRect(
        color = bicepColor,
        topLeft = Offset(w * 0.19f, h * 0.29f),
        size = Size(w * 0.12f, h * 0.12f),
        cornerRadius = CornerRadius(8f, 8f)
    )
    drawRoundRect(
        color = bicepColor,
        topLeft = Offset(w * 0.69f, h * 0.29f),
        size = Size(w * 0.12f, h * 0.12f),
        cornerRadius = CornerRadius(8f, 8f)
    )

    // Предплечья
    val forearmColor = colorForGroup(MuscleGroup.FOREARMS, primary, secondary, primaryColor, secondaryColor, neutralColor)
    drawRoundRect(
        color = forearmColor,
        topLeft = Offset(w * 0.16f, h * 0.42f),
        size = Size(w * 0.11f, h * 0.13f),
        cornerRadius = CornerRadius(6f, 6f)
    )
    drawRoundRect(
        color = forearmColor,
        topLeft = Offset(w * 0.73f, h * 0.42f),
        size = Size(w * 0.11f, h * 0.13f),
        cornerRadius = CornerRadius(6f, 6f)
    )

    // Грудь (Левая и правая грудная)
    val chestColor = colorForGroup(MuscleGroup.CHEST, primary, secondary, primaryColor, secondaryColor, neutralColor)
    drawRoundRect(
        color = chestColor,
        topLeft = Offset(w * 0.36f, h * 0.19f),
        size = Size(w * 0.13f, h * 0.11f),
        cornerRadius = CornerRadius(8f, 8f)
    )
    drawRoundRect(
        color = chestColor,
        topLeft = Offset(w * 0.51f, h * 0.19f),
        size = Size(w * 0.13f, h * 0.11f),
        cornerRadius = CornerRadius(8f, 8f)
    )

    // Пресс (Прямая мышца)
    val absColor = colorForGroup(MuscleGroup.ABS, primary, secondary, primaryColor, secondaryColor, neutralColor)
    drawRoundRect(
        color = absColor,
        topLeft = Offset(w * 0.41f, h * 0.31f),
        size = Size(w * 0.18f, h * 0.15f),
        cornerRadius = CornerRadius(6f, 6f)
    )

    // Косые мышцы
    val obliquesColor = colorForGroup(MuscleGroup.OBLIQUES, primary, secondary, primaryColor, secondaryColor, neutralColor)
    drawRoundRect(
        color = obliquesColor,
        topLeft = Offset(w * 0.34f, h * 0.32f),
        size = Size(w * 0.06f, h * 0.14f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    drawRoundRect(
        color = obliquesColor,
        topLeft = Offset(w * 0.60f, h * 0.32f),
        size = Size(w * 0.06f, h * 0.14f),
        cornerRadius = CornerRadius(4f, 4f)
    )

    // Таз
    drawRoundRect(
        color = neutralColor,
        topLeft = Offset(w * 0.36f, h * 0.47f),
        size = Size(w * 0.28f, h * 0.07f),
        cornerRadius = CornerRadius(6f, 6f)
    )

    // Квадрицепсы (Левое и правое бедро)
    val quadsColor = colorForGroup(MuscleGroup.QUADS, primary, secondary, primaryColor, secondaryColor, neutralColor)
    drawRoundRect(
        color = quadsColor,
        topLeft = Offset(w * 0.35f, h * 0.55f),
        size = Size(w * 0.13f, h * 0.22f),
        cornerRadius = CornerRadius(10f, 10f)
    )
    drawRoundRect(
        color = quadsColor,
        topLeft = Offset(w * 0.52f, h * 0.55f),
        size = Size(w * 0.13f, h * 0.22f),
        cornerRadius = CornerRadius(10f, 10f)
    )

    // Колени
    drawCircle(color = neutralColor, radius = h * 0.022f, center = Offset(w * 0.415f, h * 0.785f))
    drawCircle(color = neutralColor, radius = h * 0.022f, center = Offset(w * 0.585f, h * 0.785f))

    // Икры (Спереди)
    val calvesColor = colorForGroup(MuscleGroup.CALVES_FRONT, primary, secondary, primaryColor, secondaryColor, neutralColor)
    drawRoundRect(
        color = calvesColor,
        topLeft = Offset(w * 0.36f, h * 0.81f),
        size = Size(w * 0.11f, h * 0.17f),
        cornerRadius = CornerRadius(8f, 8f)
    )
    drawRoundRect(
        color = calvesColor,
        topLeft = Offset(w * 0.53f, h * 0.81f),
        size = Size(w * 0.11f, h * 0.17f),
        cornerRadius = CornerRadius(8f, 8f)
    )
}

private fun DrawScope.drawBackBody(
    primary: Set<MuscleGroup>,
    secondary: Set<MuscleGroup>,
    primaryColor: Color,
    secondaryColor: Color,
    neutralColor: Color,
    outlineColor: Color
) {
    val w = size.width
    val h = size.height

    // Голова
    drawCircle(
        color = neutralColor,
        radius = h * 0.07f,
        center = Offset(w * 0.5f, h * 0.08f)
    )
    drawCircle(
        color = outlineColor,
        radius = h * 0.07f,
        center = Offset(w * 0.5f, h * 0.08f),
        style = Stroke(width = 1.5f)
    )

    // Трапеции
    val trapsColor = colorForGroup(MuscleGroup.TRAPS, primary, secondary, primaryColor, secondaryColor, neutralColor)
    val trapsPath = Path().apply {
        moveTo(w * 0.5f, h * 0.14f)
        lineTo(w * 0.36f, h * 0.19f)
        lineTo(w * 0.5f, h * 0.29f)
        lineTo(w * 0.64f, h * 0.19f)
        close()
    }
    drawPath(path = trapsPath, color = trapsColor, style = Fill)

    // Задние дельты
    val rearDeltoidColor = colorForGroup(MuscleGroup.REAR_DELTOID, primary, secondary, primaryColor, secondaryColor, neutralColor)
    drawRoundRect(
        color = rearDeltoidColor,
        topLeft = Offset(w * 0.22f, h * 0.18f),
        size = Size(w * 0.14f, h * 0.10f),
        cornerRadius = CornerRadius(10f, 10f)
    )
    drawRoundRect(
        color = rearDeltoidColor,
        topLeft = Offset(w * 0.64f, h * 0.18f),
        size = Size(w * 0.14f, h * 0.10f),
        cornerRadius = CornerRadius(10f, 10f)
    )

    // Трицепсы
    val tricepColor = colorForGroup(MuscleGroup.TRICEPS, primary, secondary, primaryColor, secondaryColor, neutralColor)
    drawRoundRect(
        color = tricepColor,
        topLeft = Offset(w * 0.19f, h * 0.29f),
        size = Size(w * 0.12f, h * 0.12f),
        cornerRadius = CornerRadius(8f, 8f)
    )
    drawRoundRect(
        color = tricepColor,
        topLeft = Offset(w * 0.69f, h * 0.29f),
        size = Size(w * 0.12f, h * 0.12f),
        cornerRadius = CornerRadius(8f, 8f)
    )

    // Широчайшие (Lats)
    val latsColor = colorForGroup(MuscleGroup.LATS, primary, secondary, primaryColor, secondaryColor, neutralColor)
    drawRoundRect(
        color = latsColor,
        topLeft = Offset(w * 0.33f, h * 0.25f),
        size = Size(w * 0.15f, h * 0.15f),
        cornerRadius = CornerRadius(8f, 8f)
    )
    drawRoundRect(
        color = latsColor,
        topLeft = Offset(w * 0.52f, h * 0.25f),
        size = Size(w * 0.15f, h * 0.15f),
        cornerRadius = CornerRadius(8f, 8f)
    )

    // Поясница
    val lowerBackColor = colorForGroup(MuscleGroup.LOWER_BACK, primary, secondary, primaryColor, secondaryColor, neutralColor)
    drawRoundRect(
        color = lowerBackColor,
        topLeft = Offset(w * 0.40f, h * 0.39f),
        size = Size(w * 0.20f, h * 0.09f),
        cornerRadius = CornerRadius(6f, 6f)
    )

    // Ягодицы (Glutes)
    val glutesColor = colorForGroup(MuscleGroup.GLUTES, primary, secondary, primaryColor, secondaryColor, neutralColor)
    drawRoundRect(
        color = glutesColor,
        topLeft = Offset(w * 0.35f, h * 0.48f),
        size = Size(w * 0.14f, h * 0.11f),
        cornerRadius = CornerRadius(10f, 10f)
    )
    drawRoundRect(
        color = glutesColor,
        topLeft = Offset(w * 0.51f, h * 0.48f),
        size = Size(w * 0.14f, h * 0.11f),
        cornerRadius = CornerRadius(10f, 10f)
    )

    // Бицепс бедра (Hamstrings)
    val hamstringsColor = colorForGroup(MuscleGroup.HAMSTRINGS, primary, secondary, primaryColor, secondaryColor, neutralColor)
    drawRoundRect(
        color = hamstringsColor,
        topLeft = Offset(w * 0.35f, h * 0.60f),
        size = Size(w * 0.13f, h * 0.18f),
        cornerRadius = CornerRadius(10f, 10f)
    )
    drawRoundRect(
        color = hamstringsColor,
        topLeft = Offset(w * 0.52f, h * 0.60f),
        size = Size(w * 0.13f, h * 0.18f),
        cornerRadius = CornerRadius(10f, 10f)
    )

    // Икры (Сзади)
    val calvesColor = colorForGroup(MuscleGroup.CALVES_BACK, primary, secondary, primaryColor, secondaryColor, neutralColor)
    drawRoundRect(
        color = calvesColor,
        topLeft = Offset(w * 0.35f, h * 0.80f),
        size = Size(w * 0.12f, h * 0.18f),
        cornerRadius = CornerRadius(8f, 8f)
    )
    drawRoundRect(
        color = calvesColor,
        topLeft = Offset(w * 0.53f, h * 0.80f),
        size = Size(w * 0.12f, h * 0.18f),
        cornerRadius = CornerRadius(8f, 8f)
    )
}
