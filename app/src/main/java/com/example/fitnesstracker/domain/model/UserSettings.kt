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
    val themeMode: ThemeMode
) {
    companion object {
        const val DEFAULT_BODY_WEIGHT_KG = 75.0
        val DEFAULT = UserSettings(DEFAULT_BODY_WEIGHT_KG, ThemeMode.SYSTEM)
    }
}
