package com.example.fitnesstracker.data.prepopulate

import com.example.fitnesstracker.data.local.entity.ExerciseEntity
import com.example.fitnesstracker.domain.model.ExerciseCategory
import com.example.fitnesstracker.domain.model.TrackingType

/**
 * Предустановленный справочник упражнений.
 *
 * [baseMetOrKcalFactor]:
 *  - REPS / REPS_WEIGHT — примерные ккал за 1 повторение при весе тела 75 кг;
 *  - DURATION — MET (планка ≈ 3.8);
 *  - CARDIO_SPEED — 0 (MET динамически вычисляется по скорости).
 */
object DefaultExercises {

    val all: List<ExerciseEntity> = listOf(
        // ── Свой вес ──────────────────────────────────────────────
        ExerciseEntity(id = 1, name = "Отжимания", category = ExerciseCategory.BODYWEIGHT,
            gripType = "Классические", trackingType = TrackingType.REPS, baseMetOrKcalFactor = 0.40),
        ExerciseEntity(id = 2, name = "Отжимания", category = ExerciseCategory.BODYWEIGHT,
            gripType = "Широким хватом", trackingType = TrackingType.REPS, baseMetOrKcalFactor = 0.42),
        ExerciseEntity(id = 3, name = "Отжимания", category = ExerciseCategory.BODYWEIGHT,
            gripType = "Узкие / алмазные", trackingType = TrackingType.REPS, baseMetOrKcalFactor = 0.45),
        ExerciseEntity(id = 4, name = "Отжимания на брусьях", category = ExerciseCategory.BODYWEIGHT,
            gripType = null, trackingType = TrackingType.REPS, baseMetOrKcalFactor = 0.50),
        ExerciseEntity(id = 5, name = "Приседания", category = ExerciseCategory.BODYWEIGHT,
            gripType = "Классические", trackingType = TrackingType.REPS, baseMetOrKcalFactor = 0.32),
        ExerciseEntity(id = 6, name = "Приседания", category = ExerciseCategory.BODYWEIGHT,
            gripType = "Сумо / широкая постановка", trackingType = TrackingType.REPS, baseMetOrKcalFactor = 0.34),
        ExerciseEntity(id = 7, name = "Выпады", category = ExerciseCategory.BODYWEIGHT,
            gripType = null, trackingType = TrackingType.REPS, baseMetOrKcalFactor = 0.35),

        // ── Турник ────────────────────────────────────────────────
        ExerciseEntity(id = 8, name = "Подтягивания", category = ExerciseCategory.PULL_UP_BAR,
            gripType = "Прямой средний", trackingType = TrackingType.REPS, baseMetOrKcalFactor = 0.60),
        ExerciseEntity(id = 9, name = "Подтягивания", category = ExerciseCategory.PULL_UP_BAR,
            gripType = "Обратный (на бицепс)", trackingType = TrackingType.REPS, baseMetOrKcalFactor = 0.60),
        ExerciseEntity(id = 10, name = "Подтягивания", category = ExerciseCategory.PULL_UP_BAR,
            gripType = "Широкий", trackingType = TrackingType.REPS, baseMetOrKcalFactor = 0.62),
        ExerciseEntity(id = 11, name = "Подтягивания", category = ExerciseCategory.PULL_UP_BAR,
            gripType = "Нейтральный / параллельный", trackingType = TrackingType.REPS, baseMetOrKcalFactor = 0.60),
        ExerciseEntity(id = 12, name = "Подтягивания", category = ExerciseCategory.PULL_UP_BAR,
            gripType = "Узкий", trackingType = TrackingType.REPS, baseMetOrKcalFactor = 0.58),

        // ── Гантели ───────────────────────────────────────────────
        ExerciseEntity(id = 13, name = "Подъем гантелей на бицепс", category = ExerciseCategory.DUMBBELLS,
            gripType = "Супинированный / прямой", trackingType = TrackingType.REPS_WEIGHT, baseMetOrKcalFactor = 0.22),
        ExerciseEntity(id = 14, name = "«Молотки»", category = ExerciseCategory.DUMBBELLS,
            gripType = "Нейтральный / молотковый", trackingType = TrackingType.REPS_WEIGHT, baseMetOrKcalFactor = 0.22),
        ExerciseEntity(id = 15, name = "Подъем гантелей на бицепс обратным хватом", category = ExerciseCategory.DUMBBELLS,
            gripType = "Обратный / пронированный", trackingType = TrackingType.REPS_WEIGHT, baseMetOrKcalFactor = 0.20),
        ExerciseEntity(id = 16, name = "Жим гантелей лежа / сидя", category = ExerciseCategory.DUMBBELLS,
            gripType = "Классический", trackingType = TrackingType.REPS_WEIGHT, baseMetOrKcalFactor = 0.35),
        ExerciseEntity(id = 17, name = "Жим гантелей", category = ExerciseCategory.DUMBBELLS,
            gripType = "Нейтральный", trackingType = TrackingType.REPS_WEIGHT, baseMetOrKcalFactor = 0.34),
        ExerciseEntity(id = 18, name = "Тяга гантели в наклоне", category = ExerciseCategory.DUMBBELLS,
            gripType = "Нейтральный", trackingType = TrackingType.REPS_WEIGHT, baseMetOrKcalFactor = 0.30),
        ExerciseEntity(id = 19, name = "Махи гантелями в стороны", category = ExerciseCategory.DUMBBELLS,
            gripType = null, trackingType = TrackingType.REPS_WEIGHT, baseMetOrKcalFactor = 0.18),
        ExerciseEntity(id = 20, name = "Приседания с гантелями", category = ExerciseCategory.DUMBBELLS,
            gripType = null, trackingType = TrackingType.REPS_WEIGHT, baseMetOrKcalFactor = 0.38),

        // ── Пресс ─────────────────────────────────────────────────
        ExerciseEntity(id = 21, name = "Скручивания лежа", category = ExerciseCategory.ABS,
            gripType = null, trackingType = TrackingType.REPS, baseMetOrKcalFactor = 0.25),
        ExerciseEntity(id = 22, name = "Подъем ног в висе / лежа", category = ExerciseCategory.ABS,
            gripType = null, trackingType = TrackingType.REPS, baseMetOrKcalFactor = 0.30),
        ExerciseEntity(id = 23, name = "«Велосипед»", category = ExerciseCategory.ABS,
            gripType = null, trackingType = TrackingType.REPS, baseMetOrKcalFactor = 0.30),
        ExerciseEntity(id = 24, name = "Планка", category = ExerciseCategory.ABS,
            gripType = null, trackingType = TrackingType.DURATION, baseMetOrKcalFactor = 3.8),

        // ── Кардио ────────────────────────────────────────────────
        ExerciseEntity(id = 25, name = "Ходьба", category = ExerciseCategory.CARDIO,
            gripType = null, trackingType = TrackingType.CARDIO_SPEED, baseMetOrKcalFactor = 0.0),
        ExerciseEntity(id = 26, name = "Бег", category = ExerciseCategory.CARDIO,
            gripType = null, trackingType = TrackingType.CARDIO_SPEED, baseMetOrKcalFactor = 0.0)
    )
}
