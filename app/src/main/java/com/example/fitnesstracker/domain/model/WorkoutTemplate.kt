package com.example.fitnesstracker.domain.model

data class WorkoutTemplate(
    val id: Long = 0L,
    val title: String,
    val description: String = "",
    val iconName: String = "fitness",
    val isDefault: Boolean = false,
    val items: List<WorkoutTemplateItem> = emptyList()
)

data class WorkoutTemplateItem(
    val id: Long = 0L,
    val templateId: Long = 0L,
    val exerciseId: Long,
    val exerciseName: String,
    val orderIndex: Int = 0,
    val targetSets: Int = 3,
    val targetReps: Int? = 10,
    val targetWeightKg: Float? = null,
    val targetDurationMinutes: Float? = null
)
