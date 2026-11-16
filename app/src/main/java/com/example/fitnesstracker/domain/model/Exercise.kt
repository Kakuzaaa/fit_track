package com.example.fitnesstracker.domain.model

/**
 * Категории упражнений по используемому инвентарю.
 * ⚠️ Хранятся в Room как ordinal — новые значения добавлять ТОЛЬКО в конец.
 */
enum class ExerciseCategory(val label: String) {
    BODYWEIGHT("Свой вес"),
    PULL_UP_BAR("Турник"),
    DUMBBELLS("Гантели"),
    ABS("Пресс"),
    CARDIO("Кардио"),
    BARBELL("Штанга")
}

/**
 * Что именно учитывается при записи подхода.
 */
enum class TrackingType(val label: String) {
    REPS("Повторения"),
    REPS_WEIGHT("Повторения и вес"),
    CARDIO_SPEED("Время и скорость"),
    DURATION("Время")
}

/**
 * Доменная модель упражнения из справочника.
 *
 * @param baseMetOrKcalFactor коэффициент энергоёмкости:
 *        для силовых — ккал за 1 повторение у человека весом 75 кг,
 *        для статики (планка) — MET,
 *        для кардио — не используется (MET считается по скорости).
 */
data class Exercise(
    val id: Long,
    val name: String,
    val category: ExerciseCategory,
    val gripType: String?,
    val trackingType: TrackingType,
    val baseMetOrKcalFactor: Double,
    val isCustom: Boolean = false
) {
    /** Полное имя с уточнением хвата, например «Подтягивания · Обратный». */
    val displayName: String
        get() = if (gripType.isNullOrBlank()) name else "$name · $gripType"
}
