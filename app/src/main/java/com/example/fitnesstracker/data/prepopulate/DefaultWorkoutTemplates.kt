package com.example.fitnesstracker.data.prepopulate

import com.example.fitnesstracker.data.local.entity.WorkoutTemplateEntity
import com.example.fitnesstracker.data.local.entity.WorkoutTemplateItemEntity

object DefaultWorkoutTemplates {

    val templates: List<Pair<WorkoutTemplateEntity, List<WorkoutTemplateItemEntity>>> = listOf(
        WorkoutTemplateEntity(
            id = 1,
            title = "Базовый воркаут",
            description = "Комплекс со своим весом: турник, брусья, отжимания и приседания",
            iconName = "fitness",
            isDefault = true
        ) to listOf(
            WorkoutTemplateItemEntity(templateId = 1, exerciseId = 8, orderIndex = 0, targetSets = 4, targetReps = 8),
            WorkoutTemplateItemEntity(templateId = 1, exerciseId = 4, orderIndex = 1, targetSets = 4, targetReps = 10),
            WorkoutTemplateItemEntity(templateId = 1, exerciseId = 1, orderIndex = 2, targetSets = 4, targetReps = 15),
            WorkoutTemplateItemEntity(templateId = 1, exerciseId = 5, orderIndex = 3, targetSets = 4, targetReps = 20),
            WorkoutTemplateItemEntity(templateId = 1, exerciseId = 24, orderIndex = 4, targetSets = 3, targetReps = null, targetDurationMinutes = 1.0f)
        ),
        WorkoutTemplateEntity(
            id = 2,
            title = "Верх тела & Гантели",
            description = "Грудь, плечи, бицепс и спина с гантелями",
            iconName = "fire",
            isDefault = true
        ) to listOf(
            WorkoutTemplateItemEntity(templateId = 2, exerciseId = 16, orderIndex = 0, targetSets = 4, targetReps = 10, targetWeightKg = 16f),
            WorkoutTemplateItemEntity(templateId = 2, exerciseId = 18, orderIndex = 1, targetSets = 4, targetReps = 10, targetWeightKg = 16f),
            WorkoutTemplateItemEntity(templateId = 2, exerciseId = 19, orderIndex = 2, targetSets = 3, targetReps = 12, targetWeightKg = 8f),
            WorkoutTemplateItemEntity(templateId = 2, exerciseId = 13, orderIndex = 3, targetSets = 3, targetReps = 10, targetWeightKg = 12f),
            WorkoutTemplateItemEntity(templateId = 2, exerciseId = 14, orderIndex = 4, targetSets = 3, targetReps = 10, targetWeightKg = 12f)
        ),
        WorkoutTemplateEntity(
            id = 3,
            title = "Пресс & Кор",
            description = "Интенсивная тренировка мышц живота и стабилизаторов",
            iconName = "abs",
            isDefault = true
        ) to listOf(
            WorkoutTemplateItemEntity(templateId = 3, exerciseId = 22, orderIndex = 0, targetSets = 3, targetReps = 15),
            WorkoutTemplateItemEntity(templateId = 3, exerciseId = 21, orderIndex = 1, targetSets = 3, targetReps = 20),
            WorkoutTemplateItemEntity(templateId = 3, exerciseId = 23, orderIndex = 2, targetSets = 3, targetReps = 25),
            WorkoutTemplateItemEntity(templateId = 3, exerciseId = 24, orderIndex = 3, targetSets = 3, targetReps = null, targetDurationMinutes = 1.5f)
        )
    )
}
