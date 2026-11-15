package com.example.fitnesstracker.ui.quicklog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
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
import com.example.fitnesstracker.domain.model.ExerciseCategory
import com.example.fitnesstracker.domain.model.TrackingType

/**
 * Диалог создания пользовательского упражнения.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateExerciseDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, category: ExerciseCategory, gripType: String?, trackingType: TrackingType) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var grip by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ExerciseCategory.BODYWEIGHT) }
    var trackingType by remember { mutableStateOf(TrackingType.REPS) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Своё упражнение") },
        text = {
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
                Text(text = "Категория", style = androidx.compose.material3.MaterialTheme.typography.labelMedium)
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
                Text(text = "Что учитывать", style = androidx.compose.material3.MaterialTheme.typography.labelMedium)
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
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(name, category, grip, trackingType) },
                enabled = name.isNotBlank()
            ) {
                Text(text = "Создать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Отмена")
            }
        }
    )
}
