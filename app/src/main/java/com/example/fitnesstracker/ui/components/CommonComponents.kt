package com.example.fitnesstracker.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
 * [onRepeat] — быстрый повтор подхода «+1 такой же»,
 * [onEdit] — открытие записи в режиме редактирования.
 */
@Composable
fun WorkoutLogRow(
    log: WorkoutLog,
    modifier: Modifier = Modifier,
    onEdit: (() -> Unit)? = null,
    onRepeat: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LogIconBadge(icon = logIcon(log))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.exerciseNameSnapshot,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = workoutDetails(log),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            if (onEdit != null) {
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Редактировать запись",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            if (onRepeat != null) {
                IconButton(onClick = onRepeat, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Repeat,
                        contentDescription = "Повторить такой же подход",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+${log.caloriesBurned} ккал",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
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
