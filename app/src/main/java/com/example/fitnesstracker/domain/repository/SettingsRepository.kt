package com.example.fitnesstracker.domain.repository

import com.example.fitnesstracker.domain.model.ThemeMode
import com.example.fitnesstracker.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

/**
 * Доступ к настройкам пользователя (DataStore).
 */
interface SettingsRepository {
    val settings: Flow<UserSettings>

    suspend fun setBodyWeight(weightKg: Double)

    suspend fun setThemeMode(mode: ThemeMode)

    suspend fun setDailyGoalKcal(goalKcal: Int)

    /** Длительность таймера отдыха после подхода; 0 — выключен. */
    suspend fun setRestTimerSeconds(seconds: Int)
}
