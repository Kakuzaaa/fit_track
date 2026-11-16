package com.example.fitnesstracker.domain.model

/**
 * Режим темы приложения.
 */
enum class ThemeMode(val label: String) {
    SYSTEM("Системная"),
    LIGHT("Светлая"),
    DARK("Тёмная")
}

/**
 * Настройки пользователя, хранящиеся в DataStore.
 */
data class UserSettings(
    val bodyWeightKg: Double,
    val themeMode: ThemeMode,
    /** Дневная цель по калориям; 0 — цель не задана (функция выключена). */
    val dailyGoalKcal: Int = 0,
    /** Отдых между подходами (таймер после сохранения); 0 — выключен. */
    val restTimerSeconds: Int = 0
) {
    companion object {
        const val DEFAULT_BODY_WEIGHT_KG = 75.0
        const val DEFAULT_DAILY_GOAL_KCAL = 0
        const val DEFAULT_REST_TIMER_SECONDS = 0
        val DEFAULT = UserSettings(
            bodyWeightKg = DEFAULT_BODY_WEIGHT_KG,
            themeMode = ThemeMode.SYSTEM,
            dailyGoalKcal = DEFAULT_DAILY_GOAL_KCAL,
            restTimerSeconds = DEFAULT_REST_TIMER_SECONDS
        )
    }
}
