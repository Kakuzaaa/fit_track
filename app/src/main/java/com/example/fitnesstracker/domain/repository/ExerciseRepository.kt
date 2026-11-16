package com.example.fitnesstracker.domain.repository

import com.example.fitnesstracker.domain.model.Exercise
import com.example.fitnesstracker.domain.model.ExerciseCategory
import com.example.fitnesstracker.domain.model.ExercisePackage
import com.example.fitnesstracker.domain.model.TrackingType
import kotlinx.coroutines.flow.Flow

/**
 * Доступ к справочнику упражнений.
 */
interface ExerciseRepository {

    /** Доступные для импорта пакеты упражнений. */
    val packages: List<ExercisePackage>

    fun observeExercises(): Flow<List<Exercise>>

    suspend fun getExercise(id: Long): Exercise?

    /** Создание пользовательского упражнения; возвращает id новой записи. */
    suspend fun createCustomExercise(
        name: String,
        category: ExerciseCategory,
        gripType: String?,
        trackingType: TrackingType
    ): Long

    /**
     * Импорт пакета упражнений в справочник (отсутствующие строки).
     * Возвращает число добавленных упражнений.
     */
    suspend fun importExercisePackage(packageId: String): Int

    suspend fun deleteCustomExercise(exercise: Exercise)
}
