package com.example.fitnesstracker.ui.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitnesstracker.domain.model.Exercise
import com.example.fitnesstracker.domain.model.WorkoutTemplate
import com.example.fitnesstracker.domain.model.WorkoutTemplateItem
import com.example.fitnesstracker.domain.repository.ExerciseRepository
import com.example.fitnesstracker.domain.repository.WorkoutRepository
import com.example.fitnesstracker.util.Formatters
import kotlinx.coroutines.launch

/**
 * Шторка готовых программ и шаблонов тренировок:
 * просмотр готовых комплексов, создание собственных и запуск упражнений программы.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutTemplatesSheet(
    workoutRepository: WorkoutRepository,
    exerciseRepository: ExerciseRepository,
    onStartExercise: (exerciseId: Long, reps: Int?, weightKg: Float?, durationMinutes: Float?) -> Unit,
    onDismiss: () -> Unit
) {
    val templates by workoutRepository.observeTemplates()
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val exercises by exerciseRepository.observeExercises()
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val scope = rememberCoroutineScope()
    var showCreateDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Программы тренировок",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Готовые комплексы и свои шаблоны",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { showCreateDialog = true }) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "Создать шаблон")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(templates, key = { it.id }) { template ->
                    WorkoutTemplateCard(
                        template = template,
                        onStartItem = { item ->
                            onStartExercise(
                                item.exerciseId,
                                item.targetReps,
                                item.targetWeightKg,
                                item.targetDurationMinutes
                            )
                        },
                        onDelete = if (!template.isDefault) {
                            { scope.launch { workoutRepository.deleteTemplate(template.id) } }
                        } else null
                    )
                }

                item(key = "create_new_template_btn") {
                    OutlinedButton(
                        onClick = { showCreateDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Создать свою программу")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }

    if (showCreateDialog) {
        CreateTemplateDialog(
            allExercises = exercises,
            onDismiss = { showCreateDialog = false },
            onSave = { newTemplate ->
                scope.launch {
                    workoutRepository.saveTemplate(newTemplate)
                    showCreateDialog = false
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WorkoutTemplateCard(
    template: WorkoutTemplate,
    onStartItem: (WorkoutTemplateItem) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.FitnessCenter,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = template.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (template.description.isNotEmpty()) {
                        Text(
                            text = template.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Удалить шаблон",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Список упражнений комплекса
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                template.items.forEachIndexed { index, item ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onStartItem(item) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${index + 1}.",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.width(24.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.exerciseName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                val targetDesc = buildString {
                                    append("${item.targetSets} подх.")
                                    if (item.targetReps != null) append(" · ${item.targetReps} повт.")
                                    if (item.targetWeightKg != null) append(" · ${Formatters.number(item.targetWeightKg)} кг")
                                    if (item.targetDurationMinutes != null) append(" · ${Formatters.minutes(item.targetDurationMinutes)}")
                                }
                                Text(
                                    text = targetDesc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Записать подход",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            if (template.items.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                FilledTonalButton(
                    onClick = { onStartItem(template.items.first()) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Начать первое упражнение (${template.items.first().exerciseName})")
                }
            }
        }
    }
}

@Composable
private fun CreateTemplateDialog(
    allExercises: List<Exercise>,
    onDismiss: () -> Unit,
    onSave: (WorkoutTemplate) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val selectedExercises = remember { mutableStateListOf<Exercise>() }
    var showExercisePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая программа", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название программы") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание (необязательно)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Упражнения (${selectedExercises.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                selectedExercises.forEachIndexed { index, ex ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${index + 1}. ${ex.name}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        IconButton(onClick = { selectedExercises.removeAt(index) }) {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = "Удалить")
                        }
                    }
                }

                OutlinedButton(
                    onClick = { showExercisePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Добавить упражнение")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && selectedExercises.isNotEmpty()) {
                        val items = selectedExercises.mapIndexed { index, ex ->
                            WorkoutTemplateItem(
                                exerciseId = ex.id,
                                exerciseName = ex.displayName,
                                orderIndex = index,
                                targetSets = 3,
                                targetReps = 10
                            )
                        }
                        onSave(
                            WorkoutTemplate(
                                title = title.trim(),
                                description = description.trim(),
                                isDefault = false,
                                items = items
                            )
                        )
                    }
                },
                enabled = title.isNotBlank() && selectedExercises.isNotEmpty()
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )

    if (showExercisePicker) {
        AlertDialog(
            onDismissRequest = { showExercisePicker = false },
            title = { Text("Выберите упражнение") },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp)
                ) {
                    items(allExercises) { ex ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedExercises.add(ex)
                                    showExercisePicker = false
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = ex.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showExercisePicker = false }) {
                    Text("Закрыть")
                }
            }
        )
    }
}
