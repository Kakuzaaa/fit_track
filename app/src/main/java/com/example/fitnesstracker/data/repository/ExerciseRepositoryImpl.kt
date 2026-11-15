package com.example.fitnesstracker.data.repository

import com.example.fitnesstracker.data.local.dao.ExerciseDao
import com.example.fitnesstracker.data.local.entity.ExerciseEntity
import com.example.fitnesstracker.data.mapper.toDomain
import com.example.fitnesstracker.data.mapper.toEntity
import com.example.fitnesstracker.domain.model.Exercise
import com.example.fitnesstracker.domain.model.ExerciseCategory
import com.example.fitnesstracker.domain.model.TrackingType
import com.example.fitnesstracker.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExerciseRepositoryImpl(private val dao: ExerciseDao) : ExerciseRepository {

    override fun observeExercises(): Flow<List<Exercise>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getExercise(id: Long): Exercise? =
        dao.getById(id)?.toDomain()

    override suspend fun createCustomExercise(
        name: String,
        category: ExerciseCategory,
        gripType: String?,
        trackingType: TrackingType
    ): Long {
        // Разумные значения энергоёмкости по умолчанию для произвольного упражнения.
        val baseFactor = when (trackingType) {
            TrackingType.REPS -> 0.35
            TrackingType.REPS_WEIGHT -> 0.30
            TrackingType.DURATION -> 3.8
            TrackingType.CARDIO_SPEED -> 0.0
        }
        val entity = ExerciseEntity(
            name = name.trim(),
            category = category,
            gripType = gripType?.trim()?.takeIf { it.isNotEmpty() },
            trackingType = trackingType,
            baseMetOrKcalFactor = baseFactor,
            isCustom = true
        )
        return dao.insert(entity)
    }

    override suspend fun deleteCustomExercise(exercise: Exercise) {
        dao.delete(exercise.toEntity())
    }
}
