package com.example.fitnesstracker.domain.model

/**
 * Готовый «пакет» упражнений, который пользователь может добавить
 * в свой справочник одной кнопкой (идентификаторы — из DefaultExercises).
 */
data class ExercisePackage(
    val id: String,
    val title: String,
    val description: String,
    val exerciseIds: List<Long>
)
