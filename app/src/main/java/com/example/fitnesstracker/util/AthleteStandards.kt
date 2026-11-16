package com.example.fitnesstracker.util

/**
 * Уровни атлета (идея Strength Level, офлайн-версия): пороги заданы
 * для базовых движений с собственным весом и статики.
 */
enum class AthleteLevel(val label: String) {
    NEWBIE("Новичок"),
    AVERAGE("Средний"),
    ADVANCED("Продвинутый"),
    ELITE("Элита")
}

/**
 * Пороги уровня для одного упражнения.
 *
 * @param inSeconds true — значение считается в секундах (статика),
 *                  false — в повторениях за подход.
 * @param thresholds три порога по возрастанию: [Средний, Продвинутый, Элита];
 *                  значение ниже первого порога — «Новичок».
 *                  Пороги ориентировочные, для атлета ~75 кг.
 */
data class ExerciseStandard(
    val inSeconds: Boolean,
    val thresholds: List<Int>
)

/**
 * Офлайн-таблица нормативов:exerciseId → пороги. Без миграции БД,
 * уровень считается на лету из логов и текущего ввода.
 */
object AthleteStandards {

    private val standards: Map<Long, ExerciseStandard> = mapOf(
        // Свой вес
        1L to ExerciseStandard(false, listOf(25, 45, 70)),   // Отжимания классические
        2L to ExerciseStandard(false, listOf(20, 35, 55)),   // Отжимания широкие
        3L to ExerciseStandard(false, listOf(15, 30, 45)),   // Алмазные
        4L to ExerciseStandard(false, listOf(12, 22, 35)),   // Брусья
        5L to ExerciseStandard(false, listOf(40, 70, 110)),  // Приседания классические
        6L to ExerciseStandard(false, listOf(40, 70, 110)),  // Приседания сумо
        7L to ExerciseStandard(false, listOf(25, 45, 70)),   // Выпады
        // Турник
        8L to ExerciseStandard(false, listOf(6, 12, 20)),    // Подтягивания прямые
        9L to ExerciseStandard(false, listOf(6, 12, 18)),    // Обратные
        10L to ExerciseStandard(false, listOf(5, 10, 15)),   // Широкие
        11L to ExerciseStandard(false, listOf(6, 12, 20)),   // Нейтральные
        12L to ExerciseStandard(false, listOf(5, 10, 16)),   // Узкие
        // Пресс
        21L to ExerciseStandard(false, listOf(30, 50, 75)),  // Скручивания
        22L to ExerciseStandard(false, listOf(12, 20, 30)),  // Подъём ног
        23L to ExerciseStandard(false, listOf(40, 70, 110)), // Велосипед
        24L to ExerciseStandard(true, listOf(60, 120, 210)), // Планка
        // Пакет «Воркаут и турники»
        27L to ExerciseStandard(false, listOf(12, 22, 35)),  // Австралийские
        28L to ExerciseStandard(false, listOf(6, 10, 15)),   // Негативные
        29L to ExerciseStandard(false, listOf(4, 8, 14)),    // Арчера
        30L to ExerciseStandard(false, listOf(2, 5, 10)),    // С хлопком
        31L to ExerciseStandard(false, listOf(1, 3, 6)),     // Выход силой
        32L to ExerciseStandard(false, listOf(6, 12, 20)),   // Подъём туловища к перекладине
        33L to ExerciseStandard(true, listOf(45, 90, 150)),  // Вис на турнике
        34L to ExerciseStandard(false, listOf(5, 10, 16)),   // Кольца
        35L to ExerciseStandard(true, listOf(20, 45, 90)),   // Уголок на брусьях
        36L to ExerciseStandard(false, listOf(25, 40, 60)),  // Отжимания с колен
        37L to ExerciseStandard(false, listOf(6, 12, 20)),   // Отжимания с хлопком
        38L to ExerciseStandard(false, listOf(4, 8, 14)),    // Отжимания в стойке
        40L to ExerciseStandard(false, listOf(15, 30, 45)),  // Берпи
        41L to ExerciseStandard(false, listOf(3, 8, 15)),    // Пистолетик
        // Пакет «Здоровая спина»
        43L to ExerciseStandard(false, listOf(20, 35, 50)),  // Ягодичный мостик
        44L to ExerciseStandard(false, listOf(25, 40, 60)),  // Супермен
        45L to ExerciseStandard(true, listOf(30, 60, 120)),  // Лодочка
        46L to ExerciseStandard(true, listOf(40, 90, 180)),  // Боковая планка
        47L to ExerciseStandard(false, listOf(25, 40, 60)),  // Гиперэкстензия
        48L to ExerciseStandard(false, listOf(20, 35, 55))   // Мёртвый жук
    )

    fun hasStandard(exerciseId: Long): Boolean = standards.containsKey(exerciseId)

    /** Уровень по текущему значению; null — если упражнение вне нормативов или значение пустое. */
    fun levelFor(exerciseId: Long, value: Float): AthleteLevel? {
        val standard = standards[exerciseId] ?: return null
        if (value <= 0f) return null
        val t = standard.thresholds
        return when {
            value >= t[2] -> AthleteLevel.ELITE
            value >= t[1] -> AthleteLevel.ADVANCED
            value >= t[0] -> AthleteLevel.AVERAGE
            else -> AthleteLevel.NEWBIE
        }
    }

    /** Уровень по лучшему результату за всю историю. */
    fun levelForStats(exerciseId: Long, bestReps: Int, bestSeconds: Int): AthleteLevel? {
        val standard = standards[exerciseId] ?: return null
        return levelFor(exerciseId, if (standard.inSeconds) bestSeconds.toFloat() else bestReps.toFloat())
    }
}
