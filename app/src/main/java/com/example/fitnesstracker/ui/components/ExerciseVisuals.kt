package com.example.fitnesstracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SportsGymnastics
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitnesstracker.domain.model.ExerciseCategory
import com.example.fitnesstracker.domain.model.WorkoutLog

data class CategoryVisuals(
    val category: ExerciseCategory,
    val label: String,
    val emoji: String,
    val color: Color,
    val containerColor: Color,
    val icon: ImageVector
)

object ExerciseVisuals {

    val BARBELL = CategoryVisuals(
        category = ExerciseCategory.BARBELL,
        label = "Штанга",
        emoji = "🏋️",
        color = Color(0xFFEF5350), // Кораллово-красный
        containerColor = Color(0x28EF5350),
        icon = Icons.Filled.FitnessCenter
    )

    val DUMBBELLS = CategoryVisuals(
        category = ExerciseCategory.DUMBBELLS,
        label = "Гантели",
        emoji = "🔩",
        color = Color(0xFFBA68C8), // Аметистово-фиолетовый
        containerColor = Color(0x28BA68C8),
        icon = Icons.Filled.FitnessCenter
    )

    val PULL_UP_BAR = CategoryVisuals(
        category = ExerciseCategory.PULL_UP_BAR,
        label = "Турник",
        emoji = "🤸",
        color = Color(0xFF29B6F6), // Небесно-голубой
        containerColor = Color(0x2829B6F6),
        icon = Icons.Filled.SportsGymnastics
    )

    val ABS = CategoryVisuals(
        category = ExerciseCategory.ABS,
        label = "Пресс",
        emoji = "⚡",
        color = Color(0xFFFFB74D), // Янтарно-золотой
        containerColor = Color(0x28FFB74D),
        icon = Icons.Filled.Bolt
    )

    val CARDIO = CategoryVisuals(
        category = ExerciseCategory.CARDIO,
        label = "Кардио",
        emoji = "🏃",
        color = Color(0xFF00E676), // Неоново-изумрудный
        containerColor = Color(0x2800E676),
        icon = Icons.Filled.DirectionsRun
    )

    val BODYWEIGHT = CategoryVisuals(
        category = ExerciseCategory.BODYWEIGHT,
        label = "Свой вес",
        emoji = "🧘",
        color = Color(0xFF26C6DA), // Морской циан / бирюза
        containerColor = Color(0x2826C6DA),
        icon = Icons.Filled.SelfImprovement
    )

    fun forCategory(category: ExerciseCategory): CategoryVisuals = when (category) {
        ExerciseCategory.BARBELL -> BARBELL
        ExerciseCategory.DUMBBELLS -> DUMBBELLS
        ExerciseCategory.PULL_UP_BAR -> PULL_UP_BAR
        ExerciseCategory.ABS -> ABS
        ExerciseCategory.CARDIO -> CARDIO
        ExerciseCategory.BODYWEIGHT -> BODYWEIGHT
    }

    fun forLog(log: WorkoutLog): CategoryVisuals {
        val category = resolveCategory(log.exerciseId, log.exerciseNameSnapshot, log.speedKmh)
        return forCategory(category)
    }

    fun resolveCategory(exerciseId: Long, name: String, speedKmh: Float? = null): ExerciseCategory {
        if (speedKmh != null) return ExerciseCategory.CARDIO

        val fromPredefined = com.example.fitnesstracker.data.prepopulate.DefaultExercises.idToCategory[exerciseId]
        if (fromPredefined != null) return fromPredefined

        val lower = name.lowercase()
        return when {
            lower.contains("штанга") || lower.contains("жим лёжа") || lower.contains("становая") ||
                lower.contains("присед со штангой") || lower.contains("good morning") ||
                lower.contains("румынская") || lower.contains("армейский") ||
                (exerciseId in 52L..63L) || (exerciseId in 69L..73L) -> ExerciseCategory.BARBELL

            lower.contains("гантел") || lower.contains("молот") || lower.contains("арнольд") ||
                lower.contains("разводка") || lower.contains("пуловер") || lower.contains("шраги") ||
                (exerciseId in 13L..20L) || (exerciseId in 74L..79L) -> ExerciseCategory.DUMBBELLS

            lower.contains("турник") || lower.contains("подтягивани") || lower.contains("брусь") ||
                lower.contains("кольца") || lower.contains("вис") || lower.contains("выход силой") ||
                (exerciseId in 8L..12L) || (exerciseId in 27L..35L) || (exerciseId in 80L..81L) -> ExerciseCategory.PULL_UP_BAR

            lower.contains("пресс") || lower.contains("скручиван") || lower.contains("планк") ||
                lower.contains("велосипед") || lower.contains("лодочк") || lower.contains("жук") ||
                lower.contains("вакуум") || lower.contains("твист") || lower.contains("корпус") ||
                lower.contains("складка") || lower.contains("кошка") || lower.contains("собака") ||
                (exerciseId in 21L..24L) || (exerciseId in 42L..43L) || (exerciseId in 45L..47L) ||
                (exerciseId in 49L..49L) || (exerciseId in 64L..68L) -> ExerciseCategory.ABS

            lower.contains("бег") || lower.contains("ходьб") || lower.contains("скакалк") ||
                lower.contains("кардио") || (exerciseId in 25L..26L) -> ExerciseCategory.CARDIO

            else -> ExerciseCategory.BODYWEIGHT
        }
    }
}

/**
 * Цветной бейдж категории упражнения (Штанга, Гантели, Турник, Пресс, Кардио, Свой вес).
 */
@Composable
fun ExerciseCategoryBadge(
    category: ExerciseCategory,
    modifier: Modifier = Modifier
) {
    val visuals = ExerciseVisuals.forCategory(category)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(visuals.containerColor)
            .border(1.dp, visuals.color.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${visuals.emoji} ${visuals.label}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = visuals.color,
                fontSize = 10.sp
            )
        }
    }
}

/**
 * Стильная цветная круглая иконка для строки подхода.
 */
@Composable
fun CategoryIconBadge(
    log: WorkoutLog,
    modifier: Modifier = Modifier
) {
    val visuals = ExerciseVisuals.forLog(log)
    val icon = when {
        log.speedKmh != null ->
            if (log.exerciseNameSnapshot.contains("Бег", ignoreCase = true)) {
                Icons.Filled.DirectionsRun
            } else {
                Icons.Filled.DirectionsWalk
            }
        log.durationMinutes != null && log.reps == null -> Icons.Filled.Timer
        else -> visuals.icon
    }

    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(visuals.containerColor)
            .border(1.dp, visuals.color.copy(alpha = 0.3f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = visuals.label,
            tint = visuals.color,
            modifier = Modifier.size(24.dp)
        )
    }
}
