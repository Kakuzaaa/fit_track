package com.example.fitnesstracker.data.prepopulate

import com.example.fitnesstracker.data.local.entity.ExerciseEntity
import com.example.fitnesstracker.domain.model.ExerciseCategory
import com.example.fitnesstracker.domain.model.ExercisePackage
import com.example.fitnesstracker.domain.model.TrackingType

/**
 * Предустановленный справочник упражнений (85 упражнений по всем направлениям).
 *
 * [baseMetOrKcalFactor]:
 *  - REPS / REPS_WEIGHT — примерные ккал за 1 повторение при весе тела 75 кг;
 *  - DURATION — MET (планка ≈ 3.8);
 *  - CARDIO_SPEED — 0 (MET динамически вычисляется по скорости).
 */
object DefaultExercises {

    val all: List<ExerciseEntity> = listOf(
        // ── Свой вес (1–7) ─────────────────────────────────────────
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

        // ── Турник (8–12) ──────────────────────────────────────────
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

        // ── Гантели (13–20) ────────────────────────────────────────
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

        // ── Пресс (21–24) ──────────────────────────────────────────
        ExerciseEntity(id = 21, name = "Скручивания лежа", category = ExerciseCategory.ABS,
            gripType = null, trackingType = TrackingType.REPS, baseMetOrKcalFactor = 0.25),
        ExerciseEntity(id = 22, name = "Подъем ног в висе / лежа", category = ExerciseCategory.ABS,
            gripType = null, trackingType = TrackingType.REPS, baseMetOrKcalFactor = 0.30),
        ExerciseEntity(id = 23, name = "«Велосипед»", category = ExerciseCategory.ABS,
            gripType = null, trackingType = TrackingType.REPS, baseMetOrKcalFactor = 0.30),
        ExerciseEntity(id = 24, name = "Планка", category = ExerciseCategory.ABS,
            gripType = null, trackingType = TrackingType.DURATION, baseMetOrKcalFactor = 3.8),

        // ── Кардио (25–26) ─────────────────────────────────────────
        ExerciseEntity(id = 25, name = "Ходьба", category = ExerciseCategory.CARDIO,
            gripType = null, trackingType = TrackingType.CARDIO_SPEED, baseMetOrKcalFactor = 0.0),
        ExerciseEntity(id = 26, name = "Бег", category = ExerciseCategory.CARDIO,
            gripType = null, trackingType = TrackingType.CARDIO_SPEED, baseMetOrKcalFactor = 0.0),

        // ── Воркаут и турники (27–41) ──────────────────────────────
        ExerciseEntity(id = 27, name = "Австралийские подтягивания", category = ExerciseCategory.PULL_UP_BAR,
            gripType = "Низкие", trackingType = TrackingType.REPS, baseMetOrKcalFactor = 0.45),
        ExerciseEntity(id = 28, name = "Негативные подтягивания", category = ExerciseCategory.PULL_UP_BAR,
            gripType = "Негативные", trackingType = TrackingType.REPS, baseMetOrKcalFactor = 0.55),
        ExerciseEntity(id = 29, name = "Подтягивания архера", category = ExerciseCategory.PULL_UP_BAR,
            gripType = "Арчера", trackingType = TrackingType.REPS, baseMetOrKcalFactor = 0.75),
        ExerciseEntity(id = 30, name = "Подтягивания с хлопком", category = ExerciseCategory.PULL_UP_BAR,
            gripType = "Взрывные", trackingType = TrackingType.REPS, baseMetOrKcalFactor = 0.72),
        ExerciseEntity(id = 31, name = "Выход силой на турнике", category = ExerciseCategory.PULL_UP_BAR,
            gripType = null, trackingType = TrackingType.REPS, baseMetOrKcalFactor = 1.00),
        ExerciseEntity(id = 32, name = "Подъём туловища к перекладине", category = ExerciseCategory.PULL_UP_BAR,
            gripType = null, trackingType = TrackingType.REPS, baseMetOrKcalFactor = 0.55),
        ExerciseEntity(id = 33, name = "Вис на турнике", category = ExerciseCategory.PULL_UP_BAR,
            gripType = null, trackingType = TrackingType.DURATION, baseMetOrKcalFactor = 4.0),
        ExerciseEntity(id = 34, name = "Подтягивания на кольцах", category = ExerciseCategory.PULL_UP_BAR,
            gripType = "Кольца", trackingType = TrackingType.REPS, baseMetOrKcalFactor = 0.65),
        ExerciseEntity(id = 35, name = "Уголок на брусьях", category = ExerciseCategory.PULL_UP_BAR,
            gripType = "Брусья", trackingType = TrackingType.DURATION, baseMetOrKcalFactor = 4.5),
        ExerciseEntity(id = 36, name = "Отжимания", category = ExerciseCategory.BODYWEIGHT,
            gripType = "С колен", trackingType = TrackingType.REPS, baseMetOrKcalFactor = 0.25),
        ExerciseEntity(id = 37, name = "Отжимания с хлопком", category = ExerciseCategory.BODYWEIGHT,
            gripType = "Взрывные", trackingType = TrackingType.REPS, baseMetOrKcalFactor = 0.55),
        ExerciseEntity(id = 38, name = "Отжимания в стойке на руках", category = ExerciseCategory.BODYWEIGHT,
            gripType = "У стены", trackingType = TrackingType.REPS, baseMetOrKcalFactor = 0.70),
        ExerciseEntity(id = 39, name = "Стойка на руках у стены", category = ExerciseCategory.BODYWEIGHT,
            gripType = "У стены", trackingType = TrackingType.DURATION, baseMetOrKcalFactor = 4.5),
        ExerciseEntity(id = 40, name = "Берпи", category = ExerciseCategory.BODYWEIGHT,
            gripType = null, trackingType = TrackingType.REPS, baseMetOrKcalFactor = 0.65),
        ExerciseEntity(id = 41, name = "Приседания на одной ноге", category = ExerciseCategory.BODYWEIGHT,
            gripType = "Пистолетик", trackingType = TrackingType.REPS, baseMetOrKcalFactor = 0.55),

        // ── Здоровая спина и мобильность (42–51) ────────────────────
        ExerciseEntity(id = 42, name = "«Кошка-корова»", category = ExerciseCategory.ABS,
            gripType = null, trackingType = TrackingType.REPS, baseMetOrKcalFactor = 0.15),
        ExerciseEntity(id = 43, name = "«Птица-собака»", category = ExerciseCategory.ABS,
            gripType = null, trackingType = TrackingType.REPS, baseMetOrKcalFactor = 0.18),
        ExerciseEntity(id = 44, name = "Ягодичный мостик", category = ExerciseCategory.BODYWEIGHT,
            gripType = null, trackingType = TrackingType.REPS, baseMetOrKcalFactor = 0.25),
        ExerciseEntity(id = 45, name = "«Супермен»", category = ExerciseCategory.ABS,
            gripType = null, trackingType = TrackingType.REPS, baseMetOrKcalFactor = 0.20),
        ExerciseEntity(id = 46, name = "Лодочка", category = ExerciseCategory.ABS,
            gripType = null, trackingType = TrackingType.DURATION, baseMetOrKcalFactor = 3.5),
        ExerciseEntity(id = 47, name = "Боковая планка", category = ExerciseCategory.ABS,
            gripType = null, trackingType = TrackingType.DURATION, baseMetOrKcalFactor = 4.0),
        ExerciseEntity(id = 48, name = "Гиперэкстензия на полу", category = ExerciseCategory.BODYWEIGHT,
            gripType = null, trackingType = TrackingType.REPS, baseMetOrKcalFactor = 0.20),
        ExerciseEntity(id = 49, name = "«Мёртвый жук»", category = ExerciseCategory.ABS,
            gripType = null, trackingType = TrackingType.REPS, baseMetOrKcalFactor = 0.20),
        ExerciseEntity(id = 50, name = "Наклон к ногам", category = ExerciseCategory.BODYWEIGHT,
            gripType = "Растяжка", trackingType = TrackingType.DURATION, baseMetOrKcalFactor = 2.5),
        ExerciseEntity(id = 51, name = "Разминка плеч", category = ExerciseCategory.BODYWEIGHT,
            gripType = "Разминка", trackingType = TrackingType.REPS, baseMetOrKcalFactor = 0.05),

        // ── Базовый зал — штанга (52–63) ───────────────────────────
        ExerciseEntity(id = 52, name = "Становая тяга", category = ExerciseCategory.BARBELL,
            gripType = null, trackingType = TrackingType.REPS_WEIGHT, baseMetOrKcalFactor = 0.60),
        ExerciseEntity(id = 53, name = "Приседания со штангой", category = ExerciseCategory.BARBELL,
            gripType = null, trackingType = TrackingType.REPS_WEIGHT, baseMetOrKcalFactor = 0.50),
        ExerciseEntity(id = 54, name = "Жим лёжа со штангой", category = ExerciseCategory.BARBELL,
            gripType = null, trackingType = TrackingType.REPS_WEIGHT, baseMetOrKcalFactor = 0.45),
        ExerciseEntity(id = 55, name = "Жим стоя (армейский)", category = ExerciseCategory.BARBELL,
            gripType = null, trackingType = TrackingType.REPS_WEIGHT, baseMetOrKcalFactor = 0.40),
        ExerciseEntity(id = 56, name = "Тяга штанги в наклоне", category = ExerciseCategory.BARBELL,
            gripType = null, trackingType = TrackingType.REPS_WEIGHT, baseMetOrKcalFactor = 0.40),
        ExerciseEntity(id = 57, name = "Румынская тяга", category = ExerciseCategory.BARBELL,
            gripType = null, trackingType = TrackingType.REPS_WEIGHT, baseMetOrKcalFactor = 0.50),
        ExerciseEntity(id = 58, name = "Фронтальные приседания", category = ExerciseCategory.BARBELL,
            gripType = null, trackingType = TrackingType.REPS_WEIGHT, baseMetOrKcalFactor = 0.48),
        ExerciseEntity(id = 59, name = "Болгарские выпады со штангой", category = ExerciseCategory.BARBELL,
            gripType = null, trackingType = TrackingType.REPS_WEIGHT, baseMetOrKcalFactor = 0.42),
        ExerciseEntity(id = 60, name = "Шраги со штангой", category = ExerciseCategory.BARBELL,
            gripType = null, trackingType = TrackingType.REPS_WEIGHT, baseMetOrKcalFactor = 0.20),
        ExerciseEntity(id = 61, name = "Тяга штанги к подбородку", category = ExerciseCategory.BARBELL,
            gripType = null, trackingType = TrackingType.REPS_WEIGHT, baseMetOrKcalFactor = 0.30),
        ExerciseEntity(id = 62, name = "Ягодичный мост со штангой", category = ExerciseCategory.BARBELL,
            gripType = null, trackingType = TrackingType.REPS_WEIGHT, baseMetOrKcalFactor = 0.40),
        ExerciseEntity(id = 63, name = "Наклон со штангой", category = ExerciseCategory.BARBELL,
            gripType = "Good Morning", trackingType = TrackingType.REPS_WEIGHT, baseMetOrKcalFactor = 0.35),

        // ── Расширение: пресс, скамья, зал и рельеф (64–85) ─────────
        ExerciseEntity(id = 64, name = "Подъём корпуса на наклонной скамье", category = ExerciseCategory.ABS,
            gripType = "Наклонная скамья", trackingType = TrackingType.REPS, baseMetOrKcalFactor = 0.35),
        ExerciseEntity(id = 65, name = "Русский твист", category = ExerciseCategory.ABS,
            gripType = "Вращения корпуса", trackingType = TrackingType.REPS, baseMetOrKcalFactor = 0.28),
        ExerciseEntity(id = 66, name = "Вакуум живота", category = ExerciseCategory.ABS,
            gripType = "Статика", trackingType = TrackingType.DURATION, baseMetOrKcalFactor = 3.0),
        ExerciseEntity(id = 67, name = "Складка (книжка)", category = ExerciseCategory.ABS,
            gripType = "V-образные", trackingType = TrackingType.REPS, baseMetOrKcalFactor = 0.38),
        ExerciseEntity(id = 68, name = "Боковые скручивания", category = ExerciseCategory.ABS,
            gripType = null, trackingType = TrackingType.REPS, baseMetOrKcalFactor = 0.26),
        ExerciseEntity(id = 69, name = "Жим штанги на наклонной скамье", category = ExerciseCategory.BARBELL,
            gripType = "Угол 30–45°", trackingType = TrackingType.REPS_WEIGHT, baseMetOrKcalFactor = 0.45),
        ExerciseEntity(id = 70, name = "Жим штанги узким хватом", category = ExerciseCategory.BARBELL,
            gripType = "Узкий (трицепс)", trackingType = TrackingType.REPS_WEIGHT, baseMetOrKcalFactor = 0.42),
        ExerciseEntity(id = 71, name = "Французский жим со штангой", category = ExerciseCategory.BARBELL,
            gripType = "EZ-гриф / прямой", trackingType = TrackingType.REPS_WEIGHT, baseMetOrKcalFactor = 0.32),
        ExerciseEntity(id = 72, name = "Подъём штанги на бицепс", category = ExerciseCategory.BARBELL,
            gripType = "Стоя", trackingType = TrackingType.REPS_WEIGHT, baseMetOrKcalFactor = 0.28),
        ExerciseEntity(id = 73, name = "Подъём на носки со штангой", category = ExerciseCategory.BARBELL,
            gripType = "Икры", trackingType = TrackingType.REPS_WEIGHT, baseMetOrKcalFactor = 0.25),
        ExerciseEntity(id = 74, name = "Разведение гантелей лёжа", category = ExerciseCategory.DUMBBELLS,
            gripType = "На грудь", trackingType = TrackingType.REPS_WEIGHT, baseMetOrKcalFactor = 0.28),
        ExerciseEntity(id = 75, name = "Французский жим с гантелью", category = ExerciseCategory.DUMBBELLS,
            gripType = "Из-за головы", trackingType = TrackingType.REPS_WEIGHT, baseMetOrKcalFactor = 0.25),
        ExerciseEntity(id = 76, name = "Махи гантелями в наклоне", category = ExerciseCategory.DUMBBELLS,
            gripType = "Задняя дельта", trackingType = TrackingType.REPS_WEIGHT, baseMetOrKcalFactor = 0.22),
        ExerciseEntity(id = 77, name = "Жим Арнольда", category = ExerciseCategory.DUMBBELLS,
            gripType = "С разворотом кистей", trackingType = TrackingType.REPS_WEIGHT, baseMetOrKcalFactor = 0.32),
        ExerciseEntity(id = 78, name = "Пуловер с гантелью", category = ExerciseCategory.DUMBBELLS,
            gripType = "Лёжа на скамье", trackingType = TrackingType.REPS_WEIGHT, baseMetOrKcalFactor = 0.30),
        ExerciseEntity(id = 79, name = "Шраги с гантелями", category = ExerciseCategory.DUMBBELLS,
            gripType = "Стоя", trackingType = TrackingType.REPS_WEIGHT, baseMetOrKcalFactor = 0.20),
        ExerciseEntity(id = 80, name = "Подтягивания параллельным хватом", category = ExerciseCategory.PULL_UP_BAR,
            gripType = "Параллельный / нейтральный", trackingType = TrackingType.REPS, baseMetOrKcalFactor = 0.60),
        ExerciseEntity(id = 81, name = "Подъём коленей в висе", category = ExerciseCategory.PULL_UP_BAR,
            gripType = "На турнике", trackingType = TrackingType.REPS, baseMetOrKcalFactor = 0.32),
        ExerciseEntity(id = 82, name = "Приседания с выпрыгиванием", category = ExerciseCategory.BODYWEIGHT,
            gripType = "Плиометрика", trackingType = TrackingType.REPS, baseMetOrKcalFactor = 0.50),
        ExerciseEntity(id = 83, name = "Стульчик у стены", category = ExerciseCategory.BODYWEIGHT,
            gripType = "Статика", trackingType = TrackingType.DURATION, baseMetOrKcalFactor = 4.0),
        ExerciseEntity(id = 84, name = "Подъём на носки на одной ноге", category = ExerciseCategory.BODYWEIGHT,
            gripType = "Свой вес", trackingType = TrackingType.REPS, baseMetOrKcalFactor = 0.20),
        ExerciseEntity(id = 85, name = "Выпады назад", category = ExerciseCategory.BODYWEIGHT,
            gripType = "Свой вес", trackingType = TrackingType.REPS, baseMetOrKcalFactor = 0.35)
    )

    val idToCategory: Map<Long, ExerciseCategory> = all.associate { it.id to it.category }

    /** Пакеты упражнений, доступные для импорта в справочник. */
    val packages: List<ExercisePackage> = listOf(
        ExercisePackage(
            id = "workout",
            title = "Воркаут и турники",
            description = "Подтягивания всех уровней, отжимания с хлопком, стойка, берпи, пистолетик",
            exerciseIds = (27L..41L).toList()
        ),
        ExercisePackage(
            id = "back",
            title = "Здоровая спина и мобильность",
            description = "Мягкая разминка и укрепление спины и кора: кошка-собака, мостик, лодочка, боковая планка",
            exerciseIds = (42L..51L).toList()
        ),
        ExercisePackage(
            id = "barbell",
            title = "Базовый зал — штанга",
            description = "Главные движения со штангой. В поле веса указывай общий вес штанги",
            exerciseIds = (52L..63L).toList()
        ),
        ExercisePackage(
            id = "advanced_gym",
            title = "Зал, рельеф и наклонный пресс",
            description = "Подъём корпуса на наклонной скамье, жимы под углом, бицепс, трицепс и дельты",
            exerciseIds = (64L..85L).toList()
        )
    )

    /** Упражнения конкретного пакета. */
    fun exercisesForPackage(packageId: String): List<ExerciseEntity> =
        packages.firstOrNull { it.id == packageId }
            ?.let { pack -> all.filter { it.id in pack.exerciseIds } }
            ?: emptyList()
}
