package com.example.fitnesstracker.ui.components

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SportsGymnastics
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.domain.model.ExerciseCategory
import com.example.fitnesstracker.domain.model.WorkoutLog
import com.example.fitnesstracker.util.Formatters

/** Иконка по категории справочника. */
fun categoryIcon(category: ExerciseCategory): ImageVector = when (category) {
    ExerciseCategory.BODYWEIGHT -> Icons.Filled.SelfImprovement
    ExerciseCategory.PULL_UP_BAR -> Icons.Filled.SportsGymnastics
    ExerciseCategory.DUMBBELLS -> Icons.Filled.FitnessCenter
    ExerciseCategory.ABS -> Icons.Filled.Bolt
    ExerciseCategory.CARDIO -> Icons.Filled.DirectionsRun
    ExerciseCategory.BARBELL -> Icons.Filled.ViewInAr
}

/** Короткая вибрация (конец таймера отдыха). */
fun vibrateOnce(context: Context) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Vibrator::class.java)
    }
    vibrator?.vibrate(VibrationEffect.createOneShot(600, VibrationEffect.DEFAULT_AMPLITUDE))
}

/** Иконка для строки записи (по данным, без обращения к справочнику). */
fun logIcon(log: WorkoutLog): ImageVector = when {
    log.speedKmh != null ->
        if (log.exerciseNameSnapshot.contains("Бег", ignoreCase = true)) {
            Icons.Filled.DirectionsRun
        } else {
            Icons.Filled.DirectionsWalk
        }
    log.durationMinutes != null && log.reps == null -> Icons.Filled.Timer
    else -> Icons.Filled.FitnessCenter
}

/** Короткое описание параметров подхода: «10 × 8 кг», «5 км · 10 км/ч» и т.п. */
fun workoutDetails(log: WorkoutLog): String = when {
    log.speedKmh != null -> {
        val parts = mutableListOf<String>()
        log.durationMinutes?.let { parts += Formatters.minutes(it) }
        log.distanceKm?.let { parts += "${Formatters.number(it)} км" }
        parts += "${Formatters.number(log.speedKmh!!)} км/ч"
        parts.joinToString(" · ")
    }
    log.reps != null && log.weightKg != null ->
        "${log.reps} × ${Formatters.number(log.weightKg!!)} кг"
    log.reps != null ->
        "${log.reps} ${Formatters.plural(log.reps, "повторение", "повторения", "повторений")}"
    log.durationMinutes != null -> Formatters.minutes(log.durationMinutes)
    else -> ""
}

@Composable
private fun LogIconBadge(icon: ImageVector, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Строка журнала: время, название, хват, параметры, ккал.
 * Карточка НЕПРОЗРАЧНАЯ: под ней в «Сегодня» лежит красный фон свайп-удаления,
 * и полупрозрачность просвечивала его сквозь запись (визуальный баг в тёмной теме).
 *
 * [onRepeat] — кнопка «+N»: повторить ровно такой же подход
 * («сделал 8 подтягиваний — добавь ещё 8»), [onEdit] — редактирование записи.
 */
@Composable
fun WorkoutLogRow(
    log: WorkoutLog,
    modifier: Modifier = Modifier,
    onEdit: (() -> Unit)? = null,
    onRepeat: (() -> Unit)? = null
) {
    val visuals = ExerciseVisuals.forLog(log)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, visuals.color.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryIconBadge(log = log)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 2.dp)
                ) {
                    ExerciseCategoryBadge(category = visuals.category)
                }
                Text(
                    text = log.exerciseNameSnapshot,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = workoutDetails(log),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            if (onEdit != null) {
                CompactIconButton(
                    icon = Icons.Outlined.Edit,
                    contentDescription = "Редактировать запись",
                    onClick = onEdit
                )
            }
            if (onRepeat != null) {
                val interaction = remember { MutableInteractionSource() }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = visuals.color.copy(alpha = 0.15f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(
                            interactionSource = interaction,
                            indication = null,
                            onClick = onRepeat
                        )
                ) {
                    if (log.reps != null && log.reps > 0) {
                        Text(
                            text = "+${log.reps}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = visuals.color,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Repeat,
                                contentDescription = "Повторить запись",
                                tint = visuals.color,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Повтор",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = visuals.color
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+${log.caloriesBurned} ккал",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = visuals.color
                )
                Text(
                    text = Formatters.time(log.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Компактная иконка-кнопка без квадратного bounded-ripple. */
@Composable
private fun CompactIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}

/**
 * Заглушка для пустых состояний.
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Отправка текста (экспорт JSON/CSV) в системный шэринг — полностью офлайн.
 */
fun shareText(context: Context, subject: String, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    val chooser = Intent.createChooser(intent, "Экспорт данных")
    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(chooser)
}
