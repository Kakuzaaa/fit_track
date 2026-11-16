package com.example.fitnesstracker.data.mapper

import com.example.fitnesstracker.data.local.dao.DayStatsRow
import com.example.fitnesstracker.data.local.entity.ExerciseEntity
import com.example.fitnesstracker.data.local.entity.WorkoutLogEntity
import com.example.fitnesstracker.domain.model.DayStats
import com.example.fitnesstracker.domain.model.Exercise
import com.example.fitnesstracker.domain.model.WorkoutLog

fun ExerciseEntity.toDomain(): Exercise = Exercise(
    id = id,
    name = name,
    category = category,
    gripType = gripType,
    trackingType = trackingType,
    baseMetOrKcalFactor = baseMetOrKcalFactor,
    isCustom = isCustom
)

fun Exercise.toEntity(): ExerciseEntity = ExerciseEntity(
    id = id,
    name = name,
    category = category,
    gripType = gripType,
    trackingType = trackingType,
    baseMetOrKcalFactor = baseMetOrKcalFactor,
    isCustom = isCustom
)

fun WorkoutLogEntity.toDomain(): WorkoutLog = WorkoutLog(
    id = id,
    exerciseId = exerciseId,
    exerciseNameSnapshot = exerciseNameSnapshot,
    dateEpochDay = dateEpochDay,
    timestamp = timestamp,
    reps = reps,
    weightKg = weightKg,
    durationMinutes = durationMinutes,
    speedKmh = speedKmh,
    distanceKm = distanceKm,
    caloriesBurned = caloriesBurned
)

fun WorkoutLog.toEntity(): WorkoutLogEntity = WorkoutLogEntity(
    id = id,
    exerciseId = exerciseId,
    exerciseNameSnapshot = exerciseNameSnapshot,
    dateEpochDay = dateEpochDay,
    timestamp = timestamp,
    reps = reps,
    weightKg = weightKg,
    durationMinutes = durationMinutes,
    speedKmh = speedKmh,
    distanceKm = distanceKm,
    caloriesBurned = caloriesBurned
)

fun DayStatsRow.toDomain(): DayStats = DayStats(
    dateEpochDay = dateEpochDay,
    totalCalories = totalCalories,
    totalReps = totalReps,
    entries = entries
)
