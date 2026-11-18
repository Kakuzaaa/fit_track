package com.example.fitnesstracker.ui.quicklog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitnesstracker.domain.model.Exercise
import com.example.fitnesstracker.domain.model.ExerciseCategory
import com.example.fitnesstracker.domain.model.ExercisePackage
import com.example.fitnesstracker.domain.model.ExerciseProgressions
import com.example.fitnesstracker.domain.model.TrackingType
import com.example.fitnesstracker.data.guides.ExerciseGuides
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import com.example.fitnesstracker.ui.components.categoryIcon
import com.example.fitnesstracker.util.AthleteStandards
import com.example.fitnesstracker.util.Formatters
import java.time.LocalDate
import kotlin.math.roundToInt

/**
 * BottomSheet быстрого добавления / редактирования: поиск + чипсы категорий
 * -> компактная форма ввода с живым расчётом калорий -> «Сохранить».
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickLogSheet(viewModel: QuickLogViewModel, onDismiss: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            val selected = state.selected
            if (selected == null) {
                ExercisePickerContent(state = state, viewModel = viewModel)
            } else {
                ExerciseFormContent(exercise = selected, state = state, viewModel = viewModel)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }

    if (state.showCreateDialog) {
        CreateExerciseDialog(
            onDismiss = { viewModel.dismissCreateDialog() },
            onCreate = { name, category, grip, trackingType ->
                viewModel.createCustomExercise(name, category, grip, trackingType)
            }
        )
    }

    if (state.showEditExerciseDialog && state.editingExercise != null) {
        EditExerciseDialog(
            exercise = state.editingExercise!!,
            onSave = viewModel::saveEditedExercise,
            onDelete = viewModel::deleteEditedExercise,
            onDismiss = viewModel::dismissEditExerciseDialog
        )
    }
}

// ─────────────────────────── Выбор упражнения ───────────────────────────

@Composable
private fun ExercisePickerContent(state: QuickLogUiState, viewModel: QuickLogViewModel) {
    var techniqueExercise by remember { mutableStateOf<Exercise?>(null) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Записать упражнение",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            val targetDay = state.targetDateEpochDay
            if (targetDay != null && targetDay != LocalDate.now().toEpochDay()) {
                Text(
                    text = "Запись на: ${Formatters.dayTitle(targetDay)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        IconButton(onClick = { viewModel.showCreateDialog() }) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "Своё упражнение")
        }
    }

    OutlinedTextField(
        value = state.query,
        onValueChange = { viewModel.onQueryChange(it) },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(text = "Поиск упражнения") },
        leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null) },
        singleLine = true,
        shape = RoundedCornerShape(16.dp)
    )

    Spacer(modifier = Modifier.height(12.dp))

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            FilterChip(
                selected = state.selectedCategory == null,
                onClick = { viewModel.onCategorySelect(null) },
                label = { Text(text = "Все") }
            )
        }
        items(ExerciseCategory.entries) { category ->
            FilterChip(
                selected = state.selectedCategory == category,
                onClick = { viewModel.onCategorySelect(category) },
                label = { Text(text = category.label) }
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 420.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items(state.filteredExercises, key = { it.id }) { exercise ->
            ExerciseListItem(
                exercise = exercise,
                onClick = { viewModel.selectExercise(exercise) },
                onLongClick = { viewModel.onExerciseLongPress(exercise) },
                onShowTechnique = { techniqueExercise = exercise }
            )
        }
        item(key = "create_custom") {
            AddCustomExerciseItem(onClick = { viewModel.showCreateDialog() })
        }
        if (state.allExercises.any { it.isCustom }) {
            item(key = "custom_hint") {
                Text(
                    text = "Долгое нажатие на «своё» упражнение — изменить или удалить",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }

        // Пакеты упражнений — пока не импортированы.
        val availablePackages = viewModel.packages.filter { pack ->
            state.allExercises.none { it.id == pack.exerciseIds.first() }
        }
        if (availablePackages.isNotEmpty()) {
            item(key = "packages_title") {
                Text(
                    text = "Пакеты упражнений",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 14.dp, bottom = 6.dp)
                )
            }
            items(availablePackages, key = { "pkg_${it.id}" }) { pack ->
                ExercisePackageCard(
                    pack = pack,
                    onImport = { viewModel.importPackage(pack.id) }
                )
            }
        }
    }

    techniqueExercise?.let { ex ->
        val guide = ExerciseGuides.guideFor(ex.id)
        if (guide != null) {
            ExerciseTechniqueSheet(
                exercise = ex,
                guide = guide,
                onDismiss = { techniqueExercise = null }
            )
        }
    }
}

@Composable
private fun ExercisePackageCard(pack: ExercisePackage, onImport: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "📦 ${pack.title}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = pack.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onImport,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Импортировать (+${pack.exerciseIds.size})")
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ExerciseListItem(
    exercise: Exercise,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onShowTechnique: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = categoryIcon(exercise.category),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = exerciseSubtitle(exercise),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (onShowTechnique != null && ExerciseGuides.hasGuide(exercise.id)) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onShowTechnique)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Техника выполнения",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

private fun exerciseSubtitle(exercise: Exercise): String {
    val guide = ExerciseGuides.guideFor(exercise.id)
    val muscles = guide?.primaryMuscles?.take(2)?.joinToString(", ")
    val detail = exercise.gripType ?: exercise.category.label
    val text = when {
        muscles != null && exercise.gripType != null -> "$detail · $muscles"
        muscles != null -> "$muscles · $detail"
        else -> detail
    }
    return if (exercise.isCustom) "$text · своё" else text
}

@Composable
private fun AddCustomExerciseItem(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = "Создать своё упражнение", style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "Если нужного нет в списке",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─────────────────────────── Форма ввода ───────────────────────────

@Composable
private fun ExerciseFormContent(exercise: Exercise, state: QuickLogUiState, viewModel: QuickLogViewModel) {
    var showTechnique by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.clearSelection() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "К списку упражнений"
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                if (state.editingLog != null) {
                    val dateStr = Formatters.dayTitle(state.editingLog.dateEpochDay)
                    Text(
                        text = "Редактирование записи ($dateStr)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    val targetDay = state.targetDateEpochDay
                    if (targetDay != null && targetDay != LocalDate.now().toEpochDay()) {
                        Text(
                            text = "Запись на: ${Formatters.dayTitle(targetDay)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                exercise.gripType?.let { grip ->
                    Text(
                        text = grip,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (ExerciseGuides.hasGuide(exercise.id)) {
                IconButton(onClick = { showTechnique = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Техника выполнения",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        if (ExerciseGuides.hasGuide(exercise.id)) {
            val guide = ExerciseGuides.guideFor(exercise.id)
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { showTechnique = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Техника выполнения и анимация",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        guide?.let {
                            Text(
                                text = it.primaryMuscles.joinToString(" · "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        } else {
            Spacer(modifier = Modifier.height(8.dp))
        }

        when (exercise.trackingType) {
            TrackingType.REPS -> RepsForm(state = state, viewModel = viewModel)
            TrackingType.REPS_WEIGHT -> {
                RepsForm(state = state, viewModel = viewModel)
                Spacer(modifier = Modifier.height(16.dp))
                WeightForm(state = state, viewModel = viewModel)
            }
            TrackingType.CARDIO_SPEED -> CardioForm(exercise = exercise, state = state, viewModel = viewModel)
            TrackingType.DURATION -> DurationForm(state = state, viewModel = viewModel)
        }

        if (exercise.trackingType == TrackingType.REPS || exercise.trackingType == TrackingType.REPS_WEIGHT) {
            Spacer(modifier = Modifier.height(12.dp))
            ProgressionHint(exercise = exercise, state = state, viewModel = viewModel)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Примерный расход: ~${state.previewKcal} ккал",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        val levelValue: Float? = when (exercise.trackingType) {
            TrackingType.REPS -> state.reps.toFloat().takeIf { it > 0f }
            TrackingType.DURATION -> (state.durationMinutes * 60f).takeIf { it > 0f }
            else -> null
        }
        levelValue?.let { value ->
            AthleteStandards.levelFor(exercise.id, value)?.let { level ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "🏅 Уровень: ${level.label}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.save() },
            enabled = state.previewKcal > 0,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = if (state.editingLog != null) "Сохранить изменения" else "Сохранить",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }

    if (showTechnique) {
        val guide = ExerciseGuides.guideFor(exercise.id)
        if (guide != null) {
            ExerciseTechniqueSheet(
                exercise = exercise,
                guide = guide,
                onDismiss = { showTechnique = false }
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun ChipSelectorRow(options: List<String>, selectedValue: String, onSelect: (String) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(options) { option ->
            FilterChip(
                selected = option == selectedValue,
                onClick = { onSelect(option) },
                label = { Text(text = option) }
            )
        }
    }
}

@Composable
private fun NumberField(
    value: String,
    suffix: String,
    width: Dp = 110.dp,
    decimal: Boolean = false,
    onValueChange: (String) -> Unit
) {
    var text by remember(value) { mutableStateOf(value) }
    OutlinedTextField(
        value = text,
        onValueChange = { input ->
            text = input
            onValueChange(input)
        },
        suffix = { Text(text = suffix) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.width(width)
    )
}

@Composable
private fun StepperRow(
    minusIcon: ImageVector,
    plusIcon: ImageVector,
    minusDescription: String,
    plusDescription: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    field: @Composable () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        FilledTonalIconButton(onClick = onMinus) {
            Icon(imageVector = minusIcon, contentDescription = minusDescription)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) { field() }
        Spacer(modifier = Modifier.width(10.dp))
        FilledTonalIconButton(onClick = onPlus) {
            Icon(imageVector = plusIcon, contentDescription = plusDescription)
        }
    }
}

// ── Повторения ──

private val repPresets = listOf(5, 10, 12, 15, 20, 25)

@Composable
private fun RepsForm(state: QuickLogUiState, viewModel: QuickLogViewModel) {
    SectionLabel(text = "Повторения")
    ChipSelectorRow(
        options = repPresets.map { "$it" },
        selectedValue = state.reps.toString(),
        onSelect = { it.toIntOrNull()?.let(viewModel::setReps) }
    )
    Spacer(modifier = Modifier.height(10.dp))
    StepperRow(
        minusIcon = Icons.Filled.Remove,
        plusIcon = Icons.Filled.Add,
        minusDescription = "Минус повторение",
        plusDescription = "Плюс повторение",
        onMinus = { viewModel.setReps(state.reps - 1) },
        onPlus = { viewModel.setReps(state.reps + 1) },
        field = {
            NumberField(
                value = state.reps.toString(),
                suffix = "повт.",
                onValueChange = { it.toIntOrNull()?.let(viewModel::setReps) }
            )
        }
    )
}

// ── Вес гантелей ──

private val weightPresets = listOf(2f, 4f, 6f, 8f, 10f, 12f, 16f, 20f)

@Composable
private fun WeightForm(state: QuickLogUiState, viewModel: QuickLogViewModel) {
    SectionLabel(text = "Вес гантели (кг)")
    val options = buildList {
        state.lastUsedWeightKg?.let { last ->
            if (weightPresets.none { it == last }) add(last)
        }
        addAll(weightPresets)
    }
    ChipSelectorRow(
        options = options.map { Formatters.number(it) },
        selectedValue = Formatters.number(state.weightKg),
        onSelect = { it.replace(',', '.').toFloatOrNull()?.let(viewModel::setWeight) }
    )
    if (state.lastUsedWeightKg != null) {
        Text(
            text = "Сохранён прошлый вес: ${Formatters.number(state.lastUsedWeightKg!!)} кг",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
    Spacer(modifier = Modifier.height(10.dp))
    StepperRow(
        minusIcon = Icons.Filled.Remove,
        plusIcon = Icons.Filled.Add,
        minusDescription = "Меньше вес",
        plusDescription = "Больше вес",
        onMinus = { viewModel.setWeight(state.weightKg - 0.5f) },
        onPlus = { viewModel.setWeight(state.weightKg + 0.5f) },
        field = {
            NumberField(
                value = Formatters.number(state.weightKg),
                suffix = "кг",
                decimal = true,
                onValueChange = { it.replace(',', '.').toFloatOrNull()?.let(viewModel::setWeight) }
            )
        }
    )
}

// ── Кардио: скорость + расстояние, длительность необязательна ──

@Composable
private fun CardioForm(exercise: Exercise, state: QuickLogUiState, viewModel: QuickLogViewModel) {
    SectionLabel(text = "Скорость (км/ч)")
    ChipSelectorRow(
        options = viewModel.speedPresets(exercise).map { Formatters.number(it) },
        selectedValue = Formatters.number(state.speedKmh),
        onSelect = { it.replace(',', '.').toFloatOrNull()?.let(viewModel::setSpeed) }
    )
    Text(
        text = "Точная скорость: ${Formatters.number(state.speedKmh)} км/ч",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp)
    )
    Slider(
        value = state.speedKmh,
        onValueChange = { viewModel.setSpeed((it * 10).roundToInt() / 10f) },
        valueRange = 1f..20f,
        steps = 37
    )

    Spacer(modifier = Modifier.height(12.dp))

    SectionLabel(text = "Расстояние (км)")
    ChipSelectorRow(
        options = viewModel.distancePresets(exercise).map { Formatters.number(it) },
        selectedValue = Formatters.number(state.distanceKm),
        onSelect = { it.replace(',', '.').toFloatOrNull()?.let(viewModel::setDistance) }
    )
    Spacer(modifier = Modifier.height(10.dp))
    StepperRow(
        minusIcon = Icons.Filled.Remove,
        plusIcon = Icons.Filled.Add,
        minusDescription = "Минус километр",
        plusDescription = "Плюс километр",
        onMinus = { viewModel.setDistance(state.distanceKm - 1f) },
        onPlus = { viewModel.setDistance(state.distanceKm + 1f) },
        field = {
            NumberField(
                value = Formatters.number(state.distanceKm),
                suffix = "км",
                decimal = true,
                onValueChange = { it.replace(',', '.').toFloatOrNull()?.let(viewModel::setDistance) }
            )
        }
    )

    Spacer(modifier = Modifier.height(16.dp))

    SectionLabel(text = "Длительность (мин) — необязательно")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        FilterChip(
            selected = state.cardioDurationMinutes == null,
            onClick = { viewModel.setCardioDuration(null) },
            label = { Text(text = "Не указывать") }
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listOf(10, 15, 20, 30, 45, 60)) { minutes ->
                FilterChip(
                    selected = state.cardioDurationMinutes == minutes.toFloat(),
                    onClick = { viewModel.setCardioDuration(minutes.toFloat()) },
                    label = { Text(text = "$minutes") }
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(10.dp))
    StepperRow(
        minusIcon = Icons.Filled.Remove,
        plusIcon = Icons.Filled.Add,
        minusDescription = "Минус 5 минут",
        plusDescription = "Плюс 5 минут",
        onMinus = { viewModel.setCardioDuration((state.cardioDurationMinutes ?: 0f) - 5f) },
        onPlus = { viewModel.setCardioDuration((state.cardioDurationMinutes ?: 0f) + 5f) },
        field = {
            NumberField(
                value = state.cardioDurationMinutes?.let { Formatters.number(it) } ?: "",
                suffix = "мин",
                decimal = true,
                onValueChange = { input ->
                    viewModel.setCardioDuration(input.replace(',', '.').toFloatOrNull())
                }
            )
        }
    )
    if (state.cardioDurationMinutes == null && state.distanceKm > 0f && state.speedKmh > 0f) {
        Text(
            text = "Длительность не указана — рассчитаем из расстояния: " +
                "≈ ${Formatters.minutes(viewModel.effectiveCardioMinutes(state).toFloat())}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

// ── Статика (планка) ──

private val durationPresets = listOf(0.5f, 1f, 1.5f, 2f, 3f, 5f)

@Composable
private fun DurationForm(state: QuickLogUiState, viewModel: QuickLogViewModel) {
    SectionLabel(text = "Длительность")
    ChipSelectorRow(
        options = durationPresets.map { Formatters.number(it) },
        selectedValue = Formatters.number(state.durationMinutes),
        onSelect = { it.replace(',', '.').toFloatOrNull()?.let(viewModel::setDuration) }
    )
    Spacer(modifier = Modifier.height(10.dp))
    StepperRow(
        minusIcon = Icons.Filled.Remove,
        plusIcon = Icons.Filled.Add,
        minusDescription = "Минус 15 секунд",
        plusDescription = "Плюс 15 секунд",
        onMinus = { viewModel.setDuration(state.durationMinutes - 0.25f) },
        onPlus = { viewModel.setDuration(state.durationMinutes + 0.25f) },
        field = {
            NumberField(
                value = Formatters.number(state.durationMinutes),
                suffix = "мин",
                decimal = true,
                onValueChange = { it.replace(',', '.').toFloatOrNull()?.let(viewModel::setDuration) }
            )
        }
    )
    Text(
        text = "Шаг кнопок: 15 секунд",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 6.dp)
    )
}

/**
 * Подсказка прогрессии калистеники: следующий уровень цепочки, к которому
 * можно перейти одним тапом (упражнения пакета должны быть импортированы).
 */
@Composable
private fun ProgressionHint(exercise: Exercise, state: QuickLogUiState, viewModel: QuickLogViewModel) {
    val chain = ExerciseProgressions.chainFor(exercise.id) ?: return
    val nextId = chain.nextAfter(exercise.id) ?: return
    val next = state.allExercises.firstOrNull { it.id == nextId } ?: return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { viewModel.selectExercise(next) }
            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = (if (state.reps >= 15) "Делаешь легко? " else "") + "Следующий уровень: ",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "${next.displayName} →",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}
