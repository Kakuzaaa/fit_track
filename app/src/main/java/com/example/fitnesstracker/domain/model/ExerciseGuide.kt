package com.example.fitnesstracker.domain.model

/**
 * Руководство по технике выполнения упражнения.
 * Хранится в коде (ExerciseGuides), без миграций Room.
 *
 * @param exerciseId id упражнения из DefaultExercises
 * @param summary краткое описание / назначение упражнения
 * @param steps пошаговое выполнение (исходное положение, движение, дыхание)
 * @param mistakes частые ошибки, ведущие к травме или снижению эффективности
 * @param primaryMuscles основные целевые мышечные группы
 * @param secondaryMuscles вспомогательные мышцы и стабилизаторы
 * @param animationAsset имя файла анимации в assets/animations/ (null, если используется иконка)
 */
data class ExerciseGuide(
    val exerciseId: Long,
    val summary: String,
    val steps: List<String>,
    val mistakes: List<String>,
    val primaryMuscles: List<String>,
    val secondaryMuscles: List<String> = emptyList(),
    val animationAsset: String? = null
)
