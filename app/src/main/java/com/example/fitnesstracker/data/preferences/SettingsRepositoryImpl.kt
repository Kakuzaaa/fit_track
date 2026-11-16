package com.example.fitnesstracker.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.fitnesstracker.domain.model.ThemeMode
import com.example.fitnesstracker.domain.model.UserSettings
import com.example.fitnesstracker.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userSettingsDataStore by preferencesDataStore(name = "user_settings")

/**
 * Реализация настроек пользователя поверх Jetpack DataStore.
 */
class SettingsRepositoryImpl(private val context: Context) : SettingsRepository {

    private object Keys {
        val BODY_WEIGHT = doublePreferencesKey("body_weight_kg")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DAILY_GOAL = intPreferencesKey("daily_goal_kcal")
    }

    override val settings: Flow<UserSettings> = context.userSettingsDataStore.data.map { prefs ->
        UserSettings(
            bodyWeightKg = prefs[Keys.BODY_WEIGHT] ?: UserSettings.DEFAULT_BODY_WEIGHT_KG,
            themeMode = prefs[Keys.THEME_MODE]?.let { stored ->
                ThemeMode.entries.firstOrNull { it.name == stored }
            } ?: ThemeMode.SYSTEM,
            dailyGoalKcal = prefs[Keys.DAILY_GOAL] ?: UserSettings.DEFAULT_DAILY_GOAL_KCAL
        )
    }

    override suspend fun setBodyWeight(weightKg: Double) {
        context.userSettingsDataStore.edit { prefs ->
            prefs[Keys.BODY_WEIGHT] = weightKg
        }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        context.userSettingsDataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.name
        }
    }

    override suspend fun setDailyGoalKcal(goalKcal: Int) {
        context.userSettingsDataStore.edit { prefs ->
            prefs[Keys.DAILY_GOAL] = goalKcal.coerceIn(0, 10_000)
        }
    }
}
