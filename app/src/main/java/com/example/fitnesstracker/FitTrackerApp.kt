package com.example.fitnesstracker

import android.app.Application
import android.content.Context
import com.example.fitnesstracker.data.local.AppDatabase
import com.example.fitnesstracker.data.preferences.SettingsRepositoryImpl
import com.example.fitnesstracker.data.repository.ExerciseRepositoryImpl
import com.example.fitnesstracker.data.repository.WeightRepositoryImpl
import com.example.fitnesstracker.data.repository.WorkoutRepositoryImpl
import com.example.fitnesstracker.domain.repository.ExerciseRepository
import com.example.fitnesstracker.domain.repository.SettingsRepository
import com.example.fitnesstracker.domain.repository.WeightRepository
import com.example.fitnesstracker.domain.repository.WorkoutRepository
import com.example.fitnesstracker.util.AppUpdateManager

/**
 * Класс приложения: создаёт общий DI-контейнер.
 */
class FitTrackerApp : Application() {

    val container: AppContainer by lazy { AppContainer(this) }
}

/**
 * Простой ручной DI-контейнер (без внешних библиотек), живёт весь
 * срок работы процесса. UI -> ViewModel -> Repository -> Room DAO.
 */
class AppContainer(context: Context) {

    private val database: AppDatabase = AppDatabase.get(context)

    val exerciseRepository: ExerciseRepository = ExerciseRepositoryImpl(database.exerciseDao())

    val workoutRepository: WorkoutRepository = WorkoutRepositoryImpl(
        dao = database.workoutLogDao(),
        templateDao = database.workoutTemplateDao(),
        exerciseDao = database.exerciseDao()
    )

    val weightRepository: WeightRepository = WeightRepositoryImpl(database.weightLogDao())

    val settingsRepository: SettingsRepository = SettingsRepositoryImpl(context)

    val appUpdateManager: AppUpdateManager = AppUpdateManager(context.applicationContext)
}
