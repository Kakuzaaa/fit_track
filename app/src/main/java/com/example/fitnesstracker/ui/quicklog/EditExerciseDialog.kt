package com.example.fitnesstracker.ui.quicklog

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.domain.model.Exercise
import com.example.fitnesstracker.domain.model.ExerciseCategory
import com.example.fitnesstracker.domain.model.TrackingType

/**
 * Правка или удаление своего упражнения (long-press в списке шита).
 * Удаление каскадно убирает и записи этого упражнения — двухшаговое подтверждение.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun EditExerciseDialog(
    exercise: Exercise,
    onSave: (name: String, category: ExerciseCategory, gripType: String?, trackingType: TrackingType) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(exercise.name) }
    var grip by remember { mutableStateOf(exercise.gripType ?: "") }
    var category by remember { mutableStateOf(exercise.category) }
    var trackingType by remember { mutableStateOf(exercise.trackingType) }
    var confirmDelete by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = if (confirmDelete) "Удалить упражнение?" else "Изменить упражнение")
        },
        text = {
            if (confirmDelete) {
                Column {
                    Text(
                        text = "«${exercise.displayName}» и все связанные записи " +
                            "тренировок будут удалены безвозвратно."
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(text = "Название") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = grip,
                        onValueChange = { grip = it },
                        label = { Text(text = "Хват / вариация (необязательно)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(text = "Категория", style = MaterialTheme.typography.labelMedium)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ExerciseCategory.entries.forEach { option ->
                            FilterChip(
                                selected = category == option,
                                onClick = { category = option },
                                label = { Text(text = option.label) }
                            )
                        }
                    }
                    Text(text = "Что учитывать", style = MaterialTheme.typography.labelMedium)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TrackingType.entries.forEach { option ->
                            FilterChip(
                                selected = trackingType == option,
                                onClick = { trackingType = option },
                                label = { Text(text = option.label) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (confirmDelete) {
                TextButton(
                    onClick = {
                        onDelete()
                        onDismiss()
                    }
                ) {
                    Text(text = "Да, удалить", color = MaterialTheme.colorScheme.error)
                }
            } else {
                TextButton(
                    onClick = {
                        onSave(name, category, grip, trackingType)
                    },
                    enabled = name.isNotBlank()
                ) {
                    Text(text = "Сохранить")
                }
            }
        },
        dismissButton = {
            if (confirmDelete) {
                TextButton(onClick = { confirmDelete = false }) {
                    Text(text = "Отмена")
                }
            } else {
                TextButton(onClick = { confirmDelete = true }) {
                    Text(text = "Удалить…", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    )
}
