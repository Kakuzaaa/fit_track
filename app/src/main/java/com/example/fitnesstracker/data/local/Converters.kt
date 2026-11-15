package com.example.fitnesstracker.data.local

import androidx.room.TypeConverter
import com.example.fitnesstracker.domain.model.ExerciseCategory
import com.example.fitnesstracker.domain.model.TrackingType

/**
 * Конвертеры enum-ов для Room. Порядковое хранение даёт правильную
 * сортировку `ORDER BY category` согласно порядку объявления enum-а.
 */
class Converters {

    @TypeConverter
    fun categoryToOrdinal(category: ExerciseCategory): Int = category.ordinal

    @TypeConverter
    fun ordinalToCategory(ordinal: Int): ExerciseCategory = ExerciseCategory.entries[ordinal]

    @TypeConverter
    fun trackingTypeToOrdinal(trackingType: TrackingType): Int = trackingType.ordinal

    @TypeConverter
    fun ordinalToTrackingType(ordinal: Int): TrackingType = TrackingType.entries[ordinal]
}
