package com.example.fitnesstracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.fitnesstracker.domain.model.ExerciseCategory
import com.example.fitnesstracker.domain.model.TrackingType

/**
 * Справочник упражнений.
 */
@Entity(tableName = "exercises", indices = [Index("category")])
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val category: ExerciseCategory,
    /** Хват или вариация («Прямой», «Обратный», «Нейтральный / Молотковый» и т.п.). */
    val gripType: String?,
    val trackingType: TrackingType,
    /**
     * Коэффициент энергоёмкости:
     * для силовых — ккал за 1 повторение при весе 75 кг,
     * для статики — MET, для кардио — 0 (MET считается по скорости).
     */
    val baseMetOrKcalFactor: Double,
    val isCustom: Boolean = false
)
