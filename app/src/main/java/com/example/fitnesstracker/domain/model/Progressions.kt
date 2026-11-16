package com.example.fitnesstracker.domain.model

/**
 * Цепочка прогрессии калистеники: список id упражнений от простого к сложному
 * (идея Hybrid Calisthenics). Хранится в коде — без миграции БД.
 */
data class ProgressionChain(
    val title: String,
    val orderedIds: List<Long>
) {
    /** Следующий (более сложный) уровень после [exerciseId], null если конец цепочки. */
    fun nextAfter(exerciseId: Long): Long? {
        val index = orderedIds.indexOf(exerciseId)
        return if (index >= 0) orderedIds.getOrNull(index + 1) else null
    }
}

object ExerciseProgressions {

    val chains: List<ProgressionChain> = listOf(
        ProgressionChain(
            title = "Отжимания",
            orderedIds = listOf(
                36L, // с колен
                1L,  // классические
                3L,  // алмазные
                37L, // с хлопком
                38L  // в стойке на руках
            )
        ),
        ProgressionChain(
            title = "Подтягивания",
            orderedIds = listOf(
                27L, // австралийские
                28L, // негативные
                8L,  // прямые средние
                29L, // архера
                30L, // с хлопком
                31L  // выход силой
            )
        ),
        ProgressionChain(
            title = "Приседания",
            orderedIds = listOf(
                5L,  // классические
                6L,  // сумо
                41L  // пистолетик
            )
        ),
        ProgressionChain(
            title = "Пресс",
            orderedIds = listOf(
                21L, // скручивания
                22L, // подъём ног
                23L  // велосипед
            )
        ),
        ProgressionChain(
            title = "Планка",
            orderedIds = listOf(
                24L, // классическая
                47L  // боковая
            )
        )
    )

    /** Цепочка, содержащая упражнение [exerciseId]. */
    fun chainFor(exerciseId: Long): ProgressionChain? =
        chains.firstOrNull { exerciseId in it.orderedIds }
}
